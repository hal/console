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

import com.google.common.base.Joiner;
import com.google.gwt.user.client.Window;
import com.gwtplatform.mvp.client.ViewImpl;
import elemental.client.Browser;
import elemental.dom.Document;
import elemental.dom.Element;
import org.jboss.gwt.elemento.core.DataElement;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.gwt.elemento.core.EventHandler;
import org.jboss.gwt.elemento.core.IsElement;
import org.jboss.gwt.elemento.core.Templated;
import org.jboss.hal.client.NameTokens;
import org.jboss.hal.config.Endpoints;
import org.jboss.hal.config.Environment;
import org.jboss.hal.config.User;
import org.jboss.hal.core.messaging.Message;
import org.jboss.hal.resources.HalIds;
import org.jboss.hal.resources.I18n;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

import static org.jboss.gwt.elemento.core.EventType.click;
import static org.jboss.hal.config.InstanceInfo.EAP;
import static org.jboss.hal.config.InstanceInfo.WILDFLY;

/**
 * @author Harald Pehl
 */
@Templated("MainLayout.html#header")
public abstract class HeaderView extends ViewImpl implements HeaderPresenter.MyView, IsElement {

    // @formatter:off
    public static HeaderView create(final I18n i18n, final HalIds ids) {
        return new Templated_HeaderView(i18n, ids);
    }

    public abstract I18n i18n();
    public abstract HalIds ids();
    // @formatter:on


    private Map<String, Element> tlc;
    private HeaderPresenter presenter;

    @DataElement Element logoFirst;
    @DataElement Element logoLast;
    @DataElement Element messagesLabel;
    @DataElement Element userName;
    @DataElement Element roles;
    @DataElement Element connectedTo;

    @PostConstruct
    void init() {
        Element root = asElement();
        Document document = Browser.getDocument();

        tlc = new HashMap<>();
        tlc.put(NameTokens.Homepage, document.getElementById(ids().tlc_homepage()));
        tlc.put(NameTokens.Deployments, document.getElementById(ids().tlc_deployments()));
        tlc.put(NameTokens.Configuration, document.getElementById(ids().tlc_configuration()));
        tlc.put(NameTokens.Runtime, document.getElementById(ids().tlc_runtime()));
        tlc.put(NameTokens.AccessControl, document.getElementById(ids().tlc_access_control()));

        initWidget(Elements.asWidget(root));
    }

    @Override
    public void setPresenter(final HeaderPresenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public void update(Environment environment, Endpoints endpoints, User user) {
        // TODO Find a way how to set the logo based on the server info from the environment
        if (environment.getInstanceInfo() == WILDFLY) {
            setLogo("Wild", "Fly");
        } else if (environment.getInstanceInfo() == EAP) {
            setLogo("Red Hat JBoss", "Enterprise Application Platform");
        } else {
            setLogo("HAL", "Management Console");
        }

        if (endpoints.isSameOrigin()) {
            connectedTo.setInnerText(i18n().constants().same_origin());
        } else {
            connectedTo.setInnerText(i18n().messages().connected_to(endpoints.dmr()));
        }

        userName.setInnerHTML(user.getName());
        // Keep this in sync with the template!
        Elements.setVisible(roles, !user.getRoles().isEmpty());
        Elements.setVisible(roles.getNextElementSibling(), !user.getRoles().isEmpty());
        roles.setInnerText(i18n().messages().active_roles(Joiner.on(", ").join(user.getRoles())));
    }

    private void setLogo(String first, String last) {
        logoFirst.setInnerText(first);
        logoLast.setInnerText(last);
    }

    @Override
    public void selectTlc(final String nameToken) {
        for (String token : tlc.keySet()) {
            if (token.equals(nameToken)) {
                tlc.get(token).getClassList().add("active");
                tlc.get(token).getParentElement().getClassList().add("active");
            } else {
                tlc.get(token).getClassList().remove("active");
                tlc.get(token).getParentElement().getClassList().remove("active");
            }
        }
    }

    @Override
    public void updateMessageCount(int count) {
        messagesLabel.setInnerText(i18n().messages().messages(count));
    }

    @Override
    public void showMessage(final Message.Level level, final String message) {

    }

    @EventHandler(element = "logoLink", on = click)
    void onLogo() {
        presenter.navigateTo(NameTokens.Homepage);
    }

    @EventHandler(element = "messages", on = click)
    void onMessages() {
        Window.alert("Messages not yet implemented");
    }

    @EventHandler(element = "logout", on = click)
    void onLogout() {
        Window.alert("Logout not yet implemented");
    }

    @EventHandler(element = "reconnect", on = click)
    void onReconnect() {
        Window.alert("Reconnect not yet implemented");
    }

    @EventHandler(element = "homepage", on = click)
    void onHomepage() {
        presenter.navigateTo(NameTokens.Homepage);
    }

    @EventHandler(element = "deployments", on = click)
    void onDeployments() {
        presenter.navigateTo(NameTokens.Deployments);
    }

    @EventHandler(element = "configuration", on = click)
    void onConfiguration() {
        presenter.navigateTo(NameTokens.Configuration);
    }

    @EventHandler(element = "runtime", on = click)
    void onRuntime() {
        presenter.navigateTo(NameTokens.Runtime);
    }

    @EventHandler(element = "accessControl", on = click)
    void onAccessControl() {
        presenter.navigateTo(NameTokens.AccessControl);
    }
}
