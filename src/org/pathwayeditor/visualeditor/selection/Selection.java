package org.pathwayeditor.visualeditor.selection;



public abstract class Selection implements ISelection {
	private final SelectionType selectionType;
	
	protected Selection(SelectionType selectionType){
		this.selectionType = selectionType;
	}
	
	@Override
	public final SelectionType getSelectionType(){
		return this.selectionType;
	}
	
	@Override
	public final int compareTo(ISelection o) {
		Integer thisCmpValue = getSelectionTypeValue(this.getSelectionType());
		Integer otherCmpValue = getSelectionTypeValue(o.getSelectionType());
		int retVal = thisCmpValue.compareTo(otherCmpValue); 
		if(retVal == 0){
			retVal = this.getPrimitiveController().compareTo(o.getPrimitiveController());
		}
		return retVal;
	}

	private Integer getSelectionTypeValue(SelectionType selectionType) {
		int retVal = 0;
		if(selectionType.equals(SelectionType.PRIMARY)){
			retVal = 3;
		}
		else if(selectionType.equals(SelectionType.SECONDARY)){
			retVal = 2;
			
		}
		else if(selectionType.equals(SelectionType.SUBGRAPH)){
			retVal = 1;
		}
		return retVal;
	}

}
