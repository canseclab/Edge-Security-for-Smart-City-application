 /* 	Java implementation of  Server side 
	It contains two classes : Server and ClientHandler 
 	Save file as Server.java */
import it.unisa.dia.gas.jpbc.*;
import it.unisa.dia.gas.plaf.jpbc.pairing.PairingFactory;
import java.math.BigInteger;
import java.security.KeyPair;
import java.security.MessageDigest;
import java.io.*; 
import java.text.*; 
import java.util.*; 
import java.net.*; 
  
// Server class 
public class KGC_server
{ 
    public static ServerSocket ss = null; 
    //KGC's MSK
    public static Element alpha, beta, gamma, ace;
    //KGC's public parameters
    public static Element g, g1, h, T, g_actually_use, e, r, d;
    	//Create field
    public static Field Zr, G1;
    public static int KGC_port = 5487;
    public static void main(String[] args) throws Exception  
    { 
        // server is listening on port 5487
        ss = new ServerSocket(KGC_port);  
        System.out.println("Server online");
        
        //Set KGC parameters
        PairingFactory.getInstance().setUsePBCWhenPossible(true);
		Pairing pairing = PairingFactory.getPairing("a.properties");
        
        Zr = pairing.getZr();
        G1 = pairing.getG1();
        
        //KGC choose alpha, beta, gamma, ace
        alpha = Zr.newRandomElement();
        beta = Zr.newRandomElement();
        gamma = Zr.newRandomElement();
        ace = Zr.newRandomElement();
        
        //Use to Edge Controller key gen
        e = Zr.newRandomElement();
        //Use to User key gen
        r = Zr.newRandomElement();
        
        
        //KGC compute parameters
        /* jPBC element will be passed, so we need additional element.*/
        g = G1.newRandomElement();
        //g1
        g_actually_use = g.duplicate();
        g1 = g_actually_use.powZn(beta.duplicate()).duplicate();
        //h
        g_actually_use = g.duplicate();
        h = g_actually_use.powZn(gamma.duplicate()).duplicate();
        //T
        g_actually_use = g.duplicate();
        Element pair2 = h.duplicate(); 
        T = pairing.pairing(g_actually_use, pair2).powZn(alpha.duplicate());
        
        //User Key gen
        g_actually_use = g.duplicate();
        d = g_actually_use.powZn(r.duplicate());
        
        
        // running infinite loop for getting 
        // client request 
        while (true)  
        { 
            Socket s = null; 
              
            try 
            { 
                // socket object to receive incoming client requests 
                s = ss.accept(); 
                  
                System.out.println("A new client is connected : " + s); 
                  
                // obtaining input and out streams 
                ObjectOutputStream dos = new ObjectOutputStream(s.getOutputStream());
                System.out.println("Wow");
                ObjectInputStream dis = new ObjectInputStream(s.getInputStream());
               
                System.out.println("Assigning new thread for this client"); 
  
                // create a new thread object 
                Thread t = new ClientHandler(s, dos, dis); 
  
                // Invoking the start() method 
                t.start(); 
                  
            } 
            catch (Exception e){ 
                s.close(); 
                ss.close();
                System.out.println(e.getMessage()); 
            } 
        } 
    } 
} 
  
// ClientHandler class 
class ClientHandler extends Thread  
{ 
	
    DateFormat fordate = new SimpleDateFormat("yyyy/MM/dd"); 
    DateFormat fortime = new SimpleDateFormat("hh:mm:ss"); 
    final ObjectOutputStream dos; 
    final ObjectInputStream dis; 
    
    final Socket s; 
    //final ServerSocket ss;
      
  
    // Constructor 
    public ClientHandler(Socket s, ObjectOutputStream dos, ObjectInputStream dis)  
    { 
        this.s = s; 
        this.dos = dos;
        this.dis = dis; 
         
    } 
  
    @Override
    public void run()  
    { 
        String received; 
        String toreturn; 
        
        while (true)  
        { 
            try { 
                System.out.println("Thread assigned");
                
                MessageDigest digest = MessageDigest.getInstance("SHA-256");
                PairingFactory.getInstance().setUsePBCWhenPossible(true);
        		Pairing pairing = PairingFactory.getPairing("a.properties");
                
                // Ask user what he wants 
                /*dos.writeObject("What do you want?[Date | Time]..\n"+ 
                            "Type Exit to terminate connection."); */
                  
                // receive the answer from client 
                received = (String) dis.readObject();
                System.out.println(this.s + " says " + received);
                  
                if(received.equals("Exit")) 
                {  
                    System.out.println("Client " + this.s + " sends exit..."); 
                    System.out.println("Closing this connection."); 
                    this.s.close(); 
                    System.out.println("Connection closed"); 
                    break; 
                } 
                  
                // creating Date object 
                Date date = new Date(); 
                  
                // write on output stream based on the 
                // answer from the client 
                switch (received) { 
                  
                //Transfer public parameters
                    case "Initialize" : 
                       
                        //transfer "g"
                        byte[] trans = KGC_server.g.toBytes();
                        dos.writeObject(trans); 
                        
                        //transfer "g1"
                        trans = KGC_server.g1.toBytes();
                        dos.writeObject(trans); 
                        
                        //transfer "h"
                        trans = KGC_server.h.toBytes();
                        dos.writeObject(trans); 
                        
                        //transfer "T"
                        trans = KGC_server.T.toBytes();
                        dos.writeObject(trans); 
                        
                        System.out.println(KGC_server.g); 
                        System.out.println(KGC_server.g1); 
                        System.out.println(KGC_server.h); 
                        System.out.println(KGC_server.T); 
                        
                        break; 
                        
                    case "EC" : 
                    	//Compute g^e
                    	KGC_server.g_actually_use = KGC_server.g.duplicate();
                    	Element g_e = KGC_server.g_actually_use.powZn(KGC_server.e.duplicate());
                    	trans = g_e.toBytes();
                    	dos.writeObject(trans); 
                        //compute ace * gamma
                    	Element ace_gamma = KGC_server.ace.duplicate().mul(KGC_server.gamma.duplicate());
                    	trans = ace_gamma.toBytes();
                    	dos.writeObject(trans);
                    	System.out.println(g_e); 
                    	System.out.println(ace_gamma); 
                    	break; 
                    	
                    case "User" :
                    	byte[] recv = (byte[]) dis.readObject();
                    	
                    	Element ID = KGC_server.Zr.newElementFromBytes(recv);
                    	
                    	Element Ri, SKi = null;
                    	//F(x) ==> H(x)
                    	try 
                		{
                			//i ==> file type access permissions
                   			//1 ==> pdf
                   			//2 ==> picture
                   			//3 ==> word
                			
                			byte[] ri = digest.digest(ID.toBytes());
                			
                			Ri = KGC_server.Zr.newElementFromBytes(ri);
                			System.out.println("ID :" + Ri);
                			SKi = KGC_server.g.duplicate().powZn(Ri.duplicate());
                		}
                		catch (Exception e)
                		{
                			System.out.println(e);
                		}
                    	
                    	dos.writeObject(SKi.toBytes());
                    	
                    	break;
                    	
                    case "Extract" : 
                    	recv = (byte[]) dis.readObject();
                    	Element ID_a = KGC_server.Zr.newElementFromBytes(recv);
                    	
                    	recv = (byte[]) dis.readObject();
                    	Element ID_b = KGC_server.Zr.newElementFromBytes(recv);
                    	
                    	recv = (byte[]) dis.readObject();
                    	Element sb = KGC_server.Zr.newElementFromBytes(recv);
                    	
                    	recv = (byte[]) dis.readObject();
                    	Element xi = KGC_server.G1.newElementFromBytes(recv);
                    	
                    	//Compare Xi
                    	byte[] H_ID = digest.digest(ID_a.toBytes());
                    	Element H_IDa = KGC_server.Zr.newElementFromBytes(H_ID);
                    	Element second = KGC_server.g.duplicate().powZn(H_IDa.duplicate().add(sb.duplicate()));
                    	
                    	byte[] H_second = digest.digest(second.toBytes());

                    	ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        	        	outputStream.write(ID_b.toBytes());
        	        	outputStream.write(H_second);
        	        	
        	        	byte Xi[] = outputStream.toByteArray();
        	        	
        	        	byte[] H_Xi = digest.digest(Xi);
        	        	Element H_EXi = KGC_server.G1.newElementFromBytes(H_Xi);
                    	
                    	System.out.println(H_EXi);
                    	System.out.println(xi);
                    	if(Arrays.equals(xi.toBytes(), H_EXi.toBytes()))
                    	{
                    		dos.writeObject("Same");
                    		
                    		System.out.println("Xi match, generating AKS");
                    		
                    		byte[] H_IDb = digest.digest(ID_b.toBytes());
                    		
                    		Element rb = KGC_server.Zr.newElementFromBytes(H_IDb);
                    		System.out.println("rb\n" + rb);
                    		Element son = KGC_server.alpha.duplicate().add(rb);
                    		
                    		ByteArrayOutputStream stream_li = new ByteArrayOutputStream();
                    		stream_li.write(sb.toBytes());
                    		stream_li.write(ID_a.toBytes());
                    		
                    		byte[] b_li = stream_li.toByteArray();
                    		
                    		byte[] H_li = digest.digest(b_li);
                    		
                    		System.out.println("Index: \n" + Base64.getEncoder().encodeToString(H_li));
                    		
                    		Element li = KGC_server.Zr.newElementFromBytes(H_li);
                    		
                    		Element parents = KGC_server.beta.duplicate().add(li.duplicate()).add(KGC_server.ace.duplicate().invert());
                    		
                    		Element Ki = KGC_server.h.duplicate().powZn(son.duplicate().div(parents.duplicate()));
                    		trans = Ki.toBytes();
                    		
                    		dos.writeObject(Ki.toBytes());
                    	}
                    	else
                    	{
                    		dos.writeObject("not same");
                    	}
                		
                			
                        break;
                        
                        
                    default: 
                        dos.writeObject("Invalid input"); 
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
            this.dis.close(); 
            this.dos.close(); 
            
        }catch(IOException e){ 
            e.printStackTrace(); 
        } 
    } 
    
    public static int convertByteToInt(byte[] b)
	{           
	    int value= 0;
	    for(int i=0; i<b.length; i++)
	       value = (value << 8) | b[i];     
	    return value;       
	}
} 