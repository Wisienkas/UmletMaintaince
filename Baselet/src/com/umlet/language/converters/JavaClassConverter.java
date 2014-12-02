package com.umlet.language.converters;

import org.apache.log4j.Logger;
import org.jboss.forge.roaster.Roaster;
import org.jboss.forge.roaster.model.source.FieldSource;
import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.jboss.forge.roaster.model.source.JavaInterfaceSource;
import org.jboss.forge.roaster.model.source.JavaSource;
import org.jboss.forge.roaster.model.source.MethodSource;

import com.baselet.control.Constants;
import com.baselet.control.Path;
import com.umlet.language.FieldOptions;
import com.umlet.language.MethodOptions;
import com.umlet.language.SignatureOptions;
import com.umlet.language.java.Accessible.AccessFlag;
import com.umlet.language.java.Field;
import com.umlet.language.java.JavaClass;
import com.umlet.language.java.JavaClass.ClassRole;
import com.umlet.language.java.Method;
import com.umlet.language.java.bcel.BcelJavaClass;
import com.umlet.language.java.jp.JpJavaClass;

public class JavaClassConverter implements IClassConverter<JavaSource<?>> {
	private static final Logger log = Logger.getLogger(JavaClassConverter.class);

	public static final String JAVA_LANG = "java.lang";
	public static final String FINAL = "final ";
	public static final String INTERFACE_STRING = "<<" + ClassRole.INTERFACE + ">>";

	@Override
	public boolean canConvert(String extension) {
		return extension.equals("java") || extension.equals("class");
	}

	@Override
	public String getElementProperties(String filename) {
		JavaClass parsedClass = parseFile(filename);
		if (parsedClass == null) {
			return null;
		}

		StringBuilder sb = new StringBuilder();

		createTopSection(parsedClass, sb);
		sb.append(SPLITTER);

		createFieldSection(parsedClass, sb);
		sb.append(SPLITTER);

		createMethodSection(parsedClass, sb);
		sb.append(SPLITTER);

		return sb.toString();
	}

	@Override
	public void createTopSection(Object parsedClass, StringBuilder attributes) {
		ClassRole role = ((JavaClass) parsedClass).getRole();
		if (role == ClassRole.INTERFACE) {
			attributes.append(INTERFACE_STRING);
			attributes.append("\n");
			attributes.append(getClassName((JavaClass) parsedClass));
		}
		else if (role == ClassRole.ABSTRACT) {
			attributes.append("/");
			attributes.append(getClassName((JavaClass) parsedClass));
			attributes.append("/");
		}
		else {
			attributes.append(getClassName((JavaClass) parsedClass));
		}
		attributes.append("\n");
	}

	@Override
	public void createFieldSection(Object parsedClass, StringBuilder attributes) {
		for (Field field : ((JavaClass) parsedClass).getFields()) {
			if (Constants.generateClassFields == FieldOptions.ALL |
				Constants.generateClassFields == FieldOptions.PUBLIC && field.getAccess() == AccessFlag.PUBLIC) {

			}
			else {
				continue;
			}

			attributes.append(field.getAccess());
			attributes.append(field.getName());
			attributes.append(": ");
			attributes.append(field.getType());
			attributes.append("\n");
		}
	}

	@Override
	public void createMethodSection(Object parsedClass, StringBuilder attributes) {
		for (Method method : ((JavaClass) parsedClass).getMethods()) {
			if (Constants.generateClassMethods == MethodOptions.PUBLIC && method.getAccess() == AccessFlag.PUBLIC) {
				attributes.append(getMethodString(method));
			}
			else if (Constants.generateClassMethods == MethodOptions.ALL) {
				attributes.append(getMethodString(method));
			}
		}
	}

	@Override
	public SourceObject<JavaSource<?>> parseElementProperties(String attributes) {
		String[] sections = attributes.split(SPLITTER);

		log.debug("Sections: " + sections.length);

		if ((sections.length == 1 || sections.length == 3 || sections.length == 4) == false)
		{
			log.debug("Attributes was not correctly set, number of sections was " + sections.length);
			return null;
		}
		
		boolean isInterface = attributes.toLowerCase().contains(INTERFACE_STRING.toLowerCase());

		Class<? extends JavaSource<?>> sourceClass = isInterface ? JavaInterfaceSource.class : JavaClassSource.class;

		JavaSource<?> code = Roaster.create(sourceClass);

		SourceObject<JavaSource<?>> sourceObject = new SourceObject<JavaSource<?>>(code,
				p -> p.getName(),
				p -> p.getPackage());

		parseTopSection(sourceObject, ParserHelper.advancedAttributeParser(sections[0]));

		if (sections.length > 1)
		{
			parseFieldSection(sourceObject, ParserHelper.advancedAttributeParser(sections[1]));
			parseMethodSection(sourceObject, ParserHelper.advancedAttributeParser(sections[2]));
		}

		log.debug("Attributes:\n" + attributes + "\n\nwas parsed into\n\n" + code.toString());

		return sourceObject;
	}

	@Override
	public void parseTopSection(SourceObject<JavaSource<?>> sourceObject, String attributes) {
		JavaSource<?> source = sourceObject.getSource();

		for (String attribute : attributes.split("\n"))
		{
			CodeModifiers mod = new CodeModifiers();
			attribute = mod.parse(attribute);

			if (attribute.startsWith("<<") && attribute.endsWith(">>"))
			{
				String interfaze = attribute.substring(2, attribute.length() - 2);

				if (INTERFACE_STRING.equalsIgnoreCase(attribute) == false) {
					if (interfaze.contains("::") || interfaze.contains("."))
					{
						interfaze = interfaze.replace("::", ".");
					}
					else
					{
						interfaze = "com." + interfaze;
					}

					(source.isInterface() ? (JavaInterfaceSource) source : (JavaClassSource) source).addInterface(interfaze).setDefaultPackage();
				}
			}
			else
			{
				boolean hasPackageColon = attribute.contains("::");
				boolean hasPackageDot = attribute.contains(".");

				String[] identifiers = new String[2];

				if (hasPackageColon)
				{
					identifiers = attribute.split("::");
				}
				else if (hasPackageDot)
				{
					int lastDot = attribute.lastIndexOf(".");
					identifiers[0] = attribute.substring(0, lastDot);
					identifiers[1] = attribute.substring(lastDot + 1, attribute.length());
				}
				else
				{
					identifiers[0] = attribute;
				}

				if (!source.isInterface()) {
					JavaClassSource ccode = (JavaClassSource) source;
					if (mod.slash) {
						ccode.setAbstract(true);
					}
				}

				if (hasPackageColon || hasPackageDot)
				{
					log.debug(identifiers[0].trim());
					log.debug(identifiers[1].trim());
					source.setPackage(identifiers[0].trim()).setName(identifiers[1].trim());
				}
				else {
					log.debug(identifiers[0].trim());
					source.setDefaultPackage().setName(identifiers[0].trim());
				}

				return;
			}
		}
	}

	@Override
	public void parseFieldSection(SourceObject<JavaSource<?>> sourceObject, String attributes) {
		JavaSource<?> source = sourceObject.getSource();

		for (String attribute : attributes.split("\n"))
		{
			log.debug("Attribute: " + attribute);

			CodeModifiers mod = new CodeModifiers();

			attribute = mod.parse(attribute);

			if (attribute.isEmpty()) {
				continue;
			}

			AccessFlag flag = AccessFlag.PUBLIC;

			if (ParserHelper.hasAccessLevel(attribute.charAt(0)))
			{
				flag = ParserHelper.accessLevelParser(attribute.charAt(0));
				attribute = attribute.substring(1, attribute.length());
			}

			FieldSource<?> field = (source.isInterface() ? (JavaInterfaceSource) source : (JavaClassSource) source).addField();

			field.setStatic(mod.underscore);

			switch (flag)
			{
				case PACKAGE:
					field.setPackagePrivate();
					break;
				case PRIVATE:
					field.setPrivate();
					break;
				case PROTECTED:
					field.setProtected();
					break;
				case PUBLIC:
					field.setPublic();
					break;
			}

			try
			{
				String[] arguments = attribute.split(":");

				if (arguments[0].contains("="))
				{
					String[] container = arguments[0].split("=");
					arguments[0] = container[0].trim();
					field.setLiteralInitializer(container[1].trim());
				}

				field.setName(arguments[0].trim());

				if (arguments.length > 1)
				{
					log.debug(arguments[1].trim());
					field.setType(arguments[1].trim());
				}
			} catch (IllegalArgumentException e)
			{
				log.error("Field generation failed", e);
			}
		}
	}

	@Override
	public void parseMethodSection(SourceObject<JavaSource<?>> sourceObject, String attributes) {
		JavaSource<?> source = sourceObject.getSource();

		for (String attribute : attributes.split("\n"))
		{
			log.debug("Attribute: " + attribute);

			CodeModifiers mod = new CodeModifiers();
			attribute = mod.parse(attribute);

			if (attribute.isEmpty()) {
				continue;
			}

			AccessFlag flag = AccessFlag.PUBLIC;

			if (ParserHelper.hasAccessLevel(attribute.charAt(0)))
			{
				flag = ParserHelper.accessLevelParser(attribute.charAt(0));
				attribute = attribute.substring(1, attribute.length());
			}

			MethodSource<?> method = (source.isInterface() ? (JavaInterfaceSource) source : (JavaClassSource) source).addMethod();

			switch (flag)
			{
				case PACKAGE:
					method.setPackagePrivate();
					break;
				case PRIVATE:
					method.setPrivate();
					break;
				case PROTECTED:
					method.setProtected();
					break;
				case PUBLIC:
					method.setPublic();
					break;
			}

			int firstParenthesis = attribute.indexOf("(");
			int lastParenthesis = attribute.indexOf(")");
			int lastColon = attribute.lastIndexOf(":");

			String methodName = null;
			String signature = null;
			String returnType = null;

			if (firstParenthesis != -1 && lastParenthesis != -1)
			{
				methodName = attribute.substring(0, firstParenthesis).trim();
				signature = attribute.substring(firstParenthesis + 1, lastParenthesis).trim();

				if (lastColon > lastParenthesis) {
					returnType = attribute.substring(lastColon + 1, attribute.length()).trim();
				}
			}
			else
			{
				String[] arguments = attribute.split(":");
				methodName = arguments[0].trim();
				returnType = arguments[1].trim();
			}

			method.setName(methodName);

			boolean isConstructor = methodName.equals(source.getName());

			method.setConstructor(source.isInterface() ? false : isConstructor);
			method.setStatic(isConstructor ? false : mod.underscore);
			method.setAbstract(source.isInterface() ? false : mod.slash);

			if (signature != null && !signature.isEmpty())
			{
				parseMethodSignature(method, signature);
			}

			// Roaster does not currently support comments in code

			String code_body = "";

			if (returnType == null || isConstructor || returnType.trim().equals("void")) {
				method.setReturnTypeVoid();
			}
			else {

				// Default values pr http://docs.oracle.com/javase/specs/jls/se7/html/jls-4.html#jls-4.12.5
				switch (returnType.trim())
				{
					case "byte":
						code_body = "return (byte)0;";
						break;
					case "short":
						code_body = "return (short)0;";
						break;
					case "int":
						code_body = "return 0;";
						break;
					case "long":
						code_body = "return 0L;";
						break;
					case "float":
						code_body = "return 0.0f;";
						break;
					case "double":
						code_body = "return 0.0d;";
						break;
					case "char":
						code_body = "return (char)'\u0000';";
						break;
					case "boolean":
						code_body = "return false;";
						break;
					default:
						code_body = "return null;";
						break;
				}

				method.setReturnType(returnType);
			}

			if (!source.isInterface() && !method.isAbstract()) {
				method.setBody(code_body);
			}
		}
	}

	private static void parseMethodSignature(MethodSource<?> method, String signature)
	{
		String[] arguments = signature.split(",");

		StringBuilder sb = new StringBuilder();

		for (int i = 0; i < arguments.length; i++)
		{
			String sig = parseParameterSignature(method, arguments[i].trim(), i);

			if (!sig.isEmpty())
			{
				sb.append(sig);

				if (i + 1 < arguments.length) {
					sb.append(", ");
				}
			}
		}

		method.setParameters(sb.toString());

		log.debug(signature);
		log.debug(method.toString());
	}

	@SuppressWarnings("unused")
	private static String parseParameterSignature(MethodSource<?> method, String parameterString, int argIndex)
	{
		boolean isFinal = false;

		String[] arguments = new String[2];

		if (parameterString.startsWith(FINAL))
		{
			parameterString = parameterString.substring(FINAL.length(), parameterString.length());
			isFinal = true;
		}

		String[] split = parameterString.split(" ");

		if (split.length > 1)
		{
			arguments = split;
		}
		else
		{
			int genericIndex = split[0].indexOf("<");

			String classNoGenerics = null;

			if (genericIndex >= 0)
			{
				classNoGenerics = split[0].substring(0, genericIndex);
			}
			else
			{
				classNoGenerics = split[0];
			}

			if (Character.isLowerCase(split[0].charAt(0)) || ParserHelper.forName(classNoGenerics) == null) {
				arguments[0] = "String";
				arguments[1] = split[0];
			}
			else
			{
				arguments[0] = split[0];
				arguments[1] = "arg" + argIndex;
			}
		}

		String parameterSignature = String.format("%s%s %s", isFinal ? FINAL : "",
				arguments[0].trim(), arguments[1].trim());

		log.debug(parameterSignature);

		return parameterSignature;
	}

	private static String getMethodString(Method method) {
		if (Constants.generateClassSignatures == SignatureOptions.PARAMS_ONLY) {
			return method.getName() + "(" + method.getSignature() + ")\n";
		}
		else if (Constants.generateClassSignatures == SignatureOptions.RETURN_ONLY) {
			return method.getName() + ": " + method.getReturnType() + "\n";
		}
		else {
			return method.getName() + "(" + method.getSignature() + "): " + method.getReturnType() + "\n";
		}
	}

	private static String getClassName(JavaClass parsedClass) {
		String result = "";
		if (Constants.generateClassPackage) {
			result += parsedClass.getPackage() + "::";
		}
		result += parsedClass.getName();
		return result;
	}

	private static JavaClass parseFile(String filename) {
		try {
			if (Path.getExtension(filename).equals("java")) {
				return new JpJavaClass(filename);
			}
			else if (Path.getExtension(filename).equals("class")) {
				return new BcelJavaClass(filename);
			}
		} catch (Exception ignored) {}
		return null;
	}
}
