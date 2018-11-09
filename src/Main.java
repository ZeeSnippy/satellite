import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

public class Main {

    private static final String mask = "%.6f";
    private static final int a0 = 10;
    private static final int a1 = 40;
    private static int change = 0;
    private static final int tNas = 450000; //Время насышение маховика
    private static final float delT = 0.001f; // Щаг
    public static final float omega1T = 1; //1/sec^2 - ускорение торможения маховика при сбросе накопленного кинетического момента.
    public static final float Myrd = 0.08f; //нм
    private static final float Imx = 0.02f; // кгм2 - момент инерции маховика
    private static final float Io = 20f; // 20кгм2 - момент инерции спутника по каналу тангажа J0
    private static final float Mv = 0.002f; // 1/sec^2 - возмущающее ускорение

    private float X1 = 0.0f;
    private float X2 = 0.0f;
    private float X3 = 0.0f;
    private float X4 = 0.0f;

    private float mahCalculationX1() {
        return X1 + X2 * delT;
    }

    private float mahCalculationX2() {
        return X2 + delT * (-(Imx / Io) * X3 + Mv / Io);
    }

    private float mahCalculationX3() {
        return a0 * X1 + a1 * X2;
    }

    private float mahCalculationX4() {
        return X4 + delT * X3;
    }

    private float jetpackCalculationX1() {
        return (X2 / delT) + X1;
    }

    private float jetpackCalculationX2() {
        return ((Mv / Io) - (Imx / Io) * omega1T - ((Myrd * fi())/ Io)) * delT + X2 ;
    }

    private float jetpackCalculationX4() {
        return X4 + delT * X3 / 165;
    }

    private int fi() {
        return (a0 * X1 + a1 * X2) > 0 ? 1 : -1;
    }


    public XYDataset Start() {
        XYSeries X1S = new XYSeries("Угол");
        XYSeries X2S = new XYSeries("Скорость");
        XYSeries X3S = new XYSeries("Угол дискрет.");
        XYSeries X4S = new XYSeries("Линейная скорость");
        int k = 0;
        for (int i = 0; i < 1350000; i++) {
            if (i < tNas){
                X1 = mahCalculationX1();
                X2 = mahCalculationX2();
                X3 = mahCalculationX3();
                X4 = mahCalculationX4();

                X1S.add(delT * i, X1 * 10);
                X2S.add(delT * i, X2 * 20);
                X3S.add(delT * i, X3 * 1);
                X4S.add(delT * i, X4 / 250);
            } else {
                change = fi();

                X1 = jetpackCalculationX1();
                X2 = jetpackCalculationX2();
                X3 = mahCalculationX3();
                X4 = jetpackCalculationX4();

                if (change != fi()){
                    if (k > 65) {
                        k = 0;
                        //System.out.printf(fi()+"");
                        X1S.add(delT * i, Math.abs(X1 / 5));
                        X2S.add(delT * i, X2 * 20);
                        X3S.add(delT * i, Math.abs(X3 / 45));
                        if (X4 > 0 ) X4S.add(delT * i, X4 / 250);
                    } else {
                        k++;
                    }
                }

            }



//            System.out.printf("X1: " + String.format(mask, X1) + "\t" +
//                              "X2: " + String.format(mask, X2) + "\t" +
//                              "X3: " + String.format(mask, X3) + "\t i: " + i + "\n");

        }

        XYSeriesCollection Simul = new XYSeriesCollection();

        Simul.addSeries(X1S);
        Simul.addSeries(X2S);
        Simul.addSeries(X3S);
        Simul.addSeries(X4S);

        return Simul;
    }
}
