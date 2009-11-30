/////////////////////////////////////////////////////////////////////////////
//
// $RCSfile: $
//
// Project   JaxbPluginJavaForKmlApi
//
// Author    Flori (f.bachmann@micromata.de)
// Created   Nov 23, 2009
// Copyright Micromata Nov 23, 2009
//
// $Id: $
// $Revision: $
// $Date: $
//
/////////////////////////////////////////////////////////////////////////////
package org.jvnet.jaxb2_commons.javaforkmlapi.booleanconverter;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.apache.log4j.Logger;
import org.jvnet.jaxb2_commons.javaforkmlapi.ClazzPool;
import org.jvnet.jaxb2_commons.javaforkmlapi.command.Command;
import org.xml.sax.ErrorHandler;

import com.sun.codemodel.JClass;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JFieldVar;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JMod;
import com.sun.codemodel.JOp;
import com.sun.codemodel.JVar;
import com.sun.tools.xjc.Options;
import com.sun.tools.xjc.generator.bean.ClassOutlineImpl;
import com.sun.tools.xjc.outline.ClassOutline;
import com.sun.tools.xjc.outline.Outline;

public class BooleanConverter extends Command {
	private static final Logger LOG = Logger.getLogger(BooleanConverter.class.getName());
	
	public BooleanConverter(final Outline outline, final Options opts, final ErrorHandler errorHandler, final ClazzPool pool) {
		super(outline, opts, errorHandler, pool);
		createBooleanConverter(outline);
	}
	
	@SuppressWarnings("restriction")
  private void createBooleanConverter(final Outline outline) {
		final JDefinedClass booleanConverter = pool.getClassBooleanConverter();

		final JClass xmladapter = outline.getCodeModel().ref(XmlAdapter.class).narrow(Integer.class).narrow(Boolean.class);
		booleanConverter._extends(xmladapter);
		
		// toString 
		final JMethod unmarshal = booleanConverter.method(JMod.PUBLIC, Boolean.class, "unmarshal");
		final JVar stringConstructorArg = unmarshal.param(JMod.FINAL, Integer.class, "i");
		unmarshal.annotate(Override.class);
		unmarshal._throws(Exception.class);
		unmarshal.body()._return(JOp.cond(stringConstructorArg.eq(JExpr._null()), JExpr._null(), stringConstructorArg.eq(JExpr.lit(1))));
		
		// toString
		final JMethod marshal = booleanConverter.method(JMod.PUBLIC, Integer.class, "marshal");
		final JVar unmarshallparam = marshal.param(JMod.FINAL, Boolean.class, "b");
		marshal.annotate(Override.class);
		marshal._throws(Exception.class);
		marshal.body()._return(JOp.cond(unmarshallparam.eq(JExpr._null()), JExpr._null(), JOp.cond(unmarshallparam, JExpr.lit(1), JExpr.lit(0))));

	}


	@Override
	public void execute() {
		for (final ClassOutline classOutline : outline.getClasses()) {
			annotateBooleanFieldsWithBooleanConverter((ClassOutlineImpl) classOutline);
		}
	}
	
	private void annotateBooleanFieldsWithBooleanConverter(final ClassOutlineImpl cc) {
		final JDefinedClass implClass = cc.implClass;
		// if no fields are present return
		if (implClass.fields().isEmpty()) {
			return;
		}
		
		for (final JFieldVar jFieldVar : implClass.fields().values()) {
			if (jFieldVar.type().fullName().equals("java.lang.Boolean")) {
				jFieldVar.annotate(XmlJavaTypeAdapter.class).param("value", pool.getClassBooleanConverter());
			}
		}
	}

}
