/** Project: Solo Lab 7 Assignment
 * Purpose Details: Create 2D Space Game using constructors, methods, and local methods under
 * Java Swing by controlling ship, avoid obstacles, shoot laser, collect powerup, add sound
 * countdown timer, animated sprites, and level challenge.
 * Course: IST 242
 * Author: Xue Thao
 * Date Developed: 04-15-2025
 * Last Date Changed:04-27-2025
 * Rev:23

 */

import javax.imageio.ImageIO;
import javax.sound.sampled.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

// Laser https://freesound.org/s/96556/
// Thud for Astro https://freesound.org/s/527525/
// Ploop Ship https://freesound.org/people/nfrae/sounds/625813/
// JingleLose https://freesound.org/people/LittleRobotSoundFactory/sounds/270403/
// Game Music Battle https://freesound.org/people/josefpres/sounds/645163/
// PowerUP https://freesound.org/people/ReincarnatedEchoes/sounds/644306/


public class SpaceGame extends JFrame implements KeyListener {
    //constants for window size and game mechanics
    private static final int WIDTH = 500; // width of game window
    private static final int HEIGHT = 500; // height of game window
    private static final int PLAYER_WIDTH = 64; // width of player ship
    private static final int PLAYER_HEIGHT = 64; // height of player ship
    private static final int OBSTACLE_WIDTH = 30; // width of astroice
    private static final int OBSTACLE_HEIGHT = 30; // height of astroice
    private static final int PROJECTILE_WIDTH = 5; // width of laser
    private static final int PROJECTILE_HEIGHT = 10; //height of laser
    private static final int PLAYER_SPEED = 10; // ship movement speed
    private static int OBSTACLE_SPEED = 2; // astroice movement speed
    private static final int PROJECTILE_SPEED = 10; // laser speed
    private static final int POWERUP_WIDTH = 30; //health power up width
    private static final int POWERUP_HEIGHT = 30; // health power up height
    private static final int POWER_SPEED = 2; // health power up speed

    private int score = 0; // starting player score
    private boolean isGameOver; // check if game over
    private int playerX, playerY; // ship coordinates
    //private int projectileX, projectileY; //old projectile
    //private boolean isProjectileVisible; // old projectile
    //private boolean isFiring; // old projectile

    // GUI components
    private final JPanel gamePanel; // renders game panel
    private final JLabel scoreLabel; // Label frame score
    private final JLabel shipLabel; // Label frame ship image/GIF
    private final JLabel healthLabel; // Label frame ship Hit Points
    private final JLabel levelLabel; // Label frame game level

    // Game objects
    private final List<Point> obstacles; // Array astroice
    private final List<Point> projectiles; // Array laser
    private List<Point> stars; // Array Background Random Stars
    private final List<Point> healthPowerUps = new ArrayList<>(); // Array Health PowerUp

    //Sprites and images
    //private BufferedImage shipImage; //old ship.png
    private BufferedImage spriteSheet; //AstroIce.png
    private BufferedImage spriteSheet1; // PowerUP.png

    //Audio Clip
    //private Clip laserClip; // variable for laser but sounds cutoff to quickly no overlap
    private Clip battleClip, loseClip; // background music and game over clip

    //Player's ship mechanics
    private boolean shieldActive; // Shield active or not
    private final int shieldDuration; // shield duration in milliseconds 5000 ms = 5 s
    private long shieldStartTime; // timestamp activated shield

    // HP
    private final int MAX_HEALTH = 3;  // Maximum health is 3
    private int shipHealth = MAX_HEALTH;  // Starts at full health

    // Countdown Timer
    private int timeLeft = 60; // 60 seconds countdown
    private Timer countdownTimer; // Decrement timer countdown

    // Game Level
    private int gameLevel = 1; // Current level

    // additional movement, addon to keypressed method
    //private final boolean moveLeft = false;
    //private final boolean moveRight = false;
    //private boolean moveUp = false; //ship movements up, down, left, right
    //private boolean moveDown = false;

    /**
    Constructor for Space Game. Initialize the game window, GUI components, loads resources
     * (sprites and sounds), and starts the game update and countdown timers.
     */
    public SpaceGame() {
        setTitle("Space Game");
        setSize(WIDTH, HEIGHT);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        gamePanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                draw(g);
            }
        };

        scoreLabel = new JLabel("Score: 0");
        scoreLabel.setForeground(Color.BLUE); // add score text color
        scoreLabel.setBounds(WIDTH / 2 - 50, 10, 100, 30); // set bounds
        scoreLabel.setFont(new Font("Arial", Font.BOLD, 16)); //set font
        gamePanel.add(scoreLabel);

        healthLabel = new JLabel("HP: " + shipHealth);
        healthLabel.setBounds(10, 10, 100, 20); // Position: top-left
        healthLabel.setForeground(Color.GREEN);
        healthLabel.setFont(new Font("Arial", Font.BOLD, 16));
        gamePanel.add(healthLabel);

        add(gamePanel);
        gamePanel.setFocusable(true);
        gamePanel.addKeyListener(this);
        gamePanel.setLayout(null); //null to manage layout

        playerX = WIDTH / 2 - PLAYER_WIDTH / 2;
        playerY = HEIGHT - PLAYER_HEIGHT - 50;
        isGameOver = false;
        //isFiring = false;
        obstacles = new ArrayList<>();
        projectiles = new ArrayList<>();
        shieldActive = false;
        shieldDuration = 5000;
        shieldStartTime = 100;

        stars = generateStars(); // add stars background

        ImageIcon shipIcon = new ImageIcon("ship.gif"); // Replace with GIF
        shipLabel = new JLabel(shipIcon);
        //shipLabel.setBounds(playerX, playerY, PLAYER_WIDTH, PLAYER_HEIGHT); // Initial position
        gamePanel.add(shipLabel);

        levelLabel = new JLabel("Level: " + gameLevel);
        levelLabel.setFont(new Font("Arial", Font.BOLD, 16));
        levelLabel.setForeground(Color.CYAN); //color of User Interface
        levelLabel.setBounds(10,40,100,30); //position text
        gamePanel.add(levelLabel);

        countdownTimer = new Timer(1000, e -> {
            if (timeLeft > 0) {
                timeLeft--;
            } else {
                isGameOver = true;
                countdownTimer.stop();
                playGameOverSound();
                playBattleSound(null);
                gamePanel.repaint();
            }
        });
        countdownTimer.start();

        // update ships position
        // Repaint the game screen
        Timer timer = new Timer(20, e -> {
            if (!isGameOver) {
                update();
                shipLabel.setBounds(playerX, playerY, PLAYER_WIDTH, PLAYER_HEIGHT); // update ships position
                gamePanel.repaint(); // Repaint the game screen
            }
        });
        timer.start();

        try {
            //shipImage = ImageIO.read(new File("Ship.png"));  //add the ship image png
            spriteSheet = ImageIO.read(new File("AstroIce.png"));
            spriteSheet1 = ImageIO.read(new File("PowerUP.png"));

            //Single Laser but sounds cuts off too quick, use loadSound method
            //AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(new File("SingleLaser.wav").getAbsoluteFile());
            //laserClip = AudioSystem.getClip();
            //laserClip.open(audioInputStream);

            //Game Over
            AudioInputStream loseClipAudio = AudioSystem.getAudioInputStream(new File("JingleLose.wav").getAbsoluteFile());
            loseClip = AudioSystem.getClip();  // Use the class variable
            loseClip.open(loseClipAudio);

            playBattleSound("SpaceMix-120-bpm.wav");

        } catch (LineUnavailableException | UnsupportedAudioFileException | IOException ex) {
            //noinspection CallToPrintStackTrace
            ex.printStackTrace();
        }
    }

    /**
     * Draws game elements: ship, obstacles, projectiles, powerup, stars and HUG elements.
     * @param g the Graphics object used for rendering game
     */
    private void draw(Graphics g) {
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, WIDTH, HEIGHT);

        //player ship
        //g.drawImage(shipImage, playerX - 7, playerY - 2, null); //ship position
        //g.setColor(Color.BLUE); //old blue ship
        //g.fillRect(playerX, playerY, PLAYER_WIDTH, PLAYER_HEIGHT); //Hitbox

        //projectiles firing
        g.setColor(Color.GREEN);
        for (Point p : projectiles) {
            g.fillRect(p.x, p.y, PROJECTILE_WIDTH, PROJECTILE_HEIGHT);
        }

        // old obstacles hitbox
        //g.setColor(Color.RED);
        //for (Point obstacle : obstacles) {
        //    g.fillRect(obstacle.x, obstacle.y, OBSTACLE_WIDTH, OBSTACLE_HEIGHT);
        //}

        //new obstacles randomly generated sprite
        for (Point obstacle : obstacles) {
            if (spriteSheet != null) {
                //randomly slect a sprite index (0-3)
                Random random = new Random();
                int spriteIndex = random.nextInt(4);

                //calculate the x and y coordinates of the selected sprite on the sprite sheet
                int spriteWidth = 64;
                int spriteX = spriteIndex * spriteWidth;
                int spriteY = 0; //assuming all sprites are in the first time

                //draw the selected sprite onto the canvas
                int spriteHeight = 64;
                g.drawImage(spriteSheet.getSubimage(spriteX, spriteY, spriteWidth, spriteHeight), obstacle.x, obstacle.y, OBSTACLE_WIDTH, OBSTACLE_HEIGHT, null);
            }
        }

        //Draw Stars
        g.setColor(generateRandomColor());
        //g.setColor(Color.YELLOW);
        for (Point star : stars) {
            g.fillOval(star.x, star.y, 2, 2);
        }

        //draw shield
        if (isShieldActive()) {
            g.setColor(new Color(0, 255, 255, 100));//semi-transparent cyan
            g.fillOval(playerX - 4, playerY - 2, 70, 60);
            g.setFont(new Font("Arial", Font.BOLD, 14));//font of shield skill
            int secondsLeft = (int) ((shieldDuration - (System.currentTimeMillis() - shieldStartTime)) / 1000); //shield cooldown
            g.drawString("Shield: " + secondsLeft + "s", 10, 80);
        }

        g.setColor(Color.ORANGE); // Or use an image
        for (Point p : healthPowerUps) {
            g.fillOval(p.x, p.y, POWERUP_WIDTH, POWERUP_HEIGHT);
        }

        //new powerUPs randomly generated sprite
        for (Point healthPowerUps : healthPowerUps) {
            if (spriteSheet1 != null) {
                //randomly select a sprite index (0-3)
                Random random = new Random();
                int spriteIndex = random.nextInt(4);

                //calculate the x and y coordinates of the selected sprite on the sprite sheet
                int spriteWidth = 64;
                int spriteX = spriteIndex * spriteWidth;
                int spriteY = 0; //assuming all sprites are in the first time

                //draw the selected sprite onto the canvas
                int spriteHeight = 64;
                g.drawImage(spriteSheet1.getSubimage(spriteX, spriteY, spriteWidth, spriteHeight), healthPowerUps.x, healthPowerUps.y, POWERUP_WIDTH, POWERUP_HEIGHT, null);
            }
        }

        //Countdown Timer
        g.setColor(Color.YELLOW);
        g.setFont(new Font("Arial", Font.BOLD, 16));
        g.drawString("Time Left: " + timeLeft + "s", WIDTH - 130, 20); // adjust position as needed

        // Display control hints
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.PLAIN, 14));
        g.drawString("←/→ to Move | F to Shoot | S for Shield | ESC to Reset", 70, 460);

        if (isGameOver) {
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 32));
            g.drawString("PILOT FLOATED!", WIDTH / 2 - 120, HEIGHT / 2);
        }

    }

    /**
     * Updates all game logic: movement, collision, projectile, obstalce spawn, powerup spawn, shield,
     * score updates, and level progression.
     */
    private void update() {
        // Move obstacles
        for (int i = 0; i < obstacles.size(); i++) {
            obstacles.get(i).y += OBSTACLE_SPEED;
            if (obstacles.get(i).y > HEIGHT) {
                obstacles.remove(i);
                i--;
            }
        }
        // Generate new obstacles
        if (Math.random() < 0.05) {
            int obstacleX = (int) (Math.random() * (WIDTH - OBSTACLE_WIDTH));
            obstacles.add(new Point(obstacleX, 0));
        }
        //generate stars and move randomly
        if (Math.random() < 0.1) {
            stars = generateStars();
        }

        // Move projectiles
        for (int i = 0; i < projectiles.size(); i++) {
            Point p = projectiles.get(i);
            p.y -= PROJECTILE_SPEED;
            if (p.y < 0) {
                projectiles.remove(i);
                i--;
            }
        }

        // Check collision with player
        Rectangle playerRect = new Rectangle(playerX, playerY, PLAYER_WIDTH, PLAYER_HEIGHT);
        for (Point obstacle : obstacles) {
            Rectangle obstacleRect = new Rectangle(obstacle.x, obstacle.y, OBSTACLE_WIDTH, OBSTACLE_HEIGHT);
            if (playerRect.intersects(obstacleRect) && !isShieldActive()) {
                playCollisionShipSound();
                shipHealth -= 1; //reduce health (adjust value as needed)
                healthLabel.setText("HP: " + shipHealth);
                obstacles.remove(obstacle); // Remove the obstacle on collision

                if (shipHealth <= 0) {
                    isGameOver = true;
                    playGameOverSound();
                    playBattleSound(null);} // stop battle music
                break;
            }
        }
        if (shipHealth <= 0) {
            playGameOverSound();
            isGameOver = true;
        }

        //Health PowerUps
        if (Math.random() < 0.002) { // Adjust probability as needed
            int powerUpX = (int) (Math.random() * (WIDTH - POWERUP_WIDTH));
            healthPowerUps.add(new Point(powerUpX, 0));
        }
        for (int i = 0; i < healthPowerUps.size(); i++) {
            healthPowerUps.get(i).y += POWER_SPEED; //
            if (healthPowerUps.get(i).y > HEIGHT) {
                healthPowerUps.remove(i);
                i--;
            }
        }

        // Check collision with projectiles
        Iterator<Point> projectileIterator = projectiles.iterator();
        while (projectileIterator.hasNext()) {
            Point proj = projectileIterator.next();
            Rectangle projectileRect = new Rectangle(proj.x, proj.y, PROJECTILE_WIDTH, PROJECTILE_HEIGHT);
            for (int i = 0; i < obstacles.size(); i++) {
                Rectangle obstacleRect = new Rectangle(obstacles.get(i).x, obstacles.get(i).y, OBSTACLE_WIDTH, OBSTACLE_HEIGHT);
                if (projectileRect.intersects(obstacleRect)) {
                    obstacles.remove(i);
                    projectileIterator.remove();
                    score += 10;
                    playCollisionSound();
                    break;
                }
            }
        }

        //collision with health powerups
        for (int i = 0; i < healthPowerUps.size(); i++) {
            Rectangle powerUpRect = new Rectangle(
                    healthPowerUps.get(i).x,
                    healthPowerUps.get(i).y,
                    POWERUP_WIDTH,
                    POWERUP_HEIGHT
            );

            if (playerRect.intersects(powerUpRect)) {
                if (shipHealth < MAX_HEALTH) {
                    shipHealth++; // Increase health (max capped)
                    healthLabel.setText("HP: " + shipHealth);
                    playPowerUPSound();
                }
                healthPowerUps.remove(i); // Remove the collected power-up
                i--;
                playPowerUPSound();
            }
        }

        scoreLabel.setText("Score: " + score);
        checkLevel();
    }

    /**
     * Ship movements inout left/right, projectile, and shiled activation.
     * @param e the event to be processed
     */
    @Override
    public void keyPressed(KeyEvent e) {
        int keyCode = e.getKeyCode();
        if (keyCode == KeyEvent.VK_LEFT && playerX > 0) {
            playerX -= PLAYER_SPEED;
        } if (keyCode == KeyEvent.VK_RIGHT && playerX < WIDTH - PLAYER_WIDTH) {
            playerX += PLAYER_SPEED;
        } else if (keyCode == KeyEvent.VK_F) {
            int projectileX = playerX + PLAYER_WIDTH / 2 - PROJECTILE_WIDTH / 2;
            int projectileY = playerY;
            projectiles.add(new Point(projectileX, projectileY));
            playLaserSound();
        } else if (keyCode == KeyEvent.VK_S) {
            activateShield();}
        else if ( keyCode == KeyEvent.VK_D) {
            deactivateShield();
        }
    }

    /**
     * No method used for key type
     * @param e the event to be processed
     */
    @Override
    public void keyTyped(KeyEvent e) {
    }

    /**
     * Resets game when key released.
     * @param e the event to be processed
     */
    @Override
    public void keyReleased(KeyEvent e) {
        int keyCode = e.getKeyCode();
        if (keyCode == KeyEvent.VK_ESCAPE) {
            reset();
        }
    }

    /**
     * Generates random color for background stars.
     * @return a randomly generated color
     */
    public static Color generateRandomColor() {
        Random rand = new Random();
        int r = rand.nextInt(256); // Red component (0-255)
        int g = rand.nextInt(256); // Green component (0-255)
        int b = rand.nextInt(256); // Blue component (0-255)
        return new Color(r, g, b);
    }

    /**
     * Generates list of random points of background stars
     * @return list of star locations
     */
    private List<Point> generateStars() {
        List<Point> starsList = new ArrayList<>();
        Random random = new Random();
        for (int i = 0; i < 200; i++) {
            int x = random.nextInt(WIDTH);
            int y = random.nextInt(HEIGHT);
            starsList.add(new Point(x, y));
        }
        return starsList;
    }

    /**
     * Loads sound file and return as clip
     * @param fileName the name of the wav file
     * @return clip object containing loaded sound
     */
   private Clip loadSound(String fileName) {
       try {
           AudioInputStream audioIn = AudioSystem.getAudioInputStream(new File(fileName));
           Clip newClip = AudioSystem.getClip();
           newClip.open(audioIn);
           return newClip;}

           catch (Exception e) {
               //noinspection CallToPrintStackTrace
               e.printStackTrace();
               return null;
           }
       }

    /*public void playLaserSound() { // older laser method ends and repeat with no overlap
        if (laserClip != null) {
            laserClip.setFramePosition(0); // rewind to the beginning
            laserClip.start(); // Play the sound
        }
    }*/

    /**
     * Laser fire projectile clip
     */
    public void playLaserSound() {
        try (AudioInputStream audioIn = AudioSystem.getAudioInputStream(new File("SingleLaser.wav"))) {
            Clip clip = AudioSystem.getClip();
            clip.open(audioIn); // This can throw IOException or LineUnavailableException
            clip.start(); // Starts playing the sound
        } catch (LineUnavailableException | UnsupportedAudioFileException | IOException ex) {
            //noinspection CallToPrintStackTrace
            ex.printStackTrace();
        }
    }

    /**
     * Collision impact with laser and astroice clip
     */
    @SuppressWarnings("CallToPrintStackTrace")
    public void playCollisionSound() {
        Clip impactClip = loadSound("Impact.wav");
        if (impactClip != null) {
            try {
                impactClip.start();
                impactClip.addLineListener(event -> {
                    if (event.getType() == LineEvent.Type.STOP) {
                        impactClip.close();  // Close the clip once it finishes playing
                    }
                });
            } catch (Exception e) {
                e.printStackTrace(); // Handles exception
            }
        }
    }

    /**
     * Collision impact ship and astroice clip
     */
    public void playCollisionShipSound() {
        Clip impactClipShip = loadSound("ShipImpact.wav");
        if (impactClipShip != null) {
            impactClipShip.start();
            impactClipShip.addLineListener(event -> {
                if (event.getType() == LineEvent.Type.STOP) {
                    impactClipShip.close();  // Close the clip once it finishes playing
                }
            });
        }
    }

    /**
     * Pick up Powerup sound
     */
    public void playPowerUPSound() {
        Clip powerupClip = loadSound("PowerUP.wav");
        if (powerupClip != null) {
            powerupClip.start();
            powerupClip.addLineListener(event -> {
                if (event.getType() ==  LineEvent.Type.STOP) {
                powerupClip.close(); // close the clip once it finishes playing
                }
            });
        }
    }

    /**
     * Game Over sound
     */
    public void playGameOverSound() {
        if (loseClip != null) {
            if (loseClip.isRunning()) {
                loseClip.stop();
            }
            loseClip.setFramePosition(0);
            loseClip.start();
        }
    }

    /**
     * Background music during gameplay. If file path is null, stops music
     * @param path the path to background wav file
     */
    @SuppressWarnings("CallToPrintStackTrace")
    public void playBattleSound(String path) {
        if (battleClip != null && battleClip.isRunning()) {
            battleClip.stop();  // Stop any currently running music
            battleClip.close(); // Close it
            battleClip = null;  // Reset the clip
        }

        if (path != null && !path.isEmpty()) { // Only play the music if the path is valid
            try {
                AudioInputStream in = AudioSystem.getAudioInputStream(new File(path)); // Load the sound file
                battleClip = AudioSystem.getClip();  // Get the Clip object
                battleClip.open(in);  // Open the audio stream
                battleClip.loop(Clip.LOOP_CONTINUOUSLY); // Loop the music continuously
                battleClip.start();  // Start the music
            } catch (Exception e) {
                e.printStackTrace();  // Handle exceptions
            }
        }
    }

    /**
     * resets game stat to initial
     */
    private void reset() {
        playerX = WIDTH / 2 - PLAYER_WIDTH / 2;
        playerY = HEIGHT - PLAYER_HEIGHT - 50;
        obstacles.clear();
        OBSTACLE_SPEED = 2;
        projectiles.clear();
        healthPowerUps.clear();
        score = 0;
        isGameOver = false;
        shieldActive = false;
        stars = generateStars();
        shipHealth = MAX_HEALTH;
        healthLabel.setText("HP: " + shipHealth);
        gameLevel = 1;
        levelLabel.setText("Level: " + gameLevel);
        timeLeft = 60; // Reset the time
        if (countdownTimer != null) {
            countdownTimer.restart();  // Restart the existing timer
        }
        // Stop game over sound then reset
        if (loseClip != null && loseClip.isRunning()) {
            loseClip.stop();  // Stop the game over sound if it's running
            loseClip.setFramePosition(0);  // Reset the sound to the beginning
        }
        playBattleSound("SpaceMix-120-bpm.wav"); //reset to play battle music
}

    /**
     * Initialize Shield start as false
     */
    private void deactivateShield(){
        shieldActive = false;
    }

    /**
     * Check if shield start is active, falser  above
     * @return shield active true, shield not active false
     */
    private boolean isShieldActive(){
        return shieldActive && (System.currentTimeMillis() - shieldStartTime) < shieldDuration;
    }

    /**
     * Activates ship's shield for a duration.
     */
    private void activateShield() {
        shieldActive = true;
        shieldStartTime = System.currentTimeMillis();
    }

    /**
     * Check's if game level will increase base on player's score
     * and increase difficulty and obstacle speed.
     */
    private void checkLevel() {
        int newLevel = score / 100 + 1; // Every 100 points = +1 level
        if (newLevel > gameLevel) {
            gameLevel = newLevel;
            levelLabel.setText("Level: " + gameLevel);

            // Increase difficulty
            plusDifficulty();
        }
    }

    /**
     * Check score and level then, increase obstacle speed.
     */
    private void plusDifficulty() {
        //increase obstacle speed
        OBSTACLE_SPEED += 1;}

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new SpaceGame().setVisible(true));
    }
}