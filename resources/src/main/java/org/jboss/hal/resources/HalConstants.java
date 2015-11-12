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

import com.google.gwt.i18n.client.Constants;

public interface HalConstants extends Constants {

    //@formatter:off
    String add();

    String bootstrap_failed();
    String bootstrap_exception();

    String cancel();
    String close();
    String connect_to_server();

    String dispatcher_exception();
    String dispatcher_failed();

    String edit();

    String endpoint_select_title();
    String endpoint_select_description();
    String endpoint_connect();
    String endpoint_add_title();
    String endpoint_add_description();

    String expression_resolver();

    String form_errors();
    String form_reset_desc();

    String help();

    String homepage_new_to_eap();
    String homepage_take_a_tour();

    String homepage_deployments_sub_header();
    String homepage_deployments_section();
    String homepage_deployments_standalone_step_intro();
    String homepage_deployments_standalone_step_1();
    String homepage_deployments_step_enable();
    String homepage_deployments_domain_step_intro();
    String homepage_deployments_domain_step_1();
    String homepage_deployments_domain_step_2();

    String homepage_configuration_standalone_sub_header();
    String homepage_configuration_domain_sub_header();
    String homepage_configuration_section();
    String homepage_configuration_step_intro();
    String homepage_configuration_standalone_step1();
    String homepage_configuration_domain_step1();
    String homepage_configuration_step2();
    String homepage_configuration_step3();

    String homepage_runtime_standalone_sub_header();
    String homepage_runtime_standalone_section();
    String homepage_runtime_step_intro();
    String homepage_runtime_standalone_step1();
    String homepage_runtime_standalone_step2();
    String homepage_runtime_domain_sub_header();
    String homepage_runtime_domain_server_group_section();
    String homepage_runtime_domain_server_group_step_intro();
    String homepage_runtime_domain_server_group_step1();
    String homepage_runtime_domain_server_group_step2();
    String homepage_runtime_domain_create_server_section();
    String homepage_runtime_domain_create_server_step_intro();
    String homepage_runtime_domain_create_server_step1();
    String homepage_runtime_domain_create_server_step2();
    String homepage_runtime_domain_monitor_server_section();
    String homepage_runtime_domain_monitor_server_step1();
    String homepage_runtime_domain_monitor_server_step2();

    String homepage_access_control_sub_header();
    String homepage_access_control_section();
    String homepage_access_control_step_intro();
    String homepage_access_control_step1();
    String homepage_access_control_step2();

    String homepage_patching_section();
    String homepage_patching_step1();
    String homepage_patching_domain_step2();
    String homepage_patching_step_apply();

    String homepage_help_need_help();
    String homepage_help_general_resources();
    String homepage_help_eap_documentation_text();
    String homepage_help_eap_documentation_link();
    String homepage_help_learn_more_eap_text();
    String homepage_help_learn_more_eap_link();
    String homepage_help_trouble_ticket_text();
    String homepage_help_trouble_ticket_link();
    String homepage_help_training_text();
    String homepage_help_training_link();
    String homepage_help_tutorials_link();
    String homepage_help_tutorials_text();
    String homepage_help_eap_community_link();
    String homepage_help_eap_community_text();
    String homepage_help_eap_configurations_link();
    String homepage_help_eap_configurations_text();
    String homepage_help_knowledgebase_link();
    String homepage_help_knowledgebase_text();
    String homepage_help_consulting_link();
    String homepage_help_consulting_text();
    String homepage_help_wilfdfly_documentation_text();
    String homepage_help_admin_guide_text();
    String homepage_help_wildfly_issues_text();
    String homepage_help_get_help();
    String homepage_help_irc_text();
    String homepage_help_user_forums_text();
    String homepage_help_developers_mailing_list_text();
    String homepage_help_wilfdfly_home_text();
    String homepage_help_model_reference_text();
    String homepage_help_latest_news();

    String logout();
    String model_browser();
    String not_a_number();
    String required();
    String reset();
    String restricted();
    String remove();
    String same_origin();
    String save();
    String settings();
    String start();

    String table_toolbar();
    String table_default_group();
    String table_info_empty();

    String toggle_navigation();
    String tools();
    String unknown_error();
    //@formatter:on
}
