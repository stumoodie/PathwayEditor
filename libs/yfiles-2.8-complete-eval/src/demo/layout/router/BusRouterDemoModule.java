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
package demo.layout.router;

import y.layout.router.BusRouter;
import y.module.BusRouterModule;
import y.option.IntOptionItem;
import y.option.OptionGroup;
import y.option.OptionHandler;
import y.option.OptionItem;
import y.option.ResourceBundleGuiFactory;

import java.util.MissingResourceException;

/**
 * A modified {@link y.module.BusRouterModule} which omits the scope and the bus definition option which are not
 * applicable to {@link demo.layout.router.BusRouterDemo}.
 */
class BusRouterDemoModule extends BusRouterModule {

  private static final String GROUP_LAYOUT = "GROUP_LAYOUT";
  private static final String MIN_DISTANCE_TO_NODES = "MIN_DISTANCE_TO_NODES";
  private static final String MIN_DISTANCE_TO_EDGES = "MIN_DISTANCE_TO_EDGES";

  /**
   * Creates a new instance.
   */
  BusRouterDemoModule() {
    optionsLayout = false;
    optionsSelection = true;
    optionsRouting = true;
  }

  /**
   * Adds the option items used by this module to the given <code>OptionHandler</code>.
   *
   * @param oh the <code>OptionHandler</code> to add the items to
   */
  protected void addOptionItems(final OptionHandler oh) {
    OptionItem item;
    OptionGroup og;

    item = oh.addInt(MIN_DISTANCE_TO_NODES, 20);
    item.setAttribute(IntOptionItem.ATTRIBUTE_MIN_VALUE, new Integer(1));
    item = oh.addInt(MIN_DISTANCE_TO_EDGES, 10);
    item.setAttribute(IntOptionItem.ATTRIBUTE_MIN_VALUE, new Integer(1));

    og = new OptionGroup();
    og.setAttribute(OptionGroup.ATTRIBUTE_TITLE, GROUP_LAYOUT);
    og.addItem(oh.getItem(MIN_DISTANCE_TO_EDGES));
    og.addItem(oh.getItem(MIN_DISTANCE_TO_NODES));

    super.addOptionItems(oh);
  }

  protected BusRouter getBusRouter() {
    return super.getBusRouter();
  }

  /**
   * Creates an option handler for this module and adds to it its original and the demo's resource bundle.
   */
  protected OptionHandler createOptionHandler() {
    final OptionHandler oh = super.createOptionHandler();
    ResourceBundleGuiFactory gf = new ResourceBundleGuiFactory();
    try {
      gf.addBundle(BusRouterModule.class.getName());
      gf.addBundle(BusRouterDemo.class.getName());
    } catch (MissingResourceException mre) {
    }
    oh.setGuiFactory(gf);
    return oh;
  }

  /**
   * Configures an instance of {@link y.layout.router.BusRouter}. The values provided by this module's option handler
   * are being used for this purpose.
   *
   * @param busRouter the BusRouter to be configured.
   */
  public void configure(BusRouter busRouter) {
    super.configure(busRouter);
    final OptionHandler oh = getOptionHandler();

    busRouter.setMinimumDistanceToNode(oh.getInt(MIN_DISTANCE_TO_NODES));
    busRouter.setMinimumDistanceToEdge(oh.getInt(MIN_DISTANCE_TO_EDGES));
  }

  /**
   * Sets the option items of the given option handler to the settings of the given <code>BusRouter</code>.
   *
   * @param oh        the option handler
   * @param busRouter the bus router
   */
  protected void initOptionHandler(OptionHandler oh, BusRouter busRouter) {
    super.initOptionHandler(oh, busRouter);

    oh.set(MIN_DISTANCE_TO_NODES, new Integer(busRouter.getMinimumDistanceToNode()));
    oh.set(MIN_DISTANCE_TO_EDGES, new Integer(busRouter.getMinimumDistanceToEdge()));
  }

}
