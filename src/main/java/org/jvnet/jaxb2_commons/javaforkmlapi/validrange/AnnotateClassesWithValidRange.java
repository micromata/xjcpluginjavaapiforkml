/////////////////////////////////////////////////////////////////////////////
//
// $RCSfile: $
//
// Project   JaxbPluginJavaForKmlApi
//
// Author    Flori (f.bachmann@micromata.de)
// Created   25.03.2009
// Copyright Micromata 25.03.2009
//
// $Id: $
// $Revision: $
// $Date: $
//
/////////////////////////////////////////////////////////////////////////////
package org.jvnet.jaxb2_commons.javaforkmlapi.validrange;

import org.jvnet.jaxb2_commons.javaforkmlapi.ClazzPool;
import org.jvnet.jaxb2_commons.javaforkmlapi.command.Command;
import org.xml.sax.ErrorHandler;

import com.sun.tools.xjc.Options;
import com.sun.tools.xjc.outline.Outline;

public class AnnotateClassesWithValidRange extends Command {

	public AnnotateClassesWithValidRange(Outline outline, Options opts, ErrorHandler errorHandler, ClazzPool pool) {
	  super(outline, opts, errorHandler, pool);
  }

	@Override
  public void execute() {
  }

}
