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

import com.sun.codemodel.JClassAlreadyExistsException;
import com.sun.codemodel.JDefinedClass;
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

public class BuilderPattern extends Command {
	private static final Logger LOG = Logger.getLogger(BuilderPattern.class.getName());

	public BuilderPattern(Outline outline, Options opts, ErrorHandler errorHandler, ClazzPool pool) {
		super(outline, opts, errorHandler, pool);
	}

	@Override
	public void execute() {
		LOG.info(XJCJavaForKmlApiPlugin.PLUGINNAME + " appply Builder pattern for classes with required fields");
		for (final ClassOutline classOutline : outline.getClasses()) {
			ClassOutlineImpl cc = (ClassOutlineImpl) classOutline;

			Collection<JFieldVar> requiredConstructorFields = Util.getConstructorRequiredFields(cc);
			if (requiredConstructorFields.size() > 0) {
				createArgConstructor(cc, requiredConstructorFields);
			}
			
		}
	}
	
	private void createNoArgConstructor(final ClassOutline classOutline, int mods) {
		// Create the default, no-arg constructor
		final JMethod defaultConstructor = classOutline.implClass.constructor(mods);
		if (mods == JMod.PRIVATE) {
		defaultConstructor.annotate(Deprecated.class);
		}
		defaultConstructor.javadoc().add("Default no-arg constructor is private. Use overloaded constructor instead! ");
		defaultConstructor.javadoc().add("(Temporary solution, till a better and more suitable ObjectFactory is created.) ");
		defaultConstructor.body().invoke("super");
	}

	private void createArgConstructor(ClassOutlineImpl cc, Collection<JFieldVar> required) {
		StringBuffer debugOut = new StringBuffer();
		
		JDefinedClass nestedBuilderClass ;
		try {
	     nestedBuilderClass = cc.implClass._class(JMod.PUBLIC | JMod.STATIC, "Builder");
  
		Map<String, FieldOutline> fieldOutlineasMap = Util.getRequiredFieldsAsMap(cc);

		final JMethod defaultConstructor = nestedBuilderClass.constructor(JMod.PUBLIC);
		defaultConstructor.javadoc().add("Value constructor with only mandatory fields");
		defaultConstructor.body().invoke("super");

		for (JFieldVar field : required) {
//			FieldOutline fo = fieldOutlineasMap.get(field.name());
//			if (fo == null) {
//				continue;
//			}
//			if (fo.getPropertyInfo().isCollection()) {
//				System.out.println("!!!!! " + cc.implClass.name() + " is collection " + field.name() );
//				continue;
//			}
			
			final JVar arg = defaultConstructor.param(JMod.FINAL, field.type(), field.name());
			defaultConstructor.javadoc().addParam(arg).append("required parameter");
			defaultConstructor.body().assign(JExpr.refthis(field.name()), arg);
		}
		
		debugOut.append("c> " + cc.implRef.name() + " :: public " + cc.target.shortName + "(");
		for (JFieldVar field : required) {
			debugOut.append(field.type().name() + ", ");
		}
		if (required.size() > 0) {
			debugOut.delete(debugOut.length() - 2, debugOut.length());
		}
		debugOut.append(")");

		LOG.info(debugOut.toString());
	  } catch (JClassAlreadyExistsException e) {
	    // TODO Auto-generated catch block
	   LOG.info("Exception encountered " + e);
    }
	}
	
	
	



}
