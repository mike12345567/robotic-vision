package com.queens.utilities;

import com.queens.colours.Colour;
import com.queens.entities.ColouredArea;
import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;

import java.util.List;

public class MatOperations {
    static private final int maxContrastDiff = 25;
    static private final int isBrightThreshold = 80;

    static public int averageBrightness(Mat image) {
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

    static public void setBrightnessModeForColours(Mat cameraImage, List<Colour> colours) {
        boolean isBright = averageBrightness(cameraImage) >= isBrightThreshold;
        for (Colour colour : colours) {
            colour.setIsBright(isBright);
        }
    }

    static public void alterBrightness(Mat image) {
        Scalar mean = Core.mean(image);
        double totalMean = (mean.val[0] + mean.val[1] + mean.val[2])/3;
        totalMean -= 50;
        double alpha = 1;
        if (totalMean > maxContrastDiff) {
            totalMean = maxContrastDiff;
        }
        if (totalMean < -maxContrastDiff) {
            totalMean = -maxContrastDiff;
        }
        if (totalMean > 0) {
            alpha -= totalMean / 100;
        } else {
            totalMean *= -2;
        }
        image.convertTo(image, -1, alpha, totalMean);
    }

    static public void drawPoint(Mat output, List<Point> points, Colour colour) {
        for (Point point : points) {
            Rect rect = new Rect((int)point.x, (int)point.y, 5, 5);
            Imgproc.rectangle(output, rect.tl(), rect.br(), colour.rgbColour, 3);
        }
    }

    static public void drawOutline(Mat output, List<ColouredArea> areas, Colour colour) {
        for (ColouredArea area : areas) {
            if (area.getColour() == colour.getName()) {
                Rect rect = area.getBoundingBox();
                Imgproc.rectangle(output, rect.tl(), rect.br(), colour.rgbColour, 3);
            }
        }
    }
}
