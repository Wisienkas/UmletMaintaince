package com.baselet.control;

import java.awt.Frame;
import java.awt.Point;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Arrays;
import java.util.Properties;

import com.baselet.control.SharedConstants.Program;
import com.baselet.diagram.draw.geom.Dimension;
import com.baselet.gui.BaseGUI;
import com.baselet.gui.standalone.StandaloneGUI;
import com.umlet.language.FieldOptions;
import com.umlet.language.MethodOptions;
import com.umlet.language.SignatureOptions;
import com.umlet.language.sorting.SortOptions;

public class ConfigHandler {

	private static final String PROGRAM_VERSION = "program_version";
	private static final String PROPERTIES_PANEL_FONTSIZE = "properties_panel_fontsize";
	private static final String DEFAULT_FONTSIZE = "default_fontsize";
	private static final String DEFAULT_FONTFAMILY = "default_fontfamily";
	private static final String SHOW_STICKINGPOLYGON = "show_stickingpolygon";
	private static final String SHOW_GRID = "show_grid";
	private static final String ENABLE_CUSTOM_ELEMENTS = "enable_custom_elements";
	private static final String UI_MANAGER = "ui_manager";
	private static final String PRINT_PADDING = "print_padding";
	private static final String PDF_EXPORT_FONT = "pdf_export_font";
	private static final String CHECK_FOR_UPDATES = "check_for_updates";
	private static final String OPEN_FILE_HOME = "open_file_home";
	private static final String DEV_MODE = "dev_mode";
	private static final String LAST_USED_PALETTE = "last_used_palette";
	private static final String MAIN_SPLIT_POSITION = "main_split_position";
	private static final String RIGHT_SPLIT_POSITION = "right_split_position";
	private static final String START_MAXIMIZED = "start_maximized";
	private static final String MAIL_SPLIT_POSITION = "mail_split_position";
	private static final String PROGRAM_SIZE = "program_size";
	private static final String PROGRAM_LOCATION = "program_location";
	private static final String RECENT_FILES = "recent_files";
	private static final String MAIL_SMTP = "mail_smtp";
	private static final String MAIL_SMTP_AUTH = "mail_smtp_auth";
	private static final String MAIL_SMTP_USER = "mail_smtp_user";
	private static final String MAIL_SMTP_PW_STORE = "mail_smtp_pw_store";
	private static final String MAIL_SMTP_PW = "mail_smtp_pw";
	private static final String MAIL_FROM = "mail_from";
	private static final String MAIL_TO = "mail_to";
	private static final String MAIL_CC = "mail_cc";
	private static final String MAIL_BCC = "mail_bcc";
	private static final String MAIL_XML = "mail_xml";
	private static final String MAIL_GIF = "mail_gif";
	private static final String MAIL_PDF = "mail_pdf";
	private static final String GENERATE_CLASS_PACKAGE = "generate_class_package";
	private static final String GENERATE_CLASS_FIELDS = "generate_class_fields";
	private static final String GENERATE_CLASS_METHODS = "generate_class_methods";
	private static final String GENERATE_CLASS_SIGNATURES = "generate_class_signatures";
	private static final String GENERATE_CLASS_SORTINGS = "generate_class_sortings";

	private static File configfile;
	private static Properties props;

	public static void loadConfig() {
		Config cfg = Config.getInstance();

		configfile = new File(Path.config());
		if (!configfile.exists()) {
			return;
		}

		props = new Properties();
		try {
			FileInputStream inputStream = new FileInputStream(Path.config());
			try {
				props.load(inputStream);
			} finally {
				inputStream.close();
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		String cfgVersion = getStringProperty(PROGRAM_VERSION, Program.VERSION);
		Constants.defaultFontsize = getIntProperty(DEFAULT_FONTSIZE, Constants.defaultFontsize);
		Constants.propertiesPanelFontsize = getIntProperty(PROPERTIES_PANEL_FONTSIZE, Constants.propertiesPanelFontsize);
		Constants.defaultFontFamily = getStringProperty(DEFAULT_FONTFAMILY, Constants.defaultFontFamily);
		SharedConstants.show_stickingpolygon = getBoolProperty(SHOW_STICKINGPOLYGON, SharedConstants.show_stickingpolygon);
		Constants.show_grid = getBoolProperty(SHOW_GRID, Constants.show_grid);
		Constants.enable_custom_elements = getBoolProperty(ENABLE_CUSTOM_ELEMENTS, Constants.enable_custom_elements);
		cfg.setUiManager(getStringProperty(UI_MANAGER, cfg.getUiManager()));
		Constants.printPadding = getIntProperty(PRINT_PADDING, Constants.printPadding);
		Constants.pdfExportFont = getStringProperty(PDF_EXPORT_FONT, Constants.pdfExportFont);
		Constants.checkForUpdates = getBoolProperty(CHECK_FOR_UPDATES, Constants.checkForUpdates);
		Constants.openFileHome = getStringProperty(OPEN_FILE_HOME, Constants.openFileHome);
		SharedConstants.dev_mode = getBoolProperty(DEV_MODE, SharedConstants.dev_mode);

		// only set last used palette if its a valid palette and if the program version has not changed, otherwise leave the default value
		String tempVal = getStringProperty(LAST_USED_PALETTE, null);
		if (Main.getInstance().getPaletteNames().contains(tempVal) && Program.VERSION.equals(cfgVersion)) {
			Constants.lastUsedPalette = tempVal;
		}

		Constants.main_split_position = getIntProperty(MAIN_SPLIT_POSITION, Constants.main_split_position);
		Constants.right_split_position = getIntProperty(RIGHT_SPLIT_POSITION, Constants.right_split_position);
		Constants.mail_split_position = getIntProperty(MAIL_SPLIT_POSITION, Constants.mail_split_position);
		Constants.start_maximized = getBoolProperty(START_MAXIMIZED, Constants.start_maximized);

		// In case of start_maximized=true we don't store any size or location information
		if (!Constants.start_maximized) {
			Constants.program_size = getDimensionProperty(PROGRAM_SIZE, Constants.program_size);
			Constants.program_location = getPointProperty(PROGRAM_LOCATION, Constants.program_location);
		}

		String recentFiles = props.getProperty(RECENT_FILES);
		if (recentFiles != null) {
			Constants.recentlyUsedFilesList.addAll(Arrays.asList(props.getProperty(RECENT_FILES).split("\\|")));
		}

		/* Mail */
		Constants.mail_smtp = getStringProperty(MAIL_SMTP, Constants.mail_smtp);
		Constants.mail_smtp_auth = getBoolProperty(MAIL_SMTP_AUTH, Constants.mail_smtp_auth);
		Constants.mail_smtp_user = getStringProperty(MAIL_SMTP_USER, Constants.mail_smtp_user);
		Constants.mail_smtp_pw_store = getBoolProperty(MAIL_SMTP_PW_STORE, Constants.mail_smtp_pw_store);
		Constants.mail_smtp_pw = getStringProperty(MAIL_SMTP_PW, Constants.mail_smtp_pw);
		Constants.mail_from = getStringProperty(MAIL_FROM, Constants.mail_from);
		Constants.mail_to = getStringProperty(MAIL_TO, Constants.mail_to);
		Constants.mail_cc = getStringProperty(MAIL_CC, Constants.mail_cc);
		Constants.mail_bcc = getStringProperty(MAIL_BCC, Constants.mail_bcc);
		Constants.mail_xml = getBoolProperty(MAIL_XML, Constants.mail_xml);
		Constants.mail_gif = getBoolProperty(MAIL_GIF, Constants.mail_gif);
		Constants.mail_pdf = getBoolProperty(MAIL_PDF, Constants.mail_pdf);

		/* Generate Class Element Options */
		Constants.generateClassPackage = getBoolProperty(GENERATE_CLASS_PACKAGE, Constants.generateClassPackage);
		Constants.generateClassFields = FieldOptions.getEnum(getStringProperty(GENERATE_CLASS_FIELDS, Constants.generateClassFields.toString()));
		Constants.generateClassMethods = MethodOptions.getEnum(getStringProperty(GENERATE_CLASS_METHODS, Constants.generateClassMethods.toString()));
		Constants.generateClassSignatures = SignatureOptions.getEnum(getStringProperty(GENERATE_CLASS_SIGNATURES, Constants.generateClassSignatures.toString()));
		Constants.generateClassSortings = SortOptions.getEnum(getStringProperty(GENERATE_CLASS_SORTINGS, Constants.generateClassSortings.toString()));
	}

	public static void saveConfig() {
		Config cfg = Config.getInstance();

		if (configfile == null) {
			return;
		}
		try {
			configfile.delete();
			configfile.createNewFile();

			Properties props = new Properties();

			props.setProperty(PROGRAM_VERSION, Program.VERSION);
			props.setProperty(DEFAULT_FONTSIZE, Integer.toString(Constants.defaultFontsize));
			props.setProperty(PROPERTIES_PANEL_FONTSIZE, Integer.toString(Constants.propertiesPanelFontsize));
			props.setProperty(DEFAULT_FONTFAMILY, Constants.defaultFontFamily);
			props.setProperty(SHOW_STICKINGPOLYGON, Boolean.toString(SharedConstants.show_stickingpolygon));
			props.setProperty(SHOW_GRID, Boolean.toString(Constants.show_grid));
			props.setProperty(ENABLE_CUSTOM_ELEMENTS, Boolean.toString(Constants.enable_custom_elements));
			props.setProperty(UI_MANAGER, cfg.getUiManager());
			props.setProperty(PRINT_PADDING, Integer.toString(Constants.printPadding));
			props.setProperty(PDF_EXPORT_FONT, Constants.pdfExportFont);
			props.setProperty(CHECK_FOR_UPDATES, Boolean.toString(Constants.checkForUpdates));
			props.setProperty(OPEN_FILE_HOME, Constants.openFileHome);
			props.setProperty(DEV_MODE, Boolean.toString(SharedConstants.dev_mode));
			props.setProperty(LAST_USED_PALETTE, Constants.lastUsedPalette);

			BaseGUI gui = Main.getInstance().getGUI();
			props.setProperty(MAIN_SPLIT_POSITION, Integer.toString(gui.getMainSplitPosition()));
			props.setProperty(RIGHT_SPLIT_POSITION, Integer.toString(gui.getRightSplitPosition()));
			props.setProperty(MAIL_SPLIT_POSITION, Integer.toString(gui.getMailSplitPosition()));
			if (gui instanceof StandaloneGUI) {
				// If the window is maximized in any direction this fact is written in the cfg
				Frame topContainer = ((StandaloneGUI) gui).getMainFrame();
				if ((topContainer.getExtendedState() & Frame.MAXIMIZED_BOTH) == Frame.MAXIMIZED_BOTH) {
					props.setProperty(START_MAXIMIZED, "true");
				}
				// Otherwise the size and the location is written in the cfg
				else {
					props.setProperty(START_MAXIMIZED, "false");
					props.setProperty(PROGRAM_SIZE, topContainer.getSize().width + "," + topContainer.getSize().height);
					props.setProperty(PROGRAM_LOCATION, topContainer.getLocation().x + "," + topContainer.getLocation().y);
				}
			}
			if (!Constants.recentlyUsedFilesList.isEmpty()) {
				String recentFileString = "";
				for (String recentFile : Constants.recentlyUsedFilesList) {
					recentFileString += recentFile + "|";
				}
				props.setProperty(RECENT_FILES, recentFileString.substring(0, recentFileString.length() - 1));
			}

			/* MAIL */
			if (!!Constants.mail_smtp.isEmpty()) {
				props.setProperty(MAIL_SMTP, Constants.mail_smtp);
			}
			props.setProperty(MAIL_SMTP_AUTH, Boolean.toString(Constants.mail_smtp_auth));
			if (!Constants.mail_smtp_user.isEmpty()) {
				props.setProperty(MAIL_SMTP_USER, Constants.mail_smtp_user);
			}
			props.setProperty(MAIL_SMTP_PW_STORE, Boolean.toString(Constants.mail_smtp_pw_store));
			if (!Constants.mail_smtp_pw.isEmpty()) {
				props.setProperty(MAIL_SMTP_PW, Constants.mail_smtp_pw);
			}
			if (!Constants.mail_from.isEmpty()) {
				props.setProperty(MAIL_FROM, Constants.mail_from);
			}
			if (!Constants.mail_to.isEmpty()) {
				props.setProperty(MAIL_TO, Constants.mail_to);
			}
			if (!Constants.mail_cc.isEmpty()) {
				props.setProperty(MAIL_CC, Constants.mail_cc);
			}
			if (!Constants.mail_bcc.isEmpty()) {
				props.setProperty(MAIL_BCC, Constants.mail_bcc);
			}
			props.setProperty(MAIL_XML, Boolean.toString(Constants.mail_xml));
			props.setProperty(MAIL_GIF, Boolean.toString(Constants.mail_gif));
			props.setProperty(MAIL_PDF, Boolean.toString(Constants.mail_pdf));

			/* Generate Class Element Options */
			props.setProperty(GENERATE_CLASS_PACKAGE, Boolean.toString(Constants.generateClassPackage));
			props.setProperty(GENERATE_CLASS_FIELDS, Constants.generateClassFields.toString());
			props.setProperty(GENERATE_CLASS_METHODS, Constants.generateClassMethods.toString());
			props.setProperty(GENERATE_CLASS_SIGNATURES, Constants.generateClassSignatures.toString());
			props.setProperty(GENERATE_CLASS_SORTINGS, Constants.generateClassSortings.toString());

			FileOutputStream outStream = new FileOutputStream(configfile);
			try {
				props.store(outStream, null);
			} finally {
				outStream.close();
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private static int getIntProperty(String key, int defaultValue) {
		String result = props.getProperty(key);
		if (result != null) {
			try {
				return Integer.parseInt(result);
			} catch (NumberFormatException e) {
				e.printStackTrace();
			}
		}
		return defaultValue;
	}

	private static boolean getBoolProperty(String key, boolean defaultValue) {
		String result = props.getProperty(key);
		if (result != null) {
			return Boolean.parseBoolean(result);
		}
		return defaultValue;
	}

	private static String getStringProperty(String key, String defaultValue) {
		return props.getProperty(key, defaultValue);
	}

	private static Dimension getDimensionProperty(String key, Dimension defaultValue) {
		String result = props.getProperty(key);
		if (result != null) {
			try {
				int x = Integer.parseInt(result.substring(0, result.indexOf(",")));
				int y = Integer.parseInt(result.substring(result.indexOf(",") + 1));
				return new Dimension(x, y);
			} catch (NumberFormatException e) {
				e.printStackTrace();
			}
		}
		return defaultValue;
	}

	private static Point getPointProperty(String key, Point defaultValue) {
		String result = props.getProperty(key);
		if (result != null) {
			try {
				int x = Integer.parseInt(result.substring(0, result.indexOf(",")));
				int y = Integer.parseInt(result.substring(result.indexOf(",") + 1));
				return new Point(x, y);
			} catch (NumberFormatException e) {
				e.printStackTrace();
			}
		}
		return defaultValue;
	}
}