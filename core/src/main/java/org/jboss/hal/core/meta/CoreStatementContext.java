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
package org.jboss.hal.core.meta;

import org.jboss.hal.config.Environment;
import org.jboss.hal.dmr.model.StatementContext;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Harald Pehl
 */
public class CoreStatementContext implements StatementContext {

    private final Environment environment;
    private final Map<String, String> keys;

    @Inject
    public CoreStatementContext(Environment environment) {
        this.environment = environment;

        keys = new HashMap<>();
        keys.put(SELECTED_PROFILE, null);
        keys.put(SELECTED_GROUP, null);
        keys.put(SELECTED_HOST, null);
        keys.put(SELECTED_SERVER, null);
    }

    public String resolve(final String key) {
        if (!environment.isStandalone() && keys.containsKey(key)) {
            return keys.get(key);
        }
        return null;
    }

    @Override
    public String[] resolveTuple(final String key) {
        if (!environment.isStandalone() && keys.containsKey(key)) {
            String[] tuple = null;
            switch (key) {
                case SELECTED_PROFILE:
                    tuple = new String[]{"profile", "unknown"};
                    break;
                case SELECTED_GROUP:
                    tuple = new String[]{"server-group", "unknown"};
                    break;
                case SELECTED_HOST:
                    tuple = new String[]{"host", "unknown"};
                    break;
                case SELECTED_SERVER:
                    tuple = new String[]{"server", "unknown"};
                    break;
            }
            return tuple;
        }
        return null;
    }

    @Override
    public List<String> collect(final String key) {
        List<String> items = new ArrayList<>();
        String value = resolve(key);
        if (value != null) { items.add(value); }
        return items;
    }

    @Override
    public List<String[]> collectTuples(final String key) {
        List<String[]> items = new ArrayList<>();
        String[] tuple = resolveTuple(key);
        if (tuple != null) { items.add(tuple); }
        return items;
    }
}
