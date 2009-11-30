package org.jvnet.jaxb2_commons.javaforkmlapi.coordinate;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.apache.log4j.Logger;
import org.jvnet.jaxb2_commons.javaforkmlapi.ClazzPool;
import org.jvnet.jaxb2_commons.javaforkmlapi.Util;
import org.jvnet.jaxb2_commons.javaforkmlapi.command.Command;
import org.xml.sax.ErrorHandler;

import com.sun.codemodel.JClass;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JFieldVar;
import com.sun.codemodel.JForEach;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JMod;
import com.sun.codemodel.JType;
import com.sun.codemodel.JVar;
import com.sun.tools.xjc.Options;
import com.sun.tools.xjc.generator.bean.ClassOutlineImpl;
import com.sun.tools.xjc.outline.ClassOutline;
import com.sun.tools.xjc.outline.Outline;

public class CreateCoordinateClass extends Command {
	private static final Logger LOG = Logger.getLogger(CreateCoordinateClass.class.getName());

	@SuppressWarnings("unchecked")
	protected Class interfaceClass = java.util.List.class;

	@SuppressWarnings("unchecked")
	protected Class collectionClass = java.util.ArrayList.class;

	private final JCodeModel cm;

	private JFieldVar longitude;

	private JFieldVar latitude;

	private JFieldVar altitude;

	private final JType stringClass;

	private final JType illegalArgumentExceptionClass;

	private final JType doubleClass;

	private final JType stringBufferClass;

	private final JType stringBuilderClass;

	/*
	 * A single tuple consisting of floating point values for longitude, latitude, and altitude (in that order). Longitude and latitude values
	 * are in degrees, where
	 * 
	 * longitude >= -180 and <= 180 latitude >= -90 and <= 90 altitude values (optional) are in meters above sea level
	 * 
	 * Do not include spaces between the three values that describe a coordinate.
	 */

	/*
	 * JAXB unfortunately doesn't generate the Icon-Class.
	 * 
	 * @param outline
	 * 
	 * @param classFactory
	 */
	public CreateCoordinateClass(final Outline outline, final Options opts, final ErrorHandler errorHandler, final ClazzPool pool) {
		super(outline, opts, errorHandler, pool);
		cm = outline.getCodeModel();

		//	
		stringClass = cm._ref(String.class);
		illegalArgumentExceptionClass = cm._ref(IllegalArgumentException.class);
		doubleClass = cm._ref(Double.class);
		stringBufferClass = cm._ref(StringBuffer.class);
		stringBuilderClass = cm._ref(StringBuilder.class);

		createCoordinateClass(outline);
		createCoordinateClassConverter(outline);

	}

	private void createCoordinateClass(final Outline outline) {
		// JPackage kmlpackage = Util.getKmlClassPackage(outline);
		// CodeModelClassFactory classFactory = outline.getClassFactory();
		// coordinateClass = classFactory.createClass(kmlpackage, JMod.PUBLIC, "Coordinate", null, ClassType.CLASS);
		final JDefinedClass coordinateClass = pool.getClassCoordinate();

		longitude = coordinateClass.fields().get("longitude");
		latitude = coordinateClass.fields().get("latitude");
		altitude = coordinateClass.fields().get("altitude");

//		longitude.init(JExpr.lit(0.0d));
//		latitude.init(JExpr.lit(0.0d));
//		altitude.init(JExpr.lit(0.0d));
		// longitude = coordinateClass.field(JMod.PRIVATE, cm.DOUBLE, "longitu33de", JExpr.lit(0.0d));
		// latitude = coordinateClass.field(JMod.PRIVATE, cm.DOUBLE, "latitud33e", JExpr.lit(0.0d));
		// altitude = coordinateClass.field(JMod.PRIVATE, cm.DOUBLE, "altitud33e", JExpr.lit(0.0d));

		final JMethod noArgConstructor = coordinateClass.constructor(JMod.PRIVATE);
		noArgConstructor.annotate(Deprecated.class);
		noArgConstructor.javadoc().add("Default no-arg constructor is private. Use overloaded constructor instead! ");
		noArgConstructor.javadoc().add("(Temporary solution, till a better and more suitable ObjectFactory is created.) ");

		final JMethod twoDoubleArgConstructor = coordinateClass.constructor(JMod.PUBLIC);
		addConstructorParam(twoDoubleArgConstructor, longitude);
		addConstructorParam(twoDoubleArgConstructor, latitude);

		final JMethod threeDoubleArgConstructor = coordinateClass.constructor(JMod.PUBLIC);
		addConstructorParam(threeDoubleArgConstructor, longitude);
		addConstructorParam(threeDoubleArgConstructor, latitude);
		addConstructorParam(threeDoubleArgConstructor, altitude);

		final JMethod stringArgConstructor = coordinateClass.constructor(JMod.PUBLIC);
		final JVar stringConstructorArg = stringArgConstructor.param(JMod.FINAL, String.class, "coordinates");
		
		//		http://code.google.com/p/javaapiforkml/issues/detail?id=10
		//		changed:
		//			String[] coords = coordinates.replaceAll(",[ ]+?", ",").trim().split(",");
		//			to:
		//			String[] coords = coordinates.replaceAll(",\\s+", ",").trim().split(",");
		final JVar varCoords = stringArgConstructor.body().decl(stringClass.array(), "coords",
		    stringConstructorArg.invoke("replaceAll").arg(",\\s+").arg(",").invoke("trim").invoke("split").arg(","));
		// CODE: if ((coords < 1) && (coords > 3)) {throw IllegalArgumentException}
		stringArgConstructor.body()._if(
		    JExpr.ref(varCoords.name()).ref("length").lt(JExpr.lit(1)).cand(JExpr.ref(varCoords.name()).ref("length").gt(JExpr.lit(3))))
		    ._then()._throw(JExpr._new(illegalArgumentExceptionClass));
		
		stringArgConstructor.body().assign(JExpr.refthis(longitude.name()),
		    JExpr.ref("Double").invoke("parseDouble").arg(JExpr.direct("coords[0]")));
		stringArgConstructor.body().assign(JExpr.refthis(latitude.name()),
		    JExpr.ref("Double").invoke("parseDouble").arg(JExpr.direct("coords[1]")));
		stringArgConstructor.body()._if(JExpr.ref(varCoords.name()).ref("length").eq(JExpr.lit(3)))._then().assign(
		    JExpr.refthis(altitude.name()), JExpr.ref("Double").invoke("parseDouble").arg(JExpr.direct("coords[2]")));

		// toString
		final JMethod toString = coordinateClass.method(JMod.PUBLIC, stringClass, "toString");
		toString.annotate(Override.class);
		final JVar sbVar = toString.body().decl(stringBuilderClass, "sb", JExpr._new(stringBuilderClass));
		toString.body().add(sbVar.invoke("append").arg(longitude));
		toString.body().add(sbVar.invoke("append").arg(","));
		toString.body().add(sbVar.invoke("append").arg(latitude));
		toString.body()._if(JExpr.ref(altitude.name()).ne(JExpr.lit(0.0)))._then().add(sbVar.invoke("append").arg(",")).add(
		    sbVar.invoke("append").arg(altitude));
		toString.body()._return(sbVar.invoke("toString"));

		addSimpleSetterAndGetter(coordinateClass, longitude);
		addSimpleSetterAndGetter(coordinateClass, latitude);
		addSimpleSetterAndGetter(coordinateClass, altitude);

		// CreateEqualsAndHashCode.createHashCodeAndEqualsMethod(coordinateClass, null);

	}
	


	private void createCoordinateClassConverter(final Outline outline) {
		// JPackage kmlpackage = Util.getKmlClassPackage(outline);
		// CodeModelClassFactory classFactory = outline.getClassFactory();
		// coordinateClassConverter = classFactory.createClass(kmlpackage, JMod.PUBLIC | JMod.FINAL, "CoordinatesConverter", null,
		// ClassType.CLASS);
		final JDefinedClass coordinateClassConverter = pool.getClassCoordinateConverter();
		final JDefinedClass coordinateClass = pool.getClassCoordinate();

		final JClass listGenericsCoordinates = outline.getCodeModel().ref(List.class).narrow(coordinateClass);
		final JClass arraylistGenericsCoordinates = outline.getCodeModel().ref(ArrayList.class).narrow(coordinateClass);
		final JClass xmladapter = outline.getCodeModel().ref(XmlAdapter.class).narrow(String.class).narrow(listGenericsCoordinates);
		coordinateClassConverter._extends(xmladapter);

		// final JMethod stringArgConstructor = coordinateClassConverter.constructor(JMod.PRIVATE);

		// toString
		final JMethod marshall = coordinateClassConverter.method(JMod.PUBLIC, String.class, "marshal");
		final JVar stringConstructorArg = marshall.param(JMod.FINAL, listGenericsCoordinates, "dt");
		marshall._throws(Exception.class);
		marshall.annotate(Override.class);
		final JVar sbVarMarshall = marshall.body().decl(stringBuilderClass, "sb", JExpr._new(stringBuilderClass));
		final JForEach forMarshall = marshall.body().forEach(coordinateClass, "coord", stringConstructorArg);
		// forMarshall.body().add(sbVarMarshall.invoke("append").arg(forMarshall.var().name() + " + \" \""));
		forMarshall.body().add(sbVarMarshall.invoke("append").arg(JExpr.direct("coord + \" \"")));
		marshall.body()._return(sbVarMarshall.invoke("toString").invoke("trim"));

		// toString
		final JMethod unmarshall = coordinateClassConverter.method(JMod.PUBLIC, listGenericsCoordinates, "unmarshal");
		final JVar unmarshallparam = unmarshall.param(JMod.FINAL, String.class, "s");
		unmarshall._throws(Exception.class);
		unmarshall.annotate(Override.class);

		//		http://code.google.com/p/javaapiforkml/issues/detail?id=10
		//		changed:
		//			String[] coords = s.replaceAll(",[ ]+?", ",").trim().split(" ");
		//			to:
		//			String[] coords = s.replaceAll(",\\s+", ",").trim().split("\\s+");
		final JVar varCoords1 = unmarshall.body().decl(stringClass.array(), "coords",
		    unmarshallparam.invoke("replaceAll").arg(",[\\s]+").arg(",").invoke("trim").invoke("split").arg("\\s+"));

		final JVar coordinateslist = unmarshall.body().decl(listGenericsCoordinates, "coordinates", JExpr._new(arraylistGenericsCoordinates));
		unmarshall.body()._if(JExpr.ref(varCoords1.name()).ref("length").lte(JExpr.lit(0)))._then().block()._return(coordinateslist);

		final JForEach forUnMarshall = unmarshall.body().forEach(stringClass, "string", varCoords1);
		forUnMarshall.body().add(coordinateslist.invoke("add").arg(JExpr._new(coordinateClass).arg(forUnMarshall.var())));
		unmarshall.body()._return(coordinateslist);
	}

	private void addSimpleSetterAndGetter(final JDefinedClass coordinateClass, final JFieldVar arg) {
		final JMethod getMethod = coordinateClass.method(JMod.PUBLIC, cm.DOUBLE, "get" + Util.upperFirst(arg.name()));
		getMethod.body()._return(arg);

		final JMethod setMethod = coordinateClass.method(JMod.PUBLIC, coordinateClass, "set" + Util.upperFirst(arg.name()));
		addConstructorParam(setMethod, arg);
		setMethod.body()._return(JExpr._this());

	}

	private void addConstructorParam(final JMethod constructor, final JFieldVar arg) {
		final JVar constructorArg = constructor.param(JMod.FINAL, cm.DOUBLE, arg.name());
		constructor.body().assign(JExpr.refthis(arg.name()), constructorArg);
	}

	@Override
	public void execute() {
		for (final ClassOutline classOutline : outline.getClasses()) {
			convertComplexTypesToSimpleTypes((ClassOutlineImpl) classOutline);
		}
	}

	private void convertComplexTypesToSimpleTypes(final ClassOutlineImpl cc) {
		final JDefinedClass implClass = cc.implClass;
		// if no fields are present return
		if (implClass.fields().isEmpty()) {
			return;
		}

		for (final JFieldVar jFieldVar : implClass.fields().values()) {

			if (jFieldVar.name().equals("coordinates")) {
				LOG.info("1+++ " + cc.implRef.name() + " " + jFieldVar.type().name() + " " + jFieldVar.name());

				final JDefinedClass candidateClass = cc.implClass;
				final JClass newInterfaceClass = candidateClass.owner().ref(interfaceClass).narrow(pool.getClassCoordinate());
				final JClass newCollectionClass = candidateClass.owner().ref(collectionClass).narrow(pool.getClassCoordinate());

				jFieldVar.type(newInterfaceClass);
				// Find original getter and setter methods to remove.
				final ArrayList<JMethod> methodsToRemove = new ArrayList<JMethod>();
				for (final JMethod m : candidateClass.methods()) {
					if (m.name().equals("set" + Util.upperFirst(jFieldVar.name())) || m.name().equals("get" + Util.upperFirst(jFieldVar.name()))) {
						methodsToRemove.add(m);
					}
				}
				// Remove original getter and setter methods.
				for (final JMethod m : methodsToRemove) {
					candidateClass.methods().remove(m);
				}
				// Add a new getter method returning the (wrapped) field added.
				// CODE: public I<T> getFieldname() { ... };
				final JMethod method = candidateClass.method(JMod.PUBLIC, newInterfaceClass, "get" + Util.upperFirst(jFieldVar.name()));

				// CODE: if (fieldName == null) fieldName = new C<T>();
				method.body()._if(JExpr.ref(jFieldVar.name()).eq(JExpr._null()))._then().assign(JExpr.ref(jFieldVar.name()),
				    JExpr._new(newCollectionClass));

				// CODE: return "fieldName";
				method.body()._return(JExpr.ref(jFieldVar.name()));

			}
		}
	}
}
