package com.example.patrick.test;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;


@Table(name = "Bloggers")
public class Blogger extends Model {
    @Column(name = "Name", index = true)
    public String name;

    @Column(name = "Json", index = true)
    public String json = null;

    public Blogger() {
        super();
    }

    public Blogger(String name, String json) {
        super();
        this.name = name;
        this.json = json;
    }


}