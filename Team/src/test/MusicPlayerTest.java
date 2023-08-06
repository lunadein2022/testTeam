package test;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.datatype.Artwork;

import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.Player;

public class MusicPlayerTest extends JPanel {
    JLabel songNameLabel, artistNameLabel, albumCoverLabel;
    JSlider songProgressBar;
    JButton playButton, pauseButton, nextButton, previousButton, openFileButton;
    JFileChooser fileChooser;

    List<File> songFiles = new ArrayList<>();
    Player player;
    int currentSongIndex = 0;

    public MusicPlayerTest() {
        // Previous layout and GUI setup code
    	openFileButton = new JButton("Open File");
    	playButton = new JButton("PLAY");
    	pauseButton = new JButton("PAUSE");
    	nextButton = new JButton("NEXT");
    	previousButton = new JButton("PREV");
    	
        setLayout(new BorderLayout());
        albumCoverLabel.setPreferredSize(new Dimension(300, 300));


        // Create a panel for the controls and add them to it
        JPanel controlsPanel = new JPanel();
        controlsPanel.setLayout(new BoxLayout(controlsPanel, BoxLayout.PAGE_AXIS));
        controlsPanel.add(openFileButton);
        controlsPanel.add(Box.createRigidArea(new Dimension(50,0)));
        controlsPanel.add(previousButton);
        controlsPanel.add(Box.createRigidArea(new Dimension(20,0)));
        controlsPanel.add(playButton);
        controlsPanel.add(Box.createRigidArea(new Dimension(10,0)));
        controlsPanel.add(pauseButton);
        controlsPanel.add(Box.createRigidArea(new Dimension(10,0)));
        controlsPanel.add(nextButton);

        // Create a panel for the info and add them to it        
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.PAGE_AXIS));
        infoPanel.add(albumCoverLabel);
        infoPanel.add(Box.createRigidArea(new Dimension(0,10)));
        infoPanel.add(songNameLabel);
        infoPanel.add(Box.createRigidArea(new Dimension(0,10)));
        infoPanel.add(artistNameLabel);
        infoPanel.add(Box.createRigidArea(new Dimension(0,10)));
        infoPanel.add(songProgressBar);

        // Add components to the panel
        add(infoPanel, BorderLayout.CENTER);
        add(controlsPanel, BorderLayout.EAST);
    	
        openFileButton.addActionListener(e -> openFile());
        playButton.addActionListener(e -> playMusic());
        pauseButton.addActionListener(e -> pauseMusic());
        nextButton.addActionListener(e -> nextMusic());
        previousButton.addActionListener(e -> previousMusic());
    }

    public void openFile() {
        fileChooser = new JFileChooser();
        fileChooser.setMultiSelectionEnabled(true);
        int returnValue = fileChooser.showOpenDialog(null);
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            File[] files = fileChooser.getSelectedFiles();
            for (File file : files) {
                songFiles.add(file);
            }
            currentSongIndex = 0;
            playMusic();
        }
    }

    public void playMusic() {
        try {
            File song = songFiles.get(currentSongIndex);
            FileInputStream fis = new FileInputStream(song);
            BufferedInputStream bis = new BufferedInputStream(fis);
            player = new Player(bis);
            Thread playerThread = new Thread(() -> {
                try {
                    player.play();
                    if (player.isComplete() && currentSongIndex < songFiles.size()) {
                        nextMusic();
                    }
                } catch (JavaLayerException e) {
                    e.printStackTrace();
                }
            });
            playerThread.start();
            updateMetadata(song);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public void pauseMusic() {
        if (player != null) {
            player.close();
            player = null;
        }
    }

    public void nextMusic() {
        if (player != null) {
            player.close();
            player = null;
        }
        currentSongIndex = (currentSongIndex + 1) % songFiles.size();
        playMusic();
    }

    public void previousMusic() {
        if (player != null) {
            player.close();
            player = null;
        }
        currentSongIndex = (currentSongIndex - 1 < 0) ? songFiles.size() - 1 : currentSongIndex - 1;
        playMusic();
    }

    public void updateMetadata(File song) {
        try {
            AudioFile audioFile = AudioFileIO.read(song);
            Tag tag = audioFile.getTag();
            if (tag != null) {
                songNameLabel.setText(tag.getFirst(FieldKey.TITLE));
                artistNameLabel.setText(tag.getFirst(FieldKey.ARTIST));

                Artwork artwork = tag.getFirstArtwork();
                if (artwork != null) {
                    byte[] imageData = artwork.getBinaryData();
                    BufferedImage img = ImageIO.read(new ByteArrayInputStream(imageData));
                    albumCoverLabel.setIcon(new ImageIcon(img));
                }
            }
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}

