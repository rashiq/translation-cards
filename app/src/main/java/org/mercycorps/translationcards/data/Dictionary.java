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

package org.mercycorps.translationcards.data;

import com.orm.SugarRecord;
import com.orm.dsl.Column;
import com.orm.dsl.Table;

import java.util.List;

/**
 * Contains information about a set of phrases for a particular language.
 *
 * @author nick.c.worden@gmail.com (Nick Worden)
 */
@Table(name = "dictionaries")
public class Dictionary extends SugarRecord {

    private String label;
    @Column(name = "deckId")
    private long deckId;

    public Dictionary() {

    }

    public Dictionary(String label, long deckId) {
        this.label = label;
        this.deckId = deckId;
    }

    public String getLabel() {
        return label;
    }

    public List<Translation> getTranslations() {
        return Translation.find(Translation.class, "dictionaryId = ?", getId().toString());
    }

    public int getTranslationCount() {
        return getTranslations().size();
    }

    @Override
    public boolean delete() {
        for (Translation translation : getTranslations()) {
            translation.delete();
        }

        return super.delete();
    }

    public void setDeckId(long deckId) {
        this.deckId = deckId;
    }
}
