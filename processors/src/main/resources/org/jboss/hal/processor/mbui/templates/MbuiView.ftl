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
import org.jboss.hal.core.mbui.MbuiContext;
import org.jboss.hal.core.mbui.dialog.AddResourceDialog;
import org.jboss.hal.core.mbui.form.ModelNodeForm;
import org.jboss.hal.core.mbui.table.ModelNodeTable;
import org.jboss.hal.dmr.model.Composite;
import org.jboss.hal.dmr.model.CompositeResult;
import org.jboss.hal.dmr.model.Operation;
import org.jboss.hal.dmr.model.ResourceAddress;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.resources.IdBuilder;
import org.jboss.hal.spi.Message;
import org.jboss.hal.spi.MessageEvent;

import static java.util.Arrays.asList;
import static org.jboss.hal.dmr.ModelDescriptionConstants.ADD;

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
    private final Map<String, Element> handlebarElements;

    ${context.subclass}(MbuiContext mbuiContext<#list context.abstractProperties as abstractProperty>, ${abstractProperty.type} ${abstractProperty.field}</#list>) {
        super(mbuiContext);

        <#list context.abstractProperties as abstractProperty>
        this.${abstractProperty.field} = ${abstractProperty.field};
        </#list>
        <#list context.metadataInfos as metadataInfo>
        AddressTemplate ${metadataInfo.name}Template = AddressTemplate.of("${metadataInfo.template}");
        this.${metadataInfo.name} = mbuiContext.metadataRegistry().lookup(${metadataInfo.name}Template);
        </#list>
        this.handlebarElements = new HashMap<>();

        <#list context.forms as form>
        ${form.name} = new ModelNodeForm.Builder<${form.typeParameter}>("${form.selector}", ${form.metadata.name})
            <#if form.attributes?has_content>
                <#if form.hasAttributesWithProvider>
                    <#list form.attributes as attribute>
                        <#if attribute.provider??>
            .customFormItem("${attribute.name}", ${attribute.provider})
                        <#else>
            .include("${attribute.name}")
                        </#if>
                    </#list>
                <#else>
            .include(<#list form.attributes as attribute>"${attribute.name}"<#if attribute_has_next>, </#if></#list>)
                </#if>
            .unsorted()
            </#if>
            <#if form.autoSave>
                <#if form.nameResolver??>
            .onSave((form, changedValues) -> {
                ResourceAddress address = ${form.metadata.name}Template.resolve(mbuiContext.statementContext(), ${form.nameResolver});
                Composite composite = mbuiContext.operationFactory().fromChangeSet(address, changedValues);
                mbuiContext.dispatcher().execute(composite, (CompositeResult result) -> {
                    presenter.reload();
                    MessageEvent.fire(mbuiContext.eventBus(), Message.success(mbuiContext.resources().messages().modifyResourceSuccess(${form.title}, ${form.nameResolver})));
                });
            })
                <#else>
            .onSave((form, changedValues) -> {
                ResourceAddress address = ${form.metadata.name}Template.resolve(mbuiContext.statementContext());
                Composite composite = mbuiContext.operationFactory().fromChangeSet(address, changedValues);
                mbuiContext.dispatcher().execute(composite, (CompositeResult result) -> {
                    presenter.reload();
                    MessageEvent.fire(mbuiContext.eventBus(), Message.success(mbuiContext.resources().messages().modifySingleResourceSuccess(${form.title})));
                });
            })
                </#if>
            <#elseif form.onSave??>
            .onSave((form, changedValues) -> ${form.onSave})
            </#if>
            .build();
            <#list form.suggestHandlerAttributes as attribute>
                <#if attribute.suggestHandlerTemplates?size == 1>
        ResourceAddress ${form.name}Address = AddressTemplate.of("${attribute.suggestHandlerTemplates[0]}").resolve(mbuiContext.statementContext());
                <#else>
                </#if>
        List<AddressTemplate> ${form.name}Templates = asList(<#list attribute.suggestHandlerTemplates as template>
            AddressTemplate.of("${template}")<#if template_has_next>, </#if></#list>);
        List<ResourceAddress> ${form.name}Address = Lists.transform(${form.name}Templates, template -> template.resolve(mbuiContext.statementContext()));
        ${form.name}.getFormItem("${attribute.name}").registerSuggestHandler(new TypeaheadProvider().from(${form.name}Address));
            </#list>
        </#list>

        <#list context.dataTables as table>
        Options<${table.typeParameter}> ${table.name}Options = new ModelNodeTable.Builder<${table.typeParameter}>(${table.metadata.name})
            <#list table.actions as action>
                <#if action.knownHandler>
                    <#switch action.handlerRef>
                        <#case "ADD_RESOURCE">
                            <#if action.attributes?has_content>
                                <#if action.hasAttributesWithProvider>
            .button(mbuiContext.resources().constants().add(), (event, api) -> {
                ModelNodeForm form = new ModelNodeForm.Builder(IdBuilder.build("${table.selector}", "add"),
                    ${table.metadata.name})
                    .addFromRequestProperties()
                    .unboundFormItem(new org.jboss.hal.core.mbui.dialog.NameItem(), 0)
                                    <#list action.attributes as attribute>
                                        <#if attribute.provider??>
                    .customFormItem("${attribute.name}", ${attribute.provider})
                                        <#else>
                    .include("${attribute.name}")
                                        </#if>
                                    </#list>
                    .unsorted()
                    .build();
                                    <#if action.hasAttributesWithSuggestionHandlers>
                                        <#list action.suggestHandlerAttributes as attribute>
                                            <#if attribute.suggestHandlerTemplates?size == 1>
                ResourceAddress ${table.name}Address = AddressTemplate.of("${attribute.suggestHandlerTemplates[0]}").resolve(mbuiContext.statementContext());
                                            <#else>
                List<AddressTemplate> ${table.name}Templates = asList(<#list attribute.suggestHandlerTemplates as template>
                        AddressTemplate.of("${template}")<#if template_has_next>, </#if></#list>);
                List<ResourceAddress> ${table.name}Address = Lists.transform(${table.name}Templates, template -> template.resolve(mbuiContext.statementContext()));
                                            </#if>
                form.getFormItem("${attribute.name}").registerSuggestHandler(new TypeaheadProvider().from(${table.name}Address));
                                        </#list>
                                    </#if>
                AddResourceDialog dialog = new AddResourceDialog(
                    mbuiContext.resources().messages().addResourceTitle(${table.title}),
                    form,
                    (name, modelNode) -> {
                        ResourceAddress address = ${table.metadata.name}Template.resolve(mbuiContext.statementContext(), name);
                        Operation operation = new Operation.Builder(ADD, address).payload(modelNode).build();
                        mbuiContext.dispatcher().execute(operation, result -> {
                            presenter.reload();
                            MessageEvent.fire(mbuiContext.eventBus(), Message.success(
                            mbuiContext.resources().messages().addResourceSuccess(${table.title}, name)));
                        });
                    });
                dialog.show();
            })
                                <#elseif action.hasAttributesWithSuggestionHandlers>
            .button(mbuiContext.resources().constants().add(), (event, api) -> {
                AddResourceDialog dialog = new AddResourceDialog(
                    IdBuilder.build("${table.selector}", "add"),
                    mbuiContext.resources().messages().addResourceTitle(${table.title}),
                    ${table.metadata.name},
                    asList(<#list action.attributes as attribute>"${attribute.name}"<#if attribute_has_next>, </#if></#list>),
                    (name, modelNode) -> {
                        ResourceAddress address = ${table.metadata.name}Template.resolve(mbuiContext.statementContext(), name);
                        Operation operation = new Operation.Builder(ADD, address).payload(modelNode).build();
                        mbuiContext.dispatcher().execute(operation, result -> {
                            presenter.reload();
                            MessageEvent.fire(mbuiContext.eventBus(), Message.success(
                                mbuiContext.resources().messages().addResourceSuccess(${table.title}, name)));
                        });
                    });
                                    <#list action.suggestHandlerAttributes as attribute>
                                        <#if attribute.suggestHandlerTemplates?size == 1>
                ResourceAddress ${table.name}Address = AddressTemplate.of("${attribute.suggestHandlerTemplates[0]}").resolve(mbuiContext.statementContext());
                                        <#else>
                List<AddressTemplate> ${table.name}Templates = asList(<#list attribute.suggestHandlerTemplates as template>
                    AddressTemplate.of("${template}")<#if template_has_next>, </#if></#list>);
                List<ResourceAddress> ${table.name}Address = Lists.transform(${table.name}Templates, template -> template.resolve(mbuiContext.statementContext()));
                                        </#if>
                dialog.getForm().getFormItem("${attribute.name}").registerSuggestHandler(new TypeaheadProvider().from(${table.name}Address));
                                    </#list>
                dialog.show();
            })
                                <#else>
            .button(mbuiContext.tableButtonFactory().add(IdBuilder.build("${table.selector}", "add"), ${table.title},
                ${table.metadata.name}Template,
                () -> presenter.reload(),
                                    <#list action.attributes as attribute>
                "${attribute.name}"<#if attribute_has_next>, <#else>))</#if>
                                    </#list>
                                </#if>
                            <#else>
            .button(mbuiContext.tableButtonFactory().add(IdBuilder.build("${table.selector}", "add"), ${table.title},
                ${table.metadata.name}Template,
                () -> presenter.reload()))
                            </#if>
                            <#break>
                        <#case "REMOVE_RESOURCE">
            .button(mbuiContext.tableButtonFactory().remove(${table.title}, ${table.metadata.name}Template,
                (api) -> ${action.nameResolver},
                () -> presenter.reload()))
                            <#break>
                    </#switch>
                <#else>
            .button(${action.title}, <#if action.scope??>Button.Scope.${action.scope}, </#if>(event, api) -> ${action.handler})
                </#if>
            </#list>
            <#if table.onlySimpleColumns>
            .columns(<#list table.columns as column>"${column.name}"<#if column_has_next>, </#if></#list>)
            <#else>
                <#list table.columns as column>
                    <#if column.value??>
            .column("${column.name}", (cell, type, row, meta) -> ${column.value})
                    <#else>
            .column("${column.name}")
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

        LayoutBuilder layoutBuilder = new LayoutBuilder()
            .row()
                .column()
                    .addAll(${context.verticalNavigation.name}.panes())
                .end()
            .end();
        <#else>
            <#if context.content?has_content>
        LayoutBuilder layoutBuilder = new LayoutBuilder()
            .row()
                .column()
                <#list context.content as content>
                    <#if content.html??>
                    .div()
                        .innerHtml(SafeHtmlUtils.fromSafeConstant("${content.html}"))
                        .rememberAs("${content.name}")
                    .end()
                    <#elseif content.reference??>
                    .add(${content.reference})
                    </#if>
                </#list>
                .end()
            .end();
                <#list context.content as content>
                    <#if content.html??>
            handlebarElements.put("${content.name}", layoutBuilder.referenceFor("${content.name}"));
                    </#if>
                </#list>
            <#else>
        LayoutBuilder layoutBuilder = new LayoutBuilder()
            .row()
                .column()
                    .h(1).textContent("${context.base}").end()
                    .p().textContent(org.jboss.hal.resources.Names.NYI).end()
                .end()
            .end();
            </#if>
        </#if>

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
        <#else>
            <#list context.content as content>
                <#if content.html??>
                    <#list content.handlebars?keys as handlebar>
        TemplateUtil.replaceHandlebar(handlebarElements.get("${content.name}"), "${handlebar}", String.valueOf(${content.handlebars?values[handlebar_index]}));
                    </#list>
                </#if>
            </#list>
        </#if>
    }
}
