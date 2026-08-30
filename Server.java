package group.chatting.application;

import java.io.*;
import java.net.*;
import java.util.ArrayList;

public class Server {

    private static ArrayList<Socket> clients = new ArrayList<>();

    public static void main(String[] args) {
        try {
            ServerSocket serverSocket = new ServerSocket(5000);
            System.out.println("Server started on port 5000...");

            while (true) {
                Socket socket = serverSocket.accept();
                clients.add(socket);
                System.out.println("New client connected!");

                new ClientHandler(socket, clients).start();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

class ClientHandler extends Thread {
    private Socket socket;
    private ArrayList<Socket> clients;
    private BufferedReader reader;
    private PrintWriter writer;

    public ClientHandler(Socket socket, ArrayList<Socket> clients) {
        this.socket = socket;
        this.clients = clients;
        try {
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new PrintWriter(socket.getOutputStream(), true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            String message;
            while ((message = reader.readLine()) != null) {
                broadcast(message);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void broadcast(String message) {
        for (Socket client : clients) {
            try {
                PrintWriter pw = new PrintWriter(client.getOutputStream(), true);
                pw.println(message);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}


