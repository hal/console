/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2010, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.hal.ballroom.table;

import jsinterop.annotations.JsType;

import static jsinterop.annotations.JsPackage.GLOBAL;
import static org.jboss.hal.resources.Names.OBJECT;

/**
 * Options for how the row, column and cell selector should operate on rows.
 *
 * @author Harald Pehl
 * @see <a href="https://datatables.net/reference/type/selector-modifier">https://datatables.net/reference/type/selector-modifier</a>
 */
@JsType(isNative = true, namespace = GLOBAL, name = OBJECT)
public class Selector {

    // @formatter:off
    public enum Order {current, index}
    public enum Page {all, current}
    public enum Search {none, applied, removed}
    // @formatter:on

    public String order;
    public String page;
    public String search;
    public Boolean selected;
}
