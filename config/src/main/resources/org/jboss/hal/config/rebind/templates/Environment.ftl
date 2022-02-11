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
<#-- @ftlvariable name="packageName" type="java.lang.String" -->
<#-- @ftlvariable name="className" type="java.lang.String" -->
<#-- @ftlvariable name="halVersion" type="java.lang.String" -->
<#-- @ftlvariable name="halBuild" type="java.lang.String" -->
<#-- @ftlvariable name="locales" type="java.util.Set<java.lang.String>" -->
package ${packageName};

import java.util.List;
import static java.util.Arrays.asList;

/*
* WARNING! This class is generated. Do not modify.
*/
public class ${className} extends AbstractEnvironment {

    public ${className}() {
        super("${halVersion}", "${halBuild}", asList(<#list locales as locale>"${locale}"<#if locale_has_next>, </#if></#list>));
    }
}
