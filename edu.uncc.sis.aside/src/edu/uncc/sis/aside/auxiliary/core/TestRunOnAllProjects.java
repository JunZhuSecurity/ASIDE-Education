package edu.uncc.sis.aside.auxiliary.core;


import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;

import edu.uncc.sis.aside.AsidePlugin;
import edu.uncc.sis.aside.jobs.ESAPIConfigurationJob;
import edu.uncc.sis.aside.popup.actions.ManuallyLaunchAsideOnTargetAction;

public class TestRunOnAllProjects {
	private static final Logger logger = AsidePlugin.getLogManager().getLogger(
			TestRunOnAllProjects.class.getName());
	
	public void runOnAllProjects(){
		
			IProject[] allProjects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
			Set<IProject> activeProjects= new HashSet<IProject>();
			for (IProject p : allProjects){
			    if(p.isOpen())
				        activeProjects.add(p);
	     	}
			DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
			  
		    //get current date time with Date()
			Date date = new Date();
		    logger.info(dateFormat.format(date) + " " + AsidePlugin.getUserId() + " ASIDE starts to inspect all the projects in the workspace");

			for(IProject project : activeProjects){
			
				if(project == null)
					continue;
				System.out.println("projectname = " + project.getName());
				if(project.getName().equals("Servers") || project.getName().equals("servers") || project.getName().equals("Server") || project.getName().equals("server")){
					continue;
				}
				IJavaProject javaProject = JavaCore.create(project);
				if(javaProject == null)
					continue;
				AsidePlugin.getDefault().setProject(project);
				
				ESAPIConfigurationJob job = new ESAPIConfigurationJob(
						"ESAPI Configuration", project, javaProject);
				job.scheduleInteractive();
				try {
					ManuallyLaunchAsideOnTargetAction.inspectOnProject(javaProject);
					//System.out.println("test on all projects finished");
				} catch (JavaModelException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (CoreException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				//break; //for now, only run on one project
			}
			date = new Date();
		    logger.info(dateFormat.format(date) + " " + AsidePlugin.getUserId() + " ASIDE finished inspect all the projects in the workspace");
		
	}

}
