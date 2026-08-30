package group.chatting.application;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.nio.file.Files;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;

public class ChatAppFX extends Application {


    private static final String HOST = "localhost";
    private static final int PORT = 5000;

    private static final DateTimeFormatter TIME_FORMATTER =
            DateTimeFormatter.ofPattern("HH:mm");




    public static void main(String[] args) {
        launch(args);
    }


    //CREATE WINDOWS

    @Override
    public void start(Stage primaryStage) {

        createClientWindow("Ayesha", "Ayesha, Manahil, Hajra", 20);
        createClientWindow("Manahil", "Ayesha, Manahil, Hajra", 390);
        createClientWindow("Hajra", "Ayesha, Manahil, Hajra", 760);
    }

    private void createClientWindow(String userName, String statusText, double x) {
        Stage stage = new Stage();

        ChatClientPane clientPane = new ChatClientPane(userName, statusText);
        BorderPane root = clientPane.createContent(stage);

        Scene scene = new Scene(root, 350, 550);
        stage.setScene(scene);

        stage.initStyle(StageStyle.UNDECORATED);
        stage.setX(x);
        stage.setY(50);
        stage.show();
    }



    private class ChatClientPane {


        private final String userName;
        private final String statusText;

        private Socket socket;
        private BufferedWriter writer;
        private BufferedReader reader;

        private VBox messagesBox;
        private ScrollPane scrollPane;
        private TextField inputField;


        ChatClientPane(String userName, String statusText) {
            this.userName = userName;
            this.statusText = statusText;
        }




        BorderPane createContent(Stage stage) {
            BorderPane root = new BorderPane();

            // Top header
            root.setTop(buildHeader(stage));

            // Center messages area
            messagesBox = new VBox(10);
            messagesBox.setPadding(new Insets(10));

            scrollPane = new ScrollPane(messagesBox);
            scrollPane.setFitToWidth(true);
            scrollPane.setStyle("-fx-background: white;");
            root.setCenter(scrollPane);

            // Bottom input area
            root.setBottom(buildInputArea());

            // When window closes, close resources
            stage.setOnCloseRequest(e -> closeResources());

            // Connect
            connectToServer();
            startReadingThread();

            return root;
        }


        //HEADER

        private HBox buildHeader(Stage stage) {
            HBox header = new HBox(10);
            header.setPadding(new Insets(10));
            header.setAlignment(Pos.CENTER_LEFT);
            header.setStyle("-fx-background-color: #075E54;");

            ImageView backIcon = createIcon("/icons/3.png", 25, 25);
            backIcon.setOnMouseClicked(e -> stage.close());


            ImageView profileIcon = createIcon("/icons/groupdp2.PNG", 50, 50);
            Circle clip = new Circle(25, 25, 25); // centerX, centerY, radius
            profileIcon.setClip(clip);

            VBox nameBox = new VBox(2);

            Label chatTitle = new Label("Friends Group");
            chatTitle.setTextFill(Color.WHITE);
            chatTitle.setFont(Font.font("System", FontWeight.BOLD, 18));

            Label statusLabel = new Label(statusText);
            statusLabel.setTextFill(Color.WHITE);
            statusLabel.setFont(Font.font("System", 14));

            nameBox.getChildren().addAll(chatTitle, statusLabel);

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            ImageView videoIcon = createIcon("/icons/video.png", 30, 30);
            ImageView phoneIcon = createIcon("/icons/phone.png", 35, 30);
            ImageView moreIcon = createIcon("/icons/3icon.png", 10, 25);

            header.getChildren().addAll(
                    backIcon,
                    profileIcon,
                    nameBox,
                    spacer,
                    videoIcon,
                    phoneIcon,
                    moreIcon
            );

            return header;
        }

        private ImageView createIcon(String path, double width, double height) {
            Image image = new Image(getClass().getResourceAsStream(path));
            ImageView view = new ImageView(image);
            view.setFitWidth(width);
            view.setFitHeight(height);
            return view;
        }


        //INPUT

        private HBox buildInputArea() {
            HBox inputArea = new HBox(10);
            inputArea.setPadding(new Insets(10));
            inputArea.setStyle("-fx-background-color: #ECE5DD;");

            Button imageButton = new Button("Image");
            imageButton.setOnAction(e -> chooseAndSendImage());

            inputField = new TextField();
            inputField.setPromptText("Type a message...");
            HBox.setHgrow(inputField, Priority.ALWAYS);

            Button sendButton = new Button("Send");
            sendButton.setOnAction(e -> sendMessage());
            inputField.setOnAction(e -> sendMessage());

            inputArea.getChildren().addAll(imageButton, inputField, sendButton);
            return inputArea;
        }


        //SERVER

        private void connectToServer() {
            try {
                socket = new Socket(HOST, PORT);
                writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void closeResources() {
            try {
                if (reader != null) reader.close();
                if (writer != null) writer.close();
                if (socket != null && !socket.isClosed()) socket.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }


        //READ INCOMING MESSAGES

        private void startReadingThread() {
            if (reader == null) return;

            Thread t = new Thread(() -> {
                try {
                    String msg;
                    while ((msg = reader.readLine()) != null) {

                        final String incoming = msg;

                        int colonIndex = incoming.indexOf(':');
                        if (colonIndex == -1) continue;

                        String sender = incoming.substring(0, colonIndex).trim();
                        String body = incoming.substring(colonIndex + 1).trim();

                        // skip showing our own message
                        if (sender.equals(userName)) continue;

                        if (body.startsWith("[IMG]")) {
                            String base64 = body.substring(5).trim();
                            try {
                                byte[] imageBytes = Base64.getDecoder().decode(base64);
                                Platform.runLater(() -> addImageMessage(imageBytes, false));
                            } catch (IllegalArgumentException e) {
                                e.printStackTrace();
                            }
                        } else {
                            Platform.runLater(() -> addMessage(incoming, false));
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    closeResources();
                }
            });

            t.setDaemon(true);
            t.start();
        }


        //SEND MESSAGE

        private void sendMessage() {
            if (writer == null) return;

            String text = inputField.getText().trim();
            if (text.isEmpty()) return;

            String out = userName + ": " + text;

            // show on screen immediately
            addMessage(out, true);

            try {
                writer.write(out);
                writer.newLine();
                writer.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }

            inputField.clear();
        }


        //SEND IMAGE

        private void chooseAndSendImage() {
            if (writer == null) return;

            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Select Image");
            fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter(
                            "Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif"
                    )
            );

            File file = fileChooser.showOpenDialog(null);
            if (file == null) return;

            try {
                byte[] bytes = Files.readAllBytes(file.toPath());
                String base64 = Base64.getEncoder().encodeToString(bytes);

                String out = userName + ": [IMG] " + base64;

                // show on screen immediately
                addImageMessage(bytes, true);

                writer.write(out);
                writer.newLine();
                writer.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


        //ADD TEXT BUBBLE

        private void addMessage(String msg, boolean fromSelf) {
            HBox messageContainer = new HBox();
            messageContainer.setPadding(new Insets(5));
            messageContainer.setAlignment(fromSelf ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);

            Label messageLabel = new Label(msg);
            messageLabel.setWrapText(true);
            messageLabel.setFont(Font.font("Tahoma", 14));

            String bubbleColor = fromSelf ? "#25D366" : "#ECECEC";
            messageLabel.setStyle(
                    "-fx-background-color: " + bubbleColor + ";" +
                            "-fx-padding: 8 12 8 12;" +
                            "-fx-background-radius: 10;" +
                            "-fx-border-color: #BDBDBD;" +
                            "-fx-border-radius: 10;"
            );

            Label timeLabel = new Label(TIME_FORMATTER.format(LocalTime.now()));
            timeLabel.setFont(Font.font(10));
            timeLabel.setTextFill(Color.GRAY);

            VBox bubbleWithTime = new VBox(2, messageLabel, timeLabel);

            messageContainer.getChildren().add(bubbleWithTime);
            messagesBox.getChildren().add(messageContainer);

            scrollPane.setVvalue(1.0);
        }


        //ADD IMAGE BUBBLE

        private void addImageMessage(byte[] imageData, boolean fromSelf) {
            HBox messageContainer = new HBox();
            messageContainer.setPadding(new Insets(5));
            messageContainer.setAlignment(fromSelf ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);

            Image image = new Image(new ByteArrayInputStream(imageData));
            ImageView imageView = new ImageView(image);
            imageView.setFitWidth(180);
            imageView.setPreserveRatio(true);

            Label timeLabel = new Label(TIME_FORMATTER.format(LocalTime.now()));
            timeLabel.setFont(Font.font(10));
            timeLabel.setTextFill(Color.GRAY);

            VBox bubble = new VBox(2, imageView, timeLabel);
            bubble.setPadding(new Insets(8, 12, 8, 12));

            String bubbleColor = fromSelf ? "#25D366" : "#ECECEC";
            bubble.setStyle(
                    "-fx-background-color: " + bubbleColor + ";" +
                            "-fx-background-radius: 10;" +
                            "-fx-border-color: #BDBDBD;" +
                            "-fx-border-radius: 10;"
            );

            messageContainer.getChildren().add(bubble);
            messagesBox.getChildren().add(messageContainer);

            scrollPane.setVvalue(1.0);
        }
    }
}