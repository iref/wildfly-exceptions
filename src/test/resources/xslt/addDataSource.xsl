<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:as="urn:jboss:domain:2.0"
                xmlns:sd="urn:jboss:domain:datasources:2.0">

    <xsl:output method="xml" indent="yes"/>

    <xsl:template match="as:profile/sd:subsystem/sd:datasources">
        <datasources>
            <xsl:apply-templates select="@* | *" />
            <datasource jndi-name="java:jdbc/arquillian"
                        pool-name="ExampleDS" enabled="true" jta="false">
                <connection-url>jdbc:h2:mem:test2;DB_CLOSE_DELAY=-1</connection-url>
                <driver>h2</driver>
                <security>
                    <user-name>sa</user-name>
                    <password>sa</password>
                </security>
            </datasource>
        </datasources>
    </xsl:template>

    <!-- Copy everything else. -->
    <xsl:template match="@*|node()">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()"/>
        </xsl:copy>
    </xsl:template>
</xsl:stylesheet>