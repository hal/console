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

/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @author tags. All rights reserved.
 * See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU Lesser General Public License, v. 2.1.
 * This program is distributed in the hope that it will be useful, but WITHOUT A
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License,
 * v.2.1 along with this distribution; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA  02110-1301, USA.
 */
package org.jboss.hal.resources;


import com.google.gwt.safehtml.shared.SafeHtml;

public interface Messages extends com.google.gwt.i18n.client.Messages {

    //@formatter:off
    String activeRoles(String roles);
    String addResourceSuccess(String type, String name);
    String addResourceTitle(String text);
    String addSingleResourceSuccess(String type);

    String blacklist(String blacklist);

    SafeHtml configuredMailServer(String servers);
    String connectedTo(String url);

    String databaseDisabled(String name);
    String databaseEnabled(String name);
    String deploymentAdded(@PluralCount int count);
    String deploymentDisabled(String name);
    String deploymentEnabled(String name);
    String deploymentEnabledError(String name);
    String deploymentFailed(@PluralCount int count);
    String deploymentReplaced(@PluralCount int count);
    String duplicateResource(String type);

    SafeHtml emptyModelNodeForm();
    SafeHtml endpointError(String url);
    SafeHtml endpointOk(String url);

    String homepagePatchingSubHeader(String name);
    String homepagePatchingStandaloneStepIntro(String name);
    String homepagePatchingDomainStepIntro(String name);

    SafeHtml jdbcDriverDeploymentHint();
    SafeHtml jdbcDriverProvidedByPreview(String type, String value);

    String invalidRange(long value, long min, long max);

    SafeHtml listHint();

    String messages(@PluralCount int count);
    String modifyResourceSuccess(String type, String name);
    String modifySingleResourceSuccess(String type);

    SafeHtml noMacrosDescription(String startMacro);

    String pageNotFound(String invalidHistoryToken);
    String patternMismatch(String pattern);
    SafeHtml propertiesHint();

    String recordedOperations(@PluralCount int count);
    String removeResourceConfirmationTitle(String name);
    SafeHtml removeResourceConfirmationQuestion(String name);
    String removeResourceSuccess(String type, String name);
    SafeHtml requiredHelp();
    SafeHtml requiredMarker();
    String resourceNotFound(String type, String name);
    SafeHtml resourceDisabled(String type, String name);
    SafeHtml resourceEnabled(String type, String name);

    String updateAvailable(String current, String update);
    String unit(String unit);
    String unknownResource(String address, String reason);
    //@formatter:on
}
