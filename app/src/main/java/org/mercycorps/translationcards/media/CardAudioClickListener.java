package org.mercycorps.translationcards.media;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import org.mercycorps.translationcards.data.Translation;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * Created by njimenez and pdale on 2/18/16.
 */
public class CardAudioClickListener implements View.OnClickListener {
    private static final String TAG = "CardAudioClickListener";
    private final Context context;
    private Translation translation;
    private final ProgressBar progressBar;
    private MediaPlayerManager lastMediaPlayerManager;

    public CardAudioClickListener(Context context, Translation translation, ProgressBar progressBar,
                                  MediaPlayerManager lastMediaPlayerManager) {
        this.context = context;
        this.translation = translation;
        this.progressBar = progressBar;
        this.lastMediaPlayerManager = lastMediaPlayerManager;
    }

    @Override
    public void onClick(View v) {
        if(lastMediaPlayerManager.isCurrentlyPlayingSameCard(translation)) {
            stopMediaPlayer();
        } else {
            stopMediaPlayer();
            lastMediaPlayerManager.play(context, progressBar, translation);
        }
    }

    public void stopMediaPlayer() {
        if (lastMediaPlayerManager != null) {
            lastMediaPlayerManager.stop();
        }
    }
}