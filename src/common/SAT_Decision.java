package satibeaVariants;

import java.io.FileReader;
import java.util.HashSet;
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
import org.sat4j.specs.IVecInt;
import org.sat4j.tools.ModelIterator;

import jmetal.encodings.variable.Binary;

/**
 * @author ShiKai
 * 判断违背约束数量
 * 及其生成新的满足约束的二进制字符串解及其解决方案
 * 调用SAT求解器
 * */
public class SAT_Decision {

	
	/*******************************************/
    private List<List<Integer>> constraints;
    private String fm;
    private int nFeat;
    private static final int SATtimeout = 1000;
    private static final long iteratorTimeout = 150000;
    private static Random r = new Random();
    /*******************************************/
    
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

	public SAT_Decision(String fm, int nFeat, List<List<Integer>> constraints){
		this.fm = fm;
        this.nFeat = nFeat;
        this.constraints = constraints;
	}
		
	/*********************************************************************************/
    //用于检测二进制字符串的配置，是否存在约束，有几个约束
    public int numViolatedConstraints(Binary b, HashSet<Integer> blacklist) {

        //IVecInt v = bitSetToVecInt(b);
        int s = 0;
        for (List<Integer> constraint : constraints) {
            boolean sat = false;

            for (Integer i : constraint) {
                int abs = (i < 0) ? -i : i;
                boolean sign = i > 0;
                if (b.getIth(abs - 1) == sign) {
                    sat = true;
                } else {
                    blacklist.add(abs);
                }
            }
            if (!sat) {
                s++;
            }

        }

        return s;
    }
    
    public int numViolatedConstraints(Binary b) {
        //IVecInt v = bitSetToVecInt(b);
        int s = 0;
        for (List<Integer> constraint : constraints) {
            boolean sat = false;

            for (Integer i : constraint) {
                int abs = (i < 0) ? -i : i;
                boolean sign = i > 0;
                if (b.getIth(abs - 1) == sign) {
                    sat = true;
                    break;
                }
            }
            if (!sat) {
                s++;
            }
        }
        return s;
    }
    
    public int numViolatedConstraints(boolean[] b) {

        //IVecInt v = bitSetToVecInt(b);
        int s = 0;
        for (List<Integer> constraint : constraints) {
            boolean sat = false;
            for (Integer i : constraint) {
                int abs = (i < 0) ? -i : i;
                boolean sign = i > 0;
                if (b[abs - 1] == sign) {
                    sat = true;
                    break;
                }
            }
            if (!sat) {
                s++;
            }
        }
        return s;
    }
    
    public boolean[] randomProduct() {

        boolean[] prod = new boolean[nFeat];
        for (int i = 0; i < prod.length; i++) {
            prod[i] = r.nextBoolean();
        }
        int rand = r.nextInt(3);

        try {
            IOrder order;
            if (rand == 0) {
                order = new RandomWalkDecorator(new VarOrderHeap(new NegativeLiteralSelectionStrategy()), 1);
            } else if (rand == 1) {
                order = new RandomWalkDecorator(new VarOrderHeap(new PositiveLiteralSelectionStrategy()), 1);
            } else {
                order = new RandomWalkDecorator(new VarOrderHeap(new RandomLiteralSelectionStrategy()), 1);
            }

            //dimacsSolver.reset();
            ISolver dimacsSolver2 = SolverFactory.instance().createSolverByName("MiniSAT");
            dimacsSolver2.setTimeout(SATtimeout);

            DimacsReader dr = new DimacsReader(dimacsSolver2);
            dr.parseInstance(new FileReader(fm));
            ((Solver) dimacsSolver2).setOrder(order);

            ISolver solverIterator = new ModelIterator(dimacsSolver2);
            solverIterator.setTimeoutMs(iteratorTimeout);

            if (solverIterator.isSatisfiable()) {
                int[] i = solverIterator.findModel();

                for (int j = 0; j < i.length; j++) {
                    int feat = i[j];

                    int posFeat = feat > 0 ? feat : -feat;

                    if (posFeat > 0) {
                        prod[posFeat - 1] = feat > 0;
                    }
//                    else
//                    {
//                         prod[nFeat-1] = r.nextBoolean();
//                    }
                }
            }
            //solverIterator = null;
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
        }
        return prod;
    }
    
    //用于生产满足条件的二进制字符串及其求解solution
    public boolean[] randomProductAssume(IVecInt ivi) {

        boolean[] prod = new boolean[nFeat];
        for (int i = 0; i < prod.length; i++) {
            prod[i] = r.nextBoolean();

        }
        int rand = r.nextInt(3);
        
        try {
            IOrder order;
            if (rand == 0) {
                order = new RandomWalkDecorator(new VarOrderHeap(new NegativeLiteralSelectionStrategy()), 1);
            } else if (rand == 1) {
                order = new RandomWalkDecorator(new VarOrderHeap(new PositiveLiteralSelectionStrategy()), 1);
            } else {
                order = new RandomWalkDecorator(new VarOrderHeap(new RandomLiteralSelectionStrategy()), 1);
            }

            //dimacsSolver.reset();
            ISolver dimacsSolver2 = SolverFactory.instance().createSolverByName("MiniSAT");
            dimacsSolver2.setTimeout(SATtimeout);

            DimacsReader dr = new DimacsReader(dimacsSolver2);
            dr.parseInstance(new FileReader(fm));
            ((Solver) dimacsSolver2).setOrder(order);

            ISolver solverIterator = new ModelIterator(dimacsSolver2);
            solverIterator.setTimeoutMs(iteratorTimeout);
            //如果求到了，满足约束的一个二进制子串  采用SAT求解器
            if (solverIterator.isSatisfiable()) {
                int[] i = solverIterator.findModel(ivi);
                for (int j = 0; j < i.length; j++) {
                    int feat = i[j];
                    int posFeat = feat > 0 ? feat : -feat;
                    if (posFeat > 0) {
                        prod[posFeat - 1] = feat > 0;
                    }

//                    else
//                    {
//                         prod[nFeat-1] = r.nextBoolean();
//                    }
                }
            }

            //solverIterator = null;
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
        }
        return prod;
    }

   /*********************************************************************************/
}
