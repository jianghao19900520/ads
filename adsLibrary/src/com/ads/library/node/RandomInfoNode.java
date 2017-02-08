package com.ads.library.node;

import java.io.Serializable;
import java.util.ArrayList;

public class RandomInfoNode implements Serializable{

    public ArrayList<RandomLoopNode> adsLoop;

    public ArrayList<RandomLoopNode> getAdsLoop() {
        return adsLoop;
    }

    public void setAdsLoop(ArrayList<RandomLoopNode> adsLoop) {
        this.adsLoop = adsLoop;
    }
}
