package com.example.blogmy;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class Characters {

    public String name, role, mal_id, image_url;

    public Characters(){}

    public Characters(String name, String role, String mal_id, String image_url) {
        this.name = name;
        this.role = role;
        this.mal_id = mal_id;
        this.image_url = image_url;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getMal_id() {
        return mal_id;
    }

    public void setMal_id(String mal_id) {
        this.mal_id = mal_id;
    }

    public String getImage_url() {
        return image_url;
    }

    public void setImage_url(String image_url) {
        this.image_url = image_url;
    }

    public Characters(JSONObject object){
        try {
            this.name = object.getString("name");
            this.role = object.getString("role");
            this.image_url = object.getString("image_url");
            this.mal_id = object.getString("mal_id");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    // Factory method to convert an array of JSON objects into a list of objects
    // User.fromJson(jsonArray);
    public static ArrayList<Characters> fromJson(JSONArray jsonObjects) {
        ArrayList<Characters> characters = new ArrayList<Characters>();
        for (int i = 0; i < jsonObjects.length(); i++) {
            try {
                characters.add(new Characters(jsonObjects.getJSONObject(i)));
            } catch (JSONException e) {
                e.printStackTrace();
                characters.add(new Characters("null", "null", "null", "null"));
            }
        }
        return characters;
    }

}
