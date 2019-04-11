/*
 * Copyright 2015-2016 Red Hat, Inc, and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.hal.meta.description;

import java.util.List;
import java.util.function.Function;

import org.jboss.hal.dmr.ResourceAddress;
import org.jetbrains.annotations.NonNls;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.util.stream.Collectors.toList;

/**
 * Function which takes a resource address and replaces specific values with "*". Applied to addresses from
 * the r-r-d result before they are {@linkplain ResourceDescriptionRegistry#add(ResourceAddress, ResourceDescription)
 * added} to the resource description registry.
 * <p>
 * The following parts of a resource address are modified by this function:
 * <ul>
 * <li>{@code host} (only if segments &gt; 1)</li>
 * <li>{@code server-group}</li>
 * <li>{@code server} (if it's the 2nd segment of the address)</li>
 * <li>{@code server-config} (if it's the 2nd segment of the address)</li>
 * </ul>
 * <p>
 * Examples:
 * <pre>
 * /host=master/server-config=server-one &rarr; /host=&#42;/server-config=&#42;
 * /host=master/server=server-one/subsystem=data-sources &rarr; /host=&#42;/server=&#42;/subsystem=data-sources
 * /server-group=main-server-group &rarr; /server-group=&#42;
 * /subsystem=mail/mail-session=foo/server=bar &rarr; /subsystem=mail/mail-session=foo/server=bar
 * </pre>
 */
public class ResourceDescriptionAddressProcessor implements Function<ResourceAddress, ResourceAddress> {

    @NonNls private static final Logger logger = LoggerFactory.getLogger(ResourceDescriptionAddressProcessor.class);

    @Override
    public ResourceAddress apply(ResourceAddress address) {
        ResourceAddress modified = new ResourceAddress();

        if (address != null && !address.isEmpty()) {
            List<String[]> segments = address.asPropertyList().stream()
                    .map(property -> new String[]{property.getName(), property.getValue().asString()})
                    .collect(toList());
            SegmentProcessor.process(segments, segment -> modified.add(segment[0], segment[1]));
        }

        logger.debug("{} -> {}", address, modified);
        return modified;
    }
}
