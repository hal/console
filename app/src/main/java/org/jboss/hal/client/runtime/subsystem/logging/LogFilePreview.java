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
import elemental.html.PreElement;
import org.jboss.hal.core.finder.PreviewAttributes;
import org.jboss.hal.core.finder.PreviewAttributes.PreviewAttribute;
import org.jboss.hal.core.finder.PreviewContent;
import org.jboss.hal.resources.Icons;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.resources.UIConstants;

import static org.jboss.gwt.elemento.core.EventType.click;
import static org.jboss.hal.client.runtime.subsystem.logging.LogFiles.LOG_FILE_SIZE_THRESHOLD;
import static org.jboss.hal.resources.CSS.*;
import static org.jboss.hal.resources.CSS.fontAwesome;
import static org.jboss.hal.resources.CSS.marginRight5;
import static org.jboss.hal.resources.CSS.pullRight;

/**
 * @author Harald Pehl
 */
class LogFilePreview extends PreviewContent<LogFile> {

    private static final int PREVIEW_LINES = 20;
    private static final String PREVIEW_ELEMENT = "previewElement";

    private final LogFiles logFiles;
    private final Resources resources;
    private final PreElement preview;

    LogFilePreview(LogFiles logFiles, LogFile logFile, Resources resources) {
        super(logFile.getFilename());
        this.logFiles = logFiles;
        this.resources = resources;

        previewBuilder().div();
        if (logFile.getSize() > LOG_FILE_SIZE_THRESHOLD) {
            previewBuilder().css(alert, alertWarning)
                    .span().css(Icons.WARNING).end()
                    .span()
                    .innerHtml(resources.messages().largeLogFile(logFile.getFormattedSize()))
                    .end();
        } else {
            previewBuilder().css(alert, alertInfo)
                    .span().css(Icons.INFO).end()
                    .span()
                    .innerHtml(resources.messages().normalLogFile(logFile.getFormattedSize()))
                    .end();
        }
        previewBuilder()
                .span().textContent(" ").end()
                .a().css(alertLink)
                .attr(UIConstants.HREF, logFiles.downloadUrl(logFile.getFilename()))
                .attr(UIConstants.DOWNLOAD, logFile.getFilename())
                .textContent(resources.constants().download())
                .end()
                .end();

        PreviewAttributes<LogFile> previewAttributes = new PreviewAttributes<>(logFile)
                .append(model ->
                        new PreviewAttribute(resources.constants().lastModified(),
                                logFile.getFormattedLastModifiedDate()))
                .append(model ->
                        new PreviewAttribute(resources.constants().size(), logFile.getFormattedSize()))
                .end();
        previewBuilder().addAll(previewAttributes);

        // @formatter:off
        previewBuilder()
                .h(2).textContent(resources.constants().preview()).end()
                .div().css(clearfix)
                    .a().css(clickable, pullRight).on(click, event -> update(logFile))
                        .span().css(fontAwesome("refresh"), marginRight5).end()
                        .span().textContent(resources.constants().refresh()).end()
                    .end()
                    .p().textContent(resources.messages().logFilePreview(PREVIEW_LINES)).end()
                .end()
                .start("pre").css(logFilePreview).rememberAs(PREVIEW_ELEMENT).end();
        // @formatter:off

        preview = previewBuilder().referenceFor(PREVIEW_ELEMENT);
    }

    @Override
    public void update(final LogFile item) {
        logFiles.tail(item.getFilename(), PREVIEW_LINES, new AsyncCallback<String>() {
            @Override
            public void onFailure(final Throwable caught) {
                preview.setTextContent(resources.constants().logFilePreviewError());
            }

            @Override
            public void onSuccess(final String result) {
                preview.setTextContent(result);
            }
        });
    }
}
