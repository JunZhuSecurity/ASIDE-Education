package edu.uncc.sis.aside.markers;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IMarkerResolution;
import org.eclipse.ui.IMarkerResolution2;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.browser.IWebBrowser;
import org.eclipse.ui.browser.IWorkbenchBrowserSupport;

import edu.uncc.sis.aside.AsidePlugin;
import edu.uncc.sis.aside.utils.ASIDEMarkerAndAnnotationUtil;

public class IgnoreMarkerResolution implements IMarkerResolution,
		IMarkerResolution2 {

	private static final Logger logger = AsidePlugin.getLogManager()
	.getLogger(IgnoreMarkerResolution.class.getName());
	
	private ICompilationUnit fCompilationUnit;
	private int rankNum;
	private String type;
	
	public IgnoreMarkerResolution(ICompilationUnit fCompilationUnit, int rankNum, String type){
		super();
		this.fCompilationUnit = fCompilationUnit;
		this.rankNum = rankNum;
		this.type = type;
	}
	
	@Override
	public String getDescription() {
		String content = "";
		String end = "";
		if(type.equals("input"))
		end = "Follow the \"About this Input Validation Issue Website Link\" for more information about why the selected method invocation needs validation.";
		else if(type.equals("output"))
		end = "Follow the \"About this Output Encoding Issue Website Link\" for more information about why the selected method invocation needs encoding.";	
		else if(type.equals("SQL"))
			end = "Follow the \"SQL Statements: Explanations and Examples Website Link\" for more information about why the selected method invocation needs encoding.";
		else
			end = "";
			
		String instruction = "-- Double click selection to remove ASIDE warning --";
		String description = "While ASIDE has found a potential vulnerability, you would like to disable the warning and treat this issue as non-vulnerable.";
		content = instruction + "<p><p>" + description + "<p><p>" + end;
		return content;
	}

	@Override
	public Image getImage() {
		
		return AsidePlugin.getImageDescriptor("icons/devil.png")
		.createImage();
	}

	@Override
	public String getLabel() {
		String start = "0";
		String label = " - Disable Warning";
		return start + this.rankNum + label;
	}

	@Override
	public void run(IMarker marker) {

		IProject project = ASIDEMarkerAndAnnotationUtil.getProjectFromICompilationUnit(fCompilationUnit);
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		  
	    //get current date time with Date()
		Date date = new Date();
	    logger.info(dateFormat.format(date)+ " " + AsidePlugin.getUserId() + " chose to Ignore This for the warning at " 
		        + marker.getAttribute(IMarker.LINE_NUMBER, -1) + " in java file <<"
				+ fCompilationUnit.getElementName()
				+ ">> in Project ["
				+ project.getName() + "]");

		ASIDEMarkerAndAnnotationUtil.replaceMarkerWithAnnotation(fCompilationUnit, marker);
	}
	
	
}
