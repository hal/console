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
package org.jboss.hal.meta.description;

import java.util.List;
import java.util.function.Function;

import com.google.common.base.Splitter;
import org.jboss.hal.meta.AddressTemplate;

import static java.util.stream.Collectors.toList;
import static java.util.stream.StreamSupport.stream;

/**
 * Function which takes an address template and replaces specific values with "*". Applied to templates when they're
 * passed to {@link ResourceDescriptionRegistry#lookup(AddressTemplate)}.
 * <p>
 * The following parts of an address template are modified by this function:
 * <ul>
 * <li>{@code host}</li>
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
public class ResourceDescriptionTemplateProcessor implements Function<AddressTemplate, AddressTemplate> {

    @Override
    public AddressTemplate apply(final AddressTemplate template) {
        if (template != null && !AddressTemplate.ROOT.equals(template)) {
            List<String[]> segments = stream(template.spliterator(), false)
                    .map(segment -> {
                        if (segment.contains("=")) {
                            return Splitter.on('=')
                                    .omitEmptyStrings()
                                    .trimResults()
                                    .limit(2)
                                    .splitToList(segment)
                                    .toArray(new String[2]);
                        }
                        return new String[]{segment, null};
                    })
                    .collect(toList());

            StringBuilder builder = new StringBuilder();
            SegmentProcessor.process(segments, segment -> {
                builder.append("/").append(segment[0]);
                if (segment[1] != null) {
                    builder.append("=").append(segment[1]);
                }
            });
            return AddressTemplate.of(builder.toString());
        }
        return AddressTemplate.ROOT;
    }
}
