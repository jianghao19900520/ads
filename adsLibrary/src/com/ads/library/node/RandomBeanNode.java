package com.ads.library.node;

import java.io.Serializable;

public class RandomBeanNode implements Serializable{

    public int add;
    public int total;
    public String remark;
	public int getAdd()
	{
		return add;
	}
	public void setAdd(int add)
	{
		this.add = add;
	}
	public int getTotal()
	{
		return total;
	}
	public void setTotal(int total)
	{
		this.total = total;
	}
	public String getRemark()
	{
		return remark;
	}
	public void setRemark(String remark)
	{
		this.remark = remark;
	}
    
    
    
}
