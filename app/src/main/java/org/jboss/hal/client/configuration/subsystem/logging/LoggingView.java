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
package org.jboss.hal.client.configuration.subsystem.logging;

import java.util.List;

import org.jboss.hal.ballroom.VerticalNavigation;
import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.ballroom.table.Api.RefreshMode;
import org.jboss.hal.ballroom.table.DataTable;
import org.jboss.hal.core.mbui.MbuiContext;
import org.jboss.hal.core.mbui.MbuiViewImpl;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.model.NamedNode;
import org.jboss.hal.resources.Names;
import org.jboss.hal.spi.MbuiElement;
import org.jboss.hal.spi.MbuiView;

/**
 * @author Harald Pehl
 */
@MbuiView
@SuppressWarnings({"DuplicateStringLiteralInspection", "HardCodedStringLiteral", "WeakerAccess"})
public abstract class LoggingView extends MbuiViewImpl<LoggingPresenter> implements LoggingPresenter.MyView {

    public static LoggingView create(final MbuiContext mbuiContext) {
        return new Mbui_LoggingView(mbuiContext);
    }

    @MbuiElement("logging-vertical-navigation") VerticalNavigation navigation;
    @MbuiElement("logging-root-logger-form") Form<ModelNode> rootLoggerForm;
    @MbuiElement("logging-categories-table") DataTable<NamedNode> loggerTable;
    @MbuiElement("logging-categories-form") Form<NamedNode> loggerForm;
    @MbuiElement("logging-handler-console-table") DataTable<NamedNode> consoleHandlerTable;
    @MbuiElement("logging-handler-console-form") Form<NamedNode> consoleHandlerForm;
    @MbuiElement("logging-handler-file-table") DataTable<NamedNode> fileHandlerTable;
    @MbuiElement("logging-handler-file-form") Form<NamedNode> fileHandlerForm;
    @MbuiElement("logging-handler-periodic-rotating-file-table") DataTable<NamedNode> periodicHandlerTable;
    @MbuiElement("logging-handler-periodic-rotating-file-form") Form<NamedNode> periodicHandlerForm;
    @MbuiElement("logging-handler-periodic-size-rotating-file-table") DataTable<NamedNode> periodicSizeHandlerTable;
    @MbuiElement("logging-handler-periodic-size-rotating-file-form") Form<NamedNode> periodicSizeHandlerForm;
    @MbuiElement("logging-handler-size-rotating-file-table") DataTable<NamedNode> sizeHandlerTable;
    @MbuiElement("logging-handler-size-rotating-file-form") Form<NamedNode> sizeHandlerForm;
    @MbuiElement("logging-handler-async-table") DataTable<NamedNode> asyncHandlerTable;
    @MbuiElement("logging-handler-async-form") Form<NamedNode> asyncHandlerForm;
    @MbuiElement("logging-handler-custom-table") DataTable<NamedNode> customHandlerTable;
    @MbuiElement("logging-handler-custom-form") Form<NamedNode> customHandlerForm;
    @MbuiElement("logging-handler-syslog-table") DataTable<NamedNode> syslogHandlerTable;
    @MbuiElement("logging-handler-syslog-form") Form<NamedNode> syslogHandlerForm;
    @MbuiElement("logging-formatter-custom-table") DataTable<NamedNode> customFormatterTable;
    @MbuiElement("logging-formatter-custom-form") Form<NamedNode> customFormatterForm;
    @MbuiElement("logging-formatter-pattern-table") DataTable<NamedNode> patternFormatterTable;
    @MbuiElement("logging-formatter-pattern-form") Form<NamedNode> patternFormatterForm;

    LoggingView(final MbuiContext mbuiContext) {
        super(mbuiContext);
    }

    @Override
    public VerticalNavigation getVerticalNavigation() {
        return navigation;
    }

    @Override
    public void updateRootLogger(final ModelNode modelNode) {
        rootLoggerForm.view(modelNode);
    }

    @Override
    public void updateLogger(final List<NamedNode> items) {
        loggerTable.api().clear().add(items).refresh(RefreshMode.RESET);
        loggerForm.clear();
    }

    @Override
    public void updateConsoleHandler(final List<NamedNode> items) {
        navigation.updateBadge("logging-handler-console-item", items.size());
        consoleHandlerTable.api().clear().add(items).refresh(RefreshMode.RESET);
        consoleHandlerForm.clear();
    }

    @Override
    public void updateFileHandler(final List<NamedNode> items) {
        navigation.updateBadge("logging-handler-file-item", items.size());
        fileHandlerTable.api().clear().add(items).refresh(RefreshMode.RESET);
        fileHandlerForm.clear();
    }

    @Override
    public void updatePeriodicHandler(final List<NamedNode> items) {
        navigation.updateBadge("logging-handler-periodic-rotating-file-item", items.size());
        periodicHandlerTable.api().clear().add(items).refresh(RefreshMode.RESET);
        periodicHandlerForm.clear();
    }

    @Override
    public void updatePeriodicSizeHandler(final List<NamedNode> items) {
        navigation.updateBadge("logging-handler-periodic-size-rotating-file-item", items.size());
        periodicSizeHandlerTable.api().clear().add(items).refresh(RefreshMode.RESET);
        periodicSizeHandlerForm.clear();
    }

    @Override
    public void updateSizeHandlerHandler(final List<NamedNode> items) {
        navigation.updateBadge("logging-handler-size-rotating-file-item", items.size());
        sizeHandlerTable.api().clear().add(items).refresh(RefreshMode.RESET);
        sizeHandlerForm.clear();
    }

    @Override
    public void updateAsyncHandler(final List<NamedNode> items) {
        navigation.updateBadge("logging-handler-async-item", items.size());
        asyncHandlerTable.api().clear().add(items).refresh(RefreshMode.RESET);
        asyncHandlerForm.clear();
    }

    @Override
    public void updateCustomHandler(final List<NamedNode> items) {
        navigation.updateBadge("logging-handler-custom-item", items.size());
        customHandlerTable.api().clear().add(items).refresh(RefreshMode.RESET);
        customHandlerForm.clear();
    }

    @Override
    public void updateSyslogHandler(final List<NamedNode> items) {
        navigation.updateBadge("logging-handler-syslog-item", items.size());
        syslogHandlerTable.api().clear().add(items).refresh(RefreshMode.RESET);
        syslogHandlerForm.clear();
    }

    @Override
    public void updateCustomFormatter(final List<NamedNode> items) {
        navigation.updateBadge("logging-formatter-custom-item", items.size());
        customFormatterTable.api().clear().add(items).refresh(RefreshMode.RESET);
        customFormatterForm.clear();
    }

    @Override
    public void updatePatternFormatter(final List<NamedNode> items) {
        navigation.updateBadge("logging-formatter-pattern-item", items.size());
        patternFormatterTable.api().clear().add(items).refresh(RefreshMode.RESET);
        patternFormatterForm.clear();
    }

    /**
     * Helper method to build the filename using the nested ({@code relative-to/path}) attributes.
     */
    String getFilename(NamedNode node) {
        String filename = Names.NOT_AVAILABLE;
        if (node.hasDefined("file") && node.get("file").hasDefined("path")) {
            ModelNode file = node.get("file");
            if (file.hasDefined("relative-to")) {
                filename = file.get("relative-to").asString() + "/" + file.get("path").asString();
            } else {
                filename = file.get("path").asString();
            }
        }
        return filename;
    }
}
