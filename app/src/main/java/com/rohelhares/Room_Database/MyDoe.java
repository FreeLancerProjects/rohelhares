package com.rohelhares.Room_Database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface MyDoe {
    @Insert
    void add_geo(AddGeo addGeo);

    @Query("select * from Geo")
     List<AddGeo> getgeo();



}
