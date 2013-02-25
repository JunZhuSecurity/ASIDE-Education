package edu.uncc.sis.aside.markers;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.NodeFinder;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.rewrite.ITrackedNodePosition;
import org.eclipse.jdt.core.dom.rewrite.ImportRewrite;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.swt.graphics.Image;
import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IMarkerResolution;
import org.eclipse.ui.IMarkerResolution2;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.texteditor.AbstractTextEditor;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;

import edu.uncc.sis.aside.AsidePlugin;
import edu.uncc.sis.aside.ast.ASTResolving;
import edu.uncc.sis.aside.auxiliary.core.AsideScanOneICompilationUnit;
import edu.uncc.sis.aside.auxiliary.core.CodeGenerator;
import edu.uncc.sis.aside.constants.PluginConstants;
import edu.uncc.sis.aside.utils.ASIDEMarkerAndAnnotationUtil;

public class EncodingResolution implements IMarkerResolution,
		IMarkerResolution2 {

	private static final Logger logger = AsidePlugin.getLogManager().getLogger(
			EncodingResolution.class.getName());
	private IProject project;
	private ICompilationUnit fCompilationUnit;
	private CompilationUnit astRoot;
	private String fStrategyType;
	private String esapiEncoderMethodName;

	public EncodingResolution(ICompilationUnit cu, String strategyType) {
		super();
		fCompilationUnit = cu;
		fStrategyType = strategyType;
		esapiEncoderMethodName = "encodeFor" + fStrategyType;
		
		IJavaProject javaProject = cu.getJavaProject();
		if (javaProject != null) {
			project = (IProject) javaProject.getAdapter(IProject.class);
		}
	}

	@Override
	public String getDescription() {
		StringBuffer buf = new StringBuffer();
		String instruction = "-- Double click selection to auto-generate encoding method --";
		String info = "", description = "", content = "";
	    String end = "Follow the \"About this Output Encoding Issue Website Link\" for more information about why the selected method invocation needs encoding.";
		
	    if(fStrategyType.equals(PluginConstants.ENCODING_TYPES[0])){
			description = "Use CSS Encoding when the data you get from an outside source (i.e., the user) is to be written directly into your Cascading Style Sheet or your style tag. Doing so will encode (“clean”) malicious characters so an attacker can not easily exploit CSS interpreter vulnerabilities.";
			info = "&lt;style&gt; {property: ...DON’T TRUST OUTSIDE DATA IN HERE...} &lt;/style&gt;";
		}else if(fStrategyType.equals(PluginConstants.ENCODING_TYPES[1])){
			description = "Use HTML Encoding when the data you get from an outside source (i.e., the user) is to be written directly into your HTML body. Characters / statements such as &lt;script&gt; will be cleansed to &amp;lt;script&amp;gt; which the browser then interprets as the word “&lt;script&gt;” rather than the command to run a script.";
			info = "&lt;body&gt;...DON’T TRUST OUTSIDE DATA IN HERE...&lt;/body&gt;";
		}else if(fStrategyType.equals(PluginConstants.ENCODING_TYPES[2])){
			description = "Attributes provide information about an element (e.g., align=”center”) and use a minimal set of characters. Use the HTML Attribute Encoder when attributes you use in your HTML do not come from your HTML page to safeguard from dangerous characters.";
			info = "&lt;div> attr=...DON’T TRUST OUTSIDE DATA IN HERE...&gt;content&lt;/div&gt;";
		}else if(fStrategyType.equals(PluginConstants.ENCODING_TYPES[3])){
			description = "Use JavaScript encoder when you are using a script that comes from some source outside of your page or when you obtain input from the user that will be put into the script (e.g., popup message displaying a username that the user provided). The encoder changes potentially dangerous characters into usable safe ones.";
			info = "&lt;script&gt;alert(‘...DON’T TRUST OUTSIDE DATA IN HERE...’)&lt;/script&gt;";
		}

		content = instruction + "<p><p>" + description + "<p><p>" + info + "<p><p>" + end;
		buf.append(content);
		
		return buf.toString();		
	}

	@Override
	public Image getImage() {
		return AsidePlugin.getImageDescriptor("icons/devil.png").createImage();
	}

	@Override
	public String getLabel() {
		if(fStrategyType.equals("HTML"))
			return "02 - HTML Encoder";
		else if(fStrategyType.equals("HTMLAttribute"))
		    return "03 - HTML Attribute Encoder";
		else if(fStrategyType.equals("JavaScript"))
		    return "04 - JavaScript Encoder";
		else if(fStrategyType.equals("CSS"))
		    return "05 - CSS Encoder";
		else
			return "";
	}

	@Override
	public void run(IMarker marker) {
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		  
	    //get current date time with Date()
		Date date = new Date();
	    logger.info(dateFormat.format(date) + " " + AsidePlugin.getUserId() + " chose output encoding rule " + fStrategyType + " at "
				+ marker.getAttribute(IMarker.LINE_NUMBER, -1)
				+ " in java file <<"
				+ fCompilationUnit.getElementName() + ">> in Project [" + project.getName() + "]");
		try {
			astRoot = ASTResolving.createQuickFixAST(fCompilationUnit, null);
			ImportRewrite fImportRewrite = ImportRewrite.create(astRoot, true);

			int offset = (int) marker.getAttribute(IMarker.CHAR_START, -1);
			int length = (int) marker.getAttribute(IMarker.CHAR_END, -1)
					- offset;

			ASTNode node = NodeFinder.perform(astRoot, offset, length);

			if (!(node instanceof Expression)) {
				return;
			}

			MethodDeclaration declaration = ASTResolving
					.findParentMethodDeclaration(node);

			if (declaration == null) {
				return;
			}

			Block body = declaration.getBody();

			AST ast = body.getAST();

			IEditorPart part = JavaUI
					.openInEditor(fCompilationUnit, true, true);

			if (part == null) {
				return;
			}

			IEditorInput input = part.getEditorInput();

			if (input == null)
				return;

			IDocument document = JavaUI.getDocumentProvider()
					.getDocument(input);
			//System.out.println("node start="+offset + " length="+length);
			
			/*
			 * replace the marker with annotation to signify that this node has
			 * been validated
			 */
//			ASIDEMarkerAndAnnotationUtil.replaceMarkerWithAnnotation(
//					fCompilationUnit, marker);
			Statement statementStayIn = ASTResolving.findParentStatement(node);
			int statementStartPosition = statementStayIn.getStartPosition();
			
			ITrackedNodePosition replacementPositionTracking = null;
			replacementPositionTracking = CodeGenerator.getInstance().generateEncodingRoutine(document,
					fImportRewrite, ast, declaration, (Expression) node,
					esapiEncoderMethodName);
			
//			if(replacementPositionTracking == null){
//				System.out.println("replacementPositionTracking == null in EncodingResolution");
//				return;
//			}
			//System.out.println("replacementPositionTracking start="+replacementPositionTracking.getStartPosition() + " length="+replacementPositionTracking.getLength());
			ASIDEMarkerAndAnnotationUtil.createAnnotationAtPosition(fCompilationUnit, replacementPositionTracking.getStartPosition(), replacementPositionTracking.getLength());
			
//			int startPosition = replacementPositionTracking.getStartPosition();
//			int nodeLength = replacementPositionTracking.getLength();
//			CompilationUnit newAstRoot = ASTResolving.createQuickFixAST(
//					fCompilationUnit, null);
//			ASTNode tmpNode = NodeFinder.perform(newAstRoot, startPosition, nodeLength);
//			Statement tmpStatement = ASTResolving.findParentStatement(tmpNode);
//			if(tmpStatement == null){
//				System.out.println("tmpStatement == null in EncodingResolution!");
//			}
//			int realLineNum = newAstRoot.getLineNumber(tmpStatement.getStartPosition());
//			int mStartPosition = tmpStatement.getStartPosition();
			//insert comments with IDocument
//			 IWorkbench wb = PlatformUI.getWorkbench();
//			   IWorkbenchWindow win = wb.getActiveWorkbenchWindow();
//			   IWorkbenchPage page = win.getActivePage();
//			   IEditorPart tmpPart = page.getActiveEditor();
//			   if (!(tmpPart instanceof AbstractTextEditor))
//			      return;
//			   ITextEditor editor = (ITextEditor)tmpPart;
//			   IDocumentProvider dp = editor.getDocumentProvider();
//			   IDocument doc = dp.getDocument(editor.getEditorInput());
			   int tmpOffset = statementStartPosition;//doc.getLineOffset(doc.getNumberOfLines()-4);
			   try {
				document.replace(tmpOffset, 0, PluginConstants.ESAPI_ENCODING_COMMENTS);
			} catch (BadLocationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			CompilationUnit newAstRoot = ASTResolving.createQuickFixAST(fCompilationUnit, null);
				ImportRewrite newImportRewrite = ImportRewrite.create(newAstRoot, true);
				CodeGenerator.getInstance().insertEncodingImports(document, newImportRewrite);
						
			IProject fProject = ASIDEMarkerAndAnnotationUtil.getProjectFromICompilationUnit(fCompilationUnit);
			
			new AsideScanOneICompilationUnit(fProject, fCompilationUnit);
			
		} catch (JavaModelException e) {
			e.printStackTrace();
		} catch (PartInitException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (CoreException e) {
			e.printStackTrace();
		} catch (MalformedTreeException e) {
			e.printStackTrace();
		} catch (BadLocationException e) {
			e.printStackTrace();
		}

	}

}
