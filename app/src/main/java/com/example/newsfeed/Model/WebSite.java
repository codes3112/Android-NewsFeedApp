package com.example.newsfeed.Model;

import java.util.ArrayList;

public class WebSite {

    private String status;
    private ArrayList<Source> sources;

    //empty constructor
    public WebSite() {
    }

    //constructor
    public WebSite(String status, ArrayList<Source> sources) {
        this.status = status;
        this.sources = sources;
    }

    //getter and setter
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public ArrayList<Source> getSources() {
        return sources;
    }

    public void setSources(ArrayList<Source> sources) {
        this.sources = sources;
    }
}
