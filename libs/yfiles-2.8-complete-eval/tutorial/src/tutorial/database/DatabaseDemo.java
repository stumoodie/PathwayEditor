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
package tutorial.database;

import tutorial.viewer.SimpleGraphViewer5;
import tutorial.viewmodes.TooltipMode;

import y.base.Edge;
import y.base.Node;
import y.base.NodeCursor;
import y.base.NodeMap;
import y.layout.Layouter;
import y.layout.LayoutOrientation;
import y.layout.PortConstraintKeys;
import y.layout.hierarchic.IncrementalHierarchicLayouter;
import y.layout.tree.TreeLayouter;
import y.util.DataProviderAdapter;
import y.view.Arrow;
import y.view.Graph2D;
import y.view.Graph2DLayoutExecutor;
import y.view.NodeRealizer;

import java.awt.Color;
import java.awt.Dimension;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;

public class DatabaseDemo {
  public static final String CSV_DIRECTORY;
  static {
    if (DatabaseDemo.class.getResource("resource/") == null) {
      System.err.println(">> RESOURCE LOADING PROBLEM: No resource directory found.");
      System.exit(0);
    }
    CSV_DIRECTORY = DatabaseDemo.class.getResource("resource/").getPath();
  };

  /** The yFiles graph. */
  Graph2D graph;

  /** Some colors used for the employee representation. */
  protected final Color LIGHT_BLUE = new Color(102, 140, 255);
  protected final Color ROSE = new Color(255, 102, 140);

  /** A simple graph viewer component to display our graph. */
  SimpleGraphViewer5 sgv;

  public DatabaseDemo() {
    sgv = new SimpleGraphViewer5(new Dimension(800, 600), getClass().getName()) {
      // Overridden since we need other default values.
      protected void configureDefaultRealizers(Graph2D graph) {
        // Add an arrowhead decoration to the target side of the edges.
        graph.getDefaultEdgeRealizer().setTargetArrow(Arrow.STANDARD);
        // Set the node size and some other graphical properties.
        NodeRealizer defaultNodeRealizer = graph.getDefaultNodeRealizer();
        defaultNodeRealizer.setSize(200, 50);
      }
    };
    graph = sgv.getGraph();
    sgv.getView().addViewMode(new TooltipMode());
  }

  public void start() {
    // Load the database driver into memory.
    loadDBDriver();
    
    // Create the graph.
    populateGraph();
    
    // Perform a layout on the created graph.
    performLayout();
    
    // Display the graph viewer with the graph.
    sgv.show();
  }

  /**
   * Instantiate and register database engine driver with the DriverManager by 
   * calling Class.forName(...)
   * Thus the DriverManager can create database connections using this driver.
   */
  private void loadDBDriver() {
    try {
      // Check for database driver according to your database.

      // CSV-JDBC
      Class.forName("org.relique.jdbc.csv.CsvDriver");

//      //Oracle
//      Class.forName("oracle.jdbc.driver.OracleDriver");
//
//      //DB2
//      Class.forName("com.ibm.db2.jdbc.app.DB2Driver");
//
//      //Microsoft SQL
//      Class.forName("com.microsoft.jdbc.sqlserver.SQLServerDriver");
//
//      //mysql
//      Class.forName("com.mysql.jdbc.Driver");
//
//      //Derby
//      Class.forName("org.apache.derby.jdbc.ClientDriver");
//
//      //PostgreSQL
//      Class.forName("org.postgresql.Driver");
//
//      //hSQLDb
//      Class.forName("org.hsqldb.jdbcDriver");


    }
    catch (ClassNotFoundException e) {
      throw new RuntimeException("Database driver not found!");
    }
  }

  /**
   * This method will connect to a database, query the employees and create a node 
   * for each employee.
   * Furthermore an edge will be created from each supervisor to its subordinates.
   *
   * The node's labels are created from the job title and the employee names. 
   * The fill color of a node is determined by the employee's gender.
   */
  private void populateGraph() {
    Connection connection = null;
    try {
      //create a connection to the database
      connection = createDBConnection();

      // create a Statement object to execute the query with
      Statement stmt = connection.createStatement();

      //query
      ResultSet results = stmt.executeQuery(
          "SELECT id, firstname, lastname, gender, jobtitle, department, room, phoneextension, supervisor FROM employees");

      // A node map to hold the tooltip text.
      NodeMap nodeTooltips = graph.createNodeMap();
      
      //a map that binds the supervisor id of each employee to the node
      NodeMap node2SupervisorID = graph.createNodeMap();

      //this will map employee ids to te according nodes
      HashMap id2Node = new HashMap();

      //walk through the results (rows of employees), create according nodes and fill our maps
      while (results.next()) {
        Node employee = graph.createNode();

        //add mapping: id to node
        Integer id = new Integer(results.getInt("id"));
        id2Node.put(id, employee);

        //if a supervisor id is set, bind it to the created node via the NodeMap
        String supervisorString = results.getString("supervisor").trim();
        if (supervisorString.length() > 0) {
          Integer supervisorID = Integer.valueOf(supervisorString);
          node2SupervisorID.set(employee, supervisorID);
        }

        //set a label text. In this case we use one single label and customize it using html.
        // Of course multiple labels could be used and positioned using LabelModels
        graph.getRealizer(employee).setLabelText("<html><div align='center'><b>" +
            results.getString("jobtitle") + "</b><br>" +
            " " + results.getString("firstname") + " " + results.getString("lastname") + "</div></html>");

        //nodes of male employees are colored blue, nodes of female employees are colored rose
        if ("male".equals(results.getString("gender"))) {
          graph.getRealizer(employee).setFillColor(LIGHT_BLUE);
        }
        else {
          graph.getRealizer(employee).setFillColor(ROSE);
        }
        
        nodeTooltips.set(employee, 
            "<html><b>" + results.getString("department") + "</b><br>Room: " + 
            results.getString("room") + "<br>Extension: " + results.getInt("phoneextension"));
      }

      //Now we walk through all nodes and create edges from the supervisor node to the employee node
      for (NodeCursor nodeCursor = graph.nodes(); nodeCursor.ok(); nodeCursor.next()) {
        Node node = nodeCursor.node();
        //get the supervisor id of this node using the NodeMap we filled when creating the nodes
        Integer supervisorID = (Integer) node2SupervisorID.get(node);
        if (supervisorID != null) {
          //if a supervisor id is set, create an edge
          graph.createEdge((Node) id2Node.get(supervisorID), node);
        }
      }

      graph.addDataProvider(TooltipMode.NODE_TOOLTIP_DPKEY, nodeTooltips);
      
      // clean up
      stmt.close();
      results.close();
      // Dispose the NodeMap we bound to the graph, otherwise it will exist as long as the graph does
      graph.disposeNodeMap(node2SupervisorID);
    }
    catch (SQLException sqle) {
      sqle.printStackTrace();
    }
    finally {
      //make sure database connection will always be closed
      if (connection != null) {
        try {
          connection.close();
        }
        catch (SQLException sqle) {
          throw new RuntimeException("Could not close database connection. " + sqle);
        }
      }
    }
  }

  /**
   * Creates a connection (session) to the database.
   *
   * @return
   * The Connection object that is returned by the DriverManager using the given 
   * database driver.
   */
  private Connection createDBConnection() {
    try {
      Connection connection;

      //use appropriate connection depending on your database

      // Connect via the CSV JDBC driver. 'CSV_DIRECTORY' is the directory where 
      // the CSV files are located.
      connection = DriverManager.getConnection("jdbc:relique:csv:" + CSV_DIRECTORY);

//      //Oracle
//      connection = DriverManager.getConnection("jdbc:oracle:thin:@<serverip>:<port>:<sid>", "<username>", "<password>");
//
//      //DB2
//      connection = DriverManager.getConnection("jdbc:db2://<serverip>:<port>/<databaseName>", "<username>", "<password>");
//
//      //Microsoft SQL
//      connection = DriverManager.getConnection("jdbc:microsoft:sqlserver://<serverip>:<port>/<databaseName>", "<username>",
//          "<password>");
//
//      //mysql
//      connection = DriverManager.getConnection("jdbc:mysql://<serverip>:<port>/<databaseName>", "<username>", "<password>");
//
//      //derby
//      connection = DriverManager.getConnection("jdbc:derby://<serverip>:<port>/<databaseName>", "<username>", "<password>");
//
//      //PostgreSQL
//      connection = DriverManager.getConnection("jdbc:postgreesql://<serverip>:<port>/<databaseName>", "<username>", "<password>");
//
//      //hSQLDb
//      connection = DriverManager.getConnection( "jdbc:hsqldb://<serverip>:<port>/<databaseName>", "<username>", "<password>");

      return connection;
    }
    catch (SQLException sqle) {
      throw new RuntimeException("Could not create database connection " + sqle);
    }
  }

  /** Run the layout algorithm in buffered mode */
  private void performLayout() {
    // OPTIONAL.
//    prepareForLayout();
    new Graph2DLayoutExecutor().doLayout(graph, createLayouter());
    sgv.getView().fitContent();
    graph.updateViews();
  }

  /** Establish the necessary setup for bus-like edge routing. */
  private void prepareForLayout() {
    graph.addDataProvider(PortConstraintKeys.SOURCE_GROUPID_KEY, new DataProviderAdapter() {
      public Object get(Object edge) {
        return ((Edge)edge).source();
      }
    });
  }

  /**
   * Creates a {@link y.layout.Layouter} that will be used to perform a layout on the created graph.
   *
   * @return an implementation of {@link y.layout.Layouter}.
   */
  private Layouter createLayouter() {
    IncrementalHierarchicLayouter ihl = new IncrementalHierarchicLayouter();

    //optional layouter customizations

    //edges shall be routed in orthogonal fashion
    ihl.setOrthogonallyRouted(true);

    //layout graph from left to right
    ihl.setLayoutOrientation(LayoutOrientation.LEFT_TO_RIGHT);
    return ihl;
  }
  // OPTIONAL.
//  private Layouter createLayouter() {
//    TreeLayouter tl = new TreeLayouter();
//
//    //optional layouter customizations
//
//    //edges shall be routed in orthogonal fashion
//    tl.setLayoutStyle(TreeLayouter.ORTHOGONAL_STYLE);
//
//    //layout graph from left to right
//    tl.setLayoutOrientation(LayoutOrientation.LEFT_TO_RIGHT);
//    return tl;
//  }

  public static void main(String[] args) {
    new DatabaseDemo().start();
  }
}
