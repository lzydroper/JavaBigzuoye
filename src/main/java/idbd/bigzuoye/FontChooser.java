package idbd.bigzuoye;

import javafx.collections.FXCollections;
import javafx.fxml.*;
import javafx.scene.control.*;
import javafx.scene.text.Font;
import javafx.stage.Stage;

public class FontChooser
{
    @FXML private ComboBox<String> fontFamilyComboBox;
    @FXML private ComboBox<Integer> fontSizeComboBox;
    @FXML private TextArea previewTextArea;
    @FXML private Button applyButton;
    @FXML private Button cancelButton;

    private Font selectedFont;

    public Font getSelectedFont()
    {
        return selectedFont;
    }

    @FXML public void initialize()
    {
        selectedFont = null;

        // 加载系统字体
        fontFamilyComboBox.setItems(FXCollections.observableArrayList(Font.getFamilies()));
        fontFamilyComboBox.getSelectionModel().select("System");

        // 设置常用字号
        fontSizeComboBox.setItems(FXCollections.observableArrayList(
                8, 10, 12, 14, 16, 18, 20, 24, 28, 32, 36, 48, 72));
        fontSizeComboBox.getSelectionModel().select(Integer.valueOf(12));

        // 添加监听器预览字体
        fontFamilyComboBox.setOnAction(e -> updatePreview());
        fontSizeComboBox.setOnAction(e -> updatePreview());
    }

    private void updatePreview()
    {
        String family = fontFamilyComboBox.getValue();
        Integer size = fontSizeComboBox.getValue();
        if (family != null && size != null) {
            Font font = Font.font(family, size);
            previewTextArea.setFont(font);
        }
    }

    @FXML private void onApply()
    {
        String family = fontFamilyComboBox.getValue();
        Integer size = fontSizeComboBox.getValue();
        if (family != null && size != null) {
            selectedFont = Font.font(family, size);
        }
        ((Stage) applyButton.getScene().getWindow()).close();
    }

    @FXML private void onCancel()
    {
        selectedFont = null;
        ((Stage) cancelButton.getScene().getWindow()).close();
    }
}
