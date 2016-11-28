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
import elemental.dom.Element;
import elemental.events.KeyboardEvent;
import elemental.events.KeyboardEvent.KeyCode;
import elemental.html.InputElement;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.gwt.elemento.core.InputType;
import org.jboss.gwt.elemento.core.IsElement;
import org.jboss.hal.resources.Constants;
import org.jboss.hal.resources.UIConstants;
import org.jboss.hal.spi.Callback;

import static org.jboss.gwt.elemento.core.EventType.click;
import static org.jboss.gwt.elemento.core.EventType.keyup;
import static org.jboss.hal.resources.CSS.*;

/**
 * @author Harald Pehl
 */
public class Search implements IsElement {

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
    private static final String SEARCH_BOX = "searchBox";
    private static final String CLEAR_SEARCH = "clearSearch";

    private Element root;
    private InputElement searchBox;
    private Element clearSearch;

    private Search(Builder builder) {
        // @formatter:off
        Elements.Builder eb = new Elements.Builder()
            .div().css(halSearch, hasButton)
                .div().css(formGroup, hasClear)
                    .div().css(searchPfInputGroup)
                        .label().attr("for", builder.id).css(srOnly)
                            .textContent(CONSTANTS.search())
                        .end()
                        .input(InputType.search)
                            .id(builder.id)
                            .css(formControl)
                            .rememberAs(SEARCH_BOX)
                            .attr(UIConstants.PLACEHOLDER, CONSTANTS.search())
                            .on(keyup, event -> {
                                Elements.setVisible(clearSearch, !Strings.isNullOrEmpty(searchBox.getValue()));
                                if (((KeyboardEvent) event).getKeyCode() == KeyCode.ENTER) {
                                    builder.onSearch.search(searchBox.getValue());
                                }
                            })
                        .button().rememberAs(CLEAR_SEARCH).css(clear).aria(UIConstants.HIDDEN, "true") //NON-NLS
                            .on(click, event -> {
                                clear();
                                if (builder.onClear!= null) {
                                    builder.onClear.execute();
                                }
                                focus();
                            })
                            .span().css(pfIcon("close")).end()
                        .end()
                    .end()
                .end()
                .div().css(formGroup, btnGroup)
                    .button().css(btn, btnDefault).on(click, event -> builder.onSearch.search(searchBox.getValue()))
                        .span().css(fontAwesome("search")).end()
                    .end();
                    if (builder.onPrevious != null) {
                        eb.button().css(btn, btnDefault).on(click, event -> builder.onPrevious.search(searchBox.getValue()))
                            .span().css(fontAwesome("angle-left")).end()
                        .end();
                    }
                    if (builder.onNext != null) {
                        eb.button().css(btn, btnDefault).on(click, event -> builder.onNext.search(searchBox.getValue()))
                            .span().css(fontAwesome("angle-right")).end()
                        .end();
                    }
                eb.end()
            .end();
        // @formatter:on

        this.searchBox = eb.referenceFor(SEARCH_BOX);
        this.clearSearch = eb.referenceFor(CLEAR_SEARCH);
        this.root = eb.build();
        Elements.setVisible(clearSearch, false);
    }

    @Override
    public Element asElement() {
        return root;
    }

    public void clear() {
        searchBox.setValue("");
        Elements.setVisible(clearSearch, false);
    }

    public void focus() {
        searchBox.focus();
    }
}
