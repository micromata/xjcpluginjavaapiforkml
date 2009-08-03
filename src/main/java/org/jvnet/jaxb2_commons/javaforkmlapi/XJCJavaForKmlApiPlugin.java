package org.jvnet.jaxb2_commons.javaforkmlapi;

import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.jvnet.jaxb2_commons.javaforkmlapi.addproperjavadoc.XjcAddProperJavaDocumentationForKML;
import org.jvnet.jaxb2_commons.javaforkmlapi.command.Command;
import org.jvnet.jaxb2_commons.javaforkmlapi.convenience.CreateCoordinateClass;
import org.jvnet.jaxb2_commons.javaforkmlapi.convenience.CreateCreateAndAddMethodsForCoordinates;
import org.jvnet.jaxb2_commons.javaforkmlapi.convenience.CreateIconClass;
import org.jvnet.jaxb2_commons.javaforkmlapi.factory.CreateOwnObjectFactory;
import org.jvnet.jaxb2_commons.javaforkmlapi.fluent.CreateConstructors;
import org.jvnet.jaxb2_commons.javaforkmlapi.fluent.FluentPattern;
import org.jvnet.jaxb2_commons.javaforkmlapi.jaxbtools.CreateMarshalAndUnmarshal;
import org.jvnet.jaxb2_commons.javaforkmlapi.xmlrootelement.JaxbPluginXmlRootElement;
import org.xml.sax.ErrorHandler;

import com.sun.codemodel.JPackage;
import com.sun.tools.xjc.BadCommandLineException;
import com.sun.tools.xjc.Options;
import com.sun.tools.xjc.Plugin;
import com.sun.tools.xjc.model.CAttributePropertyInfo;
import com.sun.tools.xjc.model.CBuiltinLeafInfo;
import com.sun.tools.xjc.model.CClassInfo;
import com.sun.tools.xjc.model.CClassInfoParent;
import com.sun.tools.xjc.model.CCustomizations;
import com.sun.tools.xjc.model.Model;
import com.sun.tools.xjc.outline.Outline;
import com.sun.xml.xsom.XSSchema;

/**
 * {@link Plugin} that always removes the Type-Suffix (if present) from generated classes.
 * 
 * <pre>
 * 
 * Omit Type:
 *   PlacemarkType -&gt; Placemark
 * 
 * </pre>
 * 
 * @author Florian Bachmann
 */
public class XJCJavaForKmlApiPlugin extends Plugin {
	private static final String LOG4J_LOCATION = "src/main/java/log4j.properties";

	private final Logger LOG = Logger.getLogger(getClass().getName());

	/** Constant for the option string. */
	protected final String OPTION_NAME = "XJavaForKmlApi";

	public final static String PLUGINNAME = "[XJCJavaForKmlApiPlugin]";

	private List<Command> mCommand = new ArrayList<Command>();

	
	@Override
	public void postProcessModel(Model model, ErrorHandler errorHandler) {
		JPackage mainPackage = null;
		for (CClassInfo classInfo : model.beans().values()) {
			System.out.println(classInfo.fullName());
			if (classInfo.shortName.equals("Kml")) {
				mainPackage = classInfo.getOwnerPackage();
				break;
			}
		}
		assert mainPackage != null;

		CClassInfo classLink = null;
		for (CClassInfo classInfo : model.beans().values()) {
			if (classInfo.shortName.equals("Link") && classInfo.getOwnerPackage().name().equals(mainPackage.name())) {
				classLink = classInfo;
				System.out.println("Found Link class, prepare creation of Icon class");
				break;
			}
		}
		assert classLink != null;
		XSSchema schema = model.schemaComponent.getSchema("http://www.opengis.net/kml/2.2");
		
		CClassInfoParent cClassInfoParentLink = classLink.parent();
		QName qNameIcon = new QName("http://www.opengis.net/kml/2.2", "Icon");
		CClassInfo cc3 = new CClassInfo(model, cClassInfoParentLink, "Icon", null, qNameIcon, null, schema, new CCustomizations());
		cc3.setBaseClass(classLink);

		
		QName qNameCoordinate = new QName("http://www.opengis.net/kml/2.2", "Coordinate");
		QName qNameLongitude = new QName("PerlPleaseRemoveMeLongitude");
		QName qNameLatitude = new QName("PerlPleaseRemoveMeLatitude");
		QName qNameAltitude = new QName("PerlPleaseRemoveMeAltitude");
		CClassInfo cc4 = new CClassInfo(model, cClassInfoParentLink, "Coordinate", null, qNameCoordinate, null, schema, new CCustomizations());
		
//		test = new FlorisDouble(model,"double");
		CBuiltinLeafInfo doubleValue = CBuiltinLeafInfo.DOUBLE;
		
		System.out.println(":::::::::::::::::::::::::::::::::: " + doubleValue.getType().fullName());
		
		CAttributePropertyInfo cAttributeLongitude = new CAttributePropertyInfo("longitude", null, new CCustomizations(), null, qNameLongitude, doubleValue, null, false);
		CAttributePropertyInfo cAttributeLatitude = new CAttributePropertyInfo("latitude", null, null, null, qNameLatitude, doubleValue, null, false);
		CAttributePropertyInfo cAttributeAltitude = new CAttributePropertyInfo("altitude", null, new CCustomizations(), null, qNameAltitude, doubleValue, null, false);

		cc4.addProperty(cAttributeLongitude);
		cc4.addProperty(cAttributeLatitude);
		cc4.addProperty(cAttributeAltitude);
	}

	/**
	 * Returns the option string used to turn on this plugin.
	 * 
	 * @return option string to invoke this plugin
	 */
	@Override
	public String getOptionName() {
		return OPTION_NAME;
	}

	/**
	 * Returns a string specifying how to use this plugin and what it does.
	 * 
	 * @return string containing the plugin usage message
	 */
	@Override
	public String getUsage() {
		return "  -" + OPTION_NAME + " : removes every Type suffix from the generated classes";
	}

	/**
	 * On plugin activation, sets a customized NameConverter to adjust code generation.
	 * 
	 * @param opts options used to invoke XJC
	 * @throws com.sun.tools.xjc.BadCommandLineException if the plugin is invoked with wrong parameters
	 */
	@Override
	public void onActivated(Options opts) throws BadCommandLineException {

		PropertyConfigurator.configure(LOG4J_LOCATION);
		LOG
		    .info(XJCJavaForKmlApiPlugin.PLUGINNAME + " clean up generated names (Legend: T = removed Type, A = removed Abstract, E = removed Enum");
		opts.setNameConverter(new OmitTypeNameConverter(), this);
	}

	/**
	 * Returns true without touching the generated code. All the relevant work is done during name conversion.
	 * 
	 * @param model This object allows access to various generated code.
	 * @param opts options used to invoke XJC
	 * @param errorHandler Errors should be reported to this handler.
	 * @return If the add-on executes successfully, return true. If it detects some errors but those are reported and recovered gracefully,
	 *         return false.
	 */
	@Override
	public boolean run(Outline outline, Options opts, ErrorHandler errorHandler) {
		opts.noFileHeader = true;

		// annotations = new HashMap<String, JDefinedClass>();
		ClazzPool pool = new ClazzPool(outline);

		// Multimap<Salesperson, Sale> multimap = ArrayListMultimap.create();

		// mCommand.add(new CreateAnnotationClasses(outline, opts, errorHandler, this));
		mCommand.add(new CleanCode(outline, opts, errorHandler, pool));
		mCommand.add(new CreateIconClass(outline, opts, errorHandler, pool));
		mCommand.add(new CreateCoordinateClass(outline, opts, errorHandler, pool));
		mCommand.add(new JaxbPluginXmlRootElement(outline, opts, errorHandler, pool));
		mCommand.add(new ConvertComplexTypesToSimpleTypes(outline, opts, errorHandler, pool));
		// // mCommand.add(new JaxbPluginXmlRootElement(outline, opts, errorHandler));
		mCommand.add(new CreateEqualsAndHashCode(outline, opts, errorHandler, pool));
		mCommand.add(new CreateConstructors(outline, opts, errorHandler, pool));
		// // mCommand.add(new BuilderPattern(outline, opts, errorHandler));
		mCommand.add(new FluentPattern(outline, opts, errorHandler, pool));
		mCommand.add(new CreateOwnObjectFactory(outline, opts, errorHandler, pool));
		mCommand.add(new CreateMarshalAndUnmarshal(outline, opts, errorHandler, pool));
		mCommand.add(new CreateCreateAndAddMethodsForCoordinates(outline, opts, errorHandler, pool));
		mCommand.add(new DefaultValuePlugin(outline, opts, errorHandler, pool));
		mCommand.add(new XjcAddProperJavaDocumentationForKML(outline, opts, errorHandler, pool));

		for (Command command : mCommand) {
			command.execute();
		}

		return true;
	}

}
