package edu.uncc.sis.aside.visitors;

import java.util.LinkedList;

import org.eclipse.jdt.core.dom.ASTMatcher;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;

import edu.uncc.sis.aside.AsidePlugin;

public class FindNodeListVisitor extends ASTVisitor {

	private ASTNode base, target, match;
	
	private LinkedList<ASTNode> matchedNodeList;

	private ASTMatcher matcher;

	public FindNodeListVisitor(ASTNode base, ASTNode target) {
		this.base = base;
		this.target = target;
		matchedNodeList = new LinkedList<ASTNode>();
		matcher = AsidePlugin.getDefault().getASTMatcher();
	}

	@Override
	public void postVisit(ASTNode node) {
		super.postVisit(node);
	}

	@Override
	public boolean preVisit2(ASTNode node) {

		boolean matched = target.subtreeMatch(matcher, node);

		if (matched) {
			match = node;
			matchedNodeList.add(match);
			//return false;
		}

		return true;
	}

	public void process() {
		base.accept(this);
	}

	public ASTNode getNode() {
		return match;
	}

	public LinkedList<ASTNode> getMatchedNodeList() {
		return matchedNodeList;
	}

	public void setMatchedNodeList(LinkedList<ASTNode> matchedNodeList) {
		this.matchedNodeList = matchedNodeList;
	}

}
