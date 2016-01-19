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
package org.jboss.hal.client.runtime;

import elemental.dom.Element;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.hal.ballroom.layout.LayoutBuilder;
import org.jboss.hal.core.PatternFlyViewImpl;

/**
 * @author Harald Pehl
 */
public class RuntimeView extends PatternFlyViewImpl implements RuntimePresenter.MyView {

    public RuntimeView() {
        Element someText = new Elements.Builder().p().innerText("First some text above the tabs.").end().build();
        // @formatter:off
        Element root = new LayoutBuilder()
            .startRow()
                .header("Tabs Demo")
                .add(someText)
                .startTabs()
                    .tab("first", "First", sampleContent(1))
                    .tab("second", "Second", sampleContent(2))
                    .tab("third", "Third", sampleContent(3))
                .endTabs()
            .endRow()
        .build();
        // @formatter:on
        initWidget(Elements.asWidget(root));
    }

    private Element sampleContent(int number) {
        return new Elements.Builder().div().h(3).innerText("Sample Content #" + number).end()
                .p().innerText("Lorem ipsum " + number).end().end().build();
    }
}
