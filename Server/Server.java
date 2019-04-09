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
    String p1Move;
    String p2Move;
    Consumer<Serializable> callback;
    boolean gameOver = false;
    String winner;
    int numPlayers = 0;
    int maxPlayers = 4;

    public Server(Consumer<Serializable> callback) {
        this.callback = callback;
    }

    //store the active connections
    ArrayList<Connection> connectionList = new ArrayList<Connection>();

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

                    numPlayers++;
                    callback.accept("Found connection");

                    while(true) {
                        Serializable data = (Serializable) in.readObject();
                        this.ClientMove = (String) data;
                        this.madeMove = true;


                        calculateRound();
                    }
                } //end of if not null

            } //end of try
            catch (Exception e) {
                System.out.println("A player has disappeared");
                s = null;

                numPlayers = -1; //holder for if we were to lose a player, then we are going to end game
                callback.accept("Not enough players to play.");
                updateClients();

            } //end of catch
        } //end of run
    } //end of connection method

    //calculates who won the round, and the game
    private synchronized void calculateRound() {



        if ((connectionList.get(0).playerScore != 3 && connectionList.get(1).playerScore != 3)
                && numPlayers >1) {
                //if anyone has not made a move yet, do nothing
                if (!connectionList.get(0).madeMove || !connectionList.get(1).madeMove) {
                    return;
                }
                else if (connectionList.get(0).ClientMove.equals("Rock")) {
                    switch (connectionList.get(1).ClientMove) {
                        case "Rock":
                            break;
                        case "Paper":
                            connectionList.get(1).playerScore++;
                            break;
                        case "Scissors":
                            connectionList.get(0).playerScore++;
                            break;
                        case "Lizard":
                            connectionList.get(1).playerScore++;
                            break;
                        case "Spock":
                            connectionList.get(0).playerScore++;
                            break;
                    }
                }
                else if (connectionList.get(0).ClientMove.equals("Paper")) {
                    switch (connectionList.get(1).ClientMove) {
                        case "Rock":
                            connectionList.get(0).playerScore++;
                            break;
                        case "Paper":
                            break;
                        case "Scissors":
                            connectionList.get(1).playerScore++;
                            break;
                        case "Lizard":
                            connectionList.get(1).playerScore++;
                            break;
                        case "Spock":
                            connectionList.get(0).playerScore++;
                            break;
                    }
                }
                else if (connectionList.get(0).ClientMove.equals("Scissors")) {
                    switch (connectionList.get(1).ClientMove) {
                        case "Rock":
                            connectionList.get(1).playerScore++;
                            break;
                        case "Paper":
                            connectionList.get(0).playerScore++;
                            break;
                        case "Scissors":
                            break;
                        case "Lizard":
                            connectionList.get(0).playerScore++;
                            break;
                        case "Spock":
                            connectionList.get(1).playerScore++;
                            break;
                    }
                }
                else if (connectionList.get(0).ClientMove.equals("Lizard")) {
                    switch (connectionList.get(1).ClientMove) {
                        case "Rock":
                            connectionList.get(1).playerScore++;
                            break;
                        case "Paper":
                            connectionList.get(0).playerScore++;
                            break;
                        case "Scissors":
                            connectionList.get(1).playerScore++;
                            break;
                        case "Lizard":
                            break;
                        case "Spock":
                            connectionList.get(0).playerScore++;
                            break;
                    }
                }
                else if (connectionList.get(0).ClientMove.equals("Spock")) {
                    switch (connectionList.get(1).ClientMove) {
                        case "Rock":
                            connectionList.get(0).playerScore++;
                            break;
                        case "Paper":
                            connectionList.get(1).playerScore++;
                            break;
                        case "Scissors":
                            connectionList.get(0).playerScore++;
                            break;
                        case "Lizard":
                            connectionList.get(1).playerScore++;
                            break;
                        case "Spock":
                            break;
                    }
                }

                //reset so they need to make moves again
                connectionList.get(0).madeMove = false;
                connectionList.get(1).madeMove = false;

                //update the client's version of the scores
                p1Score = connectionList.get(0).playerScore;
                p2Score = connectionList.get(1).playerScore;
                p1Move = connectionList.get(0).ClientMove;
                p2Move = connectionList.get(1).ClientMove;

                callback.accept("Moves have been updated");

                if (connectionList.get(0).playerScore == 3) {
                    winner = "Player 1";
                    gameOver = true;
                }

                else if (connectionList.get(1).playerScore == 3) {
                    winner = "Player 2";
                    gameOver = true;
                }


                updateClients();
    } //end of checking if 6 case statement

        else if (connectionList.get(0).playerScore == 3) {
            if (connectionList.get(0).ClientMove.equals("Play")) {
//                if (connectionList.get(1).ClientMove.equals("Play")) {
                    connectionList.get(0).playerScore = 0;
                    connectionList.get(1).playerScore = 0;

                    connectionList.get(0).madeMove = false;
                    connectionList.get(1).madeMove = false;

                    p1Score = 0;
                    p2Score = 0;
                    winner = "Playing again";
                    callback.accept("replay");
//                }
            }
            else {
                System.out.println("Winner is already P1");
            }
        }

        else if (connectionList.get(1).playerScore == 3) {
            if (connectionList.get(1).ClientMove.equals("Play")) {
//                if (connectionList.get(0).ClientMove.equals("Play")) {
                    connectionList.get(0).playerScore = 0;
                    connectionList.get(1).playerScore = 0;

                    connectionList.get(0).madeMove = false;
                    connectionList.get(1).madeMove = false;

                    p1Score = 0;
                    p2Score = 0;

                    winner = "Playing again";
                    callback.accept("replay");
//                }
            }
            else {
                System.out.println("Winner is already P2");
            }
        }

    } //end of calculateround


    //send the new scores out to the 2 clients
    private synchronized void updateClients() {
        try {
            if (connectionList.get(0) != null) {
                //check if the connection has been closed
                if (connectionList.get(0).s != null) {
                    connectionList.get(0).output.writeObject(p1Score);
                    connectionList.get(0).output.writeObject(p2Score);
                    connectionList.get(0).output.writeObject(numPlayers);
                    connectionList.get(0).output.writeObject(p2Move);
                }
            }
            if (connectionList.get(1) != null) {
                //check if the connection has been closed
                if (connectionList.get(1).s != null) {
                    connectionList.get(1).output.writeObject(p2Score);
                    connectionList.get(1).output.writeObject(p1Score);
                    connectionList.get(1).output.writeObject(numPlayers);
                    connectionList.get(1).output.writeObject(p1Move);
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            System.out.println("A client has closed.");
        }
    } //end of updateClients



}
