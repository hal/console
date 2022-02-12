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
package org.jboss.hal.client.runtime.managementoperations;

import java.util.ArrayList;
import java.util.List;

import org.jboss.gwt.elemento.core.builder.HtmlContentBuilder;
import org.jboss.hal.ballroom.Format;
import org.jboss.hal.ballroom.listview.ItemAction;
import org.jboss.hal.ballroom.listview.ItemDisplay;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Messages;
import org.jboss.hal.resources.Resources;

import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;

import elemental2.dom.HTMLElement;

import static org.jboss.gwt.elemento.core.Elements.collect;
import static org.jboss.gwt.elemento.core.Elements.div;
import static org.jboss.gwt.elemento.core.Elements.p;
import static org.jboss.gwt.elemento.core.Elements.span;
import static org.jboss.hal.dmr.ModelDescriptionConstants.CANCEL_OPERATION;
import static org.jboss.hal.resources.CSS.*;
import static org.jboss.hal.resources.Ids.ACTIVE_OPERATION;

class ManagementOperationsDisplay implements ItemDisplay<ManagementOperations> {

    private final ManagementOperations operation;
    private final ManagementOperationsPresenter presenter;
    private final Resources resources;

    ManagementOperationsDisplay(ManagementOperations item, ManagementOperationsPresenter presenter,
            Resources resources) {
        this.operation = item;
        this.presenter = presenter;
        this.resources = resources;
    }

    @Override
    public String getId() {
        return Ids.build(ACTIVE_OPERATION, String.valueOf(operation.getName()));
    }

    @Override
    public String getTitle() {
        String domainUuid = operation.getDomainUuid() != null ? " - " + resources.messages()
                .domainUuidLabel(operation.getDomainUuid()) : "";
        return "ID: " + operation.getName() + domainUuid;
    }

    @Override
    public HTMLElement getStatusElement() {
        HtmlContentBuilder<HTMLElement> builder = span()
                .css(listHalIconBig);
        if (operation.isNonProgressing()) {
            builder.css(pfIcon(errorCircleO), listHalIconError)
                    .title(resources.messages().nonProgressingOperation());
        } else {
            builder.css(pfIcon(ok), listHalIconSuccess);
        }
        return builder.element();
    }

    @Override
    @SuppressWarnings("HardCodedStringLiteral")
    public SafeHtml getDescriptionHtml() {
        SafeHtmlBuilder html = new SafeHtmlBuilder();
        html.append(resources.messages().operationLabel(operation.getOperation()));
        if (operation.getActiveAddressHost() != null) {
            html.append(resources.messages().operationHost(operation.getActiveAddressHost()));
        }
        if (operation.getActiveAddressServer() != null) {
            html.append(resources.messages().operationServer(operation.getActiveAddressServer()));
        }
        html.append(resources.messages().addressLabel(operation.getAddress()));
        html.append(resources.messages().callerThreadLabel(operation.getCallerThread()));
        html.append(resources.messages()
                .executionStatusLabel(operation.getExecutionStatus(),
                        operation.getExecutionStatusDescription()));
        return html.toSafeHtml();
    }

    @Override
    @SuppressWarnings("HardCodedStringLiteral")
    public Iterable<HTMLElement> getAdditionalInfoElements() {
        Messages messages = resources.messages();
        return collect()
                .add(div().css(halConfChangesAdditionalInfo)
                        .add(p().css(textRight).innerHtml(new SafeHtmlBuilder()
                                .append(messages.accessMechanismLabel(operation.getAccessMechanism()))
                                .append(messages
                                        .runningTimeLabel(
                                                Format.humanReadableDurationNanoseconds(operation.getRunningTime())))
                                .append(messages
                                        .exclusiveRunningTimeLabel(Format.humanReadableDurationNanoseconds(
                                                operation.getExclusiveRunningTime())))
                                .append(messages.cancelledLabel(operation.isCancelled()))
                                .append(messages.domainRolloutLabel(operation.isDomainRollout()))
                                .toSafeHtml())))
                .elements();
    }

    @Override
    public List<ItemAction<ManagementOperations>> actions() {
        List<ItemAction<ManagementOperations>> actions = new ArrayList<>();
        String id = Ids.build(ACTIVE_OPERATION, operation.getName(), CANCEL_OPERATION);
        actions.add(new ItemAction<>(id, resources.constants().cancel(), presenter::cancel));
        return actions;
    }

}
