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

import java.util.EnumSet;

import com.google.gwt.core.client.GWT;
import elemental2.dom.File;
import elemental2.dom.HTMLInputElement;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.hal.resources.Constants;
import org.jboss.hal.resources.UIConstants;

import static org.jboss.gwt.elemento.core.Elements.input;
import static org.jboss.gwt.elemento.core.Elements.label;
import static org.jboss.gwt.elemento.core.Elements.span;
import static org.jboss.gwt.elemento.core.EventType.bind;
import static org.jboss.gwt.elemento.core.EventType.change;
import static org.jboss.gwt.elemento.core.InputType.file;
import static org.jboss.hal.ballroom.form.Decoration.ENABLED;
import static org.jboss.hal.ballroom.form.Decoration.HINT;
import static org.jboss.hal.ballroom.form.Decoration.INVALID;
import static org.jboss.hal.ballroom.form.Decoration.REQUIRED;
import static org.jboss.hal.resources.CSS.*;

public class FileItem extends AbstractFormItem<File> {

    private final HTMLInputElement fileInput;

    public FileItem(String name, String label) {
        super(name, label, null);
        addAppearance(Form.State.READONLY, new FileReadOnlyAppearance());

        fileInput = input(file).css(formControl).element();
        FileEditingAppearance editingAppearance = new FileEditingAppearance(fileInput);
        addAppearance(Form.State.EDITING, editingAppearance);
    }

    @Override
    public void attach() {
        super.attach();
        remember(bind(fileInput, change, event -> {
            setValue(fileInput.files.getAt(0), true);
            setModified(true);
            setUndefined(false);
        }));
    }

    @Override
    public boolean isEmpty() {
        return fileInput.files.length == 0;
    }

    @Override
    public boolean supportsExpressions() {
        return false;
    }


    private static class FileReadOnlyAppearance extends ReadOnlyAppearance<File> {

        FileReadOnlyAppearance() {
            super(EnumSet.of(HINT));
        }

        @Override
        protected String name() {
            return "FileReadOnlyAppearance";
        }

        @Override
        public String asString(File value) {
            return value.name;
        }
    }


    private static class FileEditingAppearance extends EditingAppearance<File> {

        private static final Constants CONSTANTS = GWT.create(Constants.class);
        private final HTMLInputElement filename;

        FileEditingAppearance(HTMLInputElement inputElement) {
            super(EnumSet.of(ENABLED, HINT, INVALID, REQUIRED), inputElement);
            Elements.setVisible(inputElement, false);

            inputGroup.appendChild(label().css(inputGroupBtn)
                    .add(span().css(btn, btnPrimary).textContent(CONSTANTS.browse())
                            .add(inputElement)).element());
            inputGroup.appendChild(filename = input(text).css(formControl)
                    .attr(UIConstants.READONLY, "").element());
            Elements.removeChildrenFrom(inputContainer);
            inputContainer.appendChild(inputGroup);
        }

        @Override
        protected String name() {
            return "FileEditingAppearance";
        }

        @Override
        public void showValue(File value) {
            if (value != null) {
                filename.value = value.name;
            } else {
                clearValue();
            }
        }

        @Override
        public void clearValue() {
            filename.value = "";
        }
    }
}
