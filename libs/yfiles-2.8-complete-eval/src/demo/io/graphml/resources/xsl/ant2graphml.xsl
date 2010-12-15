<xsl:stylesheet version='1.0'
                xmlns:xsl='http://www.w3.org/1999/XSL/Transform'
                xmlns:y='http://www.yworks.com/xml/graphml' xmlns:g="http://graphml.graphdrawing.org/xmlns"
    >

  <!-- Stylesheet that transforms ANT build scripts (http://ant.apache.org) to a 
          GraphML (http://www.yworks.com/products/graphml). 
          The output graph depicts the dependency structure of ANT targets. 
          Additional GraphML Postprocessing action will layout the dependency graph in a 
          hierarchical fashion. 
  -->

  <xsl:output method="xml" indent="yes"/>

  <xsl:template match="/project">

    <g:graphml xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
               xsi:schemaLocation="http://graphml.graphdrawing.org/xmlns http://www.yworks.com/xml/schema/graphml/1.1/ygraphml.xsd">
      <g:key id="d0" for="node" yfiles.type="nodegraphics"/>
      <g:key id="d1" for="edge" yfiles.type="edgegraphics"/>
      <g:key id="d2" for="graph" yfiles.type="postprocessors"/>
      <g:graph id="G" edgedefault="directed">

        <xsl:apply-templates select="target" mode="nodes"/>
        <xsl:apply-templates select="target[@depends]" mode="edges"/>

        <g:data key="d2">
          <y:Postprocessors>            
            <y:Processor class="demo.io.graphml.NodeSizeAdapter">
              <y:Option name="IGNORE_WIDTHS" value="false"/>
              <y:Option name="IGNORE_HEIGHTS" value="true"/>
              <y:Option name="ADAPT_TO_MAXIMUM_NODE" value="false"/>
            </y:Processor>
            <y:Processor class="y.module.HierarchicLayoutModule">
              <y:Option name="LAYOUT.MINIMAL_FIRST_SEGMENT_LENGTH" value="20"/>
              <y:Option name="LAYOUT.MINIMAL_NODE_DISTANCE" value="20"/>
              <y:Option name="GROUPING.GROUP_LAYOUT_POLICY" value="LAYOUT_GROUPS"/>
              <y:Option name="NODE_ORDER.USE_TRANSPOSITION" value="true"/>
              <y:Option name="LAYOUT.ORIENTATION" value="TOP_TO_BOTTOM"/>
              <y:Option name="NODE_RANK.RANKING_POLICY" value="TIGHT_TREE"/>
              <y:Option name="LAYOUT.EDGE_ROUTING" value="POLYLINE"/>
              <y:Option name="LABELING.EDGE_LABELING" value="NONE"/>
              <y:Option name="NODE_ORDER.RANDOMIZATION_ROUNDS" value="40"/>
              <y:Option name="NODE_ORDER.WEIGHT_HEURISTIC" value="BARYCENTER"/>
              <y:Option name="NODE_ORDER.REMOVE_FALSE_CROSSINGS" value="true"/>
              <y:Option name="LABELING.EDGE_LABEL_MODEL" value="BEST"/>
              <y:Option name="LAYOUT.BACKLOOP_ROUTING" value="false"/>
              <y:Option name="LAYOUT.MINIMAL_LAYER_DISTANCE" value="40"/>
              <y:Option name="LAYOUT.MINIMAL_EDGE_DISTANCE" value="10"/>
              <y:Option name="LAYOUT.NODE_PLACEMENT" value="MEDIAN_SIMPLEX"/>
              <y:Option name="GROUPING.ENABLE_GLOBAL_SEQUENCING" value="true"/>
              <y:Option name="LAYOUT.ACT_ON_SELECTION_ONLY" value="false"/>
              <y:Option name="LAYOUT.MAXIMAL_DURATION" value="5"/>
            </y:Processor>
          </y:Postprocessors>
        </g:data>
      </g:graph>
    </g:graphml>

  </xsl:template>

  <xsl:template match="/project/target" mode="nodes">
    <xsl:element name="g:node">
      <xsl:attribute name="id">
        <xsl:value-of select="@name"/>
      </xsl:attribute>
      <g:data key="d0">
        <y:GenericNode configuration="DemoDefaults#Node">
          <y:Fill color="#FF9900" transparent="false"/>
          <y:BorderStyle type="line" width="1.0" hasColor="false"/>
          <y:NodeLabel>
            <xsl:value-of select="@name"/>
          </y:NodeLabel>
        </y:GenericNode>
      </g:data>
    </xsl:element>
  </xsl:template>

  <xsl:template match="/project/target" mode="edges">

    <xsl:call-template name="make-sources">
      <xsl:with-param name="sources" select="@depends"/>
      <xsl:with-param name="target" select="@name"/>
    </xsl:call-template>

  </xsl:template>

  <xsl:template name="make-sources">
    <xsl:param name="sources"/>
    <xsl:param name="target"/>

    <xsl:variable name="source">
      <xsl:choose>
        <xsl:when test="contains($sources, ',')">
          <xsl:value-of select="normalize-space(substring-before($sources,','))"/>
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="normalize-space($sources)"/>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>

    <xsl:element name="g:edge">
      <xsl:attribute name="id">
        <xsl:value-of select="generate-id()"/>
      </xsl:attribute>
      <xsl:attribute name="target">
        <xsl:value-of select="$target"/>
      </xsl:attribute>
      <xsl:attribute name="source">
        <xsl:value-of select="$source"/>
      </xsl:attribute>
      <g:data key="d1">
        <y:PolyLineEdge>
          <y:Arrows source="none" target="standard"/>
        </y:PolyLineEdge>
      </g:data>
    </xsl:element>

    <xsl:if test="contains($sources, ',')">
      <xsl:call-template name="make-sources">
        <xsl:with-param name="sources" select="substring-after($sources,',')"/>
        <xsl:with-param name="target" select="$target"/>
      </xsl:call-template>
    </xsl:if>

  </xsl:template>

</xsl:stylesheet> 
