/*
 *  Copyright 2022 Red Hat
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.jboss.hal.client.accesscontrol;

import java.util.Comparator;
import java.util.List;

import org.jboss.elemento.Elements;
import org.jboss.hal.config.Role;
import org.jboss.hal.core.finder.PreviewContent;
import org.jboss.hal.resources.Previews;
import org.jboss.hal.resources.Resources;

import com.google.gwt.resources.client.ExternalTextResource;

import elemental2.dom.HTMLElement;

import static java.util.stream.Collectors.toList;

import static org.jboss.elemento.Elements.*;

class RolePreview extends PreviewContent<Role> {

    private final AccessControl accessControl;
    private final AccessControlTokens tokens;
    private final Resources resources;
    private final HTMLElement includeAllDiv;
    private final HTMLElement notIncludeAllDiv;
    private final HTMLElement noExcludes;
    private final HTMLElement excludesUl;
    private final HTMLElement noIncludes;
    private final HTMLElement includesUl;

    RolePreview(AccessControl accessControl, AccessControlTokens tokens, Role role,
            Resources resources) {
        // @formatter:off
        super(role.getName(), role.isScoped()
                ? (role.getType() == Role.Type.HOST
                        ? resources.messages().hostScopedRole(role.getBaseRole().getName(), String.join(", ", role.getScope()))
                        : resources.messages().serverGroupScopedRole(role.getBaseRole().getName(),
                                String.join(", ", role.getScope())))
                : null);
        // @formatter:on

        this.accessControl = accessControl;
        this.tokens = tokens;
        this.resources = resources;

        previewBuilder()
                .add(includeAllDiv = div()
                        .add(h(2).textContent(resources.constants().includesAllHeader()))
                        .add(p().textContent(resources.constants().includesAllDescription())).element())
                .add(notIncludeAllDiv = div()
                        .add(h(2).textContent(resources.constants().excludes()))
                        .add(noExcludes = p().textContent(resources.constants().noPrincipalsExcluded()).element())
                        .add(excludesUl = ul().element())
                        .add(h(2).textContent(resources.constants().includes()))
                        .add(noIncludes = p().textContent(resources.constants().noPrincipalsIncluded()).element())
                        .add(includesUl = ul().element()).element());

        Elements.setVisible(noExcludes, false);
        Elements.setVisible(excludesUl, false);
        Elements.setVisible(noIncludes, false);
        Elements.setVisible(includesUl, false);

        HTMLElement roleDescription = p().element();
        String roleName = role.isScoped() ? role.getBaseRole().getName() : role.getName();
        ExternalTextResource resource = resources.preview("rbac" + roleName);
        Previews.innerHtml(roleDescription, resource);
        previewBuilder()
                .add(h(2).textContent(resources.constants().description()))
                .add(roleDescription);
    }

    @Override
    public void update(Role role) {
        Comparator<Principal> byType = Comparator.comparingInt(p -> p.getType().ordinal());
        List<Principal> excludes = accessControl.assignments().excludes(role).map(Assignment::getPrincipal)
                .sorted(byType.thenComparing(Principal::getName)).collect(toList());
        List<Principal> includes = accessControl.assignments().includes(role).map(Assignment::getPrincipal)
                .sorted(byType.thenComparing(Principal::getName)).collect(toList());

        Elements.setVisible(includeAllDiv, role.isIncludeAll());
        Elements.setVisible(notIncludeAllDiv, !role.isIncludeAll());

        Elements.setVisible(noExcludes, excludes.isEmpty());
        Elements.setVisible(excludesUl, !excludes.isEmpty());
        Elements.removeChildrenFrom(excludesUl);
        excludes.forEach(principal -> principal(excludesUl, principal));

        Elements.setVisible(noIncludes, includes.isEmpty());
        Elements.setVisible(includesUl, !includes.isEmpty());
        Elements.removeChildrenFrom(includesUl);
        includes.forEach(principal -> principal(includesUl, principal));
    }

    private void principal(HTMLElement ul, Principal principal) {
        String type = principal.getType() == Principal.Type.USER ? resources.constants().user()
                : resources.constants()
                        .group();
        ul.appendChild(li()
                .add(span().textContent(type + " "))
                .add(a(tokens.principal(principal)).textContent(principal.getName())).element());
    }
}
