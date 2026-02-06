package com.test;

import com.fw.annotations.AnnotationController;
import com.fw.annotations.ManageUrl;
import com.fw.annotations.RequestParam;

@AnnotationController("/TestRequestParam")
public class TestRequestParam {
    @ManageUrl("/requestparam/insert")
    public String insert(@RequestParam("name") String n, @RequestParam("age") Integer a) {
        return "RequestParam test: name=" + (n == null ? "" : n) + ", age=" + (a == null ? "" : a);
    }

    @ManageUrl("/requestparam/onlyage")
    public String onlyAge(@RequestParam("age") Integer a) {
        return "RequestParam test: only age=" + (a == null ? "" : a);
    }

    @ManageUrl("/requestparam/mixed")
    public String mixed(String name, @RequestParam("age") Integer a, Integer momo) {
        return "RequestParam test: mixed name=" + (name == null ? "" : name) + ", age=" + (a == null ? "" : a) + ", momo=" + (momo == null ? "default" : momo);
    }

    @ManageUrl("/requestparam/list")
    public String listTest(@RequestParam("colors") java.util.List<String> colors) {
        return "RequestParam test: colors=" + (colors == null ? "null" : colors.toString());
    }
}
