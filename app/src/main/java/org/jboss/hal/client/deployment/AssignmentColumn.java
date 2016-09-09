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
package org.jboss.hal.client.deployment;

import javax.inject.Inject;

import org.jboss.hal.core.finder.Finder;
import org.jboss.hal.core.finder.FinderColumn;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.spi.AsyncColumn;
import org.jboss.hal.spi.Requires;

/**
 * @author Harald Pehl
 */
@AsyncColumn(Ids.ASSIGNED_DEPLOYMENT)
@Requires("/server-group=*/deployment=*")
public class AssignmentColumn extends FinderColumn<Assignment> {

    @Inject
    public AssignmentColumn(final Finder finder) {
        super(new FinderColumn.Builder<Assignment>(finder, Ids.ASSIGNED_DEPLOYMENT, Names.DEPLOYMENT)
                .pinnable()
                .showCount()
                .withFilter()
            );
    }
}
