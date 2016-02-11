package org.jboss.hal.meta;

import static org.jboss.hal.dmr.ModelDescriptionConstants.*;

public interface StatementContext {

    @SuppressWarnings("HardCodedStringLiteral")
    enum Key {

        ANY_PROFILE("any.profile", PROFILE),
        SELECTED_PROFILE("selected.profile", PROFILE),
        SELECTED_GROUP("selected.group", SERVER_GROUP),
        SELECTED_HOST("selected.host", HOST),
        SELECTED_SERVER("selected.server", SERVER);

        private final String key;
        private final String resource;

        Key(final String key, final String resource) {
            this.key = key;
            this.resource = resource;
        }

        public String key() {
            return key;
        }

        public String resource() {
            return resource;
        }

        public static Key fromKey(String key) {
            for (Key k : Key.values()) {
                if (k.key().equals(key)) {
                    return k;
                }
            }
            return null;
        }
    }


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
        public String selectedProfile() {
            return null;
        }
    };


    /**
     * Resolves a value matching the key.
     */
    String resolve(String key);

    /**
     * Resolves a tuple matching the key.
     */
    String[] resolveTuple(String key);

    String selectedProfile();
}
