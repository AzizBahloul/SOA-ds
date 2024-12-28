package net.javaguides.springboot.soa.fx;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import net.javaguides.springboot.soa.entity.Product;
import net.javaguides.springboot.soa.entity.User;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import net.javaguides.springboot.soa.service.AuthService;

import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Gui extends Application {
    private static final String STYLE_CLASS = "modern-look";
    private final AuthService authService = new AuthService();
    private final RestTemplate restTemplate = new RestTemplate();
    private final String baseUrl = "http://localhost:8081/api/products";
    private final String userBaseUrl = "http://localhost:8081/api/users";
    private TableView<Product> table;
    private TextField nameField, priceField, stockField, categoryField, descriptionField;
    private TextField usernameField, passwordField;
    private static final Logger logger = Logger.getLogger(Gui.class.getName());

    private Product editingProduct = null; // State variable
    private Button saveBtn;
    private Button cancelEditBtn;

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("E-Commerce Application");

        // Show login page initially
        showLoginPage(primaryStage);

        primaryStage.show();
    }

    private void showLoginPage(Stage primaryStage) {
        VBox loginForm = new VBox(10);
        loginForm.setPadding(new Insets(10));
        loginForm.setStyle("-fx-background-color: #f5f5f5;");

        usernameField = new TextField();
        usernameField.setPromptText("Username");

        passwordField = new PasswordField();
        passwordField.setPromptText("Password");

        Button loginBtn = new Button("Login");
        loginBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
        loginBtn.setOnAction(e -> login(primaryStage));

        Button registerBtn = new Button("Register");
        registerBtn.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white;");
        registerBtn.setOnAction(e -> showRegisterPage(primaryStage));

        loginForm.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                login(primaryStage);
            }
        });

        loginForm.getChildren().addAll(
                new Label("Login"),
                usernameField, passwordField,
                loginBtn, registerBtn
        );

        Scene scene = new Scene(loginForm, 400, 300);
        primaryStage.setScene(scene);
    }

    private void showRegisterPage(Stage primaryStage) {
        VBox registerForm = new VBox(10);
        registerForm.setPadding(new Insets(10));
        registerForm.setStyle("-fx-background-color: #f5f5f5;");

        TextField usernameField = new TextField();
        usernameField.setPromptText("Username");

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");

        TextField emailField = new TextField();
        emailField.setPromptText("Email");

        Button registerBtn = new Button("Register");
        registerBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
        registerBtn.setOnAction(e -> registerUser(usernameField.getText(), passwordField.getText(), emailField.getText(), primaryStage));

        Button backBtn = new Button("Back to Login");
        backBtn.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white;");
        backBtn.setOnAction(e -> showLoginPage(primaryStage));

        registerForm.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                registerUser(usernameField.getText(), passwordField.getText(), emailField.getText(), primaryStage);
            }
        });

        registerForm.getChildren().addAll(
                new Label("Register"),
                usernameField, passwordField, emailField,
                registerBtn, backBtn
        );

        Scene scene = new Scene(registerForm, 400, 400);
        primaryStage.setScene(scene);
    }

    private void login(Stage primaryStage) {
        String username = usernameField.getText();
        String password = passwordField.getText();

        User user = new User();
        user.setUsername(username);
        user.setPassword(password);

        // Set credentials in AuthService
        authService.setCredentials(username, password);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<User> request = new HttpEntity<>(user, headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                userBaseUrl + "/login",
                HttpMethod.POST,
                request,
                String.class
            );

            if (response.getStatusCode() == HttpStatus.OK) {
                String authHeader = response.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
                if (authHeader != null) {
                    authService.setCredentials(username, password);
                    
                    String authorities = response.getBody();
                    if (authorities.contains("ROLE_ADMIN")) {
                        showAdminDashboard(primaryStage);
                    } else if (authorities.contains("ROLE_CLIENT")) {
                        showClientDashboard(primaryStage);
                    }
                }
            }
        } catch (Exception e) {
            showAlert("Login failed: " + e.getMessage());
            logger.log(Level.SEVERE, "Login error", e);
        }
    }

    private void registerUser(String username, String password, String email, Stage primaryStage) {
        User user = new User();
        user.setUsername(username);
        user.setPassword(password);
        user.setEmail(email);
        user.setRole("ROLE_CLIENT"); // Only allow registration as client

        try {
            restTemplate.postForObject(userBaseUrl + "/signup", user, User.class);
            showLoginPage(primaryStage); // Automatically return to login page
        } catch (Exception e) {
            showAlert("Registration failed: " + e.getMessage());
        }
    }

    private void showAdminDashboard(Stage primaryStage) {
        BorderPane root = new BorderPane();
        root.getStyleClass().add(STYLE_CLASS);

        // Navigation bar
        ToolBar navbar = createNavBar(primaryStage, "ADMIN");
        root.setTop(navbar);

        // Main content
        SplitPane content = new SplitPane();
        content.getItems().addAll(createTableView(), createForm());
        root.setCenter(content);

        Scene scene = new Scene(root, 1200, 800);
        scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());

        primaryStage.setScene(scene);
    }

    private void showClientDashboard(Stage primaryStage) {
        BorderPane root = new BorderPane();
        root.getStyleClass().add(STYLE_CLASS);

        // Navigation bar
        ToolBar navbar = createNavBar(primaryStage, "CLIENT");
        root.setTop(navbar);

        // Main content
        root.setCenter(createTableView());

        Scene scene = new Scene(root, 1200, 800);
        scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());

        primaryStage.setScene(scene);

        refreshTable();
    }

    private ToolBar createNavBar(Stage primaryStage, String role) {
        ToolBar navbar = new ToolBar();
        Label title = new Label("E-Commerce Dashboard");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");
        Button refreshBtn = new Button("Refresh");
        refreshBtn.setOnAction(e -> refreshTable());
        
        // Logout Button
        Button logoutBtn = new Button("Logout");
        logoutBtn.setStyle("-fx-background-color: #f44336; -fx-text-fill: white;");
        logoutBtn.setOnAction(e -> logout(primaryStage));
        
        navbar.getItems().addAll(title, new Separator(), refreshBtn, logoutBtn);
        navbar.setStyle("-fx-background-color: #2196F3; -fx-padding: 10;");
        return navbar;
    }

    private void logout(Stage primaryStage) {
        // Clear credentials from AuthService
        authService.clearCredentials();

        // Call the logout endpoint (optional)
        try {
            restTemplate.postForEntity(userBaseUrl + "/logout", null, String.class);
        } catch (Exception e) {
            logger.log(Level.WARNING, "Logout request failed", e);
        }

        // Redirect to login page
        showLoginPage(primaryStage);
    }

    private VBox createForm() {
        VBox form = new VBox(15);
        form.getStyleClass().add("form-container");
        form.setPrefWidth(300);

        Label titleLabel = new Label("Product Details");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        nameField = createStyledTextField("Product Name");
        priceField = createStyledTextField("Price");
        stockField = createStyledTextField("Stock");
        categoryField = createStyledTextField("Category");
        descriptionField = createStyledTextField("Description");
        
        saveBtn = new Button("Save Product");
        saveBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold;");
        saveBtn.setMaxWidth(Double.MAX_VALUE);
        saveBtn.setOnAction(e -> saveProduct());

        cancelEditBtn = new Button("Cancel Edit");
        cancelEditBtn.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-font-weight: bold;");
        cancelEditBtn.setMaxWidth(Double.MAX_VALUE);
        cancelEditBtn.setOnAction(e -> cancelEdit());
        cancelEditBtn.setVisible(false); // Initially hidden

        form.getChildren().addAll(
                titleLabel,
                createFormGroup("Name", nameField),
                createFormGroup("Price", priceField),
                createFormGroup("Stock", stockField),
                createFormGroup("Category", categoryField),
                createFormGroup("Description", descriptionField),
                saveBtn,
                cancelEditBtn // Added Cancel button
        );

        return form;
    }

    private HBox createFormGroup(String labelText, TextField field) {
        HBox group = new HBox(10);
        Label label = new Label(labelText);
        label.setMinWidth(80);
        group.getChildren().addAll(label, field);
        return group;
    }

    private TextField createStyledTextField(String prompt) {
        TextField field = new TextField();
        field.setPromptText(prompt);
        field.setMaxWidth(Double.MAX_VALUE);
        return field;
    }

    private TableView<Product> createTableView() {
        table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        
        TableColumn<Product, String> nameCol = new TableColumn<>("Name");
        TableColumn<Product, Double> priceCol = new TableColumn<>("Price");
        TableColumn<Product, Integer> stockCol = new TableColumn<>("Stock");
        TableColumn<Product, String> categoryCol = new TableColumn<>("Category");
        
        nameCol.setCellValueFactory(data -> 
                new SimpleStringProperty(data.getValue().getName()));
        priceCol.setCellValueFactory(data -> 
                new SimpleObjectProperty<>(data.getValue().getPrice()));
        stockCol.setCellValueFactory(data -> 
                new SimpleObjectProperty<>(data.getValue().getStock()));
        categoryCol.setCellValueFactory(data -> 
                new SimpleStringProperty(data.getValue().getCategory()));
        
        table.getColumns().addAll(nameCol, priceCol, stockCol, categoryCol);
        
        // Add right-click context menu
        ContextMenu contextMenu = new ContextMenu();
        MenuItem deleteItem = new MenuItem("Delete");
        deleteItem.setOnAction(e -> {
            Product selected = table.getSelectionModel().getSelectedItem();
            if (selected != null) {
                deleteProduct(selected);
            }
        });
        
        // Added Edit MenuItem
        MenuItem editItem = new MenuItem("Edit");
        editItem.setOnAction(e -> {
            Product selected = table.getSelectionModel().getSelectedItem();
            if (selected != null) {
                populateForm(selected);
            }
        });
        
        contextMenu.getItems().addAll(deleteItem, editItem); // Modified line
        table.setContextMenu(contextMenu);
        
        return table;
    }

    // Method to populate form for editing
    private void populateForm(Product product) {
        this.editingProduct = product;
        nameField.setText(product.getName());
        priceField.setText(product.getPrice().toString());
        stockField.setText(product.getStock().toString());
        categoryField.setText(product.getCategory());
        descriptionField.setText(product.getDescription());
        
        // Update UI to reflect editing state
        saveBtn.setText("Update Product");
        cancelEditBtn.setVisible(true);
    }

    private void saveProduct() {
        try {
            String name = nameField.getText();
            Double price = Double.parseDouble(priceField.getText());
            Integer stock = Integer.parseInt(stockField.getText());
            String category = categoryField.getText();
            String description = descriptionField.getText();

            HttpHeaders headers = authService.createHeaders();
            Product product = new Product();
            product.setName(name);
            product.setPrice(price);
            product.setStock(stock);
            product.setCategory(category);
            product.setDescription(description);
            HttpEntity<Product> request = new HttpEntity<>(product, headers);

            if (editingProduct == null) {
                // Create new product
                restTemplate.exchange(
                    baseUrl,
                    HttpMethod.POST,
                    request,
                    Product.class
                );
            } else {
                // Update existing product
                restTemplate.exchange(
                    baseUrl + "/" + editingProduct.getId(),
                    HttpMethod.PUT,
                    request,
                    Product.class
                );
                editingProduct = null;
            }

            refreshTable();
            clearForm();
        } catch (Exception e) {
            showAlert("Failed to save product: " + e.getMessage());
        }
    }

    // Method to cancel editing
    private void cancelEdit() {
        this.editingProduct = null;
        clearForm();
    }

    private void clearForm() {
        nameField.clear();
        priceField.clear();
        stockField.clear();
        categoryField.clear();
        descriptionField.clear();
        
        // Reset UI to default state
        saveBtn.setText("Save Product");
        cancelEditBtn.setVisible(false);
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void deleteProduct(Product product) {
        try {
            HttpHeaders headers = authService.createHeaders();
            HttpEntity<?> request = new HttpEntity<>(headers);
            
            restTemplate.exchange(
                baseUrl + "/" + product.getId(),
                HttpMethod.DELETE,
                request,
                Void.class
            );
            refreshTable();
        } catch (Exception e) {
            showAlert("Failed to delete product: " + e.getMessage());
        }
    }

    private void refreshTable() {
        try {
            HttpHeaders headers = authService.createHeaders();
            HttpEntity<?> request = new HttpEntity<>(headers);
            
            ResponseEntity<Product[]> response = restTemplate.exchange(
                baseUrl,
                HttpMethod.GET,
                request,
                Product[].class
            );
            
            if (response.getBody() != null) {
                table.getItems().setAll(Arrays.asList(response.getBody()));
            }
        } catch (Exception e) {
            showAlert("Failed to refresh table: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}