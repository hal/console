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
package org.jboss.hal.client.runtime.subsystem.ejb;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.gwtplatform.mvp.shared.proxy.PlaceRequest;
import elemental2.dom.HTMLElement;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.hal.ballroom.Format;
import org.jboss.hal.ballroom.LabelBuilder;
import org.jboss.hal.ballroom.ProgressElement;
import org.jboss.hal.core.finder.FinderPath;
import org.jboss.hal.core.finder.FinderPathFactory;
import org.jboss.hal.core.finder.PreviewAttributes;
import org.jboss.hal.core.finder.PreviewAttributes.PreviewAttribute;
import org.jboss.hal.core.finder.PreviewContent;
import org.jboss.hal.core.mvp.Places;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.ModelNodeHelper;
import org.jboss.hal.dmr.Operation;
import org.jboss.hal.dmr.ResourceAddress;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.meta.token.NameTokens;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;

import static elemental2.dom.DomGlobal.clearInterval;
import static elemental2.dom.DomGlobal.setInterval;
import static java.lang.Math.round;
import static java.util.Arrays.asList;
import static org.jboss.gwt.elemento.core.Elements.a;
import static org.jboss.gwt.elemento.core.Elements.h;
import static org.jboss.gwt.elemento.core.Elements.section;
import static org.jboss.gwt.elemento.core.Elements.span;
import static org.jboss.hal.ballroom.ProgressElement.Label.INLINE;
import static org.jboss.hal.ballroom.ProgressElement.Size.NORMAL;
import static org.jboss.hal.client.runtime.subsystem.ejb.EjbNode.Type.MDB;
import static org.jboss.hal.client.runtime.subsystem.ejb.EjbNode.Type.STATEFUL;
import static org.jboss.hal.client.runtime.subsystem.ejb.EjbNode.Type.STATELESS;
import static org.jboss.hal.dmr.ModelDescriptionConstants.*;

class EjbPreview extends PreviewContent<EjbNode> {

    private static double intervalHandle = 0; // one handle for all previews!

    private final Dispatcher dispatcher;
    private final ResourceAddress address;
    private final LabelBuilder labelBuilder;
    private final PreviewAttributes<EjbNode> attributes;
    private final Map<EjbNode.Type, HTMLElement> typeElements;
    private PreviewAttributes<ModelNode> timer;
    private HTMLElement nextTimeoutElement;
    private ProgressElement remainingElement;
    private int maxRemaining;

    EjbPreview(EjbNode ejb, FinderPathFactory finderPathFactory, Places places, Dispatcher dispatcher,
            Resources resources) {
        super(ejb.getName(), ejb.getPath());
        this.dispatcher = dispatcher;
        this.address = ejb.getAddress();
        this.labelBuilder = new LabelBuilder();
        this.typeElements = new HashMap<>();
        this.maxRemaining = 0;

        getHeaderContainer().appendChild(refreshLink(() -> update(null)));

        FinderPath path = finderPathFactory.deployment(ejb.getDeployment());
        PlaceRequest placeRequest = places.finderPlace(NameTokens.DEPLOYMENTS, path).build();
        Elements.removeChildrenFrom(getLeadElement());
        getLeadElement().appendChild(a(places.historyToken(placeRequest))
                .textContent(ejb.getPath())
                .title(resources.messages().goTo(Names.DEPLOYMENTS))
                .asElement());

        attributes = new PreviewAttributes<>(ejb, asList(COMPONENT_CLASS_NAME, INVOCATIONS, EXECUTION_TIME));
        previewBuilder().addAll(attributes);

        HTMLElement poolElements = section().add(h(2, Names.POOL)).asElement();
        HTMLElement statefulElement = section().addAll(new PreviewAttributes<EjbNode>(ejb, STATELESS.type)).asElement();
        typeElements.put(MDB, poolElements);
        typeElements.put(STATELESS, poolElements);
        typeElements.put(STATEFUL, statefulElement);

        ModelNode firstTimer = firstTimer(ejb);
        if (firstTimer.isDefined()) {
            timer = new PreviewAttributes<>(firstTimer, Names.TIMER)
                    .append(t -> {
                        String nextTimeout = Format.mediumDateTime(new Date(t.get(NEXT_TIMEOUT).asLong()));
                        nextTimeoutElement = span().textContent(nextTimeout).asElement();
                        return new PreviewAttribute(labelBuilder.label(NEXT_TIMEOUT), nextTimeoutElement);
                    })
                    .append(t -> {
                        maxRemaining = round(t.get(TIME_REMAINING).asInt() / 1000);
                        remainingElement = new ProgressElement(NORMAL, INLINE, true);
                        remainingElement.reset(maxRemaining);
                        return new PreviewAttribute(labelBuilder.label(TIME_REMAINING), remainingElement.asElement());
                    });
            previewBuilder().addAll(timer);
        }
    }

    @Override
    public void detach() {
        clearInterval(intervalHandle);
    }

    @Override
    public void update(EjbNode item) {
        clearInterval(intervalHandle);
        Operation operation = new Operation.Builder(address, READ_RESOURCE_OPERATION)
                .param(INCLUDE_RUNTIME, true)
                .param(RECURSIVE, true)
                .build();
        dispatcher.execute(operation, result -> updateInternal(new EjbNode(address, result)));
    }

    private void updateInternal(EjbNode ejb) {
        attributes.refresh(ejb);
        ModelNode firstTimer = firstTimer(ejb);
        if (firstTimer.isDefined()) {
            timer.refresh(firstTimer);
            intervalHandle = setInterval(o -> updateDuration(), 1000);
        }
    }

    private void updateDuration() {
        Operation operation = new Operation.Builder(address, READ_RESOURCE_OPERATION)
                .param(INCLUDE_RUNTIME, true)
                .param(RECURSIVE, true)
                .build();
        dispatcher.execute(operation, result -> {
            ModelNode firstTimer = firstTimer(result);
            if (firstTimer.isDefined() && nextTimeoutElement != null && remainingElement != null) {
                nextTimeoutElement.textContent = Format.mediumDateTime(new Date(firstTimer.get(NEXT_TIMEOUT).asLong()));

                long timeRemaining = firstTimer.get(TIME_REMAINING).asLong();
                int timeRemainingInSeconds = round(timeRemaining / 1000);
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
}
