package org.jboss.hal.client.bootstrap.endpoint;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import org.jboss.as.console.client.Console;
import org.jboss.as.console.client.widgets.ContentDescription;
import org.jboss.ballroom.client.widgets.ContentHeaderLabel;
import org.jboss.ballroom.client.widgets.common.DefaultButton;
import org.jboss.ballroom.client.widgets.forms.*;
import org.jboss.ballroom.client.widgets.window.DialogueOptions;
import org.jboss.ballroom.client.widgets.window.WindowContentBuilder;

import java.util.Arrays;
import java.util.List;

/**
 * @author Harald Pehl
 */
class ConfigurePage implements IsWidget {

    private final BootstrapServerSetup serverSetup;
    private final BootstrapServerDialog serverDialog;
    private final BootstrapServerStore serverStore;

    private Form<BootstrapServer> form;
    private TextBoxItem nameItem;
    private NumberBoxItem portItem;
    private HTML configureStatus;

    ConfigurePage(final BootstrapServerSetup serverSetup, final BootstrapServerDialog serverDialog) {
        this.serverSetup = serverSetup;
        this.serverDialog = serverDialog;
        this.serverStore = new BootstrapServerStore();
    }

    public Widget asWidget() {
        FlowPanel content = new FlowPanel();
        content.add(new ContentHeaderLabel(Console.CONSTANTS.bs_configure_interface_header()));
        content.add(new ContentDescription(Console.CONSTANTS.bs_configure_interface_desc()));

        configureStatus = new HTML();

        form = new Form<>(BootstrapServer.class);
        nameItem = new TextBoxItem("name", Console.CONSTANTS.common_label_name());
        nameItem.getInputElement().setAttribute("placeholder", Console.CONSTANTS.bs_configure_interface_name_placeholder());

        ListBoxItem schemeItem = new ListBoxItem("scheme", "Scheme");
        schemeItem.setChoices(Arrays.asList("http", "https"), "http");
        TextBoxItem hostItem = new TextBoxItem("hostname", "Hostname") {
            @Override
            public boolean validate(String value) {
                boolean validate = super.validate(value);
                if (validate) {
                    if ("localhost".equals(getValue())) {
                        setErrMessage("Localhost does not work reliably. Please use 127.0.0.1 instead.");
                        validate = false;
                    }
                }
                return validate;
            }
        };
        portItem = new NumberBoxItem("port", "Port");

        form.setFields(nameItem, schemeItem, hostItem, portItem);
        content.add(form);

        DefaultButton pingButton = new DefaultButton(Console.CONSTANTS.bs_ping());
        pingButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(final ClickEvent event) {
                FormValidation formValidation = form.validate();
                if (!formValidation.hasErrors()) {
                    final BootstrapServer server = form.getUpdatedEntity();
                    serverSetup.pingServer(server, new AsyncCallback<Void>() {
                        @Override
                        public void onFailure(final Throwable caught) {
                            status(StatusMessage.warning(Console.MESSAGES.bs_interface_warning(serverSetup.getBaseUrl())));
                        }

                        @Override
                        public void onSuccess(final Void result) {
                            status(StatusMessage.success(Console.CONSTANTS.bs_interface_success()));
                        }
                    });
                }
            }
        });
        content.add(pingButton);
        content.add(configureStatus);

        DialogueOptions options = new DialogueOptions(
                Console.CONSTANTS.common_label_add(),
                new ClickHandler() {
                    @Override
                    public void onClick(ClickEvent event) {
                        FormValidation validation = form.validate();
                        if (!validation.hasErrors()) {
                            BootstrapServer newServer = form.getUpdatedEntity();

                            boolean sameName = false;
                            List<BootstrapServer> servers = serverStore.load();
                            for (BootstrapServer server : servers) {
                                if (server.getName().equals(newServer.getName())) {
                                    sameName = true;
                                    break;
                                }
                            }
                            if (sameName) {
                                status(StatusMessage.error(Console.CONSTANTS.bs_configure_interface_duplicate()));
                                nameItem.getInputElement().focus();
                            } else {
                                serverStore.add(newServer);
                                serverDialog.onConfigureOk();
                            }
                        }
                    }
                },
                Console.CONSTANTS.common_label_cancel(),
                new ClickHandler() {
                    @Override
                    public void onClick(final ClickEvent event) {
                        serverDialog.onConfigureCancel();
                    }
                }
        );

        return new WindowContentBuilder(content, options).build();
    }

    void reset() {
        configureStatus.setVisible(false);
        form.clearValues();
        portItem.setValue(9990);
        Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
            @Override
            public void execute() {
                nameItem.getInputElement().focus();
            }
        });
    }

    private void status(SafeHtml message) {
        configureStatus.setVisible(true);
        configureStatus.setHTML(message);
    }
}
