package com.example.patrick.tumblrloader.DB;

import com.activeandroid.ActiveAndroid;
import com.activeandroid.query.Select;

import java.util.List;

public class DataBaseInerator {

    public boolean isBloggerExist(String blogger) {
        BloggerDB item = new Select().from(BloggerDB.class).where("name = ?", blogger).executeSingle();
        return item != null;
    }

    public BloggerDB findBloggerByName(String blogger) {
        BloggerDB blog = new Select().from(BloggerDB.class).where("name = ?", blogger).executeSingle();
        return blog;
    }

    public List<BloggerDB> findBloggers() {
        return new Select().from(BloggerDB.class).execute();
    }

    public void saveBloggertoDB(String blog_name, String JsonStr) {
        BloggerDB item = new Select().from(BloggerDB.class).where("name = ?", blog_name).executeSingle();
        if (item == null) {
            ActiveAndroid.beginTransaction();
            try {
                BloggerDB blogger = new BloggerDB();
                blogger.name = blog_name;
                blogger.json = JsonStr;
                blogger.save();
                ActiveAndroid.setTransactionSuccessful();
            } finally {
                ActiveAndroid.endTransaction();
            }
        }
    }
}
