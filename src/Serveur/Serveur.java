package Serveur;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashSet;

public class Serveur extends Thread{
    static HashSet<String> listeMessageClient = new HashSet<>(); // Hashset qui permet de conserver tout les messages envoyés par un client
    static int cptClient = 0;
    static class Worker implements Runnable {

        private final Socket sock; // Socket de connexion du client
        private PrintWriter writer;
        private static ArrayList<Worker> listThread;
        private boolean doublon = false; // si doublon passera à true
        private boolean quit = false; // si le client se déconnecté manuellement du serveur, il passera à true
        private boolean connexion = true; // pour la première connexion
        String nomClient="";
        private static Object lock = new Object();


        public Worker(Socket sock,ArrayList<Worker> listThread) {
            this.sock = sock;
            this.listThread = listThread;
        }

        private void messageToAllClient(String messageServeur){ // méthode qui permet d'envoyer un message envoyer par un client à tous les clients
            for(Worker st: listThread){
               /*if(st.sock == sock){ // si l'on ne veut pas envoyer le message au client qui envoie ce message
                    continue;
                }*/
                st.writer.println(messageServeur);
            }
        }

        @Override
        public void run() {
            DataInputStream in;
            synchronized (lock) {
                cptClient++;
            }
            try {
                in = new DataInputStream(sock.getInputStream());
                BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                int numClient = sock.hashCode(); // Unique pour chaque client. Cela permettra donc de l'utiliser comme identidiant
                if(cptClient==1){
                    System.out.println("Un client s'est connecté. "+ (cptClient)+" client connecté au serveur" );
                }else{
                    System.out.println("Un client s'est connecté. "+ (cptClient)+" clients connectés au serveur" ); // Juste pour être cohérent avec l'orthographe
                }
                writer = new PrintWriter(sock.getOutputStream(),true);
                while(true) {
                    String messageClient = reader.readLine();
                    int indexOfMessage = messageClient.indexOf(">");
                    String messageListe = messageClient.substring(indexOfMessage+1);
                    if(connexion&&!messageClient.contains("quit")){
                        int indexOfNomClient = messageClient.indexOf(":"); // caractère permettant de délimiter la fin du prenom entrée par le client
                        nomClient = messageClient.substring(0,indexOfNomClient); // permet de récupérer le nom du client
                        System.out.println("Connexion établie. Nom du client: " + nomClient + ". Identifiant client: " + numClient);
                        connexion =false;
                    }
                    if(messageClient.contains("quit")){
                        if(nomClient.isEmpty()){
                            System.out.println("Le client s'est déconnecté sans envoyer de message");
                            writer.println("quit"); // envoie à la classe ClientEcoute
                            quit = true;

                        }else{
                            System.out.println("Arret de: "+nomClient+". Identifiant: " + numClient+". Connexion fermée avec ce client");
                            writer.println("quit"); // envoie à la classe ClientEcoute
                            quit = true;
                        }
                    }
                    if (listeMessageClient.isEmpty()){
                        synchronized (lock) {
                            listeMessageClient.add(messageListe);
                        }
                    }else {
                        for (String st : listeMessageClient) { // On cherche dans la liste de tout les messages enregistrés pour ce client si le dernier message envoyé a déjà été envoyé précédemment
                            if (st.equals(messageListe)) {
                                System.out.println("Doublon, " + nomClient +" a perdu ! Identifiant: "+numClient + ". Connexion fermé avec ce client");
                                doublon = true;
                                writer.println("9999");
                                break;
                            }
                        }
                    }
                    if (quit || doublon ) {
                        synchronized (lock) {
                            cptClient--;
                        }
                        if(cptClient==0){
                            System.out.println("Aucun client connecté au serveur ! Attente de connexion....");
                        }else{
                            System.out.println(cptClient +" client connecté au serveur !");
                        }
                        break;
                    } else {
                        synchronized (lock) {
                            listeMessageClient.add(messageListe);
                            System.out.println(listeMessageClient);
                        }
                    }
                    System.out.println(messageClient);
                    messageToAllClient(messageClient);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

    }

    public static void main(String[] args) {

       try(ServerSocket ss = new ServerSocket(5000)){
           ArrayList<Worker> listThread = new ArrayList<>();
           while(true) {
               Socket sock = ss.accept();
               Worker w = new Worker(sock,listThread);
               listThread.add(w);
               Thread t = new Thread(w);
               t.start();
           }
       }catch (Exception e){
           System.out.println("Error occured in main: "+ e.getStackTrace());
       }
    }

}