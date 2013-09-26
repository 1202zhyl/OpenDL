package org.spark.opendl.downpourSGD;

import java.io.Serializable;
import java.util.List;
import java.util.Random;

import scala.Tuple2;
import spark.api.java.JavaPairRDD;
import spark.api.java.JavaRDD;
import spark.api.java.function.PairFunction;

/**
 * Samples split for Spark train work <p/>
 * 
 * @author GuoDing
 * @since 2013-07-20
 * @param <T>
 */
public final class ModelReplicaSplit<T> implements Serializable {
    private static final long serialVersionUID = 1L;
    private Random rand = new Random(System.currentTimeMillis());

    /**
     * Split the input samples (one each split for one ModelReplica)
     * 
     * @param input
     * @param nrModelReplica
     * @param cache
     * @return
     */
    public JavaPairRDD<Integer, List<T>> split(JavaRDD<T> input, int nrModelReplica, boolean cache) {
        JavaPairRDD<Integer, List<T>> output = input.map(new SplitModelReplica(nrModelReplica)).groupByKey();
        if (cache) {
            return output.cache();
        }
        output.count();
        return output;
    }

    private class SplitModelReplica extends PairFunction<T, Integer, T> {
        private static final long serialVersionUID = 1L;
        private int nrModelReplica;

        public SplitModelReplica(int nr) {
            this.nrModelReplica = nr;
        }

        @Override
        public Tuple2<Integer, T> call(T arg) throws Exception {
            int idx = rand.nextInt(nrModelReplica);
            return new Tuple2<Integer, T>(idx, arg);
        }
    }
}
