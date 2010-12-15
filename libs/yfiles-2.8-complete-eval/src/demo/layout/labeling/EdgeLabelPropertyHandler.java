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
package demo.layout.labeling;

import y.option.OptionHandler;
import y.option.MappedListCellRenderer;
import y.view.EdgeLabel;
import y.view.View;
import y.view.Graph2D;
import y.base.Edge;
import y.base.EdgeList;

import java.util.Map;

/**
 * A simple OptionHandler that is used by the generic edge labeling demo.
 */
public class EdgeLabelPropertyHandler extends OptionHandler {
  private static final String TEXT = "Text";
  private static final String EDGE_LABEL_PROPERTIES = "Edge Label Properties";
  private static final String PREFERRED_PLACEMENT = "Preferred Placement";

  private EdgeLabel label;
  private View view;

  public EdgeLabelPropertyHandler(EdgeLabel label, View view) {
    super(EDGE_LABEL_PROPERTIES);
    setOptionsIOHandler(null);
    this.label = label;
    this.view = view;    

    addString(TEXT, label.getText(), 2);

    final Map preferredPlacementMap = EdgeLabel.preferredPlacementsToStringMap();    
    addEnum(PREFERRED_PLACEMENT, preferredPlacementMap.keySet().toArray(), new Byte(label.getPreferredPlacement()),
        new MappedListCellRenderer(preferredPlacementMap));
  }

  public void commitValues() {
    super.commitValues();

    Edge e = label.getOwner().getEdge();
    if(e != null) {
      final Graph2D graph = (Graph2D) e.getGraph();
      graph.backupRealizers(new EdgeList(e).edges());
    }

    label.setText(getString(TEXT));
    label.setPreferredPlacement(((Byte)get(PREFERRED_PLACEMENT)).byteValue());

    view.getGraph2D().updateViews();
  }
}
