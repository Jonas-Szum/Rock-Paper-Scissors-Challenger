package sample;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.Socket;
import java.util.function.Consumer;
import java.util.ArrayList;


public class Client {

    private int port;
    private InetAddress IP;
    Connection connection = new Connection();
    private boolean validSettings = false;
    private Consumer<Serializable> callback;
    boolean connected = false;
    int p1Score, p2Score, p3Score;
    int numPlayers = 0;
    int myPlayerID;
    String p1Move, p2Move, p3Move;
    String returnThisString;

    //default constructor
    public Client(Consumer<Serializable> callback) {
        this.callback = callback;
    }

    //get and set of port
    public int getPort() {
        return port;
    }
    public void setPort(int newPort) {
        this.port = newPort;
    }

    //get and set of IP
    public InetAddress getIP() {
        return IP;
    }
    public void setIP(InetAddress newIP) {
        this.IP = newIP;
    }

    //get and set of validsettings
    public boolean isValid() {
        return validSettings;
    }
    public void setValid(boolean valid) {
        this.validSettings = valid;
    }

    //send data
    public void sendInfo(Serializable data) {
        try {
            connection.output.writeObject(data);
        }
        catch (Exception e) {
//            e.printStackTrace();
            System.out.println("Issue writing out of client");
        }
    }

    //start connection
    public void startConnection() throws Exception {
        connection.start();
    }

    //stop connection
    public void stopConnection() throws Exception {
        this.connected = false;
        if (connection.s!= null) {
            if (connection.input != null) {
                connection.input.close();
            }
            if (connection.output != null) {
                connection.output.close();
            }
            connection.s.close();
        }
    }




    //inner class
    class Connection extends Thread {
        Socket s;
        ObjectInputStream input;
        ObjectOutputStream output;

        public void run() {
            try {
                s = new Socket(getIP(),getPort());

                output = new ObjectOutputStream(s.getOutputStream());
                input = new ObjectInputStream(s.getInputStream());
                s.setTcpNoDelay(true);
		//Serializable numPlayersSer = input.readObject();
                System.out.println("New connection client created.");

                //take in input
                while(connected) {
//                    Serializable firstScore = (Serializable) input.readObject();
//                    p1Score = (Integer) firstScore;
//
//                    Serializable secScore = (Serializable) input.readObject();
//                    p2Score = (Integer) secScore;
//
//                    Serializable thirdScore = (Serializable) input.readObject();
//                    p3Score = (Integer) thirdScore;
//
//                    Serializable amIAlone = (Serializable) input.readObject();
//                    numPlayers = (Integer) amIAlone;
//
//                    Serializable p1 = (Serializable) input.readObject();
//                    p1Move = (String) p1;
//
//                    Serializable p2 = (Serializable) input.readObject();
//                    p2Move = (String) p2;
//
//                    Serializable p3 = (Serializable) input.readObject();
//                    p3Move = (String) p3;

                    Serializable receivedString = (Serializable) input.readObject();
                    returnThisString = (String) receivedString;

                    Serializable amIAlone = (Serializable) input.readObject();
                    numPlayers = (Integer) amIAlone;


                    callback.accept("Changes made");
                }


            }
            catch (Exception e) {
                if (s !=null) {
                    System.out.println("Client was closed.");
                }
                else {
//                    e.printStackTrace();
                    System.out.println("Client could not find server and was closed.");
                }

            }//end of try/catch


        }//end of run

    }//end of connection class
}
