/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package assign1_3301;

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
        } catch (Exception ex) {
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
        setSize(width * 2 + 100, height + 100);
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
        if (((Button) e.getSource()).getLabel().equals("Add noise")) {
            Random rand = new Random();
            int dev = 64;
            for (int y = 0, i = 0; y < height; y++) {
                for (int x = 0; x < width; x++, i++) {
                    Color clr = new Color(source.image.getRGB(x, y));
                    int red = clr.getRed() + (int) (rand.nextGaussian() * dev);
                    int green = clr.getGreen() + (int) (rand.nextGaussian() * dev);
                    int blue = clr.getBlue() + (int) (rand.nextGaussian() * dev);
                    red = red < 0 ? 0 : red > 255 ? 255 : red;
                    green = green < 0 ? 0 : green > 255 ? 255 : green;
                    blue = blue < 0 ? 0 : blue > 255 ? 255 : blue;
                    source.image.setRGB(x, y, (new Color(red, green, blue)).getRGB());
                }
            }
            source.repaint();
        }
        
        if (((Button) e.getSource()).getLabel().equals("5x5 Kuwahara")) {
            
            int maskSize = 5;
            int[] topLeft = new int[9];
            int[] topRight = new int[9];
            int[] botLeft = new int[9];
            int[] botRight = new int[9];
            int xMin, xMax, yMin, yMax;
            int sumTopRight = 0;
            int sumTopLeft = 0;
            int sumBotLeft = 0;
            int sumBotRight = 0;
            float hue = 0;
            float saturation = 0;
            float brightness = 0;

            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    int count1 = 0;
                    int count2=0;
                    int count3=0;
                    int count4=0;
                    xMin = x - (maskSize / 2);
                    xMax = x + (maskSize / 2);
                    yMin = y - (maskSize / 2);
                    yMax = y + (maskSize / 2);
                    for (int r = yMin; r <= yMax; r++) {
                        for (int c = xMin; c <= xMax; c++) {

                            if (r < 0 || r >= height || c < 0 || c >= width) {

                                continue;
                            }

                            Color clr = new Color(source.image.getRGB(c, r));
                            int red = clr.getRed();
                            int green = clr.getGreen();
                            int blue = clr.getBlue();

                            Color.RGBtoHSB(red, green, blue, null);
                            float[] hsb = Color.RGBtoHSB(red, green, blue, null);
                            hue = hsb[0];
                            saturation = hsb[1];
                            brightness = hsb[2]*255;
                            //System.out.println("brightness " +hsb[2]);

                            if (c <= x && r <= y) { //topLeft
                                topLeft[count1] = (int) brightness;
                                sumTopRight += (int)brightness;
                                count1++;

                            } else if (c >= x && r <= y) {    //topRight
                                topRight[count2] = (int) brightness;
                                sumTopLeft += (int) brightness;
                                count2++;
                            } else if (c <= x && r >= y) {          //botLEft
                                botLeft[count3] = (int) brightness;
                                sumBotRight += (int) brightness;
                                count3++;
                            } else if (c >= x && y >= y) {    //botRight
                                botRight[count4] = (int) brightness;
                                sumBotLeft += (int) brightness;
                                count4++;
                            }

                            // target.image.setRGB(x, y, redsort[0][1] << 16 | greensort[2] << 8 | bluesort[2]);
                        }
                    }

                    int[] sortArray = new int[4];
                    
                   // System.out.println("top left "  + Arrays.toString(topLeft));
                    int topLeftVariance = variance(topLeft, 9)[0];
                    int topRightVariance = variance(topRight, 9)[0];
                    int botRightVariance = variance(botLeft, 9)[0];
                    int botLeftVariance = variance(topRight, 9)[0];
                    int topLeftmean = sumTopLeft / 9;
                    int topRightmean = sumTopRight / 9;
                    int botRightmean = sumBotRight / 9;
                    int botLeftmean = sumBotLeft / 9;

                    sortArray[0] = topLeftVariance;
                    sortArray[1] = topRightVariance;
                    sortArray[2] = botLeftVariance;
                    sortArray[3] = topRightVariance;
                    java.util.Arrays.sort(sortArray);

                    int finalVariance = sortArray[0];
                    float mean = 0;

                    if (finalVariance == topLeftVariance) {
                        mean = variance(topLeft, 9)[1];
                    }
                    if (finalVariance == topRightVariance) {
                        mean = variance(topRight, 9)[1];
                    }
                    if (finalVariance == botLeftVariance) {
                        mean = variance(botLeft, 9)[1];
                    }
                    if (finalVariance == botRightVariance) {
                        mean = variance(botRight, 9)[1];
                    }
                    
                    System.out.println("mean " +mean/255);
                    int rgb = Color.HSBtoRGB(hue, saturation, mean/255);
                    
                    
                    int red = (rgb >> 16) & 0xFF;

                    int green = (rgb >> 8) & 0xFF;

                    int blue = rgb & 0xFF;

                    target.image.setRGB(x,y,rgb);

                }
            }
            target.resetImage(target.image);
            
        }

        if (((Button) e.getSource()).getLabel().equals("5x5 Median")) {

            int maskSize = 5;
            int red[], green[], blue[];
            int xMin, xMax, yMin, yMax;
            red = new int[25];
            green = new int[25];
            blue = new int[25];
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    int count = 0;

                    xMin = x - (maskSize / 2);   //get difference between size of kernel and boundary
                    xMax = x + (maskSize / 2);
                    yMin = y - (maskSize / 2);
                    yMax = y + (maskSize / 2);
                    for (int r = yMin; r <= yMax; r++) {
                        for (int c = xMin; c <= xMax; c++) {
                            if (r < 0 || r >= height || c < 0 || c >= width) {

                                
                            } else {
                                Color clr = new Color(source.image.getRGB(c, r));
                                red[count] = clr.getRed();
                                green[count] = clr.getGreen();
                                blue[count] = clr.getBlue();
                                count++;
                            }
                        }
                    }

                    java.util.Arrays.sort(red);
                    java.util.Arrays.sort(green);
                    java.util.Arrays.sort(blue);

                    target.image.setRGB(x, y, red[12] << 16 | green[12] << 8 | blue[12]);
                }
            }
            target.resetImage(target.image);
            System.out.println("done");

        }

        if (((Button) e.getSource()).getLabel().equals("5x5 Gaussian")) {
            // create a 1D gaussian kernel
            float sigma = Float.valueOf(texSigma.getText());
            int kSize = 5;
            int w = 2; // kernel width
            float[] gKernel = new float[5];
            float sum = 0;
            for (int i = -w; i <= w; i++) {
                float gx = (float) Math.pow(1 / (float) Math.sqrt(2 * Math.PI) * sigma, 1 / Math.exp(2 * sigma * sigma) * (i * i) );
                gKernel[i + 2] = gx; 
                sum += gx;
            }
            // normalize the kernel
            for (int i = 0; i < kSize; i++) {
                gKernel[i] /= sum;
            }
            
            // apply gaussian kernel
            float rSum = 0;
            float gSum = 0;
            float bSum = 0;
            for (int q = 0; q <= height; q++) {
                for (int p = w + 1; p < width - w; p++) {
                    
                }
            }
        
           
            
            

        }

        if (((Button) e.getSource()).getLabel().equals("5x5 mean")) {
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
                    Color clr = new Color(source.image.getRGB(q + 1, p));
                    Color clr2 = new Color(source.image.getRGB(q - w - 1, p));
                    rSum += clr.getRed() - clr2.getRed();
                    gSum += clr.getGreen() - clr2.getGreen();
                    bSum += clr.getBlue() - clr2.getBlue();
                    target.image.setRGB(q, p, rSum / (2 * w + 1) << 16 | gSum / (2 * w + 1) << 8 | bSum / (2 * w + 1));
                }
            }
            target.repaint();
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
        return new int[][]{r, g, b};
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
        return new float[][]{r, g, b};
    }
    
     public int[] variance(int a[], int n) {
        // Compute mean (average of elements) 
        int sum = 0;
        int[] cont = new int[2];
        int count=0;
        for (int i = 0; i < n; i++) {
            if (a[i]!=0){
                sum += a[i];
                count++;
            }
            
        }
        int mean = sum
                / count;

        double variance = 0;
        for (int i = 0; i < n; i++) {
            double temp = a[i] - mean;
            variance += Math.pow(temp, 2);
        }
        cont[0] = (int) (variance / n);
        cont[1] = mean;
        return cont;
    }

    public int mean(int[] data) {
        int sum = 0;
        int average;

        for (int i = 0; i < data.length; i++) {
            sum = sum + data[i];
        }
        average = sum / data.length;
        return average;
    }

    public static void main(String[] args) {
        new SmoothingFilter(args.length == 1 ? args[0] : "baboon.png");
    }
}
