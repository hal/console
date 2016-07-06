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

import org.jboss.hal.core.finder.PreviewAttributes;
import org.jboss.hal.core.finder.PreviewContent;
import org.jboss.hal.resources.Icons;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.resources.UIConstants;

import static org.jboss.hal.client.runtime.subsystem.logging.LogFiles.LOG_FILE_SIZE_THRESHOLD;
import static org.jboss.hal.resources.CSS.alert;
import static org.jboss.hal.resources.CSS.alertInfo;
import static org.jboss.hal.resources.CSS.alertLink;
import static org.jboss.hal.resources.CSS.alertWarning;

/**
 * @author Harald Pehl
 */
class LogFilePreview extends PreviewContent<LogFile> {

    LogFilePreview(LogFiles logFiles, LogFile logFile, final Resources resources) {
        super(logFile.getFilename());

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
                        new String[]{resources.constants().lastModified(), logFile.getFormattedLastModifiedDate()})
                .append(model ->
                        new String[]{resources.constants().size(), logFile.getFormattedSize()})
                .end();
        previewBuilder().addAll(previewAttributes);
    }
}
