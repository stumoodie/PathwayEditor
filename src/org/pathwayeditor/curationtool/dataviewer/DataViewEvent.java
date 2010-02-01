package org.pathwayeditor.curationtool.dataviewer;

import java.util.EventObject;

import javax.swing.JTable;

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
public class DataViewEvent extends EventObject {
  private static final long serialVersionUID = -1748321238564877418L;

  public DataViewEvent(JTable source) {
    super(source);
  }

  public IDataViewTableModel getTableModel() {
    return (IDataViewTableModel)((JTable)getSource()).getModel();
  }
}
