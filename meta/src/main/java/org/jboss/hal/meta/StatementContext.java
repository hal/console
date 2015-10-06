package org.jboss.hal.meta;

import java.util.LinkedList;
import java.util.List;

public interface StatementContext {

    StatementContext NOOP = new StatementContext() {

        @Override
        public String resolve(String key) {
            // keys are resolved "as-is"
            return key;
        }

        @Override
        public String[] resolveTuple(String key) {
            // tuples are resolved as "echo"
            return new String[]{key, key};
        }

        @Override
        public LinkedList<String> collect(String key) {
            LinkedList<String> items = new LinkedList<>();
            String value = resolve(key);
            if (value != null) { items.add(value); }
            return items;
        }

        @Override
        public LinkedList<String[]> collectTuples(String key) {
            LinkedList<String[]> items = new LinkedList<>();
            String[] tuple = resolveTuple(key);
            if (tuple != null) { items.add(tuple); }
            return items;
        }
    };


    String SELECTED_PROFILE = "selected.profile";
    String SELECTED_GROUP = "selected.group";
    String SELECTED_HOST = "selected.host";
    String SELECTED_SERVER = "selected.server";

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
