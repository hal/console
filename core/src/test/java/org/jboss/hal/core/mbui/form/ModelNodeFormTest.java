package org.jboss.hal.core.mbui.form;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.gwt.junit.GWTMockUtilities;
import org.jboss.hal.ballroom.form.ExistingModelStateMachine;
import org.jboss.hal.ballroom.form.AddOnlyStateMachine;
import org.jboss.hal.ballroom.form.FormItem;
import org.jboss.hal.ballroom.form.StateMachine;
import org.jboss.hal.ballroom.form.ViewOnlyStateMachine;
import org.jboss.hal.core.mbui.ResourceDescriptionBuilder;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.meta.description.ResourceDescription;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.Iterator;

import static org.jboss.hal.meta.security.SecurityContext.RWX;
import static org.junit.Assert.assertEquals;

/**
 * @author Harald Pehl
 */
public class ModelNodeFormTest {

    ResourceDescription attributes;
    ResourceDescription requestProperties;

    @Before
    public void setUp() {
        GWTMockUtilities.disarm();
        attributes = new ResourceDescriptionBuilder().attributes("foo", "bar", "baz", "qux");
        requestProperties = new ResourceDescriptionBuilder().requestProperties(
                ImmutableMap.of("foo", true, "bar", false, "baz", true, "qux", false));
    }


    // ------------------------------------------------------ test illegal states

    @Test(expected = IllegalStateException.class)
    public void viewAndEdit() {
        new ModelNodeForm.Builder("viewAndEdit", RWX, new ResourceDescriptionBuilder().empty())
                .viewOnly()
                .addOnly()
                .build();
    }

    @Test(expected = IllegalStateException.class)
    public void noAttributes() {
        new ModelNodeForm.Builder("noAttributes", RWX, new ResourceDescriptionBuilder().empty())
                .build();
    }

    @Test(expected = IllegalStateException.class)
    public void noRequestProperties() {
        new ModelNodeForm.Builder("noRequestProperties", RWX, new ResourceDescriptionBuilder().empty())
                .createResource()
                .build();
    }

    @Test(expected = IllegalStateException.class)
    public void viewAndCreateResource() {
        new ModelNodeForm.Builder("viewAndCreateResource", RWX,
                new ResourceDescriptionBuilder().empty())
                .viewOnly()
                .createResource()
                .build();
    }

    @Test(expected = IllegalStateException.class)
    public void excludeRequiredRequestProperty() {
        new ModelNodeForm.Builder("viewAndCreateResource", RWX,
                new ResourceDescriptionBuilder().requestProperties(ImmutableMap.of("foo", true)))
                .createResource()
                .exclude("foo")
                .build();
    }


    // ------------------------------------------------------ verify different state machines

    @Test
    public void createResourceStateMachine() {
        StateMachine stateMachine = new ModelNodeForm.Builder("createResourceStateMachine", RWX,
                new ResourceDescriptionBuilder().requestProperties(Collections.emptyMap()))
                .createResource()
                .stateMachine();
        Assert.assertTrue(stateMachine instanceof AddOnlyStateMachine);
    }

    @Test
    public void editOnlyStateMachine() {
        StateMachine stateMachine = new ModelNodeForm.Builder("editOnlyStateMachine",
                RWX,
                new ResourceDescriptionBuilder().attributes()).addOnly().stateMachine();
        Assert.assertTrue(stateMachine instanceof AddOnlyStateMachine);
    }

    @Test
    public void viewOnlyStateMachine() {
        StateMachine stateMachine = new ModelNodeForm.Builder("viewOnlyStateMachine",
                RWX,
                new ResourceDescriptionBuilder().attributes()).viewOnly().stateMachine();
        Assert.assertTrue(stateMachine instanceof ViewOnlyStateMachine);
    }

    @Test
    public void defaultStateMachine() {
        StateMachine stateMachine = new ModelNodeForm.Builder("defaultStateMachine",
                RWX,
                new ResourceDescriptionBuilder().attributes()).stateMachine();
        Assert.assertTrue(stateMachine instanceof ExistingModelStateMachine);
    }


    // ------------------------------------------------------ request properties

    @Test
    public void requestProperties() {
        ModelNodeForm<ModelNode> form = builder("requestProperties", requestProperties)
                .createResource()
                .build();
        Iterable<FormItem> formItems = form.getFormItems();
        Iterator<FormItem> iterator = formItems.iterator();

        assertEquals(2, Iterables.size(formItems));
        assertEquals("baz", iterator.next().getName());
        assertEquals("foo", iterator.next().getName());
    }

    @Test
    public void requestPropertiesUnsorted() {
        ModelNodeForm<ModelNode> form = builder("requestPropertiesUnsorted", requestProperties)
                .createResource()
                .unsorted()
                .build();
        Iterable<FormItem> formItems = form.getFormItems();
        Iterator<FormItem> iterator = formItems.iterator();

        assertEquals(2, Iterables.size(formItems));
        assertEquals("foo", iterator.next().getName());
        assertEquals("baz", iterator.next().getName());
    }

    @Test
    public void includeRequestProperties() {
        ModelNodeForm<ModelNode> form = builder("includeRequestProperties", requestProperties)
                .createResource()
                .include("foo", "bar")
                .build();
        Iterable<FormItem> formItems = form.getFormItems();
        Iterator<FormItem> iterator = formItems.iterator();

        assertEquals(3, Iterables.size(formItems));
        assertEquals("bar", iterator.next().getName());
        assertEquals("baz", iterator.next().getName());
        assertEquals("foo", iterator.next().getName());
    }


    // ------------------------------------------------------ attributes

    @Test
    public void attributes() {
        ModelNodeForm<ModelNode> form = builder("attributes", attributes)
                .build();
        Iterable<FormItem> formItems = form.getFormItems();
        Iterator<FormItem> iterator = formItems.iterator();

        assertEquals(4, Iterables.size(formItems));
        assertEquals("bar", iterator.next().getName());
        assertEquals("baz", iterator.next().getName());
        assertEquals("foo", iterator.next().getName());
        assertEquals("qux", iterator.next().getName());
    }

    @Test
    public void attributesUnsorted() {
        ModelNodeForm<ModelNode> form = builder("attributesUnsorted", attributes)
                .unsorted()
                .build();
        Iterable<FormItem> formItems = form.getFormItems();
        Iterator<FormItem> iterator = formItems.iterator();

        assertEquals(4, Iterables.size(formItems));
        assertEquals("foo", iterator.next().getName());
        assertEquals("bar", iterator.next().getName());
        assertEquals("baz", iterator.next().getName());
        assertEquals("qux", iterator.next().getName());
    }

    @Test
    public void includeAttributes() {
        ModelNodeForm<ModelNode> form = builder("includeAttributes", attributes)
                .include("foo", "bar")
                .build();
        Iterable<FormItem> formItems = form.getFormItems();
        Iterator<FormItem> iterator = formItems.iterator();

        assertEquals(2, Iterables.size(formItems));
        assertEquals("bar", iterator.next().getName());
        assertEquals("foo", iterator.next().getName());
    }

    @Test
    public void excludeAttributes() {
        ModelNodeForm<ModelNode> form = builder("includeAttributes", attributes)
                .exclude("foo", "bar")
                .build();
        Iterable<FormItem> formItems = form.getFormItems();
        Iterator<FormItem> iterator = formItems.iterator();

        assertEquals("baz", iterator.next().getName());
        assertEquals("qux", iterator.next().getName());
    }


    // ------------------------------------------------------ helper methods

    private ModelNodeForm.Builder<ModelNode> builder(final String id, final ResourceDescription resourceDescription) {
        return new ModelNodeForm.Builder<>(id, RWX, resourceDescription)
                .customFormItem("foo", (property) -> new TestableFormItem("foo"))
                .customFormItem("bar", (property) -> new TestableFormItem("bar"))
                .customFormItem("baz", (property) -> new TestableFormItem("baz"))
                .customFormItem("qux", (property) -> new TestableFormItem("qux"));
    }
}
