package satibeaVariants;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import jmetal.core.Problem;
import jmetal.core.Solution;
import jmetal.core.Variable;
import jmetal.encodings.variable.Binary;
import jmetal.util.JMException;

/**
 *
 * @author shikai
 */
public class SATIBEA_Problem_Drupal extends Problem {

    public static String fm;
    private String augment;
    public static int numFeatures;
    private int numConstraints;
    public static List<List<Integer>> constraints;
    
    //augment
    private int[] LoC; 
    private String[] CC_LLOC; 
    private String[] CC_NM;
    private int[] TCs;
    private int[] Assertions;
    private int[] Reported_Installs;
    private int[] Developpers;
    private int[] Changes_722;
    private int[] Changes_723;
    private int[] Faults_722;
    private int[] Faults_723;
    
    //faults
    private int[] Single_Faults_722;
    private int[] Integ_Faults_722;
    private int[] Minor_722;
    private int[] Normal_722;
    private int[] Major_722;
    private int[] Critical_722;
    private int[] Single_Faults_723;
    private int[] Integ_Faults_723;
    private int[] Minor_723;
    private int[] Normal_723;
    private int[] Major_723;
    private int[] Critical_723;
    
//    private static int n = 0;
//    private List<Integer> mandatoryFeaturesIndices, deadFeaturesIndices;
    private List<Integer> mandatoryFeaturesIndices;
    public static List<Integer> featureIndicesAllowedFlip;
//    private List<Integer> seed;
    
    private static final int N_VARS = 1, N_OBJS = 8;

    public SATIBEA_Problem_Drupal(String fm, String augment, String faults, String mandatory) throws Exception {
        this.numberOfVariables_ = N_VARS;
        this.numberOfObjectives_ = N_OBJS;
        this.numberOfConstraints_ = 0;
        this.fm = fm;
        this.augment = augment;
        
        loadFM(fm, augment);	//载入模型augment文件
        loadFaults(faults);		//载入faults文件
        loadMandatoryDeadFeaturesIndices(mandatory);
        this.solutionType_ = new SATIBEA_BinarySolution_Drupal(this, numFeatures, fm, mandatoryFeaturesIndices);
    }
    
    //对Solution进行评估计算出目标值 drupal存在8个目标需要计算
    @Override
    public void evaluate(Solution sltn) throws JMException {
        Variable[] vars = sltn.getDecisionVariables();
        Binary bin = (Binary) vars[0];
        
        int unselected=0;
        int lineCodeCount=0,unTestAss=0,unNuminstall=0,Numdevelop=0,NumChange=0;
        double CycComplexity=0.0;
        String[] tem = null;
        for (int i = 0; i < bin.getNumberOfBits(); i++) {
            boolean b = bin.getIth(i);
            if (!b) {
                unselected++;
                unTestAss += TCs[i];
                unNuminstall += Reported_Installs[i];
            } else {
            	lineCodeCount += LoC[i];
            	
            	Numdevelop += Developpers[i];
            	NumChange += Changes_723[i];
            	
            	tem = CC_NM[i].split(",");
            	if(Integer.parseInt(tem[1].trim())==0)
            		CycComplexity += Double.valueOf(tem[0].trim());
            	else
            		CycComplexity += Double.valueOf(tem[0].trim())/Double.valueOf(tem[1].trim());
            }
        }
        sltn.setObjective(0, numViolatedConstraints(bin));
        sltn.setObjective(1, unselected);	//必须小
        sltn.setObjective(2, lineCodeCount);//必须小
        sltn.setObjective(3, CycComplexity);//必须小
        sltn.setObjective(4, unTestAss);	//必须小(TestAss必须大)
        sltn.setObjective(5, unNuminstall);	//必须小(Numinstall必须大)
        sltn.setObjective(6, Numdevelop);	//必须小
        sltn.setObjective(7, NumChange); 	//必须小
    }
    
    public List<List<Integer>> getConstraints() {
        return constraints;
    }
    
    public String getFm() {
        return fm;
    }

    public int getNumFeatures() {
        return numFeatures;
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

    public void loadFM(String fm, String augment) throws Exception {
        BufferedReader in = new BufferedReader(new FileReader(fm));
        String line;
        while ((line = in.readLine()) != null) {
            line = line.trim();
            
            if (line.startsWith("p")) {
                StringTokenizer st = new StringTokenizer(line, " ");
                st.nextToken();
                st.nextToken();
                numFeatures = Integer.parseInt(st.nextToken());
                numConstraints = Integer.parseInt(st.nextToken());
                constraints = new ArrayList<List<Integer>>(numConstraints);
            }
            
            if (!line.startsWith("c") && !line.startsWith("p") && !line.isEmpty()) {
                StringTokenizer st = new StringTokenizer(line, " ");
                List<Integer> constraint = new ArrayList<Integer>(st.countTokens() - 1);

                while (st.hasMoreTokens()) {
                    int i = Integer.parseInt(st.nextToken());
                    if (i != 0) {
                        constraint.add(i);
                    }
                }
                constraints.add(constraint);
            }
        }
        in.close();

        LoC = new int[numFeatures];
        CC_LLOC = new String[numFeatures];
        CC_NM = new String[numFeatures];
        TCs = new int[numFeatures];
        Assertions = new int[numFeatures];
        Reported_Installs = new int[numFeatures];
        Developpers = new int[numFeatures];
        Changes_722 = new int[numFeatures];
        Changes_723 = new int[numFeatures];
        Faults_722 = new int[numFeatures];
        Faults_723 = new int[numFeatures];
        in = new BufferedReader(new FileReader(augment));
        while ((line = in.readLine()) != null) {
            line = line.trim();
            if (!line.startsWith("#")) {
                StringTokenizer st = new StringTokenizer(line, " ");
                int featIndex = Integer.parseInt(st.nextToken()) - 1;
                //System.out.println(featIndex);
                LoC[featIndex] = Integer.parseInt(st.nextToken());
                CC_LLOC[featIndex] = st.nextToken();
                CC_NM[featIndex] = st.nextToken();
                
                TCs[featIndex] = Integer.parseInt(st.nextToken());
                Assertions[featIndex] = Integer.parseInt(st.nextToken());
                Reported_Installs[featIndex] = Integer.parseInt(st.nextToken());
                Developpers[featIndex] = Integer.parseInt(st.nextToken());
                Changes_722[featIndex] = Integer.parseInt(st.nextToken());
                Changes_723[featIndex] = Integer.parseInt(st.nextToken());
                Faults_722[featIndex] = Integer.parseInt(st.nextToken());
                Faults_723[featIndex] = Integer.parseInt(st.nextToken());
            }
        }
    }
    
    public void loadFaults(String faults) throws Exception {
    	BufferedReader in = new BufferedReader(new FileReader(faults));
        String line;
        Single_Faults_722 = new int[numFeatures];
        Integ_Faults_722 = new int[numFeatures];
        Minor_722 = new int[numFeatures];
        Normal_722 = new int[numFeatures];
        Major_722 = new int[numFeatures];
        Critical_722 = new int[numFeatures];
        Single_Faults_723 = new int[numFeatures];
        Integ_Faults_723 = new int[numFeatures];
        Minor_723 = new int[numFeatures];
        Normal_723 = new int[numFeatures];
        Major_723 = new int[numFeatures];
        Critical_723 = new int[numFeatures];

        while ((line = in.readLine()) != null) {
            line = line.trim();
            if (!line.startsWith("#")) {
                StringTokenizer st = new StringTokenizer(line, " ");
                int featIndex = Integer.parseInt(st.nextToken()) - 1;
                //System.out.println(featIndex);
                Single_Faults_722[featIndex] = Integer.parseInt(st.nextToken());
            	Integ_Faults_722[featIndex] = Integer.parseInt(st.nextToken());
            	Minor_722[featIndex] = Integer.parseInt(st.nextToken());
            	Normal_722[featIndex] = Integer.parseInt(st.nextToken());
            	Major_722[featIndex] = Integer.parseInt(st.nextToken());
            	Critical_722[featIndex] = Integer.parseInt(st.nextToken());
            	Single_Faults_723[featIndex] = Integer.parseInt(st.nextToken());
            	Integ_Faults_723[featIndex] = Integer.parseInt(st.nextToken());
            	Minor_723[featIndex] = Integer.parseInt(st.nextToken());
            
            	Normal_723[featIndex] = Integer.parseInt(st.nextToken());
            	Major_723[featIndex] = Integer.parseInt(st.nextToken());
            	Critical_723[featIndex] = Integer.parseInt(st.nextToken());
            }
        }
    }
    
    public void loadMandatoryDeadFeaturesIndices(String mandatory) throws Exception {
        mandatoryFeaturesIndices = new ArrayList<Integer>(numFeatures);
        //deadFeaturesIndices = new ArrayList<Integer>(numFeatures);
        featureIndicesAllowedFlip = new ArrayList<Integer>(numFeatures);
        
        BufferedReader in = new BufferedReader(new FileReader(mandatory));
        String line;
        while ((line = in.readLine()) != null) {
            if (!line.isEmpty()) {
                int i = Integer.parseInt(line) - 1;
                mandatoryFeaturesIndices.add(i);
            }
        }
        in.close();
        
        for (int i = 0; i < numFeatures; i++) {
           //if (! mandatoryFeaturesIndices.contains(i) && !deadFeaturesIndices.contains(i))
           if (! mandatoryFeaturesIndices.contains(i))
               featureIndicesAllowedFlip.add(i);
       }
    }
}
