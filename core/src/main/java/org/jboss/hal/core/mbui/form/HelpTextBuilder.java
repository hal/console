/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2010, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.hal.core.mbui.form;

import com.google.common.base.Joiner;
import com.google.gwt.core.client.GWT;
import org.jboss.hal.ballroom.form.FormItem;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.resources.Constants;
import org.jboss.hal.resources.Messages;
import org.jboss.hal.resources.Names;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import static org.jboss.hal.core.mbui.form.HelpTextBuilder.RestartMode.*;
import static org.jboss.hal.dmr.ModelDescriptionConstants.DESCRIPTION;
import static org.jboss.hal.dmr.ModelDescriptionConstants.UNIT;
import static org.jboss.hal.dmr.dispatch.ProcessStateProcessor.RESTART_REQUIRED;

/**
 * Class to build a help text from an attribute description. Besides the description itself includes information about
 * whether a attribute is required, supports expressions or needs some kind of restart after modification.
 * <p>
 * TODO Add info about "alternatives"
 * TODO Add info about capabilities & requirements
 * TODO Add info about "requires"
 */
class HelpTextBuilder {

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
    private static final Logger logger = LoggerFactory.getLogger(HelpTextBuilder.class);

    String helpText(FormItem formItem, ModelNode description) {
        StringBuilder help = new StringBuilder();
        String desc = description.get(DESCRIPTION).asString();
        if (!desc.endsWith(".")) {
            desc = desc + ".";
        }
        help.append(desc);

        RestartMode restartMode = restartRequired(description);
        if (restartMode == UNKNOWN) {
            logger.warn("Unknown restart mode in attribute description for '{}': '{}'", formItem.getName(), //NON-NLS
                    description.get(RESTART_REQUIRED).asString());
        }
        boolean showRestartHelp = (restartMode == ALL_SERVICES || restartMode == JVM || restartMode == RESOURCE_SERVICES);

        List<String> textModules = new ArrayList<>();
        if (formItem.isRequired()) {
            textModules.add(CONSTANTS.requiredField());
        }
        if (formItem.supportsExpressions()) {
            textModules.add(CONSTANTS.supportsExpressions());
        }
        if (description.hasDefined(UNIT)) {
            textModules.add(MESSAGES.unit(description.get(UNIT).asString().toLowerCase()));
        }
        if (showRestartHelp) {
            textModules.add(restartMode.description());
        }
        if (!textModules.isEmpty()) {
            help.append(" ").append(Joiner.on(". ").join(textModules)).append(".");
        }

        return help.toString();
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
                    return JVM;
                case "no-services":
                    return NO_SERVICES;
                default:
                    return UNKNOWN;
            }
        }
        return null;
    }
}
