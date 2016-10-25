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

import elemental.client.Browser;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.hal.ballroom.EmptyState;
import org.jboss.hal.ballroom.VerticalNavigation;
import org.jboss.hal.ballroom.autocomplete.ReadChildrenAutoComplete;
import org.jboss.hal.ballroom.dialog.DialogFactory;
import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.ballroom.form.FormItem;
import org.jboss.hal.ballroom.form.SuggestHandler;
import org.jboss.hal.ballroom.table.Api;
import org.jboss.hal.ballroom.table.Api.RefreshMode;
import org.jboss.hal.ballroom.table.DataTable;
import org.jboss.hal.core.mbui.MbuiContext;
import org.jboss.hal.core.mbui.MbuiViewImpl;
import org.jboss.hal.core.mbui.dialog.AddResourceDialog;
import org.jboss.hal.core.mbui.form.ModelNodeForm;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.model.NamedNode;
import org.jboss.hal.dmr.model.Operation;
import org.jboss.hal.dmr.model.ResourceAddress;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.meta.SelectionAwareStatementContext;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.spi.MbuiElement;
import org.jboss.hal.spi.MbuiView;
import org.jboss.hal.spi.Message;
import org.jboss.hal.spi.MessageEvent;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.jboss.hal.client.configuration.subsystem.logging.AddressTemplates.LOGGING_PROFILE_TEMPLATE;
import static org.jboss.hal.client.configuration.subsystem.logging.AddressTemplates.SELECTED_LOGGING_PROFILE_TEMPLATE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.ADD;
import static org.jboss.hal.dmr.ModelDescriptionConstants.HANDLERS;
import static org.jboss.hal.dmr.ModelDescriptionConstants.LEVEL;
import static org.jboss.hal.dmr.ModelDescriptionConstants.REMOVE;
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

    public static LoggingProfileView create(final MbuiContext mbuiContext) {
        return new Mbui_LoggingProfileView(mbuiContext);
    }

    final SelectionAwareStatementContext selectionAwareStatementContext;
    final SuggestHandler suggestHandlers;
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
        noRootLogger.asElement().getClassList().add(marginTopLarge);

        // hack which relies on the element hierarchy given in the template. will break if you change that hierarchy.
        rootLoggerForm.asElement().getParentElement().appendChild(noRootLogger.asElement());
        rootLoggerVisibility(false);

        // add suggest handler
        rootLoggerForm.getFormItem(HANDLERS).registerSuggestHandler(suggestHandlers);
        loggerForm.getFormItem(HANDLERS).registerSuggestHandler(suggestHandlers);
    }


    // ------------------------------------------------------ root logger

    @SuppressWarnings("UnusedParameters")
    void saveRootLogger(Form<ModelNode> form, Map<String, Object> changedValues) {
        saveSingletonForm(changedValues, SELECTED_LOGGING_PROFILE_TEMPLATE.append("root-logger=ROOT")
                .resolve(selectionAwareStatementContext), "Root Logger");
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

    private void addRootLogger() {
        Metadata metadata = mbuiContext.metadataRegistry().lookup(LOGGING_PROFILE_TEMPLATE.append("root-logger=ROOT"));

        Form<ModelNode> form = new ModelNodeForm.Builder<>("logging-profile-root-logger-add", metadata)
                .addFromRequestProperties()
                .include(LEVEL, HANDLERS)
                .build();
        AddResourceDialog dialog = new AddResourceDialog(
                mbuiContext.resources().messages().addResourceTitle(Names.ROOT_LOGGER), form,
                (name, model) -> {
                    ResourceAddress address = SELECTED_LOGGING_PROFILE_TEMPLATE.append("root-logger=ROOT")
                            .resolve(selectionAwareStatementContext);
                    Operation operation = new Operation.Builder(ADD, address)
                            .payload(model)
                            .build();
                    mbuiContext.dispatcher().execute(operation, result -> {
                        MessageEvent.fire(mbuiContext.eventBus(),
                                Message.success(mbuiContext.resources().messages()
                                        .addSingleResourceSuccess(Names.ROOT_LOGGER)));
                        presenter.reload();
                    });
                });

        dialog.getForm().getFormItem(HANDLERS).registerSuggestHandler(suggestHandlers);
        dialog.show();
    }


    // ------------------------------------------------------ logger / categories

    void addLogger() {
        addResource("logger=*", Ids.build("logging-profile-categories-table", "add"), "Category",
                "level", "handlers", "use-parent-handlers");
    }

    void removeLogger(Api<NamedNode> api) {
        removeResource(api, "logger=*", "Category");
    }

    void saveLogger(Form<NamedNode> form, Map<String, Object> changedValues) {
        String name = form.getModel().getName();
        saveForm(changedValues, SELECTED_LOGGING_PROFILE_TEMPLATE.append("logger=*")
                .resolve(selectionAwareStatementContext, name), "Category", name);
    }

    @Override
    public void updateLogger(final List<NamedNode> items) {
        loggerTable.api().clear().add(items).refresh(RefreshMode.RESET);
        loggerForm.clear();
    }


    // ------------------------------------------------------ console handler

    void addConsoleHandler() {
        addResource("console-handler=*", Ids.build("logging-profile-handler-console-table", "add"),
                "Console Handler", "level", "target", "formatter");
    }

    void removeConsoleHandler(Api<NamedNode> api) {
        removeResource(api, "console-handler=*", "Console Handler");
    }

    void saveConsoleHandler(Form<NamedNode> form, Map<String, Object> changedValues) {
        String name = form.getModel().getName();
        saveForm(changedValues, SELECTED_LOGGING_PROFILE_TEMPLATE.append("console-handler=*")
                .resolve(selectionAwareStatementContext, name), "Console Handler", name);
    }

    @Override
    public void updateConsoleHandler(final List<NamedNode> items) {
        navigation.updateBadge("logging-profile-handler-console-item", items.size());
        consoleHandlerTable.api().clear().add(items).refresh(RefreshMode.RESET);
        consoleHandlerForm.clear();
    }


    // ------------------------------------------------------ file handler

    void addFileHandler() {
        addFileHandlerResource("file-handler=*", Ids.build("logging-profile-handler-file-table", "add"),
                "File Handler", "level", "formatter");
    }

    void removeFileHandler(Api<NamedNode> api) {
        removeResource(api, "file-handler=*", "File Handler");
    }

    void saveFileHandler(Form<NamedNode> form, Map<String, Object> changedValues) {
        String name = form.getModel().getName();
        saveForm(changedValues, SELECTED_LOGGING_PROFILE_TEMPLATE.append("file-handler=*")
                .resolve(selectionAwareStatementContext, name), "File Handler", name);
    }

    @Override
    public void updateFileHandler(final List<NamedNode> items) {
        navigation.updateBadge("logging-profile-handler-file-item", items.size());
        fileHandlerTable.api().clear().add(items).refresh(RefreshMode.RESET);
        fileHandlerForm.clear();
    }


    // ------------------------------------------------------ periodic handler

    void addPeriodicHandler() {
        addFileHandlerResource("periodic-rotating-file-handler=*",
                Ids.build("logging-profile-handler-periodic-rotating-file-table", "add"),
                "Periodic Handler", "suffix", "level", "formatter");
    }

    void removePeriodicHandler(Api<NamedNode> api) {
        removeResource(api, "periodic-rotating-file-handler=*", "Periodic Handler");
    }

    void savePeriodicHandler(Form<NamedNode> form, Map<String, Object> changedValues) {
        String name = form.getModel().getName();
        saveForm(changedValues, SELECTED_LOGGING_PROFILE_TEMPLATE.append("periodic-rotating-file-handler=*")
                .resolve(selectionAwareStatementContext, name), "Periodic Handler", name);
    }

    @Override
    public void updatePeriodicHandler(final List<NamedNode> items) {
        navigation.updateBadge("logging-profile-handler-periodic-rotating-file-item", items.size());
        periodicHandlerTable.api().clear().add(items).refresh(RefreshMode.RESET);
        periodicHandlerForm.clear();
    }


    // ------------------------------------------------------ periodic size handler

    void addPeriodicSizeHandler() {
        addFileHandlerResource("periodic-size-rotating-file-handler=*",
                Ids.build("logging-profile-handler-periodic-size-rotating-file-table", "add"),
                "Periodic Size Handler", "suffix", "level", "formatter", "rotate-size", "max-backup-index");
    }

    void removePeriodicSizeHandler(Api<NamedNode> api) {
        removeResource(api, "periodic-size-rotating-file-handler=*", "Periodic Size Handler");
    }

    void savePeriodicSizeHandler(Form<NamedNode> form, Map<String, Object> changedValues) {
        String name = form.getModel().getName();
        saveForm(changedValues, SELECTED_LOGGING_PROFILE_TEMPLATE.append("periodic-size-rotating-file-handler=*")
                .resolve(selectionAwareStatementContext, name), "Periodic Size Handler", name);
    }

    @Override
    public void updatePeriodicSizeHandler(final List<NamedNode> items) {
        navigation.updateBadge("logging-profile-handler-periodic-size-rotating-file-item", items.size());
        periodicSizeHandlerTable.api().clear().add(items).refresh(RefreshMode.RESET);
        periodicSizeHandlerForm.clear();
    }


    // ------------------------------------------------------ size handler

    void addSizeHandler() {
        addFileHandlerResource("size-rotating-file-handler=*",
                Ids.build("logging-profile-handler-size-rotating-file-table", "add"),
                "Size Handler", "suffix", "level", "formatter", "rotate-size", "max-backup-index");
    }

    void removeSizeHandler(Api<NamedNode> api) {
        removeResource(api, "size-rotating-file-handler=*", "Size Handler");
    }

    void saveSizeHandler(Form<NamedNode> form, Map<String, Object> changedValues) {
        String name = form.getModel().getName();
        saveForm(changedValues, SELECTED_LOGGING_PROFILE_TEMPLATE.append("size-rotating-file-handler=*")
                .resolve(selectionAwareStatementContext, name), "Size Handler", name);
    }

    @Override
    public void updateSizeHandler(final List<NamedNode> items) {
        navigation.updateBadge("logging-profile-handler-size-rotating-file-item", items.size());
        sizeHandlerTable.api().clear().add(items).refresh(RefreshMode.RESET);
        sizeHandlerForm.clear();
    }


    // ------------------------------------------------------ async handler

    void addAsyncHandler() {
        AddressTemplate metadataTemplate = LOGGING_PROFILE_TEMPLATE.append("async-handler=*");
        Metadata metadata = mbuiContext.metadataRegistry().lookup(metadataTemplate);
        AddressTemplate selectionTemplate = SELECTED_LOGGING_PROFILE_TEMPLATE.append("async-handler=*");

        AddResourceDialog dialog = new AddResourceDialog(Ids.build("logging-profile-handler-async-table", "add"),
                mbuiContext.resources().messages().addResourceTitle("Async Handler"),
                metadata, asList("level", "subhandlers", "queue-length", "overflow-action"),
                (name, modelNode) -> {
                    ResourceAddress address = selectionTemplate.resolve(selectionAwareStatementContext, name);
                    Operation operation = new Operation.Builder(ADD, address).payload(modelNode).build();
                    mbuiContext.dispatcher().execute(operation, result -> {
                        presenter.reload();
                        MessageEvent.fire(mbuiContext.eventBus(), Message.success(
                                mbuiContext.resources().messages().addResourceSuccess("Async Handler", name)));
                    });
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

    void removeAsyncHandler(Api<NamedNode> api) {
        removeResource(api, "async-handler=*", "Async Handler");
    }

    void saveAsyncHandler(Form<NamedNode> form, Map<String, Object> changedValues) {
        String name = form.getModel().getName();
        saveForm(changedValues, SELECTED_LOGGING_PROFILE_TEMPLATE.append("async-handler=*")
                .resolve(selectionAwareStatementContext, name), "Async Handler", name);
    }

    @Override
    public void updateAsyncHandler(final List<NamedNode> items) {
        navigation.updateBadge("logging-profile-handler-async-item", items.size());
        asyncHandlerTable.api().clear().add(items).refresh(RefreshMode.RESET);
        asyncHandlerForm.clear();
    }


    // ------------------------------------------------------ custom handler

    void addCustomHandler() {
        addResource("custom-handler=*", Ids.build("logging-profile-handler-custom-table", "add"),
                "Custom Handler", "level", "module", "class", "formatter");
    }

    void removeCustomHandler(Api<NamedNode> api) {
        removeResource(api, "custom-handler=*", "Custom Handler");
    }

    void saveCustomHandler(Form<NamedNode> form, Map<String, Object> changedValues) {
        String name = form.getModel().getName();
        saveForm(changedValues, SELECTED_LOGGING_PROFILE_TEMPLATE.append("custom-handler=*")
                .resolve(selectionAwareStatementContext, name), "Custom Handler", name);
    }

    @Override
    public void updateCustomHandler(final List<NamedNode> items) {
        navigation.updateBadge("logging-profile-handler-custom-item", items.size());
        customHandlerTable.api().clear().add(items).refresh(RefreshMode.RESET);
        customHandlerForm.clear();
    }


    // ------------------------------------------------------ syslog handler

    void addSyslogHandler() {
        addResource("syslog-handler=*", Ids.build("logging-profile-handler-syslog-table", "add"),
                "Syslog Handler", "level", "syslog-format", "hostname", "server-address", "port", "app-name",
                "facility");
    }

    void removeSyslogHandler(Api<NamedNode> api) {
        removeResource(api, "syslog-handler=*", "Syslog Handler");
    }

    void saveSyslogHandler(Form<NamedNode> form, Map<String, Object> changedValues) {
        String name = form.getModel().getName();
        saveForm(changedValues, SELECTED_LOGGING_PROFILE_TEMPLATE.append("syslog-handler=*")
                .resolve(selectionAwareStatementContext, name), "Syslog Handler", name);
    }

    @Override
    public void updateSyslogHandler(final List<NamedNode> items) {
        navigation.updateBadge("logging-profile-handler-syslog-item", items.size());
        syslogHandlerTable.api().clear().add(items).refresh(RefreshMode.RESET);
        syslogHandlerForm.clear();
    }


    // ------------------------------------------------------ custom formatter

    void addCustomFormatter() {
        addResource("custom-formatter=*", Ids.build("logging-profile-formatter-custom-table", "add"),
                "Custom Formatter");
    }

    void removeCustomFormatter(Api<NamedNode> api) {
        removeResource(api, "custom-formatter=*", "Custom Formatter");
    }

    void saveCustomFormatter(Form<NamedNode> form, Map<String, Object> changedValues) {
        String name = form.getModel().getName();
        saveForm(changedValues, SELECTED_LOGGING_PROFILE_TEMPLATE.append("custom-formatter=*")
                .resolve(selectionAwareStatementContext, name), "Custom Formatter", name);
    }

    @Override
    public void updateCustomFormatter(final List<NamedNode> items) {
        navigation.updateBadge("logging-profile-formatter-custom-item", items.size());
        customFormatterTable.api().clear().add(items).refresh(RefreshMode.RESET);
        customFormatterForm.clear();
    }


    // ------------------------------------------------------ pattern formatter

    void addPatternFormatter() {
        addResource("pattern-formatter=*", Ids.build("logging-profile-formatter-pattern-table", "add"),
                "Pattern Formatter");
    }

    void removePatternFormatter(Api<NamedNode> api) {
        removeResource(api, "pattern-formatter=*", "Pattern Formatter");
    }

    void savePatternFormatter(Form<NamedNode> form, Map<String, Object> changedValues) {
        String name = form.getModel().getName();
        saveForm(changedValues, SELECTED_LOGGING_PROFILE_TEMPLATE.append("pattern-formatter=*")
                .resolve(selectionAwareStatementContext, name), "Pattern Formatter", name);
    }

    @Override
    public void updatePatternFormatter(final List<NamedNode> items) {
        navigation.updateBadge("logging-profile-formatter-pattern-item", items.size());
        patternFormatterTable.api().clear().add(items).refresh(RefreshMode.RESET);
        patternFormatterForm.clear();
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
                    Operation operation = new Operation.Builder(ADD, address).payload(modelNode).build();
                    mbuiContext.dispatcher().execute(operation, result -> {
                        presenter.reload();
                        MessageEvent.fire(mbuiContext.eventBus(), Message.success(
                                mbuiContext.resources().messages().addResourceSuccess(type, name)));
                    });
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
                .addFromRequestProperties()
                .unboundFormItem(new org.jboss.hal.core.mbui.dialog.NameItem(), 0)
                .customFormItem("file", (attributeDescription) -> new FileFormItem())
                .unsorted();
        if (attributes != null) {
            builder.include(asList(attributes));
        }
        AddResourceDialog dialog = new AddResourceDialog(
                mbuiContext.resources().messages().addResourceTitle(type), builder.build(),
                (name, modelNode) -> {
                    ResourceAddress address = selectionTemplate.resolve(selectionAwareStatementContext, name);
                    Operation operation = new Operation.Builder(ADD, address).payload(modelNode).build();
                    mbuiContext.dispatcher().execute(operation, result -> {
                        presenter.reload();
                        MessageEvent.fire(mbuiContext.eventBus(), Message.success(
                                mbuiContext.resources().messages().addResourceSuccess(type, name)));
                    });
                });
        dialog.show();
    }

    private void removeResource(Api<NamedNode> api, String templateSuffix, String type) {
        //noinspection ConstantConditions
        String name = api.selectedRow().getName();
        AddressTemplate selectionTemplate = SELECTED_LOGGING_PROFILE_TEMPLATE.append(templateSuffix);
        DialogFactory.showConfirmation(
                mbuiContext.resources().messages().removeResourceConfirmationTitle(type),
                mbuiContext.resources().messages().removeResourceConfirmationQuestion(name),
                () -> {
                    ResourceAddress address = selectionTemplate.resolve(selectionAwareStatementContext, name);
                    Operation operation = new Operation.Builder(REMOVE, address).build();
                    mbuiContext.dispatcher().execute(operation, result -> {
                        presenter.reload();
                        MessageEvent.fire(mbuiContext.eventBus(), Message.success(
                                mbuiContext.resources().messages().removeResourceSuccess(type, name)));
                    });
                });
    }
}
