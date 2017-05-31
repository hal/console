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

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import com.google.common.base.Strings;
import elemental2.dom.HTMLElement;
import elemental2.dom.HTMLInputElement;
import elemental2.dom.HTMLOptionElement;
import elemental2.dom.HTMLSelectElement;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.hal.dmr.Deprecation;
import org.jboss.hal.resources.Ids;

import static com.google.common.base.Strings.emptyToNull;
import static org.jboss.gwt.elemento.core.Elements.div;
import static org.jboss.gwt.elemento.core.Elements.input;
import static org.jboss.gwt.elemento.core.Elements.label;
import static org.jboss.gwt.elemento.core.InputType.text;
import static org.jboss.hal.ballroom.form.Decoration.*;
import static org.jboss.hal.ballroom.form.Form.State.EDITING;
import static org.jboss.hal.dmr.ModelDescriptionConstants.UNDEFINED;
import static org.jboss.hal.resources.CSS.*;

/**
 * @author Harald Pehl
 */
abstract class SelectBoxEditingAppearance<T> extends AbstractAppearance<T> {

    final HTMLSelectElement selectElement;
    private final HTMLElement root;
    private final HTMLElement inputContainer;
    private final HTMLElement helpBlock;
    private HTMLElement inputGroup;
    private HTMLInputElement restrictedInput;
    private HTMLElement restrictedMarker;
    final boolean allowEmpty;
    boolean attached;

    SelectBoxEditingAppearance(final HTMLSelectElement selectElement, final List<String> options,
            final boolean allowEmpty) {
        super(EnumSet.of(DEFAULT, DEPRECATED, ENABLED, INVALID, REQUIRED, RESTRICTED));

        this.selectElement = selectElement;
        this.selectElement.classList.add(formControl);
        this.selectElement.classList.add(selectpicker);
        this.allowEmpty = allowEmpty;
        this.helpBlock = Appearance.helpBlock();
        this.root = div().css(formGroup)
                .add(labelElement = label().css(controlLabel, halFormLabel).asElement())
                .add(inputContainer = div().css(halFormInput)
                        .add(selectElement)
                        .asElement())
                .asElement();

        List<String> localOptions = options;
        if (allowEmpty && !options.isEmpty() && emptyToNull(options.get(0)) != null) {
            localOptions = new ArrayList<>(options);
            localOptions.add(0, "");
        }
        for (String option : localOptions) {
            HTMLOptionElement optionElement = Elements.option(option).asElement();
            if (emptyToNull(option) == null) {
                optionElement.title = UNDEFINED;
            }
            this.selectElement.appendChild(optionElement);
        }
    }

    @Override
    protected String name() {
        return "SelectBoxEditingAppearance";
    }

    @Override
    public void attach() {
        this.attached = true;
    }

    @Override
    public HTMLElement asElement() {
        return root;
    }

    abstract void refresh();


    // ------------------------------------------------------ apply decoration

    @Override
    <C> void safeApply(final Decoration decoration, final C context) {
        switch (decoration) {

            case DEFAULT:
                String defaultValue = String.valueOf(context);
                selectElement.title = Strings.isNullOrEmpty(defaultValue) ? UNDEFINED : defaultValue;
                if (attached) {
                    refresh();
                }
                break;

            case DEPRECATED:
                markAsDeprecated((Deprecation) context);
                break;

            case ENABLED:
                selectElement.disabled = false;
                if (attached) {
                    refresh();
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

            case RESTRICTED:
                if (inputGroup == null && restrictedInput == null && restrictedMarker == null) {
                    restrictedInput = input(text).css(restricted)
                            .apply(input -> {
                                input.value = CONSTANTS.restricted();
                                input.readOnly = true;
                            })
                            .asElement();
                    restrictedMarker = Appearance.restrictedMarker();

                    inputGroup = Appearance.inputGroup();
                    inputGroup.appendChild(restrictedInput);
                    inputGroup.appendChild(restrictedMarker);
                }

                Elements.removeChildrenFrom(inputContainer);
                inputContainer.appendChild(inputGroup);
                break;

            // not supported
            case EXPRESSION:
            case HINT:
            case SUGGESTIONS:
                break;
        }
    }


    // ------------------------------------------------------ unapply decoration

    @Override
    void safeUnapply(final Decoration decoration) {
        switch (decoration) {

            case DEFAULT:
                selectElement.title = "";
                if (attached) {
                    refresh();
                }
                break;

            case DEPRECATED:
                clearDeprecation();
                break;

            case ENABLED:
                selectElement.disabled = true;
                if (attached) {
                    refresh();
                }
                break;

            case INVALID:
                root.classList.remove(hasError);
                Elements.failSafeRemove(inputContainer, helpBlock);
                break;

            case REQUIRED:
                clearRequired();
                break;

            case RESTRICTED:
                Elements.removeChildrenFrom(inputContainer);
                inputContainer.appendChild(selectElement);
                if (attached) {
                    refresh();
                }
                break;

            // not supported
            case EXPRESSION:
            case HINT:
            case SUGGESTIONS:
                break;
        }
    }


    // ------------------------------------------------------ properties & delegates

    @Override
    public void setId(final String id) {
        this.id = Ids.build(id, EDITING.name().toLowerCase());
        root.dataset.set(FORM_ITEM_GROUP, this.id);
        selectElement.id = this.id;
        labelElement.htmlFor = this.id;
    }

    @Override
    public void setName(final String name) {
        selectElement.name = name;
    }

    @Override
    public int getTabIndex() {
        return (int) selectElement.tabIndex;
    }

    @Override
    public void setAccessKey(final char key) {
        // noop
    }

    @Override
    public void setFocus(final boolean focused) {
        if (focused) {
            selectElement.focus();
        } else {
            selectElement.blur();
        }
    }

    @Override
    public void setTabIndex(final int index) {
        selectElement.tabIndex = index;
    }
}
