# Hadoop-Application-for-Traveling-Salesman-Problem
This is a Map-Reduce application for Haddoop that parallelizes the Branch and Bound algorithm for Traveling Salesman Problem (TSP).

## Description of the classes
### Map class
Below are some important functions with their description

setup():

This setup-function is loading the counter "global" from the context objectto a class-wide-accesable score variable. The other part of setup() loads the input matrix file from the HDFS ("hdfs://hadoopMaster:9000/user/hduser/BNB/matrix.txt") to a class-wide-accesable array. Please feel free to modify the path (line 45) for your HDFS.

map():

The key is representing the line of the input file, the value is the input line and the context object is the one to write the output of the map function. The Information that are writen in the value string are saved to an integer array. The first number in
that array is the length of the route, and from that the length of the branch can be calculated (length of route minus length of the array). The following number or numbers are the points to visit. With those numbers the rest of the route is calculated. If there are multiple routes possible, because there are free points to pick, those points are added. For each possible route the score is calculated and compared to the preloaded score from the setup function. If the score of this route is better than the class-wide-accesable score the class-wide-accesable score is updated. Otherwise the score is completly calculated, the calculation is skipped and the next route is started. The key is not needed in this implementation.

cleanup(): 

Before the Mapper is closed, the cleanup function writes the best route and its score to the context file. The writen output pair is the route as a new Text object,containing a string, plus score as a new DoubleWritable, containing a double.

### Reduce class
Below are some important functions with their description

reduce():

In this function a HashMap defined in the setup function is populated with the results from all mappers of the Map class.

cleanup(): 

This function finds the best route in the routes in HashMap populated in the reduce() function and write it to the context

## Usage of the application
### Input
Two input files are needed. One is the input distance matrix, cotaining the distance between any pair of points to be traversed. So for n points, the input matrix is n times n. The value of the pair (a,b) and pair (b,a) does not have to be the same, but can be. The other input file contains lines representing one part of a route. The number of points for the pre-set route is depending on the number of splits (map-reduce jobs) wanted by the user. The number of input lines should be higher than the number of Datanodes in the cluster, otherwise some Datanodes will not process data.

A sample distance matrix and input point files are provided in the repository.

### Running the application
A hadoop Job could be started by the console command " hadoop jar [.jar-file] [input folder][output folder] ", giving the direction and name of the .jar file, the input folder, the output folder. 

### Output
The output file contains the line-up of the points building the route and the score of the best route.

