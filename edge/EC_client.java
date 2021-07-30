// Java implementation for a client 
// Save file as Client.java 
  
import java.io.*; 
import java.net.*; 
import java.util.*;
import java.nio.file.*;


import it.unisa.dia.gas.jpbc.*;
import it.unisa.dia.gas.plaf.jpbc.pairing.PairingFactory;
import java.math.BigInteger;
import java.security.KeyPair;
import java.security.MessageDigest;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
  
// Client class 
public class EC_client  
{ 
	public static ServerSocket ss = null; 
	public static Field Zr,G1, GT;
	public static Element g, g_e, PK, SK, ace_gamma, e_i;
	public static int KGC_port = 5487;
	public static int EC_port = 5278;
    public static void main(String[] args) throws IOException  
    { 
        try
        { 
            Scanner in = new Scanner(System.in); 
              
            PairingFactory.getInstance().setUsePBCWhenPossible(true);
            Pairing pairing = PairingFactory.getPairing("a.properties");
            
            Zr = pairing.getZr();
            G1 = pairing.getG1();
            Field GT = pairing.getGT();
            Element ex = Zr.newRandomElement();
            System.out.println(Zr.getOrder() + "\n" + G1.getOrder());

            // getting localhost ip 
            InetAddress ip = InetAddress.getByName("localhost"); 
      
            // establish the connection with server port 5056 
            Socket s = new Socket(ip, KGC_port); 
            ss = new ServerSocket(EC_port); 
      
            // obtaining input and out streams 
            ObjectOutputStream dos = new ObjectOutputStream(s.getOutputStream()); 
            ObjectInputStream dis = new ObjectInputStream(s.getInputStream()); 
            
      
            //Get public parameters from KGC  
            dos.writeObject("Initialize");
            //g
            byte[] recb = (byte[]) dis.readObject();
            g = G1.newElementFromBytes(recb);
            
            //g1
            recb = (byte[]) dis.readObject();
            Element g1 = G1.newElementFromBytes(recb);
            
            //h
            recb = (byte[]) dis.readObject();
            Element h = G1.newElementFromBytes(recb);
            
            //T
            recb = (byte[]) dis.readObject();
            Element T = GT.newElementFromBytes(recb);
            
            
            dos.writeObject("EC");
            //g^e
            recb = (byte[]) dis.readObject();
            g_e = G1.newElementFromBytes(recb);
            
            //ace * gamma
            recb = (byte[]) dis.readObject();
            ace_gamma = Zr.newElementFromBytes(recb);

            dos.writeObject("Exit");
            
            Element delta = Zr.newRandomElement();
            Element g_actually_use = g.duplicate();
            System.out.println(g_actually_use);
            SK = g_actually_use.powZn(delta);
            
            g_actually_use = g_e.duplicate();
            PK = g_actually_use.powZn(delta);
            
            // closing resources 
            in.close(); 
            dis.close(); 
            dos.close(); 
            
            while (true)  
            { 
                Socket user = null; 
                  
                try 
                { 
                    // socket object to receive incoming client requests 
                    user = ss.accept(); 
                      
                    System.out.println("Edge Device connected " + user); 
                      
                    // obtaining input and out streams 
                    ObjectOutputStream uos = new ObjectOutputStream(user.getOutputStream());
                    ObjectInputStream uis = new ObjectInputStream(user.getInputStream());
                    
                    
                    System.out.println("Assigning new thread for this client!"); 
      
                    // create a new thread object 
                    Thread t = new ClientThread(user, uos, uis); 
      
                    // Invoking the start() method 
                    t.start(); 
                      
                } 
                catch (Exception e){ 
                    s.close();
                    System.out.println(e.getMessage()); 
                } 
            } 
            
           
        }catch(Exception e){ 
            e.printStackTrace(); 
        } 
    } 
} 

//ClientHandler class 
class ClientThread extends Thread  
{ 
	
 DateFormat fordate = new SimpleDateFormat("yyyy/MM/dd"); 
 DateFormat fortime = new SimpleDateFormat("hh:mm:ss"); 
 final ObjectOutputStream uos; 
 final ObjectInputStream uis; 
 
 final Socket user; 
 //final ServerSocket ss;
   

 // Constructor 
 public ClientThread(Socket user, ObjectOutputStream uos, ObjectInputStream uis)  
 { 
     this.user = user; 
     this.uos = uos;
     this.uis = uis; 
      
 } 

 @Override
 public void run()  
 { 
     String received; 
     String toreturn; 
     
     while (true)  
     { 
         try { 
             
            PairingFactory.getInstance().setUsePBCWhenPossible(true);
     		Pairing pairing = PairingFactory.getPairing("a.properties");
             
             // Ask user what he wants 
             //dos.writeObject("What do you want?[Date | Time]..\n"+ 
                         //"Type Exit to terminate connection."); 
               
             // receive the answer from client 
             received = (String) uis.readObject();
             System.out.println(this.user + " says " + received);
               
             if(received.equals("Exit")) 
             {  
                 System.out.println("Client " + this.user + " sends exit..."); 
                 System.out.println("Closing this connection."); 
                 this.user.close(); 
                 System.out.println("Connection closed"); 
                 break; 
             } 
             
               
               
             // write on output stream based on the 
             // answer from the client 
             switch (received) { 
               
                 case "ED" : 
                     //KGC choose e_i
                	 EC_client.e_i = EC_client.Zr.newRandomElement();
                	 byte[] trans = EC_client.e_i.toBytes();
                	 uos.writeObject(trans);
                	 //compute g^e_ei
                	 Element g_actually_use = EC_client.g_e.duplicate();
                	 Element g_e_ei = g_actually_use.powZn(EC_client.e_i.duplicate());
                	 trans = g_e_ei.toBytes();
                     uos.writeObject(trans); 
                     
                     System.out.println(EC_client.e_i + "\n" + g_e_ei);
                     break; 
                     
                 case "Auth":
                	 //Check download permission
                	 byte[] byte_R = (byte[]) uis.readObject();
                	 byte[] byte_X = (byte[]) uis.readObject();
                	 byte[] byte_Y = (byte[]) uis.readObject();
                	 byte[] byte_C3 = (byte[]) uis.readObject();
                	 
                	 Element X = EC_client.G1.newElementFromBytes(byte_X);
                	 Element Y = EC_client.G1.newElementFromBytes(byte_Y);
                	 Element C3 = EC_client.G1.newElementFromBytes(byte_C3);
                	 System.out.println("C3 " +C3 + "\ne_i" + EC_client.e_i);
                	 
                	 byte[] R_redo = new byte[32];
                	 Element pair_1 = X.duplicate();
                	 Element pair_2 = EC_client.SK.duplicate();
                	 Element pair_X_gdelta = pairing.pairing(pair_1, pair_2);
                	 byte[] X_gdelta = pair_X_gdelta.toBytes();
                	 MessageDigest digest = MessageDigest.getInstance("SHA-256");
         			 byte[] hash_X_gdelta = digest.digest(X_gdelta);
         			 
                	 
                	 for(int i = 0; i < 32; i++)
                	 {
                		 R_redo[i] = (byte) (byte_R[i] ^ hash_X_gdelta[i]);
                	 }

                	 
                	 Element son = EC_client.G1.newElementFromBytes(R_redo);
                	 Element parent = EC_client.ace_gamma.duplicate();
                	 Element R1 = (C3.duplicate().powZn(parent.duplicate().invert())).powZn(EC_client.e_i.duplicate());
                	 System.out.println("Son: \n" + son);

                	 Element Y1 = (Y.duplicate().powZn(parent.duplicate().invert())).powZn(EC_client.e_i.duplicate());
                	 String Revocation_list_path = "C:\\Users\\user\\Desktop\\revocation";
                	 
                	 File file = new File(Revocation_list_path);
                	 boolean legal = true;
                	 if(file.exists())
                	 {
                		 byte[] revocation = Files.readAllBytes(file.toPath());
                		 //Check if ID in revocation list
                		 
                	 }
                	 if(legal)
                	 {
                		 uos.writeObject("legal");
                		 uos.writeObject(R1.toBytes());
                		 uos.writeObject(Y1.toBytes());
                	 }
                	 else
                	 {
                		 uos.writeObject("not");
                	 }
                	 
                	 break;
                     
                 case "Public":
                	 byte[] PK_1 = EC_client.g_e.toBytes();
                	 byte[] PK_2 = EC_client.PK.toBytes();
                	 uos.writeObject(PK_1);
                	 uos.writeObject(PK_2);
                     break;
                     
                 default: 
                     uos.writeObject("Invalid input"); 
                     break; 
             } 
         } catch (Exception e) { 
             e.printStackTrace(); 
             System.out.println("Connection closed"); 
             break; 
         } 
     } 
       
     try
     { 
         // closing resources 
         this.uis.close(); 
         this.uos.close(); 
           
     }catch(IOException e){ 
         e.printStackTrace(); 
     } 
 } 
} 
