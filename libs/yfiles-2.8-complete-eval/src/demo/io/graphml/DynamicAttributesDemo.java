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

import demo.view.DemoDefaults;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import y.base.DataMap;
import y.io.GraphMLIOHandler;
import y.io.graphml.GraphMLHandler;
import y.io.graphml.KeyScope;
import y.io.graphml.input.AbstractDataAcceptorInputHandler;
import y.io.graphml.input.GraphMLParseException;
import y.io.graphml.input.GraphMLParseContext;
import y.io.graphml.input.ParseEvent;
import y.io.graphml.input.ParseEventListenerAdapter;
import y.io.graphml.input.InputHandlerProvider;
import y.io.graphml.input.QueryInputHandlersEvent;
import y.io.graphml.output.AbstractDataProviderOutputHandler;
import y.io.graphml.output.GraphMLWriteException;
import y.io.graphml.output.GraphMLXmlAttribute;
import y.io.graphml.output.OutputHandlerProvider;
import y.io.graphml.output.QueryOutputHandlersEvent;
import y.io.graphml.output.GraphMLWriteContext;
import y.util.Maps;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;
import java.awt.EventQueue;

/**
 * Demo application that shows how input handlers for GraphML attributes can be dynamically registered,
 * depending on the actual input.
 *
 * <p>
 * Each unknown attribute is saved by means of an input handler that creates document fragments
 * from the <code>data</code> element content and stores these in a map.
 * </p>
 * <p>
 * <b>Note:</b> Due to the way how nested graphs are handled internally by yFiles, data content that is bound to
 * <b>nested</b> graph elements will not be handled correctly by this demo (you'll have to associate the data with the
 * parent node instead, which is not shown here to keep the demo short). 
 * </p>
 */
public class DynamicAttributesDemo extends GraphMLDemo {

  protected void loadInitialGraph() {
    dynamicAttributes = new LinkedList();
    //We load a graph that contains already some attributes - note that we don't have to know which attributes are present in the file.
    loadGraph("resources/custom/simple-attributes.graphml");
  }

  /**
   * Creates a customized GraphMLHandler since we want to listen to a parse event.
   */
  protected GraphMLIOHandler createGraphMLIOHandler() {
    GraphMLIOHandler ioHandler = new GraphMLIOHandler();
    ioHandler.getGraphMLHandler().addParseEventListener(
        new ParseEventListenerAdapter() {
          public void onGraphMLParsing(ParseEvent event) {
            //reset the attribute list whenever we started to parse a new document.
            dynamicAttributes.clear();
          }
        });
    //Register event listeners that handle our dynamic attributes...
    ioHandler.getGraphMLHandler().addInputHandlerProvider(new DynamicAttributesInputHandlerProvider());
    ioHandler.getGraphMLHandler().addOutputHandlerProvider(new DynamicAttributesOutputHandlerProvider());

    return ioHandler;
  }

  /**
   * Helper class that captures GraphML key definition data for roundtrips.
   */
  private static class AttributeDescriptor {
    private Set attributeMetadata = new HashSet();
    private DataMap dataMap;
    private final KeyScope scope;


    public AttributeDescriptor(DataMap dataMap, KeyScope scope) {
      this.dataMap = dataMap;
      this.scope = scope;
    }

    public void addAttribute(GraphMLXmlAttribute attr) {
      attributeMetadata.add(attr);
    }

    public Collection getAttributeMetadata() {
      return attributeMetadata;
    }

    public DataMap getDataMap() {
      return dataMap;
    }

    public KeyScope getScope() {
      return scope;
    }

    public Object getDefaultValue() {
      return defaultValue;
    }

    public void setDefaultValue(Object defaultValue) {
      this.defaultValue = defaultValue;
    }

    private Object defaultValue;

    public boolean isDefaultSet() {
      return isDefaultSet;
    }

    public void setDefaultSet(boolean defaultSet) {
      isDefaultSet = defaultSet;
    }

    private boolean isDefaultSet;
  }

  private Collection dynamicAttributes;

  private class DynamicAttributesInputHandlerProvider implements InputHandlerProvider {
    public void onQueryInputHandler(QueryInputHandlersEvent event) throws GraphMLParseException {
      if (!event.isHandled()) {
        //If the event is not handled, we register a new Input handler that stores the
        //XML representation in a map
        DataMap map = Maps.createDataMap(new HashMap());
        KeyScope scope = GraphMLHandler.getKeyScope(event.getKeyDefinition());

        //Store the attribute data along with the key definition...
        //Aditionally, we could register the map as a data provider on the graph
        //(however, since we really only want to store them for a roundtrip, we just don't care...)
        AttributeDescriptor descriptor = new AttributeDescriptor(map, scope);
        dynamicAttributes.add(descriptor);

        XmlInputHandler handler = null;
        try {
          handler = new XmlInputHandler(descriptor);
        } catch (ParserConfigurationException e) {
          throw new GraphMLParseException("Error configuring internal DocumentBuilder", e);
        }
        handler.initializeFromKeyDefinition(event.getContext(), event.getKeyDefinition());
        event.addInputHandler(handler);
      }
    }
  }

  private class DynamicAttributesOutputHandlerProvider implements OutputHandlerProvider {
    public void onQueryOutputHandler(QueryOutputHandlersEvent event) throws GraphMLWriteException {
      for (Iterator iterator = dynamicAttributes.iterator(); iterator.hasNext();) {
        //For all attribute descriptors, we add an output handler that can write
        //the data (actually just the document fragments in the map)
        AttributeDescriptor descriptor = (AttributeDescriptor) iterator.next();
        KeyScope scope = descriptor.getScope();
        XmlOutputHandler outputHandler = new XmlOutputHandler(descriptor);
        event.addOutputHandler(outputHandler, scope);
      }
    }
  }

  /**
   * Custom input handler that stores data nodes as DocumentFragments and captures the key definition
   */
  private static class XmlInputHandler extends AbstractDataAcceptorInputHandler {

    private final DocumentBuilder documentBuilder;

    public XmlInputHandler(AttributeDescriptor descriptor) throws ParserConfigurationException {
      this.descriptor = descriptor;
      setDataAcceptor(descriptor.getDataMap());
      DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
      builderFactory.setNamespaceAware(true);
      documentBuilder = builderFactory.newDocumentBuilder();
    }

    private AttributeDescriptor descriptor;

    protected Object parseDataCore(GraphMLParseContext context, Node node) throws GraphMLParseException {
      NodeList children = node.getChildNodes();
      //Extract the data nodes' content into a document fragment which will be
      //saved in the map later on...
      Document targetDoc = documentBuilder.newDocument();
      DocumentFragment fragment = node.getOwnerDocument().createDocumentFragment();
      if (children != null) {
        for (int i = 0; i < children.getLength(); i++) {
          Node n = children.item(i);
          fragment.appendChild(n.cloneNode(true));
        }
      }
      Element rootElement = targetDoc.createElement("DocumentRoot");
      targetDoc.appendChild(rootElement);
      Node node1 = targetDoc.importNode(fragment, true);
      rootElement.appendChild(node1);
      return targetDoc;
    }

    public void initializeFromKeyDefinition(GraphMLParseContext context, Element definition) throws
        GraphMLParseException {
      super.initializeFromKeyDefinition(context, definition);

      //Rescue values from the key definition into descriptor, so that they can be reused for writing the attribute
      NamedNodeMap attributes = definition.getAttributes();
      for (int i = 0; i < attributes.getLength(); ++i) {
        Node attr = attributes.item(i);
        descriptor.addAttribute(new GraphMLXmlAttribute(attr.getNodeName(), attr.getNamespaceURI(),
            attr.getNodeValue()));
      }
      descriptor.setDefaultSet(isDefaultExists());
      descriptor.setDefaultValue(getDefaultValue());
    }
  }

  /**
   * Custom output handler that writes DocumentFragments as data nodes and reuses a given key definition
   */
  private static class XmlOutputHandler extends AbstractDataProviderOutputHandler {

    public XmlOutputHandler(AttributeDescriptor descriptor) {
      this.descriptor = descriptor;
      setDefaultValue(descriptor.getDefaultValue());
      setDataProvider(descriptor.getDataMap());
      setDefaultValueAssigned(descriptor.isDefaultSet());
    }

    private AttributeDescriptor descriptor;

    public Collection getKeyDefinitionAttributes() {
      return descriptor.getAttributeMetadata();
    }

    protected void writeValueCore(GraphMLWriteContext context, Object data) throws GraphMLWriteException {
      if (data != null) {
        //Just dump the document fragment...
        Document savedDoc = (Document) data;

        DocumentFragment fragment = savedDoc.createDocumentFragment();
        NodeList children = savedDoc.getDocumentElement().getChildNodes();
        if (children != null) {
          for (int i = 0; i < children.getLength(); i++) {
            Node n = children.item(i);
            fragment.appendChild(n.cloneNode(true));
          }
        }
        context.getWriter().writeDocumentFragment(fragment);
      }
    }
  }

  /**
   * Add sample graphs to the menu.
   */
  protected String[] getSampleFiles() {
    return new String[]{
        "resources/custom/simple-attributes.graphml",
        "resources/custom/complexdemo.graphml",
    };
  }

  /**
   * Launches this demo.
   */
  public static void main(String[] args) {
    EventQueue.invokeLater(new Runnable() {
      public void run() {
        initLnF();
        (new DynamicAttributesDemo()).start();
      }
    });
  }
}
