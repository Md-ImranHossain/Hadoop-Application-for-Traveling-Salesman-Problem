# Hadoop-Application-for-Traveling-Salesman-Problem
This is a Map-Reduce application for Haddoop that parallelizes the Branch and Bound algorithm for Traveling Salesman Problem (TSP).

## Description of the classes
### Map class
Below are some important functions with their description

setup()

This setup-function is loading the counter "global" from the context objectto a class-wide-accesable score variable. The other part of setup() loads the input matrix file from the HDFS ("hdfs://hadoopMaster:9000/user/hduser/BNB/matrix.txt")to a class-wide-accesable array. 



