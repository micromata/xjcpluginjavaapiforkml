// ///////////////////////////////////////////////////////////////////////////
//
// $RCSfile: $
//
// Project JaxbPluginJavaForKmlApi
//
// Author Flori (f.bachmann@micromata.de)
// Created 18.03.2009
// Copyright Micromata 18.03.2009
//
// $Id: $
// $Revision: $
// $Date: $
//
// ///////////////////////////////////////////////////////////////////////////
package org.jvnet.jaxb2_commons.javaforkmlapi.validrange;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

// http://martinfowler.com/dslwip/Annotation.html
// TODO write an annotation processor
// (put it to marshall)
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidRange {
	int lower() default Integer.MIN_VALUE;

	int upper() default Integer.MAX_VALUE;
}
