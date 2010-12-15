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
package demo.view.orgchart;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.RenderingHints.Key;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Map;

import javax.swing.Icon;

import demo.view.orgchart.OrgChartTreeModel.Employee;

import y.anim.AnimationFactory;
import y.anim.AnimationObject;
import y.anim.AnimationPlayer;
import y.base.DataProvider;
import y.base.Edge;
import y.base.Node;
import y.base.NodeMap;
import y.geom.OrientedRectangle;
import y.geom.YInsets;
import y.geom.YPoint;
import y.geom.YVector;
import y.layout.NormalizingGraphElementOrderStage;
import y.layout.Layouter;
import y.util.D;
import y.util.DataProviderAdapter;
import y.view.EdgeRealizer;
import y.view.GenericNodeRealizer;
import y.view.Graph2D;
import y.view.Graph2DSelectionEvent;
import y.view.Graph2DSelectionListener;
import y.view.Graph2DView;
import y.view.Graph2DViewMouseWheelZoomListener;
import y.view.HitInfo;
import y.view.LineType;
import y.view.MagnifierViewMode;
import y.view.NodeLabel;
import y.view.NodeRealizer;
import y.view.Overview;
import y.view.PolyLineEdgeRealizer;
import y.view.Scroller;
import y.view.ShapeNodePainter;
import y.view.ShapeNodeRealizer;
import y.view.ViewAnimationFactory;
import y.view.ViewCoordDrawableAdapter;
import y.view.ViewMode;
import y.view.YLabel;
import y.view.GenericNodeRealizer.Painter;
import y.view.hierarchy.DefaultHierarchyGraphFactory;
import y.view.hierarchy.GroupNodeRealizer;

/** 
 * This class builds upon the more generic tree chart component {@link demo.view.orgchart.JTreeChart}. 
 * It visualizes a {@link demo.view.orgchart.OrgChartTreeModel}. 
 * Also it customizes the look and feel of the component to make it suitable for
 * organization charts. 
 */
public class JOrgChart extends JTreeChart {

  /**
   * Defines the colors being used for the graph elements. There are four different states a node can be in: unselected, selected, highlighted, highlighted and selected.
   * The colors below define the colors used for each state.
   */
  static final Color FILL_COLOR = new Color( 0xCCFFFF );
  static final Color FILL_COLOR2 = new Color( 0x249AE7 );  
  static final Color LINE_COLOR = new Color( 0x249AE7 );
  
  static final Color SELECTED_FILL_COLOR = Color.WHITE; 
  static final Color SELECTED_LINE_COLOR = Color.ORANGE;
  static final Color SELECTED_FILL_COLOR2 = Color.ORANGE;
  
  static final Color SELECTED_HOVER_FILL_COLOR = SELECTED_FILL_COLOR;
  static final Color SELECTED_HOVER_LINE_COLOR = SELECTED_LINE_COLOR;
  static final Color SELECTED_HOVER_FILL_COLOR2 = new Color( 0xFFEE55 ); 
  
  static final Color HOVER_FILL_COLOR2 = new Color( 0x63CCEE );
  static final Color HOVER_FILL_COLOR = FILL_COLOR;
  static final Color HOVER_LINE_COLOR = LINE_COLOR;

  
  static final Color EDGE_COLOR = new Color( 0x999999 );  
  static final Color GROUP_FILL_COLOR = new Color(231,219,182,100);  
  
  /**
   * NodeMap used to maintain the highlighted state of a node.
   */
  private NodeMap highlightMap;  

  public JOrgChart(OrgChartTreeModel model) {
    super(model, createUserObjectDP(), createGroupIdDataProvider());
  
    highlightMap = getGraph2D().createNodeMap();

    getGraph2D().addGraph2DSelectionListener(new TreeChartSelectionListener());
      
    setPaintDetailThreshold(0);
    setAntialiasedPainting(true);    

//TURN ON FRACTIONAL FONT METRICS for precise font measurement
//    getRenderingHints().put(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
//    YLabel.setFractionMetricsForSizeCalculationEnabled(true);

    /**
     * Listener that adjusts the LoD (level of detail) of the diagram whenever the
     * zoom level of the view changes. 
     */
    getCanvasComponent().addPropertyChangeListener(new PropertyChangeListener() {
      public void propertyChange(PropertyChangeEvent evt) {
        if("Zoom".equals(evt.getPropertyName())) {
          double zoom = getZoom();
          Object lodValue = null;
          if(zoom >= 0.8) {
            lodValue = LODRenderingHint.VALUE_LOD_HIGH;
          }
          else if(zoom < 0.8 && zoom >= 0.3) {
            lodValue = LODRenderingHint.VALUE_LOD_MEDIUM;
          }
          else if(zoom < 0.3) {
            lodValue = LODRenderingHint.VALUE_LOD_LOW;
          }
          getRenderingHints().put(LODRenderingHint.KEY_LOD, lodValue);
        }          
      }      
    });           
    
    updateChart();
  }
  
  /**
   * RenderingHint that conveys information about the desired level of detail when rendering graph elements.
   */
  static class LODRenderingHint {    
    public static final Object VALUE_LOD_LOW = "LODRenderingHint#VALUE_LOD_LOW";
    public static final Object VALUE_LOD_MEDIUM = "LODRenderingHint#VALUE_LOD_MEDIUM";
    public static final Object VALUE_LOD_HIGH = "LODRenderingHint#VALUE_LOD_HIGH";
    public static final Object VALUE_LOD_OVERVIEW = "LODRenderingHint#VALUE_LOD_OVERVIEW";
    
    public static final Key KEY_LOD = new RenderingHints.Key(0) {
      public boolean isCompatibleValue(Object val) {
        return true; //allow all kinds of values 
      }    
    };    
  }

  /**
   * DataProvider that returns the userObject (--> Employee) for a tree model item.
   */
  static DataProvider createUserObjectDP() {
    return new DataProviderAdapter() {
      public Object get(Object dataHolder) {
        return dataHolder;
      }
    };
  }
  
  /**
   * DataProvider that returns the group Id for a tree model user object.
   * Group Ids are used to convey grouping information to the JTreeChart component.
   */
  static DataProvider createGroupIdDataProvider() {
    return new DataProviderAdapter() {
      public Object get(Object dataHolder) {
        return ((Employee)dataHolder).businessUnit;
      }
    };    
  }
      
  /**
   * Adds mouse interaction to the component. We inherit all that JTreeChart has, plus we add tool tip display,
   * a nifty local view magnifier, and a roll-over effect for nodes.    
   */
  protected void addMouseInteraction() {
    super.addMouseInteraction();
    addViewMode(new TooltipMode());
    addViewMode(new MiddleClickMagnifierViewMode());
    addViewMode(new RollOverViewMode());
  }
  
  /**
   * Returns a JTreeChartViewMode that will use a customized Scroller Drawable.   
   */
  protected JTreeChartViewMode createTreeChartViewMode() {
    JTreeChartViewMode viewMode = super.createTreeChartViewMode();
    Scroller scroller = viewMode.getScroller();
    scroller.setScrollSpeedFactor(2.0);
    scroller.setDrawable(new ScrollerDrawable(this,scroller));
    return viewMode;
  }
  
  /**
   * Customize and return the MouseWheelListener to be used. 
   */
  protected MouseWheelListener createMouseWheelListener() {
    Graph2DViewMouseWheelZoomListener mwl = new Graph2DViewMouseWheelZoomListener();
    mwl.setZoomIndicatorShowing(true);
    mwl.setCenterZooming(false);
    mwl.setMaximumZoom(4);
    mwl.setLimitMinimumZoomByContentSize(true);
    mwl.setZoomIndicatorColor(new Color(170, 160,125));
    return mwl;
  }
    
  /**
   * Set NodeRealizer and EdgeRealizer defaults. We use a mixed style here. Employees will be visualized using 
   * GenericNodeRealizer with a custom Painter implementation. Edges are represented using
   * a PolyLineEdgeRealizer, while group nodes get displayed by a customized GroupNodeRealizer.
   */
  protected void setRealizerDefaults() {
    PolyLineEdgeRealizer er = new PolyLineEdgeRealizer();
    er.setSmoothedBends(true);
    er.setLineColor(EDGE_COLOR);
    er.setLineType(LineType.LINE_2);

    //register default node realizer
    getGraph2D().setDefaultEdgeRealizer(er);
    
    GenericNodeRealizer.Factory factory = GenericNodeRealizer.getFactory();
    
    Painter painter = new EmployeePainter();
    //uncomment this for a nice (but rather expensive) drop shadow effect 
    //painter = new ShadowNodePainter(painter); 
    Map implementationsMap = factory.createDefaultConfigurationMap();        
    implementationsMap.put(GenericNodeRealizer.Painter.class, painter);
            
    factory.addConfiguration("employee", implementationsMap);      
    GenericNodeRealizer nr = new GenericNodeRealizer();
    nr.setFillColor(FILL_COLOR);
    nr.setFillColor2(FILL_COLOR2);
    nr.setLineColor(LINE_COLOR);
    nr.setLineType(LineType.LINE_2);
    nr.setSize(220,110);

    nr.setConfiguration("employee");
    
    //register default node realizer
    getGraph2D().setDefaultNodeRealizer(nr);
    
    DefaultHierarchyGraphFactory gf = (DefaultHierarchyGraphFactory) getGraph2D().getHierarchyManager().getGraphFactory();
    CustomGroupNodeRealizer gnr = new CustomGroupNodeRealizer();
    gnr.setConsiderNodeLabelSize(true);
    gnr.setShapeType(ShapeNodeRealizer.ROUND_RECT);
    gnr.setFillColor(GROUP_FILL_COLOR);  
    gnr.setLineType(LineType.LINE_1);
    gnr.setLineColor(Color.DARK_GRAY); 
    gnr.getLabel().setBackgroundColor(new Color(102,204,255,200));
    gnr.getLabel().setTextColor(new Color(31,104,163).darker());
    gnr.getLabel().setFontSize(20);
    gnr.getLabel().setAlignment(NodeLabel.ALIGN_LEFT);
    gnr.getStateLabel().setVisible(false);

    //register default group and folder nodes
    gf.setDefaultFolderNodeRealizer(gnr);
    gf.setDefaultGroupNodeRealizer(gnr.createCopy());
  }
     
  /**
   * Configures a realizer for a group node that is identified by a group Id.
   */
  protected void configureGroupRealizer(Node groupNode, Object groupId, boolean collapsed) {
    GroupNodeRealizer nr = (GroupNodeRealizer) getGraph2D().getRealizer(groupNode);
    nr.setGroupClosed(collapsed);    
    nr.setBorderInsets(new YInsets(0,0,0,0));
    nr.getLabel().setText(groupId.toString());            
  }  
  
  /**
   * Configures a realizer for a node representing an employee. This implementation uses node labels
   * to represent and cache visual representations used at different levels of detail. Labels
   * not displayed at a certain level of detail will be set to invisible.
   */
  protected void configureNodeRealizer(Node n) {
    Employee employee = (Employee) getUserObject(n);
    
    GenericNodeRealizer gnr = (GenericNodeRealizer) getGraph2D().getRealizer(n);
    gnr.setUserData(employee);
        
    if(gnr.labelCount() == 1) { //NOT CONFIGURED YET
      //LABEL 0: EMPLOYEE ICON
      NodeLabel label = gnr.getLabel();
      label.setIcon(employee.icon);
      label.setPosition(NodeLabel.TOP_LEFT);
      label.setInsets(new Insets(2,2,2,2));

      //LABEL 0-4: EMPLOYEE INFORMATION
      label = addTextLabel(gnr, employee.name,     65, 4, true); 
      label = addTextLabel(gnr, employee.position, 65, label.getOffsetY() + label.getHeight() + 4, true); // label 2
      label = addTextLabel(gnr, employee.email,    65, label.getOffsetY() + label.getHeight() + 4, true); // label 3
      label = addTextLabel(gnr, employee.phone,    65, label.getOffsetY() + label.getHeight() + 4, true); // label 4
      label = addTextLabel(gnr, employee.fax,      65, label.getOffsetY() + label.getHeight() + 4, true); // label 5
      
      //LABEL 6: STATE ICON 
      label = gnr.createNodeLabel(); 
      gnr.addLabel(label);
      label.setIcon(new StateIcon(employee.status));      
      label.setPosition(NodeLabel.TOP_RIGHT);
      label.setInsets(new Insets(2,2,2,2));

      //LABEL 7: USED FOR LOD & MEDIUM LOD ONLY  
      label = addTextLabel(gnr, employee.name,0,0, false);   
      label.setModel(NodeLabel.INTERNAL);
      label.setModel(NodeLabel.CENTER);
      label.setFontSize(20);
      label.setVisible(false);
    }
  }
  
  /**
   * Configures an edge realizer for the links between employees. Links that connect an employee with his/her assistant
   * will be represented by a dashed line.
   */ 
  protected void configureEdgeRealizer(Edge e) {
    EdgeRealizer er = getGraph2D().getRealizer(e);
    Employee target = (Employee) getUserObject(e.target());    
    if(target != null && "true".equals(target.assistant)) {
      er.setLineType(LineType.DASHED_2);
    }
    super.configureEdgeRealizer(e);
  }
  
  /**
   * Helper method that creates and returns node labels.
   */
  NodeLabel addTextLabel(NodeRealizer nr, String text, double x, double y, boolean cropping) {
    NodeLabel label = nr.createNodeLabel();
    nr.addLabel(label);      
    label.setModel(NodeLabel.FREE);
    label.setFontSize(11);
    if(text == null) text = "";
    label.setText(text);
    final double h = label.getHeight();
    label.setModelParameter(label.getBestModelParameterForBounds(
            new OrientedRectangle(nr.getX() + x, nr.getY() + y + h, label.getWidth(), h)));

    if(cropping) {
      double height = label.getHeight();
      label.setAutoSizePolicy(YLabel.AUTOSIZE_NONE);
      label.setConfiguration("CroppingLabel");
      label.setAlignment(YLabel.ALIGN_LEFT);      
      label.setModelParameter(label.getBestModelParameterForBounds(
              new OrientedRectangle(nr.getX() + x, nr.getY() + y + h, label.getWidth(), h)));
      double width = nr.getWidth() - x;
      label.setContentSize(width, height);                    
    }
    return label;
  }
    
  /**
   * State Icon implementation. The state icon is used by the EmployeePainter to represent 
   * the state of an employee (present, unavailable, travel). Its implementation makes use of
   * LODRenderingHints.
   */
  static class StateIcon implements Icon {

    static final Color STATUS_UNAVAILABLE_COLOR = new Color(255,120,40);
    static final Color STATUS_PRESENT_COLOR = new Color(25,205,44);
    static final Color STATUS_TRAVEL_COLOR = new Color(221,175,233);
    static final Color STATUS_UNAVAILABLE_COLOR2 = new Color(231,32,0);
    static final Color STATUS_PRESENT_COLOR2 = new Color(19,157,33);
    static final Color STATUS_TRAVEL_COLOR2 = new Color(137,44,160);

    String state;
    StateIcon(String state) {
      this.state = state;
    }
    
    public int getIconHeight() {
      return 24;
    }

    public int getIconWidth() {
      return 24;
    }

    public void paintIcon(Component c, Graphics g, int x, int y) {
      Graphics2D gfx = (Graphics2D) g;
      
      Object lod = gfx.getRenderingHint(LODRenderingHint.KEY_LOD);
      
      if(lod == LODRenderingHint.VALUE_LOD_MEDIUM) {
        if("present".equals(state)) {
          gfx.setColor(STATUS_PRESENT_COLOR);
        } 
        else if("travel".equals(state)) {
          gfx.setColor(STATUS_TRAVEL_COLOR);
        }
        else if("unavailable".equals(state)){
          gfx.setColor(STATUS_UNAVAILABLE_COLOR);
        }
        
        Rectangle2D box = new Rectangle2D.Double(x,y,getIconWidth(), getIconHeight());
        gfx.fill(box);
        gfx.setColor(gfx.getColor().darker());
        gfx.draw(box);       
      }
      else if(lod == LODRenderingHint.VALUE_LOD_HIGH) {
        Ellipse2D.Double circle = new Ellipse2D.Double(x,y,getIconWidth(), getIconHeight());
        if("present".equals(state)) {
          gfx.setColor(STATUS_PRESENT_COLOR2);          
        } 
        else if("travel".equals(state)) {
          gfx.setColor(STATUS_TRAVEL_COLOR2);
        }
        else if("unavailable".equals(state)){
          gfx.setColor(STATUS_UNAVAILABLE_COLOR2);
        }
        gfx.fill(circle);
        double size = Math.min(getIconHeight(), getIconWidth());
        double delta = size*0.12;
        circle.setFrame(circle.x+delta, circle.y+delta, circle.width-2*delta, circle.height-2*delta);        
        gfx.setColor(Color.WHITE);
        gfx.fill(circle);
        if("present".equals(state)) {
          gfx.setColor(STATUS_PRESENT_COLOR);          
        } 
        else if("travel".equals(state)) {
          gfx.setColor(STATUS_TRAVEL_COLOR);
        }
        else if("unavailable".equals(state)){
          gfx.setColor(STATUS_UNAVAILABLE_COLOR);
        }        
        circle.setFrame(circle.x+delta, circle.y+delta, circle.width-2*delta, circle.height-2*delta);        
        gfx.fill(circle);        
      }
    }    
  }
 
  /**
   * Painter implementation that renders an Employee node.
   */
  static class EmployeePainter extends ShapeNodePainter {
    
    public EmployeePainter() {
      setShapeType(ROUND_RECT);
    }
    
    public void paint(NodeRealizer context, Graphics2D gfx) {
      
      if(!context.isVisible()) {
        return;
      }
      
      GenericNodeRealizer gnr = (GenericNodeRealizer) context;    
      Employee employee = (Employee) gnr.getUserData();    
      
      Object lod = gfx.getRenderingHint(LODRenderingHint.KEY_LOD);
      
      if(lod == LODRenderingHint.VALUE_LOD_OVERVIEW) {
        Color ovc = null;
        if("present".equals(employee.status)) {
          ovc = new Color(19,157,33); //green
        } else if("travel".equals(employee.status)) {
          ovc = new Color(137,44,160);
        }
        else {
          ovc = Color.RED;
        }
        Rectangle2D rect = new Rectangle2D.Double(context.getX()+20, context.getY()+20, context.getWidth()-40, context.getHeight()-40);
        gfx.setColor(ovc);
        gfx.fill(rect);
        gfx.setColor(Color.black);
        gfx.draw(rect);
      }
      else if(lod == LODRenderingHint.VALUE_LOD_LOW || lod == LODRenderingHint.VALUE_LOD_MEDIUM) { 
        super.paintNode(context, gfx, true);
        
        context.getLabel(6).paint(gfx); //state icon 
        
        NodeLabel label = context.getLabel(7);
        label.setVisible(true);
        label.paint(gfx);
        label.setVisible(false);        
      }
      else { //defaults to LODRenderingHint.VALUE_LOD_HIGH) 
        super.paintNode(context, gfx, false);
        context.paintText(gfx);
      }      
    }
    
    public void paintSloppy(NodeRealizer context, Graphics2D gfx) {
      paint(context, gfx);
    }
    
    protected Color createSelectionColor(Color original) {
      // don't modify the fill color here - we are
      // changing the fill color externally upon selection
      return original;
    }

    /**
     * Fill the shape using a custom gradient color and a semi-transparent effect  
     */
    protected void paintFilledShape(NodeRealizer context, Graphics2D graphics, Shape shape) {
      double x = context.getX();
      double y = context.getY();
      double width = context.getWidth();
      double height = context.getHeight();

      Color c1 = getFillColor(context, context.isSelected());
      
      if (c1 != null && !context.isTransparent()) {
        Color c2 = getFillColor2(context, context.isSelected());
        if (c2 != null) {
          graphics.setPaint(
              new GradientPaint((float) (x + 0.5*width), (float) y, c1, (float) (x + 0.5*width), (float) (y+height), c2)
              );
        } else {
          graphics.setColor(c1);
        }
        graphics.fill(shape);
                
        Shape clip = graphics.getClip();        
        graphics.clip(shape);
        graphics.clip(new Rectangle2D.Double(x,y,width,0.4*height));
        graphics.setColor(new Color(1.0f, 1.0f,1.0f, 0.3f));
        graphics.fill(shape);
        graphics.setClip(clip);
      }
    }   
  }

  /**
   * Customized GroupNodeRealizer that cannot be selected. 
   */
  static class CustomGroupNodeRealizer extends GroupNodeRealizer {
    public CustomGroupNodeRealizer() {
      super();
    }
    public CustomGroupNodeRealizer(NodeRealizer nr) {
      super(nr);
    }
    public NodeRealizer createCopy(NodeRealizer nr) {
      return new CustomGroupNodeRealizer(nr);
    }
    public boolean isSelected() {
      return false;
    }
  }
  
  /**
   * Drawable implementation used by the NavigationMode Scroller. 
   * The appearance of the Scroller Drawable is zoom invariant. To accomplish this
   * it is drawn in view coordinate space.  
   */
  static class ScrollerDrawable extends ViewCoordDrawableAdapter {
    Scroller scroller;
    public ScrollerDrawable(Graph2DView view, Scroller scroller) {
      super(view, null);
      this.scroller = scroller;
    }

    protected void paintViewCoordinateDrawable(Graphics2D gfx) {     
      gfx = (Graphics2D)gfx.create();            
      YVector dir = scroller.getScrollDirection();
      YPoint p = scroller.getScrollStart();
      p = new YPoint(view.toViewCoordX(p.x), view.toViewCoordY(p.y));
      Ellipse2D circle = new Ellipse2D.Double(p.x-15, p.y-15,30,30);
      gfx.setColor(new Color(204,204,204,100));
      gfx.fill(circle);
      gfx.setColor(new Color(100,100,100,100));
      gfx.setStroke(LineType.LINE_1);
      AffineTransform trans = new AffineTransform(dir.getX(), dir.getY(),-dir.getY(), dir.getX(),p.x,p.y);
      GeneralPath arrow = new GeneralPath(GeneralPath.WIND_NON_ZERO,6);
      arrow.moveTo(15,0);
      arrow.lineTo(0,5);
      arrow.lineTo(0,-5);      
      gfx.fill(trans.createTransformedShape(arrow));      
      gfx.setStroke(LineType.LINE_2);
      gfx.draw(circle);      
      gfx.dispose();
    }

    protected Rectangle getViewCoordinateDrawableBounds() {
      YPoint p = scroller.getScrollStart();
      p = new YPoint(view.toViewCoordX(p.x), view.toViewCoordY(p.y));        
      return (new Rectangle2D.Double(p.x-20,p.y-20,40,40)).getBounds();
    }
  }

  /**
   * Configures and creates an overview component that uses this JOrgChart as its master view. 
   */
  public Overview createOverview() {
    Overview ov = super.createOverview();
    /* customize the overview */
    //animates the scrolling
    //blurs the part of the graph which can currently not be seen
    ov.putClientProperty("Overview.PaintStyle", "Funky");
    //allows zooming from within the overview
    ov.putClientProperty("Overview.AllowZooming", Boolean.TRUE);
    //provides functionality for navigation via keyboard (zoom in (+), zoom out (-), navigation with arrow keys)
    ov.putClientProperty("Overview.AllowKeyboardNavigation", Boolean.TRUE);
    //determines how to differ between the part of the graph that can currently be seen, and the rest
    ov.putClientProperty("Overview.Inverse", Boolean.TRUE);
    ov.setPreferredSize(new Dimension(200, 150));
    ov.setMinimumSize(new Dimension(200, 150));
    ov.getRenderingHints().put(LODRenderingHint.KEY_LOD, LODRenderingHint.VALUE_LOD_OVERVIEW);
    return ov;
  }

  private void configureColors(
          final NodeRealizer nr,
          final boolean selected,
          final boolean highlighted
  ) {
    if(highlighted) {
      if(selected) {
        nr.setFillColor(SELECTED_HOVER_FILL_COLOR);
        nr.setFillColor2(SELECTED_HOVER_FILL_COLOR2);
        nr.setLineColor(SELECTED_HOVER_LINE_COLOR);
      } else { //not selected
        nr.setFillColor(HOVER_FILL_COLOR);
        nr.setFillColor2(HOVER_FILL_COLOR2);
        nr.setLineColor(HOVER_LINE_COLOR);
      }
    } else { //not highlighted
      if(selected) {
        nr.setFillColor(SELECTED_FILL_COLOR);
        nr.setFillColor2(SELECTED_FILL_COLOR2);
        nr.setLineColor(SELECTED_LINE_COLOR);
      } else { // not selected
        nr.setFillColor(FILL_COLOR);
        nr.setFillColor2(FILL_COLOR2);
        nr.setLineColor(LINE_COLOR);
      }
    }
  }


  /**
   * ViewMode responsible for displaying a tool tip for graph elements.
   */
  static class TooltipMode extends ViewMode {
    public void mouseMoved(double x, double y) {
      HitInfo info = getHitInfo(x, y);
      Node node = info.getHitNode();
      if(node != null && getGraph2D().getHierarchyManager().isNormalNode(node)) {
        JOrgChart view = (JOrgChart) this.view;
        Employee e = (Employee) view.getUserObject(node);
        Object lod = view.getRenderingHints().get(LODRenderingHint.KEY_LOD);
        if(lod == LODRenderingHint.VALUE_LOD_LOW || lod == LODRenderingHint.VALUE_LOD_MEDIUM) { //tiny
          view.setToolTipText("<html><b>" + e.name + "</b><br>" + e.position + "<br>Status " + e.status);
        }
        else if(lod == LODRenderingHint.VALUE_LOD_HIGH) { 
          NodeLabel stateLabel = view.getGraph2D().getRealizer(node).getLabel(6);
          if(stateLabel.contains(x,y)) {
            view.setToolTipText("Status " + e.status);
          } else {
            view.setToolTipText(null);
          }
        }
      }
      else {
        view.setToolTipText(null);
      }      
    }
  }

  /**
   * Implementation of a ViewMode that activates {@link y.view.MagnifierViewMode} while
   * the middle mouse button or the mouse wheel is pressed or dragged.
   */
  static class MiddleClickMagnifierViewMode extends ViewMode {        
    MagnifierViewMode magnifierVM;
    public MiddleClickMagnifierViewMode() {
      magnifierVM = new MagnifierViewMode() {
        public void mouseDraggedMiddle(double x, double y) {
          mouseDraggedLeft(x, y);
        }
        protected Graph2DView createMagnifierView() {
          Graph2DView view = super.createMagnifierView();
          view.getRenderingHints().put(LODRenderingHint.KEY_LOD, LODRenderingHint.VALUE_LOD_HIGH);
          return view;
        }
      };
     magnifierVM.setMouseWheelEnabled(false);     
    }    
    
    public void mousePressedMiddle(double x, double y) {
      double zoom = view.getZoom();
      magnifierVM.setMagnifierZoomFactor(Math.max(1,1/zoom));
      magnifierVM.setMagnifierRadius(200);
      view.addViewMode(magnifierVM);
      magnifierVM.mouseMoved(lastPressEvent);
      view.updateView();
    }
    
    public void mouseReleasedMiddle(double x, double y) {
      view.removeViewMode(magnifierVM);
    }    
  }

  /**
   * A <code>ViewMode</code> that produces a roll-over effect for nodes
   * under the mouse cursor.
   */
  private class RollOverViewMode extends ViewMode {
    
    /** Preferred duration for roll over effect animations */
    private static final int PREFERRED_DURATION = 200;

    /** Stores the last node that was marked with the roll over effect */
    private Node lastHitNode;
    private AnimationPlayer player;
    private PropertyChangeListener pcl;
    private MouseEvent lastRelevantMouseEvent;
    
    public RollOverViewMode() {
    }

    public void activate(boolean b) {    
      super.activate(b);
      if(b) {
        player = new AnimationPlayer();
        player.addAnimationListener(view);
        player.setBlocking(false);
        pcl = new CanvasPropertyChangeListener();
        view.getCanvasComponent().addPropertyChangeListener(pcl);
      } else if(pcl != null){        
        view.getCanvasComponent().removePropertyChangeListener(pcl);
      }
    }
    
    public void mouseExited(MouseEvent e) {
      lastRelevantMouseEvent = null;
      if(lastHitNode != null) {
        unmark(lastHitNode);
      }
    }
    
    public void mouseDragged(MouseEvent e) {
      lastRelevantMouseEvent = e;
      super.mouseDragged(e);
    }

    public void mouseMoved(MouseEvent e) {
      lastRelevantMouseEvent = e;
      super.mouseMoved(e);
    }

    /**
     * Triggers a roll-over effect for the first node at the specified location.
     */
    public void mouseMoved( final double x, final double y ) {      
      final HitInfo hi = getHitInfo(x, y);
      
      if (hi.hasHitNodes()) {
        final Node node = (Node) hi.hitNodes().current();
        if (node != lastHitNode) {
          unmark(lastHitNode);
        }
        JOrgChart treeView = (JOrgChart) view;
        Object userObject = treeView.getUserObject(node);        
        if(userObject != null && !treeView.highlightMap.getBool(node)) {
          mark(node);
          lastHitNode = node;
        }
      } else {
        unmark(lastHitNode);
        lastHitNode = null;
      }
    }
    
     /**
     * Overridden to take only nodes into account for hit testing.
     */
    protected HitInfo getHitInfo( final double x, final double y ) {
      final HitInfo hi = new HitInfo(view, x, y, true, HitInfo.NODE);
      setLastHitInfo(hi);
      return hi;
    }
        
    /**
     * Triggers a <em>mark</em> animation for the specified node.
     * Sets the animation state of the given node to <em>MARKED</em>.
     */
    protected void mark( final Node node ) {
      // only start a mark animation if no other animation is playing
      // for the given node
      JOrgChart treeView = (JOrgChart) view;
      boolean highlighted = treeView.highlightMap.getBool(node);
      Object userObject = treeView.getUserObject(node);
      
      if(userObject != null && !highlighted) {
        treeView.highlightMap.setBool(node, true);
        
        final NodeRealizer nr = getGraph2D().getRealizer(node);
        final NodeRealizer newTheme = nr.createCopy();
        configureColors(newTheme, getGraph2D().isSelected(node), true);
        
        ViewAnimationFactory animFac = new ViewAnimationFactory(view);
        final AnimationObject ao = animFac.color(
                nr,
                newTheme.getFillColor(), newTheme.getFillColor2(), newTheme.getLineColor(),
                ViewAnimationFactory.APPLY_EFFECT, PREFERRED_DURATION);

        final AnimationObject eao = AnimationFactory.createEasedAnimation(ao);        
        player.animate(eao); 
      }     
    }

    class CanvasPropertyChangeListener implements PropertyChangeListener {
      public void propertyChange(PropertyChangeEvent evt) {
        if(lastRelevantMouseEvent != null && ("Zoom".equals(evt.getPropertyName()) || "ViewPoint".equals(evt.getPropertyName()))) {
          if(lastHitNode != null && getHitInfo(lastRelevantMouseEvent).getHitNode() != lastHitNode) {
            unmark(lastHitNode);
            lastHitNode = null;
          }
          else if(lastHitNode == null && lastRelevantMouseEvent != null) {
            lastHitNode = getHitInfo(lastRelevantMouseEvent).getHitNode();
            if(lastHitNode != null) {
              mark(lastHitNode);
            }
          }
        }          
      }        
    }
    
    /**
     * Triggers an <em>unmark</em> animation for the specified node.
     * Sets the animation state of the given node to <em>UNMARKED</em>.
     */
    protected void unmark( final Node node ) {
      if (node == null || node.getGraph() == null) {
        player.stop();
        return;
      }
      JOrgChart treeView = (JOrgChart) view;
      Object userObject = null;      
      try {
        userObject = treeView.getUserObject(node);
      }
      catch(Exception ex) {
        D.bug(node);
      }
      if(userObject != null && treeView.highlightMap.getBool(node)) {
        treeView.highlightMap.setBool(node, false);
    
        player.stop();
        configureColors(getGraph2D().getRealizer(node), getGraph2D().isSelected(node), false);
        getGraph2D().updateViews();
      }      
    }    
  }

  /**
   * {@link y.view.Graph2DSelectionListener} that assigns a properly configured 
   * realizer to a node whenever its selected state changes.
   */
  class TreeChartSelectionListener implements Graph2DSelectionListener {
    public void onGraph2DSelectionEvent(Graph2DSelectionEvent ev) {
      if(ev.getSubject() instanceof Node) {
        Node node = (Node) ev.getSubject();        
        Object userObject = getUserObject(node);
        if(userObject != null) {
          Graph2D graph = getGraph2D();
          configureColors(getGraph2D().getRealizer(node), graph.isSelected(node), highlightMap.getBool(node));
          graph.updateViews();
        }
      }
    }    
  }
  
  /**
   * Calculate a layout for organization charts. The layouter takes the Employee's <code>layout</code> 
   * and <code>assistant</code> properties into account.  
   */
  protected Layouter createLayouter() {
    OrgChartLayouter layouter = new OrgChartLayouter();
    Graph2D graph = getGraph2D();      
    DataProvider childLayoutDP = new DataProviderAdapter() {        
      public Object get(Object n) {
        Employee employee = (Employee) getUserObject((Node)n);
        if(employee != null) {
          if("leftHanging".equals(employee.layout)) {
            return OrgChartLayouter.CHILD_LAYOUT_LEFT_BELOW;
          }
          if("rightHanging".equals(employee.layout)) {
            return OrgChartLayouter.CHILD_LAYOUT_RIGHT_BELOW;
          }
          if("bothHanging".equals(employee.layout)) {
            return OrgChartLayouter.CHILD_LAYOUT_BELOW;
          }
        }
        return OrgChartLayouter.CHILD_LAYOUT_SAME_LAYER;
      }          
    };
    graph.addDataProvider(OrgChartLayouter.CHILD_LAYOUT_DPKEY, childLayoutDP);
    
    DataProvider assistantDP = new DataProviderAdapter() {
      public boolean getBool(Object n) {
        Employee employee = (Employee) getUserObject((Node)n);          
        return employee != null && "true".equals(employee.assistant);
      }
    };      
    graph.addDataProvider(OrgChartLayouter.ASSISTANT_DPKEY, assistantDP);
    
    layouter.setDuplicateBendsOnSharedBus(true);

    return new NormalizingGraphElementOrderStage(layouter);
  }  
}
