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
import org.jboss.hal.spi.MbuiElement;
import org.jboss.hal.spi.MbuiView;

/**
 * @author Harald Pehl
 */
@MbuiView
@SuppressWarnings("DuplicateStringLiteralInspection")
public abstract class LoggingView extends MbuiViewImpl implements LoggingPresenter.MyView {

    public static LoggingView create(final MbuiContext mbuiContext) {
        return new Mbui_LoggingView(mbuiContext);
    }


    @MbuiElement("logging-vertical-navigation") VerticalNavigation navigation;
    @MbuiElement("logging-root-logger-form") Form<ModelNode> rootLoggerForm;
    @MbuiElement("logging-categories-table") DataTable<NamedNode> loggerTable;
    @MbuiElement("logging-categories-form") Form<NamedNode> loggerForm;
    @MbuiElement("logging-handler-async-table") DataTable<NamedNode> asyncHandlerTable;
    @MbuiElement("logging-handler-async-form") Form<NamedNode> asyncHandlerForm;
    @MbuiElement("logging-handler-console-table") DataTable<NamedNode> consoleHandlerTable;
    @MbuiElement("logging-handler-console-form") Form<NamedNode> consoleHandlerForm;
    @MbuiElement("logging-handler-file-table") DataTable<NamedNode> fileHandlerTable;
    @MbuiElement("logging-handler-file-form") Form<NamedNode> fileHandlerForm;
    @MbuiElement("logging-formatter-custom-table") DataTable<NamedNode> customFormatterTable;
    @MbuiElement("logging-formatter-custom-form") Form<NamedNode> customFormatterForm;
    @MbuiElement("logging-formatter-pattern-table") DataTable<NamedNode> patternFormatterTable;
    @MbuiElement("logging-formatter-pattern-form") Form<NamedNode> patternFormatterForm;

    protected LoggingPresenter presenter;

    LoggingView(final MbuiContext mbuiContext) {
        super(mbuiContext);
    }

    @Override
    public void setPresenter(final LoggingPresenter presenter) {
        this.presenter = presenter;
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
    }

    @Override
    public void updateAsyncHandler(final List<NamedNode> items) {
        asyncHandlerTable.api().clear().add(items).refresh(RefreshMode.RESET);
    }

    @Override
    public void updateConsoleHandler(final List<NamedNode> items) {
        consoleHandlerTable.api().clear().add(items).refresh(RefreshMode.RESET);
    }

    @Override
    public void updateCustomHandler(final List<NamedNode> items) {
    }

    @Override
    public void updateFileHandler(final List<NamedNode> items) {
        fileHandlerTable.api().clear().add(items).refresh(RefreshMode.RESET);
    }

    @Override
    public void updatePeriodicHandler(final List<NamedNode> items) {

    }

    @Override
    public void updatePeriodicSizeHandler(final List<NamedNode> items) {

    }

    @Override
    public void updateSizeHandlerHandler(final List<NamedNode> items) {

    }

    @Override
    public void updateSyslogHandler(final List<NamedNode> items) {

    }

    @Override
    public void updateCustomFormatter(final List<NamedNode> items) {
        customFormatterTable.api().clear().add(items).refresh(RefreshMode.RESET);
    }

    @Override
    public void updatePatternFormatter(final List<NamedNode> items) {
        patternFormatterTable.api().clear().add(items).refresh(RefreshMode.RESET);
    }
}
