package edu.uncc.sis.aside.visitors;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SimpleName;

import edu.uncc.sis.aside.ast.ASTResolving;
import edu.uncc.sis.aside.preferences.PreferencesSet;
import edu.uncc.sis.aside.utils.ASIDEMarkerAndAnnotationUtil;

/**
 * 
 * @author Jun Zhu (jzhu16 at uncc dot edu) <a href="http://www.uncc.edu/">UNC
 *         Charlotte</a>
 * 
 */
public class FieldDeclarationForPojoVisitor extends ASTVisitor {

	private CompilationUnit acceptor;
	private ICompilationUnit cu;

	private Map<FieldDeclaration, ArrayList<SimpleName>> attributeMap;
    private ArrayList<SimpleName> simpleNameList;

	private PreferencesSet prefSet;

	/**
	 * Constructor
	 * 
	 * @param compilationUnit
	 * @param _existingMarkersMapForICompilationUnit
	 * @param cu
	 * @param prefSet
	 */
	public FieldDeclarationForPojoVisitor(
			CompilationUnit compilationUnit,
			
			ICompilationUnit cu,PreferencesSet prefSet) {
		super();
		acceptor = compilationUnit;
		this.cu = cu;

		if (attributeMap == null) {
			attributeMap = new HashMap<FieldDeclaration, ArrayList<SimpleName>>();
		}
		simpleNameList = new ArrayList<SimpleName>();
		this.prefSet = prefSet;
	}

	@Override
	public void endVisit(FieldDeclaration node) {

		attributeMap.put(node, simpleNameList);
		super.endVisit(node);
	}

	@Override
	public boolean visit(FieldDeclaration node) {

		simpleNameList = ASTResolving.getSimpleNameListForFieldDeclaration(node); 
		
		return false;
	}

	public Map<FieldDeclaration, ArrayList<SimpleName>> process() {
		if (acceptor != null)
			acceptor.accept(this);
		return attributeMap;
	}

}
