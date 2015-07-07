<#-- @ftlvariable name="packageName" type="java.lang.String" -->
<#-- @ftlvariable name="className" type="java.lang.String" -->
<#-- @ftlvariable name="tokenInfos" type="java.util.Set<org.jboss.hal.processors.NameTokenProcessor.TokenInfo>" -->
package ${packageName};

import com.google.common.collect.HashMultimap;

import java.util.Collections;
import java.util.Set;
import java.util.HashSet;
import javax.annotation.Generated;

import static java.util.Arrays.asList;

/*
* WARNING! This class is generated. Do not modify.
*/
@Generated("org.jboss.hal.processors.NameTokenProcessor")
public class ${className} implements org.jboss.hal.core.registry.SearchIndexRegistry {

    private final Set<String> standaloneTokens;
    private final Set<String> domainTokens;
    private final HashMultimap<String, String> keywords;
    private final HashMultimap<String, String> resources;

    public ${className}() {
        standaloneTokens = new HashSet<>();
        domainTokens = new HashSet<>();
        keywords = HashMultimap.create();
        resources = HashMultimap.create();

        <#list tokenInfos as tokenInfo>
        <#if !tokenInfo.exclude>
        <#if tokenInfo.standaloneOnly>
        standaloneTokens.add("${tokenInfo.token}");
        <#elseif tokenInfo.domainOnly>
        domainTokens.add("${tokenInfo.token}");
        <#else>
        standaloneTokens.add("${tokenInfo.token}");
        domainTokens.add("${tokenInfo.token}");
        </#if>
        <#if (tokenInfo.keywords?size > 0)>
        keywords.putAll("${tokenInfo.token}", asList(<#list tokenInfo.keywords as keyword>"${keyword}"<#if keyword_has_next>, </#if></#list>));
        </#if>
        <#if (tokenInfo.operations?size > 0)>
        resources.putAll("${tokenInfo.token}", asList(<#list tokenInfo.resources as resource>"${resource}"<#if resource_has_next>, </#if></#list>));
        </#if>
        </#if>
        </#list>
    }

    @Override
    public Set<String> getTokens(boolean standalone) {
        return standalone ? standaloneTokens : domainTokens;
    }

    @Override
    public Set<String> getKeywords(String token) {
        if (keywords.containsKey(token)) {
            return keywords.get(token);
        } else {
            return Collections.<String>emptySet();
        }
    }

    @Override
    public Set<String> getResources(String token) {
        if (resources.containsKey(token)) {
            return resources.get(token);
        } else {
            return Collections.<String>emptySet();
        }
    }
}
