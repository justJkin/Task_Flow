package com.mycompany.taskflow.controller.User;

import com.mycompany.taskflow.controller.JavaFXInitializer;
import com.mycompany.taskflow.controller.user.DashboardController;
import com.mycompany.taskflow.controller.user.UserController;
import com.mycompany.taskflow.model.UserSession;
import javafx.collections.FXCollections;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.Pane; // Zamiast Node
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.testfx.framework.junit5.ApplicationExtension;

import java.io.IOException;

import static org.mockito.Mockito.*;

@ExtendWith(ApplicationExtension.class)
class UserControllerTest extends JavaFXInitializer {

    @Mock
    private StackPane contentArea;
    @Mock
    private UserSession userSession;
    @Mock
    private FXMLLoader fxmlLoader;
    @Mock
    private Scene mockScene;
    @Mock
    private Stage mockStage;
    @Mock
    private DashboardController dashboardController;
    @Mock
    private Pane dashboardView;
    @Mock
    private Pane mockOtherView;

    @InjectMocks
    private UserController userController;

    @BeforeEach
    void setUp() throws IOException {
        MockitoAnnotations.openMocks(this);

        userSession = mock(UserSession.class);
        when(userSession.getUserId()).thenReturn(3);
        when(userSession.getFirstName()).thenReturn("Testowy");

        when(userSession.getRole()).thenReturn("USER");
        when(userSession.getTeamId()).thenReturn(1);

        try (MockedStatic<UserSession> userSessionMockedStatic = mockStatic(UserSession.class)) {
            userSessionMockedStatic.when(UserSession::getInstance).thenReturn(userSession);
        }

        when(fxmlLoader.load()).thenReturn(dashboardView).thenReturn(mockOtherView);
        when(fxmlLoader.getController()).thenReturn(dashboardController);

        when(contentArea.getChildren()).thenReturn(FXCollections.observableArrayList());
        when(mockScene.getStylesheets()).thenReturn(FXCollections.observableArrayList());
        when(contentArea.getScene()).thenReturn(mockScene);
        when(mockScene.getWindow()).thenReturn(mockStage);
    }

    @Test
    void initialize_shouldLoadDashboardIfSessionExists() {
        userController.initialize();
    }

    @Test
    void initialize_shouldNotLoadDashboardIfNoSession() {
        try (MockedStatic<UserSession> userSessionMockedStatic = mockStatic(UserSession.class)) {
            userSessionMockedStatic.when(UserSession::getInstance).thenReturn(null);
            userController.initialize();
            when(userSession).thenReturn(userSession);
        }
    }

}