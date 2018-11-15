import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import java.io.FileWriter;
import java.io.IOException;

public class Main {

    private static final String mask = "%.6f";
    private static final int a0 = 10;
    private static final int a1 = 40;
    private static int fi = 0;
    //private static final int tNas = 450000; //Время насышение маховика
    private static final float delT = 0.001f; // Щаг
    private static final float MStop = 0.002f; //
    private static final float omega1T = 1; //1/sec^2 - ускорение торможения маховика при сбросе накопленного кинетического момента.
    private static final float Myrd = 0.08f; //нм
    private static final float Im = 0.02f; // кгм2 - момент инерции маховика
    private static final float Io = 20f; // 20кгм2 - момент инерции спутника по каналу тангажа J0
    private static final float Mv = 0.002f; // 1/sec^2 - возмущающее ускорение

    private float X1 = 0.0f;
    private float X2 = 0.0f;
    private float X3 = 0.0f;
    private float X4 = 0.0f;
    private float iX4 = 0.0f;
    private float leftX2 = 0.0f;
    private float rightX2 = 0.0f;
    private boolean tNas = true;

    public Main() throws IOException {
    }

    private float CalculationX1(boolean Mupr) {
        if (Mupr)
            return X1 + X2 * delT;
        else
            return (X2 / delT) + X1;
    }

    private float CalculationX2(boolean Mupr) throws IOException {
        if (Mupr)
            return X2 + delT * (-(Im / Io) * X3 + Mv / Io);
        else {
            leftX2 = (Mv / Io) + (Im / Io) * MStop;
            rightX2 = ((Myrd * fi())/ Io);

  fileWriter.write( "lX2: " + leftX2 + "\t" +
                        "rX2: " + rightX2 + "\t" +
                        "sum: " + (leftX2 - rightX2 ) + "\t" +
                        "X2: " + (((leftX2 - rightX2 ) * delT + X2) * 100000) + " \t" +
                        "X2: " + ((leftX2 - rightX2 ) * delT + X2) + " \n");


            return (leftX2 - rightX2 ) * delT + X2;
        }

    }

    private float CalculationX3(boolean Mupr) {
        if (Mupr)
            return a0 * X1 + a1 * X2;
        else
            return -0.05f;
    }

    private float CalculationX4() {
        return X4 + delT * X3;

    }

    private int fi() {
        return (a0 * X1 + a1 * X2) > 0 ? 1 : -1;
    }

    private boolean changeFi(float fi){
        if (fi > 0 && fi() < 0) {
            return true;
        } else {
            if (fi < 0 && fi() < 0){
                return true;
            } else return false;
        }
    }

    FileWriter fileWriter = new FileWriter("out.txt");

    public XYDataset Start() throws IOException {

        XYSeries X1S = new XYSeries("Угол");
        XYSeries X2S = new XYSeries("Угловая скорость спутника");
        XYSeries X4S = new XYSeries("Угловая скорость маховик");

        int kX1 = 0;
        int kX2 = 0;

        for (int i = 0; i < 1350000; i++) {
            if (i < 450000 ){
                tNas = true;

                X1 = CalculationX1(tNas);
                X2 = CalculationX2(tNas);
                X3 = CalculationX3(tNas);
                X4 = CalculationX4();

                X1S.add(delT * i, X1 * 10);
                X2S.add(delT * i, X2 * 20);
                X4S.add(delT * i, X4 / 250);
            } else {
                fi = fi();
                tNas = false;

                X1 = CalculationX1(tNas);
                X2 = CalculationX2(tNas);
                X3 = CalculationX3(tNas);
                X4 = CalculationX4();

                if (fi != fi()) {
                    if ((kX1 > 25)) {
                        kX1 = 0;
                        X1S.add(delT * i, Math.abs(X1 / 5));
                    } else {
                        kX1++;
                    }
                }

                if (fi != fi()) {
                    if ((kX2 > 65)) {
                        kX2 = 0;
                        X2S.add(delT * i, X2 * 40);
                    } else {
                        kX2++;
                    }
                }
                X4S.add(delT * i, X4 / 250);

            }



        }

        XYSeriesCollection Simul = new XYSeriesCollection();

        Simul.addSeries(X1S);
        Simul.addSeries(X2S);
        Simul.addSeries(X4S);

        return Simul;
    }
}
