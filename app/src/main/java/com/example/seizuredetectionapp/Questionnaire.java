package com.example.seizuredetectionapp;

import androidx.annotation.NonNull;

import java.util.ArrayList;

public class Questionnaire {
    private static String name;
    private static ArrayList<String> contactList;
    private static String contactMethod;
    private static String countdownTimer;
    private static String age;

    public Questionnaire(){

    }

    public Questionnaire(String name, ArrayList<String> contactList, String countdownTimer, String age, String contactMethod){
        this.name = name;
        this.contactList = contactList;
        this.countdownTimer = countdownTimer;
        this.age = age;
        this.contactMethod = contactMethod;
    }

    @NonNull
    @Override
    public String toString() {
        return ' ' + contactMethod + ' ' + age + ' ' + countdownTimer;
    }
}
