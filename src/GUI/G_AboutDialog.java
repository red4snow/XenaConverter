package GUI;

import MAIN.Main;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

import java.time.LocalDateTime;

public class G_AboutDialog {
    private DialogPane about_dialog_pane= new DialogPane();

    public Dialog<ButtonType> about_dialog = new Dialog<>();
    private VBox about_box = new VBox();
    private GridPane about_grid_pane= new GridPane();
    private TextArea about_text= new TextArea();


    public G_AboutDialog() {

        // Set settings
        about_dialog.setTitle("About...");



        about_text.appendText("Xena XML2XPC Version = "+ Main.VERSION+"\n");
        about_text.appendText("Created by Dan Amzulescu <dsa@xenanetworks.com>\n");
        about_text.appendText("Copyright © Xena Networks "+ LocalDateTime.now().getYear());
        about_text.setPrefRowCount(3);
        about_text.setPrefColumnCount(25);




        // Linking objects
        about_dialog.getDialogPane().getButtonTypes().add(new ButtonType("OK", ButtonBar.ButtonData.OK_DONE));
        about_grid_pane.add(new ImageView(new Image("/IMAGES/Xena_Logo.png")),1,1);
        about_grid_pane.add(about_text,1,2);
        about_box.getChildren().add(about_grid_pane);
        about_dialog_pane.setContent(about_box);

        about_dialog.setDialogPane(about_dialog_pane);

    }
}
