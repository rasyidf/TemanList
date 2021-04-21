package com.rasyidf.temanlist.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import static android.content.ContentValues.TAG;

public class TemanDatabaseHelper extends SQLiteOpenHelper {
        // Database Info
        private static final String DATABASE_NAME = "postsDatabase";
        private static final int DATABASE_VERSION = 1;

        // Table Names
        private static final String TABLE_CATATAN = "catatan";
        private static final String TABLE_TEMAN = "teman";

        // Post Table Columns
        private static final String KEY_CATATAN_ID = "id";
        private static final String KEY_CATATAN_TEMAN_ID_FK = "temanId";
        private static final String KEY_CATATAN_TITLE = "title";
        private static final String KEY_CATATAN_TEXT = "text";

        // User Table Columns
        private static final String KEY_TEMAN_ID = "id";
        private static final String KEY_TEMAN_NAME = "nama";
        private static final String KEY_TEMAN_PROFILE_PICTURE_URL = "profilePictureUrl";

        private static TemanDatabaseHelper sInstance;

        public static synchronized TemanDatabaseHelper getInstance(Context context) {
            if (sInstance == null) {
                sInstance = new TemanDatabaseHelper(context.getApplicationContext());
            }
            return sInstance;
        }

        private TemanDatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onConfigure(SQLiteDatabase db) {
            super.onConfigure(db);
            db.setForeignKeyConstraintsEnabled(true);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            String CREATE_CATATAN_TABLE = "CREATE TABLE " + TABLE_CATATAN +
                    "(" +
                    KEY_CATATAN_ID + " INTEGER PRIMARY KEY," +
                    KEY_CATATAN_TEMAN_ID_FK + " INTEGER REFERENCES " + TABLE_TEMAN + "," +
                    KEY_CATATAN_TITLE + " TEXT," +
                    KEY_CATATAN_TEXT + " TEXT" +
                    ")";

            String CREATE_TEMAN_TABLE = "CREATE TABLE " + TABLE_TEMAN +
                    "(" +
                    KEY_TEMAN_ID + " INTEGER PRIMARY KEY," +
                    KEY_TEMAN_NAME + " TEXT," +
                    KEY_TEMAN_PROFILE_PICTURE_URL + " TEXT" +
                    ")";

            db.execSQL(CREATE_CATATAN_TABLE);
            db.execSQL(CREATE_TEMAN_TABLE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            if (oldVersion != newVersion) {
                db.execSQL("DROP TABLE IF EXISTS " + TABLE_CATATAN);
                db.execSQL("DROP TABLE IF EXISTS " + TABLE_TEMAN);
                onCreate(db);
            }
        }

        public void tambahCatatan(Catatan post) {
            SQLiteDatabase db = getWritableDatabase();

            db.beginTransaction();
            try {
                long userId = tambahSuntingTeman(post.teman);

                ContentValues values = new ContentValues();
                values.put(KEY_CATATAN_TEMAN_ID_FK, userId);
                values.put(KEY_CATATAN_TITLE, post.title);
                values.put(KEY_CATATAN_TEXT, post.text);

                db.insertOrThrow(TABLE_CATATAN, null, values);
                db.setTransactionSuccessful();
            } catch (Exception e) {
                Log.d(TAG, "Error menambahkan catatan ke database");
            } finally {
                db.endTransaction();
            }
        }

        public long tambahSuntingTeman(Teman user) {
            SQLiteDatabase db = getWritableDatabase();
            long temanId = -1;

            db.beginTransaction();
            try {
                ContentValues values = new ContentValues();
                values.put(KEY_TEMAN_NAME, user.Nama);
                values.put(KEY_TEMAN_PROFILE_PICTURE_URL, user.profilePictureUrl);
                int rows = db.update(TABLE_TEMAN, values, KEY_TEMAN_NAME + "= ?", new String[]{user.Nama});

                if (rows == 1) {
                    String usersSelectQuery = String.format("SELECT %s FROM %s WHERE %s = ?",
                            KEY_TEMAN_ID, TABLE_TEMAN, KEY_TEMAN_NAME);
                    Cursor cursor = db.rawQuery(usersSelectQuery, new String[]{String.valueOf(user.Nama)});
                    try {
                        if (cursor.moveToFirst()) {
                            temanId = cursor.getInt(0);
                            db.setTransactionSuccessful();
                        }
                    } finally {
                        if (cursor != null && !cursor.isClosed()) {
                            cursor.close();
                        }
                    }
                } else {
                    temanId = db.insertOrThrow(TABLE_TEMAN, null, values);
                    db.setTransactionSuccessful();
                }
            } catch (Exception e) {
                Log.d(TAG, "Error saat menambahkan Teman");
            } finally {
                db.endTransaction();
            }
            return temanId;
        }

        public List<Catatan> getAllCatatan() {
            List<Catatan> posts = new ArrayList<>();

            String POSTS_SELECT_QUERY =
                    String.format("SELECT * FROM %s LEFT OUTER JOIN %s ON %s.%s = %s.%s",
                            TABLE_CATATAN,
                            TABLE_TEMAN,
                            TABLE_CATATAN, KEY_CATATAN_TEMAN_ID_FK,
                            TABLE_TEMAN, KEY_TEMAN_ID);

            SQLiteDatabase db = getReadableDatabase();
            Cursor cursor = db.rawQuery(POSTS_SELECT_QUERY, null);
            try {
                if (cursor.moveToFirst()) {
                    do {
                        Teman temanBaru = new Teman();
                        temanBaru.Nama = cursor.getString(cursor.getColumnIndex(KEY_TEMAN_NAME));
                        temanBaru.profilePictureUrl = cursor.getString(cursor.getColumnIndex(KEY_TEMAN_PROFILE_PICTURE_URL));

                        Catatan catatanBaru = new Catatan();
                        catatanBaru.title = cursor.getString(cursor.getColumnIndex(KEY_CATATAN_TITLE));
                        catatanBaru.text = cursor.getString(cursor.getColumnIndex(KEY_CATATAN_TEXT));
                        catatanBaru.teman = temanBaru;
                        posts.add(catatanBaru);
                    } while(cursor.moveToNext());
                }
            } catch (Exception e) {
                Log.d(TAG, "Error while trying to get posts from database");
            } finally {
                if (cursor != null && !cursor.isClosed()) {
                    cursor.close();
                }
            }
            return posts;
        }


        public int perbaruiProfilTeman(Teman user) {
            SQLiteDatabase db = this.getWritableDatabase();

            ContentValues values = new ContentValues();
            values.put(KEY_TEMAN_PROFILE_PICTURE_URL, user.profilePictureUrl);

            return db.update(TABLE_TEMAN, values, KEY_TEMAN_NAME + " = ?",
                    new String[] { String.valueOf(user.Nama) });
        }

        public void hapusSemuaTabel() {
            SQLiteDatabase db = getWritableDatabase();
            db.beginTransaction();
            try {
                db.delete(TABLE_CATATAN, null, null);
                db.delete(TABLE_TEMAN, null, null);
                db.setTransactionSuccessful();
            } catch (Exception e) {
                Log.d(TAG, "Error while trying to delete all posts and users");
            } finally {
                db.endTransaction();
            }
        }
    }