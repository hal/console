<#--

     Copyright 2022 Red Hat

     Licensed under the Apache License, Version 2.0 (the "License");
     you may not use this file except in compliance with the License.
     You may obtain a copy of the License at

         http://www.apache.org/licenses/LICENSE-2.0

     Unless required by applicable law or agreed to in writing, software
     distributed under the License is distributed on an "AS IS" BASIS,
     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
     See the License for the specific language governing permissions and
     limitations under the License.

-->
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
