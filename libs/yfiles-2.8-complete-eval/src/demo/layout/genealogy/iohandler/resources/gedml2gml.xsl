 <xsl:stylesheet version = '1.0'
     xmlns:xsl='http://www.w3.org/1999/XSL/Transform'>
 <xsl:output method="xml" indent="yes"/>
    
	<xsl:template match="/GED">
    graph
    [
      defaultnodesize "labelsize"
      <xsl:apply-templates select="INDI"/>
      <xsl:apply-templates select="FAM"/>
    ]
  </xsl:template>
   
  <!-- individual: creates node, selects color and label from SEX and NAME -->

  <xsl:template match="/GED/INDI">
    node
    [
      id "<xsl:value-of select="@ID"/>"
      label
        <xsl:choose>
          <xsl:when test="./NAME/text() = ''">
            "<xsl:value-of select="NAME"/>"
          </xsl:when>
          <xsl:otherwise>
            "<xsl:value-of select="./NAME/text()"/>"
          </xsl:otherwise>
        </xsl:choose>
      graphics
      [
        customconfiguration "Individual"
          <xsl:choose>
            <xsl:when test="./SEX/text() = 'M'">
              fill "#CCCCFF"
              outline "#CCCCFF"
            </xsl:when>
            <xsl:otherwise>
              fill "#FF99CC"
              outline "#FF99CC"
            </xsl:otherwise>
          </xsl:choose>
        w 220
        h 35
      ]
    ]
  </xsl:template>

   <!-- Family: creates a circular node -->

  <xsl:template match="/GED/FAM">
    node
    [
      id "<xsl:value-of select="@ID"/>"
      graphics
      [
        w  15.0
        h  15.0
        customconfiguration "Family"
        fill  "#000000"
       ]
     ]
     <!-- creates the edges -->
    <xsl:apply-templates select="HUSB"/>
    <xsl:apply-templates select="WIFE"/>
    <xsl:apply-templates select="CHIL"/>
  </xsl:template>


   <!-- creates edges to parents (husband), EdgeType=MARRIAGE -->
  <xsl:template match="/GED/FAM/HUSB">
    edge
    [
      source "<xsl:value-of select="@REF"/>"
      target "<xsl:value-of select="../@ID"/>"
    ]
  </xsl:template>

   <!-- creates edges to parents (wife), EdgeType=MARRIAGE -->
   <xsl:template match="/GED/FAM/WIFE">
    edge
    [
      source "<xsl:value-of select="@REF"/>"
      target "<xsl:value-of select="../@ID"/>"
    ]
  </xsl:template>

   <!-- creates edges to children, EdgeType=CHILD -->
  <xsl:template match="/GED/FAM/CHIL">
    edge
    [
      source "<xsl:value-of select="../@ID"/>"
      target "<xsl:value-of select="@REF"/>"
    ]
  </xsl:template>

</xsl:stylesheet> 
