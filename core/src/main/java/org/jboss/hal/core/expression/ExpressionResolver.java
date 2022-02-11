/*
 *  Copyright 2022 Red Hat
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.jboss.hal.core.expression;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.jboss.hal.ballroom.form.ResolveExpressionEvent;
import org.jboss.hal.config.Environment;
import org.jboss.hal.core.runtime.server.Server;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.Operation;
import org.jboss.hal.dmr.Property;
import org.jboss.hal.dmr.ResourceAddress;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.spi.Message;
import org.jboss.hal.spi.MessageEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableMap;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.web.bindery.event.shared.EventBus;

import static org.jboss.hal.dmr.ModelDescriptionConstants.*;

public class ExpressionResolver implements ResolveExpressionEvent.ResolveExpressionHandler {

    private static final Logger logger = LoggerFactory.getLogger(ExpressionResolver.class);

    private final Dispatcher dispatcher;
    private final EventBus eventBus;
    private final Environment environment;
    private final Resources resources;

    @Inject
    public ExpressionResolver(final EventBus eventBus,
            final Environment environment,
            final Dispatcher dispatcher,
            final Resources resources) {
        this.eventBus = eventBus;
        this.environment = environment;
        this.dispatcher = dispatcher;
        this.resources = resources;

        eventBus.addHandler(ResolveExpressionEvent.getType(), this);
    }

    @Override
    public void onResolveExpression(final ResolveExpressionEvent event) {
        new ExpressionDialog(this, environment, resources).showAndResolve(event.getExpression());
    }

    void resolve(final Expression expression, final AsyncCallback<Map<String, String>> callback) {
        logger.debug("Resolving {}", expression);
        if (environment.isStandalone()) {
            Operation operation = new Operation.Builder(ResourceAddress.root(), RESOLVE_EXPRESSION)
                    .param(EXPRESSION, expression.toString())
                    .build();
            dispatcher.execute(operation,
                    (result) -> callback.onSuccess(ImmutableMap.of(Server.STANDALONE.getName(), result.asString())),
                    (op1, failure) -> callback.onFailure(new RuntimeException(failure)),
                    (op2, exception) -> callback.onFailure(exception));
        } else {
            Operation operation = new Operation.Builder(ResourceAddress.root(), RESOLVE_EXPRESSION_ON_DOMAIN)
                    .param(EXPRESSION, expression.toString())
                    .build();
            dispatcher.executeDMR(operation,
                    (res) -> callback.onSuccess(parseServerGroups(res.get(SERVER_GROUPS))),
                    (op1, failure) -> callback.onFailure(new RuntimeException(failure)),
                    (op2, exception) -> callback.onFailure(exception));
        }
    }

    private Map<String, String> parseServerGroups(ModelNode serverGroups) {
        Map<String, String> values = new HashMap<>();
        if (serverGroups.isDefined()) {
            List<Property> groups = serverGroups.asPropertyList();
            for (Property serverGroup : groups) {
                List<Property> hosts = serverGroup.getValue().get(HOST).asPropertyList();
                for (Property host : hosts) {
                    List<Property> servers = host.getValue().asPropertyList();
                    for (Property server : servers) {
                        values.put(server.getName(),
                                server.getValue().get(RESPONSE).get(RESULT).asString());
                    }
                }
            }
        }
        return values;
    }

    private void unableToResolve(Expression expression, String error) {
        logger.error("Unable to resolve {}: {}", expression, error);
        MessageEvent.fire(eventBus,
                Message.error(resources.messages().expressionError(expression.toString()), error));
    }
}
