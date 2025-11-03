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
package org.jboss.hal.ballroom;

import org.jboss.elemento.Elements;
import org.jboss.elemento.IsElement;
import org.jboss.hal.resources.Constants;
import org.jboss.hal.resources.UIConstants;
import org.jboss.hal.spi.Callback;

import com.google.common.base.Strings;
import com.google.gwt.core.client.GWT;

import elemental2.dom.HTMLElement;
import elemental2.dom.HTMLInputElement;

import static org.jboss.elemento.Elements.*;
import static org.jboss.elemento.Elements.label;
import static org.jboss.elemento.EventType.click;
import static org.jboss.elemento.EventType.keyup;
import static org.jboss.elemento.InputType.search;
import static org.jboss.elemento.Key.Enter;
import static org.jboss.hal.resources.CSS.*;

public class Search implements IsElement<HTMLElement> {

    private static final Constants CONSTANTS = GWT.create(Constants.class);

    private final HTMLElement root;
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
                                            setVisible(clearSearch, !Strings.isNullOrEmpty(searchBox.value));
                                            if (Enter.match(event)) {
                                                builder.onSearch.search(searchBox.value);
                                            }
                                        }).element())
                                .add(clearSearch = button().css(clear)
                                        .aria(UIConstants.HIDDEN, "true") // NON-NLS
                                        .on(click, event -> {
                                            clear();
                                            if (builder.onClear != null) {
                                                builder.onClear.execute();
                                            }
                                            focus();
                                        })
                                        .add(span().css(pfIcon("close"))).element())))
                .add(buttons = div().css(formGroup, btnGroup)
                        .add(button().css(btn, btnDefault)
                                .on(click, event -> builder.onSearch.search(searchBox.value))
                                .add(span().css(fontAwesome("search"))))
                        .element())
                .element();

        if (builder.onPrevious != null) {
            buttons.appendChild(button().css(btn, btnDefault)
                    .on(click, event -> builder.onPrevious.search(searchBox.value))
                    .add(span().css(fontAwesome("angle-left"))).element());
        }
        if (builder.onNext != null) {
            buttons.appendChild(button().css(btn, btnDefault)
                    .on(click, event -> builder.onNext.search(searchBox.value))
                    .add(span().css(fontAwesome("angle-right"))).element());
        }
        Elements.setVisible(clearSearch, false);
    }

    @Override
    public HTMLElement element() {
        return root;
    }

    public void clear() {
        searchBox.value = "";
        Elements.setVisible(clearSearch, false);
    }

    public void focus() {
        searchBox.focus();
    }

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

        public Builder(String id, SearchHandler onSearch) {
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
}
