package edu.uncc.sis.aside.visitors;

import java.util.ArrayList;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jface.text.source.Annotation;

import edu.uncc.sis.aside.utils.ASIDEMarkerAndAnnotationUtil;

public class ASTNodeAnnotationVisitor extends ASTVisitor {
	
	private ArrayList<ASTNode> annotatedNodes;
	
	private ASTNode target;
	private ICompilationUnit fCompilationUnit;
	
	public ASTNodeAnnotationVisitor(ASTNode target, ICompilationUnit cu){
		this.target = target;
		this.fCompilationUnit = cu;
		annotatedNodes = new ArrayList<ASTNode>();
	}


	@Override
	public boolean preVisit2(ASTNode node) {
		
		Annotation annotation = ASIDEMarkerAndAnnotationUtil.getAttachedAnnoation(fCompilationUnit, node);
		
		if(annotation != null){
			annotatedNodes.add(node);
		}
		
		return true;
	}
	
	@Override
	public void postVisit(ASTNode node) {
		
		super.postVisit(node);
	}
	
	public void process(){
		target.accept(this);
	}
	
    public ArrayList<ASTNode> getAnnotatedNodes(){
    	return annotatedNodes;
    }	
	
}
