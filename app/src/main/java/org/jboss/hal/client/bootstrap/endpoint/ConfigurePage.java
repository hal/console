package org.jboss.hal.client.bootstrap.endpoint;

import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author Harald Pehl
 */
class ConfigurePage implements IsWidget {

    private final EndpointManager endpointManager;
    private final EndpointDialog endpointDialog;
    private final EndpointStorage storage;

//    private Form<BootstrapServer> form;
//    private TextBoxItem nameItem;
//    private NumberBoxItem portItem;
//    private HTML configureStatus;


    ConfigurePage(final EndpointDialog endpointDialog,
            final EndpointManager endpointManager,
            final EndpointStorage storage) {
        this.endpointDialog = endpointDialog;
        this.endpointManager = endpointManager;
        this.storage = storage;
    }

    public Widget asWidget() {
        return new Label("NYI");
/*
        FlowPanel content = new FlowPanel();
        content.add(new ContentHeaderLabel(Console.CONSTANTS.bs_configure_interface_header()));
        content.add(new ContentDescription(Console.CONSTANTS.bs_configure_interface_desc()));

        configureStatus = new HTML();

        form = new Form<>(BootstrapServer.class);
        nameItem = new TextBoxItem("name", Console.CONSTANTS.common_label_name());
        nameItem.getInputElement()
                .setAttribute("placeholder", Console.CONSTANTS.bs_configure_interface_name_placeholder());

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
                    endpointManager.pingServer(server, new AsyncCallback<Void>() {
                        @Override
                        public void onFailure(final Throwable caught) {
                            status(StatusMessage
                                    .warning(Console.MESSAGES.bs_interface_warning(endpointManager.getBaseUrl())));
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
                            List<BootstrapServer> servers = storage.load();
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
                                storage.add(newServer);
                                endpointDialog.onConfigureOk();
                            }
                        }
                    }
                },
                Console.CONSTANTS.common_label_cancel(),
                new ClickHandler() {
                    @Override
                    public void onClick(final ClickEvent event) {
                        endpointDialog.onConfigureCancel();
                    }
                }
        );

        return new WindowContentBuilder(content, options).build();
*/
    }

    void reset() {
/*
        configureStatus.setVisible(false);
        form.clearValues();
        portItem.setValue(9990);
        Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
            @Override
            public void execute() {
                nameItem.getInputElement().focus();
            }
        });
*/
    }

    private void status(SafeHtml message) {
//        configureStatus.setVisible(true);
//        configureStatus.setHTML(message);
    }
}
