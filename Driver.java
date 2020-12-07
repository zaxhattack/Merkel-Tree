/**
 ***************************************
 ** File:    Driver.java
 ** Project: CSCE314 Project, Fall 2020
 ** Authors: Zach Griffin, Tony Yang
 ** Date:    12/06/2020
 ** Section: 501/502
 ** E-mail:  zach.griffin@tamu.edu
 **
 **   This file contains the driver class from which all the program runs.
 **   There are sender and receiver methods based on what the user specifies.
 * 
 **   The sender will send (using TCP sockets) a data file and it's corresponding Merkel 
 **   Tree (saved as a file) to the receiver. The receiver saves both locally, then creates
 **   it's own Merkel tree for the data file. It then compares the tree it generated for the
 **   data file and the received Merkel tree to confirm the file was sent successfully with no
 **   corruption in flight.
 **   
 **
 **********************************************
 */
package merkel;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

public class Driver {

    //size of the chunks the data will be split to
    final static int blockSize = 4096;

    //port to be used for TCP sockets
    final static int port = 9999;

    Scanner sc = new Scanner(System.in);

    public void sendFile(String fileName) throws Exception {
        //create a server socket and a normal socket, which accepts a connection to the server socket
        System.out.println("Waiting for connection...");
        ServerSocket ssocket = new ServerSocket(port);
        Socket socket = ssocket.accept();
        System.out.println("Got connection");

        //create File object
        File file = new File(fileName);

        //keep track of the number of bytes read thus far
        int numBytes = 0;

        //create a byte array buffer to store the chunks of the file for sending
        byte[] buffer = new byte[blockSize];

        //create a new socket output stream
        OutputStream socketOutput = socket.getOutputStream();

        //create a new input stream from the file
        BufferedInputStream fileIn = new BufferedInputStream(new FileInputStream(file));

        //while we have bytes to read from the file, write those bytes to the socket stream
        while ((numBytes = fileIn.read(buffer)) >= 0) {
            //write it using the correct offset cooresponding to the number of bytes read in from the file
            socketOutput.write(buffer, 0, numBytes);
            socketOutput.flush();
        }

        //close the socket
        fileIn.close();
        socketOutput.close();
        socket.close();
        ssocket.close();
    }

    public void receiveFile(String fileName, String address) throws Exception {
        //create a new TCP socket using the address specified
        Socket socket = new Socket(address, port);

        //make a new file output stream using the file name specified
        FileOutputStream fileStream = new FileOutputStream(fileName);

        //create a byte array buffer to store the chunks of the file we are receiving
        byte[] buffer = new byte[blockSize];

        //keep track of the number of bytes read thus far
        int numBytes;

        //create a new socket input stream
        InputStream socketInput = socket.getInputStream();

        while ((numBytes = socketInput.read(buffer)) >= 0) {
            fileStream.write(buffer, 0, numBytes);
            fileStream.flush();
        }

        //close all streams and sockets
        fileStream.close();
        socketInput.close();
        socket.close();
    }

    public void send() throws Exception {

        //get the file name from the user
        System.out.println("Enter file name:");
        String fileName = sc.next();

        //create a File object for the file and call the merkel tree constructor using it
        File file = new File(fileName);
        merkelTree tree = new merkelTree(file, blockSize);

        //print the merkel tree to a file
        tree.printToFile("merkel.txt");

        //first send the file
        sendFile(fileName);

        //then the merkel tree file
        sendFile("merkel.txt");
    }

    public void receive() throws Exception {
        //collect the IP address to send to
        System.out.println("Enter the server address:");
        String address = sc.next();

        //first receive the file
        receiveFile("data_rec.txt", address);
        System.out.println("Data recevied");
        //then receive it's merkel tree
        receiveFile("merkel_rec.txt", address);

        File file = new File("data_rec.txt");

        merkelTree data_tree = new merkelTree(file, blockSize);
        merkelTree rec_tree = new merkelTree("merkel_rec.txt");
        
        //for demo purposes
        data_tree.printToFile("merkel_tree_generated.txt");

        int compare = data_tree.compareTo(rec_tree);
        if (compare == -1) {
            System.out.println("The data was received successfully!");
        } else {
            System.out.println("The data was NOT received successfully!");
            System.out.println("Failed at block " + compare);
        }
    }

    public void run() throws Exception {
        System.out.println("(S)end or (R)eceive file?");
        String input = sc.next().toLowerCase();

        //call the appropriate method for sending or receiving a file 
        if (input.equals("s") || input.equals("send")) {
            send();
        } else if (input.equals("r") || input.equals("receive")) {
            receive();
        }
    }

    public static void main(String[] args) throws Exception {
        //create a new instance of Driver and call run method
        Driver main = new Driver();
        main.run();
    }
}
