package com.test.controller;

import com.fw.ModelView;
import com.fw.annotations.AnnotationController;
import com.fw.annotations.ManageUrl;
import com.fw.annotations.MyGET;

@AnnotationController("")
public class HomeController {

    @ManageUrl("/")
    @MyGET
    public ModelView index() {
        ModelView mv = new ModelView("layout.jsp");
        mv.addItem("content", "home/home_index.jsp");
        return mv;
    }

}
