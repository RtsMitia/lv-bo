package com.test;

public class Project {
    private String name;
    private String budget;

    public Project() {}

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getBudget() { return budget; }
    public void setBudget(String budget) { this.budget = budget; }

    @Override
    public String toString() {
        return "Project{name='" + name + "', budget='" + budget + "'}";
    }
}
