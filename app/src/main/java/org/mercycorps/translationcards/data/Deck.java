package org.mercycorps.translationcards.data;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * Contains information about a collection of phrases in one or more languages.
 *
 * @author nick.c.worden@gmail.com (Nick Worden)
 */
public class Deck implements Serializable {

    private final String label;
    private final String publisher;
    private final String externalId;
    private long dbId;
    private final long timestamp;
    private final boolean locked;
    private String hash;

    public Deck(String label, String publisher, String externalId, long dbId, long timestamp,
                boolean locked, String hash) {
        this.label = label;
        this.publisher = publisher;
        this.externalId = externalId;
        this.dbId = dbId;
        this.timestamp = timestamp;
        this.locked = locked;
        this.hash = hash;
    }

    public Deck(String label, String publisher, String externalId, long timestamp, boolean locked) {
        this(label, publisher, externalId, -1, timestamp, locked, "");
    }

    public String getLabel() {
        return label;
    }

    public String getPublisher() {
        return publisher;
    }

    public String getExternalId() {
        return externalId;
    }

    public long getDbId() {
        return dbId;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getCreationDateString() {
        Date date = new Date(timestamp);
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yy");
        String formattedDate = dateFormat.format(date);
        return formattedDate;
    }

    public boolean isLocked() {
        return locked;
    }

    public String getTranslationLanguages() {
        return DbManager.getDbManager().getTranslationLanguagesForDeck(dbId);
    }

    public void delete() {
        DbManager.getDbManager().deleteDeck(dbId);
    }

    public long save() {
        dbId = DbManager.getDbManager().addDeck(label, publisher, timestamp, externalId, hash, locked);
        return dbId;
    }

    public Dictionary[] getAllDictionaries() {
        return DbManager.getDbManager().getAllDictionariesForDeck(dbId);
    }
}
