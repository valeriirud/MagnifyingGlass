/**
This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <https://www.gnu.org/licenses/>.
*/

/**
The program demonstrates an example of creating a tool - a magnifying glass.
build:
javac ToolMagnifier.java
run:
java ToolMagnifier --image image.png --scale 2 --radius 150 --brightness 0.5

@author ValRud

Jan 10, 2020
*/

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.event.*; 
import java.awt.image.*;
import java.io.*;
import javax.swing.*;

/**
*   Class for demonstrating a magnifying glass tool. 
*/
public class ToolMagnifier {

	JFrame frame;
	JLabel lbImg;
	int mouseX;
	int mouseY;
	int radius;
	float scale;
	float brightness;
	BufferedImage imgShow;
	BufferedImage imgHide;
	String imageFile;
	
	/**
	*  constructor
	*/
	public ToolMagnifier()
	{
        imgShow = null;
        imgHide = null;
        imageFile = null;
        mouseX = -1;
        mouseY = -1;
        radius = 75;
        scale = 1.0f;
        brightness = 0.75f;
	}
    
    /**
    *   class for handling mouse movements
    */
    class MyMouseListener implements MouseMotionListener 
    { 
        public void mouseDragged(MouseEvent e) {}
        
        public void mouseMoved(MouseEvent e) 
        { 
            mouseX = e.getX();
            mouseY = e.getY();
            lbImg.repaint();
        } 
    }
    
    /**
    *   To clone the source image.
    *   @param img - source BufferedImage
    *   @return - clone of the source BufferedImage
    */
    private BufferedImage cloneBufferedImage(BufferedImage img) {
        ColorModel cm = img.getColorModel();
        boolean alpha = cm.isAlphaPremultiplied();
        WritableRaster wr = img.copyData(null);
        BufferedImage clone = new BufferedImage(cm, wr, alpha, null);
        return clone;
    }
	
	/**
	*  Prepare the main image for display,
	*  change image brightness.
	*  @param img - Image to display to user
	*  @param brightness - Image brightness
	*/
	private void prepareImage(BufferedImage img, float brightness)
	{
        int width = img.getWidth(); 
        int height = img.getHeight();
        
        for(int y = 0; y < height; y++)
        {
            for(int x = 0; x < width; x++)
            {
                int pix = img.getRGB(x, y);
                // get red component
                int r   = (pix & 0xff0000) >> 16;
                // get green component
                int g = (pix & 0xff00) >> 8;
                // get blue component
                int b  =  pix & 0xff;
                // change the brightness
                r  *= brightness;
                if (r > 255) r = 255;
                g *= brightness;
                if (g > 255) g = 255;
                b  *= brightness;
                if (b > 255) b = 255;
                // make new pixel
                pix = (pix & 0xff000000) | (r << 16) | (g << 8) | b;
                // set new pixel
                img.setRGB((int)x,(int)y, pix);
            }
        }
	}
	
	/**
	*  Copy the area from the latent image and paste into the displayed image.
	*  @param r - The radius of magnifier
	*  @param scale - Scale to enlarge
	*  @param -mouseX - Mouse x coordinate
	*  @param mouseY - Mouse y coordinate
	*  @param g - The Graphics class for graphics context
	*
	*/	
	private void drawImage(int r, float scale, 
                            int mouseX, int mouseY, Graphics g) {
                
        int pix;
        float x = (float)(mouseX - r);
        float y = (float)(mouseY - r);
            
        int posX = mouseX;
        int posY = mouseY;
    
        BufferedImage imgArea = new BufferedImage((int)r*2, (int)r*2, 
                                    BufferedImage.TYPE_INT_RGB);
        float d = 1/scale;
        
        for(int i = 0; i < r*2; i ++) {
            x += d;
            y = (float)mouseY;
            for(int j = -r; j < r; j ++) {
                y += d;
                pix = 0;
                try {
                    pix = imgShow.getRGB((int)(mouseX - r + i), 
                                            (int)(mouseY + j));
                    
                    if( (i-r)*(i-r) + j*j <= r*r) {
                        pix = imgHide.getRGB((int)(x), (int)(y - r));
                    }
                    
                } catch(java.lang.ArrayIndexOutOfBoundsException ex) {
                    pix = 0;
                }
                
                imgArea.setRGB(i,j+r, pix);
            }
        }
            
        g.drawImage(imgArea, posX - r, posY - r, (int)r*2, (int)r*2, null);
	}
	
	/**
	*  Parse command line.
	*  @param args - Command line arguments
	*/
	private void parseCommandLine(String[] args)
	{
        int len = args.length;
        
        for (int i = 0; i < len; i ++)
        {
            if(args[i].equals("--image") && i < len - 1)
            {
                imageFile = args[i + 1];
            }
            
            if(args[i].equals("--scale") && i < len - 1)
            {
                scale = Float.parseFloat(args[i + 1]);
            }
            
            if(args[i].equals("--radius") && i < len - 1)
            {
                radius = Integer.parseInt(args[i + 1]);
            }
            
            if(args[i].equals("--brightness") && i < len - 1)
            {
                brightness = Float.parseFloat(args[i + 1]);
            }
        }
	}

	/**
	*  Read command line options, prepare images, show the main window.
	*  @param args - Command line arguments
	*/
	public void showImage(String[] args){
		
		parseCommandLine(args);
		
		try 
		{
            // Read in the specified image
            imgShow = ImageIO.read(new File(imageFile));
        } 
        catch (IOException e) 
        {
            e.printStackTrace();
            return;
        }

        // clone image
        imgHide = cloneBufferedImage(imgShow);
		
		// change the brightness of the visible image
		prepareImage(imgShow, brightness);
		
		// Use label to display the image
		lbImg = new JLabel(new ImageIcon(imgShow)){
            public void paint(Graphics g) {                
                super.paint(g);
 
                if(mouseX < 0 || mouseY < 0) {
                    return;
                }
                
                drawImage(radius, scale, mouseX, mouseY, g);
            }
        };

        // create an object of MyMouseListener class 
        MyMouseListener mouseListener = new MyMouseListener(); 

        lbImg.addMouseMotionListener(mouseListener);          
        lbImg.setVerticalAlignment(JLabel.TOP);
        lbImg.setHorizontalAlignment(JLabel.LEFT);
        
        frame = new JFrame();
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.getContentPane().add(lbImg);
        frame.pack();        
        frame.setResizable(false);
        frame.setVisible(true);
	}
    
    public static void main(String[] args) {
        if(args.length == 0) {
            System.out.println("java ToolMagnifier " + 
                                "<--image path_to_image> [--scale scale] " + 
                                "[--radius radius] [--brightness brightness]");
            System.exit(0);
        }
        
		ToolMagnifier tm = new ToolMagnifier();
		tm.showImage(args);
	}

}


