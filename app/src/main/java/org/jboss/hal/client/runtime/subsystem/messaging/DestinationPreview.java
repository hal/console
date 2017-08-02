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
package org.jboss.hal.client.runtime.subsystem.messaging;

import com.gwtplatform.mvp.shared.proxy.PlaceRequest;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.hal.core.finder.FinderPath;
import org.jboss.hal.core.finder.FinderPathFactory;
import org.jboss.hal.core.finder.PreviewContent;
import org.jboss.hal.core.mvp.Places;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.meta.token.NameTokens;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;

import static elemental2.dom.DomGlobal.document;
import static org.jboss.gwt.elemento.core.Elements.a;

class DestinationPreview extends PreviewContent<Destination> {

    DestinationPreview(Destination destination, FinderPathFactory finderPathFactory, Places places,
            Dispatcher dispatcher, StatementContext statementContext, Resources resources) {
        super(destination.getName(), destination.type.type);

        getHeaderContainer().appendChild(refreshLink(() -> update(null)));
        if (destination.fromDeployment()) {
            FinderPath path = finderPathFactory.deployment(destination.getDeployment());
            PlaceRequest placeRequest = places.finderPlace(NameTokens.DEPLOYMENTS, path).build();
            Elements.removeChildrenFrom(getLeadElement());
            getLeadElement().appendChild(
                    document.createTextNode(destination.type.type + " @ "));
            getLeadElement().appendChild(a(places.historyToken(placeRequest))
                    .textContent(destination.getPath())
                    .title(resources.messages().goTo(Names.DEPLOYMENTS))
                    .asElement());
        }
    }

    @Override
    public void update(Destination item) {
        super.update(item);
    }
}
