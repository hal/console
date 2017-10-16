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
import static org.jboss.hal.dmr.ModelDescriptionConstants.*;
import static org.jboss.hal.resources.CSS.fontAwesome;
import static org.jboss.hal.resources.CSS.marginTopLarge;
import static org.jboss.hal.resources.Names.CATEGORY;

@MbuiView
@SuppressWarnings({"DuplicateStringLiteralInspection", "HardCodedStringLiteral", "WeakerAccess"})
public abstract class LoggingProfileView extends MbuiViewImpl<LoggingProfilePresenter>
        implements LoggingProfilePresenter.MyView {

    private static final String EQ_WILDCARD = "=*";
    private static final String ROOT_LOGGER_EQ_ROOT = "root-logger=ROOT";

    // ------------------------------------------------------ initialization

    public static LoggingProfileView create(final MbuiContext mbuiContext, final CrudOperations crud) {
        return new Mbui_LoggingProfileView(mbuiContext, crud);
    }

    abstract CrudOperations crud();

    @MbuiElement(Ids.LOGGING_PROFILE + "-vertical-navigation") VerticalNavigation navigation;
    @MbuiElement(Ids.LOGGING_PROFILE + "-root-logger-form") Form<ModelNode> rootLoggerForm;
    @MbuiElement(Ids.LOGGING_PROFILE + "-category-table") Table<NamedNode> loggerTable;
    @MbuiElement(Ids.LOGGING_PROFILE + "-category-form") Form<NamedNode> loggerForm;
    @MbuiElement(Ids.LOGGING_PROFILE + "-handler-console-table") Table<NamedNode> consoleHandlerTable;
    @MbuiElement(Ids.LOGGING_PROFILE + "-handler-console-form") Form<NamedNode> consoleHandlerForm;
    @MbuiElement(Ids.LOGGING_PROFILE + "-handler-file-table") Table<NamedNode> fileHandlerTable;
    @MbuiElement(Ids.LOGGING_PROFILE + "-handler-file-form") Form<NamedNode> fileHandlerForm;
    @MbuiElement(Ids.LOGGING_PROFILE + "-handler-periodic-rotating-file-table") Table<NamedNode> periodicHandlerTable;
    @MbuiElement(Ids.LOGGING_PROFILE + "-handler-periodic-rotating-file-form") Form<NamedNode> periodicHandlerForm;
    @MbuiElement(Ids.LOGGING_PROFILE + "-handler-periodic-size-rotating-file-table") Table<NamedNode> periodicSizeHandlerTable;
    @MbuiElement(Ids.LOGGING_PROFILE + "-handler-periodic-size-rotating-file-form") Form<NamedNode> periodicSizeHandlerForm;
    @MbuiElement(Ids.LOGGING_PROFILE + "-handler-size-rotating-file-table") Table<NamedNode> sizeHandlerTable;
    @MbuiElement(Ids.LOGGING_PROFILE + "-handler-size-rotating-file-form") Form<NamedNode> sizeHandlerForm;
    @MbuiElement(Ids.LOGGING_PROFILE + "-handler-async-table") Table<NamedNode> asyncHandlerTable;
    @MbuiElement(Ids.LOGGING_PROFILE + "-handler-async-form") Form<NamedNode> asyncHandlerForm;
    @MbuiElement(Ids.LOGGING_PROFILE + "-handler-custom-table") Table<NamedNode> customHandlerTable;
    @MbuiElement(Ids.LOGGING_PROFILE + "-handler-custom-form") Form<NamedNode> customHandlerForm;
    @MbuiElement(Ids.LOGGING_PROFILE + "-handler-syslog-table") Table<NamedNode> syslogHandlerTable;
    @MbuiElement(Ids.LOGGING_PROFILE + "-handler-syslog-form") Form<NamedNode> syslogHandlerForm;
    @MbuiElement(Ids.LOGGING_PROFILE + "-formatter-custom-table") Table<NamedNode> customFormatterTable;
    @MbuiElement(Ids.LOGGING_PROFILE + "-formatter-custom-form") Form<NamedNode> customFormatterForm;
    @MbuiElement(Ids.LOGGING_PROFILE + "-formatter-pattern-table") Table<NamedNode> patternFormatterTable;
    @MbuiElement(Ids.LOGGING_PROFILE + "-formatter-pattern-form") Form<NamedNode> patternFormatterForm;

    final SelectionAwareStatementContext selectionAwareStatementContext;
    final SuggestHandler suggestHandlers;
    EmptyState noRootLogger;

    LoggingProfileView(MbuiContext mbuiContext) {
        super(mbuiContext);
        selectionAwareStatementContext = new SelectionAwareStatementContext(mbuiContext.statementContext(),
                () -> presenter.getLoggingProfile());
        List<AddressTemplate> templates = asList(
                SELECTED_LOGGING_PROFILE_TEMPLATE.append(ASYNC_HANDLER + EQ_WILDCARD),
                SELECTED_LOGGING_PROFILE_TEMPLATE.append(CONSOLE_HANDLER + EQ_WILDCARD),
                SELECTED_LOGGING_PROFILE_TEMPLATE.append(CUSTOM_HANDLER + EQ_WILDCARD),
                SELECTED_LOGGING_PROFILE_TEMPLATE.append(FILE_HANDLER + EQ_WILDCARD),
                SELECTED_LOGGING_PROFILE_TEMPLATE.append(PERIODIC_ROTATING_FILE_HANDLER + EQ_WILDCARD),
                SELECTED_LOGGING_PROFILE_TEMPLATE.append(PERIODIC_SIZE_ROTATING_FILE_HANDLER + EQ_WILDCARD),
                SELECTED_LOGGING_PROFILE_TEMPLATE.append(SIZE_ROTATING_FILE_HANDLER + EQ_WILDCARD),
                SELECTED_LOGGING_PROFILE_TEMPLATE.append(SYSLOG_HANDLER + EQ_WILDCARD));
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
        Metadata metadata = mbuiContext.metadataRegistry().lookup(LOGGING_PROFILE_TEMPLATE.append(ROOT_LOGGER_EQ_ROOT));
        saveSingletonForm("Root Logger", SELECTED_LOGGING_PROFILE_TEMPLATE.append(ROOT_LOGGER_EQ_ROOT)
                .resolve(selectionAwareStatementContext), changedValues, metadata);
    }

    void resetRootLogger(Form<ModelNode> form) {
        Metadata metadata = mbuiContext.metadataRegistry().lookup(LOGGING_PROFILE_TEMPLATE.append(ROOT_LOGGER_EQ_ROOT));
        resetSingletonForm("Root Logger", SELECTED_LOGGING_PROFILE_TEMPLATE
                .append(ROOT_LOGGER_EQ_ROOT)
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
        Elements.setVisible((HTMLElement) document.getElementById(Ids.LOGGING_PROFILE + "-root-logger-header"),
                visible);
        Elements.setVisible((HTMLElement) document.getElementById(Ids.LOGGING_PROFILE + "-root-logger-description"),
                visible);
        Elements.setVisible(rootLoggerForm.asElement(), visible);
        Elements.setVisible(noRootLogger.asElement(), !visible);
    }

    private void addRootLogger() {
        Metadata metadata = mbuiContext.metadataRegistry().lookup(LOGGING_PROFILE_TEMPLATE.append(ROOT_LOGGER_EQ_ROOT));

        Form<ModelNode> form = new ModelNodeForm.Builder<>(Ids.LOGGING_PROFILE + "-root-logger-add", metadata)
                .fromRequestProperties()
                .include(LEVEL, HANDLERS)
                .build();
        AddResourceDialog dialog = new AddResourceDialog(
                mbuiContext.resources().messages().addResourceTitle(Names.ROOT_LOGGER), form,
                (name, model) -> {
                    ResourceAddress address = SELECTED_LOGGING_PROFILE_TEMPLATE.append(ROOT_LOGGER_EQ_ROOT)
                            .resolve(selectionAwareStatementContext);
                    crud().addSingleton(Names.ROOT_LOGGER, address, model, a -> presenter.reload());
                });

        dialog.getForm().getFormItem(HANDLERS).registerSuggestHandler(suggestHandlers);
        dialog.show();
    }


    // ------------------------------------------------------ logger / categories

    void addLogger() {
        addResource(LOGGER + EQ_WILDCARD,
                Ids.build(Ids.LOGGING_PROFILE, "category", Ids.TABLE, Ids.ADD),
                "Category", LEVEL, HANDLERS, "use-parent-handlers");
    }

    void removeLogger(Table<NamedNode> table) {
        removeResource(table, LOGGER + EQ_WILDCARD, CATEGORY);
    }

    void saveLogger(Form<NamedNode> form, Map<String, Object> changedValues) {
        String name = form.getModel().getName();
        Metadata metadata = mbuiContext.metadataRegistry()
                .lookup(LOGGING_PROFILE_TEMPLATE.append(LOGGER + EQ_WILDCARD));
        saveForm(CATEGORY, name, SELECTED_LOGGING_PROFILE_TEMPLATE
                .append(LOGGER + EQ_WILDCARD)
                .resolve(selectionAwareStatementContext, name), changedValues, metadata);
    }

    void resetLogger(Form<NamedNode> form) {
        String name = form.getModel().getName();
        Metadata metadata = mbuiContext.metadataRegistry()
                .lookup(LOGGING_PROFILE_TEMPLATE.append(LOGGER + EQ_WILDCARD));
        resetForm(CATEGORY, name, SELECTED_LOGGING_PROFILE_TEMPLATE
                .append(LOGGER + EQ_WILDCARD)
                .resolve(selectionAwareStatementContext, name), form, metadata);
    }

    @Override
    public void updateLogger(List<NamedNode> items) {
        loggerForm.clear();
        loggerTable.update(items);
    }


    // ------------------------------------------------------ console handler

    void addConsoleHandler() {
        addResource(CONSOLE_HANDLER + EQ_WILDCARD,
                Ids.build(Ids.LOGGING_PROFILE, "handler-console", Ids.TABLE, Ids.ADD),
                Names.CONSOLE_ACTION_HANDLER, LEVEL, TARGET, FORMATTER);
    }

    void removeConsoleHandler(Table<NamedNode> table) {
        removeResource(table, CONSOLE_HANDLER + EQ_WILDCARD, Names.CONSOLE_ACTION_HANDLER);
    }

    void saveConsoleHandler(Form<NamedNode> form, Map<String, Object> changedValues) {
        String name = form.getModel().getName();
        Metadata metadata = mbuiContext.metadataRegistry()
                .lookup(LOGGING_PROFILE_TEMPLATE.append(CONSOLE_HANDLER + EQ_WILDCARD));
        saveForm(Names.CONSOLE_ACTION_HANDLER, name, SELECTED_LOGGING_PROFILE_TEMPLATE
                .append(CONSOLE_HANDLER + EQ_WILDCARD)
                .resolve(selectionAwareStatementContext, name), changedValues, metadata);
    }

    void resetConsoleHandler(Form<NamedNode> form) {
        String name = form.getModel().getName();
        Metadata metadata = mbuiContext.metadataRegistry()
                .lookup(LOGGING_PROFILE_TEMPLATE.append(CONSOLE_HANDLER + EQ_WILDCARD));
        resetForm(Names.CONSOLE_ACTION_HANDLER, name, SELECTED_LOGGING_PROFILE_TEMPLATE
                .append(CONSOLE_HANDLER + EQ_WILDCARD)
                .resolve(selectionAwareStatementContext, name), form, metadata);
    }

    @Override
    public void updateConsoleHandler(List<NamedNode> items) {
        navigation.updateBadge(Ids.build(Ids.LOGGING_PROFILE, "handler-console", Ids.ITEM), items.size());
        consoleHandlerForm.clear();
        consoleHandlerTable.update(items);
    }


    // ------------------------------------------------------ file handler

    void addFileHandler() {
        addFileHandlerResource(FILE_HANDLER + EQ_WILDCARD,
                Ids.build(Ids.LOGGING_PROFILE, "handler-file", Ids.TABLE, Ids.ADD),
                Names.FILE_ACTION_HANDLER, LEVEL, FORMATTER);
    }

    void removeFileHandler(Table<NamedNode> table) {
        removeResource(table, FILE_HANDLER + EQ_WILDCARD, Names.FILE_ACTION_HANDLER);
    }

    void saveFileHandler(Form<NamedNode> form, Map<String, Object> changedValues) {
        String name = form.getModel().getName();
        Metadata metadata = mbuiContext.metadataRegistry()
                .lookup(LOGGING_PROFILE_TEMPLATE.append(FILE_HANDLER + EQ_WILDCARD));
        saveForm(Names.FILE_ACTION_HANDLER, name, SELECTED_LOGGING_PROFILE_TEMPLATE
                .append(FILE_HANDLER + EQ_WILDCARD)
                .resolve(selectionAwareStatementContext, name), changedValues, metadata);
    }

    void resetFileHandler(Form<NamedNode> form) {
        String name = form.getModel().getName();
        Metadata metadata = mbuiContext.metadataRegistry()
                .lookup(LOGGING_PROFILE_TEMPLATE.append(FILE_HANDLER + EQ_WILDCARD));
        resetForm(Names.FILE_ACTION_HANDLER, name, SELECTED_LOGGING_PROFILE_TEMPLATE
                .append(FILE_HANDLER + EQ_WILDCARD)
                .resolve(selectionAwareStatementContext, name), form, metadata);
    }

    @Override
    public void updateFileHandler(final List<NamedNode> items) {
        navigation.updateBadge(Ids.build(Ids.LOGGING_PROFILE, "handler-file", Ids.ITEM), items.size());
        fileHandlerForm.clear();
        fileHandlerTable.update(items);
    }


    // ------------------------------------------------------ periodic handler

    void addPeriodicHandler() {
        addFileHandlerResource(PERIODIC_ROTATING_FILE_HANDLER + EQ_WILDCARD,
                Ids.build(Ids.LOGGING_PROFILE, "handler-periodic-rotating-file", Ids.TABLE, Ids.ADD),
                Names.PERIODIC_ACTION_HANDLER, "suffix", LEVEL, FORMATTER);
    }

    void removePeriodicHandler(Table<NamedNode> table) {
        removeResource(table, PERIODIC_ROTATING_FILE_HANDLER + EQ_WILDCARD,
                Names.PERIODIC_ACTION_HANDLER);
    }

    void savePeriodicHandler(Form<NamedNode> form, Map<String, Object> changedValues) {
        String name = form.getModel().getName();
        Metadata metadata = mbuiContext.metadataRegistry()
                .lookup(LOGGING_PROFILE_TEMPLATE.append(PERIODIC_ROTATING_FILE_HANDLER + EQ_WILDCARD));
        saveForm(Names.PERIODIC_ACTION_HANDLER, name, SELECTED_LOGGING_PROFILE_TEMPLATE
                .append(PERIODIC_ROTATING_FILE_HANDLER + EQ_WILDCARD)
                .resolve(selectionAwareStatementContext, name), changedValues, metadata);
    }

    void resetPeriodicHandler(Form<NamedNode> form) {
        String name = form.getModel().getName();
        Metadata metadata = mbuiContext.metadataRegistry().lookup(LOGGING_PROFILE_TEMPLATE
                .append(PERIODIC_ROTATING_FILE_HANDLER + EQ_WILDCARD));
        resetForm(Names.PERIODIC_ACTION_HANDLER, name, SELECTED_LOGGING_PROFILE_TEMPLATE
                .append(PERIODIC_ROTATING_FILE_HANDLER + EQ_WILDCARD)
                .resolve(selectionAwareStatementContext, name), form, metadata);
    }

    @Override
    public void updatePeriodicHandler(final List<NamedNode> items) {
        navigation.updateBadge(Ids.build(Ids.LOGGING_PROFILE, "handler-periodic-rotating-file", Ids.ITEM),
                items.size());
        periodicHandlerForm.clear();
        periodicHandlerTable.update(items);
    }


    // ------------------------------------------------------ periodic size handler

    void addPeriodicSizeHandler() {
        addFileHandlerResource(PERIODIC_SIZE_ROTATING_FILE_HANDLER + EQ_WILDCARD,
                Ids.build(Ids.LOGGING_PROFILE, "handler-periodic-size-rotating-file", Ids.TABLE, Ids.ADD),
                Names.PERIODIC_SIZE_ACTION_HANDLER, "suffix", LEVEL, FORMATTER, "rotate-size", "max-backup-index");
    }

    void removePeriodicSizeHandler(Table<NamedNode> table) {
        removeResource(table, PERIODIC_SIZE_ROTATING_FILE_HANDLER + EQ_WILDCARD,
                Names.PERIODIC_SIZE_ACTION_HANDLER);
    }

    void savePeriodicSizeHandler(Form<NamedNode> form, Map<String, Object> changedValues) {
        String name = form.getModel().getName();
        Metadata metadata = mbuiContext.metadataRegistry().lookup(LOGGING_PROFILE_TEMPLATE
                .append(PERIODIC_SIZE_ROTATING_FILE_HANDLER + EQ_WILDCARD));
        saveForm(Names.PERIODIC_SIZE_ACTION_HANDLER, name, SELECTED_LOGGING_PROFILE_TEMPLATE
                .append(PERIODIC_SIZE_ROTATING_FILE_HANDLER + EQ_WILDCARD)
                .resolve(selectionAwareStatementContext, name), changedValues, metadata);
    }

    void resetPeriodicSizeHandler(Form<NamedNode> form) {
        String name = form.getModel().getName();
        Metadata metadata = mbuiContext.metadataRegistry().lookup(LOGGING_PROFILE_TEMPLATE
                .append(PERIODIC_SIZE_ROTATING_FILE_HANDLER + EQ_WILDCARD));
        resetForm(Names.PERIODIC_SIZE_ACTION_HANDLER, name, SELECTED_LOGGING_PROFILE_TEMPLATE
                .append(PERIODIC_SIZE_ROTATING_FILE_HANDLER + EQ_WILDCARD)
                .resolve(selectionAwareStatementContext, name), form, metadata);
    }

    @Override
    public void updatePeriodicSizeHandler(final List<NamedNode> items) {
        navigation.updateBadge(Ids.build(Ids.LOGGING_PROFILE, "handler-periodic-size-rotating-file", Ids.ITEM),
                items.size());
        periodicSizeHandlerForm.clear();
        periodicSizeHandlerTable.update(items);
    }


    // ------------------------------------------------------ size handler

    void addSizeHandler() {
        addFileHandlerResource(SIZE_ROTATING_FILE_HANDLER + EQ_WILDCARD,
                Ids.build(Ids.LOGGING_PROFILE, "handler-file-size-rotating-file", Ids.TABLE, Ids.ADD),
                Names.SIZE_ACTION_HANDLER, "suffix", LEVEL, FORMATTER, "rotate-size", "max-backup-index");
    }

    void removeSizeHandler(Table<NamedNode> table) {
        removeResource(table, SIZE_ROTATING_FILE_HANDLER + EQ_WILDCARD, Names.SIZE_ACTION_HANDLER);
    }

    void saveSizeHandler(Form<NamedNode> form, Map<String, Object> changedValues) {
        String name = form.getModel().getName();
        Metadata metadata = mbuiContext.metadataRegistry()
                .lookup(LOGGING_PROFILE_TEMPLATE.append(SIZE_ROTATING_FILE_HANDLER + EQ_WILDCARD));
        saveForm(Names.SIZE_ACTION_HANDLER, name, SELECTED_LOGGING_PROFILE_TEMPLATE
                .append(SIZE_ROTATING_FILE_HANDLER + EQ_WILDCARD)
                .resolve(selectionAwareStatementContext, name), changedValues, metadata);
    }

    void resetSizeHandler(Form<NamedNode> form) {
        String name = form.getModel().getName();
        Metadata metadata = mbuiContext.metadataRegistry()
                .lookup(LOGGING_PROFILE_TEMPLATE.append(SIZE_ROTATING_FILE_HANDLER + EQ_WILDCARD));
        resetForm(Names.SIZE_ACTION_HANDLER, name, SELECTED_LOGGING_PROFILE_TEMPLATE
                .append(SIZE_ROTATING_FILE_HANDLER + EQ_WILDCARD)
                .resolve(selectionAwareStatementContext, name), form, metadata);
    }

    @Override
    public void updateSizeHandler(final List<NamedNode> items) {
        navigation.updateBadge(Ids.build(Ids.LOGGING_PROFILE, "handler-file-size-rotating-file", Ids.ITEM),
                items.size());
        sizeHandlerForm.clear();
        sizeHandlerTable.update(items);
    }


    // ------------------------------------------------------ async handler

    void addAsyncHandler() {
        AddressTemplate metadataTemplate = LOGGING_PROFILE_TEMPLATE.append(ASYNC_HANDLER + EQ_WILDCARD);
        Metadata metadata = mbuiContext.metadataRegistry().lookup(metadataTemplate);
        AddressTemplate selectionTemplate = SELECTED_LOGGING_PROFILE_TEMPLATE.append(ASYNC_HANDLER + EQ_WILDCARD);

        AddResourceDialog dialog = new AddResourceDialog(
                Ids.build(Ids.LOGGING_PROFILE, "handler-async", Ids.TABLE, Ids.ADD),
                mbuiContext.resources().messages().addResourceTitle(Names.ASYNC_ACTION_HANDLER),
                metadata, asList(LEVEL, "subhandlers", "queue-length", "overflow-action"),
                (name, modelNode) -> {
                    ResourceAddress address = selectionTemplate.resolve(selectionAwareStatementContext, name);
                    crud().add(Names.ASYNC_ACTION_HANDLER, name, address, modelNode, (n, a) -> presenter.reload());
                });
        List<AddressTemplate> templates = asList(
                SELECTED_LOGGING_PROFILE_TEMPLATE.append(CONSOLE_HANDLER + EQ_WILDCARD),
                SELECTED_LOGGING_PROFILE_TEMPLATE.append(CUSTOM_HANDLER + EQ_WILDCARD),
                SELECTED_LOGGING_PROFILE_TEMPLATE.append(FILE_HANDLER + EQ_WILDCARD),
                SELECTED_LOGGING_PROFILE_TEMPLATE.append(PERIODIC_ROTATING_FILE_HANDLER + EQ_WILDCARD),
                SELECTED_LOGGING_PROFILE_TEMPLATE.append(PERIODIC_SIZE_ROTATING_FILE_HANDLER + EQ_WILDCARD),
                SELECTED_LOGGING_PROFILE_TEMPLATE.append(SIZE_ROTATING_FILE_HANDLER + EQ_WILDCARD),
                SELECTED_LOGGING_PROFILE_TEMPLATE.append(SYSLOG_HANDLER + EQ_WILDCARD));
        dialog.getForm().getFormItem("subhandlers").registerSuggestHandler(
                new ReadChildrenAutoComplete(mbuiContext.dispatcher(), selectionAwareStatementContext, templates));
        dialog.show();
    }

    void removeAsyncHandler(Table<NamedNode> table) {
        removeResource(table, ASYNC_HANDLER + EQ_WILDCARD, Names.ASYNC_ACTION_HANDLER);
    }

    void saveAsyncHandler(Form<NamedNode> form, Map<String, Object> changedValues) {
        String name = form.getModel().getName();
        Metadata metadata = mbuiContext.metadataRegistry().lookup(LOGGING_PROFILE_TEMPLATE
                .append(ASYNC_HANDLER + EQ_WILDCARD));
        saveForm(Names.ASYNC_ACTION_HANDLER, name, SELECTED_LOGGING_PROFILE_TEMPLATE
                .append(ASYNC_HANDLER + EQ_WILDCARD)
                .resolve(selectionAwareStatementContext, name), changedValues, metadata);
    }

    void resetAsyncHandler(Form<NamedNode> form) {
        String name = form.getModel().getName();
        Metadata metadata = mbuiContext.metadataRegistry().lookup(LOGGING_PROFILE_TEMPLATE
                .append(ASYNC_HANDLER + EQ_WILDCARD));
        resetForm(Names.ASYNC_ACTION_HANDLER, name, SELECTED_LOGGING_PROFILE_TEMPLATE
                .append(ASYNC_HANDLER + EQ_WILDCARD)
                .resolve(selectionAwareStatementContext, name), form, metadata);
    }

    @Override
    public void updateAsyncHandler(final List<NamedNode> items) {
        navigation.updateBadge(Ids.build(Ids.LOGGING_PROFILE, "handler-async", Ids.ITEM), items.size());
        asyncHandlerForm.clear();
        asyncHandlerTable.update(items);
    }


    // ------------------------------------------------------ custom handler

    void addCustomHandler() {
        addResource(CUSTOM_HANDLER + EQ_WILDCARD,
                Ids.build(Ids.LOGGING_PROFILE, "handler-custom", Ids.TABLE, Ids.ADD),
                Names.CUSTOM_ACTION_HANDLER, LEVEL, MODULE, CLASS, FORMATTER);
    }

    void removeCustomHandler(Table<NamedNode> table) {
        removeResource(table, CUSTOM_HANDLER + EQ_WILDCARD, Names.CUSTOM_ACTION_HANDLER);
    }

    void saveCustomHandler(Form<NamedNode> form, Map<String, Object> changedValues) {
        String name = form.getModel().getName();
        Metadata metadata = mbuiContext.metadataRegistry().lookup(LOGGING_PROFILE_TEMPLATE
                .append(CUSTOM_HANDLER + EQ_WILDCARD));
        saveForm(Names.CUSTOM_ACTION_HANDLER, name, SELECTED_LOGGING_PROFILE_TEMPLATE
                .append(CUSTOM_HANDLER + EQ_WILDCARD)
                .resolve(selectionAwareStatementContext, name), changedValues, metadata);
    }

    void resetCustomHandler(Form<NamedNode> form) {
        String name = form.getModel().getName();
        Metadata metadata = mbuiContext.metadataRegistry().lookup(LOGGING_PROFILE_TEMPLATE
                .append(CUSTOM_HANDLER + EQ_WILDCARD));
        resetForm(Names.CUSTOM_ACTION_HANDLER, name, SELECTED_LOGGING_PROFILE_TEMPLATE
                .append(CUSTOM_HANDLER + EQ_WILDCARD)
                .resolve(selectionAwareStatementContext, name), form, metadata);
    }

    @Override
    public void updateCustomHandler(final List<NamedNode> items) {
        navigation.updateBadge(Ids.build(Ids.LOGGING_PROFILE, "handler-custom", Ids.ITEM), items.size());
        customHandlerForm.clear();
        customHandlerTable.update(items);
    }


    // ------------------------------------------------------ syslog handler

    void addSyslogHandler() {
        addResource(SYSLOG_HANDLER + EQ_WILDCARD,
                Ids.build(Ids.LOGGING_PROFILE, "handler-syslog", Ids.TABLE, Ids.ADD),
                Names.SYSLOG_ACTION_HANDLER, LEVEL, "syslog-format", "hostname", "server-address", PORT, "app-name",
                "facility");
    }

    void removeSyslogHandler(Table<NamedNode> table) {
        removeResource(table, SYSLOG_HANDLER + EQ_WILDCARD, Names.SYSLOG_ACTION_HANDLER);
    }

    void saveSyslogHandler(Form<NamedNode> form, Map<String, Object> changedValues) {
        String name = form.getModel().getName();
        Metadata metadata = mbuiContext.metadataRegistry()
                .lookup(LOGGING_PROFILE_TEMPLATE.append(SYSLOG_HANDLER + EQ_WILDCARD));
        saveForm(Names.SYSLOG_ACTION_HANDLER, name, SELECTED_LOGGING_PROFILE_TEMPLATE
                .append(SYSLOG_HANDLER + EQ_WILDCARD)
                .resolve(selectionAwareStatementContext, name), changedValues, metadata);
    }

    void resetSyslogHandler(Form<NamedNode> form) {
        String name = form.getModel().getName();
        Metadata metadata = mbuiContext.metadataRegistry()
                .lookup(LOGGING_PROFILE_TEMPLATE.append(SYSLOG_HANDLER + EQ_WILDCARD));
        resetForm(Names.SYSLOG_ACTION_HANDLER, name, SELECTED_LOGGING_PROFILE_TEMPLATE
                .append(SYSLOG_HANDLER + EQ_WILDCARD)
                .resolve(selectionAwareStatementContext, name), form, metadata);
    }

    @Override
    public void updateSyslogHandler(final List<NamedNode> items) {
        navigation.updateBadge(Ids.build(Ids.LOGGING_PROFILE, "handler-syslog", Ids.ITEM), items.size());
        syslogHandlerForm.clear();
        syslogHandlerTable.update(items);
    }


    // ------------------------------------------------------ custom formatter

    void addCustomFormatter() {
        addResource(CUSTOM_FORMATTER + EQ_WILDCARD,
                Ids.build(Ids.LOGGING_PROFILE, "formatter-custom", Ids.TABLE, Ids.ADD),
                Names.CUSTOM_FORMATTER);
    }

    void removeCustomFormatter(Table<NamedNode> table) {
        removeResource(table, CUSTOM_FORMATTER + EQ_WILDCARD, Names.CUSTOM_FORMATTER);
    }

    void saveCustomFormatter(Form<NamedNode> form, Map<String, Object> changedValues) {
        String name = form.getModel().getName();
        Metadata metadata = mbuiContext.metadataRegistry().lookup(LOGGING_PROFILE_TEMPLATE
                .append(CUSTOM_FORMATTER + EQ_WILDCARD));
        saveForm(Names.CUSTOM_FORMATTER, name, SELECTED_LOGGING_PROFILE_TEMPLATE
                .append(CUSTOM_FORMATTER + EQ_WILDCARD)
                .resolve(selectionAwareStatementContext, name), changedValues, metadata);
    }

    void resetCustomFormatter(Form<NamedNode> form) {
        String name = form.getModel().getName();
        Metadata metadata = mbuiContext.metadataRegistry().lookup(LOGGING_PROFILE_TEMPLATE
                .append(CUSTOM_FORMATTER + EQ_WILDCARD));
        resetForm(Names.CUSTOM_FORMATTER, name, SELECTED_LOGGING_PROFILE_TEMPLATE
                .append(CUSTOM_FORMATTER + EQ_WILDCARD)
                .resolve(selectionAwareStatementContext, name), form, metadata);
    }

    @Override
    public void updateCustomFormatter(final List<NamedNode> items) {
        navigation.updateBadge(Ids.build(Ids.LOGGING_PROFILE, "formatter-custom", Ids.ITEM), items.size());
        customFormatterForm.clear();
        customFormatterTable.update(items);
    }


    // ------------------------------------------------------ pattern formatter

    void addPatternFormatter() {
        addResource(PATTERN_FORMATTER + EQ_WILDCARD,
                Ids.build(Ids.LOGGING_PROFILE, "formatter-pattern", Ids.TABLE, Ids.ADD),
                Names.PATTERN_FORMATTER);
    }

    void removePatternFormatter(Table<NamedNode> table) {
        removeResource(table, PATTERN_FORMATTER + EQ_WILDCARD, Names.PATTERN_FORMATTER);
    }

    void savePatternFormatter(Form<NamedNode> form, Map<String, Object> changedValues) {
        String name = form.getModel().getName();
        Metadata metadata = mbuiContext.metadataRegistry().lookup(LOGGING_PROFILE_TEMPLATE
                .append(PATTERN_FORMATTER + EQ_WILDCARD));
        saveForm(Names.PATTERN_FORMATTER, name, SELECTED_LOGGING_PROFILE_TEMPLATE
                .append(PATTERN_FORMATTER + EQ_WILDCARD)
                .resolve(selectionAwareStatementContext, name), changedValues, metadata);
    }

    void resetPatternFormatter(Form<NamedNode> form) {
        String name = form.getModel().getName();
        Metadata metadata = mbuiContext.metadataRegistry().lookup(LOGGING_PROFILE_TEMPLATE
                .append(PATTERN_FORMATTER + EQ_WILDCARD));
        resetForm(Names.PATTERN_FORMATTER, name, SELECTED_LOGGING_PROFILE_TEMPLATE
                .append(PATTERN_FORMATTER + EQ_WILDCARD)
                .resolve(selectionAwareStatementContext, name), form, metadata);
    }

    @Override
    public void updatePatternFormatter(final List<NamedNode> items) {
        navigation.updateBadge(Ids.build(Ids.LOGGING_PROFILE, "formatter-pattern", Ids.ITEM), items.size());
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
