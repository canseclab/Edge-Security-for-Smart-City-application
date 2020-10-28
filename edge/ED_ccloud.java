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
import java.text.DateFormat;
import java.text.SimpleDateFormat;

// Client class 
public class ED_ccloud {
	public static ServerSocket ss = null;
	public static Socket s = null;
	public static Element ED_PK, g, ED_SK, T;
	public static Field Zr, G1, GT;
	public static int KGC_port = 5487;
	public static int EC_port = 5278;
	public static int ED_port = 5457;
	public static void main(String[] args) throws IOException {

		try {
			Scanner in = new Scanner(System.in);
			PairingFactory.getInstance().setUsePBCWhenPossible(true);
			Pairing pairing = PairingFactory.getPairing("a.properties");
			Zr = pairing.getZr();
			G1 = pairing.getG1();
			GT = pairing.getGT();
			Element ex = Zr.newRandomElement();
			// System.out.println(Zr.getOrder() + "\n" + G1.getOrder());
			// getting localhost ip
			InetAddress ip = InetAddress.getByName("localhost");
			s = new Socket(ip, EC_port);
			Socket KGC_s = new Socket(ip, KGC_port);
			ss = new ServerSocket(ED_port);

			// obtaining input and out streams
			ObjectOutputStream dos = new ObjectOutputStream(s.getOutputStream());
			ObjectInputStream dis = new ObjectInputStream(s.getInputStream());

			ObjectOutputStream eos = new ObjectOutputStream(KGC_s.getOutputStream());
			ObjectInputStream eis = new ObjectInputStream(KGC_s.getInputStream());

			// Get public parameters from KGC
			String tosend = "ED";
			dos.writeObject(tosend);
			// e_i
			byte[] recb = (byte[]) dis.readObject();
			Element e_i = Zr.newElementFromBytes(recb);
			ED_SK = e_i;
			System.out.println(e_i);
			// g^e_ei
			recb = (byte[]) dis.readObject();
			Element g_e_ei = G1.newElementFromBytes(recb);
			System.out.println(g_e_ei);
			ED_PK = g_e_ei.duplicate();

			// Get public parameters from KGC
			eos.writeObject("Initialize");
			// g
			recb = (byte[]) eis.readObject();
			g = G1.newElementFromBytes(recb);
			System.out.println("g :" + g);

			// g1
			recb = (byte[]) eis.readObject();
			Element g1 = G1.newElementFromBytes(recb);

			// h
			recb = (byte[]) eis.readObject();
			Element h = G1.newElementFromBytes(recb);

			// T
			recb = (byte[]) eis.readObject();
			T = GT.newElementFromBytes(recb);

			eos.writeObject("Exit");

			dos.writeObject("Exit");

			// closing resources
			in.close();
			dis.close();
			dos.close();

			while (true) {
				Socket user = null;

				try {
					// socket object to receive incoming client requests
					user = ss.accept();

					System.out.println("User connected " + user);

					// obtaining input and out streams
					ObjectOutputStream uos = new ObjectOutputStream(user.getOutputStream());
					ObjectInputStream uis = new ObjectInputStream(user.getInputStream());

					System.out.println("Assigning new thread for this client");

					// create a new thread object
					Thread t = new ED_ccloudThread(user, uos, uis);

					// Invoking the start() method
					t.start();

				} catch (Exception e) {
					s.close();
					System.out.println(e.getMessage());
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}

//ClientHandler class 
class ED_ccloudThread extends Thread {

	DateFormat fordate = new SimpleDateFormat("yyyy/MM/dd");
	DateFormat fortime = new SimpleDateFormat("hh:mm:ss");
	final ObjectOutputStream uos;
	final ObjectInputStream uis;
	public static String NDN_IP_number = "";
	public static int NDN_port = 2323;
	public static int EC_port = 5278;
	public static String edgeFile_Path = "";
	public static int keepFileNumber = 3;
	final Socket user;
//final ServerSocket ss;

// Constructor 
	public ED_ccloudThread(Socket user, ObjectOutputStream uos, ObjectInputStream uis) {
		this.user = user;
		this.uos = uos;
		this.uis = uis;

	}

	@Override
	public void run() {
		String received;
		ArrayList<DriveFile> allEdgeFile = new ArrayList<DriveFile>();
		String edgeFile[][] = new String[keepFileNumber][2];
		int countFile = 0;
		while (true) {
			try {
				// System.out.println("Thread assigned");

				PairingFactory.getInstance().setUsePBCWhenPossible(true);
				Pairing pairing = PairingFactory.getPairing("a.properties");

				// Ask user what he wants
				// dos.writeObject("What do you want?[Date | Time]..\n"+
				// "Type Exit to terminate connection.");

				// receive the answer from client
				received = (String) uis.readObject();
				System.out.println(this.user + " says " + received);

				if (received.equals("Exit")) {
					System.out.println("Client " + this.user + " sends exit...");
					System.out.println("Closing this connection.");
					this.user.close();
					System.out.println("Connection closed");
					break;
				}

				// write on output stream based on the
				// answer from the client
				switch (received) {

				case "Public":
					byte[] PK_1 = ED_ccloud.ED_PK.toBytes();
					uos.writeObject(PK_1);
					System.out.println(ED_ccloud.ED_PK);
					break;

				case "Req":

					int try_times = (int) uis.readObject();

					System.out.println(received);
					byte[] CT_encrypted = (byte[]) uis.readObject();
					Element K;
					byte[] recv = (byte[]) uis.readObject();
					Element X = ED_ccloud.G1.newElementFromBytes(recv);
					recv = (byte[]) uis.readObject();
					Element Y = ED_ccloud.G1.newElementFromBytes(recv);

					recv = (byte[]) uis.readObject();
					Element temp_key2 = ED_ccloud.G1.newElementFromBytes(recv);
					recv = (byte[]) uis.readObject();
					Element temp_key3 = ED_ccloud.G1.newElementFromBytes(recv);
					recv = (byte[]) uis.readObject();
					Element temp_key4 = ED_ccloud.G1.newElementFromBytes(recv);

					Element g_actually_use = ED_ccloud.g.duplicate();
					Element g_ei = g_actually_use.powZn(ED_ccloud.ED_SK);
					Element pair1, pair2;
					pair1 = X.duplicate();
					pair2 = g_ei.duplicate();
					K = pairing.pairing(pair1, pair2);
					System.out.println(K);

					AES_Object use = new AES_Object();
					byte[] byte_K = K.toBytes();
					byte_K = Arrays.copyOfRange(byte_K, 0, 16);
					byte[] CT = use.File_decrypt(CT_encrypted, byte_K);

					System.out.println("CT length: \n" + CT.length);

					byte[] byte_R = Arrays.copyOfRange(CT, 0, 32);
					byte[] byte_ftype = Arrays.copyOfRange(CT, 32, 64);
					byte[] byte_faddr = Arrays.copyOfRange(CT, 64, 96);

					System.out.println(Base64.getEncoder().encodeToString(byte_ftype));

					InetAddress ip = InetAddress.getByName("localhost");
					Socket EC_auth = new Socket(ip, EC_port);

					ObjectOutputStream eos = new ObjectOutputStream(EC_auth.getOutputStream());
					ObjectInputStream eis = new ObjectInputStream(EC_auth.getInputStream());

					// Send R,X,Y,C3 to EC
					eos.writeObject("Auth");
					eos.writeObject(byte_R);
					eos.writeObject(X.toBytes());
					eos.writeObject(Y.toBytes());
					String H_re = Base64.getEncoder().encodeToString(byte_faddr).replace('/', 'a');
					H_re = H_re.replace('+', 'a');
					H_re = H_re.replace('=', 'a');

					String C_name = H_re + "en";
					String C_para = H_re + "pa";
					String file_path_para = edgeFile_Path + H_re;
					Path pa_path = Paths.get(file_path_para);
					//Path en_path = Paths.get(file_path);

					byte[] byte_file = null, byte_para = null;
					DriveFile downloadFile = new DriveFile(H_re);
					boolean fileExist  = false;
					long start_time = 0;
					long download_time = 0;
					 if (!new File(file_path_para).exists()) { //file not exist in edgeFile
						if (allEdgeFile.contains(downloadFile)) { // but has been downloaded
							int file_positrion = allEdgeFile.indexOf(downloadFile);
							allEdgeFile.get(file_positrion).setDownloadTimes();
							downloadFile.setDownloadTimes(allEdgeFile.get(file_positrion).getDownloadTimes());
						} else { // has not been downloaded
							allEdgeFile.add(downloadFile);
						}
						start_time = System.currentTimeMillis();
						if (try_times == 0) // download from NDN
						{
							InetAddress NDN_ip = InetAddress.getByName(NDN_IP_number);
							Socket NDN_socket = new Socket(NDN_ip, NDN_port);
							PrintWriter nos = new PrintWriter(NDN_socket.getOutputStream(), true);
							InputStream nis = NDN_socket.getInputStream();

							ByteArrayOutputStream file_en = new ByteArrayOutputStream();
							ByteArrayOutputStream file_pa = new ByteArrayOutputStream();
							byte[] data = new byte[200];
							//System.out.println(H_re);
							nos.printf(H_re);
							int count = -1;
							count = nis.read(data);
							String message = new String(data);
							System.out.println("The message sent form the NDN is: " + message);

							String[] split_message = message.split("/");
							String drivePath = "";
							for (int i = 1; i < split_message.length; i++) {
								drivePath = drivePath + split_message[i];
							}
							if (split_message[0].equals("Google")) {
								//System.out.println(split_message[split_message.length - 1]);
								download_time = System.currentTimeMillis();
								DriveQuickstart.file_download(H_re, file_path_para,drivePath);
							} else if (split_message[0].equals("Dropbox")) {
								if(drivePath.trim().equals("root")) {
									drivePath = "";
								}else {
									drivePath = drivePath + "/";
								}
								dropbox.downloadFile(file_path_para, H_re, drivePath);
							}else if(split_message[0].trim().equals("NoSuchFile")) {
								uos.writeObject("NoSuchFile");
								continue;
							}
							NDN_socket.close();
						}
					} else {
						int file_position = allEdgeFile.indexOf(downloadFile);
						allEdgeFile.get(file_position).setDownloadTimes();
						for(int k = 0 ; k < edgeFile.length ; k++) {
							if(edgeFile[k][0].equals(allEdgeFile.get(file_position).getFileName())) {
								edgeFile[k][1] = String.valueOf((allEdgeFile.get(file_position).getDownloadTimes()));
								break;
							}
						}
						//The array need to add the downloadtimes. 
						fileExist = true;
					}
					long end_time = System.currentTimeMillis();
					long total_time = end_time - start_time;
					long endTime = end_time - download_time;
					System.out.println("Download Time : " + total_time);
					System.out.println("endTime Time : " + endTime);					/*
					 * if(try_times == 0) { DriveQuickstart.file_download(C_para, file_path_para);
					 * DriveQuickstart.file_download(C_name, file_path); }
					 */
					// File file = new File(file_path_para);
					// byte[] parameter = Files.readAllBytes(file.toPath());
					File file = new File(file_path_para);
					byte[] parameter = Arrays.copyOfRange(Files.readAllBytes(file.toPath()), 0, 384);
					byte[] byte_C1 = Arrays.copyOfRange(parameter, 0, 128);
					byte[] byte_C2 = Arrays.copyOfRange(parameter, 128, 256);
					byte[] byte_C3 = Arrays.copyOfRange(parameter, 256, 384);
					eos.writeObject(byte_C3);
					String result = (String) eis.readObject();
					byte[] byte_R1 = null;
					byte[] byte_Y1 = null;
					if (result.equals("legal")) {
						byte_R1 = (byte[]) eis.readObject();
						byte_Y1 = (byte[]) eis.readObject();
						eos.writeObject("Exit");
					}
					Element R1 = ED_ccloud.G1.newElementFromBytes(byte_R1);
					Element Y1 = ED_ccloud.G1.newElementFromBytes(byte_Y1);
					// System.out.println("\n\n\nR1 and Y1\n" + R1 + "\n" + Y1);

					Element SK_inverse = ED_ccloud.ED_SK.duplicate().invert();
					Element Y1_pow = Y1.duplicate().powZn(SK_inverse);
					pair1 = temp_key2.duplicate().mul(Y1_pow.duplicate());
					pair2 = temp_key3.duplicate();

					Element key_der1 = pairing.pairing(pair1, pair2);
					pair1 = Y.duplicate();
					pair2 = temp_key4.duplicate();
					Element temp_key_der = pairing.pairing(pair1, pair2);
					Element key_der2 = temp_key_der.duplicate().mul(ED_ccloud.T.duplicate());

					Element C1 = ED_ccloud.GT.newElementFromBytes(byte_C1);
					Element C2 = ED_ccloud.G1.newElementFromBytes(byte_C2);
					Element C3 = ED_ccloud.G1.newElementFromBytes(byte_C3);

					if (key_der1.equals(key_der2)) {
						uos.writeObject("Match");
						Element C2_R = C2.duplicate().mul(R1.duplicate().powZn(ED_ccloud.ED_SK.duplicate().invert()));
						uos.writeObject(byte_C1);
						uos.writeObject(C2_R.toBytes());
						uos.writeObject(byte_C3);
						byte[] pic = Arrays.copyOfRange(Files.readAllBytes(file.toPath()), 384, Files.readAllBytes(file.toPath()).length);
						uos.writeObject(pic);
					} else {
						uos.writeObject("Fail");
					}
					String killPath = "";
					if (countFile < 3 && !fileExist) {
						edgeFile[countFile][0] = downloadFile.getFileName();
						edgeFile[countFile][1] = String.valueOf(downloadFile.getDownloadTimes());
						countFile ++;
					} else if(countFile >= 3 && !fileExist) {
 						parameter = null;
 						byte_C1 = null;
 						byte_C2 = null;
 						byte_C3 = null;
 						uos.flush();
						for (int j = 0; j < edgeFile.length; j++) {
							if (downloadFile.getDownloadTimes() > Integer.valueOf(edgeFile[j][1])) {
								killPath = edgeFile_Path+edgeFile[j][0];
								edgeFile[j][0] = downloadFile.getFileName();
								edgeFile[j][1] = String.valueOf(downloadFile.getDownloadTimes());						
								break;
							} else if (j == 2) {
								killPath = file_path_para;
								break;
							}
						}
							System.out.println("killing somebody :" +killPath);
							File killFile = new File(killPath);
							killFile.delete();
					}
					for(String s[] : edgeFile) {
						for(String ss: s) {
							System.out.print(ss + " ");
						}
						System.out.println();
					}
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
		try {
			// closing resources
			this.uis.close();
			this.uos.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}