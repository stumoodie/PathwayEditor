package org.pathwayeditor.visualeditor;

import java.awt.geom.Rectangle2D;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import org.apache.batik.dom.GenericDOMImplementation;
import org.apache.batik.dom.svg.SVGDOMImplementation;
import org.apache.batik.svggen.SVGGraphics2D;
import org.apache.batik.svggen.SVGGraphics2DIOException;
import org.apache.batik.transcoder.SVGAbstractTranscoder;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.PNGTranscoder;
import org.apache.log4j.Logger;
import org.pathwayeditor.businessobjects.drawingprimitives.properties.IBooleanPropertyDefinition;
import org.pathwayeditor.businessobjects.drawingprimitives.properties.IIntegerPropertyDefinition;
import org.pathwayeditor.businessobjects.drawingprimitives.properties.IListAnnotationProperty;
import org.pathwayeditor.businessobjects.drawingprimitives.properties.INumberAnnotationProperty;
import org.pathwayeditor.businessobjects.drawingprimitives.properties.INumberPropertyDefinition;
import org.pathwayeditor.businessobjects.drawingprimitives.properties.IPlainTextPropertyDefinition;
import org.pathwayeditor.businessobjects.drawingprimitives.properties.IPropertyDefinition;
import org.pathwayeditor.businessobjects.typedefn.IShapeAttributeDefaults;
import org.pathwayeditor.businessobjects.typedefn.IShapeObjectType;
import org.pathwayeditor.figure.definition.FigureDefinitionCompiler;
import org.pathwayeditor.figure.geometry.Dimension;
import org.pathwayeditor.figure.geometry.Envelope;
import org.pathwayeditor.figure.geometry.Point;
import org.pathwayeditor.figure.rendering.FigureRenderer;
import org.pathwayeditor.figure.rendering.FigureRenderingController;
import org.pathwayeditor.figure.rendering.GraphicsInstructionList;
import org.pathwayeditor.figure.rendering.IFigureRenderingController;
import org.pathwayeditor.figure.rendering.IGraphicsEngine;
import org.pathwayeditor.graphicsengine.Java2DGraphicsEngine;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;

public class ShapeIconGenerator {
	private final Logger logger = Logger.getLogger(this.getClass());
	private Document document;
	private SVGGraphics2D svgGenerator;
	private ImageIcon icon;
	private Envelope requestedBounds;
	private IShapeObjectType objectType;
	private Envelope actualBounds;

	public ShapeIconGenerator() {

	}
	
	
	
	public Envelope getRequestedBounds() {
		return requestedBounds;
	}



	public void setBounds(Envelope bounds) {
		this.requestedBounds = bounds;
	}



	public IShapeObjectType getObjectType() {
		return objectType;
	}



	public void setObjectType(IShapeObjectType objectType) {
		this.objectType = objectType;
	}



	public void generateImage() {
		this.document = null;
		this.icon = null;
		this.svgGenerator = null;
		DOMImplementation domImpl = GenericDOMImplementation.getDOMImplementation();

		// Create an instance of org.w3c.dom.Document.
		String svgNS = SVGDOMImplementation.SVG_NAMESPACE_URI;
		document = domImpl.createDocument(svgNS, "svg", null);
		svgGenerator = new SVGGraphics2D(document);
		IGraphicsEngine graphicsEngine = new Java2DGraphicsEngine(svgGenerator);
		FigureDefinitionCompiler compiler = new FigureDefinitionCompiler(objectType.getDefaultAttributes().getShapeDefinition());
		compiler.compile();
		IFigureRenderingController figureRenderingController = new FigureRenderingController(compiler.getCompiledFigureDefinition());
//		figureController.setRequestedEnvelope(requestedBounds.translate(new Point(20.0, 20.0)));
		figureRenderingController.setRequestedEnvelope(requestedBounds);
		IShapeAttributeDefaults attribute = objectType.getDefaultAttributes();
		figureRenderingController.setFillColour(attribute.getFillColour());
		figureRenderingController.setLineColour(attribute.getLineColour());
		figureRenderingController.setLineStyle(attribute.getLineStyle());
		figureRenderingController.setLineWidth(attribute.getLineWidth());
		assignBindVariablesToPropertyDefaults(attribute, figureRenderingController);
		figureRenderingController.generateFigureDefinition();
		this.actualBounds = figureRenderingController.getEnvelope();
		GraphicsInstructionList graphicsInstList = figureRenderingController.getFigureDefinition();
		FigureRenderer drawer = new FigureRenderer(graphicsInstList);
		drawer.drawFigure(graphicsEngine);
//		document.appendChild(svgGenerator.getRoot());
		document.normalizeDocument();
	}
	
	public void writeSVGToFile(File svgFile) throws IOException{
        Writer out = null;
        try {
        	out = new FileWriter(svgFile);
			svgGenerator.stream(out);
		} catch (SVGGraphics2DIOException e) {
			throw new IOException(e);
		}
		finally{
			if(out != null){
				out.close();
			}
		}
	}
	
	private void writeImageToStream(OutputStream os) throws TranscoderException, IOException{
		File tmpFile = File.createTempFile("test", ".svg");
		this.writeSVGToFile(tmpFile);
		PNGTranscoder t = new PNGTranscoder();
		Dimension size = this.actualBounds.getDimension();
    	t.addTranscodingHint(SVGAbstractTranscoder.KEY_WIDTH, new Float(size.getWidth()+2.0));
    	t.addTranscodingHint(SVGAbstractTranscoder.KEY_HEIGHT, new Float(size.getHeight()+2.0));
		Point origin = this.actualBounds.getOrigin();
    	t.addTranscodingHint(SVGAbstractTranscoder.KEY_AOI, new Rectangle2D.Double(origin.getX()-1.0, origin.getY()-1.0, size.getWidth()+2.0, size.getHeight()+2.0));
    	

    	// Set the transcoder input and output.
//    	TranscoderInput input = new TranscoderInput(document);
//    	input.setURI("http://www.pathwayeditor.org/ahd");
//    	TranscoderInput input = new TranscoderInput(document.getDocumentURI());
//    	Reader in = new CharArrayReader(out.toCharArray());
//    	TranscoderInput input = new TranscoderInput(in);
    	TranscoderInput input = new TranscoderInput(tmpFile.toURI().toString());
    	TranscoderOutput output = new TranscoderOutput(os);

    	// Perform the transcoding.
    	t.transcode(input, output);
		tmpFile.delete();
	}
	
	public void generateIcon(){
        try {
        	ByteArrayOutputStream ostream = new ByteArrayOutputStream();
			writeImageToStream(ostream);
	        ostream.flush();
	        ostream.close();
	        icon = new ImageIcon(ostream.toByteArray());
		} catch (TranscoderException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	public Icon getIcon(){
		return icon;
	}
	
	private void assignBindVariablesToPropertyDefaults(IShapeAttributeDefaults att, final IFigureRenderingController figureRenderingController) {
		for(final String varName : figureRenderingController.getBindVariableNames()){
			if(att.containsPropertyDefinition(varName)){
				IPropertyDefinition prop = att.getPropertyDefinition(varName);
				if(prop instanceof IBooleanPropertyDefinition){
					figureRenderingController.setBindBoolean(varName, ((IBooleanPropertyDefinition)prop).getDefaultValue());
				}
				else if(prop instanceof IIntegerPropertyDefinition){
					figureRenderingController.setBindInteger(varName, ((IIntegerPropertyDefinition)prop).getDefaultValue());
				}
				else if(prop instanceof IListAnnotationProperty){
					logger.error("Unmatched bind variable: " + varName + ". Property has type that cannot be matched to bind variable of same name: " + prop);
				}
				else if(prop instanceof INumberAnnotationProperty){
					figureRenderingController.setBindDouble(varName, ((INumberPropertyDefinition)prop).getDefaultValue().doubleValue());
				}
				else if(prop instanceof IPlainTextPropertyDefinition){
					figureRenderingController.setBindString(varName, ((IPlainTextPropertyDefinition)prop).getDefaultValue());
				}
			}
			else{
				logger.error("Unmatched bind variable: " + varName
						+ ". No property matched bind variable name was found.");
			}
		}
	}


	public void writeIconToFile(File testIconFile) {
		OutputStream os = null;
		try {
			os = new FileOutputStream(testIconFile);
			writeImageToStream(os);
			os.flush();
		} catch (IOException e) {
			throw new RuntimeException(e);
		} catch (TranscoderException e) {
			throw new RuntimeException(e);
		} finally {
			if (os != null) {
				try {
					os.close();
				} catch (IOException e) {
				}
			}
		}
	}
}
