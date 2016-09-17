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
    String addResourceTitle(String type);
    SafeHtml addSingleResourceSuccess(String type);
    SafeHtml allMailServersExist();
    SafeHtml allSingletonsExist();
    SafeHtml assignByDragAndDrop(String replace);
    SafeHtml assignContentDescription(String name);
    String assignedDeployments(@PluralCount int count);
    SafeHtml assignedTo(String name);
    SafeHtml assignmentExcludeSuccess(String type, String name);
    SafeHtml assignmentIncludeSuccess(String type, String name);
    String available(double value);

    String blacklist(String blacklist);

    SafeHtml configuredMailServer(String servers);
    String connectedTo(String url);
    SafeHtml contentAdded(@PluralCount int size);
    SafeHtml contentAlreadyAssignedToAllServerGroups(String name);
    SafeHtml contentAssigned(String name);
    SafeHtml contentOpFailed(@PluralCount int size);
    SafeHtml contentReplaced(@PluralCount int size);
    SafeHtml contentUnassigned(String name);
    String currentOfTottal(long current, long total);

    SafeHtml dataSourceAddError();
    SafeHtml dataSourceDisabled(String name);
    SafeHtml dataSourceDisabledNoStatistics(String name);
    SafeHtml dataSourceEnabled(String name);
    SafeHtml dataSourceStatisticsDisabled(String name);
    SafeHtml deploymentAdded(@PluralCount int count);
    SafeHtml deploymentDisabled(String name);
    SafeHtml deploymentDisabledSuccess(String name);
    SafeHtml deploymentEnabled(String name);
    SafeHtml deploymentEnabledError(String name);
    SafeHtml deploymentEnabledSuccess(String name);
    SafeHtml deploymentExploded(String name);
    SafeHtml deploymentFailed(String name);
    SafeHtml deploymentOpFailed(@PluralCount int count);
    SafeHtml deploymentReplaced(@PluralCount int count);
    SafeHtml deploymentUnassigned(String name, String serverGroup);
    SafeHtml deploymentsDescription(String serverGroupName, @PluralCount int deployments);
    SafeHtml domainConfigurationChanged();
    SafeHtml domainControllerTimeout(String name);
    String duplicateResource(String type);

    SafeHtml emptyModelNodeForm();
    SafeHtml endpointError(String url);
    SafeHtml endpointOk(String url);

    SafeHtml flushConnectionSuccess();

    String homepagePatchingSubHeader(String name);
    String homepagePatchingStandaloneStepIntro(String name);
    String homepagePatchingDomainStepIntro(String name);
    String hostScopedRole(String name, String scope);

    SafeHtml hostAdminMode(String name);
    SafeHtml hostControllerTimeout(String name);
    SafeHtml hostNeedsReload(String name);
    SafeHtml hostNeedsRestart(String name);
    SafeHtml hostPending(String name);
    SafeHtml hostRunning(String name);
    SafeHtml hostStarting(String name);
    SafeHtml hostUndefined(String name);

    SafeHtml includeAllHelpText();

    SafeHtml jdbcDriverDeploymentHint();
    SafeHtml jdbcDriverProvidedByPreview(String type, String value);

    SafeHtml jpaStatisticsDisabled(String name, String deployment);

    String invalidRange(long value, long min, long max);

    SafeHtml largeLogFile(String size);
    SafeHtml lastOperationException();
    SafeHtml lastOperationFailed();
    SafeHtml listHint();
    SafeHtml logFileError(String name);
    String logFileFullStatus(int lines, String lastUpdate);
    String logFilePartStatus(int lines, String lastUpdate);

    SafeHtml macroPlaybackError();
    SafeHtml macroPlaybackSuccessful();
    String messages(@PluralCount int count);
    SafeHtml metadataError();
    String modifyResourceTitle(String type);
    SafeHtml modifyResourceSuccess(String type, String name);
    SafeHtml modifySingleResourceSuccess(String type);

    SafeHtml noContent();
    SafeHtml noDeploymentsUploaded();
    SafeHtml noLogFile();
    SafeHtml noMacrosDescription(String startMacro);
    SafeHtml noReferenceServer(String deployment, String attribute1, String attribute2, String serverGroup, String historyToken);
    SafeHtml noServerGroupSelected();
    SafeHtml normalLogFile(String size);

    SafeHtml pageNotFound(String invalidHistoryToken);
    String patternMismatch(String pattern);
    SafeHtml propertiesHint();

    String recordedOperations(@PluralCount int count);
    SafeHtml recordingStarted();
    SafeHtml recordingStopped();

    String reload(String name);
    SafeHtml reloadDomainControllerPending(String name);
    SafeHtml reloadDomainControllerQuestion(String name);
    SafeHtml reloadHostControllerQuestion(String name);
    SafeHtml reloadHostError(String name);
    SafeHtml reloadHostSuccess(String name);
    SafeHtml reloadServerGroupError(String name);
    SafeHtml reloadServerGroupQuestion(String name);
    SafeHtml reloadServerGroupSuccess(String name);
    SafeHtml reloadServerError(String name);
    SafeHtml reloadServerQuestion(String name);
    SafeHtml reloadServerSuccess(String name);

    SafeHtml removeGroupQuestion(String name);
    SafeHtml removeGroupSuccess(String name);
    String removeResourceConfirmationTitle(String name);
    SafeHtml removeResourceConfirmationQuestion(String name);
    SafeHtml removeResourceSuccess(String type, String name);
    SafeHtml removeRoleQuestion(String name);
    SafeHtml removeUserQuestion(String name);
    SafeHtml removeUserSuccess(String name);
    SafeHtml requiredHelp();
    SafeHtml requiredMarker();
    SafeHtml resourceNotFound(String type, String name);
    SafeHtml resourceDisabled(String type, String name);
    SafeHtml resourceEnabled(String type, String name);

    String restart(String name);
    SafeHtml restartDomainControllerPending(String name);
    SafeHtml restartDomainControllerQuestion(String name);
    SafeHtml restartHostControllerQuestion(String name);
    SafeHtml restartHostError(String name);
    SafeHtml restartHostSuccess(String name);
    SafeHtml restartServerGroupError(String name);
    SafeHtml restartServerGroupQuestion(String name);
    SafeHtml restartServerGroupSuccess(String name);
    SafeHtml restartServerError(String name);
    SafeHtml restartServerQuestion(String name);
    SafeHtml restartServerSuccess(String name);
    SafeHtml restartStandalonePending(String name);
    SafeHtml restartStandaloneQuestion(String name);
    SafeHtml restartStandaloneTimeout(String name);

    SafeHtml resumeServerGroupError(String name);
    SafeHtml resumeServerGroupSuccess(String name);
    SafeHtml resumeServerError(String name);
    SafeHtml resumeServerSuccess(String name);

    SafeHtml simpleProviderWarning();
    SafeHtml serverConfigurationChanged();
    String serverGroupScopedRole(String name, String scope);

    SafeHtml serverGroupNoStartedServers(String name);
    SafeHtml serverGroupNoStoppedServers(String name);
    SafeHtml serverGroupNoSuspendedServers(String name);
    SafeHtml serverGroupTimeout(String name);

    SafeHtml serverAdminMode(String name);
    SafeHtml serverFailed(String name);
    SafeHtml serverNeedsReload(String name);
    SafeHtml serverNeedsRestart(String name);
    SafeHtml serverRunning(String name);
    SafeHtml serverPending(String name);
    SafeHtml serverStarting(String name);
    SafeHtml serverStopped(String name);
    SafeHtml serverSuspended(String name);
    SafeHtml serverTimeout(String name);
    SafeHtml serverUndefined(String name);

    SafeHtml staleStatistics();

    SafeHtml startServerGroupError(String name);
    SafeHtml startServerGroupSuccess(String name);
    SafeHtml startServerError(String name);
    SafeHtml startServerSuccess(String name);

    SafeHtml statisticsEnabled(String name);

    String stop(String name);
    SafeHtml stopServerGroupError(String name);
    SafeHtml stopServerGroupQuestion(String name);
    SafeHtml stopServerGroupSuccess(String name);
    SafeHtml stopServerError(String name);
    SafeHtml stopServerQuestion(String name);
    SafeHtml stopServerSuccess(String name);

    String suspend(String name);
    SafeHtml suspendServerGroupError(String name);
    SafeHtml suspendServerGroupQuestion(String name);
    SafeHtml suspendServerGroupSuccess(String name);
    SafeHtml suspendServerError(String name);
    SafeHtml suspendServerQuestion(String name);
    SafeHtml suspendServerSuccess(String name);

    SafeHtml switchProviderSuccess();
    SafeHtml switchToRbacProvider();
    SafeHtml switchToSimpleProvider();

    SafeHtml testConnectionError();
    SafeHtml testConnectionSuccess();
    SafeHtml topologyError();
    SafeHtml transactionSetUuidOrSocket();
    SafeHtml transactionUnableSetProcessId();

    SafeHtml unassignContentDescription(String name);
    SafeHtml unassignedContent(String name);
    String updateAvailable(String current, String update);
    SafeHtml updateServerError(String name);
    SafeHtml uploadError(String name);
    SafeHtml uploadInProgress(String name);
    SafeHtml uploadSuccessful(String name);
    String unit(String unit);
    SafeHtml unknownError();
    SafeHtml unknownResource();
    String unknownResourceDetails(String address, String reason);
    String uptime(String uptime);
    String used(double value);

    String view(String type);
    //@formatter:on
}
