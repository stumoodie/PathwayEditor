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

import y.geom.YDimension;
import y.layout.ComponentLayouter;
import y.option.ConstraintManager;
import y.option.OptionHandler;

/**
 * This module represents an interactive configurator and launcher for
 * {@link y.layout.ComponentLayouter}.
 */
public class ComponentLayoutModule extends LayoutModule
{
  private static final String COMPONENTLAYOUTER = "COMPONENTLAYOUTER";
  private static final String STYLE = "STYLE";
  
  private static final String STYLE_NONE = "STYLE_NONE";
  private static final String STYLE_ROWS = "STYLE_ROWS";
  private static final String STYLE_SINGLE_ROW = "STYLE_SINGLE_ROW";
  private static final String STYLE_SINGLE_COLUMN = "STYLE_SINGLE_COLUMN";
  private static final String STYLE_PACKED_COMPACT_RECTANGLE = "STYLE_PACKED_COMPACT_RECTANGLE";
  private static final String STYLE_PACKED_RECTANGLE = "STYLE_PACKED_RECTANGLE";
  private static final String STYLE_PACKED_CIRCLE = "STYLE_PACKED_CIRCLE";
  private static final String STYLE_PACKED_COMPACT_CIRCLE = "STYLE_PACKED_COMPACT_CIRCLE";
  private static final String FROM_SKETCH = "FROM_SKETCH";
  private static final String NO_OVERLAP = "NO_OVERLAP";
  private static final String ASPECT_RATIO = "ASPECT_RATIO";
  private static final String USE_SCREEN_RATIO = "USE_SCREEN_RATIO";
  private static final String COMPONENT_SPACING = "COMPONENT_SPACING";
  private static final String GRID_SPACING = "GRID_SPACING";
  private static final String GRID_ENABLED = "GRID_ENABLED";

  // for the option handler
  private final static String[] styleEnum =
  { 
    STYLE_NONE, 
    STYLE_ROWS,
    STYLE_SINGLE_ROW,
    STYLE_SINGLE_COLUMN,
    STYLE_PACKED_RECTANGLE,
    STYLE_PACKED_COMPACT_RECTANGLE,
    STYLE_PACKED_CIRCLE,
    STYLE_PACKED_COMPACT_CIRCLE,
  };

  private ComponentLayouter layouter;
  
  public ComponentLayoutModule()
  {
    super (COMPONENTLAYOUTER,
           "yFiles Layout Team",
           "Wrapper for ComponentLayouter");
  }

  /**
   * Factory method. Responsible for creating and initializing
   * the OptionHandler for this module.
   */
  protected OptionHandler createOptionHandler()
  {
    if (layouter == null){
      createLayouter();
    }
    
    OptionHandler op = new OptionHandler(getModuleName());
    ConstraintManager cm = new ConstraintManager(op);
    
    op.addEnum(STYLE,styleEnum,
               layouter.getStyle() & ComponentLayouter.STYLE_MASK);
    op.addBool(NO_OVERLAP, (layouter.getStyle() & ComponentLayouter.STYLE_MODIFIER_NO_OVERLAP) != 0);
    op.addBool(FROM_SKETCH, (layouter.getStyle() & ComponentLayouter.STYLE_MODIFIER_AS_IS) != 0);
    YDimension size = layouter.getPreferredLayoutSize();
    op.addBool(USE_SCREEN_RATIO, true);
    op.addDouble(ASPECT_RATIO, size.width / size.height);
    cm.setEnabledOnValueEquals(USE_SCREEN_RATIO, Boolean.FALSE, ASPECT_RATIO);
    
    op.addDouble(COMPONENT_SPACING, layouter.getComponentSpacing(), 0.0d, 400.0d);
    op.addBool(GRID_ENABLED, layouter.getGridSpacing() > 0);
    op.addDouble(GRID_SPACING, layouter.getGridSpacing() > 0 ? layouter.getGridSpacing() : 20.0d);
    cm.setEnabledOnValueEquals(GRID_ENABLED, Boolean.TRUE, GRID_SPACING);
    
    return op;
  }

  /**
   * Module initialization routine. Typically this method is used to 
   * configure the underlying algorithm with the options found in the
   * options handler of this module.
   */
  protected void init()
  {
    createLayouter();
    
    OptionHandler op = getOptionHandler();

    layouter.setComponentArrangementEnabled(true);
    byte style = (byte) op.getEnum(STYLE);
    if (op.getBool(NO_OVERLAP)){
      style |= ComponentLayouter.STYLE_MODIFIER_NO_OVERLAP;
    }
    if (op.getBool(FROM_SKETCH)){
      style |= ComponentLayouter.STYLE_MODIFIER_AS_IS;
    }
    layouter.setStyle(style);
    double w, h;
    if (op.getBool(USE_SCREEN_RATIO) && getGraph2DView() != null) {
      w = getGraph2DView().getWidth();
      h = getGraph2DView().getHeight();
    } else {
      w = op.getDouble(ASPECT_RATIO);
      h = 1.0d/w;
      w *= 400.d;
      h *= 400.d;
    }
    layouter.setPreferredLayoutSize(w, h);
    layouter.setComponentSpacing(op.getDouble(COMPONENT_SPACING));
    if (op.getBool(GRID_ENABLED)){
      layouter.setGridSpacing(op.getDouble(GRID_SPACING));
    } else {
      layouter.setGridSpacing(0);
    }
  }
  
  /**
   * Main module execution routine. launches the hierarchic layouter.
   */
  protected void mainrun()
  {
    launchLayouter(layouter);
  }
  
  /**
   * clean up the module, clear temporarily bound data providers and
   * references to the wrapped algorithm.
   */
  protected void dispose()
  {
    layouter = null;
  }
  
  private void createLayouter()
  {
    if(layouter == null)
    {
      layouter = new ComponentLayouter();
    }
  }
}

