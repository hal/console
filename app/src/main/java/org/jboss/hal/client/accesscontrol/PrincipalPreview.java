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

import java.util.List;

import org.jboss.gwt.elemento.core.Elements;
import org.jboss.hal.config.Role;
import org.jboss.hal.config.Roles;
import org.jboss.hal.core.finder.PreviewContent;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;

import elemental2.dom.HTMLElement;

import static java.util.stream.Collectors.toList;
import static org.jboss.gwt.elemento.core.Elements.*;

class PrincipalPreview extends PreviewContent<Principal> {

    private final AccessControl accessControl;
    private final AccessControlTokens tokens;
    private final HTMLElement noExcludes;
    private final HTMLElement excludesUl;
    private final HTMLElement noIncludes;
    private final HTMLElement includesUl;

    PrincipalPreview(AccessControl accessControl, AccessControlTokens tokens, Principal principal,
            Resources resources) {
        super((principal.getType() == Principal.Type.USER ? resources.constants().user()
                : resources.constants()
                        .group())
                + " " + principal.getName(),
                principal.getRealm() != null ? Names.REALM + " " + principal.getRealm() : null);
        this.accessControl = accessControl;
        this.tokens = tokens;

        previewBuilder()
                .add(h(2).textContent(resources.constants().excludes()))
                .add(noExcludes = p().textContent(resources.constants().noRolesExcluded()).element())
                .add(excludesUl = ul().element())
                .add(h(2).textContent(resources.constants().includes()))
                .add(noIncludes = p().textContent(resources.constants().noRolesIncluded()).element())
                .add(includesUl = ul().element());

        Elements.setVisible(noExcludes, false);
        Elements.setVisible(excludesUl, false);
        Elements.setVisible(noIncludes, false);
        Elements.setVisible(includesUl, false);
    }

    @Override
    public void update(Principal principal) {
        List<Role> excludes = accessControl.assignments().excludes(principal).map(Assignment::getRole)
                .sorted(Roles.STANDARD_FIRST.thenComparing(Roles.BY_NAME)).collect(toList());
        List<Role> includes = accessControl.assignments().includes(principal).map(Assignment::getRole)
                .sorted(Roles.STANDARD_FIRST.thenComparing(Roles.BY_NAME)).collect(toList());

        Elements.setVisible(noExcludes, excludes.isEmpty());
        Elements.setVisible(excludesUl, !excludes.isEmpty());
        Elements.removeChildrenFrom(excludesUl);
        excludes.forEach(role -> excludesUl.appendChild(li()
                .add(a(tokens.role(role)).textContent(role.getName())).element()));

        Elements.setVisible(noIncludes, includes.isEmpty());
        Elements.setVisible(includesUl, !includes.isEmpty());
        Elements.removeChildrenFrom(includesUl);
        includes.forEach(role -> includesUl.appendChild(li()
                .add(a(tokens.role(role)).textContent(role.getName())).element()));
    }
}
