package edu.uncc.sis.aside.markers;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;

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
import edu.uncc.sis.aside.constants.PluginConstants;
import edu.uncc.sis.aside.utils.ASIDEMarkerAndAnnotationUtil;
import edu.uncc.sis.aside.utils.Converter;
import edu.uncc.sis.aside.views.ExplanationView;

public class AsideMarkerOutputResolutionGenerator implements
		IMarkerResolutionGenerator, IMarkerResolutionGenerator2 {

	private static final Logger logger = AsidePlugin.getLogManager().getLogger(
			AsideMarkerOutputResolutionGenerator.class.getName());
	private static final String EXPLANATION_VIEW_ID = "edu.uncc.sis.aside.views.complimentaryExplanationView";

	// It is a batch model, one click will lead to multi-fixes

	private static final IMarkerResolution[] NO_RESOLUTION = new IMarkerResolution[0];

	// currently there are 13 encoding strategies documented in ESAPI, adding
	// ignore solution
	private static final int SIZE = PluginConstants.ENCODING_TYPES.length + 1;

	private IMarker targetMarker;

	private ICompilationUnit fCompilationUnit;

	/*
	 * public 0-argument constructor as required by the extension point
	 */
	public AsideMarkerOutputResolutionGenerator() {
		super();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.IMarkerResolutionGenerator#getResolutions(org.eclipse.
	 * core.resources.IMarker)
	 */
	@Override
	public IMarkerResolution[] getResolutions(IMarker marker) {

		//showGuidanceInView(marker);
		return internalGetResolutions(marker);
	}

	@Override
	public boolean hasResolutions(IMarker marker) {
		return internalHasResolution(marker);
	}

	private boolean internalHasResolution(IMarker marker) {

		try {
			String markerType = marker.getType();

			if (markerType == null
					|| !markerType.equals(PluginConstants.ASIDE_MARKER_TYPE)) {
				return false;
			}

			String flow = (String) marker
					.getAttribute("edu.uncc.sis.aside.marker.flow");

			if (flow == null) {
				return false;
			}

			String[] result = Converter.stringToStringArray(flow);
			for (int i = 0; i < result.length; i++) {
				if (result[i].equals("output")) {
					return true;
				}
			}

			return false;
		} catch (CoreException e) {
			e.printStackTrace();
			return false;
		}

	}

	private IMarkerResolution[] internalGetResolutions(IMarker marker) {

		fCompilationUnit = ASIDEMarkerAndAnnotationUtil
				.getCompilationUnit(marker);

		IProject project = ASIDEMarkerAndAnnotationUtil.getProjectFromICompilationUnit(fCompilationUnit);
		  DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		  
		    //get current date time with Date()
			Date hoverDate = new Date();
		    logger.info(dateFormat.format(hoverDate) + " " + AsidePlugin.getUserId() +" hovers over the marker or clicks on the marker of output encoding warning at Line "
				+ marker.getAttribute(IMarker.LINE_NUMBER, -1)
				+ " in java file <<"
				+ fCompilationUnit.getElementName() + ">> in Project ["
				+ project.getName() + "]");

		if (!internalHasResolution(marker)) {
			return NO_RESOLUTION;
		}

		LinkedList<IMarkerResolution> resolutions = new LinkedList<IMarkerResolution>();
		//for test use
				IMarkerResolution readMoreResolution = new ReadMoreResolution(
						fCompilationUnit, marker, project, "output");
				resolutions.add(readMoreResolution);
				
		String type = null;
		for (int i = 0; i < SIZE - 1; i++) {
			type = PluginConstants.ENCODING_TYPES[i];
			IMarkerResolution encodingResolution = new EncodingResolution(fCompilationUnit,
					type);
			resolutions.add(encodingResolution);
		}

		IMarkerResolution ignoreResolution = new IgnoreMarkerResolution(fCompilationUnit, PluginConstants.OUTPUT_IGNORE_RANK_NUM, "output");
		resolutions.add(ignoreResolution);

		//newly added
				IMarkerResolution noticeResolution = new NoticeResolution(
						fCompilationUnit, PluginConstants.OUTPUT_NOTICE_LABEL_RANK_NUM);
				resolutions.add(noticeResolution);
				
		return resolutions.toArray(new IMarkerResolution[resolutions.size()]);

	}

//	private void showGuidanceInView(IMarker marker) {
//		fCompilationUnit = ASIDEMarkerAndAnnotationUtil
//				.getCompilationUnit(marker);
//		IProject project = ASIDEMarkerAndAnnotationUtil.getProjectFromICompilationUnit(fCompilationUnit);
//		   
//		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
//		 
//		targetMarker = marker;
//		if (fCompilationUnit == null)
//			return;
//		 Date showGuidanceDate = new Date();
//	     logger.info(dateFormat.format(showGuidanceDate) +" Guidance view displays corresponding detail information for the selected marker at "
//				+ targetMarker.getAttribute(IMarker.LINE_NUMBER, -1)
//				+ " in File <<"
//				+ fCompilationUnit.getElementName()
//				+ ">> in Project =="
//				+ project.getName() + "==");
//	     
//		IEditorPart currentEditor = ASIDEMarkerAndAnnotationUtil
//				.getCurrentEditorPart(fCompilationUnit);
//		try {
//			IViewPart view = currentEditor.getSite().getPage()
//					.showView(EXPLANATION_VIEW_ID);
//			if (view != null && view instanceof ExplanationView) {
//				ExplanationView guidanceView = (ExplanationView) view;
//				StyledText text = guidanceView.getWidget();
//				// TODO: hardcode the content for now
//				String guidance = "explanation for the warning, this is hardcoded only for demo purpose.";
//				text.setText(guidance);
//			}
//		} catch (PartInitException e) {
//			e.printStackTrace();
//		}
//	}

}
