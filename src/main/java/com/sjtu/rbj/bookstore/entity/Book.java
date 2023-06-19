package com.sjtu.rbj.bookstore.entity;

import java.io.Serializable;
import java.sql.Date;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.PrePersist;
import javax.persistence.Table;

import org.hibernate.annotations.ColumnTransformer;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * @author Bojun Ren
 * @date 2023/04/08
 */
@Setter
@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "`book`")
public class Book implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(
        columnDefinition = "BINARY(16) DEFAULT(UUID_TO_BIN(UUID()))",
        nullable = false, unique = true
    )
    private UUID uuid;

    @Column(nullable = false)
    private String title;

    @Column(name = "pic_id", unique = true, columnDefinition = "CHAR(15)")
    private String picId;

    @Column(name = "`price`", columnDefinition = "DECIMAL(5, 2)")
    @ColumnTransformer(read = "100*price", write = "? / 100")
    /** Store in unit cent. */
    private Integer priceCent;

    private String author;

    private Integer stock;

    @Column(columnDefinition = "DATE")
    private Date date;

    @Column(unique = true, columnDefinition = "CHAR(13)")
    private String isbn;

    @Lob
    private String description;

    @PrePersist
    void prePersistInitialize() {
        if (uuid == null) {
            uuid = UUID.randomUUID();
        }
    }
}
