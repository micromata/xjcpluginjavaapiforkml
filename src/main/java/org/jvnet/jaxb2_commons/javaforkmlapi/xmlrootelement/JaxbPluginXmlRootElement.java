package org.jvnet.jaxb2_commons.javaforkmlapi.xmlrootelement;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.apache.log4j.Logger;
import org.jvnet.jaxb2_commons.javaforkmlapi.ClazzPool;
import org.jvnet.jaxb2_commons.javaforkmlapi.ConvertComplexTypesToSimpleTypes;
import org.jvnet.jaxb2_commons.javaforkmlapi.command.Command;
import org.xml.sax.ErrorHandler;

import com.sun.codemodel.JCodeModel;
import com.sun.tools.xjc.BadCommandLineException;
import com.sun.tools.xjc.Options;
import com.sun.tools.xjc.Plugin;
import com.sun.tools.xjc.generator.annotation.spec.XmlRootElementWriter;
import com.sun.tools.xjc.generator.bean.ClassOutlineImpl;
import com.sun.tools.xjc.outline.ClassOutline;
import com.sun.tools.xjc.outline.EnumOutline;
import com.sun.tools.xjc.outline.Outline;

import de.micromata.javaapiforkml.JaxbTool;


/**
 * {@link Plugin} that adds the XMLRootElementAnnotation to selected classes.
 * 
 * @author Florian Bachmann
 */
	public class JaxbPluginXmlRootElement extends Command {
		private static final Logger LOG = Logger.getLogger(ConvertComplexTypesToSimpleTypes.class.getName());

		private JCodeModel cm;

		private int replacedTypes;
		/** load from this file */
		public static String LOADJAVADOCSFROMFILE = "src/main/resources/schema/kmljavadocs.xml";

		private HashMap<String, String> quickAndSimpleAnnotateTheseElementsWithXmlRootElement;

		
		public JaxbPluginXmlRootElement(Outline outline, Options opts, ErrorHandler errorHandler, ClazzPool pool) {
			super(outline, opts, errorHandler, pool);

			cm = outline.getCodeModel();
		}

		@Override
		public void execute() {

//		quickAndSimpleAnnotateTheseElementsWithXmlRootElement = getObjectsFromXmlWithJaxb(LOADJAVADOCSFROMFILE);

//		if ((quickAndSimpleAnnotateTheseElementsWithXmlRootElement == null) || (quickAndSimpleAnnotateTheseElementsWithXmlRootElement.size() == 0)) {
			LOG.info("file to load (" + LOADJAVADOCSFROMFILE + ") not found. ");
			LOG.info("using hard-coded values.");

			quickAndSimpleAnnotateTheseElementsWithXmlRootElement = new HashMap<String, String>();
			quickAndSimpleAnnotateTheseElementsWithXmlRootElement.put("camera", "Camera");
			quickAndSimpleAnnotateTheseElementsWithXmlRootElement.put("document", "Document");
			quickAndSimpleAnnotateTheseElementsWithXmlRootElement.put("folder", "Folder");
			quickAndSimpleAnnotateTheseElementsWithXmlRootElement.put("groundoverlay", "GroundOverlay");
			quickAndSimpleAnnotateTheseElementsWithXmlRootElement.put("kml", "kml");
			quickAndSimpleAnnotateTheseElementsWithXmlRootElement.put("linearring", "LienarRing");
			quickAndSimpleAnnotateTheseElementsWithXmlRootElement.put("linestring", "LineString");
			quickAndSimpleAnnotateTheseElementsWithXmlRootElement.put("lookat", "LookAt");
			quickAndSimpleAnnotateTheseElementsWithXmlRootElement.put("model", "Model");
			quickAndSimpleAnnotateTheseElementsWithXmlRootElement.put("multigeometry", "MultiGeometry");
			quickAndSimpleAnnotateTheseElementsWithXmlRootElement.put("networklink", "NetworkLink");
			quickAndSimpleAnnotateTheseElementsWithXmlRootElement.put("photooverlay", "PhotoOverlay");
			quickAndSimpleAnnotateTheseElementsWithXmlRootElement.put("placemark", "Placemark");
			quickAndSimpleAnnotateTheseElementsWithXmlRootElement.put("point", "Point");
			quickAndSimpleAnnotateTheseElementsWithXmlRootElement.put("polygon", "Polygon");
			quickAndSimpleAnnotateTheseElementsWithXmlRootElement.put("screenoverlay", "ScreenOverlay");
			quickAndSimpleAnnotateTheseElementsWithXmlRootElement.put("stylemap", "StyleMap");
			quickAndSimpleAnnotateTheseElementsWithXmlRootElement.put("style", "Style");
			quickAndSimpleAnnotateTheseElementsWithXmlRootElement.put("timespan", "TimeSpan");
			quickAndSimpleAnnotateTheseElementsWithXmlRootElement.put("timestamp", "TimeStamp");

			//needed for tests
			quickAndSimpleAnnotateTheseElementsWithXmlRootElement.put("balloonstyle", "BalloonStyle");
			quickAndSimpleAnnotateTheseElementsWithXmlRootElement.put("labelstyle", "LabelStyle");
			quickAndSimpleAnnotateTheseElementsWithXmlRootElement.put("extendeddata", "ExtendedData");
			quickAndSimpleAnnotateTheseElementsWithXmlRootElement.put("link", "Link");
			quickAndSimpleAnnotateTheseElementsWithXmlRootElement.put("latlonbox", "LatLonBox");
			quickAndSimpleAnnotateTheseElementsWithXmlRootElement.put("iconstyle", "IconStyle");
			quickAndSimpleAnnotateTheseElementsWithXmlRootElement.put("linestyle", "LineStyle");
			quickAndSimpleAnnotateTheseElementsWithXmlRootElement.put("liststyle", "ListStyle");
			quickAndSimpleAnnotateTheseElementsWithXmlRootElement.put("location", "Location");
			quickAndSimpleAnnotateTheseElementsWithXmlRootElement.put("orientation", "Orientation");
			quickAndSimpleAnnotateTheseElementsWithXmlRootElement.put("scale", "Scale");
			quickAndSimpleAnnotateTheseElementsWithXmlRootElement.put("alias", "Alias");
			quickAndSimpleAnnotateTheseElementsWithXmlRootElement.put("networklinkcontrol", "NetworkLinkControl");
			quickAndSimpleAnnotateTheseElementsWithXmlRootElement.put("icon", "Icon");
			quickAndSimpleAnnotateTheseElementsWithXmlRootElement.put("polystyle", "PolyStyle");
			quickAndSimpleAnnotateTheseElementsWithXmlRootElement.put("latlonaltbox", "LatLonAltBox");
			quickAndSimpleAnnotateTheseElementsWithXmlRootElement.put("lod", "Lod");
			quickAndSimpleAnnotateTheseElementsWithXmlRootElement.put("region", "Region");
			quickAndSimpleAnnotateTheseElementsWithXmlRootElement.put("schema", "Schema");
			quickAndSimpleAnnotateTheseElementsWithXmlRootElement.put("pair", "Pair");
			quickAndSimpleAnnotateTheseElementsWithXmlRootElement.put("networkLinkcontrol", "NetworkLinkControl");
			quickAndSimpleAnnotateTheseElementsWithXmlRootElement.put("update", "Update");
			quickAndSimpleAnnotateTheseElementsWithXmlRootElement.put("region", "Region");

			// seperate the enums!?!?!?
			quickAndSimpleAnnotateTheseElementsWithXmlRootElement.put("altitudemode", "altitudeMode");
//		}

		for (final EnumOutline classOutline : outline.getEnums()) {
			annotateEnums(classOutline);
		}

		for (final ClassOutline classOutline : outline.getClasses()) {
			annotateClasses((ClassOutlineImpl) classOutline);
		}

	}

	private String eliminateTypeSuffix(String namewithoutType) {
		// if ($currentJavaFile[$i] =~ s/(.*?<)(JAXBElement<. extends )(.*?)(>)(.*?)/$1$3$5/g) {
		// if ($currentJavaFile[$i] =~ s/(.*?)(JAXBElement<\? extends )(.*?)(>)(.*?)/$1$3$5/g) {
		// if ($currentJavaFile[$i] =~ s/(.*?)(JAXBElement<)(.*?)(>)(.*?)/$1$3$5/g) {
		namewithoutType = namewithoutType.replaceAll("(.*?)(jaxbelement<. extends )(.*?)(>)(.*?)", "$1$3$5");

		if (namewithoutType.endsWith("type")) {
			namewithoutType = namewithoutType.substring(0, namewithoutType.length() - 4);
		}
		if (namewithoutType.endsWith("enum")) {
			namewithoutType = namewithoutType.substring(0, namewithoutType.length() - 4);
		}
		if (namewithoutType.startsWith("abstract")) {
			namewithoutType = namewithoutType.substring(8, namewithoutType.length());
		}
		return namewithoutType;
	}

	private void annotateEnums(final EnumOutline classOutline) {

		String currentClassName = eliminateTypeSuffix(classOutline.clazz.name().toLowerCase());
		// LOG.info("[JaxbPluginXmlRootElement] " + currentClassName );
		if (quickAndSimpleAnnotateTheseElementsWithXmlRootElement.containsKey(currentClassName)) {
			if (classOutline.target.isElement()) {
				LOG.info("> skipped " + classOutline.target.shortName + " because class is already annotated.");
				return;
			}

			String mostUsedNamespaceURI = classOutline._package().getMostUsedNamespaceURI();
			String xmlRootElementName = quickAndSimpleAnnotateTheseElementsWithXmlRootElement.get(currentClassName);
			// [RESULT]
			// @XmlRootElement(name = "foo", namespace = "bar://baz")
			XmlRootElementWriter xrew = classOutline.clazz.annotate2(XmlRootElementWriter.class);
			xrew.name(xmlRootElementName);
			xrew.namespace(mostUsedNamespaceURI);
			LOG.info("> added @XmlRootElement(name = '" + xmlRootElementName + "', namespace = '" + mostUsedNamespaceURI + "')");
			// LOG.info("> added @XmlRootElement(name = '" + xmlRootElementName + "')");
		}
	}

	private void annotateClasses(final ClassOutlineImpl cc) {
		String currentClassName = eliminateTypeSuffix(cc.implRef.name().toLowerCase());
		// LOG.info("[JaxbPluginXmlRootElement] " + currentClassName );
		if (quickAndSimpleAnnotateTheseElementsWithXmlRootElement.containsKey(currentClassName)) {
			if (cc.target.isElement()) {
				LOG.info("> skipped " + cc.target.getName() + " because class is already annotated.");
				return;
			}

			String mostUsedNamespaceURI = cc._package().getMostUsedNamespaceURI();
			String xmlRootElementName = quickAndSimpleAnnotateTheseElementsWithXmlRootElement.get(currentClassName);
			// [RESULT]
			// @XmlRootElement(name = "foo", namespace = "bar://baz")
			XmlRootElementWriter xrew = cc.implClass.annotate2(XmlRootElementWriter.class);
			xrew.name(xmlRootElementName);
			xrew.namespace(mostUsedNamespaceURI);
			LOG.info("> added @XmlRootElement(name = '" + xmlRootElementName + "', namespace = '" + mostUsedNamespaceURI + "')");
			// LOG.info("> added @XmlRootElement(name = '" + xmlRootElementName + "')");
		}
	}

	private HashMap<String, String> getObjectsFromXmlWithJaxb(String loadJavaDocsFromFile) {
		HashMap<String, String> xmlRootElements = null;
		JaxbTool<JaxbXmlRootElements> jaxt;
    JaxbXmlRootElements unmarshalledElements = null;

    Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
    jaxt = new JaxbTool<JaxbXmlRootElements>(JaxbXmlRootElements.class);

    unmarshalledElements = jaxt.unmarshal(new File(loadJavaDocsFromFile));
    LOG.info("elements2 size: " + unmarshalledElements.getElements().size());

    xmlRootElements = convertArrayListToHashMap(unmarshalledElements.getElements());

		return xmlRootElements;
	}

	/*
	 * private helper funtion needed by JAXB to convert an ArraylLst into an HashMap (JAXB's ability to marhall Hashmaps aren't that good)
	 * 
	 * @param arraylist the given HashMap
	 * 
	 * @return the newly created HashMap
	 */
	private static HashMap<String, String> convertArrayListToHashMap(List<JaxbXmlRootElement> arraylist) {
		HashMap<String, String> hashmap = new HashMap<String, String>();
		for (JaxbXmlRootElement jaxbJavaDoc : arraylist) {
			hashmap.put(jaxbJavaDoc.getLowerCaseClassName().toLowerCase().trim(), jaxbJavaDoc.getNameInKmlFile());
		}
		return hashmap;
	}
}
