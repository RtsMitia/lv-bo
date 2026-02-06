package com.test;

import java.util.Arrays;
import java.util.List;

public class Department {
    private String label;
    private Integer floor;
    private Project[] projects;

    public Department() {}

    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }
    public Integer getFloor() { return floor; }
    public void setFloor(Integer floor) { this.floor = floor; }
    public Project[] getProjects() { return projects; }
    public void setProjects(Project[] projects) { this.projects = projects; }

    @Override
    public String toString() {
        return "Department{" +
                "label='" + label + '\'' +
                ", floor=" + floor +
                ", projects=" + (projects == null ? "null" : Arrays.toString(projects)) +
                '}';
    }
}
