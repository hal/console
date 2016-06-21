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
    SafeHtml addResourceSuccess(String type, String name);
    String addResourceTitle(String text);
    SafeHtml addSingleResourceSuccess(String type);
    SafeHtml allMailServersExist();
    SafeHtml allSingletonsExist();

    String blacklist(String blacklist);

    SafeHtml configuredMailServer(String servers);
    String connectedTo(String url);

    SafeHtml databaseDisabled(String name);
    SafeHtml databaseEnabled(String name);
    SafeHtml deploymentAdded(@PluralCount int count);
    SafeHtml deploymentDisabled(String name);
    SafeHtml deploymentEnabled(String name);
    SafeHtml deploymentEnabledError(String name);
    SafeHtml deploymentFailed(@PluralCount int count);
    SafeHtml deploymentReplaced(@PluralCount int count);
    SafeHtml dispatcherException();
    SafeHtml dispatcherFailed();
    SafeHtml domainControllerTimeout(String name);
    String duplicateResource(String type);

    SafeHtml emptyModelNodeForm();
    SafeHtml endpointError(String url);
    SafeHtml endpointOk(String url);

    String homepagePatchingSubHeader(String name);
    String homepagePatchingStandaloneStepIntro(String name);
    String homepagePatchingDomainStepIntro(String name);
    SafeHtml hostAdminMode(String name);
    SafeHtml hostControllerTimeout(String name);
    SafeHtml hostNeedsReload(String name);
    SafeHtml hostNeedsRestart(String name);
    SafeHtml hostRunning(String name);
    SafeHtml hostStarting(String name);
    SafeHtml hostUndefined(String name);

    SafeHtml jdbcDriverDeploymentHint();
    SafeHtml jdbcDriverProvidedByPreview(String type, String value);

    String invalidRange(long value, long min, long max);

    SafeHtml listHint();

    SafeHtml macroPlaybackError();
    SafeHtml macroPlaybackSuccessful();
    String messages(@PluralCount int count);
    SafeHtml metadataError();
    SafeHtml modifyResourceSuccess(String type, String name);
    SafeHtml modifySingleResourceSuccess(String type);

    SafeHtml noDeploymentsUploaded();
    SafeHtml noMacrosDescription(String startMacro);

    SafeHtml pageNotFound(String invalidHistoryToken);
    String patternMismatch(String pattern);
    SafeHtml propertiesHint();

    String recordedOperations(@PluralCount int count);
    SafeHtml recordingStarted();
    SafeHtml recordingStopped();

    String reload(String name);
    SafeHtml reloadDomainControllerQuestion(String name);
    SafeHtml reloadHostControllerQuestion(String name);
    SafeHtml reloadHostPending(String name);
    SafeHtml reloadHostSuccess(String name);
    SafeHtml reloadServerGroupQuestion(String name);
    SafeHtml reloadServerGroupPending(String name);
    SafeHtml reloadServerGroupSuccess(String name);
    SafeHtml reloadServerQuestion(String name);
    SafeHtml reloadServerPending(String name);
    SafeHtml reloadServerSuccess(String name);

    String removeResourceConfirmationTitle(String name);
    SafeHtml removeResourceConfirmationQuestion(String name);
    SafeHtml removeResourceSuccess(String type, String name);
    SafeHtml requiredHelp();
    SafeHtml requiredMarker();
    SafeHtml resourceNotFound(String type, String name);
    SafeHtml resourceDisabled(String type, String name);
    SafeHtml resourceEnabled(String type, String name);

    SafeHtml resumeServerGroupPending(String name);
    SafeHtml resumeServerGroupSuccess(String name);
    SafeHtml resumeServerPending(String name);
    SafeHtml resumeServerSuccess(String name);

    String restart(String name);
    SafeHtml restartDomainControllerQuestion(String name);
    SafeHtml restartHostControllerQuestion(String name);
    SafeHtml restartHostPending(String name);
    SafeHtml restartHostSuccess(String name);
    SafeHtml restartServerGroupQuestion(String name);
    SafeHtml restartServerGroupPending(String name);
    SafeHtml restartServerGroupSuccess(String name);
    SafeHtml restartServerQuestion(String name);
    SafeHtml restartServerPending(String name);
    SafeHtml restartServerSuccess(String name);

    SafeHtml serverGroupTimeout(String name);
    SafeHtml serverAdminMode(String name);
    SafeHtml serverFailed(String name);
    SafeHtml serverNeedsReload(String name);
    SafeHtml serverNeedsRestart(String name);
    SafeHtml serverRunning(String name);
    SafeHtml serverStarting(String name);
    SafeHtml serverStopped(String name);
    SafeHtml serverSuspended(String name);
    SafeHtml serverTimeout(String name);
    SafeHtml serverUndefined(String name);

    SafeHtml startServerGroupPending(String name);
    SafeHtml startServerGroupSuccess(String name);
    SafeHtml startServerPending(String name);
    SafeHtml startServerSuccess(String name);

    String stop(String name);
    SafeHtml stopServerGroupQuestion(String name);
    SafeHtml stopServerGroupPending(String name);
    SafeHtml stopServerGroupSuccess(String name);
    SafeHtml stopServerQuestion(String name);
    SafeHtml stopServerPending(String name);
    SafeHtml stopServerSuccess(String name);

    String suspend(String name);
    SafeHtml suspendServerGroupQuestion(String name);
    SafeHtml suspendServerGroupPending(String name);
    SafeHtml suspendServerGroupSuccess(String name);
    SafeHtml suspendServerQuestion(String name);
    SafeHtml suspendServerPending(String name);
    SafeHtml suspendServerSuccess(String name);

    SafeHtml testConnectionError();
    SafeHtml testConnectionSuccess();
    SafeHtml topologyError();
    SafeHtml transactionSetUuidOrSocket();
    SafeHtml transactionUnableSetProcessId();
    SafeHtml txEnableJournalStore();

    String updateAvailable(String current, String update);
    String unit(String unit);
    SafeHtml unknownError();
    SafeHtml unknownResource();
    String unknownResourceDetails(String address, String reason);
    SafeHtml unknownState(String type, String name);
    //@formatter:on
}
