package org.jboss.hal.meta;

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
}
