package org.jboss.hal.client.runtime.managementinterface;

import java.util.Map;

import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.Metadata;

/** Not a real presenter, but common methods for {@code HostPresenter} and {@code StandaloneServerPresenter} */
public interface HttpManagementInterfacePresenter {

    void saveManagementInterface(AddressTemplate template, Map<String, Object> changedValues);

    void resetManagementInterface(AddressTemplate template, Form<ModelNode> form, Metadata metadata);

    void enableSslForManagementInterface();

    void disableSslForManagementInterface();
}
