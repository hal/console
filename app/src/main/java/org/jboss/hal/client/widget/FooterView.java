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
package org.jboss.hal.client.widget;

import com.gwtplatform.mvp.client.ViewImpl;
import elemental.dom.Element;
import elemental.html.SpanElement;
import org.jboss.gwt.waiwai.Elements;
import org.jboss.hal.ballroom.Id;
import org.jboss.hal.ballroom.ProgressElement;
import org.jboss.hal.config.Environment;
import org.jboss.hal.core.registry.UIRegistry;
import org.jboss.hal.resources.HalIds;
import org.jboss.hal.resources.I18n;

import javax.inject.Inject;

import static org.jboss.gwt.waiwai.Elements.EventType.click;

/**
 * @author Harald Pehl
 */
public class FooterView extends ViewImpl implements FooterPresenter.MyView {

    private final UIRegistry uiRegistry;
    private final I18n i18n;
    private final HalIds ids;

    private FooterPresenter presenter;

    private SpanElement halVersion;
    private Element updateAvailable;

    @Inject
    public FooterView(final UIRegistry uiRegistry,
            final I18n i18n,
            final HalIds ids) {
        this.uiRegistry = uiRegistry;
        this.i18n = i18n;
        this.ids = ids;

        initWidget(Elements.asWidget(init()));
    }

    private Element init() {
        ProgressElement progressElement = new ProgressElement();
        uiRegistry.register(progressElement);

        // @formatter:off
        Elements.Builder builder = new Elements.Builder()
            .start("footer").css("footer")
                .start("nav").css("navbar navbar-footer navbar-fixed-bottom")
                    .add(progressElement.asElement()).css("footer-progress")
                    .ul().css("nav navbar-nav footer-tools")
                        .li()
                            .a().css("clickable").on(click, event -> presenter.onShowVersion())
                                .start("i")
                                    .css("fa fa-bell-o")
                                    .attr("title", i18n.constants().update_available())
                                    .rememberAs("updateAvailable")
                                .end()
                                .span().rememberAs("halVersion").end()
                            .end()
                        .end()
                        .li().css("dropdown")
                            .a().css("clickable dropdown-toggle").data("toggle", "dropdown")
                                .span().css("fa fa-wrench").end()
                                .span().innerText(i18n.constants().tools()).end()
                                .start("b").css("caret").end()
                            .end()
                            .ul().css("dropdown-menu dropdown")
                                .li().a().css("clickable").on(click, event -> presenter.onModelBrowser())
                                    .innerText(i18n.constants().model_browser())
                                .end().end()
                                .li().a().css("clickable").on(click, event -> presenter.onExpressionResolver())
                                    .innerText(i18n.constants().expression_resolver())
                                .end().end()
                            .end()
                        .end()
                        .li().a().css("clickable").on(click, event -> presenter.onSettings())
                            .span().css("pficon pficon-settings").end()
                            .span().innerText(i18n.constants().settings()).end()
                        .end().end()
                    .end()
                .end()
            .end();
        // @formatter:on

        halVersion = builder.referenceFor("halVersion");
        updateAvailable = builder.referenceFor("updateAvailable");
        Id.set(halVersion, ids.footer_version());
        Elements.setVisible(updateAvailable, false);

        return builder.build();
    }

    @Override
    public void setPresenter(final FooterPresenter presenter) {
        this.presenter = presenter;
    }

    public void update(Environment environment) {
        halVersion.setInnerText(environment.getHalVersion().toString());
        if (environment.halUpdateAvailable()) {
            halVersion.setTitle(i18n.messages().update_available(environment.getHalVersion().toString(),
                    environment.getLatestHalVersion().toString()));
            Elements.setVisible(updateAvailable, true);
        }
    }
}
