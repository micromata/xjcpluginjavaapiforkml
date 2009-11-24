package org.jvnet.jaxb2_commons.javaforkmlapi.clone;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.xml.validation.SchemaFactory;

import org.apache.log4j.Logger;
import org.jvnet.jaxb2_commons.javaforkmlapi.ClazzPool;
import org.jvnet.jaxb2_commons.javaforkmlapi.Util;
import org.jvnet.jaxb2_commons.javaforkmlapi.command.Command;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;

import com.sun.codemodel.JBlock;
import com.sun.codemodel.JCatchBlock;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JConditional;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JExpressionImpl;
import com.sun.codemodel.JFieldRef;
import com.sun.codemodel.JFieldVar;
import com.sun.codemodel.JForEach;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JMod;
import com.sun.codemodel.JOp;
import com.sun.codemodel.JPackage;
import com.sun.codemodel.JTryBlock;
import com.sun.codemodel.JType;
import com.sun.codemodel.JVar;
import com.sun.tools.xjc.Options;
import com.sun.tools.xjc.Plugin;
import com.sun.tools.xjc.generator.bean.ClassOutlineImpl;
import com.sun.tools.xjc.model.CPropertyInfo;
import com.sun.tools.xjc.outline.ClassOutline;
import com.sun.tools.xjc.outline.EnumOutline;
import com.sun.tools.xjc.outline.Outline;
import com.sun.xml.xsom.XSAnnotation;

/**
 * {@link Plugin} This plugin modifies XJC's code generation behavior so that you get an equals() and a hashCode()-method in each genereated
 * class (as descripted in Bloch: Effective Java 2nd Edt. (page 33-50)).
 * 
 * @author Florian Bachmann
 */
public class CreateClone extends Command {
	private static final Logger LOG = Logger.getLogger(CreateClone.class.getName());

	private JCodeModel codeModel;

	private JType internalError;

	private JType cloneNotSupportedException;

	private JType collections;

	private HashSet<String> enums;

	private JClass arrayList;

	private JType object;

	public CreateClone(Outline outline, Options opts, ErrorHandler errorHandler, ClazzPool pool) {
		super(outline, opts, errorHandler, pool);
		codeModel = outline.getCodeModel();
		cloneNotSupportedException = codeModel._ref(CloneNotSupportedException.class);
		internalError = codeModel._ref(InternalError.class);
		collections = codeModel._ref(Collections.class);
		object = codeModel._ref(Object.class);
		arrayList = outline.getCodeModel().ref(ArrayList.class);
		enums = new HashSet<String>();
		for (final EnumOutline oneEnum : outline.getEnums()) {
			enums.add(oneEnum.clazz.fullName());
		}
	}

	@Override
	public void execute() {
		for (final ClassOutline classOutline : outline.getClasses()) {
			createToString(classOutline.implClass, classOutline);
		}

	}

	public void createToString(final JDefinedClass implClass, final ClassOutline classOutline) {

		JClass extends1 = implClass._extends();

		implClass._implements(Cloneable.class);
		Collection<JFieldVar> fields = Util.getAllFieldsFields(classOutline, false);

		LOG.info("generate clone fields#: " + implClass.name() + " (" + fields.size() + ") " + extends1.fullName());

		// generate hashCode() and equals()-method
		final JMethod clone = implClass.method(JMod.PUBLIC, implClass, "clone");

		// annotate with @Override
		clone.annotate(Override.class);
		// clone._throws(CloneNotSupportedException.class);

		JVar copy = clone.body().decl(implClass, "copy");

		if (extends1.fullName().equals("java.lang.Object")) {
			JTryBlock ctry = clone.body()._try();
			ctry.body().assign(copy, JExpr.cast(implClass, JExpr._super().invoke("clone")));
			ctry._catch(cloneNotSupportedException.boxify()).body()._throw(JExpr._new(internalError.boxify()).arg(JExpr.direct("_x.toString()")));
		} else {
			clone.body().assign(copy, JExpr.cast(implClass, JExpr._super().invoke("clone")));
		}

		for (JFieldVar jFieldVar : fields) {
			if (jFieldVar.type().fullName().equals("java.lang.String")) {
				continue;
			} else if (enums.contains(jFieldVar.type().fullName())) {
				System.out.println("SKIP ENUM: " + jFieldVar.type().fullName());
				continue;
			} else if (jFieldVar.name().equals("altitudeMode")) {
				System.out.println("SKIP AltitudeModeEnum: " + jFieldVar.type().fullName());
				continue;
			} else if (jFieldVar.type().fullName().equals("java.lang.Boolean")) {
				continue;
			} else if (jFieldVar.type().fullName().equals("int")) {
				continue;
			} else if (jFieldVar.type().fullName().equals("float")) {
				continue;
			} else if (jFieldVar.type().fullName().equals("double")) {
				continue;
			} else if (jFieldVar.type().fullName().equals("char")) {
				continue;
			} else if (jFieldVar.type().fullName().equals("byte")) {
				continue;
			} else if (jFieldVar.type().fullName().equals("boolean")) {
				continue;
			} else if (jFieldVar.type().fullName().startsWith("java.util.List")) {
				// System.out.println(">>>>>>>>"+jFieldVar.type().boxify().name());

				List<JClass> type = jFieldVar.type().boxify().getTypeParameters();
				JClass clazz = type.get(0);
				if (clazz.fullName().startsWith("javax.xml.bind.JAXBElement<")) {
					System.out.println("111>>>>>>>>" + clazz.fullName());
					if (clazz.fullName().startsWith("javax.xml.bind.JAXBElement<")) {

						List<JClass> typeParameters = clazz.getTypeParameters();
						JClass clazz2 = typeParameters.get(0);
						System.out.println("222>>>>>>>>" + clazz2.fullName());
						if (clazz2.fullName().startsWith("? extends")) {
							clazz = clazz2._extends();
							// if (clazz2._extends().fullName().equals("java.lang.Object")) {
							System.out.println("333a>>>>>>>" + clazz.fullName());
						} else {
							clazz = clazz2;
							System.out.println("333b>>>>>>>" + clazz.fullName());
						}
					}
				}

				clone.body().assign(copy.ref(jFieldVar),
				    JExpr._new(arrayList.narrow(clazz)).arg(JExpr.direct("get" + Util.upperFirst(jFieldVar.name()) + "().size()")));
				JForEach forEach = clone.body().forEach(clazz, "iter", jFieldVar);
				if (forEach.var().type().fullName().equals("java.lang.Object") || enums.contains(forEach.var().type().fullName()) || forEach.var().type().fullName().equals("java.lang.String") ){
					forEach.body().add(copy.ref(jFieldVar).invoke("add").arg(forEach.var()));
					System.out.println("444a>>>>>>>" + forEach.var().type().fullName());
				} else {
					forEach.body().add(copy.ref(jFieldVar).invoke("add").arg(forEach.var().invoke("clone")));
					System.out.println("444b>>>>>>>" + forEach.var().type().fullName()+".clone()");
				}
				

				// clone.body().add(collections.boxify().staticInvoke("copy").arg(copy.ref(jFieldVar)).arg(jFieldVar));

				// copy.geometrySimpleExtension = new ArrayList<Object>((getGeometrySimpleExtension().size()));
				// for (Object iterable_element : geometrySimpleExtension) {
				// copy.geometrySimpleExtension.add(iterable_element);
				// }

			} else {
				JExpression cond = JOp.cond(jFieldVar.eq(JExpr._null()), JExpr._null(), JExpr.cast(jFieldVar.type(), jFieldVar.invoke("clone")));
				clone.body().assign(copy.ref(jFieldVar), cond);
				System.out.println(jFieldVar.type().fullName());
				// clone.body().assign(copy.ref(jFieldVar), JExpr.cast(jFieldVar.type(), jFieldVar.invoke("clone")));
			}
		}

		clone.body()._return(copy);

	}

}
