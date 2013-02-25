package edu.uncc.sis.aside.markers;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.Random;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IMarkerResolution;
import org.eclipse.ui.IMarkerResolutionGenerator;
import org.eclipse.ui.IMarkerResolutionGenerator2;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.PartInitException;

import edu.uncc.sis.aside.AsidePlugin;
import edu.uncc.sis.aside.auxiliary.properties.ESAPIPropertiesReader;
import edu.uncc.sis.aside.constants.PluginConstants;
import edu.uncc.sis.aside.domainmodels.VulnerabilityKnowledge;
import edu.uncc.sis.aside.utils.ASIDEMarkerAndAnnotationUtil;
import edu.uncc.sis.aside.utils.Converter;
import edu.uncc.sis.aside.utils.SecureProgrammingKnowledgeBase;
import edu.uncc.sis.aside.views.ExplanationView;



public class AsideMarkerInputResolutionGenerator implements
		IMarkerResolutionGenerator, IMarkerResolutionGenerator2 {

	private static final Logger logger = AsidePlugin.getLogManager().getLogger(
			AsideMarkerInputResolutionGenerator.class.getName());

	private static final String EXPLANATION_VIEW_ID = "edu.uncc.sis.aside.views.complimentaryExplanationView";

	private static final String ASIDE_MARKER_TYPE = "edu.uncc.sis.aside.AsideMarker";

	private static String ABSTRACT = "ABSTRACT\n\n";
	private static String EXPLANATION = "\n\n\n\nEXPLANATION\n\n";
	private static String REMEDIATION = "\n\n\n\nREMEDIATION RECOMMENDATION\n\n";

	private static final IMarkerResolution[] NO_RESOLUTION = new IMarkerResolution[0];

	private static String validationType;

	private IMarker targetMarker;

	private ICompilationUnit fCompilationUnit;

	private SecureProgrammingKnowledgeBase knowledgeBase;

	private Random ranGen;

	/*
	 * public 0-argument constructor as required by the extension point
	 */
	public AsideMarkerInputResolutionGenerator() {
		super();
		ranGen = new Random();
		knowledgeBase = SecureProgrammingKnowledgeBase.getInstance();
		
	}

	@Override
	public IMarkerResolution[] getResolutions(IMarker marker) {

		// display the explanation view along with resolution list if there is
		// any resolution available
		//showGuidanceInView(marker);
		IMarkerResolution[] resolutionResult = internalGetResolutions(marker);
		
		return resolutionResult;
	}

	@Override
	public boolean hasResolutions(IMarker marker) {
		return internalHasResolutions(marker);
	}

	private IMarkerResolution[] internalGetResolutions(IMarker marker) {
		fCompilationUnit = ASIDEMarkerAndAnnotationUtil.getCompilationUnit(marker);
		
		if (fCompilationUnit == null) {
			return NO_RESOLUTION;
		}

		if (!internalHasResolutions(marker)) {
			return NO_RESOLUTION;
		}

		IProject project = ASIDEMarkerAndAnnotationUtil.getProjectFromICompilationUnit(fCompilationUnit);

	    DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		  
	    //get current date time with Date()
		Date hoverDate = new Date();
	    logger.info(dateFormat.format(hoverDate) + " " + AsidePlugin.getUserId() +" hovers over the marker or clicks on the marker of input validation warning at Line "
				+ marker.getAttribute(IMarker.LINE_NUMBER, -1)
				+ " in java file <<"
				+ fCompilationUnit.getElementName()
				+ ">> in Project ["
				+ project.getName() + "]");

		LinkedList<IMarkerResolution> resolutionSet = new LinkedList<IMarkerResolution>();
        //change definedInputTypeList to tmpList, but will be assigned to definedInputTypeList after filtered
		ArrayList<String> tmpList = ESAPIPropertiesReader
				.getInstance(project).retrieveESAPIDefinedInputTypes();
		//filter for input types, some input types are filtered here for educational version use
		ArrayList<String> definedInputTypeList = ASIDEMarkerAndAnnotationUtil.filterValidationTypes(tmpList);
		///////
		//for test use
		IMarkerResolution readMoreResolution = new ReadMoreResolution(
				fCompilationUnit, marker, project, "input");
		resolutionSet.add(readMoreResolution);
		
//		for (String inputType : definedInputTypeList) {
//			System.out.println("-"+inputType);
//		}
		//

		if (validationType.equals("String")) {

			if (definedInputTypeList.isEmpty()) {
				return NO_RESOLUTION;
			}

			SyntacticValidationResolution resolution = null;
			for (String inputType : definedInputTypeList) {
				resolution = new SyntacticValidationResolution(
						fCompilationUnit, marker, inputType, project);
				resolutionSet.add(resolution);
			}
		} else if (validationType.equals("int")) {

			IMarkerResolution singleResolution = new SemanticValidationResolution(
					fCompilationUnit);

			resolutionSet.add(singleResolution);
            Date resolutionDate = new Date();
			logger.info(AsidePlugin.getUserId() + dateFormat.format(resolutionDate)+" Obtained " + resolutionSet.size() + "Resolutions.");
		} else {
			Date noResolutionDate = new Date();
			logger.info(AsidePlugin.getUserId() + dateFormat.format(noResolutionDate) + " No Resolution is available.");
		}

		IMarkerResolution ignoreResolution = new IgnoreMarkerResolution(
				fCompilationUnit, PluginConstants.INPUT_IGNORE_RANK_NUM, "input");
		resolutionSet.add(ignoreResolution);
		//newly added
		IMarkerResolution noticeResolution = new NoticeResolution(
				fCompilationUnit, PluginConstants.INPUT_NOTICE_LABEL_RANK_NUM);
		resolutionSet.add(noticeResolution);
		
		return resolutionSet
				.toArray(new IMarkerResolution[resolutionSet.size()]);
	}

	private boolean internalHasResolutions(IMarker marker) {

		try {
			String markerType = marker.getType();

			if (markerType == null || !markerType.equals(ASIDE_MARKER_TYPE)) {
				return false;
			}

			validationType = (String) marker
					.getAttribute("edu.uncc.sis.aside.marker.validationType");

			if (validationType == null) {
				//System.out.println("validationType == null in AsideMarkerInputResolutionGenerator");
				
				return false;
			}

			if (!validationType.equals("String")
					&& !validationType.equals("int")) {
				System.out.println("validationType not String and not int in AsideMarkerInputResolutionGenerator");
				return false;
			}

			String flow = (String) marker
					.getAttribute("edu.uncc.sis.aside.marker.flow");

			if (flow == null) {
				System.out.println("flow == null in AsideMarkerInputResolutionGenerator");
				return false;
			}

			String[] result = Converter.stringToStringArray(flow);
			for (int i = 0; i < result.length; i++) {
				if (result[i].equals("input")) {
					return true;
				}
			}

			return false;
		} catch (CoreException e) {
			e.printStackTrace();
			return false;
		}

	}

	/*private void showGuidanceInView(IMarker marker) {
		fCompilationUnit = ASIDEMarkerAndAnnotationUtil.getCompilationUnit(marker);
		IProject project = ASIDEMarkerAndAnnotationUtil.getProjectFromICompilationUnit(fCompilationUnit);
		 
		targetMarker = marker;
		 DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		 
		if (fCompilationUnit == null)
			return;
        Date showGuidanceDate = new Date();
		logger.info(dateFormat.format(showGuidanceDate) +" Guidance view displays corresponding detail information for the selected marker at "
				+ targetMarker.getAttribute(IMarker.LINE_NUMBER, -1)
				+ " in File <<"
				+ fCompilationUnit.getElementName()
				+ ">> in Project =="
				+ project.getName() + "==");
		IEditorPart currentEditor = ASIDEMarkerAndAnnotationUtil.getCurrentEditorPart(fCompilationUnit);
		try {
			IViewPart view = currentEditor.getSite().getPage()
					.showView(EXPLANATION_VIEW_ID);
			if (view != null && view instanceof ExplanationView) {
				ExplanationView guidanceView = (ExplanationView) view;
				StyledText text = guidanceView.getWidget();
				String guidance = getGuidanceForMarker();
				text.setText(guidance);
			}
		} catch (PartInitException e) {
			e.printStackTrace();
		}
	}

	private String getGuidanceForMarker() {

		int range = knowledgeBase
				.getEntrySize(PluginConstants.VK_INPUTVALIDATION);
		if (range == 0)
			return "Range = 0: Sorry, no information is available at this moment!";

		int index = ranGen.nextInt(range);
		while (index < 0) {// make sure that index is a positive integer
			index++;
		}

		String abstractInfo = targetMarker.getAttribute(IMarker.MESSAGE, "");
		VulnerabilityKnowledge vulnerability = knowledgeBase.getEntry(
				PluginConstants.VK_INPUTVALIDATION, index);

		if(vulnerability == null){
			System.err.println("not right in AsideMarkerInputResolutionGenerator");
			return "Vulnerability == null: This view displays the detailed information about a piece of vulnerable code.";
		}
		
		String guidance = ABSTRACT + abstractInfo + EXPLANATION
				+ vulnerability.getExplanation() + REMEDIATION
				+ vulnerability.getRemedition();
	
		return guidance;
	}*/


	
}
