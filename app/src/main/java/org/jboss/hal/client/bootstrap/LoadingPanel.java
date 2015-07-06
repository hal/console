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
package org.jboss.hal.client.bootstrap;

import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author Harald Pehl
 */
public class LoadingPanel implements IsWidget {

    static public LoadingPanel get() {
        if (instance == null) {
            instance = new LoadingPanel();
            instance.off();
            RootPanel.get().add(instance);
        }
        return instance;
    }

    private static LoadingPanel instance;

    static final Template TEMPLATE = GWT.create(Template.class);

    private final HTML html;

    public LoadingPanel() {
        html = new HTML(TEMPLATE.content());
        html.addStyleName("loading-container");
    }

    @Override
    public Widget asWidget() {
        return html;
    }

    public void on() {
        html.setVisible(true);
    }

    public void off() {
        html.setVisible(false);
    }


    interface Template extends SafeHtmlTemplates {

        @SafeHtmlTemplates.Template("<div class=\"loading\"><h3>Loading</h3><span class=\"fa-spin fa-2x pficon pficon-running\"></span></div>")
        SafeHtml content();
    }
}
