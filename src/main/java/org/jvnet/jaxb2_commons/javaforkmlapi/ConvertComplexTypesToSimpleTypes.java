package org.jvnet.jaxb2_commons.javaforkmlapi;

import java.util.List;

import org.apache.log4j.Logger;
import org.jvnet.jaxb2_commons.javaforkmlapi.command.Command;
import org.xml.sax.ErrorHandler;

import com.sun.codemodel.JClass;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JFieldVar;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JPrimitiveType;
import com.sun.codemodel.JType;
import com.sun.codemodel.JVar;
import com.sun.tools.xjc.Options;
import com.sun.tools.xjc.generator.bean.ClassOutlineImpl;
import com.sun.tools.xjc.outline.ClassOutline;
import com.sun.tools.xjc.outline.FieldOutline;
import com.sun.tools.xjc.outline.Outline;
import com.sun.tools.xjc.reader.TypeUtil;

/**
 * 
 * 
 * @author Florian Bachmann
 */
public class ConvertComplexTypesToSimpleTypes extends Command {
	private static final Logger LOG = Logger.getLogger(ConvertComplexTypesToSimpleTypes.class.getName());

	private JCodeModel cm;

	private int replacedTypes;

	public ConvertComplexTypesToSimpleTypes(Outline outline, Options opts, ErrorHandler errorHandler, ClazzPool pool) {
		super(outline, opts, errorHandler, pool);

		cm = outline.getCodeModel();
	}

	@Override
	public void execute() {
		for (final ClassOutline classOutline : outline.getClasses()) {
			convertComplexTypesToSimpleTypes((ClassOutlineImpl) classOutline);
		}

	}

	private void convertComplexTypesToSimpleTypes(final ClassOutlineImpl cc) {
		final JDefinedClass implClass = cc.implClass;
		// if no fields are present return
		if (implClass.fields().isEmpty()) {
			return;
		}
		JType stringClass = cm._ref(String.class);
		replacedTypes = 0;
		replaceComplexTypes(implClass, "java.lang.Double", cm.DOUBLE);
		replaceComplexTypes(implClass, "double", cm.DOUBLE);
		replaceComplexTypes(implClass, "java.lang.Integer", cm.INT);
		replaceComplexTypes(implClass, "int", cm.INT);
		replaceComplexTypes(implClass, "byte[]", stringClass);

		if (replacedTypes == 0) {
			return;
		}

		LOG.info(implClass.name() + " types replaced: " + replacedTypes);
	}

	private void replaceComplexTypes(JDefinedClass implClass, String lookForType, JType replaceWithThisType) {

		for (final JFieldVar jFieldVar : implClass.fields().values()) {
			if (jFieldVar.type().fullName().equals(lookForType)) {
				jFieldVar.type(replaceWithThisType);
				replacedTypes++;
				// LOG.info("jFieldVar:   " + jFieldVar.name());
				// LOG.info("jFieldVar is " + jFieldVar.type().fullName());
			}
		}

		for (final JMethod jFieldVar : implClass.methods()) {
			if (jFieldVar.type().fullName().equals(lookForType)) {
				jFieldVar.type(replaceWithThisType);
				// LOG.info("JMethod:   " + jFieldVar.name());
				// LOG.info("JMethod is " + jFieldVar.type().fullName());
				replacedTypes++;
			}

			for (JVar jParams : jFieldVar.listParams()) {
				if (jParams.type().fullName().equals(lookForType)) {
					jParams.type(replaceWithThisType);
					// LOG.info("JMethod Param:    " + jParams.name());
					// LOG.info("JMethod is Param " + jParams.type().fullName());
					replacedTypes++;
				}
			}
		}
	}

}
