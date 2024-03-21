package client.scenes;

import client.utils.ServerUtils;
import commons.Event;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;

import javax.inject.Inject;
import java.awt.*;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.BrokenBarrierException;

public class AdminEventsCtrl {
    private final MainCtrl mainCtrl;

    private final ServerUtils server;

    @FXML
    private ListView<BorderPane> myListView;

    private List<Event> events;

    @Inject
    public AdminEventsCtrl(MainCtrl mainCtrl, ServerUtils server) {
        this.mainCtrl = mainCtrl;
        this.server = server;
    }

    private void removeEvent(Event e) {
        server.removeEvent(e.getId());
        populateList();
    }

    private void downloadEvent(Event e) {
        System.out.println(e.getName());
    }

    public void back(){
        mainCtrl.showSettings();
    }


    public void populateList() {
        myListView.getItems().clear();
        this.events = server.getEvents();
        List<BorderPane> contents = events.stream().map(e -> {
            Insets insets = new Insets(0.0, 5.0, 0.0, 5.0);
            BorderPane bp = new BorderPane();
            bp.setLeft(new Text(e.getName()));

            BorderPane innerBp = new BorderPane();
            innerBp.setMaxWidth(40.0);
            innerBp.setMaxHeight(15.0);

            Image removeImage = new Image("client/icons/bin.png");
            ImageView remove = new ImageView();
            remove.setImage(removeImage);
            remove.setOnMouseClicked(x -> removeEvent(e));
            remove.cursorProperty().set(Cursor.HAND);
            remove.setFitHeight(12.0);
            remove.setPickOnBounds(true);
            remove.setFitWidth(12.0);
            innerBp.setRight(remove);
            BorderPane.setMargin(remove, insets);

            Image downloadImage = new Image("client/icons/downloads.png");
            ImageView download = new ImageView();
            download.setImage(downloadImage);
            download.setOnMouseClicked(x -> downloadEvent(e));
            download.cursorProperty().set(Cursor.HAND);
            download.setFitHeight(12.0);
            download.setPickOnBounds(true);
            download.setFitWidth(12.0);
            innerBp.setLeft(download);
            BorderPane.setMargin(download, insets);

            bp.setRight(innerBp);
            return bp;
        }).toList();
        myListView.getItems().addAll(contents);
    }
}
