package org.pathwayeditor.visualeditor.dataviewer;

/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2005</p>
 *
 * <p>Company: DSTT</p>
 *
 * @author Stuart L. Moodie
 * @version 1.0
 */
public interface ITableRow extends Comparable<Object> {

  //int getId();

  /**
   * getColumnValue
   *
   * @param col int
   * @return Object
   */
  Object getColumnValue(int col);

  /**
   * setColumnValue
   *
   * @param col int
   * @param newValue Object
   */
  void setColumnValue(int col, Object newValue);

  ITableRow getCopy();

  IRowDefn getRowDefn();

}
