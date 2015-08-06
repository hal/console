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
package org.jboss.hal.client.skeleton;

import com.gwtplatform.mvp.client.ViewImpl;
import elemental.dom.Element;
import org.jboss.gwt.elemento.core.DataElement;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.gwt.elemento.core.EventHandler;
import org.jboss.gwt.elemento.core.IsElement;
import org.jboss.gwt.elemento.core.Templated;
import org.jboss.hal.ballroom.ProgressElement;
import org.jboss.hal.config.Environment;
import org.jboss.hal.core.registry.UIRegistry;
import org.jboss.hal.resources.I18n;

import javax.annotation.PostConstruct;

import static org.jboss.gwt.elemento.core.EventType.click;

/**
 * @author Harald Pehl
 */
@Templated("MainLayout.html#footer")
public abstract class FooterView extends ViewImpl implements FooterPresenter.MyView, IsElement {

    // @formatter:off
    public static FooterView create(final UIRegistry uiRegistry, final I18n i18n) {
        return new Templated_FooterView(uiRegistry, i18n);
    }

    public abstract UIRegistry uiRegistry();
    public abstract I18n i18n();
    // @formatter:on


    private FooterPresenter presenter;

    @DataElement ProgressElement progress = new ProgressElement();
    @DataElement Element halVersion;
    @DataElement Element updateAvailable;

    @PostConstruct
    void init() {
        uiRegistry().register(progress);
        Elements.setVisible(updateAvailable, false);
        initWidget(Elements.asWidget(asElement()));
    }

    @Override
    public void setPresenter(final FooterPresenter presenter) {
        this.presenter = presenter;
    }

    public void update(Environment environment) {
        halVersion.setInnerText(environment.getHalVersion().toString());
        if (environment.halUpdateAvailable()) {
            halVersion.setTitle(i18n().messages().update_available(environment.getHalVersion().toString(),
                    environment.getLatestHalVersion().toString()));
            Elements.setVisible(updateAvailable, true);
        }
    }

    @EventHandler(element = "showVersion", on = click)
    void onShowVersion() {
        presenter.onShowVersion();
    }

    @EventHandler(element = "modelBrowser", on = click)
    void onModelBrowser() {
        presenter.onModelBrowser();
    }

    @EventHandler(element = "expressionResolver", on = click)
    void onExpressionResolver() {
        presenter.onExpressionResolver();
    }

    @EventHandler(element = "settings", on = click)
    void onSettings() {
        presenter.onSettings();
    }
}
