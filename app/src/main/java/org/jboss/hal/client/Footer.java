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
package org.jboss.hal.client;

import elemental.client.Browser;
import elemental.dom.Element;
import elemental.html.SpanElement;
import org.jboss.hal.ballroom.Elements;
import org.jboss.hal.ballroom.Id;
import org.jboss.hal.ballroom.IsElement;
import org.jboss.hal.ballroom.ProgressElement;
import org.jboss.hal.config.Environment;
import org.jboss.hal.resources.HalIds;
import org.jboss.hal.resources.I18n;

import javax.inject.Inject;

import static org.jboss.hal.ballroom.Elements.EventType.click;

/**
 * @author Harald Pehl
 */
public class Footer implements IsElement {

    private final ProgressElement progressElement;
    private final I18n i18n;
    private final HalIds ids;

    private SpanElement halVersion;
    private Element updateAvailable;

    @Inject
    public Footer(final ProgressElement progressElement,
            final I18n i18n,
            final HalIds ids) {
        this.progressElement = progressElement;
        this.i18n = i18n;
        this.ids = ids;
    }

    @Override
    public Element asElement() {
        elemental.dom.Document document = Browser.getDocument();

        halVersion = document.createSpanElement();
        updateAvailable = document.createElement("i");

        Id.set(halVersion, ids.footer_version());
        Elements.setVisible(updateAvailable, false);

        // @formatter:off
        return new Elements.Builder()
            .start("footer").css("footer")
                .start("nav").css("navbar navbar-footer navbar-fixed-bottom")
                    .add(progressElement.asElement()).css("footer-progress")
                    .ul().css("nav navbar-nav footer-tools")
                        .li()
                            .a().on(click, event -> showVersion())
                                .start(halVersion).end()
                                .start(updateAvailable).end()
                            .end() // a
                        .end() // li
                        .li().css("dropdown")
                            .a().css("dropdown-toggle").attribute("data-toggle", "dropdown")
                                .span().css("fa fa-wrench").end()
                                .span().innerText(i18n.constants().tools()).end()
                                .start("b").css("caret").end()
                            .end() // a
                            .ul().css("dropdown-menu dropdown")
                                .li().a().on(click, event -> modelBrowser())
                                    .innerText(i18n.constants().model_browser())
                                .end().end()
                                .li().a().on(click, event -> expressionResolver())
                                    .innerText(i18n.constants().expression_resolver())
                                .end().end()
                            .end() // ul
                        .end() // li
                        .li().a().on(click, event -> settings())
                            .span().css("pficon pficon-settings").end()
                            .span().innerText(i18n.constants().settings()).end()
                        .end() // li
                    .end() // ul
                .end() // nav
            .end() // footer
        .build();
        // @formatter:on
    }

    public void init(Environment environment) {
        halVersion.setInnerText(environment.getHalVersion().toString());
        if (environment.halUpdateAvailable()) {
            halVersion.setTitle(i18n.messages().update_available(environment.getHalVersion().toString(),
                    environment.getLatestHalVersion().toString()));
            Elements.setVisible(updateAvailable, true);
        }
    }

    private void showVersion() {

    }

    private void modelBrowser() {

    }

    private void expressionResolver() {

    }

    private void settings() {

    }
}
