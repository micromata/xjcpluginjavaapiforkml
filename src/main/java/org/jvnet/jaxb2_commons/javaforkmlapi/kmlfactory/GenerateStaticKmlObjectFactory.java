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
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBException;

import org.jvnet.jaxb2_commons.javaforkmlapi.Util;

import com.sun.codemodel.JClass;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JFieldVar;
import com.sun.codemodel.JInvocation;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JMod;
import com.sun.codemodel.JPackage;
import com.sun.codemodel.JVar;
import com.sun.tools.xjc.generator.annotation.spec.XmlRegistryWriter;
import com.sun.tools.xjc.generator.bean.ClassOutlineImpl;
import com.sun.tools.xjc.model.CPropertyInfo;
import com.sun.tools.xjc.model.Constructor;
import com.sun.tools.xjc.outline.ClassOutline;
import com.sun.tools.xjc.outline.FieldAccessor;
import com.sun.tools.xjc.outline.FieldOutline;
import com.sun.tools.xjc.outline.Outline;

/**
 * Generates <code>ObjectFactory</code> then wraps it and provides access to it.
 * <p>
 * The ObjectFactory contains factory methods for each schema derived content class
 */
class GenerateStaticKmlObjectFactory {

	private final Outline outline;

	/**
	 * Reference to the generated ObjectFactory class.
	 */
	private final JDefinedClass objectFactory;

	/**
	 * Names of the value factory methods that are created. Used to detect collisions. The value is used for reporting error locations.
	 */
	private final Map<String, ClassOutlineImpl> valueFactoryNames = new HashMap<String, ClassOutlineImpl>();

	private HashSet<String> nestedClasses;

	/**
	 * Returns a reference to the generated (public) ObjectFactory
	 */
	public JDefinedClass getObjectFactory() {
		return objectFactory;
	}

	public GenerateStaticKmlObjectFactory(Outline outline, JPackage targetPackage) {
		this.outline = outline;

		// create the ObjectFactory class skeleton
		objectFactory = this.outline.getClassFactory().createClass(targetPackage, JMod.FINAL | JMod.PUBLIC, "KmlFactory", null);
		// add some class javadoc
		objectFactory.javadoc().append("Factory functions to create all KML complex elements.");

		nestedClasses = Util.getAllNestedClasses(outline);
	}

	protected final void populate(ClassOutlineImpl cc, JClass sigType, JPackage rootPackage) {
		// https://jaxb.dev.java.net/issues/show_bug.cgi?id=633
		if (cc.implClass.isAnonymous()) {
			return;
		}
		if (cc.implClass.isAbstract()) {
			return;
		}
		if (nestedClasses.contains(cc.implRef.fullName())) {
			return;
		}
		String methodName = Util.calculateMethodName(cc, rootPackage);
		if (cc.implClass.name().equals("Coordinate")) {
			Collection<JFieldVar> coordinateCreateMethods = new ArrayList<JFieldVar>();
			JFieldVar longitude = cc.ref.fields().get("longitude");
			JFieldVar latitude = cc.ref.fields().get("latitude");
			
			
			coordinateCreateMethods.add(longitude);
			coordinateCreateMethods.add(latitude);
			createArgFactoryMethod(cc, coordinateCreateMethods, sigType, methodName);
			
			JFieldVar altitude = cc.ref.fields().get("altitude");
			coordinateCreateMethods.add(altitude);
			createArgFactoryMethod(cc, coordinateCreateMethods, sigType, methodName);
			
			coordinateCreateMethods.clear();
			
			JMethod m = objectFactory.method(JMod.PUBLIC | JMod.STATIC, sigType, "create" + methodName);
			m.javadoc().append("Create an instance of ").append(cc.ref);
			JInvocation returntype = JExpr._new(cc.implRef);
			final JVar arg = m.param(JMod.FINAL, String.class, "coordinates");
			m.javadoc().addParam(arg).append("required parameter");
			returntype.arg(arg);
			m.body()._return(returntype);
			return;
		}
		
		
		Collection<JFieldVar> requiredConstructorFields = Util.getConstructorRequiredFields(cc);
		if (requiredConstructorFields.size() == 0) {
			createNoArgFactoryMethod(cc, sigType, methodName);
			return;
		}
		createArgFactoryMethod(cc, requiredConstructorFields, sigType, methodName);
	}

	private void createNoArgFactoryMethod(final ClassOutlineImpl cc, JClass sigType, String methodName) {
		// add static factory method for this class to JAXBContext.
		//
		// generate methods like:
		// public static final SIGTYPE createFoo() {
		// return new FooImpl();
		// }
		JMethod m = objectFactory.method(JMod.PUBLIC | JMod.STATIC, sigType, "create" + methodName);
		m.body()._return(JExpr._new(cc.implRef));
		m.javadoc().append("Create an instance of ").append(cc.ref);
	}

	private void createArgFactoryMethod(ClassOutlineImpl cc, Collection<JFieldVar> required, JClass sigType, String methodName) {
		JMethod m = objectFactory.method(JMod.PUBLIC | JMod.STATIC, sigType, "create" + methodName);
		Map<String, FieldOutline> fieldOutlineasMap = Util.getRequiredFieldsAsMap(cc);
		m.javadoc().append("Create an instance of ").append(cc.ref);
		JInvocation returntype = JExpr._new(cc.implRef);
		for (JFieldVar field : required) {

			final JVar arg = m.param(JMod.FINAL, field.type(), field.name());
			m.javadoc().addParam(arg).append("required parameter");
			returntype.arg(arg);
		}
		m.body()._return(returntype);
	}

}
