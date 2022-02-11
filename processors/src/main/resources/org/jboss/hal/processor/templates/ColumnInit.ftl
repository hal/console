<#--

     Copyright 2022 Red Hat

     Licensed under the Apache License, Version 2.0 (the "License");
     you may not use this file except in compliance with the License.
     You may obtain a copy of the License at

         http://www.apache.org/licenses/LICENSE-2.0

     Unless required by applicable law or agreed to in writing, software
     distributed under the License is distributed on an "AS IS" BASIS,
     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
     See the License for the specific language governing permissions and
     limitations under the License.

-->
<#-- @ftlvariable name="generatedWith" type="java.lang.String" -->
<#-- @ftlvariable name="packageName" type="java.lang.String" -->
<#-- @ftlvariable name="className" type="java.lang.String" -->
<#-- @ftlvariable name="columnInfos" type="java.util.Set<org.jboss.hal.processor.ColumnRegistrationProcessor.ColumnInfo>" -->
package ${packageName};

import javax.annotation.Generated;
import javax.inject.Inject;
import javax.inject.Provider;

import com.google.gwt.inject.client.AsyncProvider;

import org.jboss.hal.core.finder.ColumnRegistry;

/*
 * WARNING! This class is generated. Do not modify.
 */
@Generated("${generatedWith}")
public class ${className} {

    @Inject
    public ${className}(ColumnRegistry registry,
            <#list columnInfos as columnInfo><#if columnInfo.async>AsyncProvider<${columnInfo.fqClassName}> column${columnInfo_index}<#else>Provider<${columnInfo.fqClassName}> column${columnInfo_index}</#if><#if columnInfo_has_next>,
            </#if></#list>) {

        <#list columnInfos as columnInfo>
        <#if columnInfo.async>
        registry.registerColumn("${columnInfo.id}", column${columnInfo_index});
        <#else>
        registry.registerColumn("${columnInfo.id}", column${columnInfo_index});
        </#if>
        </#list>
    }
}
