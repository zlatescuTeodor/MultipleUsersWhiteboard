package client;

import java.awt.Color;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;


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
		if(client.getSquareOn() == false) {
			if(client.getBrushOn()==true) {
				lastX = e.getX();
				lastY = e.getY();
			}
		}
		else
		{
			setStartPoint(e.getX(), e.getY());
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
				setEndPoint(e.getX(), e.getY());
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
			client.getCanvas().drawSquare(x, y, x2, y2, color.getRGB(), client.getCurrentWidth());
		}
	}
	
	public void mouseMoved(MouseEvent e) { }
	public void mouseClicked(MouseEvent e) { }
	public void mouseEntered(MouseEvent e) { }
	public void mouseExited(MouseEvent e) { }



} 