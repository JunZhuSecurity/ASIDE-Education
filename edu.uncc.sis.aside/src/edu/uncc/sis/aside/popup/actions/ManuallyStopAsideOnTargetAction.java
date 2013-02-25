package edu.uncc.sis.aside.popup.actions;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;

import edu.uncc.sis.aside.AsidePlugin;
import edu.uncc.sis.aside.constants.PluginConstants;
import edu.uncc.sis.aside.jobs.ESAPIConfigurationJob;
import edu.uncc.sis.aside.utils.Converter;
import edu.uncc.sis.aside.utils.MakerManagement;
import edu.uncc.sis.aside.visitors.MethodDeclarationVisitor;

/**
 * Application Security IDE Plugin (ASIDE)
 * 
 * @author Jun Zhu (jzhu16 at uncc dot edu) <a href="http://www.uncc.edu/">UNC
 *         Charlotte</a>
 */
public class ManuallyStopAsideOnTargetAction implements IObjectActionDelegate {

	private static final Logger logger = AsidePlugin.getLogManager().getLogger(
			ManuallyLaunchAsideOnTargetAction.class.getName());

	private IAction targetAction;
	private IWorkbenchPart targetWorkbench;

	private static Map<ICompilationUnit, Map<MethodDeclaration, ArrayList<IMarker>>> projectMarkerMap = null;

	public ManuallyStopAsideOnTargetAction() {
		super();
	}

	@Override
	public void run(IAction action) {
		
		if(AsidePlugin.getDefault().isAllowed()){
		
			MakerManagement.removeAllASIDEMarkersInWorkspace();
			AsidePlugin.getDefault().setAllowed(false);
	}
	}
	
	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		// do nothing
	}

	@Override
	public void setActivePart(IAction action, IWorkbenchPart workbench) {
		this.targetAction = action;
		this.targetWorkbench = workbench;

	}

}

