package com.example.blogmy;

public class Anime {

    public String id;
    public String name;
    public String image;
    public boolean expanded = false;
    public boolean parent = false;

    public Anime(){

    }

    public Anime(String image, String name, String id) {
        this.image = image;
        this.name = name;
        this.id = id;

    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isExpanded() {
        return expanded;
    }

    public void setExpanded(boolean expanded) {
        this.expanded = expanded;
    }

    public boolean isParent() {
        return parent;
    }

    public void setParent(boolean parent) {
        this.parent = parent;
    }
}
