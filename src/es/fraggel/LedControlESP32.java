/*
 * Copyright (c) 1995, 2008, Oracle and/or its affiliates. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *   - Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *
 *   - Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *
 *   - Neither the name of Oracle or the names of its
 *     contributors may be used to endorse or promote products derived
 *     from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */ 

package es.fraggel;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Properties;
import javax.swing.*;
import javax.swing.colorchooser.AbstractColorChooserPanel;
import javax.swing.event.*;

/* 
 * LedControlESP32.java requires these files:
 *   CrayonPanel.java
 *   images/red.gif
 *   images/yellow.gif
 *   images/green.gif
 *   images/blue.gif
 */
public class LedControlESP32 extends JPanel
                               implements ChangeListener {
    public JLabel banner;
    public JColorChooser tcc;
    String ip;
    String url;
    String lastColor;
    private static final String USER_AGENT = "Mozilla/5.0";
    public LedControlESP32() {
        super(new BorderLayout());
        readProperties();
        JButton apagar=new JButton();
        apagar.setText("Apagar");
        apagar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                url="http://"+ip+"/?r0g0b0&";
                try {
                    sendGET(url);
                }catch(Exception e1){
                    e1.printStackTrace();
                }
            }
        });
        JButton encender=new JButton();
        encender.setText("Encender");
        encender.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String[] split = lastColor.split(",");
                if(split.length>0) {
                    url = "http://" + ip + "/?r" + tcc.getColor().getRed() + "g" + tcc.getColor().getGreen() + "b" + tcc.getColor().getBlue() + "&";
                    lastColor = tcc.getColor().getRed() + "," + tcc.getColor().getGreen() + "," + tcc.getColor().getBlue();
                    try {
                        sendGET(url);
                    } catch (Exception e1) {
                        e1.printStackTrace();
                    }
                }
            }
        });
        tcc = new JColorChooser();
        tcc.setPreviewPanel(new JPanel());
        AbstractColorChooserPanel[] panels = tcc.getChooserPanels();
        for (AbstractColorChooserPanel accp : panels) {
            if(!accp.getDisplayName().equals("HSV")) {
                tcc.removeChooserPanel(accp);
            }
        }
        String[] split = lastColor.split(",");
        if(split.length>0){
            tcc.setColor(new Color(Integer.parseInt(split[0]),Integer.parseInt(split[1]),Integer.parseInt(split[2])));
            url="http://"+ip+"/?r"+tcc.getColor().getRed()+"g"+tcc.getColor().getGreen()+"b"+tcc.getColor().getBlue()+"&";
            lastColor=tcc.getColor().getRed()+","+tcc.getColor().getGreen()+","+tcc.getColor().getBlue();
            System.out.println(lastColor);
            try {
                sendGET(url);
            }catch(Exception e1){
                e1.printStackTrace();
            }
        }
        tcc.getSelectionModel().addChangeListener(this);
        tcc.setBorder(BorderFactory.createTitledBorder("Elegir color"));
        add(encender,BorderLayout.LINE_START);
        add(apagar,BorderLayout.LINE_END);
        add(tcc,BorderLayout.PAGE_END);
    }
    private void readProperties(){
        try (InputStream input = new FileInputStream("config.properties")) {

            Properties prop = new Properties();

            // load a properties file
            prop.load(input);

            // get the property value and print it out
            ip=prop.getProperty("ip");
            lastColor=prop.getProperty("lastColor");


        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
    private void writeProperties(){
        try (OutputStream output = new FileOutputStream("config.properties")) {

            Properties prop = new Properties();

            // set the properties value
            prop.setProperty("ip", ip);
            prop.setProperty("lastColor", lastColor);

            // save properties to project root folder
            prop.store(output, null);

        } catch (IOException io) {
            io.printStackTrace();
        }

    }
    public void stateChanged(ChangeEvent e) {
        Color newColor = tcc.getColor();

        url="http://"+ip+"/?r"+newColor.getRed()+"g"+newColor.getGreen()+"b"+newColor.getBlue()+"&";
        lastColor=newColor.getRed()+","+newColor.getGreen()+","+newColor.getBlue();
        writeProperties();
        try {
            sendGET(url);
        }catch(Exception e1){
            e1.printStackTrace();
        }

    }

    /**
     * Create the GUI and show it.  For thread safety,
     * this method should be invoked from the
     * event-dispatching thread.
     */
    private static void createAndShowGUI() {
        //Create and set up the window.
        JFrame frame = new JFrame("LedControl ESP32");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        //Create and set up the content pane.
        JComponent newContentPane = new LedControlESP32();
        newContentPane.setOpaque(true); //content panes must be opaque
        frame.setContentPane(newContentPane);
        //Display the window.
        frame.pack();
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        //Schedule a job for the event-dispatching thread:
        //creating and showing this application's GUI.
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI();
            }
        });
    }
    private static void sendGET(String url) throws IOException {
        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        con.setRequestMethod("GET");
        con.setRequestProperty("User-Agent", USER_AGENT);
        int responseCode = con.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) { // success
        } else {
            System.out.println("GET request not worked");
        }

    }
}
