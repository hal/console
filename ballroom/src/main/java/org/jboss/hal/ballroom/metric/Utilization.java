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
package org.jboss.hal.ballroom.metric;

import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import elemental.dom.Element;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.gwt.elemento.core.IsElement;
import org.jboss.hal.ballroom.Tooltip;
import org.jboss.hal.resources.CSS;
import org.jboss.hal.resources.Messages;
import org.jboss.hal.resources.Names;
import org.jetbrains.annotations.NonNls;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static elemental.css.CSSStyleDeclaration.Unit.PCT;
import static org.jboss.hal.resources.CSS.*;
import static org.jboss.hal.resources.UIConstants.PROGRESSBAR;
import static org.jboss.hal.resources.UIConstants.ROLE;
import static org.jboss.hal.resources.UIConstants.TOGGLE;
import static org.jboss.hal.resources.UIConstants.TOOLTIP;

/**
 * @author Harald Pehl
 */
public class Utilization implements IsElement {

    private static final String VALUE_BAR = "value-progress-bar";
    private static final String VALUE_ELEMENT = "value-element";
    private static final String REMAINING_BAR = "remaining-progress-bar";
    private static final String REMAINING_ELEMENT = "remaining-element";

    private static final String VALUE_MIN = "valuemin";
    private static final String VALUE_MAX = "valuemax";
    private static final String VALUE_NOW = "valuenow";

    private static final Messages MESSAGES = GWT.create(Messages.class);
    @NonNls private static final Logger logger = LoggerFactory.getLogger(Utilization.class);

    private final String unit;
    private final boolean thresholds;
    private final Element valueBar;
    private final Element valueElement;
    private final Element remainingBar;
    private final Element remainingElement;
    private final Element root;
    private long total;

    public Utilization(final String label, final String unit, boolean inline, boolean thresholds) {
        this.unit = unit;
        this.thresholds = thresholds;
        this.total = 0;

        // @formatter:off
        String[] containerCss = inline ? new String[]{progressDescriptionLeft, progressLabelRight} : null;
        String[] progressCss = inline ? null : new String[]{progressLabelTopRight};
        Elements.Builder builder = new Elements.Builder()
            .div().css(progressContainer, containerCss)
                .div().css(progressDescription).title(label).textContent(label).end()
                .div().css(progress, progressCss)
                    .div().rememberAs(VALUE_BAR).css(progressBar).title(Names.NOT_AVAILABLE)
                            .attr(ROLE, PROGRESSBAR)
                            .aria(VALUE_MIN, "0")
                            .aria(VALUE_NOW, "0")
                            .aria(VALUE_MAX, "0")
                            .data(TOGGLE, TOOLTIP)
                        .span().rememberAs(VALUE_ELEMENT).end()
                    .end()
                    .div().rememberAs(REMAINING_BAR).css(progressBar, progressBarRemaining).title(Names.NOT_AVAILABLE)
                            .attr(ROLE, PROGRESSBAR)
                            .aria(VALUE_MIN, "0")
                            .aria(VALUE_NOW, "0")
                            .aria(VALUE_MAX, "0")
                            .data(TOGGLE, TOOLTIP)
                        .span().rememberAs(REMAINING_ELEMENT).css(srOnly).end()
                    .end()
                .end()
            .end();
        // @formatter:on

        this.valueBar = builder.referenceFor(VALUE_BAR);
        this.valueElement = builder.referenceFor(VALUE_ELEMENT);
        this.remainingBar = builder.referenceFor(REMAINING_BAR);
        this.remainingElement = builder.referenceFor(REMAINING_ELEMENT);
        this.root = builder.build();
    }

    @Override
    public Element asElement() {
        return root;
    }

    public void update(long current) {
        update(current, this.total);
    }

    public void update(long current, long total) {
        if (current <= total) {
            this.total = total;
            double currentPercent = Math.round(((double) current) / ((double) total) * 100.0);
            long remaining = total - current;
            double remainingPercent = 100.0 - currentPercent;

            valueBar.setAttribute(aria(VALUE_NOW), String.valueOf(current));
            valueBar.setAttribute(aria(VALUE_MAX), String.valueOf(total));
            valueBar.getStyle().setWidth(currentPercent, PCT);
            Tooltip.element(valueBar).setTitle(MESSAGES.used(currentPercent));
            //noinspection HardCodedStringLiteral
            valueElement.setInnerHTML(new SafeHtmlBuilder()
                    .appendHtmlConstant("<strong>")
                    .appendEscaped(MESSAGES.currentOfTottal(current, total))
                    .appendHtmlConstant("</strong>")
                    .appendEscaped(" " + unit)
                    .toSafeHtml().asString());

            remainingBar.setAttribute(aria(VALUE_NOW), String.valueOf(remaining));
            remainingBar.setAttribute(aria(VALUE_MAX), String.valueOf(total));
            remainingBar.getStyle().setWidth(remainingPercent, PCT);
            Tooltip.element(remainingBar).setTitle(MESSAGES.available(remainingPercent));
            remainingElement.setTextContent(MESSAGES.available(remainingPercent));

            if (thresholds) {
                valueBar.getClassList().remove(progressBarDanger);
                valueBar.getClassList().remove(progressBarWarning);
                valueBar.getClassList().remove(progressBarSuccess);
                if (currentPercent > 90) {
                    valueBar.getClassList().add(progressBarDanger);
                } else if (currentPercent > 75) {
                    valueBar.getClassList().add(progressBarWarning);
                } else {
                    valueBar.getClassList().add(progressBarSuccess);
                }
            }

        } else {
            logger.error("Invalid values for utilization bar chart: current > total ({} > {})", current, total);
        }
    }

    private String aria(String name) {
        return "aria-" + name; //NON-NLS
    }

    public void setDisabled(boolean disabled) {
        if (disabled) {
            root.getClassList().add(CSS.disabled);
        } else {
            root.getClassList().remove(CSS.disabled);
        }
    }
}
