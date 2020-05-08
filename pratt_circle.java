package com.example.democv;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

public class pratt_circle {

    Paint paintCircle;
    Canvas canvas;


    Bitmap prattNewton(Bitmap bitmap, double[][] points) {
        int nPoints = points.length;

        double[] centroid = Centroid(points);
        double Mxx = 0, Myy = 0, Mxy = 0, Mxz = 0, Myz = 0, Mzz = 0;

        for (int i = 0; i < nPoints; i++) {
            double Xi = points[i][0] - centroid[0];
            double Yi = points[i][1] - centroid[1];
            double Zi = Xi * Xi + Yi * Yi;
            Mxy += Xi * Yi;
            Mxx += Xi * Xi;
            Myy += Yi * Yi;
            Mxz += Xi * Zi;
            Myz += Yi * Zi;
            Mzz += Zi * Zi;
        }
        Mxx /= nPoints;
        Myy /= nPoints;
        Mxy /= nPoints;
        Mxz /= nPoints;
        Myz /= nPoints;
        Mzz /= nPoints;

        double Mz = Mxx + Myy;
        double Cov_xy = Mxx * Myy - Mxy * Mxy;
        double Mxz2 = Mxz * Mxz;
        double Myz2 = Myz * Myz;

        double A2 = 4 * Cov_xy - 3 * Mz * Mz - Mzz;
        double A1 = Mzz * Mz + 4 * Cov_xy * Mz - Mxz2 - Myz2 - Mz * Mz * Mz;
        double A0 = Mxz2 * Myy + Myz2 * Mxx - Mzz * Cov_xy - 2 * Mxz * Myz * Mxy + Mz * Mz * Cov_xy;
        double A22 = A2 + A2;

        System.out.println("A2: "+A2);
        System.out.println("A1: "+A1);
        System.out.println("A0: "+A0);
        System.out.println("A22: "+A22);

        double epsilon = 1e-12;
        double ynew = 1e+20;
        int IterMax = 20;
        double xnew = 0;
        for (int iter = 0; iter <= IterMax; iter++) {
            double yold = ynew;
            ynew = A0 + xnew * (A1 + xnew * (A2 + 4 * xnew * xnew));

            double Dy = A1 + xnew * (A22 + 16 * xnew * xnew);
            double xold = xnew;
            xnew = xold - ynew / Dy;
            System.out.println("Xnew: "+xnew);
            if (Math.abs((xnew - xold) / xnew) < epsilon) {
                break;
            }
            if (iter >= IterMax) {
                System.out.println("Newton-Pratt will not converge");
                xnew = 0;
            }
            if (xnew < 0) {
                System.out.println("Newton-Pratt negative root:  x= " + xnew);
                xnew = 0;
            }
        }
        double det = xnew * xnew - xnew * Mz + Cov_xy;
        double x = (Mxz * (Myy - xnew) - Myz * Mxy) / (det * 2);
        double y = (Myz * (Mxx - xnew) - Mxz * Mxy) / (det * 2);
        double r = Math.sqrt(x * x + y * y + Mz + 2 * xnew);

        double[] centreRadius = { x + centroid[0], y + centroid[1], r };

        System.out.println(centreRadius[0]);

        paintCircle = new Paint();
        paintCircle.setStyle(Paint.Style.STROKE);
        paintCircle.setColor(Color.GREEN);

        paintCircle.setStrokeWidth(5f);

        canvas = new Canvas(bitmap);
        canvas.drawBitmap(bitmap, 0, 0, null);

        canvas.drawCircle((float)centreRadius[0], (float)centreRadius[1], (float)centreRadius[2], paintCircle);

        return bitmap;
    }

    private static double[] Centroid(double[][] points) {
        double[] centroid = new double[2];
        double sumX = 0;
        double sumY = 0;
        int nPoints = points.length;

        for (int n = 0; n < nPoints; n++) {
            sumX += points[n][0];
            sumY += points[n][1];
        }

        centroid[0] = sumX / nPoints;
        centroid[1] = sumY / nPoints;

        System.out.println("centroid[0]: "+centroid[0]);
        System.out.println("centroid[1]: "+centroid[1]);

        return centroid;
    }
}

