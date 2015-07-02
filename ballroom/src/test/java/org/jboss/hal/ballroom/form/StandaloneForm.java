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
package org.jboss.hal.ballroom.form;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author Harald Pehl
 */
public class StandaloneForm extends AbstractForm<String> {

    protected StandaloneForm() {
        super("test", true, true);
    }

    @Override
    public String newModel() {
        return null;
    }

    @Override
    protected void undefineModel() {

    }

    @Override
    protected void updateModel(final Map<String, Object> changedValues) {

    }

    @Override
    public Map<String, Object> getChangedValues() {
        return Collections.emptyMap();
    }

    @Override
    protected void prepareViewState() {
        // noop
    }

    @Override
    protected void prepareEditState() {
        // noop
    }

    @Override
    protected void prepareEmptyState() {
        // noop
    }


    // ------------------------------------------------------ overridden widget related methods

    @Override
    public void clearErrors() {
    }

    @Override
    protected void showErrors(final List<String> messages) {
    }

    @Override
    protected void switchToViewState() {
    }

    @Override
    protected void switchToEditState() {
    }

    @Override
    protected void switchToEmptyState() {
    }
}
