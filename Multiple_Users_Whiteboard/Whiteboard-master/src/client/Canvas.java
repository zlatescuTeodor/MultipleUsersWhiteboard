package client;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.File;
import java.io.IOException;
import java.util.EventListener;

import javax.imageio.ImageIO;
import javax.swing.*;

/**
 * Canvas represents a drawing surface that allows the user to draw
 * on it freehand, with the mouse.
 */

public class Canvas extends JPanel {
    
	// image where the user's drawing is stored
	private static final long serialVersionUID = 2L;
	private final Client client;
	private EventListener currentListener;

	public Canvas(Client client) {
		this.client = client;
	}
	/**
	 * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
	 */
	@Override
	public void paintComponent(Graphics g) {
		// If this is the first time paintComponent() is being called,
		// make our drawing buffer.
		if (client.getDrawingBuffer() == null) {
			makeDrawingBuffer();
		}

		// Copy the drawing buffer to the screen.
		g.drawImage(client.getDrawingBuffer(), 0, 0, null);
	}
	
	

	/**
	 * Make the drawing buffer and draw some starting content for it.
	 */
	protected void makeDrawingBuffer() {
		client.setDrawingBuffer(new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_RGB));
		fillWithWhite();
	}

	/**
	 * Make the drawing buffer entirely white.
	 */
	protected void fillWithWhite() {
		final Graphics2D g = (Graphics2D) client.getDrawingBuffer().getGraphics();

		g.setColor(Color.WHITE);
		g.fillRect(0,  0,  getWidth(), getHeight());

		this.repaint();
	}
	/**
	 * Draw a selected image from file
	 */
	public void fillWithImage(String ImagePath) {
		
		final Graphics2D g = (Graphics2D) client.getDrawingBuffer().getGraphics();
		final BufferedImage img;
		ImageIcon MyImage = new ImageIcon(ImagePath);
		
		try {
            img = ImageIO.read(new File(ImagePath));
            g.drawImage(img, 0, 0, this);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
		
		this.repaint();
	}
	/**
	 * Draw a selected image from file and put it on server
	 */
	protected void fillWithImageAndCall(String ImagePath) {
		fillWithImage(ImagePath);
		try {
			client.makeDrawRequest("fillWithImage "+ImagePath);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Draw a line between two points (x1, y1) and (x2, y2), specified in
	 * pixels relative to the upper-left corner of the drawing buffer.
	 */
	protected void drawLineSegmentAndCall(int x1, int y1, int x2, int y2, int color, float width) {
		drawLineSegment(x1, y1, x2, y2, color, width);
		try {
			client.makeDrawRequest("drawLineSegment "+x1+" "+y1+" "+x2+" "+y2+" "+(color+16777216)+" "+width);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Draw a line between two points (x1, y1) and (x2, y2), specified in
	 * pixels relative to the upper-left corner of the drawing buffer.
	 */
	public void drawLineSegment(int x1, int y1, int x2, int y2, int color, float width) {
		Graphics2D g = (Graphics2D) client.getDrawingBuffer().getGraphics();
		Color colorObject = new Color(color);
		g.setColor(colorObject);
		g.setStroke(new BasicStroke(width));
		g.drawLine(x1, y1, x2, y2);

		
		this.repaint();
	}
	/**
	 * Save the canvas as .png on file
	 */
	public void saveMethod() {
		BufferedImage bi = new BufferedImage(800, 600, BufferedImage.TYPE_INT_ARGB); 
        Graphics g = bi.createGraphics();
        this.paint(g);
        g.dispose();
        JFileChooser fc = new JFileChooser();
        int returnVal = fc.showSaveDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = fc.getSelectedFile();
            try{
                ImageIO.write(bi,"png",file);
                }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
	}
	
	/**
	 * Draw a rectagle
	 */
	
	public void drawSquare(int x, int y, int x2, int y2, int color, float width) {
		Graphics2D g = (Graphics2D) client.getDrawingBuffer().getGraphics();
		Color colorObject = new Color(color);
		g.setColor(colorObject);
		g.setStroke(new BasicStroke(width));
		 int px = Math.min(x,x2);
         int py = Math.min(y,y2);
         int pw=Math.abs(x-x2);
         int ph=Math.abs(y-y2);
         g.drawRect(px, py, pw, ph);
         this.repaint();
		
	}
	protected void drawSquareAndCall(int x1, int y1, int x2, int y2, int color, float width) {
		drawSquare(x1, y1, x2, y2, color, width);
		try {
			client.makeDrawRequest("drawSquare "+x1+" "+y1+" "+x2+" "+y2+" "+(color+16777216)+" "+width);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	/**
	 * Updates the label showing the current username and the current board name
	 */
	public void updateCurrentUserBoard() {
        String user = client.getUsername();
        String board = client.getCurrentBoardName();
        client.getClientGUI().setCurrentUserBoard(new JLabel("Hi, " + user + ". This board is: " + board));
    }
    
    /**
     * Add the mouse listener that supports the user's freehand drawing.
     */
    public void addDrawingController(EventListener listener) {
        if (currentListener != null) {
            removeMouseListener((MouseListener) currentListener);
            removeMouseMotionListener((MouseMotionListener) currentListener);
        }
        currentListener = listener;
        addMouseListener((MouseListener) currentListener);
        addMouseMotionListener((MouseMotionListener) currentListener);
    }
    
    /**
     * Resets the drawing buffer to be blank and calls switch canvas on the client
     * @param board
     */
    public void switchBoard(String board) {
    	fillWithWhite();
        client.switchBoard(board);
    }
    
    public EventListener getCurrentListener() {
        return currentListener;
    }

}



