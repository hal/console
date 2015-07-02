package org.jboss.hal.ballroom.form;

import com.google.gwt.junit.GWTMockUtilities;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.jboss.hal.ballroom.form.Form.State.EDIT;
import static org.jboss.hal.ballroom.form.Form.State.EMPTY;
import static org.jboss.hal.ballroom.form.Form.State.VIEW;
import static org.junit.Assert.assertEquals;

/**
 * @author Harald Pehl
 */
public class FormTest {

    private Form<String> form;

    @Before
    public void setUp() {
        GWTMockUtilities.disarm();
        form = new StandaloneForm();
    }

    @After
    public void tearDown() {
        GWTMockUtilities.restore();
    }

    @Test
    public void initialState() {
        assertEquals("test", form.getId());
        assertEquals(EMPTY, form.getState());
    }

    @Test
    public void add() {
        form.add();
        assertEquals(EDIT, form.getState());
    }

    @Test
    public void view() {
        form.view("foo");
        assertEquals(VIEW, form.getState());
    }

    @Test
    public void edit() {
        form.edit("foo");
        assertEquals(EDIT, form.getState());
    }

    @Test
    public void viewThenEdit() {
        form.view("foo");
        form.edit("foo");
        assertEquals(EDIT, form.getState());
    }

    @Test
    public void save() {
        form.edit("foo");
        form.save();
        assertEquals(VIEW, form.getState());
    }

    @Test(expected = IllegalStateException.class)
    public void illegalSave() {
        form.save();
    }

    @Test
    public void cancel() {
        form.edit("foo");
        form.cancel();
        assertEquals(VIEW, form.getState());
    }

    @Test(expected = IllegalStateException.class)
    public void illegalCancel() {
        form.cancel();
    }

    @Test
    public void undefineFromView() {
        form.view("foo");
        form.undefine();
        assertEquals(EMPTY, form.getState());
    }

    @Test
    public void undefineFromEdit() {
        form.edit("foo");
        form.undefine();
        assertEquals(EMPTY, form.getState());
    }

    @Test(expected = IllegalStateException.class)
    public void illegalUndefine() {
        form.undefine();
    }
}