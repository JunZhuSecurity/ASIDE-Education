package edu.uncc.sis.aside;

/**
 * Application Security in IDE Plugin (ASIDE)
 * 
 */
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceDescription;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.dom.ASTMatcher;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.ICoolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.internal.Workbench;
import org.eclipse.ui.internal.WorkbenchWindow;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import edu.uncc.sis.aside.auxiliary.core.TestRunOnAllProjects;
import edu.uncc.sis.aside.constants.PluginConstants;
import edu.uncc.sis.aside.logging.AsideLoggingManager;
import edu.uncc.sis.aside.preferences.IPreferenceConstants;
import edu.uncc.sis.aside.utils.CheckingRoutine;
import edu.uncc.sis.aside.utils.MakerManagement;

/**
 * The activator class controls the plug-in life cycle
 * 
 * @author Jun Zhu (jzhu16 at uncc dot edu) Jing Xie (jxie2 at uncc dot edu) <a href="http://www.uncc.edu/">UNC
 *         Charlotte</a>
 */
public class AsidePlugin extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "edu.uncc.sis.aside";
    
	public static Date END_DAY_SEND_LOGS;
	// The shared instance
	private static AsidePlugin plugin;
	
	private static String userId;

	private static final String LOG_PROPERTIES_FILE = "logger.properties";
	
	private static final String ASIDE_USERID_FILE = "userID.txt";

	// The shared AST matcher instance
	private static ASTMatcher astMatcher;
	
	// AsideCompilationParticipant is off when signal is false
	private boolean signal = false;

	private static Map<IJavaProject, Map<ICompilationUnit, Map<MethodDeclaration, ArrayList<IMarker>>>> markerIndex = null;

	private AsideLoggingManager loggingManager;
	
	private IProject project;
	
	private static boolean isAllowed = true;
	
	private static boolean isInternetReachable = true;

	public static boolean isInternetReachable() {
		return isInternetReachable;
	}

	public static void setInternetReachable(boolean isInternetReachable) {
		AsidePlugin.isInternetReachable = isInternetReachable;
	}

	/**
	 * 0-argument constructor as required by extension point
	 */
	public AsidePlugin() {
		if (markerIndex == null) {
			markerIndex = new HashMap<IJavaProject, Map<ICompilationUnit, Map<MethodDeclaration, ArrayList<IMarker>>>>();
		}
	}

	public static boolean isAllowed() {
		return isAllowed;
	}

	public static void setAllowed(boolean isAllowed) {
		AsidePlugin.isAllowed = isAllowed;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext
	 * )
	 */
	public void start(BundleContext context) throws Exception {
		setAllowed(true);
		String userID = null;
		
		
		MakerManagement.removeAllASIDEMarkersInWorkspace();
		super.start(context);
		plugin = this;
		if (astMatcher == null) {
			astMatcher = new ASTMatcher();
		}

		IWorkspace workspace = ResourcesPlugin.getWorkspace();
        
		IWorkspaceDescription description = workspace.getDescription();
		if (!description.isAutoBuilding()) {
			description.setAutoBuilding(true);
			workspace.setDescription(description);
		}

		configure();
		//System.out.println("Aside Plugin start print test");
		
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		// get current date time with Date()
		Date date = new Date();

		// System.out.println("test casefsad");
		
		
	}

	private void configure() {

		try {
			this.END_DAY_SEND_LOGS = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").parse(PluginConstants.LOG_SEND_END_DATE);   //the date needs to be verified
			
			URL url = getBundle().getEntry("/" + LOG_PROPERTIES_FILE);
			InputStream propertiesInputStream = url.openStream();
			
			if (propertiesInputStream != null) {
				//System.out.println("configure log run.......................");
				Properties props = new Properties();
				props.load(propertiesInputStream);
				propertiesInputStream.close();
				this.loggingManager = new AsideLoggingManager(this, props);
				this.loggingManager.hookPlugin(AsidePlugin.getDefault()
						.getBundle().getSymbolicName(), AsidePlugin
						.getDefault().getLog());
			}
			
		} catch (Exception e) {
			String message = "Error while initializing log properties.\n\n\n\n"
					+ e.getMessage();
			IStatus status = new Status(IStatus.ERROR, getDefault().getBundle()
					.getSymbolicName(), IStatus.ERROR, message, e);
			getLog().log(status);
			throw new RuntimeException(
					"Error while initializing log properties.\n", e);
		}
	}

	public static AsideLoggingManager getLogManager() {
		return getDefault().loggingManager;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext
	 * )
	 */
	public void stop(BundleContext context) throws Exception {
		
		  //  System.out.println("Eclipse is closed");
		    //send log file
		
			Date currentDate = new Date();
		 plugin = null;

		if (this.loggingManager != null) {
			this.loggingManager.shutdown();
			this.loggingManager = null;
		}
		super.stop(context);
		
	}

	/**
	 * Returns the shared instance
	 * 
	 * @return the shared instance
	 */
	public static AsidePlugin getDefault() {
		return plugin;
	}

	/**
	 * Returns an image descriptor for the image file at the given plug-in
	 * relative path
	 * 
	 * @param path
	 *            the path
	 * @return the image descriptor
	 */
	public static ImageDescriptor getImageDescriptor(String path) {
		return imageDescriptorFromPlugin(PLUGIN_ID, path);
	}

	public IWorkbenchWindow getActiveWorkbenchWindow() {
		if (Display.getCurrent() != null) {
			return getDefault().getWorkbench().getActiveWorkbenchWindow();
		}
		// need to call from UI thread
		final IWorkbenchWindow[] windows = new IWorkbenchWindow[1];
		Display.getDefault().syncExec(new Runnable() {
			public void run() {
				windows[0] = getDefault().getWorkbench()
						.getActiveWorkbenchWindow();
			}
		});
		return windows[0];
	}

	public Shell getShell() {
		IWorkbenchWindow window = getActiveWorkbenchWindow();
		if (window == null)
			return null;
		return window.getShell();
	}

	/*
	 * ASIDE PREFERENCES
	 */
	public String[] getDefaultTBPathsPreference() {
		return convert(getPreferenceStore().getDefaultString(
				IPreferenceConstants.EXTERNAL_TB_PATH_PREFERENCE));
	}

	public String[] getTBPathsPreference() {
		return convert(getPreferenceStore().getString(
				IPreferenceConstants.EXTERNAL_TB_PATH_PREFERENCE));
	}

	public void setTBPathPreference(String[] elements) {

		if (elements == null) {
			return;
		}

		StringBuffer buffer = new StringBuffer();
		for (int i = 0; i < elements.length; i++) {
			buffer.append(elements[i]);
			buffer.append(IPreferenceConstants.PATH_DELIMITER);
		}
		getPreferenceStore().setValue(
				IPreferenceConstants.EXTERNAL_TB_PATH_PREFERENCE,
				buffer.toString());
	}

	public boolean getAsideTBCheckPreference() {
		return getPreferenceStore().getBoolean(
				IPreferenceConstants.ASIDE_TB_PREFERENCE);
	}

	public void setAsideTBCheckPreference(boolean check) {
		getPreferenceStore().setValue(IPreferenceConstants.ASIDE_TB_PREFERENCE,
				check);
	}

	public boolean getProjectTBCheckPreference() {
		return getPreferenceStore().getBoolean(
				IPreferenceConstants.PROJECT_TB_PREFERENCE);
	}

	public void setProjectTBCheckPreference(boolean check) {
		getPreferenceStore().setValue(
				IPreferenceConstants.PROJECT_TB_PREFERENCE, check);
	}

	public boolean getExternalTBCheckPreference() {
		return getPreferenceStore().getBoolean(
				IPreferenceConstants.EXTERNAL_TB_PREFERENCE);
	}

	public void setExternalTBCheckPreference(boolean check) {
		getPreferenceStore().setValue(
				IPreferenceConstants.EXTERNAL_TB_PREFERENCE, check);
	}

	// ========================================//

	public String[] getDefaultVRPathsPreference() {
		return convert(getPreferenceStore().getDefaultString(
				IPreferenceConstants.EXTERNAL_VR_PATH_PREFERENCE));
	}

	public String[] getVRPathsPreference() {
		return convert(getPreferenceStore().getString(
				IPreferenceConstants.EXTERNAL_VR_PATH_PREFERENCE));
	}

	public void setVRPathPreference(String[] elements) {

		if (elements == null) {
			return;
		}

		StringBuffer buffer = new StringBuffer();
		for (int i = 0; i < elements.length; i++) {
			buffer.append(elements[i]);
			buffer.append(IPreferenceConstants.PATH_DELIMITER);
		}
		getPreferenceStore().setValue(
				IPreferenceConstants.EXTERNAL_VR_PATH_PREFERENCE,
				buffer.toString());
	}

	public boolean getAsideVRCheckPreference() {
		return getPreferenceStore().getBoolean(
				IPreferenceConstants.ASIDE_VR_PREFERENCE);
	}

	public void setAsideVRCheckPreference(boolean check) {
		getPreferenceStore().setValue(IPreferenceConstants.ASIDE_VR_PREFERENCE,
				check);
	}

	public boolean getProjectVRCheckPreference() {
		return getPreferenceStore().getBoolean(
				IPreferenceConstants.PROJECT_VR_PREFERENCE);
	}

	public void setProjectVRCheckPreference(boolean check) {
		getPreferenceStore().setValue(
				IPreferenceConstants.PROJECT_VR_PREFERENCE, check);
	}

	public boolean getExternalVRCheckPreference() {
		return getPreferenceStore().getBoolean(
				IPreferenceConstants.EXTERNAL_VR_PREFERENCE);
	}

	public void setExternalVRCheckPreference(boolean check) {
		getPreferenceStore().setValue(
				IPreferenceConstants.EXTERNAL_VR_PREFERENCE, check);
	}

	private String[] convert(String preferenceValue) {
		StringTokenizer tokenizer = new StringTokenizer(preferenceValue,
				IPreferenceConstants.PATH_DELIMITER);
		int tokenCount = tokenizer.countTokens();
		String[] elements = new String[tokenCount];

		for (int i = 0; i < tokenCount; i++) {
			elements[i] = tokenizer.nextToken();
		}

		return elements;
	}

	public ASTMatcher getASTMatcher() {
		if (astMatcher == null)
			astMatcher = new ASTMatcher();
		return astMatcher;
	}

	public Map<ICompilationUnit, Map<MethodDeclaration, ArrayList<IMarker>>> getMarkerIndex(
			IJavaProject project) {

		if (markerIndex == null) {
			return null;
		}

		if (project == null) {
			return null;
		}

		return markerIndex.get(project);
	}

	public Set<IJavaProject> getIndexedJavaProjects() {
		return markerIndex.keySet();
	}

	public void setMarkerIndex(
			IJavaProject project,
			Map<ICompilationUnit, Map<MethodDeclaration, ArrayList<IMarker>>> markerIndex) {
		AsidePlugin.markerIndex.put(project, markerIndex);
	}

	public void setSignal(boolean signal) {
		this.signal = signal;
	}

	public boolean getSignal() {
		return signal;
	}

	public IProject getProject() {
		return project;
	}

	public void setProject(IProject project) {
		this.project = project;
	}
	

	public static String getUserId() {
		return userId;
	}

	public static void setUserId(String userId) {
		AsidePlugin.userId = userId;
	}

	public static String getAsideUseridFile() {
		return ASIDE_USERID_FILE;
	}
	

}
