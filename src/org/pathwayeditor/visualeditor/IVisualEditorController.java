package org.pathwayeditor.visualeditor;

import java.io.File;

public interface IVisualEditorController {

	void setVisualEditor(VisualEditor visualEditor);
	
	VisualEditor getVisualEditor();
	
	void newDiagram();

	void openFile(File file);

	void openFileAction();

	void setPathwayEditor(PathwayEditor insp);

	PathwayEditor getPathwayEditor();

	void saveFile();

	void saveFileAs();

	void closeFile();

	void undoAction();

	void redoAction();

	void deleteAction();

	void selectAllAction();
}
