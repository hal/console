<#-- @ftlvariable name="generatedWith" type="java.lang.String" -->
<#-- @ftlvariable name="jsType" type="org.jboss.hal.processor.JsDocProcessor.JsTypeInfo" -->
/*
* WARNING! This file is generated with ${generatedWith}. Do not modify.
*/

/**
 * <#if jsType.comment??>${jsType.comment}<#else>n/a</#if>
 */
${jsType.name}

<#list jsType.methods as method>
/**
 * <#if method.comment??>${method.comment}<#else>n/a</#if>
 */
${method.name}(${method.parameters})

</#list>
