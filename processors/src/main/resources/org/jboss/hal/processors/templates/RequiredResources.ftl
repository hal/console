<#-- @ftlvariable name="packageName" type="java.lang.String" -->
<#-- @ftlvariable name="className" type="java.lang.String" -->
<#-- @ftlvariable name="pageInfos" type="java.util.Set<org.jboss.hal.processors.RequiredResourcesProcessor.PageInfo>" -->
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
@Generated("org.jboss.hal.processors.RequiredResourcesProcessor")
public class ${className} implements org.jboss.hal.client.registry.RequiredResourcesRegistry {

    private final HashMultimap<String, String> resources;
    private final HashMultimap<String, String> operations;
    private final Map<String, Boolean> recursive;

    public ${className}() {
        resources = HashMultimap.create();
        operations = HashMultimap.create();
        recursive = new HashMap<>();

        <#list pageInfos as pageInfo>
        <#if (pageInfo.resources?size > 0)>
        resources.putAll("${pageInfo.pageType}", asList(<#list pageInfo.resources as resource>"${resource}"<#if resource_has_next>, </#if></#list>));
        </#if>
        <#if (pageInfo.operations?size > 0)>
        operations.putAll("${pageInfo.pageType}", asList(<#list pageInfo.operations as operation>"${operation}"<#if operation_has_next>, </#if></#list>));
        </#if>
        recursive.put("${pageInfo.pageType}", ${pageInfo.recursive?c});
        </#list>
    }

    @Override
    public Set<String> getResources(String page) {
        if (resources.containsKey(page)) {
            return resources.get(page);
        } else {
            return Collections.<String>emptySet();
        }
    }

    @Override
    public Set<String> getOperations(String page) {
        if (operations.containsKey(page)) {
            return operations.get(page);
        } else {
            return Collections.<String>emptySet();
        }
    }

    @Override
    public boolean isRecursive(String page) {
        if (recursive.containsKey(page)) {
            return recursive.get(page);
        } else {
            return false;
        }
    }
}
