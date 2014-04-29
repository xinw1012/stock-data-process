For the source data.csv, you may first use the following commands under Linux to pre-process it.

Step 1: Remove useless information 
awk '{print $1"\t"$2"\t"$3"\t"$4"\t"$5"\t"$6"\t"$7"\t"$8"\t"}' data.csv > data_removed.txt

Step 2: Select the data in the year 2012 and year 2013.
cat data_removed.txt | grep "2012-" > data_2012.txt
cat data_removed.txt | grep "2013-" > data_2013.txt

After that, you can run the java code to get the output. As for the input, they are "data_2012.txt" 
and "data_2013.txt".
