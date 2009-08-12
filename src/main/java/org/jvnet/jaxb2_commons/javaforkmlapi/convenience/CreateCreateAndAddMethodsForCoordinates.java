package org.jvnet.jaxb2_commons.javaforkmlapi.convenience;

import java.util.ArrayList;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

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
import com.sun.codemodel.JInvocation;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JMod;
import com.sun.codemodel.JVar;
import com.sun.tools.xjc.Options;
import com.sun.tools.xjc.generator.bean.ClassOutlineImpl;
import com.sun.tools.xjc.outline.ClassOutline;
import com.sun.tools.xjc.outline.Outline;

public class CreateCreateAndAddMethodsForCoordinates extends Command {
	private static final Logger LOG = Logger.getLogger(CreateCreateAndAddMethodsForCoordinates.class.getName());

	private JCodeModel codeModel;

	private JDefinedClass annotateCoordinates;


	public CreateCreateAndAddMethodsForCoordinates(Outline outline, Options opts, ErrorHandler errorHandler, ClazzPool pool) {
		super(outline, opts, errorHandler, pool);
		codeModel = outline.getCodeModel();
		annotateCoordinates = pool.getClassCoordinate();
	}

	@Override
	public void execute() {
		for (final ClassOutline classOutline : outline.getClasses()) {
			ClassOutlineImpl cc = (ClassOutlineImpl) classOutline;
			generateGetClassMethod(cc);

			for (final JFieldVar jFieldVar : cc.implClass.fields().values()) {
				if (jFieldVar.name().equals("coordinates")) {
					generateCreateAndSetCoordinatesMethod(outline, cc, cc.implClass, jFieldVar);
				}
			}
		}
	}

	@SuppressWarnings("unchecked")
	protected Class interfaceClass = java.util.List.class;

	@SuppressWarnings("unchecked")
	protected Class collectionClass = java.util.ArrayList.class;

	private void generateCreateAndSetCoordinatesMethod(Outline outline, ClassOutlineImpl cc, final JDefinedClass implClass, JFieldVar fieldVar) {

		StringBuffer debugOut = new StringBuffer();

		JClass jaxbElementClass = null;
		if (annotateCoordinates != null) {
			fieldVar.annotate(XmlElement.class).param("namespace", "http://www.opengis.net/kml/2.2").param("type", String.class);
			fieldVar.annotate(XmlJavaTypeAdapter.class).param("value", pool.getClassCoordinateConverter());
			jaxbElementClass = codeModel.ref(ArrayList.class).narrow(annotateCoordinates);
		}
		// cClassInfo
		String createAndSetCoordinatesName = "createAndSetCoordinates";
		JMethod m = implClass.method(JMod.PUBLIC, fieldVar.type(), createAndSetCoordinatesName);
		JVar newValue = m.body().decl(fieldVar.type(), "newValue", JExpr._new(jaxbElementClass));
		JInvocation methodInvoke = JExpr._this().invoke("set" + Util.upperFirst(fieldVar.name())).arg(newValue);
		m.body().add(methodInvoke);
		m.body()._return(newValue);

		m.javadoc().append("Creates a new instance of ");
		m.javadoc().append(fieldVar.type());
		m.javadoc().append("and set it to this." + fieldVar.name() + ".\n");

		ArrayList<String> javadoc = new ArrayList<String>();
		javadoc.add("\n");
		javadoc.add("This method is a short version for:\n");
		javadoc.add("<pre>\n");
		javadoc.add("<code>\n");
		javadoc.add("" + fieldVar.type().name() + " " + newValue.name() + " = new " + fieldVar.type().name() + "();\n");
		javadoc.add("this.set" + Util.upperFirst(fieldVar.name()) + "(" + newValue.name() + ");");
		javadoc.add("</code>\n");
		javadoc.add("</pre>\n");

		m.javadoc().append(javadoc);
		debugOut.append("m> " + cc.implRef.name() + " :: public " + fieldVar.type().name() + " " + createAndSetCoordinatesName + "() created.");
		LOG.info(debugOut.toString());
	}

	private void generateGetClassMethod(ClassOutlineImpl cc) {
		if (cc.target.isAbstract()) {
			return;
		}
		// final JMethod generateWith = cc.implClass.method(JMod.PUBLIC, Class.class, "type");
		// generateWith.body()._return(cc.implRef.dotclass());

		// final JMethod generateWith = cc.implClass.method(JMod.PUBLIC, Class.class, "type");
		// generateWith.body()._return(cc.implRef.dotclass());

		// creates the setter
		// final JMethod generateWith = cc.implClass.method(JMod.PUBLIC, cc.implClass, "getType");
		// final JVar value = generateWith.param(JMod.FINAL, field.type(), field.name());
		// // set the assignment to the body: this.value = value;
		// if (override) {
		// generateWith.annotate(Obvious.class);
		// generateWith.annotate(Override.class);
		// // super.setObjectSimpleExtensionGroup(objectSimpleExtensionGroup);
		// // generateSet.body().assign(JExpr._this().ref(field.name()), value);
		// generateWith.body().directStatement("super.with" + Util.upperFirst(field.name()) + "(" + field.name() + ");");
		// } else {
		// generateWith.javadoc().add("fluent setter");
		// generateWith.javadoc().addParam(value).append("required parameter");
		// generateWith.body().directStatement("this.set" + Util.upperFirst(field.name()) + "(" + field.name() + ");");
		// }
		// // generateSet.body().directStatement("this."+field.name() + " = " + field.name()+";");
		// // generate the javadoc
		// generateWith.body()._return(JExpr._this());
	}
}
