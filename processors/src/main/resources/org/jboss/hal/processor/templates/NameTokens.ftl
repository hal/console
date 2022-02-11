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
