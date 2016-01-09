/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2010, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.hal.client.configuration;

import com.gwtplatform.mvp.client.ViewImpl;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;
import com.gwtplatform.mvp.shared.proxy.TokenFormatter;
import elemental.dom.Element;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.hal.ballroom.layout.LayoutBuilder;
import org.jboss.hal.client.NameTokens;

import javax.inject.Inject;

import static org.jboss.hal.resources.Names.CONFIGURATION;
import static org.jboss.hal.resources.Names.PROFILE;

/**
 * @author Harald Pehl
 */
@SuppressWarnings("HardCodedStringLiteral")
public class ConfigurationView extends ViewImpl implements ConfigurationPresenter.MyView {

    @Inject
    public ConfigurationView(TokenFormatter tokenFormatter) {
        PlaceRequest placeRequest = new PlaceRequest.Builder().nameToken(NameTokens.DATASOURCE).with(PROFILE, "full")
                .build();
        String token = "#" + tokenFormatter.toPlaceToken(placeRequest);

        // @formatter:off
        Element element = new LayoutBuilder()
            .startRow()
                .header(CONFIGURATION)
                .add(new Elements.Builder()
                        .p().innerText("Please select from one of the configuration links below").end()
                        .ul()
                            .li().a(token).innerText("Profile (full) / DataSources").end().end()
                        .end()
                    .elements())
            .endRow()
        .build();
        // @formatter:on
        initWidget(Elements.asWidget(element));
    }
}
