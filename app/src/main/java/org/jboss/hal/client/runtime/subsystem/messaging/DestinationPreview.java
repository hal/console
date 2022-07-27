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
package org.jboss.hal.client.runtime.subsystem.messaging;

import org.jboss.gwt.elemento.core.Elements;
import org.jboss.gwt.elemento.core.builder.HtmlContent;
import org.jboss.hal.ballroom.EmptyState;
import org.jboss.hal.ballroom.LabelBuilder;
import org.jboss.hal.core.finder.FinderPath;
import org.jboss.hal.core.finder.FinderPathFactory;
import org.jboss.hal.core.finder.PreviewAttributes;
import org.jboss.hal.core.finder.PreviewAttributes.PreviewAttribute;
import org.jboss.hal.core.finder.PreviewAttributes.PreviewAttributeFunction;
import org.jboss.hal.core.finder.PreviewContent;
import org.jboss.hal.core.mvp.Places;
import org.jboss.hal.dmr.Composite;
import org.jboss.hal.dmr.CompositeResult;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.ModelNodeHelper;
import org.jboss.hal.dmr.Operation;
import org.jboss.hal.dmr.ResourceAddress;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.meta.security.Constraint;
import org.jboss.hal.meta.token.NameTokens;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;

import com.gwtplatform.mvp.shared.proxy.PlaceRequest;

import elemental2.dom.HTMLElement;

import static elemental2.dom.DomGlobal.document;
import static java.util.stream.Collectors.joining;
import static org.jboss.gwt.elemento.core.Elements.a;
import static org.jboss.gwt.elemento.core.Elements.collect;
import static org.jboss.gwt.elemento.core.Elements.section;
import static org.jboss.gwt.elemento.core.Elements.setVisible;
import static org.jboss.gwt.elemento.core.Elements.span;
import static org.jboss.hal.client.runtime.subsystem.messaging.AddressTemplates.MESSAGING_SERVER_TEMPLATE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.CONSUMER_COUNT;
import static org.jboss.hal.dmr.ModelDescriptionConstants.DEAD_LETTER_ADDRESS;
import static org.jboss.hal.dmr.ModelDescriptionConstants.DELIVERING_COUNT;
import static org.jboss.hal.dmr.ModelDescriptionConstants.DURABLE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.DURABLE_MESSAGE_COUNT;
import static org.jboss.hal.dmr.ModelDescriptionConstants.DURABLE_SUBSCRIPTION_COUNT;
import static org.jboss.hal.dmr.ModelDescriptionConstants.ENTRIES;
import static org.jboss.hal.dmr.ModelDescriptionConstants.EXPIRY_ADDRESS;
import static org.jboss.hal.dmr.ModelDescriptionConstants.INCLUDE_RUNTIME;
import static org.jboss.hal.dmr.ModelDescriptionConstants.MESSAGES_ADDED;
import static org.jboss.hal.dmr.ModelDescriptionConstants.MESSAGE_COUNT;
import static org.jboss.hal.dmr.ModelDescriptionConstants.NAME;
import static org.jboss.hal.dmr.ModelDescriptionConstants.NON_DURABLE_MESSAGE_COUNT;
import static org.jboss.hal.dmr.ModelDescriptionConstants.NON_DURABLE_SUBSCRIPTION_COUNT;
import static org.jboss.hal.dmr.ModelDescriptionConstants.PAUSED;
import static org.jboss.hal.dmr.ModelDescriptionConstants.QUEUE_ADDRESS;
import static org.jboss.hal.dmr.ModelDescriptionConstants.READ_ATTRIBUTE_OPERATION;
import static org.jboss.hal.dmr.ModelDescriptionConstants.READ_RESOURCE_OPERATION;
import static org.jboss.hal.dmr.ModelDescriptionConstants.RESOLVE_EXPRESSIONS;
import static org.jboss.hal.dmr.ModelDescriptionConstants.RESULT;
import static org.jboss.hal.dmr.ModelDescriptionConstants.SCHEDULED_COUNT;
import static org.jboss.hal.dmr.ModelDescriptionConstants.STATISTICS_ENABLED;
import static org.jboss.hal.dmr.ModelDescriptionConstants.SUBSCRIPTION_COUNT;
import static org.jboss.hal.dmr.ModelDescriptionConstants.TEMPORARY;
import static org.jboss.hal.dmr.ModelDescriptionConstants.TOPIC_ADDRESS;
import static org.jboss.hal.dmr.ModelDescriptionConstants.VALUE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.WRITE_ATTRIBUTE_OPERATION;
import static org.jboss.hal.dmr.ModelNodeHelper.failSafeBoolean;
import static org.jboss.hal.resources.CSS.fontAwesome;
import static org.jboss.hal.resources.CSS.marginRight5;
import static org.jboss.hal.resources.Icons.flag;

class DestinationPreview extends PreviewContent<Destination> {

    private final Dispatcher dispatcher;
    private final StatementContext statementContext;
    private final ResourceAddress address;
    private final EmptyState noStatistics;
    private final HTMLElement statSection;
    private final PreviewAttributes<Destination> attributes;
    private final PreviewAttributes<Destination> messages;
    private final PreviewAttributes<Destination> subscriptions;

    DestinationPreview(Destination destination, FinderPathFactory finderPathFactory, Places places,
            Dispatcher dispatcher, StatementContext statementContext, Resources resources) {
        super(destination.getName(), destination.type.type);
        this.dispatcher = dispatcher;
        this.statementContext = statementContext;
        this.address = destination.getAddress();

        AddressTemplate serverTemplate = MESSAGING_SERVER_TEMPLATE.replaceWildcards(getServerName(destination));
        noStatistics = new EmptyState.Builder(Ids.MESSAGING_STATISTICS_DISABLED,
                resources.constants().statisticsDisabledHeader())
                .description(resources.messages().messagingServerStatisticsDisabled(getServerName(destination)))
                .icon(fontAwesome("line-chart"))
                .primaryAction(resources.constants().enableStatistics(), () -> enableStatistics(destination),
                        Constraint.writable(serverTemplate, STATISTICS_ENABLED))
                .build();
        Elements.setVisible(noStatistics.element(), false);

        getHeaderContainer().appendChild(refreshLink(() -> update(destination)));
        if (destination.fromDeployment()) {
            FinderPath path = finderPathFactory.deployment(destination.getDeployment());
            PlaceRequest placeRequest = places.finderPlace(NameTokens.DEPLOYMENTS, path).build();
            Elements.removeChildrenFrom(getLeadElement());
            getLeadElement().appendChild(
                    document.createTextNode(destination.type.type + " @ "));
            getLeadElement().appendChild(a(places.historyToken(placeRequest))
                    .textContent(destination.getPath())
                    .title(resources.messages().goTo(Names.DEPLOYMENTS)).element());
        }

        PreviewAttributeFunction<Destination> jndiNamesFn = model -> {
            String jndiNames = ModelNodeHelper.failSafeList(model, ENTRIES).stream()
                    .map(ModelNode::asString)
                    .collect(joining(", "));
            return new PreviewAttribute(resources.constants().jndiNames(), jndiNames);
        };
        attributes = new PreviewAttributes<>(destination);
        messages = new PreviewAttributes<>(destination, resources.constants().messages());
        subscriptions = new PreviewAttributes<>(destination, resources.constants().subscriptions());

        LabelBuilder labelBuilder = new LabelBuilder();
        if (destination.type == Destination.Type.JMS_QUEUE || destination.type == Destination.Type.QUEUE) {
            attributes.append(QUEUE_ADDRESS)
                    .append(jndiNamesFn)
                    .append(EXPIRY_ADDRESS)
                    .append(DEAD_LETTER_ADDRESS)
                    .append(PAUSED)
                    .append(model -> {
                        String label = labelBuilder.label(DURABLE) + ", " + labelBuilder.label(TEMPORARY);
                        Iterable<HTMLElement> elements = collect()
                                .add(span()
                                        .title(labelBuilder.label(DURABLE))
                                        .css(flag(failSafeBoolean(model, DURABLE)), marginRight5))
                                .add(span()
                                        .title(labelBuilder.label(TEMPORARY))
                                        .css(flag(failSafeBoolean(model, TEMPORARY))))
                                .elements();
                        return new PreviewAttribute(label, elements);
                    });
            messages.append(MESSAGES_ADDED)
                    .append(MESSAGE_COUNT)
                    .append(DELIVERING_COUNT)
                    .append(SCHEDULED_COUNT)
                    .append(CONSUMER_COUNT);

        } else if (destination.type == Destination.Type.JMS_TOPIC) {
            attributes.append(TOPIC_ADDRESS)
                    .append(jndiNamesFn)
                    .append(PAUSED)
                    .append(TEMPORARY);
            messages.append(MESSAGE_COUNT)
                    .append(DURABLE_MESSAGE_COUNT)
                    .append(NON_DURABLE_MESSAGE_COUNT)
                    .append(MESSAGES_ADDED)
                    .append(DELIVERING_COUNT);
            subscriptions.append(SUBSCRIPTION_COUNT)
                    .append(DURABLE_SUBSCRIPTION_COUNT)
                    .append(NON_DURABLE_SUBSCRIPTION_COUNT);
        }

        previewBuilder().addAll(attributes)
                .add(noStatistics);
        HtmlContent sectionContent = section()
                .addAll(messages);
        if (destination.type == Destination.Type.JMS_TOPIC) {
            sectionContent.addAll(subscriptions);
        }
        previewBuilder().add(statSection = sectionContent
                .element());
    }

    @Override
    public void update(Destination item) {
        Operation opDestination = new Operation.Builder(address, READ_RESOURCE_OPERATION)
                .param(INCLUDE_RUNTIME, true)
                .build();
        ResourceAddress serverAddress = MESSAGING_SERVER_TEMPLATE.resolve(statementContext, getServerName(item));
        Operation opStatistics = new Operation.Builder(serverAddress, READ_ATTRIBUTE_OPERATION)
                .param(NAME, STATISTICS_ENABLED)
                .param(RESOLVE_EXPRESSIONS, true)
                .build();

        dispatcher.execute(new Composite(opDestination, opStatistics), (CompositeResult compositeResult) -> {
            ModelNode destinationResult = compositeResult.step(0).get(RESULT);
            ModelNode statisticsResult = compositeResult.step(1).get(RESULT);

            boolean statsAvailable = destinationResult.get(MESSAGES_ADDED).asLong() > 0;
            boolean statsEnabled = statisticsResult.asBoolean(statsAvailable);

            if (statsEnabled) {
                Destination update = new Destination(item.getAddress(), item);
                attributes.refresh(update);
                messages.refresh(update);
                if (update.type == Destination.Type.JMS_TOPIC) {
                    subscriptions.refresh(item);
                }
            }

            setVisible(noStatistics, !statsEnabled);
            setVisible(statSection, statsEnabled);
        });
    }

    private void enableStatistics(Destination item) {
        String serverName = getServerName(item);
        ResourceAddress serverAddress = MESSAGING_SERVER_TEMPLATE.resolve(statementContext, serverName);
        Operation operation = new Operation.Builder(serverAddress, WRITE_ATTRIBUTE_OPERATION)
                .param(NAME, STATISTICS_ENABLED)
                .param(VALUE, true)
                .build();
        dispatcher.execute(operation, result -> update(item));
    }

    private String getServerName(Destination item) {
        return item.getAddress().getParent().lastValue();
    }
}
