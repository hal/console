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

import com.google.common.base.Joiner;
import com.gwtplatform.mvp.client.ViewImpl;
import elemental.dom.Element;
import elemental.html.SpanElement;
import org.jboss.hal.ballroom.Elements;
import org.jboss.hal.ballroom.Id;
import org.jboss.hal.client.NameTokens;
import org.jboss.hal.config.Endpoints;
import org.jboss.hal.config.Environment;
import org.jboss.hal.config.User;
import org.jboss.hal.core.messaging.Message;
import org.jboss.hal.resources.HalIds;
import org.jboss.hal.resources.I18n;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;

import static org.jboss.hal.ballroom.Elements.EventType.click;
import static org.jboss.hal.config.InstanceInfo.EAP;
import static org.jboss.hal.config.InstanceInfo.WILDFLY;

/**
 * @author Harald Pehl
 */
public class HeaderView extends ViewImpl implements HeaderPresenter.MyView {

    private final static String TOGGLE_NAV_SELECTOR = "hal-header-collapse";

    private final I18n i18n;
    private final HalIds ids;
    private final Map<String, Element> tlc;

    private HeaderPresenter presenter;

    private SpanElement logoFirst;
    private SpanElement logoLast;
    private SpanElement messagesLabel;
    private SpanElement username;
    private SpanElement roles;
    private SpanElement connectedTo;

    @Inject
    public HeaderView(final I18n i18n,
            final HalIds ids) {
        this.i18n = i18n;
        this.ids = ids;
        this.tlc = new HashMap<>();

        initWidget(Elements.asWidget(init()));
    }

    private Element init() {
        // @formatter:off
        Elements.Builder headerBuilder = new Elements.Builder()
            .div().css("navbar-header")
                .button().css("navbar-toggle").attr("type", "button").data("toggle", "collapse").data("target", "." + TOGGLE_NAV_SELECTOR)
                    .span().css("sr-only").innerText(i18n.constants().toggle_navigation()).end()
                    .span().css("icon-bar").end()
                    .span().css("icon-bar").end()
                    .span().css("icon-bar").end()
                .end()
                .a().css("clickable navbar-brand logo").on(click, event -> presenter.navigateTo(NameTokens.Homepage))
                    .span().css("logo-text logo-text-first").rememberAs("logoFirst").end()
                    .span().css("logo-text logo-text-last").rememberAs("logoLast").end()
                .end()
            .end();
        // @formatter:on

        logoFirst = headerBuilder.referenceFor("logoFirst");
        logoLast = headerBuilder.referenceFor("logoLast");
        Element header = headerBuilder.build();

        // @formatter:off
        Elements.Builder toolsBuilder = new Elements.Builder()
            .ul().css("nav navbar-nav navbar-utility")
                .li()
                    .a().css("clickable").on(click, event -> presenter.toggleMessages())
                        .start("i").css("fa fa-envelope").end()
                        .span().rememberAs("messagesLabel").end()
                    .end()
                .end()
                .li().css("dropdown")
                    .a().css("clickable dropdown-toggle").data("toggle", "dropdown")
                        .span().css("pficon pficon-user").end()
                        .span().rememberAs("username").end()
                        .start("b").css("caret").end()
                    .end()
                    .ul().css("dropdown-menu")
                        .li().css("static").span().rememberAs("roles").end().end()
                        .li().css("divider").end()
                        .li().a().css("clickable").on(click, event -> presenter.logout())
                            .innerText(i18n.constants().logout())
                        .end().end()
                    .end()
                .end()
                .li().css("dropdown")
                    .a().css("clickable dropdown-toggle").data("toggle", "dropdown")
                        .span().css("fa fa-share-alt").end()
                        .start("b").css("caret").end()
                    .end()
                    .ul().css("dropdown-menu")
                        .li().css("static").span().rememberAs("connectedTo").end().end()
                        .li().css("divider").end()
                        .li().a().css("clickable").on(click, event -> presenter.reconnect())
                            .innerText(i18n.constants().connect_to_server())
                        .end().end()
                    .end()
                .end()
            .end();
        // @formatter:on

        messagesLabel = toolsBuilder.referenceFor("messagesLabel");
        username = toolsBuilder.referenceFor("username");
        roles = toolsBuilder.referenceFor("roles");
        connectedTo = toolsBuilder.referenceFor("connectedTo");

        Id.set(messagesLabel, ids.header_messages());
        Id.set(username, ids.header_username());
        Id.set(roles, ids.header_roles());
        Id.set(connectedTo, ids.header_connected_to());

        Element tools = toolsBuilder.build();

        // @formatter:off
        Elements.Builder tlcBuilder = new Elements.Builder()
            .ul().css("nav navbar-nav navbar-primary")
                .li()
                    .a()
                        .css("clickable")
                        .id(ids.tlc_home())
                        .rememberAs(ids.tlc_home())
                        .on(click, event -> presenter.navigateTo(NameTokens.Homepage))
                        .innerText("Homepage")
                    .end()
                .end()
                .li()
                    .a()
                        .css("clickable")
                        .id(ids.tlc_deployments())
                        .rememberAs(ids.tlc_deployments())
                        .on(click, event -> presenter.navigateTo(NameTokens.Deployments))
                        .innerText("Deployments")
                    .end()
                .end()
                .li()
                    .a()
                        .css("clickable")
                        .id(ids.tlc_configuration())
                        .rememberAs(ids.tlc_configuration())
                        .on(click, event -> presenter.navigateTo(NameTokens.Configuration))
                        .innerText("Configuration")
                    .end()
                .end()
                .li()
                    .a()
                        .css("clickable")
                        .id(ids.tlc_runtime())
                        .rememberAs(ids.tlc_runtime())
                        .on(click, event -> presenter.navigateTo(NameTokens.Runtime))
                        .innerText("Runtime")
                    .end()
                .end()
                .li()
                    .a()
                        .css("clickable")
                        .id(ids.tlc_access_control())
                        .rememberAs(ids.tlc_access_control())
                        .on(click, event -> presenter.navigateTo(NameTokens.AccessControl))
                        .innerText("Access Control")
                    .end()
                .end()
            .end();
        // @formatter:on

        Element tlcElement = tlcBuilder.build();
        tlc.put(NameTokens.Homepage, tlcBuilder.referenceFor(ids.tlc_home()));
        tlc.put(NameTokens.Deployments, tlcBuilder.referenceFor(ids.tlc_deployments()));
        tlc.put(NameTokens.Configuration, tlcBuilder.referenceFor(ids.tlc_configuration()));
        tlc.put(NameTokens.Runtime, tlcBuilder.referenceFor(ids.tlc_runtime()));
        tlc.put(NameTokens.AccessControl, tlcBuilder.referenceFor(ids.tlc_access_control()));

        // @formatter:off
        return new Elements.Builder()
            .start("nav").css("navbar navbar-default navbar-pf navbar-fixed-top").attr("role", "navigation")
                .add(header)
                .div().css("collapse navbar-collapse " + TOGGLE_NAV_SELECTOR)
                    .add(tools)
                    .add(tlcElement)
                .end()
            .end()
        .build();
        // @formatter:on
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
            connectedTo.setInnerText(i18n.constants().same_origin());
        } else {
            connectedTo.setInnerText(i18n.messages().connected_to(endpoints.dmr()));
        }

        username.setInnerHTML(user.getName());
        // Keep this in sync with the template!
        Elements.setVisible(roles.getParentElement(), !user.getRoles().isEmpty());
        Elements.setVisible(roles.getParentElement().getNextElementSibling(), !user.getRoles().isEmpty());
        roles.setInnerText(i18n.messages().active_roles(Joiner.on(", ").join(user.getRoles())));
    }

    private void setLogo(String first, String last) {
        logoFirst.setInnerText(first);
        logoLast.setInnerText(last);
    }

    @Override
    public void select(final String nameToken) {
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
        messagesLabel.setInnerText(i18n.messages().messages(count));
    }

    @Override
    public void showMessage(final Message.Level level, final String message) {

    }
}
