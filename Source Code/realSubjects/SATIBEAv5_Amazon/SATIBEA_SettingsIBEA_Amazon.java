package satibeaVariants;

import java.util.HashMap;
import java.util.List;

import jmetal.core.Algorithm;
import jmetal.core.Operator;
import jmetal.core.Problem;
import jmetal.experiments.Settings;
import jmetal.operators.selection.BinaryTournament;
import jmetal.util.JMException;
import jmetal.util.comparators.FitnessComparator;

/**
 * Settings class of algorithm
 */
public class SATIBEA_SettingsIBEA_Amazon extends Settings {

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
    public SATIBEA_SettingsIBEA_Amazon(Problem p) {
        super(p.getName());
        problem_ = p;
    }
    
    public Algorithm configureSATIBEA(long maxRunTimeMS, String fm, int numFeat,List<List<Integer>> constr,String logpath) throws JMException {

        populationSize_ = 300;
        archiveSize_ = 300;

        //mutationProbability_ = 0.001;
        mutationProbability_ = 1;
        crossoverProbability_ = 0.05;

        Algorithm algorithm;
        Operator selection;
        Operator crossover;
        Operator mutation;

        HashMap parameters; 	// Operator parameters
        /************************/
        SAT_Decision sat_dec;
        sat_dec = new SAT_Decision(fm, numFeat, constr);
        /************************/
        
        algorithm = new IBEATimeLimitedAmazon(problem_,maxRunTimeMS,sat_dec);
        
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
        crossover = new SATIBEA_SinglePointCrossover_Amazon(parameters);

        parameters = new HashMap();
        parameters.put("probability", mutationProbability_);
        mutation = new SATIBEA_NewMutation_Amazon(parameters, fm, numFeat, constr, sat_dec);

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

    public Algorithm configure() throws JMException {
        Algorithm algorithm = null;
        return algorithm;
    } // configure

} //
