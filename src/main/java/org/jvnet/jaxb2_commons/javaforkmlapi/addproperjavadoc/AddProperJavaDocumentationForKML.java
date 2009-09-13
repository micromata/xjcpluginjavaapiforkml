package org.jvnet.jaxb2_commons.javaforkmlapi.addproperjavadoc;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;
import org.jvnet.jaxb2_commons.javaforkmlapi.ClazzPool;
import org.jvnet.jaxb2_commons.javaforkmlapi.CreateEqualsAndHashCode;
import org.jvnet.jaxb2_commons.javaforkmlapi.Util;
import org.jvnet.jaxb2_commons.javaforkmlapi.command.Command;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;

import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JFieldVar;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JType;
import com.sun.codemodel.JVar;
import com.sun.tools.xjc.Options;
import com.sun.tools.xjc.Plugin;
import com.sun.tools.xjc.generator.bean.ClassOutlineImpl;
import com.sun.tools.xjc.model.CClassInfo;
import com.sun.tools.xjc.model.CPropertyInfo;
import com.sun.tools.xjc.model.CTypeInfo;
import com.sun.tools.xjc.outline.Aspect;
import com.sun.tools.xjc.outline.ClassOutline;
import com.sun.tools.xjc.outline.EnumOutline;
import com.sun.tools.xjc.outline.FieldOutline;
import com.sun.tools.xjc.outline.Outline;
import com.sun.tools.xjc.reader.TypeUtil;

import de.micromata.javaapiforkml.JaxbTool;

/**
 * {@link Plugin}
 * 
 * @author Florian Bachmann
 */
public class AddProperJavaDocumentationForKML extends Command {
	private static final Logger LOG = Logger.getLogger(AddProperJavaDocumentationForKML.class.getName());

	public AddProperJavaDocumentationForKML(Outline outline, Options opts, ErrorHandler errorHandler, ClazzPool pool) {
		super(outline, opts, errorHandler, pool);

	}

	/** load from this file */
	public static String LOADJAVADOCSFROMFILE = "src/main/resources/schema/kmljavadocs.xml";

	@Override
	public void execute() {
		HashMap<String, ClassOutlineImpl> classList = Util.getClassList(outline);
		HashMap<String, ArrayList<CClassInfo>> subclasses = Util.findSubClasses(outline);

		HashMap<String, JaxbJavaDoc> kmlJavaDocElements = null;
		kmlJavaDocElements = getObjectsFromXmlWithJaxb(LOADJAVADOCSFROMFILE);

		if ((kmlJavaDocElements == null) || (kmlJavaDocElements.size() == 0)) {
			LOG.info("file to load (" + LOADJAVADOCSFROMFILE + ") not found. exit plugin");
			return;
		}

		LOG.info("trying to add javadocs to found enums:");
		for (final EnumOutline classOutline : outline.getEnums()) {
			JDefinedClass implClass = classOutline.clazz;
			String namewithoutType = implClass.name().toLowerCase().trim();

			namewithoutType = eliminateTypeSuffix(namewithoutType);
			// special case for ItemIconState
			if (namewithoutType.endsWith("state")) {
				namewithoutType = namewithoutType.substring(0, namewithoutType.length() - 5);
			}
			// special case for ListItemType
			if (namewithoutType.equals("listitem")) {
				namewithoutType = "listitemtype";
			}

			JaxbJavaDoc kmlJavaDocElement = kmlJavaDocElements.get(namewithoutType);
			if (kmlJavaDocElement == null) {
				LOG.info("- E> " + implClass.name());
				continue;
			}

			// LOG.info("+ E> " + namewithoutType);
			implClass.javadoc().clear();
			implClass.javadoc().append(kmlJavaDocElement.getJavaDoc());
		}

		LOG.info("trying to add javadocs to found classes:");
		for (final ClassOutline classOutline : outline.getClasses()) {
			JDefinedClass implClass = classOutline.implClass;
			String namewithoutType = classOutline.implClass.name().toLowerCase().trim();
			namewithoutType = eliminateTypeSuffix(namewithoutType);
			JaxbJavaDoc kmlJavaDocElement = kmlJavaDocElements.get(namewithoutType);
			if (kmlJavaDocElement == null) {
				// LOG.info("- C> " + classOutline.implClass.name());
				continue;
			}

			// LOG.info("+ C> " + namewithoutType);

			implClass.javadoc().clear();
			implClass.javadoc().append(kmlJavaDocElement.getJavaDoc());

			for (final FieldOutline fieldOutline : classOutline.getDeclaredFields()) {
				if (fieldOutline instanceof FieldOutline) {
					final CPropertyInfo property = fieldOutline.getPropertyInfo();

					final JCodeModel codeModel = classOutline.parent().getCodeModel();

					JFieldVar currentField = implClass.fields().get(property.getName(false));

					// LOG.info("fieldType: " + currentField.name());
					// find the common type
					final JType currentFieldType = TypeUtil
					    .getCommonBaseType(codeModel, listPossibleTypes((ClassOutlineImpl) classOutline, property));
					String currentFieldTypeName = currentFieldType.name().trim().toLowerCase();
					// if found field-type equals object, then there is no need, to document it
					if (currentFieldTypeName.equals("object")) {
						continue;
					}
					currentFieldTypeName = eliminateTypeSuffix(currentFieldTypeName);
					JaxbJavaDoc javadocForCurrentFieldName = kmlJavaDocElements.get(property.getName(false).trim().toLowerCase());
					if (javadocForCurrentFieldName != null) {
						// LOG.info("++C> " + currentFieldTypeName + " " + property.getName(false));
						currentField.javadoc().clear();
						currentField.javadoc().add(javadocForCurrentFieldName.getJavaDoc());
						continue;
					}

					JaxbJavaDoc javadocForCurrentField = kmlJavaDocElements.get(currentFieldTypeName);
					if (javadocForCurrentField != null) {
						// LOG.info("+  > " + currentFieldTypeName + " " + property.getName(false));
						currentField.javadoc().clear();
						currentField.javadoc().add(javadocForCurrentField.getJavaDoc());
						continue;
					}
					// LOG.info("--C> " + currentFieldTypeName + " " + property.getName(false));
				}
			}

			for (final JMethod jmethod : classOutline.implClass.methods()) {

				if (jmethod.name().equals("hashCode") || jmethod.name().equals("equals")) {
					// skip equals and hashcode methods
					continue;
				}

				if (jmethod.name().startsWith("with") || jmethod.name().startsWith("addTo")) {
					// skip equals and hashcode methods
					continue;
				}

				if (jmethod.name().startsWith("createAndAdd") || jmethod.name().startsWith("createAndSet")) {
					// skip equals and hashcode methods
					continue;
				}
				// LOG.info("--!> " + classOutline.implClass.name() + " !>" + jmethod.name());
				if (jmethod.name().startsWith("get") || jmethod.name().startsWith("set")) {
					String subSequence = jmethod.name().substring(3, jmethod.name().length());
					// if (classOutline.implRef._package().name())
					//				
				
					jmethod.javadoc().clear();
					// jmethod.javadoc().addDeprecated();
					jmethod.javadoc().add("@see " + Util.lowerFirst(subSequence));
				}
			}
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

	/*
	 * returns contents to be added to javadoc.
	 */
	private final ArrayList<JType> listPossibleTypes(final ClassOutlineImpl outline, final CPropertyInfo prop) {
		final ArrayList<JType> r = new ArrayList<JType>();
		for (final CTypeInfo tt : prop.ref()) {
			r.add(tt.getType().toType(outline.parent(), Aspect.EXPOSED));
		}
		return r;
	}

	private HashMap<String, JaxbJavaDoc> getObjectsFromXmlWithJaxb(String loadJavaDocsFromFile) {
		HashMap<String, JaxbJavaDoc> javaDocElements = null;
		JaxbTool<JaxbJavaDocElements> jaxt;
		JaxbJavaDocElements unmarshalledElements = null;

		Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
		jaxt = new JaxbTool<JaxbJavaDocElements>(JaxbJavaDocElements.class);

		unmarshalledElements = jaxt.unmarshal(new File(loadJavaDocsFromFile));
		LOG.info("Elements loaded: " + unmarshalledElements.getElements().size());

		javaDocElements = convertArrayListToHashMap(unmarshalledElements.getElements());

		return javaDocElements;
	}

	/*
	 * private helper funtion needed by JAXB to convert an ArraylLst into an HashMap (JAXB's ability to marhall Hashmaps aren't that good)
	 * 
	 * @param arraylist the given HashMap
	 * 
	 * @return the newly created HashMap
	 */
	private static HashMap<String, JaxbJavaDoc> convertArrayListToHashMap(List<JaxbJavaDoc> arraylist) {
		HashMap<String, JaxbJavaDoc> hashmap = new HashMap<String, JaxbJavaDoc>();
		for (JaxbJavaDoc jaxbJavaDoc : arraylist) {
			hashmap.put(jaxbJavaDoc.getClassName(), jaxbJavaDoc);
		}
		return hashmap;
	}
}
