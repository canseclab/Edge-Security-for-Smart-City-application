import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.WindowAdapter;
import java.awt.FileDialog;
import java.awt.Font;
import javax.swing.plaf.basic.BasicTextFieldUI;
import javax.swing.text.JTextComponent;
import java.util.*;
import java.util.Date;
import javax.swing.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.DataFlavor;
import java.awt.Toolkit;

import java.security.GeneralSecurityException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.net.*; 

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Field;
import it.unisa.dia.gas.jpbc.Pairing;
import it.unisa.dia.gas.plaf.jpbc.pairing.PairingFactory;
import java.math.BigInteger;
import java.security.KeyPair;
import java.security.MessageDigest;


public class GUI
{

    public static String User_name = "";
    public static String Upload_file_name = "", Upload_file_path = "", Upload_file_type = "";
    public static String Download_file_name = "";
    public static String str_ID = "";
    public static byte[] b_ID = null, trans = null;
    public static Vector<byte[]> AKS = new Vector<byte[]>();
    public static Vector<String> AKS_detail = new Vector<String>();
    public static int key_num = 0;
    public static Element ID = null, SKi = null, share_id;
    public static int KGC_port = 5487;
	public static int EC_port = 5278;
	public static int ED_port = 5400;
	public static String edgeSever_IP = "127.0.0.1";
    public static void main(String[] argv) throws InvalidKeyException,NoSuchAlgorithmException, NoSuchPaddingException, 
                                                UnsupportedEncodingException, InvalidAlgorithmParameterException, 
                                                IllegalBlockSizeException, BadPaddingException, IOException, ClassNotFoundException,
                                                GeneralSecurityException
    {
        /*
            Main program
            Generate cryptography element
        */
        AES_Object use = new AES_Object();
	    MessageDigest digest = MessageDigest.getInstance("SHA-256");		
        PairingFactory.getInstance().setUsePBCWhenPossible(true);
        Pairing pairing = PairingFactory.getPairing("a.properties");       
        Field Zr = pairing.getZr();
        Field G1 = pairing.getG1();
        Field GT = pairing.getGT();
        // getting localhost ip 
        InetAddress ip = InetAddress.getByName(edgeSever_IP); 
       
        Socket s = new Socket(ip, KGC_port); 
      
        // obtaining input and out streams 
        ObjectOutputStream dos = new ObjectOutputStream(s.getOutputStream()); 
        ObjectInputStream dis = new ObjectInputStream(s.getInputStream()); 
            
        Socket s_EC = new Socket(ip, EC_port); 
   		ObjectOutputStream uos = new ObjectOutputStream(s_EC.getOutputStream()); 
   		ObjectInputStream uis = new ObjectInputStream(s_EC.getInputStream()); 
   			
   		Socket s_ED = new Socket(ip, ED_port);
        ObjectOutputStream eos = new ObjectOutputStream(s_ED.getOutputStream()); 
   		ObjectInputStream eis = new ObjectInputStream(s_ED.getInputStream()); 
      
        //Get public parameters from KGC  
        dos.writeObject("Initialize");
        //g
        byte[] recb = (byte[]) dis.readObject();
        Element g = G1.newElementFromBytes(recb);
            
        //g1
        recb = (byte[]) dis.readObject();
        Element g1 = G1.newElementFromBytes(recb);
            
        //h
        recb = (byte[]) dis.readObject();
        Element h = G1.newElementFromBytes(recb);
            
        //T
        recb = (byte[]) dis.readObject();
        Element T = GT.newElementFromBytes(recb);
            
        str_ID = User_name;
        //String str_share_id = "";
        share_id = null;

        /*
            Input user name
        */
        JFrame initialization_ui = new JFrame("KAC-CE");
        initialization_ui.setSize(600,200);
        initialization_ui.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        initialization_ui.getContentPane().setLayout(new GridBagLayout());
        initialization_ui.setLocationRelativeTo(null);

        initialization_ui.addWindowListener(new WindowAdapter(){
            @Override
            public void windowClosing(WindowEvent e){
                try
                {
                    dos.writeObject("Exit");
                    eos.writeObject("Exit");
                    uos.writeObject("Exit");
                    dos.close();
                    dis.close();
                    
                    uos.close();
                    uis.close();
                    
                    eos.close();
                    eis.close();
                }
                catch(Exception s)
                {
                    System.out.println(s);
                }
            }
        });

        JLabel ID_notice = new JLabel("User ID  ");
        GridBagConstraints G_ID_notice = new GridBagConstraints();
        G_ID_notice.gridx = 0;
        G_ID_notice.gridy = 0;
        G_ID_notice.gridwidth = 1;
        G_ID_notice.gridheight = 1;
        G_ID_notice.weightx = 0;
        G_ID_notice.weighty = 0;
        G_ID_notice.fill = GridBagConstraints.NONE;
        G_ID_notice.anchor = GridBagConstraints.CENTER;

        final JTextField userID = new HintTextField("User ID");
        GridBagConstraints G_userID = new GridBagConstraints();
        G_userID.gridx = 1;
        G_userID.gridy = 0;
        G_userID.gridwidth = 10;
        G_userID.gridheight = 1;
        G_userID.weightx = 0;
        G_userID.weighty = 0;
        G_userID.ipadx = 200;
        //G_userID.fill = GridBagConstraints.BOTH;
        G_userID.anchor = GridBagConstraints.WEST;

        JButton confirm = new JButton("Confirm");
        GridBagConstraints G_confirm = new GridBagConstraints();
        G_confirm.gridx = 3;
        G_confirm.gridy = 1;
        G_confirm.gridwidth = 1;
        G_confirm.gridheight = 1;
        G_confirm.weightx = 0;
        G_confirm.weighty = 0;
        G_confirm.ipadx = 35;
        //G_confirm.fill = GridBagConstraints.NONE;
        G_confirm.anchor = GridBagConstraints.CENTER;

        JLabel layout = new JLabel(" ");
        GridBagConstraints G_layout = new GridBagConstraints();
        G_layout.gridx = 5;
        G_layout.gridy = 1;
        G_layout.gridwidth = 1;
        G_layout.gridheight = 1;
        G_layout.weightx = 0;
        G_layout.weighty = 0;
        G_layout.ipadx = 25;
        //G_reset.fill = GridBagConstraints.NONE;
        G_layout.anchor = GridBagConstraints.CENTER;

        JButton reset = new JButton("Reset");
        GridBagConstraints G_reset = new GridBagConstraints();
        G_reset.gridx = 6;
        G_reset.gridy = 1;
        G_reset.gridwidth = 1;
        G_reset.gridheight = 1;
        G_reset.weightx = 0;
        G_reset.weighty = 0;
        G_reset.ipadx = 35;
        //G_reset.fill = GridBagConstraints.NONE;
        G_reset.anchor = GridBagConstraints.CENTER;

        JLabel hint = new JLabel(" ");
        GridBagConstraints G_hint = new GridBagConstraints();
        G_hint.gridx = 0;
        G_hint.gridy = 2;
        G_hint.gridwidth = 1;
        G_hint.gridheight = 1;
        G_hint.weightx = 0;
        G_hint.weighty = 0;
        G_hint.ipadx = 25;
        //G_reset.fill = GridBagConstraints.NONE;
        G_hint.anchor = GridBagConstraints.CENTER;

        initialization_ui.add(ID_notice, G_ID_notice);
        initialization_ui.add(userID, G_userID);
        initialization_ui.add(confirm, G_confirm);
        initialization_ui.add(layout, G_layout);
        initialization_ui.add(reset, G_reset);
        initialization_ui.add(hint, G_hint);

        /*
            Setting
        */
        JFrame setting_ui = new JFrame("KAC-CE");
        setting_ui.setSize(600,300);
        setting_ui.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setting_ui.getContentPane().setLayout(new GridBagLayout());
        setting_ui.setLocationRelativeTo(null);

        setting_ui.addWindowListener(new WindowAdapter(){
            @Override
            public void windowClosing(WindowEvent e){
                try
                {
                    dos.writeObject("Exit");
                    eos.writeObject("Exit");
                    uos.writeObject("Exit");
                    dos.close();
                    dis.close();
                    
                    uos.close();
                    uis.close();
                    
                    eos.close();
                    eis.close();
                }
                catch(Exception s)
                {
                    System.out.println(s);
                }
            }
        });

        DefaultListModel model = new DefaultListModel<String>();
        model.addElement("No token generated");

        JList JAKS_detail = new JList(model);
        JScrollPane sc = new JScrollPane(JAKS_detail);
        //JAKS_detail.setSelectionMode(SINGLE_SELECTION);
        GridBagConstraints GAKS_detail = new GridBagConstraints();
        GAKS_detail.gridx = 0;
        GAKS_detail.gridy = 0;
        GAKS_detail.gridwidth = 2;
        GAKS_detail.gridheight = 1;
        GAKS_detail.weightx = 0;
        GAKS_detail.weighty = 0;
        //GAKS_detail.ipadx = 25;
        GAKS_detail.fill = GridBagConstraints.NONE;
        GAKS_detail.anchor = GridBagConstraints.CENTER;

        JLabel JAKS_space = new JLabel(" ");
        GridBagConstraints GAKS_space = new GridBagConstraints();
        GAKS_space.gridx = 1;
        GAKS_space.gridy = 0;
        GAKS_space.gridwidth = 2;
        GAKS_space.gridheight = 1;
        GAKS_space.weightx = 0;
        GAKS_space.weighty = 0;
        //GAKS_space.ipadx = 25;
        GAKS_space.fill = GridBagConstraints.NONE;
        GAKS_space.anchor = GridBagConstraints.CENTER;

        JButton JAKS_delete = new JButton("Delete");
        GridBagConstraints GAKS_delete = new GridBagConstraints();
        GAKS_delete.gridx = 2;
        GAKS_delete.gridy = 0;
        GAKS_delete.gridwidth = 2;
        GAKS_delete.gridheight = 1;
        GAKS_delete.weightx = 0;
        GAKS_delete.weighty = 0;
        //GAKS_delete.ipadx = 25;
        GAKS_delete.fill = GridBagConstraints.NONE;
        GAKS_delete.anchor = GridBagConstraints.CENTER;

        JButton JAKS_location = new JButton("Set location");
        GridBagConstraints GAKS_location = new GridBagConstraints();
        GAKS_location.gridx = 4;
        GAKS_location.gridy = 1;
        GAKS_location.gridwidth = 1;
        GAKS_location.gridheight = 1;
        GAKS_location.weightx = 0;
        GAKS_location.weighty = 0;
        //GAKS_location.ipadx = 25;
        GAKS_location.fill = GridBagConstraints.NONE;
        GAKS_location.anchor = GridBagConstraints.CENTER;

        JLabel JAKS_space3 = new JLabel(" ");
        GridBagConstraints GAKS_space3 = new GridBagConstraints();
        GAKS_space3.gridx = 3;
        GAKS_space3.gridy = 1;
        GAKS_space3.gridwidth = 1;
        GAKS_space3.gridheight = 1;
        GAKS_space3.weightx = 0;
        GAKS_space3.weighty = 0;
        //GAKS_space3.ipadx = 25;
        GAKS_space3.fill = GridBagConstraints.NONE;
        GAKS_space3.anchor = GridBagConstraints.CENTER;

        JLabel JAKS_showlocation = new JLabel(System.getProperty("user.home") + "\\Desktop");
        GridBagConstraints GAKS_showlocation = new GridBagConstraints();
        GAKS_showlocation.gridx = 0;
        GAKS_showlocation.gridy = 1;
        GAKS_showlocation.gridwidth = 2;
        GAKS_showlocation.gridheight = 1;
        GAKS_showlocation.weightx = 0;
        GAKS_showlocation.weighty = 0;
        //GAKS_showlocation.ipadx = 25;
        GAKS_showlocation.fill = GridBagConstraints.NONE;
        GAKS_showlocation.anchor = GridBagConstraints.CENTER;

        JLabel JAKS_space2 = new JLabel(" ");
        GridBagConstraints GAKS_space2 = new GridBagConstraints();
        GAKS_space2.gridx = 1;
        GAKS_space2.gridy = 2;
        GAKS_space2.gridwidth = 2;
        GAKS_space2.gridheight = 1;
        GAKS_space2.weightx = 0;
        GAKS_space2.weighty = 0;
        //GAKS_space2.ipadx = 25;
        GAKS_space2.fill = GridBagConstraints.NONE;
        GAKS_space2.anchor = GridBagConstraints.CENTER;

        JButton JAKS_goback = new JButton("Back");
        GridBagConstraints GAKS_goback = new GridBagConstraints();
        GAKS_goback.gridx = 4;
        GAKS_goback.gridy = 3;
        GAKS_goback.gridwidth = 1;
        GAKS_goback.gridheight = 1;
        GAKS_goback.weightx = 0;
        GAKS_goback.weighty = 0;
        //GAKS_goback.ipadx = 25;
        GAKS_goback.fill = GridBagConstraints.NONE;
        GAKS_goback.anchor = GridBagConstraints.CENTER;

        setting_ui.add(JAKS_location, GAKS_location);
        setting_ui.add(JAKS_delete, GAKS_delete);
        setting_ui.add(sc, GAKS_detail);
        setting_ui.add(JAKS_goback, GAKS_goback);
        setting_ui.add(JAKS_space, GAKS_space);
        setting_ui.add(JAKS_space2, GAKS_space2);
        setting_ui.add(JAKS_space3, GAKS_space3);
        setting_ui.add(JAKS_showlocation, GAKS_showlocation);

        /*
            Choose command
        */
        JFrame main_ui = new JFrame("KAC-CE");
        main_ui.setSize(600,200);
        main_ui.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        main_ui.getContentPane().setLayout(new GridBagLayout());
        main_ui.setLocationRelativeTo(null);

        main_ui.addWindowListener(new WindowAdapter(){
            @Override
            public void windowClosing(WindowEvent e){
                try
                {
                    dos.writeObject("Exit");
                    eos.writeObject("Exit");
                    uos.writeObject("Exit");
                    dos.close();
                    dis.close();
                    
                    uos.close();
                    uis.close();
                    
                    eos.close();
                    eis.close();
                }
                catch(Exception s)
                {
                    System.out.println(s);
                }
            }
        });

        JLabel hello = new JLabel("Hello");
        GridBagConstraints G_hello = new GridBagConstraints();
        G_hello.gridx = 1;
        G_hello.gridy = 0;
        G_hello.gridwidth = 2;
        G_hello.gridheight = 1;
        G_hello.weightx = 0;
        G_hello.weighty = 0;
        //G_upload.ipadx = 25;
        G_hello.fill = GridBagConstraints.NONE;
        G_hello.anchor = GridBagConstraints.CENTER;

        JLabel space = new JLabel(" ");
        GridBagConstraints G_space = new GridBagConstraints();
        G_space.gridx = 1;
        G_space.gridy = 1;
        G_space.gridwidth = 1;
        G_space.gridheight = 1;
        G_space.weightx = 0;
        G_space.weighty = 0;
        //G_upload.ipadx = 25;
        G_space.fill = GridBagConstraints.NONE;
        G_space.anchor = GridBagConstraints.CENTER;

        JLabel space2 = new JLabel(" ");
        GridBagConstraints G_space2 = new GridBagConstraints();
        G_space2.gridx = 1;
        G_space2.gridy = 3;
        G_space2.gridwidth = 1;
        G_space2.gridheight = 1;
        G_space2.weightx = 0;
        G_space2.weighty = 0;
        //G_space2.ipadx = 25;
        G_space2.fill = GridBagConstraints.NONE;
        G_space2.anchor = GridBagConstraints.CENTER;

        JButton upload = new JButton("Upload");
        GridBagConstraints G_upload = new GridBagConstraints();
        G_upload.gridx = 0;
        G_upload.gridy = 2;
        G_upload.gridwidth = 1;
        G_upload.gridheight = 1;
        G_upload.weightx = 0.2;
        G_upload.weighty = 0;
        //G_upload.ipadx = 25;
        G_upload.fill = GridBagConstraints.NONE;
        G_upload.anchor = GridBagConstraints.CENTER;

        JButton share = new JButton("Share");
        GridBagConstraints G_share = new GridBagConstraints();
        G_share.gridx = 1;
        G_share.gridy = 2;
        G_share.gridwidth = 1;
        G_share.gridheight = 1;
        G_share.weightx = 0.2;
        G_share.weighty = 0;
        //G_share.ipadx = 25;
        G_share.fill = GridBagConstraints.NONE;
        G_share.anchor = GridBagConstraints.CENTER;

        JButton Generate = new JButton("TokenGen");
        GridBagConstraints G_generate = new GridBagConstraints();
        G_generate.gridx = 2;
        G_generate.gridy = 2;
        G_generate.gridwidth = 1;
        G_generate.gridheight = 1;
        G_generate.weightx = 0.2;
        G_generate.weighty = 0;
        //G_generate.ipadx = 25;
        G_generate.fill = GridBagConstraints.NONE;
        G_generate.anchor = GridBagConstraints.CENTER;

        JButton download = new JButton("Download");
        GridBagConstraints G_download = new GridBagConstraints();
        G_download.gridx = 3;
        G_download.gridy = 2;
        G_download.gridwidth = 1;
        G_download.gridheight = 1;
        G_download.weightx = 0.2;
        G_download.weighty = 0;
        //G_download.ipadx = 25;
        G_download.fill = GridBagConstraints.NONE;
        G_download.anchor = GridBagConstraints.CENTER;

        JButton setting = new JButton("Setting");
        GridBagConstraints G_setting = new GridBagConstraints();
        G_setting.gridx = 3;
        G_setting.gridy = 4;
        G_setting.gridwidth = 1;
        G_setting.gridheight = 1;
        G_setting.weightx = 0.2;
        G_setting.weighty = 0;
        //G_setting.ipadx = 25;
        G_setting.fill = GridBagConstraints.NONE;
        G_setting.anchor = GridBagConstraints.CENTER;

        main_ui.add(hello, G_hello);
        main_ui.add(upload, G_upload);
        main_ui.add(share, G_share);
        main_ui.add(Generate, G_generate);
        main_ui.add(download, G_download);
        main_ui.add(space, G_space);
        main_ui.add(space2, G_space2);
        main_ui.add(setting, G_setting);

        /*
            Upload file
        */
        JFrame upload_ui = new JFrame("KAC-CE");
        upload_ui.setSize(600,200);
        upload_ui.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        upload_ui.getContentPane().setLayout(new GridBagLayout());
        upload_ui.setLocationRelativeTo(null);

        upload_ui.addWindowListener(new WindowAdapter(){
            @Override
            public void windowClosing(WindowEvent e){
                try
                {
                    dos.writeObject("Exit");
                    eos.writeObject("Exit");
                    uos.writeObject("Exit");
                    dos.close();
                    dis.close();
                    
                    uos.close();
                    uis.close();
                    
                    eos.close();
                    eis.close();
                }
                catch(Exception s)
                {
                    System.out.println(s);
                }
            }
        });

        String[] file_type = {"1.Document", "2.Media", "3.Picture"};
        JComboBox Jfile_type = new JComboBox<String>(file_type);
        GridBagConstraints G_file_type = new GridBagConstraints();
        G_file_type.gridx = 0;
        G_file_type.gridy = 0;
        G_file_type.gridwidth = 1;
        G_file_type.gridheight = 1;
        G_file_type.weightx = 0.2;
        G_file_type.weighty = 0;
        //G_file_type.ipadx = 25;
        G_file_type.fill = GridBagConstraints.NONE;
        G_file_type.anchor = GridBagConstraints.CENTER;
        
        String[] upload_type = {"1.GOOGLE", "2.DROPBOX"};
        JComboBox Jupload_type = new JComboBox<String>(upload_type);
        GridBagConstraints G_upload_type = new GridBagConstraints();
        G_upload_type.gridx = 0;
        G_upload_type.gridy = 4;
        G_upload_type.gridwidth = 1;
        G_upload_type.gridheight = 1;
        G_upload_type.weightx = 0.2;
        G_upload_type.weighty = 0;
        //G_file_type.ipadx = 25;
        G_upload_type.fill = GridBagConstraints.NONE;
        G_upload_type.anchor = GridBagConstraints.CENTER;

        JLabel Jfile_name = new JLabel("File");
        GridBagConstraints G_file_name = new GridBagConstraints();
        G_file_name.gridx = 0;
        G_file_name.gridy = 2;
        G_file_name.gridwidth = 3;
        G_file_name.gridheight = 1;
        G_file_name.weightx = 0.2;
        G_file_name.weighty = 0;
        //G_file_name.ipadx = 25;
        G_file_name.fill = GridBagConstraints.NONE;
        G_file_name.anchor = GridBagConstraints.CENTER;

        JLabel Jspace = new JLabel(" ");
        GridBagConstraints G_uspace = new GridBagConstraints();
        G_uspace.gridx = 0;
        G_uspace.gridy = 1;
        G_uspace.gridwidth = 1;
        G_uspace.gridheight = 1;
        G_uspace.weightx = 0.2;
        G_uspace.weighty = 0;
        //G_uspace.ipadx = 25;
        G_uspace.fill = GridBagConstraints.NONE;
        G_uspace.anchor = GridBagConstraints.CENTER;

        JLabel Jdspace = new JLabel(" ");
        GridBagConstraints G_dspace = new GridBagConstraints();
        G_dspace.gridx = 0;
        G_dspace.gridy = 3;
        G_dspace.gridwidth = 1;
        G_dspace.gridheight = 1;
        G_dspace.weightx = 0.2;
        G_dspace.weighty = 0;
        //G_dspace.ipadx = 25;
        G_dspace.fill = GridBagConstraints.NONE;
        G_dspace.anchor = GridBagConstraints.CENTER;

        JButton Jchoose_file = new JButton("Choose file");
        GridBagConstraints G_choose_file = new GridBagConstraints();
        G_choose_file.gridx = 1;
        G_choose_file.gridy = 0;
        G_choose_file.gridwidth = 1;
        G_choose_file.gridheight = 1;
        G_choose_file.weightx = 0.2;
        G_choose_file.weighty = 0;
        //G_choose_file.ipadx = 25;
        G_choose_file.fill = GridBagConstraints.NONE;
        G_choose_file.anchor = GridBagConstraints.CENTER;

        JButton Jgo_back = new JButton("Back");
        GridBagConstraints G_go_back = new GridBagConstraints();
        G_go_back.gridx = 2;
        G_go_back.gridy = 4;
        G_go_back.gridwidth = 1;
        G_go_back.gridheight = 1;
        G_go_back.weightx = 0.2;
        G_go_back.weighty = 0;
        //G_go_back.ipadx = 25;
        G_go_back.fill = GridBagConstraints.NONE;
        G_go_back.anchor = GridBagConstraints.CENTER;

        JButton J_confirm = new JButton("Confirm");
        GridBagConstraints G_uconfirm = new GridBagConstraints();
        G_uconfirm.gridx = 1;
        G_uconfirm.gridy = 4;
        G_uconfirm.gridwidth = 1;
        G_uconfirm.gridheight = 1;
        G_uconfirm.weightx = 0.2;
        G_uconfirm.weighty = 0;
        //G_uconfirm.ipadx = 25;
        G_uconfirm.fill = GridBagConstraints.NONE;
        G_uconfirm.anchor = GridBagConstraints.CENTER;

        upload_ui.add(Jfile_type, G_file_type);
        upload_ui.add(Jupload_type,G_upload_type);
        upload_ui.add(Jfile_name, G_file_name);
        upload_ui.add(Jchoose_file, G_choose_file);
        upload_ui.add(Jgo_back, G_go_back);
        upload_ui.add(J_confirm, G_uconfirm);
        upload_ui.add(Jspace, G_uspace);
        upload_ui.add(Jdspace, G_dspace);
        

        /*
            Share file
        */
        JFrame share_ui = new JFrame("KAC-CE");
        share_ui.setSize(600,200);
        share_ui.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        share_ui.getContentPane().setLayout(new GridBagLayout());
        share_ui.setLocationRelativeTo(null);

        share_ui.addWindowListener(new WindowAdapter(){
            @Override
            public void windowClosing(WindowEvent e){
                try
                {
                    dos.writeObject("Exit");
                    eos.writeObject("Exit");
                    uos.writeObject("Exit");
                    dos.close();
                    dis.close();
                    
                    uos.close();
                    uis.close();
                    
                    eos.close();
                    eis.close();
                }
                catch(Exception s)
                {
                    System.out.println(s);
                }
            }
        });

        JButton Jsgo_back = new JButton("Back");
        GridBagConstraints G_sgo_back = new GridBagConstraints();
        G_sgo_back.gridx = 3;
        G_sgo_back.gridy = 4;
        G_sgo_back.gridwidth = 1;
        G_sgo_back.gridheight = 1;
        G_sgo_back.weightx = 0.2;
        G_sgo_back.weighty = 0;
        //G_sgo_back.ipadx = 25;
        G_sgo_back.fill = GridBagConstraints.NONE;
        G_sgo_back.anchor = GridBagConstraints.CENTER;

        JButton Jsconfirm = new JButton("Confirm");
        GridBagConstraints G_sconfirm = new GridBagConstraints();
        G_sconfirm.gridx = 2;
        G_sconfirm.gridy = 4;
        G_sconfirm.gridwidth = 1;
        G_sconfirm.gridheight = 1;
        G_sconfirm.weightx = 0.2;
        G_sconfirm.weighty = 0;
        //G_sconfirm.ipadx = 25;
        G_sconfirm.fill = GridBagConstraints.NONE;
        G_sconfirm.anchor = GridBagConstraints.CENTER;

        JTextField Jsname = new HintTextField("User ID:");
        GridBagConstraints G_sname = new GridBagConstraints();
        G_sname.gridx = 1;
        G_sname.gridy = 0;
        G_sname.gridwidth = 3;
        G_sname.gridheight = 1;
        G_sname.weightx = 0.2;
        G_sname.weighty = 0;
        G_sname.ipadx = 250;
        //G_sname.fill = GridBagConstraints.BOTH;
        G_sname.anchor = GridBagConstraints.CENTER;

        JComboBox Jsfile_type = new JComboBox<String>(file_type);
        GridBagConstraints G_sfile_type = new GridBagConstraints();
        G_sfile_type.gridx = 0;
        G_sfile_type.gridy = 0;
        G_sfile_type.gridwidth = 1;
        G_sfile_type.gridheight = 1;
        G_sfile_type.weightx = 0.2;
        G_sfile_type.weighty = 0;
        //G_sfile_type.ipadx = 25;
        G_sfile_type.fill = GridBagConstraints.NONE;
        G_sfile_type.anchor = GridBagConstraints.CENTER;

        JTextPane Jtoken = new JTextPane();
        //Jtoken.setContentType("text/html"); // let the text pane know this is what you want
        Jtoken.setEditable(false); // as before
        Jtoken.setBackground(null); // this is the same as a JLabel
        Jtoken.setBorder(null); // remove the border
        Jtoken.setText("Token");
        GridBagConstraints G_token = new GridBagConstraints();
        G_token.gridx = 0;
        G_token.gridy = 5;
        G_token.gridwidth = 3;
        G_token.gridheight = 1;
        G_token.weightx = 0.2;
        G_token.weighty = 0;
        //G_token.ipadx = 25;
        G_token.fill = GridBagConstraints.NONE;
        G_token.anchor = GridBagConstraints.CENTER;

        JButton Jscopy = new JButton("Copy");
        GridBagConstraints G_scopy = new GridBagConstraints();
        G_scopy.gridx = 3;
        G_scopy.gridy = 5;
        G_scopy.gridwidth = 1;
        G_scopy.gridheight = 1;
        G_scopy.weightx = 0.2;
        G_scopy.weighty = 0;
        //G_scopy.ipadx = 25;
        G_scopy.fill = GridBagConstraints.NONE;
        G_scopy.anchor = GridBagConstraints.CENTER;

        share_ui.add(Jtoken, G_token);
        share_ui.add(Jsgo_back, G_sgo_back);
        share_ui.add(Jsconfirm, G_sconfirm);
        share_ui.add(Jsfile_type, G_sfile_type);
        share_ui.add(Jsname, G_sname);
        share_ui.add(Jscopy, G_scopy);

        /*
            Share token
        */
        JFrame token_ui = new JFrame("KAC-CE");
        token_ui.setSize(600,200);
        token_ui.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        token_ui.getContentPane().setLayout(new GridBagLayout());
        token_ui.setLocationRelativeTo(null);

        token_ui.addWindowListener(new WindowAdapter(){
            @Override
            public void windowClosing(WindowEvent e){
                try
                {
                    dos.writeObject("Exit");
                    eos.writeObject("Exit");
                    uos.writeObject("Exit");
                    dos.close();
                    dis.close();
                    
                    uos.close();
                    uis.close();
                    
                    eos.close();
                    eis.close();
                }
                catch(Exception s)
                {
                    System.out.println(s);
                }
            }
        });

        JButton Jgconfirm = new JButton("Confirm");
        GridBagConstraints G_gconfirm = new GridBagConstraints();
        G_gconfirm.gridx = 1;
        G_gconfirm.gridy = 4;
        G_gconfirm.gridwidth = 1;
        G_gconfirm.gridheight = 1;
        G_gconfirm.weightx = 0.2;
        G_gconfirm.weighty = 0;
        //G_gconfirm.ipadx = 25;
        G_gconfirm.fill = GridBagConstraints.NONE;
        G_gconfirm.anchor = GridBagConstraints.CENTER;

        JButton Jgback = new JButton("Back");
        GridBagConstraints G_gback = new GridBagConstraints();
        G_gback.gridx = 2;
        G_gback.gridy = 4;
        G_gback.gridwidth = 1;
        G_gback.gridheight = 1;
        G_gback.weightx = 0.2;
        G_gback.weighty = 0;
        //G_gback.ipadx = 25;
        G_gback.fill = GridBagConstraints.NONE;
        G_gback.anchor = GridBagConstraints.CENTER;

        JTextField Jgtoken = new HintTextField("Token");
        GridBagConstraints G_gtoken = new GridBagConstraints();
        G_gtoken.gridx = 1;
        G_gtoken.gridy = 3;
        G_gtoken.gridwidth = 1;
        G_gtoken.gridheight = 1;
        G_gtoken.weightx = 0.2;
        G_gtoken.weighty = 0;
        //G_gtoken.ipadx = 25;
        G_gtoken.fill = GridBagConstraints.BOTH;
        G_gtoken.anchor = GridBagConstraints.CENTER;

        JLabel Jghint_token = new JLabel("Token");
        GridBagConstraints G_ghint_token = new GridBagConstraints();
        G_ghint_token.gridx = 0;
        G_ghint_token.gridy = 3;
        G_ghint_token.gridwidth = 1;
        G_ghint_token.gridheight = 1;
        G_ghint_token.weightx = 0.2;
        G_ghint_token.weighty = 0;
        //G_ghint_token.ipadx = 25;
        G_ghint_token.fill = GridBagConstraints.NONE;
        G_ghint_token.anchor = GridBagConstraints.CENTER;

        JComboBox Jgfile_type = new JComboBox<String>(file_type);
        GridBagConstraints T_file_type = new GridBagConstraints();
        T_file_type.gridx = 0;
        T_file_type.gridy = 0;
        T_file_type.gridwidth = 1;
        T_file_type.gridheight = 1;
        T_file_type.weightx = 0.2;
        T_file_type.weighty = 0;
        //T_file_type.ipadx = 25;
        T_file_type.fill = GridBagConstraints.NONE;
        T_file_type.anchor = GridBagConstraints.CENTER;

        JLabel Jghint_user = new JLabel("User");
        GridBagConstraints G_ghint_user = new GridBagConstraints();
        G_ghint_user.gridx = 0;
        G_ghint_user.gridy = 2;
        G_ghint_user.gridwidth = 1;
        G_ghint_user.gridheight = 1;
        G_ghint_user.weightx = 0.2;
        G_ghint_user.weighty = 0;
        //G_ghint_user.ipadx = 25;
        G_ghint_user.fill = GridBagConstraints.NONE;
        G_ghint_user.anchor = GridBagConstraints.CENTER;

        JTextField Jguser = new HintTextField("User");
        GridBagConstraints G_guser = new GridBagConstraints();
        G_guser.gridx = 1;
        G_guser.gridy = 2;
        G_guser.gridwidth = 1;
        G_guser.gridheight = 1;
        G_guser.weightx = 0.2;
        G_guser.weighty = 0;
        //G_guser.ipadx = 25;
        G_guser.fill = GridBagConstraints.BOTH;
        G_guser.anchor = GridBagConstraints.CENTER;

        token_ui.add(Jgconfirm, G_gconfirm);
        token_ui.add(Jgback, G_gback);
        token_ui.add(Jgfile_type, T_file_type);
        token_ui.add(Jguser, G_guser);
        token_ui.add(Jghint_user, G_ghint_user);
        token_ui.add(Jghint_token, G_ghint_token);
        token_ui.add(Jgtoken, G_gtoken);


        /*
            Download file
        */
        JFrame download_ui = new JFrame("KAC-CE");
        download_ui.setSize(600,200);
        download_ui.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        download_ui.getContentPane().setLayout(new GridBagLayout());
        download_ui.setLocationRelativeTo(null);

        download_ui.addWindowListener(new WindowAdapter(){
            @Override
            public void windowClosing(WindowEvent e){
                try
                {
                    dos.writeObject("Exit");
                    eos.writeObject("Exit");
                    uos.writeObject("Exit");
                    dos.close();
                    dis.close();
                    
                    uos.close();
                    uis.close();
                    
                    eos.close();
                    eis.close();
                }
                catch(Exception s)
                {
                    System.out.println(s);
                }
            }
        });

        JButton Jdconfirm = new JButton("Confirm");
        GridBagConstraints G_dconfirm = new GridBagConstraints();
        G_dconfirm.gridx = 1;
        G_dconfirm.gridy = 4;
        G_dconfirm.gridwidth = 1;
        G_dconfirm.gridheight = 1;
        G_dconfirm.weightx = 0.2;
        G_dconfirm.weighty = 0;
        //G_dconfirm.ipadx = 25;
        G_dconfirm.fill = GridBagConstraints.NONE;
        G_dconfirm.anchor = GridBagConstraints.CENTER;

        JButton Jdgo_back = new JButton("Back");
        GridBagConstraints G_dgo_back = new GridBagConstraints();
        G_dgo_back.gridx = 2;
        G_dgo_back.gridy = 4;
        G_dgo_back.gridwidth = 1;
        G_dgo_back.gridheight = 1;
        G_dgo_back.weightx = 0.2;
        G_dgo_back.weighty = 0;
        //G_dgo_back.ipadx = 25;
        G_dgo_back.fill = GridBagConstraints.NONE;
        G_dgo_back.anchor = GridBagConstraints.CENTER;

        JTextField Jduser = new HintTextField("Owner ID");
        GridBagConstraints G_duser = new GridBagConstraints();
        G_duser.gridx = 1;
        G_duser.gridy = 1;
        G_duser.gridwidth = 3;
        G_duser.gridheight = 1;
        G_duser.weightx = 0;
        G_duser.weighty = 0;
        G_duser.ipadx = 250;
        //G_duser.fill = GridBagConstraints.NONE;
        G_duser.anchor = GridBagConstraints.CENTER;

        JTextField Jdname = new HintTextField("File name");
        GridBagConstraints G_dname = new GridBagConstraints();
        G_dname.gridx = 1;
        G_dname.gridy = 2;
        G_dname.gridwidth = 3;
        G_dname.gridheight = 1;
        G_dname.weightx = 0;
        G_dname.weighty = 0;
        G_dname.ipadx = 247;
        //G_dname.fill = GridBagConstraints.NONE;
        G_dname.anchor = GridBagConstraints.CENTER;

        JTextField Jdtoken = new HintTextField("Token");
        GridBagConstraints G_dtoken = new GridBagConstraints();
        G_dtoken.gridx = 1;
        G_dtoken.gridy = 3;
        G_dtoken.gridwidth = 3;
        G_dtoken.gridheight = 1;
        G_dtoken.weightx = 0;
        G_dtoken.weighty = 0;
        G_dtoken.ipadx = 269;
        //G_dtoken.fill = GridBagConstraints.NONE;
        G_dtoken.anchor = GridBagConstraints.CENTER;

        JComboBox Jdfile_type = new JComboBox<String>(file_type);
        GridBagConstraints G_dfile_type = new GridBagConstraints();
        G_dfile_type.gridx = 0;
        G_dfile_type.gridy = 1;
        G_dfile_type.gridwidth = 1;
        G_dfile_type.gridheight = 1;
        G_dfile_type.weightx = 0.2;
        G_dfile_type.weighty = 0;
        //G_dfile_type.ipadx = 25;
        G_dfile_type.fill = GridBagConstraints.NONE;
        G_dfile_type.anchor = GridBagConstraints.CENTER;

        download_ui.add(Jdfile_type, G_dfile_type);
        download_ui.add(Jdname, G_dname);
        download_ui.add(Jduser, G_duser);
        download_ui.add(Jdconfirm, G_dconfirm);
        download_ui.add(Jdgo_back, G_dgo_back);
        //download_ui.add(Jdtoken, G_dtoken);

        /*
            Return result
        */
        JFrame result_ui = new JFrame("KAC-CE");
        result_ui.setSize(300,300);
        result_ui.getContentPane().setLayout(new GridBagLayout());
        result_ui.setLocationRelativeTo(null);

        JLabel result = new JLabel("");
        GridBagConstraints G_result = new GridBagConstraints();
        G_result.gridx = 0;
        G_result.gridy = 0;
        G_result.gridwidth = 1;
        G_result.gridheight = 1;
        G_result.weightx = 0;
        G_result.weighty = 0;
        //G_result.ipadx = 25;
        G_result.fill = GridBagConstraints.BOTH;
        G_result.anchor = GridBagConstraints.CENTER;

        result_ui.add(result, G_result);

        /*
            Initialization events
        */
        confirm.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                
            
                try
                {
                    User_name = userID.getText();
                    String str_ID = User_name;
                    String str_share_id = "";
                
            
                    dos.writeObject("User");
            
                    ID = Zr.newElementFromBytes(str_ID.getBytes());

                    byte[] trans = ID.toBytes();
                    b_ID = ID.toBytes();

                    byte[] recb = null;
            
                    dos.writeObject(trans);
                    recb = (byte[]) dis.readObject();
                    SKi = G1.newElementFromBytes(recb);

                    //writeSKi(ski);

                    if(User_name == "" || User_name == null || User_name == "\n")
                    {
                        hint.setText("User can't be empty!");
                    }
                    else
                    {
                        initialization_ui.setVisible(false);
                        main_ui.setLocation(initialization_ui.getLocation());
                        main_ui.setVisible(true);
                        hello.setText("Hello   " + User_name);
                    }
                    System.out.println(User_name);
                }
                catch(Exception x)
                {
                    System.out.println(x);
                }
            }
        });

        reset.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                userID.setText("");        
            }
        });

        /*
            Main events
        */
        upload.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                //upload_ui.repaint();
                upload_ui.setLocation(main_ui.getLocation());
                upload_ui.setVisible(true);
                main_ui.setVisible(false);
            }
        });

        share.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                share_ui.setLocation(main_ui.getLocation());
                share_ui.setVisible(true);
                main_ui.setVisible(false);
            }
        });

        Generate.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                token_ui.setLocation(main_ui.getLocation());
                token_ui.setVisible(true);
                main_ui.setVisible(false);
            }
        });

        download.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                download_ui.setLocation(main_ui.getLocation());
                download_ui.setVisible(true);
                main_ui.setVisible(false);
            }
        });

        setting.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                setting_ui.setLocation(main_ui.getLocation());
                setting_ui.setVisible(true);
                main_ui.setVisible(false);
            }
        });

        /*
            Setting events
        */
        JAKS_goback.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                main_ui.setLocation(setting_ui.getLocation());
                main_ui.setVisible(true);
                setting_ui.setVisible(false);
            }
        });

        JAKS_delete.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                if(JAKS_detail.getSelectedIndex() != -1)
                {
                    int now = JAKS_detail.getSelectedIndex();
                    model.removeElementAt(now);
                    AKS_detail.remove(now);
                    AKS.remove(now);
                    
                }
                
            }
        });

        JAKS_location.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser f = new JFileChooser();
                f.setCurrentDirectory(new File  (System.getProperty("user.home") + "/Desktop"));
                f.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY); 
                f.showSaveDialog(null);

                JAKS_showlocation.setText(f.getCurrentDirectory().getAbsolutePath() + File.separator + f.getSelectedFile().getName());
            }
        });



        /*
            Upload events
        */
        Jchoose_file.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                FileDialog dialog = new FileDialog(upload_ui, "Select File to Open");
                dialog.setMode(FileDialog.LOAD);
                dialog.setVisible(true);
                String file = dialog.getFile();
                Upload_file_name = file;
                Upload_file_path = dialog.getDirectory();
                //System.out.println(dialog.getDirectory() + file + " chosen.");
                Jfile_name.setText(dialog.getDirectory() + file);
            }
        });

        Jgo_back.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                main_ui.setLocation(upload_ui.getLocation());
                upload_ui.setVisible(false);
                main_ui.setVisible(true);
                Jfile_name.setText("File");
            }
        });

        J_confirm.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                try
                {
                    String File_name = Upload_file_name;
	    		    String File_path = Upload_file_path;
	    		    File file = new File(File_path+File_name);
	    		    if(file.exists())
	    		    {
	    			    byte[] File_content = Files.readAllBytes(file.toPath());
	    			    System.out.println(File_content);
	    			
	    			    //Generate f_key
	    			    Element f_key = GT.newRandomElement();
	    			    byte[] key = Arrays.copyOfRange(f_key.toBytes(), 0, 16);
	    			
	    			    //Encrypted file and store in Desktop
	    			    byte[] encrypted_file = use.File_encrypt(File_content, key);
	    			
	    			    String str_ftype = (String) Jfile_type.getSelectedItem();
	    			    String str_utype = (String) Jupload_type.getSelectedItem();
	    			    Element ftype = Zr.newElementFromBytes(str_ftype.getBytes());
	    			
	    	            //Generate parameter
	    			    //1 ==> pdf
	    			    //2 ==> picture
	    			    //3 ==> word
	    			
	    			    ByteArrayOutputStream stream_index = new ByteArrayOutputStream();
	    			    stream_index.write(ftype.toBytes());
	    			    stream_index.write(ID.toBytes());
	    			
	    			   			
	    			    byte[] index = digest.digest(stream_index.toByteArray());
	    			
	    			    Element f_index = Zr.newElementFromBytes(index);
	    			    Element t = Zr.newRandomElement();
	    			    Element temp = T.duplicate().powZn(t.duplicate());
	    			    Element C1 = f_key.duplicate().mul(temp.duplicate());
	    			    Element C2 = (g1.duplicate().mul(g.duplicate().powZn(f_index.duplicate()))).powZn(t.duplicate());
	    			    Element C3 = h.duplicate().powZn(t.duplicate());
	    	        
	    	            byte[] parameter_C1 = C1.toBytes();
	    	            byte[] parameter_C2 = C2.toBytes();
	    	            byte[] parameter_C3 = C3.toBytes();
	    	       
	    	            ByteArrayOutputStream output = new ByteArrayOutputStream();
	    	            output.write(parameter_C1);
	    	            output.write(parameter_C2);
	    	            output.write(parameter_C3);
                        byte[] parameter = output.toByteArray();
                        
                        ByteArrayOutputStream file_combine = new ByteArrayOutputStream();
                        file_combine.write(parameter);
                        file_combine.write(encrypted_file);

                        byte[] all_combine = file_combine.toByteArray();

	    	            Path path_all_combine = Paths.get(File_path + File_name + "en");
	    	            System.out.println(path_all_combine);
	    			    Files.write(path_all_combine, all_combine);

	    			    String C_name = File_name;
	    			    //String C_para = File_name;
	    			
	    	            byte[] H_name = digest.digest(C_name.getBytes());
	    	        
	    	            String H_re = Base64.getEncoder().encodeToString(H_name).replace('/', 'a');
	    	            H_re = H_re.replace('+', 'a');
	    	            H_re = H_re.replace('=', 'a');

	    	        
	    	            if(str_utype.equals("1.GOOGLE")) {
	    	            	DriveQuickstart.file_upload(H_re, File_path + File_name + "en",str_ftype);
	    	            }else if(str_utype.equals("2.DROPBOX")) {
	    	            	dropbox.uploadFile(H_re,File_path + File_name + "en", str_ftype);
	    	            	
	    	            }else {
	    	            	System.out.println("it will never happend");
	    	            }
                        result.setFont(new Font("Serif", Font.PLAIN, 25));
                        result.setText("Success");
                        result_ui.setVisible(true);
	    		    }
	    		    else
	    		    {
	    			    result.setFont(new Font("Serif", Font.PLAIN, 25));
                        result.setText("Failed");
                        result_ui.setVisible(true);
	    		    }
                }
                catch(Exception z)
                {
                    System.out.println(z);
                }
            }
        });
        /*
            Share events
        */

        Jsgo_back.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                main_ui.setLocation(share_ui.getLocation());
                share_ui.setVisible(false);
                main_ui.setVisible(true);
                Jtoken.setText("Token");
                Jsname.setText("");
            }
        });

        Jscopy.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                String text = Jtoken.getText();
                System.out.println(text);
                StringSelection stringSelection = new StringSelection(text);
                Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                clipboard.setContents(stringSelection, null);

                result_ui.setVisible(true);
                result.setFont(new Font("Serif", Font.PLAIN, 25));
                result.setText("Token Copied");

            }
        });

        Jsconfirm.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {

                try
                {
                    //System.out.println("Enter ID you want to share :");
	        	    String str_share_id = Jsname.getText();
	        	    String s_type = (String) Jsfile_type.getSelectedItem();
	        	
	        	    Element sharing_id = Zr.newElementFromBytes(str_share_id.getBytes());
	        	
	        	    Element S_type = Zr.newElementFromBytes(s_type.getBytes());
	        	    Element second = SKi.duplicate().mul(g.duplicate().powZn(S_type.duplicate()));
	        	
	        	    byte[] H_second = digest.digest(second.toBytes());
	        	
	        	    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
	        	    outputStream.write(sharing_id.toBytes());
	        	    outputStream.write(H_second);

	        	    byte Xi[] = outputStream.toByteArray();
	        	
	        	    byte[] H_Xi = digest.digest(Xi);
	        	
	        	    String B64_Xi = Base64.getEncoder().encodeToString(H_Xi);

                    Jtoken.setText(B64_Xi);
                    System.out.println(Jsname.getText());
                    
                }
                catch(Exception a)
                {
                    System.out.println(a);
                }
            }
                
        });

        /*
            Token events
        */
        Jgconfirm.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {

                try 
	        	{
	        		String IDa = Jguser.getText();
	        		share_id = Zr.newElementFromBytes(IDa.getBytes());
	        		String sb = (String) Jgfile_type.getSelectedItem();
	        		String xi = Jgtoken.getText();
	        		
	        		Element e_sb = Zr.newElementFromBytes(sb.getBytes());
	        		
	        		dos.writeObject("Extract");
		            
	        		dos.writeObject(share_id.toBytes());
	        		dos.writeObject(ID.toBytes());
	        		dos.writeObject(e_sb.toBytes());
	        		dos.writeObject(Base64.getDecoder().decode(xi));
	        		
	        		String input = (String) dis.readObject();
	        		System.out.println(input);
		            if(input.equals("Same"))
		            {
                        byte[] recv = (byte[]) dis.readObject();			
                        AKS.add(Arrays.copyOf(recv, recv.length));   
                        Element SK = G1.newElementFromBytes(recv);	

                        String tag = IDa + " " + sb;
                        if(model.getElementAt(0).equals("No token generated"))
                        {
                            model.removeElementAt(0);
                        }
                        model.addElement(tag);
                        AKS_detail.add(tag);
                        System.out.println(AKS.size());

                        result.setFont(new Font("Serif", Font.PLAIN, 25));
                        result.setText("Success");
                        result_ui.setVisible(true);
                    }
                    else if(!input.equals("Same"))
                    {
                        result.setFont(new Font("Serif", Font.PLAIN, 25));
                        result.setText("Failed");
                        result_ui.setVisible(true);
                    }
                }

                catch(Exception q)
                {
                    System.out.println(q);
                }

                
            }
                
        });

        Jgback.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                main_ui.setLocation(token_ui.getLocation());
                main_ui.setVisible(true);
                token_ui.setVisible(false);
            }
                
        });


        /*
            Download events
        */
        Jdconfirm.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                long total_time = 0;
                try 
	        	{
                    long start_time = System.currentTimeMillis();
                    byte[] recv = null;
	        		//System.out.print("Enter file owner ID :");
	        		String IDa = Jduser.getText();
	        		share_id = Zr.newElementFromBytes(IDa.getBytes());
	        		//System.out.print("Enter file type :");
	        		String sb = (String) Jdfile_type.getSelectedItem();
	        		//System.out.print("Enter authorization token :");
                    Element e_sb = Zr.newElementFromBytes(sb.getBytes());
                    
                    if(AKS_detail.contains(IDa + " " + sb))
                    {
                        for(int z = 0; z < AKS.size(); z++)
                        {
                            Element SK = G1.newElementFromBytes(AKS.get(z));		
				            
				            //Data consumer step 1
				   		    byte[] ID_hash = digest.digest(b_ID);
				   			
				   		    Element l = Zr.newRandomElement();
			            
			                uos.writeObject("Public");
                            
			                recv = (byte[]) uis.readObject();
			                //g^e
			                Element EC_PK_1 = G1.newElementFromBytes(recv);
			                recv = (byte[]) uis.readObject();
			                //g^e*Delta
			                Element EC_PK_2 = G1.newElementFromBytes(recv);
			            
			                Element g_actually_use = g.duplicate();
			            
			                Element g_l = g_actually_use.powZn(l);
			                //System.out.println("g^l :" + g_l);
			                Element pair1, pair2;
			                pair1 = EC_PK_2.duplicate();
			                pair2 = g_l.duplicate();
			                Element pair = pairing.pairing(pair1, pair2);
			                byte[] pair_byte = digest.digest(pair.toBytes());

			                byte[] R = new byte[32];
			                for(int i = 0; i < ID_hash.length; i++)
			                {
			            	    R[i] = (byte) (ID_hash[i] ^ pair_byte[i]);
			                }
			                   
			   			
			   			    eos.writeObject("Public");
			   			
			   			    recv = (byte[]) eis.readObject();
			   			    Element ED_PK = G1.newElementFromBytes(recv);
			   			    //System.out.println(ED_PK);
			   			
			   			    pair1 = ED_PK.duplicate();
			   			    pair2 = g_l.duplicate();
			   			    Element K = pairing.pairing(pair1, pair2);
			   			    //System.out.println("K :" + K);
			   			    Element X = EC_PK_1.duplicate().powZn(l.duplicate());
			   			    Element Y = h.duplicate().powZn(l.duplicate());
			   			
			   			    //System.out.println(K);
			   			
			   			    byte[] K_byte = K.toBytes();
			   			    //System.out.println(K_byte.length);
			   			    //Use First 128 bits / 16 bytes to be AES key
			   			    byte[] AES_Key = Arrays.copyOfRange(K_byte, 0, 16);
			   			
			   			    //R      ===> 32 bytes
			   			    //f_type ===> 32 bytes
			   			    //f_addr ===> 32 bytes
			   			    //System.out.println("Enter file name:");
			   			    String file_name = Jdname.getText();
			   			
			   			    ByteArrayOutputStream stream_index = new ByteArrayOutputStream();
			   			    stream_index.write(e_sb.toBytes());
		    			    stream_index.write(share_id.toBytes());
		    			   			
		    			    byte[] index = digest.digest(stream_index.toByteArray());
		    			
			   			    Element f_type = Zr.newElementFromBytes(index);
			   			    byte[] byte_faddr = digest.digest(file_name.getBytes());
			   			    Element f_addr = Zr.newElementFromBytes(byte_faddr);

			   			
			   			    ByteArrayOutputStream output = new ByteArrayOutputStream();
			   	            output.write(R);
			   	            output.write(index);
			   	            output.write(byte_faddr);
			   	        
			   	            byte[] CT = output.toByteArray();
			   	        
			   	            byte[] CT_encrypted = use.File_encrypt(CT, AES_Key);
			   	            byte[] CT_decrypted = use.File_decrypt(CT_encrypted, AES_Key);
			   	            byte[] R_dec = Arrays.copyOfRange(CT_decrypted, 0, 32);
			   	            System.out.println(Arrays.equals(R, R_dec));
			   	        
			   	            g_actually_use = g1.duplicate();
			   	            Element gg = g.duplicate();
			   	            Element g_ftype = gg.powZn(f_type.duplicate());
			   	            Element temp_key2 = g_actually_use.mul(g_ftype.duplicate());
			   	            temp_key2 = temp_key2.duplicate().powZn(l.duplicate());
			   	            Element l_inverse = l.duplicate().invert();
			   	            Element temp_key3 = SK.duplicate().powZn(l_inverse.duplicate());
			   	            Element temp_key4 = SKi.duplicate().powZn(l_inverse.duplicate());

			   	        
                            eos.writeObject("Req");
                           
                            eos.writeObject(z);
			   	        
			   	            //Trans acc_token
			   	            eos.writeObject(CT_encrypted);
			   	            trans = X.toBytes();
			   	            eos.writeObject(trans);
			   	            trans = Y.toBytes();
			   	            eos.writeObject(trans);
			   	            //Trans temp_key
			   	            trans = temp_key2.toBytes();
			   	            eos.writeObject(trans);
			     	   	    trans = temp_key3.toBytes();
			     	   	    eos.writeObject(trans);
			   	            trans = temp_key4.toBytes();
			   	            eos.writeObject(trans);
			   	    	  
                            String compare_result = (String) eis.readObject();
                        
                            if(compare_result.equals("Match"))
                            {
                                recv = (byte[]) eis.readObject();
			   	                Element C1 = GT.newElementFromBytes(recv);
			   	                recv = (byte[]) eis.readObject();
			   	                Element C2_R = G1.newElementFromBytes(recv);
			   	                recv = (byte[]) eis.readObject();
			   	                Element C3 = G1.newElementFromBytes(recv);
			   	                byte[] pic_encrypt = (byte[]) eis.readObject();
			   	        
			   	                pair1 = SKi.duplicate();
			   	                pair2 = C3.duplicate();
			   	                Element son = pairing.pairing(pair1, pair2);
			   	                pair1 = C2_R.duplicate();
			   	                pair2 = SK.duplicate();
			   	                Element parents = pairing.pairing(pair1, pair2);
			   	                Element fkey = C1.duplicate().mul(son.duplicate().div(parents).duplicate());
			   	        
			   	                byte[] key = fkey.toBytes();
			   	                System.out.println("fkey " + fkey);
			   	                System.out.println("key " + key.length);
			   	                key = Arrays.copyOfRange(key, 0, 16);
			   	                byte[] pic = use.File_decrypt(pic_encrypt, key);
			   	                Path path_dec = Paths.get(System.getProperty("user.home") +File.separator + "Desktop" + File.separator+ "new" + file_name);	
                                Files.write(path_dec, pic);
                                long end_time = System.currentTimeMillis();
                                total_time = end_time - start_time;
                                result.setFont(new Font("Serif", Font.PLAIN, 25));
                                result.setText("Success \n Time: " + total_time + "ms");
                            }else if(!compare_result.equals("Match") && z == AKS.size() - 1)
                            {

                                result.setFont(new Font("Serif", Font.PLAIN, 25));
                                result.setText("Fail, unauthorization");
                                result_ui.setVisible(true);
                            }
                        
                        
                        }	              
                    }
                    else
                    {
                        result.setFont(new Font("Serif", Font.PLAIN, 25));
                        result.setText("Fail, unauthorization");
                        result_ui.setVisible(true);
                    }
                }
	        	catch(Exception f)
	        	{ 
	        		f.printStackTrace(); 
	        	} 

                
            }
        });

        Jdgo_back.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                main_ui.setLocation(download_ui.getLocation());
                main_ui.setVisible(true);
                download_ui.setVisible(false);
            }
        });

        initialization_ui.setVisible(true);

    }

}
class HintTextField extends JTextField implements FocusListener {

    private final String hint;
    private boolean showingHint;
  
    public HintTextField(final String hint) {
      super(hint);
      this.hint = hint;
      this.showingHint = true;
      super.addFocusListener(this);
    }
  
    @Override
    public void focusGained(FocusEvent e) {
      if(this.getText().isEmpty()) {
        super.setText("");
        showingHint = false;
      }
    }
    @Override
    public void focusLost(FocusEvent e) {
      if(this.getText().isEmpty()) {
        super.setText(hint);
        showingHint = true;
      }
    }
  
    @Override
    public String getText() {
      return showingHint ? "" : super.getText();
    }
  }
