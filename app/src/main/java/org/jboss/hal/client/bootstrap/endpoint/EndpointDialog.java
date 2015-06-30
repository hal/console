package org.jboss.hal.client.bootstrap.endpoint;

import com.google.gwt.user.client.ui.DeckLayoutPanel;
import com.google.gwt.user.client.ui.PopupPanel;
import org.jboss.ballroom.client.widgets.window.DefaultWindow;

/**
 * Modal dialog to manage bootstrap servers. The dialog offers a page to connect to an existing server and a page to
 * add new servers.
 *
 * @author Harald Pehl
 */
class EndpointDialog {

    private final PopupPanel popupPanel;
    private final DeckLayoutPanel deck;
    private final ConnectPage connectPage;
    private final ConfigurePage configurePage;

    EndpointDialog(final EndpointSelection endpointSelection) {
        connectPage = new ConnectPage(serverSetup, this);
        configurePage = new ConfigurePage(serverSetup, this);

        deck = new DeckLayoutPanel();
        deck.addStyleName("window-content"); // white background for forms
        deck.addStyleName("default-window-content");
        deck.add(connectPage);
        deck.add(configurePage);

        int width = 700;
        popupPanel = new PopupPanel(false, true);
        popupPanel.setGlassEnabled(true);
        popupPanel.setAnimationEnabled(false);
        popupPanel.setWidget(deck);
        popupPanel.setWidth(String.valueOf(width) + "px");
        popupPanel.setHeight(String.valueOf(width / DefaultWindow.GOLDEN_RATIO) + "px");
        popupPanel.setStyleName("default-window");
    }

    void open() {
        connectPage.reset();
        deck.showWidget(0);
        popupPanel.center();
    }

    void hide() {
        popupPanel.hide();
    }

    void onConfigure() {
        configurePage.reset();
        deck.showWidget(1);
    }

    void onConfigureOk() {
        connectPage.reset();
        deck.showWidget(0);
    }

    void onConfigureCancel() {
        connectPage.reset();
        deck.showWidget(0);
    }
}
