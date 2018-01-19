package satibeaVariants;

import java.util.List;
import java.util.Random;

import jmetal.core.Problem;
import jmetal.core.Variable;
import jmetal.encodings.solutionType.BinarySolutionType;
import jmetal.encodings.variable.Binary;

/**
 *
 * SATIBEA的二进制解决方案类型   自定义的解决方案类型   继承BinarySolutionType
 */
public class SATIBEA_BinarySolution_Drupal extends BinarySolutionType {

    private String fm;
    private int nFeat;
    private List<Integer> mandatoryFeaturesIndices;
    //private List<Integer> deadFeaturesIndices;
    int n = 0;
    //private List<Integer> seed;
    private static Random r = new Random();
    private static final int SATtimeout = 1000;
    private static final long iteratorTimeout = 150000;

    public SATIBEA_BinarySolution_Drupal(Problem problem, int nFeat, String fm, List<Integer> mandatoryFeaturesIndices) {
        super(problem);
        this.fm = fm;
        this.nFeat = nFeat;
        this.mandatoryFeaturesIndices = mandatoryFeaturesIndices;
    }
    
    public Variable[] createVariables() {
        Variable[] vars = new Variable[problem_.getNumberOfVariables()];

        for (int i = 0; i < vars.length; i++) {
            Binary bin = new Binary(nFeat);
            
            //System.out.println(bin);
            for (int j = 0; j < bin.getNumberOfBits(); j++) {
                bin.setIth(j, r.nextBoolean());
            }
            
            for (Integer f : this.mandatoryFeaturesIndices) {
                bin.setIth(f, true);
            }
            
//            for (Integer f : this.deadFeaturesIndices) {
//                bin.setIth(f, false);
//            }
            vars[i] = bin;
        }
        return vars;
    }
}
