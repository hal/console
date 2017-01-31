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
package org.jboss.hal.ballroom;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.google.common.base.Joiner;
import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.Property;
import org.jboss.hal.resources.Constants;
import org.jboss.hal.resources.Messages;
import org.jboss.hal.resources.Names;
import org.jetbrains.annotations.NonNls;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static org.jboss.hal.ballroom.HelpTextBuilder.RestartMode.ALL_SERVICES;
import static org.jboss.hal.ballroom.HelpTextBuilder.RestartMode.NO_SERVICES;
import static org.jboss.hal.ballroom.HelpTextBuilder.RestartMode.RESOURCE_SERVICES;
import static org.jboss.hal.ballroom.HelpTextBuilder.RestartMode.UNKNOWN;
import static org.jboss.hal.dmr.ModelDescriptionConstants.*;

/**
 * Class to build a help text from an attribute description. Besides the description itself includes information about
 * whether a attribute is required, supports expressions or needs some kind of restart after modification.
 * <p>
 * TODO Add info about capabilities & requirements
 */
public class HelpTextBuilder {

    enum RestartMode {
        ALL_SERVICES(CONSTANTS.restartAllServices()),
        JVM(CONSTANTS.restartJvm()),
        NO_SERVICES(CONSTANTS.restartNoServices()),
        RESOURCE_SERVICES(CONSTANTS.restartResourceServices()),
        UNKNOWN(Names.UNKNOWN);

        private final String description;

        RestartMode(final String description) {
            this.description = description;
        }

        public String description() {
            return description;
        }
    }


    private static final Constants CONSTANTS = GWT.create(Constants.class);
    private static final Messages MESSAGES = GWT.create(Messages.class);
    @NonNls private static final Logger logger = LoggerFactory.getLogger(HelpTextBuilder.class);

    public SafeHtml helpText(Property property) {
        SafeHtmlBuilder help = new SafeHtmlBuilder();
        ModelNode attribute = property.getValue();
        boolean supportsExpression = attribute.hasDefined(EXPRESSIONS_ALLOWED) && attribute.get(EXPRESSIONS_ALLOWED)
                .asBoolean();
        boolean required = attribute.hasDefined(NILLABLE) && !attribute.get(NILLABLE).asBoolean();
        List<String> requires = attribute.hasDefined(REQUIRES)
                ? attribute.get(REQUIRES).asList().stream().map(ModelNode::asString).collect(toList())
                : Collections.emptyList();
        List<String> alternatives = attribute.hasDefined(ALTERNATIVES)
                ? attribute.get(ALTERNATIVES).asList().stream().map(ModelNode::asString).collect(toList())
                : Collections.emptyList();

        SafeHtml desc = SafeHtmlUtils.fromSafeConstant(attribute.get(DESCRIPTION).asString());
        help.append(desc);

        RestartMode restartMode = restartRequired(attribute);
        if (restartMode == UNKNOWN) {
            logger.warn("Unknown restart mode in attribute description for '{}': '{}'", property.getName(),
                    attribute.get(RESTART_REQUIRED).asString());
        }
        boolean showRestartHelp = (restartMode == ALL_SERVICES || restartMode == RestartMode.JVM || restartMode == RESOURCE_SERVICES);

        LabelBuilder labelBuilder = new LabelBuilder();
        List<String> textModules = new ArrayList<>();
        if (required) {
            textModules.add(CONSTANTS.requiredField());
        }
        if (supportsExpression) {
            textModules.add(CONSTANTS.supportsExpressions());
        }
        if (attribute.hasDefined(UNIT)) {
            textModules.add(MESSAGES.unit(attribute.get(UNIT).asString().toLowerCase()));
        }
        if (!requires.isEmpty()) {
            String textModule;
            if (requires.size() == 1) {
                textModule = labelBuilder.label(requires.get(0));
            } else {
                textModule = requires.stream().map(labelBuilder::label).collect(joining(", "));
            }
            textModules.add(MESSAGES.requires(textModule));
        }
        if (!alternatives.isEmpty()) {
            String textModule;
            if (alternatives.size() == 1) {
                textModule = labelBuilder.label(alternatives.get(0));
            } else {
                textModule = alternatives.stream().map(labelBuilder::label).collect(joining(", "));
            }
            textModules.add(MESSAGES.alternativesHelp(textModule));
        }
        if (showRestartHelp) {
            textModules.add(restartMode.description());
        }
        if (!textModules.isEmpty()) {
            help.appendHtmlConstant("<br/>").appendEscaped(Joiner.on(". ").join(textModules)).append('.'); //NON-NLS
        }

        return help.toSafeHtml();
    }

    @SuppressWarnings("HardCodedStringLiteral")
    private RestartMode restartRequired(ModelNode description) {
        if (description.hasDefined(RESTART_REQUIRED)) {
            String flag = description.get(RESTART_REQUIRED).asString();
            switch (flag) {
                case "all-services":
                    return ALL_SERVICES;
                case "resource-services":
                    return RESOURCE_SERVICES;
                case "jvm":
                    return RestartMode.JVM;
                case "no-services":
                    return NO_SERVICES;
                default:
                    return UNKNOWN;
            }
        }
        return null;
    }
}
