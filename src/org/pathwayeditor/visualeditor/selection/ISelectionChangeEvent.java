package org.pathwayeditor.visualeditor.selection;

import java.util.Iterator;

public interface ISelectionChangeEvent {

	SelectionChangeType getSelectionChange();
	
	ISelectionRecord getSelectionRecord();
	
	ISelection getPrimarySelection();
	
	Iterator<ISelection> newSelectionIter();
	
	Iterator<ISelection> oldSelectionIter();
	
}
