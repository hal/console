package org.jboss.hal.dmr.model;

import java.util.List;

public interface StatementContext {

    /**
     * Resolves a value matching the key.
     */
    String resolve(String key);

    /**
     * Resolves a tuple matching the key.
     */
    String[] resolveTuple(String key);

    /**
     * Collects all values matching a key.
     */
    List<String> collect(String key);

    /**
     * Collects all tuples matching a key.
     */
    List<String[]> collectTuples(String key);
}
