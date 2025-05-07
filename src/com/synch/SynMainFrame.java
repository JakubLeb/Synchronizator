package com.synch;

import java.awt.EventQueue;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import java.awt.Color;
import javax.swing.JButton;
import javax.swing.JTextArea;
import javax.swing.JScrollPane;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

public class SynMainFrame extends JFrame {
    private static final long serialVersionUID = 1L;
    private JPanel contentPane;
    private JTextArea textArea;
    private Synchronizator synch;

    private JButton btnStart;
    private JButton btnStop;

    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    SynMainFrame frame = new SynMainFrame();
                    frame.setVisible(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public SynMainFrame() {
    	synch = new Synchronizator(this);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBounds(100, 100, 450, 300);
        contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));

        setContentPane(contentPane);
        contentPane.setLayout(null);

        JLabel lblNewLabel = new JLabel("Synchronizator");
        lblNewLabel.setHorizontalAlignment(SwingConstants.CENTER);
        lblNewLabel.setBounds(141, 11, 156, 14);
        contentPane.add(lblNewLabel);

        btnStop = new JButton("Stop");
        btnStop.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                synch.stop();
                addMessageToTextArea("Synchronizacja zatrzymana");
           
                btnStop.setEnabled(false);
                btnStart.setEnabled(true);
            }
        });
        btnStop.setBackground(Color.RED);
        btnStop.setBounds(227, 36, 89, 23);
        btnStop.setEnabled(false);
        contentPane.add(btnStop);

        btnStart = new JButton("Start");
        btnStart.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                synch.start();
                addMessageToTextArea("Synchronizacja rozpoczęta");

                btnStart.setEnabled(false);
                btnStop.setEnabled(true);
            }
        });
        btnStart.setBackground(Color.GREEN);
        btnStart.setBounds(128, 36, 89, 23);
        contentPane.add(btnStart);

        textArea = new JTextArea();
        textArea.setEditable(false);

        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setBounds(10, 63, 418, 191);
        contentPane.add(scrollPane);
    }

    public void addMessageToTextArea(String message) {
      textArea.append(message + "\n");
      textArea.setCaretPosition(textArea.getDocument().getLength());
    }
        
    
}