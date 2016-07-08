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
package org.jboss.hal.core.subsystem;

import java.util.List;

import elemental.dom.Element;
import org.jboss.hal.core.finder.ItemAction;
import org.jboss.hal.core.finder.ItemDisplay;

/**
 * @author Harald Pehl
 */
public class SubsystemDisplay implements ItemDisplay<SubsystemMetadata> {

    private final SubsystemMetadata subsystemMetadata;
    private final List<ItemAction<SubsystemMetadata>> actions;

    public SubsystemDisplay(final SubsystemMetadata subsystemMetadata,
            final List<ItemAction<SubsystemMetadata>> actions) {
        this.subsystemMetadata = subsystemMetadata;
        this.actions = actions;
    }

    @Override
    public String getId() {
        return subsystemMetadata.getName();
    }

    @Override
    public Element asElement() {
        return subsystemMetadata.getSubtitle() != null ? ItemDisplay
                .withSubtitle(subsystemMetadata.getTitle(), subsystemMetadata.getSubtitle()) : null;
    }

    @Override
    public String getTitle() {
        return subsystemMetadata.getTitle();
    }

    @Override
    public String getFilterData() {
        return subsystemMetadata.getSubtitle() != null
                ? subsystemMetadata.getTitle() + " " + subsystemMetadata.getSubtitle()
                : subsystemMetadata.getTitle();
    }

    @Override
    public String nextColumn() {
        return subsystemMetadata.getNextColumn();
    }

    @Override
    public List<ItemAction<SubsystemMetadata>> actions() {
        return actions;
    }
}
