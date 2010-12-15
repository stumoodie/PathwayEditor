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

import demo.view.DemoBase;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.geom.Rectangle2D;
import java.net.URL;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JToolBar;
import javax.swing.JButton;

import y.base.Edge;
import y.base.EdgeCursor;
import y.base.Graph;
import y.base.GraphEvent;
import y.base.GraphListener;
import y.base.Node;
import y.base.NodeCursor;
import y.layout.ComponentLayouter;
import y.layout.Layouter;
import y.layout.hierarchic.IncrementalHierarchicLayouter;
import y.layout.orthogonal.OrthogonalLayouter;
import y.layout.router.OrthogonalEdgeRouter;
import y.layout.tree.BalloonLayouter;
import y.layout.tree.TreeReductionStage;
import y.view.DefaultBackgroundRenderer;
import y.view.EditMode;
import y.view.Graph2D;
import y.view.Graph2DCopyFactory;
import y.view.Graph2DEvent;
import y.view.Graph2DListener;
import y.view.Graph2DView;
import y.view.Graph2DViewActions;
import y.view.Graph2DViewMouseWheelZoomListener;
import y.view.ModelViewManager;
import y.view.NodeLabel;
import y.view.NodeRealizer;
import y.view.ShapeNodeRealizer;
import y.view.Graph2DLayoutExecutor;

/**
 * Demonstrates automatic structural synchronization between several graphs using 
 * {@link y.view.ModelViewManager}.
 * <p>
 * The demo shows four different Graph2DViews in a 2-by-2 matrix. The top-left one 
 * presents the model graph, the latter three ones show derived views of this model 
 * graph.
 * <br/>
 * Each of the derived views has special characteristics: for example, the bottom-left 
 * one does not show any of the edges from the model graph, the bottom-right one 
 * is empty at first and only shows nodes created interactively by a user. 
 * Also, in some views the visual representation of the nodes differs from the model 
 * graph.
 * </p>
 * <p>
 * In all views there can be applied an automatic layout to the contained graph. 
 * Additionally, the two views at the bottom, which prevent editing of their contained 
 * graphs, provide a button to synchronize their contents back to the model graph's 
 * view, which in turn updates the other derived views.
 * </p>
 *
 */
public class ModelViewManagerDemo extends DemoBase {
  private final ModelViewManager manager;
  private final Graph2DView[] subViews;

  public ModelViewManagerDemo() {
    subViews = new Graph2DView[3];

    initGraph(view.getGraph2D());
    manager = ModelViewManager.getInstance(view.getGraph2D());

    contentPane.remove(view);
    contentPane.add(createMultiView(), BorderLayout.CENTER);
  }

  /**
   * Creates a sample graph.
   */
  private void initGraph( final Graph2D graph ) {
    graph.clear();
    graph.getDefaultNodeRealizer().setFillColor(new Color(73, 147, 255));

    //create nodes
    final Node[] nodes = new Node[10];
    for (int i = 0; i < nodes.length; ++i) {
      nodes[i] = graph.createNode();
    }

    //create edges
    graph.createEdge(nodes[1], nodes[8]);
    graph.createEdge(nodes[1], nodes[2]);
    graph.createEdge(nodes[1], nodes[6]);
    graph.createEdge(nodes[1], nodes[0]);
    graph.createEdge(nodes[2], nodes[0]);
    graph.createEdge(nodes[3], nodes[5]);
    graph.createEdge(nodes[3], nodes[6]);
    graph.createEdge(nodes[3], nodes[1]);
    graph.createEdge(nodes[4], nodes[2]);
    graph.createEdge(nodes[4], nodes[7]);
    graph.createEdge(nodes[5], nodes[0]);
    graph.createEdge(nodes[6], nodes[5]);
    graph.createEdge(nodes[6], nodes[0]);
    graph.createEdge(nodes[7], nodes[2]);
    graph.createEdge(nodes[7], nodes[8]);
    graph.createEdge(nodes[8], nodes[4]);
    graph.createEdge(nodes[9], nodes[8]);
    graph.createEdge(nodes[9], nodes[7]);

    //node labels
    for (NodeCursor nc = graph.nodes(); nc.ok(); nc.next()) {
      graph.getRealizer(nc.node()).setLabelText(Integer.toString(nc.node().index()));
    }

    // calculate an initial hierarchical layout
    final IncrementalHierarchicLayouter ihl = new IncrementalHierarchicLayouter();
    new Graph2DLayoutExecutor().doLayout(graph, ihl);
  }

  /**
   * Creates and initializes the views managed by the demo's
   * <code>ModelViewManager</code>.
   */
  private JComponent createMultiView() {
    final JPanel pane = new JPanel(new GridLayout(2, 2, 1, 1));

    // shared listeners
    final LabelChangeHandler labelChangeHandler = new LabelChangeHandler();
    final Graph2DViewMouseWheelZoomListener mwzl =
            new Graph2DViewMouseWheelZoomListener();

    // the model view
    final JToolBar vtb = createToolBar(view);
    if (vtb != null) {
      final JPanel viewAndTools = new JPanel(new BorderLayout());
      viewAndTools.add(view, BorderLayout.CENTER);
      viewAndTools.add(vtb, BorderLayout.NORTH);
      pane.add(viewAndTools);
    } else {
      pane.add(view);
    }
    view.fitContent();
    view.getGraph2D().addGraph2DListener(labelChangeHandler);
    MyBackgroundRenderer.newInstance(view).setText("Editable Model");



    // create Graph2DViews for the graphs handled as views for the model
    // in the demo's ModelViewManager
    for (int i = 0; i < subViews.length; ++i) {
      subViews[i] = new Graph2DView();
      subViews[i].setFitContentOnResize(true);
      final JToolBar svitb = createToolBar(subViews[i]);
      if (svitb != null) {
        final JPanel viewAndTools = new JPanel(new BorderLayout());
        viewAndTools.add(subViews[i], BorderLayout.CENTER);
        viewAndTools.add(svitb, BorderLayout.NORTH);
        pane.add(viewAndTools);
      } else {
        pane.add(subViews[i]);
      }
    }



    // set up an editable, auto synchronizing view
    final Graph2D graph = subViews[0].getGraph2D();
    graph.setGraphCopyFactory(new MyCopyFactory(createRedCircle()));

    // register the Graph2DView's graph as a graph view of the demo's
    // ModelViewManager model
    manager.addViewGraph(graph, null, true);
    manager.synchronizeModelToViewGraph(graph);

    graph.setDefaultNodeRealizer(createRedCircle());
    graph.addGraph2DListener(labelChangeHandler);

    // configure the Graph2DView for editing
    final Graph2DViewActions actions = new Graph2DViewActions(subViews[0]);
    final ActionMap amap = actions.createActionMap();
    final InputMap imap = actions.createDefaultInputMap(amap);
    subViews[0].getCanvasComponent().setActionMap(amap);
    subViews[0].getCanvasComponent().setInputMap(JComponent.WHEN_FOCUSED, imap);
    subViews[0].getCanvasComponent().addMouseWheelListener(mwzl);
    subViews[0].addViewMode(new EditMode());
    MyBackgroundRenderer.newInstance(subViews[0]).setText("Editable View");

    // calculate an initial layout that differs from the model's layout
    subViews[0].applyLayout(createOrthogonalLayouter());
    subViews[0].fitContent();



    // set up non-editable view, that displays nodes only
    subViews[1].setGraph2D((Graph2D) manager.createViewGraph(
            new MyCopyFactory(createOrangeOctagon()), new NoEdgesFilter(), false));

    // configure the Graph2DView
    subViews[1].fitContent();
    subViews[1].getCanvasComponent().addMouseWheelListener(mwzl);
    MyBackgroundRenderer.newInstance(subViews[1]).setText("Non-editable View");



    // set up non-editable view, that displays only user-created graph elements
    subViews[2].setGraph2D((Graph2D) manager.createViewGraph(
            null, new ExcludeFilter(view.getGraph2D()), false));

    // configure the Graph2DView
    subViews[2].fitContent();
    subViews[2].getCanvasComponent().addMouseWheelListener(mwzl);
    MyBackgroundRenderer.newInstance(subViews[2]).setText("Non-editable View");



    // ensure that all Graph2DViews are properly refreshed on structural
    // changes
    manager.getModel().addGraphListener(new UpdateHandler());
    for (Iterator it = manager.viewGraphs(); it.hasNext();) {
      ((Graph) it.next()).addGraphListener(new UpdateHandler());
    }


    return pane;
  }

  /**
   * Overwritten to be able to trigger an initial <code>fitContent()</code>
   * for all the <code>Graph2DView</code>s used in this demo.
   */
  public void addContentTo( final JRootPane rootPane ) {
    super.addContentTo(rootPane);
    final ComponentAdapter handler = new ComponentAdapter() {
      private int callCount;

      public void componentResized( final ComponentEvent e ) {
        if (callCount < 2) {
          configureviews();
        }
        ++callCount;
        if (callCount == 2) {
          rootPane.removeComponentListener(this);
        }
      }

      private void configureviews() {
        view.fitContent();
        view.updateView();
        for (int i = 0; i < subViews.length; ++i) {
          subViews[i].fitContent();
          subViews[i].updateView();
        }
      }
    };
    rootPane.addComponentListener(handler);
  }

  /**
   * Overwritten to prevent the standard toolbar from being created.
   * Each <code>Graph2DView</code> used in this demo comes with its own
   * custom toolbar.
   * @return <code>null</code>.
   */
  protected JToolBar createToolBar() {
    return null;
  }

  /**
   * Create a custom toolbar for the specified <code>Graph2DView</code>.
   */
  private JToolBar createToolBar( final Graph2DView view ) {
    final JToolBar jtb = new JToolBar();
    jtb.setFloatable(false);

    // add delete actions for the two editable views
    if (view == this.view || view == this.subViews[0]) {
      jtb.add(new DeleteSelection(view));
    }

    // add a fit content action for all views
    jtb.add(new FitContent(view));

    jtb.addSeparator();
    // add a layout action for each view
    jtb.add(createActionControl(createLayoutAction(view)));

    // add a synchronize view contents to model for the two non-editable views
    if (view == this.subViews[1] || view == this.subViews[2]) {
      jtb.add(createActionControl(new SynchViewToModel(view)));
    }

    return jtb;
  }

  /**
   * Creates a control for triggering the specified action from the
   * demo toolbars.
   * @param action   the <code>Action</code> that is triggered by the
   * created control.
   * @return a control for triggering the specified action from the
   * demo toolbars.
   */
  private JComponent createActionControl( final Action action ) {
    return new JButton(action);
  }

  /**
   * Factory method for layout actions depending on the specified
   * <code>Graph2DView</code>.
   */
  private Action createLayoutAction( final Graph2DView view ) {
    final Layout layout;

    if (view == this.view) {
      // create a hierarchical layout action for the model view
      layout = new Layout(view, new IncrementalHierarchicLayouter());
      layout.putValue(Action.SHORT_DESCRIPTION, "Layout Hierarchically");
    } else if (view == subViews[0]) {
      // create an orthogonal layout action for the editable non-model view
      layout = new Layout(view, createOrthogonalLayouter());
      layout.putValue(Action.SHORT_DESCRIPTION, "Layout Orthogonally");
    } else if (view == subViews[1]) {
      // create a grid layout action for the non-editable nodes-only view
      layout = new Layout(view, new ComponentLayouter());
      layout.putValue(Action.SHORT_DESCRIPTION, "Layout Component Grid");
    } else if (view == subViews[2]) {
      // create a balloon layout action for the non-editable diffs view
      layout = new Layout(view, createBalloonLayouter());
      layout.putValue(Action.SHORT_DESCRIPTION, "Layout Balloon-style Tree");
    } else {
      layout = new Layout(view, null);
    }

    return layout;
  }


  public static void main( String[] args ) {
    try {
      EventQueue.invokeAndWait(new Runnable() {
        public void run() {
          initLnF();
          (new ModelViewManagerDemo()).start();
        }
      });
    } catch (Throwable t) {
      t.printStackTrace();
    }
  }

  /**
   * Factory method for a template <code>NodeRealizer</code>.
   */
  private static NodeRealizer createRedCircle() {
    final ShapeNodeRealizer snr = new ShapeNodeRealizer();
    snr.setShapeType(ShapeNodeRealizer.ELLIPSE);
    snr.setFillColor(new Color(196, 0, 64));
    return snr;
  }

  /**
   * Factory method for a template <code>NodeRealizer</code>.
   */
  private static NodeRealizer createOrangeOctagon() {
    final ShapeNodeRealizer snr = new ShapeNodeRealizer();
    snr.setWidth(50);
    snr.setShapeType(ShapeNodeRealizer.OCTAGON);
    snr.setFillColor(new Color(223, 134, 17));
    return snr;
  }

  /**
   * Factory method for a configured <code>Layouter</code>.
   */
  private static Layouter createBalloonLayouter() {
    final OrthogonalEdgeRouter orthogonal = new OrthogonalEdgeRouter();
    orthogonal.setCrossingCost(1.0);
    orthogonal.setReroutingEnabled(true);
    orthogonal.setSphereOfAction(OrthogonalEdgeRouter.ROUTE_SELECTED_EDGES);

    final TreeReductionStage trs = new TreeReductionStage();
    trs.setNonTreeEdgeSelectionKey(OrthogonalEdgeRouter.SELECTED_EDGES);
    trs.setNonTreeEdgeRouter(orthogonal);

    final BalloonLayouter bl = new BalloonLayouter();
    bl.setRootNodePolicy(BalloonLayouter.DIRECTED_ROOT);
    bl.setPreferredChildWedge(300);
    bl.setPreferredRootWedge(360);
    bl.setMinimalEdgeLength(40);
    bl.setCompactnessFactor(0.5);
    bl.setAllowOverlaps(false);
    bl.setFromSketchModeEnabled(false);
    bl.appendStage(trs);
    return bl;
  }

  /**
   * Factory method for a configured <code>Layouter</code>.
   */
  private static Layouter createOrthogonalLayouter() {
    final OrthogonalLayouter ol = new OrthogonalLayouter();
    ol.setLayoutStyle(OrthogonalLayouter.NORMAL_TREE_STYLE);
    ol.setGrid(25);
    ol.setUseLengthReduction(true);
    ol.setUseCrossingPostprocessing(true);
    ol.setPerceivedBendsOptimizationEnabled(true);
    ol.setUseRandomization(false);
    ol.setUseFaceMaximization(true);
    ol.setUseSketchDrawing(false);
    return ol;
  }


  /**
   * Custom <code>ModelViewManager.Filter</code> filter implementation
   * that rejects all edge representatives from being automatically created by a
   * <code>ModelViewManager</code>.
   */
  private static final class NoEdgesFilter implements ModelViewManager.Filter {
    public boolean acceptInsertion( final Node node ) {
      return true;
    }

    public boolean acceptInsertion( final Edge edge ) {
      return false;
    }

    public boolean acceptRemoval( final Node node ) {
      return true;
    }

    public boolean acceptRemoval( final Edge edge ) {
      return true;
    }

    public boolean acceptRetention( final Node node ) {
      return true;
    }

    public boolean acceptRetention( final Edge edge ) {
      return true;
    }
  }

  /**
   * Custom <code>ModelViewManager.Filter</code> filter implementation
   * that rejects edge and node representatives from being automatically
   * created by a <code>ModelViewManager</code>, if the corresponding
   * model element is stored in one of this filter's exclusion sets.
   */
  private static final class ExcludeFilter implements ModelViewManager.Filter {
    final Set excludedNodes;
    final Set excludedEdges;

    ExcludeFilter( final Graph graph ) {
      excludedNodes = new HashSet();
      excludedEdges = new HashSet();

      for (NodeCursor nc = graph.nodes(); nc.ok(); nc.next()) {
        excludedNodes.add(nc.node());
      }
      for (EdgeCursor ec = graph.edges(); ec.ok(); ec.next()) {
        excludedEdges.add(ec.edge());
      }
    }

    public boolean acceptInsertion( final Node node ) {
      return !excludedNodes.contains(node);
    }

    public boolean acceptInsertion( final Edge edge ) {
      return !excludedEdges.contains(edge);
    }

    public boolean acceptRemoval( final Node node ) {
      return true;
    }

    public boolean acceptRemoval( final Edge edge ) {
      return true;
    }

    public boolean acceptRetention( final Node node ) {
      return true;
    }

    public boolean acceptRetention( final Edge edge ) {
      return true;
    }
  }

  /**
   * <code>Graph2DCopyFactory</code> that uses a template
   * <code>NodeRealizer</code> when copying nodes instead of copying the
   * original realizer.
   */
  private static final class MyCopyFactory extends Graph2DCopyFactory {
    private NodeRealizer template;

    MyCopyFactory( final NodeRealizer template ) {
      setTemplateImpl(template);
    }

    protected NodeRealizer copyRealizer( final NodeRealizer nr ) {
      if (template != null) {
        // instead of copying the original realizer, create a copy of the
        // template realizer at an appropriate position
        final NodeRealizer _nr = template.createCopy();
        _nr.setCenter(nr.getCenterX(), nr.getCenterY());

        // manually copy node labels
        final int lc = nr.labelCount();
        if (lc > 0) {
          _nr.setLabel((NodeLabel) nr.getLabel().clone());
          for (int i = 1; i < lc; ++i) {
            _nr.addLabel((NodeLabel) nr.getLabel(i).clone());
          }
        }
        return _nr;
      } else {
        return nr.createCopy();
      }
    }

    NodeRealizer getTemplate() {
      return template;
    }

    void setTemplate( final NodeRealizer template ) {
      setTemplateImpl(template);
    }

    private void setTemplateImpl( final NodeRealizer template ) {
      this.template = template != null ? template.createCopy() : null;
    }
  }

  /**
   * <code>BackgroundRenderer</code> that displays a short text message.
   */
  private static final class MyBackgroundRenderer
          extends DefaultBackgroundRenderer {
    private String text;
    private Color textColor;
    private final Rectangle r;

    MyBackgroundRenderer( final Graph2DView view ) {
      super(view);
      textColor = new Color(192, 192, 192);
      r = new Rectangle(0, 0, -1, 1);
    }

    String getText() {
      return text;
    }

    void setText( final String text ) {
      this.text = text;
    }

    Color getTextColor() {
      return textColor;
    }

    void setTextColor( final Color color ) {
      this.textColor = color;
    }

    public void paint(
            final Graphics2D gfx,
            final int x,
            final int y,
            final int w,
            final int h ) {
      super.paint(gfx, x, y, w, h);
      paintText(gfx);
    }

    private void paintText( final Graphics2D gfx ) {
      if (text != null && textColor != null) {
        final Color oldColor = gfx.getColor();
        final Font oldFont = gfx.getFont();

        undoWorldTransform(gfx);

        gfx.setColor(textColor);
        gfx.setFont(oldFont.deriveFont(30.0f));

        view.getBounds(r);
        r.setLocation(0, 0);
        final FontMetrics fm = gfx.getFontMetrics();
        final Rectangle2D bnds = fm.getStringBounds(text, gfx);
        final float textX = (float) (r.x + (r.width - bnds.getWidth()) * 0.5);
        final float textY = (float) (r.y + (r.height - bnds.getHeight()) * 0.5 + fm.getMaxAscent());
        gfx.drawString(text, textX, textY);

        redoWorldTransform(gfx);

        gfx.setFont(oldFont);
        gfx.setColor(oldColor);
      }
    }


    static MyBackgroundRenderer newInstance( final Graph2DView view ) {
      final MyBackgroundRenderer mbr = new MyBackgroundRenderer(view);
      view.setBackgroundRenderer(mbr);
      return mbr;
    }
  }

  /**
   * <code>GraphListener</code> that updates all {@link y.view.View}s
   * associated to source of an structural change.
   */
  private static class UpdateHandler implements GraphListener {
    private int block;

    public void onGraphEvent( final GraphEvent e ) {
      if (e.getGraph() instanceof Graph2D) {
        switch (e.getType()) {
          case GraphEvent.PRE_EVENT:
            ++block;
            break;
          case GraphEvent.POST_EVENT:
            --block;
            break;
          default:
            break;
        }
        if (block == 0) {
          ((Graph2D) e.getGraph()).updateViews();
        }
      }
    }
  }

  /**
   * <code>Graph2DListener</code> that propagates label text changes to the
   * model and all views of the demo's <code>ModelViewManager</code>.
   */
  private class LabelChangeHandler implements Graph2DListener {
    private boolean armed;

    LabelChangeHandler() {
      armed = true;
    }

    public void onGraph2DEvent( final Graph2DEvent e ) {
      if (!armed) {
        return;
      }

      if ("text".equals(e.getPropertyName()) &&
          e.getSubject() instanceof NodeLabel) {
        final NodeLabel nl = (NodeLabel) e.getSubject();
        setLabelText(nl.getNode(), nl.getText());
      }
    }

    private void setLabelText( final Node node, final String text ) {
      armed = false;

      final Node mn;
      final Graph2D model = view.getGraph2D();
      if (node.getGraph() != model) {
        // determine the model representative of node
        mn = manager.getModelNode(node);
        if (mn != null) {
          // set the label text for the model representative
          model.getRealizer(mn).setLabelText(text);
          model.updateViews();
        }
      } else {
        mn = node;
      }

      if (mn != null) {
        for (Iterator it = manager.viewGraphs(); it.hasNext();) {
          final Graph2D graph = ((Graph2D) it.next());
          // determine the view representative of node
          final Node vn = manager.getViewNode(mn, graph);
          if (vn != null && vn != node) {
            // set the label text for the view representative
            graph.getRealizer(vn).setLabelText(text);
            graph.updateViews();
          }
        }
      }

      armed = true;
    }
  }

  /**
   * <code>Action</code> that synchronizes the contents of the graph of its
   * associated view to the model of the demo's <code>ModelViewManager</code.
   */
  private final class SynchViewToModel extends AbstractAction {
    private final Graph2DView view;

    SynchViewToModel( final Graph2DView view ) {
      super("Synchronize");
      this.view = view;
      final URL imageURL = ClassLoader.getSystemResource(
              "demo/view/advanced/resource/Export16.gif");
      if (imageURL != null) {
        this.putValue(Action.SMALL_ICON, new ImageIcon(imageURL));
      }
      this.putValue(Action.SHORT_DESCRIPTION, "Synchronize View to Model");
    }

    public void actionPerformed( final ActionEvent e ) {
      manager.synchronizeViewGraphToModel(view.getGraph2D());

      ModelViewManagerDemo.this.view.fitContent();
      ModelViewManagerDemo.this.view.updateView();
      for (int i = 0; i < subViews.length; ++i) {
        subViews[i].fitContent();
        subViews[i].updateView();
      }
    }
  }

  /**
   * <code>Action</code> that calculates a layout for the graph of its
   * associated view using its associated layout algorithm.
   */
  private static final class Layout extends AbstractAction {
    private final Graph2DView view;
    private final Layouter layouter;

    Layout( final Graph2DView view, final Layouter layouter ) {
      super("Layout");
      this.view = view;
      this.layouter = layouter;
      final URL imageURL = ClassLoader.getSystemResource(
              "demo/view/resource/layout.png");
      if (imageURL != null) {
        this.putValue(Action.SMALL_ICON, new ImageIcon(imageURL));
      }
      this.putValue(Action.SHORT_DESCRIPTION, "Layout Graph");
    }

    public void actionPerformed( final ActionEvent e ) {
      view.applyLayout(layouter);

      view.fitContent();
      view.updateView();
    }
  }
}
