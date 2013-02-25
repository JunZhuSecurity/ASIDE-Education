package edu.uncc.sis.aside.utils;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;

public class Converter {

	private static final String DELIMITER = "\\p{Punct}";

	public static String arrayToString(String[] stringArray) {
		String output = "";
		
		if(stringArray == null){
			return output;
		}
		
		for (int i = 0; i < stringArray.length; i++) {
			if (i == stringArray.length - 1) {
				output = output.concat(stringArray[i]);
			} else {
				output = output.concat(stringArray[i]).concat("|");
			}
		}
		return output;
	}

	public static String arrayToString(int[] intArray) {
		String output = "";
		if(intArray == null){
			return output;
		}
		
        for(int i=0; i<intArray.length; i++){
        	if(i==intArray.length - 1){
        		output = output.concat(String.valueOf(intArray[i]));
        	}else{
        		output = output.concat(String.valueOf(intArray[i])).concat(",");
        	}
        }
		return output;
	}

	public static int[] stringToIntArray(String input) {

		String[] result = input.split(DELIMITER);
		int[] output = new int[result.length];
		for (int i = 0; i < result.length; i++) {
			output[i] = Integer.parseInt(result[i]);
		}
		return output;
	}

	public static String[] stringToStringArray(String input) {
		if(input == null)
			return null;
		
		String[] output = input.split(DELIMITER);
		String[] output_copy = new String[output.length];
		for(int i = 0; i < output.length; i++){
			String trimed_copy = output[i].trim();
			output_copy[i] = trimed_copy;
		}
		
		return output_copy;
	}
	
	public static CompilationUnit parse(ICompilationUnit unit) {

		ASTParser parser = ASTParser.newParser(AST.JLS3);
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		parser.setResolveBindings(true);
		parser.setSource(unit);

		return (CompilationUnit) parser.createAST(null);

	}
	
	public static String removeSlash(String str){
		String tmpStr = "";
		char[] array = str.toCharArray();
		char[] resultArray = new char[array.length];
	    char tmpChar;
	    int k = 0;
		for(int i=0;i<array.length;i++){
			tmpChar = array[i];
			if(tmpChar != '\\'){
				resultArray[k] = tmpChar;
				k++;
			}
		}
		tmpStr = new String(resultArray);
		return tmpStr;
	}

	public static String getTypeNameBelongTo(String[] attrTypes) {
		//Feb. 16, in current version, only one attriType for each rule.
		String typeNameBelongTo = "";
		if(attrTypes.length==1)
		    typeNameBelongTo = attrTypes[0];
		else if(attrTypes.length>1)
			typeNameBelongTo = "attrTypesLargerThanTwoError";
		else
			typeNameBelongTo = "NoAttrTypeError";
		//System.out.println("typeNameBelongTo: "+ typeNameBelongTo);
		return typeNameBelongTo;
	}

	public static String ruleTypeToUrlParam(String ruleNameBelongTo) {
		String ruleTypeUrlParam = "";
		if(ruleNameBelongTo.equals("input") || ruleNameBelongTo.equals("specialOutput"))
			ruleTypeUrlParam = "InputValidation";
		else if(ruleNameBelongTo.equals("output"))
			ruleTypeUrlParam = "OutputEncoding";
		else if(ruleNameBelongTo.equals("warning"))
			ruleTypeUrlParam = "PreparedStatement";
		return ruleTypeUrlParam;
	}
	
	
}
