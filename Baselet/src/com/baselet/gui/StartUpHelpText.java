package com.baselet.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.Scanner;

import javax.swing.JEditorPane;

import org.apache.log4j.Logger;

import com.baselet.control.BrowserLauncher;
import com.baselet.control.Constants;
import com.baselet.control.Constants.Metakey;
import com.baselet.control.Constants.SystemInfo;
import com.baselet.control.Main;
import com.baselet.control.Path;
import com.baselet.control.SharedConstants.Program;
import com.baselet.control.Utils;
import com.baselet.diagram.DrawPanel;
import com.baselet.gui.listener.HyperLinkActiveListener;

public class StartUpHelpText extends JEditorPane implements ContainerListener, ComponentListener {

	private static final long serialVersionUID = 1L;

	private static final Logger log = Logger.getLogger(StartUpHelpText.class);

	private DrawPanel panel;
	private boolean visible;

	private static String filename;
	private static Thread updateChecker;

	public StartUpHelpText(DrawPanel panel) {
		super();
		this.panel = panel;

		// If the GUI is null (e.g.: if main is used in batch mode) the startup help text is not required
		if (Main.getInstance().getGUI() == null) {
			return;
		}

		panel.addContainerListener(this);
		panel.addComponentListener(this);
		addMouseListener(new DelegatingMouseListener());

		startUpdatechecker();
		try {
			if (filename == null) {
				filename = createTempFileWithText(getDefaultTextWithReplacedSystemspecificMetakeys());
			}
			showHTML();
		} catch (Exception e) {
			log.error(null, e);
		}
	}

	private void startUpdatechecker() {
		if (Constants.checkForUpdates && updateChecker == null) {
			updateChecker = new Thread(new Updater(), "Update Checker");
			updateChecker.start();
		}
	}

	// Must be overwritten to hide the helptext if a the custom elements panel is toggled without elements on the drawpanel
	@Override
	public void setEnabled(boolean en) {
		super.setEnabled(en);
		if (en && visible) {
			if (panel.getGridElements().size() == 0) {
				setVisible(true);
			}
			else {
				visible = false;
			}
		}
		else {
			visible = isVisible();
			setVisible(false);
		}
	}

	private static String getStartUpFileName() {
		return Path.homeProgram() + "html/startuphelp.html";
	}

	private void showHTML() throws Exception {
		this.setPage(new URL("file:///" + filename));
		addHyperlinkListener(new HyperLinkActiveListener());
		setEditable(false);
		setBackground(Color.WHITE);
		setSelectionColor(getBackground());
		setSelectedTextColor(getForeground());
	}

	private static String createTempFileWithText(String textToWriteIntoFile) throws IOException {
		File tempFile = File.createTempFile(Program.NAME + "_startupfile", ".html");
		tempFile.deleteOnExit();
		FileWriter w = new FileWriter(tempFile);
		w.write(textToWriteIntoFile);
		w.close();
		return tempFile.getAbsolutePath();
	}

	private String getDefaultTextWithReplacedSystemspecificMetakeys() throws FileNotFoundException {
		String text = "";
		Scanner sc = null;
		try {
			sc = new Scanner(new File(getStartUpFileName()));
			while (sc.hasNextLine()) {
				String line = sc.nextLine();
				if (SystemInfo.META_KEY == Metakey.CTRL) {
					line = line.replace(Metakey.CMD.toString(), Metakey.CTRL.toString());
				}
				else if (SystemInfo.META_KEY == Metakey.CMD) {
					line = line.replace(Metakey.CTRL.toString(), Metakey.CMD.toString());
				}
				text += line + "\n";
			}
		} finally {
			if (sc != null) {
				sc.close();
			}
		}
		return text;
	}

	@Override
	public void componentAdded(ContainerEvent e) {
		boolean gridElementAdded = panel.getElementToComponent(e.getChild()) != null;
		if (gridElementAdded) {
			setVisible(false);
		}
	}

	@Override
	public void componentRemoved(ContainerEvent e) {
		if (e.getContainer().getComponentCount() <= 1 && !equals(e.getChild())) {
			setVisible(true);
		}
	}

	@Override
	public void componentResized(ComponentEvent arg0) {
		Dimension size = panel.getSize();
		Dimension labelSize = getPreferredSize();
		this.setSize(labelSize);
		int minDistanceFromTop = 25;
		int labelSizeToSubtract = Math.max(150, labelSize.height); // the upper border of the startup panel is at least 200px over the middle of the screen (necessary to have a good position for small update info windows)
		this.setLocation(size.width / 2 - labelSize.width / 2, Math.max(minDistanceFromTop, size.height / 2 - labelSizeToSubtract));
	}

	@Override
	public void paint(Graphics g) {
		// Subpixel rendering must be disabled for the startuphelp (looks bad)
		((Graphics2D) g).setRenderingHints(Utils.getUxRenderingQualityHigh(false));
		super.paint(g);
		((Graphics2D) g).setRenderingHints(Utils.getUxRenderingQualityHigh(true));
	}

	@Override
	public void componentHidden(ComponentEvent arg0) {}

	@Override
	public void componentMoved(ComponentEvent arg0) {}

	@Override
	public void componentShown(ComponentEvent arg0) {}

	/**
	 * The MouseListener of this JEditorPane just delegates the
	 * MouseEvents up to the DiagramListener of the Handler
	 */
	private class DelegatingMouseListener implements MouseListener {

		@Override
		public void mouseClicked(MouseEvent e) {
			panel.getHandler().getListener().mouseClicked(e);
		}

		@Override
		public void mouseEntered(MouseEvent e) {
			panel.getHandler().getListener().mouseEntered(e);
		}

		@Override
		public void mouseExited(MouseEvent e) {
			panel.getHandler().getListener().mouseExited(e);
		}

		@Override
		public void mousePressed(MouseEvent e) {
			panel.getHandler().getListener().mousePressed(e);
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			panel.getHandler().getListener().mouseReleased(e);
		}
	}

	// TODO: If the thread takes too much time, the update message is only shown if a new panel is opened
	private class Updater implements Runnable {
		@Override
		public void run() {
			try {
				String newVersionText = getNewVersionTextWithStartupHtmlFormat(getStartUpFileName());
				if (newVersionText != null) { // The text is != null if a new version exists
					filename = createTempFileWithText(newVersionText);
				}
			} catch (Exception e) {
				log.error("Error at checking for new " + Program.NAME + " version", e);
			}
		}

		private String getNewVersionTextWithStartupHtmlFormat(String startupFileName) throws IOException {
			String textFromURL = getNewVersionTextFromURL();
			if (textFromURL == null) {
				return null;
			}

			String returnText = "";
			Scanner sc = null;
			try {
				sc = new Scanner(new File(startupFileName));
				while (sc.hasNextLine()) {
					String line = sc.nextLine();
					if (line.contains("<body>")) {
						break;
					}
					returnText += line + "\n";
				}
				returnText += textFromURL + "</body></html>";
			} finally {
				if (sc != null) {
					sc.close();
				}
			}
			return returnText;
		}

		private String getNewVersionTextFromURL() throws IOException {
			String versionText = BrowserLauncher.readURL(Program.WEBSITE + "/current_umlet_version_changes.txt");
			versionText = versionText.replace("<", "&lt;").replace(">", "&gt;").replace("&", "&amp;").replace("\"", "&quot;"); // escape html characters for safety
			String[] splitString = versionText.split("\n");
			String actualVersion = splitString[0];
			if (Program.VERSION.compareTo(actualVersion) > 0)
			{
				return null; // no newer version found
			}

			String returnText = "<p><b>A new version of " + Program.NAME + " (" + actualVersion + ") is available at <a href=\"" + Program.WEBSITE + "\">" + Program.WEBSITE.substring("http://".length()) + "</a></b></p>";
			// Every line after the first one describes a feature of the new version and will be listed
			for (int i = 1; i < splitString.length; i++) {
				returnText += "<p>" + splitString[i] + "</p>";
			}
			return returnText;
		}
	}
}
