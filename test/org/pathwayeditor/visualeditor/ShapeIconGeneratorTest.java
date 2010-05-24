package org.pathwayeditor.visualeditor;

import java.io.File;
import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.pathwayeditor.businessobjects.notationsubsystem.INotationSubsystem;
import org.pathwayeditor.businessobjects.typedefn.IShapeObjectType;
import org.pathwayeditor.bussinessobjects.stubs.notationsubsystem.StubNotationSubSystem;
import org.pathwayeditor.bussinessobjects.stubs.notationsubsystem.StubShapeAObjectType;
import org.pathwayeditor.figure.geometry.Envelope;

public class ShapeIconGeneratorTest {
	private static final Envelope EXPECTED_BOUNDS = new Envelope(0, 0, 50, 50);
	private static final File TEST_FILE = new File("test.svg");
	private static final File TEST_ICON_FILE = new File("test.png");
	private ShapeIconGenerator testInstance;
	private IShapeObjectType shapeObjectType;
	
	@Before
	public void setUp() throws Exception {
		INotationSubsystem ns = new StubNotationSubSystem();
		this.shapeObjectType = ns.getSyntaxService().getShapeObjectType(StubShapeAObjectType.UNIQUE_ID);
		this.testInstance = new ShapeIconGenerator();
		this.testInstance.setBounds(EXPECTED_BOUNDS);
		this.testInstance.setObjectType(shapeObjectType);
	}

	@After
	public void tearDown() throws Exception {
		this.testInstance = null;
	}

	@Test
	public void testGenerateImage() throws IOException {
		this.testInstance.generateImage();
		this.testInstance.writeSVGToFile(TEST_FILE);
	}

	@Test
	public void testGenerateIcon() throws IOException {
		this.testInstance.generateImage();
//		this.testInstance.generateIcon(new Dimension(50.0, 50.0));
		this.testInstance.writeIconToFile(TEST_ICON_FILE);
	}

}
