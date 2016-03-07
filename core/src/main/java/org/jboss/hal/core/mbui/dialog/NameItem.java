package org.jboss.hal.core.mbui.dialog;

import com.google.gwt.core.client.GWT;
import org.jboss.hal.ballroom.form.TextBoxItem;
import org.jboss.hal.dmr.ModelDescriptionConstants;
import org.jboss.hal.resources.Constants;

/**
 * A text box item useful for add resource dialogs. The form item has {@link ModelDescriptionConstants#NAME} as name,
 * is required and does not allow expressions.
 *
 * @author Harald Pehl
 */
public class NameItem extends TextBoxItem {

    private static final Constants CONSTANTS = GWT.create(Constants.class);

    public NameItem() {
        super(ModelDescriptionConstants.NAME, CONSTANTS.name());
        setRequired(true);
        setExpressionAllowed(false);
    }
}
