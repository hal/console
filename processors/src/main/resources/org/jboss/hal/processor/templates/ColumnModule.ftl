<#-- @ftlvariable name="generatedWith" type="java.lang.String" -->
<#-- @ftlvariable name="packageName" type="java.lang.String" -->
<#-- @ftlvariable name="className" type="java.lang.String" -->
<#-- @ftlvariable name="columnInitClassName" type="java.lang.String" -->
package ${packageName};

import javax.annotation.Generated;

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
        bind(${columnInitClassName}.class).asEagerSingleton();
    }
}
