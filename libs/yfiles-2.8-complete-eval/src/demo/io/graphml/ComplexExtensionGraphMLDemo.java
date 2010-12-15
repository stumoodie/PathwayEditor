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

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import demo.view.DemoDefaults;

import y.base.DataMap;
import y.base.EdgeMap;
import y.base.Node;
import y.base.NodeMap;
import y.io.GraphMLIOHandler;
import y.io.graphml.GraphMLHandler;
import y.io.graphml.KeyScope;
import y.io.graphml.KeyType;
import y.io.graphml.input.AbstractDataAcceptorInputHandler;
import y.io.graphml.input.DeserializationEvent;
import y.io.graphml.input.DeserializationHandler;
import y.io.graphml.input.GraphMLParseContext;
import y.io.graphml.input.GraphMLParseException;
import y.io.graphml.input.InputHandlerProvider;
import y.io.graphml.input.NameBasedDeserializer;
import y.io.graphml.input.QueryInputHandlersEvent;
import y.io.graphml.output.AbstractOutputHandler;
import y.io.graphml.output.GraphMLWriteContext;
import y.io.graphml.output.GraphMLWriteException;
import y.io.graphml.output.SerializationEvent;
import y.io.graphml.output.SerializationHandler;
import y.io.graphml.output.TypeBasedSerializer;
import y.io.graphml.output.XmlWriter;
import y.option.OptionHandler;
import y.view.EditMode;
import y.view.HitInfo;
import y.view.PopupMode;
import y.view.ViewMode;

import javax.swing.AbstractAction;
import javax.swing.JPopupMenu;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.StringTokenizer;

/**
 * This demo shows how to configure GraphMLIOHandler to be able to handle
 * extra node and graph data of complex type.
 * Additional data for a node and edge can be edited by right-clicking on the corresponding
 * element. To edit the graph data, right-click on the canvas background.
 * The element tool tip will show the currently set data values for each element.
 */
public class ComplexExtensionGraphMLDemo extends GraphMLDemo {

  /**
   * Store node/edge data
   */
  private NodeMap nodeDataMap;
  private EdgeMap edgeDataMap;

  public ComplexExtensionGraphMLDemo() {
    //define a view mode that displays the currently set data values
    ViewMode tooltipMode = new ViewMode() {
      public void mouseMoved(double x, double y) {
        HitInfo info = getHitInfo(x, y);
        Object hitObject = null;

        if (info.hasHitNodes()) {
          hitObject = info.getHitNode();
          Collection items = (Collection) nodeDataMap.get(hitObject);
          String tipText = "<html>Items:<table>";
          if (items != null) {
            for (Iterator iterator = items.iterator(); iterator.hasNext();) {
              tipText += "</tr></td>" + iterator.next() + "</td></tr>";
            }
            tipText += "</table>";
            view.setToolTipText(tipText);
          }
        } else if (info.hasHitEdges()) {
          hitObject = info.getHitEdge();
          Object o = edgeDataMap.get(hitObject);
          if (o instanceof Date) {
            view.setToolTipText(o.toString());
          }
        }
      }
    };

    //add the view mode to the view
    view.addViewMode(tooltipMode);
  }

  protected void loadInitialGraph() {
    loadGraph("resources/custom/complexdemo.graphml");    
  }

  /**
   * Create a GraphMLIOHandler that reads and writes additional node and graph data
   * of complex type.
   */
  protected GraphMLIOHandler createGraphMLIOHandler() {
    if (nodeDataMap == null) {
      nodeDataMap = view.getGraph2D().createNodeMap();
    }
    if (edgeDataMap == null) {
      edgeDataMap = view.getGraph2D().createEdgeMap();
    }


    GraphMLIOHandler ioHandler = super.createGraphMLIOHandler();


    //For our top-level node data, which consists of {@link Item} collections, we add (de)serializers only for
    //the parsing of the node attributes
    ioHandler.getGraphMLHandler().addInputDataAcceptor("myNodeAttribute", nodeDataMap, KeyScope.NODE,
        new ItemListDeserializer());

    ioHandler.getGraphMLHandler().addOutputDataProvider("myNodeAttribute", nodeDataMap, KeyScope.NODE,
        new ItemListSerializer());

    //We add serializers/deserializers for Item objects globally (so they can be used from inside other
    //(de)serializers
    ioHandler.getGraphMLHandler().addDeserializationHandler(new ItemDeserializer());
    ioHandler.getGraphMLHandler().addSerializationHandler(new ItemSerializer());

    //Add an explicit input handler to parse our edge data (just to show how it works - usually, a deserializer for date would be easier...)
    ioHandler.getGraphMLHandler().addInputHandlerProvider(new InputHandlerProvider() {
      public void onQueryInputHandler(QueryInputHandlersEvent event) throws GraphMLParseException {
        Element keyDefinition = event.getKeyDefinition();
        if (!event.isHandled()
            && GraphMLHandler.matchesScope(keyDefinition, KeyScope.EDGE)
            && GraphMLHandler.matchesName(keyDefinition, "myEdgeAttribute")) {
          MyDateInputHandler handler = new MyDateInputHandler();
          handler.setDataAcceptor(edgeDataMap);
          handler.initializeFromKeyDefinition(event.getContext(), keyDefinition);
          event.addInputHandler(handler);
        }
      }
    });

    //Add an explicit output handler to write our edge data (just to show how it works - usually, a serializer for date would be easier...)
    ioHandler.getGraphMLHandler().addOutputHandlerProvider(
        new AbstractOutputHandler("myEdgeAttribute", KeyScope.EDGE, KeyType.COMPLEX) {
          protected void writeValueCore(GraphMLWriteContext context, Object data) throws GraphMLWriteException {
            if (data instanceof Date) {
              context.getWriter().writeText(dateFormat.format(data));
            }
          }

          protected Object getValue(GraphMLWriteContext context, Object key) throws GraphMLWriteException {
            return edgeDataMap.get(key);
          }
        });

    return ioHandler;
  }


  public static final String DEMO_NS = "demo.io.graphml.complex";

  /**
   * Example class that is used as an example for complex objects
   */
  public static class Item {

    public Item(String value) {
      this.value = value;
    }

    public String getValue() {
      return value;
    }

    private String value;


    public String toString() {
      return value.toString();
    }
  }

  /**
   * Custom Serializer for {@link Item} objects
   */
  public static class ItemSerializer extends TypeBasedSerializer {
    public void serializeItem(Object o, XmlWriter writer, GraphMLWriteContext context) throws GraphMLWriteException {
      Item item = (Item) o;
      writer.writeStartElement("Item", DEMO_NS).writeAttribute("value", item.getValue()).writeEndElement();
    }

    protected Class getSerializationType(GraphMLWriteContext context) {
      //We are only valid for Item objects
      return Item.class;
    }
  }

  /**
   * Custom deserializer for {@link Item} objects
   */
  public static class ItemDeserializer extends NameBasedDeserializer {
    public Object deserializeNode(org.w3c.dom.Node xmlNode, GraphMLParseContext context) throws GraphMLParseException {
      if (xmlNode.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE
          && DEMO_NS.equals(xmlNode.getNamespaceURI())
          && "Item".equals(xmlNode.getLocalName())) {
        return new Item(((Element) xmlNode).getAttribute("value"));
      }
      return null;
    }


    public String getNamespaceURI(GraphMLParseContext context) {
      return DEMO_NS;
    }

    public String getNodeName(GraphMLParseContext context) {
      return "Item";
    }
  }

  /**
   * Custom serializer for Collections of {@link Item}s
   */
  public static class ItemListSerializer implements SerializationHandler {

    public void onHandleSerialization(SerializationEvent event) throws GraphMLWriteException {
      Object o = event.getItem();
      if (o instanceof Collection) {
        Collection coll = (Collection) o;
        event.getWriter().writeStartElement("ItemList", DEMO_NS);
        for (Iterator iterator = coll.iterator(); iterator.hasNext();) {
          Object item = iterator.next();
          event.getContext().serialize(item);
        }
        event.getWriter().writeEndElement();
      }
    }
  }

  /**
   * Custom deserializer for Collections of {@link Item}s
   */
  public static class ItemListDeserializer implements DeserializationHandler {
    public void onHandleDeserialization(DeserializationEvent event) throws GraphMLParseException {
      org.w3c.dom.Node xmlNode = event.getXmlNode();
      if (xmlNode.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE
          && DEMO_NS.equals(xmlNode.getNamespaceURI())
          && "ItemList".equals(xmlNode.getLocalName())) {
        Collection retval = new ArrayList();
        NodeList childNodes = xmlNode.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); ++i) {
          org.w3c.dom.Node child = childNodes.item(i);
          if (child.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) {
            Object o = event.getContext().deserialize(child);
            retval.add(o);
          }
        }
        event.setResult(retval);
      }
    }
  }

  private static SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yy");

  /** Explicit input handler for Date attributes */
  public static class MyDateInputHandler extends AbstractDataAcceptorInputHandler {
    protected Object parseDataCore(GraphMLParseContext context, org.w3c.dom.Node node) throws GraphMLParseException {
      if (node.getChildNodes().getLength() != 1) {
        throw new GraphMLParseException("Invalid data format - single text node expected");
      }
      org.w3c.dom.Node n = node.getFirstChild();
      if (n.getNodeType() == org.w3c.dom.Node.TEXT_NODE) {
        try {
          return dateFormat.parse(n.getNodeValue());
        } catch (ParseException e) {
          throw new GraphMLParseException("Invalid date format: " + e.getMessage(), e);
        }
      } else {
        throw new GraphMLParseException("Invalid data format - Text node expected");
      }
    }
  }



  protected String[] getSampleFiles() {
    return null;
  }

  /**
   * Create an edit mode that displays a context-sensitive popup-menu when right-clicking
   * on an the canvas.
   */
  protected EditMode createEditMode() {
    EditMode editMode = super.createEditMode();

    editMode.setPopupMode(new PopupMode() {
      public JPopupMenu getNodePopup(Node v) {
        JPopupMenu pm = new JPopupMenu();
        pm.add(new EditAttributeAction("Edit Node Attribute...", v, nodeDataMap));
        return pm;
      }
    });
    return editMode;
  }


  /**
   * Editor action for the additional node and edge attributes.
   */
  class EditAttributeAction extends AbstractAction {
    private Object object;
    private DataMap dataMap;

    private OptionHandler op;

    EditAttributeAction(String name, Object object, DataMap dataMap) {
      super(name);
      this.object = object;
      this.dataMap = dataMap;
      op = new OptionHandler(name);
      if (object instanceof Node) {
        Object o = dataMap.get(object);
        if (o instanceof Collection) {
          Collection coll = (Collection) o;
          String str = "";
          for (Iterator iterator = coll.iterator(); iterator.hasNext();) {
            str += iterator.next();
            if (iterator.hasNext()) {
              str += "\n";
            }
          }
          op.addString("Node Items", str, 10);
        } else {
          op.addString("Node Items", "", 10);
        }
      }      
    }

    public void actionPerformed(ActionEvent actionEvent) {
      if (op.showEditor()) {
        if (object instanceof Node) {          
          Collection coll = new ArrayList();
          String s = op.getString("Node Items");
          StringTokenizer tokenizer = new StringTokenizer(s, "\n");
          while (tokenizer.hasMoreElements()) {
            String s1 = (String) tokenizer.nextElement();
            coll.add(new Item(s1));
          }
          dataMap.set(object, coll);          
        }
        graphMLPane.updateGraphMLText(view.getGraph2D());        
      }
    }
  }

  /**
   * Launches this demo.
   */
  public static void main(String[] args) {
    EventQueue.invokeLater(new Runnable() {
      public void run() {
        initLnF();
        (new ComplexExtensionGraphMLDemo()).start();
      }
    });
  }
}
