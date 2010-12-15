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
import y.base.Node;
import y.geom.YPoint;
import y.view.CreateEdgeMode;
import y.view.Drawable;
import y.view.EditMode;
import y.view.MoveSelectionMode;
import y.view.SnapLine;
import y.view.Graph2DView;
import y.view.MoveSnapContext;

import javax.swing.BorderFactory;
import javax.swing.JSlider;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.EventQueue;

/**
 * Demonstrates {@link EditMode}'s snapping feature in conjunction with orthogonal edges. <br>
 * This demo can be used to toggle the snapping feature on and off. It shows how a custom {@link SnapLine}
 * (the red vertical line) can be used to snap nodes and edges to other entities.
 * Toggling the "Snapping" button in the toolbar toggles snapping on and off, the sliders can be used to adjust the
 * preferred distance between nodes and edges. This will influence the "preferred distance snap lines."<br>
 * Toggling the "Grid" button in the toolbar toggles the grid on and off. Note that
 * {@link Graph2DView#setGridMode(boolean) enabling the grid on the view} has the effect that nodes can only
 * be placed on grid positions, thus it prevents the other snapping rules from being applied. The grid in this
 * demo uses the {@link MoveSnapContext#setUsingGridSnapping(boolean) newer grid snapping feature} instead, which
 * coexists nicely with other snapping rules.
 */
public class SnapLineDemo extends DemoBase {

  private EditMode editMode;
  private JToggleButton snapLineButton;
  private JToggleButton showGridButton;
  private SnappingConfiguration snappingConfiguration;

  /**
   * A custom single snap line that will displayed in the view and used by the {@link MoveSelectionMode}'s
   * {@link MoveSnapContext}.
   */
  private SnapLine snapLine;

  public SnapLineDemo() {
    // Initialize snapping.
    snappingConfiguration = createDefaultSnappingConfiguration();
    snappingConfiguration.configureView(view);
    snappingConfiguration.configureEditMode(editMode);

    setUsingSnapping(true);

    final Node n1 = view.getGraph2D().createNode(40, 30, "1");
    final Node n2 = view.getGraph2D().createNode(40, 90, "2");
    final Node n3 = view.getGraph2D().createNode(40, 210,"3");
    view.getGraph2D().createEdge(n1, n2);
    view.getGraph2D().createEdge(n2, n3);
    view.updateWorldRect();
  }

  protected void initialize() {
    super.initialize();

    snapLine = new SnapLine(SnapLine.VERTICAL, SnapLine.CENTER, new YPoint(200, 200), 0, 400, null, 1.0d);
    view.getGraph2D().addDrawable(new Drawable() {
      public void paint(Graphics2D g) {
        g.setColor(Color.red);
        snapLine.paint(g);
      }

      public Rectangle getBounds() {
        return snapLine.getBounds();
      }
    });
  }


  protected JToolBar createToolBar() {
    JToolBar toolBar = super.createToolBar();
    snapLineButton = new JToggleButton("Snapping");
    toolBar.add(snapLineButton);

    showGridButton = new JToggleButton("Grid");
    toolBar.add(showGridButton);

    final JSlider s1 = new JSlider(SwingConstants.HORIZONTAL, 0, 80, 30);
    s1.setBorder(BorderFactory.createTitledBorder("Node To Node"));
    s1.setMaximumSize(new Dimension(200, 100));
    toolBar.add(s1);
    final JSlider s2 = new JSlider(SwingConstants.HORIZONTAL, 0, 80, 20);
    s2.setBorder(BorderFactory.createTitledBorder("Node To Edge"));
    s2.setMaximumSize(new Dimension(200, 100));
    toolBar.add(s2);
    final JSlider s3 = new JSlider(SwingConstants.HORIZONTAL, 0, 80, 20);
    s3.setBorder(BorderFactory.createTitledBorder("Edge To Edge"));
    s3.setMaximumSize(new Dimension(200, 100));
    toolBar.add(s3);
    final ChangeListener listener = new ChangeListener() {
      public void stateChanged(ChangeEvent e) {
        snappingConfiguration.setSnappingEnabled(snapLineButton.isSelected());
        snappingConfiguration.setRemovingInnerBends(snapLineButton.isSelected());
        snappingConfiguration.setNodeToNodeDistance(s1.getValue());
        snappingConfiguration.setNodeToEdgeDistance(s2.getValue());
        snappingConfiguration.setEdgeToEdgeDistance(s3.getValue());
        snappingConfiguration.setGridSnappingEnabled(showGridButton.isSelected());

        snappingConfiguration.configureView(view);
        snappingConfiguration.configureEditMode(editMode);
      }
    };
    s1.addChangeListener(listener);
    s2.addChangeListener(listener);
    s3.addChangeListener(listener);
    snapLineButton.addChangeListener(listener);
    showGridButton.addChangeListener(listener);

    return toolBar;
  }

  protected EditMode createEditMode() {
    editMode = super.createEditMode();
    ((MoveSelectionMode) editMode.getMoveSelectionMode()).getSnapContext().addSnapLine(snapLine);

    // Edges are always orthogonal in this demo.
    editMode.setOrthogonalEdgeRouting(true);
    ((CreateEdgeMode)editMode.getCreateEdgeMode()).setOrthogonalEdgeCreation(true);

    return editMode;
  }

  public boolean isUsingSnapping() {
    return snapLineButton.isSelected();
  }

  public void setUsingSnapping(boolean usingSnapping) {
    this.snapLineButton.setSelected(usingSnapping);
  }

  public static void main(String[] args) {
    EventQueue.invokeLater(new Runnable() {
      public void run() {
        initLnF();
        (new SnapLineDemo()).start("SnapLine Demo");
      }
    });
  }
}