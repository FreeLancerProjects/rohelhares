package com.rohelhares.Room_Database;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "Geo")
public class AddGeo {
    @PrimaryKey(autoGenerate = true)
    private int id;
    @ColumnInfo(name = "from_lat")
    private double from_lat;
    @ColumnInfo(name = "to_lat")
    private double to_lat;
    @ColumnInfo(name = "from_lng")
    private double from_lng;
    @ColumnInfo(name = "to_lng")
    private double to_lng;
    @ColumnInfo(name = "title")
    private String title;
    @ColumnInfo(name = "content")
    private String content;
//    @ColumnInfo(name = "sound")
//    private byte[] image;


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public double getFrom_lat() {
        return from_lat;
    }

    public void setFrom_lat(double from_lat) {
        this.from_lat = from_lat;
    }

    public double getTo_lat() {
        return to_lat;
    }

    public void setTo_lat(double to_lat) {
        this.to_lat = to_lat;
    }

    public double getFrom_lng() {
        return from_lng;
    }

    public void setFrom_lng(double from_lng) {
        this.from_lng = from_lng;
    }

    public double getTo_lng() {
        return to_lng;
    }

    public void setTo_lng(double to_lng) {
        this.to_lng = to_lng;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
