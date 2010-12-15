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

import y.algo.Trees;
import y.base.DataProvider;
import y.base.Edge;
import y.base.Graph;
import y.base.Node;
import y.base.NodeMap;
import y.layout.LayoutGraph;
import y.layout.Layouter;
import y.layout.tree.AbstractRotatableNodePlacer;
import y.layout.tree.AbstractRotatableNodePlacer.Matrix;
import y.layout.tree.AbstractRotatableNodePlacer.RootAlignment;
import y.layout.tree.BusPlacer;
import y.layout.tree.DefaultNodePlacer;
import y.layout.tree.DelegatingNodePlacer;
import y.layout.tree.DoubleLinePlacer;
import y.layout.tree.GenericTreeLayouter;
import y.layout.tree.LayeredNodePlacer;
import y.layout.tree.NodePlacer;
import y.layout.tree.SimpleNodePlacer;
import y.util.DataProviderAdapter;

/**
 * This class demonstrates the configuration for the {@link y.layout.tree.GenericTreeLayouter}.
 * There are several implementations that offer more or less complex configurations and compositions
 * of NodePlacers.
 * <p>
 * The configurations can be used as follows:
 * <code>
 * TreeLayoutConfiguration.PLAYOFFS.layout( new GenericTreeLayouter(), graph );
 * </code>
 *
 **/
public abstract class TreeLayoutConfiguration implements Layouter {

  /**
   * Very basic configuration that creates a "default" layered tree with orthogonal style
   * and the root alignment set to center.
   */
  public static final TreeLayoutConfiguration LAYERED_TREE = new TreeLayoutConfiguration() {
    protected void prepare() {
      super.prepare();
      LayeredNodePlacer layeredNodePlacer = new LayeredNodePlacer();
      layeredNodePlacer.setRootAlignment( RootAlignment.CENTER );
      layeredNodePlacer.setRoutingStyle( LayeredNodePlacer.ORTHOGONAL_STYLE );
      setNodePlacers( Trees.getRoot( graph ), layeredNodePlacer );
    }
  };

  /**
   * Very basic configuration that creates a rotated layered tree.
   */
  public static final TreeLayoutConfiguration LAYERED_TREE_90 = new TreeLayoutConfiguration() {
    protected void prepare() {
      super.prepare();
      LayeredNodePlacer layeredNodePlacer = new LayeredNodePlacer( Matrix.ROT90, Matrix.ROT90 );
      layeredNodePlacer.setRootAlignment( RootAlignment.CENTER );
      layeredNodePlacer.setRoutingStyle( LayeredNodePlacer.ORTHOGONAL_STYLE );
      setNodePlacers( Trees.getRoot( graph ), layeredNodePlacer );
    }
  };

  /**
   * Very basic configuration with custom spacing.
   */
  public static final TreeLayoutConfiguration DOUBLE_LINE = new TreeLayoutConfiguration() {
    protected void prepare() {
      super.prepare();
      DoubleLinePlacer nodePlacer = new DoubleLinePlacer();
      nodePlacer.setSpacing( 20 );
      layouter.setDefaultNodePlacer( nodePlacer );
    }
  };

  /**
   * Configuration that demonstrates how to use the DelegatingNodePlacer.
   */
  public static final TreeLayoutConfiguration DEFAULT_DELEGATING = new TreeLayoutConfiguration() {
    protected void prepare() {
      super.prepare();

      Node root = Trees.getRoot( graph );

      //First layer
      SimpleNodePlacer placerNorth = new SimpleNodePlacer( Matrix.ROT180 );
      placerNorth.setRootAlignment( RootAlignment.CENTER );

      SimpleNodePlacer placerSouth = new SimpleNodePlacer();
      placerSouth.setRootAlignment( RootAlignment.CENTER );

      setNodePlacer( root, new DelegatingNodePlacer( Matrix.DEFAULT, placerNorth, placerSouth ) );

      //Second layer
      int upperCount = root.outDegree() / 2;

      graph = ( LayoutGraph ) root.getGraph();
      int counter = 0;
      for ( Edge edge = root.firstOutEdge(); edge != null; edge = edge.nextOutEdge() ) {
        Node child = edge.target();

        if ( counter < upperCount ) {
          setNodePlacers( child, placerNorth );
        } else {
          setNodePlacers( child, placerSouth );
        }
        counter++;
      }
    }
  };

  /**
   * Configuration that can be used to layout *binary trees*. It generates something like a "playoff tree".
   * This configuration uses DelegatingNodePlacers for the first two layers.
   */
  public static final TreeLayoutConfiguration PLAYOFFS_DOUBLE = new TreeLayoutConfiguration() {
    protected void prepare() {
      super.prepare();

      //Root
      Node root = Trees.getRoot( graph );

      SimpleNodePlacer placerNorth = new SimpleNodePlacer( Matrix.MIR_HOR );
      placerNorth.setRootAlignment( RootAlignment.MEDIAN );

      SimpleNodePlacer placerSouth = new SimpleNodePlacer();
      placerSouth.setRootAlignment( RootAlignment.MEDIAN );

      DelegatingNodePlacer rootPlacer = new DelegatingNodePlacer( Matrix.DEFAULT, placerNorth, placerSouth );
      rootPlacer.setOrientation( DelegatingNodePlacer.VERTICAL );
      setNodePlacer( root, rootPlacer );

      if ( root.outDegree() != 2 ) {
        throw new IllegalStateException( "May only be used with a binary tree." );
      }

      //2nd layer
      Node upperChild = root.firstOutEdge().target();
      Node lowerChild = root.firstOutEdge().nextOutEdge().target();

      SimpleNodePlacer placerLeft = new SimpleNodePlacer( Matrix.ROT90 );
      placerLeft.setRootAlignment( RootAlignment.MEDIAN );

      SimpleNodePlacer placerRight = new SimpleNodePlacer( Matrix.ROT270 );
      placerRight.setRootAlignment( RootAlignment.MEDIAN );

      DelegatingNodePlacer placer2ndLayer = new DelegatingNodePlacer( Matrix.ROT180, placerLeft, placerRight );
      setNodePlacer( upperChild, placer2ndLayer );
      setNodePlacer( lowerChild, placer2ndLayer );

      //3rd layer+
      LayeredNodePlacer leftPlacer = new LayeredNodePlacer( Matrix.ROT90, Matrix.ROT90 );
      leftPlacer.setRootAlignment( RootAlignment.MEDIAN );
      leftPlacer.setRoutingStyle( LayeredNodePlacer.ORTHOGONAL_STYLE );

      LayeredNodePlacer rightPlacer = new LayeredNodePlacer( Matrix.ROT270, Matrix.ROT270 );
      rightPlacer.setRootAlignment( RootAlignment.MEDIAN );
      rightPlacer.setRoutingStyle( LayeredNodePlacer.ORTHOGONAL_STYLE );

      if ( upperChild.outDegree() != 2 ) {
        throw new IllegalStateException( "May only be used with a binary tree." );
      }
      if ( lowerChild.outDegree() != 2 ) {
        throw new IllegalStateException( "May only be used with a binary tree." );
      }

      Node upperLeft = upperChild.firstOutEdge().target();
      Node upperRight = upperChild.firstOutEdge().nextOutEdge().target();
      setNodePlacers( upperLeft, leftPlacer );
      setNodePlacers( upperRight, rightPlacer );

      Node lowerLeft = lowerChild.firstOutEdge().target();
      Node lowerRight = lowerChild.firstOutEdge().nextOutEdge().target();
      setNodePlacers( lowerLeft, leftPlacer );
      setNodePlacers( lowerRight, rightPlacer );
    }
  };

  /**
   * Configuration that can be used to layout *binary trees*. It generates something like a "playoff tree".
   */
  public static final TreeLayoutConfiguration PLAYOFFS = new TreeLayoutConfiguration() {
    protected void prepare() {
      super.prepare();

      //Root
      Node root = Trees.getRoot( graph );

      LayeredNodePlacer placerLeft = new LayeredNodePlacer( Matrix.ROT270, Matrix.ROT270 );
      placerLeft.setRootAlignment( RootAlignment.MEDIAN );
      placerLeft.setRoutingStyle( LayeredNodePlacer.ORTHOGONAL_STYLE );

      LayeredNodePlacer placerRight = new LayeredNodePlacer( Matrix.ROT90, Matrix.ROT90 );
      placerRight.setRootAlignment( RootAlignment.MEDIAN );
      placerRight.setRoutingStyle( LayeredNodePlacer.ORTHOGONAL_STYLE );

      DelegatingNodePlacer rootPlacer = new DelegatingNodePlacer( Matrix.DEFAULT, placerLeft, placerRight );
      setNodePlacer( root, rootPlacer );

      if ( root.outDegree() != 2 ) {
        throw new IllegalStateException( "May only be used with a binary tree." );
      }

      //2nd layer
      Node firstChild = root.firstOutEdge().target();
      Node secondChild = root.firstOutEdge().nextOutEdge().target();

      setNodePlacers( firstChild, placerLeft );
      setNodePlacers( secondChild, placerRight );
    }
  };

  /**
   * A special bus configuration that uses
   */
  public static final TreeLayoutConfiguration BUS = new TreeLayoutConfiguration() {
    protected void prepare() {
      super.prepare();

      Node root = Trees.getRoot( graph );
      setNodePlacer( root, new BusPlacer() );

      DoubleLinePlacer northDouble = new DoubleLinePlacer( Matrix.ROT180 );
      DoubleLinePlacer southDouble = new DoubleLinePlacer();

      SimpleNodePlacer north = new SimpleNodePlacer( Matrix.ROT180 );
      north.setRootAlignment( RootAlignment.CENTER );
      SimpleNodePlacer south = new SimpleNodePlacer();
      south.setRootAlignment( RootAlignment.CENTER );

      int upperCount = root.outDegree() / 2;

      graph = ( LayoutGraph ) root.getGraph();
      int counter = 0;
      for ( Edge edge = root.firstOutEdge(); edge != null; edge = edge.nextOutEdge() ) {
        Node child = edge.target();

        if ( counter < upperCount ) {
          setNodePlacer( child, north );
          setNodePlacerForChildren( child, northDouble );
        } else {
          setNodePlacer( child, south );
          setNodePlacerForChildren( child, southDouble );
        }
        counter++;
      }
    }
  };


  protected LayoutGraph graph;
  protected GenericTreeLayouter layouter;

  protected NodeMap nodePlacerMap;

  protected TreeLayoutConfiguration() {
  }

  protected void setNodePlacer( Node node, NodePlacer nodePlacer ) {
    nodePlacerMap.set( node, nodePlacer );
  }

  protected void setNodePlacers( Node root, NodePlacer nodePlacer ) {
    setNodePlacer( root, nodePlacer );
    setNodePlacerForChildren( root, nodePlacer );
  }

  protected void setNodePlacerForChildren( Node root, NodePlacer nodePlacer ) {
    for ( Edge edge = root.firstOutEdge(); edge != null; edge = edge.nextOutEdge() ) {
      Node child = edge.target();
      setNodePlacers( child, nodePlacer );
    }
  }

  public final void layout( GenericTreeLayouter layouter, LayoutGraph graph ) {
    configure( layouter, graph );
    try {
      layouter.doLayout( this.graph );
    } finally {
      cleanUp( this.graph );
    }

    this.layouter = null;
    this.graph = null;
  }

  protected void prepare() {
    nodePlacerMap = graph.createNodeMap();

    graph.addDataProvider( GenericTreeLayouter.NODE_PLACER_DPKEY, nodePlacerMap );
    graph.addDataProvider( GenericTreeLayouter.CHILD_COMPARATOR_DPKEY, new ChildEdgeComparatorProvider() );
  }

  /**
   * Do not forget to clean up afterwards {@link #cleanUp(y.base.Graph)}
   * @param layouter
   */
  public void configure( GenericTreeLayouter layouter, LayoutGraph graph ) {
    this.graph = graph;
    this.layouter = layouter;
    prepare();
  }

  public static void cleanUp( Graph graph ) {
    DataProvider nodePlacerMap = graph.getDataProvider( GenericTreeLayouter.NODE_PLACER_DPKEY );
    if ( nodePlacerMap != null && nodePlacerMap instanceof NodeMap ) {
      graph.disposeNodeMap( ( NodeMap ) nodePlacerMap );
    }
    graph.removeDataProvider( GenericTreeLayouter.NODE_PLACER_DPKEY );
    graph.removeDataProvider( GenericTreeLayouter.CHILD_COMPARATOR_DPKEY );
  }

  public boolean canLayout( LayoutGraph graph ) {
    return true;
  }

  public void doLayout( LayoutGraph graph ) {
    layout( new GenericTreeLayouter(), graph );
  }

  class ChildEdgeComparatorProvider extends DataProviderAdapter {
    public Object get( Object dataHolder ) {
      NodePlacer placer = ( NodePlacer ) nodePlacerMap.get( dataHolder );
      if ( placer instanceof AbstractRotatableNodePlacer ) {
        return ( ( AbstractRotatableNodePlacer ) placer ).createComparator();
      }
      if ( placer instanceof DefaultNodePlacer ) {
        return ( ( DefaultNodePlacer ) placer ).createComparator();
      }
      return null;
    }
  }
}
