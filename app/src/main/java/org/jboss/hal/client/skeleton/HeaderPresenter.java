/*
 * Copyright 2015-2016 Red Hat, Inc, and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.hal.client.skeleton;

import javax.inject.Inject;

import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.PresenterWidget;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;
import elemental2.dom.HTMLElement;
import org.jboss.gwt.elemento.core.IsElement;
import org.jboss.hal.ballroom.dialog.Dialog;
import org.jboss.hal.ballroom.dialog.DialogFactory;
import org.jboss.hal.client.bootstrap.endpoint.EndpointManager;
import org.jboss.hal.config.Endpoints;
import org.jboss.hal.config.Environment;
import org.jboss.hal.config.RolesChangedEvent;
import org.jboss.hal.config.RolesChangedEvent.RolesChangedHandler;
import org.jboss.hal.config.Settings;
import org.jboss.hal.config.User;
import org.jboss.hal.config.UserChangedEvent;
import org.jboss.hal.config.UserChangedEvent.UserChangedHandler;
import org.jboss.hal.config.keycloak.KeycloakHolder;
import org.jboss.hal.core.finder.FinderContext;
import org.jboss.hal.core.finder.FinderContextEvent;
import org.jboss.hal.core.finder.FinderContextEvent.FinderContextHandler;
import org.jboss.hal.core.finder.FinderPath;
import org.jboss.hal.core.header.HeaderModeEvent;
import org.jboss.hal.core.header.HeaderModeEvent.HeaderModeHandler;
import org.jboss.hal.core.header.PresenterType;
import org.jboss.hal.core.modelbrowser.ModelBrowserPath;
import org.jboss.hal.core.modelbrowser.ModelBrowserPathEvent;
import org.jboss.hal.core.modelbrowser.ModelBrowserPathEvent.ModelBrowserPathHandler;
import org.jboss.hal.core.mvp.HalView;
import org.jboss.hal.core.mvp.HasPresenter;
import org.jboss.hal.core.mvp.Places;
import org.jboss.hal.core.runtime.group.ServerGroupResultEvent;
import org.jboss.hal.core.runtime.group.ServerGroupResultEvent.ServerGroupResultHandler;
import org.jboss.hal.core.runtime.host.HostResultEvent;
import org.jboss.hal.core.runtime.host.HostResultEvent.HostResultHandler;
import org.jboss.hal.core.runtime.server.Server;
import org.jboss.hal.core.runtime.server.ServerActionEvent;
import org.jboss.hal.core.runtime.server.ServerActionEvent.ServerActionHandler;
import org.jboss.hal.core.runtime.server.ServerActions;
import org.jboss.hal.core.runtime.server.ServerResultEvent;
import org.jboss.hal.core.runtime.server.ServerResultEvent.ServerResultHandler;
import org.jboss.hal.dmr.ResourceAddress;
import org.jboss.hal.dmr.dispatch.ProcessStateEvent;
import org.jboss.hal.dmr.dispatch.ProcessStateEvent.ProcessStateHandler;
import org.jboss.hal.dmr.dispatch.ServerState;
import org.jboss.hal.dmr.dispatch.ServerState.State;
import org.jboss.hal.meta.token.NameTokens;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.spi.Message;
import org.jboss.hal.spi.MessageEvent;
import org.jboss.hal.spi.MessageEvent.MessageHandler;

import static elemental2.dom.DomGlobal.location;
import static elemental2.dom.DomGlobal.window;
import static org.jboss.gwt.elemento.core.Elements.p;
import static org.jboss.hal.config.Settings.Key.RUN_AS;

/**
 * Presenter which controls the header. The header is a central UI element in HAL showing global state such as
 * reload state, notifications or the current user. Additionally it contains the navigation which is either the
 * top level categories (tlc) or the breadcrumb.
 * <p>
 * The breadcrumb shows path like information such as the selected finder path or the selected address in the model
 * browser. The breadcrumb consists of these parts:
 * <ol>
 * <li>The back link which <em>always</em> brings you back to the last finder selection (unlike the browser's back
 * button)</li>
 * <li>The main part which is either
 * <ul>
 * <li>a title</li>
 * <li>the current finder path</li>
 * <li>the current address of the model browser</li>
 * </ul>
 * <li>A collection of tools / icons. Currently the following tools are available:
 * <ul>
 * <li>Switch between normal and expert mode. If supported by the current presenter, the user can switch between the
 * normal and an expert mode which uses the model browser to show a generic view of the current resource.</li>
 * <li>Open the current presenter in external tab / window w/o the header and footer.</li>
 * </ul></li>
 * </li>
 * </ol>
 * <p>
 * The header presenter is not part of the actual presenters such as finder or application presenters, its content can
 * only be controlled by sending events. A direct modification using methods is not allowed.
 */
public class HeaderPresenter extends PresenterWidget<HeaderPresenter.MyView> implements
        MessageHandler, HeaderModeHandler, FinderContextHandler, ModelBrowserPathHandler,
        HostResultHandler, ServerGroupResultHandler, ServerActionHandler, ServerResultHandler,
        ProcessStateHandler, UserChangedHandler, RolesChangedHandler, IsElement {

    static final int MAX_BREADCRUMB_VALUE_LENGTH = 20;

    private final PlaceManager placeManager;
    private final Places places;
    private final Environment environment;
    private final Endpoints endpoints;
    private final Settings settings;
    private final User user;
    private final ServerActions serverActions;
    private final Resources resources;
    private KeycloakHolder keycloakHolder;

    private PresenterType presenterType;
    private PlaceRequest normalMode;
    private FinderContext lastFinderContext;
    private ServerState serverState;

    @Inject
    public HeaderPresenter(EventBus eventBus,
            MyView view,
            PlaceManager placeManager,
            Places places,
            Environment environment,
            Endpoints endpoints,
            Settings settings,
            User user,
            ServerActions serverActions,
            Resources resources, KeycloakHolder keycloakHolder) {
        super(eventBus, view);
        this.placeManager = placeManager;
        this.places = places;
        this.environment = environment;
        this.endpoints = endpoints;
        this.settings = settings;
        this.user = user;
        this.serverActions = serverActions;
        this.resources = resources;
        this.keycloakHolder = keycloakHolder;
    }

    @Override
    public HTMLElement asElement() {
        return getView().asElement();
    }

    @Override
    protected void onBind() {
        super.onBind();
        getView().setPresenter(this);
        getView().init(environment, endpoints, settings, user);

        registerHandler(getEventBus().addHandler(ProcessStateEvent.getType(), this));
        registerHandler(getEventBus().addHandler(HostResultEvent.getType(), this));
        registerHandler(getEventBus().addHandler(ServerGroupResultEvent.getType(), this));
        registerHandler(getEventBus().addHandler(ServerActionEvent.getType(), this));
        registerHandler(getEventBus().addHandler(ServerResultEvent.getType(), this));
        registerHandler(getEventBus().addHandler(MessageEvent.getType(), this));
        registerHandler(getEventBus().addHandler(HeaderModeEvent.getType(), this));
        registerHandler(getEventBus().addHandler(FinderContextEvent.getType(), this));
        registerHandler(getEventBus().addHandler(ModelBrowserPathEvent.getType(), this));
        registerHandler(getEventBus().addHandler(UserChangedEvent.getType(), this));
        registerHandler(getEventBus().addHandler(RolesChangedEvent.getType(), this));
    }

    @Override
    protected void onReveal() {
        super.onReveal();
        if (endpoints.isSameOrigin()) {
            getView().hideReconnect();
        }
    }


    // ------------------------------------------------------ reload / restart

    @Override
    public void onProcessState(ProcessStateEvent event) {
        serverState = event.getProcessState().first();
        if (environment.isStandalone()) {
            if (serverState.getState() == State.RELOAD_REQUIRED) {
                getView().showReload(resources.constants().reloadRequired(),
                        resources.constants().reloadStandaloneTooltip());

            } else if (serverState.getState() == State.RESTART_REQUIRED) {
                getView().showReload(resources.constants().restartRequired(),
                        resources.constants().restartStandaloneTooltip());
            }

        } else {
            getView().showReload(resources.constants().domainConfigurationChanged(),
                    resources.constants().domainConfigurationChangedTooltip());
        }
    }

    void reload() {
        if (serverState != null) {
            if (environment.isStandalone()) {
                if (serverState.getState() == State.RELOAD_REQUIRED) {
                    serverActions.reload(Server.STANDALONE);

                } else if (serverState.getState() == State.RESTART_REQUIRED) {
                    serverActions.restart(Server.STANDALONE);
                }

            } else {
                FinderPath path = new FinderPath().append(Ids.DOMAIN_BROWSE_BY, Ids.asId(Names.TOPOLOGY));
                PlaceRequest place = places.finderPlace(NameTokens.RUNTIME, path).build();
                placeManager.revealPlace(place);
            }
        }
    }

    @Override
    public void onServerAction(ServerActionEvent event) {
        resetServerState();
    }

    @Override
    public void onServerResult(ServerResultEvent event) {
        resetServerState();
    }

    @Override
    public void onHostResult(HostResultEvent event) {
        resetServerState();
    }

    @Override
    public void onServerGroupResult(ServerGroupResultEvent event) {
        resetServerState();
    }

    private void resetServerState() {
        serverState = null;
        getView().hideReload();
    }


    // ------------------------------------------------------ messages & global state

    @Override
    public void onMessage(MessageEvent event) {
        getView().onMessage(event.getMessage());
    }

    void onMarkAllAsRead() {
        getView().onMarkAllAsRead();
    }

    void onClearMessage() {
        getView().onClearMessage();
    }

    void reconnect() {
        String url = window.location.getProtocol()
                + "//" + window.location.getHost()
                + window.location.getPathname()
                + "?" + EndpointManager.CONNECT_PARAMETER;
        window.location.assign(url);
    }

    void logout() {
        if (environment.isSingleSignOn()) {
            keycloakHolder.getKeycloak().logout(null);
        } else {
            Dialog dialog = new Dialog.Builder(resources.constants().logout())
                    .add(p().innerHtml(resources.messages().closeToLogout()).asElement())
                    .primary(resources.constants().ok(), () -> true)
                    .build();
            dialog.show();
        }
    }


    // ------------------------------------------------------ tlc & breadcrumb

    @Override
    public void onHeaderMode(HeaderModeEvent event) {
        presenterType = event.getPresenterType();
        if (presenterType == PresenterType.TOP_LEVEL_CATEGORY) {
            getView().topLevelCategoryMode();
            if (event.getToken() != null) {
                getView().selectTopLevelCategory(event.getToken());
            }

        } else {
            getView().applicationMode();
            if (event.getTitle() != null) {
                getView().updateBreadcrumb(event.getTitle());
            }
            if (event.isSupportsExternal()) {
                PlaceRequest placeRequest = new PlaceRequest.Builder(placeManager.getCurrentPlaceRequest())
                        .with(Places.EXTERNAL_PARAM, "true") //NON-NLS
                        .build();
                getView().showExternal(placeRequest);
            } else {
                getView().hideExternal();
            }
            if (event.getExpertModeAddress() != null) {
                getView().showExpertMode(event.getExpertModeAddress());
            } else if (event.isBackToNormalMode()) {
                getView().showBackToNormalMode();
            } else {
                getView().hideSwitchMode();
            }
        }
    }

    @Override
    public void onFinderContext(FinderContextEvent event) {
        getView().updateLinks(event.getFinderContext());
        if (presenterType == PresenterType.APPLICATION) {
            lastFinderContext = event.getFinderContext();
            getView().updateBreadcrumb(event.getFinderContext());
        }
    }

    @Override
    public void onModelBrowserAddress(ModelBrowserPathEvent event) {
        if (presenterType == PresenterType.APPLICATION) {
            getView().updateBreadcrumb(event.getPath());
        }
    }

    FinderContext lastFinderContext() {
        return lastFinderContext;
    }


    // ------------------------------------------------------ user & roles

    @Override
    public void onUserChanged(UserChangedEvent event) {
        getView().updateRoles(environment, settings, user);
    }

    @Override
    public void onRolesChanged(RolesChangedEvent event) {
        getView().updateRoles(environment, settings, user);
    }

    void runAs(String role) {
        DialogFactory.showConfirmation(resources.constants().runAsRoleTitle(),
                resources.messages().reloadSettings(), () -> {
                    settings.set(RUN_AS, role);
                    location.reload();
                });
    }

    void clearRunAs() {
        DialogFactory.showConfirmation(resources.constants().clearRunAsTitle(),
                resources.messages().reloadSettings(), () -> {
                    settings.set(RUN_AS, null);
                    location.reload();
                });
    }


    // ------------------------------------------------------ place management

    void switchToExpertMode(ResourceAddress address) {
        normalMode = placeManager.getCurrentPlaceRequest();
        PlaceRequest placeRequest = new PlaceRequest.Builder()
                .nameToken(NameTokens.EXPERT_MODE)
                .with(Places.ADDRESS_PARAM, address.toString())
                .build();
        placeManager.revealPlace(placeRequest);
    }

    void backToNormalMode() {
        if (normalMode != null) {
            placeManager.revealPlace(normalMode);
        } else {
            MessageEvent.fire(getEventBus(), Message.warning(resources.messages().noContextForNormalMode()));
        }
    }

    void goTo(String token) {
        goTo(new PlaceRequest.Builder().nameToken(token).build());
    }

    void goTo(PlaceRequest placeRequest) {
        placeManager.revealPlace(placeRequest);
    }


    // ------------------------------------------------------ inner classes


    // @formatter:off
    public interface MyView extends HalView, HasPresenter<HeaderPresenter> {
        void init(Environment environment, Endpoints endpoints, Settings settings, User user);
        void updateRoles(Environment environment, Settings settings, User user);

        void topLevelCategoryMode();
        void applicationMode();

        void showReload(String text, String tooltip);
        void hideReload();
        void hideReconnect();

        void onMessage(Message message);
        void onMarkAllAsRead();
        void onClearMessage();

        void selectTopLevelCategory(String nameToken);
        void updateLinks(FinderContext finderContext);

        void updateBreadcrumb(String title);
        void updateBreadcrumb(FinderContext finderContext);
        void updateBreadcrumb(ModelBrowserPath modelBrowserPath);

        void showBackToNormalMode();
        void showExpertMode(ResourceAddress address);
        void hideSwitchMode();

        void showExternal(PlaceRequest placeRequest);
        void hideExternal();
    }
    // @formatter:on
}
