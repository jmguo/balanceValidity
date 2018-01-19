
package satibeaVariants;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

import org.sat4j.core.VecInt;
import org.sat4j.specs.IVecInt;

import jmetal.core.Solution;
import jmetal.encodings.solutionType.BinaryRealSolutionType;
import jmetal.encodings.solutionType.BinarySolutionType;
import jmetal.encodings.solutionType.IntSolutionType;
import jmetal.encodings.variable.Binary;
import jmetal.operators.mutation.Mutation;
import jmetal.util.Configuration;
import jmetal.util.JMException;
import jmetal.util.PseudoRandom;


public class SATIBEA_NewMutation extends Mutation {

    private static Random r = new Random();
    private String fm;
    private int nFeat;
    private List<List<Integer>> constraints;

    /*************************/
    private SAT_Decision sat_dec;
    /*************************/
    
    /**
     * Valid solution types to apply this operator
     */
    private static final List VALID_TYPES = Arrays.asList(BinarySolutionType.class,
            BinaryRealSolutionType.class,
            IntSolutionType.class, SATIBEA_BinarySolution.class);

    private Double mutationProbability_ = null;

    private static final int SATtimeout = 1000;
    private static final long iteratorTimeout = 150000;

    /**
     * Constructor Creates a new instance of the Bit Flip mutation operator
     */
    public SATIBEA_NewMutation(HashMap<String, Object> parameters, String fm, int nFeat, List<List<Integer>> constraints,SAT_Decision sat_dec) {
        super(parameters);
        if (parameters.get("probability") != null) {
            mutationProbability_ = (Double) parameters.get("probability");
        }
        this.fm = fm;
        this.nFeat = nFeat;
        this.constraints = constraints;
        this.sat_dec = sat_dec;
    }

    /**
     * Perform the mutation operation
     *
     * @param probability Mutation probability
     * @param solution The solution to mutate
     * @throws JMException
     */
    public void doMutation(double probability, Solution solution) throws JMException {

        Integer in = r.nextInt(50);

        if (in != 0) {//0.98！=
        //if (false) {
            try {
                if ((solution.getType().getClass() == BinarySolutionType.class)
                        || (solution.getType().getClass() == BinaryRealSolutionType.class) 
                        || solution.getType().getClass()==SATIBEA_BinarySolution.class) {
                    for (int i = 0; i < solution.getDecisionVariables().length; i++) {
                        //for (int j = 0; j < ((Binary) solution.getDecisionVariables()[i]).getNumberOfBits(); j++) {
                        for (Integer j : SATIBEA_Problem.featureIndicesAllowedFlip) { //flip only not "fixed" features
                            if (PseudoRandom.randDouble() < probability) {
                                ((Binary) solution.getDecisionVariables()[i]).bits_.flip(j);
                            }
                        }
                    }

                    for (int i = 0; i < solution.getDecisionVariables().length; i++) {
                        ((Binary) solution.getDecisionVariables()[i]).decode();
                    }
                } // if
                else { // Integer representation
                    for (int i = 0; i < solution.getDecisionVariables().length; i++) {
                        if (PseudoRandom.randDouble() < probability) {
                            int value = PseudoRandom.randInt(
                                    (int) solution.getDecisionVariables()[i].getLowerBound(),
                                    (int) solution.getDecisionVariables()[i].getUpperBound());
                            solution.getDecisionVariables()[i].setValue(value);
                        } // if
                    }
                } // else
            } catch (ClassCastException e1) {
                Configuration.logger_.severe("BitFlipMutation.doMutation: "
                        + "ClassCastException error" + e1.getMessage());
                Class cls = java.lang.String.class;
                String name = cls.getName();
                throw new JMException("Exception in " + name + ".doMutation()");
            }

        } else {//0.02
        	/******************************************************************************/
            boolean b = r.nextBoolean();
//            long t = System.currentTimeMillis();
            if (b) {
                for (int i = 0; i < solution.getDecisionVariables().length; i++) {
                    boolean[] prod = this.sat_dec.randomProduct();
                    Binary bin = (Binary) solution.getDecisionVariables()[i];
                    for (int j = 0; j < prod.length; j++) {
                        bin.setIth(j, prod[j]);
                    }
                }
//                System.out.println("Replace (ms) " + (System.currentTimeMillis() - t));
            } else {
                HashSet<Integer> blacklist = new HashSet<Integer>();
                for (int i = 0; i < solution.getDecisionVariables().length; i++) {
                    Binary bin = (Binary) solution.getDecisionVariables()[i];
                    int violated = this.sat_dec.numViolatedConstraints(bin, blacklist);
                    if (violated > 0) {
                        IVecInt iv = new VecInt();
                        for (int j = 0; j < SATIBEA_Problem.numFeatures; j++) {
                            int feat = j + 1;
                            if (!blacklist.contains(feat)) {
                                iv.push(bin.bits_.get(j) ? feat : -feat);
                            }
                        }
                        boolean[] prod = this.sat_dec.randomProductAssume(iv);
                        for (int j = 0; j < prod.length; j++) {
                            bin.setIth(j, prod[j]);
                        }
                    }//end if
                }//end for
//              System.out.println("Fix (ms) " + (System.currentTimeMillis() - t));
            }//end if
            /******************************************************************************/
        }
    } // doMutation

    /**
     * Executes the operation
     *
     * @param object An object containing a solution to mutate
     * @return An object containing the mutated solution
     * @throws JMException
     */
    public Object execute(Object object) throws JMException {
        Solution solution = (Solution) object;

        if (!VALID_TYPES.contains(solution.getType().getClass())) {
            Configuration.logger_.severe("BitFlipMutation.execute: the solution "
                    + "is not of the right type. The type should be 'Binary', "
                    + "'BinaryReal' or 'Int', but " + solution.getType() + " is obtained");

            Class cls = java.lang.String.class;
            String name = cls.getName();
            throw new JMException("Exception in " + name + ".execute()");
        } // if 

        doMutation(mutationProbability_, solution);
        return solution;
    } // execute

}

