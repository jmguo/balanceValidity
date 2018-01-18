# balanceValidity
To Preserve or Not to Preserve Invalid Solutions in Search-Based Software Engineering: A Case Study in Software Product Lines

Introduction

This work releases five different SATIBEA variants that combins the IBEA (Indicator-Based Evolutionary Algorithm)
with the SAT (Boolean satisfiability problem) solving to address the configuration optimization problem 
in five real-world SPLs (Linux, eCos, Fiasco, FreeBSD and uClinux). 
The original SATIBEA [1] is the first hybird method that combinges SAT sovling with the muation operator of the IBEA algorithm.
Specially, we also release two additional algorihtms for solving two SPLs
with realisitc values and constraints of quality attributes (AmazonEC2 and Drupal).

The implementation framework of each algorithm variant adopts the IBEA algorithm template
of jMetal[2,3], an open-source Java-based framework for multi-objective optimization with metaheuristics.
Moreover, the constraint solving uses a widely deploy SAT solver Sat4j[4] as the underlying SAT solver.

In this repository, we release the source code and the origianl experimental results. 
Researchers firstly need to conduct seven feature models mentioned in above so that they can run our algorithms successfully. 
The feature model of Linux, eCos, Fiasco, FreeBSD and uClinux can refer to Henard et al.'s [1].
The feature model of Drupal and AmazonEC2 can refer to [5, 6], respectively.

---------------------------------------------------------------------------------------------------------------
References

[1] Christopher Henard, Mike Papadakis, Mark Harman, and Yves Le Traon. 2015. Combining multi-objective search and constraint solving for configuring large software product lines. In Proceedings of 37th IEEE/ACM International Conference on Software Engineering (ICSE). 517–528.

[2] J. J. Durillo, A. J. Nebro, and E. Alba, “The jMetal framework for multi-objective optimization: Design and architecture,” in Proceedings of the IEEE Congress on Evolutionary Computation (CEC), Barcelona, Spain. IEEE, pp. 4138–4325.

[3] Juan J. Durillo and Antonio J. Nebro. 2011. jMetal: A Java framework for multiobjective optimization. Advances in Engineering Software 42 (2011), 760–771.

[4] http://www.sat4j.org/

[5] Jesús García-Galán, Pablo Trinidad, Omer F. Rana, and Antonio Ruiz Cortés. 2016. Automated configuration support for infrastructure migration to the cloud. Future Generation Comp. Syst. 55 (2016), 200–212.

[6] Ana B. Sánchez, Sergio Segura, José Antonio Parejo, and Antonio Ruiz Cortés. 2017. Variability testing in the wild: the Drupal case study. Software and System Modeling 16, 1 (2017), 173–194.

---------------------------------------------------------------------------------------------------------------


How to run?

For five SPLs:
        1. The core jar packages used by all variants are in "lib" folder. They must be importted into a project.
	2. The general jave files of five SATIBEA variants are in "Common" folder.
	3. Each variant framework is composed of the "Common" folder and a special algorithm folder including SATIBEA, SATIBEAv1, SATIBEAv2, SATIBEAv3, SATIBEAv4 and SATIBEAv5

We mainly introduce the source code of the SATIBEA. The others are similar to the SATIBEA.
   1. The Common folder contains the following files:
	 SAT_Decision.java: a customized file of performing constriant checking and 
                              a function returning a valid configuration of the feature model.
					
	 WriteStreamAppend.java: this code can be used to recored the experimential results and 
                                  important details during the whole process of algorithm running.

	 SATIBEA_BinarySolution.java: the structure of the solution.						
						
	 SATIBEA_BitFlipMutation.java: the soruce code of the bit-flip muation operation.

	 SATIBEA_SinglePointCrossover.java: the source code of the singlepoint crossover operation.

	 SATIBEA_Problem.java: we define the structure of optimization problem which shoule be solved.
				This code shoule input the featue model file, the constraint file and 
                                the attribute file of featues.


    2. The SATIBEA folder contains the following files:		
	 IBEATimeLimited.java: we perform the SATIBEA algorithm evaluation with the terminal condition.

	 SATIBEA_SettingsIBEA.java: we configure the SATIBEA algorithm, 
					including problem, the crossover operation, the muation operation, 
                                        the seleciton operation and the parameter setting of populationsize, 
                                        mutationProbability and crossoverProbability. 
						
	 SATIBEA_NewMutation.java: a smart mutation strategy 
					in which SAT solving is invoked with a probability to return a valid 
                                        solution during the mutation operation. Meanwhile, this strategy includes 
                                        the standard bit-flip mutation and SAT sovling.

	 SATIBEA_Variants_Main.java: the main entrace to an algorithm. 
					Please, starting from here, the entrie process is automated.						

						
     3. The SATIBEAv2 algorithm is composed of the SATIBEAv2 folder and part code file in Common folder (SATIBEA_BinarySolution.java, SAT_Decision.java, WriteStreamAppend.java and SATIBEA_SinglePointCrossover.java). Specially, The "RichSeedGen.java" is used to generate a solution according to the rich seed.

---------------------------------------------------------------------------------------------------------------	

For two real-world SPLs:

     1. For two real-world SPLs:
	 The "realSubjects" folder is the soruce code of SATIBEA and SATIBEAv5 for address two real-world SPLs.
	 For Drupal, the folder structure is similar to the above descripiton.
		 realSubjects/SATIBEA_Drupal/Common: the general source codes
		 realSubjects/SATIBEA_Drupal/SATIBEA_Drupal: the core source codes of SATIBEA
		 realSubjects/SATIBEA_Drupal/SATIBEAv5_Drupal: the core source codes of SATIBEAv5

	 For Amazon, the complete source codes of SATIBEAv5 are given in "realSubjects/SATIBEAv5_Amazon".
