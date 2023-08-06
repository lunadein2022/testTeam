package test;

/*package org.sp.tproject.main.view;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.datatype.Artwork;
import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.Player;
import javax.imageio.ImageIO;

public class MusicPlayer extends JPanel {
    // UI Components
    private JLabel songNameLabel, artistNameLabel, albumCoverLabel, timeLabel;
    private JSlider songProgressBar;
    private JButton playPauseButton, nextButton, previousButton, openFileButton, switchViewButton, switchViewButtonForPlaylist, deleteSongButton, deleteAllSongsButton;
    private JPanel controlsPanel, playlistPanel, infoPanel, progressBarPanel;
    private JList<File> playlist;

    // Playlist and player state
    private List<File> songFiles = new ArrayList<>();
    private DefaultListModel<File> playlistModel;
    private Player player;
    private int currentSongIndex = 0;
    private boolean isPlayingView = true;
    private boolean isPlaying = false;
    private long songTotalLength;
    private long songPosition;
    private double totalSongDurationInSeconds;
    
    
    // Constructor
    public MusicPlayer() {
        initComponents();
    }

    private void initComponents() {
        setupPanelPreferences();
        initializeUIComponents();
        setLayouts();
        setDefaultAlbumCover();
        registerEventListeners();
        addComponentsToPanel();


    }

    private void setupPanelPreferences() {
        setPreferredSize(new Dimension(370, 350));
        setLayout(new BorderLayout());
        setBackground(Color.PINK);
    }

    private void initializeUIComponents() {
        // Initialize playlist model and renderer
        playlistModel = new DefaultListModel<>();
        playlist = new JList<>(playlistModel); 
        playlist.setCellRenderer(new SongCellRenderer());
        
        // Initialize buttons
        openFileButton = createButton("res/img/player/addsong.png");
        playPauseButton = createButton("res/img/player/play.png");
        nextButton = createButton("res/img/player/next.png");
        previousButton = createButton("res/img/player/prev.png");
        switchViewButton = createButton("res/img/player/playlist.png");
        switchViewButtonForPlaylist = createButton("res/img/player/playlist.png");
        deleteSongButton = new JButton("Delete Song");
        deleteAllSongsButton = new JButton("Delete All Songs");

        // Initialize labels
        albumCoverLabel = new JLabel();
        albumCoverLabel.setPreferredSize(new Dimension(220, 200));
        songNameLabel = createLabel("songTitle");
        artistNameLabel = createLabel("artist");

        // Initialize progress bar
        songProgressBar = new JSlider();
        songProgressBar.setPreferredSize(new Dimension(220, 20));
        songProgressBar.setMaximum(100);
        
        timeLabel = createLabel("00:00");
        timeLabel.setPreferredSize(new Dimension(40, 10));
    }

    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text, SwingConstants.CENTER);
        Font labelFont = new Font("Font name", Font.BOLD, 10);
        label.setFont(labelFont);
        label.setPreferredSize(new Dimension(370, 10));
        return label;
    }

    private void setLayouts() {
        setupInfoPanel();
        setupControlsPanel();
        setupProgressBarPanel();
        setupPlaylistPanel();
    }

    private void registerEventListeners() {
        // Event Listeners for Buttons
        openFileButton.addActionListener(e -> openFile());
        playPauseButton.addActionListener(e -> togglePlayPause());
        nextButton.addActionListener(e -> nextMusic());
        previousButton.addActionListener(e -> previousMusic());
        switchViewButton.addActionListener(e -> switchView());
        switchViewButtonForPlaylist.addActionListener(e -> switchView());
        deleteSongButton.addActionListener(e -> deleteSelectedSong());
        deleteAllSongsButton.addActionListener(e -> deleteAllSongs());

        // Event Listener for Playlist Double-Click
        playlist.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    currentSongIndex = playlist.getSelectedIndex();
                    playSong(playlistModel.get(currentSongIndex));
                }
            }
        });
        
        songProgressBar.addChangeListener(e -> {
            if (songProgressBar.getValueIsAdjusting() && songTotalLength > 0) {
                int progress = songProgressBar.getValue();
                songPosition = progress * songTotalLength / 100;
                updatePlaybackTimeLabel();
                seekToPosition(songPosition);
            }
        });

    }

    private void addComponentsToPanel() {
        add(infoPanel, BorderLayout.NORTH);
        add(progressBarPanel, BorderLayout.CENTER);
        add(controlsPanel, BorderLayout.SOUTH);
    }

    private JButton createButton(String imagePath) {
        try {
            BufferedImage img = ImageIO.read(new File(imagePath));
            Image scaledImage = img.getScaledInstance(30, 30, Image.SCALE_SMOOTH);
            return new JButton(new ImageIcon(scaledImage));
        } catch (IOException e) {
            e.printStackTrace();
            return new JButton();
        }
    }

    private void setupInfoPanel() {
        // Setup Song Info Panel
        JPanel songInfoPanel = new JPanel();
        songInfoPanel.setLayout(new BoxLayout(songInfoPanel, BoxLayout.Y_AXIS));
        songInfoPanel.add(Box.createVerticalGlue());
        songNameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        artistNameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        songInfoPanel.add(songNameLabel);
        songInfoPanel.add(artistNameLabel);
        songInfoPanel.add(Box.createVerticalGlue());
        songInfoPanel.setBackground(Color.WHITE);
        


        // Setup Main Info Panel
        infoPanel = new JPanel(new BorderLayout());
        infoPanel.add(new JLabel(""), BorderLayout.WEST);
        infoPanel.add(albumCoverLabel, BorderLayout.CENTER);
        infoPanel.add(songInfoPanel, BorderLayout.SOUTH);
        infoPanel.setPreferredSize(new Dimension(370, 250));
        ((BorderLayout)infoPanel.getLayout()).setHgap(94);
        infoPanel.setBackground(Color.WHITE);
    }

    private void setupControlsPanel() {
        controlsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        controlsPanel.add(openFileButton);
        controlsPanel.add(previousButton);
        controlsPanel.add(playPauseButton);
        controlsPanel.add(nextButton);
        controlsPanel.add(switchViewButton);
        
        controlsPanel.setBackground(Color.WHITE);
        controlsPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
    }

    private void setupProgressBarPanel() {
        progressBarPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        progressBarPanel.add(timeLabel);
        progressBarPanel.add(Box.createHorizontalStrut(200)); // Adding gap
        progressBarPanel.add(songProgressBar);
        progressBarPanel.setBackground(Color.WHITE);
    }

    
    
	private void updateProgressBar() {
	    int progress = (int) (songPosition * 100 / songTotalLength);
	    songProgressBar.setValue(progress);
	    updatePlaybackTimeLabel();
	}


    private void setupPlaylistPanel() {
        // Setup Playlist Panel
        playlistPanel = new JPanel(new BorderLayout());
        playlistPanel.add(playlist, BorderLayout.CENTER);
        JPanel playlistControlsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        playlistControlsPanel.add(switchViewButtonForPlaylist);
        playlistControlsPanel.add(deleteSongButton);
        playlistControlsPanel.add(deleteAllSongsButton);
        playlistPanel.add(playlistControlsPanel, BorderLayout.SOUTH);
    }

    private void setDefaultAlbumCover() {
        setImageToComponent(albumCoverLabel, "res/img/player/defaultcover.png", 200, 200);
    }

    private void setImageToComponent(JComponent component, String imagePath, int width, int height) {
        try {
            BufferedImage img = ImageIO.read(new File(imagePath));
            Image scaledImage = img.getScaledInstance(width, height, Image.SCALE_SMOOTH);
            if (component instanceof JButton) {
                ((JButton) component).setIcon(new ImageIcon(scaledImage));
            } else if (component instanceof JLabel) {
                ((JLabel) component).setIcon(new ImageIcon(scaledImage));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void openFile() {
        // File Chooser Logic
        JFileChooser fileChooser = new JFileChooser();
        FileNameExtensionFilter filter = new FileNameExtensionFilter("MP3 Files", "mp3");
        fileChooser.setFileFilter(filter);
        fileChooser.setMultiSelectionEnabled(true); // Enable multiple selection
        int returnVal = fileChooser.showOpenDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File[] selectedFiles = fileChooser.getSelectedFiles();
            for (File selectedFile : selectedFiles) {
                if (player == null) {
                    playSong(selectedFile);
                    currentSongIndex = 0;
                }
                playlistModel.addElement(selectedFile);
                songFiles.add(selectedFile);
            }
        }
    }


    private void updateTimeLabel() {
        int timeInSeconds = (int) (songPosition / 1000); // 총 밀리초에서 초로 변환
        int minutes = timeInSeconds / 60;
        int seconds = timeInSeconds % 60;
        timeLabel.setText(String.format("%02d:%02d", minutes, seconds));
    }


    private void playSong(File songFile) {
        // Stop the previous player if any
        if (player != null) {
            player.close();
        }

        // Start a new player for the selected song
        try {
            FileInputStream fileInputStream = new FileInputStream(songFile);
            BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);
            songTotalLength = fileInputStream.available(); // Total length of the song
            songPosition = 0; // Starting position
            player = new Player(bufferedInputStream);
            updateSongInfo(songFile);
            new Thread(() -> {
                try {
                    while (player.play(1)) { // Play the song frame by frame
                        songPosition = songTotalLength - fileInputStream.available(); // Current position
                        updateProgressBar(); // Update progress bar
                    }
                    if (player.isComplete() && currentSongIndex < songFiles.size() - 1) {
                        nextMusic();
                    }
                } catch (JavaLayerException | IOException e) {
                    e.printStackTrace();
                }
            }).start();
            updateTimeLabel();
            isPlaying = true;
            setImageToComponent(playPauseButton, "res/img/player/pause.png", 30, 30);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Cannot play the specified file!");
            e.printStackTrace();
        }
    }


    private void updateSongInfo(File songFile) {
        try {
            AudioFile audioFile = AudioFileIO.read(songFile);
            Tag tag = audioFile.getTag();
            String title = tag.getFirst(FieldKey.TITLE);
            String artist = tag.getFirst(FieldKey.ARTIST);
            songNameLabel.setText(title.isEmpty() ? "Unknown Title" : title);
            artistNameLabel.setText(artist.isEmpty() ? "Unknown Artist" : artist);
            List<Artwork> artworkList = tag.getArtworkList();
            if (!artworkList.isEmpty()) {
                byte[] albumImageData = artworkList.get(0).getBinaryData();
                setImageToComponent(albumCoverLabel, albumImageData, 200, 200);
            } else {
                setDefaultAlbumCover();
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error reading tag information!");
            e.printStackTrace();
        }
    }

    private void setImageToComponent(JComponent component, byte[] imageData, int width, int height) {
        Image img = Toolkit.getDefaultToolkit().createImage(imageData);
        Image scaledImage = img.getScaledInstance(width, height, Image.SCALE_SMOOTH);
        if (component instanceof JButton) {
            ((JButton) component).setIcon(new ImageIcon(scaledImage));
        } else if (component instanceof JLabel) {
            ((JLabel) component).setIcon(new ImageIcon(scaledImage));
        }
    }

    private void togglePlayPause() {
        if (player != null) {
            if (isPlaying) {
                player.close();
                isPlaying = false;
                setImageToComponent(playPauseButton, "res/img/player/play.png", 30, 30);
            } else {
                playSong(songFiles.get(currentSongIndex));
            }
        } else {
            JOptionPane.showMessageDialog(null, "No music selected!");
        }
    }

    private void nextMusic() {
        if (currentSongIndex < songFiles.size() - 1) {
            currentSongIndex++;
            playSong(songFiles.get(currentSongIndex));
        }
    }

    private void previousMusic() {
        if (currentSongIndex > 0) {
            currentSongIndex--;
            playSong(songFiles.get(currentSongIndex));
        }
    }

    private void switchView() {
        removeAll();
        if (isPlayingView) {
            add(playlistPanel, BorderLayout.CENTER);
            isPlayingView = false;
        } else {
            add(infoPanel, BorderLayout.NORTH);
            add(progressBarPanel, BorderLayout.CENTER);
            add(controlsPanel, BorderLayout.SOUTH);
            isPlayingView = true;
        }
        revalidate();
        repaint();
    }

    private void deleteSelectedSong() {
        if (!playlist.isSelectionEmpty()) {
            int selectedIndex = playlist.getSelectedIndex();
            playlistModel.remove(selectedIndex);
            songFiles.remove(selectedIndex);
            if (selectedIndex == currentSongIndex) {
                player.close();
                setDefaultAlbumCover();
            }
        }
    }

    private void deleteAllSongs() {
        playlistModel.clear();
        songFiles.clear();
        if (player != null) {
            player.close();
            setDefaultAlbumCover();
        }
    }
    
    private void seekToPosition(long position) {
        if (player != null) {
            player.close();
            try {
                FileInputStream fileInputStream = new FileInputStream(songFiles.get(currentSongIndex));
                BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);
                fileInputStream.skip(position);
                player = new Player(bufferedInputStream);
                new Thread(() -> {
                    try {
                        while (player.play(1)) {
                            updateProgressBar(); // Update progress bar
                        }
                    } catch (JavaLayerException e) {
                        e.printStackTrace();
                    }
                }).start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    
    
    


    private void updatePlaybackTimeLabel() {
        int totalSeconds = (int) (songPosition * totalSongDurationInSeconds / songTotalLength);
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        timeLabel.setText(String.format("%02d:%02d", minutes, seconds));
    }



    // Custom Renderer for JList
    private class SongCellRenderer extends DefaultListCellRenderer {
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            File file = (File) value;
            setText(file.getName());
            return this;
        }
    }
}

*/
