
package satibeaVariants;

import java.io.FileReader;
import java.util.List;
import java.util.Random;

import org.sat4j.minisat.SolverFactory;
import org.sat4j.minisat.core.IOrder;
import org.sat4j.minisat.core.Solver;
import org.sat4j.minisat.orders.NegativeLiteralSelectionStrategy;
import org.sat4j.minisat.orders.PositiveLiteralSelectionStrategy;
import org.sat4j.minisat.orders.RandomLiteralSelectionStrategy;
import org.sat4j.minisat.orders.RandomWalkDecorator;
import org.sat4j.minisat.orders.VarOrderHeap;
import org.sat4j.reader.DimacsReader;
import org.sat4j.specs.ISolver;
import org.sat4j.tools.ModelIterator;

import jmetal.core.Problem;
import jmetal.core.Variable;
import jmetal.encodings.solutionType.BinarySolutionType;
import jmetal.encodings.variable.Binary;

/**
 *
 * SATIBEA的二进制解决方案类型                          自定义的解决方案类型                 继承BinarySolutionType
 */
public class SATIBEA_BinarySolution extends BinarySolutionType {

    private String fm;
    private int nFeat;
    private List<Integer> mandatoryFeaturesIndices, deadFeaturesIndices;
    int n = 0;
    private List<Integer> seed;
    private static Random r = new Random();
    private static final int SATtimeout = 1000;
    private static final long iteratorTimeout = 150000;

    public SATIBEA_BinarySolution(Problem problem, int nFeat, String fm, List<Integer> mandatoryFeaturesIndices, List<Integer> deadFeaturesIndices,List<Integer> seed) {
        super(problem);
        this.fm = fm;
        this.nFeat = nFeat;
        this.mandatoryFeaturesIndices = mandatoryFeaturesIndices;
        this.deadFeaturesIndices = deadFeaturesIndices;
        this.seed = seed;
    }

    public Variable[] createVariables() {
        Variable[] vars = new Variable[problem_.getNumberOfVariables()];

        for (int i = 0; i < vars.length; i++) {
            Binary bin = new Binary(nFeat);
            
            for (int j = 0; j < bin.getNumberOfBits(); j++) {
                bin.setIth(j, r.nextBoolean());
                
            }

            for (Integer f : this.mandatoryFeaturesIndices) {
                bin.setIth(f, true);
            }

            for (Integer f : this.deadFeaturesIndices) {
                bin.setIth(f, false);
            }

            vars[i] = bin;
        }
        return vars;
    }
}
