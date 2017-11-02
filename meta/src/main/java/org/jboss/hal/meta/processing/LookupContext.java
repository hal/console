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
package org.jboss.hal.meta.processing;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.jboss.hal.dmr.ResourceAddress;
import org.jboss.hal.flow.FlowContext;
import org.jboss.hal.meta.AddressTemplate;

import static java.util.stream.Collectors.toSet;

class LookupContext extends FlowContext {

    final LookupResult lookupResult;
    final List<RrdResult> rrdResults;

    LookupContext(Set<AddressTemplate> template, boolean recursive) {
        this.lookupResult = new LookupResult(template, recursive);
        this.rrdResults = new ArrayList<>();
    }

    SortedSet<String> newResourceDescriptions() {
        SortedSet<String> result = new TreeSet<>();
        for (RrdResult rrdResult : rrdResults) {
            result.addAll(rrdResult.resourceDescriptions.keySet().stream()
                    .map(ResourceAddress::toString)
                    .collect(toSet()));
        }
        return result;
    }

    SortedSet<String> newSecurityContexts() {
        SortedSet<String> result = new TreeSet<>();
        for (RrdResult rrdResult : rrdResults) {
            result.addAll(rrdResult.securityContexts.keySet().stream()
                    .map(ResourceAddress::toString)
                    .collect(toSet()));
        }
        return result;
    }
}
