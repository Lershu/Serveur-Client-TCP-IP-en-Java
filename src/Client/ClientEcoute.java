package Client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class ClientEcoute implements Runnable { // classe qui écoute les messages reçus par le serveur

    private final Socket socket;
    private final BufferedReader input;
    private static Object lock = new Object();

    public ClientEcoute(Socket s) throws IOException { // méthode qui permet d'initialiser l'écoute de la classe
        this.socket = s;
        this.input = new BufferedReader( new InputStreamReader(socket.getInputStream()));
    }
    @Override
    public void run() {
        String rep = "";
        try {
            while(true) {
                    rep = input.readLine();
                if(rep.equals("quit")){ // si le serveur envoie "quit" on arrête le client dans la classe client
                    System.out.println("Arrêt du Client");
                    break;
                }
                if(rep.equals("9999")){ // code d'erreur du doublon envoyé par le serveur qui permet aussi d'arrêter le client
                    System.out.println("Doublon, vous n'êtes plus connecté au serveur !");
                    break;
                }
                System.out.println(rep);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                input.close();
                socket.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}