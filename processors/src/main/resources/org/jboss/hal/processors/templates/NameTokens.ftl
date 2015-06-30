<#-- @ftlvariable name="packageName" type="java.lang.String" -->
<#-- @ftlvariable name="className" type="java.lang.String" -->
<#-- @ftlvariable name="pageInfos" type="java.util.Set<org.jboss.hal.processors.RequiredResourcesProcessor.PageInfo>" -->
package ${packageName};

import com.google.common.collect.HashMultimap;

import java.util.Collections;
import java.util.Set;
import java.util.HashSet;
import javax.annotation.Generated;

import static java.util.Arrays.asList;

/*
* WARNING! This class is generated. Do not modify.
*/
@ApplicationScoped
@Generated("org.jboss.hal.processors.RequiredResourcesProcessor")
public class ${className} implements org.jboss.hal.client.registry.NameTokenRegistry {

    private final Set<String> tokens;
    private final Set<String> revealedTokens;

    public ${className}() {
        this.tokens = new HashSet<>();
        this.revealedTokens = new HashSet<>();
    }

    @Override
    public Set<String> getTokens() {
        return Collections.unmodifiableSet(tokens);
    }

    @Override
    public boolean wasRevealed(String token) {
        return revealedTokens.contains(token);
    }

    @Override
    public void markedRevealed(String token) {
        revealedTokens.add(token);
    }
}
