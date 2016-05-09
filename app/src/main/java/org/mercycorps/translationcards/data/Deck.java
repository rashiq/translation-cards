package org.mercycorps.translationcards.data;

import org.mercycorps.translationcards.MainApplication;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Contains information about a collection of phrases in one or more languages.
 *
 * @author nick.c.worden@gmail.com (Nick Worden)
 */
public class Deck implements Serializable {

    private String label;
    private String publisher;
    private String externalId;
    private long dbId;
    private long timestamp;
    private boolean locked;
    private String srcLanguageIso;
    // The dictionaries list is lazily initialized.
    private List<Dictionary> dictionaries;

    public Deck(String label, String publisher, String externalId, long dbId, long timestamp,
                boolean locked, String srcLanguageIso) {
        this.label = label;
        this.publisher = publisher;
        this.externalId = externalId;
        this.dbId = dbId;
        this.timestamp = timestamp;
        this.locked = locked;
        this.srcLanguageIso = srcLanguageIso;
        dictionaries = null;
    }

    public Deck(String label, String publisher, String externalId, int i, long timestamp, boolean locked,
                String srcLanguageIso, List<Dictionary> dictionaries) {
        this(label, publisher, externalId, -1, timestamp, locked, srcLanguageIso);

        this.dictionaries = dictionaries;
    }

    public Deck() {

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
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yy", Locale.US);
        return dateFormat.format(date);
    }

    public boolean isLocked() {
        return locked;
    }

    public String getSrcLanguageIso() {
        return srcLanguageIso;
    }

    public List<Dictionary> getDictionaries() {
        if (dictionaries == null) {
            MainApplication contextFromMainApp = (MainApplication) MainApplication.getContextFromMainApp();
            dictionaries = Arrays.asList(contextFromMainApp.getDbManager().getAllDictionariesForDeck(dbId));
        }
        return dictionaries;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public Long save() {
        return ((MainApplication) MainApplication.getContextFromMainApp()).getDbManager().addDeck(label, publisher, timestamp, externalId, "", locked, srcLanguageIso);
    }

    public void delete() {
        ((MainApplication) MainApplication.getContextFromMainApp()).getDbManager().deleteDeck(dbId);
    }

    public void deleteTranslation(String sourcePhrase) {
        for (Dictionary dictionary : dictionaries) {
            dictionary.deleteTranslation(sourcePhrase);
//            Translation translationBySourcePhrase = dictionary.getTranslationBySourcePhrase(translation.getLabel());
//            dbManager.deleteTranslation(translationBySourcePhrase.getDbId());
        }
//        dictionaries = dbManager.getAllDictionariesForDeck(deck.getDbId());
//        setDictionary(currentDictionaryIndex);
//        listAdapter.notifyDataSetChanged();
    }
}
