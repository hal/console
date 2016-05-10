<#-- @ftlvariable name="context" type="org.jboss.hal.processor.mbui.MbuiViewContext" -->
package ${context.package};

import javax.annotation.Generated;

import elemental.dom.Element;
import org.jboss.hal.ballroom.LayoutBuilder;

/*
* WARNING! This class is generated. Do not modify.
*/
@Generated("org.jboss.hal.processor.mbui.MbuiViewProcessor")
final class ${context.subclass} extends ${context.base} {

    <#list context.abstractProperties as abstractProperty>
    private final ${abstractProperty.type} ${abstractProperty.field};
    </#list>

    ${context.subclass}(<#list context.abstractProperties as abstractProperty>${abstractProperty.type} ${abstractProperty.field}<#if abstractProperty_has_next>, </#if></#list>) {
        <#list context.abstractProperties as abstractProperty>
        this.${abstractProperty.field} = ${abstractProperty.field};
        </#list>

        // @formatter:off
        LayoutBuilder layoutBuilder = new LayoutBuilder()
            .row()
                .column()
                    .h(1).textContent("Generated MBUI View").end()
                    .p().textContent("Not yet implemented.").end()
                .end()
            .end();
        // @formatter:on

        Element root = layoutBuilder.build();
        initElement(root);

        <#-- @PostConstruct -->
        <#list context.postConstructs as postConstruct>
        ${postConstruct.name}();
        </#list>
    }
    <#-- Abstract properties -->
    <#list context.abstractProperties as abstractProperty>

    @Override
    ${abstractProperty.modifier}${abstractProperty.type} ${abstractProperty.method}() {
    return ${abstractProperty.field};
    }
    </#list>
}
