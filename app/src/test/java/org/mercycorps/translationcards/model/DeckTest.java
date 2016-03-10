package org.mercycorps.translationcards.model;

import org.junit.Before;
import org.junit.Test;
import org.mercycorps.translationcards.data.Deck;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * Test for Deck
 *
 * @author patdale216@gmail.com (Pat Dale)
 */
public class DeckTest {

    private Deck deck;

    @Before
    public void setUp() throws Exception {
        long aDate = 1454946439262L;
        deck = new Deck("", "", "", -1, aDate, false, "");
    }

    @Test
    public void getCreationDate_shouldFormatCreationDate() {
        assertThat(deck.getCreationDateString(), is("02/08/16"));
    }
}