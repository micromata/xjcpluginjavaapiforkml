package org.jvnet.jaxb2_commons.javaforkmlapi.xmlrootelement;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.List;

import javax.xml.bind.JAXBElement;
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
import com.sun.tools.xjc.outline.PackageOutline;

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

		private HashMap<String, String> annotateTheseElementsDifferent;

		
		public JaxbPluginXmlRootElement(Outline outline, Options opts, ErrorHandler errorHandler, ClazzPool pool) {
			super(outline, opts, errorHandler, pool);
			Iterable< ? extends PackageOutline> allPackageContexts = this.outline.getAllPackageContexts();
			for (PackageOutline packageOutline : allPackageContexts) {
	     
	      
	      LOG.info("f<<<<>>>>>>>>>>>>>>>>" + packageOutline.getMostUsedNamespaceURI() );
      }
			cm = outline.getCodeModel();
		}

		@Override
		public void execute() {

//		quickAndSimpleAnnotateTheseElementsWithXmlRootElement = getObjectsFromXmlWithJaxb(LOADJAVADOCSFROMFILE);

//		if ((quickAndSimpleAnnotateTheseElementsWithXmlRootElement == null) || (quickAndSimpleAnnotateTheseElementsWithXmlRootElement.size() == 0)) {
			LOG.info("file to load (" + LOADJAVADOCSFROMFILE + ") not found. ");
			LOG.info("using hard-coded values.");

			annotateTheseElementsDifferent = new HashMap<String, String>();
			annotateTheseElementsDifferent.put("kml", "kml");
			annotateTheseElementsDifferent.put("link", "Link");
			annotateTheseElementsDifferent.put("icon", "Icon");
			//annotateTheseElementsDifferent.put("altitudemode", "altitudeMode");
//		}

		for (final EnumOutline classOutline : outline.getEnums()) {
		//	annotateEnums(classOutline);
		}

		for (final ClassOutline classOutline : outline.getClasses()) {
			annotateClasses((ClassOutlineImpl) classOutline);
		}

	}

	private String eliminateTypeSuffixLowercase(String namewithoutType) {
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
	

	private String eliminateTypeSuffixCaseSensitive(String namewithoutType) {
		// if ($currentJavaFile[$i] =~ s/(.*?<)(JAXBElement<. extends )(.*?)(>)(.*?)/$1$3$5/g) {
		// if ($currentJavaFile[$i] =~ s/(.*?)(JAXBElement<\? extends )(.*?)(>)(.*?)/$1$3$5/g) {
		// if ($currentJavaFile[$i] =~ s/(.*?)(JAXBElement<)(.*?)(>)(.*?)/$1$3$5/g) {
		namewithoutType = namewithoutType.replaceAll("(.*?)(JAXBElement<. extends )(.*?)(>)(.*?)", "$1$3$5");

		if (namewithoutType.endsWith("Type")) {
			namewithoutType = namewithoutType.substring(0, namewithoutType.length() - 4);
		}
		if (namewithoutType.endsWith("Enum")) {
			namewithoutType = namewithoutType.substring(0, namewithoutType.length() - 4);
		}
		if (namewithoutType.startsWith("Abstract")) {
			namewithoutType = namewithoutType.substring(8, namewithoutType.length());
		}
		return namewithoutType;
	}


	private void annotateEnums(final EnumOutline cc) {

		String currentClassName = eliminateTypeSuffixCaseSensitive(cc.clazz.name().toLowerCase());
		// LOG.info("[JaxbPluginXmlRootElement] " + currentClassName );
		//if (quickAndSimpleAnnotateTheseElementsWithXmlRootElement.containsKey(currentClassName)) {
			if (cc.target.isElement()) {
				LOG.info("> skipped " + cc.target.shortName + " because class is already annotated.");
				return;
			}

			String xmlRootElementName = null;
			if (annotateTheseElementsDifferent.containsKey(currentClassName)) {
				xmlRootElementName = annotateTheseElementsDifferent.get(currentClassName);
			} else {
				xmlRootElementName = eliminateTypeSuffixCaseSensitive(cc.clazz.name());
			}	
			
			String mostUsedNamespaceURI = cc._package().getMostUsedNamespaceURI();
			// [RESULT]
			// @XmlRootElement(name = "foo", namespace = "bar://baz")
			XmlRootElementWriter xrew = cc.clazz.annotate2(XmlRootElementWriter.class);
			xrew.name(xmlRootElementName);
			//xrew.namespace(mostUsedNamespaceURI);
			LOG.info("E> added @XmlRootElement(name = '" + xmlRootElementName + "', namespace = '" + mostUsedNamespaceURI + "')");
			// LOG.info("> added @XmlRootElement(name = '" + xmlRootElementName + "')");
		//}
	}

	private void annotateClasses(final ClassOutlineImpl cc) {
		String currentClassName = eliminateTypeSuffixLowercase(cc.implRef.name().toLowerCase());
		// LOG.info("[JaxbPluginXmlRootElement] " + currentClassName );
			if (cc.target.isElement()) {
				LOG.info("> skipped " + cc.target.getName() + " because class is already annotated.");
				return;
			}
			
			if (cc.target.isAbstract()) {
				LOG.info("> skipped " + cc.target.getName() + " because class is abstract.");
				return;
			}
			
//			cc.target.get

			String xmlRootElementName = null;
			if (annotateTheseElementsDifferent.containsKey(currentClassName)) {
				xmlRootElementName = annotateTheseElementsDifferent.get(currentClassName);
			} else {
				xmlRootElementName = eliminateTypeSuffixCaseSensitive(cc.implRef.name());
			}	
			String mostUsedNamespaceURI = cc._package().getMostUsedNamespaceURI();
			// [RESULT]
			// @XmlRootElement(name = "foo", namespace = "bar://baz")
			XmlRootElementWriter xrew = cc.implClass.annotate2(XmlRootElementWriter.class);
			xrew.name(xmlRootElementName);
			//if (!mostUsedNamespaceURI.equals("http://www.opengis.net/kml/2.2")) {
			xrew.namespace(mostUsedNamespaceURI);
			//}
			LOG.info("C> added @XmlRootElement(name = '" + xmlRootElementName + "', namespace = '" + mostUsedNamespaceURI + "')");
			// LOG.info("> added @XmlRootElement(name = '" + xmlRootElementName + "')");
		
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
