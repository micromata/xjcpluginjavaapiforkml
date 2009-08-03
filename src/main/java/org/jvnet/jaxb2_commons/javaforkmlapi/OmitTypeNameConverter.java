package org.jvnet.jaxb2_commons.javaforkmlapi;

import org.apache.log4j.Logger;

import com.sun.xml.bind.api.impl.NameConverter;

/**
 * Name converter that unconditionally converts to camel case.
 */
class OmitTypeNameConverter extends NameConverter.Standard {
	private static final Logger LOG = Logger.getLogger(OmitTypeNameConverter.class.getName());

	public String toClassName(String s) {
		s = eliminateTypeSuffix(s);
		return toMixedCaseName(toWordList(s), true);
	}

	private String eliminateTypeSuffix(String namewithoutType) {
		StringBuffer tmp = new StringBuffer();
		if (namewithoutType.endsWith("Type") && !namewithoutType.endsWith("listItemType")) {
			namewithoutType = namewithoutType.substring(0, namewithoutType.length() - 4);
			tmp.append("T");
		}
		if (namewithoutType.startsWith("Abstract")) {
			if (!namewithoutType.startsWith("AbstractObject") && !namewithoutType.startsWith("AbstractLatLonBox") && !namewithoutType
			    .startsWith("AbstractView")) {
				namewithoutType = namewithoutType.substring(8, namewithoutType.length());
				tmp.append("A");
			}
		}

		if (namewithoutType.endsWith("Enum")) {
			namewithoutType = namewithoutType.substring(0, namewithoutType.length() - 4);
			tmp.append("E");
		}
		
		if (namewithoutType.endsWith("Group")) {// && !namewithoutType.startsWith("altitudeModeGroup")) {
			namewithoutType = namewithoutType.substring(0, namewithoutType.length() - 5);
			tmp.append("G");
		}

		if (namewithoutType.equals("")) {
			namewithoutType = new String("underscore");
		}

		// if (namewithoutType.endsWith("ExtensionGroup")) {
		// namewithoutType = namewithoutType.substring(0, namewithoutType.length() - 14);
		// tmp.append("G");
		// }
		// if (namewithoutType.endsWith("Group")) {
		// namewithoutType = namewithoutType.substring(0, namewithoutType.length() - 5);
		// tmp.append("G");
		// }
		//		
		// if (namewithoutType.endsWith("Extension")) {
		// namewithoutType = namewithoutType.substring(0, namewithoutType.length() - 9);
		// tmp.append("E");
		// }

		// just debug output
//		if (namewithoutType.length() > 0 && tmp.length() > 0) {
//			LOG.info(XJCJavaForKmlApiPlugin.PLUGINNAME+" " + tmp.toString() + ": \t" + namewithoutType);
//
//		}
		return namewithoutType;
	}
}