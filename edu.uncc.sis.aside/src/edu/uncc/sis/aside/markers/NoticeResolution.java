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

public class NoticeResolution implements IMarkerResolution,
		IMarkerResolution2 {

	private static final Logger logger = AsidePlugin.getLogManager()
	.getLogger(IgnoreMarkerResolution.class.getName());
	
	private ICompilationUnit fCompilationUnit;
	private int rankNum;
	
	public NoticeResolution(ICompilationUnit fCompilationUnit, int rankNum){
		super();
		this.rankNum = rankNum;
		this.fCompilationUnit = fCompilationUnit;
	}
	
	@Override
	public String getDescription() {
		String description = "By default, Eclipse sometimes provides a list of general recommendations that appear along with ASIDE warnings. These can almost always be ignored and should go away when the ASIDE warning is addressed.";
		return description;
	}

	@Override
	public Image getImage() {
		
		return AsidePlugin.getImageDescriptor("icons/devil.png")
		.createImage();
	}

	@Override
	public String getLabel() {
		String start = "0";
		String end = " - Options below are not ASIDE generated";
		return start + this.rankNum + end;
	}

	@Override
	public void run(IMarker marker) {

	}
	
	
}
