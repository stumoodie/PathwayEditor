package org.pathwayeditor.notations.link2link;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import org.pathwayeditor.businessobjects.drawingprimitives.attributes.Colour;
import org.pathwayeditor.businessobjects.drawingprimitives.attributes.LineStyle;
import org.pathwayeditor.businessobjects.drawingprimitives.attributes.LinkEndDecoratorShape;
import org.pathwayeditor.businessobjects.drawingprimitives.attributes.RGB;
import org.pathwayeditor.businessobjects.drawingprimitives.properties.IPropertyDefinition;
import org.pathwayeditor.businessobjects.notationsubsystem.INotation;
import org.pathwayeditor.businessobjects.notationsubsystem.INotationSubsystem;
import org.pathwayeditor.businessobjects.notationsubsystem.INotationSyntaxService;
import org.pathwayeditor.businessobjects.typedefn.IAnchorNodeObjectType;
import org.pathwayeditor.businessobjects.typedefn.ILabelObjectType;
import org.pathwayeditor.businessobjects.typedefn.ILinkObjectType;
import org.pathwayeditor.businessobjects.typedefn.ILinkObjectType.LinkEditableAttributes;
import org.pathwayeditor.businessobjects.typedefn.ILinkTerminusDefinition.LinkTermEditableAttributes;
import org.pathwayeditor.businessobjects.typedefn.IObjectType;
import org.pathwayeditor.businessobjects.typedefn.IRootObjectType;
import org.pathwayeditor.businessobjects.typedefn.IShapeObjectType;
import org.pathwayeditor.figure.geometry.Dimension;
import org.pathwayeditor.notationsubsystem.toolkit.definition.AnchorNodeObjectType;
import org.pathwayeditor.notationsubsystem.toolkit.definition.LabelObjectType;
import org.pathwayeditor.notationsubsystem.toolkit.definition.LinkObjectType;
import org.pathwayeditor.notationsubsystem.toolkit.definition.LinkParentingRules;
import org.pathwayeditor.notationsubsystem.toolkit.definition.NumberPropertyDefinition;
import org.pathwayeditor.notationsubsystem.toolkit.definition.PlainTextPropertyDefinition;
import org.pathwayeditor.notationsubsystem.toolkit.definition.RootObjectType;
import org.pathwayeditor.notationsubsystem.toolkit.definition.ShapeObjectType;

public class Link2LinkSyntaxService implements INotationSyntaxService {
	public static final int ROOT_OT = 0;
	public static final int SHAPEA = 1;
	public static final int LINKA = 100;
	public static final int LABELA_OT = 1000;
	public static final int ANCHOR_SHAPEA = 10000;
	private static final int SHAPEB = 3;
	private static final int LABELB_OT = 1001;
	
	private final INotationSubsystem subsystem;
	private final SortedMap<Integer, IShapeObjectType> shapeOts;
	private final SortedMap<Integer, ILinkObjectType> linkOts;
	private final SortedMap<Integer, IAnchorNodeObjectType> anchorNodeOts;
	private final SortedMap<IPropertyDefinition, ILabelObjectType> labelOts;
	private final RootObjectType rootObjectType;
//	private final Map<ILinkObjectType, IShapeObjectType> linkEndObjectTypes;

	public Link2LinkSyntaxService(INotationSubsystem subsystem){
		this.subsystem = subsystem;
		this.shapeOts = new TreeMap<Integer, IShapeObjectType>();
		this.linkOts = new TreeMap<Integer, ILinkObjectType>();
		this.anchorNodeOts = new TreeMap<Integer, IAnchorNodeObjectType>();
		this.labelOts = new TreeMap<IPropertyDefinition, ILabelObjectType>();
//		this.linkEndObjectTypes = new HashMap<ILinkObjectType, IShapeObjectType>();
		this.rootObjectType = createRootObject();
		assignObjectType(this.shapeOts, createShapeA());
		assignObjectType(this.shapeOts, createShapeB());
		assignObjectType(this.linkOts, createLinkA());
		assignObjectType(this.anchorNodeOts, createAnchorShapeA());
		defineParenting();
		defineConnections();
	}
	
	private IAnchorNodeObjectType createAnchorShapeA() {
		AnchorNodeObjectType retVal = new AnchorNodeObjectType(this, ANCHOR_SHAPEA, "Anchor Shape A");
		retVal.setDescription("Test anchor shape A");
		retVal.getDefaultAttributes().setFillColour(new Colour(RGB.WHITE, Colour.TRANSPARENT));
		retVal.getDefaultAttributes().setLineColour(Colour.BLACK);
		retVal.getDefaultAttributes().setFontColour(Colour.BLACK);
		retVal.getDefaultAttributes().setLineStyle(LineStyle.SOLID);
		retVal.getDefaultAttributes().setLineWidth(1.0);
		retVal.getDefaultAttributes().setSize(new Dimension(30.0, 30.0));
		retVal.getDefaultAttributes().setShapeDefinition(
				"(C) setanchor\n"
						+ "curbounds /h exch def /w exch def /y exch def /x exch def\n"
						+ "x y w h oval"
				);
		return retVal;
	}

	private void defineParenting() {
		this.rootObjectType.getParentingRules().addChild(this.getShapeObjectType(SHAPEA));
		this.rootObjectType.getParentingRules().addChild(this.getShapeObjectType(SHAPEB));
		((ShapeObjectType)this.getShapeObjectType(SHAPEB)).getParentingRules().addChild(this.getShapeObjectType(SHAPEA));
		ILinkObjectType linkObjectType = this.getLinkObjectType(LINKA);
//		IShapeObjectType linkEndShapeOt = this.getLinkEndObjectType(linkObjectType);
		((LinkParentingRules)linkObjectType.getParentingRules()).addChild(this.anchorNodeOts.get(ANCHOR_SHAPEA));
	}

	private void defineConnections(){
		LinkObjectType linkOt = (LinkObjectType)this.getLinkObjectType(LINKA);
		IShapeObjectType shapeAOt = this.getShapeObjectType(SHAPEA);
		IAnchorNodeObjectType linkEndShapeOt = this.getAnchorNodeObjectType(ANCHOR_SHAPEA);
		linkOt.getLinkConnectionRules().addConnection(shapeAOt, shapeAOt);
		linkOt.getLinkConnectionRules().addConnection(shapeAOt, linkEndShapeOt);
	}
	
	private RootObjectType createRootObject() {
		RootObjectType retVal = new RootObjectType(ROOT_OT, this);
		return retVal;
	}

	private static <T extends IObjectType>  void assignObjectType(SortedMap<Integer, T> otMap, T objectType){
		otMap.put(objectType.getUniqueId(), objectType);
	}
	
	@Override
	public INotation getNotation() {
		return this.subsystem.getNotation();
	}

	@Override
	public INotationSubsystem getNotationSubsystem() {
		return this.subsystem;
	}

	@Override
	public Iterator<IShapeObjectType> shapeTypeIterator() {
		return this.shapeOts.values().iterator();
	}

	@Override
	public Iterator<ILinkObjectType> linkTypeIterator() {
		return this.linkOts.values().iterator();
	}

	@Override
	public Iterator<IObjectType> objectTypeIterator() {
		List<IObjectType> retVal = new LinkedList<IObjectType>(this.shapeOts.values());
		retVal.addAll(this.linkOts.values());
		retVal.addAll(this.anchorNodeOts.values());
		retVal.addAll(this.labelOts.values());
		return retVal.iterator();
	}

	@Override
	public IRootObjectType getRootObjectType() {
		return this.rootObjectType;
	}

	@Override
	public boolean containsShapeObjectType(int uniqueId) {
		return this.shapeOts.containsKey(uniqueId);
	}

	@Override
	public IShapeObjectType getShapeObjectType(int uniqueId) {
		return this.shapeOts.get(uniqueId);
	}

	@Override
	public boolean containsLinkObjectType(int uniqueId) {
		return this.linkOts.containsKey(uniqueId);
	}

	@Override
	public ILinkObjectType getLinkObjectType(int uniqueId) {
		return this.linkOts.get(uniqueId);
	}

	@Override
	public boolean containsObjectType(int uniqueId) {
		boolean retVal = uniqueId == ROOT_OT || this.shapeOts.containsKey(uniqueId)
				|| this.anchorNodeOts.containsKey(uniqueId) || this.linkOts.containsKey(uniqueId);
		if(!retVal){
			for(ILabelObjectType labOt : this.labelOts.values()){
				if(labOt.getUniqueId() == uniqueId){
					retVal = true;
					break;
				}
			}
		}
		return retVal;
	}

	@Override
	public IObjectType getObjectType(int uniqueId) {
		IObjectType retVal = null;
		if(uniqueId == ROOT_OT){
			retVal = this.rootObjectType;
		}
		else if(this.shapeOts.containsKey(uniqueId)){
			retVal = this.shapeOts.get(uniqueId);
		}
		else if(this.anchorNodeOts.containsKey(uniqueId)){
			retVal = this.anchorNodeOts.get(uniqueId);
		}
		else{
			retVal = this.linkOts.get(uniqueId);
		}
		return retVal;
	}

	@Override
	public ILabelObjectType getLabelObjectType(int uniqueId) {
		ILabelObjectType retVal = null;
		for(ILabelObjectType labelObjectType : this.labelOts.values()){
			if(labelObjectType.getUniqueId() == uniqueId){
				retVal = labelObjectType;
				break;
			}
		}
		return retVal;
	}

	@Override
	public ILabelObjectType getLabelObjectTypeByProperty(IPropertyDefinition propDefn) {
		return this.labelOts.get(propDefn);
	}

	@Override
	public boolean isVisualisableProperty(IPropertyDefinition propDefn) {
		return this.labelOts.containsKey(propDefn);
	}

	@Override
	public int numShapeObjectTypes() {
		return this.shapeOts.size();
	}

	@Override
	public int numLinkObjectTypes() {
		return this.linkOts.size();
	}

	@Override
	public int numObjectTypes() {
		return this.shapeOts.size();
	}

	private <T extends IObjectType> T findObjectTypeByName(
			Collection<? extends T> otSet, String name) {
		T retVal = null;
		for (T val : otSet) {
			if (val.getName().equals(name)) {
				retVal = val;
				break;
			}
		}
		return retVal;
	}

	@Override
	public IShapeObjectType findShapeObjectTypeByName(String name) {
		return findObjectTypeByName(this.shapeOts.values(), name);
	}

	@Override
	public ILinkObjectType findLinkObjectTypeByName(String name) {
		return findObjectTypeByName(this.linkOts.values(), name);
	}

//	@Override
//	public IShapeObjectType getLinkEndObjectType(ILinkObjectType linkOt) {
//		return this.linkEndObjectTypes.get(linkOt);
//	}

	
	private ShapeObjectType createShapeA(){
		ShapeObjectType retVal = new ShapeObjectType(this, SHAPEA, "Shape A");
		retVal.setDescription("Test shape A");
		retVal.setEditableAttributes(EnumSet.allOf(IShapeObjectType.EditableShapeAttributes.class));
		retVal.getDefaultAttributes().setFillColour(Colour.WHITE);
		retVal.getDefaultAttributes().setLineColour(Colour.BLACK);
		retVal.getDefaultAttributes().setFontColour(Colour.BLACK);
		retVal.getDefaultAttributes().setLineStyle(LineStyle.SOLID);
		retVal.getDefaultAttributes().setLineWidth(1.0);
		retVal.getDefaultAttributes().setSize(new Dimension(40.0, 40.0));
		retVal.getDefaultAttributes().setShapeDefinition(
				"(C) setanchor\n"
						+ "curbounds /h exch def /w exch def /y exch def /x exch def\n"
						+ "x y w h oval"
				);
		PlainTextPropertyDefinition name = new PlainTextPropertyDefinition("name", "Name");
		name.setDisplayName("Name");
		name.setEditable(true);
		retVal.getDefaultAttributes().addPropertyDefinition(name);
		this.labelOts.put(name, createNameLabel());
		return retVal;
	}
	
	private LabelObjectType createNameLabel(){
		LabelObjectType retVal = new LabelObjectType(this, LABELA_OT, "Label A");
		retVal.setDescription("Test Label");
		retVal.getDefaultAttributes().setLineColour(new Colour(Colour.BLACK.getRgb(), Colour.TRANSPARENT));
		retVal.getDefaultAttributes().setLineStyle(LineStyle.SOLID);
		retVal.getDefaultAttributes().setLineWidth(1.0);
		retVal.getDefaultAttributes().setFillColour(Colour.WHITE);
		retVal.getDefaultAttributes().setMinimumSize(new Dimension(25, 20));
		retVal.setAlwaysDisplayed(true);
		
		return retVal;
	}
	
	private LabelObjectType createNumberLabel(){
		LabelObjectType retVal = new LabelObjectType(this, LABELB_OT, "Label B");
		retVal.setDescription("Test Label");
		retVal.getDefaultAttributes().setLineColour(new Colour(Colour.BLACK.getRgb(), Colour.TRANSPARENT));
		retVal.getDefaultAttributes().setLineStyle(LineStyle.SOLID);
		retVal.getDefaultAttributes().setLineWidth(1.0);
		retVal.getDefaultAttributes().setFillColour(Colour.WHITE);
		retVal.getDefaultAttributes().setMinimumSize(new Dimension(25, 20));
		retVal.setAlwaysDisplayed(false);
		
		return retVal;
	}
	
	private ShapeObjectType createShapeB(){
		ShapeObjectType retVal = new ShapeObjectType(this, SHAPEB, "Shape B");
		retVal.setDescription("Test shape B");
		retVal.setEditableAttributes(EnumSet.allOf(IShapeObjectType.EditableShapeAttributes.class));
		retVal.getDefaultAttributes().setFillColour(Colour.WHITE);
		retVal.getDefaultAttributes().setLineColour(Colour.BLACK);
		retVal.getDefaultAttributes().setFontColour(Colour.BLACK);
		retVal.getDefaultAttributes().setLineStyle(LineStyle.SOLID);
		retVal.getDefaultAttributes().setLineWidth(1.0);
		retVal.getDefaultAttributes().setSize(new Dimension(100.0, 80.0));
		retVal.getDefaultAttributes().setShapeDefinition(
				"(C) setanchor\n"
						+ "curbounds /h exch def /w exch def /y exch def /x exch def\n"
						+ "x y w h rect"
				);
		PlainTextPropertyDefinition name = new PlainTextPropertyDefinition("name", "Name");
		name.setDisplayName("Name");
		name.setEditable(true);
		retVal.getDefaultAttributes().addPropertyDefinition(name);
		NumberPropertyDefinition numProp = new NumberPropertyDefinition("Number", new BigDecimal(2.5), true, true);
		name.setDisplayName("Num Prop");
		name.setEditable(true);
		retVal.getDefaultAttributes().addPropertyDefinition(numProp);
		this.labelOts.put(name, createNameLabel());
		this.labelOts.put(numProp, createNumberLabel());
		return retVal;
	}
	
//	private IShapeObjectType createLinkEndOt() {
//		ShapeObjectType retVal = new ShapeObjectType(this, LINKA_END_OT, "LinkA End Ot");
//		retVal.getDefaultAttributes().setShapeDefinition(
//				"(C) setanchor\n"
//				+ "curbounds /h exch def /w exch def /y exch def /x exch def\n"
//				+ "x y w h rect"
//
//				);
//		retVal.getDefaultAttributes().setFillColour(new Colour(Colour.BLACK.getRgb(), Colour.TRANSPARENT));
//		retVal.getDefaultAttributes().setLineColour(new Colour(Colour.BLACK.getRgb(), Colour.TRANSPARENT));
//		retVal.getDefaultAttributes().setSize(new Dimension(5, 5));
//
//		return retVal;
//	}

	private LinkObjectType createLinkA(){
		LinkObjectType retVal = new LinkObjectType(this, LINKA, "Link A");
		retVal.setDescription("Test link A");
		retVal.setEditableAttributes(EnumSet.allOf(ILinkObjectType.LinkEditableAttributes.class));
		retVal.getDefaultAttributes().setLineColour(Colour.BLACK);
		retVal.getDefaultAttributes().setLineStyle(LineStyle.SOLID);
		retVal.getDefaultAttributes().setLineWidth(1.0);
		retVal.setEditableAttributes(EnumSet.allOf(LinkEditableAttributes.class));
		PlainTextPropertyDefinition name = new PlainTextPropertyDefinition("linkProp", "linkProp");
		name.setDisplayName("LinkProp");
		name.setEditable(true);

		retVal.getSourceTerminusDefinition().setEditableAttributes(EnumSet.allOf(LinkTermEditableAttributes.class));
		retVal.getSourceTerminusDefinition().getDefaultAttributes().setEndSize(new Dimension(10,10));
		retVal.getSourceTerminusDefinition().getDefaultAttributes().setGap(3);
		retVal.getSourceTerminusDefinition().getDefaultAttributes().setEndDecoratorType(LinkEndDecoratorShape.ARROW);
		
		retVal.getTargetTerminusDefinition().setEditableAttributes(EnumSet.allOf(LinkTermEditableAttributes.class));
		retVal.getTargetTerminusDefinition().getDefaultAttributes().setEndSize(new Dimension(10,10));
		retVal.getTargetTerminusDefinition().getDefaultAttributes().setGap(3);
		retVal.getTargetTerminusDefinition().getDefaultAttributes().setEndDecoratorType(LinkEndDecoratorShape.ARROW);
		
//		this.linkEndObjectTypes.put(retVal, createLinkEndOt());
		
		return retVal;
	}

	@Override
	public Iterator<IAnchorNodeObjectType> anchorNodeTypeIterator() {
		return this.anchorNodeOts.values().iterator();
	}

	@Override
	public int numAnchorNodeTypes() {
		return this.anchorNodeOts.size();
	}

	@Override
	public IAnchorNodeObjectType getAnchorNodeObjectType(int uniqueId) {
		return this.anchorNodeOts.get(uniqueId);
	}

	@Override
	public boolean containsAnchorNodeObjectType(int uniqueId) {
		return this.anchorNodeOts.containsKey(uniqueId);
	}
}
