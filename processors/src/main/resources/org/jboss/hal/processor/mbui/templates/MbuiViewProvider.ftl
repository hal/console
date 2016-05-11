<#-- @ftlvariable name="context" type="org.jboss.hal.processor.mbui.MbuiViewContext" -->
package ${context.package};

import com.google.inject.Provider;

import javax.inject.Inject;
import javax.annotation.Generated;

import org.jboss.hal.meta.MetadataRegistry;
import org.jboss.hal.resources.Resources;

/*
* WARNING! This class is generated. Do not modify.
*/
@Generated("org.jboss.hal.processor.mbui.MbuiViewGinProcessor")
public class ${context.subclass} implements Provider<${context.base}> {

    private final MetadataRegistry metadataRegistry;
    private final Resources resources;
    <#if (context.abstractProperties?size > 0)>
    <#list context.abstractProperties as abstractProperty>
    private final ${abstractProperty.type} ${abstractProperty.field};
    </#list>

    @Inject
    public ${context.subclass}(MetadataRegistry metadataRegistry, Resources resources, <#list context.abstractProperties as abstractProperty>${abstractProperty.type} ${abstractProperty.field}<#if abstractProperty_has_next>, </#if></#list>) {
        this.metadataRegistry = metadataRegistry;
        this.resources = resources;
        <#list context.abstractProperties as abstractProperty>
        this.${abstractProperty.field} = ${abstractProperty.field};
        </#list>
    }
    <#else>

    @Inject
    public ${context.subclass}(MetadataRegistry metadataRegistry, Resources resources) {
        this.metadataRegistry = metadataRegistry;
        this.resources = resources;
    }
    </#if>

    @Override
    public ${context.base} get() {
        return ${context.base}.${context.createMethod}(metadataRegistry, resources<#list context.abstractProperties as abstractProperty>, ${abstractProperty.field}</#list>);
    }
}
