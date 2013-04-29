/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
import java.io.*;
import java.util.*;
import java.net.*;
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

class Connexion extends Thread{
    Socket cs;
    String current_user;
    ObjectInputStream ois = null;
    ObjectOutputStream oos = null;
    
    public Connexion(Socket cs, String current_user, ObjectInputStream ois, ObjectOutputStream oos){
        this.cs = cs;
        this.current_user = current_user;
        this.ois = ois;
        this.oos = oos;
        this.start();
    }

    public void run(){

        for( ; ; ){
            Command command = null;
            try {
                command = (Command) ois.readObject();
            } catch (IOException ex) {
                Logger.getLogger(Connexion.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(Connexion.class.getName()).log(Level.SEVERE, null, ex);
            }
            String response = "";
            if( command.command.equals("LIST") ){
                    Set set = ProiectServer.users.entrySet();

                    Iterator it = set.iterator();

                    while(it.hasNext()){
                        Map.Entry me = (Map.Entry) it.next();
                        response += " " + me.getKey();
                    }
                    try {
                        oos.writeObject(response);
                    } catch (IOException ex) {
                        Logger.getLogger(Connexion.class.getName()).log(Level.SEVERE, null, ex);
                    }

                } else if( command.command.equals("MSG") ){
                   ObjectOutputStream sender = ProiectServer.users.get(command.nickname);
                    try {
                        sender.writeObject(current_user + " : " + command.message);
                    } catch (IOException ex) {
                        Logger.getLogger(Connexion.class.getName()).log(Level.SEVERE, null, ex);
                    }


                } else if( command.command.equals("BCAST") ){
                   Set set = ProiectServer.users.entrySet();

                   Iterator it = set.iterator();

                   ObjectOutputStream sender = null;

                   while(it.hasNext()){
                       Map.Entry me = (Map.Entry) it.next();
                       sender = (ObjectOutputStream) me.getValue();
                        try {
                            sender.writeObject(current_user + " : " + command.message);
                        } catch (IOException ex) {
                            Logger.getLogger(Connexion.class.getName()).log(Level.SEVERE, null, ex);
                        }
                   }

                } else if( command.command.equals("NICK") ){
                     response = "";
                     if(ProiectServer.users.get(command.nickname) == null){
                           ProiectServer.users.remove(current_user);

                           //Modifica nickname-ul userului curent
                           current_user = command.nickname;

                           ProiectServer.users.put(current_user,oos);
                           response = "Nickname successfully changed!";
                    } else {
                         response = "The nickname " + command.nickname + " is allready taken!\n Please choose another nickname";
                    }
                    try {
                          oos.writeObject(response);
                    } catch (IOException ex) {
                           Logger.getLogger(Connexion.class.getName()).log(Level.SEVERE, null, ex);
                    }

                } else if( command.command.equals("QUIT") ){
                   ProiectServer.users.remove(current_user);
                   break;

                }
        }
        try {
            oos.writeObject("Successfully disconnected!");
        } catch (IOException ex) {
            Logger.getLogger(Connexion.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}


class SelectName{
    
}

public class ProiectServer {

    /**
     * @param args the command line arguments
     */
    //String[] users;

    /*Iterator iter = set.iterator();
    while (iter.hasNext()) {
      System.out.println(iter.next());
    }*/
    static HashMap<String, ObjectOutputStream> users = new HashMap<String, ObjectOutputStream>();
    

    static String select_name(Socket cs, ObjectInputStream ois, ObjectOutputStream oos) throws IOException, ClassNotFoundException{
        
        boolean response = false;
        String nickname;
        
        for( ; ; ){
            nickname = (String) ois.readObject();
            System.out.println(nickname);
            if(users.get(nickname) != null){
                oos.writeObject(response);
            } else {
                users.put(nickname,oos);
                response = true;
                oos.writeObject(response);
                break;
            }

        }

        return nickname;
    }

    public static void main(String[] args) throws IOException, ClassNotFoundException {
        int port;
        Scanner sc = new Scanner(System.in);
        System.out.println("Give the port :\t");
        port = sc.nextInt();
       
            ServerSocket ss = new ServerSocket(port);

        System.out.println("The Server is on");
        for( ; ; ){

                Socket cs = ss.accept();
                ObjectInputStream ois = new ObjectInputStream(cs.getInputStream());
                ObjectOutputStream oos = new ObjectOutputStream(cs.getOutputStream());

                String current_user = select_name(cs,ois,oos);
                System.out.println(current_user + " is now online!");
                new Connexion(cs, current_user, ois, oos);



        }
        


    }

}