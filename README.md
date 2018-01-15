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


For two real-world SPLs:


