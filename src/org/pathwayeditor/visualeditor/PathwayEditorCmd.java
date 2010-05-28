package org.pathwayeditor.visualeditor;

public class PathwayEditorCmd {
	public PathwayEditorCmd(){
		
	}

	public void splashScreen(){
		new StartupSplashScreen();
	}
	
	public void runApplication(){
		new VisualEditor("Pathway Editor");
	}
	
	
	public static final void main(String argv[]){
		PathwayEditorCmd cmd = new PathwayEditorCmd();
		cmd.splashScreen();
		cmd.runApplication();
	}

}
