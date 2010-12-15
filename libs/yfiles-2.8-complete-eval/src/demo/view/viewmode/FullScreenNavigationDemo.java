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
package demo.view.viewmode;

import demo.view.DemoBase;
import y.view.Arrow;
import y.view.Graph2D;
import y.view.Graph2DView;
import y.view.Graph2DViewMouseWheelZoomListener;
import y.view.ImageNodeRealizer;
import y.view.NavigationComponent;
import y.view.NavigationMode;
import y.view.NodeRealizer;
import y.view.Overview;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.JViewport;
import javax.swing.KeyStroke;
import javax.swing.ScrollPaneConstants;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.DisplayMode;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.net.URL;

/**
 * This demo demonstrates the usage of {@link y.view.NavigationComponent} and {@link y.view.Overview}. Both controls
 * will be added to a glass pane the view provides {@link y.view.Graph2DView#getGlassPane()} and can be toggled during
 * runtime.
 * <p/>
 * Besides one can switch to a full screen mode and navigate through the graph view.
 *
 */
public class FullScreenNavigationDemo extends DemoBase {
  protected Icon overviewIcon, navigationIcon;
  private JComponent overview, navigationComponent;
  private JToolBar toolBar;

  public FullScreenNavigationDemo() {
    overviewIcon = createIcon("demo/view/viewmode/resource/overview_tool.png");
    navigationIcon = createIcon("demo/view/viewmode/resource/navigation_tool.png");

    //add some controls to the glass pane
    addGlassPaneComponents();

    //fill the toolbar
    fillToolBar();

    //load an initial graph
    loadGraph("resource/peopleNav_small.graphml");

    //set default edge arrow
    Graph2D graph = view.getGraph2D();
    graph.getDefaultEdgeRealizer().setArrow(Arrow.DELTA);

    //set a default node realizer (from the loaded graph
    NodeRealizer realizer = graph.getRealizer(graph.getNodeArray()[10]);
    if (realizer instanceof ImageNodeRealizer) {
      ImageNodeRealizer inr = new ImageNodeRealizer();
      inr.setImage(((ImageNodeRealizer) realizer).getImage());
      inr.setSize(48, 48);
      graph.setDefaultNodeRealizer(inr);
    }

    //focus some nodes in the graph
    view.focusView(1.1, new Point2D.Double(150, 750), false);
  }

  protected Action createLoadAction() {
    //Overridden method to disable the Load menu in the demo
    //The load action for other graphs makes no sense, because the overview window is wrong for other graphs.
    return null;
  }

  protected Action createSaveAction() {
    //Overridden method to disable the Save menu in the demo
    return null;
  }

  protected void registerViewModes() {
    view.addViewMode(new NavigationMode());
  }

  protected JToolBar createToolBar() {
    toolBar = new JToolBar();
    return toolBar;
  }

  protected Icon createIcon(String resourceName) {
    URL imageURL = ClassLoader.getSystemResource(resourceName);
    if (imageURL != null) {
      return new ImageIcon(imageURL);
    } else {
      throw new IllegalArgumentException(resourceName + " not found");
    }
  }

  private void addGlassPaneComponents() {
    //get the glass pane
    JPanel glassPane = view.getGlassPane();
    //set an according layout manager
    glassPane.setLayout(new BorderLayout());

    JPanel toolsPanel = new JPanel(new GridBagLayout());
    toolsPanel.setOpaque(false);
    toolsPanel.setBackground(null);
    toolsPanel.setBorder(BorderFactory.createEmptyBorder(16, 16, 0, 0));

    //create and add the overview to the tools panel
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.gridx = 0;
    gbc.anchor = GridBagConstraints.LINE_START;
    gbc.insets = new Insets(0, 0, 16, 0);
    overview = createOverview(view);
    toolsPanel.add(overview, gbc);

    //create and add the navigation component to the tools panel
    navigationComponent = createNavigationComponent(view, 20, 30);
    toolsPanel.add(navigationComponent, gbc);

    //add the toolspanel to the glass pane
    gbc.gridx = 0;
    gbc.gridy = 0;
    gbc.weightx = 1;
    gbc.weighty = 1;
    gbc.anchor = GridBagConstraints.FIRST_LINE_START;
    JViewport viewport = new JViewport();
    viewport.add(toolsPanel);
    viewport.setOpaque(false);
    viewport.setBackground(null);
    JPanel westPanel = new JPanel(new BorderLayout());
    westPanel.setOpaque(false);
    westPanel.setBackground(null);
    westPanel.add(viewport, BorderLayout.NORTH);
    glassPane.add(westPanel, BorderLayout.WEST);
  }

  private NavigationComponent createNavigationComponent(Graph2DView view, double scrollStepSize, int scrollTimerDelay) {
    //create the NavigationComponent itself
    final NavigationComponent navigation = new NavigationComponent(view);
    navigation.setScrollStepSize(scrollStepSize);
    //set the duration between scroll ticks
    navigation.putClientProperty("NavigationComponent.ScrollTimerDelay", new Integer(scrollTimerDelay));
    //set the initial duration until the first scroll tick is triggered
    navigation.putClientProperty("NavigationComponent.ScrollTimerInitialDelay", new Integer(scrollTimerDelay));
    //set a flag so that the fit content button will adjust the viewports in an animated fashion
    navigation.putClientProperty("NavigationComponent.AnimateFitContent", Boolean.TRUE);

    //add a mouse listener that will make a semi transparent background, as soon as the mouse enters this component
    navigation.setBackground(new Color(255, 255, 255, 0));
    MouseAdapter navigationToolListener = new MouseAdapter() {
      public void mouseEntered(MouseEvent e) {
        super.mouseEntered(e);
        Color background = navigation.getBackground();
        //add some semi transparent background
        navigation.setBackground(new Color(background.getRed(), background.getGreen(), background.getBlue(), 196));
      }

      public void mouseExited(MouseEvent e) {
        super.mouseExited(e);
        Color background = navigation.getBackground();
        //make the background completely transparent
        navigation.setBackground(new Color(background.getRed(), background.getGreen(), background.getBlue(), 0));
      }
    };
    navigation.addMouseListener(navigationToolListener);

    //add mouse listener to all sub components of the navigationComponent
    for (int i = 0; i < navigation.getComponents().length; i++) {
      Component component = navigation.getComponents()[i];
      component.addMouseListener(navigationToolListener);
    }

    return navigation;
  }

  private Overview createOverview(Graph2DView view) {
    Overview ov = new Overview(view);
    /* customize the overview */
    //animates the scrolling
    ov.putClientProperty("Overview.AnimateScrollTo", Boolean.TRUE);
    //blurs the part of the graph which can currently not be seen
    ov.putClientProperty("Overview.PaintStyle", "Funky");
    //allows zooming from within the overview
    ov.putClientProperty("Overview.AllowZooming", Boolean.TRUE);
    //provides functionality for navigation via keybord (zoom in (+), zoom out (-), navigation with arrow keys)
    ov.putClientProperty("Overview.AllowKeyboardNavigation", Boolean.TRUE);
    //determines how to differ between the part of the graph that can currently be seen, and the rest
    ov.putClientProperty("Overview.Inverse", Boolean.TRUE);
    ov.setPreferredSize(new Dimension(150, 150));
    ov.setMinimumSize(new Dimension(150, 150));

    ov.setBorder(BorderFactory.createEtchedBorder());
    return ov;
  }

  private void fillToolBar() {
    //create and add the overview button to the toolbar
    AbstractAction overviewAction = new ToggleComponentVisibilityAction(overview);
    overviewAction.putValue(Action.SMALL_ICON, overviewIcon);
    overviewAction.putValue(Action.SHORT_DESCRIPTION, "Toggle Overview");
    JToggleButton overviewButton = new JToggleButton(overviewAction);
    overviewButton.setSelected(true);
    toolBar.add(overviewButton);

    AbstractAction navigationControlsAction = new ToggleComponentVisibilityAction(navigationComponent);
    navigationControlsAction.putValue(Action.SMALL_ICON, navigationIcon);
    navigationControlsAction.putValue(Action.SHORT_DESCRIPTION, "Toggle Navigation Controls");
    JToggleButton navigationButton = new JToggleButton(navigationControlsAction);
    navigationButton.setSelected(true);
    toolBar.add(navigationButton);

    toolBar.addSeparator();

    //add the fullscreen action to the toolbar
    toolBar.add(new FullScreenAction(view.getGraph2D()));
  }


  /**
   * Launches this demo.
   *
   * @param args args
   */
  public static void main(String[] args) {
    EventQueue.invokeLater(new Runnable() {
      public void run() {
        initLnF();
        (new FullScreenNavigationDemo()).start("Full Screen Navigation Demo");
      }
    });
  }

  /** An action that will toggle the visibility of the given component. */
  static class ToggleComponentVisibilityAction extends AbstractAction {
    private final Component component;

    public ToggleComponentVisibilityAction(Component component) {
      super();
      this.component = component;
    }

    public void actionPerformed(ActionEvent e) {
      component.setVisible(!component.isVisible());
    }
  }

  /** displays the current graph in full screen mode. */
  class FullScreenAction extends AbstractAction {
    private Graph2DView view;
    private JFrame frame;
    private int scrollStepSize = 15;
    private int scrollTimerDelay = 5;
    private final Graph2D graph2D;
    private Icon closeIcon;

    /**
     * creates an instance.
     *
     * @param graph2D the graph this action is created for
     */
    public FullScreenAction(Graph2D graph2D) {
      super("Full Screen");
      closeIcon = createIcon("demo/view/viewmode/resource/close.png");

      this.putValue(Action.SMALL_ICON, createIcon("demo/view/viewmode/resource/fullscreen.png"));
      this.putValue(Action.SHORT_DESCRIPTION, "Fullscreen Mode");
      this.graph2D = graph2D;
    }

    /** @return the current step size for scrolling the graph with the navigation component in the full screen view. */
    public int getScrollStepSize() {
      return scrollStepSize;
    }

    /**
     * sets the step size for scrolling the graph with the navigation component in the full screen view.
     *
     * @param scrollStepSize the step size for scrolling
     */
    public void setScrollStepSize(int scrollStepSize) {
      this.scrollStepSize = scrollStepSize;
    }

    /** @return the current delay between two scroll events of the navigation component in the full screen view. */
    public int getScrollTimerDelay() {
      return scrollTimerDelay;
    }

    /**
     * sets the delay between two scroll events of the navigation component in the full screen view.
     *
     * @param scrollTimerDelay the delay in ms
     */
    public void setScrollTimerDelay(int scrollTimerDelay) {
      this.scrollTimerDelay = scrollTimerDelay;
    }

    private void showFrame() {
      frame.setVisible(true);
      view.fitContent();
    }

    private boolean addGraphView() {
      createGraphView();
      if (view != null) {
        frame.getRootPane().setContentPane(view);
        return true;
      } else {
        return false;
      }
    }

    private void addGlassPaneComponents() {
      //get the glass pane
      JPanel glassPane = view.getGlassPane();
      //set n according layout
      glassPane.setLayout(new GridBagLayout());

      JPanel toolsPanel = new JPanel(new GridBagLayout());
      toolsPanel.setOpaque(false);
      toolsPanel.setBackground(null);

      //creat the overview
      Overview overview = createOverview(view);
      //create the navigation component
      NavigationComponent navigationComponent = createNavigationComponent(view, scrollStepSize, scrollTimerDelay);

      //create the inner toolbar and add
      JPanel innerToolbar = createInnerToolbar(overview, navigationComponent);
      GridBagConstraints gbc = new GridBagConstraints();
      gbc.gridx = 0;
      gbc.anchor = GridBagConstraints.LINE_END;
      toolsPanel.add(innerToolbar, gbc);

      //add the overview
      gbc.gridy = 1;
      gbc.insets = new Insets(16, 0, 0, 0);
      toolsPanel.add(overview, gbc);

      //add the navigation component
      gbc.gridx = 0;
      gbc.gridy = 0;
      gbc.anchor = GridBagConstraints.FIRST_LINE_START;
      gbc.insets = new Insets(11, 11, 0, 0);
      navigationComponent.setPreferredSize(new Dimension((int) navigationComponent.getPreferredSize().getWidth(), 300));
      glassPane.add(navigationComponent, gbc);

      gbc.gridx = 0;
      gbc.gridy = 0;
      gbc.weightx = 1;
      gbc.weighty = 1;
      gbc.anchor = GridBagConstraints.FIRST_LINE_END;
      gbc.insets = new Insets(16, 0, 0, 16);
      glassPane.add(toolsPanel, gbc);
    }

    private JPanel createInnerToolbar(final Overview overview, final NavigationComponent navigationComponent) {
      GridBagConstraints gbc = new GridBagConstraints();
      Insets margin = new Insets(2, 2, 2, 2);

      JPanel headPanel = new JPanel(new GridBagLayout());
      headPanel.setBackground(Color.WHITE);

      //overview toggle
      final JToggleButton overviewButton = new JToggleButton(new ToggleComponentVisibilityAction(overview));
      overviewButton.setMargin(margin);
      overviewButton.setIcon(overviewIcon);
      overviewButton.setToolTipText("Toggle Overview");
      gbc.weightx = 0;
      gbc.anchor = GridBagConstraints.LINE_END;
      gbc.insets = new Insets(0, 1, 0, 1);
      headPanel.add(overviewButton, gbc);

      //navigation controls toggle
      final JToggleButton navigateButton = new JToggleButton(new ToggleComponentVisibilityAction(navigationComponent));
      navigateButton.setMargin(margin);
      navigateButton.setToolTipText("Toggle Navigation Controls");
      navigateButton.setIcon(navigationIcon);
      headPanel.add(navigateButton, gbc);

      //close button to leave fullscreen mode
      JButton closeButton = new JButton(new AbstractAction() {
        public void actionPerformed(ActionEvent e) {
          closeFrame();
        }
      });
      closeButton.setToolTipText("Leave Fullscreen Mode (ESC)");
      closeButton.setIcon(closeIcon);
      closeButton.setMargin(margin);
      headPanel.add(closeButton, gbc);

      return headPanel;
    }

    private void createFrame() {
      frame = new JFrame();
      frame.setResizable(false);
      if (!frame.isDisplayable()) {
        frame.setUndecorated(true);
      }
      frame.getContentPane().setLayout(new BorderLayout());
      GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
      DisplayMode mode = gd.getDisplayMode();
      frame.setSize(mode.getWidth(), mode.getHeight());
    }

    private void createGraphView() {
      view = new Graph2DView(graph2D);
      view.setAntialiasedPainting(true);
      view.getCanvasComponent().addMouseWheelListener(new Graph2DViewMouseWheelZoomListener());
      view.addViewMode(new NavigationMode());
      //view.addViewMode(new NavigationMode());
      view.setScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER,
          ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
    }

    private void addEscapeListener() {
      KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
      view.registerKeyboardAction(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          closeFrame();
        }
      }, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);
    }

    private void closeFrame() {
      if (frame != null) {
        frame.setVisible(false);
        frame.dispose();
      }
      if (view != null) {
        view.getGraph2D().removeView(view);
      }
      view = null;
      frame = null;
    }

    public void actionPerformed(ActionEvent e) {
      // Close former full screen, if it is still open.
      closeFrame();
      // Create new full screen.
      createFrame();
      // Add view for current graph.
      if (addGraphView()) {
        // If adding the view was successful, decorate it and show the full screen.
        addEscapeListener();
        addGlassPaneComponents();
        showFrame();
      }
    }
  }
}
