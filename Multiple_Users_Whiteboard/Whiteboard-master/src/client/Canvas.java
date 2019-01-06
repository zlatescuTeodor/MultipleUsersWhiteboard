package client;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.image.BufferedImage;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Rectangle2D;
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
	 * Draw a line between and send it to every client
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
		new BasicStroke( width,// Width
	            BasicStroke.CAP_ROUND,     	// End cap
	            BasicStroke.JOIN_ROUND,    	// Join style
	            10.0f,                     	// limit
	            null, 						// Dash pattern
	            0.0f);
		g.setStroke(new BasicStroke( width,// Width
	            BasicStroke.CAP_ROUND,     	// End cap
	            BasicStroke.JOIN_ROUND,    	// Join style
	            10.0f,                     	// limit
	            null, 						// Dash pattern
	            0.0f));
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
	//tring to make the square show where it is drawn
	public void newDrawSquare(Point startDrag, Point endDrag) {
		 	Graphics2D g2 = (Graphics2D) client.getDrawingBuffer().getGraphics();
	      g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
	      
	      Color[] colors = { Color.YELLOW, Color.MAGENTA, Color.CYAN , Color.RED, Color.BLUE, Color.PINK};
	      int colorIndex = 0;

	      g2.setStroke(new BasicStroke(2));
	      g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.50f));

	      if (startDrag != null && endDrag != null) {
	        g2.setPaint(Color.LIGHT_GRAY);
	        Shape r = makeRectangle(startDrag.x, startDrag.y, endDrag.x, endDrag.y);
	        g2.draw(r);
	        System.out.println("am ajuns aici"+" "+ startDrag.x + " " + startDrag.y + " " + endDrag.x+ " " +endDrag.y);
	      }
	      this.setVisible(true);
	      this.repaint();
		
	}
	private Rectangle2D.Float makeRectangle(int x1, int y1, int x2, int y2) {
	      return new Rectangle2D.Float(Math.min(x1, x2), Math.min(y1, y2), Math.abs(x1 - x2), Math.abs(y1 - y2));
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
	/**
	 * Draw a rectagle and call
	 */
	protected void drawSquareAndCall(int x1, int y1, int x2, int y2, int color, float width) {
		drawSquare(x1, y1, x2, y2, color, width);
		try {
			client.makeDrawRequest("drawSquare "+x1+" "+y1+" "+x2+" "+y2+" "+(color+16777216)+" "+width);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	protected void drawTextAndCall(String textFont, int color, int width, int idfk, int x, int y) {
		drawText(textFont, color,width, 0, x,  y);
		try {
			client.makeDrawRequest("drawText "+textFont+" "+(color+16777216)+" "+width+" "+idfk+" "+x+" "+y);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	/**
	 * Edit where the text will be draw
	 */
	public void drawText(String textFont,int color, int width, int idfk, int x,  int y) {
		String text= "";
		text = JOptionPane.showInputDialog(this, "Text to add", "Text");
		String textBuffer = "";
		
		Graphics2D g = (Graphics2D) client.getDrawingBuffer().getGraphics();
		
		Font font = new Font(textFont, Font.PLAIN, width);
		FontMetrics metrics = g.getFontMetrics(font);
		int i = 0;
		int j = 0;
		int k = 0;
		while(i < text.length()) {
			if(i == 0) {
				textBuffer += text.charAt(i);
						i++;
			}
			textBuffer += text.charAt(i);
			int maxDim = 550 - x - metrics.stringWidth("    ");
			if(metrics.stringWidth(text.substring(j, i)) >= maxDim) {
				Color colorObject = new Color(color);
				
				drawOnlyText(textBuffer,textFont, color, width, x, y, k);
				drawOnlyTextAndCall(textBuffer,textFont, color, width, x, y, k);
				System.out.println("draw");
				j = i + 1;
				k+=metrics.getHeight();
				textBuffer = "";
			}
			else if(metrics.stringWidth( text.substring(j, text.length()-1)) < (550 - x) ) {
				textBuffer = text.substring(j, text.length());
				Color colorObject = new Color(color);
				drawOnlyText(textBuffer,textFont, color, width, x, y, k);
				drawOnlyTextAndCall(textBuffer,textFont, color, width, x, y, k);
				System.out.println("draw");
				break;
			}
			i++;
			
		}
		
	}
	/**
	 * Acttually draw text and call
	 */
	public void drawOnlyTextAndCall(String textBuffer, String textFont, int color, int width, int x, int y, int k) {
		
		try {
			client.makeDrawRequest("drawOnlyText "+textBuffer+" "+textFont+" "+(color+16777216)+" "+width+" "+x+" "+y+" "+k);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	/**
	 * Acttually draw text
	 */
	public void drawOnlyText(String textBuffer, String textFont, int color, int width, int x, int y, int k) {
		Graphics2D g = (Graphics2D) client.getDrawingBuffer().getGraphics();
		Color colorObject = new Color(color);
		Font font = new Font(textFont, Font.PLAIN, width);
		g.setColor(colorObject);
		g.setFont(font);
		g.drawString(textBuffer, x, (y + k));
		this.repaint();
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



