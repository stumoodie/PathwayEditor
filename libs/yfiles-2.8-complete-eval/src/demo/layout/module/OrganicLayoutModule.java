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

import y.layout.organic.OrganicLayouter;
import y.option.OptionHandler;
import y.view.Selections;
import y.view.hierarchy.HierarchyManager;

/**
 * This module represents an interactive configurator and launcher for
 * {@link y.layout.organic.OrganicLayouter}.
 *
 */
public class OrganicLayoutModule extends LayoutModule
{
  private static final String ACTIVATE_DETERMINISTIC_MODE = "ACTIVATE_DETERMINISTIC_MODE";
  private static final String ACTIVATE_TREE_BEAUTIFIER = "ACTIVATE_TREE_BEAUTIFIER";
  private static final String MODE = "MODE";
  private static final String MAXIMAL_DURATION = "MAXIMAL_DURATION";
  private static final String ITERATION_FACTOR = "ITERATION_FACTOR";
  private static final String OBEY_NODE_SIZES = "OBEY_NODE_SIZES";
  private static final String GRAVITY_FACTOR = "GRAVITY_FACTOR";
  private static final String SPHERE_OF_ACTION = "SPHERE_OF_ACTION";
  private static final String INITIAL_PLACEMENT = "INITIAL_PLACEMENT";
  private static final String PREFERRED_EDGE_LENGTH = "PREFERRED_EDGE_LENGTH";
  private static final String VISUAL = "VISUAL";
  private static final String ALGORITHM = "ALGORITHM";
  private static final String ORGANIC = "ORGANIC";
  private static final String ONLY_SELECTION = "ONLY_SELECTION";
  private static final String MAINLY_SELECTION = "MAINLY_SELECTION";
  private static final String ALL = "ALL";
  private static final String AS_IS = "AS_IS";
  private static final String RANDOM = "RANDOM";
  private static final String AT_ORIGIN = "AT_ORIGIN";
  private static final String EDGE_LENGTH_DEVIATION = "EDGE_LENGTH_DEVIATION";
  private static final String REPULSION = "REPULSION";
  private static final String ATTRACTION = "ATTRACTION";
  
  private static final String GROUPING      = "GROUPING";
  private static final String GROUP_LAYOUT_POLICY = "GROUP_LAYOUT_POLICY";
  private static final String IGNORE_GROUPS = "IGNORE_GROUPS";
  private static final String LAYOUT_GROUPS = "LAYOUT_GROUPS";
  private static final String FIX_GROUPS    = "FIX_GROUPS";
  private static final String GROUP_NODE_COMPACTNESS = "GROUP_NODE_COMPACTNESS";

  // for the option handler
  private final static String[] initialPlacementEnum =
  { 
    RANDOM, 
    AT_ORIGIN,
    AS_IS 
  };

  // for the option handler
  private final static String[] sphereOfActionEnum =
  {
    ALL,
    MAINLY_SELECTION,
    ONLY_SELECTION
  };

  private OrganicLayouter organic;
  
  public OrganicLayoutModule()
  {
    super (ORGANIC,
           "yFiles Layout Team",
           "Wrapper for OrganicLayouter");
  }

  /**
   * Factory method. Responsible for creating and initializing
   * the OptionHandler for this module.
   */
  protected OptionHandler createOptionHandler()
  {
    if (organic == null){
      createOrganic();
    }
    
    OptionHandler op = new OptionHandler(getModuleName());

    op.useSection(VISUAL);
    op.addEnum(SPHERE_OF_ACTION,sphereOfActionEnum,
               organic.getSphereOfAction());
    op.addEnum(INITIAL_PLACEMENT,initialPlacementEnum,
               organic.getInitialPlacement());
    op.addInt(PREFERRED_EDGE_LENGTH, organic.getPreferredEdgeLength(), 0, 500);
    op.addBool(OBEY_NODE_SIZES,organic.getObeyNodeSize());
    op.addInt(ATTRACTION, organic.getAttraction(), 0, 2);
    op.addInt(REPULSION, organic.getRepulsion(), 0, 2);
    op.addDouble(GRAVITY_FACTOR,organic.getGravityFactor(),-0.2,2,1);
    op.addBool(ACTIVATE_TREE_BEAUTIFIER,organic.getActivateTreeBeautifier());

    op.useSection(ALGORITHM);
    op.addDouble(ITERATION_FACTOR,organic.getIterationFactor());
    op.addInt(MAXIMAL_DURATION,(int)(organic.getMaximumDuration()/1000));
    op.addBool(ACTIVATE_DETERMINISTIC_MODE,organic.getActivateDeterministicMode());
    
    op.useSection(GROUPING);
    String[] gEnum = { LAYOUT_GROUPS, FIX_GROUPS, IGNORE_GROUPS };
    op.addEnum(GROUP_LAYOUT_POLICY, gEnum, 0);
    op.addDouble(GROUP_NODE_COMPACTNESS, organic.getGroupNodeCompactness(), 0, 1);
    return op;
  }

  /**
   * Module initialization routine. Typically this method is used to 
   * configure the underlying algorithm with the options found in the
   * options handler of this module.
   */
  protected void init()
  {
    createOrganic();
    
    OptionHandler op = getOptionHandler();

    organic.setPreferredEdgeLength(op.getInt(VISUAL,PREFERRED_EDGE_LENGTH));
    organic.setMaximumDuration(1000*op.getInt(ALGORITHM,MAXIMAL_DURATION));
    organic.setInitialPlacement((byte)OptionHandler.getIndex(initialPlacementEnum,
                                          op.getString(VISUAL,INITIAL_PLACEMENT)));
    organic.setSphereOfAction((byte)OptionHandler.getIndex(sphereOfActionEnum,
                                        op.getString(VISUAL,SPHERE_OF_ACTION)));
    organic.setGravityFactor(op.getDouble(VISUAL,GRAVITY_FACTOR));
    organic.setObeyNodeSize(op.getBool(VISUAL,OBEY_NODE_SIZES));
    organic.setIterationFactor(op.getDouble(ALGORITHM,ITERATION_FACTOR));
    organic.setActivateTreeBeautifier(op.getBool(VISUAL,ACTIVATE_TREE_BEAUTIFIER));
    organic.setActivateDeterministicMode(op.getBool(ALGORITHM,ACTIVATE_DETERMINISTIC_MODE));
    organic.setAttraction(op.getInt(VISUAL,ATTRACTION));
    organic.setRepulsion(2-op.getInt(VISUAL,REPULSION));
    organic.setGroupNodeCompactness(op.getDouble(GROUPING, GROUP_NODE_COMPACTNESS));
  }
  
  /**
   * Main module execution routine. launches the hierarchic layouter.
   */
  protected void mainrun()
  {
    try{
      getGraph2D().addDataProvider(OrganicLayouter.SPHERE_OF_ACTION_NODES, 
                  Selections.createSelectionNodeMap(getGraph2D()));

      OptionHandler op = getOptionHandler();
      if(HierarchyManager.containsGroupNodes(getGraph2D()))
      {
        if (op.get(GROUPING, GROUP_LAYOUT_POLICY).equals(FIX_GROUPS))
        {
          organic.setGroupNodePolicy(OrganicLayouter.FIXED_GROUPS_POLICY);
        } else if (op.get(GROUPING, GROUP_LAYOUT_POLICY).equals(IGNORE_GROUPS)){
          organic.setGroupNodePolicy(OrganicLayouter.IGNORE_GROUPS_POLICY);
        } else {
          organic.setGroupNodePolicy(OrganicLayouter.LAYOUT_GROUPS_POLICY);
        }
      }

      launchLayouter(organic);
    } finally {
      getGraph2D().removeDataProvider(OrganicLayouter.SPHERE_OF_ACTION_NODES);
    }
  }
  
  /**
   * clean up the module, clear temporarily bound data providers and
   * references to the wrapped algorithm.
   */
  protected void dispose()
  {
    organic = null;
  }
  
  private void createOrganic()
  {
    if(organic == null)
    {
      organic = new OrganicLayouter();
    }
  }
}

