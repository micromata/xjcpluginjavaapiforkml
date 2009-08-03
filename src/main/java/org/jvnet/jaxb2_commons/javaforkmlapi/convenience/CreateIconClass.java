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
package org.jvnet.jaxb2_commons.javaforkmlapi.convenience;

import org.apache.log4j.Logger;
import org.jvnet.jaxb2_commons.javaforkmlapi.ClazzPool;
import org.jvnet.jaxb2_commons.javaforkmlapi.XJCJavaForKmlApiPlugin;
import org.jvnet.jaxb2_commons.javaforkmlapi.command.Command;
import org.xml.sax.ErrorHandler;

import com.sun.codemodel.JDefinedClass;
import com.sun.tools.xjc.Options;
import com.sun.tools.xjc.generator.bean.ClassOutlineImpl;
import com.sun.tools.xjc.outline.ClassOutline;
import com.sun.tools.xjc.outline.Outline;

public class CreateIconClass extends Command {
	private static final Logger LOG = Logger.getLogger(CreateIconClass.class.getName());

	/*
	 * JAXB unfortunately doesn't generate the Icon-Class.
	 * 
	 * @param outline
	 * 
	 * @param classFactory
	 */
	public CreateIconClass(Outline outline, Options opts, ErrorHandler errorHandler, ClazzPool pool) {
		super(outline, opts, errorHandler, pool);
	}

	@Override
	public void execute() {
		for (final ClassOutline classOutline : outline.getClasses()) {
			ClassOutlineImpl cc = (ClassOutlineImpl) classOutline;

			if (cc.implRef.name().equals("Icon") && cc.implClass._extends().name().equals("Link")) {
				System.out.println(XJCJavaForKmlApiPlugin.PLUGINNAME + "link class found.");
				JDefinedClass iconClass = cc.implClass;

				iconClass.methods().clear();
				// Iterator constructors = iconClass.constructors();
				// final JMethod stringArgConstructor = iconClass.constructor(JMod.PUBLIC);

			}
		}
	}

}
