
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
public class SATIBEA_SettingsIBEAv4 extends Settings {

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
    public SATIBEA_SettingsIBEAv4(Problem p) {
        super(p.getName());
        problem_ = p;
    }
    
    
    public Algorithm configureSATIBEAv4(long maxRunTimeMS, String fm, int numFeat,List<List<Integer>> constr,String logpath) throws JMException {

        populationSize_ = 300;
        archiveSize_ = 300;

        mutationProbability_ = 0.001;	//SATIBEA,SATIBEAv1,SATIBEAv2,SATIBEAv4
        //mutationProbability_ = 1;		//SATIBEAv3,SATIBEAv5
        
        crossoverProbability_ = 0.05;

        Algorithm algorithm;
        Operator selection;
        Operator crossover;
        Operator mutation;

        HashMap parameters; 	// Operator parameters
        SAT_Decision sat_dec;	//用于检测是否满足约束，违背多少约束，修复新的解
        /************************/
        sat_dec = new SAT_Decision(fm,  numFeat, constr);
        /************************/
        
        algorithm = new IBEATimeLimitedv4(problem_,maxRunTimeMS,sat_dec);
        
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
        mutation = new SATIBEA_NewMutationv4(parameters, fm,  numFeat, constr, sat_dec);

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
    }

}
