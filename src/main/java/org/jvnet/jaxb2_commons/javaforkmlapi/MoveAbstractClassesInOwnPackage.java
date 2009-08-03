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
package org.jvnet.jaxb2_commons.javaforkmlapi;

import org.apache.log4j.Logger;
import org.jvnet.jaxb2_commons.javaforkmlapi.command.Command;
import org.jvnet.jaxb2_commons.javaforkmlapi.fluent.FluentPattern;
import org.xml.sax.ErrorHandler;

import com.sun.codemodel.JPackage;
import com.sun.tools.xjc.Options;
import com.sun.tools.xjc.outline.Outline;

public class MoveAbstractClassesInOwnPackage extends Command {
	private static final Logger LOG = Logger.getLogger(MoveAbstractClassesInOwnPackage.class.getName());

	public MoveAbstractClassesInOwnPackage(Outline outline, Options opts, ErrorHandler errorHandler, ClazzPool pool) {
		super(outline, opts, errorHandler, pool);
	}

	@Override
	public void execute() {
		JPackage rootPackage = Util.getKmlClassPackage(outline);
		if (rootPackage == null) {
			LOG.info(XJCJavaForKmlApiPlugin.PLUGINNAME+" rootPackage not found");
		}
		
		// if (cc.target.isAbstract()) {
		// LOG.info("class is abstract");
		// LOG.info("current package is:  " + cc._package()._package().name());
		// JPackage newpackage = codeModel._package(cc._package()._package().name() + ".AbstractPackage");
		// LOG.info("created new package: " + newpackage.name());
		// // cc._package().
		//
		// // cc.target.getOwnerPackage().subPackage(pkg)
		// }
		// boolean toRet = this.needsSupport(classOutline, true);
		// LOG.info(XJCJavaForKmlApiPlugin.PLUGINNAME+" --> " + classOutline.implRef.name() + " " + toRet);

	}

}
