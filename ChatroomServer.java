/* Java Programming 
 * @author Abby Tse
 * Use Sockets (including ServerSocket) to implement a Network Server and a Network Client such that 
 * when both the Server and the Client run, the messages type in at the client site will show on the server 
 * site and also the messages type in at the server site will also show at the Client site. 
 * ChatbotServer.java
 * Server GUI: To connect, first run server GUI and get the IP address from the top. 
 * Then, set the Server IP on the client. Afterwards, you should be connected 
 */

import java.io.*;
import java.net.*;
import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;

public class ChatroomServer extends Application {
    static ServerSocket serverSocket;
    static Socket socket;
    TextArea text;
    DataOutputStream output;
    DataInputStream reader;

    public static void main(String[] args) throws IOException {
        launch(args);
        serverSocket.close();
        socket.close();
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        Button btn = new Button();
        Button send = new Button();

        Text title = new Text(); 
        title.setText("Chatbot Server");
        Font font = Font.font("Times New Roman", 25);
        title.setFont(font);
        title.setFill(Color.PURPLE); 

        Label ipAddress = new Label(); 
        ipAddress.setFont(Font.font("Times New Roman", 15));
        InetAddress host = InetAddress.getLocalHost();
        ipAddress.setText("IP Address: " + host.getHostAddress());

        text = new TextArea();
        text.setEditable(false);

        //scroll to bottom 
        text.textProperty().addListener(new ChangeListener<Object>() {
            @Override
            public void changed(ObservableValue<?> observable, Object oldValue,
                    Object newValue) {
                text.setScrollTop(Double.MAX_VALUE); //this will scroll to the bottom
            }
        });

        TextArea chat = new TextArea();
        chat.setPrefWidth(470);
        chat.setPrefRowCount(1);
        
        ScrollPane scrollPane = new ScrollPane();   //pane to display text messages      
        scrollPane.setContent(text);
        scrollPane.setFitToHeight(true);
        scrollPane.setFitToWidth(true);

        //start server, wait for client to connect & communicate 
        btn.setText("Start Server");
        btn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                Thread.UncaughtExceptionHandler h = new Thread.UncaughtExceptionHandler() {
                    public void uncaughtException(Thread th, Throwable ex) {
                        updateText("Uncaught exception: " + ex.getMessage());
                    }
                };

                Thread t = new Thread(){
                    public void run(){
                        try {
                            serverSocket = new ServerSocket(55286);
                            updateText("Server started");
                            boolean clientOnline = false; 
                            updateText("Server is waiting");
    
                            socket = serverSocket.accept();
                            output= new DataOutputStream(socket.getOutputStream());
                            //Create data input stream
                            reader = new DataInputStream(socket.getInputStream());
                            
                            updateText("Client connected");
                            clientOnline = true;
                            String welcomeMessage = "Welcome! How may I help you?";
                            sendMessage(welcomeMessage);
                            updateText("Server: " + welcomeMessage);
    
                            while(clientOnline){
                                try {
                                    //get input from the client
                                    String message = reader.readUTF();
                                    updateText("Client: " + message);
                                } catch (IOException ex) {
                                    updateText("Client disconnected");
                                    break;
                                }
                            }
                        } catch (IOException e) {
                            updateText("Client is not connected!");
                        }
                    }
                };
                t.setUncaughtExceptionHandler(h);
                t.start(); 
            }
        });

		send.setText("Send");
        send.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                String message = chat.getText();
                chat.clear();
                updateText("Server: " + message);
                sendMessage(message);
            }
        });
        
        BorderPane root = new BorderPane();
        GridPane grid = new GridPane();

        HBox btnBox = new HBox(btn);

        grid.add(title,0,0);
        title.setTextAlignment(TextAlignment.CENTER);
        grid.add(ipAddress,0,1);
        grid.add(btnBox,0,2);
        ipAddress.setAlignment(Pos.CENTER);
        btnBox.setAlignment(Pos.CENTER);
        grid.setPadding(new Insets(0,0,10,200));
        grid.setVgap(5);
        grid.setHgap(5);

        ipAddress.setTextAlignment(TextAlignment.CENTER);

        HBox centerScene = new HBox();
        centerScene.getChildren().add(text);
        centerScene.setAlignment(Pos.CENTER);

        HBox bottomScene = new HBox();
        bottomScene.getChildren().add(chat);
        bottomScene.getChildren().add(send);
        bottomScene.setAlignment(Pos.CENTER);

        root.setTop(grid);
        root.setCenter(centerScene);
        root.setBottom(bottomScene);
        root.setPadding(new Insets(20));

        Scene scene = new Scene(root, 600, 350);
        
        primaryStage.setTitle("Chatbot Server");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    //update Text in the chat for display
    private void updateText(String t){
        text.appendText(t + "\n");
    }

    //send message back to client
    public void sendMessage(String message) {
        try {
            output.writeUTF(message);
            output.flush();
        } catch (EOFException eof) {
            updateText("Client is disconnected!");
            updateText("Please exit application and reconnect.");
        } catch (SocketException socket){
            updateText("Client is disconnected!");
            updateText("Please exit application and reconnect.");
        }
        catch (Exception ex) {
            updateText(ex.getMessage());
        } 
    } 
}
