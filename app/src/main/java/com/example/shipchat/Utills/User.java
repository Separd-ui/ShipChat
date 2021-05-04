package com.example.shipchat.Utills;

public class User {
    private String userui;
    private String imageid;
    private String username;
    private String status;
    private String search;

    public String getSearch() {
        return search;
    }
    public void setSearch(String search) {
        this.search = search;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getUserui() {
        return userui;
    }

    public void setUserui(String userui) {
        this.userui = userui;
    }

    public String getImageid() {
        return imageid;
    }

    public void setImageid(String imageid) {
        this.imageid = imageid;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
