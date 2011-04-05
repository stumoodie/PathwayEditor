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
