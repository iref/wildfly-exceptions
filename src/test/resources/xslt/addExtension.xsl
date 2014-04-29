<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:as="urn:jboss:domain:2.0">

    <xsl:output method="xml" indent="yes"/>

    <xsl:template match="//as:extensions/as:extension[@module='cz.muni.exceptions']" />

    <xsl:template match="//as:extensions">
        <extensions>
            <!-- add all existing extensions -->
            <xsl:apply-templates select="@* | *" />
            <extension module="cz.muni.exceptions" />
        </extensions>
    </xsl:template>

    <xsl:template match="@* | node()">
        <xsl:copy>
            <xsl:apply-templates select="@* | node()" />
        </xsl:copy>
    </xsl:template>

</xsl:stylesheet>