package com.example.democv;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.Vector;
import Jama.Matrix;

public class kinks {

    private Mat green, rgb, edge_x, edge_y, corner;
    private Matrix Ax, Bx, Cx, Zx, Ay, By, Cy, Zy;
    private int[] LXnew4, LYnew4,qx,qy,qx1,qy1, LXnew, LYnew;
    private Bitmap sp;
    private Vector<float[]> DX;
    private List<Integer> LXnew3 = new ArrayList<>();
    private List<Integer> LYnew3 = new ArrayList<>();

    Paint paintCircle;
    Canvas canvas;

    private double[] S;

    double x[], y[], step;
    int n = 0, t[];

    List<Integer> Lx = new ArrayList<>();
    List<Integer> Ly = new ArrayList<>();

    kinks(Bitmap argb) {

        List<Mat> RGB = new ArrayList(3);

        rgb = new Mat();
        green = new Mat();

        sp = Bitmap.createBitmap(argb);

        Utils.bitmapToMat(argb, rgb);
        Core.split(rgb, RGB);

        green = RGB.get(1);
    }


    Bitmap getBitmap(Mat img) {

        Bitmap imfilt = Bitmap.createBitmap(img.cols(), img.rows(), Bitmap.Config.ARGB_8888);

        Utils.matToBitmap(img, imfilt);

        return imfilt;
    }

    Bitmap getSp() {
        return sp;
    }

    Bitmap getGreen() {

        return getBitmap(green);
    }

    Bitmap getEdge() {

        return getBitmap(edge_y);
    }

    Bitmap getCorner() {
        return getBitmap(corner);
    }


    public void run_spline() {
        sigma();
        kernel();
        convolve_spline();
        Log.e("done","done");
    }

    public void run_circle() {
        sigma();
        kernel();
        convolve_circle();
        Log.e("done","done");
    }


    void sigma() {
        int S_B = 2;
        double S_Step = 1.2;
        double S_A;
        S = new double[5];

        for (int i = 0; i < 5; i++) {
            S_A = Math.pow(S_Step, i) * S_B;
            S[i] = S_A;
        }
    }

    void kernel() {

        DX = new Vector<>();

        for (int k = 1; k < S.length - 1; k++) {
            double S_I = S[k];
            double S_D = .7 * S_I;

            int x1 = (int) Math.round(3 * S_D);
            int[] x = new int[(2 * x1) + 1];
            for (int a = -x1; a < x1 + 1; a++) {
                x[a + x1] = a;
            }

            double Dx2;
            double pi = 3.14;
            Dx2 = Math.pow(S_D, 3) * Math.sqrt(2 * pi);
            double Dx3 = 2 * S_D * S_D;
            double[] Dx4 = new double[x.length];
            double[] Dx5 = new double[x.length];
            double[] Dx = new double[x.length];

            for (int b = 0; b < x.length; b++) {
                Dx4[b] += -x[b] * x[b];
                Dx5[b] = Dx4[b] / Dx3;
                Dx[b] = x[b] * Math.exp(Dx5[b]) / Dx2;
            }

            float[] DXt = new float[x.length];
            for (int b = 0; b < x.length; b++) {
                DXt[b] = (float) Dx[b];
            }

            DX.add(DXt);
        }
    }

    public void convolve_spline() {

        for (int i = 0; i < 1; i++) {

            Mat dx = new Mat(1, DX.get(i).length, CvType.CV_32F);

            for (int j = 0; j < DX.get(i).length; j++) {
                dx.put(0, j, DX.get(i)[j]);
            }

            Mat dy = dx.t(); //Transpose Matrix

            edge_x = new Mat(green.cols(), green.rows(), CvType.CV_32F);
            edge_y = new Mat(green.cols(), green.rows(), CvType.CV_32F);

            Imgproc.filter2D(green, edge_x, green.depth(), dx);
            Imgproc.filter2D(green, edge_y, green.depth(), dy);

            //Core.multiply(edge_x, new Scalar(255), edge_x);
            //Core.multiply(edge_y, new Scalar(255), edge_y);

            Mat DXX = new Mat();
            Mat DYY = new Mat();
            Mat DXY = new Mat();

            Core.multiply(edge_x, edge_x, DXX);
            Core.multiply(edge_x, edge_y, DXY);
            Core.multiply(edge_y, edge_y, DYY);

            Mat Qpix = new Mat(DXX.rows(), DXX.cols(), DXX.type());
            corner = new Mat();
            rgb.copyTo(corner);

            int k_size = Math.max(1, (int) (6 * S[i] + 1));

            Imgproc.GaussianBlur(DXX, DXX, new Size(k_size, k_size), S[i]);
            Imgproc.GaussianBlur(DXY, DXY, new Size(k_size, k_size), S[i]);
            Imgproc.GaussianBlur(DYY, DYY, new Size(k_size, k_size), S[i]);

            double dxx, dyy, dxy, max = 0;

            for (int x = 0; x < DXX.rows(); x++) {
                for (int y = 0; y < DXX.cols(); y++) {
                    dxx = DXX.get(x, y)[0];
                    dxy = DXY.get(x, y)[0];
                    dyy = DYY.get(x, y)[0];

                    double det = dxx * dyy - dxy * dxy;
                    double trace = dxx + dyy;

                    Qpix.put(x, y, det - 0.04f * (trace * trace));

                    if (Qpix.get(x, y)[0] > max) {
                        max = Qpix.get(x, y)[0];
                    }
                }
            }


            for (int x = 1; x < Qpix.rows() - 1; x++) {

                for (int y = 1; y < Qpix.cols() - 1; y++) {

                    if (Qpix.get(x, y)[0] <= 0.0001 * max)
                        continue;

                    Boolean locMax = true;

                    for (int xx = x - 1; xx <= x + 1; xx++) {

                        for (int yy = y - 1; yy <= y + 1; yy++) {

                            if (xx < 0 || yy < 0 || xx > Qpix.rows() - 1 || yy > Qpix.cols() - 1)
                                continue;

                            if (Qpix.get(x, y)[0] > Qpix.get(xx, yy)[0]) {
                                locMax = false;
                                break;
                            }
                        }
                    }

                    if (locMax) {

                        for (int xx = x - 15; xx <= x + 15; xx++) {

                            for (int yy = y - 15; yy <= y + 15; yy++) {

                                if (xx < 0 || yy < 0 || xx > Qpix.rows() - 1 || yy > Qpix.cols() - 1)
                                    continue;

                                if (green.get(xx, yy)[0] == 0) {
                                    locMax = false;
                                    break;
                                }
                            }

                        }

                        if(locMax) {
                            Lx.add(x);
                            Ly.add(y);
                        }
                        //Imgproc.circle(corner, new Point(y, x), 1, new Scalar(255, 255, 0), -1);
                    }
                }
            }

        }

        int[] LX = new int[Lx.size()];
        int[] LY = new int[Lx.size()];
        for (int i = 0; i < Lx.size(); i++) {
            LX[i] = Lx.get(i);
            LY[i] = Ly.get(i);
            //ip1.putPixel(LX[i],LY[i], ((255 & 0xff) <<16) + ((255 & 0xff) << 8) + ( 255 & 0xff));

        }
        LXnew = new int[LX.length];
        int[] index = new int[LX.length];
        LYnew = new int[LX.length];
        for (int i = 0; i < LX.length; i++) {
            LXnew[i] = LX[i];
        }

        Arrays.sort(LXnew);
        for (int i = 0; i < LX.length; i++) {
            for (int j = 0; j < LX.length; j++) {
                if (LXnew[i] == LX[j]) {
                    index[i] = j;
                }
            }
        }
        for (int i = 0; i < LX.length; i++) {
            LYnew[i] = LY[index[i]];
        }

        for (int i = 0; i < LXnew.length - 1; i++) {
            if ((Math.abs(LXnew[i + 1] - LXnew[i]) < 50) && (Math.abs(LYnew[i + 1] - LYnew[i]) < 50)) {
                LXnew[i + 1] = 0;
                LYnew[i + 1] = 0;
            } else {
                LXnew[i + 1] = LXnew[i + 1];
                LYnew[i + 1] = LYnew[i + 1];
            }
        }

        LXnew = RemoveZero(LXnew);
        LYnew = RemoveZero(LYnew);

        for (int i = 0; i < LXnew.length - 2; i++) {
            if ((LXnew[i + 2] == LXnew[i]) || (LYnew[i + 2] == LYnew[i]) || (LXnew[i + 1] == LXnew[i]) || (LYnew[i + 1] == LYnew[i])) {
                LXnew[i + 1] = 0;
                LYnew[i + 1] = 0;
            } else {
                LXnew[i + 1] = LXnew[i + 1];
                LYnew[i + 1] = LYnew[i + 1];
            }
        }

        LXnew = RemoveZero(LXnew);
        LYnew = RemoveZero(LYnew);

        for (int i = 0; i < LXnew.length - 1; i++) {
            if ((Math.abs(LXnew[i + 1] - LXnew[i]) < 25) && (Math.abs(LYnew[i + 1] - LYnew[i]) < 25)) {
                LXnew[i + 1] = 0;
                LYnew[i + 1] = 0;
            } else {
                LXnew[i + 1] = LXnew[i + 1];
                LYnew[i + 1] = LYnew[i + 1];
            }
        }
        LXnew = RemoveZero(LXnew);
        LYnew = RemoveZero(LYnew);

        List<Integer> LX1 = new ArrayList<>();
        List<Integer> LY1 = new ArrayList<>();

        for (int i = 0; i < LXnew.length; i++) {
            LX1.add(LXnew[i]);
            LY1.add(LYnew[i]);
        }



        int[] color = new int[LXnew.length];
        Random rand = new Random();

        for (int i = 0; i < LXnew.length; i++) {
            color[i] = rand.nextInt(16777215);
        }
        Mat green1 = new Mat(green.rows(),green.cols(), CvType.CV_32F);
        for (int X = 0; X < green.rows(); X++) {
            for (int y = 0; y < green.cols(); y++) {
                int n = 0;
                for (int i = 0; i < LXnew.length ; i++) {
                    if (distance(LXnew[i], X, LYnew[i], y) < distance(LXnew[n], X, LYnew[n], y)) {
                        n = i;
                    }
                }
                green1.put(X,y,color[n]);
            }
        }


        for(int j=1;j<green1.rows()-1;j++) {
            for(int k1=0;k1<color.length;k1++) {
                if((green1.get(10,j)[0]==color[k1]) && (green1.get(LXnew[k1], LYnew[k1])[0]==color[k1])) {
                    LXnew3.add(LXnew[k1]);
                    LYnew3.add(LYnew[k1]);
                }
            }
        }
        for(int j=1;j<green1.rows()-1;j++) {
            for(int k1=0;k1<color.length;k1++) {
                if((green1.get(390,j)[0]==color[k1]) && (green1.get(LXnew[k1], LYnew[k1])[0]==color[k1])) {
                    LXnew3.add(LXnew[k1]);
                    LYnew3.add(LYnew[k1]);
                }
            }
        }
        for(int i=1;i<green1.rows()-1;i++) {
            for(int k1=0;k1<color.length;k1++) {
                if((green1.get(i,10)[0]==color[k1]) && (green1.get(LXnew[k1], LYnew[k1])[0]==color[k1])) {
                    LXnew3.add(LXnew[k1]);
                    LYnew3.add(LYnew[k1]);
                }
            }
        }
        for(int i=1;i<green1.rows()-1;i++) {
            for(int k1=0;k1<color.length;k1++) {
                if((green1.get(i, 390)[0]==color[k1]) && (green1.get(LXnew[k1], LYnew[k1])[0]==color[k1])) {
                    LXnew3.add(LXnew[k1]);
                    LYnew3.add(LYnew[k1]);
                }
            }
        }

//        List<List<Integer>> result1 = Voronoi(green, LXnew, LYnew);

//        LXnew3 = result1.get(0);
//        LYnew3 = result1.get(1);

//        System.out.println(Arrays.toString(LXnew3.toArray()));
//        System.out.println(Arrays.toString(LYnew3.toArray()));

        List<Integer> list = LXnew3;
        List<Integer> list1 = LYnew3;
        List<Integer> newList = RemoveDuplicates(list);
        List<Integer> newList1 = RemoveDuplicates(list1);

        int m = newList.size();
        int n = newList1.size();
        int size = Math.min(m, n);

        LXnew4 = new int[size];
        LYnew4 = new int[size];
        //System.out.print(size);
        for (int i=0;i<size;i++) {
            LXnew4[i] = newList.get(i);
            LYnew4[i] = newList1.get(i);
        }

        List<Double> angle = new ArrayList<>();

        for(int i=0;i<LXnew4.length;i++) {
            double aa = (200-LYnew4[i]);double bb = (200-LXnew4[i]);
            angle.add(Math.toDegrees(Math.atan2(aa,bb)));
        }

        double[] l = new double[angle.size()];
        double[] l1 = new double[angle.size()];
        for(int i=0;i<angle.size();i++) {
            l[i] = angle.get(i);
            l1[i] = angle.get(i);
            if(l[i]<0 & l1[i]<0) {
                l[i] = l[i]+360;l1[i] = l1[i] +360;
            }
        }
        Arrays.sort(l);
        qx = new int[angle.size()];
        int[] index1 = new int[angle.size()];
        qy = new int[angle.size()];

        for(int i=0;i<angle.size();i++) {
            for(int j=0;j<angle.size();j++) {
                if(l[i]==l1[j]) {
                    index1[i] = j;
                }
            }
        }
        for(int i=0;i<angle.size();i++) {
            qx[i] = LXnew4[index1[i]];
            qy[i] = LYnew4[index1[i]];
        }

        for(int i=0;i<qx.length-1;i++) {
            if(distance(qx[i],qx[i+1],qy[i],qy[i+1])<20) {
                qx[i]=0;qy[i]=0;
            }
            else {
                qx[i]=qx[i];qy[i]=qy[i];
            }
        }
        qx = RemoveZero(qx); qy = RemoveZero(qy);

        for(int i=1;i<qx.length-1;i++) {
            double r1 = distance(qx[i-1],qx[i],qy[i-1],qy[i]);
            double r2 = distance(qx[i+1],qx[i],qy[i+1],qy[i]);
            if((r1<100) && (r2<100)) {
                qx[i]=0; qy[i]=0;
            }
            else{
                qx[i] = qx[i]; qy[i]=qy[i];
            }
        }
        qx = RemoveZero(qx); qy = RemoveZero(qy);

        Vector X = new Vector();
        Vector Y = new Vector();

        for (int i = 0; i < qx.length; i++) {
            Imgproc.circle(corner, new Point(qy[i], qx[i]), 3, new Scalar(0, 255, 0), -1);
            Y.add(qy[i]); X.add(qx[i]);
        }

        //Y.add(qy[0]); X.add(qx[0]);
        b_spline b = new b_spline();

        b.init(qx.length,0.001, Y, X,true);
        sp = b.compute_spline(sp,1);
    }


    public void convolve_circle() {

        for (int i = 0; i < 1; i++) {

            Mat dx = new Mat(1, DX.get(i).length, CvType.CV_32F);

            for (int j = 0; j < DX.get(i).length; j++) {
                dx.put(0, j, DX.get(i)[j]);
            }

            Mat dy = dx.t(); //Transpose Matrix

            edge_x = new Mat(green.cols(), green.rows(), CvType.CV_32F);
            edge_y = new Mat(green.cols(), green.rows(), CvType.CV_32F);

            Imgproc.filter2D(green, edge_x, green.depth(), dx);
            Imgproc.filter2D(green, edge_y, green.depth(), dy);

            //Core.multiply(edge_x, new Scalar(255), edge_x);
            //Core.multiply(edge_y, new Scalar(255), edge_y);

            Mat DXX = new Mat();
            Mat DYY = new Mat();
            Mat DXY = new Mat();

            Core.multiply(edge_x, edge_x, DXX);
            Core.multiply(edge_x, edge_y, DXY);
            Core.multiply(edge_y, edge_y, DYY);

            Mat Qpix = new Mat(DXX.rows(), DXX.cols(), DXX.type());
            corner = new Mat();
            rgb.copyTo(corner);

            int k_size = Math.max(1, (int) (6 * S[i] + 1));

            Imgproc.GaussianBlur(DXX, DXX, new Size(k_size, k_size), S[i]);
            Imgproc.GaussianBlur(DXY, DXY, new Size(k_size, k_size), S[i]);
            Imgproc.GaussianBlur(DYY, DYY, new Size(k_size, k_size), S[i]);

            double dxx, dyy, dxy, max = 0;

            for (int x = 0; x < DXX.rows(); x++) {
                for (int y = 0; y < DXX.cols(); y++) {
                    dxx = DXX.get(x, y)[0];
                    dxy = DXY.get(x, y)[0];
                    dyy = DYY.get(x, y)[0];

                    double det = dxx * dyy - dxy * dxy;
                    double trace = dxx + dyy;

                    Qpix.put(x, y, det - 0.04f * (trace * trace));

                    if (Qpix.get(x, y)[0] > max) {
                        max = Qpix.get(x, y)[0];
                    }
                }
            }


            for (int x = 1; x < Qpix.rows() - 1; x++) {

                for (int y = 1; y < Qpix.cols() - 1; y++) {

                    if (Qpix.get(x, y)[0] <= 0.0001 * max)
                        continue;

                    Boolean locMax = true;

                    for (int xx = x - 1; xx <= x + 1; xx++) {

                        for (int yy = y - 1; yy <= y + 1; yy++) {

                            if (xx < 0 || yy < 0 || xx > Qpix.rows() - 1 || yy > Qpix.cols() - 1)
                                continue;

                            if (Qpix.get(x, y)[0] > Qpix.get(xx, yy)[0]) {
                                locMax = false;
                                break;
                            }
                        }
                    }

                    if (locMax) {

                        for (int xx = x - 15; xx <= x + 15; xx++) {

                            for (int yy = y - 15; yy <= y + 15; yy++) {

                                if (xx < 0 || yy < 0 || xx > Qpix.rows() - 1 || yy > Qpix.cols() - 1)
                                    continue;

                                if (green.get(xx, yy)[0] == 0) {
                                    locMax = false;
                                    break;
                                }
                            }

                        }

                        if(locMax) {
                            Lx.add(x);
                            Ly.add(y);
                        }
                        //Imgproc.circle(corner, new Point(y, x), 1, new Scalar(255, 255, 0), -1);
                    }
                }
            }

        }

        int[] LX = new int[Lx.size()];
        int[] LY = new int[Lx.size()];
        for (int i = 0; i < Lx.size(); i++) {
            LX[i] = Lx.get(i);
            LY[i] = Ly.get(i);
            //ip1.putPixel(LX[i],LY[i], ((255 & 0xff) <<16) + ((255 & 0xff) << 8) + ( 255 & 0xff));

        }
        LXnew = new int[LX.length];
        int[] index = new int[LX.length];
        LYnew = new int[LX.length];
        for (int i = 0; i < LX.length; i++) {
            LXnew[i] = LX[i];
        }

        Arrays.sort(LXnew);
        for (int i = 0; i < LX.length; i++) {
            for (int j = 0; j < LX.length; j++) {
                if (LXnew[i] == LX[j]) {
                    index[i] = j;
                }
            }
        }
        for (int i = 0; i < LX.length; i++) {
            LYnew[i] = LY[index[i]];
        }

        for (int i = 0; i < LXnew.length - 1; i++) {
            if ((Math.abs(LXnew[i + 1] - LXnew[i]) < 50) && (Math.abs(LYnew[i + 1] - LYnew[i]) < 50)) {
                LXnew[i + 1] = 0;
                LYnew[i + 1] = 0;
            } else {
                LXnew[i + 1] = LXnew[i + 1];
                LYnew[i + 1] = LYnew[i + 1];
            }
        }

        LXnew = RemoveZero(LXnew);
        LYnew = RemoveZero(LYnew);

        for (int i = 0; i < LXnew.length - 2; i++) {
            if ((LXnew[i + 2] == LXnew[i]) || (LYnew[i + 2] == LYnew[i]) || (LXnew[i + 1] == LXnew[i]) || (LYnew[i + 1] == LYnew[i])) {
                LXnew[i + 1] = 0;
                LYnew[i + 1] = 0;
            } else {
                LXnew[i + 1] = LXnew[i + 1];
                LYnew[i + 1] = LYnew[i + 1];
            }
        }

        LXnew = RemoveZero(LXnew);
        LYnew = RemoveZero(LYnew);

        for (int i = 0; i < LXnew.length - 1; i++) {
            if ((Math.abs(LXnew[i + 1] - LXnew[i]) < 25) && (Math.abs(LYnew[i + 1] - LYnew[i]) < 25)) {
                LXnew[i + 1] = 0;
                LYnew[i + 1] = 0;
            } else {
                LXnew[i + 1] = LXnew[i + 1];
                LYnew[i + 1] = LYnew[i + 1];
            }
        }
        LXnew = RemoveZero(LXnew);
        LYnew = RemoveZero(LYnew);

        List<Integer> LX1 = new ArrayList<>();
        List<Integer> LY1 = new ArrayList<>();

        for (int i = 0; i < LXnew.length; i++) {
            LX1.add(LXnew[i]);
            LY1.add(LYnew[i]);
        }



        int[] color = new int[LXnew.length];
        Random rand = new Random();

        for (int i = 0; i < LXnew.length; i++) {
            color[i] = rand.nextInt(16777215);
        }
        Mat green1 = new Mat(green.rows(),green.cols(), CvType.CV_32F);
        for (int X = 0; X < green.rows(); X++) {
            for (int y = 0; y < green.cols(); y++) {
                int n = 0;
                for (int i = 0; i < LXnew.length ; i++) {
                    if (distance(LXnew[i], X, LYnew[i], y) < distance(LXnew[n], X, LYnew[n], y)) {
                        n = i;
                    }
                }
                green1.put(X,y,color[n]);
            }
        }


        for(int j=1;j<green1.rows()-1;j++) {
            for(int k1=0;k1<color.length;k1++) {
                if((green1.get(10,j)[0]==color[k1]) && (green1.get(LXnew[k1], LYnew[k1])[0]==color[k1])) {
                    LXnew3.add(LXnew[k1]);
                    LYnew3.add(LYnew[k1]);
                }
            }
        }
        for(int j=1;j<green1.rows()-1;j++) {
            for(int k1=0;k1<color.length;k1++) {
                if((green1.get(390,j)[0]==color[k1]) && (green1.get(LXnew[k1], LYnew[k1])[0]==color[k1])) {
                    LXnew3.add(LXnew[k1]);
                    LYnew3.add(LYnew[k1]);
                }
            }
        }
        for(int i=1;i<green1.rows()-1;i++) {
            for(int k1=0;k1<color.length;k1++) {
                if((green1.get(i,10)[0]==color[k1]) && (green1.get(LXnew[k1], LYnew[k1])[0]==color[k1])) {
                    LXnew3.add(LXnew[k1]);
                    LYnew3.add(LYnew[k1]);
                }
            }
        }
        for(int i=1;i<green1.rows()-1;i++) {
            for(int k1=0;k1<color.length;k1++) {
                if((green1.get(i, 390)[0]==color[k1]) && (green1.get(LXnew[k1], LYnew[k1])[0]==color[k1])) {
                    LXnew3.add(LXnew[k1]);
                    LYnew3.add(LYnew[k1]);
                }
            }
        }

//        List<List<Integer>> result1 = Voronoi(green, LXnew, LYnew);

//        LXnew3 = result1.get(0);
//        LYnew3 = result1.get(1);

//        System.out.println(Arrays.toString(LXnew3.toArray()));
//        System.out.println(Arrays.toString(LYnew3.toArray()));

        List<Integer> list = LXnew3;
        List<Integer> list1 = LYnew3;
        List<Integer> newList = RemoveDuplicates(list);
        List<Integer> newList1 = RemoveDuplicates(list1);

        int m = newList.size();
        int n = newList1.size();
        int size = Math.min(m, n);

        LXnew4 = new int[size];
        LYnew4 = new int[size];
        //System.out.print(size);
        for (int i=0;i<size;i++) {
            LXnew4[i] = newList.get(i);
            LYnew4[i] = newList1.get(i);
        }

        List<Double> angle = new ArrayList<>();

        for(int i=0;i<LXnew4.length;i++) {
            double aa = (200-LYnew4[i]);double bb = (200-LXnew4[i]);
            angle.add(Math.toDegrees(Math.atan2(aa,bb)));
        }

        double[] l = new double[angle.size()];
        double[] l1 = new double[angle.size()];
        for(int i=0;i<angle.size();i++) {
            l[i] = angle.get(i);
            l1[i] = angle.get(i);
            if(l[i]<0 & l1[i]<0) {
                l[i] = l[i]+360;l1[i] = l1[i] +360;
            }
        }
        Arrays.sort(l);
        qx = new int[angle.size()];
        int[] index1 = new int[angle.size()];
        qy = new int[angle.size()];

        for(int i=0;i<angle.size();i++) {
            for(int j=0;j<angle.size();j++) {
                if(l[i]==l1[j]) {
                    index1[i] = j;
                }
            }
        }
        for(int i=0;i<angle.size();i++) {
            qx[i] = LXnew4[index1[i]];
            qy[i] = LYnew4[index1[i]];
        }

        for(int i=0;i<qx.length-1;i++) {
            if(distance(qx[i],qx[i+1],qy[i],qy[i+1])<20) {
                qx[i]=0;qy[i]=0;
            }
            else {
                qx[i]=qx[i];qy[i]=qy[i];
            }
        }
        qx = RemoveZero(qx); qy = RemoveZero(qy);

        for(int i=1;i<qx.length-1;i++) {
            double r1 = distance(qx[i-1],qx[i],qy[i-1],qy[i]);
            double r2 = distance(qx[i+1],qx[i],qy[i+1],qy[i]);
            if((r1<100) && (r2<100)) {
                qx[i]=0; qy[i]=0;
            }
            else{
                qx[i] = qx[i]; qy[i]=qy[i];
            }
        }
        qx = RemoveZero(qx); qy = RemoveZero(qy);

        Vector X = new Vector();
        Vector Y = new Vector();

        for (int i = 0; i < qx.length; i++) {
            Imgproc.circle(corner, new Point(qy[i], qx[i]), 3, new Scalar(0, 255, 0), -1);
            Y.add(qy[i]); X.add(qx[i]);
        }

        //Y.add(qy[0]); X.add(qx[0]);
//        b_spline b = new b_spline();
//
//        b.init(qx.length,0.001, Y, X,true);
//        sp = b.compute_spline(sp,1);

        double[][] points = new double[Y.size()][2];

        for (int i = 0; i <Y.size(); i++){
            points[i][0] = qy[i];
            points[i][1] = qx[i];
            System.out.println("qx: "+qx[i]);
            System.out.println("qy: "+qy[i]);
        }

        pratt_circle pc = new pratt_circle();
        sp = pc.prattNewton(sp, points);

    }


//    public List<List<Integer>> Voronoi(Mat green, int[] LXnew, int[] LYnew){
//        int[] color = new int[LXnew.length];
//        Random rand = new Random();
//
//        for (int i = 0; i < LXnew.length; i++) {
//            color[i] = rand.nextInt(16777215);
//        }
//        Mat green1 = new Mat(green.rows(),green.cols(), CvType.CV_32F);
//        for (int X = 0; X < green.rows(); X++) {
//            for (int y = 0; y < green.cols(); y++) {
//                int n = 0;
//                for (int i = 0; i < LXnew.length ; i++) {
//                    if (distance(LXnew[i], X, LYnew[i], y) < distance(LXnew[n], X, LYnew[n], y)) {
//                        n = i;
//                    }
//                }
//                green1.put(X,y,color[n]);
//            }
//        }
//
//        List<java.lang.Integer> LXnew3 = new ArrayList<>();
//        List<java.lang.Integer> LYnew3 = new ArrayList<>();
//
//        for(int j=1;j<green1.rows()-1;j++) {
//            for(int k1=0;k1<color.length;k1++) {
//                if((green1.get(10,j)[0]==color[k1]) && (green1.get(LXnew[k1], LYnew[k1])[0]==color[k1])) {
//                    LXnew3.add(LXnew[k1]);
//                    LYnew3.add(LYnew[k1]);
//                }
//            }
//        }
//        for(int j=1;j<green1.rows()-1;j++) {
//            for(int k1=0;k1<color.length;k1++) {
//                if((green1.get(390,j)[0]==color[k1]) && (green1.get(LXnew[k1], LYnew[k1])[0]==color[k1])) {
//                    LXnew3.add(LXnew[k1]);
//                    LYnew3.add(LYnew[k1]);
//                }
//            }
//        }
//        for(int i=1;i<green1.rows()-1;i++) {
//            for(int k1=0;k1<color.length;k1++) {
//                if((green1.get(i,10)[0]==color[k1]) && (green1.get(LXnew[k1], LYnew[k1])[0]==color[k1])) {
//                    LXnew3.add(LXnew[k1]);
//                    LYnew3.add(LYnew[k1]);
//                }
//            }
//        }
//        for(int i=1;i<green1.rows()-1;i++) {
//            for(int k1=0;k1<color.length;k1++) {
//                if((green1.get(i, 390)[0]==color[k1]) && (green1.get(LXnew[k1], LYnew[k1])[0]==color[k1])) {
//                    LXnew3.add(LXnew[k1]);
//                    LYnew3.add(LYnew[k1]);
//                }
//            }
//        }
//
//        List<Integer> Lxx = new ArrayList<>();
//        List<Integer> Lyy = new ArrayList<>();
//
//        for(int i=0;i<LXnew.length;i++){
//            Lxx.add(LXnew[i]);
//            Lyy.add(LYnew[i]);
//        }
//
//        List<List<Integer>> result = new ArrayList<>();
//        result.add(Lxx);
//        result.add(Lyy);
//
//        return result;
//    }

    public static <Integer> List<Integer> RemoveDuplicates(List<Integer> list)
    {
        Set<Integer> set = new LinkedHashSet<>();
        set.addAll(list);
        list.clear();
        list.addAll(set);
        return list;
    }


    public static int[] RemoveZero(int[] L) {
        Integer[] numbers = new Integer[L.length];
        for (int i = 0; i < L.length; i++) {
            numbers[i] = L[i];
        }
        List<Integer> list = new ArrayList<Integer>(Arrays.asList(numbers));
        list.removeAll(Arrays.asList(Integer.valueOf(0)));
        numbers = list.toArray(new Integer[list.size()]);
        int[] point = new int[list.size()];
        for (int i = 0; i < list.size(); i++) {
            point[i] = numbers[i];
        }
        return point;
    }

    public double distance(int x1, int x2, int y1, int y2) {
        double d;
        d = Math.sqrt((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2));
        return d;
    }
}
