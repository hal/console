<#-- @ftlvariable name="context" type="org.jboss.hal.processor.mbui.MbuiViewContext" -->
package ${context.package};

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Generated;
import javax.inject.Inject;

import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import elemental2.dom.HTMLElement;
import org.jboss.gwt.elemento.core.builder.ElementsBuilder;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.gwt.elemento.template.TemplateUtil;
import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.ballroom.table.Scope;
import org.jboss.hal.ballroom.LayoutBuilder;
import org.jboss.hal.ballroom.autocomplete.ReadChildrenAutoComplete;
<#if context.verticalNavigation??>
import org.jboss.hal.ballroom.VerticalNavigation;
</#if>
import org.jboss.hal.core.mbui.dialog.AddResourceDialog;
import org.jboss.hal.core.mbui.form.GroupedForm;
import org.jboss.hal.core.mbui.form.ModelNodeForm;
import org.jboss.hal.core.mbui.table.ModelNodeTable;
import org.jboss.hal.core.mbui.MbuiContext;
import org.jboss.hal.dmr.Operation;
import org.jboss.hal.dmr.ResourceAddress;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.meta.security.Constraint;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.spi.Message;
import org.jboss.hal.spi.MessageEvent;

import static java.util.Arrays.asList;
import static org.jboss.gwt.elemento.core.Elements.*;
import static org.jboss.hal.ballroom.LayoutBuilder.column;
import static org.jboss.hal.ballroom.LayoutBuilder.row;
import static org.jboss.hal.dmr.ModelDescriptionConstants.ADD;
import static org.jboss.hal.dmr.ModelDescriptionConstants.READ_RESOURCE_OPERATION;

/*
 * WARNING! This class is generated. Do not modify.
 */
@Generated("org.jboss.hal.processor.mbui.MbuiViewProcessor")
public final class ${context.subclass} extends ${context.base} {

    <#list context.abstractProperties as abstractProperty>
    private final ${abstractProperty.type} ${abstractProperty.field};
    </#list>
    <#list context.metadataInfos as metadataInfo>
    private final Metadata ${metadataInfo.name};
    </#list>
    private final Map<String, HTMLElement> expressionElements;

    @Inject
    @SuppressWarnings("unchecked")
    public ${context.subclass}(MbuiContext mbuiContext<#list context.abstractProperties as abstractProperty>, ${abstractProperty.type} ${abstractProperty.field}</#list>) {
        super(mbuiContext);

        <#list context.abstractProperties as abstractProperty>
        this.${abstractProperty.field} = ${abstractProperty.field};
        </#list>
        <#list context.metadataInfos as metadataInfo>
        AddressTemplate ${metadataInfo.name}Template = AddressTemplate.of("${metadataInfo.template}");
        this.${metadataInfo.name} = mbuiContext.metadataRegistry().lookup(${metadataInfo.name}Template);
        </#list>
        this.expressionElements = new HashMap<>();

        <#list context.forms as form>
            <#if form.groups?has_content>
        ${form.name} = new GroupedForm.Builder<${form.typeParameter.type}>("${form.selector}", ${form.metadata.name})
                <#list form.groups as group>
                    <#if group.attributes?has_content || group.excludes?has_content>
            .customGroup("${group.id}", ${group.title})
                        <#if group.hasAttributesWithProvider>
                            <#list group.attributes as attribute>
                                <#if attribute.provider??>
                .customFormItem("${attribute.name}", attributeDescription -> ${attribute.provider})
                                <#else>
                .include("${attribute.name}")
                                </#if>
                            </#list>
                        <#elseif group.hasUnboundAttributes>
                            <#list group.attributes as attribute>
                                <#if attribute.formItem??>
                .unboundFormItem(${attribute.formItem}, ${attribute_index})
                                <#else>
                .include("${attribute.name}")
                                </#if>
                            </#list>
                        <#else>
                .include(<#list group.attributes as attribute>"${attribute.name}"<#if attribute_has_next>, </#if></#list>)
                        </#if>
                        <#if group.excludes?has_content>
                .exclude(<#list group.excludes as exclude>"${exclude}"<#if exclude_has_next>, </#if></#list>)
                        </#if>
            .end()
                    <#else>
            .attributeGroup(<#if group.id??>"${group.id}", </#if>"${group.name}"<#if group.title??>, ${group.title}</#if>).end()
                    </#if>
                </#list>
            <#else>
        ${form.name} = new ModelNodeForm.Builder<${form.typeParameter.type}>("${form.selector}", ${form.metadata.name})
                <#if form.singleton>
            .singleton(
                () -> new Operation.Builder(${form.metadata.name}Template.resolve(mbuiContext.statementContext()), READ_RESOURCE_OPERATION).build(),
                <#if form.metadata.singleton>
                    <#if form.addHandler??>
                () -> ${form.addHandler})
                    <#else>
                () -> addSingleton("${form.selector}", ${form.title}, ${form.metadata.name}Template))
                    </#if>
                <#else>
                () -> add("${form.selector}", ${form.title}, ${form.metadata.name}Template))
                </#if>
            .prepareRemove(form -> removeSingletonForm(${form.title}, ${form.metadata.name}Template.resolve(mbuiContext.statementContext()), form))
                </#if>
                <#if form.includeRuntime>
            .includeRuntime()
                </#if>
                <#if form.attributes?has_content>
                    <#if form.hasAttributesWithProvider>
                        <#list form.attributes as attribute>
                            <#if attribute.provider??>
            .customFormItem("${attribute.name}", attributeDescription -> ${attribute.provider})
                            <#else>
            .include("${attribute.name}")
                            </#if>
                        </#list>
                    <#elseif form.hasUnboundAttributes>
                        <#list form.attributes as attribute>
                            <#if attribute.formItem??>
            .unboundFormItem(${attribute.formItem}, ${attribute_index})
                            <#else>
            .include("${attribute.name}")
                            </#if>
                        </#list>
                    <#else>
            .include(<#list form.attributes as attribute>"${attribute.name}"<#if attribute_has_next>, </#if></#list>)
                    </#if>
            .unsorted()
                </#if>
            </#if>
            <#if form.autoSave>
                <#if form.nameResolver??>
            .onSave((form, changedValues) -> {
                String name = ${form.nameResolver};
                saveForm(${form.title}, name, ${form.metadata.name}Template.resolve(mbuiContext.statementContext(), name), changedValues, ${form.metadata.name});
            })
                <#else>
            .onSave((form, changedValues) -> saveSingletonForm(${form.title}, ${form.metadata.name}Template.resolve(mbuiContext.statementContext()), changedValues, ${form.metadata.name}))
                </#if>
            <#elseif form.onSave??>
            .onSave((form, changedValues) -> ${form.onSave})
            </#if>
            <#if form.reset>
                <#if form.nameResolver??>
            .prepareReset(form -> {
                String name = ${form.nameResolver};
                resetForm(${form.title}, name, ${form.metadata.name}Template.resolve(mbuiContext.statementContext(), name), form, ${form.metadata.name});
            })
                <#else>
            .prepareReset(form -> resetSingletonForm(${form.title}, ${form.metadata.name}Template.resolve(mbuiContext.statementContext()), form, ${form.metadata.name}))
                </#if>
            <#elseif form.prepareReset??>
            .prepareReset(form -> ${form.prepareReset})
            </#if>
            .build();
            <#list form.validationHandlerAttributes as attribute>
        ${form.name}.getFormItem("${attribute.name}").addValidationHandler(${attribute.validationHandler});
            </#list>
            <#list form.suggestHandlerAttributes as attribute>
                <#if attribute.suggestHandler??>
        ${form.name}.getFormItem("${attribute.name}").registerSuggestHandler(${attribute.suggestHandler});
                <#elseif attribute.suggestHandlerTemplates?size == 1>
        ${form.name}.getFormItem("${attribute.name}").registerSuggestHandler(new ReadChildrenAutoComplete(
            mbuiContext.dispatcher(), mbuiContext.statementContext(), AddressTemplate.of("${attribute.suggestHandlerTemplates[0]}")));
                <#else>
        List<AddressTemplate> ${form.name}Templates = asList(<#list attribute.suggestHandlerTemplates as template>
            AddressTemplate.of("${template}")<#if template_has_next>, </#if></#list>);
        ${form.name}.getFormItem("${attribute.name}").registerSuggestHandler(new ReadChildrenAutoComplete(
                    mbuiContext.dispatcher(), mbuiContext.statementContext(), ${form.name}Templates));
                </#if>
            </#list>
        </#list>

        <#list context.dataTables as table>
        ${table.name} = new ModelNodeTable.Builder<${table.typeParameter.type}>("${table.selector}", ${table.metadata.name})
            <#list table.actions as action>
                <#if action.knownHandler>
                    <#switch action.handlerRef>
                        <#case "ADD_RESOURCE">
                            <#if action.attributes?has_content>
                                <#if action.hasAttributesWithProvider || action.hasUnboundAttributes>
            .button(mbuiContext.tableButtonFactory().add(${table.metadata.name}Template, table -> {
                ModelNodeForm form = new ModelNodeForm.Builder(Ids.build("${table.selector}", Ids.ADD),
                    ${table.metadata.name})
                    .fromRequestProperties()
                    .unboundFormItem(new org.jboss.hal.core.mbui.dialog.NameItem(), 0)
                                    <#list action.attributes as attribute>
                                        <#if attribute.provider??>
                    .customFormItem("${attribute.name}", attributeDescription -> ${attribute.provider})
                                        <#elseif attribute.formItem??>
                    .unboundFormItem(${attribute.formItem}, ${attribute_index})
                                        <#else>
                    .include("${attribute.name}")
                                        </#if>
                                    </#list>
                    .unsorted()
                    .build();
                                    <#list action.validationHandlerAttributes as attribute>
                form.getFormItem("${attribute.name}").addValidationHandler(${attribute.validationHandler});
                                    </#list>
                                    <#list action.suggestHandlerAttributes as attribute>
                                        <#if attribute.suggestHandler??>
                form.getFormItem("${attribute.name}").registerSuggestHandler(${attribute.suggestHandler});
                                        <#elseif attribute.suggestHandlerTemplates?size == 1>
                form.getFormItem("${attribute.name}").registerSuggestHandler(new ReadChildrenAutoComplete(
                                            mbuiContext.dispatcher(), mbuiContext.statementContext(), AddressTemplate.of("${attribute.suggestHandlerTemplates[0]}")));
                                        <#else>
                List<AddressTemplate> ${table.name}Templates = asList(<#list attribute.suggestHandlerTemplates as template>
                    AddressTemplate.of("${template}")<#if template_has_next>, </#if></#list>);
                form.getFormItem("${attribute.name}").registerSuggestHandler(new ReadChildrenAutoComplete(
                                            mbuiContext.dispatcher(), mbuiContext.statementContext(), ${table.name}Templates));
                                        </#if>
                                    </#list>
                AddResourceDialog dialog = new AddResourceDialog(
                    mbuiContext.resources().messages().addResourceTitle(${table.title}),
                    form,
                    (name, modelNode) -> {
                        ResourceAddress address = ${table.metadata.name}Template.resolve(mbuiContext.statementContext(), name);
                        mbuiContext.crud().add(${table.title}, name, address, modelNode, (n, a) -> presenter.reload());
                    });
                dialog.show();
            }))
                                <#elseif action.hasAttributesWithValidationsHandler || action.hasAttributesWithSuggestionHandler>
            .button(mbuiContext.tableButtonFactory().add(${table.metadata.name}Template, table -> {
                AddResourceDialog dialog = new AddResourceDialog(
                    Ids.build("${table.selector}", Ids.ADD),
                    mbuiContext.resources().messages().addResourceTitle(${table.title}),
                    ${table.metadata.name},
                    asList(<#list action.attributes as attribute>"${attribute.name}"<#if attribute_has_next>, </#if></#list>),
                    (name, modelNode) -> {
                        ResourceAddress address = ${table.metadata.name}Template.resolve(mbuiContext.statementContext(), name);
                        mbuiContext.crud().add(${table.title}, name, address, modelNode, (n, a) -> presenter.reload());
                    });
                                    <#list action.validationHandlerAttributes as attribute>
                dialog.getForm().getFormItem("${attribute.name}").addValidationHandler(${attribute.validationHandler});
                                    </#list>
                                    <#list action.suggestHandlerAttributes as attribute>
                                        <#if attribute.suggestHandler??>
                dialog.getForm().getFormItem("${attribute.name}").registerSuggestHandler(${attribute.suggestHandler});
                                        <#elseif attribute.suggestHandlerTemplates?size == 1>
                dialog.getForm().getFormItem("${attribute.name}").registerSuggestHandler(new ReadChildrenAutoComplete(
                                            mbuiContext.dispatcher(), mbuiContext.statementContext(), AddressTemplate.of("${attribute.suggestHandlerTemplates[0]}")));
                                        <#else>
                List<AddressTemplate> ${table.name}Templates = asList(<#list attribute.suggestHandlerTemplates as template>
                    AddressTemplate.of("${template}")<#if template_has_next>, </#if></#list>);
                dialog.getForm().getFormItem("${attribute.name}").registerSuggestHandler(new ReadChildrenAutoComplete(
                                            mbuiContext.dispatcher(), mbuiContext.statementContext(), ${table.name}Templates));
                                        </#if>
                                    </#list>
                dialog.show();
            }))
                                <#else>
            .button(mbuiContext.tableButtonFactory().add(Ids.build("${table.selector}", Ids.ADD), ${table.title},
                ${table.metadata.name}Template, <#if action.attributes?has_content>asList(<#list action.attributes as attribute>"${attribute.name}"<#if attribute_has_next>, </#if></#list>), </#if>(name, address) -> presenter.reload()))
                                </#if>
                            <#else>
            .button(mbuiContext.tableButtonFactory().add(Ids.build("${table.selector}", Ids.ADD), ${table.title},
                ${table.metadata.name}Template,
                (name, address) -> presenter.reload()))
                            </#if>
                            <#break>
                        <#case "REMOVE_RESOURCE">
            .button(mbuiContext.tableButtonFactory().remove(${table.title}, ${table.metadata.name}Template,
                table -> ${action.nameResolver},
                () -> presenter.reload()))
                            <#break>
                    </#switch>
                <#else>
            .button(${action.title}, table -> ${action.handler}<#if action.scope??>, Scope.${action.scope}</#if><#if action.constraint??>, Constraint.parse("${action.constraint}")</#if>)
                </#if>
            </#list>
            <#if table.onlySimpleColumns>
            .columns(<#list table.columns as column>"${column.name}"<#if column_has_next>, </#if></#list>)
            <#else>
                <#list table.columns as column>
                    <#if column.value??>
            .column("${column.name}", (cell, type, row, meta) -> SafeHtmlUtils.fromString(${column.value}).asString())
                    <#else>
            .column("${column.name}")
                    </#if>
                </#list>
            </#if>
            .build();
        </#list>

        <#if context.verticalNavigation??>
        ${context.verticalNavigation.name} = new VerticalNavigation();
            <#list context.verticalNavigation.items as primaryItem>
                <#if primaryItem.content?has_content>
                    <#if primaryItem.htmlContent?has_content>
        HTMLElement <#list primaryItem.htmlContent as htmlContent>${htmlContent.name}<#if htmlContent_has_next>, </#if></#list>;
                    </#if>
        HTMLElement ${primaryItem.name}Element = section()
                    <#list primaryItem.content as content>
                        <#if content.html??>
            .add(${content.name} = div()
                .innerHtml(SafeHtmlUtils.fromSafeConstant("${content.html}"))
                .asElement())
                        <#elseif content.reference??>
            .add(${content.reference})
                        </#if>
                    </#list>
            .asElement();
                    <#list primaryItem.htmlContent as htmlContent>
        expressionElements.put("${htmlContent.name}", ${htmlContent.name});
                    </#list>
        ${context.verticalNavigation.name}.addPrimary("${primaryItem.id}", ${primaryItem.title}<#if primaryItem.icon??>, "${primaryItem.icon}"</#if>, ${primaryItem.name}Element);
                <#elseif primaryItem.subItems?has_content>
        ${context.verticalNavigation.name}.addPrimary("${primaryItem.id}", ${primaryItem.title}<#if primaryItem.icon??>, "${primaryItem.icon}"</#if>);
                    <#list primaryItem.subItems as subItem>
                        <#if subItem.content?has_content>
                            <#if subItem.htmlContent?has_content>
        HTMLElement <#list subItem.htmlContent as htmlContent>${htmlContent.name}<#if htmlContent_has_next>, </#if></#list>;
                            </#if>
        HTMLElement ${subItem.name}Element = section()
                            <#list subItem.content as content>
                                <#if content.html??>
            .add(${content.name} = div()
                .innerHtml(SafeHtmlUtils.fromSafeConstant("${content.html}"))
                .asElement())
                                <#elseif content.reference??>
            .add(${content.reference})
                                </#if>
                            </#list>
            .asElement();
                            <#list subItem.htmlContent as htmlContent>
        expressionElements.put("${htmlContent.name}", ${htmlContent.name});
                            </#list>
        ${context.verticalNavigation.name}.addSecondary("${primaryItem.id}", "${subItem.id}", ${subItem.title}, ${subItem.name}Element);
                        </#if>
                    </#list>
                </#if>
            </#list>
        HTMLElement root = row()
            .add(column()
                .addAll(${context.verticalNavigation.name}.panes()))
            .asElement();
        <#else>
            <#if context.content?has_content>
                <#if context.htmlContent?has_content>
        HTMLElement <#list context.htmlContent as htmlContent>${htmlContent.name}<#if htmlContent_has_next>, </#if></#list>;
                </#if>
        HTMLElement root = row()
            .add(column()
                <#list context.content as content>
                    <#if content.html??>
                .add(${content.name} = div()
                    .innerHtml(SafeHtmlUtils.fromSafeConstant("${content.html}"))
                    .asElement())
                    <#elseif content.reference??>
                .add(${content.reference})
                    </#if>
                </#list>)
            .asElement();
                <#list context.htmlContent as htmlContent>
        expressionElements.put("${htmlContent.name}", ${htmlContent.name});
                </#list>
            <#else>
        HTMLElement root = row()
            .add(
                column()
                    .add(h(1).textContent("${context.base}"))
                    .add(p().textContent(org.jboss.hal.resources.Names.NYI)))
            .asElement();
            </#if>
        </#if>

        <#list context.attachables as attachable>
        registerAttachable(${attachable.name});
        </#list>

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
            ${table.name}.bindForm(${table.formRef.name});
            </#if>
        </#list>

        <#if context.verticalNavigation??>
            <#list context.verticalNavigation.items as primaryItem>
                <#list primaryItem.content as content>
                    <#if content.html??>
                        <#list content.handlebars?keys as handlebar>
        TemplateUtil.replaceExpression(expressionElements.get("${content.name}"), "${handlebar}", String.valueOf(${content.handlebars?values[handlebar_index]}));
                        </#list>
                    </#if>
                </#list>
                <#list primaryItem.subItems as subItem>
                    <#list subItem.content as content>
                        <#if content.html??>
                            <#list content.handlebars?keys as handlebar>
        TemplateUtil.replaceExpression(expressionElements.get("${content.name}"), "${handlebar}", String.valueOf(${content.handlebars?values[handlebar_index]}));
                            </#list>
                        </#if>
                    </#list>
                </#list>
            </#list>
        <#else>
            <#list context.content as content>
                <#if content.html??>
                    <#list content.handlebars?keys as handlebar>
        TemplateUtil.replaceExpression(expressionElements.get("${content.name}"), "${handlebar}", String.valueOf(${content.handlebars?values[handlebar_index]}));
                    </#list>
                </#if>
            </#list>
        </#if>
    }
}
