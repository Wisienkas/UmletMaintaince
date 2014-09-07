package com.baselet.gui;

import java.awt.Color;
import java.io.IOException;
import java.net.URL;

import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.border.LineBorder;

import org.apache.log4j.Logger;

import com.baselet.control.Path;
import com.baselet.control.SharedConstants.Program;
import com.baselet.gui.listener.HyperLinkActiveListener;

public class AboutDialog {

	private static final Logger log = Logger.getLogger(AboutDialog.class);

	public static void show() {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				try {
					final JEditorPane edit = new JEditorPane();
					edit.setBorder(new LineBorder(Color.GRAY, 1, true));
					edit.setPage(new URL("file:///" + Path.homeProgram() + "html/about.html"));
					edit.addHyperlinkListener(new HyperLinkActiveListener());
					edit.setEditable(false);
					edit.setSelectionColor(Color.WHITE);
					JDialog instance = new JOptionPane(edit, JOptionPane.PLAIN_MESSAGE).createDialog("About " + Program.NAME);
					instance.setVisible(true);
				} catch (IOException e) {
					log.error(null, e);
				}
			}
		});
	}

}
