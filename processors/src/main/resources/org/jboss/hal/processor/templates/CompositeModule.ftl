<#-- @ftlvariable name="generatedWith" type="java.lang.String" -->
<#-- @ftlvariable name="packageName" type="java.lang.String" -->
<#-- @ftlvariable name="className" type="java.lang.String" -->
<#-- @ftlvariable name="modules" type="java.util.Set<String>" -->
package ${packageName};

import javax.annotation.Generated;

import com.google.gwt.inject.client.AbstractGinModule;

/*
 * WARNING! This class is generated. Do not modify.
 */
@Generated("${generatedWith}")
public class ${className} extends AbstractGinModule {

    @Override
    protected void configure() {
        <#list modules as module>
        install(new ${module}());
        </#list>
    }
}
