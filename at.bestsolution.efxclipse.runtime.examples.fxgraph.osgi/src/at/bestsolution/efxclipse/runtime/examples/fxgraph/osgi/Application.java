package at.bestsolution.efxclipse.runtime.examples.fxgraph.osgi;

import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.fxml.FXMLLoader;
import javafx.fxml.JavaFXBuilderFactory;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import at.bestsolution.efxclipse.runtime.application.AbstractJFXApplication;
import at.bestsolution.efxclipse.runtime.examples.fxgraph.osgi.model.User;
import at.bestsolution.efxclipse.runtime.examples.fxgraph.osgi.security.Authenticator;

/**
 * This class controls all aspects of the JavaFX OSGi application's execution
 */
public class Application extends AbstractJFXApplication {
	private Stage stage;
    private User loggedUser;
    
    private static Application instance;
	
    public Application() {
		instance = this;
	}
    
    public static Application getInstance() {
        return instance;
    }
    
	protected void jfxStart(Stage primaryStage) {
		try {
            stage = primaryStage;
            gotoLogin();
            primaryStage.show();
        } catch (Exception ex) {
            Logger.getLogger(Application.class.getName()).log(Level.SEVERE, null, ex);
        }
	}
	
	public User getLoggedUser() {
        return loggedUser;
    }
        
    public boolean userLogging(String userId, String password){
        if (Authenticator.validate(userId, password)) {
            loggedUser = User.of(userId);
            gotoProfile();
            return true;
        } else {
            return false;
        }
    }
    
    public void userLogout(){
        loggedUser = null;
        gotoLogin();
    }
    
    private void gotoProfile() {
        try {
            replaceSceneContent("profile.fxml");
        } catch (Exception ex) {
            Logger.getLogger(Application.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void gotoLogin() {
        try {
            replaceSceneContent("login.fxml");
        } catch (Exception ex) {
            Logger.getLogger(Application.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private AnchorPane replaceSceneContent(String fxml) throws Exception {
        AnchorPane page = (AnchorPane) FXMLLoader.load(Application.class.getResource(fxml), null, new JavaFXBuilderFactory());
        Scene scene = stage.getScene();
        if (scene == null) {
            scene = new Scene(page);
            scene.getStylesheets().add(Application.class.getResource("demo.css").toExternalForm());
            stage.setScene(scene);
        } else {
            stage.getScene().setRoot(page);
        }
        stage.sizeToScene();
        return page;
    }
}