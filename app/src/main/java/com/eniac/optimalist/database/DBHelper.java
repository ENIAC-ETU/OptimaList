package com.eniac.optimalist.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

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
        db.execSQL(ShoppingList.CREATE_TABLE);
        db.execSQL(ReminderModel.CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("drop table if exists " + ShoppingList.TABLE_NAME);
        db.execSQL("drop table if exists " + ReminderModel.TABLE_NAME);
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
                        cursor.getInt(cursor.getColumnIndex(ShoppingList.COLUMN_ID)),
                        cursor.getString(cursor.getColumnIndex(ShoppingList.COLUMN_TITLE)),
                        cursor.getString(cursor.getColumnIndex(ShoppingList.COLUMN_CREATED_AT))
                );
            }
        }

        return shoppingList;
    }

    public int updateShoppingList(ShoppingList shoppingList) {
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

    public List<ShoppingList> getAllShoppingLists() {
        List<ShoppingList> shoppingLists = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        try (Cursor cursor = db.query(ShoppingList.TABLE_NAME, null, null, null, null, null, ShoppingList.COLUMN_CREATED_AT + " desc")){
            if (cursor.moveToFirst()) {
                do {
                    shoppingLists.add(new ShoppingList(
                                    cursor.getInt(cursor.getColumnIndex(ShoppingList.COLUMN_ID)),
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



    public long insertReminder(String title) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(ReminderModel.COLUMN_TITLE, title);
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
                        cursor.getString(cursor.getColumnIndex(ReminderModel.COLUMN_CREATED_AT))
                );
            }
        }

        return reminder;
    }

    public int updateReminder(ReminderModel reminder) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(reminder.COLUMN_TITLE, reminder.getTitle());

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
                                    cursor.getString(cursor.getColumnIndex(ReminderModel.COLUMN_CREATED_AT))
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

        // return count
        return count;
    }
}
