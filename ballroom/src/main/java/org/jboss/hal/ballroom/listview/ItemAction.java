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
package org.jboss.hal.ballroom.listview;

import org.jboss.hal.resources.Ids;

/**
 * @author Harald Pehl
 */
public class ItemAction<T> {

    private static int counter = 0;

    public static <T> ItemAction<T> placeholder(String title) {
        return new ItemAction<>(Ids.build(Ids.LIST_VIEW_ACTION_PLACEHOLDER, String.valueOf(counter++)), title, item -> {
            // noop
        });
    }


    final String id;
    final String title;
    final ItemActionHandler<T> handler;

    public ItemAction(final String id, final String title, final ItemActionHandler<T> handler) {
        this.id = id;
        this.title = title;
        this.handler = handler;
    }
}
