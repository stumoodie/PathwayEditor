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
package demo.view.orgchart;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.DefaultTreeSelectionModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import demo.view.DemoDefaults;
import org.xml.sax.InputSource;

import demo.view.orgchart.OrgChartTreeModel.Employee;

import y.base.Node;
import y.base.NodeCursor;
import y.view.Graph2DSelectionEvent;
import y.view.Graph2DSelectionListener;
import y.view.Overview;

/**
 * This demo visualizes an organization chart. This comprehensive demo shows 
 * many aspects of yFiles. In particular it shows how to
 * <ul>
 * <li>visualize XML-formatted data as a graph</li>
 * <li>create a tree diagram from a {@link javax.swing.tree.TreeModel}</li>
 * <li>create customized node realizers that show text and multiple icons</li>
 * <li>create a customized {@link y.view.NavigationMode} view mode</li>
 * <li>create fancy roll-over effects when hovering over nodes</li> 
 * <li>implement level of detail (LoD) rendering of graphs</li>
 * <li>synchronize the selection state of {@link javax.swing.JTree} and {@link y.view.Graph2D}</li>
 * <li>customize {@link y.layout.tree.GenericTreeLayouter} to make it a perfect match for laying out organization charts</li>
 * <li>create local views for a large diagram</li>
 * <li>apply incremental layout and apply nice fade in and fade out effects to added or removed elements</li>
 * <li>implement and use keyboard navigation for {@link y.view.Graph2DView}</li>
 * </ul>
 * <p>
 * This demo is composed of multiple classes. Class {@link demo.view.orgchart.OrgChartDemo} is the organization chart application and driver class 
 * that organizes the UI elements and makes use of the Swing component {@link demo.view.orgchart.JOrgChart}. The model data used in this sample
 * is represented by the Swing tree model {@link demo.view.orgchart.OrgChartTreeModel}. Class {@link demo.view.orgchart.JOrgChart}
 * builds upon the more generic tree chart component {@link demo.view.orgchart.JTreeChart}. In a nutshell, JTreeChart 
 * visualizes a generic TreeModel and includes all the viewer logic, while JOrgChart visualizes a OrgChartTreeModel and 
 * customizes the look and feel of the component. Also it customizes the look and feel of the component to make it suitable for
 * organization charts.
 * </p> 
 */
public class OrgChartDemo {
  
  private JOrgChart orgChart;
  private JTable propertiesTable;
  private JTree tree;
    
  /**
   * Adds all UI elements of the application to a root pane container. 
   */
  public void addContentTo( final JRootPane rootPane ) {
    final JPanel contentPane = new JPanel(new BorderLayout());

    OrgChartTreeModel model = readOrgChart(getClass().getResource("resources/orgchartmodel.xml"));
    if (model != null) {
      Box leftPanel = new Box(BoxLayout.Y_AXIS);

      orgChart = createOrgChart(model);
      orgChart.setFitContentOnResize(true);

      Overview overview = orgChart.createOverview();
      leftPanel.add(createTitledPanel(overview, "Overview"));

      tree = createStructureView(model);
      JComponent viewOptions = createViewOptionsPanel();
      leftPanel.add(viewOptions, BorderLayout.NORTH);
      leftPanel.add(createTitledPanel(new JScrollPane(tree),"Structure View"));

      propertiesTable = createPropertiesTable();
      JScrollPane scrollPane = new JScrollPane(propertiesTable);
      scrollPane.setPreferredSize(new Dimension(200, 120));
      leftPanel.add(createTitledPanel(scrollPane,"Properties"));


      //sync app whenever orgchart selection changes occur
      orgChart.getGraph2D().addGraph2DSelectionListener(new OrgChartSelectionUpdater());
      //sync app whenever tree selection changes occur
      tree.getSelectionModel().addTreeSelectionListener(new TreeSelectionUpdater());
      //tree.addKeyListener(new TreeActionListener());

      contentPane.setLayout(new BorderLayout());
      contentPane.add(leftPanel, BorderLayout.WEST);

      JComponent helpPane = createHelpPane(getClass().getResource("resources/orgcharthelp.html"));
      helpPane.setMinimumSize(new Dimension(200,10));
      
      JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
          createTitledPanel(orgChart, "Organization Chart"),
          createTitledPanel(helpPane, "Help"));
      splitPane.setOneTouchExpandable(true);
      
      splitPane.setResizeWeight(1);

      contentPane.add(splitPane, BorderLayout.CENTER);
    } else {
      contentPane.setPreferredSize(new Dimension(320, 24));
      contentPane.add(new JLabel("Could not create Organization Chart.", JLabel.CENTER));
    }

    rootPane.setContentPane(contentPane);
  }

  /**
   * Starts the application in a JFrame.
   */
  private void start() {
    final JFrame frame = new JFrame("yFiles Organization Chart Demo");
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    addContentTo(frame.getRootPane());
    frame.pack();
    frame.setVisible(true);
    orgChart.requestFocus();
  }

  /**
   * Creates the application help pane.
   */
  JComponent createHelpPane(URL helpURL) {
    try {
      JEditorPane editorPane = new JEditorPane(helpURL);                
      editorPane.setEditable(false);
      editorPane.setPreferredSize(new Dimension(250, 250));
      return new JScrollPane(editorPane);
    } catch (IOException e) {
      e.printStackTrace();
    }
    return null;
  }

  /**
   * Reads and returns the OrgChartTreeModel from an XML file.
   */
  OrgChartTreeModel readOrgChart(URL orgChartURL) {
    OrgChartTreeModel model = null;
    try {
      InputStream stream = orgChartURL.openStream();
      model = OrgChartTreeModel.create(new InputSource(stream));    
      stream.close();      
    }catch(IOException ioex) {
      System.err.println("Failed to read from " + orgChartURL);
      ioex.printStackTrace();
    }
    return model;
  }
  
  /**
   * Creates a JOrgChart component for the given model.
   */
  JOrgChart createOrgChart(OrgChartTreeModel model) {    
    JOrgChart orgChart = new JOrgChart(model);
    orgChart.setPreferredSize(new Dimension(720, 750));
    addGlassPaneComponents(orgChart);
    return orgChart;
  }
  
  /**
   * Creates and returns a JTree-based structure view of the tree model.
   */
  JTree createStructureView(TreeModel model) {
    JTree tree = new JTree(model);      
    tree.setCellRenderer(new DefaultTreeCellRenderer() {
      public Component getTreeCellRendererComponent(
          JTree tree,Object value,boolean sel,boolean expanded,boolean leaf,int row, boolean hasFocus) {
        if(value instanceof Employee) {
            Employee employee = (Employee)value;
            value = employee.name;
        }
        return super.getTreeCellRendererComponent(
            tree, value, sel,
            expanded, leaf, row,
            hasFocus);            
      }
    });
    
    for(int i = 0; i < tree.getRowCount(); i++) {
      tree.expandPath(tree.getPathForRow(i));
    }

    return tree;
  }
  
  /**
   * Creates a JTable based properties view that displays the details of a selected model element.
   */
  JTable createPropertiesTable() {
    DefaultTableModel tm = new DefaultTableModel();
    tm.addColumn("", new Object[]{"Name", "Position", "Phone", "Fax", "Email", "Business Unit", "Status"});
    tm.addColumn("", new Object[]{"",     "",         "",       "",    ""    , ""             , ""});
    return new JTable(tm) {
      public boolean isCellEditable(int row, int column) {
        return false;
      }
    };
  }
  
  /**
   * Updates the properties table when being called.
   */
  void updatePropertiesTable(Employee e) {
    DefaultTableModel tm = (DefaultTableModel) propertiesTable.getModel();
    tm.setValueAt(e.name,             0, 1);
    tm.setValueAt(e.position,         1, 1);
    tm.setValueAt(e.phone,            2, 1);
    tm.setValueAt(e.fax,              3, 1);
    tm.setValueAt(e.email,            4, 1);
    tm.setValueAt(e.businessUnit,     5, 1);
    tm.setValueAt(e.status,           6, 1);   
  }
  
  /**
   * Create a panel that allows to configure view options.  
   */
  JPanel createViewOptionsPanel() {
    JPanel panel = new JPanel();
    panel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
    final JRadioButton button1 = new JRadioButton(new AbstractAction("Global View") {
      public void actionPerformed(ActionEvent e) {
        orgChart.showGlobalHierarchy();
      }      
    });
    button1.setSelected(true);
        
    final JRadioButton button2 = new JRadioButton(new AbstractAction("Local View") {
      public void actionPerformed(ActionEvent e) {
        orgChart.showLocalHierarchy(null);                
      }      
    });
        
    ButtonGroup bg = new ButtonGroup();
    bg.add(button1);
    bg.add(button2);
    panel.add(button1);
    panel.add(button2);

    final JCheckBox checkBox = new JCheckBox(new AbstractAction("Show Colleagues") {
      public void actionPerformed(ActionEvent e) {        
        boolean result = ((JCheckBox)e.getSource()).isSelected();
        if(result != orgChart.isSiblingViewEnabled()) {
          orgChart.setSiblingViewEnabled(result);
          orgChart.showLocalHierarchy(null);
        }        
      }      
    });
    checkBox.setEnabled(false);       
    panel.add(checkBox);

    final JCheckBox checkBox2 = new JCheckBox(new AbstractAction("Show Business Units") {
      public void actionPerformed(ActionEvent e) {        
        boolean result = ((JCheckBox)e.getSource()).isSelected();
        if(result != orgChart.isGroupViewEnabled()) {
          orgChart.setGroupViewEnabled(result);
          orgChart.updateChart();
        }        
      }      
    });          
    panel.add(checkBox2);
    
    button2.addChangeListener(new ChangeListener() {
      public void stateChanged(ChangeEvent e) {
        checkBox.setEnabled(button2.isSelected());                
      }      
    });
    
    panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
    
    return createTitledPanel(panel, "View Options");
    
  }
  
  /**
   * Adds some toolbar buttons on top of JOrgChart.
   */
  private void addGlassPaneComponents(JOrgChart orgChart ) {
    JPanel glassPane = orgChart.getGlassPane();
    glassPane.setLayout(new BorderLayout());
    
    JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT,5,0));
    bar.setOpaque(false);
    bar.setBorder(BorderFactory.createEmptyBorder(20,15,0,0));

    Action zoomIn = orgChart.createZoomInAction();
    zoomIn.putValue(AbstractAction.SMALL_ICON, new ImageIcon(getClass().getResource("resources/icons/zoomIn.png")));
    zoomIn.putValue(AbstractAction.SHORT_DESCRIPTION, "Zoom into Chart");
    bar.add(createButton(zoomIn));
    
    Action zoomOut = orgChart.createZoomOutAction();
    zoomOut.putValue(AbstractAction.SMALL_ICON, new ImageIcon(getClass().getResource("resources/icons/zoomOut.png")));    
    zoomOut.putValue(AbstractAction.SHORT_DESCRIPTION, "Zoom out of Chart");
    bar.add(createButton(zoomOut));
  
    Action fitContent = orgChart.createFitContentAction();
    fitContent.putValue(AbstractAction.SMALL_ICON, new ImageIcon(getClass().getResource("resources/icons/zoomFit.png")));    
    fitContent.putValue(AbstractAction.SHORT_DESCRIPTION, "Fit Chart into View");
    bar.add(createButton(fitContent));
    
    glassPane.add(bar, BorderLayout.NORTH);
  }

  /**
   * Creates a button for an action.
   */
  private JButton createButton(Action action) {
    JButton button = new JButton(action);
    button.setBackground(Color.WHITE);
    return button;
  }


  /**
   * A TreeSelectionListener that propagates selection changes to JOrgChart.
   */
  class TreeSelectionUpdater implements TreeSelectionListener {
    public void valueChanged(TreeSelectionEvent e) {
      TreePath path = e.getPath();
      Employee employee = (Employee) path.getLastPathComponent();
      Node node = orgChart.getNodeForUserObject(employee);
      
      if(orgChart.isLocalViewEnabled() && (node == null || node.getGraph() == null)) {
        orgChart.showLocalHierarchy(employee);
        node = orgChart.getNodeForUserObject(employee);
      }
      
      if(node != null) {
        if(e.isAddedPath()) {
          for(NodeCursor nc = orgChart.getGraph2D().selectedNodes(); nc.ok(); nc.next()) {
            if(nc.node() != node) {
              orgChart.getGraph2D().setSelected(nc.node(), false);
            }
          }
          if(!orgChart.getGraph2D().isSelected(node)) {
            orgChart.getGraph2D().setSelected(node, e.isAddedPath());        
            orgChart.focusNode(node);
          }
        }
      }
    }        
  }

  /**
   * A Graph2DSelectionListener that propagates selection changes to a JTree.
   */
  class OrgChartSelectionUpdater implements Graph2DSelectionListener {    
    public void onGraph2DSelectionEvent(Graph2DSelectionEvent e) {
      if(e.getSubject() instanceof Node) {
        Node node = (Node) e.getSubject();        
        Employee p = (Employee) orgChart.getUserObject(node);
        if(p != null) {
          syncTreeSelection(node);
          if(orgChart.getGraph2D().isSelected(node)) { 
            updatePropertiesTable(p);          
          }
        }
      }    
    }
    
    void syncTreeSelection(Node node) {
      
      DefaultTreeSelectionModel smodel = (DefaultTreeSelectionModel) tree.getSelectionModel();
      smodel.setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
          
      DefaultTreeModel model = (DefaultTreeModel) tree.getModel();     
      TreeNode treeNode = (TreeNode) orgChart.getTreeNode(node);      
      TreeNode[] pathToRoot = model.getPathToRoot(treeNode);
      TreePath path = new TreePath(pathToRoot);
       
      if(orgChart.getGraph2D().isSelected(node)) {
        smodel.addSelectionPath(path);
      } else  {
        smodel.removeSelectionPath(path);
      }
      tree.scrollPathToVisible(path);
    }
  }

  /**
   * Create a panel for a component and adds a title to it. 
   */
  public JPanel createTitledPanel(JComponent content, String title) {
    JPanel panel = new JPanel();
    JLabel label = new JLabel(title);
    label.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
    label.setBackground(new Color(231, 219,182));
    label.setOpaque(true);
    label.setForeground(Color.DARK_GRAY);
    label.setFont(label.getFont().deriveFont(Font.BOLD));
    label.setFont(label.getFont().deriveFont(13.0f));
    panel.setLayout(new BorderLayout());
    panel.add(label, BorderLayout.NORTH);
    panel.add(content, BorderLayout.CENTER);
    return panel;
  }
  
  /**
   * Main driver method.
   */
  public static void main(String[] args) {
    EventQueue.invokeLater(new Runnable() {
      public void run() {
        DemoDefaults.initLnF();
        (new OrgChartDemo()).start();
      }
    });
  }
}
