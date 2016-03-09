package org.mercycorps.translationcards.data;

import com.orm.SugarRecord;
import com.orm.dsl.Column;
import com.orm.dsl.Ignore;
import com.orm.dsl.Table;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Contains information about a collection of phrases in one or more languages.
 *
 * @author nick.c.worden@gmail.com (Nick Worden)
 */
@Table(name = "decks")
public class Deck extends SugarRecord implements Serializable {

    private String label;
    private String publisher;
    @Column(name = "creationTimestamp")
    private long creationTimestamp;
    @Column(name = "externalId")
    private String externalId;
    private String hash;
    private boolean locked;

    public Deck() {}

    public Deck(String label, String publisher, String externalId, long timestamp,
                boolean locked, String hash) {
        this.label = label;
        this.publisher = publisher;
        this.externalId = externalId;
        this.creationTimestamp = timestamp;
        this.locked = locked;
        this.hash = hash;
    }

    public Deck(String label, String publisher, String externalId, long timestamp, boolean locked) {
        this(label, publisher, externalId, timestamp, locked, "");
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

    public long getTimestamp() {
        return creationTimestamp;
    }

    public String getCreationDateString() {
        Date date = new Date(creationTimestamp);
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yy");
        String formattedDate = dateFormat.format(date);
        return formattedDate;
    }

    public boolean isLocked() {
        return locked;
    }

    public List<Dictionary> getDictionaries() {
        return Dictionary.find(Dictionary.class, "deckId = ?", new String[] {getId().toString()}, null, "label ASC", "");
    }

    public String getDictionaryLanguages() {
        String dictionaryLanguages = "";
        String delimiter = "   ";
        for (Dictionary dictionary : getDictionaries()) {
            dictionaryLanguages = dictionaryLanguages.concat(dictionary.getLabel().toUpperCase()).concat(delimiter);
        }
        return dictionaryLanguages.trim();
    }

    @Override
    public boolean delete() {
        for (Dictionary dictionary : getDictionaries()) {
            dictionary.delete();
        }

        return super.delete();
    }
}
