package APCluster;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;

import APCluster.APNode.Complete;
import APCluster.APNode.Incomplete;

public class APCluster {
	double lambda;
	double stable;
	int iteration;
	int batch;
	APNode apnode;
	LinkedList<Incomplete> incompleteData;
	LinkedList<Complete> completeData;
	ArrayList<ArrayList<Double>> similarity;
	ArrayList<ArrayList<Double>> responsibility;
	ArrayList<ArrayList<Double>> availability;
	HashSet<Integer> exemplar;
	LinkedList<Integer> distributedNode;
	public APCluster(APNode apnode,double lambda,double stable,int iteration) {
		super();
		this.apnode = apnode;
		this.lambda = lambda;
		this.stable = stable;
		this.iteration = iteration;
		this.similarity = apnode.similarity;
		this.responsibility = new ArrayList<ArrayList<Double>>();
		this.availability = new ArrayList<ArrayList<Double>>();
		int num = similarity.size();
		for(int i=0; i<num; i++){
			ArrayList<Double> tmp = new ArrayList<>();
			for(int j=0; j<num; j++){
				tmp.add(0.0);
			}
			responsibility.add(tmp);
		}
		for(int i=0; i<num; i++){
			ArrayList<Double> tmp = new ArrayList<>();
			for(int j=0; j<num; j++){
				tmp.add(0.0);
			}
			availability.add(tmp);
		}
		this.exemplar = new HashSet<Integer>();
		this.distributedNode = new LinkedList<>();
		
	}
	public APCluster(APNode apnode,double lambda,double stable,int iteration,int batch) {
		this(apnode,lambda,stable,iteration);
		this.batch = batch;
		this.incompleteData = apnode.incompleteData;
		this.completeData = apnode.completeData;
	}
	//calculate the preference
	public void calculatePreference(){
		int num = similarity.size();
		//use the median as preference
		int m = 0;
		int n = num * num;
		double median = 0;
		double list[] = new double[n];
		//find the median
		for(int i=0; i<num; i++){
			for(int j=0; j<num; j++){
				list[m++]=(i==j?0:similarity.get(i).get(j));
			}
		}
		Arrays.sort(list);
		if(n%2 == 0){
			median = (list[n/2] + list[n/2-1]) / 2;
		}else{
			median = list[n/2];
		}
		for(int i=0; i<num; i++){
			similarity.get(i).set(i, median);
		}
	}
	public void calculateResponsibility(){
		int num = similarity.size();
		for(int i=0; i<num; i++){
			for(int k=0; k<num; k++){
				if(i == k){
					double max = Double.NEGATIVE_INFINITY;
					for(int j=0; j<num; j++){
						if(j != k){
							max = Math.max(max, similarity.get(i).get(j));
						}
					}
					responsibility.get(i).set(k, (1-lambda)*(similarity.get(i).get(k) - max) + lambda*responsibility.get(i).get(k));
				}else{
					double max = Double.NEGATIVE_INFINITY;
					for(int j=0; j<num; j++){
						if(j != k){
							max = Math.max(max, availability.get(i).get(j) + similarity.get(i).get(j));
						}
					}
					responsibility.get(i).set(k, (1-lambda)*(similarity.get(i).get(k) - max) + lambda*responsibility.get(i).get(k));
				}
			}
		}
	}
	public void calculateAvailability(){
		int num = similarity.size();
		for(int i=0; i<num; i++){
			for(int k=0; k<num; k++){
				if(i == k){
					double sum = 0;
					for(int j=0; j<num; j++){
						if(j != k){
							sum += Math.max(responsibility.get(j).get(k), 0.0);
						}
					}
					availability.get(i).set(k, (1-lambda)*(sum) + lambda*availability.get(i).get(k));
				}
				else{
					double sum = 0;
					for(int j=0; j<num; j++){
						if(j!=i && j!=k){
							sum += Math.max(responsibility.get(j).get(k), 0.0);
						}
					}
					sum = Math.min((responsibility.get(k).get(k) + sum), 0.0);
					availability.get(i).set(k, (1-lambda)*(sum) + lambda*availability.get(i).get(k));
				}
			}
		}
	}
	public boolean changeExamplar(){
		int num = similarity.size();
		boolean res = false;
		//find the clustering centers
		for(int k=0; k<num; k++){
			if(responsibility.get(k).get(k)+availability.get(k).get(k) > 0){
//				System.out.println("exemplar add:"+k);
				res = exemplar.add(k)||res;
			}else{
				res = exemplar.remove(k)||res;
			}
		}
		return res;
	}
	public void clustering(){
		calculatePreference();
		//iteratively calculate responsibility and availability
		int count=0;
		for(int i=0; i<iteration; i++){
			if(count>=stable){
				if(!exemplar.isEmpty()){
					break;
				}else{
					count=0;
				}
			}
			calculateResponsibility();
			calculateAvailability();
			if(changeExamplar()){
				count=0;
			}{
				count++;
			}
//			showMatrix();
//			System.out.println(exemplar);
//			System.out.println("iter " + i +" complete");
		}
		distributeNodes();
	}
	public void distributeNodes(){
		distributedNode.clear();
//		System.out.println(exemplar);
		int num = similarity.size();
		//data point assignment
		for(int i=0; i<num; i++){
			double max = Double.NEGATIVE_INFINITY;
			int index = 0;
			for(int j:exemplar){
				double tmp = i==j?0:similarity.get(i).get(j);
				if(max < tmp){
					max = tmp;
					index = j;
				}
			}
			distributedNode.add(index);
		}
	}
	public int distributeIncomNode(Incomplete node){
		double max = Double.NEGATIVE_INFINITY;
		int index = 0;
		for(int j:exemplar){
			double tmp = Tools.compareSimilarity(completeData.get(j), node);
			if(max < tmp){
				max = tmp;
				index = j;
			}
		}
		return index;
	}
	public void printDistributeResult(){
		int num = distributedNode.size();
		//result output
		for(int i=0; i<num; i++){
			System.out.println(i+"->"+distributedNode.get(i));
		}		
	}
	public void incrementalClustering(){
		if(batch==0){
			clustering();
			return;
		}
		while(incompleteData.size()>0){
			for(int i=0;i<batch&&incompleteData.size()>0;i++){
				Incomplete node = incompleteData.getFirst();
//				System.out.println(node);
				int exem = distributeIncomNode(node);
				NNFill stack = new NNFill();
				for(int j=0;j<distributedNode.size();j++){
					if(distributedNode.get(j).intValue()==exem){
						stack.add(j, Tools.compareSimilarity(completeData.get(j), node), completeData.get(j));
					}
				}
//				stack.showbox();
				int nn = stack.fill(node);
				updateResponsibility(nn);
				updateAvailability(nn);
				apnode.transfer();
//				showMatrix();
//				apnode.showData();
			}
			clustering();
		}
	}
	public void updateResponsibility(int n){
		int num = completeData.size();
		ArrayList<Double> s = new ArrayList<>();
		double tmp;
		for(int i=0; i<num; i++){
			tmp = responsibility.get(i).get(n);
			responsibility.get(i).add(tmp);
			tmp = responsibility.get(n).get(i);
			s.add(tmp);
		}
		tmp = responsibility.get(n).get(n);
		s.add(tmp);
		responsibility.add(s);
	}
	public void updateAvailability(int n){
		int num = completeData.size();
		ArrayList<Double> s = new ArrayList<>();
		double tmp;
		for(int i=0; i<num; i++){
			tmp = availability.get(i).get(n);
			availability.get(i).add(tmp);
			tmp = availability.get(n).get(i);
			s.add(tmp);
		}
		tmp = availability.get(n).get(n);
		s.add(tmp);
		availability.add(s);
	}
	public void showMatrix(){
		System.out.println("similarity:");
		for(ArrayList<Double> m:similarity){
			System.out.println(m);
		}
		System.out.println("responsibility:");
		for(ArrayList<Double> m:responsibility){
			System.out.println(m);
		}
		System.out.println("availability:");
		for(ArrayList<Double> m:availability){
			System.out.println(m);
		}
	}
	public static void main(String[] args){
		APNode testnode = new APNode();
		testnode.dataInputFromFile("./data/winedata",",");
		testnode.showData();
		testnode.calculateSimlarity();
//		for(ArrayList<Double> m:testnode.similarity){
//			System.out.println(m);
//		}
		APCluster test = new APCluster(testnode,0.5,50,500,5);
		test.clustering();
//		test.showMatrix();
//		test.printDistributeResult();
		test.incrementalClustering();
		testnode.showData();
//		test.showMatrix();
//		test.printDistributeResult();
	}
}
