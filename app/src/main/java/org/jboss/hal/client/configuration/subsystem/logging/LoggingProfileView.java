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
import java.util.Map;
import javax.annotation.PostConstruct;

import elemental2.dom.DomGlobal;
import elemental2.dom.HTMLElement;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.hal.ballroom.EmptyState;
import org.jboss.hal.ballroom.VerticalNavigation;
import org.jboss.hal.ballroom.autocomplete.ReadChildrenAutoComplete;
import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.ballroom.form.FormItem;
import org.jboss.hal.ballroom.form.SuggestHandler;
import org.jboss.hal.ballroom.table.Table;
import org.jboss.hal.core.CrudOperations;
import org.jboss.hal.core.mbui.MbuiContext;
import org.jboss.hal.core.mbui.MbuiViewImpl;
import org.jboss.hal.core.mbui.dialog.AddResourceDialog;
import org.jboss.hal.core.mbui.dialog.NameItem;
import org.jboss.hal.core.mbui.form.ModelNodeForm;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.NamedNode;
import org.jboss.hal.dmr.ResourceAddress;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.meta.SelectionAwareStatementContext;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.spi.MbuiElement;
import org.jboss.hal.spi.MbuiView;

import static elemental2.dom.DomGlobal.document;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.jboss.hal.client.configuration.subsystem.logging.AddressTemplates.LOGGING_PROFILE_TEMPLATE;
import static org.jboss.hal.client.configuration.subsystem.logging.AddressTemplates.SELECTED_LOGGING_PROFILE_TEMPLATE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.HANDLERS;
import static org.jboss.hal.dmr.ModelDescriptionConstants.LEVEL;
import static org.jboss.hal.resources.CSS.fontAwesome;
import static org.jboss.hal.resources.CSS.marginTopLarge;

/**
 * @author Harald Pehl
 */
@MbuiView
@SuppressWarnings({"DuplicateStringLiteralInspection", "HardCodedStringLiteral", "WeakerAccess"})
public abstract class LoggingProfileView extends MbuiViewImpl<LoggingProfilePresenter>
        implements LoggingProfilePresenter.MyView {

    // ------------------------------------------------------ initialization

    public static LoggingProfileView create(final MbuiContext mbuiContext, final CrudOperations crud) {
        return new Mbui_LoggingProfileView(mbuiContext, crud);
    }

    abstract CrudOperations crud();

    final SelectionAwareStatementContext selectionAwareStatementContext;
    final SuggestHandler suggestHandlers;
    @MbuiElement("logging-profile-vertical-navigation") VerticalNavigation navigation;
    @MbuiElement("logging-profile-root-logger-form") Form<ModelNode> rootLoggerForm;
    @MbuiElement("logging-profile-categories-table") Table<NamedNode> loggerTable;
    @MbuiElement("logging-profile-categories-form") Form<NamedNode> loggerForm;
    @MbuiElement("logging-profile-handler-console-table") Table<NamedNode> consoleHandlerTable;
    @MbuiElement("logging-profile-handler-console-form") Form<NamedNode> consoleHandlerForm;
    @MbuiElement("logging-profile-handler-file-table") Table<NamedNode> fileHandlerTable;
    @MbuiElement("logging-profile-handler-file-form") Form<NamedNode> fileHandlerForm;
    @MbuiElement("logging-profile-handler-periodic-rotating-file-table") Table<NamedNode> periodicHandlerTable;
    @MbuiElement("logging-profile-handler-periodic-rotating-file-form") Form<NamedNode> periodicHandlerForm;
    @MbuiElement("logging-profile-handler-periodic-size-rotating-file-table") Table<NamedNode> periodicSizeHandlerTable;
    @MbuiElement("logging-profile-handler-periodic-size-rotating-file-form") Form<NamedNode> periodicSizeHandlerForm;
    @MbuiElement("logging-profile-handler-size-rotating-file-table") Table<NamedNode> sizeHandlerTable;
    @MbuiElement("logging-profile-handler-size-rotating-file-form") Form<NamedNode> sizeHandlerForm;
    @MbuiElement("logging-profile-handler-async-table") Table<NamedNode> asyncHandlerTable;
    @MbuiElement("logging-profile-handler-async-form") Form<NamedNode> asyncHandlerForm;
    @MbuiElement("logging-profile-handler-custom-table") Table<NamedNode> customHandlerTable;
    @MbuiElement("logging-profile-handler-custom-form") Form<NamedNode> customHandlerForm;
    @MbuiElement("logging-profile-handler-syslog-table") Table<NamedNode> syslogHandlerTable;
    @MbuiElement("logging-profile-handler-syslog-form") Form<NamedNode> syslogHandlerForm;
    @MbuiElement("logging-profile-formatter-custom-table") Table<NamedNode> customFormatterTable;
    @MbuiElement("logging-profile-formatter-custom-form") Form<NamedNode> customFormatterForm;
    @MbuiElement("logging-profile-formatter-pattern-table") Table<NamedNode> patternFormatterTable;
    @MbuiElement("logging-profile-formatter-pattern-form") Form<NamedNode> patternFormatterForm;
    EmptyState noRootLogger;

    LoggingProfileView(final MbuiContext mbuiContext) {
        super(mbuiContext);
        selectionAwareStatementContext = new SelectionAwareStatementContext(mbuiContext.statementContext(),
                () -> presenter.getLoggingProfile());
        List<AddressTemplate> templates = asList(
                SELECTED_LOGGING_PROFILE_TEMPLATE.append("async-handler=*"),
                SELECTED_LOGGING_PROFILE_TEMPLATE.append("console-handler=*"),
                SELECTED_LOGGING_PROFILE_TEMPLATE.append("custom-handler=*"),
                SELECTED_LOGGING_PROFILE_TEMPLATE.append("file-handler=*"),
                SELECTED_LOGGING_PROFILE_TEMPLATE.append("periodic-rotating-file-handler=*"),
                SELECTED_LOGGING_PROFILE_TEMPLATE.append("periodic-size-rotating-file-handler=*"),
                SELECTED_LOGGING_PROFILE_TEMPLATE.append("size-rotating-file-handler=*"),
                SELECTED_LOGGING_PROFILE_TEMPLATE.append("syslog-handler=*"));
        suggestHandlers = new ReadChildrenAutoComplete(mbuiContext.dispatcher(), selectionAwareStatementContext,
                templates);
    }

    @PostConstruct
    void init() {
        noRootLogger = new EmptyState.Builder(mbuiContext.resources().constants().noRootLogger())
                .description(mbuiContext.resources().constants().noRootLoggerDescription())
                .icon(fontAwesome("sitemap"))
                .primaryAction(mbuiContext.resources().constants().add(), this::addRootLogger)
                .build();
        noRootLogger.asElement().classList.add(marginTopLarge);

        // hack which relies on the element hierarchy given in the template. will break if you change that hierarchy.
        rootLoggerForm.asElement().parentNode.appendChild(noRootLogger.asElement());
        rootLoggerVisibility(false);

        // add suggest handler
        rootLoggerForm.getFormItem(HANDLERS).registerSuggestHandler(suggestHandlers);
        loggerForm.getFormItem(HANDLERS).registerSuggestHandler(suggestHandlers);
    }


    // ------------------------------------------------------ root logger

    @SuppressWarnings("UnusedParameters")
    void saveRootLogger(Form<ModelNode> form, Map<String, Object> changedValues) {
        Metadata metadata = mbuiContext.metadataRegistry().lookup(LOGGING_PROFILE_TEMPLATE.append("root-logger=ROOT"));
        saveSingletonForm("Root Logger", SELECTED_LOGGING_PROFILE_TEMPLATE.append("root-logger=ROOT")
                .resolve(selectionAwareStatementContext), changedValues, metadata);
    }

    void resetRootLogger(Form<ModelNode> form) {
        Metadata metadata = mbuiContext.metadataRegistry().lookup(LOGGING_PROFILE_TEMPLATE.append("root-logger=ROOT"));
        resetSingletonForm("Root Logger", SELECTED_LOGGING_PROFILE_TEMPLATE.append("root-logger=ROOT")
                .resolve(selectionAwareStatementContext), form, metadata);
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
        Elements.setVisible((HTMLElement) document.getElementById("logging-profile-root-logger-header"),
                visible);
        Elements.setVisible((HTMLElement) document.getElementById("logging-profile-root-logger-description"),
                visible);
        Elements.setVisible(rootLoggerForm.asElement(), visible);
        Elements.setVisible(noRootLogger.asElement(), !visible);
    }

    private void addRootLogger() {
        Metadata metadata = mbuiContext.metadataRegistry().lookup(LOGGING_PROFILE_TEMPLATE.append("root-logger=ROOT"));

        Form<ModelNode> form = new ModelNodeForm.Builder<>("logging-profile-root-logger-add", metadata)
                .fromRequestProperties()
                .include(LEVEL, HANDLERS)
                .build();
        AddResourceDialog dialog = new AddResourceDialog(
                mbuiContext.resources().messages().addResourceTitle(Names.ROOT_LOGGER), form,
                (name, model) -> {
                    ResourceAddress address = SELECTED_LOGGING_PROFILE_TEMPLATE.append("root-logger=ROOT")
                            .resolve(selectionAwareStatementContext);
                    crud().addSingleton(Names.ROOT_LOGGER, address, model, a -> presenter.reload());
                });

        dialog.getForm().getFormItem(HANDLERS).registerSuggestHandler(suggestHandlers);
        dialog.show();
    }


    // ------------------------------------------------------ logger / categories

    void addLogger() {
        addResource("logger=*", Ids.build("logging-profile-categories-table", "add"), "Category",
                "level", "handlers", "use-parent-handlers");
    }

    void removeLogger(Table<NamedNode> table) {
        removeResource(table, "logger=*", "Category");
    }

    void saveLogger(Form<NamedNode> form, Map<String, Object> changedValues) {
        String name = form.getModel().getName();
        Metadata metadata = mbuiContext.metadataRegistry().lookup(LOGGING_PROFILE_TEMPLATE.append("logger=*"));
        saveForm("Category", name, SELECTED_LOGGING_PROFILE_TEMPLATE.append("logger=*")
                .resolve(selectionAwareStatementContext, name), changedValues, metadata);
    }

    void resetLogger(Form<NamedNode> form) {
        String name = form.getModel().getName();
        Metadata metadata = mbuiContext.metadataRegistry().lookup(LOGGING_PROFILE_TEMPLATE.append("logger=*"));
        resetForm("Category", name, SELECTED_LOGGING_PROFILE_TEMPLATE.append("logger=*")
                .resolve(selectionAwareStatementContext, name), form, metadata);
    }

    @Override
    public void updateLogger(final List<NamedNode> items) {
        loggerForm.clear();
        loggerTable.update(items);
    }


    // ------------------------------------------------------ console handler

    void addConsoleHandler() {
        addResource("console-handler=*", Ids.build("logging-profile-handler-console-table", "add"),
                "Console ActionHandler", "level", "target", "formatter");
    }

    void removeConsoleHandler(Table<NamedNode> table) {
        removeResource(table, "console-handler=*", "Console ActionHandler");
    }

    void saveConsoleHandler(Form<NamedNode> form, Map<String, Object> changedValues) {
        String name = form.getModel().getName();
        Metadata metadata = mbuiContext.metadataRegistry().lookup(LOGGING_PROFILE_TEMPLATE.append("console-handler=*"));
        saveForm("Console ActionHandler", name, SELECTED_LOGGING_PROFILE_TEMPLATE.append("console-handler=*")
                .resolve(selectionAwareStatementContext, name), changedValues, metadata);
    }

    void resetConsoleHandler(Form<NamedNode> form) {
        String name = form.getModel().getName();
        Metadata metadata = mbuiContext.metadataRegistry().lookup(LOGGING_PROFILE_TEMPLATE.append("console-handler=*"));
        resetForm("Console ActionHandler", name, SELECTED_LOGGING_PROFILE_TEMPLATE.append("console-handler=*")
                .resolve(selectionAwareStatementContext, name), form, metadata);
    }

    @Override
    public void updateConsoleHandler(final List<NamedNode> items) {
        navigation.updateBadge("logging-profile-handler-console-item", items.size());
        consoleHandlerForm.clear();
        consoleHandlerTable.update(items);
    }


    // ------------------------------------------------------ file handler

    void addFileHandler() {
        addFileHandlerResource("file-handler=*", Ids.build("logging-profile-handler-file-table", "add"),
                "File ActionHandler", "level", "formatter");
    }

    void removeFileHandler(Table<NamedNode> table) {
        removeResource(table, "file-handler=*", "File ActionHandler");
    }

    void saveFileHandler(Form<NamedNode> form, Map<String, Object> changedValues) {
        String name = form.getModel().getName();
        Metadata metadata = mbuiContext.metadataRegistry().lookup(LOGGING_PROFILE_TEMPLATE.append("file-handler=*"));
        saveForm("File ActionHandler", name, SELECTED_LOGGING_PROFILE_TEMPLATE.append("file-handler=*")
                .resolve(selectionAwareStatementContext, name), changedValues, metadata);
    }

    void resetFileHandler(Form<NamedNode> form) {
        String name = form.getModel().getName();
        Metadata metadata = mbuiContext.metadataRegistry().lookup(LOGGING_PROFILE_TEMPLATE.append("file-handler=*"));
        resetForm("File ActionHandler", name, SELECTED_LOGGING_PROFILE_TEMPLATE.append("file-handler=*")
                .resolve(selectionAwareStatementContext, name), form, metadata);
    }

    @Override
    public void updateFileHandler(final List<NamedNode> items) {
        navigation.updateBadge("logging-profile-handler-file-item", items.size());
        fileHandlerForm.clear();
        fileHandlerTable.update(items);
    }


    // ------------------------------------------------------ periodic handler

    void addPeriodicHandler() {
        addFileHandlerResource("periodic-rotating-file-handler=*",
                Ids.build("logging-profile-handler-periodic-rotating-file-table", "add"),
                "Periodic ActionHandler", "suffix", "level", "formatter");
    }

    void removePeriodicHandler(Table<NamedNode> table) {
        removeResource(table, "periodic-rotating-file-handler=*", "Periodic ActionHandler");
    }

    void savePeriodicHandler(Form<NamedNode> form, Map<String, Object> changedValues) {
        String name = form.getModel().getName();
        Metadata metadata = mbuiContext.metadataRegistry()
                .lookup(LOGGING_PROFILE_TEMPLATE.append("periodic-rotating-file-handler=*"));
        saveForm("Periodic ActionHandler", name,
                SELECTED_LOGGING_PROFILE_TEMPLATE.append("periodic-rotating-file-handler=*")
                        .resolve(selectionAwareStatementContext, name), changedValues, metadata);
    }

    void resetPeriodicHandler(Form<NamedNode> form) {
        String name = form.getModel().getName();
        Metadata metadata = mbuiContext.metadataRegistry()
                .lookup(LOGGING_PROFILE_TEMPLATE.append("periodic-rotating-file-handler=*"));
        resetForm("Periodic ActionHandler", name,
                SELECTED_LOGGING_PROFILE_TEMPLATE.append("periodic-rotating-file-handler=*")
                        .resolve(selectionAwareStatementContext, name), form, metadata);
    }

    @Override
    public void updatePeriodicHandler(final List<NamedNode> items) {
        navigation.updateBadge("logging-profile-handler-periodic-rotating-file-item", items.size());
        periodicHandlerForm.clear();
        periodicHandlerTable.update(items);
    }


    // ------------------------------------------------------ periodic size handler

    void addPeriodicSizeHandler() {
        addFileHandlerResource("periodic-size-rotating-file-handler=*",
                Ids.build("logging-profile-handler-periodic-size-rotating-file-table", "add"),
                "Periodic Size ActionHandler", "suffix", "level", "formatter", "rotate-size", "max-backup-index");
    }

    void removePeriodicSizeHandler(Table<NamedNode> table) {
        removeResource(table, "periodic-size-rotating-file-handler=*", "Periodic Size ActionHandler");
    }

    void savePeriodicSizeHandler(Form<NamedNode> form, Map<String, Object> changedValues) {
        String name = form.getModel().getName();
        Metadata metadata = mbuiContext.metadataRegistry()
                .lookup(LOGGING_PROFILE_TEMPLATE.append("periodic-size-rotating-file-handler=*"));
        saveForm("Periodic Size ActionHandler", name,
                SELECTED_LOGGING_PROFILE_TEMPLATE.append("periodic-size-rotating-file-handler=*")
                        .resolve(selectionAwareStatementContext, name), changedValues, metadata);
    }

    void resetPeriodicSizeHandler(Form<NamedNode> form) {
        String name = form.getModel().getName();
        Metadata metadata = mbuiContext.metadataRegistry()
                .lookup(LOGGING_PROFILE_TEMPLATE.append("periodic-size-rotating-file-handler=*"));
        resetForm("Periodic Size ActionHandler", name,
                SELECTED_LOGGING_PROFILE_TEMPLATE.append("periodic-size-rotating-file-handler=*")
                        .resolve(selectionAwareStatementContext, name), form, metadata);
    }

    @Override
    public void updatePeriodicSizeHandler(final List<NamedNode> items) {
        navigation.updateBadge("logging-profile-handler-periodic-size-rotating-file-item", items.size());
        periodicSizeHandlerForm.clear();
        periodicSizeHandlerTable.update(items);
    }


    // ------------------------------------------------------ size handler

    void addSizeHandler() {
        addFileHandlerResource("size-rotating-file-handler=*",
                Ids.build("logging-profile-handler-size-rotating-file-table", "add"),
                "Size ActionHandler", "suffix", "level", "formatter", "rotate-size", "max-backup-index");
    }

    void removeSizeHandler(Table<NamedNode> table) {
        removeResource(table, "size-rotating-file-handler=*", "Size ActionHandler");
    }

    void saveSizeHandler(Form<NamedNode> form, Map<String, Object> changedValues) {
        String name = form.getModel().getName();
        Metadata metadata = mbuiContext.metadataRegistry()
                .lookup(LOGGING_PROFILE_TEMPLATE.append("size-rotating-file-handler=*"));
        saveForm("Size ActionHandler", name, SELECTED_LOGGING_PROFILE_TEMPLATE.append("size-rotating-file-handler=*")
                .resolve(selectionAwareStatementContext, name), changedValues, metadata);
    }

    void resetSizeHandler(Form<NamedNode> form) {
        String name = form.getModel().getName();
        Metadata metadata = mbuiContext.metadataRegistry()
                .lookup(LOGGING_PROFILE_TEMPLATE.append("size-rotating-file-handler=*"));
        resetForm("Size ActionHandler", name, SELECTED_LOGGING_PROFILE_TEMPLATE.append("size-rotating-file-handler=*")
                .resolve(selectionAwareStatementContext, name), form, metadata);
    }

    @Override
    public void updateSizeHandler(final List<NamedNode> items) {
        navigation.updateBadge("logging-profile-handler-size-rotating-file-item", items.size());
        sizeHandlerForm.clear();
        sizeHandlerTable.update(items);
    }


    // ------------------------------------------------------ async handler

    void addAsyncHandler() {
        AddressTemplate metadataTemplate = LOGGING_PROFILE_TEMPLATE.append("async-handler=*");
        Metadata metadata = mbuiContext.metadataRegistry().lookup(metadataTemplate);
        AddressTemplate selectionTemplate = SELECTED_LOGGING_PROFILE_TEMPLATE.append("async-handler=*");

        AddResourceDialog dialog = new AddResourceDialog(Ids.build("logging-profile-handler-async-table", "add"),
                mbuiContext.resources().messages().addResourceTitle("Async ActionHandler"),
                metadata, asList("level", "subhandlers", "queue-length", "overflow-action"),
                (name, modelNode) -> {
                    ResourceAddress address = selectionTemplate.resolve(selectionAwareStatementContext, name);
                    crud().add("Async ActionHandler", name, address, modelNode, (n, a) -> presenter.reload());
                });
        List<AddressTemplate> templates = asList(
                SELECTED_LOGGING_PROFILE_TEMPLATE.append("console-handler=*"),
                SELECTED_LOGGING_PROFILE_TEMPLATE.append("custom-handler=*"),
                SELECTED_LOGGING_PROFILE_TEMPLATE.append("file-handler=*"),
                SELECTED_LOGGING_PROFILE_TEMPLATE.append("periodic-rotating-file-handler=*"),
                SELECTED_LOGGING_PROFILE_TEMPLATE.append("periodic-size-rotating-file-handler=*"),
                SELECTED_LOGGING_PROFILE_TEMPLATE.append("size-rotating-file-handler=*"),
                SELECTED_LOGGING_PROFILE_TEMPLATE.append("syslog-handler=*"));
        dialog.getForm().getFormItem("subhandlers").registerSuggestHandler(
                new ReadChildrenAutoComplete(mbuiContext.dispatcher(), selectionAwareStatementContext, templates));
        dialog.show();
    }

    void removeAsyncHandler(Table<NamedNode> table) {
        removeResource(table, "async-handler=*", "Async ActionHandler");
    }

    void saveAsyncHandler(Form<NamedNode> form, Map<String, Object> changedValues) {
        String name = form.getModel().getName();
        Metadata metadata = mbuiContext.metadataRegistry().lookup(LOGGING_PROFILE_TEMPLATE.append("async-handler=*"));
        saveForm("Async ActionHandler", name, SELECTED_LOGGING_PROFILE_TEMPLATE.append("async-handler=*")
                .resolve(selectionAwareStatementContext, name), changedValues, metadata);
    }

    void resetAsyncHandler(Form<NamedNode> form) {
        String name = form.getModel().getName();
        Metadata metadata = mbuiContext.metadataRegistry().lookup(LOGGING_PROFILE_TEMPLATE.append("async-handler=*"));
        resetForm("Async ActionHandler", name, SELECTED_LOGGING_PROFILE_TEMPLATE.append("async-handler=*")
                .resolve(selectionAwareStatementContext, name), form, metadata);
    }

    @Override
    public void updateAsyncHandler(final List<NamedNode> items) {
        navigation.updateBadge("logging-profile-handler-async-item", items.size());
        asyncHandlerForm.clear();
        asyncHandlerTable.update(items);
    }


    // ------------------------------------------------------ custom handler

    void addCustomHandler() {
        addResource("custom-handler=*", Ids.build("logging-profile-handler-custom-table", "add"),
                "Custom ActionHandler", "level", "module", "class", "formatter");
    }

    void removeCustomHandler(Table<NamedNode> table) {
        removeResource(table, "custom-handler=*", "Custom ActionHandler");
    }

    void saveCustomHandler(Form<NamedNode> form, Map<String, Object> changedValues) {
        String name = form.getModel().getName();
        Metadata metadata = mbuiContext.metadataRegistry().lookup(LOGGING_PROFILE_TEMPLATE.append("custom-handler=*"));
        saveForm("Custom ActionHandler", name, SELECTED_LOGGING_PROFILE_TEMPLATE.append("custom-handler=*")
                .resolve(selectionAwareStatementContext, name), changedValues, metadata);
    }

    void resetCustomHandler(Form<NamedNode> form) {
        String name = form.getModel().getName();
        Metadata metadata = mbuiContext.metadataRegistry().lookup(LOGGING_PROFILE_TEMPLATE.append("custom-handler=*"));
        resetForm("Custom ActionHandler", name, SELECTED_LOGGING_PROFILE_TEMPLATE.append("custom-handler=*")
                .resolve(selectionAwareStatementContext, name), form, metadata);
    }

    @Override
    public void updateCustomHandler(final List<NamedNode> items) {
        navigation.updateBadge("logging-profile-handler-custom-item", items.size());
        customHandlerForm.clear();
        customHandlerTable.update(items);
    }


    // ------------------------------------------------------ syslog handler

    void addSyslogHandler() {
        addResource("syslog-handler=*", Ids.build("logging-profile-handler-syslog-table", "add"),
                "Syslog ActionHandler", "level", "syslog-format", "hostname", "server-address", "port", "app-name",
                "facility");
    }

    void removeSyslogHandler(Table<NamedNode> table) {
        removeResource(table, "syslog-handler=*", "Syslog ActionHandler");
    }

    void saveSyslogHandler(Form<NamedNode> form, Map<String, Object> changedValues) {
        String name = form.getModel().getName();
        Metadata metadata = mbuiContext.metadataRegistry().lookup(LOGGING_PROFILE_TEMPLATE.append("syslog-handler=*"));
        saveForm("Syslog ActionHandler", name, SELECTED_LOGGING_PROFILE_TEMPLATE.append("syslog-handler=*")
                .resolve(selectionAwareStatementContext, name), changedValues, metadata);
    }

    void resetSyslogHandler(Form<NamedNode> form) {
        String name = form.getModel().getName();
        Metadata metadata = mbuiContext.metadataRegistry().lookup(LOGGING_PROFILE_TEMPLATE.append("syslog-handler=*"));
        resetForm("Syslog ActionHandler", name, SELECTED_LOGGING_PROFILE_TEMPLATE.append("syslog-handler=*")
                .resolve(selectionAwareStatementContext, name), form, metadata);
    }

    @Override
    public void updateSyslogHandler(final List<NamedNode> items) {
        navigation.updateBadge("logging-profile-handler-syslog-item", items.size());
        syslogHandlerForm.clear();
        syslogHandlerTable.update(items);
    }


    // ------------------------------------------------------ custom formatter

    void addCustomFormatter() {
        addResource("custom-formatter=*", Ids.build("logging-profile-formatter-custom-table", "add"),
                "Custom Formatter");
    }

    void removeCustomFormatter(Table<NamedNode> table) {
        removeResource(table, "custom-formatter=*", "Custom Formatter");
    }

    void saveCustomFormatter(Form<NamedNode> form, Map<String, Object> changedValues) {
        String name = form.getModel().getName();
        Metadata metadata = mbuiContext.metadataRegistry()
                .lookup(LOGGING_PROFILE_TEMPLATE.append("custom-formatter=*"));
        saveForm("Custom Formatter", name, SELECTED_LOGGING_PROFILE_TEMPLATE.append("custom-formatter=*")
                .resolve(selectionAwareStatementContext, name), changedValues, metadata);
    }

    void resetCustomFormatter(Form<NamedNode> form) {
        String name = form.getModel().getName();
        Metadata metadata = mbuiContext.metadataRegistry()
                .lookup(LOGGING_PROFILE_TEMPLATE.append("custom-formatter=*"));
        resetForm("Custom Formatter", name, SELECTED_LOGGING_PROFILE_TEMPLATE.append("custom-formatter=*")
                .resolve(selectionAwareStatementContext, name), form, metadata);
    }

    @Override
    public void updateCustomFormatter(final List<NamedNode> items) {
        navigation.updateBadge("logging-profile-formatter-custom-item", items.size());
        customFormatterForm.clear();
        customFormatterTable.update(items);
    }


    // ------------------------------------------------------ pattern formatter

    void addPatternFormatter() {
        addResource("pattern-formatter=*", Ids.build("logging-profile-formatter-pattern-table", "add"),
                "Pattern Formatter");
    }

    void removePatternFormatter(Table<NamedNode> table) {
        removeResource(table, "pattern-formatter=*", "Pattern Formatter");
    }

    void savePatternFormatter(Form<NamedNode> form, Map<String, Object> changedValues) {
        String name = form.getModel().getName();
        Metadata metadata = mbuiContext.metadataRegistry()
                .lookup(LOGGING_PROFILE_TEMPLATE.append("pattern-formatter=*"));
        saveForm("Pattern Formatter", name, SELECTED_LOGGING_PROFILE_TEMPLATE.append("pattern-formatter=*")
                .resolve(selectionAwareStatementContext, name), changedValues, metadata);
    }

    void resetPatternFormatter(Form<NamedNode> form) {
        String name = form.getModel().getName();
        Metadata metadata = mbuiContext.metadataRegistry()
                .lookup(LOGGING_PROFILE_TEMPLATE.append("pattern-formatter=*"));
        resetForm("Pattern Formatter", name, SELECTED_LOGGING_PROFILE_TEMPLATE.append("pattern-formatter=*")
                .resolve(selectionAwareStatementContext, name), form, metadata);
    }

    @Override
    public void updatePatternFormatter(final List<NamedNode> items) {
        navigation.updateBadge("logging-profile-formatter-pattern-item", items.size());
        patternFormatterForm.clear();
        patternFormatterTable.update(items);
    }


    // ------------------------------------------------------ helper methods

    private void addResource(String templateSuffix, String id, String type, String... attributes) {
        AddressTemplate metadataTemplate = LOGGING_PROFILE_TEMPLATE.append(templateSuffix);
        Metadata metadata = mbuiContext.metadataRegistry().lookup(metadataTemplate);
        AddressTemplate selectionTemplate = SELECTED_LOGGING_PROFILE_TEMPLATE.append(templateSuffix);

        AddResourceDialog dialog = new AddResourceDialog(id,
                mbuiContext.resources().messages().addResourceTitle(type),
                metadata, attributes == null ? emptyList() : asList(attributes),
                (name, modelNode) -> {
                    ResourceAddress address = selectionTemplate.resolve(selectionAwareStatementContext, name);
                    crud().add(type, name, address, modelNode, (n, a) -> presenter.reload());
                });
        FormItem<Object> handlers = dialog.getForm().getFormItem("handlers");
        if (handlers != null) {
            handlers.registerSuggestHandler(suggestHandlers);
        }
        dialog.show();
    }

    private void addFileHandlerResource(String templateSuffix, String id, String type, String... attributes) {
        AddressTemplate metadataTemplate = LOGGING_PROFILE_TEMPLATE.append(templateSuffix);
        Metadata metadata = mbuiContext.metadataRegistry().lookup(metadataTemplate);
        AddressTemplate selectionTemplate = SELECTED_LOGGING_PROFILE_TEMPLATE.append(templateSuffix);

        ModelNodeForm.Builder<ModelNode> builder = new ModelNodeForm.Builder<>(id, metadata)
                .fromRequestProperties()
                .unboundFormItem(new NameItem(), 0)
                .customFormItem("file", (attributeDescription) -> new FileFormItem())
                .unsorted();
        if (attributes != null) {
            builder.include(asList(attributes));
        }
        AddResourceDialog dialog = new AddResourceDialog(
                mbuiContext.resources().messages().addResourceTitle(type), builder.build(),
                (name, modelNode) -> {
                    ResourceAddress address = selectionTemplate.resolve(selectionAwareStatementContext, name);
                    crud().add(type, name, address, modelNode, (n, a) -> presenter.reload());
                });
        dialog.show();
    }

    private void removeResource(Table<NamedNode> table, String templateSuffix, String type) {
        //noinspection ConstantConditions
        String name = table.selectedRow().getName();
        AddressTemplate selectionTemplate = SELECTED_LOGGING_PROFILE_TEMPLATE.append(templateSuffix);
        ResourceAddress address = selectionTemplate.resolve(selectionAwareStatementContext, name);
        crud().remove(type, name, address, () -> presenter.reload());
    }
}
