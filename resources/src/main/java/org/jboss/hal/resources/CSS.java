/*
 * Copyright 2015-2016 Red Hat, Inc, and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.hal.resources;

import java.util.Arrays;

import elemental2.dom.CSSProperties.HeightUnionType;
import elemental2.dom.CSSProperties.WidthUnionType;

import static java.util.stream.Collectors.joining;

/**
 * Contains common CSS classes from HAL, PatternFly & Bootstrap. The constants in this interface are not involved in any
 * kind of code generation or GWT magic. They're just here to have them in one place.
 */
public interface CSS {

    String aboutModalPf = "about-modal-pf";
    String address = "address";
    String active = "active";
    String activeRoles = "active-roles";
    String alert = "alert";
    String alertDanger = "alert-danger";
    String alertDismissable = "alert-dismissable";
    String alertInfo = "alert-info";
    String alertLink = "alert-link";
    String alertSuccess = "alert-success";
    String alertSuspended = "alert-suspended";
    String alertWarning = "alert-warning";
    String arrow = "arrow";
    String attributes = "attributes";
    String autocompleteSuggestion = "autocomplete-suggestion";
    String autocompleteSuggestions = "autocomplete-suggestions";

    String back = "back";
    String badge = "badge";
    String badgeContainerPf = "badge-container-pf";
    String blankSlatePf = "blank-slate-pf";
    String blankSlatePfIcon = "blank-slate-pf-icon";
    String blankSlatePfMainAction = "blank-slate-pf-main-action";
    String blankSlatePfSecondaryAction = "blank-slate-pf-secondary-action";
    String blue = "blue";
    String bootstrapError = "bootstrap-error";
    String bootstrapSelect = "bootstrap-select";
    String bootstrapSwitch = "bootstrap-switch";
    String bootstrapSwitchContainer = "bootstrap-switch-container";
    String breadcrumb = "breadcrumb";
    String breadcrumbTools = "breadcrumb-tools";
    String breakTooltip = "break-tooltip";
    String btn = "btn";
    String btnCancel = "btn-cancel";
    String btnDefault = "btn-default";
    String btnFinder = "btn-finder";
    String btnGroup = "btn-group";
    String btnHal = "btn-hal";
    String btnLg = "btn-lg";
    String btnLink = "btn-link";
    String btnPrimary = "btn-primary";
    String btnToolbar = "btn-toolbar";

    String caret = "caret";
    String centerBlock = "center-block";
    String clear = "clear";
    String clearfix = "clearfix";
    String clickable = "clickable";
    String close = "close";
    String column = "col";
    String columnAction = "column-action";
    String columnLg = "lg";
    String columnMd = "md";
    String columnSm = "sm";
    String columnXs = "xs";
    String collapse = "collapse";
    String containerFluid = "container-fluid";
    String containerPfNavPfVertical = "container-pf-nav-pf-vertical";
    String containerPfNavPfVerticalWithSubMenus = "container-pf-nav-pf-vertical-with-sub-menus";
    String contentViewPfPagination = "content-view-pf-pagination";
    String controlLabel = "control-label";
    String copy = "copy";
    String currentStep = "current-step";

    String dataTable = "datatable";
    String date = "date";
    String defaultValue = "default-value";
    String deprecated = "deprecated";
    String disabled = "disabled";
    String divider = "divider";
    String disconnected = "disconnected";
    String dlHorizontal = "dl-horizontal";
    String drawerPf = "drawer-pf";
    String drawerPfAction = "drawer-pf-action";
    String drawerPfActionLink = "drawer-pf-action-link";
    String drawerPfClose = "drawer-pf-close";
    String drawerPfExpanded = "drawer-pf-expanded";
    String drawerPfHal = "drawer-pf-hal";
    String drawerPfNotification = "drawer-pf-notification";
    String drawerPfNotificationContent = "drawer-pf-notification-content";
    String drawerPfNotificationMessage = "drawer-pf-notification-message";
    String drawerPfNotificationInfo = "drawer-pf-notification-info";
    String drawerPfTitle = "drawer-pf-title";
    String drawerPfTrigger = "drawer-pf-trigger";
    String drawerPfTriggerIcon = "drawer-pf-trigger-icon";
    String drawerPfToggleExpand = "drawer-pf-toggle-expand";
    String drawerPfNotificationsNonClickable = "drawer-pf-notifications-non-clickable";
    String dropdown = "dropdown";
    String dropdownKebabPf = "dropdown-kebab-pf";
    String dropdownMenu = "dropdown-menu";
    String dropdownMenuRight = "dropdown-menu-right";
    String dropdownToggle = "dropdown-toggle";

    String eapHomeCol = "eap-home-col";
    String eapHomeModule = "eap-home-module";
    String eapHomeModuleContainer = "eap-home-module-container";
    String eapHomeModuleCol = "eap-home-module-col";
    String eapHomeModuleHeader = "eap-home-module-header";
    String eapHomeModuleLink = "eap-home-module-link";
    String eapHomeModuleIcon = "eap-home-module-icon";
    String eapHomeRow = "eap-home-row";
    String eapHomeSectionIcon = "eap-home-section-icon";
    String eapHomeTitle = "eap-home-title";
    String eapPagination = "eap-pagination";
    String eapQuickTour = "eap-quick-tour";
    String eapQuickTourStep1 = "eap-quick-tour-step-1";
    String eapQuickTourStep2 = "eap-quick-tour-step-2";
    String eapQuickTourStep3 = "eap-quick-tour-step-3";
    String eapQuickTourStep4 = "eap-quick-tour-step-4";
    String eapQuickTourStep5 = "eap-quick-tour-step-5";
    String eapQuickTourSteps = "eap-quick-tour-steps";
    String eapToggleContainer = "eap-toggle-container";
    String eapToggleControls = "eap-toggle-controls";
    String editing = "editing";
    String editor = "editor";
    String editorButtons = "editor-buttons";
    String editorControls = "editor-controls";
    String editorStatus = "editor-status";
    String empty = "empty";
    String error = "error";
    String errorCircleO = "error-circle-o";
    String equals = "equals";
    String expressionModeSwitcher = "expression-mode-switcher";
    String external = "external";

    String faAngleDown = "fa-angle-down";
    String fade = "fade";
    String faSpin = "fa-spin";
    String fieldSectionTogglePf = "field-section-toggle-pf";
    String fieldsSectionPf = "fields-section-pf";
    String fieldsSectionHeaderPf = "fields-section-header-pf";
    String filter = "filter";
    String finder = "finder";
    String finderColumn = "finder-column";
    String finderItem = "finder-item";
    String finderPreview = "finder-preview";
    String flexRow = "flex-row";
    String folder = "folder";
    String footer = "footer";
    String footerProgress = "footer-progress";
    String footerTools = "footer-tools";
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

    String grey = "grey";

    String halBreadcrumb = "hal-breadcrumb";
    String halConfChangesAdditionalInfo = "hal-conf-changes-additional-info";
    String halExecutionDuration = "hal-execution-duration";
    String halExecutionTime = "hal-execution-time";
    String halFormLabel = "hal-form-label";
    String halFormInput = "hal-form-input";
    String halFormOffset = "hal-form-offset";
    String halHeaderCollapse = "hal-header-collapse";
    String halSearch = "hal-search";
    String halTableButtons = "hal-table-buttons";
    String hasButton = "has-button";
    String hasClear = "has-clear";
    String hasError = "has-error";
    String headerForm = "header-form";
    String header = "header";
    String helpBlock = "help-block";
    String hidden = "hidden";
    String hiddenXs = "hidden-xs";
    String hide = "hide";
    String hiddenColumns = "hidden-columns";
    String hint = "hint";
    String hostContainer = "host-container";

    String i = "i";
    String iconBar = "icon-bar";
    String imgResponsive = "img-responsive";
    String imgThumbnail = "img-thumbnail";
    String in = "in";
    String inactive = "inactive";
    String indicator = "indicator";
    String info = "info";
    String inner = "inner";
    String inputGroup = "input-group";
    String inputGroupAddon = "input-group-addon";
    String inputGroupBtn = "input-group-btn";
    String invisible = "invisible";
    String itemIcon = "item-icon";
    String itemText = "item-text";

    String key = "key";

    String label = "label";
    String labelInfo = "label-info";
    String langJava = "lang-java";
    String last = "last";
    String lead = "lead";
    String link = "link";
    String listGroup = "list-group";
    String listGroupItem = "list-group-item";
    String listGroupItemValue = "list-group-item-value";
    String listHalActions = "list-hal-actions";
    String listHalAdditionalContent = "list-hal-additional-content";
    String listHalIconBig = "list-hal-icon-big";
    String listHalIconError = "list-hal-icon-error";
    String listHalIconInfo = "list-hal-icon-info";
    String listHalIconProgress = "list-hal-icon-progress";
    String listHalIconSuccess = "list-hal-icon-success";
    String listHalMainContent = "list-hal-main-content";
    String listHalSelected = "list-hal-selected";
    String listInline = "list-inline";
    String listPf = "list-pf";
    String listPfActions = "list-pf-actions";
    String listPfAdditionalContent = "list-pf-additional-content";
    String listPfContainer = "list-pf-container";
    String listPfContainerLong = "list-pf-container-long";
    String listPfContent = "list-pf-content";
    String listPfContentFlex = "list-pf-content-flex";
    String listPfContentWrapper = "list-pf-content-wrapper";
    String listPfDescription = "list-pf-description";
    String listPfHeader = "list-pf-header";
    String listPfIcon = "list-pf-icon";
    String listPfIconBordered = "list-pf-icon-bordered";
    String listPfIconSmall = "list-pf-icon-small";
    String listPfItem = "list-pf-item";
    String listPfLeft = "list-pf-left";
    String listPfMainContent = "list-pf-main-content";
    String listPfSelect = "list-pf-select";
    String listPfStacked = "list-pf-stacked";
    String listPfTitle = "list-pf-title";
    String loading = "loading";
    String loadingContainer = "loading-container";
    String logFileEditorContainer = "log-file-editor-container";
    String logFileFollow = "log-file-follow";
    String logFileLoading = "log-file-loading";
    String logFilePreview = "log-file-preview";
    String logo = "logo";
    String logoText = "logo-text";
    String logoTextFirst = "logo-text-first";
    String logoTextLast = "logo-text-last";

    String macroEditor = "macro-editor";
    String macroList = "macro-list";
    String marginBottomLarge = "margin-bottom-large";
    String marginBottomSmall = "margin-bottom-small";
    String marginLeftSmall = "margin-left-small";
    String marginRight5 = "margin-right-5";
    String marginRightLarge = "margin-right-large";
    String marginRightSmall = "margin-right-small";
    String marginLeft5 = "margin-left-5";
    String marginTop5 = "margin-top-5";
    String marginTopLarge = "margin-top-large";
    String messageDetails = "message-details";
    String messageDetailsPre = "message-details-pre";
    String modal = "modal";
    String modalBody = "modal-body";
    String modalContent = "modal-content";
    String modalDialog = "modal-dialog";
    String modalFooter = "modal-footer";
    String modalHeader = "modal-header";
    String modalLg = "modal-lg";
    String modalMx = "modal-mx";
    String modalMd = "modal-md";
    String modelSm = "modal-sm";
    String modalTitle = "modal-title";
    String modelBrowserButtons = "model-browser-buttons";
    String modelBrowserContent = "model-browser-content";

    String name = "name";
    String nav = "nav";
    String navbar = "navbar";
    String navbarBrand = "navbar-brand";
    String navbarCollapse = "navbar-collapse";
    String navbarDefault = "navbar-default";
    String navbarFixedBottom = "navbar-fixed-bottom";
    String navbarFixedTop = "navbar-fixed-top";
    String navbarFooter = "navbar-footer";
    String navbarHeader = "navbar-header";
    String navbarNav = "navbar-nav";
    String navbarPf = "navbar-pf";
    String navbarPrimary = "navbar-primary";
    String navbarToggle = "navbar-toggle";
    String navbarUtility = "navbar-utility";
    String navItemIconic = "nav-item-iconic";
    String navItemPfHeader = "nav-item-pf-header";
    String navPfPersistentSecondary = "nav-pf-persistent-secondary";
    String navPfSecondaryNav = "nav-pf-secondary-nav";
    String navPfSecondaryNavHal = "nav-pf-secondary-nav-hal";
    String navPfVertical = "nav-pf-vertical";
    String navPfVerticalHal = "nav-pf-vertical-hal";
    String navPfVerticalWithSubMenus = "nav-pf-vertical-with-sub-menus";
    String navTabs = "nav-tabs";
    String navTabsHal = "nav-tabs-hal";
    String navTabsPf = "nav-tabs-pf";
    String noMacros = "no-macros";

    String offset = "offset";
    String ok = "ok";
    String ondrag = "ondrag";
    String open = "open";
    String operationParameter = "operation-parameter";
    String operations = "operations";

    String page = "page";
    String pagination = "pagination";
    String paginationHal = "pagination-hal";
    String paginationPfBack = "paginationpfBack";
    String paginationPfForward = "paginationpfForward";
    String paginationPfItemsCurrent = "pagination-pf-items-current";
    String paginationPfItemsTotal = "pagination-pf-items-total";
    String paginationPfPage = "pagination-pf-page";
    String paginationPfPages = "pagination-pf-pages";
    String paginationPfPagesize = "pagination-pf-pagesize";
    String panel = "panel";
    String panelBody = "panel-body";
    String panelCollapse = "panel-collapse";
    String panelDefault = "panel-default";
    String panelGroup = "panel-group";
    String panelHeading = "panel-heading";
    String panelTitle = "panel-title";
    String paused = "paused";
    String pin = "pin";
    String pinned = "pinned";
    String pinnable = "pinnable";
    String prettyPrint = "prettyprint";
    String preview = "preview";
    String productVersionsPf = "product-versions-pf";
    String progress = "progress";
    String progressBar = "progress-bar";
    String progressBarDanger = "progress-bar-danger";
    String progressBarRemaining = "progress-bar-remaining";
    String progressBarStriped = "progress-bar-striped";
    String progressBarSuccess = "progress-bar-success";
    String progressBarWarning = "progress-bar-warning";
    String progressContainer = "progress-container";
    String progressDescription = "progress-description";
    String progressDescriptionLeft = "progress-description-left ";
    String progressLabelLeft = "progress-label-left";
    String progressLabelRight = "progress-label-right";
    String progressLabelTopRight = "progress-label-top-right";
    String progressSm = "progress-sm";
    String progressXs = "progress-xs";
    String properties = "properties";
    String pullLeft = "pull-left";
    String pullRight = "pull-right";
    String pulse = "pulse";

    String radio = "radio";
    String radioInline = "radio-inline";
    String rbacHidden = "rbac-hidden";
    String readonly = "readonly";
    String recording = "recording";
    String refresh = "refresh";
    String restResources = "rest-resources";
    String restricted = "restricted";
    String row = "row";
    String rowHeader = "row-header";

    String searchPfInputGroup = "search-pf-input-group";
    String secondaryCollapseTogglePf = "secondary-collapse-toggle-pf";
    String secondaryNavItemPf = "secondary-nav-item-pf";
    String secondaryVisiblePf = "secondary-visible-pf";
    String selectCheckbox = "select-checkbox"; // used in DataTables for the checkbox column
    String selected = "selected";
    String selectpicker = "selectpicker";
    String separator = "separator";
    String server = "server";
    String servers = "servers";
    String serverGroupContainer = "server-group-container";
    String smallLink = "small-link";
    String spinner = "spinner";
    String spinnerLg = "spinner-lg";
    String srOnly = "sr-only";
    String standalone = "standalone";
    String static_ = "static";
    String stopCircleO = "stop-circle-o";
    String subtitle = "subtitle";
    String suspended = "suspended";

    String tabContent = "tab-content";
    String tabPane = "tab-pane";
    String table = "table";
    String tableBordered = "table-bordered";
    String tableHover = "table-hover";
    String tableStriped = "table-striped";
    String tableViewHalActions = "table-view-hal-actions";
    String tableViewHalBtn = "table-view-hal-btn";
    String tagManagerContainer = "tag-manager-container";
    String tagManagerTag = "tag-manager-tag";
    String tags = "tags";
    String text = "text";
    String text1 = "text-1";
    String text2 = "text-2";
    String text3 = "text-3";
    String textCenter = "text-center";
    String textRight = "text-right";
    String time = "time";
    String timestamp = "timestamp";
    String tmTag = "tm-tag";
    String tmTagRemove = "tm-tag-remove";
    String toastNotificationsListPf = "toast-notifications-list-pf";
    String toastPfAction = "toast-pf-action";
    String tool = "tool";
    String toolbarPf = "toolbar-pf";
    String toolbarPfActionRight = "toolbar-pf-action-right";
    String toolbarPfActions = "toolbar-pf-actions";
    String toolbarPfFilter = "toolbar-pf-filter";
    String toolbarPfResults = "toolbar-pf-results";
    String topology = "topology";
    String treeContainer = "tree-container";

    String underline = "underline";
    String unpin = "unpin";
    String unpinned = "unpinned";
    String unread = "unread";
    String upload = "upload";
    String uploadAdvanced = "upload-advanced";
    String uploadIcon = "upload-icon";
    String uploadFile = "upload-file";
    String userDropdown = "user-dropdown";

    String value = "value";
    String valueDropdown = "value-dropdown";

    String warning = "warning";
    String warningTriangleO = "warning-triangle-o";
    String withProgress = "with-progress";
    String wizardHalErrorText = "wizard-hal-error-text";
    String wizardHalNoSidebar = "wizard-hal-no-sidebar";
    String wizardPf = "wizard-pf";
    String wizardPfBody = "wizard-pf-body";
    String wizardPfComplete = "wizard-pf-complete";
    String wizardPfContents = "wizard-pf-contents";
    String wizardPfErrorIcon = "wizard-pf-error-icon";
    String wizardPfFooter = "wizard-pf-footer";
    String wizardPfMain = "wizard-pf-main";
    String wizardPfProcess = "wizard-pf-process";
    String wizardPfRow = "wizard-pf-row";
    String wizardPfStepNumber = "wizard-pf-step-number";
    String wizardPfStepTitle = "wizard-pf-step-title";
    String wizardPfStep = "wizard-pf-step";
    String wizardPfSteps = "wizard-pf-steps";
    String wizardPfStepsIndicator = "wizard-pf-steps-indicator";
    String wizardPfSuccessIcon = "wizard-pf-success-icon";
    String wrap = "wrap";

    String DASH = "-";
    String SPACE = " ";

    static String column(int columns, String... sizes) {
        if (sizes != null && sizes.length != 0) {
            return Arrays.stream(sizes)
                    .map(size -> column + DASH + size + DASH + columns)
                    .collect(joining(SPACE));
        } else {
            return column + DASH + columnXs + DASH + columns + SPACE +
                    column + DASH + columnSm + DASH + columns + SPACE +
                    column + DASH + columnMd + DASH + columns + SPACE +
                    column + DASH + columnLg + DASH + columns;
        }
    }

    static String offset(int columns, String... sizes) {
        if (sizes != null && sizes.length != 0) {
            return Arrays.stream(sizes)
                    .map(size -> column + DASH + size + DASH + offset + DASH + columns)
                    .collect(joining(SPACE));
        } else {
            return column + DASH + columnXs + DASH + offset + DASH + columns + SPACE +
                    column + DASH + columnSm + DASH + offset + DASH + columns + SPACE +
                    column + DASH + columnMd + DASH + offset + DASH + columns + SPACE +
                    column + DASH + columnLg + DASH + offset + DASH + columns;
        }
    }

    static HeightUnionType vh(int offset) {
        return height("calc(100vh - " + offset + "px)"); //NON-NLS
    }

    static HeightUnionType height(Object height) {
        return HeightUnionType.of(height);
    }

    static WidthUnionType width(Object height) {
        return WidthUnionType.of(height);
    }

    static String px(int value) {
        return value + "px"; //NON-NLS
    }

    static String px(double value) {
        return value + "px"; //NON-NLS
    }

    /**
     * Builds a FontAwesome icons class.
     *
     * @param name the name of the FontAwesome icon <strong>w/o</strong> the "fa fa-" prefix.
     */
    static String fontAwesome(String name) {
        return fontAwesome(name, null);
    }

    /**
     * Builds a FontAwesome icons class.
     *
     * @param name the name of the FontAwesome icon <strong>w/o</strong> the "fa fa-" prefix.
     */
    static String fontAwesome(String name, FontAwesomeSize size) {
        String css = "fa fa-" + name;
        if (size != null) {
            css += " fa-" + size.size();
        }
        return css;
    }

    /**
     * Builds a PatternFly icon class.
     *
     * @param name the name of the PatternFly icon <strong>w/o</strong> the "pficon pficon-" prefix.
     */
    static String pfIcon(String name) {
        return "pficon pficon-" + name; //NON-NLS
    }

    /**
     * Builds a Glyphicon icon class.
     *
     * @param name the name of the Glyphicon icon <strong>w/o</strong> the "glyphicon glyphicon-" prefix.
     */
    static String glyphicon(String name) {
        return "glyphicon glyphicon-" + name; //NON-NLS
    }
}
