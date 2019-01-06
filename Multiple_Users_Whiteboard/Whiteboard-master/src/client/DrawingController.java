package client;

import java.awt.Color;
import java.awt.Point;
import java.awt.Shape;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;


/*
 * DrawingController handles the user's freehand drawing.
 */
public class DrawingController implements MouseListener, MouseMotionListener {
	// store the coordinates of the last mouse event, so we can
	// draw a line segment from that last point to the point of the next mouse event.
	private int lastX, lastY;
	private final Client client;
	// coordinates for drawing a square
	private int x, y, x2, y2;
	// time for dotted line
	private int i = 0;
	private boolean drawOn = true;
	
	ArrayList<Shape> shapes = new ArrayList<Shape>();

    Point startDrag, endDrag;
	
	public DrawingController(Client client) {
		this.client = client;
	}

	public void setStartPoint(int x, int y) {
		this.x = x;
		this.y = y;
	}

	public void setEndPoint(int x, int y) {
		x2 = (x);
		y2 = (y);
	}
	/*
	 * When mouse button is pressed down, start drawing.
	 */
	public void mousePressed(MouseEvent e) {
		System.out.println("square " + Boolean.toString(client.getSquareOn()) );
		System.out.println("text" + Boolean.toString(client.getText()));
		if(client.getSquareOn() == false) {
			if(client.getText()==true)
			{
				lastX = e.getX();
				lastY = e.getY();
				String textFont = new String();
				if(client.getFontArial() == true)
					textFont = "Arial";
				else
					if(client.getFontComic() == true)
						textFont = "Comic Sans MS";
				
				Color color = client.getCurrentColor();
		        
				client.getCanvas().drawText(textFont, color.getRGB(),(int)client.getCurrentWidth(), 0, lastX,  lastY);
			}
			else
				
			if(client.getBrushOn()==true) {
				lastX = e.getX();
				lastY = e.getY();
			}
		}
			
		else
		{
			setStartPoint(e.getX(), e.getY());
			startDrag = new Point(e.getX(), e.getY());
	        endDrag = startDrag;
	        client.getCanvas().newDrawSquare(startDrag, endDrag);
		}
	}

	/*
	 * When mouse moves while a button is pressed down,
	 * draw a line segment or a square
	 */
	public void mouseDragged(MouseEvent e) {
		if(client.getSquareOn() == false) {
			if(client.getBrushOn()==true) {
				int x = e.getX();
				int y = e.getY();

				Color color = client.getCurrentColor();
				if (client.getEraserOn()) { color = Color.white; client.setStraight(true);}
				
				
				if(client.getStraight()==true) {
					client.getCanvas().drawLineSegmentAndCall(lastX, lastY, x, y, color.getRGB(), client.getCurrentWidth());
					System.out.println("is straight");					
				}
				else
					if(client.getDotted()) {
						i++;
						if(i == 30)
						{
							if(drawOn == false)
								drawOn = true;
							else
								drawOn = false;
							i = 0;
						}
						if(drawOn == true)
							client.getCanvas().drawLineSegmentAndCall(lastX, lastY, x, y, color.getRGB(), client.getCurrentWidth());
						
						System.out.println("is dotted");
					}
				lastX = x;
				lastY = y;
			}
			else {
				endDrag = new Point(e.getX(), e.getY());
				setEndPoint(e.getX(), e.getY());
				//client.getCanvas().newDrawSquare(startDrag, endDrag);
			}
		}
	}
	/*
	 * When mouse is realeased draw a rectangle
	 */
	public void mouseReleased(MouseEvent e) {
		if(client.getSquareOn() == true) {
			Color color = client.getCurrentColor();
			setEndPoint(e.getX(), e.getY());
			client.getCanvas().drawSquareAndCall(x, y, x2, y2, color.getRGB(), client.getCurrentWidth());
	          startDrag = null;
	          endDrag = null;
		}
	}
	
	public void mouseMoved(MouseEvent e) { }
	public void mouseClicked(MouseEvent e) { }
	public void mouseEntered(MouseEvent e) { }
	public void mouseExited(MouseEvent e) { }



} 