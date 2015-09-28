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
package org.jboss.hal.meta;

import java.util.LinkedList;

/**
 * A statement context useful for unit tests. Resolves a key "abc" as "abc" and a tuple "foo" as {"foo", "foo"}.
 *
 * @author Harald Pehl
 */
public class EchoContext implements StatementContext {

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
}
