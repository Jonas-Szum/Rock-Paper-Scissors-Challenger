package sample;

import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.function.Consumer;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;

public class Server {

    private int port;
    private ServerSocket server_socket;
    boolean active = false;
    boolean validPort = false;
    int p1Score = 0;
    int p2Score = 0;
    //int p3Score = 0;
    volatile ArrayList<Integer> playerScores;	//new to this version
    volatile ArrayList<String>  playerMoves;	//new to this version
    String p1Move;
    String p2Move;
    //String p3Move;
    Consumer<Serializable> callback;
    boolean gameOver = false;
    String winner;
    int numPlayers = 0;
    int maxPlayers = 3;
    int clientIDs = 1;

    //store the active connections
    ArrayList<Connection> connectionList = new ArrayList<Connection>();

    public Server(Consumer<Serializable> callback) {
        this.callback = callback;
        playerScores = new ArrayList<Integer>(); //can now handle any amount of players
        playerMoves = new ArrayList<String>();
        for(int i = 0; i < maxPlayers; i++)
        {
            playerScores.add(0);
            playerMoves.add("");
        }
    }


    //get the port of the server
    int getPort() {
        return port;
    }

    //set the port of the server
    void setPort(int port) {
        if (!active) {
            this.port = port;
        }
        else {
            System.out.println("Cannot change port because server is already on");
        }
    }

    //get status of server
    boolean getActive() {
        return active;
    }

    //starts looking for connections via Client subclass
    void turnOnServer() {
        this.active = true;

        try {server_socket = new ServerSocket(port); }
        catch (Exception e) { e.printStackTrace(); }

        //creates four players
        for (int i=0; i < maxPlayers; i++) {
            Connection newClient = new Connection();
            newClient.playerID = clientIDs;
            clientIDs++;
            newClient.start();
            connectionList.add(newClient);
        }
    }

    //close all of the existing threads connected to the server
    void turnOffServer() throws Exception {
        for (int i = 0; i < connectionList.size(); i++) {
            if (connectionList.get(i).s != null) {
                if (connectionList.get(i).output !=null && connectionList.get(i).input.read() != -1) {
                    connectionList.get(i).output.close();
                }
                if (connectionList.get(i).input !=null && connectionList.get(i).input.read() != -1) {
                    connectionList.get(i).input.close();
                }
                connectionList.get(i).s.close();
            }
        }
        if (server_socket !=null ) {
            server_socket.close();
        }
        this.active = false;
        System.out.println("All connections closed.");
    }

    //helper method to get a socket;
    private Socket receiveClient() {
        try {
            return server_socket.accept();
        }
        catch (Exception e) {
            System.out.println("Server socket closed before able to accept.");
        }
        return null;
    }

    //inner class for connecting server with threads
    class Connection extends Thread {
        Socket s;
        ObjectInputStream input;
        ObjectOutputStream output;
        String ClientMove;
        boolean madeMove = false;
        int playerScore = 0;
        int playerID;
        int currentOpponent = -1;
        volatile boolean alreadyInGame = false;
        public void run() {
            try {
                //get a socket from server.accept()
                s = receiveClient();

                //update the input and output streams
                if (s != null) {
                    ObjectOutputStream out = new ObjectOutputStream(s.getOutputStream());
                    ObjectInputStream in = new ObjectInputStream(s.getInputStream());
                    this.output = out;
                    this.input = in;
                    playerID = numPlayers; //starts at 0, goes up to numPlayers-1
                    numPlayers++;
                    updateClients();
                    callback.accept("Found connection");

                    while(true) {
                        Serializable challenge = (Serializable) in.readObject(); //read someone challenging someone else, or accepting someone else's challenge
                        if(alreadyInGame == false) //user is challenging someone else
                        {
                            alreadyInGame = true;
                            currentOpponent = (int)challenge; //set current opponent the the user that you challenged

                            connectionList.get(currentOpponent).alreadyInGame = true;  //set opponent's current opponent to you
                            connectionList.get(currentOpponent).currentOpponent = playerID; //after that, get ready to read the user's moves
                            updateClients(); //update the clients GUIs to reflect who's in a game
                            Serializable data = (Serializable) in.readObject();
                            this.ClientMove = (String) data;
                            this.madeMove = true;
                            //notify other user that they're in a game
                        }
                        else //user is already in game, and is sending their move
                        {
                            Serializable data = (Serializable) in.readObject();
                            this.ClientMove = (String) data;
                            this.madeMove = true;
                        }


                        calculateRound(playerID, currentOpponent); //if you are not versing anyone, currentOpponent is -1, go to calculateRound to see why
                    }
                } //end of if not null

            } //end of try
            catch (Exception e) {
                System.out.println("A player has disappeared");
                s = null;

                //numPlayers = -1; //holder for if we were to lose a player, then we are going to end game
                numPlayers = -1; //new to this version
                callback.accept("Not enough players to play.");
                //BELOW NEEDS TO BE CHANGED
                updateClients();

            } //end of catch
        } //end of run
    } //end of connection method

    //calculates who won the round, and the game
    private synchronized void calculateRound(int player1, int player2) { //now takes the ID's of the two players, so it knows who to check
        if (	player2 != -1 //if player2 is -1, then there is not a game currently going on, this if statement shorts out
                && (connectionList.get(player1).playerScore != 3 && connectionList.get(player2).playerScore != 3)
                && numPlayers >1) {
            //if anyone has not made a move yet, do nothing
            if (!connectionList.get(player1).madeMove || !connectionList.get(player2).madeMove) {
                return;
            }
            else if (connectionList.get(player1).ClientMove.equals("Rock")) {
                switch (connectionList.get(player2).ClientMove) {
                    case "Rock":
                        break;
                    case "Paper":
                        connectionList.get(player2).playerScore++;
                        break;
                    case "Scissors":
                        connectionList.get(player1).playerScore++;
                        break;
                    case "Lizard":
                        connectionList.get(player2).playerScore++;
                        break;
                    case "Spock":
                        connectionList.get(player1).playerScore++;
                        break;
                }
            }
            else if (connectionList.get(player1).ClientMove.equals("Paper")) {
                switch (connectionList.get(player2).ClientMove) {
                    case "Rock":
                        connectionList.get(player1).playerScore++;
                        break;
                    case "Paper":
                        break;
                    case "Scissors":
                        connectionList.get(player2).playerScore++;
                        break;
                    case "Lizard":
                        connectionList.get(player2).playerScore++;
                        break;
                    case "Spock":
                        connectionList.get(player1).playerScore++;
                        break;
                }
            }
            else if (connectionList.get(player1).ClientMove.equals("Scissors")) {
                switch (connectionList.get(player2).ClientMove) {
                    case "Rock":
                        connectionList.get(player2).playerScore++;
                        break;
                    case "Paper":
                        connectionList.get(player1).playerScore++;
                        break;
                    case "Scissors":
                        break;
                    case "Lizard":
                        connectionList.get(player1).playerScore++;
                        break;
                    case "Spock":
                        connectionList.get(player2).playerScore++;
                        break;
                }
            }
            else if (connectionList.get(player1).ClientMove.equals("Lizard")) {
                switch (connectionList.get(player2).ClientMove) {
                    case "Rock":
                        connectionList.get(player2).playerScore++;
                        break;
                    case "Paper":
                        connectionList.get(player1).playerScore++;
                        break;
                    case "Scissors":
                        connectionList.get(player2).playerScore++;
                        break;
                    case "Lizard":
                        break;
                    case "Spock":
                        connectionList.get(player1).playerScore++;
                        break;
                }
            }
            else if (connectionList.get(player1).ClientMove.equals("Spock")) {
                switch (connectionList.get(player2).ClientMove) {
                    case "Rock":
                        connectionList.get(player1).playerScore++;
                        break;
                    case "Paper":
                        connectionList.get(player2).playerScore++;
                        break;
                    case "Scissors":
                        connectionList.get(player1).playerScore++;
                        break;
                    case "Lizard":
                        connectionList.get(player2).playerScore++;
                        break;
                    case "Spock":
                        break;
                }
            }

            //reset so they need to make moves again
            connectionList.get(player1).madeMove = false;
            connectionList.get(player2).madeMove = false;

            //update the client's version of the scores
            p1Score = connectionList.get(player1).playerScore;
            p2Score = connectionList.get(player2).playerScore;
            p1Move = connectionList.get(player1).ClientMove;
            p2Move = connectionList.get(player2).ClientMove;

            callback.accept("Moves have been updated");

            if (connectionList.get(player1).playerScore == 3) {
                winner = "Player " + String.valueOf(player1);
                gameOver = true;
            }

            else if (connectionList.get(player2).playerScore == 3) {
                winner = "Player " + String.valueOf(player2);
                gameOver = true;
            }


            updateClients(); //server knows which players to send data to this way
        } //end of checking if 6 case statement

        else if (connectionList.get(player1).playerScore == 3) {
            if (connectionList.get(player1).ClientMove.equals("Play")) {

                //when you click play again, you elicit this so everyone is available to play
                for (Connection conn : connectionList) {
                    conn.alreadyInGame = false;
                }

                connectionList.get(player1).playerScore = 0;
                connectionList.get(player2).playerScore = 0;

                connectionList.get(player1).madeMove = false;
                connectionList.get(player2).madeMove = false;
                playerScores.set(player1, 0);
                playerScores.set(player2, 0);
                ///p1Score = 0;
                //p2Score = 0;
                winner = "Playing again";
                callback.accept("replay");
            }
            else {
                System.out.println("Winner is already P1");
            }
        }

        else if (connectionList.get(player2).playerScore == 3) {
            if (connectionList.get(player2).ClientMove.equals("Play")) {
                connectionList.get(player1).playerScore = 0;
                connectionList.get(player2).playerScore = 0;

                connectionList.get(player1).madeMove = false;
                connectionList.get(player2).madeMove = false;

                playerScores.set(player1, 0);
                playerScores.set(player2, 0);
                //p1Score = 0;
                //p2Score = 0;

                winner = "Playing again";
                callback.accept("replay");
            }
            else {
                System.out.println("Winner is already P2");
            }
        }

    } //end of calculateround


    //send the new scores out to the 2 clients
    private synchronized void updateClients() {
        try {
            for (Connection conn : connectionList) { //update everyone
                if (conn != null) {
                    if (conn.s != null) {
                        String playerInformation = "Not yet Implemented";
                        conn.output.writeObject(numPlayers);		//send how many players are connected
                        for(int i = 0; i < numPlayers; i++)
                        {
                            conn.output.writeObject(connectionList.get(i).alreadyInGame); //send all users information regarding whether or not a user is in a game already
                        }
                        conn.output.writeObject(conn.playerID); //write the connection's playerID
                        conn.output.writeObject(playerInformation); //send all user turns to clients
                    }
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            System.out.println("A client has closed.");
        }
    } //end of updateClients



}
