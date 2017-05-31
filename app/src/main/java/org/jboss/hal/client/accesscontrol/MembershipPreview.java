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

import elemental2.dom.HTMLElement;
import org.jboss.hal.core.finder.PreviewContent;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;

import static org.jboss.gwt.elemento.core.Elements.a;
import static org.jboss.gwt.elemento.core.Elements.p;
import static org.jboss.gwt.elemento.core.Elements.span;

/**
 * @author Harald Pehl
 */
class MembershipPreview extends PreviewContent<Assignment> {

    MembershipPreview(final AccessControlTokens tokens, final Principal principal, final Resources resources) {
        super((principal.getType() == Principal.Type.USER
                        ? resources.constants().user()
                        : resources.constants().group()) + " " + principal.getName(),
                principal.getRealm() != null
                        ? Names.REALM + " " + principal.getRealm()
                        : null);

        HTMLElement p;
        previewBuilder().add(p = p().asElement());
        if (principal.getType() == Principal.Type.USER) {
            p.appendChild(span().textContent(resources.constants().assignmentsOfUser() + " ").asElement());
        } else {
            p.appendChild(span().textContent(resources.constants().assignmentsOfGroup() + " ").asElement());
        }
        p.appendChild(a(tokens.principal(principal)).textContent(principal.getName()).asElement());
        p.appendChild(span().textContent(".").asElement());
    }
}
