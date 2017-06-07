package com.example.popular_movies.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import static com.example.popular_movies.data.ItemsProvider.Tables;

public class ItemsDatabase extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "popular_movies.db";
    private static final int DATABASE_VERSION = 5;

    public ItemsDatabase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + Tables.ITEMS + " ("
                + ItemsContract.ItemsColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + ItemsContract.ItemsColumns.TITLE + " TEXT NOT NULL,"
                + ItemsContract.ItemsColumns.VOTE_AVERAGE + " TEXT NOT NULL,"
                + ItemsContract.ItemsColumns.POPULARITY + " TEXT NOT NULL,"
                + ItemsContract.ItemsColumns.RELEASE_DATE + " INTEGER NOT NULL DEFAULT 0,"
                + ItemsContract.ItemsColumns.SAVE_AS_FAVORITE + " INTEGER NOT NULL DEFAULT 0,"
                + ItemsContract.ItemsColumns.OVERVIEW + " TEXT NOT NULL,"
                + ItemsContract.ItemsColumns.POSTER_URL + " TEXT NOT NULL,"
                + ItemsContract.ItemsColumns.PHOTO_URL + " TEXT NOT NULL"
                + ")" );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + Tables.ITEMS);
        onCreate(db);
    }
}
