package org.spark.opendl.downpourSGD.hLayer;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.Serializable;
import java.io.Writer;

import org.jblas.DoubleMatrix;
import org.spark.opendl.downpourSGD.SGDPersistable;
import org.spark.opendl.downpourSGD.SGDTrainConfig;
import org.spark.opendl.util.MathUtil;

public abstract class HiddenLayer implements SGDPersistable, Serializable {
    private static final long serialVersionUID = 1L;
    protected int n_visible;
    protected int n_hidden;
    protected DoubleMatrix w;
    protected DoubleMatrix hbias;
    protected DoubleMatrix vbias;

    public HiddenLayer(int _n_in, int _n_out) {
        this(_n_in, _n_out, null, null);
    }

    public HiddenLayer(int _n_in, int _n_out, double[][] _w, double[] _b) {
        n_visible = _n_in;
        n_hidden = _n_out;

        if (null == _w) {
            w = new DoubleMatrix(n_hidden, n_visible);
            double a = 1.0 / n_visible;
            for (int i = 0; i < n_hidden; i++) {
                for (int j = 0; j < n_visible; j++) {
                    w.put(i, j, MathUtil.uniform(-a, a));
                }
            }
        } else {
            w = new DoubleMatrix(_w);
        }

        if (null == _b) {
            this.hbias = new DoubleMatrix(n_hidden);
        } else {
            this.hbias = new DoubleMatrix(_b);
        }
        vbias = new DoubleMatrix(n_visible);
    }

    public final DoubleMatrix sigmod_output(DoubleMatrix input) {
        DoubleMatrix ret = input.mmul(w.transpose()).addiRowVector(hbias);
        MathUtil.sigmod(ret);
        return ret;
    }

    public DoubleMatrix getW() {
        return w;
    }

    public DoubleMatrix getHBias() {
        return hbias;
    }

    public DoubleMatrix getVBias() {
        return vbias;
    }

    public int getVisible() {
        return n_visible;
    }

    public int getHidden() {
        return n_hidden;
    }

    @Override
    public final void read(DataInput in) throws IOException {
        n_visible = in.readInt();
        n_hidden = in.readInt();
        for (int i = 0; i < n_hidden; i++) {
            for (int j = 0; j < n_visible; j++) {
                w.put(i, j, in.readDouble());
            }
        }
        for (int i = 0; i < n_hidden; i++) {
            hbias.put(i, 0, in.readDouble());
        }
        for (int i = 0; i < n_visible; i++) {
            hbias.put(i, 0, in.readDouble());
        }
    }

    @Override
    public final void write(DataOutput out) throws IOException {
        out.writeInt(n_visible);
        out.writeInt(n_hidden);
        for (int i = 0; i < n_hidden; i++) {
            for (int j = 0; j < n_visible; j++) {
                out.writeDouble(w.get(i, j));
            }
        }
        for (int i = 0; i < n_hidden; i++) {
            out.writeDouble(hbias.get(i, 0));
        }
        for (int i = 0; i < n_visible; i++) {
            out.writeDouble(vbias.get(i, 0));
        }
    }

    @Override
    public final void print(Writer wr) throws IOException {
        String newLine = System.getProperty("line.separator");
        wr.write(String.valueOf(n_visible));
        wr.write(",");
        wr.write(String.valueOf(n_hidden));
        wr.write(newLine);
        for (int i = 0; i < n_hidden; i++) {
            for (int j = 0; j < n_visible; j++) {
                wr.write(String.valueOf(w.get(i, j)));
                wr.write(",");
            }
            wr.write(newLine);
        }
        for (int i = 0; i < n_hidden; i++) {
            wr.write(String.valueOf(hbias.get(i, 0)));
            wr.write(",");
        }
        wr.write(newLine);
        for (int i = 0; i < n_visible; i++) {
            wr.write(String.valueOf(vbias.get(i, 0)));
            wr.write(",");
        }
        wr.write(newLine);
    }

    public final void mergeParam(DoubleMatrix new_w, DoubleMatrix new_hbias, DoubleMatrix new_vbias, int nbr_model) {
        w.addi(new_w.sub(w).divi(nbr_model));
        hbias.addi(new_hbias.sub(hbias).divi(nbr_model));
        vbias.addi(new_vbias.sub(vbias).divi(nbr_model));
    }

    protected abstract void gradientUpdateMiniBatch(SGDTrainConfig config, DoubleMatrix samples, DoubleMatrix curr_w,
            DoubleMatrix curr_hbias, DoubleMatrix curr_vbias);

    protected abstract void gradientUpdateCG(SGDTrainConfig config, DoubleMatrix samples, DoubleMatrix curr_w,
            DoubleMatrix curr_hbias, DoubleMatrix curr_vbias);

    protected abstract DoubleMatrix reconstruct(DoubleMatrix input);
}
