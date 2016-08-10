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

import org.jboss.hal.core.finder.PreviewContent;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;

import static java.util.stream.Collectors.toList;

/**
 * @author Harald Pehl
 */
class PrincipalPreview extends PreviewContent<Principal> {

    PrincipalPreview(final AccessControl accessControl, final AccessControlTokens tokens, final Principal principal,
            final Resources resources) {
        super((principal.getType() == Principal.Type.USER ? resources.constants().user() : resources.constants()
                .group()) + " " + principal.getName(),
                principal.getRealm() != null ? Names.REALM + " " + principal.getRealm() : null);

        List<Role> excludes = accessControl.assignments().excludes(principal).map(Assignment::getRole)
                .sorted(Roles.STANDARD_FIRST.thenComparing(Roles.BY_NAME)).collect(toList());
        List<Role> includes = accessControl.assignments().includes(principal).map(Assignment::getRole)
                .sorted(Roles.STANDARD_FIRST.thenComparing(Roles.BY_NAME)).collect(toList());

        previewBuilder().h(2).textContent(resources.constants().excludes()).end();
        if (excludes.isEmpty()) {
            previewBuilder().p().textContent(resources.constants().noRolesExcluded()).end();
        } else {
            previewBuilder().ul();
            excludes.forEach((role) ->
                    previewBuilder().li()
                    .a().attr("href", tokens.role(role)).textContent(role.getName()).end()
                    .end());
            previewBuilder().end();
        }

        previewBuilder().h(2).textContent(resources.constants().includes()).end();
        if (includes.isEmpty()) {
            previewBuilder().p().textContent(resources.constants().noRolesIncluded()).end();
        } else {
            previewBuilder().ul();
            includes.forEach((role) ->
                    previewBuilder().li()
                    .a().attr("href", tokens.role(role)).textContent(role.getName()).end()
                    .end());
            previewBuilder().end();
        }
    }
}
