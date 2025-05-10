package idbd.bigzuoye;

import javafx.animation.*;
import javafx.application.*;
import javafx.beans.binding.*;
import javafx.beans.property.*;
import javafx.beans.value.ChangeListener;
import javafx.fxml.*;
import javafx.scene.*;
import javafx.scene.layout.*;
import javafx.scene.control.*;
import javafx.scene.text.Font;
import javafx.stage.*;
import javafx.util.Duration;

import java.io.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.*;

public class MyNotepadController
{
    // 选项卡
    // 文件选项卡
//    @FXML private MenuItem miCreate;          // 由于这些选项不会出现无法操作的情况，所以不需要绑定
//    @FXML private MenuItem miOpen;
//    @FXML private MenuItem miSave;
//    @FXML private MenuItem miSaveAs;
//    @FXML private MenuItem miQuit;
    // 编辑选项卡
    @FXML private MenuItem miUndo;
    @FXML private MenuItem miRedo;
    @FXML private MenuItem miCut;
    @FXML private MenuItem miCopy;
//    @FXML private MenuItem miPaste;           // 由于这些选项不会出现无法操作的情况，所以不需要绑定
    @FXML private MenuItem miDelete;
//    @FXML private MenuItem miSearch;
//    @FXML private MenuItem miSearchPrev;
//    @FXML private MenuItem miSearchNext;
//    @FXML private MenuItem miReplace;
//    @FXML private MenuItem miSelectAll;
//    @FXML private MenuItem miDate;
//    @FXML private MenuItem miFont;
    // 查看选项卡
    @FXML private CheckMenuItem cmiWrapText;
//    @FXML private MenuItem miHelp;            // 由于这些选项不会出现无法操作的情况，所以不需要绑定
//    @FXML private MenuItem miAbout;

    // 状态栏
    @FXML private Label lblRow;
    @FXML private Label lblColumn;
    @FXML private Label lblCount;

    // 文本框
    @FXML private TextArea textArea;

    // 私有变量
    // 指示是否选择了文字
    private final BooleanProperty notSelection = new SimpleBooleanProperty(true);
    // 搜索与替换相关
    public enum SEARCHandREPLACEMODE { CLOSE, SEARCH, REPLACE }
    private SEARCHandREPLACEMODE searchAndReplaceMode = SEARCHandREPLACEMODE.CLOSE;
    private Stage stageSearchAndReplace;
    // 指示是否已保存修改，每保存一次设定为true，每修改一次设定为true
    private final BooleanProperty hadSaved = new SimpleBooleanProperty(true);
    // 当前文件路径
    private String currentFilePath = "";
    // 当前文件名称
    private String currentFileName = "";
    private final String defaultFileName = "无敌简易记事本 无标题";

    // 初始化
    @FXML public void initialize()
    {
        // 绑定
        // 文件选项卡绑定
        // 绑定是否保存修改
        textArea.textProperty().addListener(
                (obs, oldText, newText) ->
                {
                    setTitle((currentFileName.isEmpty() ? defaultFileName : currentFileName)
                            + " *");
                    hadSaved.set(false);
                }
        );

        // 编辑选项卡绑定
        miUndo.disableProperty().bind(Bindings.not(textArea.undoableProperty()));
        miRedo.disableProperty().bind(Bindings.not(textArea.redoableProperty()));
        textArea.selectedTextProperty().addListener(
                (obs, oldVal, newVal) ->
                        notSelection.set(newVal.isEmpty())
        );
        miCut.disableProperty().bind(notSelection);
        miCopy.disableProperty().bind(notSelection);
        miDelete.disableProperty().bind(notSelection);
        // 查看选项卡绑定
        textArea.wrapTextProperty().bindBidirectional(cmiWrapText.selectedProperty());
        // 状态栏绑定
        textArea.caretPositionProperty().addListener(
                (obs, oldPosition, newPosition) ->
                        updateCursorPositionInfo(newPosition.intValue())
        );
        textArea.textProperty().addListener(
                (obs, oldText, newText) ->
                        lblCount.setText(String.valueOf(newText.length()))
        );

        // 初始化
        // 状态栏初始化
        updateCursorPositionInfo(textArea.getCaretPosition());
        lblCount.setText(String.valueOf(textArea.getText().length()));
    }

    // 关联方法
    // 文件选项卡
    @FXML private void OnCreate()
    {
        System.out.println("OnCreate called");

        // 同OnQuit一样的判定逻辑
        // 新打开的文件有false、empty、empty，这种不需要保存
        if (!hadSaved.getValue() && !(currentFilePath.isEmpty() && textArea.getText().isEmpty()))
        {
            switch (showSaveDialog(
                    "是否要将旧的更改保存为txt文件？",
                    "是否要将旧的更改保存到" + currentFileName + ".txt？"))
            {
                case YES -> OnSave();
                case NO, CANCEL_CLOSE -> {}
            }
        }
        // 清空textarea
        textArea.setText("");
        // 记录当前打开文件
        currentFilePath = "";
        currentFileName = defaultFileName;
        // 重置标题
        setTitle(currentFileName);
        // 告知已保存
        hadSaved.set(true);
    }

    @FXML private void OnOpen()
    {
        System.out.println("OnOpen called");

        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("文本文档(*.txt)", "*.txt"));
        File file = fileChooser.showOpenDialog(null);

        if (file != null)
        {
            // 同OnQuit一样的判定逻辑
            // 新打开的文件有false、empty、empty，这种不需要保存
            if (!hadSaved.getValue() && !(currentFilePath.isEmpty() && textArea.getText().isEmpty()))
            {
                switch (showSaveDialog(
                        "是否要将旧的更改保存为txt文件？",
                        "是否要将旧的更改保存到" + currentFileName + ".txt？"))
                {
                    case YES -> OnSave();
                    case NO, CANCEL_CLOSE -> {}
                }
            }

            try (BufferedReader reader = new BufferedReader(new FileReader(file)))
            {
                StringBuilder content = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null)
                {
                    content.append(line).append("\n"); // 读取文件内容，注意处理换行符
                }
                // 移除最后一个换行符
                if (!content.isEmpty() && content.charAt(content.length() - 1) == '\n')
                {
                    content.deleteCharAt(content.length() - 1);
                }

                textArea.setText(content.toString());
                System.out.println("load success");

                // 记录当前打开文件
                currentFilePath = file.getAbsolutePath();
                currentFileName = file.getName();
                // 重置标题
                setTitle(currentFileName);
                // 告知已保存
                hadSaved.set(true);
            } catch (IOException e) {
                System.err.println("Error during file open and loading: " + e.getMessage());
                showInfoDialogOnlyAccept("打开文件失败: " + e.getMessage());
            }
        }
    }

    @FXML private void OnSave()
    {
        System.out.println("OnSave called");

        if (!hadSaved.getValue())
        {
            if (currentFilePath.isEmpty())
            {
                OnSaveAs();
            }
            else
            {
                File file = new File(currentFilePath);

                saveTo(file);
            }
        }
    }

    @FXML private void OnSaveAs()
    {
        System.out.println("OnSaveAs called");

        // 永远可以另存为
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("文本文档(*.txt)", "*.txt"));
        File file = fileChooser.showSaveDialog(null);

        saveTo(file);
    }

    @FXML public void OnQuit()
    {
        System.out.println("OnQuit called");

        // 检测是否保存修改，若未保存则询问是否要保存，是则调用save，若已保存或选择否则直接关闭
        // 检测是否是已打开文件，若否则询问要不要另存为，若是则看上面
        boolean needClose = true;
        // 新打开的文件有false、empty、empty，这种不需要保存
        if (!hadSaved.getValue() && !(currentFilePath.isEmpty() && textArea.getText().isEmpty()))
        {
            switch (showSaveDialog(
                    "是否要将更改保存为txt文件？",
                    "是否要将更改保存到" + currentFileName + ".txt？"))
            {
                case YES -> OnSave();
                case NO -> {}
                case CANCEL_CLOSE -> needClose = false;
            }
        }
        if (needClose)
        {
            Platform.exit();
        }
    }

    @FXML private void OnUndo()
    {
        System.out.println("OnUndo called");

        textArea.undo();
    }

    @FXML private void OnRedo()
    {
        System.out.println("OnRedo called");

        textArea.redo();
    }

    @FXML private void OnCut()
    {
        System.out.println("OnCut called");

        textArea.cut();
    }

    @FXML private void OnCopy()
    {
        System.out.println("OnCopy called");

        textArea.copy();
    }

    @FXML private void OnPaste()
    {
        System.out.println("OnPaste called");

        textArea.paste();
    }

    @FXML private void OnDelete()
    {
        System.out.println("OnDelete called");

        // 如果选择了东西才有用
        if (!notSelection.getValue())
        {
            textArea.replaceSelection("");
        }
    }

    @FXML public void OnSearch() throws IOException
    {
        System.out.println("OnSearch called");

        switch (searchAndReplaceMode)
        {
            case CLOSE:
                openSearchAndReplace(false);
                searchAndReplaceMode = SEARCHandREPLACEMODE.SEARCH;
                break;
            case SEARCH:
//                closeSearchAndReplace();  // 无反应
                break;
            case REPLACE:
                stageSearchAndReplace.close();
                openSearchAndReplace(false);
                searchAndReplaceMode = SEARCHandREPLACEMODE.SEARCH;
                break;
        }
    }

    @FXML public void OnReplace() throws IOException
    {
        System.out.println("OnReplace called");

        switch (searchAndReplaceMode)
        {
            case CLOSE:
                openSearchAndReplace(true);
                searchAndReplaceMode = SEARCHandREPLACEMODE.REPLACE;
                break;
            case SEARCH:
                stageSearchAndReplace.close();
                openSearchAndReplace(true);
                searchAndReplaceMode = SEARCHandREPLACEMODE.REPLACE;
                break;
            case REPLACE:
//                closeSearchAndReplace();  // 无反应
                break;
        }
    }

    @FXML private void OnSelectAll()
    {
        System.out.println("OnSelectAll called");

        textArea.selectAll();
    }

    @FXML private void OnDate()
    {
        System.out.println("OnDate called");

        // 插入日期
        String date = LocalDateTime.now().toString();
        int caretPos = textArea.getCaretPosition();
        String currentText = textArea.getText();
        String newText = currentText.substring(0, caretPos) + date + currentText.substring(caretPos);

        // 设置新的内容
        textArea.setText(newText);
        textArea.positionCaret(caretPos + date.length());
    }

    @FXML private void OnFont() throws IOException
    {
        System.out.println("OnFont called");

        FXMLLoader loader = new FXMLLoader(getClass().getResource("FontChooser.fxml"));
        Parent root = loader.load();
        FontChooser controller = loader.getController();

        Stage childStage = new Stage();
        childStage.initStyle(StageStyle.UTILITY);
        childStage.setTitle("无敌字体选择");
        childStage.setScene(new Scene(root));
        childStage.initModality(Modality.WINDOW_MODAL);
        childStage.initOwner(textArea.getScene().getWindow());

        childStage.showAndWait();

        Font newFont = controller.getSelectedFont();
        if (newFont != null)
        {
            textArea.setFont(newFont);
        }
    }

//    @FXML private void OnHelp()
//    {
//        System.out.println("OnHelp called");
//
//
//    }

    @FXML private void OnAbout() throws IOException
    {
        System.out.println("OnAbout called");

        FXMLLoader loader = new FXMLLoader(getClass().getResource("About.fxml"));
        Parent root = loader.load();

        Stage childStage = new Stage();
        childStage.initStyle(StageStyle.UTILITY);
        childStage.setTitle("无敌作者介绍");
        childStage.setScene(new Scene(root));
        childStage.setResizable(false);
        childStage.initModality(Modality.WINDOW_MODAL);
        childStage.initOwner(textArea.getScene().getWindow());

        childStage.showAndWait();
    }

    // 非关联函数
    // 文件相关
    private void saveTo(File file)
    {
        if (file != null)
        {
            try (FileWriter fileWriter = new FileWriter(file))
            {
                // 保存写入
                fileWriter.write(textArea.getText());
                fileWriter.flush();
                // 记录当前打开文件
                currentFilePath = file.getAbsolutePath();
                currentFileName = file.getName();
                // 重置标题
                setTitle(currentFileName);
                // 告知已保存
                hadSaved.set(true);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
    // 编辑相关
    // 搜索与替换
    private void openSearchAndReplace(boolean replace) throws IOException
    {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("SearchAndReplace.fxml"));
        Parent root = loader.load();
        SearchAndReplace controller = loader.getController();

        controller.Init(this, replace);
        controller.replacePane.setVisible(replace);
        controller.replacePane.setManaged(replace);

        Stage childStage = new Stage();
        childStage.initStyle(StageStyle.UNDECORATED);
        childStage.setScene(new Scene(root));

        Stage owner = (Stage) textArea.getScene().getWindow();
        childStage.initOwner(owner);

        // 使用 Platform.runLater 确保布局已完成，获取窗口实际大小
        Platform.runLater(() -> {
            double offsetY = 130;
            double widthRatio = 0.8; // 子窗口宽度为主窗口的60%
            double minWidth = childStage.getWidth();

            Runnable updatePositionAndSize = () -> {
                double proposedWidth = owner.getWidth() * widthRatio;
                double newWidth = Math.max(proposedWidth, minWidth);
                childStage.setWidth(newWidth);
                childStage.setHeight(replace ? 100 : 50);
                double centerX = owner.getX() + (owner.getWidth() - childStage.getWidth()) / 2;
                childStage.setX(centerX);
                childStage.setY(owner.getY() + offsetY);
            };

            // 初始设置
            updatePositionAndSize.run();

            // 监听主窗口位置和大小变化
            ChangeListener<Number> repositionListener = (obs, oldVal, newVal) -> updatePositionAndSize.run();
            owner.xProperty().addListener(repositionListener);
            owner.yProperty().addListener(repositionListener);
            owner.widthProperty().addListener(repositionListener);
            owner.heightProperty().addListener(repositionListener);
        });

        childStage.show();

        stageSearchAndReplace = childStage;
    }

    public void closeSearchAndReplace()
    {
        stageSearchAndReplace.close();
        searchAndReplaceMode = SEARCHandREPLACEMODE.CLOSE;
    }

    public void SearchPrev(String searchText, boolean caseSensitive, boolean wrapSearch)
    {
        System.out.println("Search Prev: " + searchText + ", setting: " + caseSensitive + wrapSearch);

        // 若没有选中文字，则从光标位置开始查找，否则从选中之 前 开始查找
        int startPosition = notSelection.getValue() ?
                textArea.getCaretPosition() : textArea.getSelection().getStart();
        String content = caseSensitive ? textArea.getText() : textArea.getText().toLowerCase();
        String beforeCaret = content.substring(0, startPosition);
        String searchFor = caseSensitive ? searchText : searchText.toLowerCase();

        boolean hadWrapSearch = false;
        int index;
        index = beforeCaret.lastIndexOf(searchFor);
        // 未找到且可以回绕则回绕查找
        if (index == -1 && wrapSearch)
        {
            hadWrapSearch = true;
            index = content.lastIndexOf(searchFor);
        }

        if (index != -1)
        {
            if (hadWrapSearch)
                showTemporaryTip("已从底部查找下一个");
            textArea.selectRange(index, index + searchText.length());
        }
        else
        {
            showInfoDialogOnlyAccept("找不到\"" + searchText + "\"");
        }
    }

    public int SearchNext(String searchText, boolean caseSensitive, boolean wrapSearch)
    {
        System.out.println("Search Next: " + searchText + ", setting: " + caseSensitive + wrapSearch);

        // 若没有选中文字，则从光标位置开始查找，否则从选中之 后 开始查找
        int startPosition = notSelection.getValue() ?
                textArea.getCaretPosition() : textArea.getSelection().getEnd();
        String content = caseSensitive ? textArea.getText() : textArea.getText().toLowerCase();
        String beforeCaret = content.substring(0, startPosition);
        String afterCaret = content.substring(startPosition);
        String searchFor = caseSensitive ? searchText : searchText.toLowerCase();

        boolean hadWrapSearch = false;
        int index;
        index = afterCaret.indexOf(searchFor);
        // 未找到且可以回绕则回绕查找
        if (index == -1 && wrapSearch)
        {
            hadWrapSearch = true;
            index = beforeCaret.indexOf(searchFor);
        }

        if (index != -1)
        {
            if (hadWrapSearch)
            {
                showTemporaryTip("已从顶部查找下一个");
                textArea.selectRange(index, index + searchText.length());
            }
            else
            {
                index += beforeCaret.length();
                textArea.selectRange(index, index + searchText.length());
            }
        }
        else
        {
            showInfoDialogOnlyAccept("找不到\"" + searchText + "\"");
        }

        return index;
    }

    public void Replace(String searchText, String replaceText, boolean replaceAll, boolean caseSensitive, boolean wrapSearch)
    {
        System.out.println("Replace, SearchFor: " + searchText + ", ReplaceTo: " + replaceText +
                ", setting: " + replaceAll + caseSensitive + wrapSearch);

        // 原版的替换好反直觉，微软不如我
        // 若选中了东西，则将光标移动到选中之前
        if (!notSelection.getValue())
            textArea.positionCaret(textArea.getSelection().getStart());

        if (replaceAll)
        {
            String content = wrapSearch ?
                    textArea.getText() : textArea.getText().substring(textArea.getCaretPosition());

            // 正则表达式秒了
            int flags = caseSensitive ? 0 : Pattern.CASE_INSENSITIVE;
            Pattern pattern = Pattern.compile(searchText, flags);
            Matcher matcher = pattern.matcher(content);
            String newContent = matcher.replaceAll(replaceText);

            // 检查是否需要替换
            if (!content.equals(newContent))
                textArea.setText(newContent);
        }
        else
        {
            int foundIndex = SearchNext(searchText, caseSensitive, wrapSearch);

            if (foundIndex != -1)
            {
                textArea.replaceText(foundIndex, foundIndex + searchText.length(), replaceText);
                // 替换以后移动光标方便下次替换
                textArea.positionCaret(foundIndex + replaceText.length());
            }
            else
            {
                // 没有找到匹配项
                showInfoDialogOnlyAccept("找不到\"" + searchText + "\"");
            }
        }
    }

    // 状态栏更新
    private void updateCursorPositionInfo(int caretPosition)
    {
        int row = 1; // 行号从1开始
        int column = 1; // 列号从1开始
        String text = textArea.getText();

        // 遍历文本，计算行和列
        // 从文本开头到光标位置
        for (int i = 0; i < caretPosition; i++) {
            // 遇到换行符 '\n'，行号加1，列号重置为1
            if (text.charAt(i) == '\n') {
                row++;
                column = 1;
            } else {
                // 否则，列号加1
                column++;
            }
        }

        // 更新Label显示
        lblRow.setText(String.valueOf(row));
        lblColumn.setText(String.valueOf(column));
    }

    // 修改标题
    private void setTitle(String title)
    {
        Stage stage = (Stage) textArea.getScene().getWindow();
        stage.setTitle(title);
    }

    // 创建提示
    // 确认框提示
    private void showInfoDialogOnlyAccept(String message)
    {
        Alert alert = new Alert(Alert.AlertType.NONE, message, ButtonType.OK);
        alert.setTitle("无敌简易记事本");
        alert.setHeaderText(null);
        alert.initStyle(StageStyle.UTILITY);
        alert.showAndWait();
    }

    // 纯提示
    private void showTemporaryTip(String message)
    {
        Popup popup = new Popup();

        // 创建一个 Label 用于显示文本
        Label messageLabel = new Label(message);
        messageLabel.setStyle("-fx-padding: 10px; -fx-background-color: #f0f0f0;" +
                " -fx-border-color: #cccccc; -fx-border-width: 1px;" +
                " -fx-background-radius: 5px; -fx-border-radius: 5px;" +
                " -fx-font-size: 16px;");

        // 将 Label 添加到一个 StackPane 中作为 Popup 的内容
        StackPane popupContent = new StackPane(messageLabel);
        popup.getContent().add(popupContent);

        // 设置 Popup 的 autoFix 属性为 true，尝试调整位置以保持在屏幕内
        popup.setAutoFix(true);

        // 设置 Popup 的 consumeAutoHidingEvents 属性为 false，不消耗自动隐藏事件
        // 这有助于确保点击其他地方不会阻止 Timeline 关闭
        popup.setConsumeAutoHidingEvents(false);

        // 使用 Timeline 来在一段时间后关闭 Popup
        Timeline timeline = new Timeline(new KeyFrame(Duration.millis(500), event ->
        {
            popup.hide(); // 隐藏 Popup
        }));

        // 显示 Popup
        popup.show(textArea.getScene().getWindow());

        // 启动 Timeline
        timeline.play();
    }

    // 保存提示
    private ButtonBar.ButtonData showSaveDialog(String notCurrentFile, String hasCurrentFile)
    {
        Optional<ButtonType> result;
        Alert alert = new Alert(Alert.AlertType.NONE,
                currentFilePath.isEmpty() ? notCurrentFile : hasCurrentFile);

        alert.setTitle("无敌简易记事本");
        alert.setHeaderText(null);
        alert.initStyle(StageStyle.UTILITY);

        alert.getButtonTypes().setAll(
                new ButtonType("保存", ButtonBar.ButtonData.YES),
                new ButtonType("不保存", ButtonBar.ButtonData.NO),
                new ButtonType("取消", ButtonBar.ButtonData.CANCEL_CLOSE)
        );

        result = alert.showAndWait();

        return result.map(ButtonType::getButtonData).orElse(null);  // idea自动改的，我看不懂
    }
}
