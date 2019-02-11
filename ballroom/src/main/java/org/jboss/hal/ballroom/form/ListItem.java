/*
 * Copyright 2015-2016 Red Hat, Inc, and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.hal.ballroom.form;

import java.util.EnumSet;
import java.util.List;

import com.google.common.base.Splitter;
import com.google.gwt.core.client.GWT;
import org.jboss.hal.resources.Messages;

import static org.jboss.hal.ballroom.form.Decoration.*;

public class ListItem extends TagsItem<List<String>> {

    private static final Messages MESSAGES = GWT.create(Messages.class);

    public ListItem(String name, String label) {
        super(name, label, MESSAGES.listHint(),
                EnumSet.of(DEFAULT, DEPRECATED, ENABLED, INVALID, REQUIRED, RESTRICTED, SUGGESTIONS),
                new ListMapping());
    }

    @Override
    public boolean isEmpty() {
        return getValue() == null || getValue().isEmpty();
    }


    private static class ListMapping implements TagsMapping<List<String>> {

        @Override
        public List<String> parse(String cst) {
            //noinspection UnstableApiUsage
            return Splitter.on(',')
                    .trimResults()
                    .omitEmptyStrings()
                    .splitToList(cst);
        }

        @Override
        public List<String> tags(List<String> value) {
            return value;
        }

        @Override
        public String asString(List<String> value) {
            return String.join(", ", value);
        }
    }
}
