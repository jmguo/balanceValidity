package satibeaVariants;

import java.util.List;
import java.util.Random;

import jmetal.core.Variable;
import jmetal.encodings.variable.Binary;

/**
 * @author ShiKai
 * */
public class RichSeedGen {
	
	private int nFe;
    private List<Integer> rich_seed;
    private static Random r = new Random();
    
    public RichSeedGen(int nFeat, List<Integer> seed)
    {
    	this.nFe = nFeat;
    	this.rich_seed = seed;
    }
    
    public Variable[] richSeedGeneration()
    {
    	Variable[] vars = new Variable[1];
    	Binary bin = new Binary(nFe);
    	for (int j = 0; j < bin.getNumberOfBits(); j++) {
            bin.setIth(j, r.nextBoolean());
        }
    	
    	for (Integer f : rich_seed){
          boolean sign = f > 0;
          int index = f > 0? f :-f;
          index--;
          bin.setIth(index, sign);
        }
    	vars[0] = bin;
    	return vars;
    }
}
