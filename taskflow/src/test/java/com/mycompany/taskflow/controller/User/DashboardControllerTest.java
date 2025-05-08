package com.mycompany.taskflow.controller.User;

import com.mycompany.taskflow.controller.JavaFXInitializer;
import com.mycompany.taskflow.controller.user.DashboardController;
import com.mycompany.taskflow.model.UserSession;
import com.mycompany.taskflow.model.user.Dashboard;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.AnchorPane;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.testfx.framework.junit5.ApplicationExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(ApplicationExtension.class)
class DashboardControllerTest extends JavaFXInitializer {

    private DashboardController controller;

    @Mock
    private Label welcomeLabel;
    @Mock
    private ListView<String> taskListView;
    @Mock
    private ProgressBar progressBar;
    @Mock
    private Label progressLabel;
    @Mock
    private Label notificationsCountLabel;
    @Mock
    private ListView<String> notificationsListView;
    @Mock
    private AnchorPane parentPane;
    @Mock
    private UserSession userSession;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        controller = new DashboardController();
        controller.welcomeLabel = welcomeLabel;
        controller.taskListView = taskListView;
        controller.progressBar = progressBar;
        controller.progressLabel = progressLabel;
        controller.notificationsCountLabel = notificationsCountLabel;
        controller.notificationsListView = notificationsListView;

        try (MockedStatic<UserSession> userSessionMockedStatic = mockStatic(UserSession.class)) {
            userSessionMockedStatic.when(UserSession::getInstance).thenReturn(userSession);
            when(userSession.getUserId()).thenReturn(3);
            when(userSession.getFirstName()).thenReturn("Testowy");
        }
    }



    @Test
    void initialize_shouldHandleNoActiveSession() {
        UserSession.setInstance(null);
        controller.initialize();
        assertNull(controller.userId);
        assertNull(controller.userName);
        UserSession.setInstance(userSession);
    }



    @Test
    void setUserData_shouldUpdateUserIdAndNameButNotCallUpdateViewIfNotInitialized() {
        controller.isInitialized = false;
        controller.setUserData(789, "JeszczeJeden");
        verifyNoInteractions(welcomeLabel, taskListView, progressBar, progressLabel, notificationsCountLabel, notificationsListView);
    }



    @Test
    void initializeWelcomeLabel_shouldSetWelcomeTextWithUserNameAndCurrentDateTime() {
        controller.userName = "Testowy";
        controller.initializeWelcomeLabel();
        verify(welcomeLabel).setText(startsWith("Witaj Testowy! " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"))));
    }

    @Test
    void initializeWelcomeLabel_shouldSetDefaultWelcomeTextIfUserNameIsNull() {
        controller.userName = null;
        controller.initializeWelcomeLabel();
        verify(welcomeLabel).setText("Witaj użytkowniku!");
    }

    @Test
    void loadTasksForNextWeek_shouldLoadTasksAndSetToListView() {
        List<Dashboard.Deadline> deadlines = Arrays.asList(
                new Dashboard.Deadline("Zadanie", "Zadanie 1", LocalDate.parse("2025-05-05")),
                new Dashboard.Deadline("Zadanie", "Zadanie 2", LocalDate.parse("2025-05-08"))
        );
        ObservableList<String> expectedTasks = FXCollections.observableArrayList(
                "Zadanie 1 - Termin: 2025-05-05",
                "Zadanie 2 - Termin: 2025-05-08"
        );
        try (MockedStatic<Dashboard> dashboardMockedStatic = mockStatic(Dashboard.class)) {
            dashboardMockedStatic.when(() -> Dashboard.loadUserDeadlines(3)).thenReturn(deadlines);
            controller.loadTasksForNextWeek(3);
            verify(taskListView).setItems(eq(expectedTasks));
        }
    }


    @Test
    void updateProgressBar_shouldCalculateProgressAndSetToProgressBarAndLabel() {
        double progressValue = 0.75;
        try (MockedStatic<Dashboard> dashboardMockedStatic = mockStatic(Dashboard.class)) {
            dashboardMockedStatic.when(() -> Dashboard.calculateUserProgress(3)).thenReturn(progressValue);
            controller.updateProgressBar(3);
            verify(progressBar).setProgress(progressValue);
            verify(progressLabel).setText(String.format("%.0f%%", progressValue * 100));
        }
    }

    @Test
    void loadNotificationsCount_shouldLoadCountAndSetToLabel() {
        int notificationCount = 5;
        try (MockedStatic<Dashboard> dashboardMockedStatic = mockStatic(Dashboard.class)) {
            dashboardMockedStatic.when(() -> Dashboard.getUserNotificationsCount(3)).thenReturn(notificationCount);
            controller.loadNotificationsCount(3);
            verify(notificationsCountLabel).setText("(" + notificationCount + ")");
        }
    }

    @Test
    void getView_shouldReturnParentNode() {
        when(welcomeLabel.getParent()).thenReturn(parentPane);
        assertEquals(parentPane, controller.getView());
    }
    @Test
    void updateView_shouldCallAllUpdateMethodsIfUserIdIsNotNull() {
        controller.userId = 3;
        DashboardController dashboardControllerSpy = spy(controller);

        try (MockedStatic<Dashboard> dashboardMockedStatic = mockStatic(Dashboard.class)) {
            dashboardControllerSpy.updateView();

            verify(dashboardControllerSpy).initializeWelcomeLabel();
            verify(dashboardControllerSpy).loadTasksForNextWeek(3);
            verify(dashboardControllerSpy).updateProgressBar(3);
            verify(dashboardControllerSpy).loadNotificationsCount(3);
            verify(dashboardControllerSpy).loadNotifications(3);
        }
    }
    @Test
    void updateView_shouldNotCallUpdateMethodsIfUserIdIsNull() {
        controller.userId = null;
        DashboardController dashboardControllerSpy = spy(controller);
        dashboardControllerSpy.updateView();

        verify(dashboardControllerSpy, never()).initializeWelcomeLabel();
        verify(dashboardControllerSpy, never()).loadTasksForNextWeek(anyInt());
        verify(dashboardControllerSpy, never()).updateProgressBar(anyInt());
        verify(dashboardControllerSpy, never()).loadNotificationsCount(anyInt());
        verify(dashboardControllerSpy, never()).loadNotifications(anyInt());
    }
    @Test
    void loadNotifications_shouldShowEmptyStateWhenNoNotifications() {
        int userId = 3;

        DashboardController controller = new DashboardController();
        controller.notificationsListView = new ListView<>();
        controller.notificationItems = FXCollections.observableArrayList();

        controller.notificationsListView.setItems(controller.notificationItems);

        try (MockedStatic<Dashboard> mocked = mockStatic(Dashboard.class)) {
            mocked.when(() -> Dashboard.getUserNotifications(userId))
                    .thenReturn(null);

            controller.loadNotifications(userId);

            System.out.println("Notification items after load: " + controller.notificationItems);
            System.out.println("ListView items after load: " + controller.notificationsListView.getItems());

            assertEquals(1, controller.notificationItems.size(),
                    "Should show empty state for null notifications");
            assertEquals("Brak powiadomień.", controller.notificationItems.get(0));

            assertEquals(controller.notificationItems, controller.notificationsListView.getItems());
        }
    }
}