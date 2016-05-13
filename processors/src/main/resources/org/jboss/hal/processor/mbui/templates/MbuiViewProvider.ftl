<#-- @ftlvariable name="context" type="org.jboss.hal.processor.mbui.MbuiViewContext" -->
package ${context.package};

import com.google.inject.Provider;

import javax.inject.Inject;
import javax.annotation.Generated;

import org.jboss.hal.core.mbui.MbuiContext;

/*
 * WARNING! This class is generated. Do not modify.
 */
@Generated("org.jboss.hal.processor.mbui.MbuiViewGinProcessor")
public class ${context.subclass} implements Provider<${context.base}> {

    private final MbuiContext mbuiContext;
    <#if (context.abstractProperties?size > 0)>
    <#list context.abstractProperties as abstractProperty>
    private final ${abstractProperty.type} ${abstractProperty.field};
    </#list>

    @Inject
    public ${context.subclass}(MbuiContext mbuiContext<#list context.abstractProperties as abstractProperty>, ${abstractProperty.type} ${abstractProperty.field}</#list>) {
        this.mbuiContext = mbuiContext;
        <#list context.abstractProperties as abstractProperty>
        this.${abstractProperty.field} = ${abstractProperty.field};
        </#list>
    }
    <#else>

    @Inject
    public ${context.subclass}(MbuiContext mbuiContext) {
        this.mbuiContext = mbuiContext;
    }
    </#if>

    @Override
    public ${context.base} get() {
        return ${context.base}.${context.createMethod}(mbuiContext<#list context.abstractProperties as abstractProperty>, ${abstractProperty.field}</#list>);
    }
}
