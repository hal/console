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
package org.jboss.hal.dmr.dispatch;

import javax.inject.Inject;

import org.jboss.hal.config.Endpoints;
import org.jboss.hal.dmr.model.ResourceAddress;

import static java.util.stream.Collectors.joining;

/**
 * @author Harald Pehl
 */
public class Download {

    private final Endpoints endpoints;

    @Inject
    public Download(final Endpoints endpoints) {this.endpoints = endpoints;}

    public String url(ResourceAddress address, String operation, String... parameter) {
        String path = address.asPropertyList().stream()
                .map(property -> property.getName() + "/" + property.getValue().asString())
                .collect(joining("/"));
        return url(path, operation, parameter);
    }

    @SuppressWarnings("HardCodedStringLiteral")
    public String url(String address, String operation, String... parameter) {
        StringBuilder builder = new StringBuilder();
        builder.append(endpoints.dmr())
                .append("/")
                .append(address)
                .append("?operation=")
                .append(operation);
        if (parameter != null && parameter.length > 1) {
            if (parameter.length % 2 != 0) {
                throw new IllegalArgumentException("Parameter in Download.url() must be key/value pairs");
            }
            for (int i = 0; i < parameter.length; i += 2) {
                builder.append("&")
                        .append(parameter[i])
                        .append("=")
                        .append(parameter[i + 1]);
            }
        }
        builder.append("&useStreamAsResponse");
        return builder.toString();
    }
}
