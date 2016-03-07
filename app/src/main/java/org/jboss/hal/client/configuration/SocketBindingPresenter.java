package org.jboss.hal.client.configuration;

import org.jboss.hal.meta.AddressTemplate;

/**
 * @author Harald Pehl
 */
public class SocketBindingPresenter {

    static final String ROOT_ADDRESS = "/socket-binding-group=*";
    static final AddressTemplate ROOT_TEMPLATE = AddressTemplate.of(ROOT_ADDRESS);
}
