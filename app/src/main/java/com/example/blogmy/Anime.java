package com.example.blogmy;

public class Anime {

    public String image;
    public String name;
    public boolean expanded = false;
    public boolean parent = false;

    public Anime(){

    }

    public Anime(String image, String name) {
        this.image = image;
        this.name = name;

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
