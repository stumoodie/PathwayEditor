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

import y.base.DataProvider;
import y.base.Node;
import y.layout.ComponentLayouter;
import y.layout.LayoutOrientation;
import y.layout.genealogy.FamilyTreeLayouter;
import y.layout.hierarchic.IncrementalHierarchicLayouter;
import y.layout.hierarchic.incremental.EdgeLayoutDescriptor;
import y.layout.hierarchic.incremental.NodeLayoutDescriptor;
import y.option.ConstraintManager;
import y.option.OptionGroup;
import y.option.OptionHandler;
import y.util.DataProviderAdapter;
import y.view.Graph2D;
import y.view.NodeRealizer;

import java.awt.Color;

/**
 * This module represents an interactive configurator and launcher for
 * {@link y.layout.genealogy.FamilyTreeLayouter}.
 * */
public class FamilyTreeLayoutModule extends LayoutModule {

  private static final String FAMILY_TREE_LAYOUTER = "FAMILY_TREE_LAYOUTER";

  // The colors used by the data provider to distiguish familiy nodes from individual nodes
  private static final String FAMILY_PROPERTIES = "FAMILY_PROPERTIES";
  private static final String FAMILY_COLOR = "FAMILY_COLOR";
  private static final String MALE_COLOR = "MALE_COLOR";
  private static final String FEMALE_COLOR = "FEMALE_COLOR";

  // Basic layout properties
  private static final String LAYOUT = "LAYOUT";
  private static final String ORIENTATION = "ORIENTATION";
  private static final String RIGHT_TO_LEFT = "RIGHT_TO_LEFT";
  private static final String BOTTOM_TO_TOP = "BOTTOM_TO_TOP";
  private static final String LEFT_TO_RIGHT = "LEFT_TO_RIGHT";
  private static final String TOP_TO_BOTTOM = "TOP_TO_BOTTOM";
  private static final String SINGLE_DIRECT_BELOW = "SINGLE_DIRECT_BELOW";
  private static final String FAMILIES_ALWAYS_BELOW = "FAMILIES_ALWAYS_BELOW";

  private static final String DISTANCES = "DISTANCES";
  private static final String SIBLING_DISTANCES = "SIBLING_DISTANCES";
  private static final String HORIZONTAL_SPACING = "HORIZONTAL_SPACING";
  private static final String NODE_TO_NODE_DISTANCE = "NODE_TO_NODE_DISTANCE";

  private static final String GENERATION_DISTANCES = "GENERATION_DISTANCES";
  private static final String VERTICAL_SPACING = "VERTICAL_SPACING";
  private static final String MINIMUM_LAYER_DISTANCE = "MINIMUM_LAYER_DISTANCE";
  private static final String MINIMUM_FIRST_SEGMENT = "MINIMUM_FIRST_SEGMENT";
  private static final String MINIMUM_LAST_SEGMENT = "MINIMUM_LAST_SEGMENT";

  // Advanced layout properties
  private static final String ADVANCED_LAYOUT = "ADVANCED_LAYOUT";
  private static final String COMPONENT_LAYOUTER = "COMPONENT_LAYOUTER";
  private static final String USE_COMPONENT_LAYOUTER = "USE_COMPONENT_LAYOUTER";
  private static final String COMPONENT_DISTANCE = "COMPONENT_DISTANCE";
  private static final String COMPONENT_STYLE = "COMPONENT_STYLE";
  private static final String COMPONENT_STYLE_NONE = "COMPONENT_STYLE_NONE";
  private static final String COMPONENT_STYLE_ROWS = "COMPONENT_STYLE_ROWS";
  private static final String COMPONENT_STYLE_SINGLE_ROW = "COMPONENT_STYLE_SINGLE_ROW";
  private static final String COMPONENT_STYLE_SINGLE_COLUMN = "COMPONENT_STYLE_SINGLE_COLUMN";
  private static final String COMPONENT_STYLE_PACKED_COMPACT_RECTANGLE = "COMPONENT_STYLE_PACKED_COMPACT_RECTANGLE";
  private static final String COMPONENT_STYLE_PACKED_RECTANGLE = "COMPONENT_STYLE_PACKED_RECTANGLE";
  private static final String COMPONENT_STYLE_PACKED_CIRCLE = "COMPONENT_STYLE_PACKED_CIRCLE";
  private static final String COMPONENT_STYLE_PACKED_COMPACT_CIRCLE = "COMPONENT_STYLE_PACKED_COMPACT_CIRCLE";
  private static final String SORT_BY_SEX = "SORT_BY_SEX";
  private static final String DO_NOT_SORT = "DO_NOT_SORT";
  private static final String FEMALE_LEFT = "FEMALE_LEFT";
  private static final String FEMALE_ALWAYS_LEFT = "FEMALE_ALWAYS_LEFT";
  private static final String MALE_LEFT = "MALE_LEFT";
  private static final String MALE_ALWAYS_LEFT = "MALE_ALWAYS_LEFT";

  private static final String NODE_ALIGNMENT = "NODE_ALIGNMENT";
  private static final String NODE_ALIGN_TOP = "NODE_ALIGN_TOP";
  private static final String NODE_ALIGN_CENTER = "NODE_ALIGN_CENTER";
  private static final String NODE_ALIGN_BOTTOM = "NODE_ALIGN_BOTTOM";

//  private static final String USE_LEAN_LAYERS_LAYERER = "USE_LEAN_LAYERS_LAYERER";
//  private static final String ASPECT_RATIO = "ASPECT_RATIO";
//  private static final String MAX_ITERATIONS = "MAX_ITERATIONS";
//  private static final String LAYER_WIDTH = "LAYER_WIDTH";
//  private static final String LAYER_ENHANCED = "LAYER_ENHANCED";
//  private static final String HORIZONTAL_COMPACTION = "HORIZONTAL_COMPACTION";
//  private static final String BREAK_LONG_SEGMENTS = "BREAK_LONG_SEGMENTS";


  private static final Object[] sortBySexEnum = {DO_NOT_SORT, FEMALE_LEFT, FEMALE_ALWAYS_LEFT, MALE_LEFT, MALE_ALWAYS_LEFT};
  private static final Object[] orientEnum = {TOP_TO_BOTTOM, LEFT_TO_RIGHT, BOTTOM_TO_TOP, RIGHT_TO_LEFT};
  private static final Object[] nodeAlignmentEnum = {NODE_ALIGN_TOP, NODE_ALIGN_CENTER, NODE_ALIGN_BOTTOM};
  private static final Object[] componentStyleEnum =
  {
          COMPONENT_STYLE_NONE,
          COMPONENT_STYLE_ROWS,
          COMPONENT_STYLE_SINGLE_ROW,
          COMPONENT_STYLE_SINGLE_COLUMN,
          COMPONENT_STYLE_PACKED_RECTANGLE,
          COMPONENT_STYLE_PACKED_COMPACT_RECTANGLE,
          COMPONENT_STYLE_PACKED_CIRCLE,
          COMPONENT_STYLE_PACKED_COMPACT_CIRCLE,
  };

  public FamilyTreeLayoutModule() {
    super(FAMILY_TREE_LAYOUTER, "yFiles Layout Team", "Layouter for genealogic data");
  }

  /**
   * Creates an option handler for this class.
   *
   * @return The option handler for this module.
   */
  protected OptionHandler createOptionHandler() {
    OptionHandler op = new OptionHandler(getModuleName());
    ConstraintManager cm = new ConstraintManager(op);

    op.useSection(FAMILY_PROPERTIES);
    op.addColor(FAMILY_COLOR, Color.black, true);
    op.addColor(MALE_COLOR, new Color(0xCCCCFF), true);
    op.addColor(FEMALE_COLOR, new Color(0xFF99CC), true);

    op.useSection(LAYOUT);
    op.addEnum(ORIENTATION, orientEnum, 0);
    op.addBool(SINGLE_DIRECT_BELOW, true);
    op.addBool(FAMILIES_ALWAYS_BELOW, false);
    op.addEnum(NODE_ALIGNMENT, nodeAlignmentEnum, 0);
    op.addEnum(SORT_BY_SEX, sortBySexEnum, 0);

    op.useSection(DISTANCES);
    OptionGroup ogSiblings = new OptionGroup();
    ogSiblings.setAttribute(OptionGroup.ATTRIBUTE_TITLE, SIBLING_DISTANCES);
    ogSiblings.addItem(op.addDouble(HORIZONTAL_SPACING, 40, 0, 400));
    ogSiblings.addItem(op.addDouble(NODE_TO_NODE_DISTANCE, 40, 0, 400));
    OptionGroup ogGenerations = new OptionGroup();
    ogGenerations.setAttribute(OptionGroup.ATTRIBUTE_TITLE, GENERATION_DISTANCES);
    ogGenerations.addItem(op.addDouble(VERTICAL_SPACING, 10, 0, 100));
    ogGenerations.addItem(op.addDouble(MINIMUM_LAYER_DISTANCE, 40, 0, 400));
    ogGenerations.addItem(op.addDouble(MINIMUM_FIRST_SEGMENT, 40, 0, 400));
    ogGenerations.addItem(op.addDouble(MINIMUM_LAST_SEGMENT, 20, 0, 400));

    op.useSection(ADVANCED_LAYOUT);
    op.addBool(USE_COMPONENT_LAYOUTER, true);
    OptionGroup ogComp = new OptionGroup();
    ogComp.setAttribute(OptionGroup.ATTRIBUTE_TITLE, COMPONENT_LAYOUTER);
    ogComp.addItem(op.addInt(COMPONENT_DISTANCE, 40));
    ogComp.addItem(op.addEnum(COMPONENT_STYLE, componentStyleEnum, 1));
    cm.setEnabledOnValueEquals(USE_COMPONENT_LAYOUTER, Boolean.TRUE, COMPONENT_DISTANCE);

//    op.addBool(USE_LEAN_LAYERS_LAYERER, false);
//    op.addDouble(ASPECT_RATIO, 0.75, 0.1, 10);
//    op.addInt(MAX_ITERATIONS, 5, 1, 20);
////    op.addInt(LAYER_WIDTH, 10, 1, 1000);
////    op.addBool(LAYER_ENHANCED, true);
//    op.addBool(HORIZONTAL_COMPACTION, true);
//    op.addBool(BREAK_LONG_SEGMENTS, true);


    return op;
  }

  /** Main execution code to be implemented by any subclassed module. */
  public void mainrun() {

    final OptionHandler op = getOptionHandler();

    final Graph2D graph = getGraph2D();

    /* Sets the properties for the inner layouter */
    FamilyTreeLayouter layouter = new FamilyTreeLayouter();
    layouter.setSpacingBetweenFamilyMembers(op.getDouble(HORIZONTAL_SPACING));
    layouter.setOffsetForFamilyNodes(op.getDouble(VERTICAL_SPACING));
    layouter.setFamilyNodesAlwaysBelow(op.getBool(FAMILIES_ALWAYS_BELOW));
    layouter.setPartnerlessBelow(op.getBool(SINGLE_DIRECT_BELOW));

    /* Sets the orientation */
//    ((OrientationLayouter) layouter.getOrientationLayouter()).setMirrorMask(
//        OrientationLayouter.MIRROR_BOTTOM_TO_TOP | OrientationLayouter.MIRROR_LEFT_TO_RIGHT);
    if (op.get(ORIENTATION).equals(TOP_TO_BOTTOM)) {
      layouter.setLayoutOrientation(LayoutOrientation.TOP_TO_BOTTOM);
    } else if (op.get(ORIENTATION).equals(LEFT_TO_RIGHT)) {
      layouter.setLayoutOrientation(LayoutOrientation.LEFT_TO_RIGHT);
    } else if (op.get(ORIENTATION).equals(BOTTOM_TO_TOP)) {
      layouter.setLayoutOrientation(LayoutOrientation.BOTTOM_TO_TOP);
    } else if (op.get(ORIENTATION).equals(RIGHT_TO_LEFT)) {
      layouter.setLayoutOrientation(LayoutOrientation.RIGHT_TO_LEFT);
    }

    /* Advanced */

    /* Component Layouter */
    layouter.setComponentLayouterEnabled(op.getBool(USE_COMPONENT_LAYOUTER));
    ComponentLayouter cl = (ComponentLayouter) layouter.getComponentLayouter();
    cl.setComponentSpacing(op.getInt(COMPONENT_DISTANCE));
    if (op.get(COMPONENT_STYLE).equals(COMPONENT_STYLE_NONE)) {
      cl.setStyle(ComponentLayouter.STYLE_NONE);
    } else if (op.get(COMPONENT_STYLE).equals(COMPONENT_STYLE_PACKED_CIRCLE)) {
      cl.setStyle(ComponentLayouter.STYLE_PACKED_CIRCLE);
    } else if (op.get(COMPONENT_STYLE).equals(COMPONENT_STYLE_PACKED_COMPACT_CIRCLE)) {
      cl.setStyle(ComponentLayouter.STYLE_PACKED_COMPACT_CIRCLE);
    } else if (op.get(COMPONENT_STYLE).equals(COMPONENT_STYLE_PACKED_COMPACT_RECTANGLE)) {
      cl.setStyle(ComponentLayouter.STYLE_PACKED_COMPACT_RECTANGLE);
    } else if (op.get(COMPONENT_STYLE).equals(COMPONENT_STYLE_PACKED_RECTANGLE)) {
      cl.setStyle(ComponentLayouter.STYLE_PACKED_RECTANGLE);
    } else if (op.get(COMPONENT_STYLE).equals(COMPONENT_STYLE_ROWS)) {
      cl.setStyle(ComponentLayouter.STYLE_ROWS);
    } else if (op.get(COMPONENT_STYLE).equals(COMPONENT_STYLE_SINGLE_COLUMN)) {
      cl.setStyle(ComponentLayouter.STYLE_SINGLE_COLUMN);
    } else if (op.get(COMPONENT_STYLE).equals(COMPONENT_STYLE_SINGLE_ROW)) {
      cl.setStyle(ComponentLayouter.STYLE_SINGLE_ROW);
    }

      IncrementalHierarchicLayouter ihl = new IncrementalHierarchicLayouter();
      layouter.setTopLayouter(ihl);

      /* Vertical node alignment */
      NodeLayoutDescriptor nld = ihl.getNodeLayoutDescriptor();
      if (op.get(NODE_ALIGNMENT).equals(NODE_ALIGN_TOP)) {
        nld.setLayerAlignment(0.0);
        layouter.setAlignment(FamilyTreeLayouter.ALIGN_TOP);
      } else if (op.get(NODE_ALIGNMENT).equals(NODE_ALIGN_CENTER)) {
        nld.setLayerAlignment(0.5);
        layouter.setAlignment(FamilyTreeLayouter.ALIGN_CENTER);
      } else if (op.get(NODE_ALIGNMENT).equals(NODE_ALIGN_BOTTOM)) {
        nld.setLayerAlignment(1.0);
        layouter.setAlignment(FamilyTreeLayouter.ALIGN_BOTTOM);
      }

      ihl.setMinimumLayerDistance(op.getDouble(MINIMUM_LAYER_DISTANCE));
      ihl.setNodeToNodeDistance(op.getDouble(NODE_TO_NODE_DISTANCE));
      EdgeLayoutDescriptor eld = ihl.getEdgeLayoutDescriptor();
      eld.setMinimumFirstSegmentLength(op.getDouble(MINIMUM_FIRST_SEGMENT));
      eld.setMinimumLastSegmentLength(op.getDouble(MINIMUM_LAST_SEGMENT));
      eld.setOrthogonallyRouted(true);

    if (op.get(SORT_BY_SEX).equals(DO_NOT_SORT)) {
      layouter.setSortFamilyMembers(FamilyTreeLayouter.DO_NOT_SORT_BY_SEX);
    } else  if (op.get(SORT_BY_SEX).equals(FEMALE_LEFT)) {
      layouter.setSortFamilyMembers(FamilyTreeLayouter.FEMALE_FIRST);
    } else if (op.get(SORT_BY_SEX).equals(FEMALE_ALWAYS_LEFT)) {
      layouter.setSortFamilyMembers(FamilyTreeLayouter.FEMALE_ALWAYS_FIRST);
    } else if (op.get(SORT_BY_SEX).equals(MALE_LEFT)) {
      layouter.setSortFamilyMembers(FamilyTreeLayouter.MALE_FIRST);
    } else if (op.get(SORT_BY_SEX).equals(MALE_ALWAYS_LEFT)) {
      layouter.setSortFamilyMembers(FamilyTreeLayouter.MALE_ALWAYS_FIRST);
    }

//    if (op.getBool(USE_LEAN_LAYERS_LAYERER)) {
//      layouter.setAspect(-1);
//      layouter.setAspect(op.getDouble(ASPECT_RATIO));
//      layouter.setMaxIter(op.getInt(MAX_ITERATIONS));
////      lwtl.setPreferredLayerWidth(op.getInt(LAYER_WIDTH));
////      lwtl.setUseEnhancedDistribution(op.getBool(LAYER_ENHANCED));
////      ihl.setFromScratchLayerer(new OldLayererWrapper(lwtl));
//      SimplexNodePlacer snp = (SimplexNodePlacer)ihl.getNodePlacer();
//      snp.setHorizontalCompactionEnabled(op.getBool(HORIZONTAL_COMPACTION));
//      snp.setBreakLongSegmentsEnabled(op.getBool(BREAK_LONG_SEGMENTS));
//    } else {
//      layouter.setAspect(-1);
//    }

    /* Create the family data provider */
    DataProvider dpType = null;
    if(graph.getDataProvider(FamilyTreeLayouter.DP_KEY_FAMILY_TYPE) == null) {
      /* Create the family info if not already existing */
      dpType = new DataProviderAdapter() {
        public Object get(Object o) {
          NodeRealizer nr = graph.getRealizer((Node)o);
          Color nodeColor = nr.getFillColor();
          if (nodeColor != null && nodeColor.equals(op.get(MALE_COLOR))) {
            return FamilyTreeLayouter.TYPE_MALE;
          }
          if (nodeColor != null && nodeColor.equals(op.get(FEMALE_COLOR))) {
            return FamilyTreeLayouter.TYPE_FEMALE;
          }
          if (nodeColor != null && nodeColor.equals(op.get(FAMILY_COLOR))) {
            return FamilyTreeLayouter.TYPE_FAMILY;
          }
          return null;
        }
      };
      graph.addDataProvider(FamilyTreeLayouter.DP_KEY_FAMILY_TYPE, dpType);
    }


    launchLayouter(layouter, true);
    if (dpType != null) {
      graph.removeDataProvider(FamilyTreeLayouter.DP_KEY_FAMILY_TYPE);
    }
  }
}
