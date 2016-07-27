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
package org.jboss.hal.client.runtime.subsystem.logging;

import javax.inject.Inject;

import com.gwtplatform.mvp.shared.proxy.PlaceRequest;
import com.gwtplatform.mvp.shared.proxy.TokenFormatter;
import elemental.client.Browser;
import org.jboss.hal.config.Endpoints;
import org.jboss.hal.config.Environment;
import org.jboss.hal.dmr.Property;
import org.jboss.hal.dmr.model.ResourceAddress;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.meta.token.NameTokens;
import org.jboss.hal.resources.Ids;

import static java.util.Collections.singletonList;
import static org.jboss.hal.client.runtime.subsystem.logging.LogFilePresenter.EXTERNAL_PARAM;
import static org.jboss.hal.dmr.ModelDescriptionConstants.HOST;
import static org.jboss.hal.dmr.ModelDescriptionConstants.NAME;
import static org.jboss.hal.dmr.ModelDescriptionConstants.SERVER;

/**
 * Common code used by the finder column and the presenter.
 *
 * @author Harald Pehl
 */
public class LogFiles {

    /**
     * If log files are bigger than this threshold a warning is displayed.
     */
    public static final int LOG_FILE_SIZE_THRESHOLD = 15000000; // bytes

    /**
     * The number of lines in the log file viewer
     */
    public static final int LINES = 2000;

    private final Endpoints endpoints;
    private final Environment environment;
    private final StatementContext statementContext;
    private final TokenFormatter tokenFormatter;

    @Inject
    public LogFiles(final Endpoints endpoints,
            final Environment environment,
            final StatementContext statementContext,
            final TokenFormatter tokenFormatter) {
        this.endpoints = endpoints;
        this.environment = environment;
        this.statementContext = statementContext;
        this.tokenFormatter = tokenFormatter;
    }

    public void download(final String logFile) {
        Browser.getWindow().open(downloadUrl(logFile), "", "");
    }

    String downloadUrl(String name) {
        ResourceAddress address = AddressTemplates.LOG_FILE_TEMPLATE.resolve(statementContext, name);
        StringBuilder url = new StringBuilder();

        url.append(endpoints.dmr()).append("/");
        for (Property property : address.asPropertyList()) {
            url.append(property.getName()).append("/").append(property.getValue().asString()).append("/");
        }
        url.append("?operation=attribute&name=stream&useStreamAsResponse"); //NON-NLS
        return url.toString();
    }

    String externalUrl(String name) {
        PlaceRequest request = new PlaceRequest.Builder().nameToken(NameTokens.LOG_FILE)
                .with(HOST, statementContext.selectedHost())
                .with(SERVER, statementContext.selectedServer())
                .with(NAME, name)
                .with(EXTERNAL_PARAM, String.valueOf(true))
                .build();
        String href = Browser.getWindow().getLocation().getHref();
        href = href.substring(0, href.indexOf('#'));
        return href + "#" + tokenFormatter.toHistoryToken(singletonList(request));
    }

    public String target(String name) {
        return environment.isStandalone() ? Ids.asId(name) : Ids
                .build(statementContext.selectedHost(), statementContext.selectedServer(), Ids.asId(name));
    }
}
