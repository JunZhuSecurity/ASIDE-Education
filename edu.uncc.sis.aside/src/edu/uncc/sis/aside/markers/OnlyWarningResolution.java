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
import edu.uncc.sis.aside.auxiliary.core.CodeGenerator;
import edu.uncc.sis.aside.constants.PluginConstants;
import edu.uncc.sis.aside.utils.ASIDEMarkerAndAnnotationUtil;

public class OnlyWarningResolution implements IMarkerResolution,
		IMarkerResolution2 {

	private static final Logger logger = AsidePlugin.getLogManager().getLogger(
			SyntacticValidationResolution.class.getName());

	private ICompilationUnit fCompilationUnit;
	private String fInputType;
	private IMarker fMarker;
	private IProject fProject;
    private static int count = 0;
	/**
	 * Constructor for SyntacticValidationResolution
	 * 
	 * @param cu
	 * @param validationRule
	 */
	public OnlyWarningResolution(ICompilationUnit cu, IMarker marker,
			String inputType, IProject project) {
		super();
		fCompilationUnit = cu;
		fInputType = inputType;
		fMarker = marker;
		fProject = project;
	}

	@Override
	public String getDescription() {

		StringBuffer buf = new StringBuffer();

		String title, description, info, content;
		title = "Please do not use dynamic SQL statement! ";
		if (fInputType == null)
			description = "Description about this rule is not available";
        description = "The important thing to remember is to never construct SQL statements " 
			        + "using string concatenation of unchecked input values. Execution of dynamic SQl statement would lead to potential vulnerabilities! "
			        + "<p> "
        	        + "Security experts recommend using parameterized SQL statements!"
			        + "<p> "
        	        + "Example of parameterized SQL statements."
        	        + "<p>"
        	        + " Connection conn = DriverManager.getConnection(protocol + dbName, username, password); "
			        + "<p>"
        	        + " PreparedStatement query;"
			        + "<p>"
			        + " query = conn.prepareStatement(\"UPDATE players SET name = ?, score = ?, active = ? WHERE jerseyNum = ?\");"
			        + "<p>"
			        + " query.setString(1, \"Smith\");"
			        +"<p>"
                    +" query.setInt(2, 42);"
                    +"<p>"
                    +" query.setBoolean(3, true);"
                    +"<p>"
                    +" query.setInt(4, 99);"
                    +"<p>"
                    +" query.executeUpdate()";
        
//		description = "try{"
//				+ "<p>"
//				+ "ESAPI.validator.getValidInput(\"validation context of the input\", \"the input variable to be validated\", "
//				+ fInputType.toString()
//				+ ", \"Max length of the input\", \"whether the input is allowed to be null\");"
//				+ "<p>" + "}catch(ValiationException e){" + "<p>"
//				+ "}catch(IntrusionException e){" + "<p>" + "}";

		String projectPath = fProject.getFullPath().toString();
		info = "";
		content = title + "<p><p>" + description + "<p><p>" + info;
		buf.append(content);
		return buf.toString();
	}

	@Override
	public Image getImage() {

        
		return AsidePlugin.getImageDescriptor("icons/devil.png")
				.createImage();
	}

	@Override
	public String getLabel() {
		return fInputType;
	}

	@Override
	public void run(IMarker marker) {
		IProject project = ASIDEMarkerAndAnnotationUtil.getProjectFromICompilationUnit(fCompilationUnit);
		   DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
			  
		    //get current date time with Date()
			Date date = new Date();
		    logger.info(dateFormat.format(date)+ " " + AsidePlugin.getUserId() + " clicks on <<Warning Message of Dynamic Statement>>"
					+ "in an attempt to get input validated at "
					+ marker.getAttribute(IMarker.LINE_NUMBER, -1) + " in File <<"
					+ fCompilationUnit.getElementName()
					+ ">> in Project =="
					+ project.getName() + "==");
		/*
		 * depends on the type (semantic|syntactic) of rule, marker resolution
		 * should respond with different annotations. But now, we consider only
		 * URL and email that need advanced checking
		 */
		try {
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
			//~~~~~~~~~~ even the developer click this warning label, the marker is still there
			//~~~~~~~~~~the only way to remove the label is to remove this line of code,
			//ASIDEMarkerAndAnnotationUtil.deleteMarkerAtPosition(marker);
//			CodeGenerator.getInstance().generateSpecialOutputValidationCode(document,
//					astRoot, fImportRewrite, ast, node, fInputType);
			
//			if(replacementPositionTracking == null){
//				System.out.println("replacementPositionTracking == null");
//				return;
//			}
//			System.out.println("SpecialOutputValidation---replacementPositionTracking start="+replacementPositionTracking.getStartPosition() + " length="+replacementPositionTracking.getLength());
//			ASIDEMarkerAndAnnotationUtil.createAnnotationAtPosition(fCompilationUnit, replacementPositionTracking.getStartPosition(), replacementPositionTracking.getLength());
//		
			
			//////////////
//			ASIDEMarkerAndAnnotationUtil.setASIDE_Flag(node, "true");
//			newNode = CodeGenerator.getInstance().generateValidationCodeAndAddASIDE_Flag(document,
//					astRoot, fImportRewrite, ast, node, fInputType);
//			ASIDEMarkerAndAnnotationUtil.setASIDE_Flag(node, "true");
//			//ASIDEMarkerAndAnnotationUtil.deleteMarkerAtPosition(marker);
//			System.out.println(ASIDEMarkerAndAnnotationUtil.getASIDE_Flag(node)+" "+node);
			
			//add Annotation using the property in the node.
			//createAnnotationAtPosition();
			/*
			 * replace the marker with annotation to signify that this node has
			 * been validated
			 */
			//ASIDEMarkerAndAnnotationUtil.replaceMarkerWithAnnotation(fCompilationUnit, marker);
			
			
//			CodeGenerator.getInstance().generateTryCode(document,
//					astRoot, fImportRewrite, ast, node, fInputType);
			//newly added
			//my new approach is to set a property named ASIDE_Flag for the ASTNode, 
			//if ASIDE_Flag is true, it means we have generated validation code for 
			//this node. so next scanning would not show the marker for it.
			//ASIDEMarkerAndAnnotationUtil.replaceMarkerWithASIDE_Flag(fComilationUtil,);
			
//			if(newNode==null)
//				return;
			
//			 ASIDEMarkerAndAnnotationUtil.deleteMarkerAndAddAnnotation(fCompilationUnit,
//					marker,newNode);
			//newly added^^^^^^^^^^^^^^
			//AsideScanning asideScanning = new AsideScanning(fProject);

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
