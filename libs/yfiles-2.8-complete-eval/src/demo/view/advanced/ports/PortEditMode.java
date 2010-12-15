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
package demo.view.advanced.ports;

import y.view.CreateEdgeMode;
import y.view.EditMode;
import y.view.MouseInputMode;
import y.view.MovePortMode;
import y.view.SelectionBoxMode;
import y.view.ViewMode;

/**
 * Customized {@link y.view.EditMode} that takes {@link y.view.NodePort}s
 * into account (especially when deselecting elements on paper clicks).
 *
 */
class PortEditMode extends EditMode {
  PortEditMode() {
    setCyclicSelectionEnabled(true);
  }

  /**
   * Overwritten to create a {@link demo.view.advanced.ports.NodePortPopupMode}
   * instance.
   * @return a {@link demo.view.advanced.ports.NodePortPopupMode}
   * instance.
   */
  protected ViewMode createPopupMode() {
    return new NodePortPopupMode();
  }

  /**
   * Overwritten to create a {@link y.view.MovePortMode} instance that allows
   * reassigning edges to new nodes.
   * @return a {@link y.view.MovePortMode} instance.
   */
  protected ViewMode createMovePortMode() {
    final MovePortMode mpm = new MovePortMode();
    mpm.setChangeEdgeEnabled(true);
    return mpm;
  }

  /**
   * Overwritten to create a {@link y.view.CreateEdgeMode} instance that
   * visually indicates target nodes and ports for newly created edges.
   * @return a {@link y.view.CreateEdgeMode} instance.
   */
  protected ViewMode createCreateEdgeMode() {
    final CreateEdgeMode cem = new PortCreateEdgeMode();
    cem.setIndicatingTargetNode(true);
    return cem;
  }

  /**
   * Overwritten to create a {@link y.view.MouseInputMode} instance
   * that supports interactive selecting/deselecting and moving of
   * {@link y.view.NodePort} instances.
   * @return a {@link y.view.MouseInputMode} instance.
   */
  protected MouseInputMode createMouseInputMode() {
    final MouseInputMode mim = new MouseInputMode();
    mim.setNodeSearchingEnabled(true);
    return mim;
  }

  /**
   * Overwritten to create a {@link y.view.SelectionBoxMode} instance
   * that supports selecting {@link y.view.NodePort} instances and
   * {@link y.view.YLabel} instances as well as {@link y.base.Node},
   * {@link y.base.Edge}, and {@link y.view.Bend} instances.
   * @return a {@link y.view.SelectionBoxMode} instance.
   */
  protected ViewMode createSelectionBoxMode() {
    final SelectionBoxMode sbm = new SelectionBoxMode();
    sbm.setExtendedTypeSelectionEnabled(true);
    return sbm;
  }
}
