package APCluster;

import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;

import APCluster.APNode.Complete;
import APCluster.APNode.Incomplete;

public class NNFill {
	class Neighbor{
		int location;
		double similarity;
		Complete data;
		public Neighbor(int location, double similarity, Complete data) {
			super();
			this.location = location;
			this.similarity = similarity;
			this.data = data;
		}
		@Override
		public String toString() {
			return "Neighbor [location=" + location + ", similarity=" + similarity + ", data=" + data + "]";
		}
	}
	LinkedList<Neighbor> box = new LinkedList<>();
	public void sort(){
		box.sort(new Comparator<Neighbor>(){
				public int compare(Neighbor o1, Neighbor o2) {
					if(o1.similarity>=o2.similarity){
						return 1;
					}else{
						return -1;
					}
				}
			}
		);
	}
	public boolean add(int location, double similarity, Complete data){
		return box.add(new Neighbor(location,similarity,data));
	}
	public void showbox(){
		for(Neighbor n: box){
			System.out.println(n);
		}
	}
	public void fillNum(Incomplete node){
		if(node.numData==null)return;
		for(int i=0; i<node.numData.size(); i++){
			if(node.numData.get(i).isNaN()){
				int count = 0;
				double oldvalue = 0.0;
				double value = 0.0;
				double sum = 0.0;
				for(Neighbor tmp:box){
					if(count==2)break;
					double s = Math.pow((-1/(tmp.similarity-0.1)), 2);
//					System.out.println("s:"+s);
					sum += s;
					value = value*(1-s/sum)+tmp.data.numData.get(i)*(s/sum);
					if(Math.abs((oldvalue-value)/value)<0.12){
						count++;
					}else{
						count=0;
					}
//					System.out.println(value);
					oldvalue = value;
				}
				node.numData.set(i, value);
			}
		}
	}
	public void fillCla(Incomplete node){
		if(node.claData==null)return;
		for(int i=0; i<node.claData.size(); i++){
			if(node.claData.get(i).equals("?")){
				int count = 0;
				String value = "";
				HashMap<String,Double> buffer = new HashMap<>(); 
				for(Neighbor tmp:box){
					if(count==2)break;
					String s = tmp.data.claData.get(i);
					if(buffer.containsKey(s)){
						buffer.put(s, buffer.get(s)+tmp.similarity);
					}else{
						buffer.put(s, tmp.similarity);
					}
					for(String g:buffer.keySet()){
						if(!g.equals(s)){
							if(buffer.get(g)>buffer.get(s)){
								s = g;
							}
						}
					}
					if(s.equals(value)){
						count++;
					}else{
						count=0;
					}
					value = s;
				}
				node.claData.set(i, value);
			}
		}
	}
	public int fill(Incomplete node){
		fillNum(node);
		fillCla(node);
		node.miss = 0;
//		System.out.println(node);
		return box.peekFirst().location;
	}
}
