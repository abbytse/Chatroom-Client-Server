/* Java Programming
 * @author Abby Tse
 * Use Sockets (including ServerSocket) to implement a Network Server and a Network Client such that 
 * when both the Server and the Client run, the messages type in at the client site will show on the server 
 * site and also the messages type in at the server site will also show at the Client site. 
 * ChatbotClient.java
 * Client GUI: To connect, first run server GUI and get the IP address from the top. 
 * Then, set the Server IP on the client. Afterwards, you should be connected 
 */

import java.io.*;
import java.net.*;
import java.util.Optional;
import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;

public class ChatroomClient extends Application {
    Socket socket = new Socket();
    DataOutputStream os;
    DataInputStream reader;
    TextArea text;

    public static void main(final String[] args) throws UnknownHostException, IOException {
        launch(args);
    }

    @Override
    public void start(final Stage primaryStage) throws Exception {
        Button btn = new Button();
        TextArea chat = new TextArea();

        Text title = new Text();
        title.setText("Chatbot Client");
        Font font = Font.font("Times New Roman", 25);
        title.setFont(font);
        title.setFill(Color.GREEN);

        chat.setPrefWidth(470);
        chat.setPrefRowCount(1);

        text = new TextArea();
        text.setEditable(false);

        // ensure that it scrolls to the bottom to see chat
        text.textProperty().addListener(new ChangeListener<Object>() {
            @Override
            public void changed(ObservableValue<?> observable, Object oldValue, Object newValue) {
                text.setScrollTop(Double.MAX_VALUE); // this will scroll to the bottom
            }
        });

        ScrollPane scrollPane = new ScrollPane(); // pane to display text messages
        scrollPane.setContent(text);
        scrollPane.setFitToHeight(true);
        scrollPane.setFitToWidth(true);

        btn.setText("Send");
        btn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                String message = chat.getText();
                chat.clear();
                sendMessage(message);
                updateText("Client: " + message);
            }
        });

        // create a text input dialog
        TextInputDialog td = new TextInputDialog("localhost");
        // setHeaderText
        td.setHeaderText("Enter Server IP Address");

        // create a button
        Button d = new Button("Connect to Server IP");

        // create a event handler
        //handles connection to the server and exceptions 
        EventHandler<ActionEvent> event = new EventHandler<ActionEvent>() {
            public void handle(ActionEvent e) {

                Optional<String> result = td.showAndWait();

                if (result.isPresent()) {
                    try {
                        socket.connect(new InetSocketAddress(InetAddress.getByName(td.getEditor().getText()), 55286),80);
                    } catch (SocketTimeoutException exception) {
                        updateText("Invalid IP Address! " + exception.getMessage());
                    } catch (ConnectException cException) {
                        updateText("Server not started yet! " + cException.getMessage());
                    } catch (IOException e1) {
                        updateText("Error! " + e1.getMessage());
                    } finally{
                        if(!socket.isConnected()){
                            socket = new Socket();
                        }
                    }
                } else {
                    updateText("You must input the server ip!");
                }
                //open input and output datastreams 
                if (socket.isConnected()) {
                    try {
                        reader = new DataInputStream(socket.getInputStream());
                        os = new DataOutputStream(socket.getOutputStream());
                        new ReceiveMessage(socket); // send socket receieve class
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }
            }
        };

        // set on action of event
        d.setOnAction(event);
        
        BorderPane root = new BorderPane();
        GridPane grid = new GridPane();

        grid.add(title,0,0);
        title.setTextAlignment(TextAlignment.CENTER);
        grid.add(d,0,1);
        d.setTextAlignment(TextAlignment.CENTER);
        grid.setPadding(new Insets(0,0,10,200));
        grid.setVgap(5);
        grid.setHgap(5);

        HBox centerScene = new HBox();
        centerScene.getChildren().add(text);
        centerScene.setAlignment(Pos.CENTER);

        HBox bottomScene = new HBox();
        bottomScene.getChildren().add(chat);
        bottomScene.getChildren().add(btn);
        bottomScene.setAlignment(Pos.CENTER);

        root.setTop(grid);
        root.setCenter(centerScene);
        root.setBottom(bottomScene);
        root.setPadding(new Insets(20));

        Scene scene = new Scene(root, 600, 350);

        primaryStage.setTitle("Chatbot Client");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    //update text in display chat
    private void updateText(String t) {
        text.appendText(t + "\n");
    }

    // send message back to server
    public void sendMessage(String message) {
        try {
            os.writeUTF(message);
            os.flush();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    //intake messages 
    class ReceiveMessage implements Runnable { // class receive message
        private final Socket socket;

        public ReceiveMessage(Socket socket) { // constructor
            this.socket = socket; // socket intializes
            Thread thread = new Thread(this);
            thread.setDaemon(true);
            thread.start();
        }

        public void run() {
            try {
                while (true) { // to continously receieve messages
                    String textmessage = reader.readUTF(); // read message from server
                    os.flush(); // flush
                    updateText("Server: " + textmessage);
                }
            } catch (EOFException eof) {
                updateText("Server is down!");
                updateText("Please exit application and reconnect.");
            } catch (IOException e) {
                updateText("Error " + e);
            }
        }
    }
    
}
