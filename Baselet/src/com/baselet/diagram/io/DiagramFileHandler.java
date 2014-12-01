package com.baselet.diagram.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.baselet.control.Constants;
import com.baselet.control.Main;
import com.baselet.control.Notifier;
import com.baselet.control.Path;
import com.baselet.control.SharedConstants.Program;
import com.baselet.diagram.DiagramHandler;
import com.baselet.element.GridElement;
import com.baselet.elementnew.NewGridElement;
import com.umlet.custom.CustomElement;

public class DiagramFileHandler {

	private static final Logger log = Logger.getLogger(DiagramFileHandler.class);

	private static JFileChooser saveFileChooser;

	private String fileName;
	private DiagramHandler handler;
	private FileChangeListener fileChangelistener;
	private File file;
	private File exportFile;
	private HashMap<String, FileFilter> filters = new HashMap<String, FileFilter>();
	private HashMap<FileFilter, String> fileextensions = new HashMap<FileFilter, String>();

	private OwnFileFilter filterxml = new OwnFileFilter(Program.EXTENSION, Program.NAME + " diagram format");
	private OwnFileFilter filterbmp = new OwnFileFilter("bmp", "BMP");
	private OwnFileFilter filtereps = new OwnFileFilter("eps", "EPS");
	private OwnFileFilter filtergif = new OwnFileFilter("gif", "GIF");
	private OwnFileFilter filterjpg = new OwnFileFilter("jpg", "JPG");
	private OwnFileFilter filterpdf = new OwnFileFilter("pdf", "PDF");
	private OwnFileFilter filterpng = new OwnFileFilter("png", "PNG");
	private OwnFileFilter filtersvg = new OwnFileFilter("svg", "SVG");

	private OwnFileFilter[] saveFileFilter = new OwnFileFilter[] { filterxml };
	private OwnFileFilter[] exportFileFilter = new OwnFileFilter[] { filterbmp, filtereps, filtergif, filterjpg, filterpdf, filterpng, filtersvg };
	private List<OwnFileFilter> allFileFilters = new ArrayList<OwnFileFilter>();

	protected DiagramFileHandler(DiagramHandler diagramHandler, File file) {
		handler = diagramHandler;
		if (file != null) {
			fileName = file.getName();
			//We will only start a fileChangeListener if the file is not one of the internally used palletes
			if (!file.getAbsolutePath().contains("Baselet\\palettes\\")) {
				fileChangelistener = new FileChangeListener(handler, this, file);
			}
		}
		else {
			fileName = "new." + Program.EXTENSION;
		}
		this.file = file;
		exportFile = file;

		allFileFilters.addAll(Arrays.asList(saveFileFilter));
		allFileFilters.addAll(Arrays.asList(exportFileFilter));
		for (OwnFileFilter filter : allFileFilters) {
			filters.put(filter.getFormat(), filter);
			fileextensions.put(filter, filter.getFormat());
		}
	}

	public static DiagramFileHandler createInstance(DiagramHandler diagramHandler, File file) {
		return new DiagramFileHandler(diagramHandler, file);
	}

	private JFileChooser reloadSaveFileChooser(boolean ownXmlFormat) {
		// Set the initial target location for the fileChooser
		if (file != null) {
			if (ownXmlFormat) {
				saveFileChooser = new JFileChooser(file);
			}
			else {
				saveFileChooser = new JFileChooser(exportFile);
			}
		}
		else {
			saveFileChooser = new JFileChooser(System.getProperty("user.dir"));
		}

		saveFileChooser.setAcceptAllFileFilterUsed(false); // We don't want "all files" as a choice
		// The input field should show the diagram name as preset
		saveFileChooser.setSelectedFile(new File(Main.getInstance().getDiagramHandler().getName()));
		return saveFileChooser;
	}

	public String getFileName() {
		return fileName;
	}

	public String getFullPathName() {
		if (file != null) {
			return file.getAbsolutePath();
		}
		return "";
	}

	private void setFileName(String fileName) {
		this.fileName = fileName;
		Main.getInstance().getGUI().updateDiagramName(handler, handler.getName());
	}

	private void createXMLOutputDoc(Document doc, Collection<GridElement> elements, Element current) {
		for (GridElement e : elements) {
			appendRecursively(doc, current, e);
		}
	}

	private void appendRecursively(Document doc, Element parentXmlElement, GridElement e) {
		parentXmlElement.appendChild(createXmlElementForGridElement(doc, e));
	}

	private Element createXmlElementForGridElement(Document doc, GridElement e) {
		// insert normal entity element
		java.lang.Class<? extends GridElement> c = e.getClass();
		String sElType = c.getName();
		String sElPanelAttributes = e.getPanelAttributes();
		String sElAdditionalAttributes = e.getAdditionalAttributes();

		Element el = doc.createElement("element");

		if (e instanceof NewGridElement) {
			Element elType = doc.createElement("id");
			elType.appendChild(doc.createTextNode(((NewGridElement) e).getId().toString()));
			el.appendChild(elType);
		}
		else { // OldGridElement
			Element elType = doc.createElement("type");
			elType.appendChild(doc.createTextNode(sElType));
			el.appendChild(elType);
		}

		Element elCoor = doc.createElement("coordinates");
		el.appendChild(elCoor);

		Element elX = doc.createElement("x");
		elX.appendChild(doc.createTextNode("" + e.getRectangle().x));
		elCoor.appendChild(elX);

		Element elY = doc.createElement("y");
		elY.appendChild(doc.createTextNode("" + e.getRectangle().y));
		elCoor.appendChild(elY);

		Element elW = doc.createElement("w");
		elW.appendChild(doc.createTextNode("" + e.getRectangle().width));
		elCoor.appendChild(elW);

		Element elH = doc.createElement("h");
		elH.appendChild(doc.createTextNode("" + e.getRectangle().height));
		elCoor.appendChild(elH);

		Element elPA = doc.createElement("panel_attributes");
		elPA.appendChild(doc.createTextNode(sElPanelAttributes));
		el.appendChild(elPA);

		Element elAA = doc.createElement("additional_attributes");
		elAA.appendChild(doc.createTextNode(sElAdditionalAttributes));
		el.appendChild(elAA);

		if (e instanceof CustomElement) {
			Element elCO = doc.createElement("custom_code");
			elCO.appendChild(doc.createTextNode(((CustomElement) e).getCode()));
			el.appendChild(elCO);
		}
		return el;
	}

	protected String createStringToBeSaved() {
		DocumentBuilder db = null;
		String returnString = null;

		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			db = dbf.newDocumentBuilder();
			Document doc = db.newDocument();

			Element root = doc.createElement("diagram");
			root.setAttribute("program", Program.NAME.toLowerCase());
			root.setAttribute("version", String.valueOf(Program.VERSION));
			doc.appendChild(root);

			// save helptext
			String helptext = handler.getHelpText();
			if (!helptext.equals(Constants.getDefaultHelptext())) {
				Element help = doc.createElement("help_text");
				help.appendChild(doc.createTextNode(helptext));
				root.appendChild(help);
			}

			// save zoom
			Element zoom = doc.createElement("zoom_level");
			zoom.appendChild(doc.createTextNode(String.valueOf(handler.getGridSize())));
			root.appendChild(zoom);

			createXMLOutputDoc(doc, handler.getDrawPanel().getGridElements(), root);

			// output the stuff...
			DOMSource source = new DOMSource(doc);
			StringWriter stringWriter = new StringWriter();
			StreamResult result = new StreamResult(stringWriter);

			TransformerFactory transFactory = TransformerFactory.newInstance();
			Transformer transformer = transFactory.newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");

			transformer.transform(source, result);

			returnString = stringWriter.toString();
		} catch (Exception e) {
			log.error("Error saving XML.", e);
		}

		return returnString;

	}

	public void doOpen() {
		try {
			SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
			FileInputStream input = new FileInputStream(file);
			InputHandler xmlhandler = new InputHandler(handler);
			parser.parse(input, xmlhandler);
			input.close();
		} catch (Exception e) {
			log.error("Cannot open the file: " + file.getAbsolutePath(), e);

		}
	}

	public void doSaveAs(String fileextension) throws IOException {
		String fileName = chooseFileName(fileextension.equals(Program.EXTENSION), filters.get(fileextension));
		String extension = fileextensions.get(saveFileChooser.getFileFilter());
		if (fileName == null)
		{
			return; // If the filechooser has been closed without saving
		}
		if (!fileName.endsWith("." + extension)) {
			fileName += "." + extension;
		}

		File fileToSave = new File(fileName);
		if (extension.equals(Program.EXTENSION)) {
			file = fileToSave;
			setFileName(file.getName());
			save();

			if (fileChangelistener != null) {
				fileChangelistener.stopListening();
			}
			fileChangelistener = new FileChangeListener(handler, this, file);
		}
		else {
			exportFile = fileToSave;
			doExportAs(extension, fileToSave);
		}
	}

	public File doSaveTempDiagram(String filename, String fileextension) throws IOException {
		File tempFile = new File(Path.temp() + filename + "." + fileextension);
		tempFile.deleteOnExit();

		if (fileextension.equals(Program.EXTENSION)) {
			save(tempFile, true);
		}
		else {
			doExportAs(fileextension, tempFile);
		}

		return tempFile;
	}

	public void doSave() throws IOException {
		if (file == null || !file.exists()) {
			doSaveAs(Program.EXTENSION);
		}
		else {
			save();
		}
	}

	public void doExportAs(String extension, File file) throws IOException {
		// CustomElementSecurityManager.addThreadPrivileges(Thread.currentThread(), fileName);
		try {
			OutputHandler.createAndOutputToFile(extension, file, handler);
		} catch (Exception e) {
			throw new IOException(e.getMessage());
		}
		// CustomElementSecurityManager.remThreadPrivileges(Thread.currentThread());
	}

	private void save() throws UnsupportedEncodingException, FileNotFoundException {
		save(file, false); // If save is called without a parameter it uses the class variable "file"
	}

	private void save(File saveToFile, boolean tempFile) throws UnsupportedEncodingException, FileNotFoundException {
		String tmp = createStringToBeSaved();
		PrintWriter out = new PrintWriter(new OutputStreamWriter(new FileOutputStream(saveToFile), "UTF-8"));
		out.print(tmp);
		out.close();
		if (!tempFile) {
			handler.setChanged(false);
			Constants.recentlyUsedFilesList.add(saveToFile.getAbsolutePath());
		}
		Notifier.getInstance().showNotification(saveToFile.getAbsolutePath() + " saved");
	}

	private String chooseFileName(boolean ownXmlFormat, FileFilter filefilter) {
		String fileName = null;

		// filechooser must be recreated to avoid a bug where getSelectedFile() was null (if a file is saved more than one time by doubleclicking on an existing file)
		reloadSaveFileChooser(ownXmlFormat);

		setAvailableFileFilters(ownXmlFormat);
		saveFileChooser.setFileFilter(filefilter);

		int returnVal = saveFileChooser.showSaveDialog(Main.getInstance().getGUI().getMainFrame());
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File selectedFileWithExt = getFileWithExtension();
			if (selectedFileWithExt.exists()) {
				int overwriteQuestionResult = JOptionPane.showConfirmDialog(Main.getInstance().getGUI().getMainFrame(), "File already exists! Overwrite?", "Overwrite File", JOptionPane.YES_NO_OPTION);
				if (overwriteQuestionResult == JOptionPane.NO_OPTION) {
					return chooseFileName(ownXmlFormat, filefilter);
				}
			}
			fileName = selectedFileWithExt.getAbsolutePath();
		}
		return fileName;
	}

	/**
	 * If the filename of the filechooser has no extension, the extension from the filefilter is added to the name
	 */
	private File getFileWithExtension() {
		String extension = "." + fileextensions.get(saveFileChooser.getFileFilter());
		String filename = saveFileChooser.getSelectedFile().getAbsolutePath();
		if (!filename.endsWith(extension)) {
			filename += extension;
		}
		File selectedFileWithExt = new File(filename);
		return selectedFileWithExt;
	}

	/**
	 * Updates the available FileFilter to "only uxf/pxf" or "all but uxf/pxf"
	 * 
	 * @param ownXmlFormat
	 *            If this param is set, only uxf/pxf is visible, otherwise all but uxf/pxf is visible
	 */
	private void setAvailableFileFilters(boolean ownXmlFormat) {
		if (ownXmlFormat) {
			saveFileChooser.resetChoosableFileFilters();
			saveFileChooser.addChoosableFileFilter(filterxml);
		}
		else {
			saveFileChooser.resetChoosableFileFilters();
			saveFileChooser.addChoosableFileFilter(filterbmp);
			saveFileChooser.addChoosableFileFilter(filtereps);
			saveFileChooser.addChoosableFileFilter(filtergif);
			saveFileChooser.addChoosableFileFilter(filterjpg);
			saveFileChooser.addChoosableFileFilter(filterpdf);
			saveFileChooser.addChoosableFileFilter(filterpng);
			saveFileChooser.addChoosableFileFilter(filtersvg);
		}
	}

	protected class OwnFileFilter extends FileFilter {
		private String format;
		private String description;

		protected OwnFileFilter(String format, String description) {
			this.format = format;
			this.description = description;
		}

		@Override
		public boolean accept(File f) {
			return f.getName().endsWith("." + format) || f.isDirectory();
		}

		@Override
		public String getDescription() {
			return description + " (*." + format + ")";
		}

		public String getFormat() {
			return format;
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (file == null ? 0 : file.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		DiagramFileHandler other = (DiagramFileHandler) obj;
		if (file == null) {
			if (other.file != null) {
				return false;
			}
		}
		else if (!file.equals(other.file)) {
			return false;
		}
		return true;
	}

	public void close() {
		if (fileChangelistener != null) {
			fileChangelistener.stopListening();
		}
	}

}
