/*
Copyright 2009, Court of the University of Edinburgh

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License. 
*/
/**
 * 
 */
package org.pathwayeditor.graphicsengine.stubs.notationsubsystem;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.pathwayeditor.businessobjects.management.INotationSubsystemPool;
import org.pathwayeditor.businessobjects.notationsubsystem.INotation;
import org.pathwayeditor.businessobjects.notationsubsystem.INotationSubsystem;

/**
 * @author smoodie
 *
 */
public class StubNotationSubsystemPool implements INotationSubsystemPool {
	
	private Set <INotationSubsystem> subSystemIterator = new HashSet<INotationSubsystem> ();
	private INotationSubsystem notationSubSystem= new StubNotationSubSystem(); 
//	private INotationSubsystem notationSubSystem1= new StubNotationSubSystem("a");
//	private INotationSubsystem notationSubSystem2= new StubNotationSubSystem("b");
//	private INotationSubsystem notationSubSystem3= new StubNotationSubSystem("c");
//	private INotationSubsystem notationSubSystem4= new StubNotationSubSystem("d");
//	private INotationSubsystem notationSubSystem5= new StubNotationSubSystem("e");

	
	public StubNotationSubsystemPool () 
	{
		subSystemIterator.add(notationSubSystem) ;
//		subSystemIterator.add(notationSubSystem1) ; //only added for testing scrolling of GUI boxes. Do not add back
//		subSystemIterator.add(notationSubSystem2) ;
//		subSystemIterator.add(notationSubSystem3) ;
//		subSystemIterator.add(notationSubSystem4) ;
//		subSystemIterator.add(notationSubSystem5) ;
	}
	/* (non-Javadoc)
	 * @see org.pathwayeditor.businessobjects.bolayer.INotationSubsystemPool#getSubsystem(org.pathwayeditor.businessobjects.notationsubsystem.INotation)
	 */
	public INotationSubsystem getSubsystem(INotation notation) {
		return notationSubSystem ;
	}

	/* (non-Javadoc)
	 * @see org.pathwayeditor.businessobjects.bolayer.INotationSubsystemPool#hasNotationSubsystem(org.pathwayeditor.businessobjects.notationsubsystem.INotation)
	 */
	public boolean hasNotationSubsystem(INotation notation) {
		return true;
	}

	/* (non-Javadoc)
	 * @see org.pathwayeditor.businessobjects.bolayer.INotationSubsystemPool#subsystemIterator()
	 */
	public Iterator<INotationSubsystem> subsystemIterator() {
		
		return this.subSystemIterator.iterator();
	}

}
