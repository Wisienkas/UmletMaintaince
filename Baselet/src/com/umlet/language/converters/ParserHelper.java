package com.umlet.language.converters;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;

import com.baselet.elementnew.PropertiesParserState;
import com.baselet.elementnew.facet.Facet;
import com.baselet.elementnew.facet.common.AutosizeFacet;
import com.baselet.elementnew.facet.common.BackgroundColorFacet;
import com.baselet.elementnew.facet.common.ElementStyleFacet;
import com.baselet.elementnew.facet.common.FontSizeFacet;
import com.baselet.elementnew.facet.common.ForegroundColorFacet;
import com.baselet.elementnew.facet.common.GroupFacet;
import com.baselet.elementnew.facet.common.HierarchyRelation;
import com.baselet.elementnew.facet.common.HorizontalAlignFacet;
import com.baselet.elementnew.facet.common.LayerFacet;
import com.baselet.elementnew.facet.common.LineTypeFacet;
import com.baselet.elementnew.facet.common.LineWidthFacet;
import com.baselet.elementnew.facet.common.SeparatorLineFacet;
import com.baselet.elementnew.facet.common.VerticalAlignFacet;
import com.baselet.elementnew.facet.relation.DescriptionPositionFacet;
import com.baselet.elementnew.facet.relation.RelationLineTypeFacet;
import com.baselet.elementnew.facet.specific.ActiveClassFacet;
import com.baselet.elementnew.facet.specific.InnerClassFacet;
import com.baselet.elementnew.facet.specific.SpecialStateTypeFacet;
import com.baselet.elementnew.facet.specific.StateTypeFacet;
import com.baselet.elementnew.facet.specific.SubStateSymbolFacet;
import com.baselet.elementnew.facet.specific.TemplateClassFacet;
import com.baselet.elementnew.facet.specific.UpperRightSymbolFacet;
import com.baselet.elementnew.settings.SettingsManualResizeTop;
import com.umlet.language.java.Accessible.AccessFlag;

public class ParserHelper {
	private static Logger log = Logger.getLogger(ParserHelper.class);

	private static final List<? extends Facet> facets = Arrays.asList(
			AutosizeFacet.INSTANCE,
			BackgroundColorFacet.INSTANCE,
			ElementStyleFacet.INSTANCE,
			FontSizeFacet.INSTANCE,
			ForegroundColorFacet.INSTANCE,
			GroupFacet.INSTANCE,
			HierarchyRelation.INSTANCE,
			HorizontalAlignFacet.INSTANCE,
			LayerFacet.INSTANCE,
			LineTypeFacet.INSTANCE,
			LineWidthFacet.INSTANCE,
			SeparatorLineFacet.INSTANCE,
			VerticalAlignFacet.INSTANCE,
			DescriptionPositionFacet.INSTANCE_START,
			DescriptionPositionFacet.INSTANCE_END,
			RelationLineTypeFacet.INSTANCE,
			ActiveClassFacet.INSTANCE,
			InnerClassFacet.INSTANCE,
			SpecialStateTypeFacet.INSTANCE,
			StateTypeFacet.INSTANCE,
			SubStateSymbolFacet.INSTANCE,
			TemplateClassFacet.INSTANCE,
			UpperRightSymbolFacet.INSTANCE
			);

	private static final PropertiesParserState propertiesParser = new PropertiesParserState(new SettingsManualResizeTop() {
		@Override
		public List<? extends Facet> createFacets() {
			return facets;
		}
	});

	private static final Map<String, Class<?>> BUILT_IN_MAP = new ConcurrentHashMap<String, Class<?>>();

	static
	{
		for (Class<?> c : new Class[] { String.class, List.class, void.class, boolean.class, byte.class, char.class,
				short.class, int.class, float.class, double.class, long.class }) {
			BUILT_IN_MAP.put(c.getSimpleName(), c);
		}
	}

	private static String codeTrimming(String attribute)
	{
		attribute = attribute.trim();

		if (attribute.isEmpty()) {
			return attribute;
		}

		if (attribute.startsWith("//")) {
			return "";
		}

		if (attribute.startsWith("."))
		{
			boolean only = true;
			int len = attribute.length();
			for (int i = 1; i < len; i++) {
				if (attribute.charAt(i) != attribute.charAt(0)) {
					only = false;
				}
			}
			if (only) {
				return "";
			}
		}

		for (Facet facet : facets)
		{
			if (facet.checkStart(attribute, propertiesParser)) {
				log.debug(attribute + " was recogniced as facet");
				return "";
			}
		}

		return attribute;
	}

	public static String advancedAttributeParser(String attributes)
	{
		StringBuilder sb = new StringBuilder();

		String[] array = attributes.split("\n");

		CodeModifiers mod = new CodeModifiers();

		for (String element : array) {
			element = codeTrimming(element);

			if (!element.isEmpty()) {
				sb.append(element);

				String modified = mod.parse(element).trim();

				if (!element.isEmpty() && !modified.endsWith(","))
				{
					sb.append("\n");
				}
			}
		}

		return sb.toString();
	}

	public static boolean hasAccessLevel(char accesslevel)
	{
		String cflag = String.valueOf(accesslevel);

		for (AccessFlag flag : AccessFlag.values())
		{
			if (cflag.equals(flag.toString())) {
				return true;
			}
		}

		return false;
	}

	public static AccessFlag accessLevelParser(char accesslevel)
	{
		String cflag = String.valueOf(accesslevel);

		for (AccessFlag flag : AccessFlag.values())
		{
			if (cflag.equals(flag.toString())) {
				return flag;
			}
		}

		return AccessFlag.PUBLIC;
	}

	public static Class<?> forName(String name) {
		int indexOf = name.indexOf("<");

		if (indexOf >= 0) {
			name = name.substring(0, indexOf);
		}
		if (name.isEmpty()) {
			return null;
		}

		Class<?> c = BUILT_IN_MAP.get(name);
		if (c == null) {
			try
			{
				// assumes you have only one class loader!
				BUILT_IN_MAP.put(name, c = Class.forName(name));
			} catch (ClassNotFoundException e)
			{
				log.info("ClassLoader did not find " + name);
			}
		}

		return c;
	}
}
