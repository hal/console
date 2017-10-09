package org.jboss.hal.meta.processing;

import org.jboss.hal.dmr.ExternalModelNode;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.ResourceAddress;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.StatementContext;
import org.junit.Before;
import org.junit.Test;

import static org.jboss.hal.meta.processing.RrdParserTestHelper.assertResourceDescriptions;
import static org.jboss.hal.meta.processing.RrdParserTestHelper.assertSecurityContexts;

@SuppressWarnings({"DuplicateStringLiteralInspection", "HardCodedStringLiteral"})
public class SingleRrdParserTest {

    private RrdResult rrdResult;

    @Before
    public void setUp() throws Exception {
        rrdResult = new RrdResult();
        // mock(Browser.class);
    }


    // ------------------------------------------------------ concrete resources

    @Test
    public void concreteResourceDescriptionOnly() {
        // /subsystem=mail:read-resource-description(operations=true,recursive=true)
        ResourceAddress address = AddressTemplate.of("/subsystem=mail").resolve(StatementContext.NOOP);
        ModelNode modelNode = ExternalModelNode
                .read(SingleRrdParserTest.class.getResourceAsStream("rrd_concrete_resource_description_only.dmr"));
        new SingleRrdParser(rrdResult).parse(address, modelNode);

        assertResourceDescriptions(rrdResult, 6,
                "/subsystem=mail",
                "/subsystem=mail/mail-session=*",
                "/subsystem=mail/mail-session=*/server=smtp",
                "/subsystem=mail/mail-session=*/server=pop3",
                "/subsystem=mail/mail-session=*/server=imap",
                "/subsystem=mail/mail-session=*/custom=*");
    }

    @Test
    public void concreteResourceSecurityContextOnly() {
        // /subsystem=mail:read-resource-description(access-control=trim-descriptions,operations=true,recursive=true)
        ResourceAddress address = AddressTemplate.of("/subsystem=mail").resolve(StatementContext.NOOP);
        ModelNode modelNode = ExternalModelNode
                .read(SingleRrdParserTest.class.getResourceAsStream("rrd_concrete_resource_security_only.dmr"));
        new SingleRrdParser(rrdResult).parse(address, modelNode);

        assertSecurityContexts(rrdResult, 6,
                "/subsystem=mail",
                "/subsystem=mail/mail-session=*",
                "/subsystem=mail/mail-session=*/server=smtp",
                "/subsystem=mail/mail-session=*/server=pop3",
                "/subsystem=mail/mail-session=*/server=imap",
                "/subsystem=mail/mail-session=*/custom=*");
    }

    @Test
    public void concreteResourceCombined() {
        // /subsystem=mail:read-resource-description(access-control=combined-descriptions,operations=true,recursive=true)
        ResourceAddress address = AddressTemplate.of("/subsystem=mail").resolve(StatementContext.NOOP);
        ModelNode modelNode = ExternalModelNode
                .read(SingleRrdParserTest.class.getResourceAsStream("rrd_concrete_resource_combined.dmr"));
        new SingleRrdParser(rrdResult).parse(address, modelNode);

        assertResourceDescriptions(rrdResult, 6,
                "/subsystem=mail",
                "/subsystem=mail/mail-session=*",
                "/subsystem=mail/mail-session=*/server=smtp",
                "/subsystem=mail/mail-session=*/server=pop3",
                "/subsystem=mail/mail-session=*/server=imap",
                "/subsystem=mail/mail-session=*/custom=*");

        assertSecurityContexts(rrdResult, 6,
                "/subsystem=mail",
                "/subsystem=mail/mail-session=*",
                "/subsystem=mail/mail-session=*/server=smtp",
                "/subsystem=mail/mail-session=*/server=pop3",
                "/subsystem=mail/mail-session=*/server=imap",
                "/subsystem=mail/mail-session=*/custom=*");
    }


    // ------------------------------------------------------ wildcard resources

    @Test
    public void wildcardResourceDescriptionOnly() {
        // /subsystem=mail/mail-session=*/server=*:read-resource-description(operations=true,recursive=true)
        ResourceAddress address = AddressTemplate.of("/subsystem=mail/mail-session=*/server=*")
                .resolve(StatementContext.NOOP);
        ModelNode modelNode = ExternalModelNode
                .read(SingleRrdParserTest.class.getResourceAsStream("rrd_wildcard_resource_description_only.dmr"));
        new SingleRrdParser(rrdResult).parse(address, modelNode);

        assertResourceDescriptions(rrdResult, 3,
                "/subsystem=mail/mail-session=*/server=smtp",
                "/subsystem=mail/mail-session=*/server=pop3",
                "/subsystem=mail/mail-session=*/server=imap");
    }

    @Test
    public void wildcardResourceSecurityContextOnly() {
        // /subsystem=mail/mail-session=*/server=*:read-resource-description(access-control=trim-descriptions,operations=true,recursive=true)
        ResourceAddress address = AddressTemplate.of("/subsystem=mail/mail-session=*/server=*")
                .resolve(StatementContext.NOOP);
        ModelNode modelNode = ExternalModelNode
                .read(SingleRrdParserTest.class.getResourceAsStream("rrd_wildcard_resource_security_only.dmr"));
        new SingleRrdParser(rrdResult).parse(address, modelNode);

        assertSecurityContexts(rrdResult, 3,
                "/subsystem=mail/mail-session=*/server=smtp",
                "/subsystem=mail/mail-session=*/server=pop3",
                "/subsystem=mail/mail-session=*/server=imap");
    }

    @Test
    public void wildcardResourceCombined() {
        // /subsystem=mail/mail-session=*/server=*:read-resource-description(access-control=combined-descriptions,operations=true,recursive=true)
        ResourceAddress address = AddressTemplate.of("/subsystem=mail/mail-session=*/server=*")
                .resolve(StatementContext.NOOP);
        ModelNode modelNode = ExternalModelNode
                .read(SingleRrdParserTest.class.getResourceAsStream("rrd_wildcard_resource_combined.dmr"));
        new SingleRrdParser(rrdResult).parse(address, modelNode);

        assertResourceDescriptions(rrdResult, 3,
                "/subsystem=mail/mail-session=*/server=smtp",
                "/subsystem=mail/mail-session=*/server=pop3",
                "/subsystem=mail/mail-session=*/server=imap");

        assertSecurityContexts(rrdResult, 3,
                "/subsystem=mail/mail-session=*/server=smtp",
                "/subsystem=mail/mail-session=*/server=pop3",
                "/subsystem=mail/mail-session=*/server=imap");
    }


    // ------------------------------------------------------ security exceptions

    @Test
    public void securityExceptions() {
        // /server-group=*:read-resource-description(access-control=trim-descriptions,recursive=true){roles=other-admin}
        ResourceAddress address = AddressTemplate.of("/server-group=*").resolve(StatementContext.NOOP);
        ModelNode modelNode = ExternalModelNode
                .read(SingleRrdParserTest.class.getResourceAsStream("rrd_security_exceptions.dmr"));
        new SingleRrdParser(rrdResult).parse(address, modelNode);

        assertSecurityContexts(rrdResult, 8,
                "/server-group=*",
                "/server-group=*/deployment=*",
                "/server-group=*/jvm=*",
                "/server-group=*/deployment-overlay=*",
                "/server-group=*/deployment-overlay=*/deployment=*",
                "/server-group=*/system-property=*",
                "/server-group=other-server-group",
                "/server-group=other-server-group/jvm=default");
    }
}