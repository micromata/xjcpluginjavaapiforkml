package org.jvnet.jaxb2_commons.javaforkmlapi;

import java.util.Collection;
import java.util.List;

import org.apache.log4j.Logger;
import org.jvnet.jaxb2_commons.javaforkmlapi.command.Command;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.xml.sax.ErrorHandler;

import com.sun.codemodel.JFieldVar;
import com.sun.codemodel.JMethod;
import com.sun.tools.xjc.Options;
import com.sun.tools.xjc.generator.bean.ClassOutlineImpl;
import com.sun.tools.xjc.generator.bean.field.SingleField;
import com.sun.tools.xjc.model.CCustomizations;
import com.sun.tools.xjc.model.CPluginCustomization;
import com.sun.tools.xjc.model.CPropertyInfo;
import com.sun.tools.xjc.outline.ClassOutline;
import com.sun.tools.xjc.outline.FieldOutline;
import com.sun.tools.xjc.outline.Outline;
import com.sun.tools.xjc.reader.xmlschema.bindinfo.BindInfo;
import com.sun.xml.xsom.XSAnnotation;
import com.sun.xml.xsom.impl.AnnotationImpl;

public class CleanCode extends Command {
	private static final Logger LOG = Logger.getLogger(CleanCode.class.getName());

	public CleanCode(Outline outline, Options opts, ErrorHandler errorHandler, ClazzPool pool) {
		super(outline, opts, errorHandler, pool);
	}

	@Override
	public void execute() {
		for (final ClassOutline classOutline : outline.getClasses()) {
			
			
				
				markDeprecated(classOutline, "de.micromata.opengis.kml.v_2_2_0.BalloonStyle", "color");
				markDeprecated(classOutline, "de.micromata.opengis.kml.v_2_2_0.Metadata", null);
				markDeprecated(classOutline, "de.micromata.opengis.kml.v_2_2_0.Snippet", null);
				markDeprecated(classOutline, "de.micromata.opengis.kml.v_2_2_0.NetworkLink", "url");
//				de.micromata.opengis.kml.v_2_2_0.BalloonStyle
//				de.micromata.opengis.kml.v_2_2_0.Metadata
//				de.micromata.opengis.kml.v_2_2_0.Snippet
//				de.micromata.opengis.kml.v_2_2_0.NetworkLink
//			List<CPropertyInfo> properties = classOutline.target.getProperties();
//		for (CPropertyInfo cPropertyInfo : properties) {
//		if (cPropertyInfo.getSchemaComponent() == null) continue;
//		cPropertyInfo.getSchemaComponent().ge
//		System.out.println("       1YY>>" + cPropertyInfo.getName(true));
//		System.out.println("       1YY>>" + cPropertyInfo.javadoc);
//		cPropertyInfo.getSchemaComponent().
//		
//		XSAnnotation annotation = cPropertyInfo.getSchemaComponent().getAnnotation();
//		if (annotation == null) continue;
//		CCustomizations customizationList = ((BindInfo)annotation.getAnnotation()).toCustomizationList();
//		for (CPluginCustomization cPluginCustomization : customizationList) {
//	    ;
//	  	System.out.println("       2YY>>" + cPluginCustomization.element.toString());
//    }
//		System.out.println("       3YY>>" + ((BindInfo)annotation.getAnnotation()).getDocumentation().toString());
////		anno = (AnnotationImpl)classOutline.target.//.currentSchema.getAnnotation();
//    }
			//LOG.info("clean class: " + classOutline.implClass.fullName());
			classOutline.implClass.javadoc().clear();

//		FieldOutline[] declaredFields = classOutline.getDeclaredFields();
//			for (FieldOutline fieldOutline : declaredFields) {
//				if (fieldOutline instanceof SingleField) {
////					LOG.info("1clean class: " + fieldOutline.getRawType().fullName());
////					LOG.info("2clean class: " + fieldOutline.getPropertyInfo());
////					
//				}
//      }
//			classOutline.implRef.
			Iterable<JMethod> methods = classOutline.implClass.methods();
			for (JMethod jMethod : methods) {
	      jMethod.javadoc().clear();
      }
		}
	}

	//temporary solution, till i figure a way out, how to get the deprecated annotations from the schema
	private void markDeprecated(final ClassOutline classOutline, String string, String field) {
	  if (classOutline.implClass.fullName().equals(string)) {
	  	if (field == null) {
	  		classOutline.implClass.annotate(Deprecated.class);
	  		System.out.println("       1YY>>" + string);
	  	} else {
	  		JFieldVar jFieldVar = classOutline.implClass.fields().get(field);
	  		jFieldVar.annotate(Deprecated.class);
	  		System.out.println("       1YY>>" + string + "#" + field);
	  	}
	  }
  }
}
