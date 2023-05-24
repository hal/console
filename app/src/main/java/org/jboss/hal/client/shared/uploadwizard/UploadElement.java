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
package org.jboss.hal.client.shared.uploadwizard;

import org.jboss.elemento.Elements;
import org.jboss.elemento.IsElement;
import org.jboss.hal.ballroom.Alert;
import org.jboss.hal.js.JsHelper;
import org.jboss.hal.resources.Constants;
import org.jboss.hal.resources.Icons;
import org.jboss.hal.resources.Ids;

import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtml;

import elemental2.dom.HTMLElement;
import elemental2.dom.HTMLInputElement;

import static org.jboss.elemento.Elements.div;
import static org.jboss.elemento.Elements.form;
import static org.jboss.elemento.Elements.input;
import static org.jboss.elemento.Elements.label;
import static org.jboss.elemento.Elements.li;
import static org.jboss.elemento.Elements.removeChildrenFrom;
import static org.jboss.elemento.Elements.span;
import static org.jboss.elemento.Elements.ul;
import static org.jboss.elemento.EventType.change;
import static org.jboss.elemento.InputType.file;
import static org.jboss.hal.resources.CSS.clickable;
import static org.jboss.hal.resources.CSS.fontAwesome;
import static org.jboss.hal.resources.CSS.link;
import static org.jboss.hal.resources.CSS.upload;
import static org.jboss.hal.resources.CSS.uploadAdvanced;
import static org.jboss.hal.resources.CSS.uploadFile;
import static org.jboss.hal.resources.CSS.uploadFileList;
import static org.jboss.hal.resources.CSS.uploadIcon;
import static org.jboss.hal.resources.FontAwesomeSize.x4;

/** Generic upload component used in various wizards and dialogs. */
public class UploadElement implements IsElement<HTMLElement> {

    private static final Constants CONSTANTS = GWT.create(Constants.class);

    private final boolean single;
    private final HTMLElement root;
    private final Alert alert;
    private final HTMLElement fileListElement;
    private HTMLInputElement fileInput;

    public UploadElement(boolean single, SafeHtml noFilesError) {
        this.single = single;
        HTMLElement iconElement, dragElement;

        this.alert = new Alert(Icons.ERROR, noFilesError);
        String choose = single ? CONSTANTS.chooseOrDragFile() : CONSTANTS.chooseOrDragFiles();
        this.root = form().css(upload)
                .apply(f -> f.noValidate = true)
                .add(alert)
                .add(iconElement = div().css(uploadIcon, fontAwesome("upload", x4)).element())
                .add(fileInput = input(file)
                        .id(Ids.UPLOAD_FILE_INPUT)
                        .css(uploadFile)
                        .apply(f -> f.multiple = !single)
                        .on(change, event -> showFiles(fileInput.files)).element())
                .add(label()
                        .apply(l -> l.htmlFor = Ids.UPLOAD_FILE_INPUT)
                        .css(clickable, link)
                        .add(dragElement = span().textContent(choose).element()).element())
                .add(fileListElement = ul().css(uploadFileList).element())
                .element();

        Elements.setVisible(alert.element(), false);
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
    public HTMLElement element() {
        return root;
    }

    private void showFiles(elemental2.dom.FileList files) {
        if (single) {
            removeChildrenFrom(fileListElement);
        }
        if (files.getLength() > 0) {
            for (int i = 0; i < files.getLength(); i++) {
                elemental2.dom.File file = files.item(i);
                ul(fileListElement).add(li().textContent(file.name));
            }
            Elements.setVisible(alert.element(), false);
        }
    }

    public void reset() {
        Elements.setVisible(alert.element(), false);
    }

    public boolean validate() {
        boolean valid = fileInput.files.getLength() > 0;
        Elements.setVisible(alert.element(), !valid);
        return valid;
    }

    public elemental2.dom.FileList getFiles() {
        return fileInput.files;
    }
}
