<?xml version="1.0" encoding="iso-8859-1"?>
<xsl:stylesheet version="1.1" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:fo="http://www.w3.org/1999/XSL/Format" xmlns:xslt="http://www.w3.org/1999/XSL/Transform"
                exclude-result-prefixes="fo">
    <xsl:template match="*"></xsl:template>
    <xsl:template match="schedule">
        <fo:root xmlns:fo="http://www.w3.org/1999/XSL/Format" font-family="Roboto">
            <fo:layout-master-set>
                <fo:simple-page-master master-name="my-page">
                    <fo:region-body margin="1in"/>
                </fo:simple-page-master>
            </fo:layout-master-set>
            <fo:page-sequence master-reference="my-page">
                <fo:flow flow-name="xsl-region-body">
                    <fo:block font-size="18pt" font-weight="bold" space-after="12pt">
                        <xsl:value-of select="title"/>
                    </fo:block>
                    <xsl:apply-templates select="*"/>
                </fo:flow>
            </fo:page-sequence>
        </fo:root>
    </xsl:template>
    <xsl:template name="row">
        <xsl:param name = "content" />
        <xsl:param name = "details" />
        <fo:block space-after="24pt">
            <xsl:value-of select="position() -1"/>.
            <fo:inline font-size="12pt" padding-left="4mm" >
            <xsl:value-of select = "$content" />
            </fo:inline>
            <fo:inline font-size="8pt" padding-left="22mm" font-style="italic">
                <xsl:value-of select = "$details" />
            </fo:inline>
        </fo:block>
    </xsl:template>
    <xsl:template match="song">
        <xsl:call-template name="row">
            <xsl:with-param name="content">
                <xsl:value-of select="title"/>
            </xsl:with-param>
            <xsl:with-param name="details">
                <xsl:choose>
                    <xsl:when test="author != ''"><xsl:value-of select="author"/></xsl:when>
                </xsl:choose>
                <xsl:choose>
                    <xsl:when test="ccli != ''">(CCLI <xsl:value-of select="ccli"/>)</xsl:when>
                </xsl:choose>
            </xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <xsl:template match="passage">
        <xsl:call-template name="row">
            <xsl:with-param name="content">
                <xsl:value-of select="@summary"/>
            </xsl:with-param>
            <xsl:with-param name="details">
                <xsl:choose>
                    <xsl:when test="@bible != ''">(<xsl:value-of select="@bible"/>) </xsl:when>
                </xsl:choose>
            </xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <xsl:template name="file">
        <xsl:param name = "filetype" />
        <xsl:call-template name="row">
            <xsl:with-param name="content">
                <xsl:value-of select="$filetype"/>
            </xsl:with-param>
            <xsl:with-param name="details">
                <fo:inline font-family="monospace"><xsl:value-of select="text()"/></fo:inline>
            </xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <xsl:template match="fileaudio">
        <xsl:call-template name="file">
            <xsl:with-param name="filetype">
                Audio
            </xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <xsl:template match="fileimage">
        <xsl:call-template name="file">
            <xsl:with-param name="filetype">
                Image
            </xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <xsl:template match="filepdf">
        <xsl:call-template name="file">
            <xsl:with-param name="filetype">
                PDF
            </xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <xsl:template match="filepresentation">
        <xsl:call-template name="file">
            <xsl:with-param name="filetype">
                Presentation
            </xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <xsl:template match="filevideo">
        <xsl:call-template name="file">
            <xsl:with-param name="filetype">
                Video
            </xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <xsl:template match="url">
        <xsl:call-template name="file">
            <xsl:with-param name="filetype">
                Web
            </xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <xsl:template match="fileimagegroup">
        <xsl:call-template name="file">
            <xsl:with-param name="filetype">
                Image Group
            </xsl:with-param>
        </xsl:call-template>
    </xsl:template>
</xsl:stylesheet>