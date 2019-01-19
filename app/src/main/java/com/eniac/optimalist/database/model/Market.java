package com.eniac.optimalist.database.model;

public class Market {
    public static final String TABLE_NAME = "market";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_TITLE = "title";
    public static final String COLUMN_CREATED_AT = "created_at";
    public static final String COLUMN_LAT = "lat";
    public static final String COLUMN_LNG = "lng";

    private long id;
    private String title;
    private String createdAt;
    private double lat;
    private double lng;

    public static final String CREATE_TABLE =
            "create table " + TABLE_NAME + "("
                    + COLUMN_ID + " integer primary key autoincrement,"
                    + COLUMN_TITLE + " text not null,"
                    + COLUMN_CREATED_AT + " datetime default (DATETIME(CURRENT_TIMESTAMP, 'LOCALTIME')),"
                    + COLUMN_LAT + " real not null,"
                    + COLUMN_LNG + " real not null"
                    + ")";

    public Market(long id, String title, String createdAt, double lat, double lng) {
        this.id = id;
        this.title = title;
        this.createdAt = createdAt;
        this.lat = lat;
        this.lng = lng;
    }

    public long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public double getLat() {
        return lat;
    }

    public double getLng() {
        return lng;
    }
}
