package edu.uncc.sis.aside.auxiliary.core;

import java.net.URI;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.launching.AbstractVMInstall;
import org.eclipse.jdt.launching.JavaRuntime;

public class SetEnvironmentVariables {
	private final static String ESAPI_VM_ARG = "-Dorg.owasp.esapi.resources";
	private final static String ESAPI_CONFIG_DIR_NAME = "ASIDE-ESAPI";
	private final static String ASIDE_ESAPI_CONTAINER = "ESAPI Libraries";
	private final static String PROJECT_LIB_PATH = "WebContent"
			+ IPath.SEPARATOR + "WEB-INF" + IPath.SEPARATOR + "lib";
	private final static String PROJECT_WEBINF_PATH = "src" ;
	
	private IProject fProject;
	private IJavaProject javaProject;

	public SetEnvironmentVariables(IProject project,
			IJavaProject javaProject) {
		this.fProject = project;
		this.javaProject = javaProject;
	}
	
	public void setVMarguments(){
		URI locationUri = null;
		IFolder folder = fProject.getFolder(PROJECT_WEBINF_PATH + IPath.SEPARATOR + "esapi"); //here may need changes

		if (folder.exists()) {
			//System.out.println("setESAPIResourceLocation class--folder.exists()="+locationUri);
			locationUri = folder.getRawLocationURI();
			//System.out.println("setESAPIResourceLocation class--older.getRawLocationURI="+locationUri);
		} else {
			folder = fProject.getFolder(".esapi");
			System.out.println("folder not exist!");
			
			if (folder.exists()) {
				locationUri = folder.getRawLocationURI();
				//System.out.println("setESAPIResourceLocation class-- fProject.getFolder .esapi exist, locationUri="+locationUri);
				
			}
		}

		if (locationUri == null){
			System.out.println("setESAPIResourceLocation class--locationUri= null");
			return;
		}
		//String path = ESAPI_VM_ARG + "=\"" + locationUri.getPath().substring(1) + "\""; just added for test Feb. 28
		
		String path = ESAPI_VM_ARG + "=\"" + "/" + locationUri.getPath().substring(1) + "\"";
		
        //System.out.println("Line 307 Path = " + path);
		try {
			AbstractVMInstall vminstall = (AbstractVMInstall) JavaRuntime
					.getVMInstall(javaProject);
			if (vminstall != null) {
				String[] vmargs = vminstall.getVMArguments();
				if (vmargs == null) {
					vminstall.setVMArguments(new String[] { path });
				    System.out.println("vmargs == null, and it is reseted, Line 315 Path = " + path);
				}
			}
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}

}
