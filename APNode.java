package APCluster;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.LinkedList;



public class APNode {
	class Incomplete{
		int index; // 元组标号
		ArrayList<Double> numData;
		ArrayList<String> claData;
		int miss;
		public Incomplete(int index, ArrayList<Double> numData, ArrayList<String> claData, int miss) {
			super();
			this.index = index;
			this.numData = numData;
			this.claData = claData;
			this.miss = miss;
		}
		@Override
		public String toString() {
			return "Incomplete [index=" + index + ", numData=" + numData + ", claData=" + claData + ", miss=" + miss
					+ "]";
		}
		
	}
	class Complete{
		int index; // 元组标号
		ArrayList<Double> numData;
		ArrayList<String> claData;
		public Complete(int index, ArrayList<Double> numData, ArrayList<String> claData) {
			super();
			this.index = index;
			this.numData = numData;
			this.claData = claData;
		}
		@Override
		public String toString() {
			return "Complete [index=" + index + ", numData=" + numData + ", claData=" + claData + "]";
		}
		
	}
	int NN,CN;
	LinkedList<Incomplete> incompleteData;
	LinkedList<Complete> completeData;
	ArrayList<ArrayList<Double>> similarity;
	public APNode() {
		super();
		NN=CN=0;
		similarity = new ArrayList<ArrayList<Double>>();
		incompleteData = new LinkedList<Incomplete>();
		completeData = new LinkedList<Complete>();
	}
	public void dataInputFromFile(String filePath,String split){
		File file=new File(filePath);
		if(!file.isFile() || !file.exists()){ //判断文件是否存在
            System.out.println("找不到指定的文件");
            return;
        }
		String encoding="UTF-8";
		try(
				BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(//考虑到编码格式
                     	new FileInputStream(file),encoding));
				){
			String linedata = bufferedReader.readLine();
			String[] Mark = linedata.split(split);
			for(String m:Mark){
				if(m.equals("Num")){
          			NN++;
          		}else if(m.equals("Cla")){
          			CN++;
          		}
			}
			int count=1;
          	while((linedata = bufferedReader.readLine()) != null){
          		ArrayList<Double> numData = NN>0?new ArrayList<Double>():null;
          		ArrayList<String> claData = CN>0?new ArrayList<String>():null;
          		int miss=0;
          		String[] data = linedata.split(split);
              	for(int i=0; i<Mark.length; i++){
              		if(Mark[i].equals("Num")){
              			double value;
              			if(data[i].equals("?")){
              				miss++;
              				value = Double.NaN;
              			}else{
              				value = Double.parseDouble(data[i]);
              			}
              			numData.add(value);
              		}else if(Mark[i].equals("Cla")){
              			if(data[i].equals("?")) miss++;
              			claData.add(data[i]);
              		}
              	}
              	if(miss>0){
              		incompleteData.add(new Incomplete(count++,numData,claData,miss));
              	}else{
              		completeData.add(new Complete(count++,numData,claData));
              	}
           	}
          	incompleteData.sort((o1,o2)->(o1.miss-o2.miss));
          	if(NN>0) Tools.getMaxMin(this);
        } catch (Exception e) {
            System.out.println("读取文件内容出错");
            e.printStackTrace();
        }
    }
	public void calculateSimlarity(){
		int num = completeData.size();
		for(int i=0; i<num; i++){
			ArrayList<Double> s = new ArrayList<>();
			for(int j=0; j<num; j++){
				s.add(Tools.compareSimilarity(completeData.get(i), completeData.get(j)));
			}
			similarity.add(s);
		}
	}
	public void updateSimilarity(Incomplete node){
		int num = completeData.size();
		ArrayList<Double> s = new ArrayList<>();
		for(int i=0; i<num; i++){
			double stmp = Tools.compareSimilarity(completeData.get(i), node);
			similarity.get(i).add(stmp);
			s.add(stmp);
		}
		s.add(new Double(0));
		similarity.add(s);
	}
	public void transfer(){
		while(!incompleteData.isEmpty()&&incompleteData.getFirst().miss==0){
			Incomplete tmp = incompleteData.removeFirst();
			updateSimilarity(tmp);
			completeData.add(new Complete(tmp.index,tmp.numData, tmp.claData));
		}
	}
	public void showData(){
		for(Complete j:completeData){
			System.out.println(j);
		}
		for(Incomplete i:incompleteData){
			System.out.println(i);
		}
	}
	public static void main(String[] args){
		APNode test = new APNode();
		test.dataInputFromFile("./data/winedata",",");
		for(Complete j:test.completeData){
			System.out.println(j);
		}
		for(Incomplete i:test.incompleteData){
			System.out.println(i);
		}
		test.calculateSimlarity();
		for(ArrayList<Double> m:test.similarity){
			System.out.println(m);
		}
	}
}
