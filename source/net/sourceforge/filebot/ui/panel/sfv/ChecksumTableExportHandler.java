
package net.sourceforge.filebot.ui.panel.sfv;


import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.Formatter;
import net.sourceforge.filebot.Settings;
import net.sourceforge.filebot.ui.transfer.TextFileExportHandler;
import net.sourceforge.tuned.FileUtilities;


public class ChecksumTableExportHandler extends TextFileExportHandler {
	
	private final ChecksumTableModel model;
	
	
	public ChecksumTableExportHandler(ChecksumTableModel model) {
		this.model = model;
	}
	

	@Override
	public boolean canExport() {
		return model.getRowCount() > 0 && model.getChecksumList().size() > 0;
	}
	

	@Override
	public void export(Formatter out) {
		export(out, model.getChecksumList().get(0));
	}
	

	@Override
	public String getDefaultFileName() {
		return getDefaultFileName(model.getChecksumList().get(0));
	}
	

	public void export(File file, File column) throws IOException {
		PrintWriter out = new PrintWriter(file, "UTF-8");
		
		try {
			export(new Formatter(out), column);
		} finally {
			out.close();
		}
	}
	

	public void export(Formatter out, File column) {
		out.format("; Generated by %s on %tF at %<tT%n", Settings.getApplicationName(), new Date());
		out.format(";%n");
		out.format(";%n");
		
		for (ChecksumRow row : model) {
			//TODO select hash type
			out.format("%s %s%n", row.getName(), row.getChecksum(column).getChecksum(HashType.CRC32));
		}
	}
	

	public String getDefaultFileName(File column) {
		String name = "";
		
		if (column != null)
			name = FileUtilities.getName(column);
		
		if (name.isEmpty())
			name = "name";
		
		return name + ".sfv";
	}
	
}
