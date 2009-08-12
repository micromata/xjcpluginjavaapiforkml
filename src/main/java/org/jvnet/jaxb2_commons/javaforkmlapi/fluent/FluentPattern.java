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
package org.jvnet.jaxb2_commons.javaforkmlapi.fluent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.jvnet.jaxb2_commons.javaforkmlapi.ClazzPool;
import org.jvnet.jaxb2_commons.javaforkmlapi.Util;
import org.jvnet.jaxb2_commons.javaforkmlapi.XJCJavaForKmlApiPlugin;
import org.jvnet.jaxb2_commons.javaforkmlapi.command.Command;

import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JFieldRef;
import com.sun.codemodel.JFieldVar;
import com.sun.codemodel.JInvocation;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JMod;
import com.sun.codemodel.JType;
import com.sun.codemodel.JVar;
import com.sun.tools.xjc.Options;
import com.sun.tools.xjc.generator.bean.ClassOutlineImpl;
import com.sun.tools.xjc.generator.bean.field.UntypedListField;
import com.sun.tools.xjc.model.CClassInfo;
import com.sun.tools.xjc.model.CPropertyInfo;
import com.sun.tools.xjc.outline.Aspect;
import com.sun.tools.xjc.outline.ClassOutline;
import com.sun.tools.xjc.outline.FieldOutline;
import com.sun.tools.xjc.outline.Outline;
import com.sun.tools.xjc.reader.TypeUtil;

import org.xml.sax.ErrorHandler;

public class FluentPattern extends Command {
	private static final Logger LOG = Logger.getLogger(FluentPattern.class.getName());

	private JCodeModel codeModel;

	private HashMap<String, ClassOutlineImpl> classList;

	private HashMap<String, ArrayList<CClassInfo>> subclasses;

	private JDefinedClass annotateObvicious = null;

	private JDefinedClass classCoordinates = null;

	private JDefinedClass classIcon;

	private JDefinedClass classLink;

	public FluentPattern(Outline outline, Options opts, ErrorHandler errorHandler, ClazzPool pool) {
		super(outline, opts, errorHandler, pool);
		annotateObvicious = pool.getClassObviousAnnotation();

		LOG.info(XJCJavaForKmlApiPlugin.PLUGINNAME + " generate Fluent API + " + annotateObvicious.name());

		classCoordinates = pool.getClassCoordinate();
		classList = Util.getClassList(outline);
		codeModel = outline.getCodeModel();
		subclasses = Util.findSubClasses(outline);

		classIcon = pool.getClassIcon();
		classLink = pool.getClassLink();

	}

	@Override
	public void execute() {
		LOG.info(XJCJavaForKmlApiPlugin.PLUGINNAME + " generate Fluent API");
		for (final ClassOutline classOutline : outline.getClasses()) {
			ClassOutlineImpl cc = (ClassOutlineImpl) classOutline;
			final JDefinedClass implClass = classOutline.implClass;

			for (FieldOutline fieldOutline : classOutline.getDeclaredFields()) {
				JType type = TypeUtil.getCommonBaseType(codeModel, Util.listPossibleTypes(cc, fieldOutline.getPropertyInfo()));
				if (((type.name().equals("BasicLink") || (type.name().equals("Link")) && Util.upperFirst(fieldOutline.getPropertyInfo().getName(false)).equals("Icon")))) {
					/*
					 * special case for Icon. IconStyle uses protected BasicLink icon.
					 *  subclasses.get(currentFieldName) will return, that 
					 *  possible subclasses for BasicLink are Icon and Link,
					 *  hence createAndSetLink and createAndSetIcon methods
					 *  are created. This isn't desired as IconStyle should
					 *  define protected Icon icon.
					 *  
					 *  If it is possible to change the type of a variable
					 *  at the CClassInfo-Level, this if block would be
					 *  obsolete.
					 */
					type = classIcon.unboxify();
					
				}
				
				String currentFieldName = Util.eliminateTypeSuffix(type.name());
			// if (fieldOutline.getPropertyInfo().getName(false).equals("coordinates")) {
				// LOG.info("+1 "+ cc.implRef.name() + " " + currentFieldName + " " + fieldOutline.getPropertyInfo().getName(false));
				// }
				if (currentFieldName.equals("AbstractObject")) {
					// LOG.info("skip abstract object");
					continue;
				}

				if (subclasses.containsKey(currentFieldName) == false) {
					// LOG.info("-  " + currentFieldName + " > type.name(): " + type.name());
					continue;
				}

				if (fieldOutline.getPropertyInfo().getName(false).contains("Deprecated")) {
					// LOG.info("skip deprecated");
					continue;
				}

				ArrayList<CClassInfo> subclasseslist = subclasses.get(currentFieldName);
				// LOG.info("+  " + currentFieldName + " " + fieldOutline.getPropertyInfo().getName(false));
				
//				if (cc.implClass.name().equals("Overlay")) {
//				System.out.println("<<>><<>><<>>fn "+ cc.implClass.fullName());
//				System.out.println("<<>><<>><<>>nn "+ type.name());
//				System.out.println("<<>><<>><<>>fn "+ type.unboxify().fullName());
//				System.out.println("<<>><<>><<>>nn "+ type.unboxify().name());
//				System.out.println("<<>><<>><<>>nn "+ fieldOutline.getPropertyInfo().getName(false));
//				}
				
//				if (type.unboxify().name().equals("Link") && fieldOutline.getPropertyInfo().getName(false).equals("icon")) {
//					type = classIcon.unboxify();
//				}
//				if (subclasseslist.size() > 0 && !(type.name().equals("Icon") || type.name().equals("Link") || currentFieldName.equals("Icon") || currentFieldName.equals("Link"))) {
				if (subclasseslist.size() > 0) { // && !(type.name().equals("Icon") || type.name().equals("Link") || currentFieldName.equals("Icon") || currentFieldName.equals("Link"))) {
					for (CClassInfo cClassInfo : subclasseslist) {
						System.out.println("1<<>><<>><<>>fn 1:"+ cc.implClass.name() + " 2:" + cClassInfo.toType(outline, Aspect.EXPOSED).name() + " 3:" + cClassInfo.shortName + " 4:"+type.name() );
						generateCreateAndSetOrAddMethod(outline, cc, implClass, fieldOutline, cClassInfo.toType(outline, Aspect.EXPOSED),
						    cClassInfo.shortName);
					}
					continue;
				}

				// use variable-name everywhere instead of variable-type-name (because of Vec2-name conflict)
				System.out.println("2<<>><<>><<>>fn 1:"+ cc.implClass.name() + " 2:" + type.name() + " 3:" + Util.upperFirst(fieldOutline.getPropertyInfo().getName(false)) + " 4:"+type.name());
				generateCreateAndSetOrAddMethod(outline, cc, implClass, fieldOutline, type, Util.upperFirst(fieldOutline.getPropertyInfo().getName(false)));
				
			}

			generateSetAndAddToCollection(cc);
			generateWithMethods(cc);

		}

	}

	/*
	 * generates the following code:
	 * 
	 * <pre> void setField(T field) { this.field = field; } </pre>
	 */
	private void generateSetCollection(final ClassOutlineImpl cc, final JFieldVar field, boolean override) {
		// creates the setter
		final JMethod generateSet = cc.implClass.method(JMod.PUBLIC, cc.implClass.owner().VOID, "set" + Util.upperFirst(field.name()));
		final JVar value = generateSet.param(JMod.FINAL, field.type(), field.name());
		// set the assignment to the body: this.value = value;
		if (override) {
			if (annotateObvicious != null) {
				generateSet.annotate(annotateObvicious);
			}
			generateSet.annotate(Override.class);
			// super.setObjectSimpleExtensionGroup(objectSimpleExtensionGroup);
			// generateSet.body().assign(JExpr._this().ref(field.name()), value);
			generateSet.body().directStatement("super.set" + Util.upperFirst(field.name()) + "(" + value.name() + ");");
		} else {
			generateSet.javadoc().append("Sets the value of the " + field.name() + " property");
			generateSet.javadoc().addParam(value);
			generateSet.javadoc().append("Objects of the following type(s) are allowed in the list ");
			generateSet.javadoc().append(field.type().name());
			generateSet.javadoc().append(".\n<p>Note:\n<p>");
			generateSet.javadoc().append("This method does not make use of the fluent pattern.");
			generateSet.javadoc()
			    .append("If you would like to make it fluent, use {@link #with" + Util.upperFirst(field.name()) + "} instead.\n");
			generateSet.body().assign(JExpr._this().ref(field.name()), value);
		}
		// LOG.info("s> " + implClass.name() + " >> public void set" + JaxbUtil.upperFirst(field.name()) + "(" + field.type().name() +
		// ") created [Override: " + override + "].");
	}

	/**
	 * generates the following code:
	 * 
	 * <pre>
	 * boolean addCollection(T field) {
	 * 	if (collection == null) {
	 * 		collection = new ArrayList&lt;T&gt;();
	 * 	}
	 * 	return this.collection.add(field);
	 * }
	 * // changed into: return this.getCollection().add(abstractFeatureGroup);
	 * </pre>
	 * @param type2
	 */
	private void generateAddToCollection(final ClassOutlineImpl cc, final JFieldVar field, boolean override) {
		CPropertyInfo propertyInfo = Util.searchPropertyInfo(cc, field.name());
		if (propertyInfo == null) {
			return;
		}

		if (field.name().equals("coordinates") && !override) {
			System.out.println("its a coordinate");
			coordinateAddtoCase(cc, field, propertyInfo);
			return;
		}

		commonAddtoCase(cc, field, override, propertyInfo);
	}

	private void coordinateAddtoCase(ClassOutlineImpl cc, JFieldVar field, CPropertyInfo propertyInfo) {

		Collection<JFieldVar> coordinateCreateMethods = new ArrayList<JFieldVar>();
		JFieldVar longitude = classCoordinates.fields().get("longitude");
		JFieldVar latitude = classCoordinates.fields().get("latitude");

		coordinateCreateMethods.add(longitude);
		coordinateCreateMethods.add(latitude);
		createArgFactoryMethod(cc, coordinateCreateMethods);

		JFieldVar altitude = classCoordinates.fields().get("altitude");
		coordinateCreateMethods.add(altitude);
		createArgFactoryMethod(cc, coordinateCreateMethods);

		coordinateCreateMethods.clear();

		// create public Point addToCoordinates(final String coordinates) {...}
		final JMethod generateAdd = cc.implClass.method(JMod.PUBLIC, cc.implClass, "addToCoordinates");
		generateAdd.javadoc().append("add a value to the coordinates property collection");
		JInvocation returntype = JExpr._new(classCoordinates);
		final JVar arg = generateAdd.param(JMod.FINAL, String.class, field.name());
		generateAdd.javadoc().addParam(arg).append("required parameter");
		returntype.arg(arg);

		generateAdd.javadoc().addReturn().append("<tt>true</tt> (as general contract of <tt>Collection.add</tt>). ");
		generateAdd.body().add(JExpr._this().invoke("getCoordinates").invoke("add").arg(returntype));
		generateAdd.body()._return(JExpr._this());

	}

	private void createArgFactoryMethod(ClassOutlineImpl cc, Collection<JFieldVar> coordinateCreateMethods) {
		final JMethod generateAdd = cc.implClass.method(JMod.PUBLIC, cc.implClass, "addToCoordinates");
		generateAdd.javadoc().append("add a value to the coordinates property collection");
		JInvocation returntype = JExpr._new(classCoordinates);
		for (JFieldVar field : coordinateCreateMethods) {

			final JVar arg = generateAdd.param(JMod.FINAL, field.type(), field.name());
			generateAdd.javadoc().addParam(arg).append("required parameter");
			returntype.arg(arg);
		}

		generateAdd.javadoc().addReturn().append("<tt>true</tt> (as general contract of <tt>Collection.add</tt>). ");
		generateAdd.body().add(JExpr._this().invoke("getCoordinates").invoke("add").arg(returntype));
		generateAdd.body()._return(JExpr._this());

	}

	private void commonAddtoCase(final ClassOutlineImpl cc, final JFieldVar field, boolean override, CPropertyInfo propertyInfo) {
		// find the common type
		final JType type = TypeUtil.getCommonBaseType(codeModel, Util.listPossibleTypes(cc, propertyInfo));

		// creates the add method
		final JMethod generateAdd = cc.implClass.method(JMod.PUBLIC, cc.implClass, "addTo" + Util.upperFirst(field.name()));
		final JVar value = generateAdd.param(JMod.FINAL, type, field.name());

		if (override) {
			if (annotateObvicious != null) {
				generateAdd.annotate(annotateObvicious);
			}
			generateAdd.annotate(Override.class);
			// if (field.name().equals("coordinates")) {
			// LOG.info("####################### coordinates found : create new #######################");
			// generateAdd.body().add(JExpr._super().invoke("get" +
			// Util.upperFirst(field.name())).invoke("add").arg(JExpr._new(annotateCoordinates).arg(value)));
			// } else {
			generateAdd.body().add(JExpr._super().invoke("get" + Util.upperFirst(field.name())).invoke("add").arg(value));
			// LOG.info("#######################  ####################### " + field.name());
			// }

			// generateAdd.body().directStatement("super.get" + Util.upperFirst(field.name()) + "().add(" + value.name() + ");");
		} else {
			generateAdd.javadoc().append("add a value to the " + field.name() + " property collection");
			generateAdd.javadoc().addParam(value).append("Objects of the following type are allowed in the list: ").append(
			    Util.listPossibleTypes(cc, propertyInfo));
			generateAdd.javadoc().addReturn().append("<tt>true</tt> (as general contract of <tt>Collection.add</tt>). ");
			generateAdd.body().directStatement("this.get" + Util.upperFirst(field.name()) + "().add(" + value.name() + ");");

		}
		generateAdd.body()._return(JExpr._this());
		// LOG.info("a> " + cc.implClass.name() + " >> public " + cc.implClass.name() + " addTo" + Util.upperFirst(field.name()) + "(" +
		// type.name() + ") created [Override: " + override + "].");
	}

	/*
	 * generates the following code:
	 * 
	 * <pre> void setField(T field) { this.field = field; } </pre>
	 */
	private void generateSetAndAddToCollection(ClassOutlineImpl cc) {
		Collection<JFieldVar> optionalFluentFields = Util.getAllFieldsFields(cc, true);

		if (optionalFluentFields.size() == 0) {
			return;
		}

		// LOG.info(cc.implRef.name() + " contains fields: " + optionalFluentFields.size());
		for (JFieldVar field : optionalFluentFields) {
			generateSetCollection(cc, field, false);
			generateAddToCollection(cc, field, false);
		}

		// check for methods in superclass
		Collection<JFieldVar> superclassFields = Util.getSuperclassAllFields(cc, true);
		if (superclassFields.size() == 0) {
			return;
		}

		// LOG.info(cc.implRef.name() + " contains super fields: " + superclassFields.size());
		for (JFieldVar field : superclassFields) {
			generateSetCollection(cc, field, true);
			generateAddToCollection(cc, field, true);
		}
	}

	/*
	 * generates the following code:
	 * 
	 * <pre> void setField(T field) { this.field = field; } </pre>
	 */
	private void generateWith(final ClassOutlineImpl cc, final JFieldVar field, boolean override) {
		// creates the setter
		final JMethod generateWith = cc.implClass.method(JMod.PUBLIC, cc.implClass, "with" + Util.upperFirst(field.name()));
		final JVar value = generateWith.param(JMod.FINAL, field.type(), field.name());
		// set the assignment to the body: this.value = value;
		if (override) {
			if (annotateObvicious != null) {
				generateWith.annotate(annotateObvicious);
			}
			generateWith.annotate(Override.class);
			// super.setObjectSimpleExtensionGroup(objectSimpleExtensionGroup);
			// generateSet.body().assign(JExpr._this().ref(field.name()), value);
			generateWith.body().directStatement("super.with" + Util.upperFirst(field.name()) + "(" + field.name() + ");");
		} else {
			generateWith.javadoc().add("fluent setter");
			generateWith.javadoc().addParam(value).append("required parameter");
			generateWith.body().directStatement("this.set" + Util.upperFirst(field.name()) + "(" + field.name() + ");");
		}
		// generateSet.body().directStatement("this."+field.name() + " = " + field.name()+";");
		// generate the javadoc
		generateWith.body()._return(JExpr._this());
		// LOG.info("w> " + cc.implClass.name() + " >> public " + cc.implClass.name() + " with" + Util.upperFirst(field.name()) + "("
		// +field.type().name() + ") created [Override: " + override + "].");
	}

	private void generateWithMethods(final ClassOutlineImpl cc) {
		Collection<JFieldVar> optionalFluentFields = Util.getFields(cc, false, false);
		if (optionalFluentFields.size() == 0) {
			return;
		}

		// LOG.info(cc.implRef.name() + " contains fields: " + optionalFluentFields.size());
		for (JFieldVar field : optionalFluentFields) {
			generateWith(cc, field, false);
		}

		// check for methods in superclass
		Collection<JFieldVar> superclassFields = Util.getSuperclassFields(cc, false, false);
		if (superclassFields.size() == 0) {
			return;
		}

		// LOG.info(cc.implRef.name() + " contains super fields: " + superclassFields.size());
		for (JFieldVar field : superclassFields) {
			generateWith(cc, field, true);
		}
	}

	private void generateCreateAndSetOrAddMethod(Outline outline, ClassOutlineImpl cc, final JDefinedClass implClass,
	    FieldOutline fieldOutline, JType cClassInfo, String shortName) {

		StringBuffer debugOut = new StringBuffer();

		String localName = "newValue";
		final CPropertyInfo property = fieldOutline.getPropertyInfo();
		StringBuffer methodName = new StringBuffer();
		ArrayList<String> javadoc = new ArrayList<String>();
		methodName.append("createAnd");

		JInvocation methodInvoke = null;
		if (property.isCollection()) {
			methodName.append("Add");
			methodInvoke = JExpr._this().invoke("get" + property.getName(true)).invoke("add").arg(JExpr.ref(localName));
			javadoc.add("and adds it to " + property.getName(false) + ".");
			javadoc.add("\n");
			javadoc.add("This method is a short version for:\n");
			javadoc.add("<code>\n");
			javadoc.add("" + cClassInfo.name() + " " + Util.lowerFirst(cClassInfo.name()) + " = new " + cClassInfo.name() + "();\n");
			javadoc.add("this.get" + property.getName(true) + "().add(" + Util.lowerFirst(cClassInfo.name()) + ");");
			javadoc.add("</code>\n");

		} else {
			methodName.append("Set");
			methodInvoke = JExpr._this().invoke("set" + property.getName(true)).arg(JExpr.ref(localName));
			javadoc.add("and set it to " + property.getName(false) + ".\n");
			javadoc.add("\n");
			javadoc.add("This method is a short version for:\n");
			javadoc.add("<code>\n");
			javadoc.add("" + cClassInfo.name() + " " + Util.lowerFirst(cClassInfo.name()) + " = new " + cClassInfo.name() + "();\n");
			javadoc.add("this.set" + property.getName(true) + "(" + Util.lowerFirst(cClassInfo.name()) + ");");
			javadoc.add("</code>\n");
		}

		// cClassInfo
		ClassOutlineImpl asClass = classList.get(cClassInfo.fullName());

		Collection<JFieldVar> relevantFields = Util.getConstructorRequiredFields(asClass);

		Map<String, FieldOutline> fieldOutlineasMap = Util.getRequiredFieldsAsMap(cc);

		JMethod m = implClass.method(JMod.PUBLIC, cClassInfo, methodName.toString() + shortName);
		JInvocation args = JExpr._new(cClassInfo);
		for (JFieldVar field : relevantFields) {
			// FieldOutline fo = fieldOutlineasMap.get(field.name());
			// if (fo == null) {
			// continue;
			// }
			// if (fo.getPropertyInfo().isCollection()) {
			// System.out.println("!!!!! " + cc.implClass.name() + " is collection " + methodName );
			// continue;
			// }
			final JVar arg = m.param(JMod.FINAL, field.type(), field.name());
			args.arg(JExpr.ref(field.name()));
			m.javadoc().addParam(arg).append("required parameter");
			// m.body().assign(JExpr.refthis(field.name()), arg);
		}

		m.body().decl(cClassInfo, localName, args);

		m.body().add(methodInvoke);
		m.body()._return(JExpr.ref(localName));

		m.javadoc().append("Creates a new instance of ");
		m.javadoc().append(cClassInfo);
		m.javadoc().append(javadoc);
		debugOut.append("m> " + cc.implRef.name() + " :: public " + cClassInfo.name() + " " + methodName.toString() + shortName + "(");

		for (JFieldVar field : relevantFields) {
			debugOut.append(field.type().name() + ", ");
		}
		if (relevantFields.size() > 0) {
			debugOut.delete(debugOut.length() - 2, debugOut.length());
		}
		debugOut.append(") created.");

		// LOG.info(debugOut.toString());
	}

}
