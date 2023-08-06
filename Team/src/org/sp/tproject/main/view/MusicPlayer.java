package org.sp.tproject.main.view;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.SwingConstants;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.datatype.Artwork;

import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.Player;

public class MusicPlayer extends JPanel {
    // Declare component variables
    private JLabel songNameLabel, artistNameLabel, albumCoverLabel;
    private JSlider songProgressBar;
    private JButton playPauseButton, nextButton, previousButton, openFileButton, switchViewButton, switchViewButtonForPlaylist;
    private JFileChooser fileChooser;
    private JPanel controlsPanel, playlistPanel, infoPanel, progressBarPanel;
    private JList<String> playlist;

    private List<File> songFiles = new ArrayList<>();
    private Player player;
    private int currentSongIndex = 0;

    private boolean isPlayingView = true;
    private boolean isPlaying = false; // new variable to track play state
    
    private long pauseLocation;
    private long totalLength;
    private int pausedFrame;



    public MusicPlayer() {
        // Initialize GUI components
        initComponents();
    }

    private void initComponents() {
        setPreferredSize(new Dimension(370, 350));
        setLayout(new BorderLayout());

        // Initialize Buttons with icons
        openFileButton = createButton("res/img/player/addsong.png");
        playPauseButton = createButton("res/img/player/play.png");
        nextButton = createButton("res/img/player/next.png");
        previousButton = createButton("res/img/player/prev.png");
        switchViewButton = createButton("res/img/player/playlist.png");
        switchViewButtonForPlaylist = createButton("res/img/player/playlist.png");

        albumCoverLabel = new JLabel();
        albumCoverLabel.setPreferredSize(new Dimension(220, 200));

        songNameLabel = new JLabel("songTitle", SwingConstants.CENTER);
        artistNameLabel = new JLabel("artist", SwingConstants.CENTER);

        songProgressBar = new JSlider();
        songProgressBar.setPreferredSize(new Dimension(220, 10));

        Font labelFont = new Font("Font name", Font.BOLD, 10);
        songNameLabel.setFont(labelFont);
        artistNameLabel.setFont(labelFont);
        
        songNameLabel.setPreferredSize(new Dimension(370, 10));
        artistNameLabel.setPreferredSize(new Dimension(370, 10));

        setupInfoPanel();
        setupControlsPanel();
        setupProgressBarPanel();
        setupPlaylistPanel();
        setDefaultAlbumCover();

        add(progressBarPanel, BorderLayout.CENTER); // Move progress bar to CENTER
        add(infoPanel, BorderLayout.NORTH); // Move info panel to NORTH
        add(controlsPanel, BorderLayout.SOUTH);

        // Setup Event listeners
        openFileButton.addActionListener(e -> openFile());
        playPauseButton.addActionListener(e -> togglePlayPause());
        nextButton.addActionListener(e -> nextMusic());
        previousButton.addActionListener(e -> previousMusic());
        switchViewButton.addActionListener(e -> switchView());
        switchViewButtonForPlaylist.addActionListener(e -> switchView());
    }

    private JButton createButton(String imagePath) {
        BufferedImage img = null;
        try {
            img = ImageIO.read(new File(imagePath));
        } catch (IOException e) {
            e.printStackTrace();
        }
        Image scaledImage = img.getScaledInstance(30, 30, Image.SCALE_SMOOTH);
        return new JButton(new ImageIcon(scaledImage));
    }

    private void setupInfoPanel() {
        // Create a panel with BoxLayout for song name and artist name
        JPanel songInfoPanel = new JPanel();
        songInfoPanel.setLayout(new BoxLayout(songInfoPanel, BoxLayout.Y_AXIS));
        songInfoPanel.add(Box.createVerticalGlue()); // Center alignment
        songNameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        artistNameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        songInfoPanel.add(songNameLabel);
        songInfoPanel.add(artistNameLabel);
        songInfoPanel.add(Box.createVerticalGlue());

        // Use a BorderLayout for the overall info panel
        infoPanel = new JPanel(new BorderLayout());
        infoPanel.add(new JLabel(""), BorderLayout.WEST); // 왼쪽에 빈 레이블을 추가합니다.
        infoPanel.add(albumCoverLabel, BorderLayout.CENTER); // Move album cover to CENTER
        infoPanel.add(songInfoPanel, BorderLayout.SOUTH);
        infoPanel.setPreferredSize(new Dimension(370, 250));

        // 왼쪽 여백을 75 픽셀로 설정합니다.
        ((BorderLayout)infoPanel.getLayout()).setHgap(97); // Horizontal gap
    }

    

    private void setupControlsPanel() {        
        controlsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER)); // FlowLayout을 사용하고 중앙 정렬합니다.
        controlsPanel.add(openFileButton);
        controlsPanel.add(previousButton);
        controlsPanel.add(playPauseButton);
        controlsPanel.add(nextButton);
        controlsPanel.add(switchViewButton);
        controlsPanel.setPreferredSize(new Dimension(370, 70));
    }

    private void setupProgressBarPanel() {
        progressBarPanel = new JPanel(new FlowLayout(FlowLayout.CENTER)); // FlowLayout을 사용하고 중앙 정렬합니다.
        progressBarPanel.add(songProgressBar);
        progressBarPanel.setPreferredSize(new Dimension(370, 20));
    }

    private void setupPlaylistPanel() {
        playlistPanel = new JPanel();
        playlistPanel.setLayout(new BoxLayout(playlistPanel, BoxLayout.PAGE_AXIS));
        playlist = new JList<>();
        playlistPanel.add(new JScrollPane(playlist));
        playlistPanel.add(switchViewButtonForPlaylist);
    }

    private void setDefaultAlbumCover() {
        // Set a default album cover image when there's no music playing
        String imagePath = "res/img/player/defaultcover.png";
        setImageToLabel(albumCoverLabel, imagePath);
    }

    private void openFile() {
        if (fileChooser == null) {
            fileChooser = new JFileChooser();
            fileChooser.setMultiSelectionEnabled(true);
        }
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File[] files = fileChooser.getSelectedFiles();
            for (File file : files) {
                if (file.getName().endsWith(".mp3")) {
                    songFiles.add(file);
                }
            }
            if (!songFiles.isEmpty()) {
                playSong(songFiles.get(currentSongIndex));
            }
        }
    }

    private void playSong(File songFile) {
        try {
            // Use JAudioTagger to read metadata
            AudioFile audioFile = AudioFileIO.read(songFile);
            Tag tag = audioFile.getTag();
            String artist = tag.getFirst(FieldKey.ARTIST);
            String album = tag.getFirst(FieldKey.ALBUM);
            String title = tag.getFirst(FieldKey.TITLE);

            // Update song information
            songNameLabel.setText(title);
            artistNameLabel.setText(artist);

            // Update album cover if available
            Artwork artwork = tag.getFirstArtwork();
            if (artwork != null) {
                byte[] albumImageData = artwork.getBinaryData();
                ImageIcon albumCover = new ImageIcon(albumImageData);
                Image scaledImage = albumCover.getImage().getScaledInstance(200, 200, Image.SCALE_SMOOTH);
                albumCoverLabel.setIcon(new ImageIcon(scaledImage));
            } else {
                setDefaultAlbumCover();
            }

            // Stop current player if a song is already playing
            if (player != null) {
                player.close();
            }

            // Play the selected song using JLayer
            new Thread(() -> {
                try {
                    FileInputStream fis1 = new FileInputStream(songFile);
                    BufferedInputStream bis = new BufferedInputStream(fis1);
                    player = new Player(bis);
                    player.play(); // 항상 처음부터 재생
                } catch (JavaLayerException e) {
                    e.printStackTrace();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }).start();



            isPlaying = true;
            setImageToButton(playPauseButton, "res/img/player/pause.png");
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Unable to play the selected file.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }




    private void togglePlayPause() {
        if (isPlaying) {
            // Pause the song
            if (player != null) {
                pausedFrame = player.getPosition(); // Store the current frame
                player.close();
            }
            setImageToButton(playPauseButton, "res/img/player/play.png");
            isPlaying = false;
        } else {
            // Resume the song
            playSong(songFiles.get(currentSongIndex)); // Just call playSong method to handle resuming
            setImageToButton(playPauseButton, "res/img/player/pause.png");
            isPlaying = true;
        }
    }


    private void nextMusic() {
        if (songFiles.size() > 0) {
            // Move to the next track
            currentSongIndex = (currentSongIndex + 1) % songFiles.size();
            playSong(songFiles.get(currentSongIndex));
        }
        isPlaying = true;
        setImageToButton(playPauseButton, "res/img/player/pause.png");
    }

    private void previousMusic() {
        if (songFiles.size() > 0) {
            // Move to the previous track
            currentSongIndex = (currentSongIndex - 1 + songFiles.size()) % songFiles.size();
            playSong(songFiles.get(currentSongIndex));
        }
        isPlaying = true;
        setImageToButton(playPauseButton, "res/img/player/pause.png");
    }


    private void switchView() {
        // Switch between playing view and playlist view
        if (isPlayingView) {
            remove(progressBarPanel);
            remove(infoPanel);
            remove(controlsPanel);
            add(playlistPanel);
        } else {
            remove(playlistPanel);
            add(progressBarPanel, BorderLayout.NORTH);
            add(infoPanel, BorderLayout.CENTER);
            add(controlsPanel, BorderLayout.SOUTH);
        }
        isPlayingView = !isPlayingView;
        revalidate();
        repaint();
    }

    private void setImageToLabel(JLabel label, String imagePath) {
        // Set image to the specified label
        try {
            BufferedImage img = ImageIO.read(new File("res/img/player/defaultcover.png"));
            Image scaledImage = img.getScaledInstance(200, 200, Image.SCALE_SMOOTH);
            label.setIcon(new ImageIcon(scaledImage));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setImageToButton(JButton button, String imagePath) {
        // Set image to the specified button
        try {
            BufferedImage img = ImageIO.read(new File(imagePath));
            Image scaledImage = img.getScaledInstance(30, 30, Image.SCALE_SMOOTH);
            button.setIcon(new ImageIcon(scaledImage));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    
}
