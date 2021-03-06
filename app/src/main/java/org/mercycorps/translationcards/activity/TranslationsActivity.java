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

package org.mercycorps.translationcards.activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.SwitchCompat;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.mercycorps.translationcards.MainApplication;
import org.mercycorps.translationcards.R;
import org.mercycorps.translationcards.activity.addTranslation.AddNewTranslationContext;
import org.mercycorps.translationcards.activity.addTranslation.AddTranslationActivity;
import org.mercycorps.translationcards.activity.addTranslation.EnterSourcePhraseActivity;
import org.mercycorps.translationcards.activity.addTranslation.GetStartedActivity;
import org.mercycorps.translationcards.activity.addTranslation.NewTranslation;
import org.mercycorps.translationcards.data.DbManager;
import org.mercycorps.translationcards.data.Deck;
import org.mercycorps.translationcards.data.Dictionary;
import org.mercycorps.translationcards.data.Translation;
import org.mercycorps.translationcards.media.CardAudioClickListener;
import org.mercycorps.translationcards.media.DecoratedMediaManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import butterknife.Bind;
import butterknife.OnClick;

/**
 * Activity for the main screen, with lists of phrases to play.
 *
 * @author nick.c.worden@gmail.com (Nick Worden)
 */
public class TranslationsActivity extends AbstractTranslationCardsActivity {

    public static final String INTENT_KEY_DECK = "Deck";
    private static final String TAG = "TranslationsActivity";
    private static final int REQUEST_KEY_ADD_CARD = 1;
    private static final int REQUEST_KEY_EDIT_CARD = 2;
    public static final String INTENT_KEY_CURRENT_DICTIONARY_INDEX = "CurrentDictionaryIndex";
    private static final boolean IS_EDIT = true;


    @Bind(R.id.add_translation_button) RelativeLayout addTranslationButton;

    DbManager dbManager;
    private Dictionary[] dictionaries;
    private int currentDictionaryIndex;
    private TextView[] languageTabTextViews;
    private View[] languageTabBorders;
    private CardListAdapter listAdapter;
    private Deck deck;
    private List<Boolean> translationCardStates;
    private DecoratedMediaManager decoratedMediaManager;
    private Boolean hideTranslationsWithoutAudioToggle;

    @Override
    public void inflateView() {
        MainApplication application = (MainApplication) getApplication();
        decoratedMediaManager = application.getDecoratedMediaManager();
        dbManager = application.getDbManager();
        deck = (Deck) getIntent().getSerializableExtra(INTENT_KEY_DECK);
        dictionaries = dbManager.getAllDictionariesForDeck(deck.getDbId());
        currentDictionaryIndex = getIntent().getIntExtra(INTENT_KEY_CURRENT_DICTIONARY_INDEX, 0);
        setContentView(R.layout.activity_translations);
        translationCardStates = new ArrayList<>(Arrays.asList(new Boolean[dictionaries[currentDictionaryIndex].getTranslationCount()]));
        Collections.fill(translationCardStates, Boolean.FALSE);
        hideTranslationsWithoutAudioToggle = false;

        initTabs();
        initList();
        setDictionary(currentDictionaryIndex);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(deck.getLabel());
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setElevation(0);
    }

    @Override
    protected void initStates() {
        updateAddTranslationButtonVisibility();
    }

    private void updateHeader() {
        Dictionary currentDictionary = dictionaries[currentDictionaryIndex];

        int headerVisibility = (currentDictionary.getTranslationCount() == 0) ? View.GONE : View.VISIBLE;
        findViewById(R.id.translation_list_header).setVisibility(headerVisibility);
        int numberofTranslations = currentDictionary.getNumberOfTranslationsWithNoRecording();
        String message = String.format(getString(R.string.no_audio_toggle_text), numberofTranslations);
        ((TextView)findViewById(R.id.no_audio_toggle_text)).setText(message);
    }

    private void setSwitchClickListener() {
        SwitchCompat noAudioSwitch = (SwitchCompat) findViewById(R.id.no_audio_toggle);

        noAudioSwitch.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                hideTranslationsWithoutAudioToggle = isChecked;
                Collections.fill(translationCardStates, Boolean.FALSE);
                setDictionary(currentDictionaryIndex);
            }
        });
    }

    private void updateAddTranslationButtonVisibility() {
        if(deck.isLocked()){
            addTranslationButton.setVisibility(View.GONE);
        }
    }

    @Override
    public void setBitmapsForActivity() {

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        getIntent().putExtra(INTENT_KEY_CURRENT_DICTIONARY_INDEX, currentDictionaryIndex);
    }

    @OnClick(R.id.add_translation_button)
    protected void addTranslationButtonClicked() {
        launchGetStartedActivity();
    }

    private void initTabs() {
        LayoutInflater inflater = LayoutInflater.from(this);
        languageTabTextViews = new TextView[dictionaries.length];
        languageTabBorders = new View[dictionaries.length];
        LinearLayout tabContainer = (LinearLayout) findViewById(R.id.tabs);
        for (int i = 0; i < dictionaries.length; i++) {
            Dictionary dictionary = dictionaries[i];
            View textFrame = inflater.inflate(R.layout.language_tab, tabContainer, false);
            TextView textView = (TextView) textFrame.findViewById(R.id.tab_label_text);
            textView.setText(dictionary.getLabel().toUpperCase());
            final int index = i;
            textFrame.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    setDictionary(index);
                }
            });
            tabContainer.addView(textFrame);
            languageTabTextViews[i] = textView;
            languageTabBorders[i] = textFrame.findViewById(R.id.tab_border);
        }
    }

    private void initList() {
        ListView list = (ListView) findViewById(R.id.translations_list);
        LayoutInflater layoutInflater = getLayoutInflater();
        list.addHeaderView(layoutInflater.inflate(R.layout.translation_list_header, list, false));
        findViewById(R.id.translation_list_header).setOnClickListener(null);
        setSwitchClickListener();
        inflateListFooter();

        listAdapter = new CardListAdapter(
                this, R.layout.translation_item, R.id.origin_translation_text,
                new ArrayList<Translation>());
        list.setAdapter(listAdapter);
    }

    private void inflateListFooter() {
        ListView list = (ListView) findViewById(R.id.translations_list);
        LayoutInflater layoutInflater = getLayoutInflater();
        list.addFooterView(layoutInflater.inflate(R.layout.translation_list_footer, list, false));
        findViewById(R.id.translations_list_footer).setOnClickListener(null);
        updateWelcomeInstructionsState();
    }

    private void updateWelcomeInstructionsState() {
        ListView list = (ListView) findViewById(R.id.translations_list);
        boolean isTranslationsListEmpty = dictionaries[currentDictionaryIndex].getTranslationCount() == 0;
        int welcomeInstructionsVisibility = isTranslationsListEmpty ? View.VISIBLE : View.GONE;
        findViewById(R.id.empty_deck_title).setVisibility(welcomeInstructionsVisibility);
        findViewById(R.id.empty_deck_message).setVisibility(welcomeInstructionsVisibility);
        updateListViewCentered(list, isTranslationsListEmpty);
    }

    private void launchGetStartedActivity(){
        Intent nextIntent = new Intent(TranslationsActivity.this, GetStartedActivity.class);
        nextIntent.putExtra(AddTranslationActivity.CONTEXT_INTENT_KEY, createTranslationContext());
        nextIntent.putExtra(INTENT_KEY_DECK, deck);
        startActivity(nextIntent);
    }

    //// TODO: FACTORY
    private AddNewTranslationContext createTranslationContext() {
        ArrayList<NewTranslation> newTranslations = new ArrayList<>();
        for (Dictionary dictionary : dictionaries) {
            newTranslations.add(new NewTranslation(dictionary));
        }
        return new AddNewTranslationContext(newTranslations);
    }

    private void setDictionary(int dictionaryIndex) {
        decoratedMediaManager.stop();

        if (currentDictionaryIndex != -1) {
            languageTabTextViews[currentDictionaryIndex].setTextColor(
                    ContextCompat.getColor(this, R.color.unselectedLanguageTabText));
            languageTabBorders[currentDictionaryIndex].setBackgroundColor(0);
        }
        languageTabTextViews[dictionaryIndex].setTextColor(
                ContextCompat.getColor(this, R.color.textColor));
        languageTabBorders[dictionaryIndex].setBackgroundColor(
                ContextCompat.getColor(this, R.color.textColor));
        currentDictionaryIndex = dictionaryIndex;
        Dictionary dictionary = dictionaries[dictionaryIndex];
        listAdapter.clear();

        for (int translationIndex = 0; translationIndex < dictionary.getTranslationCount(); translationIndex++) {
            Translation currentTranslation = dictionary.getTranslation(translationIndex);
            if(hideTranslationsWithoutAudioToggle && !currentTranslation.isAudioFilePresent()){
                continue;
            }
            listAdapter.add(currentTranslation);
        }
        updateHeader();
        updateWelcomeInstructionsState();
    }

    @Override
    protected void onPause() {
        super.onPause();
        decoratedMediaManager.stop();
    }

    private class CardListAdapter extends ArrayAdapter<Translation> {

        public CardListAdapter(
                Context context, int resource, int textViewResourceId,
                List<Translation> objects) {
            super(context, resource, textViewResourceId, objects);
        }

        @Override
        public View getView(int position, View translationItemView, ViewGroup parent) {
            if (translationItemView == null) {
                translationItemView = inflateTranslationItemView(parent);
            }

            if (translationCardStates.get(position)) {
                translationItemView.findViewById(R.id.translation_child).setVisibility(View.VISIBLE);
                translationItemView.findViewById(R.id.indicator_icon).setBackgroundResource(
                        R.drawable.collapse_arrow);
            } else {
                translationItemView.findViewById(R.id.translation_child).setVisibility(View.GONE);
                translationItemView.findViewById(R.id.indicator_icon).setBackgroundResource(
                        R.drawable.expand_arrow);
            }

            translationItemView.setOnClickListener(null);

            translationItemView.findViewById(R.id.translation_indicator_layout)
                    .setOnClickListener(new CardIndicatorClickListener(translationItemView, position));

            View editView = translationItemView.findViewById(R.id.translation_card_edit);
            View deleteView = translationItemView.findViewById(R.id.translation_card_delete);
            if (deck.isLocked()) {
                editView.setVisibility(View.GONE);
                deleteView.setVisibility(View.GONE);
            } else {
                editView.setOnClickListener(new CardEditClickListener(getItem(position)));
                deleteView.setOnClickListener(new CardDeleteClickListener(getItem(position)));
            }

            String currentDictionaryLabel = dictionaries[currentDictionaryIndex].getLabel();

            ProgressBar progressBar = (ProgressBar) translationItemView.findViewById(
                    R.id.list_item_progress_bar);

            setCardTextView(position, translationItemView, currentDictionaryLabel, progressBar);

            setTranslatedTextView(position, translationItemView, currentDictionaryLabel);

            translationItemView.findViewById(R.id.translated_text_layout)
                    .setOnClickListener(new CardAudioClickListener(getItem(position), progressBar,
                            decoratedMediaManager, currentDictionaryLabel));

            return translationItemView;
        }

        @NonNull
        private View inflateTranslationItemView(ViewGroup parent) {
            View translationItemView = getLayoutInflater().inflate(R.layout.translation_item,
                    parent, false);
            translationItemView.findViewById(R.id.indicator_icon).setBackgroundResource(
                    R.drawable.expand_arrow);
            return translationItemView;
        }

        private void setCardTextView(int position, View convertView, String currentDictionaryLabel,
                                     ProgressBar progressBar) {
            TextView cardTextView = (TextView) convertView.findViewById(
                    R.id.origin_translation_text);
            cardTextView.setText(getItem(position).getLabel());
            int cardTextColor = getItem(position).isAudioFilePresent() ? R.color.primaryTextColor : R.color.textDisabled;
            cardTextView.setTextColor(ContextCompat.getColor(TranslationsActivity.this, cardTextColor));
            cardTextView.setOnClickListener(new CardAudioClickListener(getItem(position), progressBar,
                    decoratedMediaManager, currentDictionaryLabel));
        }

        private void setTranslatedTextView(int position, View convertView, String currentDictionaryLabel) {
            TextView translatedText = (TextView) convertView.findViewById(R.id.translated_text);
            if(getItem(position).getTranslatedText().isEmpty()){
                translatedText.setText(String.format(getString(R.string.translated_text_hint), currentDictionaryLabel));
                translatedText.setTextColor(ContextCompat.getColor(getContext(),
                        R.color.textDisabled));
                translatedText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18);
            } else {
                translatedText.setText(getItem(position).getTranslatedText());
                translatedText.setTextColor(ContextCompat.getColor(getContext(),
                        R.color.primaryTextColor));
                translatedText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20);
            }
        }
    }

    private class CardIndicatorClickListener implements View.OnClickListener {

        private View translationItem;
        private int position;

        public CardIndicatorClickListener(View translationItem, int position) {

            this.translationItem = translationItem;
            this.position = position;
        }

        @Override
        public void onClick(View view) {
            View translationChild = translationItem.findViewById(R.id.translation_child);
            if (translationChild.getVisibility() == View.GONE) {
                translationChild.setVisibility(View.VISIBLE);
                translationItem.findViewById(R.id.indicator_icon).setBackgroundResource(
                        R.drawable.collapse_arrow);
                translationCardStates.set(position, true);
            } else {
                translationChild.setVisibility(View.GONE);
                translationItem.findViewById(R.id.indicator_icon).setBackgroundResource(
                        R.drawable.expand_arrow);
                translationCardStates.set(position, false);
            }
        }
    }

    private class CardEditClickListener implements View.OnClickListener {
        private Translation translationCard;

        public CardEditClickListener(Translation translationCard) {
            this.translationCard = translationCard;
        }

        @Override
        public void onClick(View view) {
            Intent nextIntent = new Intent(TranslationsActivity.this, EnterSourcePhraseActivity.class);
            List<NewTranslation> newTranslations = new ArrayList<>();
            for (Dictionary dictionary : dictionaries) {
                Translation translation = dictionary.getTranslationBySourcePhrase(translationCard.getLabel());
                newTranslations.add(new NewTranslation(dictionary, translation, IS_EDIT));
            }
            nextIntent.putExtra(AddTranslationActivity.CONTEXT_INTENT_KEY, new AddNewTranslationContext(newTranslations, IS_EDIT));
            nextIntent.putExtra(INTENT_KEY_DECK, deck);
            startActivity(nextIntent);
        }
    }

    private class CardDeleteClickListener implements View.OnClickListener {

        Translation translation;

        public CardDeleteClickListener(Translation translation) {
            this.translation = translation;
        }

        @Override
        public void onClick(View view) {
            new AlertDialog.Builder(TranslationsActivity.this)
                    .setTitle(R.string.delete_dialog_title)
                    .setMessage(R.string.delete_dialog_message)
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            for (Dictionary dictionary : dictionaries) {
                                Translation translationBySourcePhrase = dictionary.getTranslationBySourcePhrase(translation.getLabel());
                                dbManager.deleteTranslation(translationBySourcePhrase.getDbId());
                            }
                            dictionaries = dbManager.getAllDictionariesForDeck(deck.getDbId());
                            setDictionary(currentDictionaryIndex);
                            listAdapter.notifyDataSetChanged();
                        }
                    })
                    .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    }).show();
        }
    }

}
