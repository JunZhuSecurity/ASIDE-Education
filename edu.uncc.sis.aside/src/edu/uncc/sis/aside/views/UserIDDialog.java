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
public class UserIDDialog extends Dialog {
  private String message;
  private String input;
  private Shell shell;
  /**
   * InputDialog constructor
   * 
   * @param parent the parent
   */
  public UserIDDialog(Shell parent) {
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
  public UserIDDialog(Shell parent, int style) {
    // Let users override the default styles
	
    super(parent, style);
    this.shell = parent;
    setText("ASIDE Usage Logs Submission");
  }

  /**
   * Opens the dialog and returns the input
   * 
   * @return String
   */
  public void showConsentForm() {
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
    final String asideLink = "http://hci.uncc.edu/tomcat/ASIDE/ASIDEoverview.jsp";
    String msg = "Welcome to ASIDE! \n\nASIDE is a research tool for detecting security vulnerabilities in Java Code, while you program. For more information on ASIDE or this research, please see [http://hci.uncc.edu/tomcat/ASIDE/ASIDEoverview.jsp] or contact Michael Whitney at ASIDEplugin@gmail.com. \n\nThis tool is currently being used as part of a research study. If you are a part of that study, the tool will automatically submit logs of your interaction back to the research team. These logs will only contain information about how you used ASIDE, if you choose to use it but not about your code. \n\n Please provide your UNCC email address (i.e., JoeNiner@uncc.edu) to begin. If you would like to remove yourself from the study, email Michael Whitney at ASIDEplugin@gmail.com.";
    String additionalMsg = "Note, you can stop at any time by contacting Michael Whitney at ASIDEplugin@gmail.com";
    final StyledText styledText = new StyledText(shell, SWT.WRAP | SWT.BORDER);
	styledText.setText(msg);
	//styledText.setLineIndent(0, 1, 50);
	//styledText.setLineAlignment(6, 1, SWT.LEFT);
	//styledText.setLineJustify(6, 1, true);
	styledText.setEditable(false);
	GridData gridLayoutData = new GridData(GridData.FILL_BOTH);
	gridLayoutData.horizontalSpan = 2;
	styledText.setLayoutData(gridLayoutData);
	
	StyleRange style = new StyleRange();
	style.underline = true;
	style.underlineStyle = SWT.UNDERLINE_LINK;
	
	int[] ranges = {msg.indexOf(asideLink), asideLink.length()}; 
	StyleRange[] styles = {style};
	styledText.setStyleRanges(ranges, styles);
	
	styledText.addListener(SWT.MouseDown, new Listener() {
		public void handleEvent(Event event) {
				try {
					int offset = styledText.getOffsetAtLocation(new Point(event.x, event.y));
					StyleRange style = styledText.getStyleRangeAtOffset(offset);
					if (style != null && style.underline && style.underlineStyle == SWT.UNDERLINE_LINK) {
						System.out.println("Click on a Link");
						String url = asideLink;
						Runtime rt = Runtime.getRuntime();
						try {
							String os = System.getProperty("os.name").toLowerCase();
							 if (os.indexOf( "win" ) >= 0) {
								 
							        // this doesn't support showing urls in the form of "page.html#nameLink" 
							        rt.exec( "rundll32 url.dll,FileProtocolHandler " + url);
						 
							    } else if (os.indexOf( "mac" ) >= 0) {
						 
							        rt.exec( "open " + url);
						 
						            } else if (os.indexOf( "nix") >=0 || os.indexOf( "nux") >=0) {
						 
							        // Do a best guess on unix until we get a platform independent way
							        // Build a list of browsers to try, in this order.
							        String[] browsers = {"epiphany", "firefox", "mozilla", "konqueror",
							       			             "netscape","opera","links","lynx"};
						 
							        // Build a command string which looks like "browser1 "url" || browser2 "url" ||..."
							        StringBuffer cmd = new StringBuffer();
							        for (int i=0; i<browsers.length; i++)
							            cmd.append( (i==0  ? "" : " || " ) + browsers[i] +" \"" + url + "\" ");
						 
							        rt.exec(new String[] { "sh", "-c", cmd.toString() });
						 
						           } else {
						                return;
						           }
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				} catch (IllegalArgumentException e) {
					// no character under event.x, event.y
				}
				
			
		}
	});
	//styledText.setForegroundColor(Color.darkGray);
	
	final Label input_label = new Label(shell, SWT.NONE);
	input_label.setText("UNCC email (i.e., JoeNiner@uncc.edu)");
	    GridData data40 = new GridData(GridData.FILL_HORIZONTAL);
	    data40.horizontalSpan = 2;
	    input_label.setLayoutData(data40);
	    
    // Display the input box
    final Text text = new Text(shell, SWT.BORDER);
    GridData data = new GridData(GridData.FILL_HORIZONTAL);
    data.horizontalSpan = 2;
    text.setLayoutData(data);

    
    /*final String agreement = "Agree to submit ASIDE logs back to us for research use";
    final Button checkBox = new Button(shell, SWT.CHECK);
    checkBox.setText(agreement);
    data = new GridData(GridData.FILL_HORIZONTAL);
    data.horizontalSpan = 2;
    checkBox.setLayoutData(data);*/
 
    final Label label4 = new Label(shell, SWT.NONE);
   // label4.setText("");
    GridData data4 = new GridData(GridData.FILL_HORIZONTAL);
    data4.horizontalSpan = 2;
    label4.setLayoutData(data4);
    
    final Label label5 = new Label(shell, SWT.NONE);
    label5.setText(additionalMsg);
     GridData data5 = new GridData(GridData.FILL_HORIZONTAL);
     data5.horizontalSpan = 2;
     label5.setLayoutData(data5);
    
    // Create the OK button and add a handler
    // so that pressing it will set input
    // to the entered value
    Button ok = new Button(shell, SWT.PUSH);
    ok.setText("Begin");
    data = new GridData(GridData.FILL_HORIZONTAL);
    ok.setLayoutData(data);
    ok.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent event) {
        input = text.getText();
       // boolean checked = checkBox.getSelection();
       // System.out.println("checked = " + checked);
        System.out.println("input=" + input);
        if(input != null && !input.equals("")){
          shell.close();
        }
        else{
        	label4.setText("*Please enter your UNCC email address and then submit");
        	
        	Display display = getParent().getDisplay();
        	label4.setForeground(display.getSystemColor(SWT.COLOR_RED));
        	
        }
      }
    });

    // Create the cancel button and add a handler
    // so that pressing it will set input to null
   /* Button cancel = new Button(shell, SWT.PUSH);
    cancel.setText("Not Participate");
    data = new GridData(GridData.FILL_HORIZONTAL);
    cancel.setLayoutData(data);
    cancel.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent event) {
        input = null;
        shell.close();
      }
    });*/

    shell.setDefaultButton(ok);
   // shell.pack();
   // shell.open();
  }

}
     
