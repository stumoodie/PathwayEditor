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
package demo;

import demo.browser.ConfigurationException;
import demo.browser.Demo;
import demo.browser.Displayable;
import demo.browser.Driver;
import demo.browser.DriverEventQueue;
import demo.browser.DriverFactory;
import demo.browser.DriverInstantiationException;
import demo.browser.DriverSecurityManager;
import demo.browser.ExceptionHandler;
import demo.browser.SyntaxMarker;
import demo.browser.XmlTreeBuilder;

import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.text.Document;
import javax.swing.text.html.HTMLDocument;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.security.Permission;
import java.util.Enumeration;

/**
 * This class is not meant to be viewed as source code demo, instead run the Ant build.xml file in the same directory.
 * This class serves as a launcher/browser for the various demos that are included with this distribution.
 * For these to run, make sure that the sources themselves or at least the *.xsl files, the *.graphml files, the *.gml files, the *.ygf
 * files, the *.xml files, and the *.properties files are visible in your classpath.
 * So please use the ant build file in order to run the demos, since the build file satisfies all of the above requirements.
 *
 */
public class DemoBrowser {
  static final String APPLICATION_ROOT = "com.yworks.demo.appRoot";
  private static final String PLAY_BUTTON = "/demo/browser/resource/start.png";
  private static final String STOP_BUTTON = "/demo/browser/resource/stop.png";
  private static final String EXEC_ICON = "/demo/browser/resource/Play12.gif";
  private static final String NOEXEC_ICON = "/demo/browser/resource/Noexec12.gif";
  private static final String CONFIGURATION = "condensed.xml";
  private static final String[] CONFIGURATION_PATHS = {
      "/",
      "/demo/",
      "/demo/browser/",
      "/demo/browser/resource"
  };
  private static final String DISPLAY_PANE_ID = "displayPane";
  private static final String ERROR_PANE_ID = "errorPane";
  private static final int DOCUMENTATION_TAB_INDEX = 0;
  private static final int SOURCE_TAB_INDEX = 1;

  private final DriverEventQueue eventQueue;
  private final ExecutionExceptionHandler exceptionHandler;
  private JRootPane driverPane;
  private JEditorPane documentationPane;
  private JEditorPane sourcePane;
  private JTextArea errorPane;
  private JPanel displayPane;
  private CardLayout displayLayout;
  private JFrame frame;

  private final SecurityManager backupSecurityManager;

  private DemoBrowser() {
    this.eventQueue = new DriverEventQueue();
    this.exceptionHandler = new ExecutionExceptionHandler();
    this.backupSecurityManager = System.getSecurityManager();
  }

  private void registerEventQueue() {
    Toolkit.getDefaultToolkit().getSystemEventQueue().push( eventQueue );
  }

  private void setVisible( final boolean visible ) {
    frame = new JFrame( "yFiles Demo Browser" );
    frame.setName( APPLICATION_ROOT );
    //reset Security Manager, to avoid catching of System.exit, when called from root
    frame.addWindowListener( new WindowAdapter() {
      public void windowClosing( WindowEvent e ) {
        if ( backupSecurityManager != null ) {
          System.setSecurityManager( backupSecurityManager );
        } else {
          //no security manager was set, just allow everything
          System.setSecurityManager( new SecurityManager() {
            public void checkPermission( final Permission perm ) {
            }
          }
          );
        }
        System.exit( 0 );
      }
    }

    );
    frame.setContentPane( createContentPane() );
    frame.pack();
    frame.setLocationRelativeTo( null );
    frame.setVisible( visible );
  }

  private boolean isRunning() {
    return eventQueue.getDriver() != null;
  }

  private void execute
      (
          final Demo demo ) {
    if ( demo == null ) {
      return;
    }

    final DriverFactory factory = new DriverFactory( frame, driverPane );
    factory.setExceptionHandler( exceptionHandler );
    try {
      final Driver driver = factory.createDriverForClass( demo.getQualifiedName() );
      eventQueue.setDriver( driver );
      driver.start();
    } catch ( DriverInstantiationException die ) {
      exceptionHandler.handleException( die );
    }
  }

  private void dispose() {
    if ( isRunning() ) {
      eventQueue.getDriver().dispose();
      eventQueue.setDriver( null );
    }
  }

  private JComponent createContentPane() {
    final Dimension preferredSizeOfDisplay = new Dimension( 600, 400 );

    driverPane = new JRootPane();
    driverPane.setPreferredSize( preferredSizeOfDisplay );
    documentationPane = new AntiAliasingEditorPane("text/html", "");
    sourcePane = new AntiAliasingEditorPane("text/html", "");
    documentationPane.setEditable( false );
    documentationPane.setFocusable( false );

    sourcePane.setEditable( false );
    sourcePane.setEnabled( true );

    errorPane = new JTextArea();
    errorPane.setEditable( false );
    errorPane.setFocusable( false );

    displayLayout = new CardLayout();
    displayPane = new JPanel( displayLayout );

    DefaultMutableTreeNode root = new DefaultMutableTreeNode();

    XmlTreeBuilder config = null;
    for ( int i = 0; i < CONFIGURATION_PATHS.length; ++i ) {
      config = XmlTreeBuilder.newInstance( CONFIGURATION_PATHS[i] + CONFIGURATION );
      if ( config != null ) {
        try {
          root = config.buildDemoTree();
          break;
        } catch ( ConfigurationException ce ) {
          ce.printStackTrace();
        }
      }
    }
    if ( config == null ) {
      System.err.println( "Could not locate resource: " + CONFIGURATION );
      System.exit( 1 );
    }

    final JTabbedPane tabbedPane = new JTabbedPane();
    tabbedPane.add( "Documentation", new JScrollPane( documentationPane ) );
    tabbedPane.add( "Source", new JScrollPane( sourcePane ) );
    tabbedPane.setTabPlacement(SwingConstants.BOTTOM);
    tabbedPane.setEnabledAt( DOCUMENTATION_TAB_INDEX, false );
    tabbedPane.setEnabledAt( SOURCE_TAB_INDEX, false );

    //turn on antialiasing for this component
    final JTree tree = new JTree( root ) {

      protected void paintComponent( final Graphics g ) {
        final Graphics2D gfx = ( Graphics2D ) g;
        final Object oldAAHint = gfx.getRenderingHint( RenderingHints.KEY_TEXT_ANTIALIASING );
        gfx.setRenderingHint( RenderingHints.KEY_TEXT_ANTIALIASING,
            RenderingHints.VALUE_TEXT_ANTIALIAS_ON );
        super.paintComponent( g );
        gfx.setRenderingHint( RenderingHints.KEY_TEXT_ANTIALIASING,
            oldAAHint != null
                ? oldAAHint
                : RenderingHints.VALUE_TEXT_ANTIALIAS_DEFAULT );
      }
    };
    tree.setCellRenderer( new DisplayableRenderer() );
    tree.setRootVisible( false );
    tree.setShowsRootHandles( true );
    tree.getSelectionModel().setSelectionMode( TreeSelectionModel.SINGLE_TREE_SELECTION );
    tree.setExpandsSelectedPaths( true );
    tree.expandRow( 0 );
    ToolTipManager.sharedInstance().registerComponent( tree );

    final JScrollPane treePane = new JScrollPane( tree );
    treePane.setPreferredSize( new Dimension( 200, 600 ) );

    final JSplitPane jsp =
        new JSplitPane( JSplitPane.VERTICAL_SPLIT, driverPane, tabbedPane );
    jsp.setOneTouchExpandable( true );
    jsp.setContinuousLayout( true );
    jsp.setDividerLocation( 0.5 );

    final JButton start = new JButton( "Start" );
    final URL startIconResource = getClass().getResource( PLAY_BUTTON );
    if ( startIconResource != null ) {
      start.setIcon( new ImageIcon( startIconResource ) );
      start.setMargin( new Insets( 0, 0, 0, 0 ) );

      // only delete the button text if the GIF was successfully read
      final Icon icon = start.getIcon();
      if ( icon != null && icon.getIconWidth() > 0 && icon.getIconHeight() > 0 ) {
        start.setText( "" );
      }
    }
    start.setToolTipText( "Starts the currently selected Demo." );
    start.setEnabled( false );

    final JButton stop = new JButton( "Stop" );
    final URL stopIconResource = getClass().getResource( STOP_BUTTON );
    if ( stopIconResource != null ) {
      stop.setIcon( new ImageIcon( stopIconResource ) );
      stop.setMargin( new Insets( 0, 0, 0, 0 ) );

      // only delete the button text if the GIF was successfully read
      final Icon icon = stop.getIcon();
      if ( icon != null && icon.getIconWidth() > 0 && icon.getIconHeight() > 0 ) {
        stop.setText( "" );
      }
    }
    stop.setToolTipText( "Stops the currently selected Demo." );
    stop.setEnabled( false );

    stop.addActionListener( new ActionListener() {
      public void actionPerformed( final ActionEvent e ) {
        dispose();
        final Displayable displayable = getSelectedDisplayable( tree );
        if ( !tree.isSelectionEmpty() &&
            displayable.isDemo() &&
            displayable.isExecutable() ) {
          start.setEnabled( true );
        }
        stop.setEnabled( false );
      }
    } );
    start.addActionListener( new ActionListener() {
      public void actionPerformed( final ActionEvent e ) {
        final Demo demo = getSelectedDemo( tree );
        if ( demo.isExecutable() ) {
          if ( !isRunning() ) {
            stop.setEnabled( true );
            start.setEnabled( false );
            execute( demo );
            if ( jsp.getDividerLocation() < 10 ) {
              jsp.setDividerLocation( 0.75 );
            }
          }
        }
      }
    } );

    final JToolBar toolBar = new JToolBar();
    toolBar.add( start );
    toolBar.add( stop );

    final JPanel controlPane = new JPanel( new BorderLayout() );
    controlPane.add( treePane, BorderLayout.CENTER );
    controlPane.add( toolBar, BorderLayout.NORTH );

    displayPane.add( new JScrollPane( errorPane ), ERROR_PANE_ID );
    displayPane.add( jsp, DISPLAY_PANE_ID );
    displayPane.setPreferredSize( preferredSizeOfDisplay );

    final Trigger trigger = new Trigger( start, stop, jsp, tabbedPane, tree );
    tree.addMouseListener( trigger );
    tree.addTreeSelectionListener( trigger );
    tabbedPane.addChangeListener( trigger );
    documentationPane.addHyperlinkListener( trigger );
    tree.setSelectionRow( 0 );

    return new JSplitPane( JSplitPane.HORIZONTAL_SPLIT, controlPane, displayPane );
  }

  final class ExecutionExceptionHandler implements ExceptionHandler {
    public void handleException( final Exception ex ) {
      if ( causedBySystemExit( ex ) ) {
        dispose();
      } else {
        final Throwable work;
        if ( ex instanceof InvocationTargetException ) {
          work = ex.getCause();
        } else {
          work = ex;
        }
        if ( errorPane != null ) {
          final StringWriter sw = new StringWriter();
          final PrintWriter pw = new PrintWriter( sw );
          work.printStackTrace( pw );
          print( sw.toString() );
        } else {
          work.printStackTrace();
        }
      }
    }

    private boolean causedBySystemExit( final Exception ex ) {
      for ( Throwable t = ex; t != null; t = t.getCause() ) {
        if ( t instanceof SecurityException &&
            DriverSecurityManager.HANDLE_EXIT_VM.equals( t.getMessage() ) ) {
          return true;
        }
      }
      return false;
    }

    private void print( final String s ) {
      if ( !EventQueue.isDispatchThread() ) {
        EventQueue.invokeLater( new Runnable() {
          private final String text = s;

          public void run() {
            errorPane.setText( text );
            displayLayout.show( displayPane, ERROR_PANE_ID );
          }
        } );
      } else {
        errorPane.setText( s );
        displayLayout.show( displayPane, ERROR_PANE_ID );
      }
    }
  }

  final class Trigger extends MouseAdapter
      implements ChangeListener, TreeSelectionListener, HyperlinkListener {
    private final JButton start;
    private final JButton stop;
    private final JSplitPane splitPane;
    private final JTabbedPane tabbedPane;
    private final JTree tree;
    private final DefaultMutableTreeNode root;
    private final String rootDirectory;

    Trigger( final JButton start,
             final JButton stop,
             final JSplitPane splitPane,
             final JTabbedPane tabbedPane,
             final JTree tree ) {
      this.start = start;
      this.stop = stop;
      this.splitPane = splitPane;
      this.tabbedPane = tabbedPane;
      this.tree = tree;
      this.root = ( DefaultMutableTreeNode ) tree.getModel().getRoot();

      String tmp = "";
      try {
        final Class c = DemoBrowser.this.getClass();
        final String cn = c.getName().replace( '.', '/' ) + ".class";
        final URL cu = c.getResource( '/' + cn );
        tmp = toFileString( cu );
        tmp = tmp.substring( 0, tmp.indexOf( cn ) );
      } catch ( Exception ex ) {
        // we'll lose the hyper link support
        tmp = "";
      }
      this.rootDirectory = tmp;
    }

    public void mouseClicked( final MouseEvent me ) {
      if ( SwingUtilities.isLeftMouseButton( me ) ) {
        if ( me.getClickCount() == 2 ) {
          if ( !tree.isSelectionEmpty() ) {
            displayLayout.show( displayPane, DISPLAY_PANE_ID );
            final Displayable displayable = getSelectedDisplayable( tree );
            if ( displayable.isDemo() && displayable.isExecutable() ) {
              tabbedPane.setEnabledAt( DOCUMENTATION_TAB_INDEX, true );
              tabbedPane.setEnabledAt( SOURCE_TAB_INDEX, true );
              if ( !isRunning() ) {
                stop.setEnabled( true );
                start.setEnabled( false );
                execute( getSelectedDemo( tree ) );
                if ( splitPane.getDividerLocation() < 10 ) {
                  splitPane.setDividerLocation( 0.75 );
                }
              }
            }
          }
        }
      } else if ( SwingUtilities.isRightMouseButton( me ) ) {
        final TreePath path = tree.getPathForLocation( me.getX(), me.getY() );
        if ( path != null ) {
          final DefaultMutableTreeNode root =
              ( DefaultMutableTreeNode ) path.getLastPathComponent();
          if ( root != null ) {
            final DefaultTreeModel model = ( DefaultTreeModel ) tree.getModel();
            final JPopupMenu pm = new JPopupMenu();
            pm.add( new AbstractAction( "Expand Children" ) {
              public void actionPerformed( final ActionEvent ae ) {
                for ( Enumeration en = root.children(); en.hasMoreElements(); ) {
                  final DefaultMutableTreeNode child =
                      ( DefaultMutableTreeNode ) en.nextElement();
                  final TreePath childPath = new TreePath( model.getPathToRoot( child ) );
                  tree.expandPath( childPath );
                }
                pm.setVisible( false );
              }
            } );
            pm.add( new AbstractAction( "Collapse Children" ) {
              public void actionPerformed( final ActionEvent ae ) {
                for ( Enumeration en = root.children(); en.hasMoreElements(); ) {
                  final DefaultMutableTreeNode child =
                      ( DefaultMutableTreeNode ) en.nextElement();
                  final TreePath childPath = new TreePath( model.getPathToRoot( child ) );
                  tree.collapsePath( childPath );
                }
                pm.setVisible( false );
              }
            } );
            pm.show( tree, me.getX(), me.getY() );
          }
        }
      }
    }

    public void valueChanged( final TreeSelectionEvent e ) {
      if ( !tree.isSelectionEmpty() ) {
        displayLayout.show( displayPane, DISPLAY_PANE_ID );

        final Displayable displayable = getSelectedDisplayable( tree );
        final boolean isDemo = displayable.isDemo();
        final boolean isExecutable = displayable.isExecutable();

        tabbedPane.setEnabledAt( DOCUMENTATION_TAB_INDEX, true );
        tabbedPane.setEnabledAt( SOURCE_TAB_INDEX, isDemo );

        final boolean isRunning = isRunning();
        stop.setEnabled( isRunning );
        start.setEnabled( isExecutable && !isRunning );

        final int selectedTab = tabbedPane.getSelectedIndex();
        if ( !isDemo && selectedTab != DOCUMENTATION_TAB_INDEX ) {
          tabbedPane.setSelectedIndex( DOCUMENTATION_TAB_INDEX );
        } else {
          dispose();
          if ( isRunning && isExecutable ) {
            stop.setEnabled( true );
            start.setEnabled( false );
            execute( ( Demo ) displayable );
            if ( splitPane.getDividerLocation() < 10 ) {
              splitPane.setDividerLocation( 0.75 );
            }
          }
          updateTabs();
        }
      } else {
        tabbedPane.setEnabledAt( DOCUMENTATION_TAB_INDEX, false );
        tabbedPane.setEnabledAt( SOURCE_TAB_INDEX, false );
        tabbedPane.setSelectedIndex( -1 );
        documentationPane.setText( "<html></html>" );
        sourcePane.setText( "" );
        errorPane.setText( "" );
        displayLayout.show( displayPane, ERROR_PANE_ID );
        start.setEnabled( false );
        stop.setEnabled( false );
      }
    }

    public void stateChanged( final ChangeEvent e ) {
      updateTabs();
    }

    public void hyperlinkUpdate( final HyperlinkEvent e ) {
      if ( HyperlinkEvent.EventType.ACTIVATED == e.getEventType() ) {
        URL target = e.getURL();
        if ( target != null ) {
          URL base = ( ( HTMLDocument ) ( ( JEditorPane ) e.getSource() ).getDocument() ).getBase();
          try {
            String baseStr = base.toString();
            if ( !baseStr.endsWith( "/" ) ) baseStr += "/";
            target = new URL( baseStr + e.getDescription() );
          } catch ( MalformedURLException mux ) {
          }

          String qn = target.getPath();
          if ( qn.toLowerCase().endsWith( "/readme.html" ) ) {
            qn = qn.substring( 0, qn.length() - 12 );
          } else if ( qn.toLowerCase().endsWith( ".java" ) ) {
            qn = qn.substring( 0, qn.length() - 5 );
          }
          qn = qn.replace( '/', '.' );

          int i = qn.indexOf( "demo." );
          if ( i > 0 ) qn = qn.substring( i, qn.length() );

          if ( qn.length() > 0 ) {
            final DefaultMutableTreeNode refNode = find( qn, root );
            if ( refNode != null ) {
              final TreePath path = new TreePath( refNode.getPath() );
              tree.setSelectionPath( path );
            }
          }
        }
      }
    }

    private DefaultMutableTreeNode find( final String uoqn,
                                         final DefaultMutableTreeNode node ) {
      final Displayable userObject = ( Displayable ) node.getUserObject();
      if ( userObject != null && uoqn.equals( userObject.getQualifiedName() ) ) {
        return node;
      }

      if ( node.getChildCount() > 0 ) {
        DefaultMutableTreeNode result = null;
        for ( Enumeration en = node.children(); en.hasMoreElements(); ) {
          result = find( uoqn, ( DefaultMutableTreeNode ) en.nextElement() );
          if ( result != null ) {
            return result;
          }
        }
        return result;
      } else {
        return null;
      }
    }

    private void updateTabs() {
      if ( tree.isSelectionEmpty() ) {
        return;
      }

      switch ( tabbedPane.getSelectedIndex() ) {
        case DOCUMENTATION_TAB_INDEX: {
          setDescription( getSelectedDisplayable( tree ), documentationPane );
          break;
        }
        case SOURCE_TAB_INDEX: {
          setSource( getSelectedDemo( tree ), sourcePane );
          break;
        }
      }
    }

    private String toFileString( final URL url ) {
      String tmp = "";
      try {
        tmp = URLDecoder.decode( url.getFile(), "UTF-8" );
      } catch ( UnsupportedEncodingException uee ) {
        tmp = "";
      }
      return tmp;
    }
  }

  public static void main( final String[] args ) {
    EventQueue.invokeLater( new Runnable() {
      public void run() {
        initLnF();
        createAndShowGUI();
      }
    } );
  }

  /**
   * Initializes to a "nice" look and feel for GUI demo applications.
   */
  public static void initLnF() {
    try {
      // check for 'os.name == Windows 7' does not work, since JDK 1.4 uses the compatibility mode
      if (!"com.sun.java.swing.plaf.motif.MotifLookAndFeel".equals(UIManager.getSystemLookAndFeelClassName())
          && !"com.sun.java.swing.plaf.gtk.GTKLookAndFeel".equals(UIManager.getSystemLookAndFeelClassName())
          && !UIManager.getSystemLookAndFeelClassName().equals(UIManager.getLookAndFeel().getClass().getName())
          && !(System.getProperty("java.version").startsWith("1.4") && System.getProperty("os.name").startsWith(
          "Windows") && "6.1".equals(System.getProperty("os.version")))) {
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private static void createAndShowGUI() {
    final DemoBrowser browser = new DemoBrowser();
    browser.registerEventQueue();
    browser.setVisible( true );
  }

  private static void setSource( final Demo demo, final JEditorPane pane ) {
    if ( demo != null ) {
      String source = demo.getSource();
      if ( source == null ) {
        source = demo.readSource();
        source = SyntaxMarker.toHtml( source );
      }
      pane.setText( source );
    } else {
      pane.setText( "" );
    }
    pane.setCaretPosition( 0 );
  }

  private static void setDescription( final Displayable displayable, final JEditorPane pane ) {
    String desc = "<html></html>";
    if ( displayable != null ) {
      desc = displayable.getDescription();
      final Document doc = pane.getDocument();
      if ( doc instanceof HTMLDocument ) {
        final URL base = displayable.getBase();
        if ( base != null ) {
          ( ( HTMLDocument ) doc ).setBase( base );
        }
      }
    }
    pane.setText( desc );
    pane.setCaretPosition( 0 );
  }

  private static Demo getSelectedDemo( final JTree tree ) {
    final Object uo = ( ( DefaultMutableTreeNode ) tree.getSelectionPath()
        .getLastPathComponent() )
        .getUserObject();
    if ( uo instanceof Demo ) {
      return ( Demo ) uo;
    } else {
      return null;
    }
  }

  private static Displayable getSelectedDisplayable( final JTree tree ) {
    return ( Displayable ) ( ( DefaultMutableTreeNode ) tree.getSelectionPath()
        .getLastPathComponent() )
        .getUserObject();
  }


  private static final class DisplayableRenderer implements TreeCellRenderer {
    private final DefaultTreeCellRenderer delegate;
    private final Color defaultBackgroundSelectionColor;
    private final Color defaultBorderSelectionColor;
    private final Color defaultTextNonSelectionColor;
    private final Color defaultTextSelectionColor;
    private static final Font defaultFont = new Font( "SansSerif", Font.PLAIN, 12 );
    private static final Font boldFont = new Font( "SansSerif", Font.BOLD, 12 );
    private static final Font italicFont = new Font( "SansSerif", Font.ITALIC, 12 );

    DisplayableRenderer() {
      this.delegate = new DefaultTreeCellRenderer();
      this.delegate.setIcon( null );
      this.delegate.setOpenIcon( null );
      this.delegate.setLeafIcon( null );
      this.delegate.setClosedIcon( null );
      this.defaultBackgroundSelectionColor = delegate.getBackgroundSelectionColor();
      this.defaultBorderSelectionColor = delegate.getBorderSelectionColor();
      this.defaultTextNonSelectionColor = delegate.getTextNonSelectionColor();
      this.defaultTextSelectionColor = delegate.getTextSelectionColor();
//
//      this.executableColor = Color.GREEN.darker().darker();
//      this.nonExecutableColor = Color.RED.darker();

    }

    public Component getTreeCellRendererComponent( final JTree tree,
                                                   Object value,
                                                   final boolean selected,
                                                   final boolean expanded,
                                                   final boolean leaf,
                                                   final int row,
                                                   final boolean hasFocus ) {
      delegate.setBackgroundSelectionColor( defaultBackgroundSelectionColor );
      delegate.setBorderSelectionColor( defaultBorderSelectionColor );
      delegate.setTextNonSelectionColor( defaultTextNonSelectionColor );
      delegate.setTextSelectionColor( defaultTextSelectionColor );

      //try to get icon for executable demos
      final URL execIconResource = getClass().getResource( EXEC_ICON );
      final URL noexecIconResource = getClass().getResource( NOEXEC_ICON );
      ImageIcon execIcon = null;
      if ( execIconResource != null ) {
        execIcon = new ImageIcon( execIconResource );
      }

      ImageIcon noexecIcon = null;
      if ( noexecIconResource != null ) {
        noexecIcon = new ImageIcon( noexecIconResource );
      }

      //catch all cases not handled below (folder nodes etc.)
      if ( !leaf ) {
        delegate.setFont( boldFont );
      } else {
        delegate.setFont( italicFont );
      }

      delegate.setLeafIcon( null );

      if ( value instanceof DefaultMutableTreeNode ) {
        value = ( ( DefaultMutableTreeNode ) value ).getUserObject();
      }
      if ( value instanceof Displayable ) {
        final Displayable displayable = ( Displayable ) value;
        value = displayable.getDisplayName();
        delegate.setToolTipText( displayable.getSummary() );
        if ( displayable.isDemo() ) {
          if ( displayable.isExecutable() ) {
            //this is always safe, even for null icons
            delegate.setLeafIcon( execIcon );
            delegate.setFont( defaultFont );

          } else {
            delegate.setLeafIcon( noexecIcon );
            delegate.setFont( italicFont );
          }
        }
      }
      return delegate.getTreeCellRendererComponent( tree, value, selected,
          expanded, leaf, row,
          hasFocus );
    }
  }

  private static final class AntiAliasingEditorPane extends JEditorPane {

    private final boolean useDefaultAA;

    AntiAliasingEditorPane(final String type, final String text) {
      super(type, text);
      final String javaVersion = System.getProperty("java.version");
      final double version = Double.parseDouble(javaVersion.substring(0, 3));
      useDefaultAA = version > 1.599;
    }

    protected void paintComponent(final Graphics g) {
      if (useDefaultAA) {
        super.paintComponent(g);
      } else {
        final Graphics2D gfx = (Graphics2D) g;
        final Object oldAAHint = gfx.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
        gfx.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
            RenderingHints.VALUE_ANTIALIAS_ON);
        super.paintComponent(g);
        gfx.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
            oldAAHint != null
                ? oldAAHint
                : RenderingHints.VALUE_ANTIALIAS_DEFAULT);
      }
    }
  }
}