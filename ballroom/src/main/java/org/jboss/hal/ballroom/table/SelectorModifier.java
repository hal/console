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

import jsinterop.annotations.JsType;

import static jsinterop.annotations.JsPackage.GLOBAL;
import static org.jboss.hal.resources.UIConstants.OBJECT;

/**
 * Options for how the row, column and cell selector should operate on rows.
 *
 * @author Harald Pehl
 * @see <a href="https://datatables.net/reference/type/selector-modifier">https://datatables.net/reference/type/selector-modifier</a>
 */
@JsType(isNative = true, namespace = GLOBAL, name = OBJECT)
class SelectorModifier {

    // @formatter:off
    enum Order {current, index}
    enum Page {all, current}
    enum Search {none, applied, removed}
    // @formatter:on

    String order;
    String page;
    String search;
    Boolean selected;
}
