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
    String accessType();
    String action();
    String add();
    String allSingletonsExist();
    String attribute();
    String attributes();

    String back();
    String bootstrapFailed();
    String bootstrapException();
    String browseBy();

    String cancel();
    String chooseSingleton();
    String chooseTemplate();
    String clearMessages();
    String close();
    String connection();
    String connectToServer();
    String custom();

    String data();
    String defaultValue();
    String details();
    String detectedDriver();
    String disable();
    String disabled();
    String dispatcherException();
    String dispatcherFailed();

    String edit();
    String enable();
    String enabled();

    String endpointSelectTitle();
    String endpointSelectDescription();
    String endpointConnect();
    String endpointAddTitle();
    String endpointAddDescription();

    String expressionResolver();

    String failed();
    String filter();
    String finish();
    String formErrors();
    String formResetDesc();

    String help();
    String input();

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

    String jdbcDriver();
    String loading();
    String logout();
    String macroEditor();
    String macroPlaybackError();
    String macroPlaybackSuccessful();
    String message();
    String metadataError();
    String modelBrowser();
    String name();
    String next();
    String no();
    String noAttributes();
    String noDetails();
    String noItems();
    String noMacros();
    String notANumber();
    String nothingSelected();
    String ok();
    String operations();
    String output();
    String ping();
    String play();
    String recordingStarted();
    String recordingStopped();
    String remove();
    String removeResource();
    String rename();
    String required();
    String requiredField();
    String reset();

    String restartAllServices();
    String restartJvm();
    String restartNoServices();
    String restartResourceServices();

    String restricted();
    String sameOrigin();
    String save();
    String settings();
    String showAll();
    String specifyDriver();
    String start();
    String startMacro();
    String stopMacro();
    String storage();
    String summary();
    String supportsExpressions();

    String testConnection();
    String toggleDropdown();
    String toggleNavigation();
    String tools();
    String type();
    String unknownError();
    String view();
    String xaProperties();
    String yes();
    //@formatter:on
}
