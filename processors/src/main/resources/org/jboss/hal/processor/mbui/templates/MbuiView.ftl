<#-- @ftlvariable name="context" type="org.jboss.hal.processor.mbui.MbuiViewContext" -->
package ${context.package};

import javax.annotation.Generated;

import elemental.dom.Element;
import org.jboss.hal.ballroom.table.DataTable;
import org.jboss.hal.ballroom.table.Options;
import org.jboss.hal.ballroom.LayoutBuilder;
<#if context.verticalNavigation??>
import org.jboss.hal.ballroom.VerticalNavigation;
</#if>
import org.jboss.hal.core.mbui.form.ModelNodeForm;
import org.jboss.hal.core.mbui.table.ModelNodeTable;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.meta.MetadataRegistry;
import org.jboss.hal.resources.Resources;

/*
* WARNING! This class is generated. Do not modify.
*/
@Generated("org.jboss.hal.processor.mbui.MbuiViewProcessor")
final class ${context.subclass} extends ${context.base} {

    <#list context.abstractProperties as abstractProperty>
    private final ${abstractProperty.type} ${abstractProperty.field};
    </#list>

    ${context.subclass}(MetadataRegistry metadataRegistry, Resources resources<#list context.abstractProperties as abstractProperty>, ${abstractProperty.type} ${abstractProperty.field}</#list>) {
        super(metadataRegistry, resources);
        <#list context.abstractProperties as abstractProperty>
        this.${abstractProperty.field} = ${abstractProperty.field};
        </#list>

        <#list context.metadataInfos as metadataInfo>
        Metadata ${metadataInfo.name} = metadataRegistry.lookup(AddressTemplate.of("${metadataInfo.template}"));
        </#list>

        <#if context.verticalNavigation??>
        ${context.verticalNavigation.name} = new VerticalNavigation();
        </#if>

        <#list context.forms as form>
        ${form.name} = new ModelNodeForm.Builder<${form.typeParameter}>("${form.selector}", ${form.metadata.name})
            .build();
        </#list>

        <#list context.dataTables as table>
        Options<${table.typeParameter}> ${table.name}Options = new ModelNodeTable.Builder<${table.typeParameter}>(${table.metadata.name})
            .build();
        ${table.name} = new ModelNodeTable<>("${table.selector}", ${table.name}Options);
        </#list>

        <#list context.attachables as attachable>
        registerAttachable(${attachable.name});
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
