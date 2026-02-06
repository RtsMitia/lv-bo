package com.test;

import com.fw.annotations.AnnotationController;
import com.fw.annotations.ManageUrl;
import java.util.Map;

@AnnotationController("/TestMap")
public class TestMap {
    @ManageUrl("/maptest")
    public String mapTest(Map<String, Object> params) {
        return "Map received: " + params;
    }

    @ManageUrl("/maptest/path/{id}")
    public String mapTestPath(Map<String, Object> params) {
        return "Map with path: " + params;
    }
}
