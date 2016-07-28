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
package org.jboss.hal.client.runtime.subsystem.jpa;

import java.util.Collections;

import com.gwtplatform.mvp.shared.proxy.PlaceRequest;
import com.gwtplatform.mvp.shared.proxy.TokenFormatter;
import elemental.dom.Element;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.hal.ballroom.Alert;
import org.jboss.hal.ballroom.metric.Utilization;
import org.jboss.hal.config.Environment;
import org.jboss.hal.core.finder.FinderPath;
import org.jboss.hal.core.finder.FinderPathFactory;
import org.jboss.hal.core.finder.PreviewContent;
import org.jboss.hal.core.mvp.Places;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.dmr.model.Operation;
import org.jboss.hal.dmr.model.ResourceAddress;
import org.jboss.hal.meta.token.NameTokens;
import org.jboss.hal.resources.Icons;
import org.jboss.hal.resources.Resources;

import static org.jboss.gwt.elemento.core.EventType.click;
import static org.jboss.hal.dmr.ModelDescriptionConstants.INCLUDE_RUNTIME;
import static org.jboss.hal.dmr.ModelDescriptionConstants.READ_RESOURCE_OPERATION;
import static org.jboss.hal.dmr.ModelDescriptionConstants.RECURSIVE;
import static org.jboss.hal.resources.CSS.*;

/**
 * @author Harald Pehl
 */
class JpaPreview extends PreviewContent<JpaStatistic> {

    private static final String REFRESH_ELEMENT = "refreshElement";

    private final Dispatcher dispatcher;
    private final ResourceAddress address;
    private final Alert noStatisticsWarning;
    private final Utilization openedSessions;
    private final Utilization closedSessions;
    private final Element refresh;

    @SuppressWarnings("HardCodedStringLiteral")
    JpaPreview(final JpaStatistic jpaStatistic, final Environment environment, final Dispatcher dispatcher,
            final FinderPathFactory finderPathFactory, final Places places, final TokenFormatter tokenFormatter,
            final Resources resources) {

        super(jpaStatistic.getName(), jpaStatistic.getDeployment());
        this.dispatcher = dispatcher;
        this.address = jpaStatistic.getAddress();

        noStatisticsWarning = new Alert(Icons.WARNING,
                resources.messages().jpaStatisticsDisabled(jpaStatistic.getName()));

        FinderPath path = finderPathFactory.deployment(jpaStatistic.getDeployment());
        PlaceRequest placeRequest = places.finderPlace(NameTokens.DEPLOYMENTS, path);
        String linkToDeployment = "#" + tokenFormatter.toHistoryToken(Collections.singletonList(placeRequest));
        previewBuilder().p()
                .innerHtml(resources.messages()
                        .jpaStatisticsPreview(jpaStatistic.getName(), jpaStatistic.getDeployment(), linkToDeployment,
                                resources.constants().view()))
                .end();

        openedSessions = new Utilization(resources.constants().opened(), resources.constants().sessions(),
                environment.isStandalone(), false);
        closedSessions = new Utilization(resources.constants().closed(), resources.constants().sessions(),
                environment.isStandalone(), false);

        // @formatter:off
        previewBuilder()
            .add(noStatisticsWarning)
            .div().css(clearfix)
                .a().rememberAs(REFRESH_ELEMENT).css(clickable, pullRight).on(click, event -> update(null))
                    .span().css(fontAwesome("refresh"), marginRight4).end()
                    .span().textContent(resources.constants().refresh()).end()
                .end()
            .end()
            .h(2).css(underline).textContent(resources.constants().sessions()).end()
            .add(openedSessions)
            .add(closedSessions);
        // @formatter:on

        refresh = previewBuilder().referenceFor(REFRESH_ELEMENT);
    }

    @Override
    public void update(final JpaStatistic item) {
        if (item == null) {
            Operation operation = new Operation.Builder(READ_RESOURCE_OPERATION, address)
                    .param(INCLUDE_RUNTIME, true)
                    .param(RECURSIVE, true)
                    .build();
            dispatcher.execute(operation, result -> internalUpdate(new JpaStatistic(address, result)));

        } else {
            internalUpdate(item);
        }
    }

    @SuppressWarnings("HardCodedStringLiteral")
    private void internalUpdate(final JpaStatistic statistic) {
        Elements.setVisible(noStatisticsWarning.asElement(), !statistic.isStatisticsEnabled());
        Elements.setVisible(refresh, statistic.isStatisticsEnabled());
        openedSessions.setDisabled(!statistic.isStatisticsEnabled());
        closedSessions.setDisabled(!statistic.isStatisticsEnabled());

        long sessions = statistic.get("connect-count").asLong();
        long opened = statistic.get("session-open-count").asLong();
        long closed = statistic.get("session-close-count").asLong();
        openedSessions.update(opened, sessions);
        closedSessions.update(closed, sessions);
    }
}
