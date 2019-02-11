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
package org.jboss.hal.client.runtime.subsystem.logging;

import com.google.gwt.user.client.rpc.AsyncCallback;
import elemental2.dom.HTMLElement;
import org.jboss.hal.core.finder.PreviewAttributes;
import org.jboss.hal.core.finder.PreviewAttributes.PreviewAttribute;
import org.jboss.hal.core.finder.PreviewContent;
import org.jboss.hal.resources.CSS;
import org.jboss.hal.resources.Icons;
import org.jboss.hal.resources.Resources;

import static org.jboss.gwt.elemento.core.Elements.*;
import static org.jboss.hal.client.runtime.subsystem.logging.LogFiles.LOG_FILE_SIZE_THRESHOLD;
import static org.jboss.hal.resources.CSS.*;

class LogFilePreview extends PreviewContent<LogFile> {

    private static final int PREVIEW_LINES = 20;

    private final LogFiles logFiles;
    private final Resources resources;
    private final HTMLElement preview;

    LogFilePreview(LogFiles logFiles, LogFile logFile, Resources resources) {
        super(logFile.getFilename());
        this.logFiles = logFiles;
        this.resources = resources;

        HTMLElement container, icon, message;
        previewBuilder()
                .add(container = div()
                        .add(icon = span().get())
                        .add(message = span().get())
                        .add(" ")
                        .add(a(logFiles.downloadUrl(logFile.getFilename(), logFile.getLoggingProfile())).css(alertLink)
                                .apply(a -> a.download = logFile.getFilename())
                                .textContent(resources.constants().download()))
                        .get());
        if (logFile.getSize() > LOG_FILE_SIZE_THRESHOLD) {
            container.classList.add(CSS.alert, alertWarning);
            icon.className = Icons.WARNING;
            message.innerHTML = resources.messages().largeLogFile(logFile.getFormattedSize()).asString();
        } else {
            container.classList.add(CSS.alert, alertInfo);
            icon.className = Icons.INFO;
            message.innerHTML = resources.messages().normalLogFile(logFile.getFormattedSize()).asString();
        }

        PreviewAttributes<LogFile> previewAttributes = new PreviewAttributes<>(logFile)
                .append(model ->
                        new PreviewAttribute(resources.constants().lastModified(),
                                logFile.getFormattedLastModifiedDate()))
                .append(model ->
                        new PreviewAttribute(resources.constants().size(), logFile.getFormattedSize()));
        if (logFile.getLoggingProfile() != null) {
            previewAttributes.append(mode -> new PreviewAttribute(resources.constants().loggingProfile(),
                    logFile.getLoggingProfile()));
        }
        previewBuilder().addAll(previewAttributes);

        previewBuilder()
                .add(h(2).textContent(resources.constants().preview()))
                .add(div().css(clearfix)
                        .add(refreshLink(() -> update(logFile)))
                        .add(p().textContent(resources.messages().logFilePreview(PREVIEW_LINES))))
                .add(preview = pre().css(logFilePreview).get());
    }

    @Override
    public void update(LogFile item) {
        logFiles.tail(item.getFilename(), item.getLoggingProfile(), PREVIEW_LINES, new AsyncCallback<String>() {
            @Override
            public void onFailure(Throwable caught) {
                preview.textContent = resources.constants().logFilePreviewError();
            }

            @Override
            public void onSuccess(String result) {
                preview.textContent = result;
            }
        });
    }
}
