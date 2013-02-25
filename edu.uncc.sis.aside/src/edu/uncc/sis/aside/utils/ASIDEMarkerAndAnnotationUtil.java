package edu.uncc.sis.aside.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.core.filebuffers.FileBuffers;
import org.eclipse.core.filebuffers.ITextFileBuffer;
import org.eclipse.core.filebuffers.ITextFileBufferManager;
import org.eclipse.core.filebuffers.LocationKind;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.ToolFactory;
import org.eclipse.jdt.core.dom.ASTMatcher;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CatchClause;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.NodeFinder;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ITrackedNodePosition;
import org.eclipse.jdt.core.formatter.CodeFormatter;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.text.edits.InsertEdit;
import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.ReplaceEdit;
import org.eclipse.text.edits.TextEdit;
import org.eclipse.text.edits.UndoEdit;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IMarkerResolution;
import org.eclipse.ui.PartInitException;

import edu.uncc.sis.aside.AsidePlugin;
import edu.uncc.sis.aside.constants.PluginConstants;
import edu.uncc.sis.aside.visitors.FindNodeVisitor;

/**
 * 
 * @author Jing Xie (jxie2 at uncc dot edu) <a href="http://www.uncc.edu/">UNC
 *         Charlotte</a>
 * 
 */
public class ASIDEMarkerAndAnnotationUtil {

	public static IMarker addMarker(CompilationUnit compilationUnit,
			Map<String, Object> markerAttributes) {

		IMarker marker = null;

		try {

			IJavaElement javaElement = compilationUnit.getJavaElement();
			if (javaElement != null) {
				IFile file = (IFile) javaElement.getCorrespondingResource();
				marker = file.createMarker(PluginConstants.ASIDE_MARKER_TYPE);
				marker.setAttributes(markerAttributes);
			}

		} catch (CoreException e) {
		}

		return marker;
	}

	public static void clearStaleMarkers(ArrayList<IMarker> markers) {
		try {
			for (IMarker marker : markers) {
				if (marker != null && marker.exists()) {
					marker.delete();
				}
			}
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}

	public static boolean hasAnnotationAtPosition(ICompilationUnit cu,
			ASTNode node) {

		int charStart = node.getStartPosition();
		int length = node.getLength();
		ITextFileBufferManager manager = FileBuffers.getTextFileBufferManager();
		ITextFileBuffer buffer = manager.getTextFileBuffer(cu.getPath(),
				LocationKind.IFILE);
		if (buffer == null) {
			return false;
		}

		IAnnotationModel model = buffer.getAnnotationModel();

		Iterator<Annotation> iterator = model.getAnnotationIterator();
		Annotation annotation = null;
		Position position = null;
		while (iterator.hasNext()) {
			annotation = iterator.next();
			if (annotation.getType().equals(
					PluginConstants.ASIDE_ANNOTATION_TYPE)) {
				position = model.getPosition(annotation);
				// newly altered,
				// && position.getLength() == length deleted
				// if (position.getOffset() == charStart
				// && position.getLength() == length) {
				if (position.getOffset() == charStart
						&& position.getLength() == length) {
					// if(position.getLength() != length)
					// System.out.println("position.getLength() != length");
					// System.out.println("position get offset block");
					return true;
				}
			}
		}

		return false;
	}

	public static boolean hasASIDE_FlagAtNode(ASTNode node) {
		if (getASIDE_Flag(node).equals("true"))
			return true;
		else
			return false;
	}

	public static Annotation getAttachedAnnoation(ICompilationUnit cu,
			ASTNode target) {

		ITextFileBufferManager manager = FileBuffers.getTextFileBufferManager();
		ITextFileBuffer buffer = manager.getTextFileBuffer(cu.getPath(),
				LocationKind.IFILE);
		if (buffer == null) {
			return null;
		}

		IAnnotationModel model = buffer.getAnnotationModel();

		Iterator<Annotation> iterator = model.getAnnotationIterator();
		Annotation annotation;
		Position position;
		while (iterator.hasNext()) {
			annotation = iterator.next();
			if (annotation.getType().equals(
					PluginConstants.ASIDE_ANNOTATION_TYPE)) {
				position = model.getPosition(annotation);
				if (position.getOffset() == target.getStartPosition()
						&& position.getLength() == target.getLength()) {
					return annotation;
				}
			}
		}

		return null;
	}

	// /////?
	public static boolean isTainted(MethodInvocation node,
			ArrayList<Expression> taintedMapSources, ASTMatcher astMatcher) {

		for (Expression exp : taintedMapSources) {
			if (exp.getNodeType() == ASTNode.METHOD_INVOCATION) {
				MethodInvocation methodInvocation = (MethodInvocation) exp;
				if (methodInvocation.subtreeMatch(astMatcher, node)) {
					return true;
				}
			}
		}

		return false;
	}

	// /?
	public static boolean isTainted(SimpleName node,
			ArrayList<Expression> taintedMapSources) {
		IBinding binding = node.resolveBinding();

		for (Expression exp : taintedMapSources) {
			if (exp.getNodeType() == ASTNode.SIMPLE_NAME) {
				SimpleName temp = (SimpleName) exp;
				IBinding tempBinding = temp.resolveBinding();
				if (tempBinding != null && binding != null
						&& tempBinding.isEqualTo(binding)) {
					return true;
				}
			}

		}

		return false;
	}

	public static MethodDeclaration getMatchee(MethodDeclaration examinee,
			Set<MethodDeclaration> candidates) {

		ASTMatcher astMatcher = AsidePlugin.getDefault().getASTMatcher();
		for (MethodDeclaration candidate : candidates) {

			if (astMatcher.match(examinee, candidate)) {
				return candidate;
			}
		}
		return null;
	}

	public static boolean isMainEntrance(ICompilationUnit cu,
			MethodDeclaration node) {
		// get the node's signature
		int start = node.getStartPosition();
		try {
			IJavaElement element = cu.getElementAt(start);

			if (element.getElementType() == IJavaElement.METHOD) {
				IMethod method = (IMethod) element;
				if (method.isMainMethod())
					return true;
			}

		} catch (JavaModelException e) {
			e.printStackTrace();
		}

		return false;
	}

	public static boolean isControlConditionStatementWithBlock(ASTNode node) {

		if (node == null)
			return false;

		switch (node.getNodeType()) {
		case ASTNode.IF_STATEMENT:
		case ASTNode.WHILE_STATEMENT:
		case ASTNode.FOR_STATEMENT:
		case ASTNode.DO_STATEMENT:
			return true;
		default:
			return false;
		}
	}

	// methods which use specialCase method need modification,
	// and specialCase method is no use for our new approach
	/**
	 * This represents cases where the method invocation introducing untrusted
	 * input is an argument of the validation API of the validation block
	 * generated by ASIDE. Thus, ASIDE should not report them as places that
	 * need validation. E.g.
	 * 
	 * ESAPI.validator.getValidInput(context, target, inputType, maxLength,
	 * allowNull);
	 * 
	 * @param target
	 *            the method invocation which introduces untrusted input into
	 *            the program.
	 * @return
	 */
	public static boolean specialCase(MethodInvocation target) {

		if (target.getParent() == null) {
			return false;
		}

		ASTNode parent = target.getParent();
		if (parent.getNodeType() != ASTNode.METHOD_INVOCATION) {
			return false;
		}

		MethodInvocation parentMethodInvocation = (MethodInvocation) parent;

		Expression expression = parentMethodInvocation.getExpression();

		if (expression == null) {
			return false;
		}

		if (expression.getNodeType() != ASTNode.SIMPLE_NAME) {
			return false;
		}

		SimpleName invoker = (SimpleName) expression;

		SimpleName name = parentMethodInvocation.getName();

		if (name.getIdentifier().equals("getValidInput")
				&& invoker.getIdentifier().equals("ESAPI.validator")) {
			return true;
		}

		return false;
	}

	// methods which use isFalsePositive method need modification, this method
	// is no use for the new approach
	public static boolean isFalsePositive(Expression argumentNode,
			int argCharStart, int argLength, ICompilationUnit cu) {

		int nodeType = argumentNode.getNodeType();

		switch (nodeType) {
		case ASTNode.STRING_LITERAL:
			return true;
		case ASTNode.NUMBER_LITERAL:
			return true;
		case ASTNode.BOOLEAN_LITERAL:
			return true;
		case ASTNode.TYPE_LITERAL:
			return true;
		case ASTNode.CHARACTER_LITERAL:
			return true;
		default:
			ITypeBinding binding = argumentNode.resolveTypeBinding();
			String qualifiedName = binding.getQualifiedName();

			if (qualifiedName != null)
				return false;
			return true;
		}

	}

	// find the position, replace it,
	// revised!
	public static void replaceMarkerWithAnnotation(ICompilationUnit cu,
			IMarker marker) {

		int offset = marker.getAttribute(IMarker.CHAR_START, -1);
		int length = marker.getAttribute(IMarker.CHAR_END, -1) - offset;

		// Add an annotation to the code
		ITextFileBufferManager manager = FileBuffers.getTextFileBufferManager();
		ITextFileBuffer buffer = manager.getTextFileBuffer(cu.getPath(),
				LocationKind.IFILE);
		if (buffer == null) {
			return;
		}

		IAnnotationModel model = buffer.getAnnotationModel();

		Annotation annotation = new Annotation(
				PluginConstants.ASIDE_ANNOTATION_TYPE, false, null);
		model.addAnnotation(annotation, new Position(offset, length));
		// System.out.println("annotation added");
		try {
			marker.delete();
			// System.out.println("marker.delete");
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}

	public static void deleteMarkerAndAddAnnotation(ICompilationUnit cu,
			IMarker marker, ASTNode node) {
		try {
			marker.delete();
			// System.out.println("marker.delete");
		} catch (CoreException e) {
			e.printStackTrace();
		}
		createAnnotationAtPosition(cu, node);
	}

	public static ICompilationUnit getCompilationUnit(IMarker marker) {
		IResource res = marker.getResource();
		if (res instanceof IFile && res.isAccessible()) {
			IJavaElement element = JavaCore.create((IFile) res);
			if (element instanceof ICompilationUnit)
				return (ICompilationUnit) element;
		}
		return null;
	}

	// methods which use hasASIDEGeneratedValidationRoutine method need
	// modifiation.
	// and hasASIDEGeneratedValidationRoutine method need modification also,
	// some logic is different with our new approach
	// since we decide to use only annotation to judge if there is validation
	// done.
	// so this method is no use any more
	public static boolean hasASIDEGeneratedValidationRoutine(
			ICompilationUnit cu, MethodInvocation targetNode,
			Statement statement) {

		ASTNode parentNode = targetNode.getParent();
		if (parentNode == null)
			return false;
		int parentType = parentNode.getNodeType();
		if (parentType == ASTNode.ASSIGNMENT
				|| parentType == ASTNode.VARIABLE_DECLARATION_FRAGMENT) {
			ASTNode statementParent = statement.getParent();
			if (statementParent == null
					|| statementParent.getNodeType() != ASTNode.BLOCK)
				return false;
			Block parentBlock = (Block) statementParent;
			List<Statement> statements = parentBlock.statements();
			Statement targetStatement = null;
			Iterator<Statement> iterator = statements.iterator();
			while (iterator.hasNext()) {
				Statement element = iterator.next();
				if (element.getNodeType() == ASTNode.LINE_COMMENT) {
					continue;
				} else {
					if (isSame(element, statement)) {
						targetStatement = iterator.next();
						break;
					}
				}

			}

			if (!(targetStatement instanceof TryStatement)) {
				return false;
			}

			TryStatement targetTryStatement = (TryStatement) targetStatement;
			List<CatchClause> catchList = targetTryStatement.catchClauses();
			if (catchList.size() < 2)
				return false;
			boolean validation = false, intrusion = false;
			for (CatchClause clause : catchList) {
				SingleVariableDeclaration exception = clause.getException();
				Type type = exception.getType();
				ITypeBinding binding = type.resolveBinding();
				if (binding != null) {
					String qualifiedName = binding.getQualifiedName();
					if (qualifiedName
							.equals("org.owasp.esapi.errors.ValidationException")) {
						validation = true;
					} else if (qualifiedName
							.equals("org.owasp.esapi.errors.IntrusionException")) {
						intrusion = true;
					}
				}
			}

			if (validation && intrusion)
				return true;
		}

		return false;
	}

	// to judge if there is already validation's tryStatement in the
	// methodDeclaration,
	// if yes, return true, else, return false

	private static boolean isSame(Statement element, Statement statement) {
		int sStartPosition = statement.getStartPosition();
		int sLength = statement.getLength();

		int eStartPosition = element.getStartPosition();
		int eLength = element.getLength();

		if (sStartPosition == eStartPosition && eLength == sLength)
			return true;
		return false;
	}

	// methods which use isConvertedToNumericType method need modification,
	// isConvertedToNumericType method is no use for the new approach
	public static boolean isConvertedToNumericType(Expression node) {

		ASTNode parent = node.getParent();
		int nodeType = parent.getNodeType();

		if (nodeType == ASTNode.METHOD_INVOCATION) {
			MethodInvocation parentMethodInvocation = (MethodInvocation) node;

			IMethodBinding methodBinding = parentMethodInvocation
					.resolveMethodBinding();

			if (methodBinding == null)
				return false;

			ITypeBinding declaringClassBinding = methodBinding
					.getDeclaringClass();
			boolean containClassName = false;
			if (declaringClassBinding != null) {
				String declaringClassQualifiedName = declaringClassBinding
						.getQualifiedName();
				containClassName = PluginConstants.JAVA_NUMERIC_TYPES
						.contains(declaringClassQualifiedName);

				if (methodBinding.isConstructor()) {
					return containClassName;
				}
			}

			// plain method invocation, not constructor
			String methodName = methodBinding.getName();
			boolean containMethod = PluginConstants.JAVA_NUMERIC_METHODS
					.contains(methodName);

			return containClassName && containMethod;
		}

		return false;
	}

	public static IProject getProjectFromICompilationUnit(ICompilationUnit cu) {
		IProject project = null;
		IJavaProject javaProject = cu.getJavaProject();
		if (javaProject != null) {
			project = (IProject) javaProject.getAdapter(IProject.class);
		}
		return project;
	}

	public static IEditorPart getCurrentEditorPart(ICompilationUnit cu) {

		try {
			IEditorPart currentEditor = JavaUI.openInEditor(cu);
			return currentEditor;
		} catch (PartInitException e) {
			e.printStackTrace();
		} catch (JavaModelException e) {
			e.printStackTrace();
		}
		return null;
	}

	// newly added
	public static Annotation getAttachedAnnotation(
			ICompilationUnit fCompilationUnit, ASTNode target) {

		ITextFileBufferManager manager = FileBuffers.getTextFileBufferManager();
		ITextFileBuffer buffer = manager.getTextFileBuffer(
				fCompilationUnit.getPath(), LocationKind.IFILE);
		if (buffer == null) {
			return null;
		}

		IAnnotationModel model = buffer.getAnnotationModel();

		Iterator<Annotation> iterator = model.getAnnotationIterator();
		Annotation annotation;
		Position position;
		while (iterator.hasNext()) {
			annotation = iterator.next();
			if (annotation.getType().equals(
					PluginConstants.ASIDE_ANNOTATION_TYPE)) {
				position = model.getPosition(annotation);
				if (position.getOffset() == target.getStartPosition()
						&& position.getLength() == target.getLength()) {
					return annotation;
				}
			}
		}

		return null;
	}

	public static void createAnnotationAtPosition(
			ICompilationUnit fCompilationUnit, int start, int length) {

		Position newPosition = new Position(start, length);
		Annotation fixedAnnotation = new Annotation(
				PluginConstants.ASIDE_ANNOTATION_TYPE, true, null);
		ITextFileBufferManager manager = FileBuffers.getTextFileBufferManager();
		ITextFileBuffer buffer = manager.getTextFileBuffer(
				fCompilationUnit.getPath(), LocationKind.IFILE);
		IAnnotationModel model = buffer.getAnnotationModel();

		model.addAnnotation(fixedAnnotation, newPosition);
	}

	public static void createAnnotationAtPosition(
			ICompilationUnit fCompilationUnit, ASTNode node) {
		if (node == null)
			return;
		int start = node.getStartPosition();
		int length = node.getLength();
		createAnnotationAtPosition(fCompilationUnit, start, length);

	}

	public static void copyAnnotation(ITrackedNodePosition trackPosition,
			ASTNode node, CompilationUnit astRoot, int extra) {

		IJavaElement javaElement = astRoot.getJavaElement();
		if (javaElement == null) {
			return;
		}

		ICompilationUnit fCompilationUnit = (ICompilationUnit) javaElement
				.getAncestor(IJavaElement.COMPILATION_UNIT);

		int start = trackPosition.getStartPosition() + extra;
		int length = trackPosition.getLength();
		NodeFinder nodeFinder = new NodeFinder(astRoot, start, length);
		ASTNode coveredNode = nodeFinder.getCoveredNode();
		FindNodeVisitor visitor = new FindNodeVisitor(coveredNode, node);
		visitor.process();
		ASTNode match = visitor.getNode();
		if (match != null) {
			createAnnotationAtPosition(fCompilationUnit, match);
		}
	}

	public static void copyAnnotation(
			Map<ITrackedNodePosition, ArrayList<ASTNode>> annotatedStatements,
			CompilationUnit astRoot, int extra) {

		IJavaElement javaElement = astRoot.getJavaElement();
		if (javaElement == null) {
			return;
		}

		ICompilationUnit fCompilationUnit = (ICompilationUnit) javaElement
				.getAncestor(IJavaElement.COMPILATION_UNIT);

		Set<Entry<ITrackedNodePosition, ArrayList<ASTNode>>> entrySet = annotatedStatements
				.entrySet();
		for (Entry<ITrackedNodePosition, ArrayList<ASTNode>> entry : entrySet) {
			ITrackedNodePosition position = entry.getKey();
			int start = position.getStartPosition() + extra;
			int length = position.getLength();
			NodeFinder nodeFinder = new NodeFinder(astRoot, start, length);
			ASTNode coveredNode = nodeFinder.getCoveredNode();

			ArrayList<ASTNode> annotatedNodes = entry.getValue();
			for (ASTNode node : annotatedNodes) {
				FindNodeVisitor visitor = new FindNodeVisitor(coveredNode, node);
				visitor.process();
				ASTNode match = visitor.getNode();
				if (match != null) {
					createAnnotationAtPosition(fCompilationUnit, match);
				}
			}

		}
	}

	public static ASTNode createNodeCopyAndAnnotation(
			ICompilationUnit fCompilationUnit, ASTRewrite astRewrite,
			ASTNode node) {
		createAnnotationAtPosition(fCompilationUnit, node);

		return astRewrite.createCopyTarget(node);
	}

	public static String getASIDE_Flag(ASTNode node) {
		if (node.getProperty("ASIDE_Flag") == null)
			return "false";
		return (String) node.getProperty("ASIDE_Flag");
	}

	public static void setASIDE_Flag(ASTNode node, String value) {
		if (value.equals("true") || value.equals("false"))
			node.setProperty("ASIDE_Flag", value);
		else {
			System.out
					.println("Wrong parameter value for setASIDE_Flag method in setASIDE_Flag");
			return;
		}
	}

	public static ASTNode createCopyAndAddASIDE_Flag(ASTRewrite fASTRewrite,
			ASTNode node) {
		ASTNode tmpNode = fASTRewrite.createCopyTarget(node);
		// setASIDE_Flag(tmpNode, true);
		return tmpNode;
	}

	public static void deleteMarkerAtPosition(IMarker marker) {
		try {
			marker.delete();
			// System.out.println("marker.delete");
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}

	public static ASTNode findTheRightNodeFromList(
			LinkedList<ASTNode> nodeList, int startPosition) {
		ASTNode tmpNode, resultNode = null;
		int tmpStartPosition = Integer.MAX_VALUE;
		int tmp;
		int size = nodeList.size();
		for (int i = 0; i < size; i++) {
			tmpNode = nodeList.get(i);
			tmp = tmpNode.getStartPosition();
			if (tmp > startPosition) {
				if (tmp < tmpStartPosition) {
					tmpStartPosition = tmp;
					resultNode = tmpNode;
				}
			}
		}
		return resultNode;

	}

	public static void removeAnnotationAtPosition(
			ICompilationUnit fCompilationUnit, ASTNode target) {

		ITextFileBufferManager manager = FileBuffers.getTextFileBufferManager();
		ITextFileBuffer buffer = manager.getTextFileBuffer(
				fCompilationUnit.getPath(), LocationKind.IFILE);
		if (buffer == null) {
			return;
		}

		IAnnotationModel model = buffer.getAnnotationModel();

		Iterator<Annotation> iterator = model.getAnnotationIterator();
		// Annotation annotation;
		Position position;
		while (iterator.hasNext()) {
			Annotation annotation = iterator.next();
			if (annotation.getType().equals(
					PluginConstants.ASIDE_ANNOTATION_TYPE)) {
				position = model.getPosition(annotation);
				if (position.getOffset() == target.getStartPosition()
						&& position.getLength() == target.getLength()) {
					model.removeAnnotation(annotation);
				}
			}
		}

		return;
	}

	public static boolean isEncodingOrValidationMethodInvocation(ASTNode node) {
		if (!(node.getNodeType() == ASTNode.METHOD_INVOCATION))
			return false;
		MethodInvocation tmpMethodInvocation = (MethodInvocation) node;
		MethodInvocation secondMethodInvocation = null;
		SimpleName tmpSimpleName = null;
		// System.out.println("tmpMethodInvocation.getName().getIdentifier())){"+
		// tmpMethodInvocation.getName().getIdentifier());
		// System.out.println("tmpMethodInvocation.getExpression() "+tmpMethodInvocation.getExpression());
		if (isEncodingType(tmpMethodInvocation.getName().getIdentifier())) {

			if (tmpMethodInvocation.getExpression() instanceof MethodInvocation) {
				secondMethodInvocation = (MethodInvocation) tmpMethodInvocation
						.getExpression();
				if (secondMethodInvocation.getName().getIdentifier()
						.equals(PluginConstants.ESAPI_ENCODER)) {
					if (secondMethodInvocation.getExpression() instanceof SimpleName) {
						tmpSimpleName = (SimpleName) secondMethodInvocation
								.getExpression();
						if (tmpSimpleName.getIdentifier().equals(
								PluginConstants.ESAPI)) {
							return true;

						}
					}
				}
			}
		}

		if (tmpMethodInvocation.getName().getIdentifier()
				.equals(PluginConstants.ESAPI_VALIDATOR_GETVALIDINPUT)) {
			if (tmpMethodInvocation.getExpression() instanceof MethodInvocation) {
				secondMethodInvocation = (MethodInvocation) tmpMethodInvocation
						.getExpression();
				if (secondMethodInvocation.getName().getIdentifier()
						.equals(PluginConstants.ESAPI_VALIDATOR)) {
					if (secondMethodInvocation.getExpression() instanceof SimpleName) {
						tmpSimpleName = (SimpleName) secondMethodInvocation
								.getExpression();
						if (tmpSimpleName.getIdentifier().equals(
								PluginConstants.ESAPI)) {
							return true;

						}
					}

				}
			}
		}

		return false;
	}

	public static boolean isEncodingType(String str) {
		String[] types = PluginConstants.ENCODING_TYPES;

		int i = 0;
		for (i = 0; i < types.length; i++)
			if (str.equals("encodeFor" + types[i]))
				break;
		if (i == types.length)
			return false;
		else
			return true;
	}

	public static boolean isDirectlyEmbracedByValidationMethodInvocation(
			ASTNode node) {
		if (!(node.getParent() instanceof MethodInvocation))
			return false;

		MethodInvocation directEmbracingMethodInvocation = (MethodInvocation) node
				.getParent();
		MethodInvocation tmpMethodInvocation = null;
		SimpleName tmpSimpleName = null;

		// check if this methodInvocation is esapi validation
		if (directEmbracingMethodInvocation.getName().getIdentifier()
				.equals(PluginConstants.ESAPI_VALIDATOR_GETVALIDINPUT)) {
			if (directEmbracingMethodInvocation.getExpression() instanceof MethodInvocation) {
				tmpMethodInvocation = (MethodInvocation) directEmbracingMethodInvocation
						.getExpression();
				if (tmpMethodInvocation.getName().getIdentifier()
						.equals(PluginConstants.ESAPI_VALIDATOR)) {
					if (tmpMethodInvocation.getExpression() instanceof SimpleName) {
						tmpSimpleName = (SimpleName) tmpMethodInvocation
								.getExpression();
						if (tmpSimpleName.getIdentifier().equals(
								PluginConstants.ESAPI))
							return true;
					}
				}
			}
		}
		return false;

	}

	public static ArrayList<String> findPojoAttributes(
			Map<FieldDeclaration, ArrayList<SimpleName>> attributeMap,
			Map<MethodDeclaration, SimpleName> methodDeclarationNameMap) {
		// get the name list of attributes
		ArrayList<String> attributeNameList = new ArrayList<String>();
		ArrayList<SimpleName> tmpNameList = null;
		String tmpStr = null;
		for (Object value : attributeMap.values()) {
			tmpNameList = (ArrayList<SimpleName>) value;
			for (int i = 0; i < tmpNameList.size(); i++) {
				tmpStr = tmpNameList.get(i).getIdentifier();
				attributeNameList.add(tmpStr);
			}
		}

		// attributeMap
		// get the getXX, setXX, isXX arrayList.
		String matchStr = PluginConstants.matchStr;
		ArrayList<String> methodDeclarationNamePartialList = new ArrayList<String>();
        SimpleName tmpName = null;
		for (Object value : methodDeclarationNameMap.values()) {
			tmpName = (SimpleName) value;
			
				tmpStr = tmpName.getIdentifier();
				// use regex to judge getXX, setXX, isXX
				if (tmpStr.matches(matchStr)) {
					methodDeclarationNamePartialList.add(tmpStr);
				}
			
		}
		// attributeNameList methodDeclarationNamePartialList
		ArrayList<String> resultNameList = new ArrayList<String>();
		for (int i = 0; i < attributeNameList.size(); i++) {
			tmpStr = attributeNameList.get(i);
			tmpStr = tmpStr.substring(0, 1).toUpperCase() + tmpStr.substring(1);
			if ((booleanToNum(methodDeclarationNamePartialList.contains("set"
					+ tmpStr))
					+ booleanToNum(methodDeclarationNamePartialList
							.contains("get" + tmpStr))==2) || (booleanToNum(methodDeclarationNamePartialList.contains("set"
									+ tmpStr))
									+ booleanToNum(methodDeclarationNamePartialList
							.contains("is" + tmpStr)) == 2))
				resultNameList.add(tmpStr);
		}
		return resultNameList;
	}

	public static int booleanToNum(boolean t) {
		if (t == true)
			return 1;
		else
			return 0;
	}
	//newly added Jan. 15
		static public void UpdateICompilationUnit(ICompilationUnit unit, String code, IProgressMonitor pm)
		{	    
			SubMonitor monitor = SubMonitor.convert(pm,"Performing Code Modification",4);
			
	        try {
	        	unit.becomeWorkingCopy(monitor.newChild(1));
	        	int oldLen = unit.getSourceRange().getLength();  	
	        	ReplaceEdit edit = new ReplaceEdit(0, oldLen, code);   	
				unit.applyTextEdit(edit, monitor.newChild(1));	
				unit.commitWorkingCopy(true, monitor.newChild(1));
				unit.discardWorkingCopy();
				unit.makeConsistent(monitor.newChild(1));
			} catch (Exception e) {
				e.printStackTrace();
			}	
			
			monitor.done();
		}
		@Deprecated 
		static public void FormattICompilationUnit(ICompilationUnit unit)
		{		
			try {
				NullProgressMonitor monitor = new NullProgressMonitor();
				unit.becomeWorkingCopy(monitor);
				CodeFormatter formatter = ToolFactory.createCodeFormatter(null);
				TextEdit formatEdit;	
				formatEdit = formatter.format(CodeFormatter.K_COMPILATION_UNIT, unit.getSource(), 0, unit.getSource().length(), 0, unit.findRecommendedLineSeparator());
				unit.applyTextEdit(formatEdit, monitor);
				unit.commitWorkingCopy(true, monitor);
				unit.discardWorkingCopy();
				unit.makeConsistent(monitor);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		//newly added
		public static void applyComments(TextEdit edits, IDocument document){
			//add comments to any stubs
			String notifier = "//TODO: This is an automatically generated code stub.";
			//ArrayList<Integer> rawOffsets = getEditOffsets(edits, new ArrayList<Integer>());
			//sort(rawOffsets);
			MultiTextEdit rawMultiEdit = new MultiTextEdit();
			int i = 0;
			rawMultiEdit.addChild(new InsertEdit(10, notifier));
			

			try {
			UndoEdit undoSourceEdit = rawMultiEdit.apply(document);
			} catch (MalformedTreeException e1) {
			e1.printStackTrace();
			} catch (BadLocationException e1) {
			e1.printStackTrace();
			}

			}
		
		//newly added Jan. 30
		//insert comments at line linenum, and save it as the working copy.
		public static void insertComments(ICompilationUnit fCompilationUnit, int lineNum, String comments){
			String noticeStr = comments;
			String originalSourceStr;
			try {
				originalSourceStr = fCompilationUnit.getSource();
			
			char[] originalSourceCharArray = originalSourceStr.toCharArray();
			StringBuffer bufferToBeCommitted = new StringBuffer();
			int countOfLine = 0;
			char tmpChar;
			for(int i=0;i<originalSourceCharArray.length;i++){
				tmpChar = originalSourceCharArray[i];
				if(tmpChar !='\n'){
					bufferToBeCommitted.append(tmpChar);
					continue;
				}
				else{
					countOfLine++;
					bufferToBeCommitted.append(tmpChar);
					if(countOfLine==lineNum-1){
						bufferToBeCommitted.append(noticeStr);
					}
				}
			}	
		
			char[] charsToBeCommitted = bufferToBeCommitted.toString().toCharArray();
			fCompilationUnit.getBuffer().setContents(charsToBeCommitted);
			fCompilationUnit.getBuffer().save(null, true);
			//fCompilationUnit.commitWorkingCopy(true, null);
	        fCompilationUnit.becomeWorkingCopy(null);
			} catch (JavaModelException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		//newly added Feb.9 2012
		public static ArrayList<String> filterValidationTypes(ArrayList<String> allValidationTypes){
			ArrayList<String> filteredValidationTypes = new ArrayList<String>();
			String tmpStr;
			for(int i=0;i<allValidationTypes.size();i++){
				tmpStr = allValidationTypes.get(i);
				if(!PluginConstants.VALIDATION_TYPES_TO_BE_DELETED.contains(tmpStr)){
					filteredValidationTypes.add(tmpStr);
				}
			}
			return filteredValidationTypes;
		}
        public static boolean isBasicConstant(ASTNode node){
        	//basic string case: statement.setString(1, "test"); 		    
	        if(node instanceof StringLiteral)
			    return true;
	       //e.g. setInt(1, 4);
	        if(node instanceof NumberLiteral)
	        	return true;
	        //e.e. setInt(1, Integer.MAX_VALUE);
	        if(node instanceof QualifiedName){
	        	QualifiedName qualifiedName = (QualifiedName)node;
	        	if(qualifiedName.resolveConstantExpressionValue() != null)
	        		return true;
	        }	        
	        	
	        if(node instanceof SimpleName){
	        	SimpleName simpleName = (SimpleName)node;
	        	//case 2: works for primitive-typed constant, final String test = "fds" or final String test = SomeAlreadyDefinedFinalStringVariable; statement.setString(1, final_str);
	        	if(simpleName.resolveConstantExpressionValue()!=null){
	        		return true;
	        	}
	        	//to be done for Constant Object-------
	        	
	        	//case 3, String str = "test"; statement.setString(1, str); now I don't consider more complicated cases
		        else{
//		     
//	        	IBinding nameBinding = simpleName.resolveBinding();
//	        	if(nameBinding instanceof IVariableBinding){
//	        		IVariableBinding variableBinding = (IVariableBinding)nameBinding;
//	        		IVariableBinding variableDeclarationBinding = variableBinding.getVariableDeclaration();
//	        		VariableDeclarationFragment vdf = (VariableDeclarationFragment)variableDeclarationBinding;
//	        		System.out.println("variableDeclarationBinding.getName()" + variableDeclarationBinding.getName()+ " " + vdf.INITIALIZER);
//        	}
		       
        }
	}
	        return false;
 }
        //we only deal with normal case, setInt(1, someIntConstant), if the line of code has compile error itself, 
        //then we can not accurately identify whether it is constant or not
		public static boolean isConstant(ASTNode arg0) {
			if(isBasicConstant(arg0))
				return true;
			else{
				//case 5: statement.setString(1, "fds"+"fdsaf"+"fsd") or statement.setString(1, "fds"+FinalStringVariable), can be many constant string 
		        if(arg0 instanceof InfixExpression){
		        	InfixExpression infixExpression = (InfixExpression)arg0;
		        	if(infixExpression.getOperator().equals(InfixExpression.Operator.PLUS)){
		        		if(isBasicConstant(infixExpression.getLeftOperand()) && isBasicConstant(infixExpression.getRightOperand())){
		        			if(!infixExpression.hasExtendedOperands()){
		        				return true;
		        			}
		        			else{
		        				List extendedOperands = infixExpression.extendedOperands();
		        				int i = 0;
		        				for(i = 0; i < extendedOperands.size(); i++){
		        					if(!isBasicConstant((ASTNode)extendedOperands.get(i)))
		        						break;
		        				}
		        				if(i == extendedOperands.size())
		        					return true;
		        			}
		        		}
		        	}
		        		
		        }
		
				
			}
	              
	        
			return false;
		}

		public static String getReturnTypeStr(MethodDeclaration methodDeclaration) {
			//newly added Feb. 24
			String returnTypeStr = null;
			
			   Type returnTypeOfMethodDeclarationBelongTo = methodDeclaration.getReturnType2();
			   if(returnTypeOfMethodDeclarationBelongTo==null)
				   return null;
				if(returnTypeOfMethodDeclarationBelongTo.isPrimitiveType()){
					PrimitiveType tmpPrimitiveType = (PrimitiveType)returnTypeOfMethodDeclarationBelongTo;
					//boolean
					if(tmpPrimitiveType.getPrimitiveTypeCode().equals(PrimitiveType.BOOLEAN))
						returnTypeStr = PluginConstants.returnTypeCategories[0];
					//void
					else if(tmpPrimitiveType.getPrimitiveTypeCode().equals(PrimitiveType.VOID))
						returnTypeStr = PluginConstants.returnTypeCategories[1];
					//byte, char, short, int, float, double, long
					else
						returnTypeStr = PluginConstants.returnTypeCategories[2];
				}
				//object type
				else
					returnTypeStr = PluginConstants.returnTypeCategories[3];
				return returnTypeStr;
		}

}
