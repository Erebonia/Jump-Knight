import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Random;
import javax.imageio.ImageIO;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.Timer;

public class Player implements ActionListener, MouseListener, KeyListener {
	
	// Game Configurations
	public static Player player;
	public final int WIDTH = 1280, HEIGHT = 720; // Jframe Dimensions
	public Renderer renderer;
	public Rectangle playerHitBox;
	public int ticks, yMotion, score;
	public ArrayList<Rectangle> obstacles;
	public Random rand;
	public Boolean gameOver = false;
	public Boolean gameStart = false;
	private BufferedImage playerAvatar;
	private BufferedImage backgroundImage;
	private BufferedImage obstacleImg;
	private Clip clip;
	private String backgroundMusic;
	public int speed = 10;
	
	public Player()
	{
		//Title screen music
		backgroundMusic = "title.wav";
		this.playMusic(backgroundMusic);
		
		//Grab our game assets.
		try {
			playerAvatar = ImageIO.read(getClass().getResourceAsStream("/DK.png"));
			backgroundImage = ImageIO.read(getClass().getResourceAsStream("/bg.png"));
			obstacleImg = ImageIO.read(getClass().getResourceAsStream("/pipe.png"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		JFrame jframe = new JFrame();
		Timer timer = new Timer(20, this);
		
		renderer = new Renderer();
		rand = new Random();
		
		//Configurations
		jframe.setTitle("Jump Knight");
		jframe.setIconImage(new ImageIcon("res/blush.png").getImage());
		jframe.add(renderer);
		jframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Let's you exit the program.
		jframe.setSize(WIDTH, HEIGHT); // Sets the size of our Jframe
		jframe.setVisible(true); // Makes it visible
		jframe.setResizable(false); // Do not allow users to resize at the moment. My assets are limited.
		jframe.addMouseListener(this);
		jframe.addKeyListener(this);
		
		
		playerHitBox = new Rectangle(WIDTH / 2 - 10, HEIGHT / 2 - 10, 64, 64); // x, y, width, height
		
		obstacles = new ArrayList<Rectangle>(); // Declare a new ArrayList for our obstacles.
		timer.start();
		
		addObstacle(true);
		addObstacle(true);
		addObstacle(true);
		addObstacle(true);
		
	}
	
	public void playMusic(String musicLocation)
	{
		//Grab music.
		
		try {
			File musicPath = new File(musicLocation);
			
			if(musicPath.exists()) {
				AudioInputStream audioInput = AudioSystem.getAudioInputStream(musicPath);
				clip = AudioSystem.getClip();
				clip.open(audioInput);
				clip.start();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void addObstacle(boolean start)
	{
		int space = 300;
		int width = 100;
		int height = 50 + rand.nextInt(300); // Max height set to 300
		
		if (start) // Adds Obstacles at game start
		{
			obstacles.add(new Rectangle(WIDTH + width + obstacles.size() * 300, HEIGHT - height, width, height)); 
			obstacles.add(new Rectangle(WIDTH + width + (obstacles.size() - 1) * 300, 0, width, HEIGHT - height - space));
		}else { // Else use previous obstacles in arrayList to generate more and slightly tweaking them. Giving that randomized effect.
			obstacles.add(new Rectangle(obstacles.get(obstacles.size() - 1).x + 600, HEIGHT - height, width, height));
			obstacles.add(new Rectangle(obstacles.get(obstacles.size() - 1).x, 0, width, HEIGHT - height - space));
		}
	}
	
	public void paintObstacle(Graphics g, Rectangle obstacle)
	{
		g.drawImage(obstacleImg, obstacle.x, obstacle.y, obstacle.width, obstacle.height, null);
	}
	
	public void jump()
	{
		if (gameOver) // Starts a new game!
		{
			playerHitBox = new Rectangle(WIDTH / 2 - 10, HEIGHT / 2 - 10, 20, 20);
			obstacles.clear(); // Clear our arraylist.
			yMotion = 0;
			score = 0;
			speed = 10;
			
			addObstacle(true);
			addObstacle(true);
			addObstacle(true);
			addObstacle(true);
			
			gameOver = false;

			//clip.stop();
			backgroundMusic = "bgmusic.wav";
			this.playMusic(backgroundMusic);
		}
		
		if (!gameStart)
		{
			gameStart = true;
		}else if (!gameOver) { // Adds our Jump feature
			
			if (yMotion > 0)
			{
				yMotion = 0;
			}
			
		yMotion -= 10;
		}
	}
	
	@Override
	public void actionPerformed(ActionEvent e)
	{
		
		if (score == 10) // Speed increase based on score.
		{
			speed = 13;
		}else if (score == 20) {
			speed = 16;
		}else if (score == 30){
			speed = 19;
		}else if (score == 40) {
			speed = 25;
		}
		
		ticks++; //Increment ticks after every actionPerformed();
		
		if (gameStart)
		{
			
			for (int i = 0; i < obstacles.size(); i++) // Loop through our ArrayList and adjust the x value of our obstacle to give that continuous moving forward look.
			{
				Rectangle obstacle = obstacles.get(i); 
				obstacle.x -= speed;
			}
			
			if (ticks % 2 == 0 && yMotion < 15) // If remainder of ticks is = 0
			{
				yMotion += 2;
			}
			
			for (int i = 0; i < obstacles.size(); i++) // Remove obstacles when they are no longer in the player's vision.
			{
				Rectangle obstacle = obstacles.get(i); 
				if (obstacle.x + obstacle.width < 0)
				{
					obstacles.remove(obstacle);
					
					if (obstacle.y == 0)
					{
						addObstacle(false);
					}
					
					addObstacle(false);
				}
			}
		}
		
		playerHitBox.y += yMotion; // Generates our player movement!
		
		for (Rectangle obstacle : obstacles)
		{
			if (obstacle.y == 0 && playerHitBox.x + playerHitBox.width / 2 > obstacle.x + obstacle.width / 2 - 10 && playerHitBox.x + playerHitBox.width / 2 < obstacle.x + obstacle.width / 2 + 10) // Center of the obstacle when passing through
			{
				score++;
			}
			
			if (obstacle.intersects(playerHitBox)) // Detects collisions!
			{
				gameOver = true;
				
				//clip.stop(); // Turn off music
				
				if (playerHitBox.x <= obstacle.x) // Prevent our player from phasing through our obstacles, by matching their positions and creating the illusion of "Physics"
				{
					playerHitBox.x = obstacle.x - playerHitBox.width;
				}else {
					if (obstacle.y != 0)
					{
						playerHitBox.y = obstacle.y - playerHitBox.y;
					}else if (playerHitBox.y < obstacle.height)
					{
						playerHitBox.y = obstacle.height;
					}
				}
			}
		}
		
		if (playerHitBox.y > HEIGHT - 120 || playerHitBox.y < 0)
		{
			gameOver = true;
		}
		
		if (playerHitBox.y + yMotion >= HEIGHT - 120)
		{
			playerHitBox.y = HEIGHT - 120 - playerHitBox.height;
			gameOver = true;
		}

		renderer.repaint();
	}
	
	public void repaint(Graphics g) {
		//Background Configs
		g.drawImage(backgroundImage, 0, 0, WIDTH, HEIGHT, null);
		
		//Player Configs
		g.drawImage(playerAvatar, playerHitBox.x, playerHitBox.y, 64, 64, null);
		g.setColor(Color.white);
		g.setFont(new Font ("Calibri", 1, 12));
		g.drawString("Knight", playerHitBox.x + 24, playerHitBox.y - 5);
		
		for (Rectangle obstacles : obstacles) // Iterator that accesses our ArrayList to paint our obstacles
		{
			paintObstacle(g, obstacles);
		}
		
		g.setColor(Color.white);
		g.setFont(new Font ("Calibri", 1, 75));
		
		if (!gameStart)
		{
			g.drawString("Project Jump", 400, HEIGHT / 6);
			g.setFont(new Font ("Calibri", 1, 50));
			g.drawString("Click or press space to start!", 300, HEIGHT / 2 + 200);
			g.setFont(new Font ("Lucida Handwriting", 1, 25));
			g.drawString("Created by Erebonia", 400, HEIGHT / 2 + 250);
		}
		
		
		if (gameOver) // Display Game over screen.
		{
			g.drawString("Game Over!", 450, HEIGHT / 2);
		}
		
		if (!gameOver && gameStart) // Display Current Score
		{
			g.drawString(String.valueOf(score), WIDTH / 2 - 25, 100);
		}
	}

	public static void main(String[] args) {
		player = new Player();
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mousePressed(MouseEvent e) {
		jump();
		
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub
	}

	@Override
	public void keyPressed(KeyEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void keyReleased(KeyEvent arg0) {
		if (arg0.getKeyCode() == KeyEvent.VK_SPACE)
		{
			jump();
		}
	}

	@Override
	public void keyTyped(KeyEvent arg0) {
		// TODO Auto-generated method stub
		
	}



}
