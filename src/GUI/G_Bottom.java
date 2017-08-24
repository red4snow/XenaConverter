package GUI;

import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;

public class G_Bottom extends TabPane {


    private TabPane xpclog_subtabpane = new TabPane ();
    private Tab debug_log_tab = new Tab("Debug Log");

    public  static TextArea debug_log_txt= new TextArea();

    public G_Bottom() {
        super();

        debug_log_txt.setEditable(false);
        debug_log_tab.setClosable(false);

        debug_log_tab.setContent(debug_log_txt);

        this.getTabs().addAll(debug_log_tab);

    }
}
