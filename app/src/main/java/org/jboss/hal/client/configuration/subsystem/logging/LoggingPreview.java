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

import com.google.common.base.Joiner;
import com.google.common.collect.FluentIterable;
import org.jboss.hal.ballroom.LabelBuilder;
import org.jboss.hal.core.finder.PreviewAttributes;
import org.jboss.hal.core.finder.PreviewContent;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.dmr.model.Operation;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;

import static org.jboss.hal.client.configuration.subsystem.logging.AddressTemplates.ROOT_LOGGER_TEMPLATE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.READ_RESOURCE_OPERATION;

/**
 * @author Harald Pehl
 */
public class LoggingPreview extends PreviewContent {

    private static final String HANDLERS = "handlers";

    public LoggingPreview(StatementContext statementContext, Dispatcher dispatcher, Resources resources) {
        super(Names.CONFIGURATION, resources.previews().loggingConfiguration());

        LabelBuilder labelBuilder = new LabelBuilder();
        PreviewAttributes<ModelNode> attributes = new PreviewAttributes<>(new ModelNode(), "Root Logger") //NON-NLS
                .append("level") //NON-NLS
                .append(model -> {
                    String handlers = "";
                    if (model.hasDefined(HANDLERS)) {
                        //noinspection Guava
                        handlers = FluentIterable.from(model.get(HANDLERS).asList())
                                .transform(ModelNode::asString)
                                .join(Joiner.on(", "));
                    }
                    return new String[]{labelBuilder.label(HANDLERS), handlers };
                })
                .end();
        previewBuilder().addAll(attributes);

        Operation operation = new Operation.Builder(READ_RESOURCE_OPERATION,
                ROOT_LOGGER_TEMPLATE.resolve(statementContext)).build();
        dispatcher.execute(operation, attributes::refresh);
    }
}
