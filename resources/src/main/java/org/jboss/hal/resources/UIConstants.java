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

/**
 * UI related constants used in more than one place.
 *
 * @author Harald Pehl
 */
public interface UIConstants {

    /**
     * The time in milliseconds after one modal dialog is closed and the next one is shown.
     */
    int SHORT_TIMEOUT = 111;

    /**
     * The timeout for long running operations / actions before some kind of loading indicator is shown
     */
    int MEDIUM_TIMEOUT = 333;

    /**
     * The timeout until a non-sticky message is hidden
     */
    int MESSAGE_TIMEOUT = 6000; // ms

    long RELOAD_MESSAGE_ID = 5102007;
    long RESTART_MESSAGE_ID = 1102010;
    long DOMAIN_CHANGED_MESSAGE_ID = 291973;


    String ARIA_DESCRIBEDBY = "aria-describedby";
    String ALERT = "alert";
    String BODY = "body";
    String BUTTON = "button";
    String CHECKED = "checked";
    String COLLAPSE = "collapse";
    String CONTAINER = "container";
    String CONTROLS = "controls";
    String DIALOG = "dialog";
    String DISMISS = "dismiss";
    String DROPDOWN = "dropdown";
    String DOWNLOAD = "download";
    String EXPANDED = "expanded";
    String FALSE = "false";
    String FOR = "for";
    String GROUP = "group";
    String HAS_POPUP = "haspopup";
    String HIDDEN_MODAL = "hidden.bs.modal";
    String HREF = "href";
    String HIDDEN = "hidden";
    String LABEL = "label";
    String LABELLED_BY = "labelledby";
    String MENU = "menu";
    String MENUITEM = "menuitem";
    String NAME = "name";
    String NBSP = "&nbsp;";
    String OBJECT = "Object";
    String PLACEHOLDER = "placeholder";
    String PLACEMENT = "placement";
    String PRESENTATION = "presentation";
    String PROGRESSBAR = "progressbar";
    String READONLY = "readonly";
    String ROLE = "role";
    String SHOWN_MODAL = "shown.bs.modal";
    String TABINDEX = "tabindex";
    String TABLIST = "tablist";
    String TARGET = "target";
    String TOGGLE = "toggle";
    String TOP = "top";
    String TOOLTIP = "tooltip";
    String TRUE = "true";
    String VALUE = "value";
}
