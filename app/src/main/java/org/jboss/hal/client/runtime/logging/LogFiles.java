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
package org.jboss.hal.client.runtime.logging;

import javax.inject.Inject;

import elemental.client.Browser;
import org.jboss.hal.config.Endpoints;
import org.jboss.hal.dmr.Property;
import org.jboss.hal.dmr.model.ResourceAddress;
import org.jboss.hal.meta.StatementContext;

/**
 * Common code used by the finder column and the presenter.
 *
 * @author Harald Pehl
 */
public class LogFiles {

    private final Endpoints endpoints;
    private final StatementContext statementContext;

    @Inject
    public LogFiles(final Endpoints endpoints,
            final StatementContext statementContext) {
        this.endpoints = endpoints;
        this.statementContext = statementContext;
    }

    public void download(final String logFile) {
        ResourceAddress address = AddressTemplates.LOG_FILE_TEMPLATE.resolve(statementContext, logFile);
        Browser.getWindow().open(streamUrl(address, logFile), "", "");
    }

    private String streamUrl(ResourceAddress address, String name) {
        StringBuilder url = new StringBuilder();

        url.append(endpoints.dmr()).append("/");
        for (Property property : address.asPropertyList()) {
            url.append(property.getName()).append("/").append(property.getValue().asString()).append("/");
        }
        url.append("?operation=attribute&name=stream&useStreamAsResponse"); //NON-NLS
        return url.toString();
    }}
