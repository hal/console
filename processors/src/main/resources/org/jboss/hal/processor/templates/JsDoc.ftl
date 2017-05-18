<#-- @ftlvariable name="generatedWith" type="java.lang.String" -->
<#-- @ftlvariable name="namespace" type="java.lang.String" -->
<#-- @ftlvariable name="jsTypes" type="java.util.Collection<org.jboss.hal.processor.JsDocProcessor.JsTypeInfo>" -->
/*
 * WARNING! This file is generated with ${generatedWith}. Do not modify.
 */

<#list jsTypes as jsType>
class ${jsType.name} {
    <#if jsType.constructor??>

    /**
     * ${jsType.constructor.comment}
     */
    constructor(${jsType.constructor.parameters}) {}
    </#if>
    <#list jsType.properties as property>

    /**
     * ${property.comment}
     */
    <#if property.static>static </#if><#if property.getter>get <#elseif property.setter>set </#if>${property.name}(<#if property.setter>value</#if>) {}
    </#list>
    <#list jsType.methods as method>

    /**
     * ${method.comment}
     */
    <#if method.static>static </#if>${method.name}(${method.parameters}) {}
    </#list>
}

</#list>
