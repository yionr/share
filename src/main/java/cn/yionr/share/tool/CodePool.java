package cn.yionr.share.tool;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
public class CodePool {
    public  static int[] codePool = new int[10000];
    //checkcode 1为被占用 0为可用 --状态数组
    public  static int[] checkcode = new int[10000];

    static {
        for (int i = 0; i < codePool.length; i++) {
            codePool[i] = i;
        }
    }
    public List<int[]> getCodePool(){
       return  Arrays.asList(codePool);

    }
    public synchronized void returnCode(String code){
        //归还code
        statchange(Integer.parseInt(code));
    }
    public synchronized String getCode() {

        return addZero(check());
    }

   //返回一个可用code
    private int check() {
        ArrayList<Integer> s1 = new ArrayList<>();
       // 。。。。优化与扩容相关
        for (int i : checkcode) {
            if (i != 1) {
                s1.add(i);
            }
        }
        int code=(int) (Math.random() * (s1.size() + 1));
        //改变状态
        //应该不需要事务
        statchange(code);
        return codePool[code];

    }
        public void statchange(int code){
        if(checkcode[code]!=0){
            checkcode[code]=0;
        }else if(checkcode[code]!=1){
            checkcode[code]=1;
        }

        }

        //获取code时候将数字格式转化为00000
        public  String addZero(int num) {
                if (num < 10)
                    return ("000" + num);
                else if (num < 100)
                    return ("00" + num);
                else if (num < 1000)
                    return ("0" + num);
                else
                    return  ("" + num);
        }

        //获取数据文件code去掉有效数字前的0，得到对应数组的数字
        public int trimZero(String num) {
            return Integer.parseInt(num);
        }
        //扩容。。。。。
}
