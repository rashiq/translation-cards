package org.mercycorps.translationcards.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import org.mercycorps.translationcards.R;

import java.util.Date;

public class DbHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "TranslationCards.db";
    private static final int DATABASE_VERSION = 2;

    // Initialization SQL.
    private static final String INIT_DECKS_SQL =
            "CREATE TABLE " + DbManager.DecksTable.TABLE_NAME + "( " +
            DbManager.DecksTable.ID + " INTEGER PRIMARY KEY," +
            DbManager.DecksTable.LABEL + " TEXT," +
            DbManager.DecksTable.PUBLISHER + " TEXT," +
            DbManager.DecksTable.CREATION_TIMESTAMP + " INTEGER," +
            DbManager.DecksTable.EXTERNAL_ID + " TEXT," +
            DbManager.DecksTable.HASH + " TEXT," +
            DbManager.DecksTable.LOCKED + " INTEGER" +
            ")";
    private static final String INIT_DICTIONARIES_SQL =
            "CREATE TABLE " + DbManager.DictionariesTable.TABLE_NAME + "( " +
            DbManager.DictionariesTable.ID + " INTEGER PRIMARY KEY," +
            DbManager.DictionariesTable.DECK_ID + " INTEGER," +
            DbManager.DictionariesTable.LABEL + " TEXT," +
            DbManager.DictionariesTable.ITEM_INDEX + " INTEGER" +
            ")";
    private static final String INIT_TRANSLATIONS_SQL =
            "CREATE TABLE " + DbManager.TranslationsTable.TABLE_NAME + " (" +
            DbManager.TranslationsTable.ID + " INTEGER PRIMARY KEY," +
            DbManager.TranslationsTable.DICTIONARY_ID + " INTEGER," +
            DbManager.TranslationsTable.LABEL + " TEXT," +
            DbManager.TranslationsTable.IS_ASSET + " INTEGER," +
            DbManager.TranslationsTable.FILENAME + " TEXT," +
            DbManager.TranslationsTable.ITEM_INDEX + " INTEGER," +
            DbManager.TranslationsTable.TRANSLATED_TEXT + " TEXT" +
            ")";

    // Update SQL.
    private static final String ALTER_TABLE_ADD_TRANSLATED_TEXT_COLUMN =
            "ALTER TABLE " + DbManager.TranslationsTable.TABLE_NAME + " ADD " +
            DbManager.TranslationsTable.TRANSLATED_TEXT + " TEXT";
    private static final String ALTER_TABLE_ADD_DECK_FOREIGN_KEY =
            "ALTER TABLE " + DbManager.DictionariesTable.TABLE_NAME + " ADD " +
            DbManager.DictionariesTable.DECK_ID + " INTEGER";

    private DbManager dbManager;
    private final Context context;

    public DbHelper(DbManager dbManager, Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.dbManager = dbManager;
        this.context = context;
    }

    public void onCreate(SQLiteDatabase db) {
        db.execSQL(INIT_DECKS_SQL);
        db.execSQL(INIT_DICTIONARIES_SQL);
        db.execSQL(INIT_TRANSLATIONS_SQL);
        long creationTimestamp = (new Date()).getTime();
        long defaultDeckId = dbManager.addDeck(
                db, context.getString(R.string.data_default_deck_name),
                context.getString(R.string.data_default_deck_publisher),
                creationTimestamp, null, null, false);
        populateIncludedData(db, defaultDeckId);
    }

    private void populateIncludedData(SQLiteDatabase db, long defaultDeckId) {
        for (int dictionaryIndex = 0; dictionaryIndex < dbManager.INCLUDED_DATA.length;
             dictionaryIndex++) {
            Dictionary dictionary = dbManager.INCLUDED_DATA[dictionaryIndex];
            long dictionaryId = dbManager.addDictionary(db, dictionary.getLabel(), dictionaryIndex,
                    defaultDeckId);
            for (int translationIndex = 0;
                 translationIndex < dictionary.getTranslationCount();
                 translationIndex++) {
                Dictionary.Translation translation =
                        dictionary.getTranslation(translationIndex);
                int itemIndex = dictionary.getTranslationCount() - translationIndex - 1;
                dbManager.addTranslation(db, dictionaryId, translation.getLabel(),
                        translation.getIsAsset(), translation.getFilename(), itemIndex,
                        translation.getTranslatedText());
            }
        }
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Translation text and the decks table were added in v2 of the database.
        if (oldVersion == 1) {
            db.execSQL(ALTER_TABLE_ADD_TRANSLATED_TEXT_COLUMN);
            db.execSQL(INIT_DECKS_SQL);
            db.execSQL(ALTER_TABLE_ADD_DECK_FOREIGN_KEY);
            long creationTimestamp = (new Date()).getTime() / 1000;
            long defaultDeckId = dbManager.addDeck(
                    db, context.getString(R.string.data_default_deck_name),
                    context.getString(R.string.data_default_deck_publisher),
                    creationTimestamp, null, null, false);
            ContentValues defaultDeckUpdateValues = new ContentValues();
            defaultDeckUpdateValues.put(DbManager.DictionariesTable.DECK_ID, defaultDeckId);
            db.update(DbManager.DictionariesTable.TABLE_NAME, defaultDeckUpdateValues, null, null);
        }
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Do nothing.
    }
}
