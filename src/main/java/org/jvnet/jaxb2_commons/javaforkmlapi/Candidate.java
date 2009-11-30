// ///////////////////////////////////////////////////////////////////////////
//
// $RCSfile: $
//
// Project JaxbPluginJavaForKmlApi
//
// Author Flori (f.bachmann@micromata.de)
// Created May 5, 2009
// Copyright Micromata May 5, 2009
//
// $Id: $
// $Revision: $
// $Date: $
//
// ///////////////////////////////////////////////////////////////////////////
package org.jvnet.jaxb2_commons.javaforkmlapi;

import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JFieldVar;
import com.sun.tools.xjc.model.CClassInfo;
import com.sun.tools.xjc.model.CPropertyInfo;
import com.sun.tools.xjc.model.CTypeInfo;

public class Candidate {
	protected CClassInfo classInfo;

	protected CPropertyInfo propertyInfo;

	protected CTypeInfo propertyTypeInfo;

	protected JDefinedClass implClass;

	protected JFieldVar field;

	public Candidate(final CClassInfo classInfo) {
		this.classInfo = classInfo;
		this.propertyInfo = classInfo.getProperties().get(0);
		this.propertyTypeInfo = propertyInfo.ref().iterator().next();
	}

	public CClassInfo getClassInfo() {
		return classInfo;
	}

	public CPropertyInfo getPropertyInfo() {
		return propertyInfo;
	}

	public CTypeInfo getPropertyTypeInfo() {
		return propertyTypeInfo;
	}

	public String getClassName() {
		return classInfo.fullName();
	}

	public String getFieldName() {
		return getPropertyInfo().getName(false);
	}

	public String getFieldTypeName() {
		return propertyTypeInfo.getType().fullName();
	}
}