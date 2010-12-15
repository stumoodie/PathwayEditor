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
package demo.view.anim;

import y.io.GraphMLIOHandler;
import y.io.IOHandler;
import y.option.CompoundEditor;
import y.option.ConstraintManager;
import y.option.DefaultEditorFactory;
import y.option.Editor;
import y.option.EnumOptionItem;
import y.option.GuiFactory;
import y.option.ItemEditor;
import y.option.OptionGroup;
import y.option.OptionHandler;
import y.option.OptionItem;
import y.util.D;
import y.view.Graph2D;
import y.view.Graph2DView;
import y.view.ViewAnimationFactory;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.ListCellRenderer;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Provides the GUI and option handling for <code>AnimationEffectsDemo</code>.
 * Cannot be used for anything else.
 *
 * @see AnimationEffectsDemo
 */
abstract class AnimationEffectsDemoBase {
  static final byte NO_ANIM = (byte) 0;

  static final byte BLUR_IN = (byte) 11;
  static final byte FADE_IN = (byte) 12;
  static final byte IMPLODE = (byte) 13;
  static final byte WHIRL_IN = (byte) 14;

  static final byte EXTRACT = (byte) 15;

  static final byte BLUR_OUT = (byte) 21;
  static final byte FADE_OUT = (byte) 22;
  static final byte EXPLODE = (byte) 23;
  static final byte WHIRL_OUT = (byte) 24;

  static final byte RETRACT = (byte) 25;

  static final byte TRAVERSE_EDGE = (byte) 31;
  static final byte ZOOM = (byte) 32;
  static final byte MOVE_CAMERA = (byte) 33;
  static final byte MORPH = (byte) 34;
  static final byte RESIZE = (byte) 35;
  static final byte BLINK = (byte) 36;
  static final byte ANIMATED_LOAD = (byte) 37;
  static final byte ANIMATED_CLEAR = (byte) 38;


  static final String DEMO_NAME;

  static {
    String name = AnimationEffectsDemo.class.getName();
    name = name.substring(name.lastIndexOf('.') + 1);
    DEMO_NAME = name;
  }


  final Graph2DView view;
  final GuiFactory i18n;
  final OptionHandler oh;
  boolean compoundAction;

  private final boolean wantsRadioButtons;

  /**
   * Creates a new AnimationEffectsDemoBase.
   * @param i18n   localization data
   */
  protected AnimationEffectsDemoBase(
          final GuiFactory i18n,
          final boolean wantsRadioButtons
  ) {
    this.wantsRadioButtons = wantsRadioButtons;
    this.view = new Graph2DView();
    this.view.setFitContentOnResize(true);
    this.i18n = i18n;
    this.oh = createOptionHandler();
    this.compoundAction = false;
  }

  abstract void animate();

  void selectAllEdges() {
    final Graph2D graph = view.getGraph2D();
    graph.setSelected(graph.edges(), true);
  }

  void openGraph(final String resource) {
    final URL url = getClass().getResource(resource);
    if (url != null) {
      final Graph2D graph = view.getGraph2D();
      graph.clear();

      try {
        final String name = URLDecoder.decode(url.getFile(), "UTF-8");
        getIoHandler(name).read(graph, name);
      }
      catch (IOException ioe) {
        D.show(ioe);
      }

      view.fitContent();
    } else {
      final File file = new File(resource);
      if (file.exists()) {
        final Graph2D graph = view.getGraph2D();
        graph.clear();

        try {
          final String name = file.getAbsolutePath();
          getIoHandler(name).read(graph, name);
        }
        catch (IOException ioe) {
          D.show(ioe);
        }

        view.fitContent();
      } else {
        D.show(new Exception("Cannot locate file: " + resource));
      }
    }
  }

  void localizeAction(final Action action, final String key) {
    action.putValue(Action.NAME, i18n.getString(key));
    action.putValue(Action.SHORT_DESCRIPTION,
        i18n.getString(key + ".shortDescription"));
    final URL iconUrl =
        getClass().getResource(i18n.getString(key + ".smallIcon"));
    if (iconUrl != null) {
      action.putValue(Action.SMALL_ICON, new ImageIcon(iconUrl));
    }
  }


  /**
   * Creates an OptionHandler.
   */
  private OptionHandler createOptionHandler() {
    final OptionHandler optionHandler = new OptionHandler(DEMO_NAME);

    final ConstraintManager cm = new ConstraintManager(optionHandler);

    OptionGroup group;
    OptionGroup section;
    OptionItem item;


    final Byte noAnim = new Byte(NO_ANIM);


    optionHandler.useSection("elements");
    section = new OptionGroup();
    section.setAttribute(OptionGroup.ATTRIBUTE_TITLE, "elements");

    final Byte blurIn = new Byte(BLUR_IN);
    final Byte fadeIn = new Byte(FADE_IN);
    final Byte implode = new Byte(IMPLODE);
    final Byte whirlIn = new Byte(WHIRL_IN);

    final Byte blurOut = new Byte(BLUR_OUT);
    final Byte fadeOut = new Byte(FADE_OUT);
    final Byte explode = new Byte(EXPLODE);
    final Byte whirlOut = new Byte(WHIRL_OUT);

    final Byte extract = new Byte(EXTRACT);
    final Byte retract = new Byte(RETRACT);

    final ElementsRenderer elementsRenderer = new ElementsRenderer();
    item = optionHandler.addEnum("createNode", new Byte[]{noAnim, blurIn, fadeIn, implode, whirlIn}, 4);
    item.setAttribute(EnumOptionItem.ATTRIBUTE_RENDERER, elementsRenderer);
    section.addItem(item);
    item = optionHandler.addEnum("deleteNode", new Byte[]{noAnim, blurOut, fadeOut, explode, whirlOut}, 1);
    item.setAttribute(EnumOptionItem.ATTRIBUTE_RENDERER, elementsRenderer);
    section.addItem(item);
    item = optionHandler.addEnum("createEdge", new Byte[]{noAnim, blurIn, fadeIn, implode, extract}, 2);
    item.setAttribute(EnumOptionItem.ATTRIBUTE_RENDERER, elementsRenderer);
    section.addItem(item);
    item = optionHandler.addEnum("deleteEdge", new Byte[]{noAnim, blurOut, fadeOut, explode, retract}, 4);
    item.setAttribute(EnumOptionItem.ATTRIBUTE_RENDERER, elementsRenderer);
    section.addItem(item);

    optionHandler.useSection("misc");
    section = new OptionGroup();

    final Byte animatedClear = new Byte(ANIMATED_CLEAR);
    final Byte animatedLoad = new Byte(ANIMATED_LOAD);
    final Byte traverseEdge = new Byte(TRAVERSE_EDGE);
    final Byte zoom = new Byte(ZOOM);
    final Byte moveCamera = new Byte(MOVE_CAMERA);
    final Byte morph = new Byte(MORPH);
    final Byte resize = new Byte(RESIZE);
    final Byte blink = new Byte(BLINK);

    item = optionHandler.addEnum("animation", new Byte[]{
        animatedLoad, animatedClear,
        traverseEdge, zoom, moveCamera,
        morph, resize, blink
    }, 2);
    item.setAttribute(DefaultEditorFactory.ATTRIBUTE_CONTROLLER_ID,
        DEMO_NAME + "Base.animation");
    item.setAttribute(EnumOptionItem.ATTRIBUTE_RENDERER,
        new AnimationRenderer());
    item.setAttribute(DefaultEditorFactory.ATTRIBUTE_ENUM_STYLE,
        wantsRadioButtons
            ? DefaultEditorFactory.STYLE_RADIO_BUTTONS
            : DefaultEditorFactory.STYLE_COMBO_BOX);
    section.addItem(item);

    final OrderRenderer orderRenderer = new OrderRenderer();
    final Object[] order = {
        ViewAnimationFactory.LEFT_TO_RIGHT,
        ViewAnimationFactory.RIGHT_TO_LEFT,
        ViewAnimationFactory.CLOCKWISE,
        ViewAnimationFactory.COUNTER_CLOCKWISE
    };

    group = new OptionGroup();
    group.setAttribute(OptionGroup.ATTRIBUTE_TITLE, "misc_animation_properties");
    group.setAttribute(DefaultEditorFactory.ATTRIBUTE_CONTROLLER_ID,
        DEMO_NAME + "Base.animation");
    group.setAttribute(DefaultEditorFactory.ATTRIBUTE_CARD_ID, animatedLoad.toString());
    item = optionHandler.addEnum("animateLoad_graph", new String[]{"big", "small"}, 0);
    item.setAttribute(DefaultEditorFactory.ATTRIBUTE_ENUM_STYLE,
        DefaultEditorFactory.STYLE_RADIO_BUTTONS);
    group.addItem(item);
    item = optionHandler.addEnum("animateLoad_nodeOrder", order, 0);
    item.setAttribute(EnumOptionItem.ATTRIBUTE_RENDERER, orderRenderer);
    group.addItem(item);
    group.addItem(optionHandler.addBool("animateLoad_obeyEdgeDirection", false));
    group.addItem(optionHandler.addDouble("animateLoad_ratio", 0.15, 0.1, 1.0));
    cm.setEnabledOnValueEquals("animation", animatedLoad, group);

    group = new OptionGroup();
    group.setAttribute(OptionGroup.ATTRIBUTE_TITLE, "misc_animation_properties");
    group.setAttribute(DefaultEditorFactory.ATTRIBUTE_CONTROLLER_ID,
        DEMO_NAME + "Base.animation");
    group.setAttribute(DefaultEditorFactory.ATTRIBUTE_CARD_ID, animatedClear.toString());
    item = optionHandler.addEnum("animateClear_nodeOrder", order, 0);
    item.setAttribute(EnumOptionItem.ATTRIBUTE_RENDERER, orderRenderer);
    group.addItem(item);
    group.addItem(optionHandler.addBool("animateClear_obeyEdgeDirection", false));
    group.addItem(optionHandler.addDouble("animateClear_ratio", 0.15, 0.1, 1.0));
    cm.setEnabledOnValueEquals("animation", animatedClear, group);

    group = new OptionGroup();
    group.setAttribute(OptionGroup.ATTRIBUTE_TITLE, "misc_animation_properties");
    group.setAttribute(DefaultEditorFactory.ATTRIBUTE_CONTROLLER_ID,
        DEMO_NAME + "Base.animation");
    group.setAttribute(DefaultEditorFactory.ATTRIBUTE_CARD_ID, traverseEdge.toString());

    group.addItem(optionHandler.addColor("colorVisited", Color.RED, true, true, false, false));
    group.addItem(optionHandler.addColor("colorUnvisited", Color.BLACK, true, true, false, false));
    cm.setEnabledOnValueEquals("animation", traverseEdge, group);
    addItems(group, section);

    group = new OptionGroup();
    group.setAttribute(OptionGroup.ATTRIBUTE_TITLE, "misc_animation_properties");
    group.setAttribute(DefaultEditorFactory.ATTRIBUTE_CONTROLLER_ID,
        DEMO_NAME + "Base.animation");
    group.setAttribute(DefaultEditorFactory.ATTRIBUTE_CARD_ID, zoom.toString());

    group.addItem(optionHandler.addDouble("zoom_factor", 1.0, 0.1, 16.0));
    cm.setEnabledOnValueEquals("animation", zoom, group);

    group = new OptionGroup();
    group.setAttribute(OptionGroup.ATTRIBUTE_TITLE, "misc_animation_properties");
    group.setAttribute(DefaultEditorFactory.ATTRIBUTE_CONTROLLER_ID,
        DEMO_NAME + "Base.animation");
    group.setAttribute(DefaultEditorFactory.ATTRIBUTE_CARD_ID, morph.toString());

    group.addItem(optionHandler.addDouble("translateX", 50));
    group.addItem(optionHandler.addDouble("translateY", 50));
    group.addItem(optionHandler.addDouble("width", 100));
    group.addItem(optionHandler.addDouble("height", 25));
    group.addItem(optionHandler.addColor("fillColor", Color.RED, true, true, false, false));
    group.addItem(optionHandler.addColor("fillColor2", Color.GREEN, true, true, false, false));
    group.addItem(optionHandler.addColor("lineColor", Color.BLACK, true, true, false, false));
    cm.setEnabledOnValueEquals("animation", morph, group);
    addItems(group, section);

    group = new OptionGroup();
    group.setAttribute(OptionGroup.ATTRIBUTE_TITLE, "misc_animation_properties");
    group.setAttribute(DefaultEditorFactory.ATTRIBUTE_CONTROLLER_ID,
        DEMO_NAME + "Base.animation");
    group.setAttribute(DefaultEditorFactory.ATTRIBUTE_CARD_ID, resize.toString());

    group.addItem(optionHandler.addDouble("resize_width", 100));
    group.addItem(optionHandler.addDouble("resize_height", 100));
    cm.setEnabledOnValueEquals("animation", resize, group);
    addItems(group, section);

    group = new OptionGroup();
    group.setAttribute(OptionGroup.ATTRIBUTE_TITLE, "misc_animation_properties");
    group.setAttribute(DefaultEditorFactory.ATTRIBUTE_CONTROLLER_ID,
        DEMO_NAME + "Base.animation");
    group.setAttribute(DefaultEditorFactory.ATTRIBUTE_CARD_ID, blink.toString());

    group.addItem(optionHandler.addInt("repetitions", 1, 1, 25));
    cm.setEnabledOnValueEquals("animation", blink, group);
    addItems(group, section);

    optionHandler.useSection("global");
    optionHandler.addDouble("speed", 1.0, 0.25, 4.0, 2);

    return optionHandler;
  }

  private JComponent createControlPane() {
    final DefaultEditorFactory editorFactory = new DefaultEditorFactory();
    editorFactory.setGuiFactory(i18n);

    final Map attributes = new HashMap();
    final Editor editor = editorFactory.createEditor(oh, attributes);
    setAutoAdopt(true, editor);
    setAutoCommit(true, editor);

    final JPanel buttonPane = new JPanel(new FlowLayout(FlowLayout.LEADING));
    buttonPane.add(new JButton(createAnimateAction()));

    JPanel spacer = new JPanel();
    final JPanel pane = new JPanel(new GridBagLayout());

    int row = 0;

    final GridBagConstraints gbc = new GridBagConstraints();
    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.anchor = GridBagConstraints.WEST;

    {
      final CompoundEditor ce = (CompoundEditor) editor;
      for (int i = 0, n = ce.editorCount() - 2; i < n; ++i) {
        gbc.gridy = row++;
        pane.add(ce.getEditor(i).getComponent(), gbc);
      }
      // special case for last two editors:
      // misc section and animation speed setting
      {
        final GridBagConstraints compoundConstraints = new GridBagConstraints();
        final JPanel compoundPane = new JPanel(new GridBagLayout());
        compoundPane.setBorder(
            BorderFactory.createTitledBorder(
                i18n.getString(DEMO_NAME + ".GROUP.misc")));

        compoundConstraints.fill = GridBagConstraints.HORIZONTAL;
        compoundConstraints.anchor = GridBagConstraints.NORTHWEST;
        compoundConstraints.gridy = 0;
        compoundConstraints.gridwidth = 2;
        compoundConstraints.weightx = 1.0;
        compoundPane.add(ce.getEditor(ce.editorCount() - 2).getComponent(),
            compoundConstraints);

        compoundConstraints.fill = GridBagConstraints.BOTH;
        compoundConstraints.anchor = GridBagConstraints.WEST;
        compoundConstraints.gridy = 1;
        compoundConstraints.weighty = 1.0;
        compoundPane.add(spacer, compoundConstraints);

        compoundConstraints.gridy = 2;
        compoundConstraints.gridwidth = 1;
        compoundConstraints.anchor = GridBagConstraints.SOUTHWEST;
        compoundPane.add(buttonPane, compoundConstraints);

        compoundConstraints.gridx = 1;
        compoundPane.add(ce.getEditor(ce.editorCount() - 1).getComponent(),
            compoundConstraints);

        gbc.gridy = row++;
        pane.add(compoundPane, gbc);
      }
    }

    spacer = new JPanel();
    gbc.fill = GridBagConstraints.BOTH;
    gbc.gridy = row;
    gbc.weighty = 0.75;
    pane.add(spacer, gbc);

    return pane;
  }

  JComponent createContentPane() {
    final JPanel pane = new JPanel(new BorderLayout());
    pane.add(createControlPane(), BorderLayout.WEST);
    pane.add(view, BorderLayout.CENTER);
    return pane;
  }

  private Action createAnimateAction() {
    final Action action = new AbstractAction() {
      public void actionPerformed(final ActionEvent e) {
        animate();
      }
    };
    localizeAction(action, DEMO_NAME + ".action.Animate");

    return action;
  }

  private IOHandler getIoHandler(final String filename) {
    return new GraphMLIOHandler();
  }


  private static void addItems(final OptionGroup src, final OptionGroup tgt) {
    for (Iterator it = src.items(); it.hasNext();) {
      tgt.addItem((OptionItem) it.next());
    }
  }

  /**
   * Sets the <code>autoCommit</code> property to the specified value,
   * if the specified editor support setting said property.
   */
  private static void setAutoCommit(final boolean autoCommit,
                                    final Editor editor) {
    if (editor instanceof CompoundEditor) {
      for (Iterator it = ((CompoundEditor) editor).editors(); it.hasNext();) {
        setAutoCommit(autoCommit, (Editor) it.next());
      }
    }
    if (editor instanceof ItemEditor) {
      ((ItemEditor) editor).setAutoCommit(autoCommit);
    }
  }

  /**
   * Sets the <code>autoAdopt</code> property for all items of the specified
   * option handler.
   */
  private static void setAutoAdopt(final boolean autoAdopt,
                                   final Editor editor) {
    if (editor instanceof CompoundEditor) {
      for (Iterator it = ((CompoundEditor) editor).editors(); it.hasNext();) {
        setAutoAdopt(autoAdopt, (Editor) it.next());
      }
    }
    if (editor instanceof ItemEditor) {
      ((ItemEditor) editor).setAutoAdopt(autoAdopt);
    }
  }


  private abstract static class I18nRenderer
      implements ListCellRenderer, TableCellRenderer {
    private final DefaultListCellRenderer listDelegate;
    private final DefaultTableCellRenderer tableDelegate;

    protected I18nRenderer() {
      this.listDelegate = new DefaultListCellRenderer();
      this.tableDelegate = new DefaultTableCellRenderer();
    }

    public Component getListCellRendererComponent(final JList list,
                                                  final Object value,
                                                  final int index,
                                                  final boolean isSelected,
                                                  final boolean hasFocus) {
      return listDelegate.getListCellRendererComponent(list, toString(value),
          index,
          isSelected, hasFocus);
    }

    public Component getTableCellRendererComponent(final JTable table,
                                                   final Object value,
                                                   final boolean isSelected,
                                                   final boolean hasFocus,
                                                   final int row,
                                                   final int column) {
      return tableDelegate.getTableCellRendererComponent(table, toString(value),
          isSelected, hasFocus,
          row, column);
    }

    abstract String toString(final Object value);
  }

  private final class ElementsRenderer extends I18nRenderer {
    String toString(final Object animation) {
      switch (((Byte) animation).byteValue()) {
        case NO_ANIM:
          return i18n.getString(DEMO_NAME + ".elements.VALUE.noAnimation");
        case BLUR_IN:
          return i18n.getString(DEMO_NAME + ".elements.VALUE.blurIn");
        case FADE_IN:
          return i18n.getString(DEMO_NAME + ".elements.VALUE.fadeIn");
        case IMPLODE:
          return i18n.getString(DEMO_NAME + ".elements.VALUE.implode");
        case WHIRL_IN:
          return i18n.getString(DEMO_NAME + ".elements.VALUE.whirlIn");
        case EXTRACT:
          return i18n.getString(DEMO_NAME + ".elements.VALUE.extract");
        case BLUR_OUT:
          return i18n.getString(DEMO_NAME + ".elements.VALUE.blurOut");
        case FADE_OUT:
          return i18n.getString(DEMO_NAME + ".elements.VALUE.fadeOut");
        case EXPLODE:
          return i18n.getString(DEMO_NAME + ".elements.VALUE.explode");
        case WHIRL_OUT:
          return i18n.getString(DEMO_NAME + ".elements.VALUE.whirlOut");
        case RETRACT:
          return i18n.getString(DEMO_NAME + ".elements.VALUE.retract");
        default:
          // this should never happen
          return "";
      }
    }
  }

  private final class AnimationRenderer extends I18nRenderer {
    String toString(final Object animation) {
      switch (((Byte) animation).byteValue()) {
        case NO_ANIM:
          return i18n.getString(DEMO_NAME + ".misc.animation.VALUE.noAnimation");
        case TRAVERSE_EDGE:
          return i18n.getString(DEMO_NAME + ".misc.animation.VALUE.traverseEdge");
        case ZOOM:
          return i18n.getString(DEMO_NAME + ".misc.animation.VALUE.zoom");
        case MOVE_CAMERA:
          return i18n.getString(DEMO_NAME + ".misc.animation.VALUE.moveCamera");
        case MORPH:
          return i18n.getString(DEMO_NAME + ".misc.animation.VALUE.morph");
        case RESIZE:
          return i18n.getString(DEMO_NAME + ".misc.animation.VALUE.resize");
        case BLINK:
          return i18n.getString(DEMO_NAME + ".misc.animation.VALUE.blink");
        case ANIMATED_LOAD:
          return i18n.getString(DEMO_NAME + ".misc.animation.VALUE.animatedLoad");
        case ANIMATED_CLEAR:
          return i18n.getString(DEMO_NAME + ".misc.animation.VALUE.animatedClear");
        default:
          return i18n.getString(DEMO_NAME + ".misc.animation.VALUE.noAnimation");
      }
    }
  }

  private final class OrderRenderer extends I18nRenderer {
    String toString(final Object nodeOrder) {
      if (nodeOrder == ViewAnimationFactory.LEFT_TO_RIGHT) {
        return i18n.getString("ViewAnimationFactory.LEFT_TO_RIGHT");
      }
      if (nodeOrder == ViewAnimationFactory.RIGHT_TO_LEFT) {
        return i18n.getString("ViewAnimationFactory.RIGHT_TO_LEFT");
      }
      if (nodeOrder == ViewAnimationFactory.CLOCKWISE) {
        return i18n.getString("ViewAnimationFactory.CLOCKWISE");
      }
      if (nodeOrder == ViewAnimationFactory.COUNTER_CLOCKWISE) {
        return i18n.getString("ViewAnimationFactory.COUNTER_CLOCKWISE");
      }
      // this should never happen
      return null;
    }
  }
}
