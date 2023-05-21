/*
 *  Copyright 2022 Red Hat
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.jboss.hal.client.runtime.subsystem.jaxrs;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.jboss.hal.core.deployment.DeploymentResource;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.ResourceAddress;

import com.google.common.base.Splitter;
import com.google.common.collect.Sets;

import static java.util.stream.Collectors.toSet;

import static org.jboss.hal.dmr.ModelDescriptionConstants.*;
import static org.jboss.hal.dmr.ModelNodeHelper.failSafeList;

class RestResource extends DeploymentResource {

    RestResource(ResourceAddress address, ModelNode modelNode) {
        super(address, modelNode);
    }

    Set<String> getResourceMethods() {
        return Sets.union(resourceMethods(REST_RESOURCE_PATHS), resourceMethods(SUB_RESOURCE_LOCATORS)).immutableCopy();
    }

    private Set<String> resourceMethods(String attribute) {
        List<ModelNode> nodes = failSafeList(this, attribute);
        return nodes.stream()
                .map(node -> failSafeList(node, RESOURCE_METHODS))
                .flatMap(Collection::stream)
                .map(method -> {
                    List<String> methods = Splitter.on(' ')
                            .omitEmptyStrings()
                            .trimResults()
                            .splitToList(method.asString());
                    return methods.isEmpty() ? null : methods.get(0);
                })
                .filter(Objects::nonNull)
                .collect(toSet());
    }

    Set<String> getConsumes() {
        return Sets.union(mediaType(REST_RESOURCE_PATHS, CONSUMES), mediaType(SUB_RESOURCE_LOCATORS, CONSUMES))
                .immutableCopy();
    }

    Set<String> getProduces() {
        return Sets.union(mediaType(REST_RESOURCE_PATHS, PRODUCES), mediaType(SUB_RESOURCE_LOCATORS, PRODUCES))
                .immutableCopy();
    }

    private Set<String> mediaType(String attribute, String type) {
        List<ModelNode> restResourcePaths = failSafeList(this, attribute);
        return restResourcePaths.stream()
                .map(rrp -> failSafeList(rrp, type))
                .flatMap(Collection::stream)
                .map(ModelNode::asString)
                .collect(toSet());
    }
}
