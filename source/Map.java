package main;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Arrays;
import java.util.Enumeration;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.log4j.Logger;

public class Map extends Mapper<LongWritable, Text, Text, DoubleWritable> {

    private Logger logger = Logger.getLogger(Map.class);

    private double score;
    private double[][] distanceMatrix;
    private int routePoints[];
    private double firstScore = 0;
    private double localScore = 0;
    private int[] bestRoute;
    private int[] bestStart;
    


    @Override
    protected void setup(Mapper<LongWritable, Text, Text, DoubleWritable>.Context context)
            throws IOException, InterruptedException {
        //load score
        score = context.getCounter("global", "global").getValue();
        //load matrix
        int length = 0;
        int count = 0;
        String[] distMatValues;
        try {
            Path pt = new Path("hdfs://hadoopMaster:9000/user/hduser/BNB/matrix.txt");//location for the distance matrix
            FileSystem fs = FileSystem.get(new Configuration());
            BufferedReader br = new BufferedReader(new InputStreamReader(fs.open(pt)));
            String line = br.readLine();
            //read number of points
            if (line != null) {
                length = line.split(";").length;
            }
            //for each line: read points and save to matrix
            distanceMatrix = new double[length][length];
            while (line != null) {
                distMatValues = line.split(";");
                for (int i = 0; i < distMatValues.length; i++) {
                    distanceMatrix[count][i] = Double.parseDouble(distMatValues[i]);
                }
                count += 1;
                line = br.readLine();
            }
        } catch (Exception e) {
            logger.error(e);
        }
    }

    @Override
    public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
        String valueString = value.toString();
        String[] arrayOfInputLine = valueString.split(";");
        int lengthOfRoute = Integer.parseInt(arrayOfInputLine[0]);
        int lengthOfBranch = lengthOfRoute - (arrayOfInputLine.length - 1);

        routePoints = new int[arrayOfInputLine.length - 1];
        for (int i = 0; i < routePoints.length; i++) {
            routePoints[i] = Integer.parseInt(arrayOfInputLine[i + 1]);
        }
        //calculate route to splitpoint
        firstScore = calculateRoute(routePoints);
        //setup routes
        int [] sequence= setup(lengthOfBranch, lengthOfRoute);
        //permute and calculate route
        permute(sequence, lengthOfBranch);
    }

    @Override
    protected void cleanup(
            Mapper<LongWritable, Text, Text, DoubleWritable>.Context context)
            throws IOException, InterruptedException {
        //produce route tag
        String tag = "0;";
        for (int i = 1; i < bestStart.length; i++) {
            tag += bestStart[i] + ";";
        }
        for (int i = 0; i < bestRoute.length; i++) {
            tag += bestRoute[i] + ";";
        }
        context.getCounter("global", "global").setValue((long) score);	//save global score
        //upload best complete score to context (incl. route)
        context.write(new Text(tag), new DoubleWritable(score));//save route score to context
    }

    /**
     * Calculating Route between multiple points
     *
     * @param points arrayOfInputLine of points
     * @return Distance between the points
     */
    public double calculateRoute(int points[]) {
        double route = 0;
        for (int i = 0; i < points.length - 1; i++) {
            route += distanceMatrix[points[i]] [points[i + 1]];
        }
        return route;
    }

    private void swap(int[] v, int i, int j) {
        int t = v[i];
        v[i] = v[j];
        v[j] = t;
    }

    public void permute(int[] v, int n) {
        if (n == 1) {
            work(v);
        } else {
            for (int i = 0; i < n; i++) {
                permute(v, n - 1);
                if (n % 2 == 1) {
                    swap(v, 0, n - 1);
                } else {
                    swap(v, i, n - 1);
                }
            }
        }
    }

    public int[] setup(int branchLength, int routeLength) {

        int[] sequence = new int[branchLength];
        int[] seq = new int[routeLength + 1];
        seq[0] = 1;
        for (int j = 0; j < routePoints.length; j++) {
            seq[routePoints[j]] = 1;
        }
        for (int i = 0; i < sequence.length; i++) {
            if (seq[i] == 0) {
                sequence[i] = i;
                seq[i] = 1;
            } else {
                for (int j = 0; j < seq.length; j++) {
                    if (seq[j] == 0 && !contains(j, sequence)) {
                        sequence[i] = j;
                        seq[j] = 1;
                        break;
                    }
                }
            }
        }
        return sequence;
    }

    public boolean contains(int n, int[] a) {
        Arrays.sort(a);
        return Arrays.binarySearch(a, n)!= -1;
    }

    public void work(int[] perm) {
        boolean noScore = false;
        int[] vector = perm.clone();
        //calculate route
        localScore = firstScore
                + distanceMatrix[routePoints[routePoints.length - 1]] [vector[0]]
                + distanceMatrix[vector[vector.length - 1]] [routePoints[0]];
        noScore = false;
        for (int i = 0; i < vector.length - 1; i++) {
            //check score
            if (localScore < score || score == 0) {
                localScore += distanceMatrix[vector[i]] [vector[i + 1]];
            } //bound hit
            else {
                noScore = true;
                break;
            }
        }
        //set score
        if (!noScore) {
            if (localScore < score || score == 0) {
                bestRoute = vector.clone();
                bestStart = routePoints.clone();
                score = localScore;
            }
        }
    }

    public void checkIP() throws SocketException {
        String ip = "";
        Enumeration e = NetworkInterface.getNetworkInterfaces();
        while (e.hasMoreElements()) {
            NetworkInterface n = (NetworkInterface) e.nextElement();
            Enumeration ee = n.getInetAddresses();
            while (ee.hasMoreElements()) {
                InetAddress i = (InetAddress) ee.nextElement();
                ip = i.getHostAddress();
                logger.info("IP of this Map Class: " + ip);
            }
        }
        logger.info("");
    }
}
