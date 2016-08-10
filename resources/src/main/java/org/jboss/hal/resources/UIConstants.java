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
     * The timeout for long running operations / actions before a loading indicator is shown
     */
    int PROGRESS_TIMEOUT = 333;

    /**
     * The time in milliseconds after one modal dialog is closed and the next one is shown.
     */
    int DIALOG_TIMEOUT = 111;

    String BUTTON = "button";
    String COLLAPSE = "collapse";
    String CONTROLS = "controls";
    String DROPDOWN = "dropdown";
    String DOWNLOAD = "download";
    String EXPANDED = "expanded";
    String GROUP = "group";
    String HAS_POPUP = "haspopup";
    String HREF = "href";
    String HIDDEN = "hidden";
    String LABELLED_BY = "labelledby";
    String MENU = "menu";
    String MENUITEM = "menuitem";
    String NBSP = "&nbsp;";
    String OBJECT = "Object";
    String PLACEMENT = "placement";
    String PRESENTATION = "presentation";
    String PROGRESSBAR = "progressbar";
    String ROLE = "role";
    String TABINDEX = "tabindex";
    String TABLIST = "tablist";
    String TARGET = "target";
    String TOGGLE = "toggle";
    String TOOLTIP = "tooltip";
}
