package org.pathwayeditor.visualeditor;

public class PathwayEditorCmd {


	public void runApplication(){
		new VisualEditor("Pathway Editor");
	}
	
	
	public static final void main(String argv[]){
		PathwayEditorCmd cmd = new PathwayEditorCmd();
		cmd.runApplication();
	}

}
