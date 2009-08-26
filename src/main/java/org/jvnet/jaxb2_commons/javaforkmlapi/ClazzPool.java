package org.jvnet.jaxb2_commons.javaforkmlapi;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

import com.sun.codemodel.ClassType;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JMod;
import com.sun.codemodel.JPackage;
import com.sun.tools.xjc.generator.bean.ClassOutlineImpl;
import com.sun.tools.xjc.model.CEnumLeafInfo;
import com.sun.tools.xjc.outline.ClassOutline;
import com.sun.tools.xjc.outline.EnumOutline;
import com.sun.tools.xjc.outline.Outline;
import com.sun.tools.xjc.util.CodeModelClassFactory;

public class ClazzPool {
	private JDefinedClass classObviousAnnotation;

	private JDefinedClass classCoordinateConverter;

	private JDefinedClass classCoordinate;

	private JDefinedClass classIcon;

	private JDefinedClass classLink;

	public ClazzPool(Outline outline) {
		JPackage kmlpackage = Util.getKmlClassPackage(outline);
		CodeModelClassFactory classFactory = outline.getClassFactory();
		JPackage ppp = outline.getCodeModel()._package(kmlpackage.getPackage().name() + ".annotations");
		classObviousAnnotation = classFactory.createClass(ppp, JMod.PUBLIC, "Obvious", null, ClassType.ANNOTATION_TYPE_DECL);
		classObviousAnnotation.annotate(Target.class).param("value", ElementType.FIELD).param("value", ElementType.METHOD);
		classCoordinateConverter = classFactory
		    .createClass(kmlpackage, JMod.PUBLIC | JMod.FINAL, "CoordinatesConverter", null, ClassType.CLASS);

		for (final ClassOutline classOutline : outline.getClasses()) {
			ClassOutlineImpl cc = (ClassOutlineImpl) classOutline;

			if (cc.implRef.name().equals("Coordinate")) {
				System.out.println(XJCJavaForKmlApiPlugin.PLUGINNAME + " Coordinate class found.");
				classCoordinate = cc.implClass;
				classCoordinate.methods().clear();
				continue;
			}

			if (cc.implRef.name().equals("Icon")) {
				System.out.println(XJCJavaForKmlApiPlugin.PLUGINNAME + " Icon class found.");
				classIcon = cc.implClass;
				continue;
			}

			if (cc.implRef.name().equals("Link")) {
				System.out.println(XJCJavaForKmlApiPlugin.PLUGINNAME + " Link class found.");
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

}
