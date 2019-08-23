/*
 * Copyright 2015-2016 Red Hat, Inc, and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.hal.client.configuration.subsystem.mail;

import org.jboss.hal.meta.AddressTemplate;

import static org.jboss.hal.meta.SelectionAwareStatementContext.SELECTION_EXPRESSION;
import static org.jboss.hal.meta.StatementContext.Expression.SELECTED_PROFILE;

interface AddressTemplates {

    String MAIL_ADDRESS = "/{selected.profile}/subsystem=mail";
    AddressTemplate MAIL_TEMPLATE = AddressTemplate.of(MAIL_ADDRESS);

    String MAIL_SESSION_ADDRESS = "/{selected.profile}/subsystem=mail/mail-session=*";
    AddressTemplate MAIL_SESSION_TEMPLATE = AddressTemplate.of(MAIL_SESSION_ADDRESS);

    // The server address is set to smtp, because the address is a singleton, so it does not load the wildcard.
    // The attributes for imap, smtp and pop3 are the same
    String SERVER_ADDRESS = "/{selected.profile}/subsystem=mail/mail-session=*/server=smtp";
    AddressTemplate SERVER_TEMPLATE = AddressTemplate.of(SERVER_ADDRESS);

    AddressTemplate SELECTED_MAIL_SESSION_TEMPLATE = AddressTemplate
            .of(SELECTED_PROFILE, "subsystem=mail/mail-session=" + SELECTION_EXPRESSION);

    AddressTemplate SOCKET_BINDING_TEMPLATE = AddressTemplate
            .of("/socket-binding-group=*/remote-destination-outbound-socket-binding=*");
}
