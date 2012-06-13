package org.pathwayeditor.visualeditor.controller;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.pathwayeditor.businessobjects.drawingprimitives.ICurveSegment;
import org.pathwayeditor.businessobjects.impl.StraightLineCurveSegment;
import org.pathwayeditor.figure.geometry.Point;

public class AnchorPointSegmentChangeRemoveCurveSegCalculatorTest {
	private static final Point REP_START = new Point(10,20);
	private static final Point REP_END = new Point(210, 70);
	private static final Point ORIG1_START = new Point(10, 20);
	private static final Point ORIG1_END = new Point(110, 120);
	private static final Point ORIG2_START = new Point(110, 120);
	private static final Point ORIG2_END = new Point(210, 70);
	private static final Point TEST_END_ANCHOR_POINT = ORIG1_START;
	private static final Point NEW_END_ANCHOR_PT = ORIG1_START;
	private static final Point TEST_LEFT_ANCHOR_POINT = new Point(70.0, 80.0);
	private static final Point NEW_LEFT_ANCHOR_PT = new Point(70, 35);
	private static final Point TEST_RIGHT_ANCHOR_POINT = new Point(130.0, 110.0);
	private static final Point NEW_RIGHT_ANCHOR_PT = new Point(130, 50.0);
	private static final Point TEST_MIDPOINT_ANCHOR_POINT = ORIG1_END;
	private static final Point NEW_MIDPOINT_ANCHOR_PT = new Point(110, 45);
	private AnchorPointSegmentChangeCalculator testInstance;
	private List<ICurveSegment> originalSegs;
	private List<ICurveSegment> replacementSegs;
	
	
	@Before
	public void setUp() throws Exception {
		this.originalSegs = Arrays.asList(new ICurveSegment[]{ new StraightLineCurveSegment(ORIG1_START, ORIG1_END), new StraightLineCurveSegment(ORIG2_START, ORIG2_END) });
		this.replacementSegs = Arrays.asList(new ICurveSegment[]{ new StraightLineCurveSegment(REP_START, REP_END) });
		this.testInstance = new AnchorPointSegmentChangeCalculator(originalSegs, replacementSegs);
	}

	@After
	public void tearDown() throws Exception {
		this.testInstance = null;
	}

	@Test
	public void testCalculateLeftNewCurveAssociation() {
		this.testInstance.calculateNewCurveAssociation(TEST_LEFT_ANCHOR_POINT);
		ICurveSegment expectedSeg = new StraightLineCurveSegment(REP_START, REP_END);
		assertEquals("Expected assoc seg", expectedSeg, this.testInstance.getNewAssociatedCurveSegment());
		Point actualAnchorPt = this.testInstance.getNewAnchorPosn();
		assertEquals("Expected new anchor pt x", NEW_LEFT_ANCHOR_PT.getX(), actualAnchorPt.getX(), 0.0001);
		assertEquals("Expected new anchor pt y", NEW_LEFT_ANCHOR_PT.getY(), actualAnchorPt.getY(), 0.0001);
	}

	@Test
	public void testCalculateRightNewCurveAssociation() {
		this.testInstance.calculateNewCurveAssociation(TEST_RIGHT_ANCHOR_POINT);
		ICurveSegment expectedSeg = new StraightLineCurveSegment(REP_START, REP_END);
		assertEquals("Expected assoc seg", expectedSeg, this.testInstance.getNewAssociatedCurveSegment());
		Point actualAnchorPt = this.testInstance.getNewAnchorPosn();
		assertEquals("Expected new anchor pt x", NEW_RIGHT_ANCHOR_PT.getX(), actualAnchorPt.getX(), 0.0001);
		assertEquals("Expected new anchor pt y", NEW_RIGHT_ANCHOR_PT.getY(), actualAnchorPt.getY(), 0.0001);
	}

	@Test
	public void testCalculateEndPointCurveAssociation() {
		this.testInstance.calculateNewCurveAssociation(TEST_END_ANCHOR_POINT);
		ICurveSegment expectedSeg = new StraightLineCurveSegment(REP_START, REP_END);
		assertEquals("Expected assoc seg", expectedSeg, this.testInstance.getNewAssociatedCurveSegment());
		Point actualAnchorPt = this.testInstance.getNewAnchorPosn();
		assertEquals("Expected new anchor pt x", NEW_END_ANCHOR_PT.getX(), actualAnchorPt.getX(), 0.0001);
		assertEquals("Expected new anchor pt y", NEW_END_ANCHOR_PT.getY(), actualAnchorPt.getY(), 0.0001);
	}

	@Test
	public void testCalculateMidPointCurveAssociation() {
		this.testInstance.calculateNewCurveAssociation(TEST_MIDPOINT_ANCHOR_POINT);
		ICurveSegment expectedSeg = new StraightLineCurveSegment(REP_START, REP_END);
		assertEquals("Expected assoc seg", expectedSeg, this.testInstance.getNewAssociatedCurveSegment());
		Point actualAnchorPt = this.testInstance.getNewAnchorPosn();
		assertEquals("Expected new anchor pt x", NEW_MIDPOINT_ANCHOR_PT.getX(), actualAnchorPt.getX(), 0.0001);
		assertEquals("Expected new anchor pt y", NEW_MIDPOINT_ANCHOR_PT.getY(), actualAnchorPt.getY(), 0.0001);
	}

}
