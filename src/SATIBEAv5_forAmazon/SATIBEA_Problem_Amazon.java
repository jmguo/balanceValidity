package satibeaVariants;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.StringTokenizer;

import jmetal.core.Problem;
import jmetal.core.Solution;
import jmetal.core.Variable;
import jmetal.encodings.variable.Binary;
import jmetal.util.JMException;
import myamazon.GetValue;
import myamazon.Instance;
import myamazon.Instanceele;
import myamazon.Price;
import myamazon.Storage;

/**
 *
 * @author shikai
 */
public class SATIBEA_Problem_Amazon extends Problem {

    public static String fm;
    private String augment;
    public static int numFeatures;
    private int numConstraints;
    public static List<List<Integer>> constraints;
        
//    private static int n = 0;
//    private List<Integer> mandatoryFeaturesIndices, deadFeaturesIndices;
    private List<Integer> mandatoryFeaturesIndices;
    public static List<Integer> featureIndicesAllowedFlip;
    
    private HashMap<String, Double> StorageOptionsMap;
    private HashMap<Integer, Instanceele> InstanceOptionsMap;
    private HashMap<String, Double> PriceOptionsMap15;
    private HashMap<String, String> PriceOptionsMap;
    
    private static final int N_VARS = 1, N_OBJS = 8;

    public SATIBEA_Problem_Amazon(String fm, String augment, String mandatory, String instancefile, String storagefile, String pri15file, String prifile) throws Exception {
        this.numberOfVariables_ = N_VARS;
        this.numberOfObjectives_ = N_OBJS;
        this.numberOfConstraints_ = 0;
        this.fm = fm;
        this.augment = augment;
        
        loadFM(fm, augment);			//载入模型文件和augment文件
        //loadFaults(faults);
        
        loadInstance_map(instancefile);	//载入instance文件
        loadStorage_map(storagefile);	//载入存储文件
        loadPrice_map15(pri15file);		//载入价格15文件
        loadPrice_map(prifile);			//载入价格文件
        loadMandatoryDeadFeaturesIndices(mandatory); //载入必选项
        
        this.solutionType_ = new SATIBEA_BinarySolution_Amazon(this, numFeatures, fm, mandatoryFeaturesIndices);
    }
    
    //对Solution进行评估计算出目标值 amazon存在8个目标需要计算
    @Override
    public void evaluate(Solution sltn) throws JMException {
        Variable[] vars = sltn.getDecisionVariables();
        Binary bin = (Binary) vars[0];
        
        int unselected=0;
        int InstanceCores=0;
        double EC2costMonth = 100000.0, InstanceRam = 0.0,InstanceCostHour = 100000.0, InstanceECU=0, BlockStorageCostGB=0;
        int InstanceSSDBacked = 0;
        int upfrontCost = 0, InstanceDedicatedFee=0;
        double PurchaseSignedUsage=730;
        
        String tem = "";
        for (int i = 0; i < bin.getNumberOfBits(); i++) {
            boolean b = bin.getIth(i);
            if (!b) {
                unselected++;
            }
        }
        
        //获得其实例-CPU核心-ECU-RAM
        InstanceCores = 32 - new GetValue().getInstancePare(bin, InstanceOptionsMap).getInstance_cores();
        InstanceECU = 108 - new GetValue().getInstancePare(bin, InstanceOptionsMap).getInstance_ecu();
        InstanceRam = 250 - new GetValue().getInstancePare(bin, InstanceOptionsMap).getInstance_ram();
        
        InstanceCostHour = new GetValue().getPricecostHour(bin,PriceOptionsMap15);
        if(InstanceCostHour == -1)
        {
        	tem = new GetValue().getPriceupfrontCost(bin,PriceOptionsMap);
        	if(tem != null)
        	{
        		InstanceCostHour = Double.valueOf(tem.substring(tem.indexOf('-')+1));
        		upfrontCost = Integer.valueOf(tem.substring(0, tem.indexOf("-")));
        	}else {
        	}
        }
        InstanceSSDBacked = 1 - new GetValue().getInstancePare(bin, InstanceOptionsMap).getInstance_ssdBacked();
        
        if(bin.getIth(21))
        	InstanceDedicatedFee = 2;
        else if(bin.getIth(20))
        	InstanceDedicatedFee = 0;
        
        if(bin.getIth(6) && InstanceSSDBacked==0)
        {
        	bin.setIth(23, true);
        	bin.setIth(22, false);
        	BlockStorageCostGB = new GetValue().getStoragecostGB(bin, StorageOptionsMap);
        }else{
        }
        
        if(bin.getIth(74) || bin.getIth(77))//75 78 
        	PurchaseSignedUsage = 730;
        
        if(bin.getIth(72) || bin.getIth(75))//73 76
        	PurchaseSignedUsage = 366;
        
        if(bin.getIth(73) || bin.getIth(76))//74 77
        	PurchaseSignedUsage = 360;
        
        if(bin.getIth(14))//15
        	PurchaseSignedUsage = 91.5;
        
        //用于计算EC2costMonth
        EC2costMonth = (InstanceCostHour + InstanceDedicatedFee) * PurchaseSignedUsage 
        		+ 10 * BlockStorageCostGB;
        
        //目标函数
        sltn.setObjective(0, numViolatedConstraints(bin));
        sltn.setObjective(1, unselected);		//必须小
        sltn.setObjective(2, EC2costMonth);		//必须小
        sltn.setObjective(3, InstanceCores);	//必须大
        sltn.setObjective(4, InstanceECU);		//必须大
        sltn.setObjective(5, InstanceRam);		//必须大
        sltn.setObjective(6, InstanceCostHour);	//必须小
        sltn.setObjective(7, InstanceSSDBacked);//必须大
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

    //载入模型文件和augment文件
    public void loadFM(String fm, String augment) throws Exception {
        BufferedReader in = new BufferedReader(new FileReader(fm));
        String line;
        while ((line = in.readLine()) != null) {
            line = line.trim();
            System.out.println(line);
            //获得存在几个特征，几个约束
            if (line.startsWith("p")) {
                StringTokenizer st = new StringTokenizer(line, " ");
                st.nextToken();
                st.nextToken();
                numFeatures = Integer.parseInt(st.nextToken());
                numConstraints = Integer.parseInt(st.nextToken());
                constraints = new ArrayList<List<Integer>>(numConstraints);
            }
            
            //将约束存放在constraints里面
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
    }
    
    //载入必选项
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

    //载入文件
    public HashMap<String, Double> loadStorage_map(String filePath) throws Exception {
    	StorageOptionsMap = new HashMap<String , Double>();
    	StorageOptionsMap = new Storage().getStorage_map(filePath);
		return StorageOptionsMap;
    }
    
    //载入文件
    public HashMap<Integer, Instanceele> loadInstance_map(String filePath) throws Exception {
    	InstanceOptionsMap = new HashMap<Integer , Instanceele>();
    	InstanceOptionsMap = new Instance().getInstance_map(filePath);
    	return InstanceOptionsMap;
    }
    
    //载入文件
    public HashMap<String, Double> loadPrice_map15(String filePath) throws Exception {
    	PriceOptionsMap15 = new HashMap<String , Double>();
    	PriceOptionsMap15 = new Price().getPrice15_map(filePath);
    	return PriceOptionsMap15;
    }
    
    //载入文件
    public HashMap<String, String> loadPrice_map(String filePath) throws Exception {
    	PriceOptionsMap = new HashMap<String, String>();
    	PriceOptionsMap = new Price().getPrice_map(filePath);
    	return PriceOptionsMap;
    }
}
