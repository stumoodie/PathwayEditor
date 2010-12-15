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
package demo.io.graphml;

import demo.view.DemoBase;
import demo.view.DemoDefaults;
import y.base.Edge;
import y.base.EdgeCursor;
import y.base.Node;
import y.base.NodeCursor;
import y.io.GraphMLIOHandler;
import y.io.graphml.output.GraphElementIdProvider;
import y.io.graphml.output.GraphMLWriteException;
import y.io.graphml.output.WriteEvent;
import y.io.graphml.output.WriteEventListenerAdapter;
import y.util.D;
import y.view.EditMode;
import y.view.Graph2D;
import y.view.Graph2DEvent;
import y.view.Graph2DListener;
import y.view.Selections;
import y.view.Graph2D.BackupRealizersHandler;
import y.view.Selections.SelectionStateObserver;
import y.view.ViewMode;
import y.view.hierarchy.HierarchyManager;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JToolBar;
import javax.swing.Timer;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.DefaultHighlighter.DefaultHighlightPainter;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * This shows the basic usage of GraphMLIOHandler to load and save in the GraphML file format.
 *
 * In addition, it shows the graphml representation of the current graph in the lower text pane.
 * This representation is updated dynamically. Also, edits in the graphml text can be applied
 * to the current graph by pressing the "Apply GraphML" button.
 *
 * A small list of predefined GraphML files can be accessed from the combobox in the toolbar.
 */
public class GraphMLDemo extends DemoBase {

  protected GraphMLPane graphMLPane;

  /**
   * Creates a new instance of GraphMLDemo
   */
  public GraphMLDemo() {

    //Create a hierarchy manager that allows us to read/write hierarchically structured graphs.
    new HierarchyManager(view.getGraph2D());

    graphMLPane = new GraphMLPane();
    graphMLPane.setPreferredSize(new Dimension(600, 350));
    graphMLPane.setMinimumSize(new Dimension(0, 100));

    //plug the gui elements together and add them to the pane

    JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, view, graphMLPane);
    view.setPreferredSize(new Dimension(600, 350));
    view.setMinimumSize(new Dimension(0, 200));
    loadInitialGraph();
    view.fitContent();
    contentPane.add(splitPane, BorderLayout.CENTER);
  }

  protected void loadInitialGraph() {
    loadGraph("resources/ygraph/visual_features.graphml");
  }


  protected EditMode createEditMode() {
    EditMode mode = super.createEditMode();    
    return mode;
  }

  protected GraphMLIOHandler createGraphMLIOHandler() {
    return super.createGraphMLIOHandler();
  }

  /**
   * Overrides the base class method to add a sample file combo box.
   */
  protected JToolBar createToolBar() {
    JToolBar jToolBar = super.createToolBar();
    JComboBox comboBox = createSampleGraphComboBox();
    if (comboBox != null) {
      jToolBar.addSeparator();
      jToolBar.add(new JLabel("Sample graphs:"));
      jToolBar.add(comboBox);
    }
    return jToolBar;
  }

  /**
   * Get a list of sample files to populate the combobox created in {@link #createSampleGraphComboBox()}
   *
   * If this method returns null, no combo box is created.
   */
  protected String[] getSampleFiles() {
    return new String[]{
        "resources/ygraph/visual_features.graphml",
        "resources/ygraph/problemsolving.graphml",
        "resources/ygraph/simple.graphml",
        "resources/ygraph/grouping.graphml",
    };
  }

  protected JComboBox createSampleGraphComboBox() {
    final String[] sampleFiles = getSampleFiles();
    if (sampleFiles != null) {
      final JComboBox comboBox = new JComboBox();
      for (int i = 0; i < sampleFiles.length; i++) {
        String sampleFile = sampleFiles[i];
        comboBox.addItem(sampleFile.substring(sampleFile.lastIndexOf('/') + 1));
      }
      comboBox.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          String fileName = sampleFiles[comboBox.getSelectedIndex()];
          loadGraph(this.getClass(), fileName);
        }
      });
      comboBox.setMaximumSize(new Dimension(200, 100));
      return comboBox;
    }
    return null;
  }


  protected void loadGraph(Class aClass, String resourceString) {
    try {
      graphMLPane.setUpdating(true);
      super.loadGraph(aClass, resourceString);
      graphMLPane.setUpdating(false);
      graphMLPane.showGraphMLText(view.getGraph2D());
    }
    finally {
      graphMLPane.setUpdating(false);
    }
  }

  /**
   * Launches this demo.
   */
  public static void main(String[] args) {
    EventQueue.invokeLater(new Runnable() {
      public void run() {
        initLnF();
        (new GraphMLDemo()).start();
      }
    });
  }

  class GraphMLPane extends JPanel {

    /**
     * Reentrancy lock
     */
    protected boolean updating;
    private JButton applyButton;

    public boolean isEditable() {
      return editable;
    }

    public void setEditable(boolean editable) {
      this.editable = editable;
      graphMLTextPane.setEditable(editable);      
      applyButton.setVisible(editable);
    }

    private boolean editable = true;

    private Map elementIdMap;
    private JTextArea graphMLTextPane;

    public GraphMLPane() {
      JPanel graphMLHeader = new JPanel();
      JLabel graphMLLabel = new JLabel("GraphML representation");
      graphMLLabel.setFont(graphMLLabel.getFont().deriveFont(Font.BOLD));
      graphMLLabel.setFont(graphMLLabel.getFont().deriveFont(12.0f));
      graphMLLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
      setLayout(new BorderLayout());

      graphMLTextPane = new JTextArea();

      JScrollPane scrollPane = new JScrollPane(graphMLTextPane);
      scrollPane.setPreferredSize(new Dimension(0, 350));

      graphMLHeader.setLayout(new BorderLayout());
      graphMLHeader.add(graphMLLabel, BorderLayout.WEST);
      applyButton = new JButton("Apply GraphML");

      applyButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          applyGraphMLText(view.getGraph2D());
        }
      });

      graphMLHeader.add(applyButton, BorderLayout.EAST);

      add(graphMLHeader, BorderLayout.NORTH);
      add(scrollPane, BorderLayout.CENTER);
      setEditable(editable);
      registerView();
    }

    void registerView() {

      for(Iterator iter = view.getViewModes(); iter.hasNext();) {
        ViewMode mode = (ViewMode) iter.next();
        if(mode instanceof EditMode) {
          EditMode editMode = (EditMode) mode;
          PropertyChangeListener pcl = new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
              if (evt.getPropertyName().equals(ViewMode.EDITING_PROPERTY) && evt.getNewValue().equals(Boolean.FALSE)) {
                firePotentialChange();                
              }
            }
          };
          editMode.addPropertyChangeListener(pcl);
          editMode.getHotSpotMode().addPropertyChangeListener(pcl);
          editMode.getMovePortMode().addPropertyChangeListener(pcl);
          editMode.getMoveLabelMode().addPropertyChangeListener(pcl);
          editMode.getMoveSelectionMode().addPropertyChangeListener(pcl);
          editMode.getMoveSelectionMode().addPropertyChangeListener(pcl);
          break;
        }
      }      
      
      view.getGraph2D().setBackupRealizersHandler(new BackupRealizersHandler() {
        
        public void backupRealizers(Graph2D graph, NodeCursor ec) {
          firePotentialChange();
        }
        
        public void backupRealizers(Graph2D graph, EdgeCursor ec) {
          firePotentialChange();
        }
      });
      
      SelectionStateObserver sto = new SelectionStateObserver() {
        protected void updateSelectionState(Graph2D graph) {
          firePotentialChange();
        }
      };
      view.getGraph2D().addGraphListener(sto);
      view.getGraph2D().addGraph2DSelectionListener(sto);

      view.getGraph2D().addGraph2DListener(new Graph2DListener() {
        public void onGraph2DEvent(Graph2DEvent e) {
          firePotentialChange();
        }
      });      
    }

    
    Timer timer;
    
    public void firePotentialChange() {
      if(!isUpdating()) {
        if(timer == null) {
          timer = new Timer(100, new ActionListener() {
            
            public void actionPerformed(ActionEvent e) {
              updateGraphMLText(view.getGraph2D());
            }
          });
          timer.setRepeats(false);
        }
        timer.restart();
      }
    }
    
    private void setUpdating(boolean updating) {
      this.updating = updating;
    }

    private boolean isUpdating() {
      return updating;
    }

    public void updateGraphMLText(Graph2D graph) {
      if (!Selections.isNodeSelectionEmpty(graph)) {
        scrollGraphMLTextTo(view.getGraph2D().selectedNodes().node());
      } else if (!Selections.isEdgeSelectionEmpty(graph)) {
        scrollGraphMLTextTo(view.getGraph2D().selectedEdges().edge());
      } else {
        showGraphMLText(graph);
      }
    }

    private void scrollGraphMLTextTo(Node node) {
      if (elementIdMap != null && elementIdMap.get(node) instanceof String) {
        scrollGraphMLTextTo("node", elementIdMap.get(node).toString());
      }
    }

    private void scrollGraphMLTextTo(Edge edge) {
      if (elementIdMap != null && elementIdMap.get(edge) instanceof String) {
        scrollGraphMLTextTo("edge", elementIdMap.get(edge).toString());
      }
    }

    private void scrollGraphMLTextTo(String tag, String elementId) {
      showGraphMLText(view.getGraph2D());
      String text = graphMLTextPane.getText();
      Pattern pattern = Pattern.compile("<" + tag + " .*id=\"" + elementId + "\"");
      int startIndex = 0;
      Matcher matcher = pattern.matcher(text);
      if (matcher.find()) {
        startIndex = matcher.start();
      }
      int endIndex = text.indexOf("</" + tag + ">", startIndex) + (tag.length() + 3);

      DefaultHighlighter highlighter = new DefaultHighlighter();
      DefaultHighlightPainter painter = new DefaultHighlightPainter(DemoDefaults.DEFAULT_CONTRAST_COLOR);
      graphMLTextPane.setHighlighter(highlighter);
      try {
        highlighter.addHighlight(startIndex, endIndex, painter);
      } catch (BadLocationException e1) {
        e1.printStackTrace();
      }

      graphMLTextPane.requestFocus();
      graphMLTextPane.setCaretPosition(startIndex);
      graphMLTextPane.moveCaretPosition(endIndex);
      view.getCanvasComponent().requestFocus();

      try {
        graphMLTextPane.scrollRectToVisible(graphMLTextPane.modelToView(startIndex));
      } catch (BadLocationException e) {
        e.printStackTrace();
      }

    }

    private String createGraphMLTextAndUpdateGraphElementIdMap(final Graph2D graph) {
       StringWriter buffer = new StringWriter();
      if (graph != null) {
        GraphMLIOHandler ioh = createGraphMLIOHandler();
        ioh.getGraphMLHandler().addWriteEventListener(
            new WriteEventListenerAdapter() {
              public void onDocumentWritten(WriteEvent event)
                  throws GraphMLWriteException {
                GraphElementIdProvider idProvider = (GraphElementIdProvider) event
                    .getContext().lookup(GraphElementIdProvider.class);
                elementIdMap = new HashMap();
                for (NodeCursor nc = graph.nodes(); nc.ok(); nc.next()) {
                  Node n = nc.node();
                  elementIdMap.put(n, idProvider.getNodeId(n, event
                      .getContext()));
                }
                for (EdgeCursor ec = graph.edges(); ec.ok(); ec.next()) {
                  Edge e = ec.edge();
                  elementIdMap.put(e, idProvider.getEdgeId(e, event
                      .getContext()));
                }
              }
            });
        try {
          ioh.write(graph, buffer);
        }catch(IOException ex) {
         ex.printStackTrace();
         return "";
        }
      }
      buffer.flush();
      return buffer.toString();
    }
    
    /**
     * Helper method that serializes the current graph content into a string which is shown in the graphml text pane.
     */
    protected void showGraphMLText(final Graph2D graph) {
      if (!isUpdating()) {
        setUpdating(true);
        try {
          graphMLTextPane.setText(createGraphMLTextAndUpdateGraphElementIdMap(graph));
        }finally {
          setUpdating(false);
        }       
      }
    }

    /**
     * Helper method that applies the text content of the graphml text pane to the current graph.
     */
    protected void applyGraphMLText(final Graph2D graph) {
      if (graph != null) {

        Graph2D testGraph = (Graph2D) graph.createGraph();
        new HierarchyManager(testGraph);
        try {
          byte[] input = graphMLTextPane.getText().getBytes("UTF-8");
          
          {
            ByteArrayInputStream byteStream = new ByteArrayInputStream(input);
            createGraphMLIOHandler().read(testGraph, byteStream);
          }

          //seems to work well. try it with original graph then.          
          {
            setUpdating(true);
            ByteArrayInputStream byteStream = new ByteArrayInputStream(input);
            GraphMLIOHandler ioh = createGraphMLIOHandler();                                              
            ioh.read(graph, byteStream);
            
            createGraphMLTextAndUpdateGraphElementIdMap(graph); 
            
            setUpdating(false);            
           
          }
        } catch (Exception e) {
          D.show(e);
        }
        finally {
          graph.updateViews(); 
        }
      }
    }

  }
}
