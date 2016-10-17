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
package org.jboss.hal.ballroom.autocomplete;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import com.google.common.base.Strings;
import org.jboss.hal.ballroom.form.SuggestHandler;
import org.jboss.hal.dmr.Property;
import org.jboss.hal.dmr.model.ResourceAddress;
import org.jetbrains.annotations.NonNls;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

abstract class AbstractReadChildrenProcessor implements ResultProcessor<ReadChildrenResult> {

    @NonNls private static final Logger logger = LoggerFactory.getLogger(AbstractReadChildrenProcessor.class);

    protected List<ReadChildrenResult> results(List<ResourceAddress> addresses) {
        List<ReadChildrenResult> results = new ArrayList<>();

        if (!addresses.isEmpty()) {
            // it's expected that all addresses are of the same type (same length of segments)
            Iterator<ResourceAddress> iterator = addresses.iterator();
            int length = iterator.next().asList().size();
            while (iterator.hasNext()) {
                if (iterator.next().asList().size() != length) {
                    logger.error("Different address types in result processor");
                    return Collections.emptyList();
                }
            }

            // turn the addresses into a list of models
            for (ResourceAddress address : addresses) {
                ReadChildrenResult model = new ReadChildrenResult(address.lastValue());
                for (Property property : address.getParent().asPropertyList()) {
                    model.addresses.put(property.getName(), property.getValue().asString());
                }
                results.add(model);
            }
        }

        return results;
    }

    protected boolean match(String query, ResourceAddress address) {
        return !Strings.isNullOrEmpty(query) &&
                (SuggestHandler.SHOW_ALL_VALUE.equals(query) || address.lastValue().contains(query));
    }
}
