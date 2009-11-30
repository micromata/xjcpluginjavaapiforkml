package org.jvnet.jaxb2_commons.javaforkmlapi;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

import org.apache.log4j.Logger;

import com.sun.codemodel.ClassType;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JMod;
import com.sun.codemodel.JPackage;
import com.sun.tools.xjc.generator.bean.ClassOutlineImpl;
import com.sun.tools.xjc.outline.ClassOutline;
import com.sun.tools.xjc.outline.Outline;
import com.sun.tools.xjc.util.CodeModelClassFactory;

public class ClazzPool {
	private static final Logger LOG = Logger.getLogger(ClazzPool.class.getName());
	private final JDefinedClass classObviousAnnotation;

	private final JDefinedClass classCoordinateConverter;

	private JDefinedClass classCoordinate;

	private JDefinedClass classIcon;

	private JDefinedClass classLink;

	private final JDefinedClass classBooleanConverter;

	public ClazzPool(final Outline outline) {
		final JPackage kmlpackage = Util.getKmlClassPackage(outline);
		final CodeModelClassFactory classFactory = outline.getClassFactory();
		final JPackage ppp = outline.getCodeModel()._package(kmlpackage.getPackage().name() + ".annotations");
		classObviousAnnotation = classFactory.createClass(ppp, JMod.PUBLIC, "Obvious", null, ClassType.ANNOTATION_TYPE_DECL);
		classObviousAnnotation.annotate(Target.class).param("value", ElementType.FIELD).param("value", ElementType.METHOD);
		classCoordinateConverter = classFactory.createClass(kmlpackage, JMod.PUBLIC | JMod.FINAL, "CoordinatesConverter", null, ClassType.CLASS);
		classBooleanConverter = classFactory.createClass(kmlpackage, JMod.PUBLIC | JMod.FINAL, "BooleanConverter", null, ClassType.CLASS);
		
		for (final ClassOutline classOutline : outline.getClasses()) {
			final ClassOutlineImpl cc = (ClassOutlineImpl) classOutline;

			if (cc.implRef.name().equals("Coordinate")) {
				LOG.info(XJCJavaForKmlApiPlugin.PLUGINNAME + " Coordinate class found.");
				classCoordinate = cc.implClass;
				classCoordinate.methods().clear();
				continue;
			}

			if (cc.implRef.name().equals("Icon")) {
				LOG.info(XJCJavaForKmlApiPlugin.PLUGINNAME + " Icon class found.");
				classIcon = cc.implClass;
				continue;
			}

			if (cc.implRef.name().equals("Link")) {
				LOG.info(XJCJavaForKmlApiPlugin.PLUGINNAME + " Link class found.");
				classLink = cc.implClass;
				continue;
			}

		}
	}

	

	
	public JDefinedClass getClassObviousAnnotation() {
		return classObviousAnnotation;
	}

	public JDefinedClass getClassCoordinateConverter() {
		return classCoordinateConverter;
	}

	public JDefinedClass getClassCoordinate() {
		return classCoordinate;
	}

	public JDefinedClass getClassIcon() {
		return classIcon;
	}

	public JDefinedClass getClassLink() {
		return classLink;
	}

	public JDefinedClass getClassBooleanConverter() {
	  return classBooleanConverter;
  }

}
