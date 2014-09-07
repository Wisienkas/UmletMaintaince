package com.umlet.language.java.jp;

import japa.parser.ast.body.ConstructorDeclaration;
import japa.parser.ast.body.ModifierSet;
import japa.parser.ast.body.Parameter;

import java.util.List;

import com.umlet.language.java.Method;

public class JpConstructor implements Method {

	private ConstructorDeclaration constructor;

	public JpConstructor(ConstructorDeclaration constructor) {
		this.constructor = constructor;
	}

	@Override
	/**
	 * Code duplicated in JpMethod&JpField because the extended class 
	 * BodyDeclaration does not provide a getModifiers() method.
	 */
	public AccessFlag getAccess() {
		int modifiers = constructor.getModifiers();
		if ((modifiers & ModifierSet.PUBLIC) != 0) {
			return AccessFlag.PUBLIC;
		}
		else if ((modifiers & ModifierSet.PROTECTED) != 0) {
			return AccessFlag.PROTECTED;
		}
		else if ((modifiers & ModifierSet.PRIVATE) != 0) {
			return AccessFlag.PRIVATE;
		}
		else {
			return AccessFlag.PACKAGE;
		}
	}

	@Override
	public String getName() {
		return constructor.getName();
	}

	@Override
	public String getReturnType() {
		return "ctor";
	}

	@Override
	public String getSignature() {
		List<Parameter> params = constructor.getParameters();
		if (params == null) {
			return "";
		}
		String paramsWithBraces = params.toString();
		return paramsWithBraces.substring(1, paramsWithBraces.length() - 1);
	}
}
