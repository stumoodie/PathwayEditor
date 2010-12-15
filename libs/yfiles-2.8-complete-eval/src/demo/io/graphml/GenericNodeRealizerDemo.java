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

import y.util.AbstractStringConverter;
import y.util.ObjectStringConversion;
import y.view.AbstractCustomHotSpotPainter;
import y.view.AbstractCustomNodePainter;
import y.view.GenericNodeRealizer;
import y.view.NodeRealizer;
import y.view.SimpleUserDataHandler;
import y.view.ShinyPlateNodePainter;
import y.view.Graph2D;
import y.view.PolyLineEdgeRealizer;
import y.view.Arrow;
import y.base.Node;

import javax.swing.JComboBox;
import javax.swing.JToolBar;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.RectangularShape;
import java.util.HashMap;
import java.util.Map;

import demo.view.DemoDefaults;

/**
 * This class demonstrates how to create customized node realizers 
 * of type {@link GenericNodeRealizer}, how to add user data and how
 * to read and write these customized types in GraphML format.
 */
public class GenericNodeRealizerDemo extends GraphMLDemo {

  /** Create the demo */
  public GenericNodeRealizerDemo() {
    super();

    //Since we use some custom data type for our UserData, make sure there is some conversion to/from strings available.
    ObjectStringConversion.getInstance().registerObjectStringConverter(UserData.class,
        new AbstractStringConverter(UserData.class) {
          protected Object convertToObject(String o) throws IllegalArgumentException {
            return new UserData(Integer.parseInt(o));
          }

          protected String convertToString(Object o) throws IllegalArgumentException {
            return String.valueOf(((UserData) o).value);
          }
        });

    // get the factory to register our own styles
    GenericNodeRealizer.Factory factory = GenericNodeRealizer.getFactory();

    // use a map to pass in our implementations
    Map implementationsMap = new HashMap();
    // create custom implementations for ...

    // the painter and contains test
    CustomPainter painter = new CustomPainter(new Ellipse2D.Double());
    // register the painter
    implementationsMap.put(GenericNodeRealizer.Painter.class, painter);
    // and the contains test
    implementationsMap.put(GenericNodeRealizer.ContainsTest.class, painter);

    // create a custom hotspot painter and hot spot hit test
    CustomHotSpotPainter chsp = new CustomHotSpotPainter(165, new Ellipse2D.Double(), null);
    // register the painter
    implementationsMap.put(GenericNodeRealizer.HotSpotPainter.class, chsp);
    // and the hit test
    implementationsMap.put(GenericNodeRealizer.HotSpotHitTest.class, chsp);

    // a simple default implementation that can deal with cloneable and serializable userdata....
    implementationsMap.put(GenericNodeRealizer.UserDataHandler.class,
        new SimpleUserDataHandler(SimpleUserDataHandler.REFERENCE_ON_FAILURE));

    // finally add the configuration to the factory
    factory.addConfiguration("Circle", implementationsMap);

    // do the same with two different styles...
    ShinyPlateNodePainter shinyPainter = new ShinyPlateNodePainter() {
      protected void paintNode(NodeRealizer context, Graphics2D graphics, boolean sloppy) {
        super.paintNode(context, graphics, sloppy);
        paintUserData(context, graphics);
      }
    };
    shinyPainter.setDrawShadow(true);
    implementationsMap.put(GenericNodeRealizer.Painter.class, shinyPainter);
    implementationsMap.put(GenericNodeRealizer.ContainsTest.class, shinyPainter);
    factory.addConfiguration("Round Rectangle", implementationsMap);

    GeneralPath gp = new GeneralPath();
    gp.moveTo(1.0f, 0.5f);
    gp.lineTo(0.0f, 1.0f);
    gp.quadTo(0.0f, 0.5f, 0.3f, 0.5f);
    gp.quadTo(0.0f, 0.5f, 0.0f, 0.0f);
    gp.closePath();

    PolygonPainter pp = new PolygonPainter(gp);
    implementationsMap.put(GenericNodeRealizer.Painter.class, pp);
    implementationsMap.put(GenericNodeRealizer.ContainsTest.class, pp);
    factory.addConfiguration("Butterfly", implementationsMap);

    // Create the default node realizer.
    Graph2D graph = view.getGraph2D();
    graph.setDefaultNodeRealizer(new GenericNodeRealizer());

    // Create an initial graph.
    configureDefaultNodeRealizer("Circle", 1);
    Node circle = graph.createNode(60, 60, "3");
    configureDefaultNodeRealizer("Round Rectangle", 2);
    Node rectangle = graph.createNode(300, 60, "2");
    configureDefaultNodeRealizer("Butterfly", 3);
    Node butterfly = graph.createNode(180, 220, "1");

    graph.createEdge(circle, rectangle);
    PolyLineEdgeRealizer edgeRealizer1 = new PolyLineEdgeRealizer();
    edgeRealizer1.setTargetArrow(Arrow.STANDARD);
    graph.createEdge(rectangle, butterfly, edgeRealizer1);
    edgeRealizer1.insertBend(300, 220);
    PolyLineEdgeRealizer edgeRealizer2 = new PolyLineEdgeRealizer();
    edgeRealizer2.setTargetArrow(Arrow.STANDARD);
    graph.createEdge(butterfly, circle, edgeRealizer2);
    edgeRealizer2.insertBend(60, 220);
    
    graphMLPane.updateGraphMLText(graph);
    
    // Set the default for new nodes to the initial value of the combo box for choosing
    // the default node realizer (see below).
    configureDefaultNodeRealizer("Circle", 1);
  }

  /** Create a toolbar to switch the default node realizer type */
  protected JToolBar createToolBar() {
    JToolBar retValue;

    retValue = super.createToolBar();
    final JComboBox cb = new JComboBox(new Object[]{"Circle", "Round Rectangle", "Butterfly"});
    cb.setMaximumSize(new Dimension(200, 100));
    retValue.add(cb);
    cb.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent ae) {
        String configuration = cb.getSelectedItem().toString();
        int userInt = cb.getSelectedIndex() + 1;
        configureDefaultNodeRealizer(configuration, userInt);
      }
    });
    return retValue;
  }


  protected String[] getSampleFiles() {
    return null;
  }

  protected void loadInitialGraph() {
    //No initial sample graph (it's created programmatically here...)
  }

  protected static void paintUserData(NodeRealizer context, Graphics2D graphics) {
    UserData data = (UserData) ((GenericNodeRealizer) context).getUserData();
    graphics.setColor(Color.black);
    graphics.drawString("data=" + data.value,
        (float) context.getX(),
        (float) (context.getY() - 1));
  }

  private void configureDefaultNodeRealizer(String configuration, int userInt) {
    GenericNodeRealizer gnr = (GenericNodeRealizer) view.getGraph2D().getDefaultNodeRealizer();
    gnr.setConfiguration(configuration);
    gnr.setUserData(new UserData(userInt));
    gnr.setFillColor(DemoDefaults.DEFAULT_NODE_COLOR);
  }

  /**
   * A custom HotSpotPainter implementation
   */
  static final class CustomHotSpotPainter extends AbstractCustomHotSpotPainter {

    private RectangularShape shape;
    private Color color;

    CustomHotSpotPainter(int mask, RectangularShape shape, Color color) {
      super(mask);
      this.shape = shape;
      this.color = color;
    }

    protected void initGraphics(NodeRealizer context, Graphics2D g) {
      super.initGraphics(context, g);
      if (color == null) {
        Color fc = context.getFillColor();
        if (fc != null) {
          g.setColor(fc);
        }
      } else {
        g.setColor(color);
      }
    }


    protected void paint(byte hotSpot, double centerX, double centerY, Graphics2D graphics) {
      shape.setFrame(centerX - 2, centerY - 2, 5, 5);
      graphics.fill(shape);
    }

    protected boolean isHit(byte hotSpot, double centerX, double centerY, double testX, double testY) {
      return Math.abs(testX - centerX) < 3 && Math.abs(testY - centerY) < 3;
    }

  }

  /**
   * A custom Painter and ContainsTest implementation.
   * This one works with any kind of RectangularShape
   */
  static final class CustomPainter extends AbstractCustomNodePainter implements GenericNodeRealizer.ContainsTest {
    RectangularShape shape;

    CustomPainter(RectangularShape shape) {
      this.shape = shape;
    }

    /** Override default fill color */
    protected Color getFillColor(NodeRealizer context, boolean selected) {
      if (selected) {
        return Color.red;
      } else {
        return super.getFillColor(context, selected);
      }
    }

    protected void paintNode(NodeRealizer context, Graphics2D graphics, boolean sloppy) {
      shape.setFrame(context.getX(), context.getY(), context.getWidth(), context.getHeight());
      if (initializeFill(context, graphics)) {
        graphics.fill(shape);
      }
      if (initializeLine(context, graphics)) {
        graphics.draw(shape);
      }
      paintUserData(context, graphics);
    }

    public boolean contains(NodeRealizer context, double x, double y) {
      shape.setFrame(context.getX(), context.getY(), context.getWidth(), context.getHeight());
      return shape.contains(x, y);
    }
  }

  /**
   * Another custom Painter and ContainsTest implementation.
   * This one works with any kind of GeneralPath
   */
  static final class PolygonPainter extends AbstractCustomNodePainter implements GenericNodeRealizer.ContainsTest {
    GeneralPath path;
    AffineTransform aft;

    PolygonPainter(GeneralPath path) {
      this.path = path;
      this.aft = AffineTransform.getScaleInstance(1.0d, 1.0d);
    }

    protected void paintNode(NodeRealizer context, Graphics2D graphics, boolean sloppy) {
      aft.setToIdentity();
      aft.translate(context.getX(), context.getY());
      aft.scale(context.getWidth(), context.getHeight());
      Shape shape = path.createTransformedShape(aft);
      if (initializeFill(context, graphics)) {
        graphics.fill(shape);
      }
      if (initializeLine(context, graphics)) {
        graphics.draw(shape);
      }
      paintUserData(context, graphics);
    }

    /** Override default fill color to be the same as the unselected fill color */
    protected Color getFillColor(NodeRealizer context, boolean selected) {
      return super.getFillColor(context, false);
    }

    public boolean contains(NodeRealizer context, double x, double y) {
      return path.contains((x - context.getX()) / context.getWidth(), (y - context.getY()) / context.getHeight());
    }
  }

  /**
   * The type for the user data that is associated with GenericNodeRealizer.
   */
  static class UserData {
    int value;

    UserData(int value) {
      this.value = value;
    }
  }

  /**
   * Launcher method. Execute this class to see sample instantiations of
   * the CustomNodeRealizer in action.
   */
  public static void main(String[] args) {
    EventQueue.invokeLater(new Runnable() {
      public void run() {
        initLnF();
        (new GenericNodeRealizerDemo()).start();
      }
    });
  }
}
