<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:as="urn:jboss:domain:1.4"
                xmlns:ex="urn:cz:muni:exceptions:1.0">

    <xsl:output method="xml" indent="yes"/>

    <xsl:template match="//as:profile/ex:subsystem" />

    <xsl:template match="as:profile">
        <profile>
            <xsl:apply-templates select="@* | *" />

            <xsl:copy-of select="document('../exceptions-subsystem.xml')" />
        </profile>
    </xsl:template>

    <!-- Copy everything else. -->
    <xsl:template match="@*|node()">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()"/>
        </xsl:copy>
    </xsl:template>

</xsl:stylesheet>