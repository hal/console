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

import org.jboss.hal.core.accesscontrol.Assignment;
import org.jboss.hal.core.accesscontrol.Principal;
import org.jboss.hal.core.finder.PreviewContent;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;

import elemental2.dom.HTMLElement;

import static org.jboss.elemento.Elements.a;
import static org.jboss.elemento.Elements.p;
import static org.jboss.elemento.Elements.span;

class MembershipPreview extends PreviewContent<Assignment> {

    private static final String SPACE = " ";

    MembershipPreview(AccessControlTokens tokens, Principal principal, Resources resources) {
        super((principal.getType() == Principal.Type.USER
                ? resources.constants().user()
                : resources.constants().group()) + SPACE + principal.getName(),
                principal.getRealm() != null
                        ? Names.REALM + SPACE + principal.getRealm()
                        : null);

        HTMLElement p;
        previewBuilder().add(p = p().element());
        if (principal.getType() == Principal.Type.USER) {
            p.appendChild(span().textContent(resources.constants().assignmentsOfUser() + SPACE).element());
        } else {
            p.appendChild(span().textContent(resources.constants().assignmentsOfGroup() + SPACE).element());
        }
        p.appendChild(a(tokens.principal(principal)).textContent(principal.getName()).element());
        p.appendChild(span().textContent(".").element());
    }
}
