package idbd.bigzuoye;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class Tester extends Application
{

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws IOException
    {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("FontChooser.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        stage.setTitle("Tester");
        stage.setScene(scene);
        stage.show();
    }
}
