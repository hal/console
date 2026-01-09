/*
 *  Copyright 2022 Red Hat
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.jboss.hal.client.configuration.subsystem.logging;

import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.jboss.elemento.Elements;
import org.jboss.hal.ballroom.EmptyState;
import org.jboss.hal.ballroom.LabelBuilder;
import org.jboss.hal.ballroom.Tabs;
import org.jboss.hal.ballroom.VerticalNavigation;
import org.jboss.hal.ballroom.autocomplete.ReadChildrenAutoComplete;
import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.ballroom.form.FormItem;
import org.jboss.hal.ballroom.form.SuggestHandler;
import org.jboss.hal.ballroom.table.Table;
import org.jboss.hal.core.CrudOperations;
import org.jboss.hal.core.configuration.PathsAutoComplete;
import org.jboss.hal.core.mbui.MbuiContext;
import org.jboss.hal.core.mbui.MbuiViewImpl;
import org.jboss.hal.core.mbui.dialog.AddResourceDialog;
import org.jboss.hal.core.mbui.dialog.NameItem;
import org.jboss.hal.core.mbui.form.ModelNodeForm;
import org.jboss.hal.core.mbui.table.ModelNodeTable;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.NamedNode;
import org.jboss.hal.dmr.Operation;
import org.jboss.hal.dmr.ResourceAddress;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.meta.SelectionAwareStatementContext;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.meta.security.Constraint;
import org.jboss.hal.resources.Constants;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.spi.MbuiElement;
import org.jboss.hal.spi.MbuiView;

import elemental2.dom.HTMLElement;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;

import static elemental2.dom.DomGlobal.document;
import static org.jboss.elemento.Elements.h;
import static org.jboss.elemento.Elements.p;
import static org.jboss.elemento.Elements.section;
import static org.jboss.hal.client.configuration.subsystem.logging.AddressTemplates.JSON_FORMATTER_TEMPLATE;
import static org.jboss.hal.client.configuration.subsystem.logging.AddressTemplates.LOGGING_PROFILE_TEMPLATE;
import static org.jboss.hal.client.configuration.subsystem.logging.AddressTemplates.SELECTED_LOGGING_PROFILE_TEMPLATE;
import static org.jboss.hal.client.configuration.subsystem.logging.AddressTemplates.XML_FORMATTER_TEMPLATE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.ADD;
import static org.jboss.hal.dmr.ModelDescriptionConstants.ASYNC_HANDLER;
import static org.jboss.hal.dmr.ModelDescriptionConstants.ATTRIBUTES;
import static org.jboss.hal.dmr.ModelDescriptionConstants.CLASS;
import static org.jboss.hal.dmr.ModelDescriptionConstants.CONSOLE_HANDLER;
import static org.jboss.hal.dmr.ModelDescriptionConstants.CUSTOM_FORMATTER;
import static org.jboss.hal.dmr.ModelDescriptionConstants.CUSTOM_HANDLER;
import static org.jboss.hal.dmr.ModelDescriptionConstants.FILE_HANDLER;
import static org.jboss.hal.dmr.ModelDescriptionConstants.FORMATTER;
import static org.jboss.hal.dmr.ModelDescriptionConstants.HANDLERS;
import static org.jboss.hal.dmr.ModelDescriptionConstants.JSON;
import static org.jboss.hal.dmr.ModelDescriptionConstants.JSON_FORMATTER;
import static org.jboss.hal.dmr.ModelDescriptionConstants.KEY_OVERRIDES;
import static org.jboss.hal.dmr.ModelDescriptionConstants.LEVEL;
import static org.jboss.hal.dmr.ModelDescriptionConstants.LOGGER;
import static org.jboss.hal.dmr.ModelDescriptionConstants.LOGGING_PROFILE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.MODULE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.NAME;
import static org.jboss.hal.dmr.ModelDescriptionConstants.PATTERN_FORMATTER;
import static org.jboss.hal.dmr.ModelDescriptionConstants.PERIODIC_ROTATING_FILE_HANDLER;
import static org.jboss.hal.dmr.ModelDescriptionConstants.PERIODIC_SIZE_ROTATING_FILE_HANDLER;
import static org.jboss.hal.dmr.ModelDescriptionConstants.PORT;
import static org.jboss.hal.dmr.ModelDescriptionConstants.READ_ATTRIBUTE_OPERATION;
import static org.jboss.hal.dmr.ModelDescriptionConstants.REMOVE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.SIZE_ROTATING_FILE_HANDLER;
import static org.jboss.hal.dmr.ModelDescriptionConstants.SOCKET_HANDLER;
import static org.jboss.hal.dmr.ModelDescriptionConstants.SYSLOG_HANDLER;
import static org.jboss.hal.dmr.ModelDescriptionConstants.TABLE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.TARGET;
import static org.jboss.hal.dmr.ModelDescriptionConstants.XML;
import static org.jboss.hal.dmr.ModelDescriptionConstants.XML_FORMATTER;
import static org.jboss.hal.dmr.ModelNodeHelper.failSafeGet;
import static org.jboss.hal.resources.CSS.fontAwesome;
import static org.jboss.hal.resources.CSS.marginTopLarge;
import static org.jboss.hal.resources.Ids.FORM;
import static org.jboss.hal.resources.Ids.TAB;
import static org.jboss.hal.resources.Ids.TAB_CONTAINER;
import static org.jboss.hal.resources.Names.CATEGORY;

@MbuiView
@SuppressWarnings({ "DuplicateStringLiteralInspection", "HardCodedStringLiteral", "WeakerAccess" })
public abstract class LoggingProfileView extends MbuiViewImpl<LoggingProfilePresenter>
        implements LoggingProfilePresenter.MyView {

    private static final String EQ_WILDCARD = "=*";
    private static final String ROOT_LOGGER_EQ_ROOT = "root-logger=ROOT";

    // ------------------------------------------------------ initialization

    public static LoggingProfileView create(MbuiContext mbuiContext, CrudOperations crud) {
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
    @MbuiElement(Ids.LOGGING_PROFILE + "-handler-socket-table") Table<NamedNode> socketHandlerTable;
    @MbuiElement(Ids.LOGGING_PROFILE + "-handler-socket-form") Form<NamedNode> socketHandlerForm;
    @MbuiElement(Ids.LOGGING_PROFILE + "-handler-syslog-table") Table<NamedNode> syslogHandlerTable;
    @MbuiElement(Ids.LOGGING_PROFILE + "-handler-syslog-form") Form<NamedNode> syslogHandlerForm;
    @MbuiElement(Ids.LOGGING_PROFILE + "-formatter-custom-table") Table<NamedNode> customFormatterTable;
    @MbuiElement(Ids.LOGGING_PROFILE + "-formatter-custom-form") Form<NamedNode> customFormatterForm;
    @MbuiElement(Ids.LOGGING_PROFILE + "-formatter-pattern-table") Table<NamedNode> patternFormatterTable;
    @MbuiElement(Ids.LOGGING_PROFILE + "-formatter-pattern-form") Form<NamedNode> patternFormatterForm;
    private Table<NamedNode> jsonFormatterTable;
    private Form<NamedNode> jsonFormatterForm;
    private Form<ModelNode> jsonKeyOverridesForm;
    private Table<NamedNode> xmlFormatterTable;
    private Form<NamedNode> xmlFormatterForm;
    private Form<ModelNode> xmlKeyOverridesForm;

    final SelectionAwareStatementContext selectionAwareStatementContext;
    final SuggestHandler suggestHandlers;
    final SuggestHandler namedFormatters;
    EmptyState noRootLogger;

    LoggingProfileView(MbuiContext mbuiContext) {
        super(mbuiContext);
        selectionAwareStatementContext = new SelectionAwareStatementContext(mbuiContext.statementContext(),
                () -> presenter.getLoggingProfile());
        List<AddressTemplate> templatesHandlers = asList(
                SELECTED_LOGGING_PROFILE_TEMPLATE.append(ASYNC_HANDLER + EQ_WILDCARD),
                SELECTED_LOGGING_PROFILE_TEMPLATE.append(CONSOLE_HANDLER + EQ_WILDCARD),
                SELECTED_LOGGING_PROFILE_TEMPLATE.append(CUSTOM_HANDLER + EQ_WILDCARD),
                SELECTED_LOGGING_PROFILE_TEMPLATE.append(FILE_HANDLER + EQ_WILDCARD),
                SELECTED_LOGGING_PROFILE_TEMPLATE.append(PERIODIC_ROTATING_FILE_HANDLER + EQ_WILDCARD),
                SELECTED_LOGGING_PROFILE_TEMPLATE.append(PERIODIC_SIZE_ROTATING_FILE_HANDLER + EQ_WILDCARD),
                SELECTED_LOGGING_PROFILE_TEMPLATE.append(SIZE_ROTATING_FILE_HANDLER + EQ_WILDCARD),
                SELECTED_LOGGING_PROFILE_TEMPLATE.append(SOCKET_HANDLER + EQ_WILDCARD),
                SELECTED_LOGGING_PROFILE_TEMPLATE.append(SYSLOG_HANDLER + EQ_WILDCARD));
        suggestHandlers = new ReadChildrenAutoComplete(mbuiContext.dispatcher(), selectionAwareStatementContext,
                templatesHandlers);

        List<AddressTemplate> templatesNamedFormatters = asList(
                SELECTED_LOGGING_PROFILE_TEMPLATE.append(PATTERN_FORMATTER + EQ_WILDCARD),
                SELECTED_LOGGING_PROFILE_TEMPLATE.append(CUSTOM_FORMATTER + EQ_WILDCARD),
                SELECTED_LOGGING_PROFILE_TEMPLATE.append(JSON_FORMATTER + EQ_WILDCARD),
                SELECTED_LOGGING_PROFILE_TEMPLATE.append(XML_FORMATTER + EQ_WILDCARD));
        namedFormatters = new ReadChildrenAutoComplete(mbuiContext.dispatcher(), selectionAwareStatementContext,
                templatesNamedFormatters);
    }

    @Override
    protected StatementContext statementContext() {
        return selectionAwareStatementContext;
    }

    @PostConstruct
    @SuppressWarnings("Duplicates")
    void init() {
        Constants constants = mbuiContext.resources().constants();
        noRootLogger = new EmptyState.Builder(Ids.LOGGING_PROFILE + "-root-logger-empty",
                constants.noRootLogger())
                .description(constants.noRootLoggerDescription())
                .icon(fontAwesome("sitemap"))
                .primaryAction(constants.add(), this::addRootLogger)
                .build();
        noRootLogger.element().classList.add(marginTopLarge);

        // hack which relies on the element hierarchy given in the template. will break if you change that hierarchy.
        rootLoggerForm.element().parentNode.appendChild(noRootLogger.element());
        rootLoggerVisibility(false);

        // --------------------------- json formatter
        AddressTemplate jsonTemplate = LOGGING_PROFILE_TEMPLATE.append(
                JSON_FORMATTER_TEMPLATE.lastName() + EQ_WILDCARD);
        LabelBuilder labelBuilder = new LabelBuilder();
        Metadata jsonMetadata = mbuiContext.metadataRegistry().lookup(jsonTemplate);
        Metadata jsonKeyOverridesMetadata = jsonMetadata.forComplexAttribute(KEY_OVERRIDES);
        String jsonLabel = labelBuilder.label(jsonTemplate.lastName());
        jsonFormatterTable = new ModelNodeTable.Builder<NamedNode>(Ids.build(LOGGING_PROFILE, JSON, FORMATTER, TABLE),
                jsonMetadata)
                .button(constants.add(), table -> crud().add(Ids.build(LOGGING_PROFILE, JSON, FORMATTER, ADD),
                        Names.JSON_FORMATTER, jsonTemplate.replaceWildcards(presenter.getLoggingProfile()),
                        (name, address) -> presenter.reload()), Constraint.executable(jsonTemplate, ADD))
                .button(constants.remove(), table -> crud().remove(Names.JSON_FORMATTER, table.selectedRow().getName(),
                        jsonTemplate.replaceWildcards(presenter.getLoggingProfile()), () -> presenter.reload()),
                        Constraint.executable(jsonTemplate, REMOVE))
                .nameColumn()
                .build();

        jsonFormatterForm = new ModelNodeForm.Builder<NamedNode>(Ids.build(LOGGING_PROFILE, JSON, FORMATTER, FORM),
                jsonMetadata)
                .exclude(KEY_OVERRIDES + ".*")
                .onSave((form, changedValues) -> mbuiContext.crud().save(jsonLabel, form.getModel().getName(),
                        jsonTemplate.replaceWildcards(presenter.getLoggingProfile()), changedValues,
                        () -> presenter.reload()))
                .prepareReset(form -> mbuiContext.crud().reset(jsonLabel, jsonFormatterTable.selectedRow().getName(),
                        jsonTemplate.replaceWildcards(presenter.getLoggingProfile()), form, jsonMetadata,
                        () -> presenter.reload()))
                .build();

        jsonKeyOverridesForm = new ModelNodeForm.Builder<>(
                Ids.build(LOGGING_PROFILE, FORMATTER, JSON, KEY_OVERRIDES, FORM),
                jsonKeyOverridesMetadata)
                .singleton(() -> {
                    StatementContext jsonStatementContext = new SelectionAwareStatementContext(
                            mbuiContext.statementContext(),
                            () -> jsonFormatterTable.hasSelection() ? jsonFormatterTable.selectedRow().getName()
                                    : null);
                    ResourceAddress address = jsonTemplate.resolve(jsonStatementContext, presenter.getLoggingProfile());
                    return new Operation.Builder(address, READ_ATTRIBUTE_OPERATION)
                            .param(NAME, KEY_OVERRIDES)
                            .build();
                },
                        () -> presenter.addComplexObject(jsonLabel, jsonFormatterTable.selectedRow().getName(),
                                KEY_OVERRIDES, jsonTemplate.replaceWildcards(presenter.getLoggingProfile())))
                .onSave((form, changedValues) -> presenter.saveComplexObject(jsonLabel,
                        jsonFormatterTable.selectedRow().getName(), KEY_OVERRIDES,
                        jsonTemplate.replaceWildcards(presenter.getLoggingProfile()), changedValues))
                .prepareReset(
                        form -> presenter.resetComplexObject(jsonLabel, jsonFormatterTable.selectedRow().getName(),
                                KEY_OVERRIDES, jsonTemplate.replaceWildcards(presenter.getLoggingProfile()),
                                jsonKeyOverridesMetadata, form))
                .prepareRemove(
                        form -> presenter.removeComplexObject(jsonLabel, jsonFormatterTable.selectedRow().getName(),
                                KEY_OVERRIDES, jsonTemplate.replaceWildcards(presenter.getLoggingProfile())))
                .build();

        Tabs jsonTabs = new Tabs(Ids.build(LOGGING_PROFILE, FORMATTER, JSON, TAB_CONTAINER));
        jsonTabs.add(Ids.build(LOGGING_PROFILE, FORMATTER, JSON, ATTRIBUTES, TAB), constants.attributes(),
                jsonFormatterForm.element());
        jsonTabs.add(Ids.build(LOGGING_PROFILE, FORMATTER, JSON, KEY_OVERRIDES, TAB), Names.KEY_OVERRIDES,
                jsonKeyOverridesForm.element());

        HTMLElement jsonSection = section()
                .add(h(1).textContent(Names.JSON_FORMATTER))
                .add(p().textContent(jsonMetadata.getDescription().getDescription()))
                .add(jsonFormatterTable)
                .add(jsonTabs).element();

        registerAttachable(jsonFormatterTable, jsonFormatterForm, jsonKeyOverridesForm);

        navigation.insertSecondary("logging-profile-formatter-item",
                Ids.build(LOGGING_PROFILE, FORMATTER, JSON, Ids.ITEM), null,
                Names.JSON_FORMATTER, jsonSection);

        // --------------------------- xml formatter
        AddressTemplate xmlTemplate = LOGGING_PROFILE_TEMPLATE.append(XML_FORMATTER_TEMPLATE.lastName() + EQ_WILDCARD);
        Metadata xmlMetadata = mbuiContext.metadataRegistry().lookup(xmlTemplate);
        Metadata xmlKeyOverridesMetadata = xmlMetadata.forComplexAttribute(KEY_OVERRIDES);
        String xmlLabel = labelBuilder.label(xmlTemplate.lastName());
        xmlFormatterTable = new ModelNodeTable.Builder<NamedNode>(Ids.build(LOGGING_PROFILE, XML, FORMATTER, TABLE),
                xmlMetadata)
                .button(constants.add(), table -> crud().add(Ids.build(LOGGING_PROFILE, XML, FORMATTER, ADD),
                        Names.XML_FORMATTER, xmlTemplate.replaceWildcards(presenter.getLoggingProfile()),
                        (name, address) -> presenter.reload()), Constraint.executable(xmlTemplate, ADD))
                .button(constants.remove(), table -> crud().remove(Names.XML_FORMATTER, table.selectedRow().getName(),
                        xmlTemplate.replaceWildcards(presenter.getLoggingProfile()), () -> presenter.reload()),
                        Constraint.executable(xmlTemplate, REMOVE))
                .nameColumn()
                .build();

        xmlFormatterForm = new ModelNodeForm.Builder<NamedNode>(Ids.build(LOGGING_PROFILE, XML, FORMATTER, FORM),
                xmlMetadata)
                .exclude(KEY_OVERRIDES + ".*")
                .onSave((form, changedValues) -> mbuiContext.crud().save(xmlLabel, form.getModel().getName(),
                        xmlTemplate.replaceWildcards(presenter.getLoggingProfile()), changedValues,
                        () -> presenter.reload()))
                .prepareReset(form -> mbuiContext.crud().reset(xmlLabel, xmlFormatterTable.selectedRow().getName(),
                        xmlTemplate.replaceWildcards(presenter.getLoggingProfile()), form, xmlMetadata,
                        () -> presenter.reload()))
                .build();

        xmlKeyOverridesForm = new ModelNodeForm.Builder<>(
                Ids.build(LOGGING_PROFILE, FORMATTER, XML, KEY_OVERRIDES, FORM),
                xmlKeyOverridesMetadata)
                .singleton(() -> {
                    StatementContext xmlStatementContext = new SelectionAwareStatementContext(
                            mbuiContext.statementContext(),
                            () -> xmlFormatterTable.hasSelection() ? xmlFormatterTable.selectedRow().getName() : null);
                    ResourceAddress address = xmlTemplate.resolve(xmlStatementContext, presenter.getLoggingProfile());
                    return new Operation.Builder(address, READ_ATTRIBUTE_OPERATION)
                            .param(NAME, KEY_OVERRIDES)
                            .build();
                },
                        () -> presenter.addComplexObject(xmlLabel, xmlFormatterTable.selectedRow().getName(),
                                KEY_OVERRIDES, xmlTemplate.replaceWildcards(presenter.getLoggingProfile())))
                .onSave((form, changedValues) -> presenter.saveComplexObject(xmlLabel,
                        xmlFormatterTable.selectedRow().getName(), KEY_OVERRIDES,
                        xmlTemplate.replaceWildcards(presenter.getLoggingProfile()), changedValues))
                .prepareReset(form -> presenter.resetComplexObject(xmlLabel, xmlFormatterTable.selectedRow().getName(),
                        KEY_OVERRIDES, xmlTemplate.replaceWildcards(presenter.getLoggingProfile()),
                        xmlKeyOverridesMetadata, form))
                .prepareRemove(
                        form -> presenter.removeComplexObject(xmlLabel, xmlFormatterTable.selectedRow().getName(),
                                KEY_OVERRIDES, xmlTemplate.replaceWildcards(presenter.getLoggingProfile())))
                .build();

        Tabs xmlTabs = new Tabs(Ids.build(LOGGING_PROFILE, FORMATTER, XML, TAB_CONTAINER));
        xmlTabs.add(Ids.build(LOGGING_PROFILE, FORMATTER, XML, ATTRIBUTES, TAB), constants.attributes(),
                xmlFormatterForm.element());
        xmlTabs.add(Ids.build(LOGGING_PROFILE, FORMATTER, XML, KEY_OVERRIDES, TAB), Names.KEY_OVERRIDES,
                xmlKeyOverridesForm.element());

        HTMLElement xmlSection = section()
                .add(h(1).textContent(Names.XML_FORMATTER))
                .add(p().textContent(xmlMetadata.getDescription().getDescription()))
                .add(xmlFormatterTable)
                .add(xmlTabs).element();

        registerAttachable(xmlFormatterTable, xmlFormatterForm, xmlKeyOverridesForm);

        navigation.insertSecondary("logging-profile-formatter-item",
                Ids.build(LOGGING_PROFILE, FORMATTER, XML, Ids.ITEM), null,
                Names.XML_FORMATTER, xmlSection);
    }

    @Override
    public void attach() {
        super.attach();
        jsonFormatterTable.bindForm(jsonFormatterForm);
        jsonFormatterTable.onSelectionChange(t -> {
            if (t.hasSelection()) {
                jsonKeyOverridesForm.view(failSafeGet(t.selectedRow().asModelNode(), KEY_OVERRIDES));
            } else {
                jsonKeyOverridesForm.clear();
            }
        });

        xmlFormatterTable.bindForm(xmlFormatterForm);
        xmlFormatterTable.onSelectionChange(t -> {
            if (t.hasSelection()) {
                xmlKeyOverridesForm.view(failSafeGet(t.selectedRow().asModelNode(), KEY_OVERRIDES));
            } else {
                xmlKeyOverridesForm.clear();
            }
        });
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
    public void updateRootLogger(ModelNode modelNode) {
        rootLoggerVisibility(true);
        rootLoggerForm.view(modelNode);
    }

    @Override
    public void noRootLogger() {
        rootLoggerVisibility(false);
    }

    private void rootLoggerVisibility(boolean visible) {
        Elements.setVisible((HTMLElement) document.getElementById(Ids.LOGGING_PROFILE + "-root-logger-header"),
                visible);
        Elements.setVisible((HTMLElement) document.getElementById(Ids.LOGGING_PROFILE + "-root-logger-description"),
                visible);
        Elements.setVisible(rootLoggerForm.element(), visible);
        Elements.setVisible(noRootLogger.element(), !visible);
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
                            .resolve(presenter.getStatementContext(), presenter.getLoggingProfile());
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
                Names.FILE_HANDLER, LEVEL, FORMATTER);
    }

    void removeFileHandler(Table<NamedNode> table) {
        removeResource(table, FILE_HANDLER + EQ_WILDCARD, Names.FILE_HANDLER);
    }

    void saveFileHandler(Form<NamedNode> form, Map<String, Object> changedValues) {
        String name = form.getModel().getName();
        Metadata metadata = mbuiContext.metadataRegistry()
                .lookup(LOGGING_PROFILE_TEMPLATE.append(FILE_HANDLER + EQ_WILDCARD));
        saveForm(Names.FILE_HANDLER, name, SELECTED_LOGGING_PROFILE_TEMPLATE
                .append(FILE_HANDLER + EQ_WILDCARD)
                .resolve(selectionAwareStatementContext, name), changedValues, metadata);
    }

    void resetFileHandler(Form<NamedNode> form) {
        String name = form.getModel().getName();
        Metadata metadata = mbuiContext.metadataRegistry()
                .lookup(LOGGING_PROFILE_TEMPLATE.append(FILE_HANDLER + EQ_WILDCARD));
        resetForm(Names.FILE_HANDLER, name, SELECTED_LOGGING_PROFILE_TEMPLATE
                .append(FILE_HANDLER + EQ_WILDCARD)
                .resolve(selectionAwareStatementContext, name), form, metadata);
    }

    @Override
    public void updateFileHandler(List<NamedNode> items) {
        navigation.updateBadge(Ids.build(Ids.LOGGING_PROFILE, "handler-file", Ids.ITEM), items.size());
        fileHandlerForm.clear();
        fileHandlerTable.update(items);
    }

    // ------------------------------------------------------ periodic handler

    void addPeriodicHandler() {
        addFileHandlerResource(PERIODIC_ROTATING_FILE_HANDLER + EQ_WILDCARD,
                Ids.build(Ids.LOGGING_PROFILE, "handler-periodic-rotating-file", Ids.TABLE, Ids.ADD),
                Names.PERIODIC_HANDLER, "suffix", LEVEL, FORMATTER);
    }

    void removePeriodicHandler(Table<NamedNode> table) {
        removeResource(table, PERIODIC_ROTATING_FILE_HANDLER + EQ_WILDCARD,
                Names.PERIODIC_HANDLER);
    }

    void savePeriodicHandler(Form<NamedNode> form, Map<String, Object> changedValues) {
        String name = form.getModel().getName();
        Metadata metadata = mbuiContext.metadataRegistry()
                .lookup(LOGGING_PROFILE_TEMPLATE.append(PERIODIC_ROTATING_FILE_HANDLER + EQ_WILDCARD));
        saveForm(Names.PERIODIC_HANDLER, name, SELECTED_LOGGING_PROFILE_TEMPLATE
                .append(PERIODIC_ROTATING_FILE_HANDLER + EQ_WILDCARD)
                .resolve(selectionAwareStatementContext, name), changedValues, metadata);
    }

    void resetPeriodicHandler(Form<NamedNode> form) {
        String name = form.getModel().getName();
        Metadata metadata = mbuiContext.metadataRegistry().lookup(LOGGING_PROFILE_TEMPLATE
                .append(PERIODIC_ROTATING_FILE_HANDLER + EQ_WILDCARD));
        resetForm(Names.PERIODIC_HANDLER, name, SELECTED_LOGGING_PROFILE_TEMPLATE
                .append(PERIODIC_ROTATING_FILE_HANDLER + EQ_WILDCARD)
                .resolve(selectionAwareStatementContext, name), form, metadata);
    }

    @Override
    public void updatePeriodicHandler(List<NamedNode> items) {
        navigation.updateBadge(Ids.build(Ids.LOGGING_PROFILE, "handler-periodic-rotating-file", Ids.ITEM),
                items.size());
        periodicHandlerForm.clear();
        periodicHandlerTable.update(items);
    }

    // ------------------------------------------------------ periodic size handler

    void addPeriodicSizeHandler() {
        addFileHandlerResource(PERIODIC_SIZE_ROTATING_FILE_HANDLER + EQ_WILDCARD,
                Ids.build(Ids.LOGGING_PROFILE, "handler-periodic-size-rotating-file", Ids.TABLE, Ids.ADD),
                Names.PERIODIC_SIZE_HANDLER, "suffix", LEVEL, FORMATTER, "rotate-size", "max-backup-index");
    }

    void removePeriodicSizeHandler(Table<NamedNode> table) {
        removeResource(table, PERIODIC_SIZE_ROTATING_FILE_HANDLER + EQ_WILDCARD,
                Names.PERIODIC_SIZE_HANDLER);
    }

    void savePeriodicSizeHandler(Form<NamedNode> form, Map<String, Object> changedValues) {
        String name = form.getModel().getName();
        Metadata metadata = mbuiContext.metadataRegistry().lookup(LOGGING_PROFILE_TEMPLATE
                .append(PERIODIC_SIZE_ROTATING_FILE_HANDLER + EQ_WILDCARD));
        saveForm(Names.PERIODIC_SIZE_HANDLER, name, SELECTED_LOGGING_PROFILE_TEMPLATE
                .append(PERIODIC_SIZE_ROTATING_FILE_HANDLER + EQ_WILDCARD)
                .resolve(selectionAwareStatementContext, name), changedValues, metadata);
    }

    void resetPeriodicSizeHandler(Form<NamedNode> form) {
        String name = form.getModel().getName();
        Metadata metadata = mbuiContext.metadataRegistry().lookup(LOGGING_PROFILE_TEMPLATE
                .append(PERIODIC_SIZE_ROTATING_FILE_HANDLER + EQ_WILDCARD));
        resetForm(Names.PERIODIC_SIZE_HANDLER, name, SELECTED_LOGGING_PROFILE_TEMPLATE
                .append(PERIODIC_SIZE_ROTATING_FILE_HANDLER + EQ_WILDCARD)
                .resolve(selectionAwareStatementContext, name), form, metadata);
    }

    @Override
    public void updatePeriodicSizeHandler(List<NamedNode> items) {
        navigation.updateBadge(Ids.build(Ids.LOGGING_PROFILE, "handler-periodic-size-rotating-file", Ids.ITEM),
                items.size());
        periodicSizeHandlerForm.clear();
        periodicSizeHandlerTable.update(items);
    }

    // ------------------------------------------------------ size handler

    void addSizeHandler() {
        addFileHandlerResource(SIZE_ROTATING_FILE_HANDLER + EQ_WILDCARD,
                Ids.build(Ids.LOGGING_PROFILE, "handler-size-rotating-file", Ids.TABLE, Ids.ADD),
                Names.SIZE_HANDLER, "suffix", LEVEL, FORMATTER, "rotate-size", "max-backup-index");
    }

    void removeSizeHandler(Table<NamedNode> table) {
        removeResource(table, SIZE_ROTATING_FILE_HANDLER + EQ_WILDCARD, Names.SIZE_HANDLER);
    }

    void saveSizeHandler(Form<NamedNode> form, Map<String, Object> changedValues) {
        String name = form.getModel().getName();
        Metadata metadata = mbuiContext.metadataRegistry()
                .lookup(LOGGING_PROFILE_TEMPLATE.append(SIZE_ROTATING_FILE_HANDLER + EQ_WILDCARD));
        saveForm(Names.SIZE_HANDLER, name, SELECTED_LOGGING_PROFILE_TEMPLATE
                .append(SIZE_ROTATING_FILE_HANDLER + EQ_WILDCARD)
                .resolve(selectionAwareStatementContext, name), changedValues, metadata);
    }

    void resetSizeHandler(Form<NamedNode> form) {
        String name = form.getModel().getName();
        Metadata metadata = mbuiContext.metadataRegistry()
                .lookup(LOGGING_PROFILE_TEMPLATE.append(SIZE_ROTATING_FILE_HANDLER + EQ_WILDCARD));
        resetForm(Names.SIZE_HANDLER, name, SELECTED_LOGGING_PROFILE_TEMPLATE
                .append(SIZE_ROTATING_FILE_HANDLER + EQ_WILDCARD)
                .resolve(selectionAwareStatementContext, name), form, metadata);
    }

    @Override
    public void updateSizeHandler(List<NamedNode> items) {
        navigation.updateBadge(Ids.build(Ids.LOGGING_PROFILE, "handler-size-rotating-file", Ids.ITEM),
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
    public void updateAsyncHandler(List<NamedNode> items) {
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
    public void updateCustomHandler(List<NamedNode> items) {
        navigation.updateBadge(Ids.build(Ids.LOGGING_PROFILE, "handler-custom", Ids.ITEM), items.size());
        customHandlerForm.clear();
        customHandlerTable.update(items);
    }

    // ------------------------------------------------------ socket handler

    void addSocketHandler() {
        addResource(SOCKET_HANDLER + EQ_WILDCARD,
                Ids.build(Ids.LOGGING_PROFILE, "handler-socket", Ids.TABLE, Ids.ADD),
                Names.SOCKET_ACTION_HANDLER, LEVEL, "named-formatter", "outbound-socket-binding-ref");
    }

    void removeSocketHandler(Table<NamedNode> table) {
        removeResource(table, SOCKET_HANDLER + EQ_WILDCARD, Names.SOCKET_ACTION_HANDLER);
    }

    void saveSocketHandler(Form<NamedNode> form, Map<String, Object> changedValues) {
        String name = form.getModel().getName();
        Metadata metadata = mbuiContext.metadataRegistry()
                .lookup(LOGGING_PROFILE_TEMPLATE.append(SOCKET_HANDLER + EQ_WILDCARD));
        saveForm(Names.SOCKET_ACTION_HANDLER, name, SELECTED_LOGGING_PROFILE_TEMPLATE
                .append(SOCKET_HANDLER + EQ_WILDCARD)
                .resolve(selectionAwareStatementContext, name), changedValues, metadata);
    }

    void resetSocketHandler(Form<NamedNode> form) {
        String name = form.getModel().getName();
        Metadata metadata = mbuiContext.metadataRegistry()
                .lookup(LOGGING_PROFILE_TEMPLATE.append(SOCKET_HANDLER + EQ_WILDCARD));
        resetForm(Names.SOCKET_ACTION_HANDLER, name, SELECTED_LOGGING_PROFILE_TEMPLATE
                .append(SOCKET_HANDLER + EQ_WILDCARD)
                .resolve(selectionAwareStatementContext, name), form, metadata);
    }

    @Override
    public void updateSocketHandler(List<NamedNode> items) {
        navigation.updateBadge(Ids.build(Ids.LOGGING_PROFILE, "handler-socket", Ids.ITEM), items.size());
        socketHandlerForm.clear();
        socketHandlerTable.update(items);
    }

    // ------------------------------------------------------ syslog handler

    void addSyslogHandler() {
        addResource(SYSLOG_HANDLER + EQ_WILDCARD,
                Ids.build(Ids.LOGGING_PROFILE, "handler-syslog", Ids.TABLE, Ids.ADD),
                Names.SYSLOG_ACTION_HANDLER, LEVEL, "named-formatter", "syslog-format", "hostname", "server-address",
                PORT, "app-name", "facility");
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
    public void updateSyslogHandler(List<NamedNode> items) {
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
    public void updateCustomFormatter(List<NamedNode> items) {
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
    public void updatePatternFormatter(List<NamedNode> items) {
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
        FormItem<Object> namedFormatter = dialog.getForm().getFormItem("named-formatter");
        if (namedFormatter != null) {
            namedFormatter.registerSuggestHandler(namedFormatters);
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
                .include("file.path", "file.relative-to")
                .unsorted();
        if (attributes != null) {
            builder.include(asList(attributes));
        }
        ModelNodeForm<ModelNode> form = builder.build();
        form.getFormItem("file.relative-to").registerSuggestHandler(new PathsAutoComplete());
        AddResourceDialog dialog = new AddResourceDialog(mbuiContext.resources().messages().addResourceTitle(type), form,
                (name, modelNode) -> {
                    ResourceAddress address = selectionTemplate.resolve(selectionAwareStatementContext, name);
                    crud().add(type, name, address, modelNode, (n, a) -> presenter.reload());
                });
        dialog.show();
    }

    private void removeResource(Table<NamedNode> table, String templateSuffix, String type) {
        String name = table.selectedRow().getName();
        AddressTemplate selectionTemplate = SELECTED_LOGGING_PROFILE_TEMPLATE.append(templateSuffix);
        ResourceAddress address = selectionTemplate.resolve(selectionAwareStatementContext, name);
        crud().remove(type, name, address, () -> presenter.reload());
    }

    @Override
    public void updateJsonFormatter(List<NamedNode> items) {
        navigation.updateBadge("logging-profile-formatter-json-item", items.size());
        jsonFormatterForm.clear();
        jsonKeyOverridesForm.clear();
        jsonFormatterTable.update(items);
    }

    @Override
    public void updateXmlFormatter(List<NamedNode> items) {
        navigation.updateBadge("logging-profile-formatter-xml-item", items.size());
        xmlFormatterForm.clear();
        xmlKeyOverridesForm.clear();
        xmlFormatterTable.update(items);
    }
}
