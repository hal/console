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
package org.jboss.hal.resources;

import com.google.gwt.safehtml.shared.SafeHtml;

public interface Messages extends com.google.gwt.i18n.client.Messages {

    //@formatter:off
    SafeHtml accessMechanismLabel(String name);
    String activeRoles(String roles);
    SafeHtml addHaPolicy();
    SafeHtml addKeyStoreError(String name);
    SafeHtml addResourceError(String name, String cause);
    SafeHtml addResourceSuccess(String type, String name);
    String addResourceTitle(String type);
    SafeHtml addressLabel(String address);
    SafeHtml addServerHostHelp();
    String addServerTitle();
    SafeHtml addSingleResourceSuccess(String type);
    SafeHtml allContentAlreadyDeployedToServerGroup(String serverGroup);
    SafeHtml allMailServersExist();
    SafeHtml allSingletonsExist();
    SafeHtml allThreadPoolsExist();
    String alternativesHelp(String alternatives);
    SafeHtml assignmentExcludeSuccess(String type, String name);
    SafeHtml assignmentIncludeSuccess(String type, String name);
    String atLeastOneIsRequired(String attributes);
    String available(double value);

    String bootErrors();
    String blacklist(String blacklist);

    String cacheStore();
    SafeHtml callerThreadLabel(String name);
    SafeHtml cancelActiveOperation(String operation);
    SafeHtml cancelledLabel(boolean name);
    SafeHtml cancelledOperation(String name);
    SafeHtml changePrioritySuccess(int priority);
    SafeHtml chooseContentToDeploy(String serverGroup);
    SafeHtml chooseReplication();
    SafeHtml chooseServerGroupsToUndeploy(String name);
    SafeHtml chooseServerGroupsToDeploy(String name);
    SafeHtml chooseSharedStore();
    SafeHtml chooseStrategy();
    String chooseTemplate(String custom);
    String cleanPatchHistory();
    SafeHtml cleanPatchHistoryQuestion(String prependMessage);
    SafeHtml cleanPatchHistorySuccess();
    SafeHtml cleanPatchHistoryFailure(String error);
    SafeHtml cloneProfileSuccess(String from, String to);
    SafeHtml closeToLogout();
    String configurationChangesDescription();
    String configurePatchTitle();
    SafeHtml configuredMailServer(String servers);
    String connectedTo(String url);
    SafeHtml contentAdded(@PluralCount int size);
    SafeHtml contentAlreadyDeployedToAllServerGroups(String name);
    SafeHtml contentDeployed1(String content);
    SafeHtml contentDeployed2(String serverGroup);
    String contentFilterDescription();
    SafeHtml contentOpFailed(@PluralCount int size);
    SafeHtml contentReplaceError(String name);
    SafeHtml contentReplaceSuccess(String name);
    SafeHtml contentReplaced(@PluralCount int size);
    SafeHtml contentUndeployed(String name);
    SafeHtml contentUndeployedFromServerGroup(String name, String serverGroup);
    String copyServerTitle();
    SafeHtml credentialReferenceAddConfirmation(String alternative);
    SafeHtml credentialReferenceAddressError();
    String credentialReferenceConflict();
    String credentialReferenceParentNoResource(String parentResource);
    String credentialReferenceValidationError(String alternative);
    String currentOfTotal(long current, long total);
    SafeHtml customListItemHint(String propAttribute, String valueAttribute);

    SafeHtml dataSourceAddError();
    SafeHtml dataSourceDisabled(String name);
    SafeHtml dataSourceDisabledNoStatistics(String name);
    SafeHtml dataSourceEnabled(String name);
    String datasourceFilterDescription();
    String datasourceRuntimeFilterDescription();
    SafeHtml dataSourceStatisticsDisabled(String name);
    SafeHtml dataSourceStatisticsFromDeployment();
    SafeHtml deployedTo(String name);
    SafeHtml deploymentActive(String name);
    SafeHtml deploymentAdded(@PluralCount int count);
    SafeHtml deploymentDisabled(String name);
    SafeHtml deploymentDisabledError(String name);
    SafeHtml deploymentDisabledSuccess(String name);
    String deploymentEmptyCreate();
    SafeHtml deploymentEmptySuccess(String name);
    SafeHtml deploymentEnabled(String name);
    SafeHtml deploymentEnabledError(String name);
    SafeHtml deploymentEnabledSuccess(String name);
    SafeHtml deploymentError(String name);
    SafeHtml deploymentExploded(String name);
    SafeHtml deploymentFailed(String name);
    SafeHtml deploymentInProgress(String name);
    SafeHtml deploymentNotEnabled(String name);
    SafeHtml deploymentOpFailed(@PluralCount int count);
    SafeHtml deploymentPreview();
    SafeHtml deploymentReadError(String deployment);
    SafeHtml deploymentReplaced(@PluralCount int count);
    String deploymentStandaloneColumnFilterDescription();
    SafeHtml deploymentStopped(String name);
    SafeHtml deploymentSuccessful(String name);
    SafeHtml deploymentUnknownState(String name);
    String deprecated(String since, String reason);
    String destinationFilterDescription();
    SafeHtml disableSSLManagementQuestion(String serverName);
    SafeHtml disableSSLManagementError(String cause);
    SafeHtml disableSSLManagementSuccess();
    SafeHtml domainConfigurationChanged();
    SafeHtml domainControllerTimeout(String name);
    String domainUuidLabel(String uuid);
    SafeHtml domainRolloutLabel(boolean name);
    SafeHtml dropSubscriptionsQuestion(String topic);
    SafeHtml dropSubscriptionsSuccess(String topic);

    SafeHtml duplicateAuthenticationModule();
    String duplicateAuthenticationModuleReason();
    String duplicateResource(String type);

    String ejbFilterDescription();
    SafeHtml emptyModelNodeForm();
    String enableSSLConfirmationDescription();
    SafeHtml enableSSLDescription();
    SafeHtml enableSSLMutualQuestion();
    SafeHtml enableSSLResultsError();
    SafeHtml enableSSLResultsSuccessDomain(String url);
    SafeHtml enableSSLResultsSuccessStandalone(String url);
    SafeHtml enableSSLStrategyQuestion();
    SafeHtml enableSSLStrategyQuestionCreateAll();
    SafeHtml enableSSLStrategyQuestionCreateKeyStore();
    SafeHtml enableSSLStrategyQuestionReuseKeyStore();
    SafeHtml enableSSLSuccess();
    String endpointColumnFilterDescription();
    SafeHtml endpointError(String interfce, String url);
    SafeHtml endpointOk(String url);
    String exactlyOneAlternativeError(String alternatives);
    String exactlyOneAlternativesError(String alternatives);
    SafeHtml exclusiveRunningTimeLabel(String name);
    SafeHtml executionStatusLabel(String status, String description);
    SafeHtml expireMessageQuestion();
    SafeHtml expireMessagesQuestion();
    SafeHtml expireMessageSuccess();
    SafeHtml explodedPreview();
    SafeHtml expressionError(String expression);
    SafeHtml expressionWarning(String expression);
    String extensionColumnFilterDescription();
    SafeHtml extensionError(int status);
    SafeHtml extensionNetworkError(String console, String extension);
    SafeHtml extensionNoJson();
    SafeHtml extensionOk();
    SafeHtml extensionProcessing();
    SafeHtml extensionNotFound();
    SafeHtml extensionScriptError();
    SafeHtml extensionUrl();

    SafeHtml failedRedirectConsole(String url, String message);
    String filterBy(String name);
    SafeHtml flushConnectionSuccess();

    String goTo(String name);

    String homepagePatchingSubHeader(String name);
    String homepagePatchingStandaloneStepIntro(String name);
    String homepagePatchingDomainStepIntro(String name);
    String hostScopedRole(String name, String scope);

    SafeHtml hostAdminMode(String name);
    String hostColumnFilterDescription();
    SafeHtml hostControllerTimeout(String name);
    SafeHtml hostDisconnected(String name);
    SafeHtml hostNameChanged();
    SafeHtml hostNeedsReload(String name);
    SafeHtml hostNeedsRestart(String name);
    String hostPatchesColumnFilterDescription();
    SafeHtml hostPending(String name);
    SafeHtml hostRunning(String name);
    SafeHtml hostStarting(String name);
    SafeHtml hostUndefined(String name);

    SafeHtml includeAllHelpText();

    SafeHtml invalidExtensionJson();
    SafeHtml invalidExtensionMetadata(String extensionDocumentation);
    String invalidFormat();
    String invalidJNDIName();
    String invalidRange(long value, long min, long max);
    String invalidateSessionTitle();
    SafeHtml invalidateSessionQuestion();
    SafeHtml invalidateSessionSuccess(String sessionId);
    SafeHtml invalidateSessionError(String sessionId, String cause);
    SafeHtml invalidateSessionNotExist(String sessionId);

    String jdbcDriverColumnFilterDescription();
    SafeHtml jdbcDriverDeploymentHint();
    SafeHtml jdbcDriverProvidedBy(String type, String value);
    String jobExecutionColumnFilterDescription();
    String jpaColumnFilterDescription();
    SafeHtml jpaStatisticsDisabled(String name, String deployment);

    String kill(String name);

    SafeHtml killServerError(String name);

    SafeHtml killServerQuestion(String name);

    SafeHtml killServerSuccess(String name);

    SafeHtml largeLogFile(String size);
    SafeHtml lastOperationException();
    SafeHtml lastOperationFailed();
    SafeHtml listHint();
    SafeHtml loadContentError();
    SafeHtml logFileError(String name);
    String logfileColumnFilterDescription();
    String logFilePreview(int lines);
    String logFileFullStatus(int lines, String lastUpdate);
    String logFilePartStatus(int lines, String lastUpdate);

    SafeHtml macroPlaybackError();
    SafeHtml macroPlaybackSuccessful();
    String mailColumnFilterDescription();
    SafeHtml managementOperationsFindNoResult();

    SafeHtml managementVersionMismatch(String managementVersion, String targetVersion);
    SafeHtml manyMessages(long number);
    SafeHtml mappingHint();
    String membershipColumnFilterDescription();
    SafeHtml metadataError();
    String modifyResourceTitle(String type);
    SafeHtml modifyResourceSuccess(String type, String name);
    SafeHtml modifySingleResourceSuccess(String type);
    SafeHtml moreThanOneCacheStore();
    String moreThanOneCacheStoreDetails();
    SafeHtml moreThanOneKeyMapperForPrincipalQuery();
    SafeHtml moveMessageSuccess(String queue);
    SafeHtml multiValueListHint();

    SafeHtml newContentSuccess(String deployment, String file);
    String noBootErrors();
    SafeHtml noChanges();
    SafeHtml noContent();
    SafeHtml noContentSelected();
    SafeHtml noContentSelectedInDeployment();
    SafeHtml noContextForNormalMode();
    SafeHtml noDeployment();
    SafeHtml noDeploymentsUploaded();
    SafeHtml noExecutions();
    SafeHtml noItems();
    SafeHtml noLogFile();
    SafeHtml noMatchingItems();
    SafeHtml noMessagesSelected();
    SafeHtml noMacrosDescription(String startMacro);
    String nonProgressingOperation();
    SafeHtml noPolicy();
    String nonEmptyRequires(String fields);
    String noPatchesForHost();
    SafeHtml noReferenceServerPreview(String deployment, String attribute1, String attribute2, String serverGroup, String historyToken);
    SafeHtml noReferenceServerEmptyState(String deployment, String serverGroup);
    SafeHtml noReset();
    SafeHtml noResource();
    SafeHtml noSecuritySettingSelected();
    SafeHtml noSelectedPatch();
    SafeHtml noServerGroupSelected();
    SafeHtml noStore();
    SafeHtml noTransport();
    String notMoreThanOneAlternativeError(String alternatives);
    String notMoreThanOneAlternativesError(String alternatives);
    SafeHtml noWrite();
    SafeHtml normalLogFile(String size);
    String notifications(@PluralCount int count);

    SafeHtml operationLabel(String name);
    String operations(int size);

    SafeHtml pageNotFound(String invalidHistoryToken);
    String patchLatestInstalledLabel();
    SafeHtml patchHostNeedsRestart(String hostname);
    SafeHtml patchInProgress(String patch);
    SafeHtml patchRestartDomainControllerQuestion(String hostname);
    SafeHtml patchRestartHostControllerQuestion(String hostname);
    SafeHtml patchRestartStandaloneQuestion();
    SafeHtml patchSucessfullyApplied(String patchId);
    String patchStopAllServersTitle();
    SafeHtml patchStopAllServersQuestion(String servers, String host);
    SafeHtml patchStopServersDialogMessage1();
    SafeHtml patchStopServersDialogMessage2();
    SafeHtml patchAddError(String patchId, String error);
    SafeHtml rollbackSucessful(String patchId);
    SafeHtml pauseQueueSuccess(String name);
    SafeHtml rollbackError(String failure);
    SafeHtml rollbackInProgress(String patchid);
    String profileIncludes(String includes);
    SafeHtml profileNotUsedInServerGroups();
    SafeHtml profileUsedInServerGroups(SafeHtml serverGroupLinks);
    SafeHtml propertiesHint();
    SafeHtml pruneDisconnectedQuestion();
    SafeHtml pruneExpiredQuestion();
    SafeHtml pruneSuccessful();

    String recordedOperations(@PluralCount int count);
    SafeHtml recordingStarted();
    SafeHtml recordingStopped();

    String referenceServer(String server);
    String reload(String name);

    SafeHtml reloadConsoleRedirect(String url);
    SafeHtml reloadConsoleTimeout(String type, String url);
    SafeHtml reloadDomainControllerPending(String name);
    SafeHtml reloadDomainControllerQuestion(String name);
    SafeHtml reloadHostControllerQuestion(String name);
    SafeHtml reloadHostError(String name);
    SafeHtml reloadErrorCause(String type, String name, String failure);
    SafeHtml reloadHostSuccess(String name);
    SafeHtml reloadServerGroupError(String name);
    SafeHtml reloadServerGroupQuestion(String name);
    SafeHtml reloadServerGroupSuccess(String name);
    SafeHtml reloadServerError(String name);
    SafeHtml reloadServerQuestion(String name);
    SafeHtml reloadServerSuccess(String name);
    SafeHtml reloadSettings();

    String removeConfirmationTitle(String name);
    SafeHtml removeConfirmationQuestion(String name);
    SafeHtml removeConfigurationChangesQuestion(String type, String name);
    SafeHtml removeContentQuestion(String deployment, String path);
    SafeHtml removeContentSuccess(String deployment, String path);
    SafeHtml removeCurrentUserError();
    SafeHtml removeExtensionQuestion();
    SafeHtml removeExtensionSuccess();
    SafeHtml removeGroupQuestion(String name);
    SafeHtml removeGroupSuccess(String name);
    SafeHtml removeMessageQuestion();
    SafeHtml removeMessagesQuestion();
    SafeHtml removeMessageSuccess();
    SafeHtml removeResourceSuccess(String type, String name);
    SafeHtml removeRoleQuestion(String name);
    SafeHtml removeRunAsRoleError(String role);
    SafeHtml removeSingletonConfirmationQuestion();
    SafeHtml removeSingletonResourceSuccess(String type);
    SafeHtml removeSingletonSuccess(String type);
    SafeHtml removeUserQuestion(String name);
    SafeHtml removeUserSuccess(String name);
    SafeHtml replicationColocated();
    SafeHtml replicationLiveOnly();
    SafeHtml replicationMaster();
    SafeHtml replicationSlave();
    SafeHtml replicationStrategy();
    SafeHtml requiredHelp();
    SafeHtml requiredMarker();
    String requires(String requires);
    String resetConfirmationTitle(String type);
    SafeHtml resetConfirmationQuestion(String name);
    String resetStatisticsTitle();
    SafeHtml resetStatisticsQuestion(String connector);
    SafeHtml  resetStatisticsSuccess(String connector);
    SafeHtml resetResourceSuccess(String type, String name);
    SafeHtml resetSingletonConfirmationQuestion();
    SafeHtml resetSingletonSuccess(String type);
    SafeHtml resourceDisabled(String type, String name);
    SafeHtml resourceEnabled(String type, String name);
    String resourceAdapterColumnFilterDescription();
    SafeHtml resourceAdapterProvidedBy(String type, String name);
    String restColumnFilterDescription();

    String results(int number);

    String restart(String name);
    SafeHtml restartDomainControllerPending(String name);
    SafeHtml restartDomainControllerQuestion(String name);
    SafeHtml restartExecutionSuccess(int id);
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

    SafeHtml resumeQueueSuccess(String name);
    SafeHtml resumeServerGroupError(String name);
    SafeHtml resumeServerGroupSuccess(String name);
    SafeHtml resumeServerError(String name);
    SafeHtml resumeServerSuccess(String name);
    String roleColumnFilterDescription();
    SafeHtml runningTimeLabel(String name);

    SafeHtml saveContentSuccess(String deployment, String file);
    String securityDomainColumnFilterDescription();
    SafeHtml selected(int selected, int total);
    SafeHtml sendMessageToDeadLetterQuestion();
    SafeHtml sendMessagesToDeadLetterQuestion();
    SafeHtml sendMessageToDeadLetterSuccess();

    SafeHtml serverGroupNoStartedServers(String name);
    SafeHtml serverGroupNoStoppedServers(String name);
    SafeHtml serverGroupNoSuspendedServers(String name);
    String serverGroupScopedRole(String name, String scope);
    SafeHtml serverGroupTimeout(String name);

    SafeHtml serverAdminMode(String name);
    SafeHtml serverBootErrors(String name);
    SafeHtml serverBootErrorsAndLink(String name, String link);
    SafeHtml serverConfigurationChanged();
    SafeHtml serverFailed(String name);
    String serverFilterDescription();
    String serverGroupColumnFilterDescription();
    SafeHtml serverNeedsReload(String name);
    SafeHtml serverNeedsRestart(String name);
    SafeHtml serverRunning(String name);
    SafeHtml serverPending(String name);
    SafeHtml serverStopped(String name);
    SafeHtml serverSuspended(String name);
    SafeHtml serverTimeout(String name);
    SafeHtml serverUndefined(String name);
    SafeHtml serverUrlCustom();
    SafeHtml serverUrlError();
    SafeHtml serverUrlManagementModel();

    SafeHtml sharedStoreColocated();
    SafeHtml sharedStoreMaster();
    SafeHtml sharedStoreSlave();
    SafeHtml sharedStoreStrategy();
    SafeHtml simpleProviderWarning();
    SafeHtml specifyParameters(String link);
    SafeHtml staleStatistics();

    SafeHtml startDeliverySuccess(String name);
    SafeHtml startJobSuccess(String job, long id);
    SafeHtml startServerError(String name);
    SafeHtml startServerGroupError(String name);
    SafeHtml startServerGroupSuccess(String name);
    SafeHtml startServerSuccess(String name);

    SafeHtml statisticsDisabled(String subsystem);
    SafeHtml statisticsEnabled(String name);

    String stop(String name);
    SafeHtml stopDeliverySuccess(String name);
    SafeHtml stopExecutionSuccess(int id);
    SafeHtml stopServerGroupError(String name);
    SafeHtml stopServerGroupQuestion(String name);
    SafeHtml stopServerGroupSuccess(String name);
    SafeHtml stopServerError(String name);
    SafeHtml stopServerQuestion(String name);
    SafeHtml stopServerSuccess(String name);

    String susbsystemFilterDescription();
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

    SafeHtml testConnectionCancelError(String datasource);
    String testConnectionErrorDomain();
    SafeHtml testConnectionDomain(String testConnection);
    SafeHtml testConnectionError(String datasource);
    SafeHtml testConnectionStandalone(String testConnection);
    SafeHtml testConnectionSuccess(String datasource);
    SafeHtml topologyError();
    SafeHtml transactionSetUuidOrSocket();
    SafeHtml transactionUnableSetProcessId();

    SafeHtml unauthorized();
    SafeHtml undeployedContent(String name);

    String undertowListenerProcessingDisabled(String listener, String server);
    String updateAvailable(String current, String update);
    SafeHtml updateServerError(String name);
    SafeHtml uploadContentDescription();
    String uploadContentInvalid();
    SafeHtml uploadError(String name);
    SafeHtml uploadInProgress(String name);
    SafeHtml uploadSuccessful(String name);
    String unit(String unit);
    SafeHtml unknownError();
    SafeHtml unknownResource();
    String unknownResourceDetails(String address, String reason);
    SafeHtml unsupportedFileTypeDescription();
    String uptime(String uptime);
    String used(double value);

    String view(String type);

    SafeHtml writeBehaviour(String current, String switchTo);
    //@formatter:on
}
