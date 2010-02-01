package org.pathwayeditor.curationtool.dataviewer;

import java.util.EventListener;

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
public interface DataViewListener extends EventListener {

  void rowInserted(DataViewEvent e);
  void rowDeleted(DataViewEvent e);
  void viewReset(DataViewEvent e);
  void viewSaved(DataViewEvent e);

}
