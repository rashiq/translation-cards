package org.mercycorps.translationcards.data;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;

import com.orm.SugarRecord;
import com.orm.dsl.Column;
import com.orm.dsl.Table;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.List;

/**
 * Contains information about a single phrase.
 */
@Table(name = "translations")
public class Translation extends SugarRecord implements Serializable {

    public static final String DEFAULT_TRANSLATED_TEXT = "";
    @Column(name = "dictionaryId")
    private Long dictionaryId;
    private String label;
    @Column(name = "isAsset")
    private boolean isAsset;
    private String filename;
    @Column(name = "itemIndex")
    private int itemIndex;
    @Column(name = "translatedText")
    private String translatedText;

    public Translation() {
    }

    public Translation(String label, boolean isAsset, String filename, String translatedText, long dictionaryId) {
        this.label = label;
        this.isAsset = isAsset;
        this.filename = filename;
        this.translatedText = translatedText;
        this.dictionaryId = dictionaryId;
    }

    @Override
    public long save() {
        List<Translation> translations = SugarRecord.findWithQuery(Translation.class,
                "SELECT MAX(itemIndex) FROM translations WHERE dictionaryId = ?",
                String.valueOf(dictionaryId));

        if (translations.isEmpty()) {
            this.itemIndex = 1;
            return super.save();
        }

        this.itemIndex += 1;
        return super.save();
    }

    public String getLabel() {
        return label;
    }

    public boolean getIsAsset() {
        return isAsset;
    }

    public String getFilename() {
        return filename;
    }

    public String getTranslatedText() {
        return translatedText == null ? DEFAULT_TRANSLATED_TEXT : translatedText;
    }


    public void setMediaPlayerDataSource(Context context, MediaPlayer mp) throws IOException {
        if (isAsset) {
            AssetFileDescriptor fd = context.getAssets().openFd(filename);
            mp.setDataSource(fd.getFileDescriptor(), fd.getStartOffset(), fd.getLength());
            fd.close();
        } else {
            mp.setDataSource(new FileInputStream(filename).getFD());
        }
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public void setIsAsset(boolean isAsset) {
        this.isAsset = isAsset;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public void setTranslatedText(String translatedText) {
        this.translatedText = translatedText;
    }
}
