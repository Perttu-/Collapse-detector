package com.aware.plugin.collapse_detector;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHandler extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME ="collapseDatabase";
    private static final String TABLE_COLLAPSES ="collapses";
    private static final String KEY_TIMESTAMP = "timestamp";
    private static final String INFO = "info";

    public DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_CONTACTS_TABLE = "CREATE TABLE " + TABLE_COLLAPSES + "("
                + KEY_TIMESTAMP + " INTEGER PRIMARY KEY," + INFO + " TEXT"+ ")";
        db.execSQL(CREATE_CONTACTS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //drop table and create new one
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_COLLAPSES);
        onCreate(db);
    }


    public void addCollapse(CollapseInfo collapse) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_TIMESTAMP, collapse.getTimestamp());
        values.put(INFO, collapse.getInfo());
        db.insert(TABLE_COLLAPSES, null, values);
        db.close();
    }

    public CollapseInfo getCollapse(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_COLLAPSES, new String[] {KEY_TIMESTAMP, INFO},KEY_TIMESTAMP + "=?",
                new String[]{String.valueOf(id)},null,null,null,null);
        if(cursor != null){
            cursor.moveToFirst();
        }
        CollapseInfo collapse = new CollapseInfo(Long.parseLong(cursor.getString(0)),cursor.getString(1));
        return collapse;
    }

    public List<CollapseInfo> getAllCollapses() {
        List<CollapseInfo> collapseInfoList = new ArrayList<CollapseInfo>();
        String selectQuery = "SELECT * FROM "+TABLE_COLLAPSES;
        SQLiteDatabase db =this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery,null);
        if(cursor.moveToFirst()){
            do {
                CollapseInfo collapse = new CollapseInfo();
                collapse.setTimestamp(Long.parseLong(cursor.getString(0)));
                collapse.setInfo(cursor.getString(1));
                collapseInfoList.add(collapse);
            } while (cursor.moveToNext());
        }
        db.close();
        return collapseInfoList;
    }

    public int getCollapsesCount() {
        String countQuery = "SELECT * FROM "+ TABLE_COLLAPSES;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        cursor.close();

        return cursor.getCount();
    }

    public int updateCollapse(CollapseInfo collapse) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_TIMESTAMP, collapse.getTimestamp());
        values.put(INFO, collapse.getInfo());
        return db.update(TABLE_COLLAPSES, values, KEY_TIMESTAMP + " =?",new String[] {String.valueOf(collapse.getTimestamp())});
    }

    public void deleteCollapse(CollapseInfo collapse) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_COLLAPSES,KEY_TIMESTAMP + " =?",
                new String[] {String.valueOf(collapse.getTimestamp())});
        db.close();
    }
}
