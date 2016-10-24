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
import elemental.client.Browser;
import elemental.dom.Element;
import org.jboss.gwt.elemento.core.IsElement;
import org.jboss.hal.config.Endpoints;
import org.jboss.hal.config.Environment;
import org.jboss.hal.config.User;
import org.jboss.hal.core.finder.FinderContext;
import org.jboss.hal.core.finder.FinderContextEvent;
import org.jboss.hal.core.finder.FinderContextEvent.FinderContextHandler;
import org.jboss.hal.core.header.HeaderModeEvent;
import org.jboss.hal.core.header.HeaderModeEvent.HeaderModeHandler;
import org.jboss.hal.core.header.PresenterType;
import org.jboss.hal.core.modelbrowser.ModelBrowserPath;
import org.jboss.hal.core.modelbrowser.ModelBrowserPathEvent;
import org.jboss.hal.core.modelbrowser.ModelBrowserPathEvent.ModelBrowserPathHandler;
import org.jboss.hal.core.mvp.HalView;
import org.jboss.hal.core.mvp.HasPresenter;
import org.jboss.hal.core.mvp.Places;
import org.jboss.hal.dmr.model.ResourceAddress;
import org.jboss.hal.meta.token.NameTokens;
import org.jboss.hal.resources.Names;
import org.jboss.hal.spi.Message;
import org.jboss.hal.spi.MessageEvent;
import org.jboss.hal.spi.MessageEvent.MessageHandler;

/**
 * Presenter which controls the header. The header is a central UI element in HAL showing global state such as messages
 * or the current user. Additionally it contains the navigation which is either the top level categories (tlc) or the
 * breadcrumb.
 * <p>
 * The breadcrumb shows path like information such as the selected finder path or the selected address in the model
 * browser. More precisely the breadcrumb consists of these parts:
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
 * only be controlled by sending GWT events. A direct modification using methods is not allowed.
 *
 * @author Harald Pehl
 */
public class HeaderPresenter extends PresenterWidget<HeaderPresenter.MyView>
        implements MessageHandler, HeaderModeHandler, FinderContextHandler, ModelBrowserPathHandler, IsElement {

    // @formatter:off
    public interface MyView extends HalView, HasPresenter<HeaderPresenter> {
        void init(Environment environment, Endpoints endpoints, User user);

        void topLevelCategoryMode();
        void applicationMode();

        void showMessage(Message message);

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


    static final int MAX_BREADCRUMB_VALUE_LENGTH = 20;

    private final PlaceManager placeManager;
    private final Environment environment;
    private final Endpoints endpoints;
    private final User user;

    private PresenterType presenterType;
    private PlaceRequest normalMode;

    @Inject
    public HeaderPresenter(final EventBus eventBus,
            final MyView view,
            final PlaceManager placeManager,
            final Environment environment,
            final Endpoints endpoints,
            final User user) {
        super(eventBus, view);
        this.placeManager = placeManager;
        this.environment = environment;
        this.endpoints = endpoints;
        this.user = user;
    }

    @Override
    public Element asElement() {
        return getView().asElement();
    }

    @Override
    protected void onBind() {
        super.onBind();
        getView().setPresenter(this);
        getView().init(environment, endpoints, user);

        registerHandler(getEventBus().addHandler(MessageEvent.getType(), this));
        registerHandler(getEventBus().addHandler(HeaderModeEvent.getType(), this));
        registerHandler(getEventBus().addHandler(FinderContextEvent.getType(), this));
        registerHandler(getEventBus().addHandler(ModelBrowserPathEvent.getType(), this));
    }


    // ------------------------------------------------------ messages & global state

    @Override
    public void onMessage(final MessageEvent event) {
        getView().showMessage(event.getMessage());
    }

    void toggleMessages() {
        Browser.getWindow().alert(Names.NYI);
    }

    void reconnect() {
        Browser.getWindow().alert(Names.NYI);
    }

    void logout() {
        Browser.getWindow().alert(Names.NYI);
    }


    // ------------------------------------------------------ tlc & breadcrumb

    @Override
    public void onHeaderMode(final HeaderModeEvent event) {
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
    public void onFinderContext(final FinderContextEvent event) {
        getView().updateLinks(event.getFinderContext());
        if (presenterType == PresenterType.APPLICATION) {
            getView().updateBreadcrumb(event.getFinderContext());
        }
    }

    @Override
    public void onModelBrowserAddress(final ModelBrowserPathEvent event) {
        if (presenterType == PresenterType.APPLICATION) {
            getView().updateBreadcrumb(event.getPath());
        }
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
        }
    }

    void goTo(final String token) {
        goTo(new PlaceRequest.Builder().nameToken(token).build());
    }

    void goTo(PlaceRequest placeRequest) {
        placeManager.revealPlace(placeRequest);
    }
}
