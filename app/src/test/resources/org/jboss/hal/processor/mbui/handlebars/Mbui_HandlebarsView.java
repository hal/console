package org.jboss.hal.processor.mbui.handlebars;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Generated;
import javax.inject.Inject;

import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import elemental2.dom.HTMLElement;
import org.jboss.hal.ballroom.ExpressionUtil;
import org.jboss.hal.core.mbui.MbuiContext;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.Metadata;

import static org.jboss.gwt.elemento.core.Elements.div;
import static org.jboss.hal.ballroom.LayoutBuilder.column;
import static org.jboss.hal.ballroom.LayoutBuilder.row;

/*
 * WARNING! This class is generated. Do not modify.
 */
@Generated("org.jboss.hal.processor.mbui.MbuiViewProcessor")
public final class Mbui_HandlebarsView extends HandlebarsView {

    private final Metadata metadata0;
    private final Map<String, HTMLElement> expressionElements;

    @Inject
    @SuppressWarnings("unchecked")
    public Mbui_HandlebarsView(MbuiContext mbuiContext) {
        super(mbuiContext);

        AddressTemplate metadata0Template = AddressTemplate.of("/subsystem=foo");
        this.metadata0 = mbuiContext.metadataRegistry().lookup(metadata0Template);
        this.expressionElements = new HashMap<>();

        HTMLElement html0;
        HTMLElement root = row()
                .add(column()
                        .add(html0 = div()
                                .innerHtml(SafeHtmlUtils.fromSafeConstant("<h1>Handlebars</h1><p>Current time: ${org.jboss.hal.ballroom.Format.shortDateTime(new java.util.Date())}</p>"))
                                .get())
                )
                .get();
        expressionElements.put("html0", html0);

        initElement(root);
    }

    @Override
    public void attach() {
        super.attach();

        ExpressionUtil.replaceExpression(expressionElements.get("html0"), "${org.jboss.hal.ballroom.Format.shortDateTime(new java.util.Date())}", String.valueOf(org.jboss.hal.ballroom.Format.shortDateTime(new java.util.Date())));
    }
}