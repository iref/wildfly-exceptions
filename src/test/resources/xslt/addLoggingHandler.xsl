<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:as="urn:jboss:domain:2.0"
                xmlns:log="urn:jboss:domain:logging:2.0">

    <xsl:output method="xml" indent="yes"/>

    <xsl:template match="//log:subsystem[@xmlns='urn:jboss:domain:logging:2.0']/log:custom-handler[@class='cz.muni.exceptions.source.LoggingExceptionSource']" />

    <xsl:template match="//log:subsystem">
        <subsystem xmlns="urn:jboss:domain:logging:2.0">
            <xsl:copy-of select="document('../exceptions-logging.xml')" />
            <xsl:apply-templates select="@* | *" />
        </subsystem>
    </xsl:template>

    <xsl:template match="//log:root-logger/log:handlers">
        <handlers>
            <xsl:apply-templates select="@* | *" />
            <handler name="ExceptionHandler" />
        </handlers>
    </xsl:template>

    <!-- Copy everything else. -->
    <xsl:template match="@*|node()">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()"/>
        </xsl:copy>
    </xsl:template>

</xsl:stylesheet>