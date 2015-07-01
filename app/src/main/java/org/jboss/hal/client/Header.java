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

import com.google.common.base.Joiner;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;
import elemental.client.Browser;
import elemental.dom.Document;
import elemental.dom.Element;
import elemental.html.SpanElement;
import org.jboss.hal.ballroom.Elements;
import org.jboss.hal.ballroom.Id;
import org.jboss.hal.ballroom.IsElement;
import org.jboss.hal.config.Endpoints;
import org.jboss.hal.config.Environment;
import org.jboss.hal.config.User;
import org.jboss.hal.resources.HalIds;
import org.jboss.hal.resources.I18n;

import javax.inject.Inject;

import static org.jboss.hal.ballroom.Elements.EventType.click;
import static org.jboss.hal.config.InstanceInfo.EAP;
import static org.jboss.hal.config.InstanceInfo.WILDFLY;

/**
 * @author Harald Pehl
 */
public class Header implements IsElement {

    private final static String TOGGLE_NAV_SELECTOR = "hal-header-collapse";

    private final PlaceManager placeManager;
    private final I18n i18n;
    private final HalIds ids;

    private SpanElement logoFirst;
    private SpanElement logoLast;
    private SpanElement messagesLabel;
    private SpanElement username;
    private SpanElement roles;
    private SpanElement connectedTo;

    @Inject
    public Header(final PlaceManager placeManager,
            final I18n i18n,
            final HalIds ids) {
        this.placeManager = placeManager;
        this.i18n = i18n;
        this.ids = ids;
    }

    @Override
    public Element asElement() {
        Document document = Browser.getDocument();

        logoFirst = document.createSpanElement();
        logoLast = document.createSpanElement();
        messagesLabel = document.createSpanElement();
        username = document.createSpanElement();
        roles = document.createSpanElement();
        connectedTo = document.createSpanElement();

        Id.set(messagesLabel, ids.header_messages());
        Id.set(username, ids.header_username());
        Id.set(roles, ids.header_roles());
        Id.set(connectedTo, ids.header_connected_to());

        // @formatter:off
        Element header = new Elements.Builder()
            .div().css("navbar-header")
                .button().css("navbar-toggle")
                        .attr("type", "button")
                        .data("toggle", "collapse")
                        .data("target", "." + TOGGLE_NAV_SELECTOR)
                    .span().css("sr-only").innerText(i18n.constants().toggle_navigation()).end()
                    .span().css("icon-bar").end()
                    .span().css("icon-bar").end()
                    .span().css("icon-bar").end()
                .end() // button
                .a().css("navbar-brand logo").on(click, event -> navigateTo(NameTokens.Homepage))
                    .start(logoFirst).css("logo-text logo-text-first").end()
                    .start(logoLast).css("logo-text logo-text-first").end()
                .end() // a
            .end() // div
        .build();
        // @formatter:on

        // @formatter:off
        Element tools = new Elements.Builder()
                .ul().css("nav navbar-nav navbar-utility")
                    .li()
                        .a().css("clickable").on(click, event -> toggleMessages())
                            .start("i").css("fa fa-align-left").end()
                            .start(messagesLabel).end()
                        .end() // a
                    .end() // li
                    .li().css("dropdown")
                        .a().css("dropdown-toggle").data("toggle", "dropdown")
                            .span().css("pficon pficon-user").end()
                            .start(username).end()
                            .start("b").css("caret").end()
                        .end() // a
                        .ul().css("dropdown-menu")
                            .li().css("static").start(roles).end().end()
                            .li().css("divider").end()
                            .li().a().css("clickable").on(click, event -> logout())
                                .innerText(i18n.constants().logout())
                            .end().end()
                        .end() // ul
                    .end() // li
                    .li().css("dropdown")
                        .a().css("dropdown-toggle").data("toggle", "dropdown")
                            .span().css("fa fa-share-alt").end()
                            .start("b").css("caret").end()
                        .end() // a
                        .ul().css("dropdown-menu")
                            .li().css("static").start(connectedTo).end().end()
                            .li().css("divider").end()
                            .li().a().css("clickable").on(click, event -> reconnect())
                                .innerText(i18n.constants().connect_to_server())
                            .end().end()
                        .end() // ul
                    .end() // li
                .end()
        .build();
        // @formatter:on

        // @formatter:off
        Element tlc = new Elements.Builder()
            .ul().css("nav navbar-nav navbar-primary")
                .li().a().id(ids.tlc_home()).on(click, event -> navigateTo(NameTokens.Homepage))
                    .innerText("Homepage")
                .end().end()
                .li().a().id(ids.tlc_deployments()).on(click, event -> navigateTo(NameTokens.Deployments))
                    .innerText("Deployments")
                .end().end()
                .li().a().id(ids.tlc_configuration()).on(click, event -> navigateTo(NameTokens.Configuration))
                    .innerText("Configuration")
                .end().end()
                .li().a().id(ids.tlc_runtime()).on(click, event -> navigateTo(NameTokens.Runtime))
                    .innerText("Runtime")
                .end().end()
                .li().a().id(ids.tlc_access_control()).on(click, event -> navigateTo(NameTokens.AccessControl))
                    .innerText("Access Control")
                .end().end()
            .end() // ul
        .build();
        // @formatter:on

        // @formatter:off
        return new Elements.Builder()
            .start("nav").css("navbar navbar-default navbar-pf navbar-fixed-top").attr("role", "navigation")
                .start(header).end()
                .div().css("collapse navbar-collapse " + TOGGLE_NAV_SELECTOR)
                    .start(tools).end()
                    .start(tlc).end()
                .end() // div
            .end() // nav
        .build();
        // @formatter:on
    }

    public void init(Environment environment, Endpoints endpoints, User user) {
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

    public void setMessageCount(int count) {
        messagesLabel.setInnerText(i18n.messages().messages(count));
    }

    private void navigateTo(final String place) {
        PlaceRequest placeRequest = new PlaceRequest.Builder().nameToken(place).build();
        placeManager.revealPlace(placeRequest);
    }

    private void toggleMessages() {

    }

    private void reconnect() {

    }

    private void logout() {

    }

    private void setLogo(String first, String last) {
        logoFirst.setInnerText(first);
        logoLast.setInnerText(last);
    }
}
