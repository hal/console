<#-- @ftlvariable name="types" type="java.util.Collection<org.jboss.hal.processor.EsDocProcessor.Type>" -->
<#list types as type>
<#if type.comment??>${type.comment}</#if>
class ${type.name} {
    <#if type.constructor??>

    <#if type.constructor.comment??>${type.constructor.comment}</#if>
    constructor(${type.constructor.parameters}) {}
    </#if>
    <#list type.properties as property>

    <#if property.comment??>${property.comment}</#if>
    <#if property.static>static </#if><#if property.getter>get <#elseif property.setter>set </#if>${property.name}(<#if property.setter>value</#if>) {}
    </#list>
    <#list type.methods as method>

    <#if method.comment??>${method.comment}</#if>
    <#if method.static>static </#if>${method.name}(${method.parameters}) {}
    </#list>
}

</#list>
