package com.sjtu.rbj.bookstore.controller;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.sjtu.rbj.bookstore.constant.Constant;


/**
 * @author Bojun Ren
 * @date 2023/04/19
 */
@RestController
public class ImageController {
    @CrossOrigin(Constant.ALLOW_ORIGIN)
    @GetMapping(value = "/images/{picName}", produces = MediaType.IMAGE_JPEG_VALUE)
    @Cacheable("images")
    public byte[] images(@PathVariable String picName) throws IOException {
        ClassPathResource imgFile = new ClassPathResource(Constant.IMAGES_FOLDER + picName);
        InputStream in = imgFile.getInputStream();
        return IOUtils.toByteArray(in);
    }
}