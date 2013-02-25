package edu.uncc.sis.aside.utils;

import org.eclipse.core.resources.IProject;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import edu.uncc.sis.aside.AsidePlugin;
import edu.uncc.sis.aside.views.ErrorInfoDialog;
import edu.uncc.sis.aside.views.UserIDDialog;

public class ErrorInfoForm {
	public static void process(){
		Display display = Display.getCurrent(); //new Display (); ///
		if(display == null)
			System.out.println("display == null in ErrorInfoForm.java");
		final Shell shell = new Shell(display);
		
		ErrorInfoDialog errorInfoDialog = new ErrorInfoDialog(shell, 1);
		errorInfoDialog.showErrorInfoForm();
	}

}
