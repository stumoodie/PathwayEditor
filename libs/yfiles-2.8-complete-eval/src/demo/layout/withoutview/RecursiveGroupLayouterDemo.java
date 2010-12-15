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
package demo.layout.withoutview;

import y.base.DataProvider;
import y.base.EdgeMap;
import y.base.Node;
import y.base.NodeMap;
import y.layout.BufferedLayouter;
import y.layout.DefaultLayoutGraph;
import y.layout.LayoutGraph;
import y.layout.LayoutMultiplexer;
import y.layout.LayoutOrientation;
import y.layout.PortConstraint;
import y.layout.PortConstraintKeys;
import y.layout.grouping.GroupingKeys;
import y.layout.grouping.RecursiveGroupLayouter;
import y.layout.hierarchic.IncrementalHierarchicLayouter;

import java.awt.EventQueue;

/**
 * This class shows how to layout the contents of group nodes
 * each with different layout style. In this example,
 * the graph induced by the grouped nodes labeled 0 to 6 will be laid out
 * by HierarchicLayouter using BOTTOM_TO_TOP orientation,
 * while the remaining nodes will be laid out by
 * HierarchicLayouter using LEFT_TO_RIGHT orientation.
 */
public class RecursiveGroupLayouterDemo
{
  public static void main(String[] args) {
    EventQueue.invokeLater(new Runnable() {
      public void run() {
        LayoutGraph graph = new DefaultLayoutGraph();

        //create graph structure
        Node[] v = new Node[10];
        for (int i = 0; i < v.length; i++) {
          v[i] = graph.createNode();
          graph.setSize(v[i], 30, 30);
        }
        int[][] e = {{0, 1}, {0, 2}, {0, 3}, {4, 0}, {5, 0}, {0, 7}, {6, 0}, {6, 8}, {8, 7}};
        for (int i = 0; i < e.length; i++) {
          Node s = v[e[i][0]];
          Node t = v[e[i][1]];
          graph.createEdge(s, t);
        }

        //set up fixed port constraints for edges that connect at v[0]
        EdgeMap spcMap = graph.createEdgeMap();
        EdgeMap tpcMap = graph.createEdgeMap();
        graph.addDataProvider(PortConstraintKeys.SOURCE_PORT_CONSTRAINT_KEY, spcMap);
        graph.addDataProvider(PortConstraintKeys.TARGET_PORT_CONSTRAINT_KEY, tpcMap);
        spcMap.set(v[0].getEdgeTo(v[7]), PortConstraint.create(PortConstraint.ANY_SIDE, true));
        tpcMap.set(v[6].getEdgeTo(v[0]), PortConstraint.create(PortConstraint.ANY_SIDE, true));

        //set up edge group information (optional)
        EdgeMap sgMap = graph.createEdgeMap();
        EdgeMap tgMap = graph.createEdgeMap();
        graph.addDataProvider(PortConstraintKeys.SOURCE_GROUPID_KEY, sgMap);
        graph.addDataProvider(PortConstraintKeys.TARGET_GROUPID_KEY, tgMap);
        sgMap.set(v[0].getEdgeTo(v[1]), "G1");
        sgMap.set(v[0].getEdgeTo(v[2]), "G1");
        sgMap.set(v[0].getEdgeTo(v[3]), "G1");

        tgMap.set(v[0].getEdgeFrom(v[4]), "G2");
        tgMap.set(v[0].getEdgeFrom(v[5]), "G2");

        //set up grouping information
        NodeMap groupMap = graph.createNodeMap();
        NodeMap pidMap = graph.createNodeMap();
        NodeMap idMap = graph.createNodeMap();
        graph.addDataProvider(GroupingKeys.GROUP_DPKEY, groupMap);
        graph.addDataProvider(GroupingKeys.NODE_ID_DPKEY, idMap);
        graph.addDataProvider(GroupingKeys.PARENT_NODE_ID_DPKEY, pidMap);
        groupMap.setBool(v[9], true);
        for (int i = 0; i < 6; i++) {
          pidMap.set(v[i], v[9]);
        }
        for (int i = 0; i < v.length; i++) {
          idMap.set(v[i], v[i]);
        }

        //configure layout algorithm
        IncrementalHierarchicLayouter innerHL = new IncrementalHierarchicLayouter();
        innerHL.setLayoutOrientation(LayoutOrientation.BOTTOM_TO_TOP);
        IncrementalHierarchicLayouter outerHL = new IncrementalHierarchicLayouter();
        outerHL.setLayoutOrientation(LayoutOrientation.LEFT_TO_RIGHT);
        LayoutMultiplexer lm = new LayoutMultiplexer();
        NodeMap layoutMap = graph.createNodeMap();
        graph.addDataProvider(LayoutMultiplexer.LAYOUTER_DPKEY, layoutMap);
        for (int i = 0; i < 6; i++) {
          layoutMap.set(v[i], innerHL);
        }
        for (int i = 7; i < v.length; i++) {
          layoutMap.set(v[i], outerHL);
        }

        //launch layout algorithm
        RecursiveGroupLayouter rgl = new RecursiveGroupLayouter(lm);
        new BufferedLayouter(rgl).doLayout(graph);

        //remove group node
        graph.removeNode(v[9]);

        //  display result
        LayoutPreviewPanel lpp = new LayoutPreviewPanel(graph);
        lpp.createFrame("RecursiveGroupLayouterDemo").setVisible(true);

        //remove all registered DataProviders, NodeMap and EdgeMaps
        Object[] key = graph.getDataProviderKeys();
        for (int i = 0; i < key.length; i++) {
          DataProvider dp = graph.getDataProvider(key[i]);
          graph.removeDataProvider(key[i]);
          if (dp instanceof NodeMap) {
            graph.disposeNodeMap((NodeMap) dp);
          } else if (dp instanceof EdgeMap) {
            graph.disposeEdgeMap((EdgeMap) dp);
          }
        }
      }
    });

  }

}
