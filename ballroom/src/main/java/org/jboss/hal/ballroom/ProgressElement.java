/*
 * JBoss, Home of Professional Open Source
 * Copyright 2013 Red Hat Inc. and/or its affiliates and other contributors
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
package org.jboss.hal.ballroom;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import elemental.dom.Element;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.gwt.elemento.core.IsElement;
import org.jboss.gwt.flow.Progress;

import static elemental.css.CSSStyleDeclaration.Unit.PCT;
import static elemental.css.CSSStyleDeclaration.Unit.PX;
import static java.lang.Math.min;
import static java.lang.Math.round;
import static org.jboss.hal.resources.CSS.*;
import static org.jboss.hal.resources.UIConstants.ROLE;

/**
 * @author Harald Pehl
 */
public class ProgressElement implements IsElement, Progress {

    private static final String ARIA_VALUENOW = "aria-valuenow";

    private int value;
    private int max;
    private boolean determinate;
    private final Element root;
    private final Element progressBarElement;

    public ProgressElement() {
        value = 0;
        max = 100;
        determinate = true;

        // @formatter:off
        Elements.Builder builder = new Elements.Builder()
            .div().css(progress, progressXs)
                .div().css(progressBar).rememberAs("progressBar")
                        .attr(ROLE, "progress-bar") //NON-NLS
                        .aria("valuenow", "0")
                        .aria("valuemin", "0")
                        .aria("valuemax", "100")
                    .span().css(srOnly).innerHtml(SafeHtmlUtils.EMPTY_SAFE_HTML).end()
                .end()
            .end();
        // @formatter:on

        root = builder.build();
        progressBarElement = builder.referenceFor("progressBar");
        Elements.setVisible(root, false);
    }

    @Override
    public Element asElement() {
        return root;
    }

    @Override
    public void reset() {
        reset(0);
    }

    @Override
    public void reset(final int mx) {
        value = 0;
        max = mx;
        determinate = max > 1; // if there's just one step, choose none-determinate state

        if (determinate) {
            progressBarElement.getClassList().remove(progressBarStriped);
            progressBarElement.getClassList().remove(active);
            progressBarElement.setAttribute(ARIA_VALUENOW, "0");
            progressBarElement.getStyle().setWidth(0, PX);
        } else {
            progressBarElement.getClassList().add(progressBarStriped);
            progressBarElement.getClassList().add(active);
            progressBarElement.setAttribute(ARIA_VALUENOW, "100");
            progressBarElement.getStyle().setWidth(100.0, PCT);
        }
        Elements.setVisible(root, true);
    }

    @Override
    public void tick() {
        if (determinate) {
            if (value < max) {
                value++;
                double percent = min(round(((double) value / (double) max) * 100.0), 100.0);
                progressBarElement.setAttribute(ARIA_VALUENOW, String.valueOf(percent));
                progressBarElement.getStyle().setWidth(percent, PCT);
            }
        }
    }

    @Override
    public void finish() {
        // give the user a chance to see that we're finished
        Scheduler.get().scheduleFixedDelay(() -> {
            Elements.setVisible(root, false);
            return false;
        }, 333);
    }
}
