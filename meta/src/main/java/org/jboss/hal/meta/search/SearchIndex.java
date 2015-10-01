package org.jboss.hal.meta.search;

import org.jboss.hal.spi.Keywords;

import java.util.Set;

public interface SearchIndex {

    /**
     * Returns the tokens for the given operation mode which are not excluded by {@link Keywords#exclude()}.
     *
     * @param standalone the execution mode
     *
     * @return a set of matching tokens
     */
    Set<String> getTokens(boolean standalone);

    Set<String> getResources(String token);

    Set<String> getKeywords(String token);
}
