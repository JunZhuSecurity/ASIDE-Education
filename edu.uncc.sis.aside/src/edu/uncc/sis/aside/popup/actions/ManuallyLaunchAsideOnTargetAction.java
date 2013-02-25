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
import edu.uncc.sis.aside.auxiliary.core.TestRunOnAllProjects;
import edu.uncc.sis.aside.constants.PluginConstants;
import edu.uncc.sis.aside.jobs.ESAPIConfigurationJob;
import edu.uncc.sis.aside.utils.CheckingRoutine;
import edu.uncc.sis.aside.utils.Converter;
import edu.uncc.sis.aside.visitors.MethodDeclarationVisitor;

/**
 * Application Security IDE Plugin (ASIDE)
 * 
 * @author Jing Xie (jxie2 at uncc dot edu) <a href="http://www.uncc.edu/">UNC
 *         Charlotte</a>
 */
public class ManuallyLaunchAsideOnTargetAction implements IObjectActionDelegate {

	private static final Logger logger = AsidePlugin.getLogManager().getLogger(
			ManuallyLaunchAsideOnTargetAction.class.getName());

	private IAction targetAction;
	private IWorkbenchPart targetWorkbench;

	private static Map<ICompilationUnit, Map<MethodDeclaration, ArrayList<IMarker>>> projectMarkerMap = null;

	public ManuallyLaunchAsideOnTargetAction() {
		super();
	}

	@Override
	public void run(IAction action) {
		
		
		//String userIDFromSystem = System.getProperty("user.name");
		/*String userID
		if(AuthenCenter.hasPermission(userID)){
			AsidePlugin.getDefault().setAllowed(true);
		}*/
		
		AsidePlugin.getDefault().setInternetReachable(true);
		String userID = null;
		
		if(true){
		//////////////newly added
		IPath stateLocation = AsidePlugin.getDefault().getStateLocation();
		String fileName = stateLocation + "/"
				+ AsidePlugin.getAsideUseridFile(); // might have to be updated
													// about "/"
		File userIdFile = new File(fileName);
		if (userIdFile.exists()) {
			try {
				FileReader fr = new FileReader(userIdFile);
				BufferedReader br = new BufferedReader(fr);
				String userIdRead = br.readLine();
				System.out.println("userId read from the file = "
						+ userIdRead);
				userID = userIdRead;
				br.close();
				fr.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
				AsidePlugin.getDefault().setAllowed(true);
				AsidePlugin.getDefault().setUserId(userID);
			
			
		} 
		
		if(AsidePlugin.getDefault().isAllowed()){
		System.out.println("manually run ASIDE");
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		 //System.out.println("in Manuallylsdfjalsj");
	    //get current date time with Date()
		Date date = new Date();
	    logger.info(dateFormat.format(date)+ " " + "User clicked Run ASIDE from the context menu to launch ASIDE");

		// scan the selected target's project presentation
		if (targetWorkbench == null) {
			return;
		}

		ISelectionProvider selectionProvider = targetWorkbench.getSite()
				.getSelectionProvider();

		if (selectionProvider == null) {
			return;
		}

		ISelection selection = selectionProvider.getSelection();

		if (selection != null && !selection.isEmpty()
				&& selection instanceof IStructuredSelection) {
			IStructuredSelection structuredSelection = (IStructuredSelection) selection;

			Object firstElement = structuredSelection.getFirstElement();

			try {
				IProject project = null;
				IJavaProject javaProject = null;
				if (firstElement != null && firstElement instanceof IResource) {
					IResource resource = (IResource) firstElement;
					project = resource.getProject();
					javaProject = JavaCore.create(project);
				} else if (firstElement != null
						&& firstElement instanceof IJavaElement) {
					IResource resource = (IResource) ((IJavaElement) firstElement)
							.getAdapter(IResource.class);
					if (resource != null) {
						project = resource.getProject();
						javaProject = JavaCore.create(project);
					}
				}
				if (project != null && javaProject != null) {
					
					
					// setup a new Job thread
					AsidePlugin.getDefault().setProject(project);
					
					ESAPIConfigurationJob job = new ESAPIConfigurationJob(
							"ESAPI Configuration", project, javaProject);
					job.scheduleInteractive();
					inspectOnProject(javaProject);
					
				}
			} catch (JavaModelException e) {
				e.printStackTrace();
			} catch (CoreException e) {
				e.printStackTrace();
			}

		}

		// At last, set the CompliationParticipant active
		if (!AsidePlugin.getDefault().getSignal()) {
			AsidePlugin.getDefault().setSignal(true);
		}
		//System.out.println("Manually launch aside finished!");
	}
		}
	}
	public static void inspectOnProject(IJavaProject project)
			throws JavaModelException, CoreException {

		if (project == null) {
			return;
		}
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		  
	    //get current date time with Date()
		Date date = new Date();
	    logger.info(dateFormat.format(date) + " " + AsidePlugin.getUserId() + " ASIDE is inspecting project: " + project.getElementName());
        if(project != null){
        	try{
		        project.getCorrespondingResource().deleteMarkers(
				PluginConstants.ASIDE_MARKER_TYPE, false,
				IResource.DEPTH_INFINITE);
        }catch(Exception e){
        	e.printStackTrace();
        }
        }
		projectMarkerMap = AsidePlugin.getDefault().getMarkerIndex(project);

		if (projectMarkerMap == null) {
			projectMarkerMap = new HashMap<ICompilationUnit, Map<MethodDeclaration, ArrayList<IMarker>>>();
		}

		IPackageFragment[] packageFragmentsInProject = project
				.getPackageFragments();
		for (IPackageFragment fragment : packageFragmentsInProject) {
			ICompilationUnit[] units = fragment.getCompilationUnits();
			for (ICompilationUnit unit : units) {

				Map<MethodDeclaration, ArrayList<IMarker>> fileMap = inspectOnJavaFile(unit);
				projectMarkerMap.put(unit, fileMap);
			}
		}

		// At last, put it back to Aside Plugin
		AsidePlugin.getDefault().setMarkerIndex(project, projectMarkerMap);
		date = new Date();
		logger.info(dateFormat.format(date) + " " + AsidePlugin.getUserId() + " ASIDE finished inspecting project: " + project.getElementName());
	}

	public static Map<MethodDeclaration, ArrayList<IMarker>> inspectOnJavaFile(
			ICompilationUnit unit) {
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		  
	    //get current date time with Date()
		Date date = new Date();
	    logger.info(dateFormat.format(date) + " " + AsidePlugin.getUserId() + " ASIDE starts inspecting java file: " + unit.getElementName());

		CompilationUnit astRoot = Converter.parse(unit);
		// PreferencesSet set = new PreferencesSet(true, true, false, new
		// String[0]);
		MethodDeclarationVisitor declarationVisitor = new MethodDeclarationVisitor(
				astRoot, null, unit, null);
		return declarationVisitor.process();

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
