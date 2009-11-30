package org.jvnet.jaxb2_commons.javaforkmlapi.marshal_unmarshal;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
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
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.apache.log4j.Logger;
import org.jvnet.jaxb2_commons.javaforkmlapi.ClazzPool;
import org.jvnet.jaxb2_commons.javaforkmlapi.Util;
import org.jvnet.jaxb2_commons.javaforkmlapi.XJCJavaForKmlApiPlugin;
import org.jvnet.jaxb2_commons.javaforkmlapi.command.Command;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.DTDHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.XMLReader;

import com.sun.codemodel.ClassType;
import com.sun.codemodel.JBlock;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JClassAlreadyExistsException;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JConditional;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JFieldVar;
import com.sun.codemodel.JForEach;
import com.sun.codemodel.JInvocation;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JMod;
import com.sun.codemodel.JPackage;
import com.sun.codemodel.JTryBlock;
import com.sun.codemodel.JType;
import com.sun.codemodel.JVar;
import com.sun.istack.NotNull;
import com.sun.tools.xjc.Options;
import com.sun.tools.xjc.generator.bean.ClassOutlineImpl;
import com.sun.tools.xjc.outline.ClassOutline;
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

	private JType contentHandlerClass;

	private JType parserConfigurationExceptionClass;

	private JType saxsourceClass;

	private JType fileReaderClass;

	private JType inputsourceClass;

	private JType xmlReaderClass;

	private JDefinedClass namespaceFilterHandler;
	private JDefinedClass namespaceFilterXMLReaderclass;

	private JType saxParserFactoryClass;

	private JType fileNotFoundExceptionClass;

	public CreateMarshalAndUnmarshal(final Outline outline, final Options opts, final ErrorHandler errorHandler, final ClazzPool pool) {
		super(outline, opts, errorHandler, pool);
	}

	@Override
	public void execute() {
		codeModel = outline.getCodeModel();
		for (final ClassOutline classOutline : outline.getClasses()) {
			final ClassOutlineImpl cc = (ClassOutlineImpl) classOutline;
			final String currentClassName = Util.eliminateTypeSuffix(cc.implRef.name().toLowerCase());
			if (currentClassName.equals("kml")) {
				LOG.info(XJCJavaForKmlApiPlugin.PLUGINNAME + " kml class found. creating marshall method");

				generateMarshallerAndUnMarshaller(cc);
			}
		}

	}

	private void generateMarshallerAndUnMarshaller(final ClassOutlineImpl cc) {
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
		contentHandlerClass = codeModel._ref(ContentHandler.class);
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
		fileNotFoundExceptionClass = codeModel._ref(FileNotFoundException.class);

		
		xmlReaderClass = codeModel._ref(XMLReader.class);
		inputsourceClass = codeModel._ref(InputSource.class);
		fileReaderClass = codeModel._ref(FileReader.class);
		saxsourceClass = codeModel._ref(SAXSource.class);
		saxParserFactoryClass = codeModel._ref(SAXParserFactory.class);
		parserConfigurationExceptionClass = codeModel._ref(ParserConfigurationException.class);
		
		final CodeModelClassFactory classFactory = outline.getClassFactory();
		final JPackage kmlpackage = Util.getKmlClassPackage(outline);
		namespaceFilterHandler = classFactory.createClass(kmlpackage, JMod.FINAL, "NamespaceFilterHandler", null, ClassType.CLASS);
		namespaceFilterHandler._implements(contentHandlerClass.boxify());
		final JFieldVar KML_20 = namespaceFilterHandler.field(JMod.PRIVATE | JMod.STATIC | JMod.FINAL, stringClass, "KML_20", JExpr.lit("http://earth.google.com/kml/2.0"));
		final JFieldVar KML_21 = namespaceFilterHandler.field(JMod.PRIVATE | JMod.STATIC | JMod.FINAL, stringClass, "KML_21", JExpr.lit("http://earth.google.com/kml/2.1"));
		final JFieldVar KML_22 = namespaceFilterHandler.field(JMod.PRIVATE | JMod.STATIC | JMod.FINAL, stringClass, "KML_22", JExpr.lit("http://www.opengis.net/kml/2.2"));
		final JFieldVar content = namespaceFilterHandler.field(JMod.PRIVATE, contentHandlerClass, "contentHandler");
		final JMethod constructor = namespaceFilterHandler.constructor(JMod.PUBLIC);
		final JVar constructorParam = constructor.param(contentHandlerClass, "contentHandler");
		constructor.body().assign(JExpr._this().ref(content), constructorParam);
		
		final JMethod startElement = namespaceFilterHandler.method(JMod.PUBLIC, cc.implClass.owner().VOID, "startElement");
		startElement._throws(SAXException.class);
		final JVar uri = startElement.param(stringClass, "uri");
		final JVar localName = startElement.param(stringClass, "localName");
		final JVar qName = startElement.param(stringClass, "qName");
		final JVar atts = startElement.param(Attributes.class, "atts");
		final JConditional if1 = startElement.body()._if(uri.invoke("equals").arg(KML_20).cor(uri.invoke("equals").arg(KML_21)));
		if1._then().block().add(content.invoke("startElement").arg(KML_22).arg(localName).arg(qName).arg(atts));
		if1._else().block().add(content.invoke("startElement").arg(uri).arg(localName).arg(qName).arg(atts));
		
		final JMethod characters = namespaceFilterHandler.method(JMod.PUBLIC, cc.implClass.owner().VOID, "characters");
		final JVar charCh = characters.param(cc.implClass.owner().CHAR.array(), "ch");
		final JVar charStart = characters.param(cc.implClass.owner().INT, "start");
		final JVar charLength = characters.param(cc.implClass.owner().INT, "length");
		characters._throws(saxExceptionClass.boxify());
		characters.body().add(content.invoke("characters").arg(charCh).arg(charStart).arg(charLength));
		
		final JMethod endDocument = namespaceFilterHandler.method(JMod.PUBLIC, cc.implClass.owner().VOID, "endDocument");
		endDocument._throws(saxExceptionClass.boxify());
		endDocument.body().add(content.invoke("endDocument"));
		
		final JMethod endElement = namespaceFilterHandler.method(JMod.PUBLIC, cc.implClass.owner().VOID, "endElement");
		final JVar endElementUri = endElement.param(stringClass, "uri");
		final JVar endElementLocalName = endElement.param(stringClass, "localName");
		final JVar endElementqName = endElement.param(stringClass, "qName");
		endElement._throws(saxExceptionClass.boxify());
		endElement.body().add(content.invoke("endElement").arg(endElementUri).arg(endElementLocalName).arg(endElementqName));
	
		final JMethod endPrefixMapping = namespaceFilterHandler.method(JMod.PUBLIC, cc.implClass.owner().VOID, "endPrefixMapping");
		final JVar endPrefixMappingPrefix = endPrefixMapping.param(stringClass, "prefix");
		endPrefixMapping._throws(saxExceptionClass.boxify());
		endPrefixMapping.body().add(content.invoke("endPrefixMapping").arg(endPrefixMappingPrefix));
		
		final JMethod ignorableWhitespace = namespaceFilterHandler.method(JMod.PUBLIC, cc.implClass.owner().VOID, "ignorableWhitespace");
		final JVar ignorableWhitespaceCh = ignorableWhitespace.param(cc.implClass.owner().CHAR.array(), "ch");
		final JVar ignorableWhitespaceStart = ignorableWhitespace.param(cc.implClass.owner().INT, "start");
		final JVar ignorableWhitespaceLength = ignorableWhitespace.param(cc.implClass.owner().INT, "length");
		ignorableWhitespace._throws(saxExceptionClass.boxify());
		ignorableWhitespace.body().add(content.invoke("ignorableWhitespace").arg(ignorableWhitespaceCh).arg(ignorableWhitespaceStart).arg(ignorableWhitespaceLength));
		
		final JMethod processingInstruction = namespaceFilterHandler.method(JMod.PUBLIC, cc.implClass.owner().VOID, "processingInstruction");
		final JVar processingInstructionTarget = processingInstruction.param(stringClass, "target");
		final JVar processingInstructionData = processingInstruction.param(stringClass, "data");
		processingInstruction._throws(saxExceptionClass.boxify());
		processingInstruction.body().add(content.invoke("processingInstruction").arg(processingInstructionTarget).arg(processingInstructionData));
		
		final JMethod setDocumentLocator = namespaceFilterHandler.method(JMod.PUBLIC, cc.implClass.owner().VOID, "setDocumentLocator");
		final JVar setDocumentLocatorLocator = setDocumentLocator.param(Locator.class, "locator");
		setDocumentLocator.body().add(content.invoke("setDocumentLocator").arg(setDocumentLocatorLocator));
		
		final JMethod skippedEntity = namespaceFilterHandler.method(JMod.PUBLIC, cc.implClass.owner().VOID, "skippedEntity");
		final JVar skippedEntityName = skippedEntity.param(stringClass, "name");
		skippedEntity._throws(saxExceptionClass.boxify());
		skippedEntity.body().add(content.invoke("skippedEntity").arg(skippedEntityName));
		
		final JMethod startDocument = namespaceFilterHandler.method(JMod.PUBLIC, cc.implClass.owner().VOID, "startDocument");
		startDocument._throws(saxExceptionClass.boxify());
		startDocument.body().add(content.invoke("startDocument"));
		
		final JMethod startPrefixMapping = namespaceFilterHandler.method(JMod.PUBLIC, cc.implClass.owner().VOID, "startPrefixMapping");
		final JVar startPrefixMappingPrefix = startPrefixMapping.param(stringClass, "prefix");
		final JVar startPrefixMappingUri = startPrefixMapping.param(stringClass, "uri");
		startPrefixMapping._throws(saxExceptionClass.boxify());
		startPrefixMapping.body().add(content.invoke("startPrefixMapping").arg(startPrefixMappingPrefix).arg(startPrefixMappingUri));
	
		namespaceFilterXMLReaderclass = classFactory.createClass(kmlpackage, JMod.FINAL, "NamespaceFilterXMLReader", null, ClassType.CLASS);
		namespaceFilterXMLReaderclass._implements(xmlReaderClass.boxify());
		final JFieldVar xmlReader = namespaceFilterXMLReaderclass.field(JMod.PRIVATE, xmlReaderClass, "xmlReader");
		final JMethod namespaceFilterXMLReaderclassConstrutor = namespaceFilterXMLReaderclass.constructor(JMod.PUBLIC);
		final JVar paramValidate = namespaceFilterXMLReaderclassConstrutor.param(cc.implClass.owner().BOOLEAN, "validate");
		namespaceFilterXMLReaderclassConstrutor._throws(saxExceptionClass.boxify())._throws(parserConfigurationExceptionClass.boxify());
		final JVar parserFactory = namespaceFilterXMLReaderclassConstrutor.body().decl(saxParserFactoryClass, "parserFactory", saxParserFactoryClass.boxify().staticInvoke("newInstance"));
		namespaceFilterXMLReaderclassConstrutor.body().add(parserFactory.invoke("setNamespaceAware").arg(JExpr.TRUE));
		namespaceFilterXMLReaderclassConstrutor.body().add(parserFactory.invoke("setValidating").arg(paramValidate));
		namespaceFilterXMLReaderclassConstrutor.body().assign(xmlReader, parserFactory.invoke("newSAXParser").invoke("getXMLReader"));
		
		final JMethod getContentHandler = namespaceFilterXMLReaderclass.method(JMod.PUBLIC, ContentHandler.class, "getContentHandler");
		getContentHandler.body()._return(xmlReader.invoke("getContentHandler"));
		
		final JMethod getDTDHandler = namespaceFilterXMLReaderclass.method(JMod.PUBLIC, DTDHandler.class, "getDTDHandler");
		getDTDHandler.body()._return(xmlReader.invoke("getDTDHandler"));
		
		final JMethod getEntityResolver = namespaceFilterXMLReaderclass.method(JMod.PUBLIC, EntityResolver.class, "getEntityResolver");
		getEntityResolver.body()._return(xmlReader.invoke("getEntityResolver"));
		
		final JMethod getErrorHandler = namespaceFilterXMLReaderclass.method(JMod.PUBLIC, ErrorHandler.class, "getErrorHandler");
		getErrorHandler.body()._return(xmlReader.invoke("getErrorHandler"));
		
		final JMethod getFeature = namespaceFilterXMLReaderclass.method(JMod.PUBLIC, cc.implClass.owner().BOOLEAN, "getFeature");
		final JVar getFeatureName = getFeature.param(stringClass, "name");
		getFeature._throws(SAXNotRecognizedException.class)._throws(SAXNotSupportedException.class);
		getFeature.body()._return(xmlReader.invoke("getFeature").arg(getFeatureName));
		
		final JMethod getProperty = namespaceFilterXMLReaderclass.method(JMod.PUBLIC, Object.class, "getProperty");
		final JVar getPropertyName = getProperty.param(stringClass, "name");
		getProperty._throws(SAXNotRecognizedException.class)._throws(SAXNotSupportedException.class);
		getProperty.body()._return(xmlReader.invoke("getProperty").arg(getPropertyName));
		
		final JMethod parse = namespaceFilterXMLReaderclass.method(JMod.PUBLIC, cc.implClass.owner().VOID, "parse");
		final JVar parseInput = parse.param(InputSource.class, "input");
		parse._throws(IOException.class)._throws(SAXException.class);
		parse.body().add(xmlReader.invoke("parse").arg(parseInput));
		
		final JMethod parse2 = namespaceFilterXMLReaderclass.method(JMod.PUBLIC, cc.implClass.owner().VOID, "parse");
		final JVar parseSystemId = parse2.param(String.class, "systemId");
		parse2._throws(IOException.class)._throws(SAXException.class);
		parse2.body().add(xmlReader.invoke("parse").arg(parseSystemId));
		
		final JMethod setContentHandler = namespaceFilterXMLReaderclass.method(JMod.PUBLIC, cc.implClass.owner().VOID, "setContentHandler");
		final JVar setContentHandlerHandler = setContentHandler.param(ContentHandler.class, "handler");
		setContentHandler.body().add(xmlReader.invoke("setContentHandler").arg(JExpr._new(namespaceFilterHandler).arg(setContentHandlerHandler)));
		
		final JMethod setDTDHandler = namespaceFilterXMLReaderclass.method(JMod.PUBLIC, cc.implClass.owner().VOID, "setDTDHandler");
		final JVar setDTDHandlerHandler = setDTDHandler.param(DTDHandler.class, "handler");
		setDTDHandler.body().add(xmlReader.invoke("setDTDHandler").arg(setDTDHandlerHandler));
		
		final JMethod setEntityResolver = namespaceFilterXMLReaderclass.method(JMod.PUBLIC, cc.implClass.owner().VOID, "setEntityResolver");
		final JVar setEntityResolverHandler = setEntityResolver.param(EntityResolver.class, "handler");
		setEntityResolver.body().add(xmlReader.invoke("setEntityResolver").arg(setEntityResolverHandler));
		
		final JMethod setErrorHandler = namespaceFilterXMLReaderclass.method(JMod.PUBLIC, cc.implClass.owner().VOID, "setErrorHandler");
		final JVar setErrorHandlerHandler = setErrorHandler.param(ErrorHandler.class, "handler");
		setErrorHandler.body().add(xmlReader.invoke("setErrorHandler").arg(setErrorHandlerHandler));
		
		final JMethod setFeature = namespaceFilterXMLReaderclass.method(JMod.PUBLIC, cc.implClass.owner().VOID, "setFeature");
		final JVar setFeatureName = setFeature.param(stringClass, "name");
		final JVar setFeatureValue = setFeature.param(cc.implClass.owner().BOOLEAN, "value");
		setFeature._throws(SAXNotRecognizedException.class)._throws(SAXNotSupportedException.class);
		setFeature.body().add(xmlReader.invoke("setFeature").arg(setFeatureName).arg(setFeatureValue));
		
		final JMethod setProperty = namespaceFilterXMLReaderclass.method(JMod.PUBLIC, cc.implClass.owner().VOID, "setProperty");
		final JVar setPropertyName = setProperty.param(stringClass, "name");
		final JVar setPropertyValue = setProperty.param(Object.class, "value");
		setProperty._throws(SAXNotRecognizedException.class)._throws(SAXNotSupportedException.class);
		setProperty.body().add(xmlReader.invoke("setProperty").arg(setPropertyName).arg(setPropertyValue));
		
		generateHelperMethods(cc);
		generateMarshalMethods(cc);
		generateUnMarshalMethods(cc);
		
	}


	private void generateHelperMethods(final ClassOutlineImpl cc) {
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
			final JDefinedClass createNestedPrefixCustomizer = createNestedPrefixCustomizer(cc);
			ifBlockMarshaller._then().add(
			    mVar.invoke("setProperty").arg("com.sun.xml.bind.namespacePrefixMapper").arg(JExpr._new(createNestedPrefixCustomizer)));
		} catch (final JClassAlreadyExistsException e) {
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

	private void createAddToKmzFile(final ClassOutlineImpl cc) {
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
		final JVar varKmzFile = createAddToKmzFile.param(kmlClass, "kmzFile");
		final JVar varOut = createAddToKmzFile.param(zipOutputStreamClass, "out");
		final JVar varMainFile = createAddToKmzFile.param(cc.implClass.owner().BOOLEAN, "mainfile");
		createAddToKmzFile.javadoc().add("Internal method");
		final JVar varFileName = createAddToKmzFile.body().decl(stringClass, "fileName", JExpr._null());
		final JConditional if1 = createAddToKmzFile.body()._if(
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

	private JDefinedClass createNestedPrefixCustomizer(final ClassOutlineImpl cc) throws JClassAlreadyExistsException {
		final JDefinedClass namespacebeautyfier = cc.implClass._class(JMod.PRIVATE | JMod.STATIC | JMod.FINAL, "NameSpaceBeautyfier");
		namespacebeautyfier._extends(NamespacePrefixMapper.class);

		final JMethod namespacebeautyfiergetpreferredprefix = namespacebeautyfier.method(JMod.PUBLIC, stringClass, "getPreferredPrefix");
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
		final JVar namespaceuri = namespacebeautyfiergetpreferredprefix.param(stringClass, "namespaceUri");
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
	private void generateMarshalMethods(final ClassOutlineImpl cc) {
		final ArrayList<String> comment = new ArrayList<String>();
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
		comment.add("The object graph is marshalled to a Contenthandler object.\n");
		comment.add("Useful if  marshaller cis needed to generate CDATA blocks.\n");
		comment.add("{@link https://jaxb.dev.java.net/faq/}\n");
		comment.add("{@link http://code.google.com/p/javaapiforkml/issues/detail?id=7}\n");
		comment.add("The object is not saved as a zipped .kmz file.\n");
		comment.add("@see marshalKmz(String, Kml...)");
		generateMarshal(cc, contentHandlerClass, comment);
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
		final JVar outVar = generateMarshallFilenameWithZIP.body().decl(outputStreamClass, "out", JExpr._new(fileOutputStreamClass).arg(filenameVar));
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



	private JMethod generateMarshal(final ClassOutlineImpl cc, final ArrayList<String> comment) {
		// public boolean marshall(final String filename) throws FileNotFoundException {
		final JMethod generateMarshalOutputStream = cc.implClass.method(JMod.PUBLIC, cc.implClass.owner().BOOLEAN, "marshal");
		generateMarshalOutputStream.javadoc().append(comment);

		// try {
		final JTryBlock tryBlock = generateMarshalOutputStream.body()._try();
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
		final JBlock catchBlock = tryBlock._catch(jaxbExceptionClass.boxify()).body();
		catchBlock.directStatement("_x.printStackTrace();");
		catchBlock._return(JExpr.FALSE);
		return generateMarshalOutputStream;
	}

	private JMethod generateMarshal(final ClassOutlineImpl cc, final JType argumentType, final ArrayList<String> comment) {
		// public boolean marshall(final String filename) throws FileNotFoundException {
		final JMethod generateMarshalOutputStream = cc.implClass.method(JMod.PUBLIC, cc.implClass.owner().BOOLEAN, "marshal");
		if (argumentType.equals(outputStreamClass)) {
			generateMarshalOutputStream._throws(FileNotFoundException.class);
		}
		generateMarshalOutputStream.javadoc().append(comment);
		final JVar value = generateMarshalOutputStream.param(JMod.FINAL, argumentType, argumentType.name().toLowerCase());

		// try {
		final JTryBlock tryBlock = generateMarshalOutputStream.body()._try();
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
		final JBlock catchBlock = tryBlock._catch(jaxbExceptionClass.boxify()).body();
		catchBlock.directStatement("_x.printStackTrace();");
		catchBlock._return(JExpr.FALSE);
		return generateMarshalOutputStream;
	}

	private void generateUnMarshalMethods(final ClassOutlineImpl cc) {
		schemaLocationVar = cc.implClass.field(JMod.PRIVATE | JMod.STATIC | JMod.FINAL, stringClass, "SCHEMA_LOCATION", JExpr
		    .lit("src/main/resources/schema/ogckml/ogckml22.xsd"));

		final JMethod generateValidate = generateValidateMethod(cc);

		generateUnmarshalFileWithOptionalValidate(cc, generateValidate);

		generateUnmarshalMethodFromString(cc, stringReaderClass);

		generateUnmarshalMethodPlain(cc, inputStreamClass);
		
		//generateUnmarshalLegacyKmlMethod(cc);
		
		generateUnmarshalFromKmzMethod(cc);
	}
	
	
	/**
	 * public static Kml unmarshalLegacyKml(File source) throws FileNotFoundException {
	 * 	try {
	 * 		Unmarshaller unmarshaller = JAXBContext.newInstance((Kml.class)).createUnmarshaller();
	 * 		XMLReader reader = new NamespaceFilterXMLReader();
	 * 		InputSource is = new InputSource(new FileReader(source));
	 * 		SAXSource ss = new SAXSource(reader, is);
	 * 		return (Kml) unmarshaller.unmarshal(ss);
	 * 	} catch (SAXException _x) {
	 * 		_x.printStackTrace();
	 * 	} catch (ParserConfigurationException _x) {
	 * 		_x.printStackTrace();
	 * 	} catch (JAXBException _x) {
	 * 		_x.printStackTrace();
	 * 	}
	 * 	return null;
	 * }
	 */
	private void generateUnmarshalLegacyKmlMethod(final ClassOutlineImpl cc) {
		final JMethod generateUnMarshallerFromString = cc.implClass.method(JMod.PUBLIC | JMod.STATIC, kmlClass, "unmarshalLegacyKml");
		generateUnMarshallerFromString.javadoc().add("KML to Java\n");
		generateUnMarshallerFromString.javadoc().add("reads legacy kml files. \n");
		generateUnMarshallerFromString.javadoc().add("Supported are KML 2.0 (namespace: http://earth.google.com/kml/2.0) \n");
		generateUnMarshallerFromString.javadoc().add("          and KML 2.1 (namespace: http://earth.google.com/kml/2.1) \n");
		generateUnMarshallerFromString.javadoc().trimToSize();
		
		final JVar fileunmarshallVar = generateUnMarshallerFromString.param(JMod.FINAL, fileClass, "file");
		generateUnMarshallerFromString._throws(FileNotFoundException.class);
		final JTryBlock tryBlock = generateUnMarshallerFromString.body()._try();
		final JVar localUnmarshallerFile = tryBlock.body().decl(jaxbUnmarshallerClass, "unmarshaller",
		    jaxbContextClass.boxify().staticInvoke("newInstance").arg(JExpr.direct("Kml.class")).invoke("createUnmarshaller"));
		
		final JVar inputsource = tryBlock.body().decl(inputsourceClass, "input", JExpr._new(inputsourceClass).arg(JExpr._new(fileReaderClass).arg(fileunmarshallVar)));
		final JVar saxsource = tryBlock.body().decl(saxsourceClass, "saxSource", JExpr._new(saxsourceClass).arg(JExpr._new(namespaceFilterXMLReaderclass).arg(JExpr.FALSE)).arg(inputsource));
		final JVar decl = tryBlock.body().decl(kmlClass, "jaxbRootElement", JExpr.cast(kmlClass, JExpr.invoke(localUnmarshallerFile, "unmarshal").arg(saxsource)));
		tryBlock.body()._return(decl);
		tryBlock._catch(saxExceptionClass.boxify()).body().directStatement("_x.printStackTrace();");
		tryBlock._catch(parserConfigurationExceptionClass.boxify()).body().directStatement("_x.printStackTrace();");
		tryBlock._catch(jaxbExceptionClass.boxify()).body().directStatement("_x.printStackTrace();");
		generateUnMarshallerFromString.body()._return(JExpr._null());
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

			LOG.info("element: " + entryName);
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
	private void generateUnmarshalFromKmzMethod(final ClassOutlineImpl cc) {
		final JMethod unmarshalFromKMZ = cc.implClass.method(JMod.PUBLIC | JMod.STATIC, kmlClass.array(), "unmarshalFromKmz");
		unmarshalFromKMZ.javadoc().add("KMZ to Java\n");
		unmarshalFromKMZ.javadoc().add("Similar to the other unmarshal methods\n\n");
		unmarshalFromKMZ.javadoc().add("with the exception that it transforms a KMZ-file into a graph of Java objects. \n");	
		unmarshalFromKMZ._throws(IOException.class);
		final JVar varFile = unmarshalFromKMZ.param(fileClass, "file");
		varFile.annotate(NotNull.class);
		final JVar varEMPTY_KML_ARRAY = unmarshalFromKMZ.body().decl(kmlClass.array(), "EMPTY_KML_ARRAY", JExpr.direct("new Kml[0]"));// JExpr._new(kmlClass.array().)..arg("0"));
		unmarshalFromKMZ.body()._if(varFile.invoke("getName").invoke("endsWith").arg(".kmz").not())._then()._return(varEMPTY_KML_ARRAY);
		final JVar varZip = unmarshalFromKMZ.body().decl(zipFileClass, "zip",JExpr._new(zipFileClass).arg(varFile) );
		final JVar varEntries = unmarshalFromKMZ.body().decl(enumerationClass.boxify().narrow(zipEntryClass.boxify().wildcard()), "entries", varZip.invoke("entries"));
		unmarshalFromKMZ.body()._if(varFile.invoke("exists").not())._then()._return(varEMPTY_KML_ARRAY);
		final JVar varKmlFiles = unmarshalFromKMZ.body().decl(arrayListClass.boxify().narrow(kmlClass.boxify()), "kmlfiles", JExpr._new(arrayListClass.boxify().narrow(kmlClass.boxify())));
		final JBlock while1 = unmarshalFromKMZ.body()._while(varEntries.invoke("hasMoreElements")).body();
		final JVar varEntry = while1.decl(zipEntryClass, "entry",JExpr.cast(zipEntryClass, varEntries.invoke("nextElement")));
		while1._if(varEntry.invoke("getName").invoke("contains").arg("__MACOSX").cor(varEntry.invoke("getName").invoke("contains").arg(".DS_STORE")))._then()._continue();
		final JVar entryName = while1.decl(stringClass, "entryName", urlDecoderClass.boxify().staticInvoke("decode").arg(varEntry.invoke("getName")).arg("UTF-8"));
		while1._if(entryName.invoke("endsWith").arg(".kml").not())._then()._continue();
		final JVar varIn = while1.decl(inputStreamClass, "in", varZip.invoke("getInputStream").arg(varEntry));
		final JVar varUnmarshal = while1.decl(kmlClass, "unmarshal", kmlClass.boxify().staticInvoke("unmarshal").arg(varIn));
		while1.add(varKmlFiles.invoke("add").arg(varUnmarshal));
		unmarshalFromKMZ.body().add(varZip.invoke("close"));
		unmarshalFromKMZ.body()._return(varKmlFiles.invoke("toArray").arg(varEMPTY_KML_ARRAY));
		
//		JVar varOut = unmarshalFromKMZ.body().decl(zipEntryClass, "out", JExpr._new(zipOutputStreamClass).arg(varName));
		
  }
	
	private void generateMarshalKmz(final ClassOutlineImpl cc) {
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
		final JVar varName = generateMarshalKmz.param(stringClass, "name");
		varName.annotate(NotNull.class);
		final JVar varAdditionalFiles = generateMarshalKmz.varParam(kmlClass.boxify(), "additionalFiles");
		generateMarshalKmz._throws(IOException.class);
		final JVar varOut = generateMarshalKmz.body().decl(zipOutputStreamClass, "out", JExpr._new(zipOutputStreamClass).arg(JExpr._new(fileOutputStreamClass).arg(varName)));
		generateMarshalKmz.body().add(varOut.invoke("setComment").arg("KMZ-file created with Java API for KML. Visit us: http://code.google.com/p/javaapiforkml/"));
		generateMarshalKmz.body().add(JExpr._this().invoke(createAddToKmzFile).arg(JExpr._this()).arg(varOut).arg(JExpr.TRUE));
		final JForEach forEach = generateMarshalKmz.body().forEach(kmlClass, "kml", varAdditionalFiles);
		forEach.body().add(JExpr._this().invoke(createAddToKmzFile).arg(forEach.var()).arg(varOut).arg(JExpr.FALSE));
		generateMarshalKmz.body().add(varOut.invoke("close"));
		generateMarshalKmz.body().assign(varMissingNameCounter,JExpr.lit(1));
		generateMarshalKmz.body()._return(JExpr.FALSE);
	}

	private void generateUnmarshalMethodPlain(final ClassOutlineImpl cc, final JClass invokeMarshalWith) {
		final JMethod generateUnMarshallerFromString = cc.implClass.method(JMod.PUBLIC | JMod.STATIC, kmlClass, "unmarshal");
		generateUnMarshallerFromString.javadoc().add("KML to Java\n");
		generateUnMarshallerFromString.javadoc().add("Similar to the other unmarshal methods \n\n");
		generateUnMarshallerFromString.javadoc().add(
		    "with the exception that it transforms a " + invokeMarshalWith.name() + " into a graph of Java objects. \n");
		generateUnMarshallerFromString.javadoc().trimToSize();
		final JVar stringunmarshallVar = generateUnMarshallerFromString.param(JMod.FINAL, invokeMarshalWith, "content");

		final JTryBlock tryStringBlock = generateUnMarshallerFromString.body()._try();
		final JVar localUnmarshaller = tryStringBlock.body().decl(jaxbUnmarshallerClass, "unmarshaller",
		    jaxbContextClass.boxify().staticInvoke("newInstance").arg(JExpr.direct("Kml.class")).invoke("createUnmarshaller"));
		
		final JVar inputsource = tryStringBlock.body().decl(inputsourceClass, "input", JExpr._new(inputsourceClass).arg(stringunmarshallVar));
		final JVar saxsource = tryStringBlock.body().decl(saxsourceClass, "saxSource", JExpr._new(saxsourceClass).arg(JExpr._new(namespaceFilterXMLReaderclass).arg(JExpr.FALSE)).arg(inputsource));
		final JVar decl = tryStringBlock.body().decl(kmlClass, "jaxbRootElement", JExpr.cast(kmlClass, JExpr.invoke(localUnmarshaller, "unmarshal").arg(saxsource)));
		tryStringBlock.body()._return(decl);
		tryStringBlock._catch(saxExceptionClass.boxify()).body().directStatement("_x.printStackTrace();");
		tryStringBlock._catch(parserConfigurationExceptionClass.boxify()).body().directStatement("_x.printStackTrace();");
		tryStringBlock._catch(jaxbExceptionClass.boxify()).body().directStatement("_x.printStackTrace();");
		generateUnMarshallerFromString.body()._return(JExpr._null());		
	}

	private void generateUnmarshalMethodFromString(final ClassOutlineImpl cc, final JType invokeMarshalWith) {
		final JMethod generateUnMarshallerFromString = cc.implClass.method(JMod.PUBLIC | JMod.STATIC, kmlClass, "unmarshal");
		generateUnMarshallerFromString.javadoc().add("KML to Java\n");
		generateUnMarshallerFromString.javadoc().add("Similar to the other unmarshal methods \n\n");
		generateUnMarshallerFromString.javadoc().add("with the exception that it transforms a String into a graph of Java objects. \n");
		generateUnMarshallerFromString.javadoc().trimToSize();
		final JVar stringunmarshallVar = generateUnMarshallerFromString.param(JMod.FINAL, stringClass, "content");

		final JTryBlock tryStringBlock = generateUnMarshallerFromString.body()._try();
		final JVar localUnmarshaller = tryStringBlock.body().decl(jaxbUnmarshallerClass, "unmarshaller",
		    jaxbContextClass.boxify().staticInvoke("newInstance").arg(JExpr.direct("Kml.class")).invoke("createUnmarshaller"));
		
		final JVar inputsource = tryStringBlock.body().decl(inputsourceClass, "input", JExpr._new(inputsourceClass).arg(JExpr._new(stringReaderClass).arg(stringunmarshallVar)));
		final JVar saxsource = tryStringBlock.body().decl(saxsourceClass, "saxSource", JExpr._new(saxsourceClass).arg(JExpr._new(namespaceFilterXMLReaderclass).arg(JExpr.FALSE)).arg(inputsource));
		final JVar decl = tryStringBlock.body().decl(kmlClass, "jaxbRootElement", JExpr.cast(kmlClass, JExpr.invoke(localUnmarshaller, "unmarshal").arg(saxsource)));
		tryStringBlock.body()._return(decl);
		tryStringBlock._catch(saxExceptionClass.boxify()).body().directStatement("_x.printStackTrace();");
		tryStringBlock._catch(parserConfigurationExceptionClass.boxify()).body().directStatement("_x.printStackTrace();");
		tryStringBlock._catch(jaxbExceptionClass.boxify()).body().directStatement("_x.printStackTrace();");
		generateUnMarshallerFromString.body()._return(JExpr._null());		
	}

	private void generateUnmarshalFileWithOptionalValidate(final ClassOutlineImpl cc, final JMethod generateValidate) {
		final JMethod generateUnMarshallerFileFile = cc.implClass.method(JMod.PUBLIC | JMod.STATIC, kmlClass, "unmarshal");
		generateUnMarshallerFileFile.javadoc().add("KML to Java\n");
		generateUnMarshallerFileFile.javadoc().add("KML given as a file object is transformed into a graph of Java objects.\n");
		generateUnMarshallerFileFile.javadoc().add("The boolean value indicates, whether the File object should be validated \n");
		generateUnMarshallerFileFile.javadoc().add("automatically during unmarshalling and be checked if the object graph meets \n");
		generateUnMarshallerFileFile.javadoc().add("all constraints defined in OGC's KML schema specification.");
		generateUnMarshallerFileFile.javadoc().trimToSize();
		final JVar fileunmarshallVar = generateUnMarshallerFileFile.param(JMod.FINAL, fileClass, "file");
		final JVar validateVar = generateUnMarshallerFileFile.param(JMod.FINAL, boolean.class, "validate");

		final JTryBlock tryBlock = generateUnMarshallerFileFile.body()._try();
		final JVar localUnmarshallerFile = tryBlock.body().decl(jaxbUnmarshallerClass, "unmarshaller",
		    jaxbContextClass.boxify().staticInvoke("newInstance").arg(JExpr.direct("Kml.class")).invoke("createUnmarshaller"));

		final JConditional ifBlockFilename = tryBlock.body()._if(validateVar.eq(JExpr.TRUE));
		ifBlockFilename._then().add(kmlClass.boxify().staticInvoke(generateValidate).arg(localUnmarshallerFile));
		
		final JVar inputsource = tryBlock.body().decl(inputsourceClass, "input", JExpr._new(inputsourceClass).arg(JExpr._new(fileReaderClass).arg(fileunmarshallVar)));
		final JVar saxsource = tryBlock.body().decl(saxsourceClass, "saxSource", JExpr._new(saxsourceClass).arg(JExpr._new(namespaceFilterXMLReaderclass).arg(validateVar)).arg(inputsource));
		final JVar decl = tryBlock.body().decl(kmlClass, "jaxbRootElement", JExpr.cast(kmlClass, JExpr.invoke(localUnmarshallerFile, "unmarshal").arg(saxsource)));
		tryBlock.body()._return(decl);
		tryBlock._catch(saxExceptionClass.boxify()).body().directStatement("_x.printStackTrace();");
		tryBlock._catch(parserConfigurationExceptionClass.boxify()).body().directStatement("_x.printStackTrace();");
		tryBlock._catch(jaxbExceptionClass.boxify()).body().directStatement("_x.printStackTrace();");
		tryBlock._catch(fileNotFoundExceptionClass.boxify()).body().directStatement("_x.printStackTrace();");
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

	private JMethod generateValidateMethod(final ClassOutlineImpl cc) {
		final JMethod generateValidate = cc.implClass.method(JMod.PRIVATE | JMod.STATIC, cc.implClass.owner().BOOLEAN, "validate");
		final JVar unmarshallValidateVar = generateValidate.param(JMod.FINAL, jaxbUnmarshallerClass, "unmarshaller");
		final JTryBlock tryValidateBlock = generateValidate.body()._try();
		final JVar schemaFactoryVar = tryValidateBlock.body().decl(schemaFactoryClass, "sf",
		    schemaFactoryClass.boxify().staticInvoke("newInstance").arg(xmlConstantsClass.boxify().staticRef("W3C_XML_SCHEMA_NS_URI")));
		final JVar schemaFileVar = tryValidateBlock.body().decl(fileClass, "schemaFile", JExpr._new(fileClass).arg(schemaLocationVar));
		final JVar schemaVar = tryValidateBlock.body().decl(schemaClass, "schema", schemaFactoryVar.invoke("newSchema").arg(schemaFileVar));
		tryValidateBlock.body().add(unmarshallValidateVar.invoke("setSchema").arg(schemaVar));
		tryValidateBlock.body()._return(JExpr.TRUE);
		final JBlock catchValidateBlock = tryValidateBlock._catch(saxExceptionClass.boxify()).body();
		catchValidateBlock.directStatement("_x.printStackTrace();");
		generateValidate.body()._return(JExpr.FALSE);
		return generateValidate;
	}

	/**
	 * Generates an expression that evaluates to "new QName(...)"
	 */
	private JInvocation createQName(final String packageUri, final String name) {
		return JExpr._new(codeModel.ref(QName.class)).arg(packageUri).arg(name);
	}
}
