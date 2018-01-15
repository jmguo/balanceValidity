
package satibeaVariants;

import jmetal.core.Algorithm;
import jmetal.core.Operator;
import jmetal.experiments.Settings;
import jmetal.metaheuristics.ibea.IBEA;
import jmetal.operators.crossover.Crossover;
import jmetal.operators.crossover.CrossoverFactory;
import jmetal.operators.mutation.Mutation;
import jmetal.operators.mutation.MutationFactory;
import jmetal.operators.selection.BinaryTournament;
import jmetal.operators.selection.Selection;
import jmetal.problems.ProblemFactory;
import jmetal.qualityIndicator.QualityIndicator;
import jmetal.util.JMException;
import jmetal.util.comparators.FitnessComparator;

import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import jmetal.core.Problem;

/**
 * Settings class of algorithm
 */
public class SATIBEA_SettingsIBEAv2 extends Settings {

    public int populationSize_;
    public int maxEvaluations_;
    public int archiveSize_;

    public double mutationProbability_;
    public double crossoverProbability_;

    public double crossoverDistributionIndex_;
    public double mutationDistributionIndex_;

    /**
     * Constructor
     */
    public SATIBEA_SettingsIBEAv2(Problem p) {
        super(p.getName());
        problem_ = p;
    } // 

    public Algorithm configureSATIBEAv2(long maxRunTimeMS, String fm, int numFeat,List<List<Integer>> constr,List<Integer> seed,String logpath) throws JMException {

        populationSize_ = 300;
        archiveSize_ = 300;

        mutationProbability_ = 0.001;
        //mutationProbability_ = 1;
        crossoverProbability_ = 0.05;

        Algorithm algorithm;
        Operator selection;
        Operator crossover;
        Operator mutation;

        HashMap parameters; 	// Operator parameters
        SAT_Decision sat_dec;	//用于检测是否满足约束，违背多少约束，修复新的解
        RichSeedGen richsgen;	//用于根据seed文件产生二进制字符串
        /************************/
        sat_dec = new SAT_Decision(fm, numFeat, constr);
        richsgen = new RichSeedGen(numFeat,seed);
        /************************/
        
        algorithm = new IBEATimeLimitedv2(problem_,maxRunTimeMS,sat_dec,richsgen);
        
        // Algorithm parameters
        algorithm.setInputParameter("populationSize", populationSize_);
        algorithm.setInputParameter("maxEvaluations", maxEvaluations_);
        algorithm.setInputParameter("archiveSize", archiveSize_);
        /***********************/
        algorithm.setInputParameter("logpath", logpath);
        /***********************/
        
        // Mutation and Crossover for Real codification 
        parameters = new HashMap();
        parameters.put("probability", crossoverProbability_);
        crossover = new SATIBEA_SinglePointCrossover(parameters);

        parameters = new HashMap();
        parameters.put("probability", mutationProbability_);
        mutation = new SATIBEA_NewMutationv2(parameters, fm,  numFeat, constr,sat_dec);

        /* Selection Operator */
        parameters = new HashMap();
        parameters.put("comparator", new FitnessComparator());
        selection = new BinaryTournament(parameters);

        // Add the operators to the algorithm
        algorithm.addOperator("crossover", crossover);
        algorithm.addOperator("mutation", mutation);
        algorithm.addOperator("selection", selection);
        return algorithm;
    }

    /**
     * Configure with user-defined parameter experiments.settings
     *
     * @return A algorithm object
     * @throws jmetal.util.JMException
     */
    public Algorithm configure() throws JMException {
        Algorithm algorithm=null;
        return algorithm;
    } // configure
} // 
