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
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

public class BusinessModelDemo {
  public static final String CSV_DIRECTORY;
  static {
    if (BusinessModelDemo.class.getResource("resource/") == null) {
      System.err.println(">> RESOURCE LOADING PROBLEM: No resource directory found.");
      System.exit(0);
    }
    CSV_DIRECTORY = BusinessModelDemo.class.getResource("resource/").getPath();
  };

  /** The yFiles graph. */
  Graph2D graph;

  /** Some colors used for the employee representation. */
  protected final Color LIGHT_BLUE = new Color(102, 140, 255);
  protected final Color ROSE = new Color(255, 102, 140);

  /** A simple graph viewer component to display our graph. */
  SimpleGraphViewer5 sgv;

  public BusinessModelDemo() {
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
    populateGraph(createModel().getEmployees());

    // Perform a layout on the created graph.
    performLayout();
    
    // Display the graph viewer with the graph.
    sgv.show();
  }

  /**
   * This method creates a node for each employee from the given collection of employees.
   * Furthermore it also creates edges from each supervisor to its subordinates.
   *
   * The node's labels are created from the job title and the employee names.
   * The fill color of a node is determined by the employee's gender.
   */
  private void populateGraph(Collection employees) {
    // A node map to hold the tooltip text.
    NodeMap nodeTooltips = graph.createNodeMap();

    //this will map employee ids to te according nodes
    HashMap id2Node = new HashMap();

    //a map that binds the supervisor id of each employee to the node
    NodeMap node2SupervisorID = graph.createNodeMap();

    //create a node for each employee
    for (Iterator iterator = employees.iterator(); iterator.hasNext(); ) {
      Object o = iterator.next();
      if (!(o instanceof Employee)) {
        continue;
      }

      Employee employee = (Employee)o;

      Node employeeNode = graph.createNode();
      graph.getRealizer(employeeNode).setLabelText(employee.getJobTitle());

      //set a label text. In this case we use one single label and customize it using html.
      // Of course multiple labels could be used and positioned using LabelModels
      graph.getRealizer(employeeNode).setLabelText("<html><div align='center'><b>" +
          employee.getJobTitle() + "</b><br>" +
          " " + employee.getFirstname() + " " + employee.getLastname() + "</div></html>");

      //nodes of male employees are colored blue, nodes of female employees are colored rose
      if (employee.isMale()) {
        graph.getRealizer(employeeNode).setFillColor(LIGHT_BLUE);
      }
      else {
        graph.getRealizer(employeeNode).setFillColor(ROSE);
      }

      id2Node.put(new Integer(employee.getId()), employeeNode);

      //if a supervisor id is set, bind it to the created node via the NodeMap
      int supervisor = employee.getSupervisor();
      if (supervisor > 0) {
        Integer supervisorID = new Integer(supervisor);
        node2SupervisorID.set(employeeNode, supervisorID);
      }

      nodeTooltips.set(employeeNode,
          "<html><b>" + employee.getDepartment().getName() + "</b><br>Room: " +
          employee.getRoom() + "<br>Extension: " + employee.getPhoneExtension());
    }

    graph.addDataProvider(TooltipMode.NODE_TOOLTIP_DPKEY, nodeTooltips);

    for (NodeCursor nodeCursor = graph.nodes(); nodeCursor.ok(); nodeCursor.next()) {
      Node employeeNode = nodeCursor.node();
      Integer supervisorID = (Integer)node2SupervisorID.get(employeeNode);
      if (supervisorID != null) {
        graph.createEdge((Node)id2Node.get(supervisorID), employeeNode);
      }
    }
    
    // Dispose of the NodeMap that we have bound to the graph, otherwise it will
    // exist as long as the graph does.
    graph.disposeNodeMap(node2SupervisorID);
  }

  private Organization createModel() {
    HashMap departments = new HashMap();
    
    Organization organization = new Organization("BusinessModelDemo Corp.");
    
    Connection connection = null;
    try {
      //create a connection to the database
      connection = createDBConnection();

      // create a Statement object to execute the query with
      Statement stmt = connection.createStatement();

      //query
      ResultSet results = stmt.executeQuery(
          "SELECT id, firstname, lastname, gender, jobtitle, department, room, phoneextension, supervisor FROM employees");

      //walk through the results (rows of employees), create according nodes and fill our maps
      while (results.next()) {
        String departmentName = results.getString("department");
        Department department = null;
        if (!departments.containsKey(departmentName)) {
          department = new Department(departmentName);
          departments.put(departmentName, department);
        }
        else {
          department = (Department)departments.get(departmentName);
        }
        Employee employee = new Employee(
            Integer.valueOf(results.getString("id")).intValue(), results.getString("firstname"), 
            results.getString("lastname"), "male".equals(results.getString("gender")), 
            results.getString("jobtitle"), department, 
            results.getString("room"), results.getInt("phoneextension"), 
            results.getString("supervisor").length() > 0 ? Integer.valueOf(results.getString("supervisor")).intValue() : -1);
        department.addEmployee(employee);
        organization.addEmployee(employee);
        organization.addDepartment(department);
      }

      // clean up
      stmt.close();
      results.close();
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
    return organization;
  }

  /**
   * Creates a connection (session) to the database.
   *
   * @return a connection to the database
   */
  private Connection createDBConnection() {
    try {
      // Connect via the CSV JDBC driver. 'CSV_DIRECTORY' is the directory where
      // the CSV files are located.
      return DriverManager.getConnection("jdbc:relique:csv:" + CSV_DIRECTORY);
    }
    catch (SQLException sqle) {
      throw new RuntimeException("Could not create database connection " + sqle);
    }
  }

  /**
   * Instantiate and register database engine driver with the DriverManager by
   * calling Class.forName(...)
   * Thus the DriverManager can create database connections using this driver.
   */
  private void loadDBDriver() {
    try {
      // CSV-JDBC
      Class.forName("org.relique.jdbc.csv.CsvDriver");
    }
    catch (ClassNotFoundException e) {
      throw new RuntimeException("Database driver not found!");
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
    new BusinessModelDemo().start();
  }
}
