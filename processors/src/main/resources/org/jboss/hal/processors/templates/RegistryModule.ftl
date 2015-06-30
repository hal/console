<#-- @ftlvariable name="packageName" type="java.lang.String" -->
<#-- @ftlvariable name="className" type="java.lang.String" -->
<#-- @ftlvariable name="bindings" type="java.util.Map<String, String>" -->
package ${packageName};

import javax.annotation.Generated;

import com.google.inject.Singleton;
import com.google.gwt.inject.client.AbstractGinModule;
import org.jboss.hal.spi.GinModule;

/*
* WARNING! This class is generated. Do not modify.
*/
@GinModule
@Generated("org.jboss.hal.processors.NameTokenProcessor")
public class ${className} extends AbstractGinModule {

    @Override
    protected void configure() {
        <#list bindings.entrySet() as binding>
        bind(${binding.key}.class).to(${binding.value}).in(Singleton.class);
        </#list>
    }
}
