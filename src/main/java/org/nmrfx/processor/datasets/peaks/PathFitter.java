/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.nmrfx.processor.datasets.peaks;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import org.apache.commons.math3.optim.PointValuePair;
import org.nmrfx.processor.datasets.peaks.PeakPath.Path;
import org.nmrfx.processor.datasets.peaks.PeakPath.PeakDistance;
import org.nmrfx.processor.optimization.Fitter;

/**
 *
 * @author brucejohnson
 */
public class PathFitter {

    List<Path> currentPaths = new ArrayList<>();
    boolean fit0 = false;
    boolean fitLog = false;
    double[] bestPars;
    double[] parErrs;
    double[][] xVar;
    double[] yVar;
    double[] errVar;
    int[] indices;
    int nPaths;

    class PathFunct implements BiFunction<double[], double[][], Double> {

        double yCalc(double a, double b, double c, double x, double p) {
            double dP = c - a;
            double kD = fitLog ? Math.pow(10.0, b) : b;
            double n1 = p + x + kD;
            double s1 = Math.sqrt(n1 * n1 - 4.0 * x * p);
            double yCalc = a + dP * (n1 - s1) / (2.0 * p);
            return yCalc;

        }

        @Override
        public Double apply(double[] pars, double[][] values) {
            double a = fit0 ? pars[0] : 0.0;
            double b = fit0 ? pars[1] : pars[0];
            double sum = 0.0;
            int n = values[0].length;
            for (int i = 0; i < n; i++) {
                int iOff = indices[i];
                double c = fit0 ? pars[2 + iOff] : pars[1 + iOff];
                double x = values[0][i];
                double p = values[1][i];
                double y = values[2][i];
                double yCalc = yCalc(a, b, c, x, p);

                double delta = yCalc - y;
                sum += delta * delta;

            }
            double value = Math.sqrt(sum / n);
            return value;
        }

        public double[] getGuess(double[] x, double[] y) {
            int nPars = 1 + nPaths;

            double[] result = new double[nPars];
            for (int iPath = 0; iPath < nPaths; iPath++) {
                double yMax = Fitter.getMaxValue(y, indices, iPath);
                double yAtMinX = Fitter.getYAtMinX(x, y, indices, iPath);
                double xMid = Fitter.getMidY0(x, y, indices, iPath);
                result[1 + iPath] = yMax;
                result[0] += fitLog ? Math.log10(xMid) : xMid;
            }
            result[0] /= nPaths;
            return result;
        }

        public double[][] getSimValues(double[] pars, double first, double last, int n, double p) {
            double a = fit0 ? pars[0] : 0.0;
            double b = fit0 ? pars[1] : pars[0];
            double c = fit0 ? pars[2] : pars[1];

            double[][] result = new double[2][n];
            double delta = (last - first) / (n - 1);
            for (int i = 0; i < n; i++) {
                double x = first + delta * i;
                double y = yCalc(a, b, c, x, p);
                result[0][i] = x;
                result[1][i] = y;
            }
            return result;
        }

    }

    public double[] getPars() {
        return bestPars;
    }

    public double[] getParErrs() {
        return parErrs;
    }

    public double[][] getSimValues(double[] pars, double first, double last, int n, double p) {
        PathFunct fun = new PathFunct();
        return fun.getSimValues(pars, first, last, n, p);
    }

    public double[][] getX() {
        return xVar;
    }

    public double[] getY() {
        return yVar;
    }

    public void setup(PeakPath peakPath, Path path) {
        currentPaths.clear();
        currentPaths.add(path);
        double[] iVar0 = peakPath.concentrations;
        double[] iVar1 = peakPath.binderConcs;
        List<PeakDistance> peakDists = path.getPeakDistances();
        int i = 0;
        double errValue = 0.1;
        List<double[]> values = new ArrayList<>();
        for (PeakDistance peakDist : peakDists) {
            if (peakDist != null) {
                double[] row = {iVar0[i], iVar1[i], peakDist.distance, errValue};
                System.out.printf("%2d %.3f %.3f %.3f %.3f\n", i, row[0], row[1], row[2], row[3]);
                values.add(row);
            }
            i++;
        }
        int n = values.size();
        xVar = new double[2][n];
        yVar = new double[n];
        errVar = new double[n];
        indices = new int[n];
        i = 0;
        for (double[] v : values) {
            xVar[0][i] = v[0];
            xVar[1][i] = v[1];
            yVar[i] = v[2];
            errVar[i] = v[3];
            indices[i] = 0;
            i++;
        }
        nPaths = 1;
    }

    public void setup(PeakPath peakPath, List<Path> paths) {
        currentPaths.clear();
        currentPaths.addAll(paths);
        double[] iVar0 = peakPath.concentrations;
        double[] iVar1 = peakPath.binderConcs;
        List<double[]> values = new ArrayList<>();
        List<Integer> pathIndices = new ArrayList<>();
        int iPath = 0;
        for (Path path : paths) {
            List<PeakDistance> peakDists = path.getPeakDistances();
            int i = 0;
            double errValue = 0.1;

            for (PeakDistance peakDist : peakDists) {
                if (peakDist != null) {
                    double[] row = {iVar0[i], iVar1[i], peakDist.distance, errValue};
                    System.out.printf("%2d %.3f %.3f %.3f %.3f\n", i, row[0], row[1], row[2], row[3]);
                    values.add(row);
                    pathIndices.add(iPath);
                }
                i++;
            }
            iPath++;
        }
        int n = values.size();
        xVar = new double[2][n];
        yVar = new double[n];
        errVar = new double[n];
        indices = new int[n];

        int i = 0;
        for (double[] v : values) {
            xVar[0][i] = v[0];
            xVar[1][i] = v[1];
            yVar[i] = v[2];
            errVar[i] = v[3];
            indices[i] = pathIndices.get(i);
            i++;
        }
        nPaths = paths.size();

    }

    public Fitter fit() throws Exception {
        PathFunct fun = new PathFunct();
        Fitter fitter = Fitter.getArrayFitter(fun::apply);
        fitter.setXYE(xVar, yVar, yVar);
        double[] guess = fun.getGuess(xVar[0], yVar);
        double[] lower = new double[guess.length];
        double[] upper = new double[guess.length];
        int iG = 0;
        if (fit0) {
            lower[0] = -guess[2] * 0.1;
            upper[0] = guess[0] + guess[2] * 0.1;
            iG = 1;
        }
        lower[iG] = guess[iG] / 4.0;
        upper[iG] = guess[iG] * 3.0;
        for (int iPath = 0; iPath < nPaths; iPath++) {
            lower[iG + 1 + iPath] = guess[iG + 1 + iPath] / 2.0;
            upper[iG + 1 + iPath] = guess[iG + 1 + iPath] * 2.0;
        }
        for (int k = 0; k < guess.length; k++) {
            System.out.printf("%.3f %.3f %.3f\n", lower[k], guess[k], upper[k]);
        }

        PointValuePair result = fitter.fit(guess, lower, upper, 10.0);
        System.out.println(result.getValue());
        bestPars = result.getPoint();
        parErrs = fitter.bootstrap(result.getPoint(), 300);
        for (int iPath = 0; iPath < nPaths; iPath++) {
            Path path = currentPaths.get(iPath);
            double[] pars = {bestPars[0], bestPars[iPath + 1]};
            double[] errs = {parErrs[0], parErrs[iPath + 1]};
            path.setFitPars(pars);
            path.setFitErrs(errs);
        }
        return fitter;
    }
}