package org.jvnet.jaxb2_commons.javaforkmlapi;

import java.util.Collection;

import org.apache.log4j.Logger;
import org.jvnet.jaxb2_commons.javaforkmlapi.command.Command;
import org.xml.sax.ErrorHandler;

import com.sun.codemodel.JFieldVar;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JVar;
import com.sun.tools.xjc.Options;
import com.sun.tools.xjc.outline.ClassOutline;
import com.sun.tools.xjc.outline.Outline;

public class CleanCode extends Command {
	private static final Logger LOG = Logger.getLogger(CleanCode.class.getName());

	public CleanCode(final Outline outline, final Options opts, final ErrorHandler errorHandler, final ClazzPool pool) {
		super(outline, opts, errorHandler, pool);
	}

	@Override
	public void execute() {
		for (final ClassOutline classOutline : outline.getClasses()) {

			markDeprecated(classOutline, "de.micromata.opengis.kml.v_2_2_0.BalloonStyle", "color");
			markDeprecated(classOutline, "de.micromata.opengis.kml.v_2_2_0.Metadata", null);
			markDeprecated(classOutline, "de.micromata.opengis.kml.v_2_2_0.Snippet", null);
			markDeprecated(classOutline, "de.micromata.opengis.kml.v_2_2_0.NetworkLink", "url");
			final Collection<JFieldVar> fields = classOutline.implClass.fields().values();
			for (final JFieldVar jFieldVar : fields) {
				jFieldVar.type(Util.removeJAXBElement(cm, jFieldVar.type()));
			}

			// de.micromata.opengis.kml.v_2_2_0.BalloonStyle
			// de.micromata.opengis.kml.v_2_2_0.Metadata
			// de.micromata.opengis.kml.v_2_2_0.Snippet
			// de.micromata.opengis.kml.v_2_2_0.NetworkLink
			// List<CPropertyInfo> properties = classOutline.target.getProperties();
			// for (CPropertyInfo cPropertyInfo : properties) {
			// if (cPropertyInfo.getSchemaComponent() == null) continue;
			// cPropertyInfo.getSchemaComponent().ge
			// cPropertyInfo.getSchemaComponent().
			//		
			// XSAnnotation annotation = cPropertyInfo.getSchemaComponent().getAnnotation();
			// if (annotation == null) continue;
			// CCustomizations customizationList = ((BindInfo)annotation.getAnnotation()).toCustomizationList();
			// for (CPluginCustomization cPluginCustomization : customizationList) {
			// ;
			// }
			// // anno = (AnnotationImpl)classOutline.target.//.currentSchema.getAnnotation();
			// }
			// LOG.info("clean class: " + classOutline.implClass.fullName());
			classOutline.implClass.javadoc().clear();

			// FieldOutline[] declaredFields = classOutline.getDeclaredFields();
			// for (FieldOutline fieldOutline : declaredFields) {
			// if (fieldOutline instanceof SingleField) {
			// // LOG.info("1clean class: " + fieldOutline.getRawType().fullName());
			// // LOG.info("2clean class: " + fieldOutline.getPropertyInfo());
			// //
			// }
			// }
			// classOutline.implRef.
			final Iterable<JMethod> methods = classOutline.implClass.methods();
			for (final JMethod jMethod : methods) {
				jMethod.javadoc().clear();
				
				jMethod.type(Util.removeJAXBElement(cm, jMethod.type()));
				
				JVar[] listVarParam = jMethod.listParams();
				for (JVar jVar : listVarParam) {
					jVar.type(Util.removeJAXBElement(cm, jVar.type()));
        }
			}
		}
	}



	// temporary solution, till i figure a way out, how to get the deprecated annotations from the schema
	private void markDeprecated(final ClassOutline classOutline, final String string, final String field) {
		if (classOutline.implClass.fullName().equals(string)) {
			if (field == null) {
				classOutline.implClass.annotate(Deprecated.class);
				LOG.info("       1YY>>" + string);
			} else {
				final JFieldVar jFieldVar = classOutline.implClass.fields().get(field);
				jFieldVar.annotate(Deprecated.class);
				LOG.info("       1YY>>" + string + "#" + field);
			}
		}
	}
}
