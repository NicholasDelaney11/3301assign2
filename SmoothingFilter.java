/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package assign2_3301;

import java.util.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.*;
import javax.imageio.*;

// Main class
public class SmoothingFilter extends Frame implements ActionListener {
	BufferedImage input;
	ImageCanvas source, target;
	TextField texSigma;
	int width, height;
        final int GREY_LEVEL = 256;
        int[][] sHistogram;   // histogram for source image
	// Constructor
	public SmoothingFilter(String name) {
		super("Smoothing Filters");
		// load image
		try {
			input = ImageIO.read(new File(name));
		}
		catch ( Exception ex ) {
			ex.printStackTrace();
		}
		width = input.getWidth();
		height = input.getHeight();
		// prepare the panel for image canvas.
		Panel main = new Panel();
		source = new ImageCanvas(input);
		target = new ImageCanvas(input);
                sHistogram = getHistogram(source);
		main.setLayout(new GridLayout(1, 2, 10, 10));
		main.add(source);
		main.add(target);
		// prepare the panel for buttons.
		Panel controls = new Panel();
		Button button = new Button("Add noise");
		button.addActionListener(this);
		controls.add(button);
		button = new Button("5x5 mean");
		button.addActionListener(this);
		controls.add(button);
		controls.add(new Label("Sigma:"));
		texSigma = new TextField("1", 1);
		controls.add(texSigma);
		button = new Button("5x5 Gaussian");
		button.addActionListener(this);
		controls.add(button);
		button = new Button("5x5 median");
		button.addActionListener(this);
		controls.add(button);
		button = new Button("5x5 Kuwahara");
		button.addActionListener(this);
		controls.add(button);
		// add two panels
		add("Center", main);
		add("South", controls);
		addWindowListener(new ExitListener());
		setSize(width*2+100, height+100);
		setVisible(true);
	}
	class ExitListener extends WindowAdapter {
		public void windowClosing(WindowEvent e) {
			System.exit(0);
		}
	}
	// Action listener for button click events
	public void actionPerformed(ActionEvent e) {
		// example -- add random noise
		if ( ((Button)e.getSource()).getLabel().equals("Add noise") ) {
			Random rand = new Random();
			int dev = 64;
			for ( int y=0, i=0 ; y<height ; y++ )
				for ( int x=0 ; x<width ; x++, i++ ) {
					Color clr = new Color(source.image.getRGB(x, y));
					int red = clr.getRed() + (int)(rand.nextGaussian() * dev);
					int green = clr.getGreen() + (int)(rand.nextGaussian() * dev);
					int blue = clr.getBlue() + (int)(rand.nextGaussian() * dev);
					red = red < 0 ? 0 : red > 255 ? 255 : red;
					green = green < 0 ? 0 : green > 255 ? 255 : green;
					blue = blue < 0 ? 0 : blue > 255 ? 255 : blue;
					source.image.setRGB(x, y, (new Color(red, green, blue)).getRGB());
				}
			source.repaint();
		}
                
                if ( ((Button)e.getSource()).getLabel().equals("5x5 Median") ) {
                    
                }
                
                if ( ((Button)e.getSource()).getLabel().equals("5x5 Gaussian") ) {
                    // 3 mean filters?
                    
                }
                
                if ( ((Button)e.getSource()).getLabel().equals("5x5 mean") ) {                 
                    int w = 2;
                    for (int q = 0; q < height; q++) {   // full height of image
                        int rSum = 0;
                        int gSum = 0;
                        int bSum = 0;                    
                        for (int u = -w; u <= w; u++) {   // y kernal?                        
                            Color clr = new Color(source.image.getRGB(q, u + w));      // x and y switched? 
                            rSum += clr.getRed();
                            gSum += clr.getGreen();
                            bSum += clr.getBlue();                    
                            target.image.setRGB(q, w, rSum / (2 * w + 1) << 16 | gSum / (2 * w + 1) << 8 | bSum / (2 * w + 1)); // set pixel in middle of kernel                                                      
                        }
                        for (int p = w + 1; p < width - w; p++) {                       // incremental
                            Color clr = new Color(source.image.getRGB(q, p + w));
                            Color clr2 = new Color(source.image.getRGB(q, p - w - 1));
                            rSum += clr.getRed() - clr2.getRed();
                            gSum += clr.getGreen() - clr2.getGreen();
                            bSum += clr.getBlue() - clr2.getBlue();
                            target.image.setRGB(q, p, rSum / (2 * w + 1) << 16 | gSum / (2 * w + 1) << 8 | bSum / (2 * w + 1));
                        }         
                    }
                    for (int p = 0; p < width; p++) {
                        int rSum = 0;
                        int gSum = 0;
                        int bSum = 0;
                        for (int u = -w; u <= w; u++) {
                            Color clr = new Color(source.image.getRGB(p, u + w));
                            rSum += clr.getRed();
                            gSum += clr.getGreen();
                            bSum += clr.getBlue();
                            target.image.setRGB(p, w, rSum / (2 * w + 1) << 16 | gSum / (2 * w + 1) << 8 | bSum / (2 * w + 1)); 
                        }
                        for (int q = w + 1; q < height - w; q++) {
                            Color clr = new Color(source.image.getRGB(p, q + 1));
                            Color clr2 = new Color(source.image.getRGB(p, q - w - 1));
                            System.out.println(p - w - 1);
                            System.out.println(q);
                            rSum += clr.getRed() - clr2.getRed();
                            gSum += clr.getGreen() - clr2.getGreen();
                            bSum += clr.getBlue() - clr2.getBlue();
                            target.image.setRGB(q, p, rSum / (2 * w + 1) << 16 | gSum / (2 * w + 1) << 8 | bSum / (2 * w + 1));
                        }
                    }
                    
                }
	}
        
        public int[][] getHistogram(ImageCanvas picture) {
            int r[] = new int[GREY_LEVEL];
            int g[] = new int[GREY_LEVEL];
            int b[] = new int[GREY_LEVEL];
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    Color clr = new Color(picture.image.getRGB(x, y));
                    int red = clr.getRed();
                    int green = clr.getGreen();
                    int blue = clr.getBlue();
                    r[red]++;
                    g[green]++;
                    b[blue]++;
                }                      
            }            
            return new int[][] { r, g, b };
        }
        
        public float[][] normalizeHistogram(int[][] h) {
            float r[] = new float[GREY_LEVEL];
            float g[] = new float[GREY_LEVEL];
            float b[] = new float[GREY_LEVEL];
            for (int i = 0; i < GREY_LEVEL; i++) {
                r[i] = (float) h[0][i] / (width * height);
                g[i] = (float) h[1][i] / (width * height);
                b[i] = (float) h[2][i] / (width * height);
            }           
            return new float[][] { r, g, b };
        }
             
        
	public static void main(String[] args) {
		new SmoothingFilter(args.length==1 ? args[0] : "baboon.png");
	}
}