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

    String controlLabel = "control-label";
    String inputGroup = "input-group";
    String formGroup = "form-group";
    String inputGroupBtn = "input-group-btn";
    String helpBlock = "help-block";
    String btn = "btn";
    String btnDefault = "btn-default";
    String btnHal = "btn-Hal";
    String formControl = "form-control";
    String inputGroupAddon = "input-group-addon";
    String hasError = "has-error";
    String hasFeedback = "has-feedback";
    String modelSmall = "modal-sm";
    String modalMedium = "modal-md";
    String modalLarge = "modal-lg";
    String modalMax = "modal-mx";
    String eapHomeTitle = "eap-home-title";
    String clickable = "clickable";
    String eapHomeRow = "eap-home-row";
    String containerFluid = "container-fluid";

    static String fontAwesome(@NonNls String name) {
        return "fa fa-" + name; //NON-NLS
    }
}
