package at.bestsolution.efxclipse.text.test.app;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javafx.application.Application;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.ToolBar;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;
import javafx.util.Callback;

import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;

import at.bestsolution.efxclipse.runtime.application.AbstractJFXApplication;
import at.bestsolution.efxclipse.styledtext.StyledTextArea;
import at.bestsolution.efxclipse.text.SimpleSourceViewer;
import at.bestsolution.efxclipse.text.jface.rules.ITokenScanner;
import at.bestsolution.efxclipse.text.jface.source.ISourceViewer;
import at.bestsolution.efxclipse.text.jface.source.SourceViewer;
import at.bestsolution.efxclipse.text.test.app.sample.JavaSourceViewerConfiguration;
import at.bestsolution.efxclipse.text.test.app.sample.jscanners.IJavaPartitions;
import at.bestsolution.efxclipse.text.test.app.sample.jscanners.JavaTextTools;

public class MainApplication extends AbstractJFXApplication {
	protected void jfxStart(IApplicationContext applicationContext,
			Application jfxApplication, final Stage primaryStage) {
		BorderPane mainPane = new BorderPane();
//		ToolBar bar = new ToolBar();
//		final TabPane pane = new TabPane();
//		
//		{
//			Button b = new Button();
//			b.setGraphic(new ImageView(MainApplication.class.getResource("folder-development.png").toExternalForm()));
//			b.setOnAction(new EventHandler<ActionEvent>() {
//				
//				@Override
//				public void handle(ActionEvent arg0) {
//					FileChooser chooser = new FileChooser();
//					chooser.getExtensionFilters().add(new ExtensionFilter("Java-Files", "*.java"));
//					File f = chooser.showOpenDialog(primaryStage);
//					if( f != null ) {
//						Tab t = createSourceViewer(f);
//						pane.getTabs().add(t);
//						pane.getSelectionModel().select(t);
//					}
//				}
//			});
//			bar.getItems().add(b);
//		}
//		
//		{
//			Button b = new Button();
//			b.setGraphic(new ImageView(MainApplication.class.getResource("document-save.png").toExternalForm()));
//			bar.getItems().add(b);	
//		}
//		
//		mainPane.setTop(bar);
//		
//		
//		mainPane.setCenter(pane);
		
//		final Set<LineCell> activeCells = new HashSet<>();
//		ListView<Line> view = new ListView<>();
//		view.setCellFactory(new Callback<ListView<Line>, ListCell<Line>>() {
//			
//			@Override
//			public ListCell<Line> call(ListView<Line> arg0) {
//				return new LineCell(activeCells);
//			}
//		});
//		
//		ObservableList<Line> lines = FXCollections.observableArrayList();
//		
//		for( int i = 0; i < 300_000; i++ ) {
//			Line l = new Line(new StyledSegment(i+"",Color.BLUE), new StyledSegment(" - Hello World", Color.BLACK));
//			lines.add(l);
//		}
//		
//		Button b = new Button("Update it");
//		b.setOnAction(new EventHandler<ActionEvent>() {
//			@Override
//			public void handle(ActionEvent arg0) {
//				for( LineCell c : activeCells ) {
//					c.domainObject.segments.add(new StyledSegment(" A new value", Color.GREEN));
//				}
//			}
//		});
//		
//		
//		view.setItems(lines);
//		view.setMinWidth(0);
//		view.setMinHeight(0);
//		mainPane.setCenter(view);
//		
//		mainPane.setBottom(b);
		
//		StyledTextControl control = new StyledTextControl();
//		control.setText(getSample());
		
//		mainPane.setCenter(createSourceViewerPane(new File("/Users/tomschindl/git/e-fx-clipse/at.bestsolution.efxclipse.text.test.app/sample/Grid.java")));
		
		IDocument document = new Document(getFileContent(new File("/Users/tomschindl/git/e-fx-clipse/at.bestsolution.efxclipse.text.test.app/sample/Grid.java")));
		final SourceViewer viewer = new SourceViewer();
		JavaTextTools textTools = new JavaTextTools();
		viewer.configure(new JavaSourceViewerConfiguration(textTools));
		
		textTools.setupJavaDocumentPartitioner(document, IJavaPartitions.JAVA_PARTITIONING); 
		viewer.setDocument(document);
		mainPane.setCenter(viewer.getLayoutNode());
		
		Scene s = new Scene(mainPane);
		s.getStylesheets().add(MainApplication.class.getResource("test.css").toExternalForm());
		s.addEventHandler(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {

			@Override
			public void handle(KeyEvent event) {
				if( event.isControlDown() && event.getCode() == KeyCode.SPACE ) {
					System.err.println("==============> EXEC");
					viewer.doOperation(ISourceViewer.CONTENTASSIST_PROPOSALS);
				}
			}
		});
		primaryStage.setScene(s);
		primaryStage.setWidth(300);
		primaryStage.setHeight(400);
		primaryStage.setTitle("Java Source Viewer");
		primaryStage.show();
	}

	private Tab createSourceViewer(File file) {
		Tab tab = new Tab(file.getName());
		tab.setContent(createSourceViewerPane(file));
		return tab;
	}
	
	private StackPane createSourceViewerPane(File file) {
		StackPane pane = new StackPane();

		JavaTextTools t = new JavaTextTools();

		Map<String, ITokenScanner> scanners = new HashMap<>();
		scanners.put(IDocument.DEFAULT_CONTENT_TYPE, t.getCodeScanner());
		scanners.put(IJavaPartitions.JAVA_DOC, t.getJavaDocScanner());
		scanners.put(IJavaPartitions.JAVA_MULTI_LINE_COMMENT,
				t.getMultilineCommentScanner());
		scanners.put(IJavaPartitions.JAVA_SINGLE_LINE_COMMENT,
				t.getSinglelineCommentScanner());
		scanners.put(IJavaPartitions.JAVA_STRING, t.getStringScanner());
		scanners.put(IJavaPartitions.JAVA_CHARACTER, t.getStringScanner());

		StyledTextArea styledText = new StyledTextArea();
		pane.getChildren().add(styledText);
		
		IDocument doc = new Document(getFileContent(file));
		SimpleSourceViewer viewer = new SimpleSourceViewer(styledText,
				t.createDocumentPartitioner(), scanners);
		viewer.setDocument(doc);
		
		return pane;
	}
	
	private static String getFileContent(File file) {
//		StringBuilder b = new StringBuilder();
//		try {
//			FileReader r = new FileReader(file);
//			char[] buf = new char[1024];
//			int l;
//			while( (l = r.read(buf)) != -1 ) {
//				b.append(buf,0,l);
//			}
//			r.close();
//		} catch (FileNotFoundException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		StringBuilder b = new StringBuilder("package test;\n");
		b.append("\n");
		b.append("public class Test {\n");
		b.append("  public static void main(String[] args) {\n");
		b.append("    System.out.println(\"Hello World\");\n");
		b.append("  }\n");
		b.append("}");
//		StringBuilder b = new StringBuilder("\"Hello World\"private");
		return b.toString();
	}
	
	private static String getSample() {
		StringBuilder b = new StringBuilder("package test;\n");
		b.append("\n");
		b.append("public class Test {\n");
		b.append("	public static void main(String[] args) {\n");
		b.append("		System.out.println(\"Hello World\");\n");
		b.append("	}\n");
		b.append("}");
		return b.toString();
//		return getFileContent(new File("/Users/tomschindl/git/e-fx-clipse/at.bestsolution.efxclipse.text.test.app/sample/Grid.java"));
	}
}
