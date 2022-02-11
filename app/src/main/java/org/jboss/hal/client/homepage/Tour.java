/*
 *  Copyright 2022 Red Hat
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.jboss.hal.client.homepage;

import java.util.ArrayList;
import java.util.List;

import org.jboss.hal.ballroom.Popover;
import org.jboss.hal.ballroom.Popover.Placement;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.resources.UIConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.event.shared.HandlerRegistration;
import com.gwtplatform.mvp.client.proxy.NavigationEvent;
import com.gwtplatform.mvp.client.proxy.NavigationHandler;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;

import elemental2.dom.Element;
import elemental2.dom.HTMLButtonElement;

import static elemental2.dom.DomGlobal.clearInterval;
import static elemental2.dom.DomGlobal.document;
import static elemental2.dom.DomGlobal.setInterval;
import static org.jboss.gwt.elemento.core.Elements.setVisible;
import static org.jboss.hal.ballroom.Popover.Trigger.MANUAL;
import static org.jboss.hal.resources.CSS.disabled;
import static org.jboss.hal.resources.CSS.pfIcon;

class Tour implements NavigationHandler {

    private static final int MAX_TRIES = 50;
    private static final Logger logger = LoggerFactory.getLogger(Tour.class);

    private final EventBus eventBus;
    private final PlaceManager placeManager;
    private final Resources resources;
    private final List<Step> steps;
    private int index;
    private int tries;
    private double handle;
    private Step currentStep;
    private boolean pendingNavigation;
    private HandlerRegistration registration;

    Tour(EventBus eventBus, PlaceManager placeManager, Resources resources) {
        this.eventBus = eventBus;
        this.placeManager = placeManager;
        this.resources = resources;
        this.steps = new ArrayList<>();
        reset();
    }

    @Override
    public void onNavigation(NavigationEvent navigationEvent) {
        if (pendingNavigation && currentStep != null) {
            tries = 0;
            logger.debug("Tour navigation finished");
            handle = setInterval(any -> {
                Element element = document.getElementById(currentStep.id);
                if (element != null) {
                    logger.debug("Found step {}", currentStep);
                    currentStep.show();
                    pendingNavigation = false;
                    clearInterval(handle);
                }
                if (tries == MAX_TRIES) {
                    logger.error("Unable to find step {}", currentStep);
                    pendingNavigation = false;
                    clearInterval(handle);
                    currentStep.close();
                    reset();
                }
                tries++;
            }, UIConstants.MEDIUM_TIMEOUT);
        }
    }

    void addStep(PlaceRequest placeRequest, String id, String title, SafeHtml content, Placement placement) {
        steps.add(new Step(placeRequest, id, title, content, placement));
    }

    void start() {
        if (!steps.isEmpty()) {
            logger.info("Start tour");
            registration = eventBus.addHandler(NavigationEvent.getType(), this);
            next();
        }
    }

    boolean running() {
        return currentStep != null;
    }

    private void previous() {
        if (!steps.isEmpty() && index > 0 && index < steps.size()) {
            if (currentStep != null) {
                currentStep.close();
            }
            index--;
            navigate();
        }
    }

    private void next() {
        if (!steps.isEmpty() && index >= -1 && index < steps.size() - 1) {
            if (currentStep != null) {
                currentStep.close();
            }
            index++;
            navigate();

        } else if (index == steps.size() - 1) {
            // we're done
            if (currentStep != null) {
                currentStep.close();
                reset();
            }
        }
    }

    private void navigate() {
        boolean switchPlace = false;
        Step oldStep = currentStep;
        currentStep = steps.get(index);
        if (oldStep != null) {
            switchPlace = !oldStep.placeRequest.equals(currentStep.placeRequest);
        }
        logger.debug("Tour navigation from {} to {}: index: {}, switch place: {}",
                oldStep != null ? oldStep : "n/a", currentStep, index, switchPlace);
        if (switchPlace) {
            pendingNavigation = true;
            placeManager.revealPlace(currentStep.placeRequest);
            // currentStep.show() will be called from the event handler
        } else {
            currentStep.show();
        }
    }

    private void reset() {
        logger.debug("Reset tour");
        index = -1;
        currentStep = null;
        pendingNavigation = false;
        if (registration != null) {
            registration.removeHandler();
            registration = null;
        }
    }

    private static final String CLOSE_BUTTON = "<button type=\"button\" class=\"close\" aria-hidden=\"true\">" +
            "<span class=\"" + pfIcon("close") + "\"></span>" +
            "</button>";
    private static final SafeHtml POPOVER_TEMPLATE = SafeHtmlUtils.fromSafeConstant(
            "<div class=\"popover\" role=\"tooltip\">" +
                    "<div class=\"arrow\"></div>" +
                    "<h3 class=\"popover-title closable\"></h3>" +
                    "<div class=\"popover-content\"></div>" +
                    "</div>");
    private final ButtonsTemplate BUTTONS_TEMPLATE = GWT.create(ButtonsTemplate.class);

    interface ButtonsTemplate extends SafeHtmlTemplates {

        @Template("<div class=\"tour-buttons\">" +
                "<button type=\"button\" id=\"{0}\" class=\"btn btn-default\">" +
                "<span class=\"fa fa-angle-left\"></span> {1}" +
                "</button>" +
                "<button type=\"submit\" id=\"{2}\" class=\"btn btn-primary\">" +
                "{3} <span class=\"fa fa-angle-right\"></span>" +
                "</button>" +
                "<button type=\"submit\" id=\"{4}\" class=\"btn btn-primary\">{5}</button>" +
                "</div>")
        SafeHtml buttons(String backId, String back, String nextId, String next, String doneId, String done);
    }

    private class Step {

        private final PlaceRequest placeRequest;
        private final String id;
        private final String title;
        private final SafeHtml content;
        private final Placement placement;
        private Popover popover;

        Step(PlaceRequest placeRequest, String id, String title, SafeHtml content, Placement placement) {
            this.placeRequest = placeRequest;
            this.id = id;
            this.title = title;
            this.content = content;
            this.placement = placement;
        }

        void show() {
            String fullTitle = title + CLOSE_BUTTON;
            SafeHtml buttons = BUTTONS_TEMPLATE.buttons(Ids.TOUR_BUTTON_BACK, resources.constants().back(),
                    Ids.TOUR_BUTTON_NEXT, resources.constants().next(),
                    Ids.TOUR_BUTTON_DONE, resources.constants().finish());
            SafeHtml fullContent = new SafeHtmlBuilder()
                    .append(content)
                    .append(buttons)
                    .toSafeHtml();

            popover = new Popover.Builder("#" + id, fullTitle, fullContent)
                    .placement(placement)
                    .trigger(MANUAL)
                    .template(POPOVER_TEMPLATE)
                    .build();
            popover.onInserted(() -> {
                Element closeIcon = document.querySelector(".popover .close");
                closeIcon.onclick = e -> {
                    close();
                    reset();
                    return null;
                };
                HTMLButtonElement backButton = (HTMLButtonElement) document.getElementById(Ids.TOUR_BUTTON_BACK);
                backButton.onclick = e -> {
                    previous();
                    return null;
                };
                HTMLButtonElement nextButton = (HTMLButtonElement) document.getElementById(Ids.TOUR_BUTTON_NEXT);
                nextButton.onclick = e -> {
                    next();
                    return null;
                };
                HTMLButtonElement doneButton = (HTMLButtonElement) document.getElementById(Ids.TOUR_BUTTON_DONE);
                doneButton.onclick = e -> {
                    next();
                    return null;
                };

                boolean first = index == 0;
                boolean last = index == steps.size() - 1;
                if (first) {
                    backButton.disabled = true;
                    backButton.classList.add(disabled);
                } else {
                    backButton.disabled = false;
                    backButton.classList.remove(disabled);
                }
                setVisible(nextButton, !last);
                setVisible(doneButton, last);
            });
            popover.show();
        }

        void close() {
            if (popover != null) {
                popover.destroy();
                Element element = document.querySelector(".popover");
                if (element != null) {
                    element.remove();
                }
                logger.debug("Close step {}", id);
                popover = null;
            }
        }

        @Override
        public String toString() {
            return id;
        }
    }
}
