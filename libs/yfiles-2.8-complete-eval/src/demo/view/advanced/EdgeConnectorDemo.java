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
package demo.view.advanced;

import java.awt.Graphics2D;
import java.awt.EventQueue;
import java.awt.geom.GeneralPath;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.WeakHashMap;

import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;

import org.w3c.dom.Element;

import demo.view.DemoBase;
import demo.view.DemoDefaults;

import y.base.DataMap;
import y.base.Edge;
import y.base.EdgeCursor;
import y.base.EdgeList;
import y.base.Graph;
import y.base.GraphEvent;
import y.base.GraphListener;
import y.base.Node;
import y.base.NodeCursor;
import y.base.NodeList;
import y.base.YList;
import y.geom.AffineLine;
import y.geom.YPoint;
import y.geom.YVector;
import y.io.GraphMLIOHandler;
import y.io.graphml.KeyScope;
import y.io.graphml.KeyType;
import y.io.graphml.input.GraphMLParseContext;
import y.io.graphml.input.GraphMLParseException;
import y.io.graphml.input.NameBasedDeserializer;
import y.io.graphml.input.ParseEventListenerAdapter;
import y.io.graphml.output.AbstractOutputHandler;
import y.io.graphml.output.GraphElementIdProvider;
import y.io.graphml.output.GraphMLWriteContext;
import y.io.graphml.output.GraphMLWriteException;
import y.io.graphml.output.XmlWriter;
import y.util.DataAcceptorAdapter;
import y.util.Maps;
import y.util.Tuple;
import y.view.Bend;
import y.view.BendCursor;
import y.view.BendList;
import y.view.CreateEdgeMode;
import y.view.DefaultGraph2DRenderer;
import y.view.EditMode;
import y.view.GenericNodeRealizer;
import y.view.Graph2D;
import y.view.Graph2DTraversal;
import y.view.Graph2DViewActions;
import y.view.HitInfo;
import y.view.MovePortMode;
import y.view.MoveSelectionMode;
import y.view.NodeRealizer;
import y.view.Port;
import y.view.ShapeNodePainter;

/**
 * Class that shows how to mimic node-to-edge and edge-to-edge connections. In this demo an edge that connects
 * to a node or to another edge is modeled as a normal edge that has a special node as its end point. That special
 * node is located on the path of the edge. When moving the edge path the special node will also be moved. Thus,
 * it looks and feels like a proper edge connection to an edge.
 * <p>
 * Usage: to create an edge that starts at another edge, shift-press on the edge to initiate the
 * edge creation gesture, then drag the mouse. To create an edge that ends at another edge,
 * shift-release the mouse on the edge.
 * </p>
 */
public class EdgeConnectorDemo extends DemoBase {
  
  /**
   * Create a GenericNodeRealizer configuration for nodes that represent edge connectors
   */
  static {
    Map configurationMap = GenericNodeRealizer.getFactory().createDefaultConfigurationMap();
    ShapeNodePainter painter = new ShapeNodePainter();
    painter.setShapeType(ShapeNodePainter.ELLIPSE);
    configurationMap.put(y.view.GenericNodeRealizer.Painter.class, painter);
    GenericNodeRealizer.getFactory().addConfiguration("EdgeConnector", configurationMap);
  }
  
  protected void initialize() {
    super.initialize();
    view.setAntialiasedPainting(true);
    EdgeConnectorGraph2DRenderer r = new EdgeConnectorGraph2DRenderer();
    r.setDrawEdgesFirst(true);
    view.setGraph2DRenderer(r);
    view.getGraph2D().addGraphListener(new EdgeConnectorListener());
    loadGraph("resource/EdgeConnectorDemo.graphml");
  }

  protected void registerViewModes() {
    EditMode editMode = new EdgeConnectorEditMode();
    editMode.setCreateEdgeMode(new CreateEdgeConnectorMode());
    editMode.setMoveSelectionMode(new EdgeConnectorMoveSelectionMode());
    editMode.setMovePortMode(new EdgeConnectorMovePortMode());
    view.addViewMode(editMode);
  }

  /**
   * Special Graph2DRenderer that updates the edge connector locations before graph elements
   * are rendered to the view.
   */
  static class EdgeConnectorGraph2DRenderer extends DefaultGraph2DRenderer {
    public void paint(final Graphics2D gfx, final Graph2D graph) {
      for (NodeCursor nc = graph.nodes(); nc.ok(); nc.next()) {
        Node n = nc.node();
        if(EdgeConnectorManager.isEdgeConnector(n)) {          
          updateEdgeConnectorLocation(n);
        }
      }
      super.paint(gfx, graph);
    }
    
    public void updateEdgeConnectorLocation(Node node) {      
      if(node != null) {
        Edge edge = EdgeConnectorManager.getEdgeConnection(node);
        if(edge != null) {
          Graph2D graph = (Graph2D) node.getGraph();
          double ratio = EdgeConnectorManager.getEdgeConnectionRatio(node);
          try {
            Point2D point = PointPathProjector.getPointForGlobalRatio(graph.getRealizer(edge).getPath(), ratio);
            NodeRealizer nr = graph.getRealizer(node);
            nr.setCenter(point.getX(), point.getY());
          }catch(IllegalStateException isex) {}
        }
      }
    }
  }
  
  /**
   * Create a GraphMLIOHandler that will serialize and deserialize data that is associated with edge connector nodes. 
   */
  protected GraphMLIOHandler createGraphMLIOHandler() {
    GraphMLIOHandler ioHandler = super.createGraphMLIOHandler();
    
    ioHandler.getGraphMLHandler().addOutputHandlerProvider(new AbstractOutputHandler("edgeConnectingData", KeyScope.NODE, KeyType.COMPLEX) {      
      protected void writeValueCore(GraphMLWriteContext context, Object data)
          throws GraphMLWriteException {
        if(data != null) {
          Tuple tuple = (Tuple) data;
          Edge edge = (Edge) tuple.o1;
          double ratio = ((Double)tuple.o2).doubleValue();
          XmlWriter writer = context.getWriter();
          GraphElementIdProvider idProvider = (GraphElementIdProvider) context.lookup(GraphElementIdProvider.class);
          String edgeId = idProvider.getEdgeId(edge, context);
          writer.writeStartElement("connectorData", "demo");
          writer.writeAttribute("edgeId", edgeId);
          writer.writeAttribute("ratio", ratio);
          writer.writeEndElement();
        }
      }      
      
      protected Object getValue(GraphMLWriteContext context, Object key)
          throws GraphMLWriteException {
        return EdgeConnectorManager.map.get(key);        
      }
    });

    ioHandler.getGraphMLHandler().addOutputHandlerProvider(new AbstractOutputHandler("edgeId", KeyScope.EDGE, KeyType.STRING) {      
      protected void writeValueCore(GraphMLWriteContext context, Object data)
          throws GraphMLWriteException {
        if(data != null) {
          XmlWriter writer = context.getWriter();
          writer.writeText(data.toString());
        }
      }      
      
      protected Object getValue(GraphMLWriteContext context, Object key)
          throws GraphMLWriteException {
        GraphElementIdProvider idProvider = (GraphElementIdProvider) context.lookup(GraphElementIdProvider.class);
        return idProvider.getEdgeId((Edge) key, context);        
      }
    });

    final DataMap edgeIdMap = Maps.createHashedDataMap();          
    ioHandler.getGraphMLHandler().addInputDataAcceptor("edgeId", 
        new DataAcceptorAdapter() {
          public void set(Object dataHolder, Object value) {
            edgeIdMap.set(value, dataHolder);
          }
        },
        KeyScope.EDGE, KeyType.STRING);
    
    
    final DataMap tempConnectorMap = Maps.createHashedDataMap();
      
    ioHandler.getGraphMLHandler().addInputDataAcceptor("edgeConnectingData", tempConnectorMap, KeyScope.NODE, new NameBasedDeserializer() {      
      public Object deserializeNode(org.w3c.dom.Node xmlNode,
          GraphMLParseContext context) throws GraphMLParseException {
        Element xmlElem = (Element) xmlNode;
        String edgeId = xmlElem.getAttribute("edgeId");
        String doubleStr = xmlElem.getAttribute("ratio");
        return new Tuple(edgeId, doubleStr);        
      }
      
      public String getNodeName(GraphMLParseContext context) {
        return "connectorData";
      }
      
      public String getNamespaceURI(GraphMLParseContext context) {
        return "demo";
      }
    });
    
    ioHandler.getGraphMLHandler().addParseEventListener(new ParseEventListenerAdapter() {
      public void onGraphMLParsed(y.io.graphml.input.ParseEvent event) {
        Graph2D graph = (Graph2D) event.getContext().getGraph();
        for (NodeCursor nc = graph.nodes(); nc.ok(); nc.next()) {
          Node n = nc.node();
          Tuple tuple = (Tuple) tempConnectorMap.get(n);
          if(tuple != null) {
            Edge edge = (Edge) edgeIdMap.get(tuple.o1);
            Double ratio = Double.valueOf(tuple.o2.toString());
            EdgeConnectorManager.map.put(n, new Tuple(edge, ratio));
          }
        }
      }
    });
    return ioHandler;
  }
  
  protected void registerViewActions() {
    //register keyboard actions
    Graph2DViewActions actions = new Graph2DViewActions(view);
    ActionMap amap = actions.createActionMap();
    InputMap imap = actions.createDefaultInputMap(amap);
    if (!isDeletionEnabled()) {
      amap.remove(Graph2DViewActions.DELETE_SELECTION);
    }
    view.getCanvasComponent().setActionMap(amap);
    view.getCanvasComponent().setInputMap(JComponent.WHEN_FOCUSED, imap);
  }

  /**
   * Manages edge-to-edge dependency information.
   */
  static class EdgeConnectorManager {
    static final Map map = new WeakHashMap();

    private EdgeConnectorManager() {
    }

    static boolean isEdgeConnector(Node n) {
      return map.containsKey(n);
    }

    static void addEdgeConnection(Node connector, Edge edge, double pathRatio) {
      map.put(connector, Tuple.create(edge, new Double(pathRatio)));
    }

    static Edge getEdgeConnection(Node connector) {
      Tuple tuple = (Tuple) map.get(connector);
      if (tuple != null) {
        return (Edge) tuple.o1;
      }
      return null;
    }

    static double getEdgeConnectionRatio(Node connector) {
      Tuple tuple = (Tuple) map.get(connector);
      if (tuple != null) {
        return ((Double) tuple.o2).doubleValue();
      }
      return 0.0;  //should throw an exception
    }

    static NodeList getConnectorNodes(Edge edge) {
      NodeList result = new NodeList();
      for (Iterator iter = map.entrySet().iterator(); iter.hasNext();) {
        Map.Entry entry = (Map.Entry) iter.next();
        Tuple value = (Tuple) entry.getValue();
        if (value.o1 == edge) {
          result.add(entry.getKey());
        }
      }
      return result;
    }
    
    static NodeRealizer createEdgeConnectorRealizer() {
      GenericNodeRealizer gnr = new GenericNodeRealizer("EdgeConnector");      
      gnr.setSize(5,5);
      gnr.setFillColor(DemoDefaults.DEFAULT_CONTRAST_COLOR);
      return gnr;
    }
    
  }

  /**
   * Graph listener that automatically removes edges that connect to edges that
   * are to be removed.
   * This implementation assumes that all edge removal operations are triggered
   * through user interaction, i.e. that <em>all</em> edge removal events are
   * bracketed in <code>PRE</code> and <code>POST</code> events.
   */
  static class EdgeConnectorListener implements GraphListener {
    /** The current event block */
    private int block;
    /** Stores edges by event block */
    private Map block2edges;
    /** Stores the active/inactive state of this listener */
    private boolean armed;

    EdgeConnectorListener() {
      armed = true;
    }

    public void onGraphEvent(final GraphEvent e) {
      if (!armed) {
        return;
      }

      switch (e.getType()) {
        case GraphEvent.PRE_EVENT:
          ++block;
          break;
        case GraphEvent.POST_EVENT:
          handleBlock();
          --block;
          break;
        case GraphEvent.POST_EDGE_REMOVAL:
          storeForHandleBlock((Edge) e.getData());
          break;
      }
    }

    /**
     * Stores the specified edge for later processing upon completion of the
     * current event block.
     */
    private void storeForHandleBlock( final Edge e ) {
      if (block2edges == null) {
        block2edges = new HashMap();
      }
      final Integer key = new Integer(block);
      EdgeList edges = (EdgeList) block2edges.get(key);
      if (edges == null) {
        edges = new EdgeList();
        block2edges.put(key, edges);
      }
      edges.add(e);
    }

    /**
     * Handles cleanup of the edge-to-edge connection data upon completion
     * of the current event block.
     */
    private void handleBlock() {
      if (block2edges == null) {
        return;
      }

      final EdgeList el = (EdgeList) block2edges.remove(new Integer(block));
      if (el == null) {
        return;
      }

      armed = false;
      handleRecursive(el);
      armed = true;

      if (block2edges.isEmpty()) {
        block2edges = null;
      }
    }

    private void handleRecursive( final EdgeList el ) {
      final EdgeList cascade = new EdgeList();
      for (EdgeCursor ec = el.edges(); ec.ok(); ec.next()) {
        final Edge edge = ec.edge();
        Node node;
        node = edge.source();
        if (EdgeConnectorManager.getEdgeConnection(node) != null) {
          final Graph graph = node.getGraph();
          if (graph != null && node.degree() == 0) {
            graph.removeNode(node);
          }
        }
        node = edge.target();
        if (EdgeConnectorManager.getEdgeConnection(node) != null) {
          final Graph graph = node.getGraph();
          if (graph != null && node.degree() == 0) {
            graph.removeNode(node);
          }
        }
        final NodeList connectors = EdgeConnectorManager.getConnectorNodes(edge);
        if (connectors != null) {
          for (NodeCursor nc = connectors.nodes(); nc.ok(); nc.next()) {
            node = nc.node();
            final Graph graph = node.getGraph();
            if (graph != null) {
              for (EdgeCursor nec = node.edges(); nec.ok(); nec.next()) {
                cascade.add(nec.edge());
              }
              graph.removeNode(node);
            }
          }
        }
      }

      if (!cascade.isEmpty()) {
        handleRecursive(cascade);
      }
    }
  }
//
//  /**
//   * Represents the end point of an edge that connects to another edge. Note that
//   * with this implementation a call to updateLocation enforces that the location
//   * of the node will be on the corresponding edge path. In this demo the call to
//   * updateLocation is performed by the Graph2DRenderer implementation EdgeConnectorGraph2DRenderer.
//   */
//  static class EdgeConnectorRealizer extends ShapeNodeRealizer {
//    public EdgeConnectorRealizer() {
//      setShapeType(ELLIPSE);
//      setSize(5,5);
//      setFillColor(Color.yellow);
//    }
//
//    public EdgeConnectorRealizer(NodeRealizer nr) {
//      super(nr);
//    }
//    public NodeRealizer createCopy(NodeRealizer nr) {
//      return new EdgeConnectorRealizer(nr);
//    }
//
//
////    public void calcUnionRect(Rectangle2D r) {
////      updateLocation();
////      super.calcUnionRect(r);
////    }
//
////    public void paint(Graphics2D gfx) {
////      updateLocation();
////      super.paintNode(gfx);
////    }
//  }

  /**
   * Extends MoveSelectionMode to also handle edge-to-edge connections.
   */
  static class EdgeConnectorMoveSelectionMode extends MoveSelectionMode {
    protected NodeList getNodesToBeMoved() {
      NodeList result = super.getNodesToBeMoved();
      for(NodeCursor nc = result.nodes(); nc.ok(); nc.next()) {
        Node n = nc.node();
        for(EdgeCursor ec = n.edges(); ec.ok(); ec.next()) {
          Edge edge = ec.edge();
          NodeList connectors = EdgeConnectorManager.getConnectorNodes(edge);
          result.splice(connectors);
        }
      }
      BendList bends = getBendsToBeMoved();
      for (BendCursor bc = bends.bends(); bc.ok(); bc.next()) {
        Bend b = bc.bend();
        NodeList connectors = EdgeConnectorManager.getConnectorNodes(b.getEdge());
        result.splice(connectors);
      }
      return result;
    }
  }

  /**
   * Extends CreateEdgeMode to also handle edge-to-edge connections.
   */
  static class CreateEdgeConnectorMode extends CreateEdgeMode {
    private Node startNode;

    public void mouseShiftPressedLeft(double x, double y) {
      if(isEditing()) {
        super.mouseShiftPressedLeft(x,y);
      }
      else {
        Graph2D graph = getGraph2D();
        Edge edge = getHitInfo(x,y).getHitEdge();
        if (edge != null) {
          NodeRealizer ecNR = EdgeConnectorManager.createEdgeConnectorRealizer();
          Point2D p = new Point2D.Double(x, y);
          double[] result = PointPathProjector.calculateClosestPathPoint(graph.getRealizer(edge).getPath(), p);
          ecNR.setCenter(result[0], result[1]);
          //ecNR.setCenter(x,y);
          startNode = getGraph2D().createNode(ecNR);
          view.updateView();
          super.mouseShiftPressedLeft(result[0], result[1]);
          EdgeConnectorManager.addEdgeConnection(startNode, edge, result[5]);
        }
        else {
          startNode = null;
          super.mouseShiftPressedLeft(x, y);
        }
      }
    }

    public void mouseShiftReleasedLeft(double x, double y) {
      Graph2D graph = getGraph2D();
      Edge edge = getHitInfo(x, y).getHitEdge();
      if (edge != null) {
        NodeRealizer ecNR = EdgeConnectorManager.createEdgeConnectorRealizer();
        Point2D p = new Point2D.Double(x, y);
        double[] result = PointPathProjector.calculateClosestPathPoint(graph.getRealizer(edge).getPath(), p);
        ecNR.setCenter(result[0], result[1]);
        Node endNode = getGraph2D().createNode(ecNR);
        view.updateView();
        super.mouseShiftReleasedLeft(result[0], result[1]);
        EdgeConnectorManager.addEdgeConnection(endNode, edge, result[5]);
      } else {
        super.mouseShiftReleasedLeft(x, y);
      }
    }

    public HitInfo getHitInfo(double x, double y) {
      final HitInfo info = view.getHitInfoFactory()
              .createHitInfo(x, y, Graph2DTraversal.ALL, false);
      setLastHitInfo(info);
      return info;
    }

    protected void cancelEdgeCreation() {
      if(startNode != null) {
        getGraph2D().removeNode(startNode);
      }
      super.cancelEdgeCreation();
    }

    public void setEditing(boolean active) {
      if (!active) {
        startNode = null;
      }
      super.setEditing(active);
    }
  }

  static class EdgeConnectorEditMode extends EditMode {
    public void mouseDraggedLeft(double x, double y) {
      if(isModifierPressed(lastPressEvent)) {
        double px = translateX(lastPressEvent.getX());
        double py = translateY(lastPressEvent.getY());
        Edge edge = getHitInfo(px,py).getHitEdge();
        if(edge != null) {
          setChild(getCreateEdgeMode(), lastPressEvent, lastDragEvent);
          return;
        }
      }
      super.mouseDraggedLeft(x, y);
    }
  }

  /**
   * Special MovePortMode that will allow to move the port of an edge that connects to
   * another edge to be moved along the edge path.
   */
  static class EdgeConnectorMovePortMode extends MovePortMode {

    protected YList getPortCandidates(Node v, Edge e, double gridSpacing) {
      Edge connectedEdge = EdgeConnectorManager.getEdgeConnection(v);
      if(connectedEdge != null) {
        Graph2D graph = getGraph2D();
        //v is a connector point
        YList result = new YList();
        YPoint yport = e.source() == v ? graph.getSourcePointAbs(e) : graph.getTargetPointAbs(e);
        Point2D p = new Point2D.Double(yport.x, yport.y);
        double[] pppResult = PointPathProjector.calculateClosestPathPoint(getGraph2D().getRealizer(connectedEdge).getPath(), p);
        result.add(new YPoint(pppResult[0], pppResult[1]));
        return result;
      }
      return super.getPortCandidates(v,e,gridSpacing);
    }

    public void mouseReleasedLeft(double x, double y) {
      Port p = this.port;
      if(p != null) {
        Edge e = p.getOwner().getEdge();
        Node v = null;
        if(p == p.getOwner().getTargetPort()) {
          v = e.target();
        }
        else {
          v = e.source();
        }
        Edge connectedEdge = EdgeConnectorManager.getEdgeConnection(v);
        if(connectedEdge == null) {
          super.mouseReleasedLeft(x,y);
          return;
        }
        else {
          double[] result = PointPathProjector.calculateClosestPathPoint(getGraph2D().getRealizer(connectedEdge).getPath(),  x, y);
          double ratio = result[5];
          EdgeConnectorManager.addEdgeConnection(v, connectedEdge, ratio);
          super.mouseReleasedLeft(x,y);
          getGraph2D().setCenter(v, result[0], result[1]);
          p.setOffsets(0,0);
        }
        getGraph2D().updateViews();
      }
    }
  }

  /**
   * Helper class that provides diverse services related to working with points on a path.
   */
  static class PointPathProjector {
    private PointPathProjector() {
    }

    static double[] calculateClosestPathPoint(GeneralPath path, double px, double py) {
      return calculateClosestPathPoint(path, new Point2D.Double(px,py));
    }

    /**
     * Calculates the point on the path which is closest to the given point.
     * Ties are broken arbitrarily.
     * @param path where to look for the closest point
     * @param p to this point
     * @return double[6]
     * <ul>
     *   <li>x coordinate of the closest point</li>
     *   <li>y coordinate of the closest point</li>
     *   <li>distance of the closest point to given point</li>
     *   <li>index of the segment of the path including the closest point
     *       (as a double starting with 0.0, segments are computed with a
     *       path iterator with flatness 1.0)</li>
     *   <li>ratio of closest point on the the including segment (between 0.0 and 1.0)</li>
     *   <li>ratio of closest point on the entire path (between 0.0 and 1.0)</li>
     * </ul>
     */
    static double[] calculateClosestPathPoint(GeneralPath path, Point2D p) {
      double[] result = new double[6];
      double px = p.getX();
      double py = p.getY();
      YPoint point = new YPoint(px, py);
      double pathLength = 0;

      CustomPathIterator pi = new CustomPathIterator(path, 1.0);
      double[] curSeg = new double[4];
      double minDist;
      if (pi.ok()) {
        curSeg = pi.segment();
        minDist = YPoint.distance(px, py, curSeg[0], curSeg[1]);
        result[0] = curSeg[0];
        result[1] = curSeg[1];
        result[2] = minDist;
        result[3] = 0.0;
        result[4] = 0.0;
        result[5] = 0.0;
      } else {
        // no points in GeneralPath: should not happen in this context
        throw new IllegalStateException("path without any coordinates");
      }

      int segmentIndex = 0;
      double lastPathLength = 0.0;
      do {
        YPoint segmentStart = new YPoint(curSeg[0], curSeg[1]);
        YPoint segmentEnd = new YPoint(curSeg[2], curSeg[3]);
        YVector segmentDirection = new YVector(segmentEnd, segmentStart);
        double segmentLength = segmentDirection.length();
        pathLength += segmentLength;
        segmentDirection.norm();

        AffineLine currentSegment = new AffineLine(segmentStart, segmentDirection);
        AffineLine throughPoint = new AffineLine(point, YVector.orthoNormal(segmentDirection));
        YPoint crossing = AffineLine.getCrossing(currentSegment, throughPoint);
        YVector crossingVector = new YVector(crossing, segmentStart);

        YVector segmentVector = new YVector(segmentEnd, segmentStart);
        double indexEnd = YVector.scalarProduct(segmentVector, segmentDirection);
        double indexCrossing = YVector.scalarProduct(crossingVector, segmentDirection);

        double dist;
        double segmentRatio;
        YPoint nearestOnSegment;
        if (indexCrossing <= 0.0) {
          dist = YPoint.distance(point, segmentStart);
          nearestOnSegment = segmentStart;
          segmentRatio = 0.0;
        } else if (indexCrossing >= indexEnd) {
          dist = YPoint.distance(point, segmentEnd);
          nearestOnSegment = segmentEnd;
          segmentRatio = 1.0;
        } else {
          dist = YPoint.distance(point, crossing);
          nearestOnSegment = crossing;
          segmentRatio = indexCrossing / indexEnd;
        }

        if (dist < minDist) {
          minDist = dist;
          result[0] = nearestOnSegment.getX();
          result[1] = nearestOnSegment.getY();
          result[2] = minDist;
          result[3] = segmentIndex;
          result[4] = segmentRatio;
          result[5] = segmentLength * segmentRatio + lastPathLength;
        }

        segmentIndex++;
        lastPathLength = pathLength;
        pi.next();
      } while (pi.ok());

      if(pathLength > 0) {
        result[5] = result[5] / pathLength;
      } else {
        result[5] = 0.0;
      }
      return result;
    }

    static Point2D getPointForGlobalRatio(GeneralPath path, double globalRatio) {
      if(globalRatio > 1.0 || globalRatio < 0.0) {
        throw new IllegalArgumentException("globalRatio outside of [0,1]");
      }
      double totalPathLength = getPathLength(path);
      double targetPathLength = totalPathLength * globalRatio;
      CustomPathIterator pi = new CustomPathIterator(path, 1.0);
      YPoint segmentStart = null, segmentEnd = null;
      if (pi.isDone()) {
        // no points in GeneralPath: should not happen in this context
        throw new IllegalStateException("path without any coordinates");
      } else {
        segmentStart = pi.segmentStart();
        segmentEnd = pi.segmentEnd();
      }

      double currentPathLength = 0.0;
      double lastPathLength = 0.0;
      while (pi.ok()) {
        YVector segmentDirection = new YVector(segmentEnd, segmentStart);
        double segmentLength = segmentDirection.length();
        currentPathLength += segmentLength;
        if(currentPathLength / totalPathLength >= globalRatio) {
          double remainingLength = targetPathLength - lastPathLength;
          double localRatio = remainingLength / segmentLength;
          segmentDirection.scale(localRatio);
          YPoint targetPoint = YVector.add(segmentStart, segmentDirection);
          return new Point2D.Double(targetPoint.getX(),targetPoint.getY());
        }

        lastPathLength = currentPathLength;
        pi.next();
        segmentStart = pi.segmentStart();
        segmentEnd = pi.segmentEnd();
      }

      // we ran past the last point of the path (numeric problems?), return last point
      return new Point2D.Double(segmentStart.getX(), segmentStart.getY());
    }

    static Point2D getPointForLocalRatio(GeneralPath path, int segmentIndex, double segmentRatio) {
      if (segmentRatio > 1.0 || segmentRatio < 0.0) {
        throw new IllegalArgumentException("segmentRatio outside of [0,1]");
      }
      CustomPathIterator pi = new CustomPathIterator(path, 1.0);
      if (pi.isDone()) {
        // no points in GeneralPath: should not happen in this context
        throw new IllegalStateException("path without any coordinates");
      }
      int currentIndex = 0;
      while (pi.ok() && currentIndex < segmentIndex) {
        pi.next();
        currentIndex++;
      }
      if(currentIndex < segmentIndex)
      {
        throw new IllegalArgumentException("found no segment for given segmentIndex");
      }

      YPoint segmentStart = pi.segmentStart();
      YPoint segmentEnd = pi.segmentEnd();
      YVector segmentDirection = new YVector(segmentEnd, segmentStart);
      segmentDirection.scale(segmentRatio);
      YPoint targetPoint = YVector.add(segmentStart, segmentDirection);
      return new Point2D.Double(targetPoint.getX(), targetPoint.getY());
    }

    private static double getPathLength(GeneralPath path) {
      double length = 0.0;
      for(CustomPathIterator pi = new CustomPathIterator(path, 1.0); pi.ok(); pi.next()) {
        length += pi.segmentDirection().length();
      }
      return length;
    }
  }

  /**
   * Helper class used by PointPathProjector.
   */
  static class CustomPathIterator {
    private double[] cachedSegment;
    private boolean moreToGet;
    private PathIterator pathIterator;

    public CustomPathIterator(GeneralPath path, double flatness) {
      // copy the path, thus the original may safely change during iteration
      pathIterator = (new GeneralPath(path)).getPathIterator(null, flatness);
      cachedSegment = new double[4];
      getFirstSegment();
    }

    public boolean ok()
    {
      return moreToGet;
    }

    public boolean isDone() {
      return !moreToGet;
    }

    public final double[] segment() {
      if (moreToGet) {
        return cachedSegment;
      } else {
        return null;
      }
    }

    public YPoint segmentStart() {
      if(moreToGet) {
        return new YPoint(cachedSegment[0], cachedSegment[1]);
      } else {
        return null;
      }
    }

    public YPoint segmentEnd() {
      if(moreToGet) {
        return new YPoint(cachedSegment[2], cachedSegment[3]);
      } else {
        return null;
      }
    }

    public YVector segmentDirection() {
      if(moreToGet) {
        return new YVector(segmentEnd(), segmentStart());
      } else {
        return null;
      }
    }

    public void next() {
      if (!pathIterator.isDone()) {
        float[] curSeg = new float[2];
        cachedSegment[0] = cachedSegment[2];
        cachedSegment[1] = cachedSegment[3];
        pathIterator.currentSegment(curSeg);
        cachedSegment[2] = curSeg[0];
        cachedSegment[3] = curSeg[1];
        pathIterator.next();
      } else {
        moreToGet = false;
      }
    }

    private void getFirstSegment() {
      float[] curSeg = new float[2];
      if (!pathIterator.isDone()) {
        pathIterator.currentSegment(curSeg);
        cachedSegment[0] = curSeg[0];
        cachedSegment[1] = curSeg[1];
        pathIterator.next();
        moreToGet = true;
      } else {
        moreToGet = false;
      }
      if (!pathIterator.isDone()) {
        pathIterator.currentSegment(curSeg);
        cachedSegment[2] = curSeg[0];
        cachedSegment[3] = curSeg[1];
        pathIterator.next();
        moreToGet = true;
      } else {
        moreToGet = false;
      }
    }
  }

  public static void main(String[] args) {
    EventQueue.invokeLater(new Runnable() {
      public void run() {
        initLnF();
        (new EdgeConnectorDemo()).start();
      }
    });
  }
}
