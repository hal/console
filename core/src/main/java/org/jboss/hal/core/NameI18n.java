/*
 *  Copyright 2022 Red Hat
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.jboss.hal.core;

import org.jboss.hal.ballroom.form.FormItem;
import org.jboss.hal.ballroom.table.Column;
import org.jboss.hal.resources.Constants;

import com.google.gwt.core.client.GWT;

import static org.jboss.hal.dmr.ModelDescriptionConstants.NAME;

/**
 * The term "name" is special in a way that it is <strong>always</strong> localized in the UI.
 */
public final class NameI18n {

    private static final Constants CONSTANTS = GWT.create(Constants.class);

    public static <T> boolean shouldBeLocalized(FormItem<T> formItem) {
        return NAME.equals(formItem.getName());
    }

    public static <T> boolean shouldBeLocalized(Column<T> column) {
        return NAME.equals(column.name);
    }

    public static <T> void localize(FormItem<T> formItem) {
        formItem.setLabel(CONSTANTS.name());
    }

    public static <T> void localize(Column<T> column) {
        column.title = CONSTANTS.name();
    }

    private NameI18n() {
    }
}
