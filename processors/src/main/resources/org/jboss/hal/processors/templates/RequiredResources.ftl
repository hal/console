<#-- @ftlvariable name="packageName" type="java.lang.String" -->
<#-- @ftlvariable name="className" type="java.lang.String" -->
<#-- @ftlvariable name="tokenInfos" type="java.util.Set<org.jboss.hal.processors.NameTokenProcessor.TokenInfo>" -->
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
public class ${className} implements org.jboss.hal.core.registry.RequiredResourcesRegistry {

    private final HashMultimap<String, String> resources;
    private final HashMultimap<String, String> operations;
    private final Map<String, Boolean> recursive;

    public ${className}() {
        resources = HashMultimap.create();
        operations = HashMultimap.create();
        recursive = new HashMap<>();

        <#list tokenInfos as tokenInfo>
        <#if (tokenInfo.resources?size > 0)>
        resources.putAll("${tokenInfo.token}", asList(<#list tokenInfo.resources as resource>"${resource}"<#if resource_has_next>, </#if></#list>));
        </#if>
        <#if (tokenInfo.operations?size > 0)>
        operations.putAll("${tokenInfo.token}", asList(<#list tokenInfo.operations as operation>"${operation}"<#if operation_has_next>, </#if></#list>));
        </#if>
        recursive.put("${tokenInfo.token}", ${tokenInfo.recursive?c});
        </#list>
    }

    @Override
    public Set<String> getResources(String token) {
        if (resources.containsKey(token)) {
            return resources.get(token);
        } else {
            return Collections.<String>emptySet();
        }
    }

    @Override
    public Set<String> getOperations(String token) {
        if (operations.containsKey(token)) {
            return operations.get(token);
        } else {
            return Collections.<String>emptySet();
        }
    }

    @Override
    public boolean isRecursive(String token) {
        if (recursive.containsKey(token)) {
            return recursive.get(token);
        } else {
            return false;
        }
    }
}
