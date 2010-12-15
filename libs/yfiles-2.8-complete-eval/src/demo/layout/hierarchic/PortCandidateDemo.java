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
package demo.layout.hierarchic;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.EventQueue;
import java.awt.Graphics2D;
import java.awt.Color;
import java.awt.geom.Ellipse2D;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Iterator;

import javax.swing.AbstractAction;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;

import demo.view.DemoBase;
import demo.view.DemoDefaults;
import demo.view.application.DragAndDropDemo;

import y.base.DataProvider;
import y.base.Edge;
import y.base.Node;
import y.layout.PortCandidate;
import y.layout.PortCandidateSet;
import y.layout.PortConstraint;
import y.layout.PortConstraintKeys;
import y.layout.hierarchic.IncrementalHierarchicLayouter;
import y.layout.hierarchic.incremental.SimplexNodePlacer;
import y.util.DataProviderAdapter;
import y.util.DataProviders;
import y.view.BridgeCalculator;
import y.view.DefaultGraph2DRenderer;
import y.view.EditMode;
import y.view.GenericNodeRealizer;
import y.view.Graph2D;
import y.view.NodeRealizer;
import y.view.GenericNodeRealizer.ContainsTest;
import y.view.GenericNodeRealizer.Painter;

/**
 * This demo shows how {@link PortCandidateSet}s can be used with {@link IncrementalHierarchicLayouter} to control 
 * from what side edges connect to certain node types in the automatic layout process.
 * <br/>
 * Usage: The template nodes in the list to the left have different port candidate sets. Try changing
 * the graph using the templates and note the effect for a new layout.
 *  
 */
public class PortCandidateDemo extends DemoBase {

  // the layouter instance to use for the automatic layouts
  private IncrementalHierarchicLayouter layouter;
  
  private Map portCandidateMap;
  
  public PortCandidateDemo() {    
    
    final List nodeRealizerList = new ArrayList();
    portCandidateMap = new HashMap();
    addNodeRealizerTemplates(nodeRealizerList);

    // create DragAndDrop List support
    DragAndDropDemo.DragAndDropSupport dndSupport = new DragAndDropDemo.DragAndDropSupport(nodeRealizerList, view);
    
    // add the list to the UI
    contentPane.add(new JScrollPane(dndSupport.getList()), BorderLayout.WEST);
    layouter = createLayouter();
    
    loadGraph("resource/PortCandidateDemo.graphml");
  }

  /**
   * Overwritten to disable node label setting and disallow resizing.
   */
  protected EditMode createEditMode() {
    final EditMode editMode = super.createEditMode();
    editMode.assignNodeLabel(false);
    editMode.allowResizeNodes(false);
    return editMode;
  }

  /**
   * Add a layout button to the ToolBar
   */
  protected JToolBar createToolBar() {
    final JToolBar toolBar = super.createToolBar();
    toolBar.add(new AbstractAction("Layout") {
      public void actionPerformed(ActionEvent e) {
        runLayout();
      }
    });
    return toolBar;
  }

  /**
   * Configures the view.
   */
  protected void configureDefaultRealizers() {
    super.configureDefaultRealizers();
    view.getGraph2D().getDefaultNodeRealizer().getLabel().setFontSize(16);
    ((DefaultGraph2DRenderer) view.getGraph2DRenderer()).setBridgeCalculator(new BridgeCalculator());    
  }

  /**
   * Creates the Layouter instance.
   */
  private IncrementalHierarchicLayouter createLayouter() {
    final IncrementalHierarchicLayouter ihl = new IncrementalHierarchicLayouter();
    ((SimplexNodePlacer) ihl.getNodePlacer()).setBaryCenterModeEnabled(true);
    ihl.setLayoutMode(IncrementalHierarchicLayouter.LAYOUT_MODE_FROM_SCRATCH);
    ihl.setOrthogonallyRouted(true);
    final Graph2D graph = view.getGraph2D();

    // create an adapter that returns the PortCandidateSet associated with the GenericNodeRealizer configuration
    graph.addDataProvider(PortCandidateSet.NODE_DP_KEY, new DataProviderAdapter() {
      public Object get(Object dataHolder) {
        final Node node = (Node) dataHolder;
        final NodeRealizer realizer = graph.getRealizer(node);
        if (realizer instanceof GenericNodeRealizer) {
          return portCandidateMap.get(((GenericNodeRealizer) realizer).getConfiguration());
        } else {
          return null;
        }
      }
    });

    // create automatic bus structures for outgoing edges of "start" nodes
    graph.addDataProvider(PortConstraintKeys.SOURCE_GROUPID_KEY, new DataProviderAdapter() {
      public Object get(Object dataHolder) {
        Edge edge = (Edge) dataHolder;
        Node source = edge.source();
        GenericNodeRealizer gnr = (GenericNodeRealizer) graph.getRealizer(source);
        String sourceConfiguration = gnr.getConfiguration();
        if ("start".equals(sourceConfiguration)) {
          return source;
        }
        return null;
      }
    });

    //... and bus structures for incoming edges at "switch" and "branch" nodes
    graph.addDataProvider(PortConstraintKeys.TARGET_GROUPID_KEY, new DataProviderAdapter() {
      public Object get(Object dataHolder) {
        Edge edge = (Edge) dataHolder;
        Node target = edge.target();
        GenericNodeRealizer gnr = (GenericNodeRealizer) graph.getRealizer(target);
        String targetConfiguration = gnr.getConfiguration();
        if ("switch".equals(targetConfiguration) || "branch".equals(targetConfiguration)) {
          return target;
        }
        return null;
      }
    });

    // make sure that edges enter at the north side and exit at the south side of the nodes
    final DataProvider southDP = DataProviders.createConstantDataProvider(PortConstraint.create(PortConstraint.SOUTH));
    graph.addDataProvider(PortConstraintKeys.SOURCE_PORT_CONSTRAINT_KEY, southDP);
    final DataProvider northDP = DataProviders.createConstantDataProvider(PortConstraint.create(PortConstraint.NORTH));
    graph.addDataProvider(PortConstraintKeys.TARGET_PORT_CONSTRAINT_KEY, northDP);
    return ihl;
  }

  /**
   * Run the layout in normal mode.
   */
  private void runLayout() {
    Cursor oldCursor = view.getCanvasComponent().getCursor();
    try {
      contentPane.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
      view.applyLayoutAnimated(layouter);
    } finally {
      contentPane.setCursor(oldCursor);
    }
  }

  /** This method adds the possible NodeRealizer's for this application to the given list and configures the
   * port candidate sets for each node type
   */
  private void addNodeRealizerTemplates(List nodeRealizerList) {    
    {
      //configure the PortCandidateSet for start nodes
      //edges connected to start nodes may connect from any side. the port location is always in the center of the node   
      PortCandidateSet candidateSet = new PortCandidateSet();
      candidateSet.add(PortCandidate.createCandidate(0.0d, 0.0d, PortCandidate.ANY, 0.0), Integer.MAX_VALUE);
      addNodeRealizerTemplate("start", nodeRealizerList, candidateSet);
    }
    {
      //configure the PortCandidateSet for state nodes
      //edges can connect form either north (top) or south (bottom) side. port locations are not constrained. 
      PortCandidateSet candidateSet = new PortCandidateSet();
      candidateSet.add(PortCandidate.createCandidate(PortCandidate.NORTH, 0.0), Integer.MAX_VALUE);
      candidateSet.add(PortCandidate.createCandidate(PortCandidate.SOUTH, 0.0), Integer.MAX_VALUE);
      addNodeRealizerTemplate("state", nodeRealizerList, candidateSet);
    }
    {      
      // configure the PortCandidateSet for switch nodes
      // at most one edge can connect from the east (right) and the west (left) side of the node.
      // these east and west locations are preferred over the top and bottom locations which will only
      // be used if the east and west sides are already saturated
      PortCandidateSet candidateSet = new PortCandidateSet();
      candidateSet.add(PortCandidate.createCandidate(0.0, -15.0, PortCandidate.NORTH, 0.0), 1);
      candidateSet.add(PortCandidate.createCandidate(0.0, +15.0, PortCandidate.SOUTH, 0.0), 1);
      candidateSet.add(PortCandidate.createCandidate(+30.0, 0.0, PortCandidate.EAST, 0.0), 1);
      candidateSet.add(PortCandidate.createCandidate(-30.0, 0.0, PortCandidate.WEST, 0.0), 1);
      candidateSet.add(PortCandidate.createCandidate(0.0, -15.0, PortCandidate.NORTH, 1.0), Integer.MAX_VALUE);
      candidateSet.add(PortCandidate.createCandidate(0.0, +15.0, PortCandidate.SOUTH, 1.0), Integer.MAX_VALUE);
      addNodeRealizerTemplate("switch", nodeRealizerList, candidateSet);
    }
    {
      // configure the PortCandidateSet for branch nodes
      // at most one edge can connect from the south (bottom) side of the node.
      // this south location is preferred over the left and right side locations which will only
      // be used if the south side is already saturated
      PortCandidateSet candidateSet = new PortCandidateSet();
      candidateSet.add(PortCandidate.createCandidate(0.0, -15.0, PortCandidate.NORTH, 0.0), Integer.MAX_VALUE);
      candidateSet.add(PortCandidate.createCandidate(0.0, +15.0, PortCandidate.SOUTH, 0.0), 1);
      candidateSet.add(PortCandidate.createCandidate(+30.0, 0.0, PortCandidate.EAST, 1.0), Integer.MAX_VALUE);
      candidateSet.add(PortCandidate.createCandidate(-30.0, 0.0, PortCandidate.WEST, 1.0), Integer.MAX_VALUE);
      addNodeRealizerTemplate("branch", nodeRealizerList, candidateSet);
    }
    {     
      // configure the PortCandidateSet for end nodes  
      // the first edge will be connected to the top side
      // the second edge will be connected to either the left or right side
      // the third edge will be connected to either the left or right side but not the same side as the second edge
      // all further edges will either be connected to the left or right side      
      PortCandidateSet candidateSet = new PortCandidateSet();
      candidateSet.add(PortCandidate.createCandidate(0.0, -15.0, PortCandidate.NORTH, 0.0), 1);
      candidateSet.add(PortCandidate.createCandidate(+30.0, 0.0, PortCandidate.EAST, 1.0), 1);
      candidateSet.add(PortCandidate.createCandidate(-30.0, 0.0, PortCandidate.WEST, 1.0), 1);
      candidateSet.add(PortCandidate.createCandidate(+30.0, 0.0, PortCandidate.EAST, 2.0), Integer.MAX_VALUE);
      candidateSet.add(PortCandidate.createCandidate(-30.0, 0.0, PortCandidate.WEST, 2.0), Integer.MAX_VALUE);
      addNodeRealizerTemplate("end", nodeRealizerList, candidateSet);
    }
  }
  
  void addNodeRealizerTemplate(String configuration, List nodeRealizerList, PortCandidateSet candidateSet) {
    final GenericNodeRealizer.Factory factory = GenericNodeRealizer.getFactory();    
    final Map map = GenericNodeRealizer.getFactory().createDefaultConfigurationMap();
    GenericNodeRealizer gnr = (GenericNodeRealizer) view.getGraph2D().getDefaultNodeRealizer();
    map.put(Painter.class, new PortCandidatePainter(
        (Painter)factory.getImplementation(DemoDefaults.NODE_CONFIGURATION, Painter.class), candidateSet));
    map.put(ContainsTest.class, factory.getImplementation(DemoDefaults.NODE_CONFIGURATION, ContainsTest.class));

    //create a switch node configuration     
    factory.addConfiguration(configuration, map);
    gnr = (GenericNodeRealizer) gnr.createCopy();
    gnr.setConfiguration(configuration);
    gnr.setLabelText(configuration);
    nodeRealizerList.add(gnr);
    portCandidateMap.put(configuration, candidateSet);
  }

  /**
   * Decorator implementation that draws the set of fixed port candidate locations as small gray circles
   */
  static class PortCandidatePainter implements Painter {
    private final Painter delegatePainter;
    private final PortCandidateSet set;

    PortCandidatePainter(Painter delegatePainter, PortCandidateSet set) {
      this.delegatePainter = delegatePainter;
      this.set = set;
    }

    public void paint(NodeRealizer context, Graphics2D graphics) {
      delegatePainter.paint(context, graphics);
      if (set != null) {
        final Iterator entries = set.getEntries();
        graphics.setColor(new Color(0,0,0,128));
        while (entries.hasNext()) {
          PortCandidateSet.Entry entry = (PortCandidateSet.Entry) entries.next();
          final PortCandidate candidate = entry.getPortCandidate();
          if (candidate.isFixed()) {
            double x = candidate.getXOffset() + context.getCenterX();
            double y = candidate.getYOffset() + context.getCenterY();
            graphics.fill(new Ellipse2D.Double(x - 2.0, y - 2.0, 4.0, 4.0));
          }
        }
      }
    }

    public void paintSloppy(NodeRealizer context, Graphics2D graphics) {
      delegatePainter.paintSloppy(context, graphics);
    }
  }

  
  public static void main(String[] args) {
    EventQueue.invokeLater(new Runnable() {
      public void run() {
        initLnF();
        new PortCandidateDemo().start();
      }
    });    
  }
}
