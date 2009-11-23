package org.jvnet.jaxb2_commons.javaforkmlapi.tostring;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import javax.xml.validation.SchemaFactory;

import org.apache.log4j.Logger;
import org.jvnet.jaxb2_commons.javaforkmlapi.ClazzPool;
import org.jvnet.jaxb2_commons.javaforkmlapi.command.Command;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;

import com.sun.codemodel.JConditional;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JFieldRef;
import com.sun.codemodel.JFieldVar;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JMod;
import com.sun.codemodel.JOp;
import com.sun.codemodel.JPackage;
import com.sun.codemodel.JType;
import com.sun.codemodel.JVar;
import com.sun.tools.xjc.Options;
import com.sun.tools.xjc.Plugin;
import com.sun.tools.xjc.generator.bean.ClassOutlineImpl;
import com.sun.tools.xjc.model.CAttributePropertyInfo;
import com.sun.tools.xjc.model.CClassInfo;
import com.sun.tools.xjc.model.CPropertyInfo;
import com.sun.tools.xjc.model.CReferencePropertyInfo;
import com.sun.tools.xjc.outline.ClassOutline;
import com.sun.tools.xjc.outline.Outline;
import com.sun.xml.xsom.XSAnnotation;

/**
 * {@link Plugin} This plugin modifies XJC's code generation behavior so that you get an equals() and a hashCode()-method in each genereated
 * class (as descripted in Bloch: Effective Java 2nd Edt. (page 33-50)).
 * 
 * @author Florian Bachmann
 */
public class CreateToString extends Command {
	private static final Logger LOG = Logger.getLogger(CreateToString.class.getName());

	public CreateToString(Outline outline, Options opts, ErrorHandler errorHandler, ClazzPool pool) {
		super(outline, opts, errorHandler, pool);
	}

	@Override
	public void execute() {
		for (final ClassOutline classOutline : outline.getClasses()) {
			createToString(classOutline.implClass, classOutline);
		}

	}

	public static void createToString(final JDefinedClass implClass, final ClassOutline classOutline) {
		// if no fields are present return

		// classOutline.target.model.

		// classOutline.target.getAnnotations();

		if (implClass.name().equals("Coordinate")) {
			return;
		}

		if (implClass.fields().isEmpty()) {
			return;
		}

		CClassInfo target = classOutline.target;
		ArrayList<String> props = new ArrayList<String>();
		if (target.isOrdered()) {
			for (CPropertyInfo p : target.getProperties()) {
				if (!(p instanceof CAttributePropertyInfo)) {
					if (!((p instanceof CReferencePropertyInfo) && ((CReferencePropertyInfo) p).isDummy())) {
						props.add(p.getName(false));
					}
				}
			}
		}

		// check for suitable fields
		final ArrayList<JFieldVar> fields = getRelevantFields(implClass);
		HashSet<JFieldVar> fff = new HashSet<JFieldVar>();
		for (JFieldVar jFieldVar : fields) {
			fff.add(jFieldVar);
    }
		
		// generate only if suitable fields exist
		if (fields.size() == 0) {
			return;
		}
		LOG.info("generate toString field#: " + implClass.name() + " (" + fields.size() + ")");

		// generate hashCode() and equals()-method
		final JMethod toString = implClass.method(JMod.PUBLIC, String.class, "toString");

		boolean isSuperClass = false;
		if (classOutline != null) {
			isSuperClass = checkForSuperClass((ClassOutlineImpl) classOutline);
		}
		// annotate with @Override
		toString.annotate(Override.class);

		for (JFieldVar jFieldVar : fields) {
			System.out.println("!!!!!!!!!!!!!!!!!!!!! " + jFieldVar.name());
		}

		// List<CPropertyInfo> properties = classOutline.target.getProperties();
		// // for (CPropertyInfo cPropertyInfo : properties) {
		// for (CPropertyInfo cPropertyInfo : properties) {
		//			
		// System.out.println("!!!!!!!!!!!!!!!!!!!!! " + cPropertyInfo.realization.toString() );
		// }
		//
		// // cast: TYPE other = (TYPE) obj;
		//
		// /*
		// * check for each declared field distinguish between primitive types:
		// *
		// * <pre> if (field != other.field) { return false; } </pre>
		// *
		// * and reference types: <pre> if (field == null) { if (other.field != null) { return false; } } else if (!field.equals(other.field)) {
		// * // --> if (field.equals(other.field) == false) return false; } </code>
		// */
		// JConditional condFieldCheck;
		// boolean containsDouble = false;
		// for (final JFieldVar jFieldVar : fields) {
		// if (jFieldVar.type().fullName().equals("java.lang.Double") || jFieldVar.type().fullName().equals("double")) {
		// jFieldVar.type(implClass.owner().DOUBLE);
		// containsDouble = true;
		// // break;
		// }
		// }
		//
		//	
		//
		//
		// // useful fields
		// final JFieldRef vPrime = JExpr.ref("prime");
		// final JFieldRef vResult = JExpr.ref("result");
		//
		// // ==> final int prime = 31;
		// toString.body().assign(JExpr.ref("final int prime"), JExpr.ref("31"));
		//
		// // ==> int result = 1; || int result = super.hashCode();
		// JExpression initializeResult = null;
		// if (isSuperClass) {
		// initializeResult = JExpr._super().invoke("hashCode");
		// } else {
		// initializeResult = JExpr.ref("1");
		// }
		// toString.body().assign(JExpr.ref("int result"), initializeResult);
		//
		// // if class contains double
		// // long temp;
		// JVar tempVariableForDoubleChecks = null;
		// JType doubleClass = null;
		// if (containsDouble == true) {
		// tempVariableForDoubleChecks = toString.body().decl(implClass.owner().LONG, "temp");
		// doubleClass = implClass.owner()._ref(Double.class);
		// }
		//
		// JExpression tenaryCond;
		// for (final JFieldVar jFieldVar : fields) {
		// if (jFieldVar.type().isPrimitive()) {
		// if (jFieldVar.type().fullName().equals("double")) {
		// // temp = Double.doubleToLongBits(altitude);
		// // result = prime * result + (int) (temp ^ (temp >>> 32));
		// // LOG.info("implClass: " + implClass.name());
		// // LOG.info("jFieldVar: " + jFieldVar.name());
		// // LOG.info(" is " + jFieldVar.type().fullName());
		// // LOG.info(" + " + tempVariableForDoubleChecks.name());
		// // LOG.info(" + " + doubleClass.boxify().fullName());
		// toString.body().assign(tempVariableForDoubleChecks, doubleClass.boxify().staticInvoke("doubleToLongBits").arg(jFieldVar));
		// tenaryCond = JExpr.cast(implClass.owner().INT, tempVariableForDoubleChecks.xor(tempVariableForDoubleChecks.shrz(JExpr
		// .direct("32"))));
		// } else {
		// // if field is primitive:
		// // ==> result = prime * result + field;
		// tenaryCond = JExpr.ref(jFieldVar.name());
		// }
		// } else {
		// // else:
		// // ==> result = prime * result + ((field == null) ? 0 :
		// tenaryCond = JOp.cond(JOp.eq(JExpr.ref(jFieldVar.name()), JExpr._null()), JExpr.ref("0"), JExpr.invoke(JExpr.ref(jFieldVar.name()),
		// "hashCode"));
		// }
		//
		// final JExpression hashCodeCollection = JOp.mul(vPrime, vResult);
		// final JExpression addition = JOp.plus(hashCodeCollection, tenaryCond);
		//
		// // add each value that influence to the method
		// toString.body().assign(vResult, addition);
		// }
		//
		// // the returned hashCode
		toString.body()._return(JExpr.lit("null"));
	}

	/*
	 * check if a superclass exist and if it defines some hashCode- and equals-worthy fields
	 */
	private static boolean checkForSuperClass(final ClassOutlineImpl classOutline) {
		if (classOutline.getSuperClass() == null) {
			return false;
		}

		if (getRelevantFields(classOutline.getSuperClass().implClass).size() == 0) {
			return false;
		}

		return true;
	}

	/*
	 * returns all fields that could be used in hashCode and equals
	 */
	private static ArrayList<JFieldVar> getRelevantFields(final JDefinedClass implClass) {
		final ArrayList<JFieldVar> fields = new ArrayList<JFieldVar>();
		int mods = 0;
		for (final JFieldVar field : implClass.fields().values()) {
			mods = field.mods().getValue();

			if ((mods & JMod.PRIVATE) != 0) {
				continue;
			}
			if ((mods & JMod.STATIC) != 0) {
				continue;
			}
			fields.add(field);
		}
		return fields;
	}

}
