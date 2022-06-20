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
package org.jboss.hal.client.runtime.subsystem.ejb;

import java.util.Date;
import java.util.List;

import org.jboss.elemento.Elements;
import org.jboss.elemento.HtmlContent;
import org.jboss.hal.ballroom.EmptyState;
import org.jboss.hal.ballroom.Format;
import org.jboss.hal.ballroom.LabelBuilder;
import org.jboss.hal.ballroom.ProgressElement;
import org.jboss.hal.ballroom.chart.Utilization;
import org.jboss.hal.core.finder.FinderPath;
import org.jboss.hal.core.finder.FinderPathFactory;
import org.jboss.hal.core.finder.PreviewAttributes;
import org.jboss.hal.core.finder.PreviewAttributes.PreviewAttribute;
import org.jboss.hal.core.finder.PreviewContent;
import org.jboss.hal.core.mvp.Places;
import org.jboss.hal.dmr.Composite;
import org.jboss.hal.dmr.CompositeResult;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.ModelNodeHelper;
import org.jboss.hal.dmr.Operation;
import org.jboss.hal.dmr.ResourceAddress;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.meta.security.Constraint;
import org.jboss.hal.meta.token.NameTokens;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;

import com.gwtplatform.mvp.shared.proxy.PlaceRequest;

import elemental2.dom.CSSProperties.MarginBottomUnionType;
import elemental2.dom.HTMLElement;

import static elemental2.dom.DomGlobal.clearInterval;
import static elemental2.dom.DomGlobal.document;
import static elemental2.dom.DomGlobal.setInterval;
import static java.lang.Math.round;
import static java.util.Arrays.asList;
import static org.jboss.elemento.Elements.a;
import static org.jboss.elemento.Elements.h;
import static org.jboss.elemento.Elements.section;
import static org.jboss.elemento.Elements.span;
import static org.jboss.hal.ballroom.ProgressElement.Label.INLINE;
import static org.jboss.hal.ballroom.ProgressElement.Size.NORMAL;
import static org.jboss.hal.client.runtime.subsystem.ejb.AddressTemplates.EJB3_SUBSYSTEM_TEMPLATE;
import static org.jboss.hal.client.runtime.subsystem.ejb.EjbNode.Type.MDB;
import static org.jboss.hal.client.runtime.subsystem.ejb.EjbNode.Type.STATEFUL;
import static org.jboss.hal.client.runtime.subsystem.ejb.EjbNode.Type.STATELESS;
import static org.jboss.hal.dmr.ModelDescriptionConstants.CACHE_SIZE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.COMPONENT_CLASS_NAME;
import static org.jboss.hal.dmr.ModelDescriptionConstants.DELIVERY_ACTIVE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.EXECUTION_TIME;
import static org.jboss.hal.dmr.ModelDescriptionConstants.INCLUDE_RUNTIME;
import static org.jboss.hal.dmr.ModelDescriptionConstants.INVOCATIONS;
import static org.jboss.hal.dmr.ModelDescriptionConstants.NAME;
import static org.jboss.hal.dmr.ModelDescriptionConstants.NEXT_TIMEOUT;
import static org.jboss.hal.dmr.ModelDescriptionConstants.PASSIVATED_COUNT;
import static org.jboss.hal.dmr.ModelDescriptionConstants.POOL_CURRENT_SIZE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.POOL_MAX_SIZE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.READ_ATTRIBUTE_OPERATION;
import static org.jboss.hal.dmr.ModelDescriptionConstants.READ_RESOURCE_OPERATION;
import static org.jboss.hal.dmr.ModelDescriptionConstants.RECURSIVE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.RESOLVE_EXPRESSIONS;
import static org.jboss.hal.dmr.ModelDescriptionConstants.RESULT;
import static org.jboss.hal.dmr.ModelDescriptionConstants.STATISTICS_ENABLED;
import static org.jboss.hal.dmr.ModelDescriptionConstants.TIMERS;
import static org.jboss.hal.dmr.ModelDescriptionConstants.TIME_REMAINING;
import static org.jboss.hal.dmr.ModelDescriptionConstants.TOTAL_SIZE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.VALUE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.WRITE_ATTRIBUTE_OPERATION;
import static org.jboss.hal.resources.CSS.fontAwesome;

class EjbPreview extends PreviewContent<EjbNode> {

    private static double intervalHandle = 0; // one handle for all previews!

    private final Dispatcher dispatcher;
    private final StatementContext statementContext;
    private final ResourceAddress address;
    private final LabelBuilder labelBuilder;
    private final EmptyState noStatistics;
    private final PreviewAttributes<EjbNode> attributes;
    private final HTMLElement statefulSection;
    private final PreviewAttributes<EjbNode> statefulAttributes;
    private final HTMLElement poolSection;
    private final Utilization poolUtilization;
    private final HTMLElement timerSection;
    private PreviewAttributes<ModelNode> timer;
    private HTMLElement nextTimeoutElement;
    private ProgressElement remainingElement;
    private int maxRemaining;

    EjbPreview(EjbNode ejb, FinderPathFactory finderPathFactory, Places places, Dispatcher dispatcher,
            StatementContext statementContext, Resources resources) {
        super(ejb.getName(), ejb.type.type);
        this.dispatcher = dispatcher;
        this.statementContext = statementContext;
        this.address = ejb.getAddress();
        this.labelBuilder = new LabelBuilder();
        this.maxRemaining = 0;

        noStatistics = new EmptyState.Builder(Ids.EJB3_DEPLOYMENT_STATISTICS_DISABLED,
                resources.constants().statisticsDisabledHeader())
                .description(resources.messages().statisticsDisabled(Names.EJB3))
                .icon(fontAwesome("line-chart"))
                .primaryAction(resources.constants().enableStatistics(), () -> enableStatistics(ejb),
                        Constraint.writable(EJB3_SUBSYSTEM_TEMPLATE, STATISTICS_ENABLED))
                .build();

        getHeaderContainer().appendChild(refreshLink(() -> update(null)));
        FinderPath path = finderPathFactory.deployment(ejb.getDeployment());
        PlaceRequest placeRequest = places.finderPlace(NameTokens.DEPLOYMENTS, path).build();
        Elements.removeChildrenFrom(getLeadElement());
        getLeadElement().appendChild(
                document.createTextNode(ejb.type.type + " @ "));
        getLeadElement().appendChild(a(places.historyToken(placeRequest))
                .textContent(ejb.getPath())
                .title(resources.messages().goTo(Names.DEPLOYMENTS)).element());

        attributes = new PreviewAttributes<>(ejb,
                asList(COMPONENT_CLASS_NAME, INVOCATIONS, EXECUTION_TIME, DELIVERY_ACTIVE));
        previewBuilder().addAll(attributes);

        previewBuilder().add(noStatistics);
        Elements.setVisible(noStatistics, false);

        poolSection = section().add(h(2, Names.POOL))
                .add(poolUtilization = new Utilization(resources.constants().size(), resources.constants().instances(),
                        false, true))
                .element();
        statefulSection = section().addAll(statefulAttributes = new PreviewAttributes<>(ejb, STATEFUL.type,
                asList(CACHE_SIZE, PASSIVATED_COUNT, TOTAL_SIZE))).element();
        previewBuilder().addAll(poolSection, statefulSection);

        ModelNode firstTimer = firstTimer(ejb);
        HtmlContent timerContent = section();
        if (firstTimer.isDefined()) {
            timer = new PreviewAttributes<>(firstTimer, Names.TIMER)
                    .append(t -> {
                        String nextTimeout = Format.mediumDateTime(new Date(t.get(NEXT_TIMEOUT).asLong()));
                        nextTimeoutElement = span().textContent(nextTimeout).element();
                        return new PreviewAttribute(labelBuilder.label(NEXT_TIMEOUT), nextTimeoutElement);
                    })
                    .append(t -> {
                        maxRemaining = (int) round(t.get(TIME_REMAINING).asLong() / 1000.0);
                        remainingElement = new ProgressElement(NORMAL, INLINE, true);
                        remainingElement.reset(maxRemaining);
                        remainingElement.element().style.marginBottom = MarginBottomUnionType.of(0);
                        return new PreviewAttribute(labelBuilder.label(TIME_REMAINING), remainingElement.element());
                    });
            timerContent.addAll(timer);
        }
        previewBuilder().add(timerSection = timerContent.element());
    }

    @Override
    public void detach() {
        super.detach();
        clearInterval(intervalHandle);
    }

    @Override
    public void update(EjbNode item) {
        Operation opNode = new Operation.Builder(address, READ_RESOURCE_OPERATION)
                .param(INCLUDE_RUNTIME, true)
                .param(RECURSIVE, true)
                .build();
        ResourceAddress configurationAddress = EJB3_SUBSYSTEM_TEMPLATE.resolve(statementContext);
        Operation opStatistics = new Operation.Builder(configurationAddress, READ_ATTRIBUTE_OPERATION)
                .param(NAME, STATISTICS_ENABLED)
                .param(RESOLVE_EXPRESSIONS, true)
                .build();
        dispatcher.execute(new Composite(opNode, opStatistics), (CompositeResult compositeResult) -> {
            ModelNode nodeResult = compositeResult.step(0).get(RESULT);
            ModelNode statisticsResult = compositeResult.step(1).get(RESULT);

            boolean statsAvailable = nodeResult.get(INVOCATIONS).asLong() > 0;
            boolean statsEnabled = statisticsResult.asBoolean(statsAvailable);

            updateInternal(new EjbNode(address, nodeResult), statsEnabled);
        });
    }

    private void updateInternal(EjbNode ejb, boolean statsEnabled) {
        Elements.setVisible(poolSection, statsEnabled && (ejb.type == MDB || ejb.type == STATELESS));
        Elements.setVisible(statefulSection, statsEnabled && ejb.type == STATEFUL);
        attributes.setVisible(DELIVERY_ACTIVE, statsEnabled && ejb.type == MDB);
        if (statsEnabled) {
            attributes.refresh(ejb);
            switch (ejb.type) {
                case MDB:
                case STATELESS:
                    poolUtilization.update(ejb.get(POOL_CURRENT_SIZE).asLong(), ejb.get(POOL_MAX_SIZE).asLong());
                    break;
                case STATEFUL:
                    statefulAttributes.refresh(ejb);
                    break;
                default:
                    break;
            }
            ModelNode firstTimer = firstTimer(ejb);
            if (firstTimer.isDefined()) {
                timer.refresh(firstTimer);
                clearInterval(intervalHandle);
                intervalHandle = setInterval(o -> updateRemaining(), 1000);
            }
        }
        Elements.setVisible(noStatistics, !statsEnabled);
        attributes.setVisible(INVOCATIONS, statsEnabled);
        attributes.setVisible(EXECUTION_TIME, statsEnabled);
        Elements.setVisible(timerSection, statsEnabled);
    }

    private void updateRemaining() {
        Operation operation = new Operation.Builder(address, READ_RESOURCE_OPERATION)
                .param(INCLUDE_RUNTIME, true)
                .param(RECURSIVE, true)
                .build();
        dispatcher.execute(operation, result -> {
            ModelNode firstTimer = firstTimer(result);
            if (firstTimer.isDefined() && nextTimeoutElement != null && remainingElement != null) {
                nextTimeoutElement.textContent = Format.mediumDateTime(new Date(firstTimer.get(NEXT_TIMEOUT).asLong()));

                long timeRemaining = firstTimer.get(TIME_REMAINING).asLong();
                int timeRemainingInSeconds = (int) round((timeRemaining / 1000.0));
                String humanReadableDuration = Format.humanReadableDuration(timeRemaining);
                if (maxRemaining < timeRemainingInSeconds) {
                    maxRemaining = timeRemainingInSeconds;
                }
                if (maxRemaining == timeRemainingInSeconds) {
                    remainingElement.reset(maxRemaining, humanReadableDuration);
                } else {
                    remainingElement.tick(humanReadableDuration);
                }
            }
        });
    }

    private ModelNode firstTimer(ModelNode ejb) {
        List<ModelNode> timers = ModelNodeHelper.failSafeList(ejb, TIMERS);
        return timers.isEmpty() ? new ModelNode() : timers.get(0);
    }

    private void enableStatistics(EjbNode ejb) {
        ResourceAddress address = EJB3_SUBSYSTEM_TEMPLATE.resolve(statementContext);
        Operation operation = new Operation.Builder(address, WRITE_ATTRIBUTE_OPERATION)
                .param(NAME, STATISTICS_ENABLED)
                .param(VALUE, true)
                .build();
        dispatcher.execute(operation, result -> update(ejb));
    }
}
