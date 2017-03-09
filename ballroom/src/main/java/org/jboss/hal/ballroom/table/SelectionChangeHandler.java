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
package org.jboss.hal.ballroom.table;

/**
 * Convenience handler when a <em>row</em> selection <em>or</em> deselection takes place.
 *
 * @param <T> the row type
 */
@FunctionalInterface
public interface SelectionChangeHandler<T> {

    /**
     * Called when a selection changed. That is when a row is selected <em>or</em> deselected.
     *
     * @param api the api instance
     */
    void onSelectionChanged(Api<T> api);
}
