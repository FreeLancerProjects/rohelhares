package com.rohelhares.model;

import java.io.Serializable;

public class TimesModel implements Serializable {

   private String title;



    public TimesModel(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }
}
