<#-- @ftlvariable name="context" type="org.jboss.hal.processor.mbui.MbuiViewContext" -->
package ${context.package};

import java.util.List;
import javax.annotation.Generated;

import com.google.common.collect.Lists;
import elemental.dom.Element;
import org.jboss.hal.ballroom.table.Options;
import org.jboss.hal.ballroom.LayoutBuilder;
import org.jboss.hal.ballroom.typeahead.TypeaheadProvider;
<#if context.verticalNavigation??>
import org.jboss.hal.ballroom.VerticalNavigation;
</#if>
import org.jboss.hal.core.mbui.form.ModelNodeForm;
import org.jboss.hal.core.mbui.table.ModelNodeTable;
import org.jboss.hal.dmr.model.ResourceAddress;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.meta.MetadataRegistry;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.resources.Resources;

import static java.util.Arrays.asList;

/*
 * WARNING! This class is generated. Do not modify.
 */
@Generated("org.jboss.hal.processor.mbui.MbuiViewProcessor")
final class ${context.subclass} extends ${context.base} {

    <#list context.abstractProperties as abstractProperty>
    private final ${abstractProperty.type} ${abstractProperty.field};
    </#list>

    ${context.subclass}(MetadataRegistry metadataRegistry, StatementContext statementContext, Resources resources<#list context.abstractProperties as abstractProperty>, ${abstractProperty.type} ${abstractProperty.field}</#list>) {
        super(metadataRegistry, statementContext, resources);
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
            <#if form.attributes?has_content>
            .include(<#list form.attributes as attribute>"${attribute.name}"<#if attribute_has_next>, </#if></#list>)
            </#if>
            .build();
            <#list form.suggestHandlerAttributes as attribute>
                <#if attribute.suggestHandlerTemplates?size == 1>
        ResourceAddress ${form.name}Address = AddressTemplate.of("${attribute.suggestHandlerTemplates[0]}").resolve(statementContext);
                <#else>
                </#if>
        List<AddressTemplate> ${form.name}Templates = asList(<#list attribute.suggestHandlerTemplates as template>
                AddressTemplate.of("${template}")<#if template_has_next>, </#if></#list>);
        List<ResourceAddress> ${form.name}Address = Lists.transform(${form.name}Templates, template -> template.resolve(statementContext));
        ${form.name}.getFormItem("${attribute.name}").registerSuggestHandler(new TypeaheadProvider().from(${form.name}Address));
            </#list>
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

    @Override
    public void attach() {
        super.attach();
        <#list context.dataTables as table>
            <#if table.formRef??>
        ${table.name}.api().bindForm(${table.formRef.name});
            </#if>
        </#list>
    }
}
