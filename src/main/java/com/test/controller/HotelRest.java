package com.test.controller;

import com.fw.annotations.AnnotationController;
import com.fw.annotations.ManageUrl;
import com.fw.annotations.JSON;
import com.test.model.Hotel;
import com.test.repository.HotelRepository;

@AnnotationController("/api/hotels")
public class HotelRest {

    private final HotelRepository repo = new HotelRepository();

    @ManageUrl("/{id}")
    @JSON
    public Hotel getHotelById(int id) {
        return repo.findById(id);
    }
}
