/*
 * Copyright 2015-2016 Red Hat, Inc, and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.hal.meta;

/**
 * A statement context which resolves the {@code selected.*} tuples to wildcards if they are not yet defined.
 *
 * @author Harald Pehl
 */
public class WildcardWhenUndefinedContext extends FilteringStatementContext implements StatementContext {

    public WildcardWhenUndefinedContext(final StatementContext delegate) {
        super(delegate, new Filter() {
            @Override
            public String filter(final String key) {
                return null;
            }

            @Override
            public String[] filterTuple(final String tuple) {
                String[] resolved = delegate.resolveTuple(tuple);
                if (resolved == null) {
                    Tuple t = Tuple.from(tuple);
                    return t != null ? new String[]{t.resource(), "*"} : null;
                } else {
                    return resolved;
                }
            }
        });
    }
}
