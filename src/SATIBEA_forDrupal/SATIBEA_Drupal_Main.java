/*
 * Author : shikai(BH4AWS@163.com)
 */
package util;

import java.io.File;

import jmetal.core.Algorithm;
import jmetal.core.Problem;
import jmetal.core.SolutionSet;
import jmetal.core.Variable;
import satibeaVariants.SATIBEA_Problem_Drupal;
import satibeaVariants.SATIBEA_SettingsIBEA_Drupal;

public class SATIBEA_Drupal_Main{

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception {

    	try{
    		//the number of iteration
            int repetition =30;
            /*************/
            //terminal time
            //int[] program_timeout_ms = {300000,600000,900000,1200000,1800000};
            int[] program_timeout_ms = {90000};
            /*************/
            //Feature Model Path
            String path = "E:\\java-coding\\work\\ICSE2018_ProV2.0\\FM\\";
    		
            String[] systems = new String[]{"drupal"};
            //String[] systems = new String[]{"fiasco", "ecos-icse11", "uClinux","freebsd-icse11","2.6.28.6-icse11"};
            
            /*************/
            String ResultDirectory = "SATIBEArealDrual_result";
            String Outpath = path+ResultDirectory+"\\";
            File file = new File(Outpath);
            if(!file.exists() && !file.isDirectory())
            	file.mkdirs();
            /************/
            
            String fm = "";
            String augment ="";
            String faults = "";
            String mandatory ="";
            String outputFileSATibea ="";
            
            Problem p =null;
            Algorithm a =null;

            for(int count = 0;count<program_timeout_ms.length;count++)
            {
            	// for each system
            	for(int i = 0; i< systems.length; i++){
            		String inputFile = path + systems[i] + ".dimacs";
            		System.out.println(inputFile);
            	
            		fm = inputFile;
            		augment = fm + ".augment";
            		faults = fm + ".faults";
            		mandatory = fm + ".mandatory";
            		
            		//SATIBEA_drupal
            		int loop = repetition;
            		while(loop > 0){
            			/********************/
            			outputFileSATibea = Outpath + systems[i] + ".SATibearealD-"+(program_timeout_ms[count]/6000)+"m.out." + loop;

            			p = new SATIBEA_Problem_Drupal(fm, augment, faults, mandatory);
            			a = new SATIBEA_SettingsIBEA_Drupal(p).configureSATIBEA(program_timeout_ms[count], fm, 
            					((SATIBEA_Problem_Drupal) p).getNumFeatures(), 
            					((SATIBEA_Problem_Drupal) p).getConstraints(),outputFileSATibea);
            			/********************/
            			long start = System.currentTimeMillis();
            			SolutionSet pop = a.execute();
            			System.out.println("evaluation times：SATibearealD-"+(program_timeout_ms[count]/6000)+"m_"+
            			systems[i]+"_"+loop+"_evaluation_times(including population generation)time: " + (System.currentTimeMillis()-start) );
            			System.out.println("====================================");
            			
            			/********************/
            			//output one result
            			for (int k = 0; k < pop.size(); k++) {
            				Variable v = pop.get(k).getDecisionVariables()[0];
            				String line = "";
            				for (int j = 0; j < pop.get(k).getNumberOfObjectives(); j++) {
//                            	System.out.print(pop.get(k).getObjective(j) + " ");
            					line += pop.get(k).getObjective(j) + " ";
            				}
            				WriteStreamAppend.method1(outputFileSATibea, line.trim()+"\r\n");
            			}
            			loop--;
            			/********************/
            		}//end while
            		/*****************************************************************************************/
            	}// end for system
            }  
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Usage: java -jar satibea.jar fmDimacs timeMS\nThe .augment, .dead, .mandatory and .richseed files should be in the same directory as the FM.");
        }
    }
}
