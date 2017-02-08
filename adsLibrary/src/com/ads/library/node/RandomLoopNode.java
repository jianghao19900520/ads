package com.ads.library.node;

import java.io.Serializable;

/**
 * Created by jason on 2016/9/13.
 */
public class RandomLoopNode implements Serializable {

    public String amount;
    public String position_id;
    
	public String getAmount()
	{
		return amount;
	}
	public void setAmount(String amount)
	{
		this.amount = amount;
	}
	public String getPosition_id()
	{
		return position_id;
	}
	public void setPosition_id(String position_id)
	{
		this.position_id = position_id;
	}

    
    
}