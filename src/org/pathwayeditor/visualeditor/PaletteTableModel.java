package org.pathwayeditor.visualeditor;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import org.pathwayeditor.businessobjects.notationsubsystem.INotationSyntaxService;
import org.pathwayeditor.businessobjects.typedefn.ILinkObjectType;
import org.pathwayeditor.businessobjects.typedefn.IObjectType;
import org.pathwayeditor.businessobjects.typedefn.IShapeObjectType;

public class PaletteTableModel extends AbstractTableModel {
	private static final long serialVersionUID = 1L;
	private int numColumns = 1;
	private final INotationSyntaxService syntaxService;
	private List<IObjectType> objectTypes;

	public PaletteTableModel(INotationSyntaxService syntaxService){
		this.syntaxService = syntaxService;
		this.objectTypes = new ArrayList<IObjectType>(this.syntaxService.numLinkObjectTypes() + this.syntaxService.numShapeObjectTypes());
		Iterator<IShapeObjectType> shapeTypeIterator = syntaxService.shapeTypeIterator();
		while(shapeTypeIterator.hasNext()){
			IShapeObjectType shapeObjectType = shapeTypeIterator.next();
			this.objectTypes.add(shapeObjectType);
		}
		Iterator<ILinkObjectType> linkTypeIterator = syntaxService.linkTypeIterator();
		while(linkTypeIterator.hasNext()){
			ILinkObjectType linkObjectType = linkTypeIterator.next();
			this.objectTypes.add(linkObjectType);
		}
	}
	
	@Override
	public int getColumnCount() {
		return numColumns ;
	}

	@Override
	public int getRowCount() {
		return this.objectTypes.size();
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		return this.objectTypes.get(rowIndex).getName();
	}

}
