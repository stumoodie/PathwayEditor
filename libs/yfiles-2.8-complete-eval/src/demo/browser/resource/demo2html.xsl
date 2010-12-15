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

    <xsl:template match="y:demo" xmlns:y="http://www.yworks.com/demo">
      <html xmlns="http://www.w3.org/1999/xhtml">
        <head>
          <title><xsl:value-of select="y:displayname" /></title>
          <style type="text/css">
            <xsl:comment>
              body {
  font-family:Verdana, sans-serif; 
  margin-left:12px;
  margin-right:12px;
  margin-top:12px;
  margin-bottom:12px;
}

h1 { 
  padding-left:8px;
  padding-top:8px;
  padding-bottom:8px;
  background-color:#d0d0d0;
  color:#4079ea;
/*  color:#4d88ff; */
  font-size:18px;
  margin-bottom:5px;
}

p {   
  padding-bottom:0px;
  padding-top:0px;
  margin-top:3px;
  margin-bottom:0px;
/*  margin-left:10px; */
  margin-left:8px;
  font-size:12px;
  color:#000000;
}

h2 { 
  font-size:14px; 
  color:#4079ea; 
  margin-top:10px; 
  margin-bottom:0px; 
}

h3 { 
  font-size:13px; 
/*  margin-left:10px; */
  margin-left:8px;
  margin-top:8px; 
  margin-bottom:2px; 
}

table {
/*  margin-left:10px; */
  margin-left: 8px;
  margin-top: 8px;
  padding-top: 5px;
  padding-bottom: 5px;
  border: 1px solid white;
  vertical-align: baseline;
  border-collapse: collapse;
}

td { 
  font-size:12px;
  background-color: #E3EEFE;
  vertical-align:top;
  border: 1px solid white;
  vertical-align: baseline;
}

th {
  font-size:12px; 
  background-color:#a4b9ea;
  text-align: left;
}

a {
  color: #AA5522; font-weight:bold;
}
a:link {
  text-decoration:none;
}
a:visited {
  text-decoration:none; color:#772200;
}
a:active, 
a:focus, 
a:hover {
  text-decoration: underline;
}

div.li1 {
  font-size:12px;
  margin-top:8px; 
  margin-bottom:0px; 
  margin-left:10px; 
}
div.li2 { 
  margin-left:20px;
  margin-top:2px; 
  margin-bottom:2px; 
  font-weight:bold;
  font-size:12px; 
}

dl { 
  font-size:12px;
  margin-top:2px;
  margin-bottom:2px;
  margin-left:15px;
}
dt { 
  font-weight:bold;
  margin-top:4px;
/*  color:#000000; */
  color:#333366;
}
dd { 
  margin-left:15px;
}

li { 
  /* does not work in pre-1.5 Java JEditorPane :-( */
/*  list-style-type:none; */
  font-size:12px;
}

caption {
  font-size:12px;
}

.copyright { 
  font-size: 10px;
  font-style:italic;
  text-align:right;
  margin-top:20px;
}

code, tt { 
  font-family:monospace;
  font-size:12px;
  color:#0000C0;
}

            </xsl:comment>
          </style>
        </head>
        <body>
          <h1><xsl:value-of select="y:displayname" /></h1>
          <xsl:if test="count(./y:description/*|./y:description/text())!=0">
            <table>
              <tr>
                <td style="background-color:#ffffff; vertical-align:top;">
                  <xsl:apply-templates select="y:description" />
                </td>
              </tr>
            </table>
          </xsl:if>
        </body>
      </html>
    </xsl:template>

    <xsl:template match="y:description//*" priority="1" xmlns:y="http://www.yworks.com/demo">
      <xsl:copy>
        <xsl:for-each select="@*">
          <xsl:copy/>
        </xsl:for-each>
        <xsl:apply-templates />
      </xsl:copy>	
    </xsl:template>

    <!--
    <xsl:template match="html:a" priority="2" xmlns:html="http://www.w3.org/1999/xhtml">
           evil usage of <font>-tag since Java 1.4 JEditorPane does not like
           <span style="color: #0000C0;">
      <font color="#0000C0"><xsl:apply-templates /></font>
    </xsl:template>
    -->

    <xsl:template match="html:br" priority="2" xmlns:html="http://www.w3.org/1999/xhtml">
      <xsl:text disable-output-escaping="yes">&lt;br&gt;</xsl:text>
    </xsl:template>

    <xsl:template match="html:img" priority="2" xmlns:html="http://www.w3.org/1999/xhtml">
      <xsl:text disable-output-escaping="yes">&lt;img</xsl:text><xsl:for-each select="@*"><xsl:text disable-output-escaping="yes"> </xsl:text><xsl:value-of select="name()" />="<xsl:value-of select="." />"</xsl:for-each><xsl:text disable-output-escaping="yes">&gt;</xsl:text>
    </xsl:template>

    <!--
    <xsl:template match="html:ul" priority="2" xmlns:html="http://www.w3.org/1999/xhtml">
      <table style="background-color: #ffffff;" border="1" cellspacing="0" cellpadding="0">
        <xsl:apply-templates select="html:li" />
      </table>
    </xsl:template>

    <xsl:template match="html:li" priority="2" xmlns:html="http://www.w3.org/1999/xhtml">
      <tr valign="top">
        <td style="background-color: #ffffff;">-<xsl:text disable-output-escaping="yes">&amp;</xsl:text>nbsp;</td>
        <td style="background-color: #ffffff;"><xsl:apply-templates /></td>
      </tr>
    </xsl:template>
    -->

</xsl:stylesheet>
