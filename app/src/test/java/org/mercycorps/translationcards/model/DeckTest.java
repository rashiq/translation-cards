package org.mercycorps.translationcards.model;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mercycorps.translationcards.BuildConfig;
import org.mercycorps.translationcards.MainApplication;
import org.mercycorps.translationcards.data.DbManager;
import org.mercycorps.translationcards.data.Deck;
import org.mercycorps.translationcards.data.Dictionary;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@Config(constants = BuildConfig.class, sdk = 21)
@RunWith(RobolectricGradleTestRunner.class)
public class DeckTest {
    private Deck deck;
    public static final String SOURCE_PHRASE = "I am a source phrase";
    private List<Dictionary> dictionaries;
    private Dictionary dictionary1;
    private Dictionary dictionary2;

    @Before
    public void setUp() throws Exception {
        deck = new Deck("", "", "", -1, 1454946439262L, false, "");
        dictionaries = new ArrayList<>();
        dictionary1 = mock(Dictionary.class);
        dictionary2 = mock(Dictionary.class);
    }

    @Test
    public void getCreationDate_shouldFormatCreationDate() {
        assertThat(deck.getCreationDateString(), is("02/08/16"));
    }

    @Test
    public void shouldDeleteDeckFromDBWhenDeleteIsCalled() {
        deck.delete();

        verify(((MainApplication)MainApplication.getContextFromMainApp()).getDbManager()).deleteDeck(deck.getDbId());
    }

    @Test
    public void shouldSaveDeckToDBWhenSaveIsCalled() {
        deck.save();

        verify(((MainApplication)MainApplication.getContextFromMainApp()).getDbManager()).addDeck(deck.getLabel(), deck.getPublisher(), deck.getTimestamp(), deck.getExternalId(), "", deck.isLocked(), deck.getSrcLanguageIso());
    }
    @Test
    public void shouldDeleteTranslationFromDictionariesWhenDeletingAPhrase() {
        dictionaries.add(dictionary1);
        dictionaries.add(dictionary2);
        deck = new Deck("", "", "", -1, 1454946439262L, false, "", dictionaries);

        deck.deleteTranslation(SOURCE_PHRASE);

        verify(dictionary1).deleteTranslation(SOURCE_PHRASE);
        verify(dictionary2).deleteTranslation(SOURCE_PHRASE);

    }
}