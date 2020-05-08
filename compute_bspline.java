package com.example.democv;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import java.util.Random;
import java.util.Vector;
import Jama.Matrix;

public class compute_bspline {

    double x[], y[], step;
    int n = 0, t[];

    Matrix Ax, Bx, Cx, Zx, Ay, By, Cy, Zy;
    Paint paintCurve;
    Canvas canvas;

    void init(int N, double S, Vector X, Vector Y, Boolean c) {
        Vector A = (Vector) X.clone();
        Vector B = (Vector) Y.clone();

        if(c) {
            for(int i = 0; i < N - 1; i++) {
                A.add(X.get(i));
                B.add(Y.get(i));
            }

            N = A.size();
        }

        n = N;
        step = S;
        x = new double[N];
        y = new double[N];
        t = new int[N];

        Ax = new Matrix(N - 2, N - 2);
        Bx = new Matrix(N - 2, 1);
        Cx = new Matrix(N - 2, 1);
        Zx = new Matrix(N, 1);

        Ay = new Matrix(N - 2, N - 2);
        By = new Matrix(N - 2, 1);
        Cy = new Matrix(N - 2, 1);
        Zy = new Matrix(N, 1);

        for (int i = 0; i < N; i++) {
            x[i] = (double) A.get(i);
            y[i] = (double) B.get(i);
            t[i] = i;
        }

        //t[N - 1] = t[1];

        paintCurve = new Paint();
        if(c)
            paintCurve.setColor(Color.YELLOW);
        else
            paintCurve.setColor(Color.WHITE);

        paintCurve.setStrokeWidth(10f);

        /*Random r = new Random();
        int rand = r.nextInt(5 - 1) + 1;

        switch (rand){
            case 0: paintCurve.setColor(Color.YELLOW);
                    break;

            case 1: paintCurve.setColor(Color.DKGRAY);
                    break;

            case 2: paintCurve.setColor(Color.WHITE);
                    break;

            case 3: paintCurve.setColor(Color.CYAN);
                    break;

            case 4: paintCurve.setColor(Color.MAGENTA);
                    break;

                    default: paintCurve.setColor(Color.GRAY);
        }*/
    }

    private double b_i_x(int i) {
        return (x[i + 1] - x[i]);
    }

    private double b_i_y(int i) {
        return (y[i + 1] - y[i]);
    }

    private double u_i(double b0, double b1) {
        return 6 * (b1 - b0);
    }

    Bitmap compute_spline(Bitmap bitmap, int dub) {

        double ux, uy;

        for (int i = 0; i < n - 1; i++) {

            if (i > 0 && i < n - 1) {
                ux = u_i(b_i_x(i - 1), b_i_x(i));
                uy = u_i(b_i_y(i - 1), b_i_y(i));

                Cx.set(i - 1, 0, ux);
                Cy.set(i - 1, 0, uy);

                Ax.set(i - 1, i - 1, 4);
                Ay.set(i - 1, i - 1, 4);

                if (i - 2 >= 0) {
                    Ax.set(i - 1, i - 2, 1);
                    Ay.set(i - 1, i - 2, 1);
                }
                if (i < n - 2) {
                    Ax.set(i - 1, i, 1);
                    Ay.set(i - 1, i, 1);
                }
            }
        }

        if (Ax.det() == 0 || Ay.det() == 0)
            return null;

        Bx = Ax.solve(Cx);
        By = Ay.solve(Cy);

        double ea, eb, ec, ed, ee, ef, eg, eh, ex;
        double qa, qc, qe, qg, qy;

        for (int i = 1; i < n - 1; i++) {
            Zx.set(i, 0, Bx.get(i - 1, 0));
            Zy.set(i, 0, By.get(i - 1, 0));
        }

        canvas = new Canvas(bitmap);
        canvas.drawBitmap(bitmap, 0, 0, null);
        double px = x[0], py = y[0];

        for (int i = 0; i < n - 1; i++) {

            ea = Zx.get(i + 1, 0) / 6;
            qa = Zy.get(i + 1, 0) / 6;

            ec = Zx.get(i, 0) / 6;
            qc = Zy.get(i, 0) / 6;

            ee = x[i + 1] - (Zx.get(i + 1, 0) / 6);
            qe = y[i + 1] - (Zy.get(i + 1, 0) / 6);

            eg = x[i] - (Zx.get(i, 0) / 6);
            qg = y[i] - (Zy.get(i, 0) / 6);

            for (double j = t[i] + step; j <= t[i + 1]; j += step) {

                eb = Math.pow(j - t[i], 3);

                ed = Math.pow(t[i + 1] - j, 3);

                ef = j - t[i];

                eh = t[i + 1] - j;

                ex = (ea * eb) + (ec * ed) + (ee * ef) + (eg * eh);
                qy = (qa * eb) + (qc * ed) + (qe * ef) + (qg * eh);

                if(i > n / 2 - 2 && dub == 1)
                    canvas.drawLine((float) px, (float) py, (float) ex, (float) qy, paintCurve);
                else if(dub == 0)
                    canvas.drawLine((float) px, (float) py, (float) ex, (float) qy, paintCurve);

                px = ex;
                py = qy;
            }
        }
        canvas.drawLine((float) px, (float) py, (float) x[n - 1], (float) y[n - 1], paintCurve);
        return bitmap;
    }
}

