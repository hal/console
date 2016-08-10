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

import java.util.Comparator;
import java.util.List;

import com.google.gwt.resources.client.ExternalTextResource;
import elemental.client.Browser;
import elemental.dom.Element;
import org.jboss.hal.core.finder.PreviewContent;
import org.jboss.hal.resources.Previews;
import org.jboss.hal.resources.Resources;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;

/**
 * @author Harald Pehl
 */
class RolePreview extends PreviewContent<Role> {

    private final AccessControlTokens tokens;
    private final Resources resources;

    RolePreview(final AccessControl accessControl, final AccessControlTokens tokens, final Role role,
            final Resources resources) {
        // @formatter:off
        super(role.getName(), role.isScoped()
                ? (role.getType() == Role.Type.HOST
                    ? resources.messages().hostScopedRole(role.getBaseRole().getName(), String.join(", ", role.getScope()))
                    : resources.messages().serverGroupScopedRole(role.getBaseRole().getName(), String.join(", ", role.getScope())))
                : null);
        // @formatter:on
        this.tokens = tokens;
        this.resources = resources;

        if (role.isIncludeAll()) {
            previewBuilder().h(2).textContent(resources.constants().includesAllHeader()).end();
            previewBuilder().p().textContent(resources.constants().includesAllDescription()).end();

        } else {
            Comparator<Principal> byType = (p1, p2) -> p1.getType().ordinal() - p2.getType().ordinal();
            List<Principal> excludes = accessControl.assignments().excludes(role).map(Assignment::getPrincipal)
                    .sorted(byType.thenComparing(comparing(Principal::getName))).collect(toList());
            List<Principal> includes = accessControl.assignments().includes(role).map(Assignment::getPrincipal)
                    .sorted(byType.thenComparing(comparing(Principal::getName))).collect(toList());

            previewBuilder().h(2).textContent(resources.constants().excludes()).end();
            if (excludes.isEmpty()) {
                previewBuilder().p().textContent(resources.constants().noPrincipalsExcluded()).end();
            } else {
                previewBuilder().ul();
                excludes.forEach(this::principal);
                previewBuilder().end();
            }

            previewBuilder().h(2).textContent(resources.constants().includes()).end();
            if (includes.isEmpty()) {
                previewBuilder().p().textContent(resources.constants().noPrincipalsIncluded()).end();
            } else {
                previewBuilder().ul();
                includes.forEach(this::principal);
                previewBuilder().end();
            }
        }

        Element roleDescription = Browser.getDocument().createElement("p"); //NON-NLS
        String roleName = role.isScoped() ? role.getBaseRole().getName() : role.getName();
        ExternalTextResource resource = resources.preview("rbac" + roleName);
        Previews.innerHtml(roleDescription, resource);
        previewBuilder().h(2).textContent(resources.constants().description()).end()
                .add(roleDescription);
    }

    private void principal(Principal principal) {
        String type = principal.getType() == Principal.Type.USER ? resources.constants().user() : resources.constants()
                .group();
        previewBuilder().li()
                .span().textContent(type + " ").end()
                .a().attr("href", tokens.principal(principal)).textContent(principal.getName()).end()
                .end();
    }
}
