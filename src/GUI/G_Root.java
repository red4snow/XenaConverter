package GUI;

import javafx.scene.layout.BorderPane;

/**
 * Created by dan on 4/3/2017.
 */
public class G_Root extends BorderPane {
    public G_Root() {
        super();
        this.setTop(new G_Top());
     //   this.setLeft(new FE_G_Left());
        this.setCenter(new G_Center());
        this.setBottom(new G_Bottom());

    }

}