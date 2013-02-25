package edu.uncc.sis.aside.domainmodels;

import java.io.File;
import java.sql.PreparedStatement;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.w3c.dom.Document;

import edu.uncc.sis.aside.AsidePlugin;
import edu.uncc.sis.aside.constants.PluginConstants;
import edu.uncc.sis.aside.preferences.IPreferenceConstants;
import edu.uncc.sis.aside.preferences.PreferencesSet;
import edu.uncc.sis.aside.utils.ASIDEMarkerAndAnnotationUtil;
import edu.uncc.sis.aside.utils.Converter;
import edu.uncc.sis.aside.utils.Validator;
import edu.uncc.sis.aside.xml.TrustBoundariesReader;
import edu.uncc.sis.aside.xml.XMLFileUtil;

public final class TrustBoundaryRepository {

	private static final String TRUST_BOUNDARY_RULESET_PATH = PluginConstants.USER_DEFINED_ASIDE_RULES_Folder + IPath.SEPARATOR + "trust-boundaries.xml";

	private static TrustBoundaryRepository handler;
	private static LinkedList<Document> boundaryFileDocuments = new LinkedList<Document>();

	private static LinkedList<TrustBoundary> inventory = null;
	//For current rules pack, only deal with one argument, encode it or validate it 
	private int[] arguments = new int[0]; 
	private String[] attrTypes;

	private IProject fProject;

	//newly added attributes
	private String ruleNameBelongTo;
	//newly added attributes, Feb. 16
	private String typeNameBelongTo;
	// When to update this inventory and how? see ASTView

	private TrustBoundaryRepository(IProject project, PreferencesSet prefSet) {
    //System.out.println("TRUST_BOUNDARY_RULESET_PATH " + TRUST_BOUNDARY_RULESET_PATH);
		fProject = project;

		if (inventory == null || prefSet != null) {

			inventory = new LinkedList<TrustBoundary>();
			boundaryFileDocuments.clear();
			checkTrustBoundaryPreferences(prefSet);
			Iterator<Document> iterator = boundaryFileDocuments.iterator();
			while (iterator.hasNext()) {
				Document boundaryFileDocument = iterator.next();
				LinkedList<TrustBoundary> boundaries = TrustBoundariesReader
						.getInstance().getTrustBoundaries(boundaryFileDocument);
				inventory.addAll(boundaries);
			}

		}
	}

	public String getRuleNameBelongTo() {
		return ruleNameBelongTo;
	}

	public void setRuleNameBelongTo(String ruleNameBelongTo) {
		this.ruleNameBelongTo = ruleNameBelongTo;
	}
	

	public String getTypeNameBelongTo() {
		return typeNameBelongTo;
	}

	public void setTypeNameBelongTo(String typeNameBelongTo) {
		this.typeNameBelongTo = typeNameBelongTo;
	}

	public static TrustBoundaryRepository getHandler(IProject project,
			PreferencesSet prefSet) {
		// TODO repeatedly build repository while prefSet is not null, severely
		// decreases performance
		if (handler == null){
			handler = new TrustBoundaryRepository(project, prefSet);
		}else if (prefSet != null) {
			handler = new TrustBoundaryRepository(project, prefSet);
		}
		return handler;
	}

	public boolean isExist(IMethodBinding methodBinding,
			boolean isMethodInvocation, String returnClass) {
		ITypeBinding classBinding = null;
		String declarationClass = null;
		String methodName = null;
//test use
//		HttpServletRequest request = null;
//		String st = request.getParameter("test");
//		st = "t";
//		System.out.println(st);
//		System.out.println("test");
		
		//test use
		if (isMethodInvocation) {
			// method invocation
			classBinding = methodBinding.getDeclaringClass();
			if (classBinding == null) {
				return false;
			}
			declarationClass = classBinding.getQualifiedName();
			if (declarationClass == null) {
				return false;
			}
			methodName = methodBinding.getName();
			// check the return type
			if (!returnClass.equals("void")) {

				// use iterator to avoid ConcurrentModificationException
				synchronized (inventory) {
					Iterator<TrustBoundary> iterator = inventory.iterator();
					while (iterator.hasNext()) {
						TrustBoundary entry = iterator.next();

						if (entry instanceof ReturnedTrustBoundary
								&& entry.getMethodType().equals(
										"MethodInvocation")) {

							ReturnedTrustBoundary _entry = (ReturnedTrustBoundary) entry;
							String entryName = _entry.getMethodName();
							String entryClass = _entry.getDeclarationClass();
							String entryReturn = _entry.getReturnClass();
							if (entryName.equals(methodName)
									&& Validator.validate(entryClass,
											declarationClass)
									&& Validator.validate(entryReturn,
											returnClass)) {
								attrTypes = entry.getAttrTypes();
							
								//Feb. 16, in current version, only one attriType for each rule.
								typeNameBelongTo = Converter.getTypeNameBelongTo(attrTypes);
									
								ruleNameBelongTo = Converter.removeSlash(entryClass+"."+entryName);
								return true;
							}
						}
					}
				}
			} else if (returnClass.equals("void")) {
				// output encoding trust boundaries, or PreparedStatement.setXX() (the later one needs validation)
				synchronized (inventory) {
					Iterator<TrustBoundary> iterator = inventory.iterator();
					while (iterator.hasNext()) {
						TrustBoundary entry = iterator.next();
						if (entry instanceof NonReturnedTrustBoundary
								&& entry.getMethodType().equals(
										"MethodInvocation")) {
							NonReturnedTrustBoundary _entry = (NonReturnedTrustBoundary) entry;
							String entryName = _entry.getMethodName();
							String entryClass = _entry.getDeclarationClass();
							int[] argumentIndice = _entry.getArgumentIndice();
							if (argumentIndice.length == 0)
								continue;

							int maxIndex = getMaxIndex(argumentIndice);
							int length = (methodBinding.getParameterTypes()).length;

							if (maxIndex > length || maxIndex == length)
								continue;

							if (!entryName.equals(methodName))
								continue;
							
							if (Validator
									.validate(entryClass, declarationClass)) {
								attrTypes = entry.getAttrTypes();
								arguments = argumentIndice;
								//Feb. 16, in current version, only one attriType for each rule.
								typeNameBelongTo = Converter.getTypeNameBelongTo(attrTypes);
								ruleNameBelongTo = Converter.removeSlash(entryClass+"."+entryName);

								return true;
							}
						}
					}
				}
			} 
			///////////
			else if (PluginConstants.JAVA_MAP_TYPES.contains(returnClass)
					|| PluginConstants.JAVA_LIST_TYPES.contains(returnClass)) {
				//for the methods with return type of map and list, the program has not support these cases.
				//we need to come up with solutions on how to validate the map and list.
				
				// Complex datastructures such as: java.util.Map, java.util.List
				Iterator<TrustBoundary> iterator = inventory.iterator();
				while (iterator.hasNext()) {
					TrustBoundary entry = iterator.next();
					if (entry instanceof ReturnedTrustBoundary
							&& entry.getMethodType().equals("MethodInvocation")) {
						ReturnedTrustBoundary _entry = (ReturnedTrustBoundary) entry;
						String entryName = _entry.getMethodName();
						String entryClass = _entry.getDeclarationClass();
						String entryReturn = _entry.getReturnClass();
						if (entryName.equals(methodName)
								&& Validator.validate(entryClass,
										declarationClass)
								&& Validator.validate(entryReturn, returnClass)) {
							attrTypes = entry.getAttrTypes();
							Converter.removeSlash(entryClass+"."+entryName);
							return true;
						}
					}
				}
			}
			/////////////the block of else if (PluginConstants.JAVA_MAP_TYPES.contains(returnClass)
			//|| PluginConstants.JAVA_LIST_TYPES.contains(returnClass)) is set not to be run, the case has
			//been supported yet.

		} else {
			// TODO method declaration
			classBinding = methodBinding.getDeclaringClass();
			if (classBinding == null) {
				return false;
			}
			declarationClass = classBinding.getQualifiedName();
			if (declarationClass == null) {
				return false;
			}
			methodName = methodBinding.getName();
			// TODO

		}

		return false;
	}

	private int getMaxIndex(int[] argumentIndice) {
		int max = 0;

		for (int index : argumentIndice) {
			if (max < index) {
				max = index;
			}
		}

		return max;
	}

	private void checkTrustBoundaryPreferences(PreferencesSet prefSet) {

		if (prefSet != null) {
			boolean asideRulesChecked = prefSet.isAside_check();
			if (asideRulesChecked) {
				// add default ruleset into file path array
				Document defaultRulesDocument = XMLFileUtil
						.getDefaultRuleSetDocument(PluginConstants.TB);
				if (defaultRulesDocument != null) {
					boundaryFileDocuments.add(defaultRulesDocument);
				}
			}

			boolean projectRulesChecked = prefSet.isProject_check();
			if (projectRulesChecked) {

				if (fProject != null) {
					IPath fullPath = fProject.getLocation();
					IPath newPath = fullPath
							.append(TRUST_BOUNDARY_RULESET_PATH);
					File rulesFile = newPath.toFile();

					if (rulesFile != null && rulesFile.exists()) {

						Document projectRulesDocument = XMLFileUtil
								.getCustomizedRuleSetDocument(rulesFile);
						if (projectRulesDocument != null) {
							boundaryFileDocuments.add(projectRulesDocument);
						}
					}
				}

			}

			boolean externalRulesChecked = prefSet.isExternal_check();
			if (externalRulesChecked) {
				String[] elements = prefSet.getPath_items();

				for (String element : elements) {
					Document doc = XMLFileUtil
							.getCustomizedRuleSetDocument(element);
					boundaryFileDocuments.add(doc);
				}
			}
		} else if (prefSet == null) {
			boolean asideRulesChecked = AsidePlugin.getDefault()
					.getPreferenceStore()
					.getBoolean(IPreferenceConstants.ASIDE_TB_PREFERENCE);
			if (asideRulesChecked) {
				// add default ruleset into file path array
				Document defaultRulesDocument = XMLFileUtil
						.getDefaultRuleSetDocument(PluginConstants.TB);
				if (defaultRulesDocument != null) {
					boundaryFileDocuments.add(defaultRulesDocument);
				}
			}

			boolean projectRulesChecked = AsidePlugin.getDefault()
					.getPreferenceStore()
					.getBoolean(IPreferenceConstants.PROJECT_TB_PREFERENCE);
			if (projectRulesChecked) {

				if (fProject != null) {
					IPath fullPath = fProject.getLocation();
					IPath newPath = fullPath
							.append(TRUST_BOUNDARY_RULESET_PATH);
					File rulesFile = newPath.toFile();

					if (rulesFile != null && rulesFile.exists()) {

						Document projectRulesDocument = XMLFileUtil
								.getCustomizedRuleSetDocument(rulesFile);
						if (projectRulesDocument != null) {
							boundaryFileDocuments.add(projectRulesDocument);
						}
					}
				}

			}

			boolean externalRulesChecked = AsidePlugin.getDefault()
					.getPreferenceStore()
					.getBoolean(IPreferenceConstants.EXTERNAL_TB_PREFERENCE);
			if (externalRulesChecked) {
				String[] elements = AsidePlugin.getDefault()
						.getTBPathsPreference();

				for (String element : elements) {
					Document doc = XMLFileUtil
							.getCustomizedRuleSetDocument(element);
					boundaryFileDocuments.add(doc);
				}
			}
		}

	}

	public String[] getAttrTypes() {
		return attrTypes;
	}

	public int[] getArguments() {
		return arguments;
	}

}
