package idbd.bigzuoye;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.stage.Stage;

public class About
{
    @FXML private Button btnConfirm;
    @FXML private void OnConfirm()
    {
        ((Stage)btnConfirm.getScene().getWindow()).close();
    }
}