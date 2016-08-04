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
