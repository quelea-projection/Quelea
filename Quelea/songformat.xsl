<?xml version="1.0" encoding="iso-8859-1"?>
<xsl:stylesheet version="1.1" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:fo="http://www.w3.org/1999/XSL/Format" exclude-result-prefixes="fo">
    <xsl:template match="songs">
        <fo:root xmlns:fo="http://www.w3.org/1999/XSL/Format" font-family="Roboto">
            <fo:layout-master-set>
                <fo:simple-page-master master-name="my-page">
                    <fo:region-body margin="1in"/>
                </fo:simple-page-master>
            </fo:layout-master-set>
            <fo:page-sequence master-reference="my-page">
                <fo:flow flow-name="xsl-region-body">
                    <xsl:for-each select="song">
                        <fo:block font-size="18pt" font-weight="bold">
                            <xsl:value-of select="title"/>
                        </fo:block>
                        <fo:block font-size="15pt" font-style="italic">
                            <xsl:value-of select="author"/>
                        </fo:block>
                        <fo:block break-after="page" space-before="12pt" linefeed-treatment="preserve" font-size="12pt">
                            <xsl:value-of select="lyrics"/>
                        </fo:block>
                    </xsl:for-each>
                </fo:flow>
            </fo:page-sequence>
        </fo:root>
    </xsl:template>
</xsl:stylesheet>