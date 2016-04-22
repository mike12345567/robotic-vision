package com.queens.utilities;

import com.queens.colours.Colour;
import com.queens.entities.ColouredArea;
import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;

import java.util.List;

public class MatOperations {
    static public final double defaultFactor = 0.1;

    public static double alphaFactor = defaultFactor;
    public static double betaFactor = defaultFactor*5;
    static public double factorChange = 0.1;

    private static final int isBrightThreshold = 80;
    private static int lastBrightnessAvg = 0;

    public static int averageBrightness(Mat image) {
        Mat grayscale = new Mat();
        Imgproc.cvtColor(image, grayscale, Imgproc.COLOR_BGR2GRAY);
        int lineCount = 0;
        long lineAvgs[] = new long[grayscale.width()];
        for (int x = 0; x < grayscale.width(); x++) {
            long lineAvg = 0;
            for (int y = 0; y < grayscale.height(); y++) {
                lineAvg += grayscale.get(y, x)[0];
            }
            lineAvgs[lineCount] = lineAvg / grayscale.height();
            lineCount++;
        }
        long finalAvg = 0;
        for (long avg : lineAvgs) {
            finalAvg += avg;
        }
        return (int)(finalAvg / grayscale.width());
    }

    public static void setBrightnessModeForColours(Mat cameraImage, List<Colour> colours) {
        MatOperations.lastBrightnessAvg = averageBrightness(cameraImage) + (MatOperations.lastBrightnessAvg == 0 ? 30 : 0);
        boolean isBright = MatOperations.lastBrightnessAvg >= isBrightThreshold;
        for (Colour colour : colours) {
            colour.setIsBright(isBright);
        }
    }

    public static Mat alterBrightness(Mat image) {
        double alpha = 1;
        double beta = 1;

        alpha += alphaFactor;
        beta += betaFactor;

        if (image == null) return null;
        image.convertTo(image, -2, alpha, beta);
        return image;
    }

    public static void drawPoint(Mat output, Point point, Colour colour) {
        if (point == null) return;

        Rect rect = new Rect((int)point.x, (int)point.y, 5, 5);
        Imgproc.rectangle(output, rect.tl(), rect.br(), colour.rgbColour, 3);
    }

    public static void drawOutline(Mat output, List<ColouredArea> areas, Colour colour) {
        for (ColouredArea area : areas) {
            if (area.getColour() == colour.getName() && area.shouldDraw()) {
                Rect rect = area.getBoundingBox();
                Imgproc.rectangle(output, rect.tl(), rect.br(), colour.rgbColour, 3);
            }
        }
    }
}
