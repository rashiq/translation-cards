package org.mercycorps.translationcards;

import android.app.Application;
import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;

import org.mercycorps.translationcards.data.DbManager;
import org.mercycorps.translationcards.media.MediaPlayerManager;

/**
 * Used to create singletons for dependency injection.
 *
 * @author patdale216@gmail.com (Pat Dale)
 * @author natashaj7@hotmail.com (Natasha Jimenez)
 */
public class MainApplication extends Application {

    private MediaPlayerManager mediaPlayerManager;

    @Override
    public void onCreate() {
        super.onCreate();
        MediaPlayer mediaPlayer = new MediaPlayer();
        DbManager.init(this);
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mediaPlayerManager = new MediaPlayerManager(mediaPlayer);
    }

    public MediaPlayerManager getMediaPlayerManager() {
        return mediaPlayerManager;
    }

    @Override
    public void onTerminate() {
        DbManager.terminate();
        super.onTerminate();
    }
}
