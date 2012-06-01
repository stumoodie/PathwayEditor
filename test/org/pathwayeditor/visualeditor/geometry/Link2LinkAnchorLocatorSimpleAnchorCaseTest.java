package org.pathwayeditor.visualeditor.geometry;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.pathwayeditor.figure.geometry.LineSegment;
import org.pathwayeditor.figure.geometry.Point;

@RunWith(JMock.class)
public class Link2LinkAnchorLocatorSimpleAnchorCaseTest {
	private static final Point EXPECTED_ANCHOR_PNT = new Point(2.0, 1.5);
	private Mockery mockery;
	private Link2LinkAnchorLocator testInstance;
	private Point expectedSuggestedAnchorPoint;
	
	
	@Before
	public void setUp(){
		this.mockery = new JUnit4Mockery();
		this.expectedSuggestedAnchorPoint = new Point(2.0, 1.5);
		final ILinkPointDefinition mockLinkDefn = this.mockery.mock(ILinkPointDefinition.class, "mockLinkDefn");
		mockery.checking(new Expectations(){{
			allowing(mockLinkDefn).drawnLineSegIterator(); will(returnIterator(new LineSegment(Point.ORIGIN, new Point(4.0, 3.0))));
		}});
		this.testInstance = new Link2LinkAnchorLocator(mockLinkDefn);
		this.testInstance.setOtherEndPoint(expectedSuggestedAnchorPoint);
	}
	
	@After
	public void tearDown(){
		this.testInstance = null;
	}

	@Test
	public void testSetOtherEndPoint() {
		this.testInstance.setOtherEndPoint(Point.ORIGIN);
		assertEquals("Point set", Point.ORIGIN, this.testInstance.getOtherEndPoint());
	}

	@Test
	public void testGetOtherEndPoint() {
		assertEquals("Expected val", this.expectedSuggestedAnchorPoint, this.testInstance.getOtherEndPoint());
	}

	@Test
	public void testCanCalcAnchorPosition() {
		assertTrue("Can calc", this.testInstance.canCalcAnchorPosition());
	}

	@Test
	public void testCalcAnchorPosition() {
		Point actualResult = this.testInstance.calcAnchorPosition();
		assertNotNull("returned a value", actualResult);
		assertEquals("As expected", EXPECTED_ANCHOR_PNT, actualResult);
	}

}
