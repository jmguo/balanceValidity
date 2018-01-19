package satibeaVariants;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.sat4j.core.VecInt;
import org.sat4j.specs.IVecInt;

import jmetal.core.Algorithm;
import jmetal.core.Operator;
import jmetal.core.Problem;
import jmetal.core.Solution;
import jmetal.core.SolutionSet;
import jmetal.encodings.variable.Binary;
import jmetal.util.JMException;
import jmetal.util.Ranking;
import jmetal.util.comparators.DominanceComparator;
import util.WriteStreamAppend;

/**
 * @author chris
 */
public class IBEATimeLimitedv5Drupal extends Algorithm {

    private int print_time = 0;

    private long maxRunTimeMS;
    
    /*************************/
    private SAT_Decision sat_dec;
    /*************************/
    
    /**
     * Defines the number of tournaments for creating the mating pool
     */
    public static final int TOURNAMENTS_ROUNDS = 1;

    /**
     * Stores the value of the indicator between each pair of solutions into the
     * solution set
     */
    private List<List<Double>> indicatorValues_;

    /**
     *
     */
    private double maxIndicatorValue_;
    
	/**
     * Constructor. Create a new IBEA instance
     *
     * @param problem Problem to solve
     */
    public IBEATimeLimitedv5Drupal(Problem problem, long maxRunTimeMS,SAT_Decision sat_dec) {
        super(problem);
        this.maxRunTimeMS = maxRunTimeMS;
        this.sat_dec = sat_dec;
    }
    
    /**
     * calculates the hypervolume of that portion of the objective space that is
     * dominated by individual a but not by individual b
     */
    double calcHypervolumeIndicator(Solution p_ind_a,
            Solution p_ind_b,
            int d,
            double maximumValues[],
            double minimumValues[]) {
        double a, b, r, max;
        double volume = 0;
        double rho = 2.0;

        r = rho * (maximumValues[d - 1] - minimumValues[d - 1]);
        max = minimumValues[d - 1] + r;

        a = p_ind_a.getObjective(d - 1);
        if (p_ind_b == null) {
            b = max;
        } else {
            b = p_ind_b.getObjective(d - 1);
        }

        if (d == 1) {
            if (a < b) {
                volume = (b - a) / r;
            } else {
                volume = 0;
            }
        } else {
            if (a < b) {
                volume = calcHypervolumeIndicator(p_ind_a, null, d - 1, maximumValues, minimumValues)
                        * (b - a) / r;
                volume += calcHypervolumeIndicator(p_ind_a, p_ind_b, d - 1, maximumValues, minimumValues)
                        * (max - b) / r;
            } else {
                volume = calcHypervolumeIndicator(p_ind_a, p_ind_b, d - 1, maximumValues, minimumValues)
                        * (max - b) / r;
            }
        }
        return (volume);
    }

    /**
     * This structure store the indicator values of each pair of elements
     */
    public void computeIndicatorValuesHD(SolutionSet solutionSet,
            double[] maximumValues,
            double[] minimumValues) {
        SolutionSet A, B;
        // Initialize the structures
        indicatorValues_ = new ArrayList<List<Double>>();
        maxIndicatorValue_ = -Double.MAX_VALUE;

        for (int j = 0; j < solutionSet.size(); j++) {
            A = new SolutionSet(1);
            A.add(solutionSet.get(j));

            List<Double> aux = new ArrayList<Double>();
            for (int i = 0; i < solutionSet.size(); i++) {
                B = new SolutionSet(1);
                B.add(solutionSet.get(i));

                int flag = (new DominanceComparator()).compare(A.get(0), B.get(0));

                double value = 0.0;
                if (flag == -1) {
                    value = -calcHypervolumeIndicator(A.get(0), B.get(0), problem_.getNumberOfObjectives(), maximumValues, minimumValues);
                } else {
                    value = calcHypervolumeIndicator(B.get(0), A.get(0), problem_.getNumberOfObjectives(), maximumValues, minimumValues);
                }
        //double value = epsilon.epsilon(matrixA,matrixB,problem_.getNumberOfObjectives());

                //Update the max value of the indicator
                if (Math.abs(value) > maxIndicatorValue_) {
                    maxIndicatorValue_ = Math.abs(value);
                }
                aux.add(value);
            }
            indicatorValues_.add(aux);
        }
    } // computeIndicatorValues

    /**
     * Calculate the fitness for the individual at position pos
     */
    public void fitness(SolutionSet solutionSet, int pos) {
        double fitness = 0.0;
        double kappa = 0.05;

        for (int i = 0; i < solutionSet.size(); i++) {
            if (i != pos) {
                fitness += Math.exp((-1 * indicatorValues_.get(i).get(pos) / maxIndicatorValue_) / kappa);
            }
        }
        solutionSet.get(pos).setFitness(fitness);
    }

    /**
     * Calculate the fitness for the entire population.
     *
     */
    public void calculateFitness(SolutionSet solutionSet) {
        // Obtains the lower and upper bounds of the population
        double[] maximumValues = new double[problem_.getNumberOfObjectives()];
        double[] minimumValues = new double[problem_.getNumberOfObjectives()];

        for (int i = 0; i < problem_.getNumberOfObjectives(); i++) {
            maximumValues[i] = -Double.MAX_VALUE; // i.e., the minus maxium value
            minimumValues[i] = Double.MAX_VALUE; // i.e., the maximum value
        }

        for (int pos = 0; pos < solutionSet.size(); pos++) {
            for (int obj = 0; obj < problem_.getNumberOfObjectives(); obj++) {
                double value = solutionSet.get(pos).getObjective(obj);
                if (value > maximumValues[obj]) {
                    maximumValues[obj] = value;
                }
                if (value < minimumValues[obj]) {
                    minimumValues[obj] = value;
                }
            }
        }

        computeIndicatorValuesHD(solutionSet, maximumValues, minimumValues);
        for (int pos = 0; pos < solutionSet.size(); pos++) {
            fitness(solutionSet, pos);
        }
    }

    /**
     * Update the fitness before removing an individual
     */
    public void removeWorst(SolutionSet solutionSet) {

        // Find the worst;
        double worst = solutionSet.get(0).getFitness();
        int worstIndex = 0;
        double kappa = 0.05;

        for (int i = 1; i < solutionSet.size(); i++) {
            if (solutionSet.get(i).getFitness() > worst) {
                worst = solutionSet.get(i).getFitness();
                worstIndex = i;
            }
        }

        //if (worstIndex == -1) {
        //    System.out.println("Yes " + worst);
        //}
        //System.out.println("Solution Size "+solutionSet.size());
        //System.out.println(worstIndex);
        // Update the population
        for (int i = 0; i < solutionSet.size(); i++) {
            if (i != worstIndex) {
                double fitness = solutionSet.get(i).getFitness();
                fitness -= Math.exp((-indicatorValues_.get(worstIndex).get(i) / maxIndicatorValue_) / kappa);
                solutionSet.get(i).setFitness(fitness);
            }
        }

        // remove worst from the indicatorValues list
        indicatorValues_.remove(worstIndex); // Remove its own list
        Iterator<List<Double>> it = indicatorValues_.iterator();
        while (it.hasNext()) {
            it.next().remove(worstIndex);
        }

        // remove the worst individual from the population
//        if (solutionSet.get(worstIndex).getObjective(0) == 0) {
//            System.out.print("Removed-" + print_time + "-: ");
//            Variable v = solutionSet.get(worstIndex).getDecisionVariables()[0];
//            for (int j = 0; j < solutionSet.get(worstIndex).getNumberOfObjectives(); j++) {
//                System.out.print(solutionSet.get(worstIndex).getObjective(j) + " ");
//            }
//            System.out.println("");
//        }
        solutionSet.remove(worstIndex);
    } // removeWorst

    /**
     * Runs of the IBEA algorithm.
     *
     * @return a <code>SolutionSet</code> that is a set of non dominated
     * solutions as a result of the algorithm execution
     * @throws JMException
     */
    public SolutionSet execute() throws JMException, ClassNotFoundException {

        long elapsed = 0, last = 0, start = System.currentTimeMillis();

        int populationSize, archiveSize, maxEvaluations, evaluations;
        Operator crossoverOperator, mutationOperator, selectionOperator;
        SolutionSet solutionSet, archive, offSpringSolutionSet;
        
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式

        //Read the params
        populationSize = ((Integer) getInputParameter("populationSize")).intValue();
        archiveSize = ((Integer) getInputParameter("archiveSize")).intValue();
        maxEvaluations = ((Integer) getInputParameter("maxEvaluations")).intValue();
        
//      QualityIndicator indicators = (QualityIndicator) getInputParameter("indicators");
        
        //Read the operators
        crossoverOperator = operators_.get("crossover");
        mutationOperator = operators_.get("mutation");
        selectionOperator = operators_.get("selection");

        //Initialize the variables
        solutionSet = new SolutionSet(populationSize);
        archive = new SolutionSet(archiveSize);
        evaluations = 0;

        //-> Create the initial solutionSet
        Solution newSolution;
        
        /******************************/
        HashSet<Integer> blacklist= new HashSet<Integer>();
        Binary bin;
        int violated;
        /******************************/
        System.out.println("开始产生种群：" + df.format(new Date()));
        for (int i = 0; i < populationSize; i++) {
        	
        	/****************************************************************/
            newSolution = new Solution(problem_);
            
//            //临时检查一下存在几个违背约束-违背的数量
//            blacklist= new HashSet<Integer>();
//            bin = (Binary) newSolution.getDecisionVariables()[0];
//            violated = this.sat_dec.numViolatedConstraints(bin, blacklist);
//        	System.out.println("产生第"+(i+1)+"个种群，违背violated为："+violated);
            /****************************************************************/
            
            problem_.evaluate(newSolution);
            problem_.evaluateConstraints(newSolution);
            evaluations++;
            solutionSet.add(newSolution);
        }
        System.out.println("初始种群产生完成，准备开始修正：" + df.format(new Date()));
        /**************************************************/
        //调用SAT求解器，进行对solutionSet中的newSolution进行修复，主要函数就是用来判断是否满足约束
        blacklist = new HashSet<Integer>();
        SolutionSet solutionSet_temp = new SolutionSet(populationSize);
        Solution Ssolution;//存放从Set中取出的solution
        for(int soi=0; soi < solutionSet.size(); soi++)
        {
        	Ssolution = solutionSet.get(soi);//从SolutionSet中取第几个Solution
        	//System.out.println(Ssolution.getDecisionVariables().length);
        	for(int ij=0;ij<Ssolution.getDecisionVariables().length; ij++)
        	{
        		bin = (Binary) Ssolution.getDecisionVariables()[ij];
        		violated = this.sat_dec.numViolatedConstraints(bin, blacklist);
        		if (violated > 0) {
        			IVecInt iv = new VecInt();
        			for (int j = 0; j < SATIBEA_Problem_Drupal.numFeatures; j++) {
        				int feat = j + 1;
        				if (!blacklist.contains(feat)) {
        					iv.push(bin.bits_.get(j) ? feat : -feat);
        				}
        			}
        			boolean[] prod = this.sat_dec.randomProductAssume(iv);
        			for (int j = 0; j < prod.length; j++) {
        				bin.setIth(j, prod[j]);
        			}
        		}
        	}
//        	//重新检查违背（临时的）
//        	blacklist = new HashSet<Integer>();
//        	bin = (Binary) Ssolution.getDecisionVariables()[0];
//        	violated = this.sat_dec.numViolatedConstraints(bin, blacklist);
//            System.out.println("对第"+(soi+1)+"个种群修复之后，违背violated为："+violated);
        	
        	//重新计算并评估
        	problem_.evaluate(Ssolution);
            problem_.evaluateConstraints(Ssolution);
            solutionSet_temp.add(Ssolution);
        }
        solutionSet = solutionSet_temp;//重新赋值
        /**************************************************/

        System.out.println("演化结束时间总长：" +  this.maxRunTimeMS);
        start = System.currentTimeMillis();
        System.out.println("演化开始时间时间戳：" +  start + "=>时间：" +  df.format(new Date()));
        while (elapsed < this.maxRunTimeMS) {

            //while (evaluations < maxEvaluations){
            SolutionSet union = ((SolutionSet) solutionSet).union(archive);
            calculateFitness(union);
            archive = union;

            while (archive.size() > populationSize) {
                removeWorst(archive);
            }
            // Create a new offspringPopulation
            offSpringSolutionSet = new SolutionSet(populationSize);
            Solution[] parents = new Solution[2];
            while (offSpringSolutionSet.size() < populationSize) {
                int j = 0;
                do {
                    j++;
                    parents[0] = (Solution) selectionOperator.execute(archive);
                } while (j < IBEATimeLimitedv5Drupal.TOURNAMENTS_ROUNDS); // do-while
                int k = 0;
                do {
                    k++;
                    parents[1] = (Solution) selectionOperator.execute(archive);
                } while (k < IBEATimeLimitedv5Drupal.TOURNAMENTS_ROUNDS); // do-while

                //make the crossover
                Solution[] offSpring = (Solution[]) crossoverOperator.execute(parents);
                mutationOperator.execute(offSpring[0]);
                problem_.evaluate(offSpring[0]);
                problem_.evaluateConstraints(offSpring[0]);
                offSpringSolutionSet.add(offSpring[0]);
                evaluations++;
            } // while
            // End Create a offSpring solutionSet
            solutionSet = offSpringSolutionSet;//执行完上部的交叉，变异之后将根据父代生成的子代，继续赋值变为父代

            elapsed = System.currentTimeMillis() - start;
        } // while
        
        System.out.println("RunTimeMS: " + this.maxRunTimeMS);
        System.out.println("Evaluations: " + evaluations);
        
        WriteStreamAppend.method1(getInputParameter("logpath").toString()+"_log", "RunTimeMS: " + this.maxRunTimeMS+" "+"Evaluations: " + evaluations+"\r\n");
        
        Ranking ranking = new Ranking(archive);
        return ranking.getSubfront(0);
    } // execute 
}