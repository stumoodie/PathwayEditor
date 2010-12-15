/****************************************************************************
 **
 ** This file is part of yFiles-2.8. 
 ** 
 ** yWorks proprietary/confidential. Use is subject to license terms.
 **
 ** Redistribution of this file or of an unauthorized byte-code version
 ** of this file is strictly forbidden.
 **
 ** Copyright (c) 2000-2010 by yWorks GmbH, Vor dem Kreuzberg 28, 
 ** 72070 Tuebingen, Germany. All rights reserved.
 **
 ***************************************************************************/
package demo.io.graphml;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import y.io.graphml.graph2d.ShapeNodeRealizerSerializer;
import y.io.graphml.input.GraphMLParseException;
import y.io.graphml.input.GraphMLParseContext;
import y.io.graphml.output.GraphMLWriteException;
import y.io.graphml.output.GraphMLWriteContext;
import y.io.graphml.output.XmlWriter;
import y.view.NodeRealizer;

/**
 * Serializer for instances of class {@link demo.io.graphml.CustomNodeRealizer}.
 * <p>
 * Generates XML markup nested within a node's GraphML <code>&lt;data></code>
 * element similar to the following:
 * </p>
 * <pre>
 *   &lt;custom:CustomNode customAttribute="v1.0">
 *      &lt;custom:CustomElement value="333"/>
 *   &lt;/custom:CustomNode>
 * </pre>
 * Note that for presentation purposes the content of the XML markup is used as
 * the node's label.
 */
public class CustomNodeRealizerSerializer extends ShapeNodeRealizerSerializer {
  /**
   * Returns the string <tt>CustomNode</tt>.
   */
  public String getName() {
    return "CustomNode";
  }


  public String getNamespaceURI() {
    return "demo.io.graphml.CustomNodeRealizer";
  }


  public String getNamespacePrefix() {
    return "custom";
  }

  /**
   * Returns class {@link CustomNodeRealizer}.
   */
  public Class getRealizerClass() {
    return CustomNodeRealizer.class;
  }

  /**
   * Writes the <code>customElement</code> field of a CustomNodeRealizer object
   * as an additional XML element.
   * (This XML element is nested within the GraphML &lt;data> element of nodes.)
   */
  public void write(NodeRealizer realizer, XmlWriter writer, GraphMLWriteContext context) throws GraphMLWriteException {
    super.write(realizer, writer, context);
    CustomNodeRealizer fnr = (CustomNodeRealizer) realizer;
    writer.writeStartElement(getNamespacePrefix(), "CustomElement", getNamespaceURI())
        .writeAttribute("value", fnr.getCustomValue())
        .writeEndElement();
  }

  /**
   * For demonstration purposes this method writes an additional <code>customAttribute</code> value as an XML attribute of a CustomNodeRealizer's
   * XML markup. 
   * (This XML attribute enhances the GraphML &lt;data&gt; element of nodes.)
   */
  public void writeAttributes(NodeRealizer realizer, XmlWriter writer, GraphMLWriteContext context) {
    super.writeAttributes(realizer, writer, context);
    CustomNodeRealizer fnr = (CustomNodeRealizer) realizer;
    writer.writeAttribute("customAttribute", fnr.getCustomAttribute());
  }

  /**
   * Parses parts of the content of a GraphML file by processing its DOM structure. 
   */
  public void parse(NodeRealizer realizer, Node domNode, GraphMLParseContext context) throws GraphMLParseException {
    super.parse(realizer, domNode, context);

    CustomNodeRealizer result = (CustomNodeRealizer) realizer;

    //parse attributes
    NamedNodeMap nm = domNode.getAttributes();
    Node a = nm.getNamedItem("customAttribute");
    if (a != null) {
      result.setCustomAttribute(a.getNodeValue());
    }

    //parse elements
    for (Node child = domNode.getFirstChild(); child != null; child = child.getNextSibling()) {
      if (child.getNodeType() == Node.ELEMENT_NODE &&
          "CustomElement".equals(child.getLocalName())) {
        nm = child.getAttributes();
        a = nm.getNamedItem("value");
        if (a != null) {
          result.setCustomValue(Integer.parseInt(a.getNodeValue()));
        }
      }
    }
  }
}
