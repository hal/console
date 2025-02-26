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

import org.jboss.hal.ballroom.wizard.Wizard;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.resources.Resources;

import static org.jboss.hal.client.installer.ImportComponentCertificateState.CONFIRM_CERTIFICATE;
import static org.jboss.hal.client.installer.ImportComponentCertificateState.UPLOAD_CERTIFICATE;
import static org.jboss.hal.core.finder.FinderColumn.RefreshMode.CLEAR_SELECTION;

class ImportComponentCertificateWizard {
    private final Dispatcher dispatcher;
    private final StatementContext statementContext;
    private final Resources resources;
    private final ImportComponentCertificateContext context;

    ImportComponentCertificateWizard(
            final Dispatcher dispatcher,
            final StatementContext statementContext,
            final Resources resources) {
        this.dispatcher = dispatcher;
        this.statementContext = statementContext;
        this.resources = resources;
        this.context = new ImportComponentCertificateContext();
    }

    void show(CertificateColumn column) {
        Wizard.Builder<ImportComponentCertificateContext, ImportComponentCertificateState> builder = new Wizard.Builder<>(
                resources.constants().importCertificate(), context);

        builder.stayOpenAfterFinish()
                .addStep(UPLOAD_CERTIFICATE, new UploadComponentCertificateStep(
                        dispatcher,
                        statementContext,
                        resources))
                .addStep(CONFIRM_CERTIFICATE, new ConfirmComponentCertificateStep(
                        dispatcher,
                        statementContext,
                        resources));

        builder.onBack((ctx, currentState) -> {
            ImportComponentCertificateState previous = null;
            switch (currentState) {
                case UPLOAD_CERTIFICATE:
                    break;
                case CONFIRM_CERTIFICATE:
                    previous = UPLOAD_CERTIFICATE;
                    break;
            }
            return previous;
        });

        builder.onNext((ctx, currentState) -> {
            ImportComponentCertificateState next = null;
            switch (currentState) {
                case UPLOAD_CERTIFICATE:
                    next = CONFIRM_CERTIFICATE;
                    break;
                case CONFIRM_CERTIFICATE:
                    break;
            }
            return next;
        });

        builder.onFinish((wizard, ctx) -> {
            if (column != null) {
                column.refresh(CLEAR_SELECTION);
            }
            wizard.showSuccess(resources.constants().componentCertificateImported(),
                    resources.messages().componentCertificateImportedDescription());
        });

        builder.build().show();
    }
}
