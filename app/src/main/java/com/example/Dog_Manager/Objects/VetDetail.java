package com.example.Dog_Manager.Objects;

public class VetDetail {
    private String name;
    private String location;
    private String lastVisitDate;
    private String nextVisitScheduled;

    public VetDetail(String name, String location, String lastVisitDate, String nextVisitScheduled) {
        this.name = name;
        this.location = location;
        this.lastVisitDate = lastVisitDate;
        this.nextVisitScheduled = nextVisitScheduled;
    }

    // Getters
    public String getName() { return name; }
    public String getLocation() { return location; }
    public String getLastVisitDate() { return lastVisitDate; }
    public String getNextVisitScheduled() { return nextVisitScheduled; }
}