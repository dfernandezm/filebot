
package net.sourceforge.filebot.ui.panel.search;


import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.URI;
import java.text.NumberFormat;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.KeyStroke;
import javax.swing.ScrollPaneConstants;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;

import net.sourceforge.filebot.resources.ResourceManager;
import net.sourceforge.filebot.ui.FileBotPanel;
import net.sourceforge.filebot.ui.HistoryPanel;
import net.sourceforge.filebot.ui.MessageManager;
import net.sourceforge.filebot.ui.SelectDialog;
import net.sourceforge.filebot.ui.transfer.SaveAction;
import net.sourceforge.filebot.ui.transfer.Saveable;
import net.sourceforge.filebot.web.Episode;
import net.sourceforge.filebot.web.EpisodeListClient;
import net.sourceforge.filebot.web.SearchResult;
import net.sourceforge.tuned.ExceptionUtil;
import net.sourceforge.tuned.ui.SelectButton;
import net.sourceforge.tuned.ui.SelectButtonTextField;
import net.sourceforge.tuned.ui.SimpleIconProvider;
import net.sourceforge.tuned.ui.SwingWorkerPropertyChangeAdapter;
import net.sourceforge.tuned.ui.TextCompletion;
import net.sourceforge.tuned.ui.TunedUtil;


public class SearchPanel extends FileBotPanel {
	
	private JTabbedPane tabbedPane = new JTabbedPane(SwingConstants.TOP, JTabbedPane.SCROLL_TAB_LAYOUT);
	
	private HistoryPanel historyPanel = new HistoryPanel();
	
	private SpinnerNumberModel seasonSpinnerModel = new SpinnerNumberModel(SeasonSpinnerEditor.ALL_SEASONS, SeasonSpinnerEditor.ALL_SEASONS, Integer.MAX_VALUE, 1);
	
	private SelectButtonTextField<EpisodeListClient> searchField;
	
	private TextCompletion searchFieldCompletion;
	
	
	public SearchPanel() {
		super("Search", ResourceManager.getIcon("panel.search"));
		
		searchField = new SelectButtonTextField<EpisodeListClient>();
		
		searchField.getSelectButton().setModel(EpisodeListClient.getAvailableEpisodeListClients());
		searchField.getSelectButton().setIconProvider(SimpleIconProvider.forClass(EpisodeListClient.class));
		
		searchField.getSelectButton().addPropertyChangeListener(SelectButton.SELECTED_VALUE, selectButtonListener);
		
		historyPanel.setColumnHeader1("Show");
		historyPanel.setColumnHeader2("Number of Episodes");
		historyPanel.setColumnHeader3("Duration");
		
		JPanel mainPanel = new JPanel(new BorderLayout(5, 5));
		
		Box searchBox = Box.createHorizontalBox();
		searchBox.setBorder(new EmptyBorder(5, 5, 5, 5));
		
		JSpinner seasonSpinner = new JSpinner(seasonSpinnerModel);
		seasonSpinner.setEditor(new SeasonSpinnerEditor(seasonSpinner));
		searchField.setMaximumSize(searchField.getPreferredSize());
		seasonSpinner.setMaximumSize(seasonSpinner.getPreferredSize());
		
		searchBox.add(Box.createHorizontalGlue());
		searchBox.add(searchField);
		searchBox.add(Box.createHorizontalStrut(15));
		searchBox.add(seasonSpinner);
		searchBox.add(Box.createHorizontalStrut(15));
		searchBox.add(new JButton(searchAction));
		searchBox.add(Box.createHorizontalGlue());
		
		JPanel centerPanel = new JPanel(new BorderLayout());
		centerPanel.setBorder(BorderFactory.createTitledBorder("Search Results"));
		
		Box buttonBox = Box.createHorizontalBox();
		buttonBox.setBorder(new EmptyBorder(5, 5, 5, 5));
		buttonBox.add(Box.createHorizontalGlue());
		buttonBox.add(new JButton(saveAction));
		buttonBox.add(Box.createHorizontalGlue());
		
		centerPanel.add(tabbedPane, BorderLayout.CENTER);
		centerPanel.add(buttonBox, BorderLayout.SOUTH);
		
		JScrollPane historyScrollPane = new JScrollPane(historyPanel, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		historyScrollPane.setBorder(BorderFactory.createEmptyBorder());
		
		tabbedPane.addTab("History", ResourceManager.getIcon("tab.history"), historyScrollPane);
		
		mainPanel.add(searchBox, BorderLayout.NORTH);
		mainPanel.add(centerPanel, BorderLayout.CENTER);
		
		this.add(mainPanel, BorderLayout.CENTER);
		
		TunedUtil.registerActionForKeystroke(this, KeyStroke.getKeyStroke("ENTER"), searchAction);
		TunedUtil.registerActionForKeystroke(this, KeyStroke.getKeyStroke("UP"), upAction);
		TunedUtil.registerActionForKeystroke(this, KeyStroke.getKeyStroke("DOWN"), downAction);
	}
	

	public void setSeasonValue(Object value) {
		if (value != null)
			seasonSpinnerModel.setValue(value);
	}
	
	private final PropertyChangeListener selectButtonListener = new PropertyChangeListener() {
		
		public void propertyChange(PropertyChangeEvent evt) {
			EpisodeListClient client = searchField.getSelected();
			
			if (!client.isSingleSeasonSupported()) {
				seasonSpinnerModel.setValue(SeasonSpinnerEditor.ALL_SEASONS);
				seasonSpinnerModel.setMaximum(SeasonSpinnerEditor.ALL_SEASONS);
			} else {
				seasonSpinnerModel.setMaximum(Integer.MAX_VALUE);
			}
			
		}
		
	};
	
	private final AbstractAction searchAction = new AbstractAction("Find", ResourceManager.getIcon("action.find")) {
		
		public void actionPerformed(ActionEvent e) {
			EpisodeListClient searchEngine = searchField.getSelected();
			
			SearchTask task = new SearchTask(searchEngine, searchField.getText(), seasonSpinnerModel.getNumber().intValue());
			task.addPropertyChangeListener(new SearchTaskListener());
			
			task.execute();
		}
	};
	
	private final AbstractAction upAction = new AbstractAction("Up") {
		
		public void actionPerformed(ActionEvent e) {
			setSeasonValue(seasonSpinnerModel.getNextValue());
		}
	};
	
	private final AbstractAction downAction = new AbstractAction("Down") {
		
		public void actionPerformed(ActionEvent e) {
			setSeasonValue(seasonSpinnerModel.getPreviousValue());
		}
	};
	
	private final SaveAction saveAction = new SaveAction(null) {
		
		@Override
		public void actionPerformed(ActionEvent e) {
			Component comp = tabbedPane.getSelectedComponent();
			
			if (comp instanceof Saveable) {
				setSaveable((Saveable) comp);
				super.actionPerformed(e);
			}
		}
		
	};
	
	
	private class SearchTask extends SwingWorker<Collection<SearchResult>, Void> {
		
		private final String query;
		private final EpisodeListClient client;
		private final int numberOfSeason;
		
		
		public SearchTask(EpisodeListClient client, String query, int numberOfSeason) {
			this.query = query;
			this.client = client;
			this.numberOfSeason = numberOfSeason;
		}
		

		@Override
		protected Collection<SearchResult> doInBackground() throws Exception {
			return client.search(query);
		}
		
	}
	

	private class SearchTaskListener extends SwingWorkerPropertyChangeAdapter {
		
		private EpisodeListPanel episodeList;
		
		
		@Override
		public void started(PropertyChangeEvent evt) {
			SearchTask task = (SearchTask) evt.getSource();
			
			episodeList = new EpisodeListPanel();
			
			String title = task.query;
			
			if (task.numberOfSeason != SeasonSpinnerEditor.ALL_SEASONS) {
				title += String.format(" - Season %d", task.numberOfSeason);
			}
			
			episodeList.setTitle(title);
			episodeList.setIcon(task.client.getIcon());
			
			tabbedPane.addTab(title, episodeList);
			tabbedPane.setTabComponentAt(tabbedPane.indexOfComponent(episodeList), episodeList.getTabComponent());
			
			episodeList.setLoading(true);
		}
		

		@Override
		public void done(PropertyChangeEvent evt) {
			// tab might have been closed
			if (tabbedPane.indexOfComponent(episodeList) < 0)
				return;
			
			SearchTask task = (SearchTask) evt.getSource();
			
			Collection<SearchResult> searchResults;
			
			try {
				searchResults = task.get();
			} catch (Exception e) {
				tabbedPane.remove(episodeList);
				
				Throwable cause = ExceptionUtil.getRootCause(e);
				
				MessageManager.showWarning(cause.getMessage());
				Logger.getLogger(Logger.GLOBAL_LOGGER_NAME).log(Level.WARNING, cause.toString());
				
				return;
			}
			
			SearchResult selectedResult = null;
			/*
			 * NEEDED??? exact find without cache???
			/// TODO: ??????
			if (task.client.getFoundName(task.query) != null) {
				// a show matching the search term exactly has already been found 
				showname = task.client.getFoundName(task.query);
			}*/

			if (searchResults.size() == 1) {
				// only one show found, select this one
				selectedResult = searchResults.iterator().next();
			} else if (searchResults.size() > 1) {
				// multiple shows found, let user selected one
				Window window = SwingUtilities.getWindowAncestor(SearchPanel.this);
				
				SelectDialog<SearchResult> select = new SelectDialog<SearchResult>(window, searchResults);
				
				select.setText("Select a Show:");
				select.setIconImage(episodeList.getIcon().getImage());
				select.setVisible(true);
				
				selectedResult = select.getSelectedValue();
			} else {
				MessageManager.showWarning("\"" + task.query + "\" has not been found.");
			}
			
			if (selectedResult == null) {
				tabbedPane.remove(episodeList);
				return;
			}
			
			String title = selectedResult.getName();
			
			searchFieldCompletion.addTerm(title);
			//TODO fix
			//			Settings.getSettings().putStringList(Settings.SEARCH_HISTORY, searchFieldCompletion.getTerms());
			
			if (task.numberOfSeason != SeasonSpinnerEditor.ALL_SEASONS) {
				title += String.format(" - Season %d", task.numberOfSeason);
			}
			
			episodeList.setTitle(title);
			
			FetchEpisodeListTask getEpisodesTask = new FetchEpisodeListTask(task.client, selectedResult, task.numberOfSeason);
			getEpisodesTask.addPropertyChangeListener(new FetchEpisodeListTaskListener(episodeList));
			
			getEpisodesTask.execute();
		}
	}
	

	private class FetchEpisodeListTaskListener extends SwingWorkerPropertyChangeAdapter {
		
		private EpisodeListPanel episodeList;
		
		
		public FetchEpisodeListTaskListener(EpisodeListPanel episodeList) {
			this.episodeList = episodeList;
		}
		

		@Override
		public void done(PropertyChangeEvent evt) {
			// tab might have been closed
			if (tabbedPane.indexOfComponent(episodeList) < 0)
				return;
			
			FetchEpisodeListTask task = (FetchEpisodeListTask) evt.getSource();
			
			try {
				URI link = task.getSearchEngine().getEpisodeListLink(task.getSearchResult(), task.getNumberOfSeason());
				
				Collection<Episode> episodes = task.get();
				
				String info = (episodes.size() > 0) ? String.format("%d episodes", episodes.size()) : "No episodes found";
				
				historyPanel.add(episodeList.getTitle(), link, episodeList.getIcon(), info, NumberFormat.getInstance().format(task.getDuration()) + " ms");
				
				if (episodes.size() <= 0)
					tabbedPane.remove(episodeList);
				else {
					episodeList.setLoading(false);
					episodeList.getModel().addAll(episodes);
				}
			} catch (Exception e) {
				tabbedPane.remove(episodeList);
				
				Throwable cause = ExceptionUtil.getRootCause(e);
				
				MessageManager.showWarning(cause.getMessage());
				Logger.getLogger(Logger.GLOBAL_LOGGER_NAME).log(Level.SEVERE, cause.getMessage(), cause);
			}
		}
	}
	
}
