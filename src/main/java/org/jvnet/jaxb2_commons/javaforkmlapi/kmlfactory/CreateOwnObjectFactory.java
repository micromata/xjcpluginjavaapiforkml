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
package org.jvnet.jaxb2_commons.javaforkmlapi.kmlfactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import org.apache.log4j.Logger;
import org.jvnet.jaxb2_commons.javaforkmlapi.ClazzPool;
import org.jvnet.jaxb2_commons.javaforkmlapi.Util;
import org.jvnet.jaxb2_commons.javaforkmlapi.XJCJavaForKmlApiPlugin;
import org.jvnet.jaxb2_commons.javaforkmlapi.command.Command;
import org.xml.sax.ErrorHandler;

import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JPackage;
import com.sun.tools.xjc.Options;
import com.sun.tools.xjc.generator.bean.ClassOutlineImpl;
import com.sun.tools.xjc.model.Model;
import com.sun.tools.xjc.outline.ClassOutline;
import com.sun.tools.xjc.outline.Outline;
import com.sun.tools.xjc.outline.PackageOutline;

public class CreateOwnObjectFactory extends Command {
	private static final Logger LOG = Logger.getLogger(CreateOwnObjectFactory.class.getName());

	private final Model model;

	public CreateOwnObjectFactory(final Outline outline, final Options opts, final ErrorHandler errorHandler, final ClazzPool pool) {
		super(outline, opts, errorHandler, pool);

		model = outline.getModel();
	}

	@Override
	public void execute() {
	
		// Get the default package from options.
		for (final PackageOutline packageoutline : outline.getAllPackageContexts()) {
			// Get the factory class from the default package.
			final JDefinedClass factoryClass = packageoutline.objectFactory();
			if (factoryClass != null) {
				// LOG.info(XJCJavaForKmlApiPlugin.PLUGINNAME+" pkg:          " + pkg.name());
				LOG.info(XJCJavaForKmlApiPlugin.PLUGINNAME + " factoryClass: " + factoryClass.name());
				LOG.info(XJCJavaForKmlApiPlugin.PLUGINNAME + " factoryClass: " + factoryClass.fullName());
				LOG.info(XJCJavaForKmlApiPlugin.PLUGINNAME + " factoryClass: " + factoryClass.methods().size());
				factoryClass.methods().clear();
				LOG.info(XJCJavaForKmlApiPlugin.PLUGINNAME + " factoryClass: " + factoryClass.methods().size());
//				factoryClass.fields().clear();
				final JPackage parent = (JPackage)factoryClass.parentContainer();
				parent.remove(factoryClass);
			}
		}

		final JPackage rootPackage = Util.getKmlClassPackage(outline);
		if (rootPackage == null) {
			LOG.info(XJCJavaForKmlApiPlugin.PLUGINNAME + " rootPackage not found");
			return;
		}

		// generate static factory
		final GenerateStaticKmlObjectFactory own = new GenerateStaticKmlObjectFactory(outline, rootPackage);

		// Assign the HashSet to a new ArrayList
		final ArrayList<ClassOutline> arrayList2 = new ArrayList<ClassOutline>(outline.getClasses());
		// Ensure correct order, since HashSet doesn't
		class OutlineCompare implements Comparator<ClassOutline> {

			public int compare(final ClassOutline o1, final ClassOutline o2) {
				return o1.implRef.fullName().compareTo(o2.implRef.fullName());
			}

		}
		Collections.sort(arrayList2, new OutlineCompare());

		for (final ClassOutline classOutline : arrayList2) {
			final ClassOutlineImpl cc = (ClassOutlineImpl) classOutline;
			own.populate(cc, cc.implRef, rootPackage);
			// subclasses.get(cc.target.getName());
		}

	}

}
