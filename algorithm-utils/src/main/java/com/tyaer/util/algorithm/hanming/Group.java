package com.tyaer.util.algorithm.hanming;

import java.io.Serializable;
import java.util.List;


public class Group implements Comparable<Group> , Serializable{

	private List<String> eles;
	
	public Group(List<String> eles){
		this.eles=eles;
	}

	public int compareTo(Group o) {
		
		return (o.eles.size()-eles.size());
	}

	public List<String> getEles() {
		return eles;
	}

	public void setEles(List<String> eles) {
		this.eles = eles;
	}
	
	
	
	
}
