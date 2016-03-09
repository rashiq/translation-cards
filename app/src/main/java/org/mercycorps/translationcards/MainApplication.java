package org.mercycorps.translationcards;

import android.media.AudioManager;
import android.media.MediaPlayer;

import com.orm.SugarApp;
import com.orm.SugarRecord;

import org.mercycorps.translationcards.data.Deck;
import org.mercycorps.translationcards.data.Dictionary;
import org.mercycorps.translationcards.media.MediaPlayerManager;

import java.util.Date;
import java.util.List;

/**
 * Used to create singletons for dependency injection.
 *
 * @author patdale216@gmail.com (Pat Dale)
 * @author natashaj7@hotmail.com (Natasha Jimenez)
 */
public class MainApplication extends SugarApp {

    private MediaPlayerManager mediaPlayerManager;

    @Override
    public void onCreate() {
        super.onCreate();

        createDefaultDeck();

        MediaPlayer mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mediaPlayerManager = new MediaPlayerManager(mediaPlayer);
    }

    private void createDefaultDeck() {
        if(SugarRecord.first(Deck.class) == null) {
            long deckId = new Deck("Default", "My Deck", "", new Date().getTime(), false).save();

            List<Dictionary> dictionaryList = SugarRecord.find(Dictionary.class, "deckId IS NULL");
            if (dictionaryList.isEmpty()) {
                new Dictionary("Arabic", deckId).save();
                new Dictionary("Pashto", deckId).save();
                new Dictionary("Farsi", deckId).save();
            } else {
                for (Dictionary dictionary : dictionaryList) {
                    dictionary.setDeckId(deckId);
                    dictionary.save();
                }
            }
        }
    }

    public MediaPlayerManager getMediaPlayerManager() {
        return mediaPlayerManager;
    }
}
