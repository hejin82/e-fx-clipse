package at.bestsolution.efxclipse.runtime.di.internal;

import javax.swing.JPanel;

import javafx.embed.swing.JFXPanel;
import javafx.embed.swt.FXCanvas;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;

import org.eclipse.e4.core.contexts.ContextFunction;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

@SuppressWarnings("restriction")
public class InteroptContextFunction extends ContextFunction {

	@Override
	public Object compute(IEclipseContext context) {
		System.err.println("Computing value");
		
		Object comp = context.get("org.eclipse.swt.widgets.Composite");
		
		if( comp != null ) {
			BorderPane pane = new BorderPane();
			FXCanvas canvas = new FXCanvas((Composite) comp, SWT.NONE);
			canvas.setScene(new Scene(pane));
			
			return pane;
		} else {
			JPanel jpanel = (JPanel) context.get("javax.swing.JPanel");
			
			if( jpanel != null ) {
				BorderPane pane = new BorderPane();
				JFXPanel fxPanel = new JFXPanel();
				fxPanel.setScene(new Scene(pane));
				jpanel.add(fxPanel);
				
				return pane;
			}
		}
		
		return null;
	}

}
