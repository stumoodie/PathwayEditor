package org.pathwayeditor.visualeditor.behaviour;


public interface IKeyboardResponse {
	enum CursorType { Up, Down, Right, Left, None };
	
	void cursorKeyDown(CursorType cursorKeyType); 
	
	void cursorKeyStillDown(CursorType cursorKeyType);
	
	void cursorsKeyUp();
	
	CursorType getCurrentCursorKey();
	
	boolean isKeyPressed();

	void deleteKeyDetected();
}
