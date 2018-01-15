package util;

import java.io.File;

import jmetal.core.Algorithm;
import jmetal.core.Problem;
import jmetal.core.SolutionSet;
import jmetal.core.Variable;
import satibeaVariants.SATIBEA_Problem;
import satibeaVariants.SATIBEA_SettingsIBEAv3;

public class SATIBEAv3_Variants_Main{

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
            String path = "E:\\java-coding\\work\\ICSE2018-Pro\\FM\\";
            
            //Feature Model Name
            String[] systems = new String[]{"fiasco"};
            //String[] systems = new String[]{"fiasco", "ecos-icse11", "uClinux", "freebsd-icse11", "2.6.28.6-icse11"};
            
            /*************/
            String ResultDirectory = "SATIBEAv3_result";
            String Outpath = path+ResultDirectory+"\\";
            File file = new File(Outpath);
            if(!file.exists() && !file.isDirectory())
            	file.mkdirs();
            /************/
            
            String fm = "";
            String augment ="";
            String dead ="";
            String mandatory ="";
            String seed ="";
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
            		dead = fm + ".dead";
            		mandatory = fm + ".mandatory";
            		seed = fm + ".richseed";
                
            		// SATIBEAv3_variants
            		int loop = repetition;
            		while(loop > 0){
            			/********************/
            			outputFileSATibea = Outpath + systems[i] + ".SATibeav3-"+(program_timeout_ms[count]/6000)+"m.out." + loop;

            			p = new SATIBEA_Problem(fm, augment, mandatory, dead, seed);
            			a = new SATIBEA_SettingsIBEAv3(p).configureSATIBEAv3(program_timeout_ms[count], fm, 
            					((SATIBEA_Problem) p).getNumFeatures(), 
            					((SATIBEA_Problem) p).getConstraints(),outputFileSATibea);
            			/********************/
            			long start = System.currentTimeMillis();
            			SolutionSet pop = a.execute();
            			System.out.println("evaluation times：SATibeav3-"+(program_timeout_ms[count]/6000)+"m_"+
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
            			/********************/
            			loop--;
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
