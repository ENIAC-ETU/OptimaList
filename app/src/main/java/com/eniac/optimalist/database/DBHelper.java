package com.eniac.optimalist.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import com.eniac.optimalist.database.model.ItemList;
import com.eniac.optimalist.database.model.Market;
import com.eniac.optimalist.database.model.ShoppingList;
import com.eniac.optimalist.database.model.ReminderModel;

public class DBHelper extends SQLiteOpenHelper {

    private static DBHelper sInstance;
    private static final String DATABASE_NAME = "optimalist.db";

    public static synchronized DBHelper getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new DBHelper(context.getApplicationContext());
        }
        return sInstance;
    }

    private DBHelper(Context context) {
        super(context, DATABASE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(Market.CREATE_TABLE);
        db.execSQL(ShoppingList.CREATE_TABLE);
        db.execSQL(ReminderModel.CREATE_TABLE);
        db.execSQL(ItemList.CREATE_TABLE);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("drop table if exists " + Market.TABLE_NAME);
        db.execSQL("drop table if exists " + ShoppingList.TABLE_NAME);
        db.execSQL("drop table if exists " + ReminderModel.TABLE_NAME);
        db.execSQL("drop table if exists " + ItemList.TABLE_NAME);
        onCreate(db);
    }

    public long insertShoppingList(String title) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(ShoppingList.COLUMN_TITLE, title);
        return db.insert(ShoppingList.TABLE_NAME, null, contentValues);
    }

    public ShoppingList getShoppingList(long id) {
        SQLiteDatabase db = this.getReadableDatabase();
        ShoppingList shoppingList = null;

        try (Cursor cursor = db.query(ShoppingList.TABLE_NAME, null, ShoppingList.COLUMN_ID + " = " + id, null, null, null, null)){
            if (cursor.moveToFirst()) {
                shoppingList = new ShoppingList(
                        cursor.getLong(cursor.getColumnIndex(ShoppingList.COLUMN_ID)),
                        cursor.getString(cursor.getColumnIndex(ShoppingList.COLUMN_TITLE)),
                        cursor.getString(cursor.getColumnIndex(ShoppingList.COLUMN_CREATED_AT))
                );
            }
        }

        return shoppingList;
    }

    public long updateShoppingList(ShoppingList shoppingList) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(shoppingList.COLUMN_TITLE, shoppingList.getTitle());
        // updating row
        return db.update(ShoppingList.TABLE_NAME, values, ShoppingList.COLUMN_ID + " = " + shoppingList.getId(), null);
    }

    public boolean deleteShoppingList(ShoppingList shoppingList) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(ShoppingList.TABLE_NAME, ShoppingList.COLUMN_ID + " = " + shoppingList.getId(), null) > 0;
    }

    public boolean deleteItem(ItemList itemList) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(ItemList.TABLE_NAME, ItemList.COLUMN_ID + " = " + itemList.getId(), null) > 0;
    }

    public List<ShoppingList> getAllShoppingLists() {
        List<ShoppingList> shoppingLists = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        try (Cursor cursor = db.query(ShoppingList.TABLE_NAME, null, null, null, null, null, ShoppingList.COLUMN_CREATED_AT + " desc")){
            if (cursor.moveToFirst()) {
                do {
                    shoppingLists.add(new ShoppingList(
                                    cursor.getLong(cursor.getColumnIndex(ShoppingList.COLUMN_ID)),
                                    cursor.getString(cursor.getColumnIndex(ShoppingList.COLUMN_TITLE)),
                                    cursor.getString(cursor.getColumnIndex(ShoppingList.COLUMN_CREATED_AT))
                            )
                    );
                } while (cursor.moveToNext());
            }
        }

        return shoppingLists;
    }

    public int getShoppingListsCount() {
        String countQuery = "select * from " + ShoppingList.TABLE_NAME;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);

        int count = cursor.getCount();
        cursor.close();

        // return count
        return count;
    }

    public long insertReminder(String title, long shopping_list_id, long market_id) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(ReminderModel.COLUMN_TITLE, title);
        contentValues.put(ReminderModel.COLUMN_SHOPPING_LIST_ID,shopping_list_id);
        contentValues.put(ReminderModel.COLUMN_MARKET_ID,market_id);
        return db.insert(ReminderModel.TABLE_NAME, null, contentValues);
    }

    public ReminderModel getReminder(long id) {
        SQLiteDatabase db = this.getReadableDatabase();
        ReminderModel reminder = null;

        try (Cursor cursor = db.query(ReminderModel.TABLE_NAME, null, ReminderModel.COLUMN_ID + " = " + id, null, null, null, null)){
            if (cursor.moveToFirst()) {
                reminder = new ReminderModel(
                        cursor.getInt(cursor.getColumnIndex(ReminderModel.COLUMN_ID)),
                        cursor.getString(cursor.getColumnIndex(ReminderModel.COLUMN_TITLE)),
                        cursor.getString(cursor.getColumnIndex(ReminderModel.COLUMN_CREATED_AT)),
                        cursor.getInt(cursor.getColumnIndex(ReminderModel.COLUMN_SHOPPING_LIST_ID)),
                        cursor.getInt(cursor.getColumnIndex(ReminderModel.COLUMN_MARKET_ID))
                );
            }
        }

        return reminder;
    }

    public int updateReminder(ReminderModel reminder) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(reminder.COLUMN_TITLE, reminder.getTitle());
        values.put(reminder.COLUMN_SHOPPING_LIST_ID,reminder.get_shopping_list_id());
        values.put(reminder.COLUMN_MARKET_ID,reminder.get_market_id());

        // updating row
        return db.update(ReminderModel.TABLE_NAME, values, ReminderModel.COLUMN_ID + " = " + reminder.getId(), null);
    }

    public boolean deleteReminder(ReminderModel reminder) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(ReminderModel.TABLE_NAME, ReminderModel.COLUMN_ID + " = " + reminder.getId(), null) > 0;
    }

    public List<ReminderModel> getAllReminders() {
        List<ReminderModel> reminders = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        try (Cursor cursor = db.query(ReminderModel.TABLE_NAME, null, null, null, null, null, ReminderModel.COLUMN_CREATED_AT + " desc")){
            if (cursor.moveToFirst()) {
                do {
                    reminders.add(new ReminderModel(
                            cursor.getInt(cursor.getColumnIndex(ReminderModel.COLUMN_ID)),
                            cursor.getString(cursor.getColumnIndex(ReminderModel.COLUMN_TITLE)),
                            cursor.getString(cursor.getColumnIndex(ReminderModel.COLUMN_CREATED_AT)),
                            cursor.getLong(cursor.getColumnIndex(ReminderModel.COLUMN_SHOPPING_LIST_ID)),
                            cursor.getLong(cursor.getColumnIndex(ReminderModel.COLUMN_MARKET_ID))

                            )
                    );
                } while (cursor.moveToNext());
            }
        }

        return reminders;
    }

    public int getRemindersCount() {
        String countQuery = "select * from " + ReminderModel.TABLE_NAME;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);

        int count = cursor.getCount();
        cursor.close();

        return count;
    }

    public long insertMarket(String title, double lat, double lng) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(Market.COLUMN_TITLE, title);
        contentValues.put(Market.COLUMN_LAT, lat);
        contentValues.put(Market.COLUMN_LNG, lng);
        return db.insert(Market.TABLE_NAME, null, contentValues);
    }

    public Market getMarket(long id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Market market = null;

        try (Cursor cursor = db.query(Market.TABLE_NAME, null, Market.COLUMN_ID + " = " + id, null, null, null, null)){
            if (cursor.moveToFirst()) {
                market = new Market(
                        cursor.getLong(cursor.getColumnIndex(Market.COLUMN_ID)),
                        cursor.getString(cursor.getColumnIndex(Market.COLUMN_TITLE)),
                        cursor.getString(cursor.getColumnIndex(Market.COLUMN_CREATED_AT)),
                        cursor.getDouble(cursor.getColumnIndex(Market.COLUMN_LAT)),
                        cursor.getDouble(cursor.getColumnIndex(Market.COLUMN_LNG))
                );
            }
        }

        return market;
    }

    public List<Market> getAllMarkets() {
        List<Market> markets = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        try (Cursor cursor = db.query(Market.TABLE_NAME, null, null, null, null, null, Market.COLUMN_CREATED_AT + " desc")){
            if (cursor.moveToFirst()) {
                do {
                    markets.add(new Market(
                                    cursor.getLong(cursor.getColumnIndex(Market.COLUMN_ID)),
                                    cursor.getString(cursor.getColumnIndex(Market.COLUMN_TITLE)),
                                    cursor.getString(cursor.getColumnIndex(Market.COLUMN_CREATED_AT)),
                                    cursor.getDouble(cursor.getColumnIndex(Market.COLUMN_LAT)),
                                    cursor.getDouble(cursor.getColumnIndex(Market.COLUMN_LNG))
                            )
                    );
                } while (cursor.moveToNext());
            }
        }

        return markets;
    }

    public int getMarketsCount() {
        String countQuery = "select * from " + Market.TABLE_NAME;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);

        int count = cursor.getCount();
        cursor.close();

        // return count
        return count;
    }

    public boolean deleteMarket(Market market) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(Market.TABLE_NAME, Market.COLUMN_ID + " = " + market.getId(), null) > 0;
    }

    public long insertItemList(String title, long currentPositionId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(ItemList.COLUMN_TITLE, title);
        contentValues.put(ItemList.COLUMN_SHOPPING_LIST_ID,currentPositionId);
        return db.insert(ItemList.TABLE_NAME, null, contentValues);
    }

    public ItemList getItemList(long id) {
        SQLiteDatabase db = this.getReadableDatabase();
        ItemList itemList = null;

        try (Cursor cursor = db.query(ItemList.TABLE_NAME, null ,ItemList.COLUMN_ID + " = " + id, null, null, null, null)){
            if (cursor.moveToFirst()) {
                itemList = new ItemList(
                        cursor.getInt(cursor.getColumnIndex(ItemList.COLUMN_ID)),
                        cursor.getString(cursor.getColumnIndex(ItemList.COLUMN_TITLE)),
                        cursor.getString(cursor.getColumnIndex(ItemList.COLUMN_CREATED_AT)),
                        cursor.getInt(cursor.getColumnIndex(ItemList.COLUMN_SHOPPING_LIST_ID))


                );
            }
        }

        return itemList;
    }




    public List<ItemList> getAllItemLists() {
        List<ItemList> itemLists = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        try (Cursor cursor = db.query(ItemList.TABLE_NAME, null, null, null, null, null, ItemList.COLUMN_CREATED_AT + " desc")){
            if (cursor.moveToFirst()) {
                do {
                    itemLists.add(new ItemList(
                                    cursor.getInt(cursor.getColumnIndex(ItemList.COLUMN_ID)),
                                    cursor.getString(cursor.getColumnIndex(ItemList.COLUMN_TITLE)),
                                    cursor.getString(cursor.getColumnIndex(ItemList.COLUMN_CREATED_AT)),
                                    cursor.getInt(cursor.getColumnIndex(ItemList.COLUMN_SHOPPING_LIST_ID))
                            )
                    );
                } while (cursor.moveToNext());
            }
        }

        return itemLists;
    }


    public List<ItemList> getCurrentItems(long shoppingListId) {
        List<ItemList> itemLists = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        try (Cursor cursor = db.query(ItemList.TABLE_NAME, null, ItemList.COLUMN_SHOPPING_LIST_ID + " = " + shoppingListId, null, null, null, ItemList.COLUMN_CREATED_AT + " desc")){
            if (cursor.moveToFirst()) {
                do {
                    itemLists.add(new ItemList(
                                    cursor.getInt(cursor.getColumnIndex(ItemList.COLUMN_ID)),
                                    cursor.getString(cursor.getColumnIndex(ItemList.COLUMN_TITLE)),
                                    cursor.getString(cursor.getColumnIndex(ItemList.COLUMN_CREATED_AT)),
                                    cursor.getInt(cursor.getColumnIndex(ItemList.COLUMN_SHOPPING_LIST_ID))
                            )
                    );
                } while (cursor.moveToNext());
            }
        }

        return itemLists;
    }

    public int getItemListsCount() {
        String countQuery = "select * from " + ItemList.TABLE_NAME;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);

        int count = cursor.getCount();
        cursor.close();

        // return count
        return count;
    }
}
