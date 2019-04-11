/*
 * Copyright 2015-2016 Red Hat, Inc, and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.hal.client.configuration.subsystem.security;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import org.jboss.hal.core.CrudOperations;
import org.jboss.hal.core.finder.ColumnActionFactory;
import org.jboss.hal.core.finder.Finder;
import org.jboss.hal.core.finder.FinderColumn;
import org.jboss.hal.core.finder.ItemAction;
import org.jboss.hal.core.finder.ItemActionFactory;
import org.jboss.hal.core.finder.ItemDisplay;
import org.jboss.hal.core.mvp.Places;
import org.jboss.hal.meta.token.NameTokens;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.spi.AsyncColumn;
import org.jboss.hal.spi.Requires;

import static java.util.stream.Collectors.toList;
import static org.jboss.hal.client.configuration.subsystem.security.AddressTemplates.SECURITY_DOMAIN_ADDRESS;
import static org.jboss.hal.client.configuration.subsystem.security.AddressTemplates.SECURITY_DOMAIN_TEMPLATE;
import static org.jboss.hal.client.configuration.subsystem.security.AddressTemplates.SECURITY_SUBSYSTEM_TEMPLATE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.CACHE_TYPE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.NAME;
import static org.jboss.hal.dmr.ModelDescriptionConstants.SECURITY_DOMAIN;

@AsyncColumn(Ids.SECURITY_DOMAIN)
@Requires(value = SECURITY_DOMAIN_ADDRESS, recursive = false)
public class SecurityDomainColumn extends FinderColumn<SecurityDomain> {

    @Inject
    public SecurityDomainColumn(Finder finder,
            ColumnActionFactory columnActionFactory,
            ItemActionFactory itemActionFactory,
            CrudOperations crud,
            Resources resources,
            Places places) {

        super(new FinderColumn.Builder<SecurityDomain>(finder, Ids.SECURITY_DOMAIN, Names.SECURITY_DOMAIN)

                .itemsProvider((context, callback) ->
                        crud.readChildren(SECURITY_SUBSYSTEM_TEMPLATE, SECURITY_DOMAIN, children -> {
                            List<SecurityDomain> securityDomains = children.stream()
                                    .map(SecurityDomain::new)
                                    .collect(toList());
                            callback.onSuccess(securityDomains);
                        }))

                .withFilter()
                .filterDescription(resources.messages().securityDomainColumnFilterDescription())
                .useFirstActionAsBreadcrumbHandler()
                //Do not optimize this lambda to a method reference,
                //the JavaToJavaScript transpiler can't handle a method reference here
                .onPreview(item -> new SecurityDomainPreview(item))
        );

        addColumnAction(columnActionFactory.add(
                Ids.SECURITY_DOMAIN_ADD,
                Names.SECURITY_DOMAIN,
                SECURITY_DOMAIN_TEMPLATE,
                Collections.singletonList(CACHE_TYPE),
                Ids::securityDomain,
                this::createUniqueValidation
        ));

        setItemRenderer(item -> new ItemDisplay<SecurityDomain>() {
            @Override
            public String getId() {
                return Ids.securityDomain(item.getName());
            }

            @Override
            public String getTitle() {
                return item.getName();
            }

            @Override
            public String getFilterData() {
                List<String> data = new ArrayList<>();
                data.add(item.getName());
                if (item.hasDefined(CACHE_TYPE)) {
                    data.add(item.get(CACHE_TYPE).asString());
                }
                return String.join(" ", data);
            }

            @Override
            public List<ItemAction<SecurityDomain>> actions() {
                List<ItemAction<SecurityDomain>> actions = new ArrayList<>();
                actions.add(itemActionFactory.view(places.selectedProfile(NameTokens.SECURITY_DOMAIN)
                        .with(NAME, item.getName()).build()));
                actions.add(itemActionFactory.remove(Names.SECURITY_DOMAIN, item.getName(),
                        SECURITY_DOMAIN_TEMPLATE, SecurityDomainColumn.this));
                return actions;
            }
        });
    }
}
