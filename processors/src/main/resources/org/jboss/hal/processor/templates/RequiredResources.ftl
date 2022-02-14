<#--

     Copyright 2022 Red Hat

     Licensed under the Apache License, Version 2.0 (the "License");
     you may not use this file except in compliance with the License.
     You may obtain a copy of the License at

         https://www.apache.org/licenses/LICENSE-2.0

     Unless required by applicable law or agreed to in writing, software
     distributed under the License is distributed on an "AS IS" BASIS,
     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
     See the License for the specific language governing permissions and
     limitations under the License.

-->
<#-- @ftlvariable name="generatedWith" type="java.lang.String" -->
<#-- @ftlvariable name="packageName" type="java.lang.String" -->
<#-- @ftlvariable name="className" type="java.lang.String" -->
<#-- @ftlvariable name="requiredInfos" type="java.util.Collection<org.jboss.hal.processor.RequiredResourcesProcessor.RequiredInfo>" -->
package ${packageName};

import com.google.common.collect.HashMultimap;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.HashMap;
import javax.annotation.Generated;

import static java.util.Arrays.asList;

/*
 * WARNING! This class is generated. Do not modify.
 */
@Generated("${generatedWith}")
public class ${className} implements org.jboss.hal.meta.resource.RequiredResources {

    private final HashMultimap<String, String> resources;
    private final Map<String, Boolean> recursive;

    public ${className}() {
        resources = HashMultimap.create();
        recursive = new HashMap<>();

        <#list requiredInfos as requiredInfo>
        <#if (requiredInfo.resources?size > 0)>
        resources.putAll("${requiredInfo.id}", asList(<#list requiredInfo.resources as resource>"${resource}"<#if resource_has_next>, </#if></#list>));
        </#if>
        recursive.put("${requiredInfo.id}", ${requiredInfo.recursive?c});
        </#list>
    }

    @Override
    public Set<String> getResources(String id) {
        if (resources.containsKey(id)) {
            return resources.get(id);
        } else {
            return Collections.<String>emptySet();
        }
    }

    @Override
    public boolean isRecursive(String id) {
        if (recursive.containsKey(id)) {
            return recursive.get(id);
        } else {
            return false;
        }
    }
}
