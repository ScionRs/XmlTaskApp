<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:template match="/">
        <xsl:element name="entries">
            <xsl:for-each select="entries/entry">
                <xsl:element name="entry">
                    <xsl:attribute name="field" >
                        <xsl:value-of select="field"/>
                    </xsl:attribute>
                    <xsl:for-each select="entry">
                        <xsl:attribute name="field" >
                            <xsl:value-of select="field"/>
                        </xsl:attribute>
                    </xsl:for-each>
                </xsl:element>
            </xsl:for-each>
        </xsl:element>
    </xsl:template>
</xsl:stylesheet>
