package com.example.blogmy;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Locale;

public class UserSearchAnime {

    public String mal_id;
    public String image_url;
    public String title;
    public String airing;
    public String score;
    public String episodes;
    public String start_date;
    public String end_date;


    public UserSearchAnime(){

    }

    public UserSearchAnime(String mal_id, String image_url, String title, String airing, String score, String episodes, String start_date, String end_date) {
        this.mal_id = mal_id;
        this.image_url = image_url;
        this.title = title;
        this.airing = airing;
        this.score = score;
        this.episodes = episodes;
        this.start_date = start_date;
        this.end_date = end_date;
    }

    public String getMal_id() {
        return mal_id;
    }

    public String getImage_url() {
        return image_url;
    }

    public String getTitle() {
        return title;
    }

    public String getAiring() {
        return airing;
    }

    public String getScore() {
        return score;
    }

    public String getEpisodes() {
        return episodes;
    }

    public String getStart_date() {
        DateFormat originalFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.XXX", Locale.ENGLISH);
        DateFormat targetFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date date = null;
        String formattedDate = "";
        try {
            date = originalFormat.parse(start_date);
            formattedDate = targetFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return formattedDate;
    }

    public String getEnd_date() {
        DateFormat originalFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.XXX", Locale.ENGLISH);
        DateFormat targetFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date date = null;
        String formattedDate = "";
        try {
            date = originalFormat.parse(end_date);
            formattedDate = targetFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return formattedDate;
    }
}
