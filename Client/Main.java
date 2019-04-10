package sample;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Border;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.geometry.Pos;
import javafx.scene.text.*;
import javafx.scene.image.*;

import java.net.InetAddress;


public class Main extends Application {

    private Stage welcomeStage, playStage, challengeStage;
    private Text welcomeText, portInputText, ipInputText;
    private Button systemsButton, playButton, closeButton;
    private Button Rock, Paper, Scissors, Lizard, Spock;
    private TextField portRequest, ipRequest;
    //    private Text selfScoreText, theirScoreText;
    private Text p1ScoreText, p2ScoreText, p3ScoreText;
    private int p1Score = 0;
    private int p2Score = 0;
    private int p3Score = 0;
    private Text gameState = new Text("");
    private int portNumber;
    private InetAddress ipNumber;
    private Text winner = new Text("Winner: ");
    private Text lonePlayer = new Text("Someone has left. Close client.");
    private Button playAgainButton = new Button("Play again");
    //    private Text theirMove = new Text("Their move: " );
    private Text p1Move = new Text("P1 Move:");
    private Text p2Move = new Text("P2 Move:");
    private Text p3Move = new Text("P3 Move:");

    private Image rock = new Image("sample/rock.jpeg",75,75,true, true);
    private Image paper = new Image("sample/paper.jpeg", 75, 75, true, true);
    private Image scissors = new Image("sample/scissors.jpeg", 50, 50, true, true);
    private Image lizard = new Image("sample/lizard.jpeg", 75, 75, true, true);
    private Image spock = new Image("sample/spock.jpeg", 75, 75, true, true);

    private Client thisClient = createClient();

    @Override
    public void start(Stage primaryStage) throws Exception{

        //////////////////          welcome page            //////////////////

        welcomeStage = primaryStage;
        welcomeStage.setTitle("This is the client.");
        BorderPane welcomePane = new BorderPane();
        Scene welcomeScene = new Scene(welcomePane, 400,600);

        welcomeText = new Text("Client - RSPLS");
        welcomeText.setFont(Font.font("Verdana", 20));

        portRequest = new TextField("Input desired port here");
        ipRequest = new TextField("Input desired IP here");
        portRequest.setMaxWidth(200);
        ipRequest.setMaxWidth(200);

        portInputText = new Text("Port: ");
        ipInputText = new Text("IP: ");

        systemsButton = new Button("Confirm settings");
        playButton = new Button("Let's play!");
        closeButton = new Button("End client");

        VBox Cproperties = new VBox(welcomeText, portRequest,ipRequest, systemsButton, playButton, portInputText, ipInputText, closeButton);
        Cproperties.setSpacing(10);
        Cproperties.setAlignment(Pos.CENTER);

        welcomePane.setTop(Cproperties);
        welcomeStage.setScene(welcomeScene);

        //////////////////          play page            //////////////////
        playStage = new Stage();
        playStage.setTitle("Game in progress...");
        BorderPane playPane = new BorderPane();
        Scene playScene = new Scene(playPane, 400,600);

        Rock = new Button("Rock");
        Paper = new Button("Paper");
        Scissors = new Button("Scissors");
        Lizard = new Button("Lizard");
        Spock = new Button("Spock");

        Rock.setGraphic(new ImageView(rock));
        Paper.setGraphic(new ImageView(paper));
        Scissors.setGraphic(new ImageView(scissors));
        Lizard.setGraphic(new ImageView(lizard));
        Spock.setGraphic(new ImageView(spock));

        p1ScoreText = new Text("P1 Points: " + p1Score);
        p2ScoreText = new Text("P2 Points:" + p2Score);
        p3ScoreText = new Text("P3 Points:" + p3Score);

//        VBox moves = new VBox(Rock,Paper,Scissors,Lizard,Spock, theirMove, selfScoreText,theirScoreText, winner, closeButton, playAgainButton);
        VBox moves = new VBox(Rock,Paper,Scissors,Lizard,Spock, p1ScoreText, p2ScoreText, p3ScoreText, winner, closeButton, playAgainButton);
        moves.setSpacing(10);
        moves.setAlignment(Pos.CENTER);

        playPane.setCenter(moves);
        playAgainButton.setVisible(false);

        //////////////////          challenge page            ///////////////////
        challengeStage = new Stage();
        challengeStage.setTitle("Choose your challenger");
        BorderPane challengePane = new BorderPane();
        Scene challengeScene = new Scene(challengePane,400,600);

        Button challengeOne = new Button("Challenge P1");
        Button challengeTwo = new Button("Challenge P2");
        Button challengeThree = new Button("Challenge P3");
        Button challengeFour = new Button("Challenge P4");

        VBox challenger = new VBox(challengeOne, challengeTwo,challengeThree,challengeFour);
        challenger.setSpacing(20);
        challenger.setAlignment(Pos.CENTER);

        challengePane.setCenter(challenger);

        //sets the buttons for the client
        systemsButton.setOnAction(event -> {
            try {
                portNumber = Integer.parseInt(portRequest.getText());
                ipNumber = InetAddress.getByName(ipRequest.getText());

                portInputText.setText("Port: " + portNumber);
                ipInputText.setText("IP: " + ipNumber);

                thisClient.setPort(portNumber);
                thisClient.setIP(ipNumber);


                thisClient.setValid(true);
                System.out.println("Port: " + thisClient.getPort() + " IP: " + thisClient.getIP());
            }
            catch (Exception e) {
                portInputText.setText("Port: " + " needs to be a number");
                ipInputText.setText("IP: " + "needs to be an address");
            }
        });

        playButton.setOnAction(event -> {
            if(thisClient.isValid()) {
                try {
                    thisClient.startConnection();
                    thisClient.connected = true;
                    primaryStage.setScene(challengeScene);
                }
                catch (Exception e) {
                    e.printStackTrace();
                    System.out.println("Issue starting the connection. Valid settings.");
                }
            }
        });

        closeButton.setOnAction(event -> {
            try {
                thisClient.stopConnection();
                primaryStage.close();
            }
            catch (Exception e) {
                e.printStackTrace();
                System.out.println("Issue with closing the connection of the client.");
            }
        });

        challengeOne.setOnAction(e ->{
            primaryStage.setScene(playScene);
        });

        challengeTwo.setOnAction(e ->{
            primaryStage.setScene(playScene);
        });

        challengeThree.setOnAction(e ->{
            primaryStage.setScene(playScene);
        });

        challengeFour.setOnAction(e ->{
            primaryStage.setScene(playScene);
        });

        Rock.setOnAction(event -> {
            thisClient.sendInfo("Rock");
        });

        Paper.setOnAction(event -> {
            thisClient.sendInfo("Paper");
        });

        Scissors.setOnAction(event -> {
            thisClient.sendInfo("Scissors");
        });

        Lizard.setOnAction(event -> {
            thisClient.sendInfo("Lizard");
        });

        Spock.setOnAction(event -> {
            thisClient.sendInfo("Spock");
        });

        playAgainButton.setOnAction(event -> {
            thisClient.sendInfo("Play");

//            thisClient.p1Score = 0;
//            thisClient.theirScore = 0;

//            selfScore = 0;
//            theirScore = 0;

            p1ScoreText.setText("P1 Points:" + p1Score);
            p2ScoreText.setText("P2 Points:" + p2Score);
            p3ScoreText.setText("P3 Points:" + p3Score);

//            selfScoreText.setText("My Points: " + selfScore);
//            theirScoreText.setText("Their Points" + theirScore);
            winner.setText("Winner: ");

            playAgainButton.setVisible(false);
        });


        primaryStage.show();
    }

    private Client createClient() {
        return new Client(data -> {
            Platform.runLater(() -> {
//                selfScore = thisClient.myScore;
//                theirScore = thisClient.theirScore;
                p1Score = thisClient.p1Score;
                p2Score = thisClient.p2Score;
                p3Score = thisClient.p3Score;

                gameState.setText(thisClient.returnThisString);



//                selfScoreText.setText("My Points: " + selfScore);
//                theirScoreText.setText("Their Points" + theirScore);

//                p1ScoreText.setText("P1 Points:" + p1Score);
//                p2ScoreText.setText("P2 Points:" + p2Score);
//                p3ScoreText.setText("P3 Points:" + p3Score);



//                theirMove.setText("Their move: " + thisClient.theirMove);
//                private Text p1Move = new Text("P1 Move:");
//                p1Move.setText("P1 Move:" + thisClient.p1Move);
//                p2Move.setText("P2 Move:" + thisClient.p2Move);
//                p3Move.setText("P3 Move:" + thisClient.p3Move);

                if (thisClient.numPlayers == -1) {
                    winner.setText("A player left. Please close client.");


                }

//                if (selfScore == 3) {
//                    winner.setText("Winner: Me!!");
//                    playAgainButton.setVisible(true);
//                }
//                else if (theirScore == 3) {
//                    winner.setText("Winner: Other guy...");
//                    playAgainButton.setVisible(true);
//                }
            });
        });
    }


    public static void main(String[] args) {
        launch(args);
    }


}

