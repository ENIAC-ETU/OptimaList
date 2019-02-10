package com.eniac.optimalist.database.model;

public class ReminderModel {

    public static final String TABLE_NAME = "reminders";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_TITLE = "title";
    public static final String COLUMN_CREATED_AT = "created_at";
    public static final String COLUMN_SHOPPING_LIST_ID = "shopping_list_id";
    public static final String COLUMN_MARKET_ID = "market_id";





    private long id;
    private String title;
    private String createdAt;
    private long shopping_list_id;
    private long market_id;


    public static final String CREATE_TABLE =
            "create table " + TABLE_NAME + "("
                    + COLUMN_ID + " integer primary key autoincrement,"
                    + COLUMN_TITLE + " text,"
                    + COLUMN_CREATED_AT + " datetime default (DATETIME(CURRENT_TIMESTAMP, 'LOCALTIME')),"
                    + COLUMN_SHOPPING_LIST_ID + " integer, "
                    + COLUMN_MARKET_ID + " integer "
                    + ")";

    public ReminderModel(int id, String title, String createdAt, long shopping_list_id, long market_id) {
        this.id = id;
        this.title = title;
        this.createdAt = createdAt;
        this.shopping_list_id = shopping_list_id;
        this.market_id = market_id;
    }

    public long getId() {
        return id;
    }

    public long get_shopping_list_id(){return shopping_list_id;}

    public long get_market_id(){return market_id;}

    public String getTitle() {
        return title;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setShoppingListId(long id){this.shopping_list_id=id;}

    public void setMarketId(long id){this.market_id=id;}

    public String toString() {
        return title;
    }


}
