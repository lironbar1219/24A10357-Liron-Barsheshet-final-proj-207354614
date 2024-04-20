package com.example.Dog_Manager.Objects;

import android.util.Log;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Map;

public class Dog {
    private Map<String, String> dogData;
    private Map<String, String> vetData;
    private ArrayList<Map<String, String>> vaccinesData;
    private String imageUrl;

    public Map<String, String> getDogData() {
        return dogData;
    }

    public Dog setDogData(Map<String, String> dogData) {
        this.dogData = dogData;
        return this;
    }

    public Map<String, String> getVetData() {
        return vetData;
    }

    public Dog setVetData(Map<String, String> vetData) {
        this.vetData = vetData;
        return this;
    }

    public ArrayList<Map<String, String>> getVaccinesData() {
        return vaccinesData;
    }

    public Dog setVaccinesData(ArrayList<Map<String, String>> vaccinesData) {
        this.vaccinesData = vaccinesData;
        return this;
    }

    // Constructor
    public Dog(Map<String, String> dogData, Map<String, String> vetData, ArrayList<Map<String, String>> vaccinesData, String imageUrl) {
        this.dogData = dogData;
        this.vetData = vetData;
        this.vaccinesData = vaccinesData;
        this.imageUrl = imageUrl;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }



    public String toString() {
        return "Dog{" +
                "dogData=" + dogData +
                ", vetData=" + vetData +
                ", vaccinesData=" + vaccinesData +
                ", imageUrl=" + imageUrl + " }";
    }

    public int getDaysToBirthday() {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDate nowDate = LocalDate.now();
            LocalDate dob = LocalDate.parse(dogData.get("dateOfBirth"), formatter);
            LocalDate nextBirthday = dob.withYear(nowDate.getYear());
            // If the birthday has already passed this year, add 1 to the year
            if (nextBirthday.isBefore(nowDate) || nextBirthday.isEqual(nowDate)) {
                nextBirthday = nextBirthday.plusYears(1);
            }
            return (int) ChronoUnit.DAYS.between(nowDate, nextBirthday);
        } catch (Exception e) {
            Log.e("Dog", "Failed to calculate days to birthday", e);
            return -1;
        }
    }
}
