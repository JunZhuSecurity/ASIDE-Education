package edu.uncc.sis.aside.jobs;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;

import edu.uncc.sis.aside.AsidePlugin;
import edu.uncc.sis.aside.ast.ASTBuilder;
import edu.uncc.sis.aside.constants.PluginConstants;
import edu.uncc.sis.aside.preferences.PreferencesSet;
import edu.uncc.sis.aside.visitors.MethodDeclarationVisitor;

public class TrustBoundariesResetJob extends Job {

	private static final Logger logger = AsidePlugin.getLogManager().getLogger(
			TrustBoundariesResetJob.class.getName());

	private PreferencesSet prefSet;
	private static Map<ICompilationUnit, Map<MethodDeclaration, ArrayList<IMarker>>> projectMarkerMap = null;

	public TrustBoundariesResetJob(String name, PreferencesSet prefSet) {
		super(name);
		this.prefSet = prefSet;
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		
		if(AsidePlugin.getDefault().isInternetReachable()){
			
		
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		// get current date time with Date()
		Date date = new Date();
		logger.info(dateFormat.format(date) + AsidePlugin.getUserId() + 
				" User resets trust boundary rules through ASIDE preference page");

		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}

		Set<IJavaProject> keySet = AsidePlugin.getDefault()
				.getIndexedJavaProjects();
		Set<IJavaProject> indexedProjects = new HashSet<IJavaProject>();
		indexedProjects.addAll(keySet);

		if (keySet == null) {
			return Status.OK_STATUS;
		}
		IPath stateLocation = AsidePlugin.getDefault().getStateLocation();
        
		try {
			monitor.beginTask("Working on current workspace at "
					+ stateLocation.toOSString() + ";", indexedProjects.size());
			if(AsidePlugin.getDefault().isAllowed()){
			while (!indexedProjects.isEmpty()) {
				for (IJavaProject indexedProject : indexedProjects) {
					monitor.subTask("Checking on Project "
							+ indexedProject.getElementName() + "...");

					IPackageFragment[] fragments = indexedProject
							.getPackageFragments();
					ArrayList<ICompilationUnit> units = new ArrayList<ICompilationUnit>();

					for (IPackageFragment fragment : fragments) {
						ICompilationUnit[] temp = fragment
								.getCompilationUnits();
						for (ICompilationUnit unit : temp) {
							units.add(unit);
						}
					}

					Map<ICompilationUnit, Map<MethodDeclaration, ArrayList<IMarker>>> projectMarkerMap = AsidePlugin
							.getDefault().getMarkerIndex(indexedProject);

					for (ICompilationUnit unit : units) {
						monitor.subTask("Working on Java file "
								+ unit.getElementName());
						CompilationUnit astRoot = ASTBuilder.getASTBuilder()
								.parse(unit);

						if (projectMarkerMap == null) {
							projectMarkerMap = new HashMap<ICompilationUnit, Map<MethodDeclaration, ArrayList<IMarker>>>();

							MethodDeclarationVisitor declarationVisitor = new MethodDeclarationVisitor(
									astRoot, null, unit, prefSet);
							Map<MethodDeclaration, ArrayList<IMarker>> fileMap = declarationVisitor
									.process();
							projectMarkerMap.put(unit, fileMap);

						} else if (projectMarkerMap != null) {
							Map<MethodDeclaration, ArrayList<IMarker>> fileMap = projectMarkerMap
									.get(unit);
							MethodDeclarationVisitor declarationVisitor = new MethodDeclarationVisitor(
									astRoot, fileMap, unit, prefSet);
							projectMarkerMap.put(unit,
									declarationVisitor.process());
						}
						monitor.worked(1);
					}

					// inspectOnProject(indexedProject);

					AsidePlugin.getDefault().setMarkerIndex(indexedProject,
							projectMarkerMap);

					if (monitor.isCanceled())
						return Status.CANCEL_STATUS;

					indexedProjects.remove(indexedProject);
					monitor.worked(1);
				}

			}
			}
		} catch (JavaModelException e) {
			return e.getStatus();
		} finally {
			monitor.done();
		}
		}
		return Status.OK_STATUS;
		}
	

	public void scheduleInteractive() {
		setUser(true);
		setPriority(Job.INTERACTIVE);
		schedule();
	}

	private void inspectOnProject(IJavaProject project)
			throws JavaModelException, CoreException {

		if (project == null) {
			return;
		}

		project.getCorrespondingResource().deleteMarkers(
				PluginConstants.ASIDE_MARKER_TYPE, false,
				IResource.DEPTH_INFINITE);

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
	}

	private Map<MethodDeclaration, ArrayList<IMarker>> inspectOnJavaFile(
			ICompilationUnit unit) {

		CompilationUnit astRoot = ASTBuilder.getASTBuilder().parse(unit);
		MethodDeclarationVisitor declarationVisitor = new MethodDeclarationVisitor(
				astRoot, null, unit, prefSet);
		return declarationVisitor.process();

	}

}
