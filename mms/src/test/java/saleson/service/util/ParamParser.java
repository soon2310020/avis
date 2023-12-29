package saleson.service.util;


import saleson.model.Transfer;
import unused.util.AESUtils;

import java.net.URLDecoder;
import java.util.ArrayList;

public class ParamParser {

	private String at = "";
	private ArrayList paramList = new ArrayList();
	
	public void init(String param ) throws Exception{
		Transfer dto;
		String plaintext="";
		String [] q = param.split("&q=");
		
		for (int i=0,size=q.length;i<size;i++){
			q[i] =  AESUtils.decrypt(q[i]);
			plaintext=q[i];
			String [] pairs = q[i].split("&");
			String name="";
			String value="";
			dto = new Transfer();
			for(int j=0;j<pairs.length;j++){
				int idx = pairs[j].indexOf("=");
				name = URLDecoder.decode(pairs[j].substring(0,idx),"UTF-8");
				value = URLDecoder.decode(pairs[j].substring(idx+1),"UTF-8");
				
//				System.out.println("■■■■■■■■■■■■■■■■■■■■■■for j["+j+"]");
//				System.out.println("■■■■■■■■■■■■■■■■■■■■■■for name["+name+"]");
//				System.out.println("■■■■■■■■■■■■■■■■■■■■■■for value["+value+"]");
				
				if("at".equals(name)){
					at = value;
					dto.setAt(value);
				}else if("ti".equals(name)){
					dto.setTi(value);
				}else if("rc".equals(name)){
					dto.setRc(Integer.parseInt(value));
				}else if("tv".equals(name)){
					dto.setTv(value);
				}else if("dh".equals(name)){
					dto.setDh(value);
				}else if("ip".equals(name)){
					dto.setIp(value);
				}else if("gw".equals(name)){
					dto.setGw(value);
				}else if("dn".equals(name)){
					dto.setDn(value);
				}else if("ci".equals(name)){
					dto.setCi(value);
				}else if("sc".equals(name)){
					dto.setSc(Integer.parseInt(value));
				}else if("lst".equals(name)){
					dto.setLst(value);
				}else if("rt".equals(name)){
					dto.setRt(value);
				}else if("cf".equals(name)){
					dto.setCf(value);
				}else if("ct".equals(name)){
					dto.setCt(Double.parseDouble(value));
				}else if("bs".equals(name)){
					dto.setBs(value);
				}else if("sn".equals(name)){
					
//					System.out.println("■■■■■■■■■■■■■■■■■■■■■■sn");
//					System.out.println("value["+value+"]");
//					System.out.println("■■■■■■■■■■■■■■■■■■■■■■sn");
					
					dto.setSn(Integer.parseInt(value));
				}else{
					
				}
				
			}
			dto.setEs(param);
			dto.setDs(plaintext);
			paramList.add(dto);
		}
	}
	
	
	public ArrayList getParam(){
		return paramList;
	}
	
	public String getAt(){
		return at;
	}
	public boolean isEmpty(){
		return paramList.isEmpty();
	}
	
	public int size(){
		return paramList.size();
	}

}
