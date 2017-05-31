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
package org.jboss.hal.client.configuration.subsystem.mail;

import com.google.common.base.Joiner;
import org.jboss.hal.core.finder.PreviewAttributes;
import org.jboss.hal.core.finder.PreviewAttributes.PreviewAttribute;
import org.jboss.hal.core.finder.PreviewContent;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;

import static org.jboss.hal.dmr.ModelDescriptionConstants.IMAP;
import static org.jboss.hal.dmr.ModelDescriptionConstants.JNDI_NAME;
import static org.jboss.hal.dmr.ModelDescriptionConstants.POP3;
import static org.jboss.hal.dmr.ModelDescriptionConstants.SMTP;

/**
 * @author Claudio Miranda
 */
class MailSessionPreview extends PreviewContent<MailSession> {

    MailSessionPreview(final MailSession mailSession, final Resources resources) {
        super(mailSession.getName(), mailSession.getServers().isEmpty()
                ? resources.constants().noConfiguredMailServers()
                : resources.messages().configuredMailServer(Joiner.on(", ").join(mailSession.getServers())).asString());

        PreviewAttributes<MailSession> attributes = new PreviewAttributes<>(mailSession);
        attributes.append(JNDI_NAME);
        if (mailSession.hasServer(SMTP)) {
            attributes.append(model -> new PreviewAttribute(
                    SMTP.toUpperCase() + " " + Names.SOCKET_BINDING,
                    model.getServerSocketBinding(SMTP)));
        }
        if (mailSession.hasServer(IMAP)) {
            attributes.append(model -> new PreviewAttribute(
                    IMAP.toUpperCase() + " " + Names.SOCKET_BINDING,
                    model.getServerSocketBinding(IMAP)));
        }
        if (mailSession.hasServer(POP3)) {
            attributes.append(model -> new PreviewAttribute(
                    POP3.toUpperCase() + " " + Names.SOCKET_BINDING,
                    model.getServerSocketBinding(POP3)));
        }
        previewBuilder().addAll(attributes);
    }
}
