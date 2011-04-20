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
package org.pathwayeditor.visualeditor.layout;

import java.util.HashMap;
import java.util.Map;

import org.pathwayeditor.businessobjects.drawingprimitives.attributes.LabelLocationPolicy;
import org.pathwayeditor.businessobjects.typedefn.ILabelObjectType;

public class LabelLocationPolicyLookup {
	private static LabelLocationPolicyLookup anInstance = null;
	
	private final Map<LabelLocationPolicy, IShapeLabelLocationPolicy> shapePolicyLookup;
	private final ILabelLocationPolicy linkPolicy = new LinkLabelPositionPolicy();
	
	public static LabelLocationPolicyLookup getInstance(){
		if(anInstance == null){
			anInstance = new LabelLocationPolicyLookup();
		}
		return anInstance;
	}
	
	private LabelLocationPolicyLookup(){
		this.shapePolicyLookup = new HashMap<LabelLocationPolicy, IShapeLabelLocationPolicy>();
		this.shapePolicyLookup.put(LabelLocationPolicy.COMPASS, new CompassLabelPositionPolicy());
		this.shapePolicyLookup.put(LabelLocationPolicy.CENTRE, new ShapeCentreLabelPositionPolicy());
	}
	
	public IShapeLabelLocationPolicy getShapeLabelLocationPolicy(ILabelObjectType prop){
		IShapeLabelLocationPolicy retVal = this.shapePolicyLookup.get(prop.getDefaultAttributes().getLabelLocationPolicy());
		return retVal;
	}

	/**
	 * @param prop  
	 */
	public ILabelLocationPolicy getLinkLabelLocationPolicy(ILabelObjectType prop) {
		return this.linkPolicy;
	}
	
}
