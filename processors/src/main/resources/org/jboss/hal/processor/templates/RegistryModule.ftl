<#-- @ftlvariable name="generatedWith" type="java.lang.String" -->
<#-- @ftlvariable name="packageName" type="java.lang.String" -->
<#-- @ftlvariable name="className" type="java.lang.String" -->
<#-- @ftlvariable name="bindings" type="java.util.List<org.jboss.hal.processor.RegistryBinding>" -->
package ${packageName};

import javax.annotation.Generated;

import com.google.inject.Singleton;
import com.google.gwt.inject.client.AbstractGinModule;

import org.jboss.hal.spi.GinModule;

/*
 * WARNING! This class is generated. Do not modify.
 */
@GinModule
@Generated("${generatedWith}")
public class ${className} extends AbstractGinModule {

    @Override
    protected void configure() {
        <#list bindings as binding>
        bind(${binding.interface}.class).to(${binding.implementation}.class).in(Singleton.class);
        </#list>
    }
}
