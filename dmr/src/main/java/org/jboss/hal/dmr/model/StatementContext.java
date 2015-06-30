package org.jboss.hal.dmr.model;

import java.util.LinkedList;

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
    LinkedList<String> collect(String key);

    /**
     * Collects all tuples matching a key.
     */
    LinkedList<String[]> collectTuples(String key);
}
