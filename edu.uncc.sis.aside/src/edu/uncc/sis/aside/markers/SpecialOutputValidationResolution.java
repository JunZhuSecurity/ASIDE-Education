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
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.NodeFinder;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
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
import org.eclipse.ui.PartInitException;

import edu.uncc.sis.aside.AsidePlugin;
import edu.uncc.sis.aside.ast.ASTResolving;
import edu.uncc.sis.aside.auxiliary.core.AsideScanOneICompilationUnit;
import edu.uncc.sis.aside.auxiliary.core.CodeGenerator;
import edu.uncc.sis.aside.utils.ASIDEMarkerAndAnnotationUtil;

public class SpecialOutputValidationResolution implements IMarkerResolution,
		IMarkerResolution2 {

	private static final Logger logger = AsidePlugin.getLogManager().getLogger(
			SyntacticValidationResolution.class.getName());

	private ICompilationUnit fCompilationUnit;
	private String fInputType;
	private IMarker fMarker;
	private IProject fProject;

	/**
	 * Constructor for SyntacticValidationResolution
	 * 
	 * @param cu
	 * @param validationRule
	 */
	public SpecialOutputValidationResolution(ICompilationUnit cu, IMarker marker,
			String inputType, IProject project) {
		super();
		fCompilationUnit = cu;
		fInputType = inputType;
		fMarker = marker;
		fProject = project;
	}

	@Override
	public String getDescription() {
		String title, description = "", info = "", content;
		//title = "Selection of this rule will add the following validation routine to your code:";
		if (fInputType == null)
			description = "Description about this rule is not available";
		
		StringBuffer buf = new StringBuffer();
		if(fInputType.equals("SafeString")){
		    description = "getParameter(value) is typically used to obtain outside (user) data which can be exploited with malicious injections of bad characters. Use this to limit input from an outside source (users) to alphabetical characters and numbers only as well as controlling for a buffer overflow vulnerability. ";
		    info = "Note, special characters are not allowed (&gt;.@&, etc) and if they are entered, an exception will be thrown that must be handled by the developer (e.g., place in the validation catch response.sendRedirect(\"Login.jsp\"); and an accompanying message to the user that their attempt was unsuccessful)"; 
		}else if(fInputType.equals("HTTPParameterValue")){
			description = "getParameter(value) is typically used to obtain outside (user) data which can be exploited with malicious injections of bad characters. Use this to limit HTTP input from an outside source (users) to alphabetical characters, numbers and the special characters .-/+=_ !$*?@";
			info = "Note, if malicious characters are entered, an exception will be thrown that must be handled by the developer (e.g., place in the validation catch response.sendRedirect(\"Login.jsp\"); and an accompanying message to the user that their attempt was unsuccessful)";
		}
		else if(fInputType.equals("HTTPServletPath")){
			description = "Servlet path information (e.g., request.getServletPath()) uses a limited amount of characters and is susceptible to malicious characters. Use this filter when obtaining HttpServletPath information.";
			info = "Note, if malicious characters are entered, an exception will be thrown that must be handled by the developer (e.g., place in the validation catch response.sendRedirect(\"Login.jsp\"); and an accompanying message to the user that their attempt was unsuccessful)";
	}else if(fInputType.equals("URL")){
			description = "Ensure that all provided URL input follow appropriate protocol-host-port format. If malicious input is found, an exception will be thrown that must be handled by the developer (e.g., place in the validation catch response.sendRedirect(\"Login.jsp\"); and an accompanying message to the user that their attempt was unsuccessful)";
			info = "";
	}
		else if(fInputType.equals("CreditCard")){
			description = "CreditCard Validation will only allow credit card numbers in the form of xxxx.xxxx.xxxx.xxxx. Anything else will throw an exception that must be handled by the developer (e.g., place in the validation catch response.sendRedirect(\"Login.jsp\"); and an accompanying message to the user that their attempt was unsuccessful)";
			info = "";
	}else if(fInputType.equals("Email")){
			description = "eMail Validation will only allow addresses in the form of foo@foo.foo . Anything else will throw an exception that must be handled by the developer (e.g., place in the validation catch response.sendRedirect(\"Login.jsp\"); and an accompanying message to the user that their attempt was unsuccessful)";
			info = "";
	}else if(fInputType.equals("SSN")){
		description = "SSN Validation will only allow numbers in the form of xxx-xx-xxxx. Anything else will throw an exception that must be handled by the developer (e.g., place in the validation catch response.sendRedirect(\"Login.jsp\"); and an accompanying message to the user that their attempt was unsuccessful)";
	}

		content = description + "<p><p>" + info;
		buf.append(content);
		return buf.toString();
	}

	@Override
	public Image getImage() {

		if (fInputType.equalsIgnoreCase("URL")
				|| fInputType.equalsIgnoreCase("email")) {
			return AsidePlugin.getImageDescriptor("icons/globecompass.png")
					.createImage();
		}

		return AsidePlugin.getImageDescriptor("icons/pyramid.png")
				.createImage();
	}

	@Override
	public String getLabel() {
		String labelStr = "Filter HttpServletPath";
		if(fInputType.equals("SafeString"))
			labelStr= "Filter String Input";
		else if(fInputType.equals("HttpServletPath"))
			labelStr= "Filter HttpServletPath";
		else if(fInputType.equals("HTTPParameterValue"))
			labelStr= "Filter HTTPParameterValue";
		else if(fInputType.equals("URL"))
			labelStr= "Filter URLs";
		else if(fInputType.equals("CreditCard"))
			labelStr= "CreditCard Validation";
		else if(fInputType.equals("Email"))
			labelStr= "eMail Validation";
		else if(fInputType.equals("SSN"))
			labelStr=  "SSN Validation";
		return labelStr;
	}

	@Override
	public void run(IMarker marker) {
	    DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		  
	    //get current date time with Date()
		Date date = new Date();
	    logger.info(dateFormat.format(date)+ " " + AsidePlugin.getUserId() + " chose input validation rule <validation>" + fInputType
				+ "<validation> at "
				+ marker.getAttribute(IMarker.LINE_NUMBER, -1) + " in "
				+ fCompilationUnit.getElementName());

		try {
			String returnTypeOfMethodDeclarationStr = (String) marker.getAttribute("edu.uncc.sis.aside.marker.returnTypeOfMethodDeclarationBelongTo");
			
			CompilationUnit astRoot = ASTResolving.createQuickFixAST(
					fCompilationUnit, null);
			ImportRewrite fImportRewrite = ImportRewrite.create(astRoot, true);

			int offset = (int) fMarker.getAttribute(IMarker.CHAR_START, -1);
			int length = (int) fMarker.getAttribute(IMarker.CHAR_END, -1)
					- offset;

			ASTNode node = NodeFinder.perform(astRoot, offset, length);
			if (node == null) {
				System.out.println("node is null after NodeFinder in SpecialOutputValidationResolution");
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
//generateValidationRoutine returns the new node of request.getParameter() embraced by the validation code
			int nodeStartPosition = node.getStartPosition();
			int nodeLength = node.getLength();
			
			ASTNode newNode;
			if(node==null)
				System.out.println("node is null in SyntacticValidation. start="+offset+" length="+length+"in generateSpecialOutputValidationRoutine");
//			newNode = CodeGenerator.getInstance().generateValidationRoutine(document,
//					astRoot, fImportRewrite, ast, node, fInputType);
			///////////
//			ITrackedNodePosition replacementPositionTracking = null;
			ASIDEMarkerAndAnnotationUtil.deleteMarkerAtPosition(marker);
			boolean result = CodeGenerator.getInstance().generateSpecialOutputValidationCode(document,
					astRoot, fImportRewrite, ast, node, fInputType, returnTypeOfMethodDeclarationStr);
			if(result==false){
				System.out.println("generateSpecialOutputValidationCode does not run properly!");
			}

			new AsideScanOneICompilationUnit(fProject, fCompilationUnit);

		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (MalformedTreeException e) {
			e.printStackTrace();
		} catch (PartInitException e) {
			e.printStackTrace();
		} catch (JavaModelException e) {
			e.printStackTrace();
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 

	}
}
