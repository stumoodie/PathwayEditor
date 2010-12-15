 <xsl:stylesheet version = '1.0'
     xmlns:xsl='http://www.w3.org/1999/XSL/Transform'
     xmlns:y='http://www.yworks.com/xml/graphml' xmlns:g="http://graphml.graphdrawing.org/xmlns">
 <xsl:output method="xml" indent="yes"/>
    
	<xsl:template match="/GED">
       	 
		<g:graphml xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
      <g:key id="d0" for="node" yfiles.type="nodegraphics"/>
      <g:key id="d1" for="edge" yfiles.type="edgegraphics"/>
      <g:key id="d2" for="graph" yfiles.type="postprocessors"/>
      <g:key id="d3" for="node" attr.type="string" attr.name="GedcomType"/>
        <!-- String Attribute for nodes which can be used as  data provider FamilyTreeLayout.DP_KEY_FAMILY_TYPE  by the FamilyTreeLayout -->
      <g:key id="d4" for="node" attr.type="complex" attr.name="GedcomData"/>
        <!-- Holds the XML converted gedcom data -->

      <g:graph id="G" edgedefault="directed">

    	  <xsl:apply-templates select="INDI"/>
        <xsl:apply-templates select="FAM"/>
    	  
    	  <g:data key="d2" >
					<!--<y:Postprocessors>-->
              <!--<y:Processor class="y.module.FamilyTreeLayoutModule">-->
            <!--</y:Processor>-->
					<!--</y:Postprocessors>-->
		    </g:data>
		  </g:graph>
   	</g:graphml>

	</xsl:template>

  <!-- individual: creates node, selects color and label from SEX and NAME;
        GedcomType attribute = MALE/FEMALE, IndividualData = the corresponding raw xml-->

   <xsl:template match="/GED/INDI">
     <xsl:element name="g:node">
       <xsl:attribute name="id">
         <xsl:value-of select="@ID"/>
       </xsl:attribute>
       <g:data key="d0">
         <y:GenericNode configuration="Individual">
           <y:Geometry height="35.0" width="220.0" x="0.0" y="0.0"/>
           <xsl:choose>
             <xsl:when test="./SEX/text() = 'M'">
               <y:Fill color="#CAE3FF"/>
               <y:BorderStyle hasColor="false"/>
             </xsl:when>
             <xsl:otherwise>
               <y:Fill color="#FF9900"/>
               <y:BorderStyle hasColor="false"/>
             </xsl:otherwise>
           </xsl:choose>
           <xsl:choose>
             <xsl:when test="./NAME/text() = ''">
               <y:NodeLabel>
                 <xsl:value-of select="NAME"/>
               </y:NodeLabel>
             </xsl:when>
             <xsl:otherwise>
               <y:NodeLabel>
                 <xsl:value-of select="./NAME/text()"/>
               </y:NodeLabel>
             </xsl:otherwise>
           </xsl:choose>

         </y:GenericNode>
       </g:data>
       <g:data key="d3">
         <xsl:choose>
           <xsl:when test="./SEX/text() = 'M'">MALE</xsl:when>
           <xsl:otherwise>FEMALE</xsl:otherwise>
         </xsl:choose>
       </g:data>
       <g:data key="d4">
         <xsl:copy-of select="."></xsl:copy-of>
       </g:data>
     </xsl:element>
   </xsl:template>

   <!-- Family: creates a circular node, NodeTypeIndividual attribute = false,
   GedcomData = the corresponding raw xml -->

   <xsl:template match="/GED/FAM">
  	<xsl:element name="g:node">
  	  <xsl:attribute name="id">
  	    <xsl:value-of select="@ID"/>
  	  </xsl:attribute>
  	  <g:data key="d0" >
				<y:GenericNode configuration="Family">
          <y:Geometry height="15.0" width="15.0" x="0.0" y="0.0"/>
        <y:Fill color="#000000"/>
        <!--<y:NodeLabel><xsl:value-of select="@ID"/></y:NodeLabel>-->
     		</y:GenericNode>
  	</g:data>
    <g:data key="d3">FAMILY</g:data>
      <g:data key="d4">
        <xsl:copy-of select="."></xsl:copy-of>
      </g:data>
    </xsl:element>
     <!-- creates the edges -->
    <xsl:apply-templates select="HUSB"/>
    <xsl:apply-templates select="WIFE"/>
    <xsl:apply-templates select="CHIL"/>
  </xsl:template>


   <!-- creates edges to parents (husband) -->
  <xsl:template match="/GED/FAM/HUSB">
  	<xsl:element name="g:edge">
      <xsl:attribute name="id">
          <xsl:value-of select="@REF"/>-<xsl:value-of select="../@ID"/>
      </xsl:attribute>
  	  <xsl:attribute name="source">
  	    <xsl:value-of select="@REF"/>
  	  </xsl:attribute>
      <xsl:attribute name="target">
  	    <xsl:value-of select="../@ID"/>
  	  </xsl:attribute>
    </xsl:element>
  </xsl:template>

   <!-- creates edges to parents (wife) -->
   <xsl:template match="/GED/FAM/WIFE">
  	<xsl:element name="g:edge">
      <xsl:attribute name="id">
          <xsl:value-of select="@REF"/>-<xsl:value-of select="../@ID"/>
      </xsl:attribute>
  	  <xsl:attribute name="source">
  	    <xsl:value-of select="@REF"/>
  	  </xsl:attribute>
      <xsl:attribute name="target">
  	    <xsl:value-of select="../@ID"/>
  	  </xsl:attribute>
    </xsl:element>
  </xsl:template>

   <!-- creates edges to children, EdgeType=CHILD -->
  <xsl:template match="/GED/FAM/CHIL">
  	<xsl:element name="g:edge">
      <xsl:attribute name="id">
          <xsl:value-of select="../@ID"/>-<xsl:value-of select="@REF"/>
      </xsl:attribute>
      <xsl:attribute name="source">
  	    <xsl:value-of select="../@ID"/>
  	  </xsl:attribute>
      <xsl:attribute name="target">
  	    <xsl:value-of select="@REF"/>
  	  </xsl:attribute>
    </xsl:element>
  </xsl:template>

</xsl:stylesheet> 
