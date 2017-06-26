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
package org.jboss.hal.ballroom;

import com.google.common.base.Strings;
import com.google.gwt.core.client.GWT;
import elemental2.dom.HTMLElement;
import elemental2.dom.HTMLInputElement;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.gwt.elemento.core.IsElement;
import org.jboss.hal.resources.Constants;
import org.jboss.hal.resources.UIConstants;
import org.jboss.hal.spi.Callback;

import static org.jboss.gwt.elemento.core.Elements.*;
import static org.jboss.gwt.elemento.core.EventType.click;
import static org.jboss.gwt.elemento.core.EventType.keyup;
import static org.jboss.gwt.elemento.core.InputType.search;
import static org.jboss.hal.resources.CSS.*;

/**
 * @author Harald Pehl
 */
public class Search implements IsElement<HTMLElement> {

    @FunctionalInterface
    public interface SearchHandler {

        void search(String query);
    }


    public static class Builder {

        private final String id;
        private final SearchHandler onSearch;
        private Callback onClear;
        private SearchHandler onPrevious;
        private SearchHandler onNext;

        public Builder(final String id, final SearchHandler onSearch) {
            this.id = id;
            this.onSearch = onSearch;
        }

        public Builder onClear(Callback onClear) {
            this.onClear = onClear;
            return this;
        }

        public Builder onPrevious(SearchHandler onPrevious) {
            this.onPrevious = onPrevious;
            return this;
        }

        public Builder onNext(SearchHandler onNext) {
            this.onNext = onNext;
            return this;
        }

        public Search build() {
            return new Search(this);
        }
    }


    private static final Constants CONSTANTS = GWT.create(Constants.class);

    private HTMLElement root;
    private HTMLInputElement searchBox;
    private HTMLElement clearSearch;

    private Search(Builder builder) {
        HTMLElement buttons;
        root = div().css(halSearch, hasButton)
                .add(div().css(formGroup, hasClear)
                        .add(div().css(searchPfInputGroup)
                                .add(label().css(srOnly)
                                        .textContent(CONSTANTS.search())
                                        .apply(label -> label.htmlFor = builder.id))
                                .add(searchBox = input(search)
                                        .id(builder.id)
                                        .css(formControl)
                                        .attr(UIConstants.PLACEHOLDER, CONSTANTS.search())
                                        .on(keyup, event -> {
                                            Elements.setVisible(clearSearch, !Strings.isNullOrEmpty(searchBox.value));
                                            if ("Enter".equals(event.key)) { //NON-NLS
                                                builder.onSearch.search(searchBox.value);
                                            }
                                        })
                                        .asElement())
                                .add(clearSearch = button().css(clear)
                                        .aria(UIConstants.HIDDEN, "true") //NON-NLS
                                        .on(click, event -> {
                                            clear();
                                            if (builder.onClear != null) {
                                                builder.onClear.execute();
                                            }
                                            focus();
                                        })
                                        .add(span().css(pfIcon("close")))
                                        .asElement())))
                .add(buttons = div().css(formGroup, btnGroup)
                        .add(button().css(btn, btnDefault)
                                .on(click, event -> builder.onSearch.search(searchBox.value))
                                .add(span().css(fontAwesome("search"))))
                        .asElement())
                .asElement();

        if (builder.onPrevious != null) {
            buttons.appendChild(button().css(btn, btnDefault)
                    .on(click, event -> builder.onPrevious.search(searchBox.value))
                    .add(span().css(fontAwesome("angle-left")))
                    .asElement());
        }
        if (builder.onNext != null) {
            buttons.appendChild(button().css(btn, btnDefault)
                    .on(click, event -> builder.onNext.search(searchBox.value))
                    .add(span().css(fontAwesome("angle-right")))
                    .asElement());
        }
        Elements.setVisible(clearSearch, false);
    }

    @Override
    public HTMLElement asElement() {
        return root;
    }

    public void clear() {
        searchBox.value = "";
        Elements.setVisible(clearSearch, false);
    }

    public void focus() {
        searchBox.focus();
    }
}
