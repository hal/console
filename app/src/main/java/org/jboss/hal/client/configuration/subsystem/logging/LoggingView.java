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

import javax.annotation.PostConstruct;

import org.jboss.hal.ballroom.EmptyState;
import org.jboss.hal.ballroom.LabelBuilder;
import org.jboss.hal.ballroom.Tabs;
import org.jboss.hal.ballroom.VerticalNavigation;
import org.jboss.hal.ballroom.autocomplete.ReadChildrenAutoComplete;
import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.ballroom.form.SuggestHandler;
import org.jboss.hal.ballroom.table.Table;
import org.jboss.hal.core.mbui.MbuiContext;
import org.jboss.hal.core.mbui.MbuiViewImpl;
import org.jboss.hal.core.mbui.dialog.AddResourceDialog;
import org.jboss.hal.core.mbui.form.ModelNodeForm;
import org.jboss.hal.core.mbui.table.ModelNodeTable;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.NamedNode;
import org.jboss.hal.dmr.Operation;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.meta.SelectionAwareStatementContext;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.spi.MbuiElement;
import org.jboss.hal.spi.MbuiView;
import org.jboss.hal.spi.Message;
import org.jboss.hal.spi.MessageEvent;

import elemental2.dom.HTMLElement;

import static java.util.Arrays.asList;

import static elemental2.dom.DomGlobal.document;
import static org.jboss.elemento.Elements.h;
import static org.jboss.elemento.Elements.p;
import static org.jboss.elemento.Elements.section;
import static org.jboss.elemento.Elements.setVisible;
import static org.jboss.hal.client.configuration.subsystem.logging.AddressTemplates.*;
import static org.jboss.hal.dmr.ModelDescriptionConstants.*;
import static org.jboss.hal.dmr.ModelNodeHelper.failSafeGet;
import static org.jboss.hal.resources.CSS.marginTopLarge;
import static org.jboss.hal.resources.Ids.FORM;
import static org.jboss.hal.resources.Ids.LOGGING_FORMATTER_ITEM;
import static org.jboss.hal.resources.Ids.TAB;
import static org.jboss.hal.resources.Ids.TAB_CONTAINER;

@MbuiView
@SuppressWarnings({ "DuplicateStringLiteralInspection", "HardCodedStringLiteral", "WeakerAccess" })
public abstract class LoggingView extends MbuiViewImpl<LoggingPresenter> implements LoggingPresenter.MyView {

    // ------------------------------------------------------ initialization

    public static LoggingView create(MbuiContext mbuiContext) {
        return new Mbui_LoggingView(mbuiContext);
    }

    @MbuiElement("logging-vertical-navigation") VerticalNavigation navigation;
    @MbuiElement("logging-config-form") Form<ModelNode> loggingConfigForm;
    @MbuiElement("logging-root-logger-form") Form<ModelNode> rootLoggerForm;
    @MbuiElement("logging-category-table") Table<NamedNode> loggerTable;
    @MbuiElement("logging-category-form") Form<NamedNode> loggerForm;
    @MbuiElement("logging-handler-console-table") Table<NamedNode> consoleHandlerTable;
    @MbuiElement("logging-handler-console-form") Form<NamedNode> consoleHandlerForm;
    @MbuiElement("logging-handler-file-table") Table<NamedNode> fileHandlerTable;
    @MbuiElement("logging-handler-file-form") Form<NamedNode> fileHandlerForm;
    @MbuiElement("logging-handler-periodic-rotating-file-table") Table<NamedNode> periodicHandlerTable;
    @MbuiElement("logging-handler-periodic-rotating-file-form") Form<NamedNode> periodicHandlerForm;
    @MbuiElement("logging-handler-periodic-size-rotating-file-table") Table<NamedNode> periodicSizeHandlerTable;
    @MbuiElement("logging-handler-periodic-size-rotating-file-form") Form<NamedNode> periodicSizeHandlerForm;
    @MbuiElement("logging-handler-size-rotating-file-table") Table<NamedNode> sizeHandlerTable;
    @MbuiElement("logging-handler-size-rotating-file-form") Form<NamedNode> sizeHandlerForm;
    @MbuiElement("logging-handler-async-table") Table<NamedNode> asyncHandlerTable;
    @MbuiElement("logging-handler-async-form") Form<NamedNode> asyncHandlerForm;
    @MbuiElement("logging-handler-custom-table") Table<NamedNode> customHandlerTable;
    @MbuiElement("logging-handler-custom-form") Form<NamedNode> customHandlerForm;
    @MbuiElement("logging-handler-socket-table") Table<NamedNode> socketHandlerTable;
    @MbuiElement("logging-handler-socket-form") Form<NamedNode> socketHandlerForm;
    @MbuiElement("logging-handler-syslog-table") Table<NamedNode> syslogHandlerTable;
    @MbuiElement("logging-handler-syslog-form") Form<NamedNode> syslogHandlerForm;
    @MbuiElement("logging-formatter-custom-table") Table<NamedNode> customFormatterTable;
    @MbuiElement("logging-formatter-custom-form") Form<NamedNode> customFormatterForm;
    @MbuiElement("logging-formatter-pattern-table") Table<NamedNode> patternFormatterTable;
    @MbuiElement("logging-formatter-pattern-form") Form<NamedNode> patternFormatterForm;
    private Table<NamedNode> jsonFormatterTable;
    private Form<NamedNode> jsonFormatterForm;
    private Form<ModelNode> jsonKeyOverridesForm;
    private Table<NamedNode> xmlFormatterTable;
    private Form<NamedNode> xmlFormatterForm;
    private Form<ModelNode> xmlKeyOverridesForm;

    EmptyState noRootLogger;

    LoggingView(MbuiContext mbuiContext) {
        super(mbuiContext);
    }

    @PostConstruct
    void init() {
        noRootLogger = new EmptyState.Builder("logging-root-logger-empty",
                mbuiContext.resources().constants().noRootLogger())
                .description(mbuiContext.resources().constants().noRootLoggerDescription())
                .icon("fa fa-sitemap")
                .primaryAction(mbuiContext.resources().constants().add(), this::addRootLogger)
                .build();
        noRootLogger.element().classList.add(marginTopLarge);

        // hack which relies on the element hierarchy given in the template. will break if you change that hierarchy.
        rootLoggerForm.element().parentNode.appendChild(noRootLogger.element());
        rootLoggerVisibility(true);

        // --------------------------- json formatter
        LabelBuilder labelBuilder = new LabelBuilder();
        Metadata jsonMetadata = mbuiContext.metadataRegistry().lookup(JSON_FORMATTER_TEMPLATE);
        Metadata jsonKeyOverridesMetadata = jsonMetadata.forComplexAttribute(KEY_OVERRIDES);
        String jsonLabel = labelBuilder.label(JSON_FORMATTER_TEMPLATE.lastName());
        jsonFormatterTable = new ModelNodeTable.Builder<NamedNode>(Ids.build(LOGGING, JSON, FORMATTER, TABLE),
                jsonMetadata)
                .button(mbuiContext.tableButtonFactory().add(Ids.build(LOGGING, JSON, FORMATTER, Ids.ADD), jsonLabel,
                        JSON_FORMATTER_TEMPLATE, (name, address) -> presenter.reload()))
                .button(mbuiContext.tableButtonFactory().remove(jsonLabel, JSON_FORMATTER_TEMPLATE,
                        table -> table.selectedRow().getName(), () -> presenter.reload()))
                .nameColumn()
                .build();

        jsonFormatterForm = new ModelNodeForm.Builder<NamedNode>(Ids.build(LOGGING, JSON, FORMATTER, FORM),
                jsonMetadata)
                .exclude(KEY_OVERRIDES + ".*")
                .onSave((form, changedValues) -> mbuiContext.crud().save(jsonLabel, form.getModel().getName(),
                        JSON_FORMATTER_TEMPLATE, changedValues, () -> presenter.reload()))
                .prepareReset(form -> mbuiContext.crud().reset(jsonLabel, jsonFormatterTable.selectedRow().getName(),
                        JSON_FORMATTER_TEMPLATE, form, jsonMetadata, () -> presenter.reload()))
                .build();

        jsonKeyOverridesForm = new ModelNodeForm.Builder<>(Ids.build(LOGGING, FORMATTER, JSON, KEY_OVERRIDES, FORM),
                jsonKeyOverridesMetadata)
                .singleton(() -> {
                    StatementContext jsonStatementContext = new SelectionAwareStatementContext(
                            presenter.getStatementContext(), () -> jsonFormatterTable.selectedRow().getName());
                    return new Operation.Builder(JSON_FORMATTER_TEMPLATE.resolve(jsonStatementContext),
                            READ_ATTRIBUTE_OPERATION)
                            .param(NAME, KEY_OVERRIDES)
                            .build();
                },
                        () -> presenter.addComplexObject(jsonLabel, jsonFormatterTable.selectedRow().getName(),
                                KEY_OVERRIDES, JSON_FORMATTER_TEMPLATE))
                .onSave((form, changedValues) -> presenter.saveComplexObject(jsonLabel,
                        jsonFormatterTable.selectedRow().getName(), KEY_OVERRIDES, JSON_FORMATTER_TEMPLATE,
                        changedValues))
                .prepareReset(
                        form -> presenter.resetComplexObject(jsonLabel, jsonFormatterTable.selectedRow().getName(),
                                KEY_OVERRIDES, JSON_FORMATTER_TEMPLATE, jsonKeyOverridesMetadata, form))
                .prepareRemove(
                        form -> presenter.removeComplexObject(jsonLabel, jsonFormatterTable.selectedRow().getName(),
                                KEY_OVERRIDES, JSON_FORMATTER_TEMPLATE))
                .build();

        Tabs jsonTabs = new Tabs(Ids.build(LOGGING, FORMATTER, JSON, TAB_CONTAINER));
        jsonTabs.add(Ids.build(LOGGING, FORMATTER, JSON, ATTRIBUTES, TAB),
                mbuiContext.resources().constants().attributes(), jsonFormatterForm.element());
        jsonTabs.add(Ids.build(LOGGING, FORMATTER, JSON, KEY_OVERRIDES, TAB),
                Names.KEY_OVERRIDES, jsonKeyOverridesForm.element());

        HTMLElement jsonSection = section()
                .add(h(1).textContent(Names.JSON_FORMATTER))
                .add(p().textContent(jsonMetadata.getDescription().getDescription()))
                .add(jsonFormatterTable)
                .add(jsonTabs).element();

        registerAttachable(jsonFormatterTable, jsonFormatterForm, jsonKeyOverridesForm);

        navigation.insertSecondary(LOGGING_FORMATTER_ITEM, Ids.build(LOGGING, FORMATTER, JSON, Ids.ITEM), null,
                Names.JSON_FORMATTER, jsonSection);

        // --------------------------- xml formatter
        Metadata xmlMetadata = mbuiContext.metadataRegistry().lookup(XML_FORMATTER_TEMPLATE);
        Metadata xmlKeyOverridesMetadata = xmlMetadata.forComplexAttribute(KEY_OVERRIDES);
        String xmlLabel = labelBuilder.label(XML_FORMATTER_TEMPLATE.lastName());
        xmlFormatterTable = new ModelNodeTable.Builder<NamedNode>(Ids.build(LOGGING, XML, FORMATTER, TABLE),
                xmlMetadata)
                .button(mbuiContext.tableButtonFactory().add(Ids.build(LOGGING, XML, FORMATTER, Ids.ADD), xmlLabel,
                        XML_FORMATTER_TEMPLATE, (name, address) -> presenter.reload()))
                .button(mbuiContext.tableButtonFactory().remove(xmlLabel, XML_FORMATTER_TEMPLATE,
                        table -> table.selectedRow().getName(), () -> presenter.reload()))
                .nameColumn()
                .build();

        xmlFormatterForm = new ModelNodeForm.Builder<NamedNode>(Ids.build(LOGGING, XML, FORMATTER, FORM), xmlMetadata)
                .exclude(KEY_OVERRIDES + ".*")
                .onSave((form, changedValues) -> mbuiContext.crud().save(xmlLabel, form.getModel().getName(),
                        XML_FORMATTER_TEMPLATE, changedValues, () -> presenter.reload()))
                .prepareReset(form -> mbuiContext.crud().reset(xmlLabel, xmlFormatterTable.selectedRow().getName(),
                        XML_FORMATTER_TEMPLATE, form, xmlMetadata, () -> presenter.reload()))
                .build();

        xmlKeyOverridesForm = new ModelNodeForm.Builder<>(Ids.build(LOGGING, FORMATTER, XML, KEY_OVERRIDES, FORM),
                xmlKeyOverridesMetadata)
                .singleton(() -> {
                    StatementContext xmlStatementContext = new SelectionAwareStatementContext(
                            presenter.getStatementContext(), () -> xmlFormatterTable.selectedRow().getName());
                    return new Operation.Builder(XML_FORMATTER_TEMPLATE.resolve(xmlStatementContext),
                            READ_ATTRIBUTE_OPERATION)
                            .param(NAME, KEY_OVERRIDES)
                            .build();
                },
                        () -> presenter.addComplexObject(xmlLabel, xmlFormatterTable.selectedRow().getName(),
                                KEY_OVERRIDES, XML_FORMATTER_TEMPLATE))
                .onSave((form, changedValues) -> presenter.saveComplexObject(xmlLabel,
                        xmlFormatterTable.selectedRow().getName(), KEY_OVERRIDES, XML_FORMATTER_TEMPLATE,
                        changedValues))
                .prepareReset(form -> presenter.resetComplexObject(xmlLabel, xmlFormatterTable.selectedRow().getName(),
                        KEY_OVERRIDES, XML_FORMATTER_TEMPLATE, xmlKeyOverridesMetadata, form))
                .prepareRemove(
                        form -> presenter.removeComplexObject(xmlLabel, xmlFormatterTable.selectedRow().getName(),
                                KEY_OVERRIDES, XML_FORMATTER_TEMPLATE))
                .build();

        Tabs xmlTabs = new Tabs(Ids.build(LOGGING, FORMATTER, XML, TAB_CONTAINER));
        xmlTabs.add(Ids.build(LOGGING, FORMATTER, XML, ATTRIBUTES, TAB),
                mbuiContext.resources().constants().attributes(), xmlFormatterForm.element());
        xmlTabs.add(Ids.build(LOGGING, FORMATTER, XML, KEY_OVERRIDES, TAB),
                Names.KEY_OVERRIDES, xmlKeyOverridesForm.element());

        HTMLElement xmlSection = section()
                .add(h(1).textContent(Names.XML_FORMATTER))
                .add(p().textContent(xmlMetadata.getDescription().getDescription()))
                .add(xmlFormatterTable)
                .add(xmlTabs).element();

        registerAttachable(xmlFormatterTable, xmlFormatterForm, xmlKeyOverridesForm);

        navigation.insertSecondary(LOGGING_FORMATTER_ITEM, Ids.build(LOGGING, FORMATTER, XML, Ids.ITEM), null,
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
    // ------------------------------------------------------ logging configuration

    @Override
    public void updateLoggingConfig(ModelNode modelNode) {
        loggingConfigForm.view(modelNode);
    }

    // ------------------------------------------------------ root logger

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
        setVisible((HTMLElement) document.getElementById("logging-root-logger-header"), visible);
        setVisible((HTMLElement) document.getElementById("logging-root-logger-description"), visible);
        setVisible(rootLoggerForm.element(), visible);
        setVisible(noRootLogger.element(), !visible);
    }

    private void addRootLogger() {
        Metadata metadata = mbuiContext.metadataRegistry().lookup(ROOT_LOGGER_TEMPLATE);

        Form<ModelNode> form = new ModelNodeForm.Builder<>("logging-root-logger-add", metadata)
                .fromRequestProperties()
                .include(LEVEL, HANDLERS)
                .build();
        AddResourceDialog dialog = new AddResourceDialog(
                mbuiContext.resources().messages().addResourceTitle(Names.ROOT_LOGGER), form,
                (name, model) -> {
                    Operation operation = new Operation.Builder(
                            ROOT_LOGGER_TEMPLATE.resolve(mbuiContext.statementContext()), ADD)
                            .payload(model)
                            .build();
                    mbuiContext.dispatcher().execute(operation, result -> {
                        MessageEvent.fire(mbuiContext.eventBus(),
                                Message.success(mbuiContext.resources().messages()
                                        .addSingleResourceSuccess(Names.ROOT_LOGGER)));
                        presenter.reload();
                    });
                });

        SuggestHandler suggestHandler = new ReadChildrenAutoComplete(
                mbuiContext.dispatcher(),
                mbuiContext.statementContext(),
                asList(ASYNC_HANDLER_TEMPLATE, CONSOLE_HANDLER_TEMPLATE, CUSTOM_HANDLER_TEMPLATE, FILE_HANDLER_TEMPLATE,
                        PERIODIC_ROTATING_FILE_HANDLER_TEMPLATE, PERIODIC_SIZE_ROTATING_FILE_HANDLER_TEMPLATE,
                        SIZE_ROTATING_FILE_HANDLER_TEMPLATE, SYSLOG_HANDLER_TEMPLATE));
        dialog.getForm().getFormItem(HANDLERS).registerSuggestHandler(suggestHandler);
        dialog.show();
    }

    // ------------------------------------------------------ logger / categories

    @Override
    public void updateLogger(List<NamedNode> items) {
        loggerForm.clear();
        loggerTable.update(items);
    }

    // ------------------------------------------------------ handler

    @Override
    public void updateConsoleHandler(List<NamedNode> items) {
        navigation.updateBadge("logging-handler-console-item", items.size());
        consoleHandlerForm.clear();
        consoleHandlerTable.update(items);
    }

    @Override
    public void updateFileHandler(List<NamedNode> items) {
        navigation.updateBadge("logging-handler-file-item", items.size());
        fileHandlerForm.clear();
        fileHandlerTable.update(items);
    }

    @Override
    public void updatePeriodicHandler(List<NamedNode> items) {
        navigation.updateBadge("logging-handler-periodic-rotating-file-item", items.size());
        periodicHandlerForm.clear();
        periodicHandlerTable.update(items);
    }

    @Override
    public void updatePeriodicSizeHandler(List<NamedNode> items) {
        navigation.updateBadge("logging-handler-periodic-size-rotating-file-item", items.size());
        periodicSizeHandlerForm.clear();
        periodicSizeHandlerTable.update(items);
    }

    @Override
    public void updateSizeHandlerHandler(List<NamedNode> items) {
        navigation.updateBadge("logging-handler-size-rotating-file-item", items.size());
        sizeHandlerForm.clear();
        sizeHandlerTable.update(items);
    }

    @Override
    public void updateAsyncHandler(List<NamedNode> items) {
        navigation.updateBadge("logging-handler-async-item", items.size());
        asyncHandlerForm.clear();
        asyncHandlerTable.update(items);
    }

    @Override
    public void updateCustomHandler(List<NamedNode> items) {
        navigation.updateBadge("logging-handler-custom-item", items.size());
        customHandlerForm.clear();
        customHandlerTable.update(items);
    }

    @Override
    public void updateSocketHandler(List<NamedNode> items) {
        navigation.updateBadge("logging-handler-socket-item", items.size());
        socketHandlerForm.clear();
        socketHandlerTable.update(items);
    }

    @Override
    public void updateSyslogHandler(List<NamedNode> items) {
        navigation.updateBadge("logging-handler-syslog-item", items.size());
        syslogHandlerForm.clear();
        syslogHandlerTable.update(items);
    }

    // ------------------------------------------------------ formatter

    @Override
    public void updateCustomFormatter(List<NamedNode> items) {
        navigation.updateBadge("logging-formatter-custom-item", items.size());
        customFormatterForm.clear();
        customFormatterTable.update(items);
    }

    @Override
    public void updatePatternFormatter(List<NamedNode> items) {
        navigation.updateBadge("logging-formatter-pattern-item", items.size());
        patternFormatterForm.clear();
        patternFormatterTable.update(items);
    }

    @Override
    public void updateJsonFormatter(List<NamedNode> items) {
        navigation.updateBadge("logging-formatter-json-item", items.size());
        jsonFormatterForm.clear();
        jsonKeyOverridesForm.clear();
        jsonFormatterTable.update(items);
    }

    @Override
    public void updateXmlFormatter(List<NamedNode> items) {
        navigation.updateBadge("logging-formatter-xml-item", items.size());
        xmlFormatterForm.clear();
        xmlKeyOverridesForm.clear();
        xmlFormatterTable.update(items);
    }
}
