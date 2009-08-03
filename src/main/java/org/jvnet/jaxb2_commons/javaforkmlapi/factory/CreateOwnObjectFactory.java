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
package org.jvnet.jaxb2_commons.javaforkmlapi.factory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import org.apache.log4j.Logger;
import org.jvnet.jaxb2_commons.javaforkmlapi.ClazzPool;
import org.jvnet.jaxb2_commons.javaforkmlapi.Util;
import org.jvnet.jaxb2_commons.javaforkmlapi.XJCJavaForKmlApiPlugin;
import org.jvnet.jaxb2_commons.javaforkmlapi.command.Command;
import org.jvnet.jaxb2_commons.javaforkmlapi.fluent.CreateConstructors;
import org.xml.sax.ErrorHandler;

import com.sun.codemodel.JClass;
import com.sun.codemodel.JClassContainer;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JPackage;
import com.sun.tools.xjc.Options;
import com.sun.tools.xjc.generator.bean.ClassOutlineImpl;
import com.sun.tools.xjc.model.CElementInfo;
import com.sun.tools.xjc.model.Model;
import com.sun.tools.xjc.outline.Aspect;
import com.sun.tools.xjc.outline.ClassOutline;
import com.sun.tools.xjc.outline.Outline;
import com.sun.tools.xjc.outline.PackageOutline;

public class CreateOwnObjectFactory extends Command {
	private static final Logger LOG = Logger.getLogger(CreateOwnObjectFactory.class.getName());

	private Model model;

	public CreateOwnObjectFactory(Outline outline, Options opts, ErrorHandler errorHandler, ClazzPool pool) {
		super(outline, opts, errorHandler, pool);

		model = outline.getModel();
	}

	@Override
	public void execute() {
	
		// Get the default package from options.
		for (PackageOutline packageoutline : outline.getAllPackageContexts()) {
			// Get the factory class from the default package.
			JDefinedClass factoryClass = packageoutline.objectFactory();
			if (factoryClass != null) {
				// LOG.info(XJCJavaForKmlApiPlugin.PLUGINNAME+" pkg:          " + pkg.name());
				LOG.info(XJCJavaForKmlApiPlugin.PLUGINNAME + " factoryClass: " + factoryClass.name());
				LOG.info(XJCJavaForKmlApiPlugin.PLUGINNAME + " factoryClass: " + factoryClass.fullName());
				LOG.info(XJCJavaForKmlApiPlugin.PLUGINNAME + " factoryClass: " + factoryClass.methods().size());
				factoryClass.methods().clear();
				LOG.info(XJCJavaForKmlApiPlugin.PLUGINNAME + " factoryClass: " + factoryClass.methods().size());
//				factoryClass.fields().clear();
				JPackage parent = (JPackage)factoryClass.parentContainer();
				parent.remove(factoryClass);
			}
		}

		JPackage rootPackage = Util.getKmlClassPackage(outline);
		if (rootPackage == null) {
			LOG.info(XJCJavaForKmlApiPlugin.PLUGINNAME + " rootPackage not found");
			return;
		}

		// generate static factory
		final GenerateStaticKmlObjectFactory own = new GenerateStaticKmlObjectFactory(outline, rootPackage);

		// Assign the HashSet to a new ArrayList
		ArrayList<ClassOutline> arrayList2 = new ArrayList<ClassOutline>(outline.getClasses());
		// Ensure correct order, since HashSet doesn't
		class OutlineCompare implements Comparator<ClassOutline> {

			public int compare(ClassOutline o1, ClassOutline o2) {
				return o1.implRef.fullName().compareTo(o2.implRef.fullName());
			}

		}
		Collections.sort(arrayList2, new OutlineCompare());

		for (final ClassOutline classOutline : arrayList2) {
			ClassOutlineImpl cc = (ClassOutlineImpl) classOutline;
			own.populate(cc, cc.implRef, rootPackage);
			// subclasses.get(cc.target.getName());
		}

	}

}
