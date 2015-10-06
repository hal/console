package org.jboss.hal.meta.processing;

import elemental.client.Browser;
import org.jboss.hal.dmr.ExternalModelNode;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.model.ResourceAddress;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.StatementContext;
import org.junit.Before;
import org.junit.Test;

import java.util.Set;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

/**
 * @author Harald Pehl
 */
public class SingleRrdParserTest {

    @Before
    public void setUp() {
        mock(Browser.class);
    }


    // ------------------------------------------------------ concrete resources

    @Test
    public void concreteResourceDescriptionOnly() {
        // /subsystem=mail:read-resource-description(operations=true,recursive=true)
        ResourceAddress address = AddressTemplate.of("/subsystem=mail").resolve(StatementContext.NOOP);
        ModelNode modelNode = read("rrd_concrete_resource_description_only.dmr");
        Set<RrdResult> results = new SingleRrdParser().parse(address, modelNode);

        assertResults(results, 6,
                "/subsystem=mail",
                "/subsystem=mail/mail-session=*",
                "/subsystem=mail/mail-session=*/server=smtp",
                "/subsystem=mail/mail-session=*/server=pop3",
                "/subsystem=mail/mail-session=*/server=imap",
                "/subsystem=mail/mail-session=*/custom=*");
        assertDescriptionOnly(results);
    }

    @Test
    public void concreteResourceSecurityContextOnly() {
        // /subsystem=mail:read-resource-description(access-control=trim-descriptions,operations=true,recursive=true)
        ResourceAddress address = AddressTemplate.of("/subsystem=mail").resolve(StatementContext.NOOP);
        ModelNode modelNode = read("rrd_concrete_resource_security_only.dmr");
        Set<RrdResult> results = new SingleRrdParser().parse(address, modelNode);

        assertResults(results, 6,
                "/subsystem=mail",
                "/subsystem=mail/mail-session=*",
                "/subsystem=mail/mail-session=*/server=smtp",
                "/subsystem=mail/mail-session=*/server=pop3",
                "/subsystem=mail/mail-session=*/server=imap",
                "/subsystem=mail/mail-session=*/custom=*");
        assertSecurityContextOnly(results);
    }

    @Test
    public void concreteResourceCombined() {
        // /subsystem=mail:read-resource-description(access-control=combined-descriptions,operations=true,recursive=true)
        ResourceAddress address = AddressTemplate.of("/subsystem=mail").resolve(StatementContext.NOOP);
        ModelNode modelNode = read("rrd_concrete_resource_combined.dmr");
        Set<RrdResult> results = new SingleRrdParser().parse(address, modelNode);

        assertResults(results, 6,
                "/subsystem=mail",
                "/subsystem=mail/mail-session=*",
                "/subsystem=mail/mail-session=*/server=smtp",
                "/subsystem=mail/mail-session=*/server=pop3",
                "/subsystem=mail/mail-session=*/server=imap",
                "/subsystem=mail/mail-session=*/custom=*");
        assertCombined(results);
    }


    // ------------------------------------------------------ wildcard resources

    @Test
    public void wildcardResourceDescriptionOnly() {
        // /subsystem=mail/mail-session=*/server=*:read-resource-description(operations=true,recursive=true)
        ResourceAddress address = AddressTemplate.of("/subsystem=mail/mail-session=*/server=*")
                .resolve(StatementContext.NOOP);
        ModelNode modelNode = read("rrd_wildcard_resource_description_only.dmr");
        Set<RrdResult> results = new SingleRrdParser().parse(address, modelNode);

        assertResults(results, 3,
                "/subsystem=mail/mail-session=*/server=smtp",
                "/subsystem=mail/mail-session=*/server=pop3",
                "/subsystem=mail/mail-session=*/server=imap");
        assertDescriptionOnly(results);
    }

    @Test
    public void wildcardResourceSecurityContextOnly() {
        // /subsystem=mail/mail-session=*/server=*:read-resource-description(access-control=trim-descriptions,operations=true,recursive=true)
        ResourceAddress address = AddressTemplate.of("/subsystem=mail/mail-session=*/server=*")
                .resolve(StatementContext.NOOP);
        ModelNode modelNode = read("rrd_wildcard_resource_security_only.dmr");
        Set<RrdResult> results = new SingleRrdParser().parse(address, modelNode);

        assertResults(results, 3,
                "/subsystem=mail/mail-session=*/server=smtp",
                "/subsystem=mail/mail-session=*/server=pop3",
                "/subsystem=mail/mail-session=*/server=imap");
        assertSecurityContextOnly(results);
    }

    @Test
    public void wildcardResourceCombined() {
        // /subsystem=mail/mail-session=*/server=*:read-resource-description(access-control=combined-descriptions,operations=true,recursive=true)
        ResourceAddress address = AddressTemplate.of("/subsystem=mail/mail-session=*/server=*")
                .resolve(StatementContext.NOOP);
        ModelNode modelNode = read("rrd_wildcard_resource_combined.dmr");
        Set<RrdResult> results = new SingleRrdParser().parse(address, modelNode);

        assertResults(results, 3,
                "/subsystem=mail/mail-session=*/server=smtp",
                "/subsystem=mail/mail-session=*/server=pop3",
                "/subsystem=mail/mail-session=*/server=imap");
        assertCombined(results);
    }


    // ------------------------------------------------------ security exceptions

    @Test
    public void securityExceptions() {
        // /server-group=*:read-resource-description(access-control=trim-descriptions,recursive=true){roles=other-admin}
        ResourceAddress address = AddressTemplate.of("/server-group=*").resolve(StatementContext.NOOP);
        ModelNode modelNode = read("rrd_security_exceptions.dmr");
        Set<RrdResult> results = new SingleRrdParser().parse(address, modelNode);

        assertResults(results, 8,
                "/server-group=*",
                "/server-group=*/deployment=*",
                "/server-group=*/jvm=*",
                "/server-group=*/deployment-overlay=*",
                "/server-group=*/deployment-overlay=*/deployment=*",
                "/server-group=*/system-property=*",
                "/server-group=other-server-group",
                "/server-group=other-server-group/jvm=default");
        assertSecurityContextOnly(results);
    }


    // ------------------------------------------------------ helper methods

    private void assertResults(Set<RrdResult> results, int size, String... addresses) {
        assertEquals(size, results.size());

        for (String address : addresses) {
            assertTrue("RrdResults does not contain address " + address,
                    results.contains(new RrdResult(AddressTemplate.of(address).resolve(StatementContext.NOOP))));
        }
    }

    private void assertDescriptionOnly(Set<RrdResult> results) {
        for (RrdResult result : results) {
            assertNotNull(result.resourceDescription);
            assertNull(result.securityContext);
        }
    }

    private void assertSecurityContextOnly(Set<RrdResult> results) {
        for (RrdResult result : results) {
            assertNull(result.resourceDescription);
            assertNotNull(result.securityContext);
        }
    }

    private void assertCombined(Set<RrdResult> results) {
        for (RrdResult result : results) {
            assertNotNull(result.resourceDescription);
            assertNotNull(result.securityContext);
        }
    }

    private ModelNode read(String name) {
        return ExternalModelNode.read(SingleRrdParserTest.class.getResourceAsStream(name));
    }
}