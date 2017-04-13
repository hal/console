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
