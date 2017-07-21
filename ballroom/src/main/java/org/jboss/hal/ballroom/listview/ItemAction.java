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

import org.jboss.hal.meta.security.Constraint;
import org.jboss.hal.meta.security.Constraints;

public class ItemAction<T> {

    final String id;
    final String title;
    final ItemActionHandler<T> handler;
    final Constraints constraints;

    public ItemAction(String id, String title, ItemActionHandler<T> handler) {
        this(id, title, handler, Constraints.empty());
    }

    public ItemAction(String id, String title, ItemActionHandler<T> handler, Constraint constraint) {
        this(id, title, handler, Constraints.single(constraint));
    }

    public ItemAction(String id, String title, ItemActionHandler<T> handler, Constraints constraints) {
        this.id = id;
        this.title = title;
        this.handler = handler;
        this.constraints = constraints;
    }
}
