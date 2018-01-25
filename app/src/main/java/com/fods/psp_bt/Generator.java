package com.fods.psp_bt;

import java.util.Random;

/**
 * Created by User on 2017-12-17.
 */

public class Generator {

    public static int[] generateRandom(int array_size){
        int[] sk = new int[array_size];
        Random rand = new Random();
        for(int i = 0;i < sk.length;i++){
            //sk[i] = rand.nextInt(65535);
            //sk[i] = rand.nextInt(35535);
            sk[i] = RandomInteger(35535,65535, rand);
        }
        int pick = 35535;
        for(int i = 0;i < sk.length/265;i++){
            if( i > (sk.length/265)/2 ) {
                //pick = pick - (i*2);
            }else{
                pick = pick - (i*2);
            }
            sk[array_size/2+i] = pick;
        }
        return sk;
    }

    private static int RandomInteger(int aStart, int aEnd, Random aRandom){
        if (aStart > aEnd) {
            throw new IllegalArgumentException("Start cannot exceed End.");
        }
        //get the range, casting to long to avoid overflow problems
        long range = (long)aEnd - (long)aStart + 1;
        // compute a fraction of the range, 0 <= frac < range
        long fraction = (long)(range * aRandom.nextDouble());
        return (int)(fraction + aStart);
    }

    public static int[] generateStrait(int array_size){
        int[] sk = new int[array_size];
        for(int i = 0;i<array_size;i++){
            if(i >= 65535){
                sk[i] = 65535;
            }
            else{
                sk[i] = i;
            }
        }
        return sk;
    }

    public static int[] generateLog(int array_size){
        int[] sk = new int[100000];
        int check = 0;
        for(int i = 0;i<array_size;i++){
            if(i != 0){
                check = 2/i;
                if(check >= 65535){
                    sk[i] = 65535;
                }
                else {
                    sk[i] = 2/i;
                }
            }
            else{
                sk[i] = 0;
            }
        }
        return sk;
    }

    public static int[] generateSin(int array_size){
        int[] sk = new int[array_size];
        int check = 0;
        for(int i = 0;i<array_size;i++){
            check = (int) ((Math.sin(i) + 2) * 2);
            if(check < 0){
                sk[i] = 0;
            }
            else{
                if(check >= 65535){
                    sk[i] = 65535;
                }
                else{
                    sk[i] = check;
                }
            }
        }
        return sk;
    }

}
