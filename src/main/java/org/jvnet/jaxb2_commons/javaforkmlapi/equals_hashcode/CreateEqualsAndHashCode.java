package org.jvnet.jaxb2_commons.javaforkmlapi.equals_hashcode;

import java.util.ArrayList;

import org.apache.log4j.Logger;
import org.jvnet.jaxb2_commons.javaforkmlapi.ClazzPool;
import org.jvnet.jaxb2_commons.javaforkmlapi.command.Command;
import org.xml.sax.ErrorHandler;

import com.sun.codemodel.JConditional;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JFieldRef;
import com.sun.codemodel.JFieldVar;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JMod;
import com.sun.codemodel.JOp;
import com.sun.codemodel.JType;
import com.sun.codemodel.JVar;
import com.sun.tools.xjc.Options;
import com.sun.tools.xjc.Plugin;
import com.sun.tools.xjc.generator.bean.ClassOutlineImpl;
import com.sun.tools.xjc.outline.ClassOutline;
import com.sun.tools.xjc.outline.Outline;

/**
 * {@link Plugin} This plugin modifies XJC's code generation behavior so that you get an equals() and a hashCode()-method in each genereated
 * class (as descripted in Bloch: Effective Java 2nd Edt. (page 33-50)).
 * 
 * @author Florian Bachmann
 */
public class CreateEqualsAndHashCode extends Command {
	private static final Logger LOG = Logger.getLogger(CreateEqualsAndHashCode.class.getName());

	public CreateEqualsAndHashCode(final Outline outline, final Options opts, final ErrorHandler errorHandler, final ClazzPool pool) {
		super(outline, opts, errorHandler, pool);
	}

	@Override
	public void execute() {
		for (final ClassOutline classOutline : outline.getClasses()) {
			createHashCodeAndEqualsMethod(classOutline.implClass, classOutline);
		}

	}

	public static void createHashCodeAndEqualsMethod(final JDefinedClass implClass, final ClassOutline classOutline) {
		// if no fields are present return
		if (implClass.fields().isEmpty()) {
			return;
		}
		// check for suitable fields
		final ArrayList<JFieldVar> fields = getRelevantFields(implClass);

		// generate only if suitable fields exist
		if (fields.size() == 0) {
			return;
		}
		LOG.info("generateEquals and hashCode field#: "+ implClass.name() + " (" + fields.size() + ")");

		// generate hashCode() and equals()-method
		final JMethod hashCodeMethod = implClass.method(JMod.PUBLIC, implClass.owner().INT, "hashCode");
		final JMethod equalsMethod = implClass.method(JMod.PUBLIC, implClass.owner().BOOLEAN, "equals");

		boolean isSuperClass = false;
		if (classOutline != null) {
			isSuperClass = checkForSuperClass((ClassOutlineImpl)classOutline);
		}
		// annotate with @Override
		hashCodeMethod.annotate(Override.class);
		equalsMethod.annotate(Override.class);

		// add paramter for equals()-method
		final JVar vObj = equalsMethod.param(Object.class, "obj");

		// is it me? this==obj -> true
		final JConditional condMe = equalsMethod.body()._if(JExpr._this().eq(vObj));
		condMe._then()._return(JExpr.TRUE);

		// it's null? obj==null -> false
		final JConditional condNull = equalsMethod.body()._if(vObj.eq(JExpr._null()));
		condNull._then()._return(JExpr.FALSE);

		// paternity test (only add if equals in superclass isn't overridden)?
		// !super.equals(obj) -> false --> (super.equals(obj) == false)
		if (isSuperClass) {
			final JConditional condSuper = equalsMethod.body()._if(JExpr._super().invoke("equals").arg(vObj).eq(JExpr.FALSE));
			condSuper._then()._return(JExpr.FALSE);
		}

		// suit the class? !(obj instanceof TypeB) -> false --> if ((obj instanceof TypeB) == false)
		final JConditional condInstance = equalsMethod.body()._if(vObj._instanceof(implClass).eq(JExpr.FALSE));
		condInstance._then()._return(JExpr.FALSE);

		// cast: TYPE other = (TYPE) obj;
		final JVar vOther = equalsMethod.body().decl(implClass, "other", JExpr.cast(implClass, vObj));

		/*
		 * check for each declared field distinguish between primitive types:
		 * 
		 * <pre> if (field != other.field) { return false; } </pre>
		 * 
		 * and reference types: <pre> if (field == null) { if (other.field != null) { return false; } } else if (!field.equals(other.field)) {
		 * // --> if (field.equals(other.field) == false) return false; } </code>
		 */
		JConditional condFieldCheck;
		boolean containsDouble = false;
		for (final JFieldVar jFieldVar : fields) {
			if (jFieldVar.type().fullName().equals("java.lang.Double") || jFieldVar.type().fullName().equals("double")) {
				jFieldVar.type(implClass.owner().DOUBLE);
				containsDouble = true;
			//	break;
			}
		}

		for (final JFieldVar jFieldVar : fields) {
			if (jFieldVar.type().isPrimitive()) {
				// LOG.info("JConditional: " + jFieldVar.name() +
				// " is PRIMITIVE");
				condFieldCheck = equalsMethod.body()._if(JExpr.ref(jFieldVar.name()).ne(vOther.ref(jFieldVar.name())));
				condFieldCheck._then()._return(JExpr.FALSE);
			} else {
				// LOG.info("JConditional: " + jFieldVar.name() +
				// " is REFERENCE");
				condFieldCheck = equalsMethod.body()._if(JExpr.ref(jFieldVar.name()).eq(JExpr._null()));
				condFieldCheck._then()._if(vOther.ref(jFieldVar.name()).ne(JExpr._null()))._then()._return(JExpr.FALSE);
				condFieldCheck._elseif(JExpr.ref(jFieldVar.name()).invoke("equals").arg(vOther.ref(jFieldVar.name())).eq(JExpr.FALSE))._then()
				    ._return(JExpr.FALSE);
			}
		}

		// ir all works out, the objects are equal, return true
		equalsMethod.body()._return(JExpr.TRUE);

		// useful fields
		final JFieldRef vPrime = JExpr.ref("prime");
		final JFieldRef vResult = JExpr.ref("result");

		// ==> final int prime = 31;
		hashCodeMethod.body().assign(JExpr.ref("final int prime"), JExpr.ref("31"));

		// ==> int result = 1; || int result = super.hashCode();
		JExpression initializeResult = null;
		if (isSuperClass) {
			initializeResult = JExpr._super().invoke("hashCode");
		} else {
			initializeResult = JExpr.ref("1");
		}
		hashCodeMethod.body().assign(JExpr.ref("int result"), initializeResult);

		// if class contains double
		// long temp;
		JVar tempVariableForDoubleChecks = null;
		JType doubleClass = null;
		if (containsDouble == true) {
			tempVariableForDoubleChecks = hashCodeMethod.body().decl(implClass.owner().LONG, "temp");
			doubleClass = implClass.owner()._ref(Double.class);
		}

		JExpression tenaryCond;
		for (final JFieldVar jFieldVar : fields) {
			if (jFieldVar.type().isPrimitive()) {
				if (jFieldVar.type().fullName().equals("double")) {
					// temp = Double.doubleToLongBits(altitude);
					// result = prime * result + (int) (temp ^ (temp >>> 32));
					// LOG.info("implClass: " + implClass.name());
					// LOG.info("jFieldVar: " + jFieldVar.name());
					// LOG.info(" is " + jFieldVar.type().fullName());
					// LOG.info(" + " + tempVariableForDoubleChecks.name());
					// LOG.info(" + " + doubleClass.boxify().fullName());
					hashCodeMethod.body().assign(tempVariableForDoubleChecks, doubleClass.boxify().staticInvoke("doubleToLongBits").arg(jFieldVar));
					tenaryCond = JExpr.cast(implClass.owner().INT, tempVariableForDoubleChecks.xor(tempVariableForDoubleChecks.shrz(JExpr
					    .direct("32"))));
				} else {
					// if field is primitive:
					// ==> result = prime * result + field;
					tenaryCond = JExpr.ref(jFieldVar.name());
				}
			} else {
				// else:
				// ==> result = prime * result + ((field == null) ? 0 :
				tenaryCond = JOp.cond(JOp.eq(JExpr.ref(jFieldVar.name()), JExpr._null()), JExpr.ref("0"), JExpr.invoke(JExpr.ref(jFieldVar.name()),
				    "hashCode"));
			}

			final JExpression hashCodeCollection = JOp.mul(vPrime, vResult);
			final JExpression addition = JOp.plus(hashCodeCollection, tenaryCond);

			// add each value that influence to the method
			hashCodeMethod.body().assign(vResult, addition);
		}

		// the returned hashCode
		hashCodeMethod.body()._return(vResult);
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
