package MAIN;

import GUI.G_Root;
import javafx.application.Application;

import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class Main extends Application {

    final static public String VERSION = "0.0.6";

    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = new G_Root();

        primaryStage.setTitle("Xena XML2XPC Converter "+VERSION);

        primaryStage.getIcons().add(new Image("/IMAGES/Xena_Icon.png"));

        Scene mainScene=new Scene(root, 956, 900);

        primaryStage.setScene(mainScene);

        primaryStage.setMaxWidth(956);
        primaryStage.setMinWidth(500);
        primaryStage.show();

    }


    public static void main(String[] args) {
        launch(args);
    }
}
