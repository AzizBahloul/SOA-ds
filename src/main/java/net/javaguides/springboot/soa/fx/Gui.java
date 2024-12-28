package net.javaguides.springboot.soa.fx;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import net.javaguides.springboot.soa.entity.Product;
import org.springframework.web.client.RestTemplate;
import java.util.Arrays;

public class Gui extends Application {
    private static final String STYLE_CLASS = "modern-look";
    private final RestTemplate restTemplate = new RestTemplate();
    private final String baseUrl = "http://localhost:8081/api/products";
    private TableView<Product> table;
    private TextField nameField, priceField, stockField, categoryField, descriptionField;

    @Override
    public void start(Stage primaryStage) {
        BorderPane root = new BorderPane();
        root.getStyleClass().add(STYLE_CLASS);

        // Navigation bar
        ToolBar navbar = createNavBar();
        root.setTop(navbar);

        // Main content
        SplitPane content = new SplitPane();
        content.getItems().addAll(createTableView(), createForm());
        root.setCenter(content);

        Scene scene = new Scene(root, 1200, 800);
        scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
        
        primaryStage.setTitle("E-Commerce Dashboard");
        primaryStage.setScene(scene);
        primaryStage.show();

        refreshTable();
    }

    private ToolBar createNavBar() {
        ToolBar navbar = new ToolBar();
        Label title = new Label("E-Commerce Dashboard");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");
        Button refreshBtn = new Button("Refresh");
        refreshBtn.setOnAction(e -> refreshTable());
        navbar.getItems().addAll(title, new Separator(), refreshBtn);
        navbar.setStyle("-fx-background-color: #2196F3; -fx-padding: 10;");
        return navbar;
    }

    private VBox createForm() {
        VBox form = new VBox(10);
        form.setPadding(new Insets(10));
        form.setStyle("-fx-background-color: #f5f5f5;");

        nameField = new TextField();
        nameField.setPromptText("Product Name");
        
        priceField = new TextField();
        priceField.setPromptText("Price");
        
        stockField = new TextField();
        stockField.setPromptText("Stock");
        
        categoryField = new TextField();
        categoryField.setPromptText("Category");
        
        descriptionField = new TextField();
        descriptionField.setPromptText("Description");

        Button saveBtn = new Button("Save");
        saveBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
        saveBtn.setOnAction(e -> saveProduct());

        form.getChildren().addAll(
            new Label("Product Details"),
            nameField, priceField, stockField, 
            categoryField, descriptionField,
            saveBtn
        );

        return form;
    }

    private TableView<Product> createTableView() {
        table = new TableView<>();
        
        TableColumn<Product, String> nameCol = new TableColumn<>("Name");
        TableColumn<Product, Double> priceCol = new TableColumn<>("Price");
        TableColumn<Product, Integer> stockCol = new TableColumn<>("Stock");
        TableColumn<Product, String> categoryCol = new TableColumn<>("Category");
        
        nameCol.setCellValueFactory(data -> 
            new javafx.beans.property.SimpleStringProperty(data.getValue().getName()));
        priceCol.setCellValueFactory(data -> 
            new javafx.beans.property.SimpleObjectProperty<>(data.getValue().getPrice()));
        stockCol.setCellValueFactory(data -> 
            new javafx.beans.property.SimpleObjectProperty<>(data.getValue().getStock()));
        categoryCol.setCellValueFactory(data -> 
            new javafx.beans.property.SimpleStringProperty(data.getValue().getCategory()));

        table.getColumns().addAll(nameCol, priceCol, stockCol, categoryCol);
        return table;
    }

    private void refreshTable() {
        Product[] products = restTemplate.getForObject(baseUrl, Product[].class);
        if (products != null) {
            table.getItems().setAll(Arrays.asList(products));
        }
    }

    private void saveProduct() {
        try {
            Product product = new Product();
            product.setName(nameField.getText());
            product.setPrice(Double.parseDouble(priceField.getText()));
            product.setStock(Integer.parseInt(stockField.getText()));
            product.setCategory(categoryField.getText());
            product.setDescription(descriptionField.getText());

            restTemplate.postForObject(baseUrl, product, Product.class);
            refreshTable();
            clearForm();
        } catch (NumberFormatException e) {
            showAlert("Invalid input! Please check the price and stock values.");
        }
    }

    private void clearForm() {
        nameField.clear();
        priceField.clear();
        stockField.clear();
        categoryField.clear();
        descriptionField.clear();
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}