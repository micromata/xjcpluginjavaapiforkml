package org.jvnet.jaxb2_commons.javaforkmlapi.tostring;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

import org.apache.log4j.Logger;
import org.jvnet.jaxb2_commons.javaforkmlapi.ClazzPool;
import org.jvnet.jaxb2_commons.javaforkmlapi.command.Command;
import org.xml.sax.ErrorHandler;

import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JFieldVar;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JMod;
import com.sun.tools.xjc.Options;
import com.sun.tools.xjc.Plugin;
import com.sun.tools.xjc.generator.bean.ClassOutlineImpl;
import com.sun.tools.xjc.model.CAttributePropertyInfo;
import com.sun.tools.xjc.model.CClassInfo;
import com.sun.tools.xjc.model.CPropertyInfo;
import com.sun.tools.xjc.model.CReferencePropertyInfo;
import com.sun.tools.xjc.outline.ClassOutline;
import com.sun.tools.xjc.outline.Outline;

/**
 * {@link Plugin} This plugin modifies XJC's code generation behavior so that you get an equals() and a hashCode()-method in each genereated
 * class (as descripted in Bloch: Effective Java 2nd Edt. (page 33-50)).
 * 
 * @author Florian Bachmann
 */
public class CreateToString extends Command {
	private static final Logger LOG = Logger.getLogger(CreateToString.class.getName());

	public CreateToString(final Outline outline, final Options opts, final ErrorHandler errorHandler, final ClazzPool pool) {
		super(outline, opts, errorHandler, pool);
	}

	@Override
	public void execute() {
		for (final ClassOutline classOutline : outline.getClasses()) {
			createToString(classOutline.implClass, classOutline);
		}

	}

	public static void createToString(final JDefinedClass implClass, final ClassOutline classOutline) {
		// if no fields are present return

		// classOutline.target.model.

		// classOutline.target.getAnnotations();
		if (implClass.name().equals("Coordinate")) {
			return;
		}

		if (implClass.fields().isEmpty()) {
			return;
		}

		final CClassInfo target = classOutline.target;
		final ArrayList<String> props = new ArrayList<String>();
		if (target.isOrdered()) {
			for (final CPropertyInfo p : target.getProperties()) {
				if (!(p instanceof CAttributePropertyInfo)) {
					if (!((p instanceof CReferencePropertyInfo) && ((CReferencePropertyInfo) p).isDummy())) {
						props.add(p.getName(false));
					}
				}
			}
		}
		
		// check for suitable fields
		final ArrayList<JFieldVar> fields = getRelevantFields(implClass);
		final HashSet<JFieldVar> fff = new HashSet<JFieldVar>();
		for (final JFieldVar jFieldVar : fields) {
			fff.add(jFieldVar);
		}

		// generate only if suitable fields exist
		if (fields.size() == 0) {
			return;
		}
		LOG.info("generate toString field#: " + implClass.name() + " (" + fields.size() + ")");

		// generate hashCode() and equals()-method
		final JMethod toString = implClass.method(JMod.PUBLIC, String.class, "toString");

		boolean isSuperClass = false;
		if (classOutline != null) {
			isSuperClass = checkForSuperClass((ClassOutlineImpl) classOutline);
		}
		// annotate with @Override
		toString.annotate(Override.class);

		for (final JFieldVar jFieldVar : fields) {
			LOG.info("!!!!!!!!!!!!!!!!!!!!! " + jFieldVar.name());
		}

		
		toString.body()._return(JExpr.lit("null"));
	}

	/*
	 * check if a superclass exist and if it defines some hashCode- and equals-worthy fields
	 */
	private static boolean checkForSuperClass(final ClassOutlineImpl classOutline) {
		if (classOutline.getSuperClass() == null) {
			return false;
		}

		if (getRelevantFields(classOutline.getSuperClass().implClass).size() == 0) {
			return false;
		}

		return true;
	}

	/*
	 * returns all fields that could be used in hashCode and equals
	 */
	private static ArrayList<JFieldVar> getRelevantFields(final JDefinedClass implClass) {
		final ArrayList<JFieldVar> fields = new ArrayList<JFieldVar>();
		int mods = 0;
		for (final JFieldVar field : implClass.fields().values()) {
			mods = field.mods().getValue();

			if ((mods & JMod.PRIVATE) != 0) {
				continue;
			}
			if ((mods & JMod.STATIC) != 0) {
				continue;
			}
			fields.add(field);
		}
		return fields;
	}

	private List<Field> getFieldsWith(Class clazz, Class annotation) {
		Field[] fields = clazz.getDeclaredFields();
		List<Field> fList = new ArrayList<Field>();
		for (Field f : fields) {
			Annotation[] annos = f.getDeclaredAnnotations();
			for (Annotation a : annos) {
				if (annotation.isInstance(a)) {
					fList.add(f);
					break;
				}
			}
		}
		return fList;
	}

	private Map<String, String> getSchemaNames(Class clazz) throws Exception {
		Field[] fields = clazz.getDeclaredFields();
		Map<String, String> field2tag = new HashMap<String, String>();
		for (Field f : fields) {
			Annotation[] annos = f.getDeclaredAnnotations();
			for (Annotation a : annos) {
				if (a instanceof XmlElement || a instanceof XmlAttribute) {
					Class annoClass = a.annotationType();
					String tName = (String) annoClass.getMethod("name").invoke(a);
					field2tag.put(f.getName(), tName);
				}
			}
		}
		return field2tag;
	}

}
