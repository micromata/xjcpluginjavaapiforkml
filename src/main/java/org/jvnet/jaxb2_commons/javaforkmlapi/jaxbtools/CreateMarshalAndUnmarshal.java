package org.jvnet.jaxb2_commons.javaforkmlapi.jaxbtools;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.zip.ZipOutputStream;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.apache.log4j.Logger;
import org.jvnet.jaxb2_commons.javaforkmlapi.ClazzPool;
import org.jvnet.jaxb2_commons.javaforkmlapi.Util;
import org.jvnet.jaxb2_commons.javaforkmlapi.XJCJavaForKmlApiPlugin;
import org.jvnet.jaxb2_commons.javaforkmlapi.command.Command;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;

import com.sun.codemodel.ClassType;
import com.sun.codemodel.JBlock;
import com.sun.codemodel.JCatchBlock;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JClassContainer;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JConditional;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JDocComment;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JFieldVar;
import com.sun.codemodel.JInvocation;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JMod;
import com.sun.codemodel.JTryBlock;
import com.sun.codemodel.JType;
import com.sun.codemodel.JVar;
import com.sun.tools.xjc.Options;
import com.sun.tools.xjc.generator.bean.ClassOutlineImpl;
import com.sun.tools.xjc.model.nav.NClass;
import com.sun.tools.xjc.outline.ClassOutline;
import com.sun.tools.xjc.outline.FieldOutline;
import com.sun.tools.xjc.outline.Outline;
import com.sun.tools.xjc.util.CodeModelClassFactory;

public class CreateMarshalAndUnmarshal extends Command {
	private static final Logger LOG = Logger.getLogger(CreateMarshalAndUnmarshal.class.getName());

	private JCodeModel codeModel;

	private JType jaxbContextClass;
	private JType jaxbExceptionClass; 
	private JType jaxbMarshallerClass; 
	private JType kmlClass;
	private JType jaxbElementClass; 
	private JType saxExceptionClass; 
	private JType outputStreamClass;
	private JType fileOutputStreamClass; 
	private JType zipOutputStreamClass;
	private JType jaxbUnmarshallerClass;
	private JType streamSourceClass;
	private JType xmlConstantsClass;
	private JType schemaFactoryClass;
	private JType schemaClass;
	private JType fileClass;
	private JType stringReaderClass;
	private JType writerClass;
	
	private JMethod getJAXBContex;
	private JMethod createMashaller;
//	private JMethod createUnmarshaller;

	private JFieldVar jcVar;
	private JFieldVar mVar;
//	private JFieldVar uVar;
	private JFieldVar schemaLocationVar;


	

	

	public CreateMarshalAndUnmarshal(Outline outline, Options opts, ErrorHandler errorHandler, ClazzPool pool) {
		super(outline, opts, errorHandler, pool);
	}

	@Override
	public void execute() {
		codeModel = outline.getCodeModel();
		for (final ClassOutline classOutline : outline.getClasses()) {
			ClassOutlineImpl cc = (ClassOutlineImpl) classOutline;
			String currentClassName = Util.eliminateTypeSuffix(cc.implRef.name().toLowerCase());
			// LOG.info(">>>>>>>> " + currentClassName);
			if (currentClassName.equals("kml")) {
				LOG.info(XJCJavaForKmlApiPlugin.PLUGINNAME + " kml class found. creating marshall method");
				
				generateMarshallerAndUnMarshaller(cc);

			}
		}

	}

	private void generateMarshallerAndUnMarshaller(ClassOutlineImpl cc) {
		jaxbContextClass = codeModel.ref(JAXBContext.class);
		jaxbExceptionClass = codeModel.ref(JAXBException.class);
		saxExceptionClass = codeModel.ref(SAXException.class);
		jaxbMarshallerClass = codeModel.ref(Marshaller.class);
		jaxbUnmarshallerClass = codeModel.ref(Unmarshaller.class);
		kmlClass = cc.implRef;
		jaxbElementClass = codeModel.ref(JAXBElement.class).narrow(kmlClass.boxify());
		outputStreamClass = codeModel.ref(OutputStream.class);
		fileOutputStreamClass = codeModel._ref(FileOutputStream.class);
		zipOutputStreamClass = codeModel._ref(ZipOutputStream.class);
		streamSourceClass = codeModel._ref(StreamSource.class);
		fileClass = codeModel._ref(File.class);
		stringReaderClass = codeModel._ref(StringReader.class);
		writerClass = codeModel._ref(Writer.class);

		
		xmlConstantsClass = codeModel._ref(XMLConstants.class);
		schemaFactoryClass = codeModel._ref(SchemaFactory.class);
		schemaClass = codeModel._ref(Schema.class);
		
		
	
		
		generateHelperMethods(cc);
		generateMarshalMethods(cc);
		generateUnMarshalMethods(cc);
  }

	private void generateHelperMethods(ClassOutlineImpl cc) {
		// private static JAXBContext jc = null;
		jcVar = cc.implClass.field(JMod.PRIVATE | JMod.TRANSIENT, jaxbContextClass, "jc", JExpr._null());
		// private static Marshaller m = null;
		mVar = cc.implClass.field(JMod.PRIVATE | JMod.TRANSIENT, jaxbMarshallerClass, "m", JExpr._null());
		// private static Unmarshaller u = null;
//		uVar = cc.implClass.field(JMod.PRIVATE | JMod.TRANSIENT, jaxbUnmarshallerClass, "u", JExpr._null());
		
		
		//	private JAXBContext getJAXBContext() throws JAXBException {
		//	  if (jc == null) {
		//		  jc = JAXBContext.newInstance((Kml.class));
		//	  }
		//	  return jc;
		//  }
		getJAXBContex = cc.implClass.method(JMod.PRIVATE, jaxbContextClass, "getJaxbContext");
		getJAXBContex._throws(jaxbExceptionClass.boxify());
		final JConditional ifBlockOutstream = getJAXBContex.body()._if(jcVar.eq(JExpr._null()));
		ifBlockOutstream._then().assign(jcVar, jaxbContextClass.boxify().staticInvoke("newInstance").arg(JExpr.direct("Kml.class")));
		getJAXBContex.body()._return(jcVar);
		
		//	private Marshaller createMarshaller() throws JAXBException {
		//		if (m == null) {
		//			m = getJAXBContext().createMarshaller();
		//		}
		//		return m;
		//	}
		createMashaller = cc.implClass.method(JMod.PRIVATE, jaxbMarshallerClass, "createMarshaller");
		createMashaller._throws(jaxbExceptionClass.boxify());
		final JConditional ifBlockMarshaller = createMashaller.body()._if(mVar.eq(JExpr._null()));
		ifBlockMarshaller._then().assign(mVar, JExpr._this().invoke(getJAXBContex).invoke("createMarshaller")); 
		createMashaller.body()._return(mVar);
		
		
		//	private Unmarshaller createUnmarshaller() throws JAXBException {
		//		if (u == null) {
		//			u = getJAXBContext().createUnmarshaller();
		//		}
		//		return u;
		//	}
		//createUnmarshaller = cc.implClass.method(JMod.PRIVATE, jaxbUnmarshallerClass, "createUnmarshaller");
		//createUnmarshaller._throws(jaxbExceptionClass.boxify());
		//final JConditional ifBlockUnmarshaller = createUnmarshaller.body()._if(uVar.eq(JExpr._null()));
		//ifBlockUnmarshaller._then().assign(uVar, JExpr._this().invoke(getJAXBContex).invoke("createUnmarshaller")); 
		//createUnmarshaller.body()._return(uVar);
	}

	/**
	 * <pre>
	 * Java to KML
	 *      
	 * public boolean marshal(final String filename, boolean zipped) throws FileNotFoundException {
	 * 	OutputStream out = new FileOutputStream(filename);
	 * 	if (zipped) {
	 * 		out = new ZipOutputStream(out);
	 * 	}
	 * 
	 * 	return this.marshall(out);
	 * }
	 * 
	 * public boolean marshal(OutputStream outputStream)  {
	 * 	try {
	 * 		jc = this.createMarshaller();
	 * 		JAXBElement&lt;Kml&gt; jaxbRootElement = new JAXBElement&lt;Kml&gt;(new QName(&quot;http://www.opengis.net/kml/2.2&quot;, &quot;kml&quot;), Kml.class, this);
	 * 		m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
	 * 		m.marshal(jaxbRootElement, outputStream);
	 * 		return true;
	 * 	} catch (JAXBException _x) {
	 * 		_x.printStackTrace();
	 * 		return false;
	 * 	}
	 * }
	 * 
	 * 
	 * @param cc
	 */
	private void generateMarshalMethods(ClassOutlineImpl cc) {
		ArrayList<String> comment = new ArrayList<String>();
		comment.add("Java to KML\n");
		comment.add("The object graph is marshalled to an OutputStream object.\n");
		comment.add("The object is not saved as a zipped .kmz file (boolean is false).\n");
		comment.add("@see marshal(final File, final boolean)");
		final JMethod generateMarshalOutputStream = generateMarshal(cc, outputStreamClass, comment);
		comment.clear();
//		public boolean marshal(final Writer writer) {
//			try {
//				m = this.createMarshaller();
//				JAXBElement<Kml> jaxbRootElement = new JAXBElement<Kml>(new QName("http://www.opengis.net/kml/2.2", "kml"), Kml.class, this);
//				m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
//				m.marshal(jaxbRootElement, writer);
//				return true;
//			} catch (JAXBException _x) {
//				_x.printStackTrace();
//				return false;
//			}
//		}
		comment.add("Java to KML\n");
		comment.add("The object graph is marshalled to a Writer object.\n");
		comment.add("The object is not saved as a zipped .kmz file (boolean is false).\n");
		comment.add("@see marshal(final File, final boolean)");
		final JMethod generateMarshalWriter = generateMarshal(cc, writerClass, comment);
		comment.clear();
		
		//		public boolean marshal(final File filename, boolean zipped) throws FileNotFoundException {
		//			OutputStream out = new FileOutputStream(filename);
		//			if (zipped) {
		//				out = new ZipOutputStream(out);
		//			}
		//
		//			return this.marshall(out);
		//		}
		comment.add("Java to KML\n");
		comment.add("The object graph is marshalled to a File object.\n");
		comment.add("The boolean value indicates whether the File object is saved as a zipped .kmz file or not.");
		comment.add("\n<b>Warning:</b>\n");
		comment.add("<b>THE KMZ FEATURE, ISN'T WORKING YET!</b>\n");
		final JMethod generateMarshallFilenameWithZIP = cc.implClass.method(JMod.PUBLIC, cc.implClass.owner().BOOLEAN, "marshal");
		generateMarshallFilenameWithZIP._throws(FileNotFoundException.class);
		generateMarshallFilenameWithZIP.javadoc().append(comment);
		
		final JVar filenameVar = generateMarshallFilenameWithZIP.param(JMod.FINAL, File.class, "filename");
		final JVar zippedVar = generateMarshallFilenameWithZIP.param(JMod.FINAL, boolean.class, "zipped");
		JVar outVar = generateMarshallFilenameWithZIP.body().decl(outputStreamClass, "out",JExpr._new(fileOutputStreamClass).arg(filenameVar));
		final JConditional ifBlockFilename = generateMarshallFilenameWithZIP.body()._if(zippedVar.eq(JExpr.TRUE));
		ifBlockFilename._then().assign(outVar, JExpr._new(zipOutputStreamClass).arg(outVar));
		generateMarshallFilenameWithZIP.body()._return(JExpr._this().invoke(generateMarshalOutputStream).arg(outVar));
		comment.clear();
		
		
		
		comment.add("Java to KML\n");
		comment.add("The object graph is marshalled to a File object.\n");
		comment.add("The object is not saved as a zipped .kmz file (boolean is false).\n");
		comment.add("@see marshal(final File, final boolean)");
		final JMethod generateMarshallFilename = cc.implClass.method(JMod.PUBLIC, cc.implClass.owner().BOOLEAN, "marshal");
		generateMarshallFilename._throws(FileNotFoundException.class);
		generateMarshallFilename.javadoc().append(comment);
		JVar filename = generateMarshallFilename.param(JMod.FINAL, File.class, "filename");
		
		generateMarshallFilename.body()._return(JExpr._this().invoke(generateMarshallFilenameWithZIP).arg(filename).arg(JExpr.FALSE));
	}

	private JMethod generateMarshal(ClassOutlineImpl cc, JType argumentType, ArrayList<String> comment) {
	  // public boolean marshall(final String filename) throws FileNotFoundException {
		final JMethod generateMarshalOutputStream = cc.implClass.method(JMod.PUBLIC, cc.implClass.owner().BOOLEAN, "marshal");
		if (argumentType.equals(outputStreamClass)) {
		generateMarshalOutputStream._throws(FileNotFoundException.class);
		}
		generateMarshalOutputStream.javadoc().append(comment);
		final JVar value = generateMarshalOutputStream.param(JMod.FINAL, argumentType, argumentType.name().toLowerCase());

		// try {
		JTryBlock tryBlock = generateMarshalOutputStream.body()._try();
		// m = this.createMarshaller();
		tryBlock.body().assign(mVar, JExpr._this().invoke(createMashaller));

		// JAXBElement<Kml> jaxbRootElement = new JAXBElement<Kml>(new QName("http://www.opengis.net/kml/2.2", "kml"), Kml.class, this);
		JInvocation newJaxbElement = JExpr._new(jaxbElementClass).arg(createQName("http://www.opengis.net/kml/2.2", "kml")).arg(
		    kmlClass.boxify().dotclass()).arg(JExpr._this());
		final JVar jaxbRootElementVar = tryBlock.body().decl(jaxbElementClass, "jaxbRootElement", newJaxbElement);

		// m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
		tryBlock.body().add(mVar.invoke("setProperty").arg(jaxbMarshallerClass.boxify().staticRef("JAXB_FORMATTED_OUTPUT")).arg(JExpr.TRUE));

		// m.marshal(jaxbRootElement, outputStream);
		tryBlock.body().add(mVar.invoke("marshal").arg(jaxbRootElementVar).arg(value));

		// return true;
		tryBlock.body()._return(JExpr.TRUE);

		// } catch (JAXBException _x) {
		JBlock catchBlock = tryBlock._catch(jaxbExceptionClass.boxify()).body();
		catchBlock.directStatement("_x.printStackTrace();");
		catchBlock._return(JExpr.FALSE);
	  return generateMarshalOutputStream;
  }
	
	private void generateUnMarshalMethods(ClassOutlineImpl cc) {
		//		private static final String SCHEMA_LOCATION = "";
		schemaLocationVar = cc.implClass.field(JMod.PRIVATE | JMod.STATIC | JMod.FINAL, String.class, "SCHEMA_LOCATION", JExpr.lit("src/main/resources/schema/ogckml/ogckml22.xsd"));
		
		//		private boolean validate() {
		//			try {
		//				SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
		//				// sf.setErrorHandler(new SchemaErrorHandler());
		//				// u.setEventHandler(new ValidationEventListener());
		//				File schemaFile = new File(SCHEMA_LOCATION);
		//				javax.xml.validation.Schema schema = sf.newSchema(schemaFile);
		//				u.setSchema(schema);
		//				return true;
		//			} catch (SAXException e) {
		//				e.printStackTrace();
		//			}
		//			return false;
		//		}
		final JMethod generateValidate = cc.implClass.method(JMod.PRIVATE| JMod.STATIC, cc.implClass.owner().BOOLEAN, "validate");
		final JVar unmarshallValidateVar = generateValidate.param(JMod.FINAL, jaxbUnmarshallerClass, "unmarshaller");
	// try {
		JTryBlock tryValidateBlock = generateValidate.body()._try();
		JVar schemaFactoryVar = tryValidateBlock.body().decl(schemaFactoryClass, "sf", schemaFactoryClass.boxify().staticInvoke("newInstance").arg(xmlConstantsClass.boxify().staticRef("W3C_XML_SCHEMA_NS_URI")));
		JVar schemaFileVar = tryValidateBlock.body().decl(fileClass, "schemaFile", JExpr._new(fileClass).arg(schemaLocationVar));
		JVar schemaVar = tryValidateBlock.body().decl(schemaClass, "schema", schemaFactoryVar.invoke("newSchema").arg(schemaFileVar));
		tryValidateBlock.body().add(unmarshallValidateVar.invoke("setSchema").arg(schemaVar));
		tryValidateBlock.body()._return(JExpr.TRUE);
		// } catch (SAXException _x) {
		JBlock catchValidateBlock = tryValidateBlock._catch(saxExceptionClass.boxify()).body();
		catchValidateBlock.directStatement("_x.printStackTrace();");
		generateValidate.body()._return(JExpr.FALSE);
		
		
		//		public Kml unmarshal(File file, File schemaFile)  {
		//		try {
		//			u = jc.createUnmarshaller();
		//			if (schemaFile != null) {
		//			  Kml.validate(u);
		//			}
		//			StreamSource filesource = new StreamSource(file);
		//			JAXBElement<Kml> jaxbRootElement = u.unmarshal(filesource, Kml.class);
		//			return jaxbRootElement.getValue();
		//		} catch (JAXBException e) {
		//			e.printStackTrace();
		//		}
		//			return null;
		//		}			
		final JMethod generateUnMarshallerFileFile = cc.implClass.method(JMod.PUBLIC| JMod.STATIC, kmlClass, "unmarshal");
		generateUnMarshallerFileFile.javadoc().add("KML to Java\n");
		generateUnMarshallerFileFile.javadoc().add("KML given as a file object is transformed into a graph of Java objects.\n");
		generateUnMarshallerFileFile.javadoc().add("The boolean value indicates, whether the File object should be validated \n");
		generateUnMarshallerFileFile.javadoc().add("automatically during unmarshalling and be checked if the object graph meets \n");
		generateUnMarshallerFileFile.javadoc().add("all constraints defined in OGC's KML schema specification.");
		final JVar fileunmarshallVar = generateUnMarshallerFileFile.param(JMod.FINAL, fileClass, "file");
		final JVar validateVar = generateUnMarshallerFileFile.param(JMod.FINAL, boolean.class, "validate");
		
		// try {
		JTryBlock tryBlock = generateUnMarshallerFileFile.body()._try();
		// u = jc.createUnmarshaller();
//		tryBlock.body().assign(uVar, JExpr._this().invoke(createUnmarshaller));
		JVar localUnmarshallerFile = tryBlock.body().decl(jaxbUnmarshallerClass, "unmarshaller", jaxbContextClass.boxify().staticInvoke("newInstance").arg(JExpr.direct("Kml.class")).invoke("createUnmarshaller"));

		final JConditional ifBlockFilename = tryBlock.body()._if(validateVar.eq(JExpr.TRUE));
		ifBlockFilename._then().add(kmlClass.boxify().staticInvoke(generateValidate).arg(localUnmarshallerFile));
		
		//			StreamSource filesource = new StreamSource(file);
		JVar streamSourceVar = tryBlock.body().decl(streamSourceClass, "filesource",JExpr._new(streamSourceClass).arg(fileunmarshallVar));
		
		//			JAXBElement<Kml> jaxbRootElement = u.unmarshal(filesource, Kml.class);
//		JInvocation unmarshall = JExpr.invoke(uVar, "unmarshal").arg(streamSourceVar).arg(kmlClass.boxify().dotclass());
//		final JVar jaxbRootElementVar = tryBlock.body().decl(jaxbElementClass, "jaxbRootElement", unmarshall);

		final JVar  jaxbRootElementVar = tryBlock.body().decl(kmlClass, "jaxbRootElement", JExpr.cast(kmlClass, JExpr.invoke(localUnmarshallerFile, "unmarshal").arg(streamSourceVar)));
		//			return jaxbRootElement.getValue();
		tryBlock.body()._return(jaxbRootElementVar);

		
		
		//			return jaxbRootElement.getValue();
//		tryBlock.body()._return(jaxbRootElementVar.invoke("getValue"));
		
		// } catch (JAXBException _x) {
		JBlock catchBlock = tryBlock._catch(jaxbExceptionClass.boxify()).body();
		catchBlock.directStatement("_x.printStackTrace();");
		
		generateUnMarshallerFileFile.body()._return(JExpr._null());
		
		
		
		//		public static Kml unmarshal(File file) {
		//			return Kml.unmarshal(file, null);
		//		}
		final JMethod generateUnMarshallerFile = cc.implClass.method(JMod.PUBLIC| JMod.STATIC, kmlClass, "unmarshal");
		generateUnMarshallerFile.param(JMod.FINAL, fileClass, "file");
		generateUnMarshallerFile.javadoc().add("KML to Java\n");
		generateUnMarshallerFile.javadoc().add("KML given as a file object is transformed into a graph of Java objects.\n");
		generateUnMarshallerFile.javadoc().add("Similar to the method: \n");
		generateUnMarshallerFile.javadoc().add("unmarshal(final File, final boolean) \n");
		generateUnMarshallerFile.javadoc().add("but with the exception that the File object is not validated (boolean is false). \n");
		generateUnMarshallerFile.body()._return(kmlClass.boxify().staticInvoke(generateUnMarshallerFileFile).arg(fileunmarshallVar).arg(JExpr.FALSE));


		
		//		@SuppressWarnings("unchecked")
		//	  public static Kml unmarshal(final String content) {
		//			try {
		//				StringReader string = new StringReader(content);
		//				Unmarshaller u = JAXBContext.newInstance(Kml.class).createUnmarshaller();
		//				Kml jaxbRootElement = (Kml) unmarshaller.unmarshal(content);
		//				return jaxbRootElement;
		//			} catch (JAXBException _x) {
		//				_x.printStackTrace();
		//			}
		//			return null;
		//		}
		final JMethod generateUnMarshallerFromString = cc.implClass.method(JMod.PUBLIC | JMod.STATIC, kmlClass, "unmarshal");
//		generateUnMarshallerFromString.annotate(SuppressWarnings.class).param("value", "unchecked");
		generateUnMarshallerFromString.javadoc().add("KML to Java\n");
		generateUnMarshallerFromString.javadoc().add("Similar to the other unmarshal methods \n\n");
		generateUnMarshallerFromString.javadoc().add("but with the exception that it transforms a String into a graph of Java objects. \n");
		final JVar stringunmarshallVar = generateUnMarshallerFromString.param(JMod.FINAL, String.class, "content");
		
		// try {
		JTryBlock tryStringBlock = generateUnMarshallerFromString.body()._try();
		// StringReader string = new StringReader(content);
		JVar stringReaderVar = tryStringBlock.body().decl(stringReaderClass, "string",JExpr._new(stringReaderClass).arg(stringunmarshallVar));
		// Unmarshaller u = JAXBContext.newInstance(Kml.class).createUnmarshaller();
		// tryStringBlock.body().assign(jcVar, jaxbContextClass.boxify().staticInvoke("newInstance").arg(JExpr.direct("Kml.class")));
		JVar localUnmarshaller = tryStringBlock.body().decl(jaxbUnmarshallerClass, "unmarshaller", jaxbContextClass.boxify().staticInvoke("newInstance").arg(JExpr.direct("Kml.class")).invoke("createUnmarshaller"));
		//	JAXBElement<Kml> unmarshalledKml = (JAXBElement<Kml>) u.unmarshal(string);
		tryStringBlock.body().decl(kmlClass, "jaxbRootElement", JExpr.cast(kmlClass, JExpr.invoke(localUnmarshaller, "unmarshal").arg(stringReaderVar)));
		//			return jaxbRootElement.getValue();
		tryStringBlock.body()._return(jaxbRootElementVar);
		
		// } catch (JAXBException _x) {
		JBlock catchStringBlock = tryStringBlock._catch(jaxbExceptionClass.boxify()).body();
		catchStringBlock.directStatement("_x.printStackTrace();");
		
		generateUnMarshallerFromString.body()._return(JExpr._null());		
	}
	/**
	 * Generates an expression that evaluates to "new QName(...)"
	 */
	private JInvocation createQName(String packageUri, String name) {
		return JExpr._new(codeModel.ref(QName.class)).arg(packageUri).arg(name);
	}



}
