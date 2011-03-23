package org.pathwayeditor.visualeditor.behaviour.selection;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import org.apache.log4j.Logger;
import org.pathwayeditor.visualeditor.behaviour.IControllerResponses;
import org.pathwayeditor.visualeditor.behaviour.IKeyboardResponse.CursorType;

public class SelectionKeyListener implements KeyListener {
	private final Logger logger = Logger.getLogger(this.getClass());
	private final IControllerResponses responses;
	
	public SelectionKeyListener(IControllerResponses responses){
		this.responses = responses;
	}
	
	@Override
	public void keyPressed(KeyEvent e) {
		logger.trace("Key press detected");
		if(e.getKeyCode() == KeyEvent.VK_RIGHT){
			handleKeyPress(CursorType.Right);
		}
		else if(e.getKeyCode() == KeyEvent.VK_LEFT){
			handleKeyPress(CursorType.Left);
		}
		else if(e.getKeyCode() == KeyEvent.VK_UP){
			handleKeyPress(CursorType.Up);
		}
		else if(e.getKeyCode() == KeyEvent.VK_DOWN){
			handleKeyPress(CursorType.Down);
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {
		logger.trace("Key release detected");
		if(e.getKeyCode() == KeyEvent.VK_RIGHT ||
				e.getKeyCode() == KeyEvent.VK_LEFT ||
				e.getKeyCode() == KeyEvent.VK_UP ||
				e.getKeyCode() == KeyEvent.VK_DOWN){
			handleKeyRelease();
		}
	}

	@Override
	public void keyTyped(KeyEvent e) {
		logger.trace("Key type detected");
	}

	private void handleKeyRelease(){
		if(this.responses.getKeyboardResponse().isKeyPressed()){
			this.responses.getKeyboardResponse().cursorsKeyUp();
			logger.trace("Key release detected");
		}
	}
	
	private void handleKeyPress(CursorType cursorPressed){
		if(!this.responses.getKeyboardResponse().isKeyPressed()){
			this.responses.getKeyboardResponse().cursorKeyDown(cursorPressed);
			logger.trace("Initial key press detected");
		}
		else{
			this.responses.getKeyboardResponse().cursorKeyStillDown(cursorPressed);
			logger.trace("Key press ongoing");
		}
	}
}
