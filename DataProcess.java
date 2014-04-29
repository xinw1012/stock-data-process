import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;

public class DataProcess {
	String cdir;    // path for storing the data file and outputs.
	String fileName;
	String referName;
	HashMap<String, ArrayList<ArrayList<String>>> content;
	ArrayList<String> indexArrayList;
	ArrayList<String> selectArrayList;

	public DataProcess(String dir, String name,String refer) throws IOException {
		this.cdir = dir;
		this.fileName = name;
		this.referName = refer;
		this.indexArrayList = new ArrayList<String>(); //store the stock index
		this.content = constructHash(); // store the data 
		this.selectArrayList=referenceIndex();
	}

	/*
	 * BASIC IDEA:
	 * Using hashtable to construct the source data.(8 columns, 
	 * I have removed the useless information under Linux)
	 * 
	 * Store the stock index as the key for the hashtable.
	 * The element in the hashtable corresponding to each stock index is a 7-dimension ArrayList.
	 * For example, there are 120 records for stock 000001, then the length of the arrays should be 120.
	 * If the seven subarrays are not in the same length, the function will return error information 
	 * to remind you.
	 */
	public ArrayList<String> referenceIndex() throws IOException {
		ArrayList<String> ref = new ArrayList<String>();
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(this.cdir+this.referName)));
		String line = br.readLine();
		while(line!=null) {
			ref.add(line);
			line = br.readLine();
		}
		br.close();
		return ref;
	}
	
	public HashMap<String, ArrayList<ArrayList<String>>> constructHash() throws IOException{
		this.content = new HashMap<String, ArrayList<ArrayList<String>>>();
		BufferedReader br = null;
		br = new BufferedReader(new InputStreamReader(new FileInputStream(this.cdir+this.fileName)));
		String line=null;
		line = br.readLine();
		while (line != null) {
			String[] cont = line.split("\t");
			if(cont.length==8 && Double.parseDouble(cont[6])>0 && Double.parseDouble(cont[7]) >= 100 ){
				/*
				 * Remove False Data
				 */
				if (this.content.containsKey(cont[0]) == false) {
					this.indexArrayList.add(cont[0]);
					ArrayList<ArrayList<String>> arr = new ArrayList<ArrayList<String>>(); 
					for (int i = 0; i < 7; ++i) {
						ArrayList<String> list = new ArrayList<String>();
						list.add(cont[i + 1]);
						arr.add(list);
					}
					this.content.put(cont[0], arr);
				} else {
					ArrayList<ArrayList<String>> temp = this.content.get(cont[0]);
					for (int i = 0; i < 7; ++i) {
						ArrayList<String> sub = temp.get(i);
						sub.add(cont[i + 1]);
					}
				}
				line = br.readLine();
			} else{
				line = br.readLine();
			}
		}

		br.close();
		System.out.println("Finish constructing hashtable!");
		return this.content;
	}
	
	/*
	 * get William Index
	 * 
	 * calculate all the features from 26th day on.
	 * 
	 * n1 in our case is 15 days, and n2 is 10. That means we use the data of the first 10 days.
	 */
	public ArrayList<String> getWilliamIndex(Integer n) {
		ArrayList<String> str = new ArrayList<String>();
		for(String key:this.indexArrayList){
			if (this.selectArrayList.contains(key)) {
				ArrayList<ArrayList<String>> cont = this.content.get(key);
				int len = cont.get(0).size();
				for(int j=26;j<len;++j) {
					Double max = 0.0;
					Double min = 0.0;
					for(int k=0;k<n;++k){
						Double item = Double.parseDouble(cont.get(4).get(j-k));
						/*
						 * cont.get(4) means to get the price list. 
						 */
						if(item > max)
							max = item;
						if(item < min)
							min = item;
					}
					Double current = Double.parseDouble(cont.get(4).get(j));
					Double williamIndex = (max - current) / (max - min) * 100;	
					/*
					 * the following for loop is to add the source data.
					 */
					String tem = Double.toString(williamIndex);
					str.add(tem);
				}
			} else{
				System.out.println("Stock index not in the selected list!");
			}
		}
		System.out.println("size of william is "+str.size());
		return str;
	}
	
	/*
	 * get RSV
	 * in our case n is 15 days.
	 * Return  K, D, J
	 */
	public ArrayList<String> getRSVKDJ(Integer n){
		ArrayList<String> str = new ArrayList<String>();
		for (String key : this.indexArrayList){
			if (this.selectArrayList.contains(key)){
				ArrayList<ArrayList<String>> cont = this.content.get(key);
				int len = (cont.get(0)).size();
				ArrayList<Double> kValue = new ArrayList<Double>();
				ArrayList<Double> dValue = new ArrayList<Double>();
				for(int j=26;j<len;++j){
					Double max = 0.0;
					Double min = 0.0;
					for(int k=0;k<n;++k){
						Double item = Double.parseDouble(cont.get(4).get(j-k));
						if(item > max)
							max = item;
						if(item < min)
							min = item;
					}
					Double current = Double.parseDouble(cont.get(4).get(j));
					Double RSV= (current - min) / (max - min) * 100;
					Double k_val = null, d_val = null, j_val = null;
					if(j>26){
						k_val = 2/3.0*kValue.get(j-27)+1/3*RSV;
						d_val = 2/3.0*dValue.get(j-27)+1/3*k_val;	
						j_val = 3*d_val - 2*k_val;
						kValue.add(k_val);
						dValue.add(d_val);
					}
					else{
						k_val = 2/3.0*50.0+1/3*RSV;
						d_val = 2/3.0*50.0+1/3*k_val;	
						j_val = 3*d_val - 2*k_val;
						kValue.add(k_val);
						dValue.add(d_val);
					}
					String tem =Double.toString(k_val)+"\t"+Double.toString(d_val)+"\t"+Double.toString(j_val);
					str.add(tem);
				}
			} else{
				System.out.println("Stock index not in the selected list!");
			}
		}
		System.out.println("size of kdj is "+str.size());
		return str;
	}
	
	/*
	 * getMA
	 */
	public ArrayList<String> getMA(int duration1, int duration2) {
		ArrayList<String> MA = new ArrayList<String>();
		int j = 0;
		int k = 0;
		for(String key:this.indexArrayList){
			if(this.selectArrayList.contains(key)) {
				ArrayList<ArrayList<String>> cont = this.content.get(key);
				int len = (cont.get(0)).size();
				double ma1 = 0;
				double ma2 = 0;
				for(j = 26; j < len; ++j) {
					double current = Double.parseDouble(cont.get(5).get(j)); //price today
					double average = 0;
					for(k = 0; k < duration1; k++) {
						average += Double.parseDouble(cont.get(5).get(j-k));
					}
					average = average / (double)duration1;
					ma1 = Math.log(current/average);
					for(k = 0; k < duration2; k++) {
						average += Double.parseDouble(cont.get(5).get(j-k));
					}
					average = average / (double)duration2;
					ma2 = Math.log(current/average);
					
					String tem = Double.toString(ma1)+"\t"+Double.toString(ma2);
					MA.add(tem);
				}
			} else{
				System.out.println("Stock index not in the selected list!");
			}
		}
		return MA;
	}
	
	/*
	 * getY
	 */
	public ArrayList<String> getY(int n, double percent){
		ArrayList<String> str = new ArrayList<String>();
		for (String key:this.indexArrayList){
			if (this.selectArrayList.contains(key)){
				ArrayList<ArrayList<String>> cont = this.content.get(key);
				int len = (cont.get(0)).size();
				for(int j=26;j<len;++j){
					Double current = Double.parseDouble(cont.get(4).get(j)); //price today
					Double previous = Double.parseDouble(cont.get(4).get(j-n)); //price previous
					String y = "-1";
					if(current>previous*(1+percent))
						y = "1";
					else if (current<previous*(1-percent)) {
						y = "0";
					}
					String tem = y+"\t"+key +"\t"+ Integer.toString(j)+"\t";
					for(int m =4;m<=6;++m){
						Double currentDouble = Double.parseDouble(cont.get(m).get(j));
						Double previousDouble = Double.parseDouble(cont.get(m).get(j-1));
						tem +=Double.toString(Math.log(currentDouble/previousDouble));
						if(m<6){
							tem+="\t";
						}
					}
					str.add(tem);
				}
			}
			else{
				System.out.println("Stock index not in the selected list!");
			}
		}
		System.out.println("size of y is "+str.size());
		return str;
	}
	
	public String getOutput() throws IOException{
		ArrayList<String> wiliam1 = getWilliamIndex(5);
		ArrayList<String> wiliam2 = getWilliamIndex(14);
		ArrayList<String> wiliam3 = getWilliamIndex(20);
		ArrayList<String> kdj = getRSVKDJ(5);
		ArrayList<String> ma = getMA(5,10);
		ArrayList<String> yArrayList = getY(5,0);
		String pathString = this.cdir+"output_2012_5_0_selected.txt";
		FileWriter file = new FileWriter(pathString);
        BufferedWriter output = new BufferedWriter(file);
        //output.write("Y\tStkcd\tIndex\tOpendata\tOpnprc\tHiprc\tLoprc\tClsprc\tDnshrtrd\tDnvaltrd
        //\tWilliamIndex\tRSV\tK\tD\tJ\tEMA12\tEMA26\tDIF\tDEA\tMACD\n");
		if (wiliam1.size()==kdj.size() && kdj.size() == ma.size()) {
	        for(int i=0;i<wiliam1.size();++i){
				String res = yArrayList.get(i)+"\t"+wiliam1.get(i)+"\t"+wiliam2.get(i)+"\t"+wiliam3.get(i)
						+"\t"+kdj.get(i)+"\t"+ma.get(i)+"\n";
				String[] y1 = ((String) yArrayList.get(i)).split("\t");
				if (!y1[0].equals("-1")) {
					output.write(res);
				
				} else {
					System.out.println("Removed record!");
				}
			}
			output.close();
		} else {
			System.out.println("The arrays are not in the same length!");
		}
		return pathString;
	}
	
	public void getTotalOutput(String path) throws IOException{
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(path)));
		String line = br.readLine();
		HashMap<String, ArrayList<String>> hashtable = new HashMap<String, ArrayList<String>>();
		ArrayList<String> Index = new ArrayList<String>();
		while(line!=null){
			String[] contStrings = line.split("\t");
			if(!hashtable.containsKey(contStrings[1])){
				ArrayList<String> list = new ArrayList<String>();
				String resString = "";
				for(int i=0;i<contStrings.length;++i){
					if(i!=1 && i!=2){
						resString+=contStrings[i];
						if(i==0){
							resString+=",";
						} else {
							if(i<contStrings.length-1){
								resString+="\t";
							}
						}
					}
				}
				list.add(resString);
				hashtable.put(contStrings[1], list);
				Index.add(contStrings[1]);
			}
			else{
				ArrayList<String> list = hashtable.get(contStrings[1]);
				String resString = "";
				for(int i=0;i<contStrings.length;++i){
					if(i!=1 && i!=2){
						resString+=contStrings[i];
						if(i==0){
							resString+=",";
						} else {
							if(i<contStrings.length-1){
								resString+="\t";
							}
						}
					}
				}
				list.add(resString);
				hashtable.remove(contStrings[1]);
				hashtable.put(contStrings[1], list);
			}
			line = br.readLine();
		}
		br.close();
		
		String pathString = this.cdir+"output_2012_total_selected.txt";
		File file = new File(pathString);
        BufferedWriter output = new BufferedWriter(new FileWriter(file));
		for(String key:Index){
			ArrayList<String> listStrings = hashtable.get(key);
			if(listStrings.size()>=15){
				for(int i=9;i<listStrings.size();++i){
					String[] strings = listStrings.get(i).split(",");
					String ret = strings[0]+"\t";
					for(int j=i-9;j<=i;++j){
						String[] strs = listStrings.get(j).split(",");
						ret+=strs[1];
						if(j<i){
							ret+="\t";
						}
					}
					ret+="\n";
					output.write(ret);
				}
			}
			else{
				System.out.println("records are not enough!");
			}
		}
		output.close();
	}
	
	public static void main(String[] args) throws IOException{
		DataProcess hash = new DataProcess("/Users/liyingkai/Documents/code/","2012.txt","stockindex.txt");
		String filePath = hash.getOutput();
		hash.getTotalOutput(filePath);
		System.out.println("Finish!");
	}
}
