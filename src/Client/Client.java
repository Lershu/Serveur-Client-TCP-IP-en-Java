package Client;

import java.io.*;
import java.net.Socket;

public class Client {

    public static void main(String[] args) throws IOException {

        Socket sock = new Socket("127.0.0.1", 5000);
        String clientName="null"; // choix arbitraire, cela permet juste de rentrer une première fois dans la boucle pour initialiser le nom du client
        DataOutputStream out = new DataOutputStream(sock.getOutputStream());
        PrintWriter writer = new PrintWriter(out, true);
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        String clientMessage = "";
        ClientEcoute cliEcoute = new ClientEcoute(sock);
        Thread t = new Thread(cliEcoute); // On crée le thread qui va donc écouter le serveur
        t.start(); // on lance le thread

        do {
            if(!t.isAlive()){ // si le thread n'est plus en vie, cela veut dire que le serveur à envoyer un message d'erreur et que la connexion est fermé entre les deux
                break;
            }
            if(clientName.equals("null")){ // on initialise clientName à null pour rentrer une seule fois dans la boucle
                System.out.println("Enter your name: ");
                clientName = br.readLine();
                if (clientName.equals("quit")){
                    writer.println("quit"); // permettra d'envoyer le message au serveur pour qu'il puisse fermer la connexion directement
                    break;
                }
            }else{
                    String message = (clientName+": message -> ");
                    System.out.println(message);
                    clientMessage = br.readLine();
                    writer.println(message + " "+ clientMessage); // envoie le message du client au serveur
                }
                try {
                    Thread.sleep(300); // cela permet juste d'ameliorer l'esthétique lorsque le client recoit le message qui l'a envoyé par le serveur
                } catch (InterruptedException e) { // La réponse du serveur arrive avant la demande de nouveau message
                    e.printStackTrace();
                }
        }while(true);
    }
}
