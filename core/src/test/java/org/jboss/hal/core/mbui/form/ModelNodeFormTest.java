package org.jboss.hal.core.mbui.form;

import java.util.Collections;
import java.util.Iterator;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.gwt.junit.GWTMockUtilities;
import org.jboss.hal.ballroom.form.AddOnlyStateMachine;
import org.jboss.hal.ballroom.form.ExistingStateMachine;
import org.jboss.hal.ballroom.form.FormItem;
import org.jboss.hal.ballroom.form.ReadOnlyStateMachine;
import org.jboss.hal.ballroom.form.StateMachine;
import org.jboss.hal.core.Core;
import org.jboss.hal.core.mbui.ResourceDescriptionBuilder;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.meta.description.ResourceDescription;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static org.jboss.hal.dmr.ModelDescriptionConstants.CONFIGURATION;
import static org.jboss.hal.dmr.ModelDescriptionConstants.RUNTIME;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

@SuppressWarnings("HardCodedStringLiteral")
public class ModelNodeFormTest {

    private ResourceDescription attributes;
    private ResourceDescription requestProperties;

    @Before
    public void setUp() {
        GWTMockUtilities.disarm();
        Core.INSTANCE = mock(Core.class);
        attributes = new ResourceDescriptionBuilder().attributes("foo", "bar", "baz", "qux");
        requestProperties = new ResourceDescriptionBuilder().requestProperties(
                ImmutableMap.of("foo", true, "bar", false, "baz", true, "qux", false));
    }


    // ------------------------------------------------------ test illegal states

    @Test(expected = IllegalStateException.class)
    public void addOnlyAndView() {
        new ModelNodeForm.Builder("addOnlyAndView", metadata())
                .addOnly()
                .readOnly()
                .build();
    }

    @Test(expected = IllegalStateException.class)
    public void addFromRequestPropertiesAndView() {
        new ModelNodeForm.Builder("addFromRequestPropertiesAndView", metadata())
                .fromRequestProperties()
                .readOnly()
                .build();
    }

    @Test(expected = IllegalStateException.class)
    public void excludeRequiredAttributes() {
        new ModelNodeForm.Builder("viewAndCreateResource",
                metadata(new ResourceDescriptionBuilder().requestProperties(ImmutableMap.of("foo", true))))
                .fromRequestProperties()
                .exclude("foo")
                .build();
    }


    // ------------------------------------------------------ verify different state machines

    @Test
    public void createResourceStateMachine() {
        StateMachine stateMachine = new ModelNodeForm.Builder("createResourceStateMachine",
                metadata(new ResourceDescriptionBuilder().requestProperties(Collections.emptyMap())))
                .fromRequestProperties()
                .stateMachine();
        Assert.assertTrue(stateMachine instanceof AddOnlyStateMachine);
    }

    @Test
    public void editOnlyStateMachine() {
        StateMachine stateMachine = new ModelNodeForm.Builder("editOnlyStateMachine",
                metadata(new ResourceDescriptionBuilder().attributes()))
                .fromRequestProperties()
                .stateMachine();
        Assert.assertTrue(stateMachine instanceof AddOnlyStateMachine);
    }

    @Test
    public void viewOnlyStateMachine() {
        StateMachine stateMachine = new ModelNodeForm.Builder("viewOnlyStateMachine",
                metadata(new ResourceDescriptionBuilder().attributes()))
                .readOnly().stateMachine();
        Assert.assertTrue(stateMachine instanceof ReadOnlyStateMachine);
    }

    @Test
    public void defaultStateMachine() {
        StateMachine stateMachine = new ModelNodeForm.Builder("defaultStateMachine",
                metadata(new ResourceDescriptionBuilder().attributes()))
                .stateMachine();
        Assert.assertTrue(stateMachine instanceof ExistingStateMachine);
    }


    // ------------------------------------------------------ request properties

    @Test
    public void requestProperties() {
        ModelNodeForm<ModelNode> form = builder("requestProperties", requestProperties)
                .fromRequestProperties()
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
    public void requestPropertiesUnsorted() {
        ModelNodeForm<ModelNode> form = builder("requestPropertiesUnsorted", requestProperties)
                .fromRequestProperties()
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
    public void includeRequestProperties() {
        ModelNodeForm<ModelNode> form = builder("includeRequestProperties", requestProperties)
                .fromRequestProperties()
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
        ModelNodeForm<ModelNode> form = builder("excludeAttributes", attributes)
                .exclude("foo", "bar")
                .build();
        Iterable<FormItem> formItems = form.getFormItems();
        Iterator<FormItem> iterator = formItems.iterator();

        assertEquals(2, Iterables.size(formItems));
        assertEquals("baz", iterator.next().getName());
        assertEquals("qux", iterator.next().getName());
    }

    @Test
    public void excludeFormAddAttributes() {
        ModelNodeForm<ModelNode> form = builder("excludeFormAddAttributes", attributes)
                .exclude("foo", "bar")
                .addOnly()
                .build();
        Iterable<FormItem> formItems = form.getFormItems();
        Iterator<FormItem> iterator = formItems.iterator();

        assertEquals(2, Iterables.size(formItems));
        assertEquals("baz", iterator.next().getName());
        assertEquals("qux", iterator.next().getName());
    }

    @Test
    public void excludeFormAddRequestProperties() {
        ModelNodeForm<ModelNode> form = builder("excludeFormAddRequestProperties", requestProperties)
                .exclude("bar", "qux")
                .fromRequestProperties()
                .build();
        Iterable<FormItem> formItems = form.getFormItems();
        Iterator<FormItem> iterator = formItems.iterator();

        assertEquals(2, Iterables.size(formItems));
        assertEquals("baz", iterator.next().getName());
        assertEquals("foo", iterator.next().getName());
    }


    @Test
    public void noRuntime() throws Exception {
        ModelNodeForm<ModelNode> form = builder("noRuntime",
                new ResourceDescriptionBuilder().storage(ImmutableMap.of("foo", CONFIGURATION, "bar", RUNTIME)))
                .build();
        Iterable<FormItem> formItems = form.getFormItems();
        Iterator<FormItem> iterator = formItems.iterator();

        assertEquals(1, Iterables.size(formItems));
        assertEquals("foo", iterator.next().getName());
    }

    @Test
    public void withRuntime() throws Exception {
        ModelNodeForm<ModelNode> form = builder("withRuntime",
                new ResourceDescriptionBuilder().storage(ImmutableMap.of("foo", CONFIGURATION, "bar", RUNTIME)))
                .includeRuntime()
                .build();
        Iterable<FormItem> formItems = form.getFormItems();
        Iterator<FormItem> iterator = formItems.iterator();

        assertEquals(2, Iterables.size(formItems));
        assertEquals("bar", iterator.next().getName());
        assertEquals("foo", iterator.next().getName());
    }


    // ------------------------------------------------------ helper methods

    private ModelNodeForm.Builder<ModelNode> builder(final String id, final ResourceDescription description) {
        return new ModelNodeForm.Builder<>(id, metadata(description))
                .defaultFormItemProvider((property) -> new TestableFormItem(property.getName()));
    }

    private Metadata metadata() {
        return metadata(new ResourceDescriptionBuilder().empty());
    }

    private Metadata metadata(ResourceDescription description) {
        return Metadata.staticDescription(description);
    }
}
