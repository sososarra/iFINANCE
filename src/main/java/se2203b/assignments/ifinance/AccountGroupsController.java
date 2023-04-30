package se2203b.assignments.ifinance;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.net.URL;
import java.sql.SQLException;
import java.util.*;

public class AccountGroupsController implements Initializable {
    @FXML
    private ContextMenu editMenu;
    @FXML
    private TreeView<String> treeView;

    TreeItem<String> Child = null;

    int counter =0;

    @FXML
    TextField nameTxt;

    @FXML
    private Button save;
    @FXML
    Button exit;

    @FXML
    private MenuItem add;
    @FXML
    private MenuItem change;
    @FXML
    private MenuItem delete;

    private TreeItem<String> itemNode;

    private TreeItem<String> assets = new TreeItem<>("Assets");;
    private TreeItem<String> liabilities = new TreeItem<>("Liabilities");
    private TreeItem<String> income = new TreeItem<>("Income");
    private TreeItem<String> expenses = new TreeItem<>("Expenses");;



    private ObservableList<AccountCategory> catList = FXCollections.observableArrayList();
    private ObservableList<Group> groupList = FXCollections.observableArrayList();
    private GroupAdapter groupAdapter;
    private ArrayList TreeItems;
    TreeItem<String> subRoot = null;
    private String selectedItemString;

    private AccountCategoryAdapter accountCategoryAdapter;
    private IFinanceController iFinanceController;
    private Boolean changeName = false;
    private Boolean addGroup = false;

    public void setAdapters(AccountCategoryAdapter accountCategoryAdapter,GroupAdapter groupAdapter, IFinanceController iFinanceController) throws SQLException {
        this.accountCategoryAdapter = accountCategoryAdapter;
        this.groupAdapter = groupAdapter;
        this.iFinanceController = iFinanceController;
        fillView();
    }


    public void fillView() throws SQLException {
        try {
            catList.addAll(accountCategoryAdapter.getAccCategoryList());
        } catch (SQLException ex) {
            iFinanceController.displayAlert("Account Category List: " + ex.getMessage());
        }
        try {
            groupList.addAll(groupAdapter.getGroupList());
        } catch (SQLException ex) {
            iFinanceController.displayAlert("Group List: " + ex.getMessage());
        }

        TreeItem<String> dummyRoot = new TreeItem<>();

        treeView.setRoot(dummyRoot);
        treeView.setShowRoot(false);

        dummyRoot.getChildren().addAll(assets, liabilities, income, expenses);


        TreeItems = new ArrayList<>();

        for (Group group : groupList) {
            TreeItem<String> treeItem = new TreeItem<>(group.getName());
            TreeItems.add(group.getID(),treeItem);
        }

        TreeItem<String> subRoot;
        TreeItem<String> root;

        for (Group group : groupList) {
            subRoot = (TreeItem<String>) TreeItems.get(group.getID());
            root = (TreeItem<String>) TreeItems.get(group.getParent().getID());

            if (group.getParent().getID() == 0) {
                if (group.getElement().getName().equals("Assets")) {
                    root = assets;
                } else if (group.getElement().getName().equals("Liabilities")) {
                    root = liabilities;
                } else if (group.getElement().getName().equals("Income")) {
                    root = income;
                } else {
                    root = expenses;
                }
            }
            if(subRoot.getValue()== "")
                continue;
            root.getChildren().add(subRoot);
        }
        groupList.clear();
        catList.clear();
    }

    public void rootOrBranch(){
        TreeItem<String> selectedItem = treeView.getSelectionModel().getSelectedItem();
        if (selectedItem != null && selectedItem.isLeaf()) {
            branch();
        } else {
            root();
        }
    }

    public void root(){
        add.setDisable(false);
        change.setDisable(true);
        delete.setDisable(true);
    }
    public void branch(){
        add.setDisable(false);
        change.setDisable(false);
        delete.setDisable(false);
    }


    public void addGroup(){
        addGroup = true;
        nameTxt.setDisable(false);
        save.setDisable(false);

    }
    public void changeName(){
        changeName = true;
        nameTxt.setDisable(false);
        save.setDisable(false);
    }
    public void deleteGroup() throws SQLException {

        TreeItem<String> selectedItem = treeView.getSelectionModel().getSelectedItem();
        selectedItemString = selectedItem.getValue();
        System.out.println(selectedItemString);
        int id = groupAdapter.getID(selectedItemString);
        Group delete = groupAdapter.findRecord(id);
        groupAdapter.deleteRecord(delete);

        TreeItem<String> parentItem = selectedItem.getParent();
        parentItem.getChildren().remove(selectedItem);
    }

    @FXML
    public void save() throws SQLException {
        TreeItem<String> Parent;


        if(addGroup){

            TreeItem<String> selectedItem = treeView.getSelectionModel().getSelectedItem();
            selectedItemString = selectedItem.getValue();

            int id = groupAdapter.getID(selectedItemString);

            TreeItem<String> Child = new TreeItem<>(nameTxt.getText());


            Parent = (TreeItem<String>) TreeItems.get(id);


            Group newGroup;
            if(id!=0) {
                Group parentGroup = groupAdapter.findRecord(id);

                newGroup = new Group(groupAdapter.getMax() + 1, nameTxt.getText(), parentGroup, parentGroup.getElement());

                TreeItems.add(newGroup.getID(),Child);

            }
            else {

                AccountCategory accountCategory = accountCategoryAdapter.findRecord(selectedItemString);
                Group parentGroup = new Group();
                newGroup = new Group(groupAdapter.getMax() + 1, nameTxt.getText(), parentGroup, accountCategory);
                TreeItems.add(newGroup.getID(),Child);

                if (newGroup.getElement().getName().equals("Assets")) {
                    Parent = assets;
                } else if (newGroup.getElement().getName().equals("Liabilities")) {
                    Parent = liabilities;
                } else if (newGroup.getElement().getName().equals("Income")) {
                    Parent = income;
                } else {
                    Parent = expenses;
                }
                addGroup =false;
            }
            groupAdapter.insertRecord(newGroup);
            Parent.getChildren().add(Child);
            nameTxt.setDisable(true);
            save.setDisable(true);
            nameTxt.setText("");
            addGroup = false;
        }
        if(changeName){
            TreeItem<String> selectedItem = treeView.getSelectionModel().getSelectedItem();
            selectedItemString = selectedItem.getValue();
            System.out.println(selectedItemString);
            int id = groupAdapter.getID(selectedItemString);
            Group changeName = groupAdapter.findRecord(id);
            Group newGroup = new Group(changeName.getID(), nameTxt.getText(),changeName.getParent(),changeName.getElement());
            groupAdapter.updateRecord(newGroup);


            selectedItem.setValue(nameTxt.getText());
            // Refresh the TreeView to reflect the changes
            treeView.refresh();
            nameTxt.setDisable(true);
            save.setDisable(true);
            nameTxt.setText("");
            this.changeName = false;
        }
    }
    public void exitM() {
        // Get current stage reference
        Stage stage = (Stage) exit.getScene().getWindow();
        // Close stage
        stage.close();
    }


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        nameTxt.setDisable(true);
        save.setDisable(true);
    }


}

