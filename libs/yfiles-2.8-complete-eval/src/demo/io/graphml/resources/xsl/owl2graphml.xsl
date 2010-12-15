<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:food="http://www.w3.org/2001/sw/WebOnt/guide-src/food#"
                xmlns:vin="http://www.w3.org/2001/sw/WebOnt/guide-src/wine#"
                xmlns:owl="http://www.w3.org/2002/07/owl#"
                xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
                xmlns:rdfs="http://www.w3.org/2000/01/rdf-schema#"
                xmlns:y="http://www.yworks.com/xml/graphml"
                xmlns="http://graphml.graphdrawing.org/xmlns">

  <!-- Stylesheet that transforms OWL web ontology files (http://www.w3.org/TR/owl-ref) to a
          GraphML (http://www.yworks.com/products/graphml).
          The output graph depicts owl classes as nodes and class relationships as edges.
          Currently three kinds of realationships are considered: subClass, disjointWith and objectProperty.
  -->

  <xsl:output method="xml" indent="yes"/>
  <xsl:template match="/rdf:RDF">
    <graphml
        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
        xsi:schemaLocation="http://graphml.graphdrawing.org/xmlns http://www.yworks.com/xml/schema/graphml/1.1/ygraphml.xsd">
      <key id="d0" for="node" yfiles.type="nodegraphics"/>
      <key id="d1" for="edge" yfiles.type="edgegraphics"/>
      <key id="d2" for="graph" yfiles.type="postprocessors"/>
      <graph id="G" edgedefault="directed">
        <xsl:apply-templates select="owl:Class"/>
        <xsl:apply-templates select="owl:ObjectProperty"/>
        <xsl:apply-templates
            select="owl:Class/rdfs:subClassOf[@rdf:resource]|owl:Class/owl:disjointWith[@rdf:resource]"/>
        <data key="d2">
          <y:Postprocessors>            
            <y:Processor class="demo.io.graphml.NodeSizeAdapter">
              <y:Option name="IGNORE_WIDTHS" value="false"/>
              <y:Option name="IGNORE_HEIGHTS" value="true"/>
              <y:Option name="ADAPT_TO_MAXIMUM_NODE" value="false"/>
            </y:Processor>
            <y:Processor class="y.module.SmartOrganicLayoutModule">
              <y:Option name="VISUAL.PREFERRED_EDGE_LENGTH" value="90"/>
              <y:Option name="ALGORITHM.QUALITY_TIME_RATIO" value="1.0"/>
              <y:Option name="VISUAL.OBEY_NODE_SIZES" value="true"/>
              <y:Option name="ALGORITHM.MAXIMAL_DURATION" value="30"/>
              <y:Option name="VISUAL.ALLOW_NODE_OVERLAPS" value="false"/>
              <y:Option name="VISUAL.COMPACTNESS" value="0.6"/>
              <y:Option name="VISUAL.MINIMAL_NODE_DISTANCE" value="10.0"/>
              <y:Option name="ALGORITHM.ACTIVATE_DETERMINISTIC_MODE" value="false"/>
              <y:Option name="VISUAL.SCOPE" value="ALL"/>
            </y:Processor>
          </y:Postprocessors>
        </data>
      </graph>
    </graphml>
  </xsl:template>
  <xsl:template match="/rdf:RDF/owl:Class">
    <xsl:element name="node">
      <xsl:attribute name="id">
        <xsl:value-of select="@rdf:ID"/>
      </xsl:attribute>
      <data key="d0">
        <y:GenericNode configuration="DemoDefaults#Node">
          <y:Fill color="#FF9900" transparent="false"/>
          <y:BorderStyle type="line" width="1.0" hasColor="false"/>
          <y:NodeLabel>
            <xsl:value-of select="@rdf:ID"/>
          </y:NodeLabel>
        </y:GenericNode>
      </data>
    </xsl:element>
  </xsl:template>
  <xsl:template match="/rdf:RDF/owl:Class/rdfs:subClassOf">
    <xsl:element name="edge">
      <xsl:attribute name="id">
        <xsl:value-of select="generate-id()"/>
      </xsl:attribute>
      <xsl:attribute name="target">
        <xsl:value-of select="substring(@rdf:resource,2)"/>
      </xsl:attribute>
      <xsl:attribute name="source">
        <xsl:value-of select="parent::node()/@rdf:ID"/>
      </xsl:attribute>
      <data key="d1">
        <y:PolyLineEdge>
          <y:LineStyle type="line" width="1.0" color="#000000"/>
          <y:Arrows source="none" target="white_delta"/>
        </y:PolyLineEdge>
      </data>
    </xsl:element>
  </xsl:template>
  <xsl:template match="/rdf:RDF/owl:Class/owl:disjointWith">
    <xsl:element name="edge">
      <xsl:attribute name="id">
        <xsl:value-of select="generate-id()"/>
      </xsl:attribute>
      <xsl:attribute name="source">
        <xsl:value-of select="substring(@rdf:resource,2)"/>
      </xsl:attribute>
      <xsl:attribute name="target">
        <xsl:value-of select="parent::node()/@rdf:ID"/>
      </xsl:attribute>
      <data key="d1">
        <y:PolyLineEdge>
          <y:LineStyle type="line" width="1.0" color="#00FF00"/>
          <y:Arrows source="none" target="none"/>
        </y:PolyLineEdge>
      </data>
    </xsl:element>
  </xsl:template>

  <xsl:template match="/rdf:RDF/owl:ObjectProperty">
    <xsl:element name="edge">
      <xsl:attribute name="id">
        <xsl:value-of select="generate-id()"/>
      </xsl:attribute>
      <xsl:attribute name="source">
        <xsl:value-of select="substring(rdfs:domain/@rdf:resource,2)"/>
      </xsl:attribute>
      <xsl:attribute name="target">
        <xsl:value-of select="substring(rdfs:range/@rdf:resource,2)"/>
      </xsl:attribute>
      <data key="d1">
        <y:PolyLineEdge>
          <y:LineStyle type="line" width="1.0" color="#FF0000"/>
          <y:Arrows source="none" target="default"/>
          <xsl:element name="y:EdgeLabel">
            <xsl:value-of select="@rdf:ID"/>
          </xsl:element>
        </y:PolyLineEdge>
      </data>
    </xsl:element>
  </xsl:template>

</xsl:stylesheet>
