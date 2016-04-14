<#-- @ftlvariable name="generatedWith" type="java.lang.String" -->
<#-- @ftlvariable name="packageName" type="java.lang.String" -->
<#-- @ftlvariable name="className" type="java.lang.String" -->
<#-- @ftlvariable name="tokenInfos" type="java.util.Set<org.jboss.hal.processor.NameTokenProcessor.TokenInfo>" -->
package ${packageName};

import java.util.Collections;
import java.util.Set;
import java.util.HashSet;
import javax.annotation.Generated;

/*
* WARNING! This class is generated. Do not modify.
*/
@Generated("${generatedWith}")
public class ${className} implements org.jboss.hal.meta.token.NameTokens {

    private final Set<String> tokens;

    public ${className}() {
        this.tokens = new HashSet<>();
        <#list tokenInfos as tokenInfo>
        this.tokens.add("${tokenInfo.token}");
        </#list>
    }

    @Override
    public Set<String> getTokens() {
        return Collections.unmodifiableSet(tokens);
    }
}
