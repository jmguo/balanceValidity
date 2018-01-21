/*
 * Author : shikai(BH4AWS@163.com)
 */
package satibeaVariants;

import java.io.File;

import jmetal.core.Algorithm;
import jmetal.core.Problem;
import jmetal.core.SolutionSet;
import jmetal.core.Variable;

public class SATIBEA_Amazon_Main{

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
            int[] program_timeout_ms = {900000};
            /*************/
            //Feature Model Path
            String path = "E:\\java-coding\\work\\ICSE2018_ProV2.0\\FM\\";
            
            String[] systems = new String[]{"Amazon"};
            
            /*************/
            String ResultDirectory = "SATIBEArealAmazon_result";
            String Outpath = path+ResultDirectory+"\\";
            File file = new File(Outpath);
            if(!file.exists() && !file.isDirectory())
            	file.mkdirs();
            /************/
            
            String fm = "";
            String augment ="";
            String mandatory ="";
            String instancefile ="";
            String storagefile ="";
            String pri15file ="";
            String prifile ="";
            
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
            		
            		mandatory = fm + ".mandatory";
            		instancefile = fm + ".instance";
            		storagefile = fm + ".storage";
            		pri15file = fm + ".pri15";
            		prifile = fm + ".pri";
            		
            		//SATIBEA_Amazon
            		int loop = repetition;
            		while(loop > 0){
            			/********************/
            			outputFileSATibea = Outpath + systems[i] + ".SATibearealA-"+(program_timeout_ms[count]/6000)+"m.out." + loop;

            			p = new SATIBEA_Problem_Amazon(fm, augment, mandatory, instancefile, storagefile, pri15file, prifile);
            			a = new SATIBEA_SettingsIBEA_Amazon(p).configureSATIBEA(program_timeout_ms[count], fm, 
            					((SATIBEA_Problem_Amazon) p).getNumFeatures(), 
            					((SATIBEA_Problem_Amazon) p).getConstraints(),outputFileSATibea);

            			/********************/
            			long start = System.currentTimeMillis();
            			SolutionSet pop = a.execute();
            			System.out.println("evaluation times：SATibearealA-"+(program_timeout_ms[count]/6000)+"m_"+
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
