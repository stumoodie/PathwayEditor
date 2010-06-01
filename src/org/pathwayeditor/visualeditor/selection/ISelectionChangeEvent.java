package org.pathwayeditor.visualeditor.selection;

import java.util.Iterator;

public interface ISelectionChangeEvent {

	ISelectionRecord getSelectionRecord();
	
	Iterator<ISelection> newSelectionIter();
	
	Iterator<ISelection> oldSelectionIter();
	
}
