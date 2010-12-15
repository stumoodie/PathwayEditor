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
package demo.layout.module;

import y.module.LayoutModule;
import y.module.YModule;

import y.base.EdgeMap;
import y.layout.BendConverter;
import y.layout.CompositeLayoutStage;
import y.layout.LayoutStage;
import y.layout.Layouter;
import y.layout.SequentialLayouter;
import y.layout.grouping.GroupNodeHider;
import y.layout.organic.RemoveOverlapsLayoutStage;
import y.layout.router.OrganicEdgeRouter;
import y.option.OptionGroup;
import y.option.OptionHandler;
import y.view.Selections;

/**
 * Module for the Organic Edge Router Algorithm.
 */
public class OrganicEdgeRouterModule extends LayoutModule
{
  private static final String ORGANIC_EDGE_ROUTER = "ORGANIC_EDGE_ROUTER";
  private static final String MINIMAL_NODE_DISTANCE = "MINIMAL_NODE_DISTANCE";
  private static final String USE_BENDS = "USE_BENDS";
  private static final String ROUTE_ONLY_NECESSARY = "ROUTE_ONLY_NECESSARY";
  private static final String SELECTION_ONLY = "SELECTION_ONLY";
  private static final String ALLOW_MOVING_NODES = "ALLOW_MOVING_NODES";

  private static final String LAYOUT_OPTIONS = "LAYOUT_OPTIONS";


  /**
   * Creates a new Instance of this Module.
   */
  public OrganicEdgeRouterModule()
  {
    super(ORGANIC_EDGE_ROUTER, "Sebastian Mueller", "Routes edges organically");
  }
  
  /**
   * Creates an option handler for this module that
   * manages options for the force transfer algorithm.
   */
  public OptionHandler createOptionHandler()
  {
    OrganicEdgeRouter router = new OrganicEdgeRouter();
    
    OptionHandler op = new OptionHandler(getModuleName());
    OptionGroup og = new OptionGroup();
    og.setAttribute(OptionGroup.ATTRIBUTE_TITLE, LAYOUT_OPTIONS);
    og.addItem(op.addBool(SELECTION_ONLY, false));
    og.addItem(op.addInt(MINIMAL_NODE_DISTANCE, (int)router.getMinimalDistance(), 10, 300));
    og.addItem(op.addBool(USE_BENDS, router.isUsingBends()));
    og.addItem(op.addBool(ROUTE_ONLY_NECESSARY, !router.isRoutingAll()));
    og.addItem(op.addBool(ALLOW_MOVING_NODES, false));
    return op;
  }
  
  /**
   * Launches this module.
   */
  protected void mainrun()
  {
    final OrganicEdgeRouter router = new OrganicEdgeRouter();
    OptionHandler op = getOptionHandler();
    router.setMinimalDistance(op.getInt(MINIMAL_NODE_DISTANCE));
    router.setUsingBends(op.getBool(USE_BENDS));
    router.setRoutingAll(!op.getBool(ROUTE_ONLY_NECESSARY));
    
    RemoveOverlapsLayoutStage rmos = new RemoveOverlapsLayoutStage(0);
    LayoutStage nodeEnlarger = router.createNodeEnlargementStage();
    final CompositeLayoutStage cls = new CompositeLayoutStage();
    cls.appendStage(nodeEnlarger);
    if (router.isUsingBends()) {
      cls.appendStage(new BendConverter());
    }
    cls.appendStage(rmos);

    final SequentialLayouter sl = new SequentialLayouter();
    if(op.getBool(ALLOW_MOVING_NODES)) {
      sl.appendLayouter(cls);
    }
    sl.appendLayouter(router);
    Layouter custom = new GroupNodeHider(sl);

    // register grouping relevant DataProviders
    if (op.getBool(SELECTION_ONLY)) {
      EdgeMap nm = Selections.createSelectionEdgeMap(getGraph2D());
      getGraph2D().addDataProvider(OrganicEdgeRouter.ROUTE_EDGE_DPKEY, nm);
      launchLayouter(custom);
      getGraph2D().removeDataProvider(OrganicEdgeRouter.ROUTE_EDGE_DPKEY);
    } else {
      launchLayouter(custom);
    }
  }
}





