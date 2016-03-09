package org.mercycorps.translationcards.activity;

import android.content.Intent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mercycorps.translationcards.BuildConfig;
import org.mercycorps.translationcards.R;
import org.mercycorps.translationcards.data.Deck;
import org.mercycorps.translationcards.data.Dictionary;
import org.mercycorps.translationcards.data.Translation;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.Shadows;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowActivity;
import org.robolectric.shadows.ShadowAlertDialog;

import roboguice.RoboGuice;

import static junit.framework.Assert.assertTrue;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.robolectric.Shadows.shadowOf;

/**
 * Test for TranslationsActivity
 *
 * @author patdale216@gmail.com (Pat Dale)
 */
@Config(constants = BuildConfig.class, sdk = 21)
@RunWith(RobolectricGradleTestRunner.class)
public class TranslationsActivityTest {

    public static final int DEFAULT_DECK_ID = 1;
    public static final String NO_VALUE = "";
    public static final long DEFAULT_LONG = -1;
    public static final String DICTIONARY_TEST_LABEL = "TestLabel";
    public static final String TRANSLATED_TEXT = "TranslatedText";
    public static final String TRANSLATION_LABEL = "TranslationLabel";
    public static final String DEFAULT_DECK_NAME = "Default";
    private TranslationsActivity translationsActivity;

    @Before
    public void setUp() {
        RoboGuice.setUseAnnotationDatabases(false);
        Intent intent = new Intent();
        Deck deck = new Deck(DEFAULT_DECK_NAME, NO_VALUE, NO_VALUE, DEFAULT_LONG, false, "");
        deck.save();
        Dictionary dictionary = new Dictionary(DICTIONARY_TEST_LABEL, deck.getId());
        dictionary.save();
        new Translation(TRANSLATION_LABEL, false, NO_VALUE, TRANSLATED_TEXT, dictionary.getId()).save();
        new Translation(TRANSLATION_LABEL, false, NO_VALUE, null, dictionary.getId()).save();
        intent.putExtra("Deck", deck.getId());
        translationsActivity = Robolectric.buildActivity(TranslationsActivity.class).withIntent(
                intent).create().get();
    }

    @Test
    public void onCreate_shouldShowDeckNameInToolbar() {
        assertThat(translationsActivity.getSupportActionBar().getTitle().toString(), is(
                DEFAULT_DECK_NAME));
    }

    @Test
    public void onCreate_shouldInitializeCards() {
        ListView translationsList = (ListView) translationsActivity
                .findViewById(R.id.translations_list);
        View translationsListItem = translationsList.getAdapter().getView(1, null, translationsList);

        TextView originTranslationText = (TextView) translationsListItem.findViewById(
                R.id.origin_translation_text);
        assertThat(originTranslationText.getText().toString(), is(TRANSLATION_LABEL));

        TextView translatedText = (TextView) translationsListItem.findViewById(R.id.translated_text);
        assertThat(translatedText.getText().toString(), is(TRANSLATED_TEXT));

        ImageView editCardIcon = (ImageView) translationsListItem.findViewById(R.id.edit_card_icon);
        assertThat(editCardIcon, is(notNullValue()));

        TextView editCardLabel = (TextView) translationsListItem.findViewById(R.id.edit_card_label);
        assertThat(editCardLabel.getText().toString(), is("Edit this flashcard"));

        ImageView deleteCardIcon = (ImageView) translationsListItem.findViewById(R.id.delete_card_icon);
        assertThat(deleteCardIcon, is(notNullValue()));

        TextView deleteCardLabel = (TextView) translationsListItem.findViewById(R.id.delete_card_label);
        assertThat(deleteCardLabel.getText().toString(), is("Delete this flashcard"));
    }

    @Test
    public void shouldDisplayAndFormatNoTranslationTextStringWhenTranslatedTextLeftEmpty() {
        int disabledTextColor = -7960954;
        ListView translationsList = (ListView) translationsActivity.findViewById(
                R.id.translations_list);
        View translationsListItem = translationsList.getAdapter().getView(2, null, translationsList);

        TextView translatedText = (TextView) translationsListItem.findViewById(R.id.translated_text);
        assertThat(translatedText.getText().toString(), is(
                "Add " + DICTIONARY_TEST_LABEL + " translation"));
        assertThat(translatedText.getTextSize(), is(18f));
        assertThat(translatedText.getCurrentTextColor(), is(disabledTextColor));
    }

    @Test
    public void onClick_shouldStartRecordingActivityWhenEditLayoutIsClicked() {
        ListView translationsList = (ListView) translationsActivity
                .findViewById(R.id.translations_list);
        View translationsListItem = translationsList.getAdapter().getView(1, null, translationsList);

        translationsListItem.findViewById(R.id.translation_card_edit).performClick();

        Intent nextStartedActivity = shadowOf(translationsActivity).getNextStartedActivity();
        assertThat(nextStartedActivity.getComponent().getClassName(), is(
                RecordingActivity.class.getCanonicalName()));
        String dictionaryLabel = nextStartedActivity.getStringExtra(
                RecordingActivity.INTENT_KEY_DICTIONARY_LABEL);
        assertThat(dictionaryLabel, is(DICTIONARY_TEST_LABEL));
    }

    @Test
    public void onClick_shouldShowDeleteConfirmationDialogWhenDeleteLayoutIsClicked(){
        ListView translationsList = (ListView) translationsActivity.findViewById(
                R.id.translations_list);

        View translationsListItem = translationsList.getAdapter().getView(1, null, translationsList);
        translationsListItem.findViewById(R.id.translation_card_delete).performClick();

        ShadowAlertDialog shadowAlertDialog = shadowOf(ShadowAlertDialog.getLatestAlertDialog());
        assertThat(shadowAlertDialog.getMessage().toString(),
                is("Are you sure you want to delete this translation card?"));
    }

    @Test
    public void onClick_shouldInflateAndDeflateTranslationListItem() {
        ListView translationsList = (ListView) translationsActivity
                .findViewById(R.id.translations_list);

        View translationsListItem = translationsList.getAdapter().getView(1, null, translationsList);

        ImageView cardIndicator = (ImageView) translationsListItem.findViewById(R.id.indicator_icon);

        assertThat(translationsListItem.findViewById(R.id.translation_child).getVisibility(), is(
                View.GONE));
        assertThat(shadowOf(cardIndicator.getBackground()).getCreatedFromResId(), is(
                R.drawable.expand_arrow));

        translationsListItem.findViewById(R.id.translation_indicator_layout).performClick();
        assertThat(translationsListItem.findViewById(R.id.translation_child).getVisibility(), is(
                View.VISIBLE));
        assertThat(shadowOf(cardIndicator.getBackground()).getCreatedFromResId(), is(
                R.drawable.collapse_arrow));

        translationsListItem.findViewById(R.id.translation_indicator_layout).performClick();
        assertThat(translationsListItem.findViewById(R.id.translation_child).getVisibility(), is(
                View.GONE));
        assertThat(shadowOf(cardIndicator.getBackground()).getCreatedFromResId(), is(
                R.drawable.expand_arrow));
    }

    @Test
    public void initTabs_shouldShowLanguageTabWhenOnHomeScreen() {
        LinearLayout tabContainer = (LinearLayout) translationsActivity.findViewById(R.id.tabs);

        assertThat(tabContainer.getChildCount(), is(1));

        View languageTab = tabContainer.getChildAt(0);
        TextView languageTabText = (TextView) languageTab.findViewById(R.id.tab_label_text);
        assertThat(languageTabText.getText().toString(), is(DICTIONARY_TEST_LABEL.toUpperCase()));
    }

    @Test
    public void setDictionary_shouldNotHaveAnyTranslationCardsWhenNoneHaveBeenCreated() {
        TextView translationCardText = (TextView) translationsActivity.findViewById(
                R.id.origin_translation_text);

        assertThat(translationCardText, is(nullValue()));
    }

    @Test
    public void shouldGoToDecksActivityWhenBackButtonPressed() {
        ShadowActivity shadowActivity = Shadows.shadowOf(translationsActivity);

        shadowActivity.onBackPressed();
        assertTrue(shadowActivity.isFinishing());
    }

    @Ignore
    @Test
    public void onPause_shouldStopPlayingMediaPlayerManagerWhenActivityPaused() {
    }
}