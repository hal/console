package org.jboss.hal.core.meta;

import org.jboss.hal.spi.SearchIndex;

import java.util.Set;

public interface SearchIndexRegistry {

    /**
     * Returns the tokens for the given operation mode which are not excluded by {@link SearchIndex#exclude()}.
     *
     * @param standalone the execution mode
     *
     * @return a set of matching tokens
     */
    Set<String> getTokens(boolean standalone);

    Set<String> getResources(String token);

    Set<String> getKeywords(String token);
}
