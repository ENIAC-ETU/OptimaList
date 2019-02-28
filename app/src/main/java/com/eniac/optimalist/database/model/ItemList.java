package com.eniac.optimalist.database.model;

public class ItemList {
    public static final String TABLE_NAME = "item_lists";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_TITLE = "title";
    public static final String COLUMN_CREATED_AT = "created_at";
    public static final String COLUMN_SHOPPING_LIST_ID = "shopping_list_id";
    public static final String COLUMN_AMOUNT = "amount";
    public static final String COLUMN_PRICE = "price";
    public static final String COLUMN_CATEGORY = "category";


    private long id;
    private long shopping_list_id;
    private String title;
    private String createdAt;
    private int amount;
    private float price;
    private String category;

    public static final String CREATE_TABLE =
            "create table " + TABLE_NAME + "("
                    + COLUMN_ID + " integer primary key autoincrement,"
                    + COLUMN_SHOPPING_LIST_ID + " integer,"
                    + COLUMN_TITLE + " text,"
                    + COLUMN_AMOUNT + " integer,"
                    + COLUMN_PRICE + " real,"
                    + COLUMN_CREATED_AT + " datetime default (DATETIME(CURRENT_TIMESTAMP, 'LOCALTIME')),"
                    + COLUMN_CATEGORY + " text"
                    + ")";

    public ItemList(int id, String title, int amount, float price, String createdAt, int shopping_list_id, String category) {
        this.id = id;
        this.title = title;
        this.createdAt = createdAt;
        this.shopping_list_id=shopping_list_id;
        this.amount=amount;
        this.price=price;
        this.category=category;
    }

    public long getId() {
        return id;
    }

    public long getShoppingListId() {
        return shopping_list_id;
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

    public float getPrice(){return price;}

    public int getAmount (){return amount;}

    public String getCategory() { return category; }

    public void setCategory(String category) { this.category=category; }
}
