package idiots.com.dsp;

import android.support.v4.util.CircularArray;

import com.jjoe64.graphview.series.DataPoint;

/**
 * Created by KimHJ on 2018-04-27.
 */

public class CircularQueue {

    private DataPoint[] q;
    private int maxSize;
    private int front;
    private int size;

    public CircularQueue(int maxSize){
        this.maxSize = maxSize;
        this.q = new DataPoint[this.maxSize];
        this.front = 0;
        this.size = 0;
    }

    public DataPoint[] getArray(){
        if(this.size < this.maxSize){
            DataPoint[] temp = new DataPoint[this.size];
            for(int i = 0; i < this.size; i++){
                temp[i] = q[i];
            }
            return temp;
        }else{
            return this.q;
        }
    }

    public boolean add(DataPoint d){
        q[this.front] = d;
        this.front = (this.front + 1)%this.maxSize;
        if(this.size < this.maxSize){
            this.size++;
        }
        return true;
    }
}
