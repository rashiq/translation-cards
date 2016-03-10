/*
 * Copyright (C) 2015 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.mercycorps.translationcards.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.google.inject.Inject;

import java.io.File;

/**
 * Manages database operations.
 *
 * @author nick.c.worden@gmail.com (Nick Worden)
 */
public class DbManager {

    private static DbManager instance = null;
    private static final String TAG = "DbManager";

    // The value used in place of database IDs for items not yet in the database.
    private static final long NO_VALUE_ID = -1;

    private final DbHelper dbh;

    @Inject
    public DbManager(Context context) {
        this.dbh = new DbHelper(this, context);
    }

    public Dictionary[] getAllDictionariesForDeck(long deckId) {
        Cursor cursor = dbh.getReadableDatabase().query(
                DictionariesTable.TABLE_NAME, null,
                DictionariesTable.DECK_ID + " = ?",
                new String[]{String.valueOf(deckId)}, null, null,
                String.format("%s ASC", DictionariesTable.LABEL));

        Dictionary[] dictionaries = new Dictionary[cursor.getCount()];
        boolean hasNext = cursor.moveToFirst();
        int i = 0;
        while (hasNext) {
            String label = cursor.getString(cursor.getColumnIndex(DictionariesTable.LABEL));
            long dictionaryId = cursor.getLong(cursor.getColumnIndex(DictionariesTable.ID));
            Dictionary dictionary = new Dictionary(label, getTranslationsByDictionaryId(dictionaryId), dictionaryId, deckId);
            dictionaries[i] = dictionary;
            i++;
            hasNext = cursor.moveToNext();
        }
        cursor.close();
        return dictionaries;
    }

    public long addDeck(SQLiteDatabase writableDatabase, String label, String publisher,
                        long creationTimestamp, String externalId, String hash, boolean locked) {
        ContentValues values = new ContentValues();
        values.put(DecksTable.LABEL, label);
        values.put(DecksTable.PUBLISHER, publisher);
        values.put(DecksTable.CREATION_TIMESTAMP, creationTimestamp);
        values.put(DecksTable.EXTERNAL_ID, externalId);
        values.put(DecksTable.HASH, hash);
        values.put(DecksTable.LOCKED, locked ? 1 : 0);
        return writableDatabase.insert(DecksTable.TABLE_NAME, null, values);
    }

    public long addDeck(String label, String publisher, long creationTimestamp, String externalId,
                        String hash, boolean locked) {
        return addDeck(dbh.getWritableDatabase(), label, publisher, creationTimestamp, externalId,
                hash, locked);
    }

    public void deleteDeck(long deckId) {
        Dictionary[] dictionaries = getAllDictionariesForDeck(deckId);
        for (Dictionary dictionary : dictionaries) {
            // Delete all the files.
            for (int i = 0; i < dictionary.getTranslationCount(); i++) {
                Dictionary.Translation translation = dictionary.getTranslation(i);
                if (translation.getIsAsset()) {
                    // Don't delete the built-in assets.
                    continue;
                }
                File file = new File(translation.getFilename());
                if (file.exists()) {
                    // It should always exist, but check to be safe.
                    file.delete();
                }
            }
            // Delete the rows in the translations table.
            String whereClause = TranslationsTable.DICTIONARY_ID + " = ?";
            String[] whereArgs = new String[] {String.valueOf(dictionary.getDbId())};
            dbh.getWritableDatabase().delete(TranslationsTable.TABLE_NAME, whereClause, whereArgs);
        }
        // Delete the rows in the dictionaries table.
        String whereClause = DictionariesTable.DECK_ID + " = ?";
        String[] whereArgs = new String[] {String.valueOf(deckId)};
        dbh.getWritableDatabase().delete(DictionariesTable.TABLE_NAME, whereClause, whereArgs);
        // Delete the row from the deck table.
        whereClause = DecksTable.ID + " = ?"; // whereArgs remain the same
        dbh.getWritableDatabase().delete(DecksTable.TABLE_NAME, whereClause, whereArgs);
    }

    public long addDictionary(SQLiteDatabase writableDatabase, String label, int itemIndex,
                              long deckId) {
        ContentValues values = new ContentValues();
        values.put(DictionariesTable.LABEL, label);
        values.put(DictionariesTable.ITEM_INDEX, itemIndex);
        values.put(DictionariesTable.DECK_ID, deckId);
        return writableDatabase.insert(DictionariesTable.TABLE_NAME, null, values);
    }

    public long addDictionary(String label, int itemIndex, long deckId) {
        return addDictionary(dbh.getWritableDatabase(), label, itemIndex, deckId);
    }

    public long addTranslation(SQLiteDatabase writableDatabase,
            long dictionaryId, String label, boolean isAsset, String filename, int itemIndex, String translatedText) {
        Log.d(TAG, "Inserting translation...");
        ContentValues values = new ContentValues();
        values.put(TranslationsTable.DICTIONARY_ID, dictionaryId);
        values.put(TranslationsTable.LABEL, label);
        values.put(TranslationsTable.IS_ASSET, isAsset ? 1 : 0);
        values.put(TranslationsTable.FILENAME, filename);
        values.put(TranslationsTable.ITEM_INDEX, itemIndex);
        values.put(TranslationsTable.TRANSLATED_TEXT, translatedText);
        return writableDatabase.insert(TranslationsTable.TABLE_NAME, null, values);
    }

    public long addTranslation(
            long dictionaryId, String label, boolean isAsset, String filename, int itemIndex, String translatedText) {
        long translationId = addTranslation(
                dbh.getWritableDatabase(), dictionaryId, label, isAsset, filename, itemIndex, translatedText);
        dbh.close();
        return translationId;
    }

    public long addTranslationAtTop(
            long dictionaryId, String label, boolean isAsset, String filename, String translatedText) {
        String maxColumnName = String.format("MAX(%s)", TranslationsTable.ITEM_INDEX);
        Cursor cursor = dbh.getReadableDatabase().query(
                TranslationsTable.TABLE_NAME, new String[]{maxColumnName},
                String.format("%s = ?", TranslationsTable.DICTIONARY_ID),
                new String[]{String.format("%d", dictionaryId)},
                null, null, null);
        if (!cursor.moveToFirst()) {
            return addTranslation(dictionaryId, label, isAsset, filename, 0, translatedText);
        }
        int itemIndex = cursor.getInt(cursor.getColumnIndex(maxColumnName)) + 1;
        cursor.close();
        dbh.close();
        return addTranslation(dictionaryId, label, isAsset, filename, itemIndex, translatedText);
    }

    public void updateTranslation(
            long translationId, String label, boolean isAsset, String filename, String translatedText) {
        ContentValues values = new ContentValues();
        values.put(TranslationsTable.LABEL, label);
        values.put(TranslationsTable.IS_ASSET, isAsset);
        values.put(TranslationsTable.FILENAME, filename);
        values.put(TranslationsTable.TRANSLATED_TEXT, translatedText);
        String whereClause = String.format("%s = ?", TranslationsTable.ID);
        String[] whereArgs = new String[] {String.format("%d", translationId)};
        dbh.getWritableDatabase().update(
                TranslationsTable.TABLE_NAME, values, whereClause, whereArgs);
        dbh.close();
    }

    public void deleteTranslation(long translationId) {

        String whereClause = String.format("%s = ?", TranslationsTable.ID);
        String[] whereArgs = new String[] {String.format("%d", translationId)};
        dbh.getWritableDatabase().delete(TranslationsTable.TABLE_NAME, whereClause, whereArgs);
        dbh.close();
    }

    public Deck[] getAllDecks() {
        Cursor cursor = dbh.getReadableDatabase().query(
                DecksTable.TABLE_NAME, null,
                null, null, null, null,
                String.format("%s DESC", DecksTable.ID));
        Deck[] decks = new Deck[cursor.getCount()];
        boolean hasNext = cursor.moveToFirst();
        int i = 0;
        while(hasNext){
            Deck deck = new Deck(cursor.getString(cursor.getColumnIndex(DecksTable.LABEL)),
                    cursor.getString(cursor.getColumnIndex(DecksTable.PUBLISHER)),
                    cursor.getString(cursor.getColumnIndex(DecksTable.EXTERNAL_ID)),
                    cursor.getLong(cursor.getColumnIndex(DecksTable.ID)),
                    cursor.getLong(cursor.getColumnIndex(DecksTable.CREATION_TIMESTAMP)),
                    cursor.getInt(cursor.getColumnIndex(DecksTable.LOCKED)) == 1, "");

            decks[i] = deck;
            hasNext = cursor.moveToNext();
            i++;
        }
        cursor.close();
        dbh.close();
        return decks;
    }

    public boolean hasDeckWithHash(String hash) {
        String[] columns = new String[] {DecksTable.ID};
        String selection = DecksTable.HASH + " = ?";
        String[] selectionArgs = new String[] {hash};
        Cursor cursor = dbh.getReadableDatabase().query(
                DecksTable.TABLE_NAME, columns, selection, selectionArgs, null, null, null);
        boolean result = cursor.getCount() > 0;
        cursor.close();
        return result;
    }

    public long hasDeckWithExternalId(String externalId) {
        // TODO(nworden): consider handling this better when there's multiple existing decks with
        // this external ID
        String[] columns = new String[] {DecksTable.ID};
        String selection = DecksTable.EXTERNAL_ID + " = ?";
        String[] selectionArgs = new String[] {externalId};
        Cursor cursor = dbh.getReadableDatabase().query(
                DecksTable.TABLE_NAME, columns, selection, selectionArgs, null, null,
                String.format("%s DESC", DecksTable.CREATION_TIMESTAMP), "1");
        if (cursor.getCount() == 0) {
            cursor.close();
            return -1;
        }
        cursor.moveToFirst();
        long result = cursor.getLong(cursor.getColumnIndexOrThrow(DecksTable.ID));
        cursor.close();
        return result;
    }

    private Dictionary.Translation[] getTranslationsByDictionaryId(long dictionaryId) {
        Cursor cursor = dbh.getReadableDatabase().query(TranslationsTable.TABLE_NAME, null,
                TranslationsTable.DICTIONARY_ID + " = ?", new String[]{String.valueOf(dictionaryId)},
                null, null, String.format("%s DESC", TranslationsTable.ITEM_INDEX));
        Dictionary.Translation[] translations = new Dictionary.Translation[cursor.getCount()];
        boolean hasNext = cursor.moveToFirst();
        int i=0;
        while(hasNext){
            Dictionary.Translation translation = new Dictionary.Translation(
                    cursor.getString(cursor.getColumnIndex(TranslationsTable.LABEL)),
                    cursor.getInt(cursor.getColumnIndex(TranslationsTable.IS_ASSET)) == 1,
                    cursor.getString(cursor.getColumnIndex(TranslationsTable.FILENAME)),
                    cursor.getLong(cursor.getColumnIndex(TranslationsTable.ID)),
                    cursor.getString(cursor.getColumnIndex(TranslationsTable.TRANSLATED_TEXT))
            );
            translations[i] = translation;
            i++;
            hasNext = cursor.moveToNext();
        }
        cursor.close();
        dbh.close();
        return translations;
    }

    public String getTranslationLanguagesForDeck(long deckDbId) {
        Cursor cursor = dbh.getReadableDatabase().query(
                DictionariesTable.TABLE_NAME,
                new String[]{DictionariesTable.LABEL},
                DictionariesTable.DECK_ID + " = ?",
                new String[]{String.valueOf(deckDbId)}, null, null,
                String.format("%s ASC", DictionariesTable.LABEL));

        String translationLanguages = "";
        String delimiter = "   ";
        boolean hasNext = cursor.moveToFirst();
        while(hasNext) {
            String translationLanguage = cursor.getString(cursor.getColumnIndex(DictionariesTable.LABEL));
            translationLanguages += translationLanguage.toUpperCase() + delimiter;
            hasNext = cursor.moveToNext();
        }
        cursor.close();
        dbh.close();
        return translationLanguages.trim();
    }

    public static void init(Context context) {
        instance = new DbManager(context);
    }

    public static DbManager getDbManager() {
        if (instance == null) {
            throw new NullPointerException("DbManager has not been initialized properly. Call DbManger.init(Context) in your DbManager.onCreate() method and DbManager.terminate() in your Application.onTerminate() method.");
        }
        return instance;
    }

    public static void terminate() {
        if (instance == null) {
            return;
        }
        instance.doTerminate();
    }

    private void doTerminate() {
        if (this.dbh != null) {
            this.dbh.getReadableDatabase().close();
        }
    }

    public class DecksTable {
        public static final String TABLE_NAME = "decks";
        public static final String ID = "id";
        public static final String LABEL = "label";
        public static final String PUBLISHER = "publisher";
        public static final String CREATION_TIMESTAMP = "creationTimestamp";
        public static final String EXTERNAL_ID = "externalId";
        public static final String HASH = "hash";
        public static final String LOCKED = "locked";
    }

    public class DictionariesTable {
        public static final String TABLE_NAME = "dictionaries";
        public static final String ID = "id";
        public static final String DECK_ID = "deckId";
        public static final String LABEL = "label";
        public static final String ITEM_INDEX = "itemIndex";
    }

    public class TranslationsTable {
        public static final String TABLE_NAME = "translations";
        public static final String ID = "id";
        public static final String DICTIONARY_ID = "dictionaryId";
        public static final String LABEL = "label";
        public static final String IS_ASSET = "isAsset";
        public static final String FILENAME = "filename";
        public static final String ITEM_INDEX = "itemIndex";
        public static final String TRANSLATED_TEXT = "translationText";
    }

    public final Dictionary[] INCLUDED_DATA = new Dictionary[] {
            new Dictionary("Arabic", new Dictionary.Translation[] {}, NO_VALUE_ID, NO_VALUE_ID),
            new Dictionary("Farsi", new Dictionary.Translation[] {}, NO_VALUE_ID, NO_VALUE_ID),
            new Dictionary("Pashto", new Dictionary.Translation[] {}, NO_VALUE_ID, NO_VALUE_ID),
    };
}
