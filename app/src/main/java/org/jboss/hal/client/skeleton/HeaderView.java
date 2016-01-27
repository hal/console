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
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;
import com.gwtplatform.mvp.shared.proxy.TokenFormatter;
import elemental.dom.Element;
import org.jboss.gwt.elemento.core.DataElement;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.gwt.elemento.core.EventHandler;
import org.jboss.gwt.elemento.core.IsElement;
import org.jboss.gwt.elemento.core.Templated;
import org.jboss.hal.client.NameTokens;
import org.jboss.hal.config.Endpoints;
import org.jboss.hal.config.Environment;
import org.jboss.hal.config.InstanceInfo;
import org.jboss.hal.config.User;
import org.jboss.hal.core.Breadcrumb;
import org.jboss.hal.core.Breadcrumb.Segment;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.spi.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static org.jboss.gwt.elemento.core.EventType.click;
import static org.jboss.hal.config.InstanceInfo.WILDFLY;
import static org.jboss.hal.resources.CSS.*;
import static org.jboss.hal.resources.Names.*;


/**
 * @author Harald Pehl
 */
@Templated("MainLayout.html#header")
public abstract class HeaderView extends ViewImpl implements HeaderPresenter.MyView, IsElement,
        NameTokens /* makes it easier to reference the name tokens in the HTML template */ {

    // @formatter:off
    public static HeaderView create(final TokenFormatter tokenFormatter, final Resources resources, final User user) {
        return new Templated_HeaderView(tokenFormatter, resources, user);
    }

    public abstract TokenFormatter tokenFormatter();
    public abstract Resources resources();
    public abstract User user();
    // @formatter:on


    private static final Logger logger = LoggerFactory.getLogger(HeaderView.class);

    private Map<String, Element> tlc;
    private HeaderPresenter presenter;

    @DataElement Element logoFirst;
    @DataElement Element logoLast;
    @DataElement Element messagesLabel;
    @DataElement Element userName;
    @DataElement Element roles;
    @DataElement Element connectedTo;
    @DataElement Element accessControl;
    @DataElement Element patching;
    @DataElement Element topLevelTabs;
    @DataElement Element breadcrumbs;


    @PostConstruct
    void init() {
        Element root = asElement();

        tlc = new HashMap<>();
        resources();
        tlc.put(HOMEPAGE, root.querySelector("#" + Ids.TLC_HOMEPAGE));
        tlc.put(DEPLOYMENTS, root.querySelector("#" + Ids.TLC_DEPLOYMENTS));
        tlc.put(CONFIGURATION, root.querySelector("#" + Ids.TLC_CONFIGURATION));
        tlc.put(RUNTIME, root.querySelector("#" + Ids.TLC_RUNTIME));
        tlc.put(ACCESS_CONTROL, root.querySelector("#" + Ids.TLC_ACCESS_CONTROL));
        tlc.put(PATCHING, root.querySelector("#" + Ids.TLC_PATCHING));

        boolean su = user().isSuperuser() || user().isAdministrator();
        Elements.setVisible(accessControl, su);
        Elements.setVisible(patching, su);
        Elements.setVisible(breadcrumbs, false);

        initWidget(Elements.asWidget(root));
    }

    String historyToken(String token) {
        PlaceRequest placeRequest = new PlaceRequest.Builder().nameToken(token).build();
        return "#" + tokenFormatter().toHistoryToken(Collections.singletonList(placeRequest));
    }

    @Override
    public void setPresenter(final HeaderPresenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public void update(Environment environment, Endpoints endpoints, User user) {
        if (environment.getInstanceInfo() == WILDFLY) {
            setLogo(new String[]{
                    environment.getInstanceInfo().description().substring(0, 4),
                    environment.getInstanceInfo().description().substring(4),
            });
        } else if (environment.getInstanceInfo() == InstanceInfo.EAP) {
            setLogo(new String[]{
                    environment.getInstanceInfo().description().substring(0, 13),
                    environment.getInstanceInfo().description().substring(13).trim(),
            });
        } else {
            setLogo(new String[]{HAL, MANAGEMENT_CONSOLE});
        }

        if (endpoints.isSameOrigin()) {
            connectedTo.setInnerText(resources().constants().sameOrigin());
        } else {
            connectedTo.setInnerText(resources().messages().connectedTo(endpoints.dmr()));
        }

        userName.setInnerHTML(user.getName());
        // Keep this in sync with the template!
        Elements.setVisible(roles, !user.getRoles().isEmpty());
        Elements.setVisible(roles.getNextElementSibling(), !user.getRoles().isEmpty());
        roles.setInnerText(resources().messages().activeRoles(Joiner.on(", ").join(user.getRoles())));
    }

    private void setLogo(String[] parts) {
        logoFirst.setInnerText(parts[0]);
        logoLast.setInnerText(parts[1]);
    }

    @Override
    public void selectTlc(final String nameToken) {
        for (String token : tlc.keySet()) {
            if (token.equals(nameToken)) {
                tlc.get(token).getClassList().add(active);
                tlc.get(token).getParentElement().getClassList().add(active);
            } else {
                tlc.get(token).getClassList().remove(active);
                tlc.get(token).getParentElement().getClassList().remove(active);
            }
        }
    }

    @Override
    public void showMessage(final Message message) {
        switch (message.getLevel()) {
            case ERROR:
                logger.error(message.getMessage());
                break;
            case WARNING:
                logger.warn(message.getMessage());
                break;
            case INFO:
                logger.info(message.getMessage());
                break;
        }
    }

    @Override
    public void tlcMode() {
        Elements.setVisible(topLevelTabs, true);
        Elements.setVisible(breadcrumbs, false);
    }

    @Override
    public void applicationMode() {
        Elements.setVisible(topLevelTabs, false);
        Elements.setVisible(breadcrumbs, true);
    }

    @Override
    public void updateBreadcrumb(final Breadcrumb breadcrumb) {
        while (breadcrumbs.getLastChild() != null && breadcrumbs.getChildren().getLength() > 1) {
            breadcrumbs.removeChild(breadcrumbs.getLastChild());
        }
        for (Iterator<Segment> iterator = breadcrumb.iterator(); iterator.hasNext(); ) {
            Segment segment = iterator.next();
            // @formatter:off
            Element li = new Elements.Builder()
                .li()
                    .span().css(key).innerText(segment.key).end()
                    .span().css(value).innerText(segment.value).end()
                .end()
            .build();
            // @formatter:off
            if (!iterator.hasNext()) {
                li.getClassList().add(active);
            }
            breadcrumbs.appendChild(li);
        }
    }

    @EventHandler(element = "logoLink", on = click)
    void onLogo() {
        presenter.goTo(HOMEPAGE);
    }

    @EventHandler(element = "messages", on = click)
    void onMessages() {
        Window.alert(NYI);
    }

    @EventHandler(element = "logout", on = click)
    void onLogout() {
        Window.alert(NYI);
    }

    @EventHandler(element = "reconnect", on = click)
    void onReconnect() {
        Window.alert(NYI);
    }
}
