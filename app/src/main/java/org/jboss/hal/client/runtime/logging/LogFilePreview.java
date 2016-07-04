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
package org.jboss.hal.client.runtime.logging;

import org.jboss.gwt.elemento.core.Elements;
import org.jboss.hal.ballroom.Alert;
import org.jboss.hal.ballroom.Format;
import org.jboss.hal.core.finder.PreviewAttributes;
import org.jboss.hal.core.finder.PreviewContent;
import org.jboss.hal.resources.Icons;
import org.jboss.hal.resources.Resources;

import static org.jboss.hal.client.runtime.logging.LogFileColumn.LOG_FILE_SIZE_THRESHOLD;

/**
 * @author Harald Pehl
 */
class LogFilePreview extends PreviewContent<LogFile> {

    private final Alert alert;

    LogFilePreview(LogFiles logFiles, LogFile logFile, final Resources resources) {
        super(logFile.getFilename());

        alert = new Alert(Icons.WARNING, resources.messages().largeLogFile(
                logFile.getFilename(), Format.humanReadableFileSize(LOG_FILE_SIZE_THRESHOLD)),
                resources.constants().download(), event -> logFiles.download(logFile.getFilename()));
        previewBuilder().add(alert);

        PreviewAttributes<LogFile> previewAttributes = new PreviewAttributes<>(logFile)
                .append(model ->
                        new String[]{resources.constants().lastModified(), logFile.getFormattedLastModifiedDate()})
                .append(model ->
                        new String[]{resources.constants().size(), logFile.getFormattedSize()})
                .end();
        previewBuilder().addAll(previewAttributes);
    }

    @Override
    public void update(final LogFile item) {
        Elements.setVisible(alert.asElement(), item.getSize() > LOG_FILE_SIZE_THRESHOLD);
    }
}
