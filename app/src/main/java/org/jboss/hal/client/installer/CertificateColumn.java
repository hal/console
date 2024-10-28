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

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.jboss.hal.ballroom.dialog.DialogFactory;
import org.jboss.hal.core.finder.ColumnAction;
import org.jboss.hal.core.finder.ColumnActionFactory;
import org.jboss.hal.core.finder.Finder;
import org.jboss.hal.core.finder.FinderColumn;
import org.jboss.hal.core.finder.ItemAction;
import org.jboss.hal.core.finder.ItemDisplay;
import org.jboss.hal.dmr.Operation;
import org.jboss.hal.dmr.ResourceAddress;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.meta.security.Constraint;
import org.jboss.hal.resources.CSS;
import org.jboss.hal.resources.Icons;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.resources.UIConstants;
import org.jboss.hal.spi.Callback;
import org.jboss.hal.spi.Column;
import org.jboss.hal.spi.Message;
import org.jboss.hal.spi.MessageEvent;
import org.jboss.hal.spi.Requires;

import com.google.web.bindery.event.shared.EventBus;

import elemental2.dom.HTMLElement;
import elemental2.promise.Promise;

import static java.util.stream.Collectors.toList;

import static org.jboss.hal.client.installer.AddressTemplates.INSTALLER_ADDRESS;
import static org.jboss.hal.client.installer.AddressTemplates.INSTALLER_TEMPLATE;
import static org.jboss.hal.core.finder.FinderColumn.RefreshMode.CLEAR_SELECTION;
import static org.jboss.hal.dmr.ModelDescriptionConstants.CERTIFICATES;
import static org.jboss.hal.dmr.ModelDescriptionConstants.CERTIFICATE_REMOVE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.INCLUDE_RUNTIME;
import static org.jboss.hal.dmr.ModelDescriptionConstants.KEY_ID;
import static org.jboss.hal.dmr.ModelDescriptionConstants.READ_RESOURCE_OPERATION;
import static org.jboss.hal.dmr.ModelDescriptionConstants.WRITE_ATTRIBUTE_OPERATION;

@Column(Ids.UPDATE_MANAGER_CERTIFICATE)
@Requires(INSTALLER_ADDRESS)
public class CertificateColumn extends FinderColumn<CertificateInfo> {

    private final Resources resources;
    private final EventBus eventBus;
    private final Dispatcher dispatcher;
    private final StatementContext statementContext;

    @Inject
    public CertificateColumn(final Finder finder,
            final EventBus eventBus,
            final ColumnActionFactory columnActionFactory,
            final StatementContext statementContext,
            final Dispatcher dispatcher,
            final Resources resources) {
        super(new Builder<CertificateInfo>(finder, Ids.UPDATE_MANAGER_CERTIFICATE, Names.CERTIFICATES)
                .onPreview((CertificateInfo c) -> new CertificatePreview(c))
                .showCount()
                .withFilter());

        this.eventBus = eventBus;
        this.dispatcher = dispatcher;
        this.statementContext = statementContext;
        this.resources = resources;

        setItemsProvider(context -> {
            ResourceAddress address = INSTALLER_TEMPLATE.resolve(statementContext);
            Operation operation = new Operation.Builder(address, READ_RESOURCE_OPERATION)
                    .param(INCLUDE_RUNTIME, true)
                    .build();
            return dispatcher.execute(operation).then(result -> {
                // noinspection Convert2MethodRef
                List<CertificateInfo> certificates = result.get(CERTIFICATES).asList().stream()
                        .map(node -> new CertificateInfo(node))
                        .collect(toList());
                return Promise.resolve(certificates);
            });
        });

        setItemRenderer(item -> new ItemDisplay<CertificateInfo>() {
            @Override
            public String getId() {
                return Ids.asId(item.getKeyID());
            }

            @Override
            public String getTitle() {
                return item.getDescription();
            }

            @Override
            public HTMLElement getIcon() {
                if (item.getStatus().equals(CertificateInfo.TRUSTED)) {
                    return Icons.ok();
                } else {
                    return Icons.warning();
                }
            }

            @Override
            public HTMLElement element() {
                return ItemDisplay.withSubtitle(item.getDescription(), item.getKeyID());
            }

            @Override
            public List<ItemAction<CertificateInfo>> actions() {
                List<ItemAction<CertificateInfo>> actions = new ArrayList<>();
                actions.add(new ItemAction.Builder<CertificateInfo>()
                        .title(resources.constants().remove())
                        .handler(itm -> remove(itm))
                        .constraint(Constraint.executable(INSTALLER_TEMPLATE, WRITE_ATTRIBUTE_OPERATION))
                        .build());
                return actions;
            }
        });

        addColumnAction(new ColumnAction.Builder<CertificateInfo>(Ids.UPDATE_MANAGER_CERTIFICATE_ADD)
                .element(columnActionFactory.addButton(resources.constants().importCertificate(),
                        CSS.pfIcon(UIConstants.ADD_CIRCLE_O)))
                .handler(column -> add())
                .constraint(Constraint.executable(INSTALLER_TEMPLATE, WRITE_ATTRIBUTE_OPERATION))
                .build());
        addColumnAction(columnActionFactory.refresh(Ids.UPDATE_MANAGER_CERTIFICATE_REFRESH));
    }

    private void add() {
        new ImportComponentCertificateWizard(dispatcher, statementContext, resources).show(this);
    }

    private void remove(CertificateInfo certificate) {
        DialogFactory.showConfirmation(resources.constants().removeComponentCertificate(),
                resources.messages().removeUpdateCertificateQuestion(certificate.getKeyID()),
                () -> removeCertificate(certificate.getKeyID(), () -> refresh(CLEAR_SELECTION)));
    }

    void removeCertificate(final String name, final Callback callback) {
        Operation operation = new Operation.Builder(INSTALLER_TEMPLATE.resolve(statementContext), CERTIFICATE_REMOVE)
                .param(KEY_ID, name)
                .build();
        dispatcher.execute(operation)
                .then(result -> {
                    MessageEvent.fire(eventBus, Message.success(resources.messages().removeResourceSuccess(Names.CERTIFICATE,
                            name)));
                    return null;
                })
                .catch_(failure -> {
                    MessageEvent.fire(eventBus,
                            Message.error(resources.messages().lastOperationFailed(), String.valueOf(failure)));
                    return null;
                })
                .finally_(callback::execute);
    }
}
