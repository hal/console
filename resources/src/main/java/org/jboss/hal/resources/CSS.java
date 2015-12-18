/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2010, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.hal.resources;

import org.jetbrains.annotations.NonNls;

/**
 * Contains common CSS classes from PatternFly & Bootstrap. The constants in this interface are not involved in any
 * kind of code generation or GWT magic. They're just here to have a them in one place.
 *
 * @author Harald Pehl
 */
public interface CSS {

    int labelColumns = 3;
    int inputColumns = 9;

    String active = "active";
    String alert = "alert";
    String alertDanger = "alert-danger";
    String btn = "btn";
    String btnDefault = "btn-default";
    String btnGroup = "btn-group";
    String btnPrimary = "btn-primary";
    String btnHal = "btn-hal";
    String clearfix = "clearfix";
    String clickable = "clickable";
    String close = "close";
    String column = "col";
    String columnMedium = "md";
    String collapse = "collapse";
    String containerFluid = "container-fluid";
    String controlLabel = "control-label";
    String dataTable = "datatable";
    String eapHomeRow = "eap-home-row";
    String eapHomeTitle = "eap-home-title";
    String editButtons = "edit-buttons";
    String fade = "fade";
    String form = "form";
    String formButtons = "form-buttons";
    String formControl = "form-control";
    String formControlStatic = "form-control-static";
    String formGroup = "form-group";
    String formHelpContent = "form-help-content";
    String formHorizontal = "form-horizontal";
    String formLinkLabel = "form-link-label";
    String formLinks = "form-links";
    String hasError = "has-error";
    String hasFeedback = "has-feedback";
    String helpBlock = "help-block";
    String hover = "hover";
    String in = "in";
    String inputGroup = "input-group";
    String inputGroupAddon = "input-group-addon";
    String inputGroupBtn = "input-group-btn";
    String loading = "loading";
    String loadingContainer = "loading-container";
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
    String offset = "offset";
    String progress = "progress";
    String progressBar = "progress-bar";
    String progressBarStriped = "progress-bar-striped";
    String progressXs = "progress-xs";
    String pullRight = "pull-right";
    String row = "row";
    String selectpicker = "selectpicker";
    String spinner = "spinner";
    String srOnly = "sr-only";
    String table = "table";
    String tableBordered = "table-bordered";
    String tableStriped = "table-striped";
    String tooltip = "tooltip";


    static String column(int columns) {
        return column + "-" + columnMedium + "-" + String.valueOf(columns);
    }

    static String offset(int columns) {
        return column + "-" + columnMedium + "-" + offset + "-" + String.valueOf(columns);
    }

    static String fontAwesome(@NonNls String name) {
        return "fa fa-" + name; //NON-NLS
    }

    static String pfIcon(@NonNls String name) {
        return "pficon pficon-" + name; //NON-NLS
    }
}
