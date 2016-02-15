<#-- @ftlvariable name="generatedWith" type="java.lang.String" -->
<#-- @ftlvariable name="packageName" type="java.lang.String" -->
<#-- @ftlvariable name="className" type="java.lang.String" -->
<#-- @ftlvariable name="storeInfos" type="java.util.Set<org.jboss.hal.processor.StoreInitProcessor.StoreInfo>" -->
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
        <#list storeInfos as storeInfo>
        bind(${storeInfo.packageName}.${storeInfo.storeDelegate}.class).in(Singleton.class);
        bind(${storeInfo.packageName}.${storeInfo.storeAdapter}.class).asEagerSingleton();
        </#list>
    }
}
