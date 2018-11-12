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
    String accessControlSsoDescription();
    String activeRoles(String roles);
    SafeHtml addHaPolicy();
    SafeHtml addError(String type, String identity, String resource, String error);
    SafeHtml addSuccess(String type, String identity, String resource);
    SafeHtml addKeyStoreError(String name);
    SafeHtml addResourceError(String name, String cause);
    SafeHtml addResourceSuccess(String type, String name);
    String addResourceTitle(String type);
    SafeHtml addressLabel(String address);
    SafeHtml addServerHostHelp();
    String addServerTitle();
    SafeHtml addSingleResourceError(String type, String error);
    SafeHtml addSingleResourceSuccess(String type);
    SafeHtml allContentAlreadyDeployedToServerGroup(String serverGroup);
    SafeHtml allMailServersExist();
    SafeHtml allSingletonsExist();
    SafeHtml allThreadPoolsExist();

    SafeHtml alternativesHelp(String alternatives);
    SafeHtml assignmentExcludeSuccess(String type, String name);
    SafeHtml assignmentIncludeSuccess(String type, String name);
    String atLeastOneIsRequired(String attributes);
    String available(double value);

    String bootErrors();
    String blacklist(String blacklist);

    SafeHtml callerThreadLabel(String name);
    SafeHtml cancelActiveOperation(String operation);
    SafeHtml cancelledLabel(boolean name);
    SafeHtml cancelledOperation(String name);
    SafeHtml cannotBrowseUnmanaged();
    SafeHtml cannotDownloadExploded();

    SafeHtml capabilityReference(String capability);
    SafeHtml certificateExpired(String alias);
    SafeHtml certificateShouldRenew(int days, String alias, String dueDate);
    // String certificateShouldNotRenew(int days, String dueDate);
    SafeHtml changeAccountKeyError(String name, String error);
    String changeAccountKeyQuestion(String name);
    SafeHtml changeAccountKeySuccess(String name);
    SafeHtml changeAliasError(String alias, String newAlias, String resource, String error);
    SafeHtml changeAliasSuccess(String alias, String newAlias, String resource);
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
    SafeHtml clearCacheError(String name, String error);
    SafeHtml clearCacheSuccess(String name);
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
    SafeHtml createAccountError(String name, String error);
    String createAccountQuestion(String name);
    SafeHtml createAccountSuccess(String name);
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
    SafeHtml deactivateAccountError(String name, String error);
    SafeHtml deactivateAccountSuccess(String name);
    String deactivateAccountQuestion(String name);
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

    String destroy(String name);

    SafeHtml destroyServerError(String name);

    SafeHtml destroyServerQuestion(String name);

    SafeHtml destroyServerSuccess(String name);

    SafeHtml destroyServerGroupError(String name);

    SafeHtml destroyServerGroupQuestion(String name);

    SafeHtml destroyServerGroupSuccess(String name);
    SafeHtml disableSSLManagementQuestion(String serverName);
    SafeHtml disableSSLManagementError(String cause);
    SafeHtml disableSSLManagementSuccess();
    SafeHtml disableSSLUndertowQuestion(String httpsListener);
    SafeHtml disableSSLUndertowSuccess(String httpsListener);
    SafeHtml disableSSLUndertowError(String httpsListener, String cause);
    SafeHtml domainConfigurationChanged();
    SafeHtml domainControllerTimeout(String name);
    String domainUuidLabel(String uuid);
    SafeHtml domainRolloutLabel(boolean name);
    SafeHtml dropSubscriptionsQuestion(String topic);
    SafeHtml dropSubscriptionsSuccess(String topic);

    SafeHtml duplicateAuthenticationModule();
    String duplicateAuthenticationModuleReason();
    String duplicateResource(String type);
    SafeHtml loadProviderDynamicWarning();

    String ejbFilterDescription();
    SafeHtml emptyModelNodeForm();
    String enableSSLConfirmationDescription();
    SafeHtml enableManagementSSLDescription();
    SafeHtml enableSSLMutualQuestion();
    SafeHtml enableSSLResultsError();
    SafeHtml enableSSLResultsSuccessDomain(String url);
    SafeHtml enableSSLResultsSuccessStandalone(String url);
    SafeHtml enableSSLStrategyQuestion();
    SafeHtml enableSSLStrategyQuestionCreateAll();
    SafeHtml enableSSLStrategyQuestionCreateKeyStore();
    SafeHtml enableSSLStrategyQuestionObtainFromLetsEncrypt();
    SafeHtml enableSSLStrategyQuestionReuseKeyStore();
    SafeHtml enableSSLSuccess();
    SafeHtml enableUndertowSSLDescription();
    SafeHtml enableSSLResultsSuccessUndertow(String listener, String sslContext);
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
    SafeHtml exportCertificateError(String alias, String path, String resource, String error);
    SafeHtml exportCertificateSuccess(String alias, String path, String resource);
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

    SafeHtml failedReadKeycloak(String address, String errorMessage);
    SafeHtml failedRedirectConsole(String url, String message);
    String filterBy(String name);
    SafeHtml flushConnectionSuccess();

    SafeHtml generateCSRError(String alias, String path, String resource, String error);
    SafeHtml generateCSRSuccess(String alias, String path, String resource);
    SafeHtml generateKeyPairError(String alias, String resource, String error);
    SafeHtml generateKeyPairSuccess(String alias, String resource);
    SafeHtml getMetadataError(String name, String error);
    SafeHtml getMetadataSuccess(String name);
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

    String identityPasswordBcrypt();
    String identityPasswordClear();
    String identityPasswordDigest();
    String identityPasswordOtp();
    String identityPasswordSaltedSimpleDigest();
    String identityPasswordScramDigest();
    String identityPasswordSimpleDigest();
    SafeHtml includeAllHelpText();
    String identityDescription();
    SafeHtml identityAttributeHelp();
    SafeHtml importCertificateError(String alias, String path, String resource, String error);
    SafeHtml importCertificateSuccess(String alias, String path, String resource);
    SafeHtml initKeyManagerError(String name, String error);
    SafeHtml initKeyManagerSuccess(String name);
    SafeHtml initTrustManagerError(String name, String error);
    SafeHtml initTrustManagerSuccess(String name);
    SafeHtml invalidExtensionJson();
    SafeHtml invalidExtensionMetadata(String extensionDocumentation);
    String invalidFormat();
    String invalidTagFormat(String allowed);
    String invalidJNDIName();
    String invalidLength();
    String invalidRange(double value, long min, long max);
    SafeHtml invalidateSessionQuestion();
    SafeHtml invalidateSessionSuccess();
    SafeHtml invalidateSessionError(String cause);

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

    SafeHtml killServerGroupError(String name);

    SafeHtml killServerGroupQuestion(String name);

    SafeHtml killServerGroupSuccess(String name);

    SafeHtml largeLogFile(String size);
    SafeHtml lastOperationException();
    SafeHtml lastOperationFailed();
    SafeHtml listHint();
    SafeHtml loadPropertiesRealmError(String name, String error);
    SafeHtml loadPropertiesRealmSuccess(String name);
    SafeHtml loadContentError();
    String locationRequired();
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
    String microprofileHealthNoChecks();
    SafeHtml microprofileHealthOutcome(String outcome);
    String microprofileHealthPreviewDescription();
    String modifyResourceTitle(String type);
    SafeHtml modifyResourceSuccess(String type, String name);
    SafeHtml modifySingleResourceSuccess(String type);

    SafeHtml moreThanOneCacheMemory();
    SafeHtml moreThanOneCacheStore();
    String moreThanOneCacheStoreDetails();
    SafeHtml moreThanOneKeyMapperForPrincipalQuery();
    SafeHtml moveMessageSuccess(String queue);
    SafeHtml multiValueListHint();

    SafeHtml nearCacheInvalidation();

    SafeHtml nearCacheNone();

    SafeHtml nearCacheUndefined();
    SafeHtml newContentSuccess(String deployment, String file);

    String noBootErrors();
    SafeHtml noChanges();
    SafeHtml noContent();
    SafeHtml noContentSelected();
    SafeHtml noContentSelectedInDeployment();
    SafeHtml noContextForNormalMode();
    SafeHtml noDeployment();

    SafeHtml noDeploymentPermissions();
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

    SafeHtml obtainCertificateError(String alias, String resource, String error);
    SafeHtml obtainCertificateSuccess(String alias, String resource);
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
    String profileIncludes(String includes);
    SafeHtml profileNotUsedInServerGroups();
    SafeHtml profileUsedInServerGroups(SafeHtml serverGroupLinks);
    String proxyColumnFilterDescription();
    SafeHtml propertiesHint();
    SafeHtml pruneDisconnectedQuestion();
    SafeHtml pruneExpiredQuestion();
    SafeHtml pruneSuccessful();

    SafeHtml readAliasError(String alias, String resource, String error);
    SafeHtml readAliasesError(String resource, String error);
    SafeHtml readAliasesSuccess(String resource);
    String readDatasourcePropertiesErrorDomain(String profile);
    SafeHtml readIdentityError(String identity, String realm, String error);
    String recordedOperations(@PluralCount int count);
    SafeHtml recordingStarted();
    SafeHtml recordingStopped();

    String referenceServer(String server);
    String reload(String name);

    SafeHtml reloadConsoleRedirect(String url);
    SafeHtml reloadConsoleTimeout(String type, String url);
    SafeHtml reloadCRLError(String name, String error);
    SafeHtml reloadCRLSuccess(String name);
    SafeHtml reloadError(String resource, String error);
    SafeHtml reloadSuccess(String resource);
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

    SafeHtml removeAliasError(String alias, String resource, String error);
    SafeHtml removeAliasQuestion(String alias, String resource);
    SafeHtml removeAliasSuccess(String alias, String resource);
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
    SafeHtml removeIdentityQuestion(String identity, String realm);
    SafeHtml removeIdentityError(String identity, String realm, String error);
    SafeHtml removeIdentitySuccess(String identity, String realm);
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

    SafeHtml requires(String requires);
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
    SafeHtml revokeCertificateError(String alias, String resource, String error);
    SafeHtml revokeCertificateSuccess(String alias, String resource);
    String roleColumnFilterDescription();
    SafeHtml rollbackError(String failure);
    SafeHtml rollbackInProgress(String patchid);
    SafeHtml runningTimeLabel(String name);

    SafeHtml saveContentSuccess(String deployment, String file);
    SafeHtml saveIdentityError(String identity, String realm, String error);
    SafeHtml saveIdentitySuccess(String identity, String realm);
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
    SafeHtml setIdentityPasswordError(String identity, String realm, String error);
    SafeHtml setIdentityPasswordSuccess(String identity, String realm);
    SafeHtml setIdentityPasswordQuestion();
    SafeHtml setSecretPasswordError(String alias, String resource, String error);
    SafeHtml setSecretPasswordSuccess(String alias, String resource);

    SafeHtml sharedStoreColocated();
    SafeHtml sharedStoreMaster();
    SafeHtml sharedStoreSlave();
    SafeHtml sharedStoreStrategy();
    SafeHtml simpleProviderWarning();
    SafeHtml ssoAccessControlWarning();
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
    SafeHtml storeError(String resource, String error);
    SafeHtml storeSuccess(String resource);

    String susbsystemFilterDescription();
    String suspend(String name);
    SafeHtml suspendServerGroupError(String name);
    SafeHtml suspendServerGroupQuestion(String name);
    SafeHtml suspendServerGroupSuccess(String name);
    SafeHtml suspendServerError(String name);
    SafeHtml suspendServerQuestion(String name);
    SafeHtml suspendServerSuccess(String name);

    SafeHtml switchProviderSuccess(String from, String to);
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

    SafeHtml tourAccessControl();
    SafeHtml tourAccessControlRoles();
    SafeHtml tourAccessControlUsers();
    SafeHtml tourDomainConfiguration();
    SafeHtml tourDomainConfigurationSubsystem();
    SafeHtml tourDomainDeploymentsAddActions();
    SafeHtml tourDomainDeploymentsBrowseBy();
    SafeHtml tourDomainHomeConfiguration();
    SafeHtml tourDomainHomeDeployments();
    SafeHtml tourDomainHomeRuntime();
    SafeHtml tourDomainRuntimeBrowseBy();
    SafeHtml tourDomainRuntimeServer();
    SafeHtml tourDomainRuntimeServerAdd();
    SafeHtml tourDomainRuntimeServerGroup();
    SafeHtml tourDomainRuntimeServerGroupsAdd();
    SafeHtml tourStandaloneConfigurationSubsystem();
    SafeHtml tourStandaloneDeployment();
    SafeHtml tourStandaloneDeploymentAddActions();
    SafeHtml tourStandaloneHomeConfiguration();
    SafeHtml tourStandaloneHomeDeployments();
    SafeHtml tourStandaloneHomeRuntime();
    SafeHtml tourStandaloneRuntimeServer();
    SafeHtml tourStandaloneRuntimeSubsystem();

    SafeHtml unauthorized();
    SafeHtml undeployedContent(String name);

    String undertowListenerProcessingDisabled(String listener, String server);
    String updateAvailable(String current, String update);
    SafeHtml updateAccountError(String name, String error);
    String updateAccountQuestion(String name);
    SafeHtml updateAccountSuccess(String name);
    SafeHtml updateServerError(String name);
    SafeHtml uploadContentDescription();
    String uploadContentInvalid();
    SafeHtml uploadError(String name);
    SafeHtml uploadInProgress(String name);
    SafeHtml uploadSuccessful(String name);

    SafeHtml unit(String unit);
    SafeHtml unknownError();
    SafeHtml unknownResource();
    String unknownResourceDetails(String address, String reason);
    SafeHtml unsupportedFileTypeDescription();
    String uptime(String uptime);
    String used(double value);

    SafeHtml verifyRenewError(String alias, String resource, String error);
    SafeHtml verifyRenewSuccess(String alias, String resource);
    String view(String type);

    SafeHtml writeBehaviour(String current, String switchTo);
    //@formatter:on
}
