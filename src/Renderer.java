import java.awt.Graphics;
import javax.swing.JPanel;

public class Renderer extends JPanel {

	private static final long serialVersionUID = 1L; // Our version ID
	
	protected void paintComponent(Graphics g)
	{
		super.paintComponent(g); // Inherit from super because we need all functionality from it.
		
		Player.player.repaint(g); // Pass graphics into my player class.
	}
	
}
