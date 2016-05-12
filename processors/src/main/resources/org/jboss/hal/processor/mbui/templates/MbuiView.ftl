<#-- @ftlvariable name="context" type="org.jboss.hal.processor.mbui.MbuiViewContext" -->
package ${context.package};

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Generated;

import com.google.common.collect.Lists;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import elemental.dom.Element;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.gwt.elemento.core.TemplateUtil;
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
    <#list context.metadataInfos as metadataInfo>
    private final Metadata ${metadataInfo.name};
    </#list>
    <#if context.verticalNavigation??>
    private final Map<String, Element> handlebarElements;
    </#if>

    ${context.subclass}(MetadataRegistry metadataRegistry, StatementContext statementContext, Resources resources<#list context.abstractProperties as abstractProperty>, ${abstractProperty.type} ${abstractProperty.field}</#list>) {
        super(metadataRegistry, statementContext, resources);

        <#list context.abstractProperties as abstractProperty>
        this.${abstractProperty.field} = ${abstractProperty.field};
        </#list>
        <#if context.verticalNavigation??>
        this.handlebarElements = new HashMap<>();
        </#if>
        <#list context.metadataInfos as metadataInfo>
        this.${metadataInfo.name} = metadataRegistry.lookup(AddressTemplate.of("${metadataInfo.template}"));
        </#list>

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
            <#if table.onlySimpleColumns>
            .columns(<#list table.columns as column>"${column.name}"<#if column_has_next>, </#if></#list>)
            <#else>
                <#list table.columns as column>
                    <#if column.simple>
            .column("${column.name}")
                    <#elseif column.simpleWithTitle>
            .column("${column.name}", ${column.title})
                    <#elseif column.hasValue>
            .column("${column.name}", ${column.title}, (cell, type, row, meta) -> ${column.value})
                    </#if>
                </#list>
            </#if>
            .build();
        ${table.name} = new ModelNodeTable<>("${table.selector}", ${table.name}Options);
        </#list>

        <#list context.attachables as attachable>
        registerAttachable(${attachable.name});
        </#list>

        <#if context.verticalNavigation??>
        ${context.verticalNavigation.name} = new VerticalNavigation();
            <#list context.verticalNavigation.items as primaryItem>
                <#if primaryItem.content?has_content>
        Elements.Builder ${primaryItem.name}Builder = new Elements.Builder()
            .div()
                    <#list primaryItem.content as content>
                        <#if content.html??>
                .div()
                    .innerHtml(SafeHtmlUtils.fromSafeConstant("${content.html}"))
                    .rememberAs("${content.name}")
                .end()
                        <#elseif content.reference??>
                .add(${content.reference})
                        </#if>
                    </#list>
            .end();
        Element ${primaryItem.name}Element = ${primaryItem.name}Builder.build();
                    <#list primaryItem.content as content>
                        <#if content.html??>
        handlebarElements.put("${content.name}", ${primaryItem.name}Builder.referenceFor("${content.name}"));
                        </#if>
                    </#list>
        ${context.verticalNavigation.name}.addPrimary("${primaryItem.id}", ${primaryItem.title}<#if primaryItem.icon??>, "${primaryItem.icon}"</#if>, ${primaryItem.name}Element);
                <#elseif primaryItem.subItems?has_content>
        ${context.verticalNavigation.name}.addPrimary("${primaryItem.id}", ${primaryItem.title}<#if primaryItem.icon??>, "${primaryItem.icon}"</#if>);
                    <#list primaryItem.subItems as subItem>
                        <#if subItem.content?has_content>
        Elements.Builder ${subItem.name}Builder = new Elements.Builder()
            .div()
                            <#list subItem.content as content>
                                <#if content.html??>
                .div()
                    .innerHtml(SafeHtmlUtils.fromSafeConstant("${content.html}"))
                    .rememberAs("${content.name}")
                .end()
                                <#elseif content.reference??>
                .add(${content.reference})
                                </#if>
                            </#list>
            .end();
        Element ${subItem.name}Element = ${subItem.name}Builder.build();
                            <#list subItem.content as content>
                                <#if content.html??>
        handlebarElements.put("${content.name}", ${subItem.name}Builder.referenceFor("${content.name}"));
                                </#if>
                            </#list>
        ${context.verticalNavigation.name}.addSecondary("${primaryItem.id}", "${subItem.id}", ${subItem.title}, ${subItem.name}Element);
                        </#if>
                    </#list>
                </#if>
            </#list>
        </#if>

        LayoutBuilder layoutBuilder = new LayoutBuilder()
            .row()
                .column()
                    .addAll(${context.verticalNavigation.name}.panes())
                .end()
            .end();

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

        <#if context.verticalNavigation??>
            <#list context.verticalNavigation.items as primaryItem>
                <#list primaryItem.content as content>
                    <#if content.html??>
                        <#list content.handlebars?keys as handlebar>
        TemplateUtil.replaceHandlebar(handlebarElements.get("${content.name}"), "${handlebar}", String.valueOf(${content.handlebars?values[handlebar_index]}));
                        </#list>
                    </#if>
                </#list>
                <#list primaryItem.subItems as subItem>
                    <#list subItem.content as content>
                        <#if content.html??>
                            <#list content.handlebars?keys as handlebar>
        TemplateUtil.replaceHandlebar(handlebarElements.get("${content.name}"), "${handlebar}", String.valueOf(${content.handlebars?values[handlebar_index]}));
                            </#list>
                        </#if>
                    </#list>
                </#list>
            </#list>
        </#if>
    }
}
