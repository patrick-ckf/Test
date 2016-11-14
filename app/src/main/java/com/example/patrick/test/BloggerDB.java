package com.example.patrick.test;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;

import java.util.Calendar;


@Table(name = "Bloggers")
public class BloggerDB extends Model {
    @Column(name = "Name", index = true)
    public String name;

    @Column(name = "Json", index = true)
    public String json = null;

    @Column(name = "Time")
    public long time;

    public BloggerDB() {
        super();
        Calendar c = Calendar.getInstance();
        this.time = c.getTimeInMillis();
    }

    public BloggerDB(String name, String json) {
        super();
        this.name = name;
        this.json = json;
        Calendar c = Calendar.getInstance();
        this.time = c.getTimeInMillis();
    }
}