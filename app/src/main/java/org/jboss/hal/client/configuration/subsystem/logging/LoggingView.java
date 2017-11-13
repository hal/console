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

import elemental2.dom.HTMLElement;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.hal.ballroom.EmptyState;
import org.jboss.hal.ballroom.VerticalNavigation;
import org.jboss.hal.ballroom.autocomplete.ReadChildrenAutoComplete;
import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.ballroom.form.SuggestHandler;
import org.jboss.hal.ballroom.table.Table;
import org.jboss.hal.core.mbui.MbuiContext;
import org.jboss.hal.core.mbui.MbuiViewImpl;
import org.jboss.hal.core.mbui.dialog.AddResourceDialog;
import org.jboss.hal.core.mbui.form.ModelNodeForm;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.NamedNode;
import org.jboss.hal.dmr.Operation;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.resources.Names;
import org.jboss.hal.spi.MbuiElement;
import org.jboss.hal.spi.MbuiView;
import org.jboss.hal.spi.Message;
import org.jboss.hal.spi.MessageEvent;

import static elemental2.dom.DomGlobal.document;
import static java.util.Arrays.asList;
import static org.jboss.hal.client.configuration.subsystem.logging.AddressTemplates.*;
import static org.jboss.hal.dmr.ModelDescriptionConstants.ADD;
import static org.jboss.hal.dmr.ModelDescriptionConstants.HANDLERS;
import static org.jboss.hal.dmr.ModelDescriptionConstants.LEVEL;
import static org.jboss.hal.resources.CSS.marginTopLarge;

@MbuiView
@SuppressWarnings({"DuplicateStringLiteralInspection", "HardCodedStringLiteral", "WeakerAccess"})
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
    @MbuiElement("logging-handler-syslog-table") Table<NamedNode> syslogHandlerTable;
    @MbuiElement("logging-handler-syslog-form") Form<NamedNode> syslogHandlerForm;
    @MbuiElement("logging-formatter-custom-table") Table<NamedNode> customFormatterTable;
    @MbuiElement("logging-formatter-custom-form") Form<NamedNode> customFormatterForm;
    @MbuiElement("logging-formatter-pattern-table") Table<NamedNode> patternFormatterTable;
    @MbuiElement("logging-formatter-pattern-form") Form<NamedNode> patternFormatterForm;

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
        noRootLogger.asElement().classList.add(marginTopLarge);

        // hack which relies on the element hierarchy given in the template. will break if you change that hierarchy.
        rootLoggerForm.asElement().parentNode.appendChild(noRootLogger.asElement());
        rootLoggerVisibility(true);
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
        Elements.setVisible((HTMLElement) document.getElementById("logging-root-logger-header"), visible);
        Elements.setVisible((HTMLElement) document.getElementById("logging-root-logger-description"),
                visible);
        Elements.setVisible(rootLoggerForm.asElement(), visible);
        Elements.setVisible(noRootLogger.asElement(), !visible);
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
                            ROOT_LOGGER_TEMPLATE.resolve(mbuiContext.statementContext()), ADD
                    )
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
}
