// ///////////////////////////////////////////////////////////////////////////
//
// $RCSfile: $
//
// Project JaxbPluginJavaForKmlApi
//
// Author Flori (f.bachmann@micromata.de)
// Created 20.03.2009
// Copyright Micromata 20.03.2009
//
// $Id: $
// $Revision: $
// $Date: $
//
// ///////////////////////////////////////////////////////////////////////////
package org.jvnet.jaxb2_commons.javaforkmlapi.fluent;

import java.util.Collection;
import java.util.Map;

import org.apache.log4j.Logger;
import org.jvnet.jaxb2_commons.javaforkmlapi.ClazzPool;
import org.jvnet.jaxb2_commons.javaforkmlapi.Util;
import org.jvnet.jaxb2_commons.javaforkmlapi.XJCJavaForKmlApiPlugin;
import org.jvnet.jaxb2_commons.javaforkmlapi.command.Command;
import org.xml.sax.ErrorHandler;

import com.sun.codemodel.JExpr;
import com.sun.codemodel.JFieldVar;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JMod;
import com.sun.codemodel.JVar;
import com.sun.tools.xjc.Options;
import com.sun.tools.xjc.generator.bean.ClassOutlineImpl;
import com.sun.tools.xjc.outline.ClassOutline;
import com.sun.tools.xjc.outline.FieldOutline;
import com.sun.tools.xjc.outline.Outline;

public class CreateConstructors extends Command {
	private static final Logger LOG = Logger.getLogger(CreateConstructors.class.getName());

	public CreateConstructors(final Outline outline, final Options opts, final ErrorHandler errorHandler, final ClazzPool pool) {
		super(outline, opts, errorHandler, pool);
	}

	@Override
	public void execute() {
		LOG.info(XJCJavaForKmlApiPlugin.PLUGINNAME + " generate Overloaded Constructors for required fields.");
		for (final ClassOutline classOutline : outline.getClasses()) {
			final ClassOutlineImpl cc = (ClassOutlineImpl) classOutline;
			if (cc.implClass.name().equals("Coordinate")) {
				LOG.info(XJCJavaForKmlApiPlugin.PLUGINNAME + " skip Coordinate standard constructors and create custom ones");
				continue;
			}
			final Collection<JFieldVar> requiredConstructorFields = Util.getConstructorRequiredFields(cc);
			int mods = JMod.PUBLIC;
			if (requiredConstructorFields.size() > 0) {
				createArgConstructor(cc, requiredConstructorFields);
				mods = JMod.PRIVATE;
				// continue;
			}

			createNoArgConstructor(cc, mods);
		}
	}

	private void createNoArgConstructor(final ClassOutline classOutline, final int mods) {
		// Create the default, no-arg constructor
		final JMethod defaultConstructor = classOutline.implClass.constructor(mods);
		if (mods == JMod.PRIVATE) {
			defaultConstructor.annotate(Deprecated.class);
			defaultConstructor.javadoc().add("Default no-arg constructor is private. Use overloaded constructor instead! ");
			defaultConstructor.javadoc().add("(Temporary solution, till a better and more suitable ObjectFactory is created.) ");
		}
		defaultConstructor.body().invoke("super");
	}

	private void createArgConstructor(final ClassOutlineImpl cc, final Collection<JFieldVar> required) {
		final StringBuffer debugOut = new StringBuffer();
		final Map<String, FieldOutline> fieldOutlineasMap = Util.getRequiredFieldsAsMap(cc);

		final JMethod defaultConstructor = cc.implClass.constructor(JMod.PUBLIC);
		defaultConstructor.javadoc().add("Value constructor with only mandatory fields");
		defaultConstructor.body().invoke("super");

		for (final JFieldVar field : required) {
			// FieldOutline fo = fieldOutlineasMap.get(field.name());
			// if (fo == null) {
			// continue;
			// }
			// if (fo.getPropertyInfo().isCollection()) {
			// LOG.info("!!!!! " + cc.implClass.name() + " is collection " + field.name() );
			// continue;
			// }

			final JVar arg = defaultConstructor.param(JMod.FINAL, Util.removeJAXBElement(cm, field.type()), field.name());
			defaultConstructor.javadoc().addParam(arg).append("required parameter");
			defaultConstructor.body().assign(JExpr.refthis(field.name()), arg);
		}

		debugOut.append("c> " + cc.implRef.name() + " :: public " + cc.target.shortName + "(");
		for (final JFieldVar field : required) {
			debugOut.append(field.type().name() + ", ");
		}
		if (required.size() > 0) {
			debugOut.delete(debugOut.length() - 2, debugOut.length());
		}
		debugOut.append(")");

		LOG.info(debugOut.toString());
	}

}
