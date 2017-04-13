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
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.hal.config.Role;
import org.jboss.hal.core.finder.PreviewContent;
import org.jboss.hal.resources.Previews;
import org.jboss.hal.resources.Resources;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;

/**
 * @author Harald Pehl
 */
class RolePreview extends PreviewContent<Role> {

    private static final String INCLUDE_ALL_DIV = "includeAllDiv";
    private static final String NOT_INCLUDE_ALL_DIV = "notIncludeAllDiv";
    private static final String NO_EXCLUDES = "noExcludes";
    private static final String EXCLUDE_UL = "excludeUl";
    private static final String NO_INCLUDES = "noIncludes";
    private static final String INCLUDE_UL = "includeUl";

    private final AccessControl accessControl;
    private final AccessControlTokens tokens;
    private final Resources resources;
    private final Element includeAllDiv;
    private final Element notIncludeAllDiv;
    private final Element noExcludes;
    private final Element excludesUl;
    private final Element noIncludes;
    private final Element includesUl;

    RolePreview(final AccessControl accessControl, final AccessControlTokens tokens, final Role role,
            final Resources resources) {
        // @formatter:off
        super(role.getName(), role.isScoped()
                ? (role.getType() == Role.Type.HOST
                    ? resources.messages().hostScopedRole(role.getBaseRole().getName(), String.join(", ", role.getScope()))
                    : resources.messages().serverGroupScopedRole(role.getBaseRole().getName(), String.join(", ", role.getScope())))
                : null);
        // @formatter:on
        this.accessControl = accessControl;
        this.tokens = tokens;
        this.resources = resources;

        // @formatter:off
        previewBuilder()
            .div().rememberAs(INCLUDE_ALL_DIV)
                .h(2).textContent(resources.constants().includesAllHeader()).end()
                .p().textContent(resources.constants().includesAllDescription()).end()
            .end()
            .div().rememberAs(NOT_INCLUDE_ALL_DIV)
                .h(2).textContent(resources.constants().excludes()).end()
                .p().rememberAs(NO_EXCLUDES).textContent(resources.constants().noPrincipalsExcluded()).end()
                .ul().rememberAs(EXCLUDE_UL).end()

                .h(2).textContent(resources.constants().includes()).end()
                .p().rememberAs(NO_INCLUDES).textContent(resources.constants().noPrincipalsIncluded()).end()
                .ul().rememberAs(INCLUDE_UL).end()
            .end();
        // @formatter:on

        includeAllDiv = previewBuilder().referenceFor(INCLUDE_ALL_DIV);
        notIncludeAllDiv = previewBuilder().referenceFor(NOT_INCLUDE_ALL_DIV);
        noExcludes = previewBuilder().referenceFor(NO_EXCLUDES);
        excludesUl = previewBuilder().referenceFor(EXCLUDE_UL);
        noIncludes = previewBuilder().referenceFor(NO_INCLUDES);
        includesUl = previewBuilder().referenceFor(INCLUDE_UL);

        Elements.setVisible(noExcludes, false);
        Elements.setVisible(excludesUl, false);
        Elements.setVisible(noIncludes, false);
        Elements.setVisible(includesUl, false);

        Element roleDescription = Browser.getDocument().createElement("p"); //NON-NLS
        String roleName = role.isScoped() ? role.getBaseRole().getName() : role.getName();
        ExternalTextResource resource = resources.preview("rbac" + roleName);
        Previews.innerHtml(roleDescription, resource);
        previewBuilder().h(2).textContent(resources.constants().description()).end()
                .add(roleDescription);
    }

    @Override
    public void update(final Role role) {
        Comparator<Principal> byType = (p1, p2) -> p1.getType().ordinal() - p2.getType().ordinal();
        List<Principal> excludes = accessControl.assignments().excludes(role).map(Assignment::getPrincipal)
                .sorted(byType.thenComparing(comparing(Principal::getName))).collect(toList());
        List<Principal> includes = accessControl.assignments().includes(role).map(Assignment::getPrincipal)
                .sorted(byType.thenComparing(comparing(Principal::getName))).collect(toList());

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

    private void principal(Element ul, Principal principal) {
        String type = principal.getType() == Principal.Type.USER ? resources.constants().user() : resources.constants()
                .group();
        ul.appendChild(new Elements.Builder()
                .li()
                .span().textContent(type + " ").end()
                .a().attr("href", tokens.principal(principal)).textContent(principal.getName()).end()
                .end().build());
    }
}
