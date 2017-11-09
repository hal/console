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
package org.jboss.hal.client.deployment.wizard;

import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtml;
import elemental2.dom.HTMLElement;
import elemental2.dom.HTMLInputElement;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.gwt.elemento.core.IsElement;
import org.jboss.hal.ballroom.Alert;
import org.jboss.hal.js.JsHelper;
import org.jboss.hal.resources.Constants;
import org.jboss.hal.resources.Icons;
import org.jboss.hal.resources.Ids;

import static org.jboss.gwt.elemento.core.Elements.*;
import static org.jboss.gwt.elemento.core.Elements.form;
import static org.jboss.gwt.elemento.core.Elements.label;
import static org.jboss.gwt.elemento.core.EventType.change;
import static org.jboss.gwt.elemento.core.InputType.file;
import static org.jboss.hal.resources.CSS.*;
import static org.jboss.hal.resources.FontAwesomeSize.x4;

/** Generic upload component used in various wizards and dialogs. */
public class UploadElement implements IsElement<HTMLElement> {

    private static final Constants CONSTANTS = GWT.create(Constants.class);

    private HTMLElement root;
    private Alert alert;
    private HTMLInputElement fileInput;
    private HTMLElement labelElement;

    public UploadElement(final SafeHtml noFilesError) {
        HTMLElement iconElement, dragElement;

        this.alert = new Alert(Icons.ERROR, noFilesError);
        this.root = form().css(upload)
                .apply(f -> f.noValidate = true)
                .add(alert)
                .add(iconElement = div().css(uploadIcon, fontAwesome("upload", x4)).asElement())
                .add(fileInput = input(file)
                        .id(Ids.UPLOAD_FILE_INPUT)
                        .css(uploadFile)
                        .on(change, event -> showFiles(fileInput.files))
                        .asElement())
                .add(labelElement = label()
                        .apply(l -> l.htmlFor = Ids.UPLOAD_FILE_INPUT)
                        .add(a().css(clickable)
                                .textContent(CONSTANTS.chooseFile())
                                .add(dragElement = span().textContent(" " + CONSTANTS.orDragItHere()).asElement()))
                        .asElement())
                .asElement();

        Elements.setVisible(alert.asElement(), false);
        boolean advancedUpload = JsHelper.supportsAdvancedUpload();
        if (advancedUpload) {
            root.classList.add(uploadAdvanced);
            JsHelper.addDropHandler(root, event -> {
                fileInput.files = event.dataTransfer.files;
                showFiles(event.dataTransfer.files);
            });
        } else {
            root.classList.remove(uploadAdvanced);
        }
        Elements.setVisible(iconElement, advancedUpload);
        Elements.setVisible(dragElement, advancedUpload);
    }

    @Override
    public HTMLElement asElement() {
        return root;
    }

    private void showFiles(elemental2.dom.FileList files) {
        if (files.getLength() > 0) {
            elemental2.dom.File file = files.item(0);
            labelElement.textContent = file.name;
            Elements.setVisible(alert.asElement(), false);
        }
    }

    public void reset() {
        Elements.setVisible(alert.asElement(), false);
    }

    public boolean validate() {
        boolean valid = fileInput.files.getLength() > 0;
        Elements.setVisible(alert.asElement(), !valid);
        return valid;
    }

    public elemental2.dom.FileList getFiles() {
        return fileInput.files;
    }
}
