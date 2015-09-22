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

import org.jboss.hal.dmr.model.StatementContext;

import java.util.ArrayList;
import java.util.List;

/**
 * Intercepts the resolution and allows to filter/replace certain statement values.
 *
 * @author Heiko Braun
 */
public class FilteringStatementContext implements StatementContext {

    public interface Filter {

        String filter(String key);

        String[] filterTuple(String key);
    }


    private Filter filter;
    private StatementContext delegate;

    public FilteringStatementContext(StatementContext delegate, Filter filter) {
        this.delegate = delegate;
        this.filter = filter;
    }

    @Override
    public String resolve(String key) {
        String filtered = filter.filter(key);
        return filtered != null ? filtered : delegate.resolve(key);
    }

    @Override
    public String[] resolveTuple(String key) {
        String[] filtered = filter.filterTuple(key);
        return filtered != null ? filtered : delegate.resolveTuple(key);
    }

    @Override
    public List<String> collect(String key) {
        List<String> actualCollection = delegate.collect(key);
        String filtered = filter.filter(key);
        if (filtered != null) {
            List<String> filteredCollection = new ArrayList<>();
            for (String val : actualCollection) {
                filteredCollection.add(filtered);
            }
            //TODO: this currently enforces a filter to be applied (hack?)
            if (actualCollection.isEmpty()) {
                filteredCollection.add(filtered);
            }
            return filteredCollection;
        }
        return actualCollection;
    }

    @Override
    public List<String[]> collectTuples(String key) {
        List<String[]> actualCollection = delegate.collectTuples(key);
        String[] filtered = filter.filterTuple(key);
        if (filtered != null) {
            List<String[]> filteredCollection = new ArrayList<>();
            for (String[] val : actualCollection) {
                filteredCollection.add(filtered);
            }
            return filteredCollection;
        }
        return actualCollection;
    }
}
