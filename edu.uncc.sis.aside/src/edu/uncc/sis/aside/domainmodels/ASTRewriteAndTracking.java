package edu.uncc.sis.aside.domainmodels;

import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ITrackedNodePosition;

public class ASTRewriteAndTracking {
	public ASTRewrite astRewrite;
	ITrackedNodePosition firstNodePosition;
	ITrackedNodePosition secondNodePosition;
	public ASTRewrite getAstRewrite() {
		return astRewrite;
	}
	public void setAstRewrite(ASTRewrite astRewrite) {
		this.astRewrite = astRewrite;
	}
	public ITrackedNodePosition getFirstNodePosition() {
		return firstNodePosition;
	}
	public void setFirstNodePosition(ITrackedNodePosition firstNodePosition) {
		this.firstNodePosition = firstNodePosition;
	}
	public ITrackedNodePosition getSecondNodePosition() {
		return secondNodePosition;
	}
	public void setSecondNodePosition(ITrackedNodePosition secondNodePosition) {
		this.secondNodePosition = secondNodePosition;
	}
	

}
