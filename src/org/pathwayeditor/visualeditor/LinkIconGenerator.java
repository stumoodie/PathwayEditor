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
import org.pathwayeditor.businessobjects.typedefn.ILinkObjectType;
import org.pathwayeditor.figure.geometry.Dimension;
import org.pathwayeditor.figure.geometry.Envelope;
import org.pathwayeditor.figure.geometry.Point;
import org.pathwayeditor.figure.geometry.Scale;
import org.pathwayeditor.visualeditor.editingview.LinkDrawer;
import org.pathwayeditor.visualeditor.geometry.ILinkPointDefinition;
import org.pathwayeditor.visualeditor.geometry.LinkPointDefinition;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;

public class LinkIconGenerator {
//	private final Logger logger = Logger.getLogger(this.getClass());
	private Document document;
	private SVGGraphics2D svgGenerator;
	private ImageIcon icon;
	private Envelope requestedBounds;
	private ILinkObjectType objectType;

	public LinkIconGenerator() {

	}
	
	
	
	public Envelope getRequestedBounds() {
		return requestedBounds;
	}



	public void setBounds(Envelope bounds) {
		this.requestedBounds = bounds;
	}



	public ILinkObjectType getObjectType() {
		return objectType;
	}



	public void setObjectType(ILinkObjectType objectType) {
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
		ILinkPointDefinition linkDefn = new LinkPointDefinition(objectType, this.requestedBounds.getOrigin().translate(0.0, this.requestedBounds.getDimension().getHeight()/2.0),
				this.requestedBounds.getOrigin().translate(this.requestedBounds.getDimension().getWidth(), this.requestedBounds.getDimension().getHeight()/2.0));
		Dimension srcEndSize = adjustEndSize(this.requestedBounds.getDimension(), linkDefn.getSourceTerminusDefinition().getEndSize());
		linkDefn.getSourceTerminusDefinition().setEndSize(srcEndSize);
		Dimension tgtEndSize = adjustEndSize(this.requestedBounds.getDimension(), linkDefn.getTargetTerminusDefinition().getEndSize());
		linkDefn.getTargetTerminusDefinition().setEndSize(tgtEndSize);
		LinkDrawer drawer = new LinkDrawer(linkDefn);
		drawer.paint(svgGenerator);
		document.normalizeDocument();
	}
	
	private static Dimension adjustEndSize(Dimension boundsSize, Dimension srcEndSize){
		Dimension retVal = srcEndSize;
		if(srcEndSize.getHeight() > 0.0){
			Scale scale = new Scale(1.0, boundsSize.getHeight()/srcEndSize.getHeight());
			Dimension scaledSrcEndSize = srcEndSize.scale(scale);
			retVal = scaledSrcEndSize.newWidth(scaledSrcEndSize.getHeight());
		}
		else{
			retVal = new Dimension(0.0, 0.0);
		}
		return retVal;
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
//		File tmpFile = new File("test" + objectType.getName()+ ".svg");
		this.writeSVGToFile(tmpFile);
		PNGTranscoder t = new PNGTranscoder();
		Dimension size = this.requestedBounds.getDimension();
    	t.addTranscodingHint(SVGAbstractTranscoder.KEY_WIDTH, new Float(size.getWidth()+2.0));
    	t.addTranscodingHint(SVGAbstractTranscoder.KEY_HEIGHT, new Float(size.getHeight()+2.0));
		Point origin = this.requestedBounds.getOrigin();
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
