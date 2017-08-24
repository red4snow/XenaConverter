package GUI;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;

import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;


/**
 * Created by dan on 4/3/2017.
 */
public class G_Center extends TabPane {

    private Tab log_tab = new Tab("Convert Log");
    private HBox log_hbox = new HBox();

    private Tab xmllog_subtab = new Tab("XML");
    private TabPane xmllog_subtabpane = new TabPane ();
    private Tab xpclog_subtab = new Tab("XPC");
    private TabPane xpclog_subtabpane = new TabPane ();
    private Tab debug_log_tab = new Tab("Debug Log");



    public  static TextArea xmllog_txt= new TextArea();
    public  static TextArea xpclog_txt= new TextArea();
    //public  static TextArea debug_log_txt= new TextArea();




    public G_Center() {
        super();

        // Creating Items --------------------------------------------------------------------------------------------


        // Setting Items -----------------------------------------------------------------------------------------------

        log_tab.setClosable(false);
        debug_log_tab.setClosable(false);
        xmllog_subtab.setClosable(false);
        xpclog_subtab.setClosable(false);

        xmllog_txt.setEditable(false);
        xpclog_txt.setEditable(false);
        //debug_log_txt.setEditable(false);


        // Connecting Items -------------------------------------------------------------------------------------------

        log_hbox.getChildren().addAll(xmllog_subtabpane,xpclog_subtabpane);
        xmllog_subtabpane.getTabs().add(xmllog_subtab);
        xpclog_subtabpane.getTabs().add(xpclog_subtab);
        log_tab.setContent(log_hbox);


        xmllog_subtab.setContent(xmllog_txt);
        xpclog_subtab.setContent(xpclog_txt);
        //debug_log_tab.setContent(debug_log_txt);

        this.getTabs().addAll(log_tab/*,debug_log_tab*/);



    }

}