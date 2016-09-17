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

import elemental.dom.Element;
import elemental.html.File;
import elemental.html.FileList;
import elemental.html.InputElement;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.hal.ballroom.Alert;
import org.jboss.hal.ballroom.js.JsHelper;
import org.jboss.hal.ballroom.wizard.WizardStep;
import org.jboss.hal.resources.Icons;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Resources;

import static org.jboss.gwt.elemento.core.EventType.change;
import static org.jboss.gwt.elemento.core.InputType.file;
import static org.jboss.hal.resources.CSS.*;
import static org.jboss.hal.resources.FontAwesomeSize.x4;

/**
 * @author Harald Pehl
 */
public class UploadContentStep extends WizardStep<ContentContext, ContentState> {

    private static final String ICON_ELEMENT = "iconElement";
    private static final String FILE_INPUT_ELEMENT = "fileInputElement";
    private static final String LABEL_ELEMENT = "labelElement";
    private static final String DRAG_ELEMENT = "dragElement";

    private Element root;
    private Alert alert;
    private Element iconElement;
    private InputElement fileInput;
    private Element labelElement;
    private Element dragElement;

    public UploadContentStep(final Resources resources) {
        super(Ids.CONTENT_ADD_MANAGED_UPLOAD_STEP, resources.constants().uploadContent());

        alert = new Alert(Icons.ERROR, resources.messages().noContent());

        // @formatter:off
        Elements.Builder builder = new Elements.Builder()
            .add(alert)
            .form().attr("novalidate", "true").css(upload) //NON-NLS
                .div().rememberAs(ICON_ELEMENT).css(uploadIcon, fontAwesome("upload", x4)).end()
                .input(file)
                    .rememberAs(FILE_INPUT_ELEMENT)
                    .id(Ids.CONTENT_ADD_MANAGED_UPLOAD_FILE_INPUT)
                    .css(uploadFile)
                    .on(change, event -> showFiles(fileInput.getFiles()))
                .label().attr("for", Ids.CONTENT_ADD_MANAGED_UPLOAD_FILE_INPUT).rememberAs(LABEL_ELEMENT)
                    .a().css(clickable)
                        .textContent(resources.constants().chooseFile())
                    .end()
                    .span().rememberAs(DRAG_ELEMENT)
                        .textContent(" " + resources.constants().orDragItHere())
                    .end()
                .end()
            .end();
        // @formatter:on

        iconElement = builder.referenceFor(ICON_ELEMENT);
        fileInput = builder.referenceFor(FILE_INPUT_ELEMENT);
        labelElement = builder.referenceFor(LABEL_ELEMENT);
        dragElement = builder.referenceFor(DRAG_ELEMENT);
        root = builder.build();
    }

    @Override
    public Element asElement() {
        return root;
    }

    @Override
    public void reset(final ContentContext context) {
        context.file = null;
    }

    @Override
    protected void onShow(final ContentContext context) {
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

    private void showFiles(FileList files) {
        if (files.getLength() > 0) {
            File file = files.item(0);
            labelElement.setInnerHTML(file.getName());
            wizard().getContext().file = file;
        }
    }

    @Override
    protected boolean onNext(final ContentContext context) {
        boolean valid = fileInput.getFiles() != null && fileInput.getFiles().getLength() > 0;
        if (!valid) {
            Elements.setVisible(alert.asElement(), true);
        }
        return valid;
    }
}
