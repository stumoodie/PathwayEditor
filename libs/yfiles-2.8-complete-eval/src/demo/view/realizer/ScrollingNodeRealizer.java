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

import y.view.AbstractMouseInputEditor;
import y.view.EditMode;
import y.view.Graph2DView;
import y.view.HitInfo;
import y.view.Mouse2DEvent;
import y.view.MouseInputEditor;
import y.view.NodeRealizer;
import y.view.MouseInputEditorProvider;

import javax.swing.JFrame;
import javax.swing.JRootPane;

import demo.view.DemoDefaults;
import y.view.YRenderingHints;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Graphics2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;

/**
 * Demonstrates how scrollbars inside a NodeRealizer can be implemented.
 */
public class ScrollingNodeRealizer extends NodeRealizer implements MouseInputEditorProvider {

  private double viewWidth = 300;
  private double viewHeight = 300;
  private ScrollBar verticalScrollBar;
  private ScrollBar horizontalScrollBar;

  public ScrollingNodeRealizer() {
    super();
    init();
  }

  public ScrollingNodeRealizer(NodeRealizer nr) {
    super(nr);
    init();
  }

  private void init() {
    this.verticalScrollBar = new ScrollBar(true, 100, this);
    this.horizontalScrollBar = new ScrollBar(false, 100, this);
    boundsChanged();
  }

  protected void boundsChanged() {
    super.boundsChanged();
    this.horizontalScrollBar.setBounds(
        new Rectangle2D.Double(0, getViewPortHeight(), getViewPortWidth(), getHeight() - getViewPortHeight() - 1));
    this.verticalScrollBar.setBounds(
        new Rectangle2D.Double(getViewPortWidth(), 0, getWidth() - getViewPortWidth() - 1, getViewPortHeight()));
    this.horizontalScrollBar.setMaxPosition(Math.max(0, getViewWidth() - getViewPortWidth()));
    this.horizontalScrollBar.setExtent(Math.max(10, getViewPortWidth() * getViewPortWidth() / getViewWidth()));
    this.verticalScrollBar.setMaxPosition(Math.max(0, getViewHeight() - getViewPortHeight()));
    this.verticalScrollBar.setExtent(Math.max(10, getViewPortHeight() * getViewPortHeight() / getViewHeight()));
  }

  public NodeRealizer createCopy(NodeRealizer nr) {
    return new ScrollingNodeRealizer(nr);
  }

  public Rectangle2D getViewPortBounds() {
    return new Rectangle2D.Double(0, 0, getViewPortWidth(), getViewPortHeight());
  }

  public double getViewPortWidth() {
    return getWidth() - 10;
  }

  public double getViewPortHeight() {
    return getHeight() - 10;
  }

  public double getViewWidth() {
    return viewWidth;
  }

  public double getViewHeight() {
    return viewHeight;
  }

  protected void paintNode(Graphics2D g) {
    Rectangle2D.Double rect = new Rectangle2D.Double(x, y, width, height);
    g.setColor(getFillColor());
    g.fill(rect);
    if (isSelected() && YRenderingHints.isSelectionPaintingEnabled(g)) {
      paintHotSpots(g);
    }
    g = (Graphics2D) g.create();
    g.translate(x, y);
    horizontalScrollBar.paint(g);
    verticalScrollBar.paint(g);
    rect.setFrame(getViewPortBounds());
    g.clip(rect);
    g.translate(rect.x - horizontalScrollBar.getPosition(), rect.y - verticalScrollBar.getPosition());
    g.setColor(Color.black);
    renderContent(g);
    g.dispose();
  }

  private void renderContent(Graphics2D g) {
    g.setColor(getLineColor());
    for (int i = 0; i < 25; i ++) {
      g.drawString("Test" + i, i, 10 * i);
    }
  }

  /**
   * returns a mouse input editor provider for this realizer. This realizer implements the
   * {@link MouseInputEditorProvider} interface, so this method just returns this realizer.
   * This method is not really necessary, since the implementation in the super class {@link NodeRealizer}
   * would also return this realizer, after checking that this realizer implements the
   * {@link MouseInputEditorProvider} interface. The reimplementation in this class just
   * removes the check. In principle, it would also be possible to return an instance of
   * an external class here if needed.
   * @return this realizer
   * @see #findMouseInputEditor(Graph2DView, double, double, HitInfo)
   */
  public MouseInputEditorProvider getMouseInputEditorProvider() {
    return this;
  }

  public MouseInputEditor findMouseInputEditor(Graph2DView view, double x, double y, HitInfo hitInfo) {
    x -= getX();
    y -= getY();
    MouseInputEditor editor = verticalScrollBar.findEditor(x, y);
    if (editor != null) {
      return editor;
    }
    editor = horizontalScrollBar.findEditor(x, y);
    return editor;
  }

  static final class ScrollBar {
    private double x;
    private double y;
    private double w;
    private double h;
    private double position;
    private double extent;
    private double maxPosition;
    private boolean vertical;
    private NodeRealizer context;
    private ScrollBarInputEditor currentEditor;

    public ScrollBar(boolean vertical, double maxPosition, NodeRealizer context) {
      this.maxPosition = maxPosition;
      this.vertical = vertical;
      this.context = context;
    }

    public Rectangle2D getBounds(Rectangle2D rect) {
      rect.setFrame(x, y, w, h);
      return rect;
    }

    public boolean contains(double x, double y) {
      return x >= this.x && y >= this.y && x < this.x + this.w && y < this.y + this.h;
    }

    public double getExtent() {
      return extent;
    }

    public void setExtent(double extent) {
      this.extent = extent;
    }

    public void setBounds(Rectangle2D bounds) {
      this.x = bounds.getX();
      this.y = bounds.getY();
      this.w = bounds.getWidth();
      this.h = bounds.getHeight();
    }

    public double getPosition() {
      return position;
    }

    public void setPosition(double position) {
      double oldPosition = this.position;
      this.position = Math.max(0, Math.min(position, maxPosition));
      if (oldPosition != this.position) {
        repaint();
      }
    }

    public void setMaxPosition(double maxPosition) {
      if (this.maxPosition != maxPosition) {
        this.maxPosition = maxPosition;
        this.position = Math.min(this.position, maxPosition);
        repaint();
      }
    }

    public double getMaxPosition() {
      return maxPosition;
    }

    public void repaint() {
      context.repaint();
    }

    public void paint(Graphics2D g) {
      if (maxPosition == 0) {
        return;
      }
      Rectangle2D.Double bounds = new Rectangle2D.Double();
      getBounds(bounds);
      g = (Graphics2D) g.create();
      g.setColor(isHighlighted() ? context.getFillColor().brighter() : context.getFillColor());
      g.fill(bounds);
      g.setColor(context.getLineColor());
      g.draw(bounds);
      GeneralPath p = new GeneralPath(GeneralPath.WIND_EVEN_ODD, 5);
      if (vertical) {
        p.moveTo((float) (bounds.getCenterX()), (float) (bounds.getY() + 4));
        p.lineTo((float) (bounds.getCenterX() + 3), (float) (bounds.getY() + 10));
        p.lineTo((float) (bounds.getCenterX() - 3), (float) (bounds.getY() + 10));
        p.closePath();
        g.fill(p);

        p.reset();
        p.moveTo((float) (bounds.getCenterX()), (float) (bounds.getMaxY() - 4));
        p.lineTo((float) (bounds.getCenterX() + 3), (float) (bounds.getMaxY() - 10));
        p.lineTo((float) (bounds.getCenterX() - 3), (float) (bounds.getMaxY() - 10));
        p.closePath();
        g.fill(p);

        getThumbBounds(bounds);
        g.fill(bounds);

        g.dispose();
      } else {
        p.moveTo((float) (bounds.getX() + 4), (float) (bounds.getCenterY()));
        p.lineTo((float) (bounds.getX() + 10), (float) (bounds.getCenterY() + 3));
        p.lineTo((float) (bounds.getX() + 10), (float) (bounds.getCenterY() - 3));
        p.closePath();
        g.fill(p);

        p.reset();
        p.moveTo((float) (bounds.getMaxX() - 4), (float) (bounds.getCenterY()));
        p.lineTo((float) (bounds.getMaxX() - 10), (float) (bounds.getCenterY() + 3));
        p.lineTo((float) (bounds.getMaxX() - 10), (float) (bounds.getCenterY() - 3));
        p.closePath();
        g.fill(p);
        getThumbBounds(bounds);
        g.fill(bounds);
        g.dispose();
      }
    }

    private void getThumbBounds(Rectangle2D bounds) {
      getBounds(bounds);
      if (vertical) {
        bounds.setFrame(bounds.getX() + 1, bounds.getY() + 12, bounds.getWidth() - 2, bounds.getHeight() - 24);
        double p = getPosition() / getMaxPosition();
        bounds.setFrame(bounds.getX(), bounds.getY() + (bounds.getHeight() - getExtent()) * p, bounds.getWidth(),
            getExtent());
      } else {
        bounds.setFrame(bounds.getX() + 12, bounds.getY() + 1, bounds.getWidth() - 24, bounds.getHeight() - 2);
        double p = getPosition() / getMaxPosition();
        bounds.setFrame(bounds.getX() + (bounds.getWidth() - getExtent()) * p, bounds.getY(), getExtent(),
            bounds.getHeight());
      }
    }

    private boolean isHighlighted() {
      return currentEditor != null && currentEditor.isHighlighted();
    }

    public MouseInputEditor findEditor(double x, double y) {
      if (maxPosition == 0) {
        return null;
      }
      Rectangle2D.Double bounds = new Rectangle2D.Double();
      getBounds(bounds);

      if (contains(x, y)) {
        this.currentEditor = new ScrollBarInputEditor();
        return currentEditor;
      }
      return null;
    }

    class ScrollBarInputEditor extends AbstractMouseInputEditor {

      private boolean down;
      private double initialX;
      private double initialY;
      private double initialPos;
      private boolean highlighted;

      public boolean startsEditing(Mouse2DEvent event) {
        double x = event.getX() - context.getX();
        double y = event.getY() - context.getY();
        if (contains(x, y)) {
          highlighted = true;
          repaint();
          return true;
        }
        return false;
      }

      public void mouse2DEventHappened(Mouse2DEvent event) {
        if (context != null) {
          if (event.getId() == Mouse2DEvent.MOUSE_MOVED ||
              event.getId() == Mouse2DEvent.MOUSE_RELEASED) {
            double x = event.getX() - context.getX();
            double y = event.getY() - context.getY();
            if (!contains(x, y)) {
              highlighted = false;
              ScrollBar.this.currentEditor = null;
              repaint();
              stopEditing();
            }
          } else {
            double x = event.getX() - context.getX();
            double y = event.getY() - context.getY();
            switch (event.getId()) {
              case Mouse2DEvent.MOUSE_CLICKED:
                if (vertical) {
                  setPosition(getPosition() + (y > (ScrollBar.this.y + ScrollBar.this.h * 0.5d) ? 10 : -10));
                } else {
                  setPosition(getPosition() + (x > (ScrollBar.this.x + ScrollBar.this.w * 0.5d) ? 10 : -10));
                }
                break;
              case Mouse2DEvent.MOUSE_PRESSED:
                initialX = x;
                initialY = y;
                initialPos = getPosition();
                down = true;
                break;
              case Mouse2DEvent.MOUSE_RELEASED:
                if (down) {
                  double dx = x - initialX;
                  double dy = y - initialY;
                  setPosition(initialPos + (vertical ? dy : dx));
                  down = false;
                }
                break;
              case Mouse2DEvent.MOUSE_DRAGGED:
                if (down) {
                  double dx = x - initialX;
                  double dy = y - initialY;
                  setPosition(initialPos + (vertical ? dy : dx));
                }
                break;
            }
          }
        } else {
          ScrollBar.this.currentEditor = null;
          stopEditing();
        }
      }

      public boolean isHighlighted() {
        return highlighted;
      }
    }
  }



  public static void addContentTo( final JRootPane rootPane )
  {
    final ScrollingNodeRealizer r = new ScrollingNodeRealizer();
    r.setSize(200, 200);
    r.setFillColor(DemoDefaults.DEFAULT_NODE_COLOR);
    
    final EditMode editMode = new EditMode();
    editMode.getMouseInputMode().setNodeSearchingEnabled(true);
    editMode.allowMouseInput(true);

    final Graph2DView view = new Graph2DView();
    view.setFitContentOnResize(true);
    view.getGraph2D().setDefaultNodeRealizer(r.createCopy());
    view.setAntialiasedPainting(true);
    view.getGraph2D().createNode();
    view.addViewMode(editMode);

    rootPane.setContentPane(view);
  }

  /**
   * Launcher method. Execute this class to see a sample instantiation of
   * this node realizer in action.
   */
  public static void main(String[] args)
  {
    EventQueue.invokeLater(new Runnable() {
      public void run() {
        DemoDefaults.initLnF();
        final JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        addContentTo(frame.getRootPane());
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
      }
    });
  }
}
