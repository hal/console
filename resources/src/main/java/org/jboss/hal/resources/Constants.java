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

public interface Constants extends com.google.gwt.i18n.client.Constants {

    //@formatter:off
    String aboutEnvironment();
    String accessControlProvider();
    String accessType();
    String action();
    String active();
    String add();
    String adminOnly();
    String assign();
    String assignedContentDesc();
    String attribute();
    String attributes();

    String back();
    String bootstrapFailed();
    String bootstrapException();
    String browseBy();

    String cancel();
    String chooseSingleton();
    String chooseTemplate();
    String clear();
    String clearMessages();
    String close();
    String closed();
    String commited();
    String connection();
    String connections();
    String connectionPool();
    String connectToServer();
    String consoleVersion();
    String content();
    String contentRepository();
    String copied();
    String copy();
    String copyToClipboard();
    String count();
    String counter();
    String custom();

    String data();
    String day();
    String days();
    String defaultValue();
    String deploymentAttributes();
    String details();
    String disable();
    String disabled();
    String download();
    String duplicateMacro();

    String edit();
    String enable();
    String enabled();
    String enableRbac();
    String enableStatistics();
    String endpointSelectTitle();
    String endpointSelectDescription();
    String endpointConnect();
    String endpointAddTitle();
    String endpointAddDescription();
    String environment();
    String expressionResolver();

    String failed();
    String filter();
    String finish();
    String flushAll();
    String flushGracefully();
    String flushIdle();
    String flushInvalid();
    String formErrors();
    String formResetDesc();

    String gotoDeployment();
    String group();
    String groups();

    String help();
    String hiddenColumns();
    String hitCount();
    String homepageNewToEap();
    String homepageTakeATour();
    String homepageDeploymentsSubHeader();
    String homepageDeploymentsSection();
    String homepageDeploymentsStandaloneStepIntro();
    String homepageDeploymentsStandaloneStep1();
    String homepageDeploymentsStepEnable();
    String homepageDeploymentsDomainStepIntro();
    String homepageDeploymentsDomainStep1();
    String homepageDeploymentsDomainStep2();
    String homepageConfigurationStandaloneSubHeader();
    String homepageConfigurationDomainSubHeader();
    String homepageConfigurationSection();
    String homepageConfigurationStepIntro();
    String homepageConfigurationStandaloneStep1();
    String homepageConfigurationDomainStep1();
    String homepageConfigurationStep2();
    String homepageConfigurationStep3();
    String homepageRuntimeStandaloneSubHeader();
    String homepageRuntimeStandaloneSection();
    String homepageRuntimeStepIntro();
    String homepageRuntimeStandaloneStep1();
    String homepageRuntimeStandaloneStep2();
    String homepageRuntimeDomainSubHeader();
    String homepageRuntimeDomainServerGroupSection();
    String homepageRuntimeDomainServerGroupStepIntro();
    String homepageRuntimeDomainServerGroupStep1();
    String homepageRuntimeDomainServerGroupStep2();
    String homepageRuntimeDomainCreateServerSection();
    String homepageRuntimeDomainCreateServerStepIntro();
    String homepageRuntimeDomainCreateServerStep1();
    String homepageRuntimeDomainCreateServerStep2();
    String homepageRuntimeDomainMonitorServerSection();
    String homepageRuntimeDomainMonitorServerStep1();
    String homepageRuntimeDomainMonitorServerStep2();
    String homepageAccessControlSubHeader();
    String homepageAccessControlSection();
    String homepageAccessControlStepIntro();
    String homepageAccessControlStep1();
    String homepageAccessControlStep2();
    String homepagePatchingSection();
    String homepagePatchingStep1();
    String homepagePatchingDomainStep2();
    String homepagePatchingStepApply();
    String homepageHelpNeedHelp();
    String homepageHelpGeneralResources();
    String homepageHelpEapDocumentationText();
    String homepageHelpEapDocumentationLink();
    String homepageHelpLearnMoreEapText();
    String homepageHelpLearnMoreEapLink();
    String homepageHelpTroubleTicketText();
    String homepageHelpTroubleTicketLink();
    String homepageHelpTrainingText();
    String homepageHelpTrainingLink();
    String homepageHelpTutorialsLink();
    String homepageHelpTutorialsText();
    String homepageHelpEapCommunityLink();
    String homepageHelpEapCommunityText();
    String homepageHelpEapConfigurationsLink();
    String homepageHelpEapConfigurationsText();
    String homepageHelpKnowledgebaseLink();
    String homepageHelpKnowledgebaseText();
    String homepageHelpConsultingLink();
    String homepageHelpConsultingText();
    String homepageHelpWilfdflyDocumentationText();
    String homepageHelpAdminGuideText();
    String homepageHelpWildflyIssuesText();
    String homepageHelpGetHelp();
    String homepageHelpIrcText();
    String homepageHelpUserForumsText();
    String homepageHelpDevelopersMailingListText();
    String homepageHelpWildFlyHomeText();
    String homepageHelpModelReferenceText();
    String homepageHelpLatestNews();
    String hour();
    String hours();

    String input();

    String jdbcDriver();

    String lastModified();
    String loading();
    String loadingPleaseWait();
    String logFile();
    String logFiles();
    String logout();

    String macroEditor();
    String mainAttributes();
    String managementVersion();
    String maxUsed();
    String message();
    String minute();
    String minutes();
    String missCount();
    String modelBrowser();
    String monitor();

    String next();
    String needsReload();
    String needsRestart();
    String no();
    String noAttributes();
    String noConfiguredMailServers();
    String noDetails();
    String noItems();
    String noMacros();
    String noRootLogger();
    String noRootLoggerDescription();
    String noRunningServers();
    String notANumber();
    String nothingSelected();

    String ok();
    String opened();
    String openInExternalWindow();
    String operationMode();
    String operations();
    String output();

    String pending();
    String pin();
    String ping();
    String platform();
    String play();
    String pool();
    String preparedStatementCache();
    String processors();
    String productName();
    String productVersion();

    String refresh();
    String releaseName();
    String releaseVersion();
    String reload();
    String remove();
    String removeResource();
    String rename();
    String required();
    String requiredField();
    String reset();
    String restart();
    String restartAllServices();
    String restartJvm();
    String restartNoServices();
    String restartResourceServices();
    String restricted();
    String resume();
    String role();
    String roles();
    String running();

    String sameOrigin();
    String save();
    String search();
    String second();
    String seconds();
    String security();
    String serverName();
    String sessions();
    String settings();
    String showAll();
    String size();
    String start();
    String starting();
    String startMacro();
    String statements();

    String statisticsDisabled();

    String statisticsDisabledHeader();
    String status();
    String stop();
    String stopMacro();
    String stopped();
    String storage();
    String summary();
    String supportsExpressions();
    String suspend();
    String suspended();
    String switchProvider();

    String tags();
    String tailMode();

    String test();
    String testConnection();
    String timeout();
    String timeouts();
    String toggleDropdown();
    String toggleNavigation();
    String tools();
    String tracking();
    String type();

    String unassign();
    String unassignedContentDesc();
    String unknownState();
    String unpin();
    String used();
    String user();
    String users();

    String validation();
    String view();

    String xaProperties();

    String yes();
    //@formatter:on
}
