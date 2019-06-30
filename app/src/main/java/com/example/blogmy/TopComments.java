package com.example.blogmy;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class TopComments {

    public String helpful_count, date, username, content, image_url;

    public TopComments(){

    }

    public TopComments(String helpful_count, String date, String username, String content, String image_url) {
        this.helpful_count = helpful_count;
        this.date = date;
        this.username = username;
        this.content = content;
        this.image_url = image_url;
    }
    // convert iso format to format we want
    public String getDate() {
        DateFormat originalFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.ENGLISH);
        DateFormat targetFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date date = null;
        String formattedDate = "";
        try {
            date = originalFormat.parse(this.date);
            formattedDate = targetFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return formattedDate;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public TopComments(JSONObject object){
        try {
            this.date = object.getString("date");
            this.helpful_count = object.getString("helpful_count");
            this.username = object.getJSONObject("reviewer").getString("username");
            this.image_url = object.getJSONObject("reviewer").getString("image_url");
            this.content = object.getString("content");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    // Factory method to convert an array of JSON objects into a list of objects
    // User.fromJson(jsonArray);
    public static ArrayList<TopComments> fromJson(JSONArray jsonObjects) {
        ArrayList<TopComments> comments = new ArrayList<TopComments>();
        for (int i = 0; i < jsonObjects.length(); i++) {
            try {
                comments.add(new TopComments(jsonObjects.getJSONObject(i)));
            } catch (JSONException e) {
                e.printStackTrace();
                comments.add(new TopComments("null", "null", "null", "null", "null"));
            }
        }
        return comments;
    }

    public String getHelpful_count() {
        return helpful_count;
    }

    public void setHelpful_count(String helpful_count) {
        this.helpful_count = helpful_count;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getImage_url() {
        return image_url;
    }

    public void setImage_url(String image_url) {
        this.image_url = image_url;
    }
}
