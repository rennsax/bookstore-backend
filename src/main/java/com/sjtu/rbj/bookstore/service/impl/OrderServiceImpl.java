package com.sjtu.rbj.bookstore.service.impl;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sjtu.rbj.bookstore.constant.OrderState;
import com.sjtu.rbj.bookstore.dao.BookDao;
import com.sjtu.rbj.bookstore.dao.OrderDao;
import com.sjtu.rbj.bookstore.dto.OrderInfoDTO;
import com.sjtu.rbj.bookstore.entity.Book;
import com.sjtu.rbj.bookstore.entity.Order;
import com.sjtu.rbj.bookstore.entity.Order.OrderItem;
import com.sjtu.rbj.bookstore.service.OrderService;

/**
 * @author Bojun Ren
 * @data 2023/04/28
 */
@Service
public class OrderServiceImpl implements OrderService {
    @Autowired
    private OrderDao orderDao;

    @Autowired
    private BookDao bookDao;

    @Override
    public OrderInfoDTO getOrderInfoByOrderId(Integer orderId) {
        Optional<Order> maybeOrder = orderDao.findById(orderId);
        Order order = maybeOrder.orElseThrow(() -> new NoSuchElementException("no such order!"));
        return OrderInfoDTO.from(order);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submitOrder(Integer orderId) {
        Optional<Order> maybeOrder = orderDao.findById(orderId);
        Order order = maybeOrder.orElseThrow(() -> new NoSuchElementException());
        if (order.getState() != OrderState.PENDING) {
            throw new UnsupportedOperationException();
        }
        order.setState(OrderState.TRANSPORTING);
        order.setTime(Timestamp.from(Instant.now()));
    }

    @Override
    public List<Order> getOrderByUserId(Integer userId) {
        List<Order> orderList = orderDao.findByUserId(userId);
        orderList.sort((o1, o2) -> {
            return o1.getTime().compareTo(o2.getTime());
        });
        List<Order> res = new ArrayList<>();
        for (Order order : orderList) {
            if (order.getState() == OrderState.PENDING) {
                continue;
            }
            res.add(order);
        }
        return res;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateOrder(Integer orderId, UUID uuid, Integer quantity) {
        if (orderId == null || uuid == null || quantity == null) {
            throw new IllegalArgumentException("null parameters are not permitted!");
        }
        Optional<Order> maybeOrder = orderDao.findById(orderId);
        Order order = maybeOrder.orElseThrow(() -> new NoSuchElementException("no such order!"));
        if (order.getState() != OrderState.PENDING) {
            throw new UnsupportedOperationException("Only \"pending\" orders can be updated!");
        }

        Optional<Book> maybeBook = bookDao.findByUuid(uuid);
        Book targetBook = maybeBook.orElseThrow(() -> new NoSuchElementException("No such book!"));

        /** Whether the order already contains the target book. */
        final List<OrderItem> orderItemList = order.getOrderItemList();
        OrderItem targetOrderItem = null;
        for (OrderItem orderItem : orderItemList) {
            if (orderItem.getBook() == targetBook) {
                targetOrderItem = orderItem;
                break;
            }
        }

        if (targetOrderItem == null) {
            if (quantity <= 0) {
                return false;
            }
            targetOrderItem = new OrderItem();
            targetOrderItem.setBook(targetBook);
            targetOrderItem.setQuantity(quantity);
            order.addOrderItem(targetOrderItem);
            return true;
        }
        int afterQuantity = targetOrderItem.getQuantity() + quantity;
        if (afterQuantity < 0) {
            return false;
        }
        if (afterQuantity == 0) {
            order.removeOrderItem(targetOrderItem);
            return true;
        }
        targetOrderItem.setQuantity(afterQuantity);
        return true;
    }

}
