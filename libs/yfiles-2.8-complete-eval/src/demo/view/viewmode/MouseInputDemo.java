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
package demo.view.viewmode;

import demo.view.DemoBase;
import demo.view.DemoDefaults;

import y.view.AbstractMouseInputEditor;
import y.view.Drawable;
import y.view.EditMode;
import y.view.Graph2DView;
import y.view.HitInfo;
import y.view.Mouse2DEvent;
import y.view.MouseInputEditor;
import y.view.MouseInputEditorProvider;

import javax.swing.Timer;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;

/**
 * This class demonstrates how to add a custom drawable to the view that interacts
 * with {@link y.view.EditMode}'s {@link y.view.MouseInputMode}.
 */
public class MouseInputDemo extends DemoBase {

  public MouseInputDemo() {
    {
      AffineTransform transform = AffineTransform.getTranslateInstance( 45, 45 );
      transform.translate( 0, -40 );
      new ArrowButton( view,
                       ArrowButton.ARROW.createTransformedShape( transform ),
                       new ScrollActionListener( 0, -10 ) );
    }
    {
      AffineTransform transform = AffineTransform.getTranslateInstance( 45, 45 );
      transform.rotate( Math.toRadians( -90 ) );
      transform.translate( 0, -40 );
      new ArrowButton( view,
                       ArrowButton.ARROW.createTransformedShape( transform ),
                       new ScrollActionListener( -10, 0 ) );
    }
    {
      AffineTransform transform = AffineTransform.getTranslateInstance( 45, 45 );
      transform.rotate( Math.toRadians( -180 ) );
      transform.translate( 0, -40 );
      new ArrowButton( view,
                       ArrowButton.ARROW.createTransformedShape( transform ),
                       new ScrollActionListener( 0, 10 ) );
    }
    {
      AffineTransform transform = AffineTransform.getTranslateInstance( 45, 45 );
      transform.rotate( Math.toRadians( 90 ) );
      transform.translate( 0, -40 );
      new ArrowButton( view,
                       ArrowButton.ARROW.createTransformedShape( transform ),
                       new ScrollActionListener( 10, 0 ) );
    }

    loadGraph( "resource/5.graphml" );
    DemoDefaults.applyRealizerDefaults(view.getGraph2D(), true, true);
  }

  protected void registerViewModes() {
    EditMode editMode = new EditMode();
    editMode.getMouseInputMode().setDrawableSearchingEnabled( true );
    editMode.allowMouseInput( true );
    view.addViewMode( editMode );
  }

  static final class ArrowButton implements Drawable, MouseInputEditorProvider {
    private final Graph2DView view;
    private final Shape arrow;
    private final ActionListener action;
    private boolean highlight;

    public static final GeneralPath ARROW;

    static {
      GeneralPath path;
      path = new GeneralPath( GeneralPath.WIND_EVEN_ODD, 8 );
      path.moveTo( 0, 0 );
      path.lineTo( 15, 15 );
      path.lineTo( 5, 15 );
      path.lineTo( 5, 25 );
      path.lineTo( -5, 25 );
      path.lineTo( -5, 15 );
      path.lineTo( -15, 15 );
      path.closePath();
      ARROW = path;
    }


    public ArrowButton( Graph2DView view, Shape arrow, ActionListener action ) {
      this.view = view;
      this.arrow = arrow;
      this.action = action;
      view.addDrawable( this );
    }

    public void paint( Graphics2D g ) {
      double x = view.toWorldCoordX( 0 );
      double y = view.toWorldCoordY( 0 );
      g = ( Graphics2D ) g.create();
      g.translate( x, y );
      double z2 = 1 / view.getZoom();
      g.scale( z2, z2 );
      g.setColor( new Color( 0, 0, 0, 64 ) );
      g.translate( 4, 4 );
      g.fill( arrow );
      g.translate( -4, -4 );
      g.setColor( highlight ? Color.yellow : DemoDefaults.DEFAULT_CONTRAST_COLOR );
      g.fill( arrow );
      g.setColor( Color.black );
      g.draw( arrow );
      g.dispose();
    }

    public Rectangle getBounds() {
      Rectangle bounds = arrow.getBounds();
      double x = view.toWorldCoordX( ( int ) bounds.getCenterX() );
      double y = view.toWorldCoordY( ( int ) bounds.getCenterY() );
      double w2 = ( int ) ( 0.5d * bounds.getWidth() / view.getZoom() );
      double h2 = ( int ) ( 0.5d * bounds.getHeight() / view.getZoom() );
      return new Rectangle( ( int ) ( x - w2 ), ( int ) ( y - h2 ), ( int ) ( 2 * w2 ), ( int ) ( 2 * h2 ) );
    }

    public MouseInputEditor findMouseInputEditor( double x, double y ) {
      int vx = view.toViewCoordX( x );
      int vy = view.toViewCoordY( y );
      if ( arrow.contains( vx, vy ) ) {
        return new AbstractMouseInputEditor() {
          Timer timer;

          {
            timer = new Timer( 30, action );
            timer.setRepeats( true );
            timer.setInitialDelay( 200 );
          }

          public boolean startsEditing( Mouse2DEvent event ) {
            int vx = view.toViewCoordX( event.getX() );
            int vy = view.toViewCoordY( event.getY() );
            return arrow.contains( vx, vy );
          }

          public void mouse2DEventHappened( Mouse2DEvent event ) {
            int vx = view.toViewCoordX( event.getX() );
            int vy = view.toViewCoordY( event.getY() );
            boolean contains = arrow.contains( vx, vy );
            switch ( event.getId() ) {
              case Mouse2DEvent.MOUSE_DRAGGED:
                break;
              case Mouse2DEvent.MOUSE_PRESSED:
                timer.start();
                break;
              case Mouse2DEvent.MOUSE_CLICKED:
                action.actionPerformed( new ActionEvent( ArrowButton.this, ActionEvent.ACTION_PERFORMED, null ) );
                view.updateView();
                // fall through
              case Mouse2DEvent.MOUSE_RELEASED:
                timer.stop();
                // fall through
              case Mouse2DEvent.MOUSE_MOVED:
                if ( highlight != contains ) {
                  highlight = contains;
                  view.updateView();
                }
                if ( !contains ) {
                  stopEditing();
                }
            }
          }
        };
      } else {
        return null;
      }
    }

    public MouseInputEditor findMouseInputEditor( Graph2DView view, double x, double y, HitInfo hitInfo ) {
      return findMouseInputEditor( x, y );
    }
  }

  private class ScrollActionListener implements ActionListener {
    private final double dx;
    private final double dy;

    public ScrollActionListener( double dx, double dy ) {
      this.dx = dx;
      this.dy = dy;
    }

    public void actionPerformed( ActionEvent e ) {
      Point2D viewPoint2D = view.getViewPoint2D();
      view.setViewPoint2D( viewPoint2D.getX() + dx, viewPoint2D.getY() + dy );
      view.updateView();
    }
  }

  public static void main(String[] args) {
    EventQueue.invokeLater(new Runnable() {
      public void run() {
        initLnF();
        (new MouseInputDemo()).start("MouseInputDemo");
      }
    });
  }
}
