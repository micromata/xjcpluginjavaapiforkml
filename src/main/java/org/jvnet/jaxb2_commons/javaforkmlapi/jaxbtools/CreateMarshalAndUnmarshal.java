package org.jvnet.jaxb2_commons.javaforkmlapi.jaxbtools;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.Writer;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
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
import com.sun.codemodel.JAnnotationUse;
import com.sun.codemodel.JBlock;
import com.sun.codemodel.JCatchBlock;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JClassAlreadyExistsException;
import com.sun.codemodel.JClassContainer;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JConditional;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JDocComment;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JFieldVar;
import com.sun.codemodel.JForEach;
import com.sun.codemodel.JInvocation;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JMod;
import com.sun.codemodel.JTryBlock;
import com.sun.codemodel.JType;
import com.sun.codemodel.JVar;
import com.sun.codemodel.JWhileLoop;
import com.sun.istack.NotNull;
import com.sun.tools.xjc.Options;
import com.sun.tools.xjc.generator.bean.ClassOutlineImpl;
import com.sun.tools.xjc.model.nav.NClass;
import com.sun.tools.xjc.outline.ClassOutline;
import com.sun.tools.xjc.outline.FieldOutline;
import com.sun.tools.xjc.outline.Outline;
import com.sun.tools.xjc.util.CodeModelClassFactory;
import com.sun.xml.bind.marshaller.NamespacePrefixMapper;



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

	// private JMethod createUnmarshaller;

	private JFieldVar jcVar;

	private JFieldVar mVar;

	// private JFieldVar uVar;
	private JFieldVar schemaLocationVar;

	private JType systemClass;

	private JType stringClass;

	private JClass inputStreamClass;

	private JFieldVar varMissingNameCounter;

	private JMethod createAddToKmzFile;

	private JType zipEntryClass;

	private JType UrlEncoderClass;

	private JType zipFileClass;

	private JType enumerationClass;

	private JType arrayListClass;

	private JType urlDecoderClass;

	public CreateMarshalAndUnmarshal(Outline outline, Options opts, ErrorHandler errorHandler, ClazzPool pool) {
		super(outline, opts, errorHandler, pool);
	}

	@Override
	public void execute() {
		codeModel = outline.getCodeModel();
		for (final ClassOutline classOutline : outline.getClasses()) {
			ClassOutlineImpl cc = (ClassOutlineImpl) classOutline;
			String currentClassName = Util.eliminateTypeSuffix(cc.implRef.name().toLowerCase());
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
		inputStreamClass = codeModel.ref(InputStream.class);
		fileOutputStreamClass = codeModel._ref(FileOutputStream.class);
		zipOutputStreamClass = codeModel._ref(ZipOutputStream.class);
		streamSourceClass = codeModel._ref(StreamSource.class);
		fileClass = codeModel._ref(File.class);
		stringReaderClass = codeModel._ref(StringReader.class);
		writerClass = codeModel._ref(Writer.class);

		xmlConstantsClass = codeModel._ref(XMLConstants.class);
		schemaFactoryClass = codeModel._ref(SchemaFactory.class);
		schemaClass = codeModel._ref(Schema.class);
		systemClass = codeModel._ref(System.class);
		stringClass = codeModel._ref(String.class);
		zipEntryClass = codeModel._ref(ZipEntry.class);
		zipFileClass = codeModel._ref(ZipFile.class);
		UrlEncoderClass = codeModel._ref(URLEncoder.class);
		urlDecoderClass = codeModel._ref(URLDecoder.class);
		enumerationClass = codeModel._ref(Enumeration.class);
		arrayListClass = codeModel._ref(ArrayList.class);

		generateHelperMethods(cc);
		generateMarshalMethods(cc);
		generateUnMarshalMethods(cc);
	}

	private void generateHelperMethods(ClassOutlineImpl cc) {
		// private static JAXBContext jc = null;
		jcVar = cc.implClass.field(JMod.PRIVATE | JMod.TRANSIENT, jaxbContextClass, "jc", JExpr._null());
		// private static Marshaller m = null;
		mVar = cc.implClass.field(JMod.PRIVATE | JMod.TRANSIENT, jaxbMarshallerClass, "m", JExpr._null());

		varMissingNameCounter = cc.implClass.field(JMod.PRIVATE | JMod.TRANSIENT, cc.implClass.owner().INT, "missingNameCounter", JExpr
		    .direct("1"));
		// private static Unmarshaller u = null;
		// uVar = cc.implClass.field(JMod.PRIVATE | JMod.TRANSIENT, jaxbUnmarshallerClass, "u", JExpr._null());

		// private JAXBContext getJAXBContext() throws JAXBException {
		// if (jc == null) {
		// jc = JAXBContext.newInstance((Kml.class));
		// }
		// return jc;
		// }
		getJAXBContex = cc.implClass.method(JMod.PRIVATE, jaxbContextClass, "getJaxbContext");
		getJAXBContex._throws(jaxbExceptionClass.boxify());
		final JConditional ifBlockOutstream = getJAXBContex.body()._if(jcVar.eq(JExpr._null()));
		ifBlockOutstream._then().assign(jcVar, jaxbContextClass.boxify().staticInvoke("newInstance").arg(JExpr.direct("Kml.class")));
		getJAXBContex.body()._return(jcVar);

		// private Marshaller createMarshaller() throws JAXBException {
		// if (m == null) {
		// m = getJAXBContext().createMarshaller();
		// }
		// return m;
		// }
		createMashaller = cc.implClass.method(JMod.PRIVATE, jaxbMarshallerClass, "createMarshaller");
		createMashaller._throws(jaxbExceptionClass.boxify());
		final JConditional ifBlockMarshaller = createMashaller.body()._if(mVar.eq(JExpr._null()));
		ifBlockMarshaller._then().assign(mVar, JExpr._this().invoke(getJAXBContex).invoke("createMarshaller"));
		ifBlockMarshaller._then().add(
		    mVar.invoke("setProperty").arg(jaxbMarshallerClass.boxify().staticRef("JAXB_FORMATTED_OUTPUT")).arg(JExpr.TRUE));
		// m.setProperty("com.sun.xml.bind.namespacePrefixMapper", new PreferredMapper());

		createMashaller.body()._return(mVar);

		try {
			JDefinedClass createNestedPrefixCustomizer = createNestedPrefixCustomizer(cc);
			ifBlockMarshaller._then().add(
			    mVar.invoke("setProperty").arg("com.sun.xml.bind.namespacePrefixMapper").arg(JExpr._new(createNestedPrefixCustomizer)));
		} catch (JClassAlreadyExistsException e) {
		}

		// private Unmarshaller createUnmarshaller() throws JAXBException {
		// if (u == null) {
		// u = getJAXBContext().createUnmarshaller();
		// }
		// return u;
		// }
		// createUnmarshaller = cc.implClass.method(JMod.PRIVATE, jaxbUnmarshallerClass, "createUnmarshaller");
		// createUnmarshaller._throws(jaxbExceptionClass.boxify());
		// final JConditional ifBlockUnmarshaller = createUnmarshaller.body()._if(uVar.eq(JExpr._null()));
		// ifBlockUnmarshaller._then().assign(uVar, JExpr._this().invoke(getJAXBContex).invoke("createUnmarshaller"));
		// createUnmarshaller.body()._return(uVar);

		createAddToKmzFile(cc);
	}

	private void createAddToKmzFile(ClassOutlineImpl cc) {
	  // private void addKmzFile(Kml kmzFile, ZipOutputStream out, boolean mainfile) throws IOException {
		// String fileName = null;
		// if (kmzFile.getFeature() == null || kmzFile.getFeature().getName() == null || kmzFile.getFeature().getName().length() == 0) {
		// fileName = "noFeatureNameSet" + (missingNameCounter++) + ".kml";
		// } else {
		// fileName = kmzFile.getFeature().getName();
		// if (!fileName.endsWith(".kml")) {
		// fileName += ".kml";
		// }
		// }
		// if (mainfile) {
		// fileName = "doc.kml";
		// }
		// out.putNextEntry(new ZipEntry(URLEncoder.encode(fileName, "UTF-8")));
		// kmzFile.marshal(out);
		//
		// out.closeEntry();
		// }
		//  	
		createAddToKmzFile = cc.implClass.method(JMod.PRIVATE, cc.implClass.owner().VOID, "addKmzFile");
		createAddToKmzFile._throws(IOException.class);
		JVar varKmzFile = createAddToKmzFile.param(kmlClass, "kmzFile");
		JVar varOut = createAddToKmzFile.param(zipOutputStreamClass, "out");
		JVar varMainFile = createAddToKmzFile.param(cc.implClass.owner().BOOLEAN, "mainfile");
		createAddToKmzFile.javadoc().add("Internal method");
		JVar varFileName = createAddToKmzFile.body().decl(stringClass, "fileName", JExpr._null());
		JConditional if1 = createAddToKmzFile.body()._if(
		    varKmzFile.invoke("getFeature").eq(JExpr._null()).cor(varKmzFile.invoke("getFeature").invoke("getName").eq(JExpr._null())).cor(
		        varKmzFile.invoke("getFeature").invoke("getName").invoke("length").eq(JExpr.lit(0))));
		if1._then().assign(varFileName, JExpr.lit("noFeatureNameSet").plus(varMissingNameCounter.incr()).plus(JExpr.lit(".kml")));
		if1._else().assign(varFileName, varKmzFile.invoke("getFeature").invoke("getName"));
		if1._else()._if(varFileName.invoke("endsWith").arg(".kml").not())._then().assignPlus(varFileName, JExpr.lit(".kml"));

		createAddToKmzFile.body()._if(varMainFile)._then().assign(varFileName, JExpr.lit("doc.kml"));
		createAddToKmzFile.body().add(
		    varOut.invoke("putNextEntry").arg(
		        JExpr._new(zipEntryClass).arg(UrlEncoderClass.boxify().staticInvoke("encode").arg(varFileName).arg(JExpr.lit("UTF-8")))));
		createAddToKmzFile.body().add(varKmzFile.invoke("marshal").arg(varOut));
		createAddToKmzFile.body().add(varOut.invoke("closeEntry"));
  }

	private JDefinedClass createNestedPrefixCustomizer(ClassOutlineImpl cc) throws JClassAlreadyExistsException {
		JDefinedClass namespacebeautyfier = cc.implClass._class(JMod.PRIVATE | JMod.FINAL, "NameSpaceBeautyfier");
		namespacebeautyfier._extends(NamespacePrefixMapper.class);

		JMethod namespacebeautyfiergetpreferredprefix = namespacebeautyfier.method(JMod.PUBLIC, stringClass, "getPreferredPrefix");
		namespacebeautyfiergetpreferredprefix.annotate(Override.class);
		namespacebeautyfiergetpreferredprefix.javadoc().append("Internal method!\n");
		namespacebeautyfiergetpreferredprefix.javadoc().append(
		    "<p>Customizing Namespace Prefixes During Marshalling to a more readable format.</p>\n");
		namespacebeautyfiergetpreferredprefix.javadoc().append("<p>The default output is like:</p>\n");
		namespacebeautyfiergetpreferredprefix
		    .javadoc()
		    .append(
		        "<pre>{@code&lt;kml ... xmlns:ns2=\"http://www.w3.org/2005/Atom\" xmlns:ns3=\"urn:oasis:names:tc:ciq:xsdschema:xAL:2.0\" xmlns:ns4=\"http://www.google.com/kml/ext/2.2\"&gt;}</pre>\n");
		namespacebeautyfiergetpreferredprefix.javadoc().append("<p>is changed to:</p>\n");
		namespacebeautyfiergetpreferredprefix
		    .javadoc()
		    .append(
		        "<pre>{@code &lt;kml ... xmlns:atom=\"http://www.w3.org/2005/Atom\" xmlns:xal=\"urn:oasis:names:tc:ciq:xsdschema:xAL:2.0\" xmlns:gx=\"http://www.google.com/kml/ext/2.2\"&gt;}</pre>");
		namespacebeautyfiergetpreferredprefix.javadoc().append("<p>What it does:</p>\n");
		namespacebeautyfiergetpreferredprefix.javadoc().append("<p>namespaceUri: http://www.w3.org/2005/Atom              prefix: atom</p>");
		namespacebeautyfiergetpreferredprefix.javadoc().append("<p>namespaceUri: urn:oasis:names:tc:ciq:xsdschema:xAL:2.0 prefix: xal</p>");
		namespacebeautyfiergetpreferredprefix.javadoc().append("<p>namespaceUri: http://www.google.com/kml/ext/2.2        prefix: gx</p>");

		namespacebeautyfiergetpreferredprefix.javadoc().append("<p>namespaceUri: anything else prefix: null</p>");
		namespacebeautyfiergetpreferredprefix.javadoc().trimToSize();
		JVar namespaceuri = namespacebeautyfiergetpreferredprefix.param(stringClass, "namespaceUri");
		namespacebeautyfiergetpreferredprefix.param(stringClass, "suggestion");
		namespacebeautyfiergetpreferredprefix.param(cc.implClass.owner().BOOLEAN, "requirePrefix");

		namespacebeautyfiergetpreferredprefix.body()._if(namespaceuri.invoke("matches").arg("http://www.w3.org/\\d{4}/Atom"))._then()._return(
		    JExpr.lit("atom"));
		namespacebeautyfiergetpreferredprefix.body()._if(namespaceuri.invoke("matches").arg("urn:oasis:names:tc:ciq:xsdschema:xAL:.*?"))
		    ._then()._return(JExpr.lit("xal"));
		namespacebeautyfiergetpreferredprefix.body()._if(namespaceuri.invoke("matches").arg("http://www.google.com/kml/ext/.*?"))._then()
    ._return(JExpr.lit("gx"));
		//namespacebeautyfiergetpreferredprefix.body()._if(namespaceuri.invoke("matches").arg("http://www.opengis.net/kml/.*?"))._then()
    //._return(JExpr.lit("kml"));
		namespacebeautyfiergetpreferredprefix.body()._return(JExpr._null());
		return namespacebeautyfier;
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
		comment.add("The object is not saved as a zipped .kmz file.\n");
		comment.add("@see marshalKmz(String, Kml...)");
		final JMethod generateMarshalOutputStream = generateMarshal(cc, outputStreamClass, comment);
		comment.clear();
		// public boolean marshal(final Writer writer) {
		// try {
		// m = this.createMarshaller();
		// JAXBElement<Kml> jaxbRootElement = new JAXBElement<Kml>(new QName("http://www.opengis.net/kml/2.2", "kml"), Kml.class, this);
		// m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		// m.marshal(jaxbRootElement, writer);
		// return true;
		// } catch (JAXBException _x) {
		// _x.printStackTrace();
		// return false;
		// }
		// }
		comment.add("Java to KML\n");
		comment.add("The object graph is marshalled to a Writer object.\n");
		comment.add("The object is not saved as a zipped .kmz file.\n");
		comment.add("@see marshalKmz(String, Kml...)");
		generateMarshal(cc, writerClass, comment);
		comment.clear();

		comment.add("Java to KML\n");
		comment.add("The object graph is printed to the console.\n");
		comment.add("(Nothing is saved, nor saved. Just printed.)\n");
		generateMarshal(cc, comment);
		comment.clear();

		// public boolean marshal(final File filename, boolean zipped) throws FileNotFoundException {
		// OutputStream out = new FileOutputStream(filename);
		// if (zipped) {
		// out = new ZipOutputStream(out);
		// }
		//
		// return this.marshall(out);
		// }
		comment.add("Java to KML\n");
		comment.add("The object graph is marshalled to a File object.\n");
		comment.add("The object is not saved as a zipped .kmz file.\n");
		comment.add("@see marshalKmz(String, Kml...)");
		final JMethod generateMarshallFilenameWithZIP = cc.implClass.method(JMod.PUBLIC, cc.implClass.owner().BOOLEAN, "marshal");
		generateMarshallFilenameWithZIP._throws(FileNotFoundException.class);
		generateMarshallFilenameWithZIP.javadoc().append(comment);

		final JVar filenameVar = generateMarshallFilenameWithZIP.param(JMod.FINAL, File.class, "filename");
		// final JVar zippedVar = generateMarshallFilenameWithZIP.param(JMod.FINAL, boolean.class, "zipped");
		JVar outVar = generateMarshallFilenameWithZIP.body().decl(outputStreamClass, "out", JExpr._new(fileOutputStreamClass).arg(filenameVar));
		// final JConditional ifBlockFilename = generateMarshallFilenameWithZIP.body()._if(zippedVar.eq(JExpr.TRUE));
		// ifBlockFilename._then().assign(outVar, JExpr._new(zipOutputStreamClass).arg(outVar));
		generateMarshallFilenameWithZIP.body()._return(JExpr._this().invoke(generateMarshalOutputStream).arg(outVar));
		comment.clear();

//		comment.add("Java to KML\n");
//		comment.add("The object graph is marshalled to a File object.\n");
//		comment.add("The object is not saved as a zipped .kmz file.\n");
//		comment.add("@see marshalKmz(String, Kml...)");
//		final JMethod generateMarshallFilename = cc.implClass.method(JMod.PUBLIC, cc.implClass.owner().BOOLEAN, "marshal");
//		generateMarshallFilename._throws(FileNotFoundException.class);
//		generateMarshallFilename.javadoc().append(comment);
//		JVar filename = generateMarshallFilename.param(JMod.FINAL, File.class, "filename");
//
//		generateMarshallFilename.body()._return(JExpr._this().invoke(generateMarshallFilenameWithZIP).arg(filename).arg(JExpr.FALSE));

		generateMarshalKmz(cc);

	}



	private JMethod generateMarshal(ClassOutlineImpl cc, ArrayList<String> comment) {
		// public boolean marshall(final String filename) throws FileNotFoundException {
		final JMethod generateMarshalOutputStream = cc.implClass.method(JMod.PUBLIC, cc.implClass.owner().BOOLEAN, "marshal");
		generateMarshalOutputStream.javadoc().append(comment);

		// try {
		JTryBlock tryBlock = generateMarshalOutputStream.body()._try();
		// m = this.createMarshaller();
		tryBlock.body().assign(mVar, JExpr._this().invoke(createMashaller));

		// JAXBElement<Kml> jaxbRootElement = new JAXBElement<Kml>(new QName("http://www.opengis.net/kml/2.2", "kml"), Kml.class, this);
//		JInvocation newJaxbElement = JExpr._new(jaxbElementClass).arg(createQName("http://www.opengis.net/kml/2.2", "kml")).arg(
//		    kmlClass.boxify().dotclass()).arg(JExpr._this());
//		final JVar jaxbRootElementVar = tryBlock.body().decl(jaxbElementClass, "jaxbRootElement", newJaxbElement);

		// m.marshal(jaxbRootElement, outputStream);
		tryBlock.body().add(mVar.invoke("marshal").arg(JExpr._this()).arg(systemClass.boxify().staticRef("out")));

		// return true;
		tryBlock.body()._return(JExpr.TRUE);

		// } catch (JAXBException _x) {
		JBlock catchBlock = tryBlock._catch(jaxbExceptionClass.boxify()).body();
		catchBlock.directStatement("_x.printStackTrace();");
		catchBlock._return(JExpr.FALSE);
		return generateMarshalOutputStream;
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
//		JInvocation newJaxbElement = JExpr._new(jaxbElementClass).arg(createQName("http://www.opengis.net/kml/2.2", "kml")).arg(
//		    kmlClass.boxify().dotclass()).arg(JExpr._this());
//		final JVar jaxbRootElementVar = tryBlock.body().decl(jaxbElementClass, "jaxbRootElement", newJaxbElement);

		// m.marshal(jaxbRootElement, outputStream);
		tryBlock.body().add(mVar.invoke("marshal").arg(JExpr._this()).arg(value));

		// return true;
		tryBlock.body()._return(JExpr.TRUE);

		// } catch (JAXBException _x) {
		JBlock catchBlock = tryBlock._catch(jaxbExceptionClass.boxify()).body();
		catchBlock.directStatement("_x.printStackTrace();");
		catchBlock._return(JExpr.FALSE);
		return generateMarshalOutputStream;
	}

	private void generateUnMarshalMethods(ClassOutlineImpl cc) {
		schemaLocationVar = cc.implClass.field(JMod.PRIVATE | JMod.STATIC | JMod.FINAL, stringClass, "SCHEMA_LOCATION", JExpr
		    .lit("src/main/resources/schema/ogckml/ogckml22.xsd"));

		final JMethod generateValidate = generateValidateMethod(cc);

		generateUnmarshalFileWithOptionalValidate(cc, generateValidate);

		generateUnmarshalMethodFromString(cc, stringReaderClass);

		generateUnmarshalMethodPlain(cc, inputStreamClass);
		
		generateUnmarshalFromKmzMethod(cc);
	}

	/**
	 * <pre><code>
	public static Kml[] unmarshalKMZ(File file) throws IOException {
		final Kml[] EMPTY_KML_ARRAY = new Kml[0];

		if (!file.getName().endsWith(".kmz")) {
			return EMPTY_KML_ARRAY;
		}
		

		ZipFile zip = new ZipFile(file);
		Enumeration< ? extends ZipEntry> entries = zip.entries();
		if (!file.exists()) {
			return EMPTY_KML_ARRAY;
		}
		ArrayList<Kml> kmlfiles = new ArrayList<Kml>();
		while (entries.hasMoreElements()) {
			ZipEntry entry = (ZipEntry) entries.nextElement();

			// is directory
			if (entry.getName().contains("__MACOSX") || entry.getName().contains(".DS_STORE")) {
				continue;
			}

			String entryName = URLDecoder.decode(entry.getName(), "UTF-8");

			System.out.println("element: " + entryName);
			if (!entry.getName().endsWith(".kml")) {
				continue;
			}
			InputStream in = zip.getInputStream(entry);
			Kml unmarshal = Kml.unmarshal(in);
			kmlfiles.add(unmarshal);
		}
		zip.close();

		return kmlfiles.toArray(EMPTY_KML_ARRAY);
	}
	</code></pre>
  */
	private void generateUnmarshalFromKmzMethod(ClassOutlineImpl cc) {
		final JMethod unmarshalFromKMZ = cc.implClass.method(JMod.PUBLIC | JMod.STATIC, kmlClass.array(), "unmarshalFromKmz");
		unmarshalFromKMZ.javadoc().add("KMZ to Java\n");
		unmarshalFromKMZ.javadoc().add("Similar to the other unmarshal methods\n\n");
		unmarshalFromKMZ.javadoc().add("with the exception that it transforms a KMZ-file into a graph of Java objects. \n");	
		unmarshalFromKMZ._throws(IOException.class);
		JVar varFile = unmarshalFromKMZ.param(fileClass, "file");
		varFile.annotate(NotNull.class);
		JVar varEMPTY_KML_ARRAY = unmarshalFromKMZ.body().decl(kmlClass.array(), "EMPTY_KML_ARRAY", JExpr.direct("new Kml[0]"));// JExpr._new(kmlClass.array().)..arg("0"));
		unmarshalFromKMZ.body()._if(varFile.invoke("getName").invoke("endsWith").arg(".kmz").not())._then()._return(varEMPTY_KML_ARRAY);
		JVar varZip = unmarshalFromKMZ.body().decl(zipFileClass, "zip",JExpr._new(zipFileClass).arg(varFile) );
		JVar varEntries = unmarshalFromKMZ.body().decl(enumerationClass.boxify().narrow(zipEntryClass.boxify().wildcard()), "entries", varZip.invoke("entries"));
		unmarshalFromKMZ.body()._if(varFile.invoke("exists").not())._then()._return(varEMPTY_KML_ARRAY);
		JVar varKmlFiles = unmarshalFromKMZ.body().decl(arrayListClass.boxify().narrow(kmlClass.boxify()), "kmlfiles", JExpr._new(arrayListClass.boxify().narrow(kmlClass.boxify())));
		JBlock while1 = unmarshalFromKMZ.body()._while(varEntries.invoke("hasMoreElements")).body();
		JVar varEntry = while1.decl(zipEntryClass, "entry",JExpr.cast(zipEntryClass, varEntries.invoke("nextElement")));
		while1._if(varEntry.invoke("getName").invoke("contains").arg("__MACOSX").cor(varEntry.invoke("getName").invoke("contains").arg(".DS_STORE")))._then()._continue();
		JVar entryName = while1.decl(stringClass, "entryName", urlDecoderClass.boxify().staticInvoke("decode").arg(varEntry.invoke("getName")).arg("UTF-8"));
		while1._if(entryName.invoke("endsWith").arg("*.kml"))._then()._continue();
		JVar varIn = while1.decl(inputStreamClass, "in", varZip.invoke("getInputStream").arg(varEntry));
		JVar varUnmarshal = while1.decl(kmlClass, "unmarshal", kmlClass.boxify().staticInvoke("unmarshal").arg(varIn));
		while1.add(varKmlFiles.invoke("add").arg(varUnmarshal));
		unmarshalFromKMZ.body().add(varZip.invoke("close"));
		unmarshalFromKMZ.body()._return(varKmlFiles.invoke("toArray").arg(varEMPTY_KML_ARRAY));
		
//		JVar varOut = unmarshalFromKMZ.body().decl(zipEntryClass, "out", JExpr._new(zipOutputStreamClass).arg(varName));
		
  }
	
	private void generateMarshalKmz(ClassOutlineImpl cc) {
		// public boolean marshalKmz(@NotNull String name, Kml... additionalFiles) throws IOException {
		// ZipOutputStream out = new ZipOutputStream(new FileOutputStream(name));
		// out.setComment("KMZ-file created with Java API for KML.");
		// addKmzFile(this, out, true);
		//
		// for (Kml kml : additionalFiles) {
		// addKmzFile(kml, out, false);
		// }
		//
		// out.close();
		// missingNameCounter = 1;
		// return false;
		// }
		final JMethod generateMarshalKmz = cc.implClass.method(JMod.PUBLIC, cc.implClass.owner().BOOLEAN, "marshalAsKmz");
		JVar varName = generateMarshalKmz.param(stringClass, "name");
		varName.annotate(NotNull.class);
		JVar varAdditionalFiles = generateMarshalKmz.varParam(kmlClass.boxify(), "additionalFiles");
		generateMarshalKmz._throws(IOException.class);
		JVar varOut = generateMarshalKmz.body().decl(zipOutputStreamClass, "out", JExpr._new(zipOutputStreamClass).arg(JExpr._new(fileOutputStreamClass).arg(varName)));
		generateMarshalKmz.body().add(varOut.invoke("setComment").arg("KMZ-file created with Java API for KML. Visit us: http://code.google.com/p/javaapiforkml/"));
		generateMarshalKmz.body().add(JExpr._this().invoke(createAddToKmzFile).arg(JExpr._this()).arg(varOut).arg(JExpr.TRUE));
		JForEach forEach = generateMarshalKmz.body().forEach(kmlClass, "kml", varAdditionalFiles);
		forEach.body().add(JExpr._this().invoke(createAddToKmzFile).arg(forEach.var()).arg(varOut).arg(JExpr.FALSE));
		generateMarshalKmz.body().add(varOut.invoke("close"));
		generateMarshalKmz.body().assign(varMissingNameCounter,JExpr.lit(1));
		generateMarshalKmz.body()._return(JExpr.FALSE);
	}

	private void generateUnmarshalMethodPlain(ClassOutlineImpl cc, JClass invokeMarshalWith) {
		final JMethod generateUnMarshallerFromString = cc.implClass.method(JMod.PUBLIC | JMod.STATIC, kmlClass, "unmarshal");
		generateUnMarshallerFromString.javadoc().add("KML to Java\n");
		generateUnMarshallerFromString.javadoc().add("Similar to the other unmarshal methods \n\n");
		generateUnMarshallerFromString.javadoc().add(
		    "with the exception that it transforms a " + invokeMarshalWith.name() + " into a graph of Java objects. \n");
		generateUnMarshallerFromString.javadoc().trimToSize();
		final JVar stringunmarshallVar = generateUnMarshallerFromString.param(JMod.FINAL, invokeMarshalWith, "content");

		JTryBlock tryStringBlock = generateUnMarshallerFromString.body()._try();
		JVar localUnmarshaller = tryStringBlock.body().decl(jaxbUnmarshallerClass, "unmarshaller",
		    jaxbContextClass.boxify().staticInvoke("newInstance").arg(JExpr.direct("Kml.class")).invoke("createUnmarshaller"));
		JVar decl = tryStringBlock.body().decl(kmlClass, "jaxbRootElement",
		    JExpr.cast(kmlClass, JExpr.invoke(localUnmarshaller, "unmarshal").arg(stringunmarshallVar)));
		tryStringBlock.body()._return(decl);

		JBlock catchStringBlock = tryStringBlock._catch(jaxbExceptionClass.boxify()).body();
		catchStringBlock.directStatement("_x.printStackTrace();");

		generateUnMarshallerFromString.body()._return(JExpr._null());
	}

	private void generateUnmarshalMethodFromString(ClassOutlineImpl cc, JType invokeMarshalWith) {
		final JMethod generateUnMarshallerFromString = cc.implClass.method(JMod.PUBLIC | JMod.STATIC, kmlClass, "unmarshal");
		generateUnMarshallerFromString.javadoc().add("KML to Java\n");
		generateUnMarshallerFromString.javadoc().add("Similar to the other unmarshal methods \n\n");
		generateUnMarshallerFromString.javadoc().add("with the exception that it transforms a String into a graph of Java objects. \n");
		generateUnMarshallerFromString.javadoc().trimToSize();
		final JVar stringunmarshallVar = generateUnMarshallerFromString.param(JMod.FINAL, stringClass, "content");

		JTryBlock tryStringBlock = generateUnMarshallerFromString.body()._try();
		JVar localUnmarshaller = tryStringBlock.body().decl(jaxbUnmarshallerClass, "unmarshaller",
		    jaxbContextClass.boxify().staticInvoke("newInstance").arg(JExpr.direct("Kml.class")).invoke("createUnmarshaller"));
		JVar decl = tryStringBlock.body().decl(kmlClass, "jaxbRootElement",
		    JExpr.cast(kmlClass, JExpr.invoke(localUnmarshaller, "unmarshal").arg(JExpr._new(invokeMarshalWith).arg(stringunmarshallVar))));
		tryStringBlock.body()._return(decl);

		JBlock catchStringBlock = tryStringBlock._catch(jaxbExceptionClass.boxify()).body();
		catchStringBlock.directStatement("_x.printStackTrace();");

		generateUnMarshallerFromString.body()._return(JExpr._null());
	}

	private void generateUnmarshalFileWithOptionalValidate(ClassOutlineImpl cc, final JMethod generateValidate) {
		final JMethod generateUnMarshallerFileFile = cc.implClass.method(JMod.PUBLIC | JMod.STATIC, kmlClass, "unmarshal");
		generateUnMarshallerFileFile.javadoc().add("KML to Java\n");
		generateUnMarshallerFileFile.javadoc().add("KML given as a file object is transformed into a graph of Java objects.\n");
		generateUnMarshallerFileFile.javadoc().add("The boolean value indicates, whether the File object should be validated \n");
		generateUnMarshallerFileFile.javadoc().add("automatically during unmarshalling and be checked if the object graph meets \n");
		generateUnMarshallerFileFile.javadoc().add("all constraints defined in OGC's KML schema specification.");
		generateUnMarshallerFileFile.javadoc().trimToSize();
		final JVar fileunmarshallVar = generateUnMarshallerFileFile.param(JMod.FINAL, fileClass, "file");
		final JVar validateVar = generateUnMarshallerFileFile.param(JMod.FINAL, boolean.class, "validate");

		JTryBlock tryBlock = generateUnMarshallerFileFile.body()._try();
		JVar localUnmarshallerFile = tryBlock.body().decl(jaxbUnmarshallerClass, "unmarshaller",
		    jaxbContextClass.boxify().staticInvoke("newInstance").arg(JExpr.direct("Kml.class")).invoke("createUnmarshaller"));

		final JConditional ifBlockFilename = tryBlock.body()._if(validateVar.eq(JExpr.TRUE));
		ifBlockFilename._then().add(kmlClass.boxify().staticInvoke(generateValidate).arg(localUnmarshallerFile));
		final JVar jaxbRootElementVar = tryBlock.body().decl(kmlClass, "jaxbRootElement",
		    JExpr.cast(kmlClass, JExpr.invoke(localUnmarshallerFile, "unmarshal").arg(JExpr._new(streamSourceClass).arg(fileunmarshallVar))));
		tryBlock.body()._return(jaxbRootElementVar);
		JBlock catchBlock = tryBlock._catch(jaxbExceptionClass.boxify()).body();
		catchBlock.directStatement("_x.printStackTrace();");
		generateUnMarshallerFileFile.body()._return(JExpr._null());

		final JMethod generateUnMarshallerFile = cc.implClass.method(JMod.PUBLIC | JMod.STATIC, kmlClass, "unmarshal");
		generateUnMarshallerFile.param(JMod.FINAL, fileClass, "file");
		generateUnMarshallerFile.javadoc().add("KML to Java\n");
		generateUnMarshallerFile.javadoc().add("KML given as a file object is transformed into a graph of Java objects.\n");
		generateUnMarshallerFile.javadoc().add("Similar to the method: \n");
		generateUnMarshallerFile.javadoc().add("unmarshal(final File, final boolean) \n");
		generateUnMarshallerFile.javadoc().add("with the exception that the File object is not validated (boolean is false). ");
		generateUnMarshallerFile.javadoc().trimToSize();
		generateUnMarshallerFile.body()._return(
		    kmlClass.boxify().staticInvoke(generateUnMarshallerFileFile).arg(fileunmarshallVar).arg(JExpr.FALSE));
	}

	private JMethod generateValidateMethod(ClassOutlineImpl cc) {
		final JMethod generateValidate = cc.implClass.method(JMod.PRIVATE | JMod.STATIC, cc.implClass.owner().BOOLEAN, "validate");
		final JVar unmarshallValidateVar = generateValidate.param(JMod.FINAL, jaxbUnmarshallerClass, "unmarshaller");
		JTryBlock tryValidateBlock = generateValidate.body()._try();
		JVar schemaFactoryVar = tryValidateBlock.body().decl(schemaFactoryClass, "sf",
		    schemaFactoryClass.boxify().staticInvoke("newInstance").arg(xmlConstantsClass.boxify().staticRef("W3C_XML_SCHEMA_NS_URI")));
		JVar schemaFileVar = tryValidateBlock.body().decl(fileClass, "schemaFile", JExpr._new(fileClass).arg(schemaLocationVar));
		JVar schemaVar = tryValidateBlock.body().decl(schemaClass, "schema", schemaFactoryVar.invoke("newSchema").arg(schemaFileVar));
		tryValidateBlock.body().add(unmarshallValidateVar.invoke("setSchema").arg(schemaVar));
		tryValidateBlock.body()._return(JExpr.TRUE);
		JBlock catchValidateBlock = tryValidateBlock._catch(saxExceptionClass.boxify()).body();
		catchValidateBlock.directStatement("_x.printStackTrace();");
		generateValidate.body()._return(JExpr.FALSE);
		return generateValidate;
	}

	/**
	 * Generates an expression that evaluates to "new QName(...)"
	 */
	private JInvocation createQName(String packageUri, String name) {
		return JExpr._new(codeModel.ref(QName.class)).arg(packageUri).arg(name);
	}
}
