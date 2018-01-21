This website provides the supplementary stuff of the paper **"To Preserve or Not to Preserve Invalid Solutions in Search-Based Software Engineering: A Case Study in Software Product Lines"**, accepted by *ICSE'18: the 40th International Conference on Software Engineering, May 27 - 3 June 2018, Gothenburg, Sweden*

# Subjects
We evaluated seven subjects in software produce lines: _eCos_, _FreeBSD_, _Fiasco_, _uClinux_, _Linux_, _Drupal_, and _AmazonEC2_. The data of these subjects, including feature models, attribute values, and attribute constraints, are publicly available. Here are the sources:
* _eCos_, _FreeBSD_, _Fiasco_, _uClinux_, _Linux_ : [1]
* _Drupal_ : [2]
* _AmazonEC2_ : [3]

# Algorithms and Results
We used _SATIBEA_ [1] as a baseline and designed five algorithm variants (_SATIBEAv1_, _SATIBEAv2_, _SATIBEAv3_, _SATIBEAv4_, and _SATIBEAv5_) that incorporate SAT solving into the initial population generation and the mutation operator of IBEA in different ways. Moreover, one algorithm (_SATIBEAv5_) designed to preserve valid solutions all along the way further incorporates a subroutine (_src/lib/amazonMapping.jar_) that resolves non-Boolean constraints over integer or real-number variables together with arithmetic or relational operators, which cannot be straightforwardly addressed by the approaches relying on SAT solving.
* _src/lib/_  includes the core jar packages used by all algorithms.
* _src/common/_  includes the common Jave code of all algorithms. The common code must be combined with the specific code (if it exists) to run a particular algorithm for a certain subject. 
* _results/_  includes the experimental results for each algorithm and for each subject.

# References
[1] Christopher Henard, Mike Papadakis, Mark Harman, and Yves Le Traon. 2015. Combining multi-objective search and constraint solving for configuring large software product lines. In Proceedings of 37th IEEE/ACM International Conference on Software Engineering (ICSE). 517–528.

[2] Ana B. Sánchez, Sergio Segura, José Antonio Parejo, and Antonio Ruiz Cortés. 2017. Variability testing in the wild: the Drupal case study. Software & Systems Modeling 16, 1 (2017), 173–194.

[3] Jesús García-Galán, Pablo Trinidad, Omer F. Rana, and Antonio Ruiz Cortés. 2016. Automated configuration support for infrastructure migration to the cloud. Future Generation Comp. Syst. 55 (2016), 200–212.
