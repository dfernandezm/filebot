
package net.sourceforge.filebot.ui.panel.sfv;


import java.io.Closeable;
import java.io.IOException;
import java.util.Formatter;


class VerificationFilePrinter implements Closeable {
	
	protected final Formatter out;
	protected final String algorithm;
	
	
	public VerificationFilePrinter(Formatter out, String algorithm) {
		this.out = out;
		this.algorithm = algorithm;
	}
	

	public void println(String path, String hash) {
		// print entry
		print(path, hash);
		
		// print line separator 
		out.format("%n");
	}
	

	protected void print(String path, String hash) {
		// e.g. 1a02a7c1e9ac91346d08829d5037b240f42ded07 ?SHA1*folder/file.txt
		out.format("%s ?%s*%s", hash, algorithm.toUpperCase(), path);
	}
	

	@Override
	public void close() throws IOException {
		out.close();
	}
	
}
