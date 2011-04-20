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

import java.io.File;
import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.pathwayeditor.businessobjects.notationsubsystem.INotationSubsystem;
import org.pathwayeditor.businessobjects.typedefn.IShapeObjectType;
import org.pathwayeditor.figure.geometry.Envelope;
import org.pathwayeditor.graphicsengine.stubs.notationsubsystem.StubNotationSubSystem;
import org.pathwayeditor.graphicsengine.stubs.notationsubsystem.StubShapeAObjectType;

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
