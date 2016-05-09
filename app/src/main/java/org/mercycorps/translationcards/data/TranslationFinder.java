package org.mercycorps.translationcards.data;

import android.database.Cursor;

import static org.mercycorps.translationcards.data.DbManager.TranslationsTable.*;

public class TranslationFinder {
    private final DbManager.DbHelper databaseHelper;

    public TranslationFinder(DbManager.DbHelper databaseHelper) {
        this.databaseHelper = databaseHelper;
    }


    public Translation[] getTranslationsByDictionaryId(long dictionaryId) {
        Cursor cursor = databaseHelper.getReadableDatabase().query(TABLE_NAME, null,
                DICTIONARY_ID + " = ?", new String[]{String.valueOf(dictionaryId)},
                null, null, String.format("%s DESC", ITEM_INDEX));
        Translation[] translations = new Translation[cursor.getCount()];
        boolean hasNext = cursor.moveToFirst();
        int i=0;
        while(hasNext){
            Translation translation = new Translation(
                    cursor.getString(cursor.getColumnIndex(LABEL)),
                    cursor.getInt(cursor.getColumnIndex(IS_ASSET)) == 1,
                    cursor.getString(cursor.getColumnIndex(FILENAME)),
                    cursor.getLong(cursor.getColumnIndex(ID)),
                    cursor.getString(cursor.getColumnIndex(TRANSLATED_TEXT))
            );
            translations[i] = translation;
            i++;
            hasNext = cursor.moveToNext();
        }
        cursor.close();
        databaseHelper.close();
        return translations;
    }
}
