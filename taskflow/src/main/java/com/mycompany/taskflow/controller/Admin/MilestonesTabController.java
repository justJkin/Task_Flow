package com.mycompany.taskflow.controller.Admin;

import com.mycompany.taskflow.model.Admin.Milestone;
import com.mycompany.taskflow.model.Admin.Project;
import com.mycompany.taskflow.model.Admin.Team;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;

import java.util.List;

public class MilestonesTabController {
    @FXML private VBox root;
    @FXML private TableView<Milestone> milestoneTable;
    @FXML private TableColumn<Milestone,String> colMsName;
    @FXML private TableColumn<Milestone,String> colMsDescription;
    @FXML private TableColumn<Milestone,Integer> colMsWeight;
    @FXML private TableColumn<Milestone,Team> colMsTeam;
    @FXML private TableColumn<Milestone,Void> colMsDelete;

    @FXML private TextField tfMsName;
    @FXML private TextArea taMsDesc;
    @FXML private Spinner<Integer> spMsWeight;
    @FXML private ComboBox<Team> cbMsTeams;
    @FXML private Button btnMsAdd;
    @FXML private Label lblMsWarning;

    private final ObservableList<Milestone> msList = FXCollections.observableArrayList();
    private Project project;

    public static MilestonesTabController controllerFactory() {
        return new MilestonesTabController();
    }

    /** Inicjalizuje widok: kolumny tabeli, spinner i przycisk Dodaj */
    @FXML
    public void initialize() {
        colMsName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colMsDescription.setCellValueFactory(new PropertyValueFactory<>("description"));
        colMsWeight.setCellValueFactory(new PropertyValueFactory<>("weight"));
        colMsTeam.setCellValueFactory(cell ->
                new SimpleObjectProperty<>(Team.getTeamById(cell.getValue().getTeamId()))
        );
        colMsDelete.setCellFactory(col -> new TableCell<>() {
            private final Button remove = new Button("X");
            {
                remove.setOnAction(e -> {
                    int idx = getIndex();
                    if (idx >= 0 && idx < msList.size()) {
                        msList.remove(idx);
                        validateSum();
                    }
                });
            }
            @Override protected void updateItem(Void v, boolean empty) {
                super.updateItem(v, empty);
                setGraphic(empty ? null : remove);
            }
        });
        milestoneTable.setItems(msList);

        spMsWeight.setValueFactory(
                new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 100, 1)
        );

        cbMsTeams.getItems().setAll(Team.getAllTeams());

        btnMsAdd.setOnAction(e -> onAdd());
        root.getProperties().put("controller", this);

    }

    /** Ładuje kamienie milowe i ustawia projekt kontekstowy */
    public void loadForProject(Project project) {
        this.project = project;
        List<Milestone> loaded = Milestone.getMilestonesByProject(project.getId());
        System.out.println(">> Załadowano " + loaded.size() + " kamieni milowych z bazy");
        msList.setAll(loaded);
        validateSum();
    }

    /** Obsługa przycisku Dodaj: wstawia nowy kamień i czyści formularz */
    private void onAdd() {
        String name = tfMsName.getText().trim();
        Team team = cbMsTeams.getValue();
        int weight = spMsWeight.getValue();

        if (name.isEmpty() || team == null) {
            showAlert("Musisz podać nazwę i wybrać zespół");
            return;
        }

        int currentSum = msList.stream().mapToInt(Milestone::getWeight).sum();
        if (currentSum + weight > 100) {
            showAlert("Nie można dodać kamienia milowego – suma wag przekroczy 100.");
            return;
        }

        Milestone m = new Milestone();
        if (project != null) {
            m.setProjectId(project.getId());
        }

        m.setName(name);
        m.setDescription(taMsDesc.getText());
        m.setWeight(weight);
        m.setTeamId(team.getId());

        msList.add(m);
        tfMsName.clear();
        taMsDesc.clear();
        spMsWeight.getValueFactory().setValue(1);
        cbMsTeams.getSelectionModel().clearSelection();
        validateSum();
    }


    public void assignProjectIdToMilestones(int projectId) {
        for (Milestone m : msList) {
            m.setProjectId(projectId);
        }
    }

    /** Waliduje sumę wag kamieni i wyświetla ostrzeżenie jeśli różna od 100 */
    private void validateSum() {
        int sum = msList.stream().mapToInt(Milestone::getWeight).sum();
        lblMsWarning.setText(
                sum == 100 ? "" : "Suma wag musi wynosić 100 (teraz: " + sum + ")"
        );
    }

    /** Pojedynczy Alert do ostrzeżeń */
    private void showAlert(String msg) {
        new Alert(Alert.AlertType.WARNING, msg).showAndWait();
    }

    /** Zwraca aktualną listę kamieni milowych (do zapisu) */
    public ObservableList<Milestone> getMilestones() {
        return msList;
    }
}
