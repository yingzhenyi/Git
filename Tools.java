package APCluster;

import java.util.ArrayList;

import APCluster.APNode.Complete;
import APCluster.APNode.Incomplete;


public class Tools {
	static class MaxMin{
		double max;
		double min;
		public MaxMin(double max, double min) {
			super();
			this.max = max;
			this.min = min;
		}
	}
	static ArrayList<MaxMin> maxmin = new ArrayList<>();
	public static double compareNumSimilarity(ArrayList<Double> n1,ArrayList<Double> n2){
		if((n1==null||n1.isEmpty())&&(n2==null||n2.isEmpty()))return 0;
		double res = 0;
		int num = n1.size();
		for(int i=0; i<num; i++){
			if(n1.get(i).isNaN()||n2.get(i).isNaN())continue;
			res += Math.pow(normalize(n1.get(i),maxmin.get(i).max,maxmin.get(i).max)-normalize(n2.get(i),maxmin.get(i).max,maxmin.get(i).max), 2);
		}
		return -Math.sqrt(res);
	}
	public static double compareClaSimilarity(ArrayList<String> n1,ArrayList<String> n2){
		if((n1==null||n1.isEmpty())&&(n2==null||n2.isEmpty()))return 0;
		double res = 0;
		int num = n1.size();
		for(int i=0; i<num; i++){
			if(n1.get(i).equals("?")||n2.get(i).equals("?"))continue;
			if(n1.get(i).equals(n2.get(i))){
				res += 1;
			}
		}
		return (res+1)/(num+1)-1;
	}
	public static double compareSimilarity(Complete n1, Complete n2){
		return compareNumSimilarity(n1.numData,n2.numData)+compareClaSimilarity(n1.claData,n2.claData);
	}
	public static double compareSimilarity(Complete n1, Incomplete n2){
		return compareNumSimilarity(n1.numData,n2.numData)+compareClaSimilarity(n1.claData,n2.claData);
	}
	public static double normalize(double c, double max, double min){
		return (c-min+1)/(max-min+1);
	}
	public static void getMaxMin(APNode node){
		int num = node.NN; 
		for(int i = 0; i<num; i++){
			double max,min;
			max = Double.NEGATIVE_INFINITY;
			min = Double.POSITIVE_INFINITY;
			for(int j=0; j<node.completeData.size(); j++){
				double tmp = node.completeData.get(j).numData.get(i);
				max = Math.max(max, tmp);
				min = Math.min(min, tmp);
			}
			maxmin.add(new MaxMin(max,min));
		}
	}
}
