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

import org.jetbrains.annotations.NonNls;

/**
 * Contains common CSS classes from HAL, PatternFly & Bootstrap. The constants in this interface are not involved in
 * any kind of code generation or GWT magic. They're just here to have them in one place.
 *
 * @author Harald Pehl
 */
public interface CSS {

    int labelColumns = 3;
    int inputColumns = 9;

    String active = "active";
    String alert = "alert";
    String alertDanger = "alert-danger";
    String alertDismissable = "alert-dismissable";
    String alertInfo = "alert-info";
    String alertLink = "alert-link";
    String alertSuccess = "alert-success";
    String alertWarning = "alert-warning";
    String arrow = "arrow";
    String attributes = "attributes";

    String badge = "badge";
    String badgeContainerPf = "badge-container-pf";
    String blankSlatePf = "blank-slate-pf";
    String blankSlatePfIcon = "blank-slate-pf-icon";
    String blankSlatePfMainAction = "blank-slate-pf-main-action";
    String blankSlatePfSecondaryAction = "blank-slate-pf-secondary-action";
    String bootstrapSwitch = "bootstrap-switch";
    String btn = "btn";
    String btnDefault = "btn-default";
    String btnFinder = "btn-finder";
    String btnGroup = "btn-group";
    String btnHal = "btn-hal";
    String btnLg = "btn-lg";
    String btnLink = "btn-link";
    String btnPrimary = "btn-primary";

    String caret = "caret";
    String clearfix = "clearfix";
    String clickable = "clickable";
    String close = "close";
    String column = "col";
    String columnMedium = "md";
    String collapse = "collapse";
    String containerFluid = "container-fluid";
    String containerPfNavPfVertical = "container-pf-nav-pf-vertical";
    String containerPfNavPfVerticalWithSecondary = "container-pf-nav-pf-vertical-with-secondary";
    String controlLabel = "control-label";
    String copy = "copy";

    String dataTable = "datatable";
    String defaultValue = "default-value";
    String disabled = "disabled";
    String dropdown = "dropdown";
    String dropdownKebabPf = "dropdown-kebab-pf";
    String dropdownMenu = "dropdown-menu";
    String dropdownMenuRight = "dropdown-menu-right";
    String dropdownToggle = "dropdown-toggle";

    String eapHomeRow = "eap-home-row";
    String eapHomeTitle = "eap-home-title";
    String editing = "editing";
    String editor = "editor";
    String errorCircleO = "error-circle-o";
    String equals = "equals";
    String empty = "empty";

    String fade = "fade";
    String filter = "filter";
    String finder = "finder";
    String finderColumn = "finder-column";
    String finderPreview = "finder-preview";
    String folder = "folder";
    String form = "form";
    String formButtons = "form-buttons";
    String formControl = "form-control";
    String formControlStatic = "form-control-static";
    String formGroup = "form-group";
    String formHelpContent = "form-help-content";
    String formHorizontal = "form-horizontal";
    String formLinkLabel = "form-link-label";
    String formLinks = "form-links";
    String formSection = "form-section";

    String hasError = "has-error";
    String hasFeedback = "has-feedback";
    String helpBlock = "help-block";
    String hiddenColumns = "hidden-columns";
    String hint = "hint";
    String hover = "hover";

    String in = "in";
    String indicator = "indicator";
    String info = "info";
    String inputGroup = "input-group";
    String inputGroupAddon = "input-group-addon";
    String inputGroupBtn = "input-group-btn";
    String itemIcon = "item-icon";
    String itemText = "item-text";

    String key = "key";

    String last = "last";
    String lead = "lead";
    String listGroup = "list-group";
    String listGroupItem = "list-group-item";
    String listGroupItemHeading = "list-group-item-heading";
    String listGroupItemText = "list-group-item-text";
    String listGroupItemValue = "list-group-item-value";
    String listViewPf = "list-view-pf";
    String listViewPfAdditionalInfo = "list-view-pf-additional-info";
    String listViewPfAdditionalInfoItem = "list-view-pf-additional-info-item";
    String listViewPfActions = "list-view-pf-actions";
    String listViewPfBody = "list-view-pf-body";
    String listViewPfCheckbox = "list-view-pf-checkbox";
    String listViewPfDescription = "list-view-pf-description";
    String listViewPfLeft = "list-view-pf-left";
    String listViewPfMainInfo = "list-view-pf-main-info";
    String listViewPfStacked = "list-view-pf-stacked";
    String loading = "loading";
    String loadingContainer = "loading-container";

    String macroEditor = "macro-editor";
    String macroList = "macro-list";
    String marginTop20 = "margin-top-20";
    String messageDetails = "message-details";
    String messageDetailsPre = "message-details-pre";
    String modal = "modal";
    String modalBody = "modal-body";
    String modalContent = "modal-content";
    String modalDialog = "modal-dialog";
    String modalFooter = "modal-footer";
    String modalHeader = "modal-header";
    String modalLarge = "modal-lg";
    String modalMax = "modal-mx";
    String modalMedium = "modal-md";
    String modelSmall = "modal-sm";
    String modalTitle = "modal-title";
    String modelBrowserButtons = "model-browser-buttons";
    String modelBrowserContent = "model-browser-content";
    String modelBrowserTree = "model-browser-tree";

    String nav = "nav";
    String navbar = "navbar";
    String navPfPersistentSecondary = "nav-pf-persistent-secondary";
    String navPfPersistentSecondaryHal = "nav-pf-persistent-secondary-hal";
    String navPfVertical = "nav-pf-vertical";
    String navPfVerticalHal = "nav-pf-vertical-hal";
    String navPfVerticalWithSecondaryNav = "nav-pf-vertical-with-secondary-nav";
    String navTabs = "nav-tabs";
    String navTabsHal = "nav-tabs-hal";
    String navTabsPf = "nav-tabs-pf";
    String noMacros = "no-macros";

    String offset = "offset";
    String ok = "ok";
    String ondrag = "ondrag";
    String operationParameter = "operation-parameter";
    String operations = "operations";

    String panel = "panel";
    String panelBody = "panel-default";
    String panelCollapse = "panel-collapse";
    String panelDefault = "panel-default";
    String panelGroup = "panel-group";
    String panelHeading = "panel-heading";
    String panelTitle = "panel-title";
    String persistentSecondary = "persistent-secondary";
    String persistentSecondaryHeader = "persistent-secondary-header";
    String pin = "pin";
    String pinned = "pinned";
    String pinnable = "pinnable";
    String progress = "progress";
    String progressBar = "progress-bar";
    String progressBarStriped = "progress-bar-striped";
    String progressXs = "progress-xs";
    String properties = "properties";
    String pullLeft = "pull-left";
    String pullRight = "pull-right";
    String pulse = "pulse";

    String radio = "radio";
    String readonly = "readonly";
    String refresh = "refresh";
    String restricted = "restricted";
    String row = "row";

    String secondaryVisiblePf = "secondary-visible-pf";
    String selectpicker = "selectpicker";
    String separator = "separator";
    String spinner = "spinner";
    String srOnly = "sr-only";
    String subtitle = "subtitle";

    String tabContent = "tab-content";
    String tabPane = "tab-pane";
    String table = "table";
    String tableBordered = "table-bordered";
    String tableStriped = "table-striped";
    String tagManagerContainer = "tag-manager-container";
    String tagManagerTag = "tag-manager-tag";
    String tags = "tags";
    String timestamp = "timestamp";
    String tmTag = "tm-tag";
    String tmTagRemove = "tm-tag-remove";
    String toastPf = "toast-pf";
    String toastPfAction = "toast-pf-action";
    String toastPfMaxWidth = "toast-pf-max-width";
    String toastPfTopRight = "toast-pf-top-right";
    String ttNested = "tt-nested";

    String unpin = "unpin";
    String unpinned = "unpinned";

    String value = "value";
    String valueDropdown = "value-dropdown";

    String warningTriangleO = "warning-triangle-o";
    String withProgress = "with-progress";
    String wizardHeader = "wizard-header";
    String wizardProgress = "wizard-progress";
    String wizardStep = "wizard-step";

    static String column(int columns) {
        return column + "-" + columnMedium + "-" + String.valueOf(columns);
    }

    static String offset(int columns) {
        return column + "-" + columnMedium + "-" + offset + "-" + String.valueOf(columns);
    }

    /**
     * Builds a FontAwesome CSS class.
     *
     * @param name the name of the FontAwesome icon <strong>w/o</strong> the "fa fa-" prefix.
     */
    static String fontAwesome(@NonNls String name) {
        return "fa fa-" + name; //NON-NLS
    }

    /**
     * Builds a PatternFly icon class.
     *
     * @param name the name of the PatternFly icon <strong>w/o</strong> the "pficon pficon-" prefix.
     */
    static String pfIcon(@NonNls String name) {
        return "pficon pficon-" + name; //NON-NLS
    }
}
