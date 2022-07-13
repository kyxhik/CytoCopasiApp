package org.cytoscape.CytoCopasiApp;

import java.awt.GridLayout;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;

public class TabCreater {
	public JTabbedPane instertTab(JFrame frame, JTabbedPane tabbedPane, String overallTitle, JScrollPane sp) {
		frame.setSize(500,300);
		frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().setLayout(new GridLayout(1, 1));
        
        tabbedPane.addTab(overallTitle, sp);
        //frame.getContentPane().add(tabbedPane);
        return tabbedPane;
	}
}
