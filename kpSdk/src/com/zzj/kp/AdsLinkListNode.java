package com.zzj.kp;

import java.io.Serializable;
import java.util.List;

public class AdsLinkListNode implements Serializable {

    public int priority;
    public List<AdsLinkNode> ad58;
    public List<AdsLinkNode> adbd;
    
	public int getPriority()
	{
		return priority;
	}
	public void setPriority(int priority)
	{
		this.priority = priority;
	}
	public List<AdsLinkNode> getAd58()
	{
		return ad58;
	}
	public void setAd58(List<AdsLinkNode> ad58)
	{
		this.ad58 = ad58;
	}
	public List<AdsLinkNode> getAdbd()
	{
		return adbd;
	}
	public void setAdbd(List<AdsLinkNode> adbd)
	{
		this.adbd = adbd;
	}
    
    

}
