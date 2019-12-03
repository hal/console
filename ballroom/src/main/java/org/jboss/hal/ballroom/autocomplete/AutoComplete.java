/*
 * Copyright 2015-2016 Red Hat, Inc, and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.hal.ballroom.autocomplete;

import elemental2.dom.Element;
import elemental2.dom.HTMLInputElement;
import elemental2.dom.KeyboardEvent;
import jsinterop.annotations.JsConstructor;
import jsinterop.annotations.JsMethod;
import jsinterop.annotations.JsType;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.hal.ballroom.Attachable;
import org.jboss.hal.ballroom.form.AbstractFormItem;
import org.jboss.hal.ballroom.form.FormItem;
import org.jboss.hal.ballroom.form.SuggestHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static elemental2.dom.DomGlobal.document;
import static elemental2.dom.DomGlobal.setTimeout;
import static jsinterop.annotations.JsPackage.GLOBAL;
import static org.jboss.gwt.elemento.core.Elements.asHtmlElement;
import static org.jboss.gwt.elemento.core.Elements.htmlElements;
import static org.jboss.hal.ballroom.form.Form.State.EDITING;
import static org.jboss.hal.resources.CSS.autocompleteSuggestions;
import static org.jboss.hal.resources.CSS.formControl;
import static org.jboss.hal.resources.UIConstants.HASH;

/**
 * Java wrapper for <a href="https://github.com/Pixabay/JavaScript-autoComplete">javascript-auto-complete</a>
 *
 * @see <a href="https://github.com/Pixabay/JavaScript-autoComplete">https://github.com/Pixabay/JavaScript-autoComplete</a>
 */
public class AutoComplete implements SuggestHandler, Attachable {

    static final Logger logger = LoggerFactory.getLogger(AutoComplete.class);

    private FormItem formItem;
    private Api api;
    private Options options;

    protected void init(Options options) {
        this.options = options;
    }

    @Override
    public void attach() {
        if (api == null) {
            options.selector = formItem().element(EDITING).getElementsByClassName(formControl).getAt(0);
            options.onSelect = (event, item, renderedItem) -> {
                if (formItem() instanceof AbstractFormItem) {
                    ((AbstractFormItem) formItem()).onSuggest(item);
                }
            };
            api = new Api(options);
        }
    }

    @Override
    public void detach() {
        Element element = document.getElementById(formItem().getId(EDITING));
        if (api != null && element != null) {
            api.destroy();
            api = null;
        }
    }

    @Override
    @SuppressWarnings("HardCodedStringLiteral")
    public void showAll() {
        HTMLInputElement element = (HTMLInputElement) document.getElementById(formItem().getId(EDITING));
        if (element != null) {
            setTimeout((o) -> {
                element.blur();
                KeyboardEvent event = new KeyboardEvent("keyup");
                triggerEvent(element, event, "", 0); // to reset 'last_val' in autoComplete.js
                triggerEvent(element, event, SHOW_ALL_VALUE, SHOW_ALL_VALUE.charAt(0));
                element.focus();
            }, 351); // timeout must be > 350, which is used in autoComplete.js
        }
    }

    private native void triggerEvent(HTMLInputElement element, KeyboardEvent event, String key, int keyCode) /*-{
        element.value = key;
        event.keyCode = keyCode;
        event.which = keyCode;
        element.dispatchEvent(event);
    }-*/;

    @Override
    public void close() {
        Elements.stream(document.querySelectorAll(autocompleteSuggestions))
                .filter(htmlElements())
                .map(asHtmlElement())
                .filter(Elements::isVisible)
                .forEach(element -> Elements.setVisible(element, false));
    }

    @Override
    public void setFormItem(FormItem formItem) {
        this.formItem = formItem;
    }

    private FormItem formItem() {
        if (formItem == null) {
            throw new IllegalStateException(
                    "No form item assigned. Please call AutoComplete.setFormItem(FormItem) before using this as a SuggestHandler.");
        }
        return formItem;
    }

    private String formItemSelector() {
        return HASH + formItem().getId(EDITING);
    }


    @JsType(isNative = true, namespace = GLOBAL, name = "autoComplete")
    static class Api {

        @JsConstructor
        @SuppressWarnings("UnusedParameters")
        Api(Options options) {
        }

        @JsMethod
        private native void destroy();
    }
}
