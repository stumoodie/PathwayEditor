/*
  Licensed to the Court of the University of Edinburgh (UofE) under one
  or more contributor license agreements.  See the NOTICE file
  distributed with this work for additional information
  regarding copyright ownership.  The UofE licenses this file
  to you under the Apache License, Version 2.0 (the
  "License"); you may not use this file except in compliance
  with the License.  You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing,
  software distributed under the License is distributed on an
  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  KIND, either express or implied.  See the License for the
  specific language governing permissions and limitations
  under the License.
*/

package org.pathwayeditor.visualeditor;

import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import org.pathwayeditor.businessobjects.management.INotationSubsystemPool;
import org.pathwayeditor.businessobjects.notationsubsystem.INotation;
import org.pathwayeditor.businessobjects.notationsubsystem.INotationSubsystem;
import org.pathwayeditor.notations.sbgnpd.services.SbgnPdNotationSubsystem;


public class NotationSubsystemPool implements INotationSubsystemPool {
    private final Map<INotation, INotationSubsystem> lookup;
    
    public NotationSubsystemPool(){
        this.lookup = new TreeMap<INotation, INotationSubsystem>();
        addNotationSubsystem(new SbgnPdNotationSubsystem());
//        addNotationSubsystem(new SbgnERNotationSubsystem());
//        addNotationSubsystem(new SbgnAfNotationSubsystem());
    }
    
    private void addNotationSubsystem(INotationSubsystem notationSubsystem) {
        this.lookup.put(notationSubsystem.getNotation(), notationSubsystem);
    }
    
    @Override
	public INotationSubsystem getSubsystem(INotation notation) {
        INotationSubsystem retVal = null;
        if(notation != null) {
            retVal = lookup.get(notation);
        }
        if(retVal == null) {
            throw new IllegalArgumentException("Cannot find the notation in this pool. Notation=" + notation);
        }
        return retVal;
    }

    @Override
	public boolean hasNotationSubsystem(INotation notation) {
        boolean retVal = false;
        if(notation != null) {
            retVal = lookup.containsKey(notation);
        }
        return retVal;
    }

    @Override
	public Iterator<INotationSubsystem> subsystemIterator() {
        return this.lookup.values().iterator();
    }
}
