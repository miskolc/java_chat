/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
import java.net.*;
import java.io.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

class Command implements Serializable{
      String command;
      String nickname;
      String message;
      public static final long serialVersionUID = 24362462L;
}
/**
 *
 * @author dragos
 */


class Receiver extends Thread {

    ObjectInputStream ois = null;

    public Receiver(ObjectInputStream ois){
        this.ois = ois;
    }

   

    public void run(){
        String response = null;

        for( ; ; ){
            try {
                response = (String) ois.readObject();
            } catch (IOException ex) {
                Logger.getLogger(Receiver.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(Receiver.class.getName()).log(Level.SEVERE, null, ex);
            }
            if( response.equals("Successfully disconnected!") ){
                System.out.println(response);
                
                synchronized (this) {
                    ProiectClient.stopApplication = true;
                    notifyAll();
                }
                break;

            } else {
                System.out.println(response);
            }
        }
    }
}

class Sender extends Thread {

    Scanner sc = null;
    ObjectOutputStream oos = null;
    private Receiver receiver = null;
    
    public Sender(Scanner sc, ObjectOutputStream oos , Receiver receiver){
        this.sc = sc;
        this.oos = oos;
        this.receiver = receiver;
    }

    public void run(){
        for( ; ; ){
                Command command = new Command();
                command.command = sc.next();

               

                if( command.command.equals("LIST") ){
                    sc.nextLine();
                    try {
                        oos.writeObject(command);
                    } catch (IOException ex) {
                        Logger.getLogger(Sender.class.getName()).log(Level.SEVERE, null, ex);
                    }

                } else if( command.command.equals("MSG") ){
                   command.nickname = sc.next();
                   command.message = sc.nextLine();
                    try {
                        oos.writeObject(command);
                    } catch (IOException ex) {
                        Logger.getLogger(Sender.class.getName()).log(Level.SEVERE, null, ex);
                    }

                } else if( command.command.equals("BCAST") ){
                   command.message = sc.nextLine();
                    try {
                        //System.out.println(command.nickname + " :\t" + command.message);
                        oos.writeObject(command);
                    } catch (IOException ex) {
                        Logger.getLogger(Sender.class.getName()).log(Level.SEVERE, null, ex);
                    }

                } else if( command.command.equals("NICK") ){
                   command.nickname = sc.next();
                   sc.nextLine();
                    try {
                        oos.writeObject(command);
                    } catch (IOException ex) {
                        Logger.getLogger(Sender.class.getName()).log(Level.SEVERE, null, ex);
                    }

                } else if( command.command.equals("QUIT") ){
                    try {
                        oos.writeObject(command);
                        // TREBUIE OPRIT CUMVA
                    } catch (IOException ex) {
                        Logger.getLogger(Sender.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    
                        synchronized(this.receiver){
                            try {
                                this.receiver.wait();
                            } catch (InterruptedException ex){
                            }
                        }
                    
                    if(ProiectClient.stopApplication == true) {
                        break;
                    }
                } else {
                    System.out.println("Invalid command. Commands available: LIST, MSG, BCAST, NICK, QUIT");
                }


            }
    }
}

public class ProiectClient {

    /**
     * @param args the command line arguments
     */
    static Scanner sc;
    static boolean stopApplication = false;

    private static void select_name(Socket cs, ObjectInputStream ois,  ObjectOutputStream oos)
            throws IOException, ClassNotFoundException{
        boolean name_selected = false;
        String nickname;

        System.out.println("Choose a nickname");
        while(!name_selected){
            nickname = sc.next();
            oos.writeObject(nickname);
            name_selected = (Boolean) ois.readObject();
            if(!name_selected){
                System.out.println("The name " + nickname + " is allready taken!");
                System.out.println("Please choose another name :\t");
            }
            // true => nickname is unique
            //false => there is another user with the same nickname
        }
    }

    public static void main(String[] args) throws IOException, ClassNotFoundException {
       String adresa; int port;
       sc = new Scanner(System.in);
       System.out.println("Address and port :\t");
       adresa = sc.next(); port = sc.nextInt();

       Socket cs = null;
        try {
            cs = new Socket(adresa, port);
            
        } catch (UnknownHostException ex) {
            System.out.println("Unable to establish server connection!");
        } catch (IOException ex) {
            System.out.println("Unable to establish server connection!");
        }
        System.out.println("able to establish server connection!");

        ObjectOutputStream oos = new ObjectOutputStream(cs.getOutputStream());
        ObjectInputStream ois = new ObjectInputStream(cs.getInputStream());
        
        select_name(cs,ois,oos);

        

        
        Receiver receiver = new Receiver(ois);
        Sender sender = new Sender(sc,oos,receiver);
        //I pass the receiver to the Sender constructor because I want the Sender to wait until the receiver
        //receives a message that the client was successfully disconnected from the Server and send calls
        //the Notify function

        sender.start();
        receiver.start();
       
    }

}
