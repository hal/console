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
import javax.annotation.PostConstruct;

import elemental.client.Browser;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.hal.ballroom.EmptyState;
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

import static org.jboss.hal.resources.CSS.marginTop20;

/**
 * @author Harald Pehl
 */
@MbuiView
@SuppressWarnings({"DuplicateStringLiteralInspection", "HardCodedStringLiteral", "WeakerAccess"})
public abstract class LoggingProfileView extends MbuiViewImpl<LoggingProfilePresenter> implements LoggingProfilePresenter.MyView {

    public static LoggingProfileView create(final MbuiContext mbuiContext) {
        return new Mbui_LoggingProfileView(mbuiContext);
    }

    @MbuiElement("logging-profile-vertical-navigation") VerticalNavigation navigation;
    @MbuiElement("logging-profile-root-logger-form") Form<ModelNode> rootLoggerForm;
    @MbuiElement("logging-profile-categories-table") DataTable<NamedNode> loggerTable;
    @MbuiElement("logging-profile-categories-form") Form<NamedNode> loggerForm;
    @MbuiElement("logging-profile-handler-console-table") DataTable<NamedNode> consoleHandlerTable;
    @MbuiElement("logging-profile-handler-console-form") Form<NamedNode> consoleHandlerForm;
    @MbuiElement("logging-profile-handler-file-table") DataTable<NamedNode> fileHandlerTable;
    @MbuiElement("logging-profile-handler-file-form") Form<NamedNode> fileHandlerForm;
    @MbuiElement("logging-profile-handler-periodic-rotating-file-table") DataTable<NamedNode> periodicHandlerTable;
    @MbuiElement("logging-profile-handler-periodic-rotating-file-form") Form<NamedNode> periodicHandlerForm;
    @MbuiElement("logging-profile-handler-periodic-size-rotating-file-table") DataTable<NamedNode> periodicSizeHandlerTable;
    @MbuiElement("logging-profile-handler-periodic-size-rotating-file-form") Form<NamedNode> periodicSizeHandlerForm;
    @MbuiElement("logging-profile-handler-size-rotating-file-table") DataTable<NamedNode> sizeHandlerTable;
    @MbuiElement("logging-profile-handler-size-rotating-file-form") Form<NamedNode> sizeHandlerForm;
    @MbuiElement("logging-profile-handler-async-table") DataTable<NamedNode> asyncHandlerTable;
    @MbuiElement("logging-profile-handler-async-form") Form<NamedNode> asyncHandlerForm;
    @MbuiElement("logging-profile-handler-custom-table") DataTable<NamedNode> customHandlerTable;
    @MbuiElement("logging-profile-handler-custom-form") Form<NamedNode> customHandlerForm;
    @MbuiElement("logging-profile-handler-syslog-table") DataTable<NamedNode> syslogHandlerTable;
    @MbuiElement("logging-profile-handler-syslog-form") Form<NamedNode> syslogHandlerForm;
    @MbuiElement("logging-profile-formatter-custom-table") DataTable<NamedNode> customFormatterTable;
    @MbuiElement("logging-profile-formatter-custom-form") Form<NamedNode> customFormatterForm;
    @MbuiElement("logging-profile-formatter-pattern-table") DataTable<NamedNode> patternFormatterTable;
    @MbuiElement("logging-profile-formatter-pattern-form") Form<NamedNode> patternFormatterForm;

    EmptyState noRootLogger;

    LoggingProfileView(final MbuiContext mbuiContext) {
        super(mbuiContext);
    }

    @PostConstruct
    void init() {
        noRootLogger = new EmptyState.Builder(mbuiContext.resources().constants().noRootLogger())
                .description(mbuiContext.resources().constants().noRootLoggerDescription())
                .icon("fa fa-sitemap")
                .primaryAction(mbuiContext.resources().constants().add(), event -> presenter.addRootLogger())
                .build();
        noRootLogger.asElement().getClassList().add(marginTop20);

        // hack which relies on the element hierarchy given in the template. will break if you change that hierarchy.
        rootLoggerForm.asElement().getParentElement().appendChild(noRootLogger.asElement());
        rootLoggerVisibility(false);
    }

    LoggingProfilePresenter getPresenter() {
        return presenter;
    }

    @Override
    public VerticalNavigation getVerticalNavigation() {
        return navigation;
    }

    @Override
    public void updateRootLogger(final ModelNode modelNode) {
        rootLoggerVisibility(true);
        rootLoggerForm.view(modelNode);
    }

    @Override
    public void noRootLogger() {
        rootLoggerVisibility(false);
    }

    private void rootLoggerVisibility(final boolean visible) {
        Elements.setVisible(Browser.getDocument().getElementById("logging-profile-root-logger-header"), visible);
        Elements.setVisible(Browser.getDocument().getElementById("logging-profile-root-logger-description"), visible);
        Elements.setVisible(rootLoggerForm.asElement(), visible);
        Elements.setVisible(noRootLogger.asElement(), !visible);
    }

    @Override
    public void updateLogger(final List<NamedNode> items) {
        loggerTable.api().clear().add(items).refresh(RefreshMode.RESET);
        loggerForm.clear();
    }

    @Override
    public void updateConsoleHandler(final List<NamedNode> items) {
        navigation.updateBadge("logging-profile-handler-console-item", items.size());
        consoleHandlerTable.api().clear().add(items).refresh(RefreshMode.RESET);
        consoleHandlerForm.clear();
    }

    @Override
    public void updateFileHandler(final List<NamedNode> items) {
        navigation.updateBadge("logging-profile-handler-file-item", items.size());
        fileHandlerTable.api().clear().add(items).refresh(RefreshMode.RESET);
        fileHandlerForm.clear();
    }

    @Override
    public void updatePeriodicHandler(final List<NamedNode> items) {
        navigation.updateBadge("logging-profile-handler-periodic-rotating-file-item", items.size());
        periodicHandlerTable.api().clear().add(items).refresh(RefreshMode.RESET);
        periodicHandlerForm.clear();
    }

    @Override
    public void updatePeriodicSizeHandler(final List<NamedNode> items) {
        navigation.updateBadge("logging-profile-handler-periodic-size-rotating-file-item", items.size());
        periodicSizeHandlerTable.api().clear().add(items).refresh(RefreshMode.RESET);
        periodicSizeHandlerForm.clear();
    }

    @Override
    public void updateSizeHandlerHandler(final List<NamedNode> items) {
        navigation.updateBadge("logging-profile-handler-size-rotating-file-item", items.size());
        sizeHandlerTable.api().clear().add(items).refresh(RefreshMode.RESET);
        sizeHandlerForm.clear();
    }

    @Override
    public void updateAsyncHandler(final List<NamedNode> items) {
        navigation.updateBadge("logging-profile-handler-async-item", items.size());
        asyncHandlerTable.api().clear().add(items).refresh(RefreshMode.RESET);
        asyncHandlerForm.clear();
    }

    @Override
    public void updateCustomHandler(final List<NamedNode> items) {
        navigation.updateBadge("logging-profile-handler-custom-item", items.size());
        customHandlerTable.api().clear().add(items).refresh(RefreshMode.RESET);
        customHandlerForm.clear();
    }

    @Override
    public void updateSyslogHandler(final List<NamedNode> items) {
        navigation.updateBadge("logging-profile-handler-syslog-item", items.size());
        syslogHandlerTable.api().clear().add(items).refresh(RefreshMode.RESET);
        syslogHandlerForm.clear();
    }

    @Override
    public void updateCustomFormatter(final List<NamedNode> items) {
        navigation.updateBadge("logging-profile-formatter-custom-item", items.size());
        customFormatterTable.api().clear().add(items).refresh(RefreshMode.RESET);
        customFormatterForm.clear();
    }

    @Override
    public void updatePatternFormatter(final List<NamedNode> items) {
        navigation.updateBadge("logging-profile-formatter-pattern-item", items.size());
        patternFormatterTable.api().clear().add(items).refresh(RefreshMode.RESET);
        patternFormatterForm.clear();
    }
}
