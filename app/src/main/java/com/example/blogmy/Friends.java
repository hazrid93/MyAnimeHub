package com.example.blogmy;

import android.text.TextUtils;

public class Friends {

    public String date, request_type;

    public Friends() {

    }

    public Friends(String date, String request_type) {
        this.date = date;
        this.request_type = request_type;
    }


    public String getRequest_type() {
        return request_type;
    }

    public void setRequest_type(String request_type) {
        if(!TextUtils.isEmpty(request_type)){
            this.request_type = request_type;
        } else {
            this.request_type = "";
        }
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        if(!TextUtils.isEmpty(date)){
            this.date = date;
        } else {
            this.date = "";
        }
    }


}
