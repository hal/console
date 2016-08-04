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

import java.util.List;
import javax.inject.Inject;

import com.google.gwt.user.client.rpc.AsyncCallback;
import org.jboss.hal.core.finder.Finder;
import org.jboss.hal.core.finder.FinderColumn;
import org.jboss.hal.core.finder.FinderContext;
import org.jboss.hal.core.finder.ItemsProvider;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.spi.AsyncColumn;

/**
 * @author Harald Pehl
 */
@AsyncColumn(Ids.USER)
public class UserColumn extends FinderColumn<Principal> {

    @Inject
    public UserColumn(final Finder finder, final Resources resources) {
        super(new Builder<Principal>(finder, Ids.USER, resources.constants().user())

                .itemsProvider(new ItemsProvider<Principal>() {
                    @Override
                    public void get(final FinderContext context, final AsyncCallback<List<Principal>> callback) {
                    }
                })

        );
    }
}
