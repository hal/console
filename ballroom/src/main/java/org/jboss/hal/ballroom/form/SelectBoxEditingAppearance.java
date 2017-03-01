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
import elemental.client.Browser;
import elemental.dom.Element;
import elemental.html.OptionElement;
import elemental.html.SelectElement;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.hal.dmr.model.Deprecation;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.UIConstants;

import static com.google.common.base.Strings.emptyToNull;
import static org.jboss.hal.ballroom.form.Decoration.*;
import static org.jboss.hal.ballroom.form.Form.State.EDITING;
import static org.jboss.hal.dmr.ModelDescriptionConstants.UNDEFINED;
import static org.jboss.hal.resources.CSS.*;

/**
 * @author Harald Pehl
 */
abstract class SelectBoxEditingAppearance<T> extends AbstractAppearance<T> {

    private static final String INPUT_CONTAINER = "inputContainer";

    final SelectElement selectElement;
    private final Element root;
    private final Element inputContainer;
    private final Element helpBlock;
    private Element inputGroup;
    private elemental.html.InputElement restrictedInput;
    private Element restrictedMarker;
    final boolean allowEmpty;
    boolean attached;

    SelectBoxEditingAppearance(final SelectElement selectElement, final List<String> options,
            final boolean allowEmpty) {
        super(EnumSet.of(DEFAULT, DEPRECATED, ENABLED, INVALID, REQUIRED, RESTRICTED));
        this.selectElement = selectElement;
        this.selectElement.getClassList().add(formControl);
        this.selectElement.getClassList().add(selectpicker);
        this.allowEmpty = allowEmpty;

        List<String> localOptions = options;
        if (allowEmpty && !options.isEmpty() && emptyToNull(options.get(0)) != null) {
            localOptions = new ArrayList<>(options);
            localOptions.add(0, "");
        }
        for (String option : localOptions) {
            OptionElement optionElement = Browser.getDocument().createOptionElement();
            optionElement.setText(option);
            if (emptyToNull(option) == null) {
                optionElement.setTitle(UNDEFINED);
            }
            this.selectElement.appendChild(optionElement);
        }

        // @formatter:off
        Elements.Builder builder = new Elements.Builder()
            .div().css(formGroup)
                .label().css(controlLabel, halFormLabel).rememberAs(LABEL_ELEMENT).end()
                .div().css(halFormInput).rememberAs(INPUT_CONTAINER)
                    .add(selectElement)
                .end()
            .end();
        // @formatter:on

        helpBlock = Appearance.helpBlock();
        labelElement = builder.referenceFor(LABEL_ELEMENT);
        inputContainer = builder.referenceFor(INPUT_CONTAINER);
        root = builder.build();
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
    public Element asElement() {
        return root;
    }

    abstract void refresh();


    // ------------------------------------------------------ apply decoration

    @Override
    <C> void safeApply(final Decoration decoration, final C context) {
        switch (decoration) {

            case DEFAULT:
                String defaultValue = String.valueOf(context);
                selectElement.setTitle(Strings.isNullOrEmpty(defaultValue) ? UNDEFINED : defaultValue);
                if (attached) {
                    refresh();
                }
                break;

            case DEPRECATED:
                markAsDeprecated((Deprecation) context);
                break;

            case ENABLED:
                selectElement.setDisabled(false);
                if (attached) {
                    refresh();
                }
                break;

            case INVALID:
                helpBlock.setTextContent(String.valueOf(context));
                root.getClassList().add(hasError);
                inputContainer.appendChild(helpBlock);
                break;

            case REQUIRED:
                markAsRequired();
                break;

            case RESTRICTED:
                if (inputGroup == null && restrictedInput == null && restrictedMarker == null) {
                    restrictedInput = Browser.getDocument().createInputElement();
                    restrictedInput.setType("text"); //NON-NLS
                    restrictedInput.setValue(CONSTANTS.restricted());
                    restrictedInput.setAttribute(UIConstants.READONLY, UIConstants.TRUE);
                    restrictedInput.getClassList().add(restricted);

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
                selectElement.setTitle("");
                if (attached) {
                    refresh();
                }
                break;

            case DEPRECATED:
                clearDeprecation();
                break;

            case ENABLED:
                selectElement.setDisabled(true);
                if (attached) {
                    refresh();
                }
                break;

            case INVALID:
                root.getClassList().remove(hasError);
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
        root.getDataset().setAt(FORM_ITEM_GROUP, this.id);
        selectElement.setId(this.id);
        labelElement.setHtmlFor(this.id);
    }

    @Override
    public void setName(final String name) {
        selectElement.setName(name);
    }

    @Override
    public int getTabIndex() {
        return selectElement.getTabIndex();
    }

    @Override
    public void setAccessKey(final char key) {
        selectElement.setAccessKey(String.valueOf(key));
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
        selectElement.setTabIndex(index);
    }
}
