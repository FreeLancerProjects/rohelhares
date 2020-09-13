package com.rohelhares.Room_Database;

import androidx.room.Database;
import androidx.room.RoomDatabase;
@Database(entities = {AddGeo.class}, version = 1)
public abstract class My_Database extends RoomDatabase {
    public abstract MyDoe myDoe();
}
