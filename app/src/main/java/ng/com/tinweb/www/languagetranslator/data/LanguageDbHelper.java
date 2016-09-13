package ng.com.tinweb.www.languagetranslator.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;

/**
 * Created by kamiye on 13/09/2016.
 */
public class LanguageDbHelper extends SQLiteOpenHelper implements LanguageDataStore {

    // Note: if you change the database schema, you must increment the database version
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "Translator.db";

    public LanguageDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(DbContract.SQL_CREATE_ENTRIES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVerson, int newVersion) {
        sqLiteDatabase.execSQL(DbContract.SQL_DELETE_ENTRIES);
        onCreate(sqLiteDatabase);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    @Override
    public void saveLanguages(JSONObject languages, DbActionCallback callback) {
        SQLiteDatabase database = getWritableDatabase();

        Iterator<String> keys = languages.keys();

        while (keys.hasNext()) {
            ContentValues values = new ContentValues();
            String key = keys.next();
            try {
                String value = languages.getString(key);
                values.put(DbContract.LanguagesSchema.COLUMN_LANGUAGE_KEY, key);
                values.put(DbContract.LanguagesSchema.COLUMN_LANGUAGE_VALUE, value);
                database.insert(DbContract.LanguagesSchema.TABLE_NAME, null, values);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        callback.onFinish();
    }

    @Override
    public boolean checkLanguages() {
        SQLiteDatabase database = getReadableDatabase();

        String[] projection = {
                DbContract.LanguagesSchema._ID,
                DbContract.LanguagesSchema.COLUMN_LANGUAGE_KEY
        };
        Cursor cursor = database.query(
                DbContract.LanguagesSchema.TABLE_NAME, projection,
                null,
                null,
                null,
                null,
                null
        );
        if (cursor.getCount() > 0) {
            cursor.close();
            return true;
        }
        return false;
    }

    @Override
    public void deleteLanguages() {
        SQLiteDatabase database = getWritableDatabase();
        database.execSQL("delete from " + DbContract.LanguagesSchema.TABLE_NAME);
    }
}