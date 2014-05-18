import org.apache.hadoop.fs.Path;
import org.apache.hadoop.conf.*;
import org.apache.hadoop.io.*;
import org.apache.hadoop.mapred.*;
import org.apache.hadoop.util.*;
// This code is based on the standard word count examples
// you can find almost everywhere.
// We modify the map function to be able to aggregate based on
// crime type.
// The reduce function as well as main is unchanged,
// except for the name of the job.
public class CrimeCount {
public static class Map extends MapReduceBase implements 
  Mapper<LongWritable, Text, Text, IntWritable> {
    private final static IntWritable one = new IntWritable(1);
    private Text word = new Text();
    private String mytoken = null;
    public void map(LongWritable key, Text value,
                    OutputCollector<Text, IntWritable> output,
                    Reporter reporter) throws IOException {
      // Read one line from the input file.
      String line = value.toString();
      // Parse the line into separate columns.
      String[] myarray = line.split(",");
      // Verify that there are at least three columns.
      if(myarray.length >= 2){
        // Grab the third column and increment that
        // crime (i.e. LOST PROPERTY found, so add 1).
        mytoken = myarray[2];
        word.set(mytoken);
        // Add the key/value pair to the output.
        output.collect(word, one);
      }
    }
  // A fairly generic implementation of reduce.
  public static class Reduce extends MapReduceBase implements Reducer<Text,
    IntWritable, Text, IntWritable> {
    public void reduce(Text key, Iterator<IntWritable> values,
                               OutputCollector<Text,
                               IntWritable> output,
                               Reporter reporter) throws IOException {
      // Loop through an aggregate key/value pairs.
      int sum = 0;
      while (values.hasNext()) {
        sum += values.next().get();
      }
      output.collect(key, new IntWritable(sum));
    }
  }
  // Kick off the MapReduce job, specifying the map and reduce
  // construct, as well as input and output parameters.
  public static void main(String[] args) throws Exception {
    JobConf conf = new JobConf(CrimeCount.class);
    conf.setJobName("crimecount");
    conf.setOutputKeyClass(Text.class);
    conf.setOutputValueClass(IntWritable.class);
    conf.setMapperClass(Map.class);
    conf.setCombinerClass(Reduce.class);
    conf.setReducerClass(Reduce.class);
    conf.setInputFormat(TextInputFormat.class);
    conf.setOutputFormat(TextOutputFormat.class);
    FileInputFormat.setInputPaths(conf, new Path(args[0]));
    FileOutputFormat.setOutputPath(conf, new Path(args[1]));
    JobClient.runJob(conf);
  }
}
