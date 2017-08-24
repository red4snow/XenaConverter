package GUI;


import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import CONVERTER.C_Converter;

import java.io.File;



public class G_Top extends VBox {

    public static Button parse_btn = new Button("Parse");
    public static Button convert_btn = new Button("Convert");
    public static Button load_btn = new Button("Load XML");
    public static Button clearlogs_btn = new Button("Clear Logs");
    public static TextField path_txtfld = new TextField("C:\\Users\\dan\\Desktop\\3.xml");

    private VBox vb = new VBox();
    private MenuBar mb = new MenuBar();
    private Menu file_menu = new Menu("File");
    private MenuItem file_submitem_Open = new MenuItem("Open");
    private MenuItem file_submitem_Exit = new MenuItem("Exit");
    private Menu help_menu = new Menu("Help");
    private MenuItem help_submitem_About = new MenuItem("About");

    private G_AboutDialog about = new G_AboutDialog();



    public G_Top() {
        super();

        // Creating Items --------------------------------------------------------------------------------------------


        ToolBar tb = new ToolBar();

        final FileChooser fileChooser = new FileChooser();

        C_Converter parser = new C_Converter();

        //Setting Items --------------------------------------------------------------------------------------------




        fileChooser.setTitle("Select XML to Convert");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("XML", "*.xml"));
        path_txtfld.setDisable(true);

        load_btn.setOnAction(
                new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(final ActionEvent e) {
                        File file = fileChooser.showOpenDialog(new Stage());
                        if (file != null) {
                            path_txtfld.setText(file.getAbsolutePath());
                        }
                    }
        });

        file_submitem_Open.setOnAction(
                new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(final ActionEvent e) {
                        File file = fileChooser.showOpenDialog(new Stage());
                        if (file != null) {
                            path_txtfld.setText(file.getAbsolutePath());
                        }
                    }
        });

        file_submitem_Exit.setOnAction(
                new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(final ActionEvent e) {
                        Platform.exit();
                        System.exit(0);
                    }
                });

        help_submitem_About.setOnAction(
                new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(final ActionEvent e) {
                        //
                        about.about_dialog.showAndWait();
                    }
                });

        parse_btn.setOnAction( new EventHandler<ActionEvent>() { @Override
            public void handle(final ActionEvent e) {
                parser.parse(false);
            }
         });

        convert_btn.setOnAction( new EventHandler<ActionEvent>() { @Override
        public void handle(final ActionEvent e) {
            parser.parse(true);
        }
        });

        clearlogs_btn.setOnAction( new EventHandler<ActionEvent>() { @Override
            public void handle(final ActionEvent e) {
                Platform.runLater(new Runnable() { @Override public void run() {G_Center.xmllog_txt.clear();}});
                Platform.runLater(new Runnable() { @Override public void run() {G_Center.xpclog_txt.clear();}});
                Platform.runLater(new Runnable() { @Override public void run() {G_Bottom.debug_log_txt.clear();}});
            }
        });

        // connecting Items --------------------------------------------------------------------------------------------
       // about_box.getChildren().add(new Image("/IMAGES/Xena_Logo.png"))
       // about_pane.setContent(about_box);

        tb.getItems().addAll(load_btn,new Separator(),parse_btn,convert_btn,new Separator(),clearlogs_btn,new Separator(),path_txtfld);
        file_menu.getItems().addAll(file_submitem_Open,new SeparatorMenuItem(),file_submitem_Exit);
        help_menu.getItems().add(help_submitem_About);
        mb.getMenus().add(file_menu);
        mb.getMenus().add(help_menu);
        vb.getChildren().addAll(mb, tb);

        about.about_dialog.getDialogPane().getButtonTypes().add(new ButtonType("OK", ButtonBar.ButtonData.OK_DONE));


        this.getChildren().add(vb);

    }

}
