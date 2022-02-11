/*
 *  Copyright 2022 Red Hat
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.jboss.hal.ballroom.form;

import java.util.SortedSet;

import org.jboss.hal.resources.Messages;

import com.google.common.base.Joiner;
import com.google.common.collect.Sets;
import com.google.gwt.core.client.GWT;

import static java.util.Arrays.asList;

public class BlacklistValidation
        implements FormItemValidation<Object> { // needs to be <Object> because it's used in generated code

    private static final Messages MESSAGES = GWT.create(Messages.class);

    private final SortedSet<String> blacklist;

    public BlacklistValidation(final String first, final String... rest) {
        this.blacklist = Sets.newTreeSet();
        this.blacklist.add(first);
        if (rest != null) {
            this.blacklist.addAll(asList(rest));
        }
    }

    public BlacklistValidation(Iterable<String> blacklist) {
        this.blacklist = Sets.newTreeSet(blacklist);
    }

    @Override
    public ValidationResult validate(final Object value) {
        if (value == null || String.valueOf(value).trim().length() == 0) {
            return ValidationResult.OK;
        } else {
            // noinspection SuspiciousMethodCalls
            return blacklist.contains(value) ? ValidationResult.invalid(errorMessage()) : ValidationResult.OK;
        }
    }

    protected String errorMessage() {
        return MESSAGES.blacklist("\"" + Joiner.on("\", \"").join(blacklist) + "\"");
    }
}
