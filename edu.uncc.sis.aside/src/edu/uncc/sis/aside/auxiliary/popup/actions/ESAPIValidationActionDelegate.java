package edu.uncc.sis.aside.auxiliary.popup.actions;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.NodeFinder;
import org.eclipse.jdt.core.dom.rewrite.ImportRewrite;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchSite;

import edu.uncc.sis.aside.ast.ASTResolving;
import edu.uncc.sis.aside.auxiliary.core.CodeGenerator;
import edu.uncc.sis.aside.utils.ASIDEMarkerAndAnnotationUtil;

public class ESAPIValidationActionDelegate implements IEditorActionDelegate {

	private IEditorPart editorPart;

	@Override
	public void setActiveEditor(IAction action, IEditorPart targetEditor) {
		this.editorPart = targetEditor;
	}

	@Override
	public void run(IAction action) {

		IWorkbenchSite site = editorPart.getSite();

		if (site == null)
			return;
		ISelectionProvider provider = site.getSelectionProvider();
		if (provider == null)
			return;
		ISelection selection = provider.getSelection();
		if (selection == null || selection.isEmpty())
			return;
		if (selection instanceof ITextSelection) {
			ITextSelection tSelection = (ITextSelection) selection;
			String key = "";// TODO this key should be passed from the user's menu selection

			processSelection(tSelection, key);
		}

	}

	@Override
	public void selectionChanged(IAction action, ISelection selection) {

	}

	private void processSelection(ITextSelection tSelection, String key) {

		IEditorInput editorInput = editorPart.getEditorInput();
		IJavaElement javaElement = JavaUI
				.getEditorInputJavaElement(editorInput);

		ICompilationUnit compilationUnit = (ICompilationUnit) javaElement;
		CompilationUnit astRoot = parse(compilationUnit);

		// only when the selected text is a string type should it be further
		// processed
		verifyTextSelection(tSelection, astRoot, key);
	}

	private void verifyTextSelection(ITextSelection tSelection, CompilationUnit root, String key) {
		int start = tSelection.getOffset();
		int length = tSelection.getLength();

		ASTNode node = NodeFinder.perform(root, start, length);
		if(node == null){
			System.out.println("no ASTNode is found for the selected text in ESAPIValidationActionDelegate");
			return;
		}
		
		int type = node.getNodeType();
		if(type != ASTNode.SIMPLE_NAME){
			System.out.println("selected text is not a simple name in ESAPIValidationActionDelegate");
			return;
		}
		
		ImportRewrite fImportRewrite = ImportRewrite.create(root, true);
		
		MethodDeclaration declaration = ASTResolving
		.findParentMethodDeclaration(node);
		
		if (declaration == null) {
			return;
		}

		Block body = declaration.getBody();

		AST ast = body.getAST();
		
		IDocument document = JavaUI.getDocumentProvider().getDocument(
				editorPart.getEditorInput());
		MethodDeclaration methodDeclaration = ASTResolving.findParentMethodDeclaration(node);
		String returnTypeOfMethodDeclarationStr = ASIDEMarkerAndAnnotationUtil.getReturnTypeStr(methodDeclaration);
		
		CodeGenerator.getInstance().generateValidationCodeAndAddASIDE_Flag(document, root, fImportRewrite, ast, node, key, returnTypeOfMethodDeclarationStr);
	}

	private CompilationUnit parse(ICompilationUnit cu) {

		ASTParser parser = ASTParser.newParser(AST.JLS3);
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		parser.setResolveBindings(true);
		parser.setSource(cu);
		return (CompilationUnit) parser.createAST(null);
	}


}
