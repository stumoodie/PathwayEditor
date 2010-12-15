<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:y="http://www.yworks.com/xml/graphml" xmlns="http://graphml.graphdrawing.org/xmlns">
  <!-- Stylesheet that outputs the XML tree structure to GraphML.
     The output graph depicts the element tree of the XML file. The edges of the graph connect 
     each child element with its parent element.
  -->
  <xsl:output method="xml" indent="yes"/>
  <xsl:template match="/">
    <graphml
        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
        xsi:schemaLocation="http://graphml.graphdrawing.org/xmlns http://www.yworks.com/xml/schema/graphml/1.1/ygraphml.xsd">
      <key id="d0" for="node" yfiles.type="nodegraphics"/>
      <key id="d1" for="edge" yfiles.type="edgegraphics"/>
      <key id="d2" for="graph" yfiles.type="postprocessors"/>
      <graph id="G" edgedefault="directed">
        <xsl:apply-templates select="//*" mode="create-nodes"/>
        <xsl:apply-templates select="/*//*" mode="create-edges"/>
        <data key="d2">
          <y:Postprocessors>            
            <y:Processor class="demo.io.graphml.NodeSizeAdapter">
              <y:Option name="IGNORE_WIDTHS" value="false"/>
              <y:Option name="IGNORE_HEIGHTS" value="false"/>
              <y:Option name="ADAPT_TO_MAXIMUM_NODE" value="false"/>
            </y:Processor>
            <y:Processor class="y.module.TreeLayoutModule">
              <y:Option name="GENERAL.LAYOUT_STYLE" value="AR"/>
              <y:Option name="AR.BEND_DISTANCE" value="20"/>
              <y:Option name="AR.VERTICAL_SPACE" value="10"/>
              <y:Option name="AR.ASPECT_RATIO" value="1.41"/>
              <y:Option name="AR.HORIZONTAL_SPACE" value="10"/>
              <y:Option name="AR.USE_VIEW_ASPECT_RATIO" value="true"/>
            </y:Processor>
          </y:Postprocessors>
        </data>
      </graph>
    </graphml>
  </xsl:template>
  <xsl:template match="//*" mode="create-nodes">
    <xsl:element name="node">
      <xsl:attribute name="id">
        <xsl:value-of select="generate-id()"/>
      </xsl:attribute>
      <data key="d0">
        <y:GenericNode configuration="DemoDefaults#Node">
          <y:Fill color="#FF9900" transparent="false"/>
          <y:BorderStyle type="line" width="1.0" hasColor="false"/>
          <y:NodeLabel>&lt;html&gt;&lt;div
            style="font-size:120%;color:blue;"&gt;
            <xsl:value-of
                select="name()"/>
            &lt;/div&gt;
            <xsl:for-each select="@*">
              <xsl:value-of select="name()"/>
              =&quot;
              <xsl:value-of select="."/>
              &quot;&lt;br&gt;
            </xsl:for-each>
          </y:NodeLabel>
        </y:GenericNode>
      </data>
    </xsl:element>
  </xsl:template>
  <xsl:template match="//*" mode="create-edges">
    <xsl:element name="edge">
      <xsl:attribute name="id">
        <xsl:value-of select="generate-id()"/>
      </xsl:attribute>
      <xsl:attribute name="source">
        <xsl:value-of select="generate-id(parent::node())"/>
      </xsl:attribute>
      <xsl:attribute name="target">
        <xsl:value-of select="generate-id()"/>
      </xsl:attribute>
    </xsl:element>
  </xsl:template>
</xsl:stylesheet>
