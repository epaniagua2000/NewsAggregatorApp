package com.example.newsaggregator;

import android.os.Parcelable;

import androidx.annotation.NonNull;

import java.io.Serializable;

public class Source implements Comparable<Source>, Serializable {

    private final String id;
    private final String name;
    private final String category;
    private final String language;
    private final String country;

    public Source(String id, String name, String category, String language, String country) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.language = language;
        this.country = country;
    }

    public String getId() { return id; }

    public String getName() { return name; }

    public String getCategory() { return category; }

    public String getLanguage() { return language; }

    public String getCountry() { return country; }

    @NonNull
    public String toString() { return name; }

    @Override
    public int compareTo (Source source) { return name.compareTo(source.name); }
}
