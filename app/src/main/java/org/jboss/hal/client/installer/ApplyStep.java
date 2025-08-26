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
package org.jboss.hal.client.installer;

import org.jboss.hal.ballroom.wizard.WizardStep;
import org.jboss.hal.dmr.Operation;
import org.jboss.hal.dmr.ResourceAddress;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.resources.Resources;

import com.google.gwt.safehtml.shared.SafeHtml;

import elemental2.dom.HTMLElement;
import elemental2.promise.Promise;

import static org.jboss.elemento.Elements.div;
import static org.jboss.hal.client.installer.AddressTemplates.ROOT_TEMPLATE;
import static org.jboss.hal.core.runtime.TimeoutHandler.repeatUntilTimeout;
import static org.jboss.hal.dmr.ModelDescriptionConstants.PERFORM_INSTALLATION;
import static org.jboss.hal.dmr.ModelDescriptionConstants.READ_RESOURCE_OPERATION;
import static org.jboss.hal.dmr.ModelDescriptionConstants.SHUTDOWN;

class ApplyStep extends WizardStep<UpdateManagerContext, UpdateState> {

    private final String progressTitle;
    private final SafeHtml progressMessage;
    private final String successTitle;
    private final SafeHtml successMessage;
    private final SafeHtml errorMessage;
    private final Dispatcher dispatcher;
    private final StatementContext statementContext;
    private final Resources resources;
    private final HTMLElement root;

    ApplyStep(final String title,
            final String progressTitle,
            final SafeHtml progressMessage,
            final String successTitle,
            final SafeHtml successMessage,
            final SafeHtml errorMessage,
            final Dispatcher dispatcher,
            final StatementContext statementContext,
            final Resources resources) {
        super(title, true);
        this.progressTitle = progressTitle;
        this.progressMessage = progressMessage;
        this.successTitle = successTitle;
        this.successMessage = successMessage;
        this.errorMessage = errorMessage;
        this.statementContext = statementContext;
        this.dispatcher = dispatcher;
        this.resources = resources;
        this.root = div().element();
    }

    @Override
    public HTMLElement element() {
        return root;
    }

    @Override
    protected Promise<UpdateManagerContext> onShowAndWait(final UpdateManagerContext context) {
        wizard().showProgress(progressTitle, progressMessage);

        ResourceAddress root = ROOT_TEMPLATE.resolve(statementContext);
        Operation shutdown = new Operation.Builder(root, SHUTDOWN)
                .param(PERFORM_INSTALLATION, true)
                .build();
        Operation ping = new Operation.Builder(root, READ_RESOURCE_OPERATION).build();
        return dispatcher.execute(shutdown)
                .then(___ -> repeatUntilTimeout(dispatcher, ping, Timeouts.APPLY * 1_000))
                .then(status -> {
                    wizard().showSuccess(successTitle, successMessage);
                    return Promise.resolve(context);
                })
                .catch_(error -> {
                    wizard().showError(resources.constants().error(), errorMessage, false);
                    return Promise.reject(error);
                });
    }
}
