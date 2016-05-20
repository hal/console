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

import com.google.gwt.user.client.ui.Focusable;
import com.google.gwt.user.client.ui.HasEnabled;
import com.google.gwt.user.client.ui.HasName;
import com.google.gwt.user.client.ui.HasText;
import org.jboss.gwt.elemento.core.IsElement;

/**
 * @author Harald Pehl
 */
public abstract class InputElement<T>
        implements IsElement, HasEnabled, Focusable, HasName, HasText /* for expression support */ {

    /**
     * Helper class to pass data from subclasses of AbstractFormItem to newInputElement().
     */
    public static class Context<C> {

        private final C data;

        Context(final C data) {this.data = data;}

        public C data() {
            return data;
        }
    }


    public static final Context<Void> EMPTY_CONTEXT = new Context<>(null);

    private boolean attached;

    void attach() {
        attached = true;
    }

    boolean isAttached() {
        return attached;
    }

    public void setId(final String id) {
        asElement().setId(id);
    }

    public String getId() {
        return asElement().getId();
    }

    public void setClassName(final String classNames) {
        asElement().setClassName(classNames);
    }

    public abstract T getValue();

    public abstract void setValue(T value);

    public abstract void clearValue();

    public void setPlaceholder(final String placeHolder) {
        // empty
    }
}
