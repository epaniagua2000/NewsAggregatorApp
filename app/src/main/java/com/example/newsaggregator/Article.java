package com.example.newsaggregator;

import android.annotation.SuppressLint;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;
import java.util.TimeZone;

public class Article implements Serializable {

    private final String author;
    private final String title;
    private final String desc;
    private final String url;
    private final String urlToImage;
    private final String date;

    public Article(String author, String title, String desc, String url, String urlToImage, String date) {
        this.author = author;
        this.title = title;
        this.desc = desc;
        this.url = url;
        this.urlToImage = urlToImage;
        this.date = date;
    }

    public String getAuthor() { return author; }

    public String getTitle() { return title; }

    public String getDesc() { return desc; }

    public String getUrl() { return url; }

    public String getUrlToImage() { return urlToImage; }

    public String getDate() { return date; }

}
