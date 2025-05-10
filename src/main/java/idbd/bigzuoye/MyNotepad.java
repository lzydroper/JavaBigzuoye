package idbd.bigzuoye;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;

public class MyNotepad extends Application
{

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws IOException
    {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("MyNotepad.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        stage.setTitle("无敌简易记事本 无标题");
        stage.getIcons().add(new Image(getClass().getResourceAsStream("icon.png")));
        stage.setScene(scene);
        stage.show();

        MyNotepadController controller = fxmlLoader.getController();

        stage.setOnCloseRequest(event ->
        {
            // 阻止默认的关闭行为
            event.consume();
            // 调用自定义的 OnQuit 方法
            controller.OnQuit();
        });
    }
}
