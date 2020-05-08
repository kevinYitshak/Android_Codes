package com.example.democv;

import android.graphics.Bitmap;
import android.graphics.Color;

import org.opencv.core.Core;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.CvType;

import android.provider.ContactsContract;
import android.util.Log;
import android.widget.ImageView;
import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;
import java.lang.Math;
import java.util.Set;
import java.util.LinkedHashSet;


public class harris {


    public Bitmap rgb2green(Bitmap rgb) {

        Bitmap im = Bitmap.createBitmap(rgb.getWidth(), rgb.getHeight(), Bitmap.Config.ARGB_8888);

        //Bitmap convx = Bitmap.createBitmap(rgb.getWidth(), rgb.getHeight(), rgb.getConfig());
        int w = im.getWidth();
        int h = im.getHeight();

        for (int i=0; i<w;i++){
            for (int j=0; j<h;j++){
                int x = rgb.getPixel(i,j) & 0xFF00FF00;
                //im.setPixel(i,j,rgb.getPixel(i,j) & 0xFF00FF00);
                im.setPixel(i, j, Color.argb(Color.alpha(im.getPixel(i, j)), x, x, x));
            }
        }

        //int[] k = {-5,1,5};
        //Bitmap cx = convolution(im,k);

        int[][] intArray = new int[][]{{1,0,1},{1,0,1},{1,0,1}};
        //System.out.println(intArray[0][2]);

        Mat kernel = new Mat(3,3,CvType.CV_8UC1);
        for (int i=0;i<kernel.rows();i++){
            for (int j=0;j<kernel.cols();j++){
                kernel.put(i,j,intArray[i][j]);
            }
        }

        Mat mat = new Mat();
        Mat mat1 = new Mat();

        Bitmap im_mat = Bitmap.createBitmap(im.getWidth(), im.getHeight(), Bitmap.Config.ARGB_8888);
        Utils.bitmapToMat(im_mat, mat);

        Imgproc.filter2D(mat,mat1,-1,kernel);

        Bitmap imfilt = Bitmap.createBitmap(mat.cols(), mat.rows(), Bitmap.Config.ARGB_8888);

        Utils.matToBitmap(mat1,imfilt);


        return imfilt;
    }

    public Bitmap kinks(Bitmap rgb) {

        //Imgproc.filter2D(mat, edge, mat.depth(), kernel);

        //Imgproc.blur(mat,edge,new Size(5, 5));
        //Imgproc.GaussianBlur(mat,edge,new Size(5,5), 5);

        //Bitmap imfilt = Bitmap.createBitmap(mat.cols(), mat.rows(), Bitmap.Config.ARGB_8888);
        //Utils.matToBitmap(edge,imfilt);

        Bitmap im = Bitmap.createBitmap(rgb.getWidth(), rgb.getHeight(), Bitmap.Config.ARGB_8888);
        Bitmap im1 = Bitmap.createBitmap(rgb.getWidth(), rgb.getHeight(), Bitmap.Config.ARGB_8888);

        Mat im_mat = new Mat();
        Utils.bitmapToMat(rgb,im_mat);
        //Imgproc.cvtColor(im_mat,im_mat,Imgproc.COLOR_BGR2GRAY);
        //Utils.matToBitmap(im_mat,im);

        for (int i=0;i<im1.getWidth();i++){
            for (int j=0;j<im1.getHeight();j++){
                int x = im.getPixel(i,j) & 0xFFFF0000;
                im1.setPixel(i,j,Color.argb(Color.alpha(im.getPixel(i, j)), x, x, x));
            }
        }
        Utils.matToBitmap(im_mat,im1);

        //Utils.matToBitmap(im_32,im2);

        int[][] intArray = new int[][]{{-1,-1,-1},{0,0,0},{1,1,1}};
        //System.out.println(intArray[0][2]);

        Mat kernel = new Mat(3,3,CvType.CV_8UC1);
        for (int i=0;i<kernel.rows();i++){
            for (int j=0;j<kernel.cols();j++){
                kernel.put(i,j,intArray[i][j]);
            }
        }

        Mat mat_x = new Mat(im.getWidth(),im.getHeight(),CvType.CV_8UC3);
        Mat mat_y = new Mat(im.getWidth(),im.getHeight(),CvType.CV_8UC3);
        Utils.bitmapToMat(rgb, mat_x);
        Utils.bitmapToMat(rgb, mat_y);

        Mat edge_x = new Mat(im.getWidth(),im.getHeight(),CvType.CV_8UC3);
        Mat edge_y = new Mat(im.getWidth(),im.getHeight(),CvType.CV_8UC3);

        int[] DX = kernel();
        Mat dx = new Mat(1, DX.length, CvType.CV_8UC1);
        for (int i = 0; i < DX.length; i++) {
            dx.put(i, DX[i]);
        }

        Mat dx1 = new Mat(DX.length, 1, CvType.CV_8UC1);
        for (int i = 0; i < DX.length; i++) {
            dx1.put(i, DX[i]);
        }


        Imgproc.filter2D(im_mat, edge_x, mat_x.depth(), kernel);
        Imgproc.filter2D(im_mat, edge_y, mat_y.depth(), dx1);


        Bitmap imfilt_x = Bitmap.createBitmap(mat_x.cols(), mat_x.rows(), Bitmap.Config.ARGB_8888);
        Bitmap imfilt_y = Bitmap.createBitmap(mat_y.cols(), mat_y.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(edge_x,imfilt_x);
        Utils.matToBitmap(edge_y,imfilt_y);

        /*Mat blur_x = new Mat();
        Mat blur_y = new Mat();
        Imgproc.GaussianBlur(edge_x,blur_x,new Size(5,5), 5);
        Imgproc.GaussianBlur(edge_y,blur_y,new Size(5,5), 5);


        Bitmap imfilt_x = Bitmap.createBitmap(mat_x.cols(), mat_x.rows(), Bitmap.Config.ARGB_8888);
        Bitmap imfilt_y = Bitmap.createBitmap(mat_y.cols(), mat_y.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(blur_x,imfilt_x);
        Utils.matToBitmap(blur_y,imfilt_y);*/

        return im1;
    }

    public static int[] kernel(){
        int S_B = 2; double S_Step = 1.2;
        //int S_no_of_step = 5;
        double S_A;
        double[] S = new double[5];
        for(int i=0;i<5;i++) {
            S_A = Math.pow(S_Step, i)*S_B;
            S[i] = S_A;
        }

        double S_I = S[2];
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

        int[] DX = new int[x.length];
        for (int b = 0; b < x.length; b++) {
            DX[b] = (int) Dx[b];
            //System.out.println(DX);
        }
        return DX;
    }



    public static int[] RemoveZero(int[] L){
        Integer[] numbers = new Integer[L.length];
        for(int i=0;i<L.length;i++) {
            numbers[i] = L[i];
        }
        List<Integer> list = new ArrayList<Integer>(Arrays.asList(numbers));
        list.removeAll(Arrays.asList(Integer.valueOf(0)));
        numbers = list.toArray(new Integer[list.size()]);
        int[] point = new int[list.size()];
        for(int i=0;i<list.size();i++) {
            point[i] = numbers[i];
        }
        return point;
    }

    public static <Integer> List<Integer> RemoveDuplicates(List<Integer> list)
    {
        Set<Integer> set = new LinkedHashSet<Integer>();
        set.addAll(list);
        list.clear();
        list.addAll(set);
        return list;
    }

    static double distance(int x1, int x2, int y1, int y2) {
        double d;
        d = Math.sqrt((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2));
        return d;
    }

    static boolean LocalMax(Bitmap im, int u, int v){
        int w = im.getWidth();
        int h = im.getHeight();

        if (u <= 0 || u >= w-1 || v <=0 || v >= h-1){
            return false;
        }

        else {
            int[] pix = new int[w * h];
            im.getPixels(pix, 0, w, 0, 0, w, h);
            int i0 = (v-1)*w+u, i1 = v*w+u, i2 = (v+1)*w+u;

            int cp = pix[i1];
            return cp > pix[i0-1] && cp > pix[i0] && cp > pix[i0+1] && cp > pix[i1-1] && cp > pix[i1+1] && cp > pix[i2-1] && cp > pix[i2] && cp > pix[i2+1] ;

        }
    }


    public Bitmap convolution(Bitmap gray, int[] xMatrix) {

        Bitmap edge = Bitmap.createBitmap(gray.getWidth(), gray.getHeight(), gray.getConfig());

        int sum;

        for (int i = 1; i < gray.getHeight() - 1; i++) {
            for (int j = 1; j < gray.getWidth() - 1; j++) {
                sum = 0;

                for (int l = i - 1, a = 0; l <= i + 1; l++, a++) {
                    sum += Color.red(gray.getPixel(l, j)) * xMatrix[a];
                }

                if (sum < 0) sum = 0;
                if (sum > 255) sum = 255;
                edge.setPixel(j, i, Color.argb(Color.alpha(gray.getPixel(j, i)), sum, sum, sum));
            }
        }

        return edge;
    }

}
