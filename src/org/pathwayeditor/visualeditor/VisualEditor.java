package org.pathwayeditor.visualeditor;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.swing.JFrame;

import org.pathwayeditor.businessobjects.exchange.FileXmlCanvasPersistenceManager;
import org.pathwayeditor.businessobjects.exchange.IXmlPersistenceManager;
import org.pathwayeditor.businessobjects.management.INotationSubsystemPool;

public class VisualEditor {
//	private final Logger logger = Logger.getLogger(this.getClass());
	
	private static final int WIDTH = 1000;
	private static final int HEIGHT = 600;
	private static final String TEST_FILE = "test/org/pathwayeditor/graphicsengine/za.pwe";

	private final JFrame frame;
	private PathwayEditor insp;
	
	public VisualEditor(String title){
		this.frame = new JFrame(title);
	}
	

	public void openFile(File file){
		try{
			INotationSubsystemPool subsystemPool = new NotationSubsystemPool();
			IXmlPersistenceManager canvasPersistenceManager = new FileXmlCanvasPersistenceManager(subsystemPool);
			InputStream in = new FileInputStream(file);
			canvasPersistenceManager.readCanvasFromStream(in);
			in.close();
			insp = new PathwayEditor(canvasPersistenceManager.getCurrentCanvas(), WIDTH, HEIGHT);
			this.frame.add(insp.getCanvas());
			this.frame.pack();
			this.frame.setVisible(true);
		}
		catch(IOException ex){
			System.err.println("Error opening file!");
			System.err.println();
		}
	}
	
	
	public void startup(){
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				insp.initialise();
			}
		});
	}
	
	public static final void main(String argv[]){
		VisualEditor visualEditor = new VisualEditor("Pathway Editor");
		visualEditor.openFile(new File(TEST_FILE));
		visualEditor.startup();
		
	}
}
