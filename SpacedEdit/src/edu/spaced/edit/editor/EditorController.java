package edu.spaced.edit.editor;

import java.awt.Component;
import java.io.File;
import java.io.FileNotFoundException;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;

import edu.spaced.simulation.Level;

public class EditorController implements EditorViewDelegate {
	JFileChooser fileChooser;
	EditorView view;
	Level currentLevel;
	boolean isDirty;
	
	private class LevelFilter extends FileFilter {

		@Override
		public boolean accept(File file) {
			if (file.isDirectory()) {
				return false;
			}
			
			return file.getName().endsWith(".spaced");
		}

		@Override
		public String getDescription() {
			return "Spaced levels (*.spaced)";
		}
	}
	
	public EditorController() {		
		fileChooser = new JFileChooser();
		fileChooser.setFileFilter(new LevelFilter());
		
		currentLevel = new Level();
		
		view = new EditorView(currentLevel);
		view.setDelegate(this);
		view.create();		
		
		isDirty = false;
	}

	@Override
	public boolean doNewLevel() {
		if (!doSaveIfDirty())
			return false;
		
		changeLevel(new Level());
		
		return true;
	}
	
	@Override
	public boolean doOpen() {
		// If there have been changes, ask about saving them.
		if (!doSaveIfDirty())
			return false;
		
		int result = fileChooser.showOpenDialog((Component)view);
		
		if (result == JFileChooser.APPROVE_OPTION) {
			Level level;
			try {
				level = Level.loadFromPath(fileChooser.getSelectedFile().getPath());
				changeLevel(level);
				view.updateStatus("Opened level " + level.getPath());
			} catch (FileNotFoundException e) {
				JOptionPane.showMessageDialog(view,
						"File " + fileChooser.getSelectedFile().getPath() + " was not found!",
						"File not found!", 
						JOptionPane.ERROR_MESSAGE);
			}
		}
		
		return true;
	}
	
	/**
	 * Returns true if the save was successful.
	 */
	public boolean doSaveIfDirty() {
		if (isDirty) {
			int confirm = JOptionPane.showConfirmDialog(view,
					"Level has been modified. Would you like to save the changes?", "Confirm save...",
					JOptionPane.YES_NO_CANCEL_OPTION,
					JOptionPane.QUESTION_MESSAGE);
			if (confirm == JOptionPane.YES_OPTION) {
				doSave();
			} else if (confirm == JOptionPane.CANCEL_OPTION) {
				return false;
			} // else ignore changes
		}
		
		return true;
	}
	
	@Override
	public boolean doSave() {
		// Ask where to save the file if it doesn't have a filename yet
		if (currentLevel.getPath() == null) {
			fileChooser.setSelectedFile(new File(currentLevel.getName() + ".spaced"));
			int result = fileChooser.showSaveDialog((Component)view);
			
			if (result == JFileChooser.APPROVE_OPTION) {
				currentLevel.setPath(fileChooser.getSelectedFile().getPath());
			} else {
				return false;
			}
		}
		isDirty = false;
		currentLevel.save();
		view.updateStatus("Level saved to " + currentLevel.getPath());
		
		return true;
	}


	@Override
	public void changeLevel(Level level) {
		isDirty = false;
		currentLevel = level;
		view.setLevel(currentLevel);	
	}


	@Override
	public EditorView getView() {
		return view;
	}


	@Override
	public void makeDirty() {
		isDirty = true;		
	}

}
