<#-- @ftlvariable name="context" type="org.jboss.hal.processor.mbui.MbuiViewContext" -->
package ${context.package};

import com.google.inject.Provider;

import javax.inject.Inject;
import javax.annotation.Generated;

import org.jboss.hal.core.ui.UIContext;

/*
 * WARNING! This class is generated. Do not modify.
 */
@Generated("org.jboss.hal.processor.mbui.MbuiViewGinProcessor")
public class ${context.subclass} implements Provider<${context.base}> {

    private final UIContext uic;
    <#if (context.abstractProperties?size > 0)>
    <#list context.abstractProperties as abstractProperty>
    private final ${abstractProperty.type} ${abstractProperty.field};
    </#list>

    @Inject
    public ${context.subclass}(UIContext uic<#list context.abstractProperties as abstractProperty>, ${abstractProperty.type} ${abstractProperty.field}</#list>) {
        this.uic = uic;
        <#list context.abstractProperties as abstractProperty>
        this.${abstractProperty.field} = ${abstractProperty.field};
        </#list>
    }
    <#else>

    @Inject
    public ${context.subclass}(UIContext uic) {
        this.uic = uic;
    }
    </#if>

    @Override
    public ${context.base} get() {
        return ${context.base}.${context.createMethod}(uic<#list context.abstractProperties as abstractProperty>, ${abstractProperty.field}</#list>);
    }
}
