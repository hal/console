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
package org.jboss.hal.ballroom.chart;

import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import elemental2.dom.HTMLElement;
import org.jboss.gwt.elemento.core.IsElement;
import org.jboss.hal.ballroom.Tooltip;
import org.jboss.hal.resources.CSS;
import org.jboss.hal.resources.Messages;
import org.jboss.hal.resources.Names;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.jboss.gwt.elemento.core.Elements.div;
import static org.jboss.gwt.elemento.core.Elements.span;
import static org.jboss.hal.resources.CSS.progress;
import static org.jboss.hal.resources.CSS.progressBar;
import static org.jboss.hal.resources.CSS.progressBarDanger;
import static org.jboss.hal.resources.CSS.progressBarRemaining;
import static org.jboss.hal.resources.CSS.progressBarSuccess;
import static org.jboss.hal.resources.CSS.progressBarWarning;
import static org.jboss.hal.resources.CSS.progressContainer;
import static org.jboss.hal.resources.CSS.progressDescription;
import static org.jboss.hal.resources.CSS.progressDescriptionLeft;
import static org.jboss.hal.resources.CSS.progressLabelRight;
import static org.jboss.hal.resources.CSS.progressLabelTopRight;
import static org.jboss.hal.resources.CSS.srOnly;
import static org.jboss.hal.resources.CSS.width;
import static org.jboss.hal.resources.UIConstants.PROGRESSBAR;
import static org.jboss.hal.resources.UIConstants.ROLE;
import static org.jboss.hal.resources.UIConstants.TOGGLE;
import static org.jboss.hal.resources.UIConstants.TOOLTIP;

/**
 * The Utilization chart can be used to show a visual relationship between the total available amount of a resource, and
 * the percentage of that resource that has been utilized.
 *
 * @see <a href="https://www.patternfly.org/pattern-library/data-visualization/utilization-bar-chart/">https://www.patternfly.org/pattern-library/data-visualization/utilization-bar-chart/</a>
 */
public class Utilization implements IsElement {

    private static final String VALUE_MIN = "valuemin";
    private static final String VALUE_MAX = "valuemax";
    private static final String VALUE_NOW = "valuenow";

    private static final Messages MESSAGES = GWT.create(Messages.class);
    private static final Logger logger = LoggerFactory.getLogger(Utilization.class);

    private final String unit;
    private final boolean thresholds;
    private final HTMLElement valueBar;
    private final HTMLElement valueElement;
    private final HTMLElement remainingBar;
    private final HTMLElement remainingElement;
    private final HTMLElement root;
    private long total;

    public Utilization(String label, String unit, boolean inline, boolean thresholds) {
        this.unit = unit;
        this.thresholds = thresholds;
        this.total = 0;

        String[] containerCss = inline
                ? new String[]{progressContainer, progressDescriptionLeft, progressLabelRight}
                : new String[]{progressContainer};
        String[] progressCss = inline
                ? new String[]{progress}
                : new String[]{progress, progressLabelTopRight};

        root = div().css(containerCss)
                .add(div().css(progressDescription)
                        .title(label)
                        .textContent(label))
                .add(div().css(progressCss)
                        .add(valueBar = div().css(progressBar)
                                .title(Names.NOT_AVAILABLE)
                                .attr(ROLE, PROGRESSBAR)
                                .aria(VALUE_MIN, "0")
                                .aria(VALUE_NOW, "0")
                                .aria(VALUE_MAX, "0")
                                .data(TOGGLE, TOOLTIP)
                                .add(valueElement = span().element()).element())
                        .add(remainingBar = div().css(progressBar, progressBarRemaining)
                                .title(Names.NOT_AVAILABLE)
                                .add(remainingElement = span().css(srOnly).element()).element())).element();

        if (inline) {
            valueElement.title = unit;
        }
    }

    @Override
    public HTMLElement element() {
        return root;
    }

    public void update(long current) {
        update(current, this.total);
    }

    public void update(long current, long total) {
        if (current > total) {
            logger.error("Invalid values for utilization bar chart: current > total ({} > {})", current, total);
        }
        this.total = total;
        double currentPercent = Math.round(((double) current) / ((double) total) * 100.0);
        long remaining = total - current;
        double remainingPercent = 100.0 - currentPercent;

        valueBar.setAttribute(aria(VALUE_NOW), String.valueOf(current));
        valueBar.setAttribute(aria(VALUE_MAX), String.valueOf(total));
        valueBar.style.width = width(currentPercent + "%");
        Tooltip.element(valueBar).setTitle(MESSAGES.used(currentPercent));
        //noinspection HardCodedStringLiteral
        valueElement.innerHTML = new SafeHtmlBuilder()
                .appendHtmlConstant("<strong>")
                .appendEscaped(MESSAGES.currentOfTotal(current, total))
                .appendHtmlConstant("</strong>")
                .appendEscaped(" " + unit)
                .toSafeHtml().asString();

        remainingBar.setAttribute(aria(VALUE_NOW), String.valueOf(remaining));
        remainingBar.setAttribute(aria(VALUE_MAX), String.valueOf(total));
        remainingBar.style.width = width(remainingPercent + "%");
        Tooltip.element(remainingBar).setTitle(MESSAGES.available(remainingPercent));
        remainingElement.textContent = MESSAGES.available(remainingPercent);

        if (thresholds) {
            valueBar.classList.remove(progressBarDanger);
            valueBar.classList.remove(progressBarWarning);
            valueBar.classList.remove(progressBarSuccess);
            if (currentPercent > 90) {
                valueBar.classList.add(progressBarDanger);
            } else if (currentPercent > 75) {
                valueBar.classList.add(progressBarWarning);
            } else {
                valueBar.classList.add(progressBarSuccess);
            }
        }
    }

    private String aria(String name) {
        return "aria-" + name; //NON-NLS
    }

    public void setDisabled(boolean disabled) {
        if (disabled) {
            root.classList.add(CSS.disabled);
        } else {
            root.classList.remove(CSS.disabled);
        }
    }
}
