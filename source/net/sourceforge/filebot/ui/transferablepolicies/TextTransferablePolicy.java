
package net.sourceforge.filebot.ui.transferablepolicies;


import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.util.logging.Level;
import java.util.logging.Logger;


public abstract class TextTransferablePolicy extends TransferablePolicy {
	
	@Override
	public boolean accept(Transferable tr) {
		if (!isEnabled())
			return false;
		
		return tr.isDataFlavorSupported(DataFlavor.stringFlavor);
	}
	

	@Override
	public void handleTransferable(Transferable tr, boolean add) {
		try {
			String string = (String) tr.getTransferData(DataFlavor.stringFlavor);
			
			if (!add) {
				clear();
			}
			
			load(string);
		} catch (Exception e) {
			// should not happen
			Logger.getLogger(Logger.GLOBAL_LOGGER_NAME).log(Level.SEVERE, e.toString());
		}
	}
	

	protected abstract void clear();
	

	protected abstract boolean load(String text);
	
}
