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

import com.google.common.base.Strings;

import static org.jboss.hal.ballroom.form.Decoration.DEFAULT;
import static org.jboss.hal.ballroom.form.Decoration.DEPRECATED;
import static org.jboss.hal.ballroom.form.Decoration.REQUIRED;
import static org.jboss.hal.ballroom.form.Decoration.RESTRICTED;

public class StaticItem extends AbstractFormItem<String> {

    private static class StaticAppearance extends ReadOnlyAppearance<String> {

        StaticAppearance() {
            super(EnumSet.of(DEFAULT, DEPRECATED, REQUIRED, RESTRICTED));
        }

        @Override
        protected String name() {
            return "StaticAppearance";
        }
    }


    public StaticItem(final String name, final String label) {
        super(name, label, null);
        addAppearance(Form.State.READONLY, new StaticAppearance());
        addAppearance(Form.State.EDITING, new StaticAppearance());
    }

    @Override
    public boolean isEmpty() {
        return Strings.isNullOrEmpty(getValue());
    }

    @Override
    public boolean supportsExpressions() {
        return false;
    }
}
