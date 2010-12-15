<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                exclude-result-prefixes="y"
                version="1.0">

    <xsl:output method="html"
                indent="yes"
                omit-xml-declaration="yes" />

    <xsl:template match="/">
        <xsl:apply-templates />
    </xsl:template>

    <xsl:template match="y:description|y:summary" xmlns:y="http://www.yworks.com/demo">
      <xsl:if test="count(./*|./text())!=0">
        <html xmlns="http://www.w3.org/1999/xhtml">
          <head>
          </head>
          <body>
            <table width="600" border="0" cellspacing="0" cellpadding="0">
              <tr>
                <td><xsl:apply-templates /></td>
              </tr>
            </table>
          </body>
        </html>
      </xsl:if>
    </xsl:template>

    <xsl:template match="y:description//*|y:summary//*" priority="1" xmlns:y="http://www.yworks.com/demo">
      <xsl:copy>
        <xsl:for-each select="@*">
          <xsl:copy/>
        </xsl:for-each>
        <xsl:apply-templates />
      </xsl:copy>	
    </xsl:template>

    <xsl:template match="html:a" priority="2" xmlns:html="http://www.w3.org/1999/xhtml">
      <span style="color:#0000C0;">
         <xsl:apply-templates />
      </span>
    </xsl:template>

    <xsl:template match="html:h1" priority="2" xmlns:html="http://www.w3.org/1999/xhtml">
      <h4><xsl:apply-templates /></h4>
    </xsl:template>
    <xsl:template match="html:h2" priority="2" xmlns:html="http://www.w3.org/1999/xhtml">
      <h4><xsl:apply-templates /></h4>
    </xsl:template>
    <xsl:template match="html:h3" priority="2" xmlns:html="http://www.w3.org/1999/xhtml">
      <h4><xsl:apply-templates /></h4>
    </xsl:template>
    <xsl:template match="html:br" priority="2" xmlns:html="http://www.w3.org/1999/xhtml">
      <xsl:text disable-output-escaping="yes">&lt;br&gt;</xsl:text>
    </xsl:template>

</xsl:stylesheet>
