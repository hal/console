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
package org.jboss.hal.ballroom.form;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.jboss.gwt.elemento.core.Elements;
import org.jboss.hal.dmr.Deprecation;
import org.jboss.hal.resources.Ids;

import elemental2.dom.HTMLElement;
import elemental2.dom.HTMLInputElement;
import elemental2.dom.HTMLLabelElement;

import static org.jboss.gwt.elemento.core.Elements.div;
import static org.jboss.gwt.elemento.core.Elements.label;
import static org.jboss.hal.ballroom.form.Decoration.*;
import static org.jboss.hal.ballroom.form.Form.State.EDITING;
import static org.jboss.hal.resources.CSS.*;

class RadioEditingAppearance extends AbstractAppearance<String> {

    private final HTMLElement root;
    private final HTMLElement inputContainer;
    private final HTMLElement helpBlock;
    private final List<HTMLLabelElement> labelElements;
    private final List<HTMLInputElement> inputElements;

    RadioEditingAppearance(List<HTMLInputElement> elements, LinkedHashMap<String, String> options,
            boolean inline) {
        super(EnumSet.of(DEFAULT, DEPRECATED, ENABLED, INVALID, REQUIRED));
        this.inputElements = elements;

        root = div().css(formGroup)
                .add(labelElement = label().css(controlLabel, halFormLabel).element())
                .add(inputContainer = div().css(halFormInput).element()).element();
        helpBlock = Appearance.helpBlock();

        int i = 0;
        labelElements = new ArrayList<>();
        for (Map.Entry<String, String> entry : options.entrySet()) {
            HTMLLabelElement labelElement;
            HTMLInputElement inputElement = inputElements.get(i);
            inputElement.value = entry.getKey();

            inputContainer.appendChild(div().css(radio)
                    .add(labelElement = label()
                            .add(inputElement)
                            .add(" " + entry.getValue()).element())
                    .element());
            if (inline) {
                labelElement.classList.add(radioInline);
            }
            labelElements.add(labelElement);
            i++;
        }
        elements.get(0).checked = true;
    }

    @Override
    protected String name() {
        return "RadioEditingAppearance";
    }

    @Override
    public void attach() {
        // noop
    }

    @Override
    public HTMLElement element() {
        return root;
    }

    // ------------------------------------------------------ apply decoration

    @Override
    <C> void safeApply(Decoration decoration, C context) {
        switch (decoration) {
            case DEFAULT:
                String defaultValue = String.valueOf(context);
                for (HTMLInputElement inputElement : inputElements) {
                    if (inputElement.value.equals(defaultValue)) {
                        inputElement.checked = true;
                        break;
                    }
                }
                break;

            case DEPRECATED:
                markAsDeprecated((Deprecation) context);
                break;

            case ENABLED:
                for (HTMLElement radioContainer : Elements.children(inputContainer)) {
                    radioContainer.classList.add(disabled);
                }
                for (HTMLInputElement inputElement : inputElements) {
                    inputElement.disabled = true;
                }
                break;

            case INVALID:
                helpBlock.textContent = String.valueOf(context);
                root.classList.add(hasError);
                inputContainer.appendChild(helpBlock);
                break;

            case REQUIRED:
                markAsRequired();
                break;

            // not supported
            case EXPRESSION:
            case HINT:
            case RESTRICTED:
            case SENSITIVE:
            case SUGGESTIONS:
                break;
            // not supported
            default:
                break;
        }
    }

    // ------------------------------------------------------ unapply decoration

    @Override
    void safeUnapply(Decoration decoration) {
        switch (decoration) {
            case DEFAULT:
                // noop
                break;

            case DEPRECATED:
                clearDeprecation();
                break;

            case ENABLED:
                for (HTMLElement radioContainer : Elements.children(inputContainer)) {
                    radioContainer.classList.remove(disabled);
                }
                for (HTMLInputElement inputElement : inputElements) {
                    inputElement.disabled = false;
                }
                break;

            case INVALID:
                root.classList.remove(hasError);
                Elements.failSafeRemove(inputContainer, helpBlock);
                break;

            case REQUIRED:
                clearRequired();
                break;

            // not supported
            case EXPRESSION:
            case HINT:
            case RESTRICTED:
            case SENSITIVE:
            case SUGGESTIONS:
                break;
            // not supported
            default:
                break;
        }
    }

    // ------------------------------------------------------ properties & delegates

    @Override
    public void setId(String id) {
        this.id = Ids.build(id, EDITING.name().toLowerCase());
        root.dataset.set(FORM_ITEM_GROUP, this.id);
        for (int i = 0; i < inputElements.size(); i++) {
            String inputId = Ids.build(id, "radio", String.valueOf(i));
            inputElements.get(i).id = inputId;
            labelElements.get(i).htmlFor = inputId;
        }
    }

    @Override
    public void setName(String name) {
        for (HTMLInputElement inputElement : inputElements) {
            inputElement.name = name;
        }
    }

    @Override
    public void showValue(String value) {
        for (HTMLInputElement inputElement : inputElements) {
            inputElement.checked = value.equals(inputElement.value);
        }
    }

    @Override
    public void clearValue() {
        for (HTMLInputElement inputElement : inputElements) {
            inputElement.checked = false;
        }
    }

    @Override
    public int getTabIndex() {
        return inputElements.get(0).tabIndex;
    }

    @Override
    public void setTabIndex(int index) {
        inputElements.get(0).tabIndex = index;
    }

    @Override
    public void setAccessKey(char key) {
        inputElements.get(0).accessKey = String.valueOf(key);
    }

    @Override
    public void setFocus(boolean focused) {
        if (focused) {
            inputElements.get(0).focus();
        } else {
            inputElements.get(0).blur();
        }
    }
}
