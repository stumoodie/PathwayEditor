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
package demo.layout.tree;

import demo.view.DemoDefaults;
import y.base.Node;
import y.base.NodeCursor;
import y.base.NodeMap;
import y.layout.tree.AbstractRotatableNodePlacer.Matrix;
import y.layout.tree.AbstractRotatableNodePlacer.RootAlignment;
import y.layout.tree.AssistantPlacer;
import y.layout.tree.DoubleLinePlacer;
import y.layout.tree.NodePlacer;
import y.layout.tree.SimpleNodePlacer;
import y.view.Arrow;
import y.view.EdgeRealizer;
import y.view.Graph2D;
import y.view.LineType;
import y.view.PolyLineEdgeRealizer;
import y.view.PopupMode;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JPopupMenu;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.EventQueue;

/**
 * This demo shows how to use {@link y.layout.tree.GenericTreeLayouter}
 * in conjunction with {@link y.layout.tree.AssistantPlacer}.
 * <br>
 * AssistantPlacer is a special NodePlacer that uses two layout strategies.
 * Depending on the boolean provided through the special DataProvider found at
 * the key {@link y.layout.tree.AssistantPlacer#ASSISTANT_DPKEY},
 * the AssistantPlacer decides how to layout its children.<br>
 * If the boolean is set to true for a specific node, it is interpreted as "assistant."
 * All assistants are placed using the {@link y.layout.tree.LeftRightPlacer}.
 * <br>
 * The other children are placed below the assistants, using the child node
 *  placer of the AssistantPlacer. The child node placer can be set using the
 * method
 * {@link y.layout.tree.AssistantPlacer#setChildNodePlacer(y.layout.tree.NodePlacer)}.
 * <br>
 * This demo offers its functionality via context menus. The actual selected
 * nodes can be marked as assistants or "non-assistants," and the child node
 * placer can be be set this way, too.
 */
public class AssistantPlacerDemo extends AbstractTreeDemo {
  public static void main(String[] args) {
    EventQueue.invokeLater(new Runnable() {
      public void run() {
        initLnF();
        (new AssistantPlacerDemo()).start("Assistant Placer Demo");
      }
    });
  }

  private NodeMap isAssistantNodeMap;

  public AssistantPlacerDemo() {
    Graph2D graph = view.getGraph2D();

    isAssistantNodeMap = graph.createNodeMap();
    graph.addDataProvider( AssistantPlacer.ASSISTANT_DPKEY, isAssistantNodeMap );

    //Realizers
    EdgeRealizer defaultER = graph.getDefaultEdgeRealizer();
    defaultER.setArrow( Arrow.STANDARD );
    ( ( PolyLineEdgeRealizer ) defaultER ).setSmoothedBends( true );
    defaultER.setLineType( LineType.LINE_2 );

    createSampleGraph( view.getGraph2D() );
    calcLayout();
  }

  protected NodePlacer createDefaultNodePlacer() {
    return new AssistantPlacer();
  }

  protected boolean isDeletionEnabled() {
    return false;
  }

  private void createSampleGraph( Graph2D graph ) {
    graph.clear();
    Node root = graph.createNode();
    graph.getRealizer( root ).setFillColor( layerColors[ 0 ] );
    nodePlacerMap.set( root, new AssistantPlacer() );
    createChildren( graph, root, 6, 1, 1 );
    calcLayout();
    view.fitContent();
  }

  private void createChildren( Graph2D graph, Node root, int children, int layer, int layers ) {
    for ( int i = 0; i < children; i++ ) {
      Node child = graph.createNode();
      graph.createEdge( root, child );
      graph.getRealizer( child ).setFillColor( layerColors[ layer % layerColors.length ] );

      if ( i % 3 == 0 ) {
        isAssistantNodeMap.setBool(child, true);
      }
      NodePlacer nodePlacer = new AssistantPlacer();
      nodePlacerMap.set( child, nodePlacer );
      if ( layers > 0 ) {
        createChildren( graph, child, children, layer + 1, layers - 1 );
      }
    }
  }

  protected PopupMode createTreePopupMode() {
    return new TreeLayouterPopupMode();
  }

  private final class TreeLayouterPopupMode extends PopupMode {
    private JPopupMenu nodePlacementMenu;
    private JCheckBoxMenuItem checkbox;

    TreeLayouterPopupMode() {
      nodePlacementMenu = new JPopupMenu();

      checkbox = new JCheckBoxMenuItem( "Assistant" );
      nodePlacementMenu.add( checkbox );

      checkbox.addActionListener( new ActionListener() {
        public void actionPerformed( ActionEvent e ) {
          for ( NodeCursor nodeCursor = getGraph2D().selectedNodes(); nodeCursor.ok(); nodeCursor.next() ) {
            Node node = nodeCursor.node();
            isAssistantNodeMap.setBool( node, checkbox.isSelected() );
          }
          calcLayout();
        }
      } );

      nodePlacementMenu.addSeparator();

      JMenu childPlacer = new JMenu( "Child NodePlacers" );
      nodePlacementMenu.add( childPlacer );

      childPlacer.add( new SetNodePlacerAction( "Default" ) {
        protected NodePlacer createNodePlacer() {
          AssistantPlacer assistantPlacer = new AssistantPlacer();
          assistantPlacer.setChildNodePlacer( new SimpleNodePlacer( Matrix.DEFAULT, RootAlignment.CENTER ) );
          return assistantPlacer;
        }
      } );

      childPlacer.add( new SetNodePlacerAction( "Double Line" ) {
        protected NodePlacer createNodePlacer() {
          AssistantPlacer assistantPlacer = new AssistantPlacer();
          assistantPlacer.setChildNodePlacer( new DoubleLinePlacer( Matrix.DEFAULT ) );
          return assistantPlacer;
        }
      } );
    }

    public JPopupMenu getNodePopup( final Node v ) {
      checkbox.setSelected( isAssistantNodeMap.getBool( v ) );
      return nodePlacementMenu;
    }

    private void updateSelectionState() {
      //Set selection state
      checkbox.setSelected( false );
      for ( NodeCursor nodeCursor = getGraph2D().selectedNodes(); nodeCursor.ok(); nodeCursor.next() ) {
        Node node = nodeCursor.node();
        if ( isAssistantNodeMap.getBool( node ) ) {
          checkbox.setSelected( true );
          break;
        }
      }
    }

    public JPopupMenu getSelectionPopup( double x, double y ) {
      if ( getGraph2D().selectedNodes().ok() ) {
        updateSelectionState();
        return nodePlacementMenu;
      } else {
        return null;
      }
    }
  }
}
