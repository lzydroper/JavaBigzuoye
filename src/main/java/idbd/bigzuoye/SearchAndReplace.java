package idbd.bigzuoye;

import javafx.fxml.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.io.IOException;

public class SearchAndReplace
{
    private MyNotepadController controller;
    @FXML public HBox replacePane;
    // 展开按钮
    @FXML private Button btnUnfold;
    private boolean toUnfold;
    // 搜索
    @FXML private TextField tfSearch;
    @FXML private TextField tfReplace;
//    private boolean lastSearchPrev = false;     // 上次搜索选择的是上一个还是下一个，默认为下一个
    // 设置搜索模式
    @FXML private CheckMenuItem cmiCaseSensitive;
    @FXML private CheckMenuItem cmiWrapSearch;

    public void Init(MyNotepadController myNotepadController, boolean _toUnfold_)
    {
        controller = myNotepadController;
        btnUnfold.setText(_toUnfold_ ? "﹀" : "︿");
        toUnfold = !_toUnfold_;
    }

    @FXML private void OnUnfold() throws IOException   // fxml在包装后用户无法更改，无意外
    {
        if (toUnfold)
        {
            btnUnfold.setText("﹀");
            controller.OnReplace();
        }
        else
        {
            btnUnfold.setText("︿");
            controller.OnSearch();
        }
        toUnfold = !toUnfold;
    }

    @FXML private void OnSearch()
    {
//        if (lastSearchPrev)       // 原来那个按钮只能向下搜索
//            OnSearchPrev();
//        else
            OnSearchNext();
    }

    @FXML private void OnSearchPrev()
    {
        if (!tfSearch.getText().isEmpty())
        {
//            lastSearchPrev = true;
            controller.SearchPrev(tfSearch.getText(), cmiCaseSensitive.isSelected(), cmiWrapSearch.isSelected());
        }
    }

    @FXML private void OnSearchNext()
    {
        if (!tfSearch.getText().isEmpty())
        {
//            lastSearchPrev = false;
            controller.SearchNext(tfSearch.getText(), cmiCaseSensitive.isSelected(), cmiWrapSearch.isSelected());
        }
    }

    @FXML private void OnQuit()
    {
        controller.closeSearchAndReplace();
    }

    @FXML private void OnReplace()      // 原来原版的也只能向下替换啊
    {
        if (!tfSearch.getText().isEmpty())
        {
            controller.Replace(tfSearch.getText(), tfReplace.getText(), false,
                    cmiCaseSensitive.isSelected(), cmiWrapSearch.isSelected());
        }
    }

    @FXML private void OnReplaceAll()
    {
        if (!tfSearch.getText().isEmpty())
        {
            controller.Replace(tfSearch.getText(), tfReplace.getText(), true,
                    cmiCaseSensitive.isSelected(), cmiWrapSearch.isSelected());
        }
    }
}