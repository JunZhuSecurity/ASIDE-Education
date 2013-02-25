package edu.uncc.sis.aside.utils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.MethodDeclaration;

import edu.uncc.sis.aside.AsidePlugin;
import edu.uncc.sis.aside.constants.PluginConstants;

public class MakerManagement {
	public static void removeAllASIDEMarkersInWorkspace(){
		IProject[] allProjects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
		Set<IProject> activeProjects= new HashSet<IProject>();
		for (IProject p : allProjects){
		    if(p.isOpen())
			        activeProjects.add(p);
		}
		
		for(IProject project : activeProjects){
			if(project == null)
				continue;
			IJavaProject javaProject = JavaCore.create(project);
			if(javaProject == null)
				continue;
			removeAllASIDEMarkersOneProject(javaProject);
		}   
	}
	
	public static void removeAllASIDEMarkersOneProject(IJavaProject project){
		if (project == null) {
			return;
		}
	    if(project != null){
        	try{
		       project.getCorrespondingResource().deleteMarkers(
				PluginConstants.ASIDE_MARKER_TYPE, false,
				IResource.DEPTH_INFINITE);
        }catch(Exception e){
        	System.err.println("project.get1");
        	e.printStackTrace();
        }
        }
	
		IPackageFragment[] packageFragmentsInProject;
		try {
			packageFragmentsInProject = project
					.getPackageFragments();
		
		for (IPackageFragment fragment : packageFragmentsInProject) {
			ICompilationUnit[] units = fragment.getCompilationUnits();
			for (ICompilationUnit unit : units) {
				removeAllASIDEMarkersOneCompilationUnit(unit);
			}
			}
		} catch (JavaModelException e) {
			// TODO Auto-generated catch block
			System.err.println("project.get2");
			//e.printStackTrace();
			return;
		}
	}
	
	public static void removeAllASIDEMarkersOneCompilationUnit(ICompilationUnit unit){
		try {
			IResource resource = unit.getUnderlyingResource();
			IMarker[] markers = resource.findMarkers("edu.uncc.sis.aside.AsideMarker", false, IResource.DEPTH_ONE);
			for(IMarker marker :markers){
				marker.delete();
			}
			markers = resource.findMarkers("edu.uncc.sis.aside.AsideMarker", false, IResource.DEPTH_ONE);
		//	System.out.println("Markers in " + unit.getElementName() + " is " + markers.length);
		} catch (JavaModelException e) {
			System.err.println("project.get3");
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (CoreException e) {
			System.err.println("project.get4");
			// TODO Auto-generated catch block
			e.printStackTrace();
		}   
	}

}
