package com.example.Dog_Manager.Objects;

public class VaccineDetail {
    private String kind;
    private String dateTook;
    private String nextDateToBeTaken;

    public VaccineDetail(String kind, String dateTook, String nextDateToBeTaken) {
        this.kind = kind;
        this.dateTook = dateTook;
        this.nextDateToBeTaken = nextDateToBeTaken;
    }

    // Getters
    public String getKind() { return kind; }
    public String getDateTook() { return dateTook; }
    public String getNextDateToBeTaken() { return nextDateToBeTaken; }
}
