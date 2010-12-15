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
package demo.view.realizer;

import demo.view.DemoBase;
import y.base.DataProvider;
import y.base.Node;
import y.util.DataProviderAdapter;
import y.view.CellEditorMode;
import y.view.EditMode;
import y.view.GenericNodeRealizer;
import y.view.Graph2DView;
import y.view.NodeCellEditor;
import y.view.NodeCellRenderer;
import y.view.NodeCellRendererPainter;
import y.view.NodeRealizer;
import y.view.ShapeNodeRealizer;
import y.view.SimpleUserDataHandler;

import javax.swing.AbstractCellEditor;
import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Map;


/**
 * This demo shows how yFiles can deal with Swing-like cell rendering and cell editing mechanisms.
 * It shows both how to customize {@link GenericNodeRealizer} to display JComponents as nodes, and
 * how to configure {@link y.view.EditMode} to work with {@link CellEditorMode} so that a double click
 * on a node initiates inline cell editing.
 */
public class SwingRendererDemo extends DemoBase
{
  private GenericNodeRealizer gnr;
  private ShapeNodeRealizer snr = new ShapeNodeRealizer();

  /**
   * Instantiates this demo.
   */
  public SwingRendererDemo()
  {
    // create a simple NodeCellRenderer and NodeCellEditor instance that work together nicely
    NodeCellRenderer simpleNodeCellRenderer = new SimpleNodeCellRenderer();

    // Get the factory to register custom styles/configurations.
    GenericNodeRealizer.Factory factory = GenericNodeRealizer.getFactory();

    // prepare a GenericNodeRealizer to use the NodeCellRenderer for rendering
    Map map = factory.createDefaultConfigurationMap();
    map.put(GenericNodeRealizer.Painter.class, new NodeCellRendererPainter(simpleNodeCellRenderer, NodeCellRendererPainter.USER_DATA_MAP));
    map.put(GenericNodeRealizer.UserDataHandler.class, new SimpleUserDataHandler(SimpleUserDataHandler.REFERENCE_ON_FAILURE));
    // register the configuration using the given name
    factory.addConfiguration("JTextField", map);

    // create another configuration based on the first one, this time use a more complex renderer
    map.put(GenericNodeRealizer.Painter.class, new NodeCellRendererPainter(new ComplexNodeCellRenderer(), NodeCellRendererPainter.USER_DATA_MAP));
    // register it
    factory.addConfiguration("JTable", map);

    // instantiate a default node realizer
    gnr = new GenericNodeRealizer();
    gnr.setSize(200.0, 50.0);
    gnr.setConfiguration("JTextField");
    gnr.setUserData("Hello Renderer World!");

    // create a sample instance
    view.getGraph2D().setDefaultNodeRealizer(gnr);
    view.getGraph2D().createNode(150.0, 50.0, 200.0, 50.0, "");

    // and another one of the other kind
    gnr.setConfiguration("JTable");
    view.getGraph2D().createNode(150.0, 200.0, 150.0, 150.0, "");

  }

  /**
   * Adds the view modes to the view.
   * This implementation adds a new EditMode (with showNodeTips enabled) and
   * a new {@link y.view.AutoDragViewMode}.
   */
  protected void registerViewModes() {
    final NodeCellEditor simpleNodeCellEditor = new SimpleNodeCellEditor();
    // instantiate an appropriate editor for the complex renderer
    final NodeCellEditor complexNodeCellEditor = new SwingRendererDemo.ComplexNodeCellEditor();

    // create a data provider that dynamically switches between the different NodeCellEditor instances
    DataProvider nodeCellEditorProvider = new DataProviderAdapter() {
      public Object get(Object dataHolder) {
        NodeRealizer realizer = view.getGraph2D().getRealizer((Node) dataHolder);
        if (realizer instanceof GenericNodeRealizer){
          if ("JTextField".equals(((GenericNodeRealizer) realizer).getConfiguration())){
            return simpleNodeCellEditor;
          } else {
            return complexNodeCellEditor;
          }
        } else {
          return null;
        }
      }
    };

    EditMode editMode = new EditMode();
    // create the CellEditorMode and give it the multiplexing NodeCellEditor provider,
    // as well as tell it where to find the user data
    CellEditorMode cellEditorMode = new CellEditorMode(nodeCellEditorProvider, NodeCellRendererPainter.USER_DATA_MAP);
    // register it with the EditMode
    editMode.setEditNodeMode(cellEditorMode);
    // Disable generic node label assignment in the view since it would spoil the
    // effect of the node cell editors/renderers.
    editMode.assignNodeLabel(false);


    view.addViewMode( editMode );
  }

  /** Creates a toolbar that allows to switch the default node realizer type. */
  protected JToolBar createToolBar()
  {
    JToolBar retValue;

    retValue = super.createToolBar();
    final JComboBox cb = new JComboBox(new Object[]{"JTextField", "JTable", "Rectangle"});
    cb.setMaximumSize(new Dimension(200, 100));
    cb.setSelectedIndex(1);
    retValue.add(cb);
    cb.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent ae)
      {
        if ( !"Rectangle".equals( cb.getSelectedItem().toString() ) ) {
          gnr.setConfiguration( cb.getSelectedItem().toString() );
          view.getGraph2D().setDefaultNodeRealizer( gnr );
        } else {
          view.getGraph2D().setDefaultNodeRealizer( snr );
        }
      }
    });

    return retValue;
  }

  /**
   * A simple {@link NodeCellEditor} implementation that is based on an even simpler
   * {@link NodeCellRenderer} implementation.
   */
  public static class SimpleNodeCellEditor extends AbstractCellEditor implements NodeCellEditor
  {
    // the delegate
    private final SimpleNodeCellRenderer ncr;

    public SimpleNodeCellEditor()
    {
      // initialize
      this.ncr = new SimpleNodeCellRenderer();
      // add editor hooks
      this.ncr.tf.addActionListener(new ActionListener()
      {
        public void actionPerformed(ActionEvent ae)
        {
          SimpleNodeCellEditor.this.fireEditingStopped();
        }
      });
      this.ncr.tf.addKeyListener(new KeyAdapter()
      {
        public void keyPressed(KeyEvent ke)
        {
          if (ke.getKeyCode() ==  KeyEvent.VK_ESCAPE)
          {
            SimpleNodeCellEditor.this.fireEditingCanceled();
          }
        }
      });
    }

    public JComponent getNodeCellEditorComponent(Graph2DView view, NodeRealizer context, Object value, boolean isSelected)
    {
      // get the renderer as editor
      return ncr.getNodeCellRendererComponent(view, context, value, isSelected);
    }

    public Object getCellEditorValue()
    {
      // get the value this editor represents
      return ncr.getValue();
    }
  }

  /**
   * A simple NodeCellRenderer that uses a JTextField and a JLabel in a JPanel to display the nodes contents.
   */
  public static final class SimpleNodeCellRenderer extends JPanel implements NodeCellRenderer
  {
    /**
     * the text field that holds/displays the actual data
     */
    JTextField tf;

    public SimpleNodeCellRenderer()
    {
      super(new BorderLayout());
      // create a nice GUI
      setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(3,3,3,3), BorderFactory.createEtchedBorder()));
      add(new JLabel("Content"), BorderLayout.NORTH);
      add(tf = new JTextField(), BorderLayout.CENTER);
    }

    public JComponent getNodeCellRendererComponent(Graph2DView view, NodeRealizer nodeRealizer, Object userObject, boolean selected)
    {
      // initialize the text field
      tf.setText(String.valueOf(userObject));
      return this;
    }

    public Object getValue()
    {
      // return the value of the text field
      return tf.getText();
    }
  }

  /**
   * A more sophisticated NodeCellEditor that uses a sophisticated NodeCellRenderer to
   * display/edit a node.
   * This implementation displays an editable JTable where the value column is editable.
   */
  public static class ComplexNodeCellEditor extends AbstractCellEditor implements NodeCellEditor
  {
    // the delegate
    private final ComplexNodeCellRenderer ncr;

    public ComplexNodeCellEditor()
    {
      this.ncr = new ComplexNodeCellRenderer();
      // add editor hooks
      this.ncr.table.addPropertyChangeListener("tableCellEditor", new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent evt) {
          if (evt.getNewValue() == null && evt.getOldValue() != null){
            ComplexNodeCellEditor.this.fireEditingStopped();
          }
        }
      });
    }

    /**
     * Delegates the request to the table.
     */
    public boolean stopCellEditing() {
      if (ncr.table.isEditing() && ncr.table.getCellEditor() != null){
        return ncr.table.getCellEditor().stopCellEditing();
      } else {
        fireEditingStopped();
        return true;
      }
    }

    /**
     * Delegates the request to the table.
     */
    public void cancelCellEditing() {
      if (ncr.table.isEditing() && ncr.table.getCellEditor() != null){
        ncr.table.getCellEditor().cancelCellEditing();
      } else {
        fireEditingCanceled();
      }
    }

    public JComponent getNodeCellEditorComponent(Graph2DView view, NodeRealizer context, Object value, boolean isSelected)
    {
      ncr.getNodeCellRendererComponent(view, context, value, isSelected);
      return ncr;
    }

    public Object getCellEditorValue()
    {
      return ncr.getValue();
    }
  }

  /**
   * A nice renderer that can be used to display data in a JTable
   */
  public static final class ComplexNodeCellRenderer extends JPanel implements NodeCellRenderer
  {
    // the table
    JTable table;
    // the data model
    DefaultTableModel tableModel;

    public ComplexNodeCellRenderer()
    {
      super(new BorderLayout());

      // create a sample table model with the first column being editable
      tableModel = new DefaultTableModel(new Object[][]{{"Keys", "Values"}}, new Object[]{"Key", "Value"}) {
        public boolean isCellEditable(int row, int column) {
          return column == 1;
        }
      };

      setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(3,3,3,3), BorderFactory.createEtchedBorder()));
      add(table = new JTable(tableModel), BorderLayout.CENTER);
      add(table.getTableHeader(), BorderLayout.NORTH);
    }

    public JComponent getNodeCellRendererComponent(Graph2DView view, NodeRealizer nodeRealizer, Object userObject, boolean selected)
    {
      // initialize the value in the model
      tableModel.setValueAt(userObject, 0, 1);
      return this;
    }

    public Object getValue()
    {
      // construct the value from the model
      return tableModel.getValueAt(0, 1);
    }
  }


  /**
   * Launches this demo.
   *
   * @param args ignored command line arguments
   */
  public static void main(String[] args) {
    EventQueue.invokeLater(new Runnable() {
      public void run() {
        initLnF();
        (new SwingRendererDemo()).start("Swing Renderer Demo");
      }
    });
  }
}
