package edu.uncc.sis.aside.domainmodels;

import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;

public class ASTRewriteAndTryCatchFlag {
	ASTRewrite astRewrite;
	int justGeneratedWhichTryCatch;
	public ASTRewriteAndTryCatchFlag(){
		this.astRewrite = null;
		this.justGeneratedWhichTryCatch = 0;
	}
	public ASTRewrite getAstRewrite() {
		return astRewrite;
	}
	public void setAstRewrite(ASTRewrite astRewrite) {
		this.astRewrite = astRewrite;
	}
	public int getJustGeneratedWhichTryCatch() {
		return justGeneratedWhichTryCatch;
	}
	public void setJustGeneratedWhichTryCatch(int justGeneratedWhichTryCatch) {
		this.justGeneratedWhichTryCatch = justGeneratedWhichTryCatch;
	}
	


}
