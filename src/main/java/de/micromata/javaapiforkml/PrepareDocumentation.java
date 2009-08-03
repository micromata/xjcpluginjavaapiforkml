package de.micromata.javaapiforkml;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Map.Entry;

import javax.xml.bind.JAXBException;

import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.Source;
import net.htmlparser.jericho.Tag;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.jvnet.jaxb2_commons.javaforkmlapi.addproperjavadoc.JaxbJavaDoc;
import org.jvnet.jaxb2_commons.javaforkmlapi.addproperjavadoc.JaxbJavaDocElements;
import org.jvnet.jaxb2_commons.javaforkmlapi.addproperjavadoc.XjcAddProperJavaDocumentationForKML;



/**
 * helper class, that parses the kml-reference and convert it into a appropriate data-format.
 */
public class PrepareDocumentation {
	private static final Logger LOG = Logger.getLogger(PrepareDocumentation.class.getName());

	/*
	 * creates a map with all elements found in the kml reference file. This method parses an given html file (should be html) found at:
	 * http://code.google.com/intl/de-DE/apis/kml/documentation/kmlreference.html and extract the documentation for each documented
	 * kml-keyword. Each kml-keyword is saved (if it is present) with a description, the syntax, examples, extended by, contained by, ... a
	 * possible kml-element structure could look like: <pre> &lt;h2&gt;KML_ELEMENT_1&lt;/h2&gt; &lt;h3&gt;Syntax&lt;/h3&gt;
	 * &lt;h3&gt;Description&lt;/h3&gt; &lt;h3&gt;Extends&lt;/h3&gt; &lt;h3&gt;Extended By&lt;/h3&gt; &lt;h2&gt;KML_ELEMENT_2&lt;/h2&gt;
	 * &lt;h3&gt;Syntax&lt;/h3&gt; &lt;h3&gt;Description&lt;/h3&gt; &lt;h3&gt;Extends&lt;/h3&gt; &lt;h3&gt;Extended By&lt;/h3&gt;
	 * &lt;h3&gt;Contains&lt;/h3&gt; </pre>
	 * 
	 * @return a map with all elements found in the kmlreference file
	 * 
	 * @throws MalformedURLException
	 * 
	 * @throws IOException
	 */
	private static HashMap<String, KMLReferenceField> parseKmlReferenceForSuitableJavaDoc(String kmlReferenceHtmlPage)
	    throws MalformedURLException, IOException {
		Source source = new Source(new URL(kmlReferenceHtmlPage));

		// look out for these tags
		ArrayList<String> lookForTheseTags = new ArrayList<String>();
		lookForTheseTags.add("p");
		lookForTheseTags.add("pre");
		lookForTheseTags.add("ul");
		lookForTheseTags.add("dl");
		lookForTheseTags.add("ol");
		lookForTheseTags.add("h4");
		lookForTheseTags.add("table");

		// returns a list with all (html-) elements found in the kml-reference-guide
		List<Element> allHtmlElements = source.getAllElements();
		HashMap<String, KMLReferenceField> kmlElements = preParseElements(allHtmlElements);

		for (Element htmlElement : allHtmlElements) {
			// each kml element has a h2-heading!
			if (htmlElement.getName().equals("h2")) {
				LOG.info("-------------------------------------------------------------------------------");
				String kmlElementName = htmlElement.getTextExtractor().toString();
				String kmlElementNameClean = cleanTagName(kmlElementName);

				// take a look if the element is already know - if not, create a new one
				KMLReferenceField kmlField = kmlElements.get(kmlElementNameClean);
				if (kmlField == null) {
					LOG.info("!! Element not found - creating new for: " + kmlElementNameClean);
					kmlField = new KMLReferenceField();
				}

				// print and set the title
				LOG.info("KML Element: " + kmlElementNameClean);
				kmlField.setName(kmlElementName);
				kmlField.setNameClean(kmlElementNameClean);

				Tag currentTag = htmlElement.getStartTag().getNextTag().getNextTag();
				while (!currentTag.getName().equals("h2")) {
					if (currentTag.getName().equals("h3")) {
						// here all the magic happens! an
						checkForPossibleSubElements(currentTag, lookForTheseTags, kmlField, kmlElements);
					}

					/*
					 * Each element is represented by two tags (start- and end-tag). And because there is no method like nextElement, we have to skip
					 * two tags, to get to the next element.
					 */
					if ((currentTag.getNextTag() == null) || (currentTag.getNextTag().getNextTag() == null)) {
						LOG.info("-> break");
						break;
					}
					currentTag = currentTag.getNextTag().getNextTag();
				}

				// put current element into the map of kmlElements (but only if a description is set)
				if (kmlField.getDescription().size() == 0) {
					continue;
				}
				kmlElements.put(kmlField.getNameClean(), kmlField);
			}
		}

		return kmlElements;
	}

	/*
	 * checks for sub-elements for an kml-tag each tag could have one of the following properties: <pre> - syntax - elementsSpecificTo -
	 * description - extend - extendedBy - contains - example - containedBy - seealso </pre> the structure of the html file is: <pre>
	 * <h2>KML_ELEMENT_2</h2> <h3>Syntax</h3> <h3>Description</h3> <h3>Extends</h3> <h3>Extended By</h3> <h3>Contains</h3> <h3>...</h3> </pre>
	 * 
	 * @param ownerTag
	 * 
	 * @param checkTag
	 * 
	 * @param elements
	 * 
	 * @param javaDoc
	 * 
	 * @param kmlElements
	 * 
	 * @return
	 */
	private static Tag checkForPossibleSubElements(Tag ownerTag, ArrayList<String> lookForTheseTags, KMLReferenceField kmlField,
	    HashMap<String, KMLReferenceField> kmlElements) {

		Tag currentTag = ownerTag.getElement().getEndTag().getNextTag();
		String h3Header = ownerTag.getElement().getTextExtractor().toString().trim().toLowerCase();

		// if current tag is a h3-tag, check of element-properties
		while (!currentTag.getName().equals("h3")) {

			for (String foundpossibleSubTag : lookForTheseTags) {
				// if possible substring iss found and is not annotated as "back-to-top" then examine the element-kind
				if (currentTag.getName().equals(foundpossibleSubTag) && !"backtotop".equals(currentTag.getElement().getAttributeValue("class"))) {

					String textFromCurrentElement = currentTag.getElement().getTextExtractor().toString().trim();
					// if there is no text in the current element -> skip to the next tag
					if (textFromCurrentElement.length() == 0) {
						continue;
					}

					/*
					 * <h3>Syntax</h3>-Element found? The syntax-element dows usually contain one <pre>-tag containing the basic syntax of the
					 * kml-element
					 */
					if (h3Header.startsWith("syntax")) {
						// LOG.info("Syntax:     \t" + textFromCurrentElement);
						for (Element elementPre : currentTag.getChildElements()) {
							kmlField.addToSyntax(elementPre.toString());
						}
					}

					/*
					 * <h3>Description</h3>-Element found? The description-element contains one or more <p>-tags, which descripe the usage of the
					 * kml-element
					 */
					else if (h3Header.startsWith("description") || foundpossibleSubTag.equals("h4")) {
						LOG.info("Description: \t" + textFromCurrentElement);
						kmlField.addToDescription(textFromCurrentElement);
					}

					/*
					 * <h3>Extends</h3>-Element found? if only one element is listed it is encapsulated in a <p>-tag and if it extends more elements
					 * it's encapsulated in an <ul>-tag as unordered list
					 */
					else if (h3Header.startsWith("extends")) {
						if (currentTag.getName().equals("p")) {
							kmlField.addToExtend(textFromCurrentElement);
							LOG.info("Extends:     \t" + textFromCurrentElement);
						} else if (currentTag.getName().equals("ul")) {
							for (Element elementUL : currentTag.getElement().getChildElements()) {

								for (Element elementLI : elementUL.getChildElements()) {
									kmlField.addToExtend(elementLI.getTextExtractor().toString());
									LOG.info("Extends:     \t" + elementLI.getTextExtractor().toString());
								}
							}
						} else {
							// LOG.info("Extends EL:     \t----------- damn");
						}
					}

					/*
					 * <h3>Extended By</h3>-Element found? if only one element is listed it is encapsulated in a <p>-tag and if it extends more
					 * elements it's encapsulated in an <ul>-tag as unordered list
					 */
					else if (h3Header.startsWith("extended by")) {
						if (currentTag.getName().equals("p")) {
							kmlField.addToExtendedBy(textFromCurrentElement);
							LOG.info("Extended By: \t" + textFromCurrentElement);
						} else if (currentTag.getName().equals("ul")) {
							for (Element elementUL : currentTag.getElement().getChildElements()) {
								// text in ul we want is encapsulated in <a>- or
								// in <strong>-tag
								LOG.info("Extended By: \t" + elementUL.getStartTag().getNextTag().getElement().getTextExtractor().toString());
								kmlField.addToExtendedBy(elementUL.getStartTag().getNextTag().getElement().getTextExtractor().toString());
							}
						} else {
							// LOG.info("Extended By EL: \t----------- damn ");
						}
					}

					/*
					 * <h3>Elements Specific to</h3>-Element found? the complex case! "elements specific to"-could define new elements or explains
					 * elements, which are only use as fields for existings kml-fields
					 */
					else if (h3Header.startsWith("elements specific to")) {
						if ((currentTag.getName().equals("p") || currentTag.getName().equals("dl") || currentTag.getName().equals("dt") || currentTag
						    .getName().equals("ul"))) {
							Tag endTag = currentTag.getElement().getEndTag();

							KMLReferenceField foundSpecificToField = null;
							while (currentTag.getEnd() < endTag.getEnd()) {
								// abort condition !h3 needed by <IconStyle> because of wrong nested h3-element (<dl><h3></h3>...)
								if (currentTag.getName().equals("h3")) {
									LOG.info("!! abort condition !h3 needed by <IconStyle> because of wrong nested h3-element (<dl><h3></h3>...)");
									break;
								}

								// abort condition: needed by <Feature> to skip the explanation of the differences between Google Earth 4.3
								// and 5.0
								if (!(currentTag.getName().equals("p")) && currentTag.getElement().getTextExtractor().toString().equals("Google Earth 5.0")) {
									LOG.info("!! needed by <Feature> to skip the explanation of the differences between Google Earth 4.3 and 5.0");
									break;
								}

								/*
								 * specific to field found check if, it is already known in the kmlElements-map and of not, register it.
								 */
								if (currentTag.getName().equals("dt")) {
									String cleanSpecificElementName = cleanTagName(currentTag.getElement().getTextExtractor().toString().toLowerCase().trim());
									foundSpecificToField = kmlElements.get(cleanSpecificElementName);

									// special case for <icon> to prevent the creation of an empty element
									if (cleanSpecificElementName.length() > 0) {
										// field not found, create new one
										if (foundSpecificToField == null) {
											LOG.info("Elements S.: \t - creating new for: " + cleanSpecificElementName);
											foundSpecificToField = new KMLReferenceField();
											kmlElements.put(cleanSpecificElementName, foundSpecificToField);
										}
										// set the names for the specific to field
										foundSpecificToField.setName(currentTag.getElement().getTextExtractor().toString().toLowerCase().trim());
										foundSpecificToField.setNameClean(cleanSpecificElementName);
										LOG.info("Elements S.: \t" + cleanSpecificElementName);
									}
								}

								// add description and (if found) the syntax-example to an possible found "specific-to"-field
								if (foundSpecificToField != null) {
									String currentText = currentTag.getElement().getTextExtractor().toString();
									if (currentText.length() > 0) {
										// description is found
										if (currentTag.getName().equals("dd") || currentTag.getName().equals("p")) {
											LOG.info("           : \t" + currentText);
											foundSpecificToField.addToDescription(currentText);
										}
										// syntax example is found
										if (currentTag.getName().equals("pre")) {
											LOG.info("Elements SE: \t" + currentText);
											foundSpecificToField.addToExample(currentText);
										}
									}
								}
								currentTag = currentTag.getNextTag().getNextTag();
							}
						} else {
							// LOG.info("Elements S.: \t----------- damn ");
						}
					}

					/*
					 * <h3>Example</h3>-Element found? an example could start with an optional link to a kml-file of the example, encapsulated in a
					 * <p>-tag or it starts directly with the excample-code encapsulated in a <pre>-tag
					 */
					else if (h3Header.startsWith("example")) {
						if (currentTag.getName().equals("p")) {
							if (currentTag.getNextTag().getNextTag().getName().equals("a")) {
								// LOG.info("Example   a: \t" +
								// currentTag.getNextTag().getNextTag().getElement().getAttributeValue("href"));
								kmlField.addToExample(currentTag.getNextTag().getNextTag().getElement().getAttributeValue("href"));
							}

						} else if (currentTag.getName().equals("pre")) {
							// LOG.info("Example pre: \t" + currentTag.getElement().getTextExtractor().toString());
							kmlField.addToExample(currentTag.getElement().toString());
						} else {
							// LOG.info("Example EL: \t----------- damn");
						}

					}

					/*
					 * <h3>See Also</h3>-Element found? if only one element is listed it is encapsulated in a <p>-tag and if it extends more elements
					 * it's encapsulated in an <ul>-tag as unordered list
					 */
					else if (h3Header.startsWith("see also")) {
						if (currentTag.getName().equals("p")) {
							kmlField.addToSeealso(textFromCurrentElement);
							LOG.info("See Also:    \t" + textFromCurrentElement);
						} else if (currentTag.getName().equals("ul")) {
							for (Element elementUL : currentTag.getElement().getChildElements()) {
								for (Element elementLI : elementUL.getChildElements()) {
									kmlField.addToSeealso(elementLI.getTextExtractor().toString());
									LOG.info("See Also:    \t" + elementLI.getTextExtractor().toString());
								}
							}
						} else {
							// LOG.info("See Also EL: \t----------- damn");
						}
					}

					/*
					 * <h3>Sample use Of</h3>-Element found?
					 */
					else if (h3Header.startsWith("sample use of")) {
						LOG.info("Sample Use of: \t" + textFromCurrentElement);
						kmlField.addToExample(textFromCurrentElement);
					}

					/*
					 * <h3>Contains</h3>-Element found? if only one element is listed it is encapsulated in a <p>-tag and if it extends more elements
					 * it's encapsulated in an <ul>-tag as unordered list
					 */
					else if (h3Header.startsWith("contains")) {
						if (currentTag.getName().equals("p")) {
							kmlField.addToContains(textFromCurrentElement);
							LOG.info("Contains:     \t" + textFromCurrentElement);
						} else if (currentTag.getName().equals("ul")) {
							for (Element elementUL : currentTag.getElement().getChildElements()) {
								for (Element elementLI : elementUL.getChildElements()) {
									kmlField.addToContains(elementLI.getTextExtractor().toString());
									LOG.info("Contains:     \t" + elementLI.getTextExtractor().toString());
								}
							}
						} else {
							// LOG.info("Contains EL:     \t----------- damn ");
						}
					}

					/*
					 * <h3>Contained By</h3>-Element found? if only one element is listed it is encapsulated in a <p>-tag and if it extends more
					 * elements it's encapsulated in an <ul>-tag as unordered list
					 */
					else if (h3Header.startsWith("contained by")) {
						if (currentTag.getName().equals("p")) {
							kmlField.addToContainedBy(textFromCurrentElement);
							LOG.info("Contained By: \t" + textFromCurrentElement);
						} else if (currentTag.getName().equals("ul")) {
							for (Element elementUL : currentTag.getElement().getChildElements()) {
								for (Element elementLI : elementUL.getChildElements()) {
									kmlField.addToContainedBy(elementLI.getTextExtractor().toString());
									LOG.info("Contained By: \t" + elementLI.getTextExtractor().toString());
								}
							}
						} else {
							// LOG.info("Contained By EL: \t----------- damn");
						}
					}

				}
			}

			/*
			 * Each element is represented by two tags (start- and end-tag). And because there is no method like nextElement, we have to skip two
			 * tags, to get to the next element.
			 */
			if ((currentTag.getNextTag() == null) || (currentTag.getNextTag().getNextTag() == null)) {
				LOG.info("-> break");
				break;
			}
			currentTag = currentTag.getNextTag().getNextTag();
		}

		return currentTag;
	}

	/**
	 * 
	 * @param kmlReferenceHtmlPage
	 * @param saveToFile
	 * @return
	 * @throws MalformedURLException
	 * @throws IOException
	 */
	public static HashMap<String, JaxbJavaDoc> buildDocumentationFromFoundElements(String kmlReferenceHtmlPage, String saveToFile)
	    throws MalformedURLException, IOException {
		HashMap<String, KMLReferenceField> kmlElements = parseKmlReferenceForSuitableJavaDoc(kmlReferenceHtmlPage);
		if ((kmlElements == null) || (kmlElements.size() == 0)) {
			return null;
		}
		HashMap<String, JaxbJavaDoc> jaxbKMLJavadocs = new HashMap<String, JaxbJavaDoc>();

		// adding the KML fields listed on the webpage
		kmlElements.put("altitudemode",
		    addSimpleField("AltitudeMode", "clampToGround, relativeToGround, absolute", "See <LookAt> and <Region>"));
		kmlElements.put("angle90", addSimpleField("Angle90", "a value >=-90 and <=90", "See <latitude> in <Model> "));
		kmlElements.put("anglepos90", addSimpleField("Anglepos90", "a value >=0 and <=90 ", "See <tilt> in <LookAt> "));
		kmlElements.put("angle180", addSimpleField("Angle180", "a value >=-180 and <=180", "See <longitude> in <Model> "));
		kmlElements.put("angle360",
		    addSimpleField("Angle360", "a value >=-360 and <=360", "See <heading>, <tilt>, and <roll> in <Orientation>"));
		kmlElements.put("colorenum", addSimpleField("Color", "hexBinary value: aabbggrr", "See any element that extends <ColorStyle>"));
		kmlElements.put("colormode", addSimpleField("ColorMode", "normal, random ", "See any element that extends <ColorStyle>"));
		kmlElements.put("datetime", addSimpleField("DateTime", "dateTime, date, gYearMonth, gYear", "See <TimeSpan> and <TimeStamp>"));
		kmlElements.put("displaymode", addSimpleField("DisplayMode", "default, hide ", "See <BalloonStyle>"));
		kmlElements.put("gridorigin", addSimpleField("GridOrigin", "lowerLeft, upperLeft", "See <PhotoOverlay>"));
		kmlElements.put("refreshmode", addSimpleField("RefreshMode", "onChange, onInterval, onExpire", "See <Link>"));
		kmlElements.put("shape", addSimpleField("Shape", "rectangle, cylinder, sphere", "See <PhotoOverlay>"));
		kmlElements.put("stylestate", addSimpleField("StyleState", "normal, highlight", "See <StyleMap>"));
		kmlElements.put("units", addSimpleField("Units", "fraction, pixels, insetPixels ", "See <hotSpot> in <IconStyle>, <ScreenOverlay>"));
		kmlElements.put("vec2", addSimpleField("Vec2", "x=double xunits=kml:unitsEnum y=double yunits=kml:unitsEnum",
		    "See <hotSpot> in <IconStyle>, <ScreenOverlay>"));
		kmlElements.put("viewrefreshmode", addSimpleField("ViewRefreshMode", "never, onRequest, onStop, onRegion ", "See <Link>"));

		// iterate over all elements found in the kml-refernce
		for (Entry<String, KMLReferenceField> entry : kmlElements.entrySet()) {
			// skip elements, that have no values
			if (entry.getValue().getName() == null) {
				continue;
			}

			KMLReferenceField kmlEntry = entry.getValue();

			JaxbJavaDoc jkje = new JaxbJavaDoc();
			// the class name, should be the same-classname as the jaxb-generated classname
			jkje.setClassName(entry.getValue().getNameClean());

			// build the javadoc comment
			StringBuffer javaDoc = new StringBuffer();

			// the kml-element-name
			javaDoc.append(kmlEntry.getName() + "\n");

			// the description
			if (kmlEntry.getDescription().size() > 0) {
				for (String docDescription : convertMapToList(kmlEntry.getDescription())) {
					docDescription = makeLineBrakes(docDescription, 80);
					javaDoc.append("<p>\n" + docDescription + "</p>\n");
				}
				javaDoc.append("\n");
			}

			// syntax
			if (kmlEntry.getSyntax().size() > 0) {
				javaDoc.append("Syntax: \n");
				javaDoc.append(kmlEntry.getSyntax().get(0));
				javaDoc.append("\n");
				javaDoc.append("\n");
			}

			// extends
			if (kmlEntry.getExtend().size() > 0) {
				javaDoc.append("Extends: \n");
				for (String docExtends : convertMapToList(kmlEntry.getExtend())) {
					javaDoc.append("@see: " + docExtends + "\n");
				}
				javaDoc.append("\n");
			}

			// extended by
			if (kmlEntry.getExtendedBy().size() > 0) {
				javaDoc.append("Extended By: \n");
				for (String docExtendedBy : convertMapToList(kmlEntry.getExtendedBy())) {
					javaDoc.append("@see: " + docExtendedBy + "\n");
				}
				javaDoc.append("\n");
			}

			// contains
			if (kmlEntry.getContains().size() > 0) {
				javaDoc.append("Contains: \n");
				for (String docContains : convertMapToList(kmlEntry.getContains())) {
					javaDoc.append("@see: " + docContains + "\n");
				}
				javaDoc.append("\n");
			}

			// contained by
			if (kmlEntry.getContainedBy().size() > 0) {
				javaDoc.append("Contained By: \n");
				for (String docContainedBy : convertMapToList(kmlEntry.getContainedBy())) {
					javaDoc.append("@see: " + docContainedBy + "\n");
				}
				javaDoc.append("\n");
			}

			// see also
			if (kmlEntry.getSeealso().size() > 0) {
				javaDoc.append("See Also: \n");
				for (String docSeeAlso : convertMapToList(kmlEntry.getSeealso())) {
					javaDoc.append(docSeeAlso + "\n");
				}
				javaDoc.append("\n");
			}

			// set the javadoc to the javadoc-element
			// LOG.info(javadoc.toString());
			jkje.setJavaDoc(javaDoc.toString());
			// add the javadoc-element to the map of all javadoc elements
			jaxbKMLJavadocs.put(kmlEntry.getNameClean(), jkje);
		}

		LOG.info("JavaDoc elements found: " + jaxbKMLJavadocs.size());

		saveJavaDocObjectIntoXmlFile(saveToFile, jaxbKMLJavadocs);

		return jaxbKMLJavadocs;
	}

	private static ArrayList<String> convertMapToList(ArrayList<String> seealso) {
		Collections.sort(seealso);
	  return seealso;
  }

	/*
	 * @param name the name of the element to be set
	 * 
	 * @param description the description to be set
	 * 
	 * @param seeAlso the 'see also'-text to be set
	 * 
	 * @return a simple Element
	 */
	private static KMLReferenceField addSimpleField(String name, String description, String seeAlso) {
		KMLReferenceField kmlField = new KMLReferenceField();

		kmlField.setName(name);
		kmlField.setNameClean(name.toLowerCase().trim());
		kmlField.addToDescription(description);
		kmlField.addToSeealso(seeAlso);

		return kmlField;
	}

	/*
	 * inserts '\n' every few chars
	 * 
	 * @param text the text that should be broken into lines
	 * 
	 * @param maxLineBreakLength insert an '\n' every n chars
	 * 
	 * @return
	 */
	private static String makeLineBrakes(String text, int maxLineBreakLength) {
		StringBuffer string = new StringBuffer();
		StringTokenizer tokens = new StringTokenizer(text);
		ArrayList<String> lines = new ArrayList<String>();

		// take the string
		while (tokens.hasMoreElements()) {
			String object = (String) tokens.nextElement();
			string.append(object + " ");

			// insert every n-chars an \n
			if ((string.length() >= maxLineBreakLength) || !tokens.hasMoreElements()) {
				string.append("\n");
				lines.add(string.toString());
				string.delete(0, string.length());
			}
		}

		// build the text block
		for (String line : lines) {
			string.append(line);
		}

		return string.toString();
	}

	/*
	 * prepare buildDocumentation() scans the google-kml-reference for the left-side-all-kml-keyword-navigation-list. Looks like: <p> <code>
	 * KML Reference - <AbstractView> - <address> - <AddressDetails> ... </code> </p>
	 * 
	 * @param allHtmlElements
	 * 
	 * @param kmlElements
	 */
	private static HashMap<String, KMLReferenceField> preParseElements(List<Element> allHtmlElements) {
		HashMap<String, KMLReferenceField> kmlElements = new HashMap<String, KMLReferenceField>();

		for (Element htmlElement : allHtmlElements) {
			if (htmlElement.getName().equals("ul") && "1-sub-1".equals(htmlElement.getAttributeValue("id"))) {
				for (Element elementLI : htmlElement.getChildElements()) {
					String cleanTitle = cleanTagName(elementLI.getTextExtractor().toString().toLowerCase());
					kmlElements.put(cleanTitle, new KMLReferenceField());
				}
				LOG.info(kmlElements.keySet().toString());
				LOG.info("Elements in KML Reference found: " + kmlElements.size());
			}
		}

		return kmlElements;
	}

	/*
	 * cleans a tag <pre> - <range> (required) --> <range> - <SimpleField type="string" name="string"> --> <simplefield> - <gx:flyto> -->
	 * <flyto> && <atom:author> --> <author> </pre>
	 */
	private static String cleanTagName(String title) {
		String cleanTitle = title.toLowerCase();
		if (cleanTitle.contains("<") && cleanTitle.contains(">")) {

			// <range> (required) --> <range>
			cleanTitle = cleanTitle.substring(cleanTitle.indexOf("<"), cleanTitle.indexOf(">") + 1);

			// <SimpleField type="string" name="string"> --> <simplefield>
			if (cleanTitle.contains(" ")) {
				cleanTitle = cleanTitle.substring(cleanTitle.indexOf("<"), cleanTitle.indexOf(" ")) + ">";
			}

			// <gx:flyto> --> <flyto> && <atom:author> --> <author>
			if (cleanTitle.contains(":")) {
				cleanTitle = "<" + cleanTitle.substring(cleanTitle.lastIndexOf(":") + 1, cleanTitle.length());
			}

			cleanTitle = cleanTitle.substring(cleanTitle.indexOf("<") + 1, cleanTitle.indexOf(">"));
		}

		return cleanTitle;
	}

	/**
	 * generates the xsd-schema-file and the xml-file with the foud JavaDocs
	 * 
	 * @param javadocElements
	 */
	private static void saveJavaDocObjectIntoXmlFile(String saveToFile, HashMap<String, JaxbJavaDoc> javadocElements) {
		if ((javadocElements == null) || (javadocElements.size() == 0)) {
			return;
		}
		JaxbJavaDocElements jdel = new JaxbJavaDocElements();
		ArrayList<JaxbJavaDoc> elementsjavadoc = convertHashMapToArrayList(javadocElements);
		jdel.setElements(elementsjavadoc);

		JaxbTool<JaxbJavaDocElements> jaxt;
		try {
			jaxt = new JaxbTool<JaxbJavaDocElements>(JaxbJavaDocElements.class);
			String schemaFileName = saveToFile.replaceAll("(.*?)(.xml)$", "$1.xsd");
			jaxt.generateSchema(schemaFileName);
			LOG.info("------------ written Schema-file:  " + schemaFileName);

			jaxt.marshal(jdel, new FileOutputStream(saveToFile));
			LOG.info("------------ written JavaDoc-file: " + saveToFile);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/*
	 * private helper funtion needed by JAXB to convert a HashMap into an ArraylLst (JAXB's ability to marshall HashMaps aren't that good)
	 * 
	 * @param hashmap the given HashMap
	 * 
	 * @return the newly created ArrayList
	 */
	private static ArrayList<JaxbJavaDoc> convertHashMapToArrayList(HashMap<String, JaxbJavaDoc> hashmap) {
		ArrayList<JaxbJavaDoc> elementsjavadoc = new ArrayList<JaxbJavaDoc>();
		for (JaxbJavaDoc arraylist : hashmap.values()) {
			elementsjavadoc.add(arraylist);
		}
		return elementsjavadoc;
	}

	public static void main(String[] args) throws Exception {
		BasicConfigurator.configure();
		try {
			PrepareDocumentation.buildDocumentationFromFoundElements("file:src/main/resources/data/kmlreference22_ext_firefox.html",
					XjcAddProperJavaDocumentationForKML.LOADJAVADOCSFROMFILE);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
