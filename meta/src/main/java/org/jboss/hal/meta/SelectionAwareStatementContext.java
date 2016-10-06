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

import java.util.function.Supplier;

/**
 * A filtering statement context which resolves the key {@code selection} to the specified selection supplier.
 *
 * @author Harald Pehl
 */
public class SelectionAwareStatementContext extends FilteringStatementContext implements StatementContext {

    private static final String SELECTION_KEY = "selection";
    public static final String SELECTION_EXPRESSION = "{" + SELECTION_KEY + "}";

    public SelectionAwareStatementContext(final StatementContext delegate, final Supplier<String> selection) {
        super(delegate, new Filter() {
            @Override
            public String filter(final String placeholder) {
                if (SELECTION_KEY.equals(placeholder)) {
                    return selection.get();
                }
                return null;
            }

            @Override
            public String[] filterTuple(final String placeholder) {
                return null;
            }
        });
    }
}
