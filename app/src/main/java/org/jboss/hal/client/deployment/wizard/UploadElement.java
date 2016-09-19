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
import elemental.dom.Element;
import elemental.html.File;
import elemental.html.FileList;
import elemental.html.InputElement;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.gwt.elemento.core.IsElement;
import org.jboss.hal.ballroom.Alert;
import org.jboss.hal.ballroom.js.JsHelper;
import org.jboss.hal.resources.Constants;
import org.jboss.hal.resources.Icons;
import org.jboss.hal.resources.Ids;

import static org.jboss.gwt.elemento.core.EventType.change;
import static org.jboss.gwt.elemento.core.InputType.file;
import static org.jboss.hal.resources.CSS.*;
import static org.jboss.hal.resources.FontAwesomeSize.x4;

/**
 * Generic upload component used in various wizards and dialogs.
 *
 * @author Harald Pehl
 */
public class UploadElement implements IsElement {

    private static final Constants CONSTANTS = GWT.create(Constants.class);
    private static final String ICON_ELEMENT = "iconElement";
    private static final String FILE_INPUT_ELEMENT = "fileInputElement";
    private static final String LABEL_ELEMENT = "labelElement";
    private static final String DRAG_ELEMENT = "dragElement";

    private Element root;
    private Alert alert;
    private InputElement fileInput;
    private Element labelElement;

    public UploadElement(final SafeHtml noFilesError) {
        this.alert = new Alert(Icons.ERROR, noFilesError);

        // @formatter:off
        Elements.Builder builder = new Elements.Builder()
            .form().attr("novalidate", "true").css(upload) //NON-NLS
                .add(alert)
                .div().rememberAs(ICON_ELEMENT).css(uploadIcon, fontAwesome("upload", x4)).end()
                .input(file)
                    .rememberAs(FILE_INPUT_ELEMENT)
                    .id(Ids.UPLOAD_FILE_INPUT)
                    .css(uploadFile)
                    .on(change, event -> showFiles(fileInput.getFiles()))
                .label().attr("for", Ids.UPLOAD_FILE_INPUT).rememberAs(LABEL_ELEMENT)
                    .a().css(clickable)
                        .textContent(CONSTANTS.chooseFile())
                    .end()
                    .span().rememberAs(DRAG_ELEMENT)
                        .textContent(" " + CONSTANTS.orDragItHere())
                    .end()
                .end()
            .end();
        // @formatter:on

        Element iconElement = builder.referenceFor(ICON_ELEMENT);
        fileInput = builder.referenceFor(FILE_INPUT_ELEMENT);
        labelElement = builder.referenceFor(LABEL_ELEMENT);
        Element dragElement = builder.referenceFor(DRAG_ELEMENT);
        root = builder.build();

        Elements.setVisible(alert.asElement(), false);
        boolean advancedUpload = JsHelper.supportsAdvancedUpload();
        if (advancedUpload) {
            root.getClassList().add(uploadAdvanced);
            JsHelper.addDropHandler(root, event -> {
                fileInput.setFiles(event.dataTransfer.files);
                showFiles(event.dataTransfer.files);
            });
        } else {
            root.getClassList().remove(uploadAdvanced);
        }
        Elements.setVisible(iconElement, advancedUpload);
        Elements.setVisible(dragElement, advancedUpload);
    }

    @Override
    public Element asElement() {
        return root;
    }

    private void showFiles(FileList files) {
        if (files.getLength() > 0) {
            File file = files.item(0);
            labelElement.setInnerHTML(file.getName());
            Elements.setVisible(alert.asElement(), false);
        }
    }

    public void reset() {
        Elements.setVisible(alert.asElement(), false);
    }

    public boolean validate() {
        boolean valid = fileInput.getFiles().getLength() > 0;
        Elements.setVisible(alert.asElement(), !valid);
        return valid;
    }

    public FileList getFiles() {
        return fileInput.getFiles();
    }
}
