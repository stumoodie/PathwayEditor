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
package demo.view.realizer;

import demo.view.DemoBase;
import demo.view.application.DragAndDropDemo;

import y.geom.YDimension;
import y.view.AbstractCustomHotSpotPainter;
import y.view.AbstractCustomNodePainter;
import y.view.BevelNodePainter;
import y.view.EditMode;
import y.view.GeneralPathNodePainter;
import y.view.GenericNodeRealizer;
import y.view.ImageNodePainter;
import y.view.NodeLabel;
import y.view.NodeRealizer;
import y.view.ShadowNodePainter;
import y.view.ShapeNodePainter;
import y.view.ShinyPlateNodePainter;
import y.view.SimpleUserDataHandler;
import y.view.YRenderingHints;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RectangularShape;
import java.awt.geom.RoundRectangle2D;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JList;
import javax.swing.JScrollPane;

/**
 * This class demonstrates various usages of the {@link y.view.GenericNodeRealizer} class.
 *
 * It shows how to create different configurations and also shows the usage of
 * some custom {@link y.view.GenericNodeRealizer.Painter} and
 * {@link y.view.GenericNodeRealizer.ContainsTest} implementations.
 */
public class GenericNodeRealizerDemo extends DemoBase {

  /** Creates the GenericNodeRealizer demo. */
  public GenericNodeRealizerDemo() {
    super();

    //create several NodeRealizer Configurations
    List configurations = createConfigurations();

    //create the drag and drop list filled with the available realizer configurations
    JList realizerList = createDnDList(configurations);
    realizerList.setBackground(Color.WHITE);

    //add the realizer list to the panel
    contentPane.add(new JScrollPane(realizerList), BorderLayout.WEST);

    realizerList.setSelectedIndex(0);
    GenericNodeRealizer gnr = (GenericNodeRealizer) realizerList.getSelectedValue();

    view.getGraph2D().setDefaultNodeRealizer(gnr.createCopy());

    //load an initial graph
    loadGraph("resource/genericNodeRealizer.graphml");   
  }
  
  /**
   * Creates a JList that contains GenericNodeRealizers that are configured with configuration names from the given
   * List.
   */
  private JList createDnDList(List configurations) {
    // create the list of NodeRealizer instances

    final List realizers = createRealizers(configurations);
    // create the customized DnD support instance
    return new DragAndDropDemo.DragAndDropSupport(realizers, view).getList();
  }

  /**
   * Creates GenericNodeRealizer configurations and registers them on the factory.
   *
   * @return the names of the registered configurations.
   */
  private List createConfigurations() {
    List configNames = new ArrayList();

    // Get the factory to register custom styles/configurations.
    GenericNodeRealizer.Factory factory = GenericNodeRealizer.getFactory();

    // Add the simple rectangle configuration to the factory.
    String configName = "Simple Rectangle";
    factory.addConfiguration(configName, createSimpleRectangleConfiguration(factory));
    configNames.add(configName);

    // Add the diamond configuration to the factory.
    configName = "Diamond";
    factory.addConfiguration(configName, createDiamondConfiguration(factory));
    configNames.add(configName);

    // Add the elliptical configuration to the factory.
    configName = "Ellipse";
    factory.addConfiguration(configName, createEllipseConfiguration(factory));
    configNames.add(configName);

    configName = "Circle";
    factory.addConfiguration(configName, createCircleConfiguration(factory));
    configNames.add(configName);

    // Add the bevel style configuration to the factory.
    configName = "Bevel";
    factory.addConfiguration(configName, createBevelNodeConfiguration(factory));
    configNames.add(configName);

    // Add the shiny plate configuration to the factory.
    configName = "Shiny Plate";
    factory.addConfiguration(configName, createShinyPlateNodeConfiguration(factory));
    configNames.add(configName);

    // Add the rounded rectangle configuration to the factory.
    Map roundRectConfiguration = createRoundRectConfiguration(factory);
    configName = "Round Rectangle";
    factory.addConfiguration(configName, roundRectConfiguration);
    configNames.add(configName);

    configName = "Note";
    factory.addConfiguration(configName, createNoteNodeConfiguration(factory));
    configNames.add(configName);

    //Add the butterfly configuration to the factory by reusing the round rect configuration and only overriding the painter
    configName = "Butterfly";
    factory.addConfiguration(configName, createButterflyConfiguration(roundRectConfiguration));
    configNames.add(configName);

    // Add the flat button style configuration to the factory.
    configName = "Flat Button";
    factory.addConfiguration(configName, createFlatButtonConfiguration(factory));
    configNames.add(configName);

    // Add the floating style configuration to the factory.
    configName = "Floating";
    factory.addConfiguration(configName, createFloatingConfiguration(factory));
    configNames.add(configName);

    // Add the image style configuration to the factory.
    configName = "Raster Graphics";
    factory.addConfiguration(configName, createRasterGraphicsConfiguration(factory));
    configNames.add(configName);

    /*
    Note: Since SVGPainter is not part of the yFiles distribution, but part of the free yFiles extension ySVG this code
    is commented out by default. For this code to work, ySVG must be included in the classpath.
    For more information on ySVG have a look at: http://www.yworks.com/ysvg
    */
    // Add the vector graphics style configuration to the factory.
    configName = "Vector Graphics";
    Map vectorGraphicsConfig = createVectorGraphicsConfiguration(factory);
    //todo: uncomment this to use svg images. Note: the ySVG package is needed for this to work
//    factory.addConfiguration(configName, vectorGraphicsConfig);
//    configNames.add(configName);

    // Add the a decorated rect style configuration to the factory.
    configName = "Decorated Rect";
    factory.addConfiguration(configName, createDecoratedRectPainterConfiguration(factory));
    configNames.add(configName);

    return configNames;
  }

  private List createRealizers(List configurations) {
    List realizers = new ArrayList(configurations.size());
    for (Iterator iterator = configurations.iterator(); iterator.hasNext();) {
      String configName = String.valueOf(iterator.next());
      GenericNodeRealizer nr = new GenericNodeRealizer(configName);
      nr.setLabelText(configName);
      nr.setWidth(120);
      nr.setFillColor(Color.ORANGE);

      //make some custom configurations for some realizers
      if ("Simple Rectangle".equals(configName)) {
      } else if ("Diamond".equals(configName)) {
      } else if ("Ellipse".equals(configName)) {
        nr.setLineColor(Color.ORANGE);
      } else if ("Bevel".equals(configName)) {
        nr.setLineColor(Color.ORANGE);
      } else if ("Shiny Plate".equals(configName)) {
      } else if ("Round Rectangle".equals(configName)) {
      } else if ("Butterfly".equals(configName)) {
      } else if ("Flat Button".equals(configName)) {
      } else if ("Floating".equals(configName)) {
      } else if ("Raster Graphics".equals(configName)) {
        nr.setLabelText("");
      } else if ("Vector Graphics".equals(configName)) {
        nr.setLabelText("");
      } else if ("Decorated Rect".equals(configName)) {
        nr.getLabel().setPosition(NodeLabel.LEFT);
      }

      realizers.add(nr);
    }

    return realizers;
  }

  /**
   * Will use the default configuration map from GenericNodeRealizer and set an own painter implementation. The painter
   * will be a ShapeNodePainter that paints a rectangle.
   *
   * No GenericNodeRealizer.ContainsTest is set explicitly so that hit testing is done using the default NodeRealizer's
   * hit test.
   */
  private Map createSimpleRectangleConfiguration(GenericNodeRealizer.Factory factory) {
    // Retrieve a map that holds the default GenericNodeRealizer configuration.
    // The implementations contained therein can be replaced one by one in order
    // to create custom configurations...
    Map implementationsMap = factory.createDefaultConfigurationMap();

    ShapeNodePainter painter = new ShapeNodePainter(ShapeNodePainter.RECT);
    implementationsMap.put(GenericNodeRealizer.Painter.class, painter);

    return implementationsMap;
  }

  /**
   * Will use the default configuration map from GenericNodeRealizer and set an own painter implementation. The painter
   * will be a ShapeNodePainter that paints a diamond.
   *
   * Since ShapeNodePainter does also implement GenericNodeRealizer.ContainsTest, it is also set as the contains test.
   * Thus, hit tests are not performed on the rectangular bounding box of the node, but really on the drawn shape.
   */
  private Map createDiamondConfiguration(GenericNodeRealizer.Factory factory) {
    Map implementationsMap = factory.createDefaultConfigurationMap();

    ShapeNodePainter painter = new ShapeNodePainter(ShapeNodePainter.DIAMOND);
    implementationsMap.put(GenericNodeRealizer.Painter.class, painter);

    implementationsMap.put(GenericNodeRealizer.ContainsTest.class, painter);
    return implementationsMap;
  }

  /**
   * Will use the default configuration map from GenericNodeRealizer and set an own painter implementation. The painter
   * will be a ShapeNodePainter that paints an ellipse. Also this painter is wrapped with a shadow painter that draws a
   * nice drop shadow.
   *
   * Since ShapeNodePainter does also implement GenericNodeRealizer.ContainsTest, it is also set as the contains test.
   * Thus, hit tests are not performed on the rectangular bounding box of the node, but really on the drawn shape.
   *
   * Finally a custom GenericNodeRealizer.HotSpotPainter is set, that is responsible for drawing the resize knobs and
   * and register hits on them.
   */
  private Map createEllipseConfiguration(GenericNodeRealizer.Factory factory) {
    Map implementationsMap = factory.createDefaultConfigurationMap();

    ShapeNodePainter painter = new ShapeNodePainter(ShapeNodePainter.ELLIPSE);
    GenericNodeRealizer.Painter wrappedPainter = new ShadowNodePainter(painter);
    implementationsMap.put(GenericNodeRealizer.Painter.class, wrappedPainter);
    implementationsMap.put(GenericNodeRealizer.ContainsTest.class, painter);

    // The node has four resize knobs, one at each of the node's corners. Both painting
    // and hit-testing is done by this custom hot spot painter.
    CustomHotSpotPainter chsp = new CustomHotSpotPainter(165, new Ellipse2D.Double(), null);
    implementationsMap.put(GenericNodeRealizer.HotSpotPainter.class, chsp);
    implementationsMap.put(GenericNodeRealizer.HotSpotHitTest.class, chsp);

    return implementationsMap;
  }

  private Map createCircleConfiguration(GenericNodeRealizer.Factory factory) {
    Map implementationsMap = factory.createDefaultConfigurationMap();

    CircleNodePainter painter = new CircleNodePainter();
    GenericNodeRealizer.Painter wrappedPainter = new ShadowNodePainter(painter);
    implementationsMap.put(GenericNodeRealizer.Painter.class, wrappedPainter);
    implementationsMap.put(GenericNodeRealizer.ContainsTest.class, painter);

    return implementationsMap;
  }

  /**
   * Will use the default configuration map from GenericNodeRealizer and set an own painter implementation. The painter
   * will be a {@link y.view.BevelNodePainter} that paints a node in a bevel like style.
   *
   * Since BevelNodePainter does also implement GenericNodeRealizer.ContainsTest, it is also set as the contains test.
   * Thus, hit tests are not performed on the rectangular bounding box of the node, but really on the drawn shape.
   *
   * Finally a special GenericNodeRealizer.UserDataHandler is set, so that serialization/deserialization of user-defined
   * data is taken care of.
   */
  private Map createBevelNodeConfiguration(GenericNodeRealizer.Factory factory) {
    Map implementationsMap = factory.createDefaultConfigurationMap();

    BevelNodePainter painter = new BevelNodePainter();
    //BevelNodePainter has an own option to draw a drop shadow that is more efficient than wrapping it with
    // {@link y.view.ShadowNodePainter}
    painter.setDrawShadow(true);
    implementationsMap.put(GenericNodeRealizer.Painter.class, painter);
    implementationsMap.put(GenericNodeRealizer.ContainsTest.class, painter);

    // User-defined data objects that implement both the Cloneable and Serializable
    // interfaces are taken care of (when serializing/deserializing the realizer).
    implementationsMap.put(GenericNodeRealizer.UserDataHandler.class,
        new SimpleUserDataHandler(SimpleUserDataHandler.REFERENCE_ON_FAILURE));

    return implementationsMap;
  }

  /**
   * Will use the default configuration map from GenericNodeRealizer and set an own painter implementation. The painter
   * will be a {@link y.view.ShinyPlateNodePainter} that paints a node like a shiny plate.
   *
   * Since ShinyPlateNodePainter does also implement GenericNodeRealizer.ContainsTest, it is also set as the contains
   * test. Thus, hit tests are not performed on the rectangular bounding box of the node, but really on the drawn
   * shape.
   *
   * Finally an {@link y.view.GenericNodeRealizer.GenericSizeConstraintProvider} is added to determine the minimum and
   * maximum bounds of the node.
   */
  private Map createShinyPlateNodeConfiguration(GenericNodeRealizer.Factory factory) {
    Map implementationsMap = factory.createDefaultConfigurationMap();

    ShinyPlateNodePainter painter = new ShinyPlateNodePainter();
    //ShinyPlateNodePainter has an own option to draw a drop shadow that is more efficient than wrapping it with
    // {@link y.view.ShadowNodePainter}
    painter.setDrawShadow(true);
    implementationsMap.put(GenericNodeRealizer.Painter.class, painter);
    implementationsMap.put(GenericNodeRealizer.ContainsTest.class, painter);

    GenericNodeRealizer.GenericSizeConstraintProvider scp = new GenericNodeRealizer.GenericSizeConstraintProvider() {
      public YDimension getMinimumSize(NodeRealizer context) {
        return new YDimension(15, 15);
      }

      public YDimension getMaximumSize(NodeRealizer context) {
        return new YDimension(250, 100);
      }
    };
    implementationsMap.put(GenericNodeRealizer.GenericSizeConstraintProvider.class, scp);

    return implementationsMap;
  }

  /**
   * Will use the default configuration map from GenericNodeRealizer and set an own painter implementation. The painter
   * will be an implementation of an own class {@link RectangularShapePainter} that paints a node with a given
   * rectangular shape. This painter is wrapped with {@link y.view.ShadowNodePainter} so that a drop shadow will be
   * painted.
   *
   * Since RectangularShapePainter does also implement GenericNodeRealizer.ContainsTest, it is also set as the contains
   * test. Thus, hit tests are not performed on the rectangular bounding box of the node, but really on the drawn
   * shape.
   *
   * A custom HotSpotPainter is set as well as an own UserDataHandler.
   */
  private Map createRoundRectConfiguration(GenericNodeRealizer.Factory factory) {
    Map implementationsMap = factory.createDefaultConfigurationMap();

    RectangularShapePainter painter = new RectangularShapePainter(new RoundRectangle2D.Double(50, 50, 50, 50, 15, 15));
    implementationsMap.put(GenericNodeRealizer.Painter.class, new ShadowNodePainter(painter));
    implementationsMap.put(GenericNodeRealizer.ContainsTest.class, painter);

    // User-defined data objects that implement both the Cloneable and Serializable
    // interfaces are taken care of (when serializing/deserializing the realizer).
    implementationsMap.put(GenericNodeRealizer.UserDataHandler.class,
        new SimpleUserDataHandler(SimpleUserDataHandler.REFERENCE_ON_FAILURE));

    // The node has the maximum of eight resize knobs, one at each of the node's
    // corners and also one at the middle of each side.
    CustomHotSpotPainter chsp = new CustomHotSpotPainter(255, new Ellipse2D.Double(), Color.red);
    implementationsMap.put(GenericNodeRealizer.HotSpotPainter.class, chsp);
    implementationsMap.put(GenericNodeRealizer.HotSpotHitTest.class, chsp);

    return implementationsMap;
  }

  private Map createNoteNodeConfiguration(GenericNodeRealizer.Factory factory) {
    Map implementationsMap = factory.createDefaultConfigurationMap();

    final NoteNodePainter painter = new NoteNodePainter();
    implementationsMap.put(GenericNodeRealizer.Painter.class, new ShadowNodePainter(painter));
    implementationsMap.put(GenericNodeRealizer.ContainsTest.class, painter);

    return implementationsMap;
  }

  /**
   * Will use the given implementationMap and simply set another Painter implementation. The painter will be a
   * y.view.GeneralPathNodePainter, which takes any GeneralPath and paints it as a node shape. This painter is wrapped
   * with {@link y.view.ShadowNodePainter} so that a drop shadow will be painted.
   *
   * Since GeneralPathNodePainter does also implement GenericNodeRealizer.ContainsTest, it is also set as the contains
   * test. Thus, hit tests are not performed on the rectangular bounding box of the node, but really on the drawn
   * shape.
   *
   * Note: all other settings of the given configuration like for example the HotSpotPainter and HotSpotHitTest will
   * remain untouched.
   */
  private Map createButterflyConfiguration(Map implementationsMap) {
    //create the general path of the butterfly
    GeneralPath gp = new GeneralPath();
    gp.moveTo(1.0f, 0.5f);
    gp.lineTo(0.0f, 1.0f);
    gp.quadTo(0.0f, 0.5f, 0.3f, 0.5f);
    gp.quadTo(0.0f, 0.5f, 0.0f, 0.0f);
    gp.closePath();

    GeneralPathNodePainter painter = new GeneralPathNodePainter(gp);
    implementationsMap.put(GenericNodeRealizer.Painter.class, new ShadowNodePainter(painter));
    implementationsMap.put(GenericNodeRealizer.ContainsTest.class, painter);

    return implementationsMap;
  }

  /**
   * Will use the default configuration map from GenericNodeRealizer and set an own custom painter implementation of
   * type {@link FlatButtonPainter}.
   */
  private Map createFlatButtonConfiguration(GenericNodeRealizer.Factory factory) {
    Map implementationsMap = factory.createDefaultConfigurationMap();

    FlatButtonPainter painter = new FlatButtonPainter();
    implementationsMap.put(GenericNodeRealizer.Painter.class, painter);

    return implementationsMap;
  }

  /**
   * Will use the default configuration map from GenericNodeRealizer and set an own custom painter implementation of
   * type {@link FloatingPainter}.This painter is wrapped with {@link y.view.ShadowNodePainter} so that a drop shadow
   * will be painted.
   *
   * Since this painter also implements GenericNodeRealizer.ContainsTest it is also set accordingly.
   */
  private Map createFloatingConfiguration(GenericNodeRealizer.Factory factory) {
    Map implementationsMap = factory.createDefaultConfigurationMap();

    FloatingPainter painter = new FloatingPainter();
    implementationsMap.put(GenericNodeRealizer.Painter.class, new ShadowNodePainter(painter));
    implementationsMap.put(GenericNodeRealizer.ContainsTest.class, painter);

    return implementationsMap;
  }

  /**
   * Will use the default configuration map from GenericNodeRealizer and set an own painter implementation. The painter
   * will be an {@link y.view.ImageNodePainter} that paints a node according to a given raster graphics image. This
   * painter is wrapped with {@link y.view.ShadowNodePainter} so that a drop shadow will be painted.
   */
  private Map createRasterGraphicsConfiguration(GenericNodeRealizer.Factory factory) {
    Map implementationsMap = factory.createDefaultConfigurationMap();
    ImageNodePainter painter = new ImageNodePainter(getClass().getResource("resource/yWorksNode.png"));
    implementationsMap.put(GenericNodeRealizer.Painter.class, new ShadowNodePainter(painter));
    return implementationsMap;
  }

  /**
   * Will use the default configuration map from GenericNodeRealizer and set an own painter implementation. The painter
   * will be a yext.svg.view.SVGPainter that paints a node according to a given vector graphics image. This painter is
   * wrapped with {@link y.view.ShadowNodePainter} so that a drop shadow will be painted.
   *
   * Note: Since SVGPainter is not part of the yFiles distribution, but part of the free yFiles extension ySVG this code
   * is commented out by default. For this code to work, ySVG must be included in the classpath. For more information on
   * ySVG have a look at: http://www.yworks.com/ysvg
   */
  private Map createVectorGraphicsConfiguration(GenericNodeRealizer.Factory factory) {
    Map implementationsMap = factory.createDefaultConfigurationMap();
    //todo: uncomment this to use svg images. Note: the ySVG package is needed for this to work
//    URL resource = getClass().getResource("resource/yWorksNode.svg");
//    SVGPainter painter = new SVGPainter(resource);
//    implementationsMap.put(GenericNodeRealizer.Painter.class, new ShadowNodePainter(painter));
    return implementationsMap;
  }

  /**
   * Creates a configuration where a painter will be used that decorates another painter with an icon.
   */
  private Map createDecoratedRectPainterConfiguration(GenericNodeRealizer.Factory factory) {
    Map implementationsMap = factory.createDefaultConfigurationMap();
    IconDecoratorPainter painter = new IconDecoratorPainter(new ShapeNodePainter());
    implementationsMap.put(GenericNodeRealizer.Painter.class, new ShadowNodePainter(painter));
    return implementationsMap;
  }

  protected EditMode createEditMode() {
    EditMode editMode = new EditMode();
    editMode.assignNodeLabel(false);
    editMode.showNodeTips(true);
    return editMode;
  }
  
  /** Launcher method. Execute this class to see sample instantiations of {@link GenericNodeRealizer} in action. */
  public static void main(String[] args) {
    EventQueue.invokeLater(new Runnable() {
      public void run() {
        initLnF();
        (new GenericNodeRealizerDemo()).start("GenericNodeRealizer Demo");
      }
    });
  }

  /**
   * A custom HotSpotPainter implementation that uses the given shape and color to paint the resize knobs, a.k.a. hot
   * spots. If the given color is <code>null</code>, then the node's fill color is used instead. <p> Note that his
   * painter also provides support for hit-testing the resize knobs.
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

  /** A custom Painter and ContainsTest implementation that can be used with any kind of <code>RectangularShape</code>. */
  public static final class RectangularShapePainter extends AbstractCustomNodePainter implements GenericNodeRealizer.ContainsTest {
    private RectangularShape shape;

    public RectangularShapePainter(RectangularShape shape) {
      this.shape = shape;
    }

    /** Overrides the default fill color. */
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
    }

    public boolean contains(NodeRealizer context, double x, double y) {
      shape.setFrame(context.getX(), context.getY(), context.getWidth(), context.getHeight());
      return shape.contains(x, y);
    }
  }

  /**
   * A custom GenericNodeRealizer.Painter implementation that will paint a node as a round rectangle surrounded with a
   * small border.
   *
   * Also implements GenericNodeRealizer.ContainsTest. The test will mark coordinates as contained if they are
   * <b>inside</b> the inner round rectangle. Thus for example edges will also be clipped there.
   *
   * This implementation also demonstrates how to use the {@link y.view.GenericNodeRealizer#getStyleProperty(String)}
   * for retrieving instance specific state that cannot be determined from the
   * {@link y.view.GenericNodeRealizer#getUserData() user data}.
   */
  public static class FloatingPainter extends AbstractCustomNodePainter implements GenericNodeRealizer.ContainsTest {
    private final RoundRectangle2D innerShape;
    private final RoundRectangle2D outerShape;
    private RoundRectangle2D measureRect;
    private double radius = 8;

    public FloatingPainter() {
      this.innerShape = new RoundRectangle2D.Double(0, 0, -1, -1, radius, radius);
      this.outerShape = new RoundRectangle2D.Double(0, 0, -1, -1, radius, radius);
    }

    protected void paintNode(NodeRealizer context, Graphics2D graphics, boolean sloppy) {
      double inset = getInset(context);
      innerShape.setFrame(context.getX() + inset, context.getY() + inset, context.getWidth() - 2 * inset,
          context.getHeight() - 2 * inset);
      if (initializeFill(context, graphics)) {
        graphics.fill(innerShape);
      }
      outerShape.setFrame(context.getX(), context.getY(), context.getWidth(), context.getHeight());
      if (initializeLine(context, graphics)) {
        graphics.draw(outerShape);
      }
    }

    /**
     * Callback method that retrieves the inset to use for the given context.
     * This will use the {@link y.view.GenericNodeRealizer#getStyleProperty(String)}
     * method to query a {@link Number} for its {@link Number#doubleValue()} as
     * the inset.
     * @param context The node realizer to obtain the inset for.
     * @return The value of the "FloatingPainter.Inset" property as a double or 4.0d
     * if none has been defined for the instance.
     */
    protected double getInset(NodeRealizer context) {
      Object o = ((GenericNodeRealizer)context).getStyleProperty("FloatingPainter.Inset");
      if (o instanceof Number){
        return ((Number)o).doubleValue();
      } else {
        return 4;
      }
    }

    protected Paint getLinePaint(final NodeRealizer context, final boolean selected) {
      return getFillPaint(context, selected);
    }

    public boolean contains(NodeRealizer context, double x, double y) {
      if (null == measureRect) {
        measureRect = new RoundRectangle2D.Double();
      }
      double inset = getInset(context);
      measureRect.setRoundRect(context.getX() + inset, context.getY() + inset, context.getWidth() - 2 * inset,
          context.getHeight() - 2 * inset, radius, radius);
      return measureRect.contains(x, y);
    }
  }

  /** A custom GenericNodeRealizer.Painter implementation that paints a node in a flat button like style. */
  static class FlatButtonPainter extends AbstractCustomNodePainter {
    protected void paintNode(NodeRealizer context, Graphics2D g, boolean sloppy) {
      double x = context.getX();
      double y = context.getY();
      double w = context.getWidth();
      double h = context.getHeight();

      Shape shape = new RoundRectangle2D.Double(x, y, w, h, 10, 10);
      Color c1 = context.getFillColor();
      paintBorder(g, c1, (float) 2, shape);
      paintContent(g, shape, c1, 0, x, y, w, h);
    }

    private void paintBorder(Graphics2D g, Color c1, float thick, Shape shape) {
      float ratio = 0.75f;
      g.setColor(mixColors(new Color(128, 128, 128, 64), c1, ratio));
      g.setStroke(new BasicStroke(thick, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
      g.draw(shape);

      g.setColor(mixColors(new Color(255, 255, 255, 196), c1, ratio));
      g.setStroke(new BasicStroke(thick / 2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
      g.translate(thick / 4, thick / 4);
      g.draw(shape);

      g.setColor(mixColors(new Color(0, 0, 0, 64), c1, ratio));
      g.setStroke(new BasicStroke(thick / 2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
      g.translate(-thick / 2, -thick / 2);
      g.draw(shape);
      g.translate(thick / 4, thick / 4);
    }

    private void paintContent(Graphics2D g, Shape shape, Color c1, int thick, double x, double y, double w, double h) {
      Color c2 = Color.WHITE;
      Color c3 = mixColors(c1, c2, 0.5f);
      BasicStroke stroke = new BasicStroke(thick, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
      g.setStroke(stroke);
      g.setPaint(new GradientPaint((float) x, (float) y - thick - 1, c3, (float) x,
          (float) (y + h + thick + 1), c1));

      Shape strokedShape = stroke.createStrokedShape(shape);
      Area area = new Area(strokedShape);
      area.add(new Area(shape));

      g.fill(area);

      Shape oldClip = g.getClip();

      g.clip(area);
      g.clip(new Ellipse2D.Double(x - w, y - h * 0.5, w * 3, h * 0.75));

      g.setPaint(new GradientPaint((float) x, (float) (y - 5), new Color(1.0f, 1.0f, 1.0f, 0.3f), (float) x,
          (float) (y + h), new Color(1.0f, 1.0f, 1.0f, 0.0f)));

      g.fill(
          new Rectangle2D.Double(x - thick - thick, y - thick, w + thick + thick + thick + thick, h + thick + thick));

      g.setClip(oldClip);
    }

    private Color mixColors(Color c1, Color c2, float ratio) {
      float b = 1 - ratio;
      return new Color((c1.getRed() * ratio + c2.getRed() * b) / 255f,
          (c1.getGreen() * ratio + c2.getGreen() * b) / 255f,
          (c1.getBlue() * ratio + c2.getGreen() * b) / 255f, (c1.getAlpha() * ratio + c2.getAlpha() * b) / 255f);
    }
  }

  static class IconDecoratorPainter implements GenericNodeRealizer.Painter {
    private final GenericNodeRealizer.Painter innerPainter;
    private Icon icon;

    public IconDecoratorPainter(GenericNodeRealizer.Painter innerPainter) {
      this.innerPainter = innerPainter;
      icon = createIcon();
    }

    public void paint( final NodeRealizer context, final Graphics2D graphics ) {
      innerPainter.paint(context, graphics);
      paintIcon(context, graphics);
    }

    public void paintSloppy( final NodeRealizer context, final Graphics2D graphics ) {
      innerPainter.paintSloppy(context, graphics);
      paintIcon(context, graphics);
    }

    private void paintIcon( final NodeRealizer context, final Graphics2D graphics ) {
      int x = (int) (context.getX() + context.getWidth() - icon.getIconWidth() - 2);
      int y = (int) (context.getY() + 2);
      icon.paintIcon(null, graphics, x, y);
    }

    protected Icon createIcon() {
      URL imageURL = ClassLoader.getSystemResource("demo/view/resource/yicon.png");
      final ImageIcon imageIcon = new ImageIcon(imageURL);
      final double zoom = 16.0d / Math.min(imageIcon.getIconHeight(), imageIcon.getIconWidth());

      return new Icon() {
        public void paintIcon(Component c, Graphics g, int x, int y) {
          g.drawImage(imageIcon.getImage(), x, y, getIconWidth(), getIconHeight(), null);
        }

        public int getIconWidth() {
          return (int) (imageIcon.getIconWidth() * zoom);
        }

        public int getIconHeight() {
          return (int) (imageIcon.getIconHeight() * zoom);
        }
      };
    }
  }

  /**
   * A custom GenericNodeRealizer.Painter implementation that paints a node as
   * a circle (independent of the actual aspect ratio of node's bounding box).
   */
  static class CircleNodePainter
          extends AbstractCustomNodePainter
          implements GenericNodeRealizer.ContainsTest {
    private final Ellipse2D.Double circle = new Ellipse2D.Double();

    protected void paintNode(
            final NodeRealizer context,
            final Graphics2D graphics,
            final boolean sloppy
    ) {
      final double w = context.getWidth();
      final double h = context.getHeight();
      final double d = Math.min(w, h);
      circle.setFrame(
              context.getX() + (w - d) * 0.5,
              context.getY() + (h - d) * 0.5, d, d);

      final boolean useSelectionStyle = useSelectionSyle(context, graphics);
      final Color fc = getFillColor(context, useSelectionStyle);
      if (fc != null) {
        graphics.setColor(fc);
        graphics.fill(circle);
      }

      final Color lc = getLineColor(context, useSelectionStyle);
      final Stroke ls = getLineStroke(context, useSelectionStyle);
      if (lc != null && ls != null) {
        graphics.setColor(lc);
        graphics.setStroke(ls);
        graphics.draw(circle);
      }
    }

    public boolean contains(
            final NodeRealizer context,
            final double x,
            final double y
    ) {
      final double w = context.getWidth();
      final double h = context.getHeight();
      final double tx = context.getX() + w * 0.5 - x;
      final double ty = context.getY() + h * 0.5 - y;
      return Math.sqrt(tx*tx + ty*ty) <= Math.min(w, h) * 0.5;
    }

    private static boolean useSelectionSyle(
            final NodeRealizer context,
            final Graphics2D graphics
    ) {
      return context.isSelected() &&
             YRenderingHints.isSelectionPaintingEnabled(graphics);
    }
  }

  /**
   * A custom GenericNodeRealizer.Painter implementation that paints a node as
   * a dog-eared note similar to the notes that are used in UML diagrams.
   */
  static class NoteNodePainter
          extends AbstractCustomNodePainter
          implements GenericNodeRealizer.ContainsTest {
    private static final double DOG_EAR_SIZE = 15;

    private final GeneralPath shape;
    private final Rectangle2D.Double fallback;

    private Color dogEarColor;

    public NoteNodePainter() {
      shape = new GeneralPath();
      fallback = new Rectangle2D.Double();
      dogEarColor = Color.LIGHT_GRAY;
    }

    public Color getDogEarColor() {
      return dogEarColor;
    }

    public void setDogEarColor( final Color dogEarColor ) {
      this.dogEarColor = dogEarColor;
    }

    protected void paintNode(
            final NodeRealizer context,
            final Graphics2D graphics,
            final boolean sloppy
    ) {
      final boolean useSelectionStyle = useSelectionSyle(context, graphics);

      final double w = context.getWidth();
      final double h = context.getHeight();
      if (w < DOG_EAR_SIZE + 5 && h < DOG_EAR_SIZE + 5) {
        // node is "too small" for a dog ear - paint a simple rectangle instead
        fallback.setFrame(context.getX(), context.getY(), w, h);
        final Paint fp = getFillPaint(context, useSelectionStyle);
        if (fp != null) {
          graphics.setPaint(fp);
          graphics.fill(fallback);
        }

        final Color lc = getLineColor(context, useSelectionStyle);
        final Stroke ls = getLineStroke(context, useSelectionStyle);
        if (lc != null && ls != null) {
          graphics.setColor(lc);
          graphics.setStroke(ls);
          graphics.draw(fallback);
        }
      } else {
        // start with the basic shape of the node
        shape.reset();
        final double x = context.getX();
        final double y = context.getY();
        final double maxX = x + w;
        final double maxY = y + h;

        shape.moveTo((float) x, (float) y);
        shape.lineTo((float) (maxX - DOG_EAR_SIZE), (float) y);
        shape.lineTo((float) maxX, (float) (y + DOG_EAR_SIZE));
        shape.lineTo((float) maxX, (float) maxY);
        shape.lineTo((float) x, (float) maxY);
        shape.closePath();

        // fill the shape's interior
        final Paint fp = getFillPaint(context, useSelectionStyle);
        if (fp != null) {
          graphics.setPaint(fp);
          graphics.fill(shape);
        }

        // draw the shape border
        final Color lc = getLineColor(context, useSelectionStyle);
        final Stroke ls = getLineStroke(context, useSelectionStyle);
        if (lc != null && ls != null) {
          graphics.setColor(lc);
          graphics.setStroke(ls);
          graphics.draw(shape);
        }

        // now paint the dog ear interior
        final Color dogEarColor = getDogEarColor(context);
        if (dogEarColor != null) {
          shape.reset();
          shape.moveTo((float) (maxX - DOG_EAR_SIZE), (float) y);
          shape.lineTo((float) (maxX - DOG_EAR_SIZE), (float) (y + DOG_EAR_SIZE));
          shape.lineTo((float) maxX, (float) (y + DOG_EAR_SIZE));
          shape.closePath();

          graphics.setColor(dogEarColor);
          graphics.fill(shape);
        }

        // and finally draw the dog ear border
        if (lc != null && ls != null) {
          shape.reset();
          shape.moveTo((float) (maxX - DOG_EAR_SIZE), (float) y);
          shape.lineTo((float) (maxX - DOG_EAR_SIZE), (float) (y + DOG_EAR_SIZE));
          shape.lineTo((float) maxX, (float) (y + DOG_EAR_SIZE));

          graphics.setColor(lc);
          graphics.setStroke(ls);
          graphics.draw(shape);
        }
      }
    }

    private Color getDogEarColor( final NodeRealizer context ) {
      if (context instanceof GenericNodeRealizer) {
        final Object property =
                ((GenericNodeRealizer) context).getStyleProperty("dogEarColor");
        if (property instanceof Color) {
          return (Color) property;
        }
      }

      return getDogEarColor();
    }

    public boolean contains(
            final NodeRealizer context,
            final double tx,
            final double ty
    ) {
      final double w = context.getWidth();
       final double h = context.getHeight();
       if (w < DOG_EAR_SIZE + 5 && h < DOG_EAR_SIZE + 5) {
         fallback.setFrame(context.getX(), context.getY(), w, h);
         return fallback.contains(tx, ty);
       } else {
         shape.reset();
         final double x = context.getX();
         final double y = context.getY();
         shape.moveTo((float) x, (float) y);
         shape.lineTo((float) (x + w - DOG_EAR_SIZE), (float) y);
         shape.lineTo((float) (x + w), (float) (y + DOG_EAR_SIZE));
         shape.lineTo((float) (x + w), (float) (y + h));
         shape.lineTo((float) x, (float) (y + h));
         shape.closePath();

         return shape.contains(tx, ty);
       }
    }

    private static boolean useSelectionSyle(
            final NodeRealizer context,
            final Graphics2D graphics
    ) {
      return context.isSelected() &&
             YRenderingHints.isSelectionPaintingEnabled(graphics);
    }
  }
}