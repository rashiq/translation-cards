package org.mercycorps.translationcards.data;

import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

public class DictionaryTest {
    @Test
    public void shouldDeleteTranslationFromDatabase() {
        Translation[] translations = new Translation[1];
        translations[0] = mock(Translation.class);
        Dictionary dictionary = new Dictionary("", "", translations, -1, -1);

        dictionary.deleteTranslation("Source phrase");


    }
}