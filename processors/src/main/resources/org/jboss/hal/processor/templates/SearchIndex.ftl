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
<#-- @ftlvariable name="tokenInfos" type="java.util.Set<org.jboss.hal.processor.NameTokenProcessor.TokenInfo>" -->
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
@Generated("${generatedWith}")
public class ${className} implements org.jboss.hal.meta.search.SearchIndex {

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
