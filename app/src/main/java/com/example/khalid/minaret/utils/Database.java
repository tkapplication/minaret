package com.example.khalid.minaret.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.khalid.minaret.models.Message;
import com.example.khalid.minaret.models.Post;

import java.util.ArrayList;

/**
 * Created by khalid on 1/16/2018.
 */

public class Database extends SQLiteOpenHelper {
    public static String DataBase_Name = "woovendor";

    String favorite = "favorite";
    String messages = "messages";
    Context context;

    public Database(Context context) {
        super(context, DataBase_Name, null, 1);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

        String sql1 = "CREATE TABLE " + messages + " (title TEXT,message TEXT)";
        String sql2 = "CREATE TABLE " + favorite + " (id TEXT PRIMARY KEY,title TEXT,content TEXT,date TEXT,image TEXT,comment TEXT,comment_count TEXT,favorite_count TEXT)";


        sqLiteDatabase.execSQL(sql1);
        sqLiteDatabase.execSQL(sql2);


    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }

    public void addMessage(Message message) {
        SQLiteDatabase database = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("title", message.getTitle());
        values.put("message", message.getMessage());


        database.insert(messages, null, values);
    }

    public void addFavorite(Post post) {
        SQLiteDatabase database = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("id", post.getId());
        values.put("title", post.getTitle());
        values.put("content", post.getContent());
        values.put("date", post.getDate());
        values.put("image", post.getImage());
        values.put("comment", post.getComment());
        values.put("comment_count", post.getComment_count());
        values.put("favorite_count", post.getFavorite_count());


        database.insert(favorite, null, values);
    }

    public ArrayList<Message> getMessages() {
        ArrayList<Message> onsaleproduct = new ArrayList<>();


        String selectQuery = "SELECT  * FROM " + messages;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {
                Message product = new Message("", "");
                product.setTitle(cursor.getString(0));
                product.setMessage(cursor.getString(1));


                onsaleproduct.add(product);
            }
            while (cursor.moveToNext());
        }
        return onsaleproduct;
    }


    public ArrayList<Post> getFavorite() {
        ArrayList<Post> onsaleproduct = new ArrayList<>();


        String selectQuery = "SELECT  * FROM " + favorite;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {
                Post product = new Post("", "", "", "", "", "", "", "");
                product.setId(cursor.getString(0));
                product.setTitle(cursor.getString(1));
                product.setContent(cursor.getString(2));
                product.setDate(cursor.getString(3));
                product.setImage(cursor.getString(4));
                product.setComment(cursor.getString(5));
                product.setComment_count(cursor.getString(6));
                product.setFavorite_count(cursor.getString(7));


                onsaleproduct.add(product);
            }
            while (cursor.moveToNext());
        }
        return onsaleproduct;
    }

    public void deleteMessage(String title) {
        SQLiteDatabase database = this.getWritableDatabase();
        database.delete(messages, "title=?", new String[]{title});
        database.close();

    }

    public void deleteFavorite(String id) {
        SQLiteDatabase database = this.getWritableDatabase();
        database.delete(favorite, "id=?", new String[]{id});
        database.close();

    }

    public boolean checkFavorite(String id) {
        SQLiteDatabase database = this.getReadableDatabase();

        Cursor mCursor = database.rawQuery("SELECT * FROM " + favorite + " WHERE id=?"
                , new String[]{id});
        if (mCursor != null) {
            if (mCursor.getCount() > 0) {
                if (mCursor.moveToFirst()) {

                    if (!mCursor.getString(3).equals(""))

                        return true;
                }
            }
        }
        return false;
    }
}