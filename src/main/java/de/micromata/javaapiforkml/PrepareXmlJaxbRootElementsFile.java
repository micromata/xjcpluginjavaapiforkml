package de.micromata.javaapiforkml;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.jvnet.jaxb2_commons.javaforkmlapi.xmlrootelement.JaxbPluginXmlRootElement;
import org.jvnet.jaxb2_commons.javaforkmlapi.xmlrootelement.JaxbXmlRootElement;
import org.jvnet.jaxb2_commons.javaforkmlapi.xmlrootelement.JaxbXmlRootElements;


/**
 * helper class, that parses the kml-reference and convert it into a appropriate data-format.
 */
public class PrepareXmlJaxbRootElementsFile {
	private static final Logger LOG = Logger.getLogger(PrepareXmlJaxbRootElementsFile.class.getName());

	private static HashMap<String, String> quickAndSimpleAnnotateTheseElementsWithXmlRootElement;

	/**
	 * 
	 * @param kmlReferenceHtmlPage
	 * @param saveToFile
	 * @return
	 * @throws MalformedURLException
	 * @throws IOException
	 */
	private static void buildDocumentationFromFoundElements(final String saveToFile) {
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

		saveJavaDocObjectIntoXmlFile(saveToFile, quickAndSimpleAnnotateTheseElementsWithXmlRootElement);

	}

	/**
	 * generates the xsd-schema-file and the xml-file with the foud JavaDocs
	 * 
	 * @param javadocElements
	 */
	private static void saveJavaDocObjectIntoXmlFile(final String saveToFile, final HashMap<String, String> javadocElements) {
		if ((javadocElements == null) || (javadocElements.size() == 0)) {
			return;
		}
		final JaxbXmlRootElements jdel = new JaxbXmlRootElements();
		final ArrayList<JaxbXmlRootElement> elementsjavadoc = convertHashMapToArrayList(javadocElements);
		jdel.setElements(elementsjavadoc);

		JaxbTool<JaxbXmlRootElements> jaxt;
		try {
			jaxt = new JaxbTool<JaxbXmlRootElements>(JaxbXmlRootElements.class);
			final String schemaFileName = saveToFile.replaceAll("(.*?)(.xml)$", "$1.xsd");
			jaxt.generateSchema(schemaFileName);
			LOG.info("------------ written Schema-file:  " + schemaFileName);

			jaxt.marshal(jdel, new FileOutputStream(saveToFile));
			LOG.info("------------ written JavaDoc-file: " + saveToFile);
		} catch (final Exception e) {
			e.printStackTrace();
		}
	}

	/*
	 * private helper funtion needed by JAXB to convert a HashMap into an ArraylLst (JAXB's ability to marshall HashMaps aren't that good)
	 * 
	 * @param hashmap the given HashMap
	 * 
	 * @return the newly created ArrayList
	 */
	private static ArrayList<JaxbXmlRootElement> convertHashMapToArrayList(final HashMap<String, String> hashmap) {
		final ArrayList<JaxbXmlRootElement> elementsjavadoc = new ArrayList<JaxbXmlRootElement>();
		for (final Entry<String, String> arraylist : hashmap.entrySet()) {
			elementsjavadoc.add(new JaxbXmlRootElement(arraylist.getKey(), arraylist.getValue()));
		}
		return elementsjavadoc;
	}

	public static void main(final String[] args) throws Exception {
		BasicConfigurator.configure();
		PrepareXmlJaxbRootElementsFile.buildDocumentationFromFoundElements(JaxbPluginXmlRootElement.LOADJAVADOCSFROMFILE);
	}
}
