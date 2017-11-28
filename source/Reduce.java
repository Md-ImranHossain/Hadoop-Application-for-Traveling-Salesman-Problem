package main;

import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.log4j.Logger;

public class Reduce extends Reducer<Text, DoubleWritable, Text, DoubleWritable> {

    private Logger logger = Logger.getLogger(Map.class);
    Map<String, Double> listFromMaps;

    @Override
    protected void setup(org.apache.hadoop.mapreduce.Reducer<Text, DoubleWritable, Text, DoubleWritable>.Context context)
            throws IOException, InterruptedException {
        listFromMaps = new HashMap<>();
    }

    @Override
    public void reduce(Text keys, Iterable<DoubleWritable> values, Context context)
            throws IOException, InterruptedException {
        String key = keys.toString();
        double value = 0;
        Iterator<DoubleWritable> iterator = values.iterator();
        if (iterator.hasNext()) {
            value = iterator.next().get();
        }
        listFromMaps.put(key, value);
    }

    @Override
    protected void cleanup(org.apache.hadoop.mapreduce.Reducer<Text, DoubleWritable, Text, DoubleWritable>.Context context) throws IOException, InterruptedException {

        Entry<String, Double> min = Collections.min(listFromMaps.entrySet(),
                Comparator.comparingDouble(Entry::getValue));

        Text outputText = new Text(min.getKey());
        DoubleWritable outputValue = new DoubleWritable(min.getValue());
        logger.info("Best Path: " + outputText + " Best Score = " + outputValue);
        context.write(outputText, outputValue);
    }
}
