package com.ads.library.node;

import java.io.Serializable;

/**
 * Created by jason on 2016/9/14.
 */
public class ShakeInfoNode implements Serializable{

    public int prize_id;
    public int face_value;
    public String auth_code;
    public int from_type;
    public String message;
	public int getPrize_id()
	{
		return prize_id;
	}
	public void setPrize_id(int prize_id)
	{
		this.prize_id = prize_id;
	}
	public int getFace_value()
	{
		return face_value;
	}
	public void setFace_value(int face_value)
	{
		this.face_value = face_value;
	}
	public String getAuth_code()
	{
		return auth_code;
	}
	public void setAuth_code(String auth_code)
	{
		this.auth_code = auth_code;
	}
	public int getFrom_type()
	{
		return from_type;
	}
	public void setFrom_type(int from_type)
	{
		this.from_type = from_type;
	}
	public String getMessage()
	{
		return message;
	}
	public void setMessage(String message)
	{
		this.message = message;
	}
    
    

}
