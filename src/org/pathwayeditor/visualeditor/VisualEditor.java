package org.pathwayeditor.visualeditor;

import java.awt.AlphaComposite;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.swing.WindowConstants;

import org.pathwayeditor.visualeditor.IPathwayEditorStateChangeEvent.StateChangeType;
import org.pathwayeditor.visualeditor.commands.ICommandChangeEvent;
import org.pathwayeditor.visualeditor.commands.ICommandChangeListener;

import com.apple.eawt.AboutHandler;
import com.apple.eawt.AppEvent.AboutEvent;
import com.apple.eawt.AppEvent.PreferencesEvent;
import com.apple.eawt.AppEvent.QuitEvent;
import com.apple.eawt.Application;
import com.apple.eawt.PreferencesHandler;
import com.apple.eawt.QuitHandler;
import com.apple.eawt.QuitResponse;

public class VisualEditor extends JFrame implements AboutHandler, QuitHandler, PreferencesHandler {
	private static final long serialVersionUID = 1L;

	private static final int WIDTH = 1200;
	private static final int HEIGHT = 800;
	private final JMenuBar menuBar;
	private final PathwayEditor insp;
	private IVisualEditorController visualEditorController;
	private JMenuItem fileMenuExitItem;
	private JMenuItem fileMenuSaveAsItem;
	private JMenuItem fileMenuSaveItem;
	private JMenuItem fileMenuCloseItem;
	private IPathwayEditorStateChangeListener pathwayEditorStateChangeListener;
	private JMenuItem editMenuRedoItem;
	private JMenuItem editMenuUndoItem;
	private ICommandChangeListener commandStackChangeListener;
	
	public VisualEditor(String title, IVisualEditorController visualEditorController){
		super(title);
//		doSplashScreen();
//		MacOSXController macController = new MacOSXController();
		boolean isMacOS = System.getProperty("mrj.version") != null;
		if (isMacOS){
			Application.getApplication().setAboutHandler(this);
			Application.getApplication().setPreferencesHandler(this);
			Application.getApplication().setQuitHandler(this);
//		  Application.getApplication().setAboutHandler(macController);
//		  Application.getApplication().setPreferencesHandler(macController);
//		  Application.getApplication().setQuitHandler(macController);
		}
		this.visualEditorController = visualEditorController;
		this.visualEditorController.setVisualEditor(this);
		this.setLayout(new BorderLayout());
		this.menuBar = new JMenuBar();
		initFileMenu(isMacOS);
		initEditMenu();
		this.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		this.addWindowListener(new WindowListener(){

			@Override
			public void windowActivated(WindowEvent e) {
				
			}

			@Override
			public void windowClosed(WindowEvent e) {
			}

			@Override
			public void windowClosing(WindowEvent e) {
				int reply = JOptionPane.showConfirmDialog(VisualEditor.this, "Are you sure you want to exit?", "Exit Confirmation", JOptionPane.YES_NO_OPTION);
				if(reply == JOptionPane.YES_OPTION){
					System.exit(0);
				}
			}

			@Override
			public void windowDeactivated(WindowEvent e) {
				
			}

			@Override
			public void windowDeiconified(WindowEvent e) {
				
			}

			@Override
			public void windowIconified(WindowEvent e) {
				
			}

			@Override
			public void windowOpened(WindowEvent e) {
				
			}
			
		});
		this.setJMenuBar(menuBar);
		this.insp = new PathwayEditor(new Dialog(this, true));
		this.insp.setPreferredSize(new Dimension(WIDTH, HEIGHT));
		this.visualEditorController.setPathwayEditor(this.insp);
		this.add(this.insp, BorderLayout.CENTER);
		this.pack();
		this.setLocationByPlatform(true);
		this.setVisible(true);
		this.pathwayEditorStateChangeListener = new IPathwayEditorStateChangeListener() {
			@Override
			public void editorChangedEvent(IPathwayEditorStateChangeEvent e) {
				setFileMenuEnablement();
				if(e.getChangeType().equals(StateChangeType.OPEN)){
					e.getSource().getCommandStack().addCommandChangeListener(commandStackChangeListener);
				}
				else if(e.getChangeType().equals(StateChangeType.CLOSED)){
					e.getSource().getCommandStack().removeCommandChangeListener(commandStackChangeListener);
				}
			}
		};
		this.commandStackChangeListener = new ICommandChangeListener() {
			
			@Override
			public void notifyCommandChange(ICommandChangeEvent e) {
				setEditMenuEnablement();
			}
		};
		setFileMenuEnablement();
		setEditMenuEnablement();
		this.insp.addEditorStateChangeListener(pathwayEditorStateChangeListener);
	}

	private void initEditMenu(){
		JMenu editMenu = new JMenu("Edit");
		editMenu.setMnemonic(KeyEvent.VK_E);
		menuBar.add(editMenu);
		editMenuUndoItem = new JMenuItem("Undo", KeyEvent.VK_U);
		editMenuUndoItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		editMenuUndoItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				visualEditorController.undoAction();
			}
		});
		editMenu.add(editMenuUndoItem);
		editMenuRedoItem = new JMenuItem("Redo", KeyEvent.VK_R);
		editMenuRedoItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, ActionEvent.SHIFT_MASK|Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		editMenuRedoItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				visualEditorController.redoAction();
			}
		});
		editMenu.add(editMenuRedoItem);
	}
	
	private void setEditMenuEnablement() {
		if(insp.getCommandStack().canRedo()){
			editMenuRedoItem.setEnabled(true);
		}
		else{
			editMenuRedoItem.setEnabled(false);
		}
		if(insp.getCommandStack().canUndo()){
			editMenuUndoItem.setEnabled(true);
		}
		else{
			editMenuUndoItem.setEnabled(false);
		}
	}

	private void initFileMenu(boolean isMacOS){
		JMenu fileMenu = new JMenu("File");
		fileMenu.setMnemonic(KeyEvent.VK_F);
		menuBar.add(fileMenu);
		JMenuItem fileMenuNewItem = new JMenuItem("New", KeyEvent.VK_N);
		fileMenuNewItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		fileMenuNewItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				visualEditorController.newDiagram();
			}
		});
		fileMenu.add(fileMenuNewItem);
		//a group of JMenuItems
		JMenuItem fileMenuOpenItem = new JMenuItem("Open", KeyEvent.VK_O);
		fileMenuOpenItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		fileMenuOpenItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				visualEditorController.openFileAction();
			}
		});
		fileMenu.add(fileMenuOpenItem);
		fileMenuCloseItem = new JMenuItem("Close", KeyEvent.VK_C);
		fileMenuCloseItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_W, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		fileMenuCloseItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				visualEditorController.closeFile();
			}
		});
		fileMenu.add(fileMenuCloseItem);
		fileMenuSaveItem = new JMenuItem("Save", KeyEvent.VK_S);
		fileMenuSaveItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		fileMenuSaveItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				visualEditorController.saveFile();
			}
		});
		fileMenu.add(fileMenuSaveItem);
		fileMenuSaveAsItem = new JMenuItem("Save As ...", KeyEvent.VK_S);
		fileMenuSaveAsItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()|ActionEvent.SHIFT_MASK));
		fileMenuSaveAsItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				visualEditorController.saveFileAs();
			}
		});
		fileMenu.add(fileMenuSaveAsItem);
		if(!isMacOS){
			// Macs handle exit in another way.
			fileMenuExitItem = new JMenuItem("Exit", KeyEvent.VK_X);
			fileMenuExitItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
			fileMenuExitItem.getAccessibleContext().setAccessibleDescription("This doesn't really do anything");
			fileMenuExitItem.addActionListener(new ActionListener(){
				@Override
				public void actionPerformed(ActionEvent e) {
					processEvent(new WindowEvent(VisualEditor.this, WindowEvent.WINDOW_CLOSING));
				}
			});
			fileMenu.add(fileMenuExitItem);
		}
	}

	private void setFileMenuEnablement(){
		if(!this.insp.isOpen()){
			fileMenuSaveAsItem.setEnabled(false);
			fileMenuCloseItem.setEnabled(false);
		}
		else{
			fileMenuSaveAsItem.setEnabled(true);
			fileMenuCloseItem.setEnabled(true);
		}
		if(this.insp.isEdited()){
			fileMenuSaveItem.setEnabled(true);
		}
		else{
			fileMenuSaveItem.setEnabled(false);
		}
	}
	

//	private void doSplashScreen(){
//		final SplashScreen splash = SplashScreen.getSplashScreen();
//		if (splash == null) {
//			System.out.println("SplashScreen.getSplashScreen() returned null");
//			return;
//		}
//		Graphics2D g = splash.createGraphics();
//		if (g == null) {
//			System.out.println("g is null");
//			return;
//		}
//		renderSplashFrame(g, 10);
//		renderSplashFrame(g, 100);
//		splash.close();
//	}
	
    static void renderSplashFrame(Graphics2D g, int frame) {
        final String[] comps = {"foo", "bar", "baz"};
        g.setComposite(AlphaComposite.Clear);
        g.fillRect(120,140,200,40);
        g.setPaintMode();
        g.setColor(Color.YELLOW);
        g.drawString("Loading "+comps[(frame/5)%3]+"...", 120, 150);
    }

	@Override
	public void handlePreferences(PreferencesEvent arg0) {
	    JOptionPane.showMessageDialog(this, 
                "prefs", 
                "prefs", 
                JOptionPane.INFORMATION_MESSAGE);
	}

	@Override
	public void handleQuitRequestWith(QuitEvent arg0, QuitResponse arg1) {
		processEvent(new WindowEvent(VisualEditor.this, WindowEvent.WINDOW_CLOSING));
//		int ok = JOptionPane.showConfirmDialog(this, "Do you want to quit?", "Quit Dialog", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
//		if(ok == JOptionPane.YES_OPTION){
//			arg1.performQuit();
//		}
//		else{
//			arg1.cancelQuit();
//		}
	}

	@Override
	public void handleAbout(AboutEvent arg0) {
	    JOptionPane.showMessageDialog(this, 
                "about", 
                "about", 
                JOptionPane.INFORMATION_MESSAGE);
	}
}
