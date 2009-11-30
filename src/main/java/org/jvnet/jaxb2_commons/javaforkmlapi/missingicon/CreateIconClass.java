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
package org.jvnet.jaxb2_commons.javaforkmlapi.missingicon;

import org.apache.log4j.Logger;
import org.jvnet.jaxb2_commons.javaforkmlapi.ClazzPool;
import org.jvnet.jaxb2_commons.javaforkmlapi.XJCJavaForKmlApiPlugin;
import org.jvnet.jaxb2_commons.javaforkmlapi.command.Command;
import org.xml.sax.ErrorHandler;

import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JFieldVar;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JType;
import com.sun.codemodel.JVar;
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
	public CreateIconClass(final Outline outline, final Options opts, final ErrorHandler errorHandler, final ClazzPool pool) {
		super(outline, opts, errorHandler, pool);
	}

	@Override
	public void execute() {
		JDefinedClass elementType = null;
		for (final ClassOutline classOutline : outline.getClasses()) {
			final ClassOutlineImpl cc = (ClassOutlineImpl) classOutline;
			if (cc.implRef.name().equals("Icon") && cc.implClass._extends().name().equals("BasicLink")) {
				LOG.info(XJCJavaForKmlApiPlugin.PLUGINNAME + " link class found.");
				elementType = cc.implClass;
			}
		}

		for (final ClassOutline classOutline : outline.getClasses()) {
			
			for (final JFieldVar jFieldVar : classOutline.implClass.fields().values()) {
				if (jFieldVar.name().equals("icon") && shouldItBeAnIcon(jFieldVar.type())) {
					LOG.info(XJCJavaForKmlApiPlugin.PLUGINNAME + " usage of link class found." + classOutline.implClass.fullName());
					jFieldVar.type(elementType);
				}

			}
			
			for (final JMethod jmethod : classOutline.implClass.methods()) {
				if (jmethod.name().endsWith("Icon") && shouldItBeAnIcon(jmethod.type())) {
					jmethod.type(elementType);
				}

				for (final JVar jParams : jmethod.listParams()) {
					if (jmethod.name().endsWith("Icon") && shouldItBeAnIcon(jParams.type())) {
						jParams.type(elementType);
					}
				}
			}
		}

	}

	private boolean shouldItBeAnIcon(final JType jType) {
	  return jType.fullName().equals("de.micromata.opengis.kml.v_2_2_0.Link") || jType.fullName().equals("de.micromata.opengis.kml.v_2_2_0.BasicLink");
  }

}
