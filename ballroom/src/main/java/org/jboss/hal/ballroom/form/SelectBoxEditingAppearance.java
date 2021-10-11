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
package org.jboss.hal.ballroom.form;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import com.google.common.base.Strings;
import com.google.gwt.safehtml.shared.SafeHtml;
import elemental2.dom.HTMLElement;
import elemental2.dom.HTMLInputElement;
import elemental2.dom.HTMLOptionElement;
import elemental2.dom.HTMLSelectElement;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.hal.ballroom.HelpPopover;
import org.jboss.hal.dmr.Deprecation;
import org.jboss.hal.resources.Ids;

import static com.google.common.base.Strings.emptyToNull;
import static org.jboss.gwt.elemento.core.Elements.a;
import static org.jboss.gwt.elemento.core.Elements.div;
import static org.jboss.gwt.elemento.core.Elements.input;
import static org.jboss.gwt.elemento.core.Elements.label;
import static org.jboss.gwt.elemento.core.Elements.span;
import static org.jboss.gwt.elemento.core.InputType.text;
import static org.jboss.hal.ballroom.form.Decoration.*;
import static org.jboss.hal.ballroom.form.Form.State.EDITING;
import static org.jboss.hal.dmr.ModelDescriptionConstants.UNDEFINED;
import static org.jboss.hal.resources.CSS.*;

abstract class SelectBoxEditingAppearance<T> extends AbstractAppearance<T> {

    final HTMLSelectElement selectElement;
    private final HTMLElement root;
    private final HTMLElement inputContainer;
    private final HTMLElement helpBlock;
    private final HTMLElement labelGroup;
    private HTMLElement inputGroup;
    private HTMLInputElement restrictedInput;
    private HTMLElement restrictedMarker;
    final boolean allowEmpty;
    boolean attached;

    SelectBoxEditingAppearance(HTMLSelectElement selectElement, List<String> options, boolean allowEmpty) {
        super(EnumSet.of(DEFAULT, DEPRECATED, ENABLED, HELP, INVALID, REQUIRED, RESTRICTED));

        this.selectElement = selectElement;
        this.selectElement.classList.add(formControl);
        this.selectElement.classList.add(selectpicker);
        this.allowEmpty = allowEmpty;
        this.helpBlock = Appearance.helpBlock();
        this.root = div().css(formGroup)
                .add(labelGroup = label().css(controlLabel, halFormLabel).element())
                .add(inputContainer = div().css(halFormInput)
                        .add(selectElement).element()).element();
        this.labelElement = (label().css("label-text").element());
        this.labelGroup.appendChild(labelElement);
        List<String> localOptions = options;
        if (allowEmpty && !options.isEmpty() && emptyToNull(options.get(0)) != null) {
            localOptions = new ArrayList<>(options);
            localOptions.add(0, "");
        }
        for (String option : localOptions) {
            HTMLOptionElement optionElement = Elements.option(option).element();
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
    public HTMLElement element() {
        return root;
    }

    void updateOptions(List<String> values) {
        double childElementCount = this.selectElement.childElementCount;
        for (int i = 0; i < childElementCount; i++) {
            this.selectElement.removeChild(this.selectElement.firstElementChild);
        }
        for (String option : values) {
            HTMLOptionElement optionElement = Elements.option(option).element();
            if (emptyToNull(option) == null) {
                optionElement.title = UNDEFINED;
            }
            this.selectElement.appendChild(optionElement);
        }
    }

    abstract void refresh();


    // ------------------------------------------------------ apply decoration

    @Override
    <C> void safeApply(Decoration decoration, C context) {
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

            case HELP:
                HTMLElement helpElement = a()
                        .css("popover-pf-info")
                        .attr("role", "button")
                        .attr("data-toggle", "popover")
                        .attr("tabindex", "0")
                        .add(span().css("pficon pficon-info")).
                        element();
                HelpPopover helpPopover = new HelpPopover(labelElement.title, (SafeHtml) context, helpElement);
                labelElement.setAttribute("class", labelElement.getAttribute("class") + " help-padding");
                labelGroup.appendChild(helpElement);
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
                            }).element();
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
            case SENSITIVE:
            case SUGGESTIONS:
                break;
            default:
                break;
        }
    }


    // ------------------------------------------------------ unapply decoration

    @Override
    void safeUnapply(Decoration decoration) {
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
            case SENSITIVE:
            case SUGGESTIONS:
                break;
            default:
                break;
        }
    }


    // ------------------------------------------------------ properties & delegates

    @Override
    public void setId(String id) {
        this.id = Ids.build(id, EDITING.name().toLowerCase());
        root.dataset.set(FORM_ITEM_GROUP, this.id);
        selectElement.id = this.id;
        labelElement.htmlFor = this.id;
    }

    @Override
    public void setName(String name) {
        selectElement.name = name;
    }

    @Override
    public int getTabIndex() {
        return (int) selectElement.tabIndex;
    }

    @Override
    public void setAccessKey(char key) {
        // noop
    }

    @Override
    public void setFocus(boolean focused) {
        if (focused) {
            //selectElement.focus();
        } else {
            selectElement.blur();
        }
    }

    @Override
    public void setTabIndex(int index) {
        selectElement.tabIndex = index;
    }
}
