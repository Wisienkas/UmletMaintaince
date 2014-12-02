package com.umlet.language;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.baselet.control.Constants;
import com.baselet.element.GridElement;
import com.umlet.language.converters.IClassConverter;
import com.umlet.language.converters.SourceObject;

public class ClassCodeConverter {
	private static final Logger log = Logger.getLogger(ClassCodeConverter.class);

	public void createCodeDiagrams(String path, List<GridElement> elements) {
		createCodeDiagrams(path, elements, false);
	}

	public void createCodeDiagrams(String path, List<GridElement> elements, boolean rethrow) {
		if (path == null || elements == null) {
			return;
		}

		IClassConverter<?> converter = ClassConverterProvider.getInstance().getCompatibleConverter(Constants.generateSourceType);

		List<SourceObject<?>> sources = new ArrayList<SourceObject<?>>();

		for (GridElement e : elements)
		{
			if (e instanceof com.umlet.element.Class || e instanceof com.baselet.elementnew.element.uml.Class)
			{
				String panelAttributes = e.getPanelAttributes();
				try
				{
					sources.add(converter.parseElementProperties(panelAttributes));
				} catch (Exception ex)
				{
					log.error(converter.getClass().getSimpleName() + " failed to generate source code for a class", ex);
					if (rethrow) {
						throw ex;
					}
				}
			}
		}

		writeSources(path, sources);
	}

	private void writeSources(String path, List<SourceObject<?>> sources)
	{
		File dir = new File(path);

		if (dir.exists() || dir.mkdirs()) {
			for (SourceObject<?> source : sources)
			{
				if (source == null)
				{
					log.debug("Source file was null");
					continue;
				}
				String folder = "";
				if (source.getPackage() != null)
				{
					folder = source.getPackage().replace('.', File.separatorChar);
				}

				String name = source.getName();

				File f = new File(dir, folder + File.separator + name + ".java");
				File parent = f.getParentFile();

				if (parent.exists() || parent.mkdirs())
				{
					@SuppressWarnings("resource")
					FileWriter writer = null;
					try
					{
						String sourcecode = source.toString();

						writer = new FileWriter(f);
						writer.write(sourcecode);
						writer.flush();

					} catch (IOException e)
					{
						log.error("Exception in ClassCodeConverter", e);
					} finally
					{
						try {
							if (writer != null) {
								writer.close();
							}
						} catch (IOException e) {
							log.error("Exception when closing file", e);
						}
					}
				}
			}
		}
	}
}
