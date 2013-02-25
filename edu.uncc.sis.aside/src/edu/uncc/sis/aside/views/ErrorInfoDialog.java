package edu.uncc.sis.aside.views;

import java.awt.Color;
import java.awt.Desktop;
import java.awt.Font;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;

import org.eclipse.swt.*;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;

/**
 * This class demonstrates how to create your own dialog classes. It allows users
 * to input a String
 */
public class ErrorInfoDialog extends Dialog {
  private String message;
  private String input;
  private Shell shell;
  /**
   * InputDialog constructor
   * 
   * @param parent the parent
   */
  public ErrorInfoDialog(Shell parent) {
    // Pass the default styles here
    this(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
  }

  public String getInput() {
	return input;
}

public void setInput(String input) {
	this.input = input;
}

/**
   * InputDialog constructor
   * 
   * @param parent the parent
   * @param style the style
   */
  public ErrorInfoDialog(Shell parent, int style) {
    // Let users override the default styles
	
    super(parent, style);
    this.shell = parent;
    setText("Network Connection Error!");
  }

  /**
   * Opens the dialog and returns the input
   * 
   * @return String
   */
  public void showErrorInfoForm() {
    // Create the dialog window
 //   Shell shell = new Shell(getParent(), getStyle());
    shell.setText(getText());
    createContents(shell);
  //  shell.pack();
    shell.open();
    Display display = getParent().getDisplay();
    while (!shell.isDisposed()) {
      if (!display.readAndDispatch()) {
        display.sleep();
      }
    }
    
  }

  /**
   * Creates the dialog's contents
   * 
   * @param shell the dialog window
   */
  private void createContents(final Shell shell) {
	FillLayout fillLayout = new FillLayout();
	fillLayout.type = SWT.VERTICAL;
    shell.setLayout(new GridLayout(2, true));
    
   	
   // shell.setSize(6, 6);
    // Show the message
    String msg = "ASIDE needs a network connection to start. Please connect to Internet and restart Eclipse if you would like ASIDE functionality.";
    final StyledText styledText = new StyledText(shell, SWT.WRAP | SWT.BORDER);
	styledText.setText(msg);
	//styledText.setLineIndent(0, 1, 50);
	//styledText.setLineAlignment(6, 1, SWT.LEFT);
	//styledText.setLineJustify(6, 1, true);
	styledText.setEditable(false);
	GridData gridLayoutData = new GridData(GridData.FILL_BOTH);
	gridLayoutData.horizontalSpan = 2;
	styledText.setLayoutData(gridLayoutData);
	
	
    Button cancel = new Button(shell, SWT.PUSH);
    cancel.setText("Close");
    GridData data = new GridData(GridData.FILL_HORIZONTAL);
    cancel.setLayoutData(data);
    cancel.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent event) {
        input = null;
        shell.close();
      }
    });

    shell.setDefaultButton(cancel);
   // shell.pack();
   // shell.open();
  }

}
     

