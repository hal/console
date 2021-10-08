package org.jboss.hal.ballroom;

import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import elemental2.dom.HTMLElement;

import static org.jboss.hal.ballroom.Popover.Trigger.FOCUS;
import static org.jboss.hal.resources.CSS.pfIcon;

/**
 *  Creates version of Popover which is used for showing help texts for form items.
 *  HelpPopover should look the same as Popover, but its parameters can be adjusted for longer texts.
 */
public class HelpPopover {

    private static final String CLOSE_BUTTON = "<button class=\"close\" aria-hidden=\"true\" tabindex=\"-1\">" +
            "<span class=\"" + pfIcon("close") + "\"></span>" +
            "</button>";
    private static final SafeHtml POPOVER_TEMPLATE = SafeHtmlUtils.fromSafeConstant(
            "<div class=\"popover help-popover\" role=\"popover\">" +
                    "<div class=\"arrow\"></div>" +
                    "<h3 class=\"popover-title closable\"></h3>" +
                    "<div class=\"popover-content\"></div>" +
                    "</div>");

    /**
     * @param title Title of the HelpPopover
     * @param content Content of the HelpPopover
     * @param selector HTMLElement to which HelpPopover will be bound, e.g. a button
     */
    public HelpPopover(String title, SafeHtml content, HTMLElement selector) {
        String fullTitle = title + CLOSE_BUTTON;
        Popover popover = new Popover.Builder(selector, fullTitle, content)
                .placement(Popover.Placement.RIGHT)
                .trigger(FOCUS)
                .template(POPOVER_TEMPLATE)
                .build();
    }
}


