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
package org.jboss.hal.client.accesscontrol;

import javax.inject.Inject;

import org.jboss.hal.core.finder.ColumnActionFactory;
import org.jboss.hal.core.finder.Finder;
import org.jboss.hal.core.finder.ItemActionFactory;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.spi.AsyncColumn;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;

/**
 * @author Harald Pehl
 */
@AsyncColumn(Ids.GROUP)
public class GroupColumn extends PrincipalColumn {

    @Inject
    public GroupColumn(final Finder finder,
            final ColumnActionFactory columnActionFactory,
            final ItemActionFactory itemActionFactory,
            final AccessControl accessControl,
            final AccessControlTokens tokens,
            final Resources resources) {
        super(finder, Ids.GROUP, resources.constants().group(),
                accessControl.principals().groups().stream().sorted(comparing(Principal::getName)).collect(toList()),
                columnActionFactory, itemActionFactory, accessControl, tokens, resources);
    }
}
