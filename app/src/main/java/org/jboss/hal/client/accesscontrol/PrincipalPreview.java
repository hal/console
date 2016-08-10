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

import elemental.dom.Element;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.hal.core.finder.PreviewContent;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;

import static java.util.stream.Collectors.toList;

/**
 * @author Harald Pehl
 */
class PrincipalPreview extends PreviewContent<Principal> {

    private static final String NO_EXCLUDES = "noExcludes";
    private static final String EXCLUDE_UL = "excludeUl";
    private static final String NO_INCLUDES = "noIncludes";
    private static final String INCLUDE_UL = "includeUl";

    private final AccessControl accessControl;
    private final AccessControlTokens tokens;
    private final Element noExcludes;
    private final Element excludesUl;
    private final Element noIncludes;
    private final Element includesUl;

    PrincipalPreview(final AccessControl accessControl, final AccessControlTokens tokens, final Principal principal,
            final Resources resources) {
        super((principal.getType() == Principal.Type.USER ? resources.constants().user() : resources.constants()
                        .group()) + " " + principal.getName(),
                principal.getRealm() != null ? Names.REALM + " " + principal.getRealm() : null);
        this.accessControl = accessControl;
        this.tokens = tokens;

        previewBuilder()
                .h(2).textContent(resources.constants().excludes()).end()
                .p().rememberAs(NO_EXCLUDES).textContent(resources.constants().noRolesExcluded()).end()
                .ul().rememberAs(EXCLUDE_UL).end()

                .h(2).textContent(resources.constants().includes()).end()
                .p().rememberAs(NO_INCLUDES).textContent(resources.constants().noRolesIncluded()).end()
                .ul().rememberAs(INCLUDE_UL).end();

        noExcludes = previewBuilder().referenceFor(NO_EXCLUDES);
        excludesUl = previewBuilder().referenceFor(EXCLUDE_UL);
        noIncludes = previewBuilder().referenceFor(NO_INCLUDES);
        includesUl = previewBuilder().referenceFor(INCLUDE_UL);

        Elements.setVisible(noExcludes, false);
        Elements.setVisible(excludesUl, false);
        Elements.setVisible(noIncludes, false);
        Elements.setVisible(includesUl, false);
    }

    @Override
    public void update(final Principal principal) {
        List<Role> excludes = accessControl.assignments().excludes(principal).map(Assignment::getRole)
                .sorted(Roles.STANDARD_FIRST.thenComparing(Roles.BY_NAME)).collect(toList());
        List<Role> includes = accessControl.assignments().includes(principal).map(Assignment::getRole)
                .sorted(Roles.STANDARD_FIRST.thenComparing(Roles.BY_NAME)).collect(toList());

        Elements.setVisible(noExcludes, excludes.isEmpty());
        Elements.setVisible(excludesUl, !excludes.isEmpty());
        Elements.removeChildrenFrom(excludesUl);
        excludes.forEach(role -> excludesUl.appendChild(
                new Elements.Builder().li()
                        .a().attr("href", tokens.role(role)).textContent(role.getName()).end()
                        .end().build()));

        Elements.setVisible(noIncludes, includes.isEmpty());
        Elements.setVisible(includesUl, !includes.isEmpty());
        Elements.removeChildrenFrom(includesUl);
        includes.forEach(role -> includesUl.appendChild(
                new Elements.Builder().li()
                        .a().attr("href", tokens.role(role)).textContent(role.getName()).end()
                        .end().build()));
    }
}
