package com.banlap.fontconversion;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Bundle;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;


import com.banlap.fontconversion.databinding.ActivityMainBinding;

import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    private boolean[][] arr;
    private int all_16_32 = 16;
    private int all_2_4 = 2;
    private int all_32_128 = 32;
    private int font_width = 8;   //ascii码 8*16
    private int font_height = 16;  //ascii码 8*16
    private int all_16 = 16;//ascii码解析成8*16 所占字节数


    private String mShowFont="";
    private String mShowHex="";

    /**
     * 生成点阵字体
     * */
    public boolean[][] drawString(ActivityMainBinding binding, String str) {
        byte[] data = null;
        int[] code = null;
        int byteCount;
        int lCount;
        for (int i = 0; i < str.length(); i++) {
            if (str.charAt(i) < 0x80) {
                //continue;
                // 字母显示
                arr = new boolean[font_height][font_width];
                data = read_a(str.charAt(i));
                byteCount = 0;
                for (int line = 0; line < 16; line++) {
                    String insertStr ="";   //内容显示
                    String insertHexStr="";  //内容转换hex
                    lCount = 0;
                    for (int k = 0; k < 1; k++) {
                        for (int j = 0; j < 8; j++) {
                            if (((data[byteCount] >> (7 - j)) & 0x1) == 1) {
                                arr[line][lCount] = true;
                                //判断是否单个字符
                                if(i>0) {
                                    insertStr = insertStr.concat("■");
                                } else {
                                    mShowFont = mShowFont.concat("■");
                                }
                                insertHexStr = insertHexStr.concat("1");
                            } else {
                                //判断是否单个字符
                                if(i>0) {
                                    insertStr = insertStr.concat("□");
                                } else {
                                    mShowFont = mShowFont.concat("□");
                                }
                                insertHexStr = insertHexStr.concat("0");
                                arr[line][lCount] = false;
                            }
                            lCount++;
                        }
                        byteCount++;
                    }

                    if(i>0) {
                        insertStr(mShowFont, insertStr, line);
                    } else {
                        mShowFont = mShowFont.concat("\n");
                    }
                    //Log.e("Hex:", "0x"+Integer.toHexString(Integer.parseInt(insertHexStr, 2)) );
                    //文本转换为16进制hex
                    int hexInt = Integer.toHexString(Integer.parseInt(insertHexStr, 2)).length();
                    String hex = (hexInt>1)? "0x"+Integer.toHexString(Integer.parseInt(insertHexStr, 2))
                            : "0x0"+Integer.toHexString(Integer.parseInt(insertHexStr, 2));
                    mShowHex = mShowHex.concat(hex);
                    mShowHex = mShowHex.concat(",");
                }


            } else {
                // 汉字显示
                arr = new boolean[all_16_32][all_16_32];
                code = getByteCode(str.substring(i, i + 1));
                data = read(code[0], code[1]);
                byteCount = 0;
                for (int line = 0; line < all_16_32; line++) {
                    String insertStr ="";   //内容显示
                    lCount = 0;
                    for (int k = 0; k < all_2_4; k++) {
                        String insertHexStr=""; //内容转换hex
                        for (int j = 0; j < 8; j++) {
                            if (((data[byteCount] >> (7 - j)) & 0x1) == 1) {
                                arr[line][lCount] = true;
                                //System.out.print("*");
                                //判断是否单个字符
                                if(i>0) {
                                    insertStr = insertStr.concat("■");
                                } else {
                                    mShowFont = mShowFont.concat("■");
                                }
                                insertHexStr = insertHexStr.concat("1");

                            } else {
                                //System.out.print(" ");
                                //判断是否单个字符
                                if(i>0) {
                                    insertStr = insertStr.concat("□");
                                } else {
                                    mShowFont = mShowFont.concat("□");
                                }
                                insertHexStr = insertHexStr.concat("0");
                                arr[line][lCount] = false;
                            }
                            lCount++;
                        }
                        byteCount++;
                        //文本转换为16进制hex
                        int hexInt = Integer.toHexString(Integer.parseInt(insertHexStr, 2)).length();
                        String hex = (hexInt>1)? "0x"+Integer.toHexString(Integer.parseInt(insertHexStr, 2))
                                : "0x0"+Integer.toHexString(Integer.parseInt(insertHexStr, 2));
                        mShowHex = mShowHex.concat(hex);
                        mShowHex = mShowHex.concat(",");
                    }
                    //System.out.println();
                    if(i>0) {
                        insertStr(mShowFont, insertStr, line);
                    } else {
                        mShowFont = mShowFont.concat("\n");
                    }

                }
            }
            mShowHex = mShowHex.concat("  -- " + str.charAt(i) +"\n\n");
        }
        //显示文本对应的hex值
        binding.etShowHex.setText(mShowHex);
        return arr;
    }

    /**
     * 横向显示内容
     * */
    private void insertStr(String str, String character, int count) {
         Matcher newMatcher = Pattern.compile("\n").matcher(str);
         int mIdx = 0;
         while(newMatcher.find()) {
             if(mIdx == count){
                 break;
             }
             mIdx++;
         }
         int location = newMatcher.start();
         StringBuffer sb = new StringBuffer(mShowFont);
         sb.insert(location, character);
         mShowFont = sb.toString();
    }

    /**
     * 读取字库中的汉字
     */
    protected byte[] read_a(char char_num) {
        byte[] data = null;
        int ascii = (int) char_num;
        try {
            data = new byte[all_16];//定义缓存区的大小
            InputStream inputStream = getResources().openRawResource(com.banlap.fontconversion.R.raw.asc16);
            int offset = ascii * 16;//ascii码在字库里的偏移量
            inputStream.skip(offset);
            inputStream.read(data, 0, all_16);//读取字库中ascii码点阵数据
            inputStream.close();
            return data;
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return data;
    }

    /**
     * 读取字库中的ASCII 码
     */
    protected byte[] read(int areaCode, int posCode) {
        byte[] data = null;
        try {
            int area = areaCode - 0xa0;
            int pos = posCode - 0xa0;
            InputStream in = getResources().openRawResource(com.banlap.fontconversion.R.raw.hzk16s);
            long offset = all_32_128 * ((area - 1) * 94 + pos - 1);
            in.skip(offset);
            data = new byte[all_32_128];
            in.read(data, 0, all_32_128);
            in.close();
        } catch (Exception ex) {
            System.err.println("SORRY,THE FILE CAN'T BE READ");
        }
        return data;

    }
    /**
     * 获取汉字的区，位（ascii码不需要区码，位码）
     * @param str
     * @return
     */
    protected int[] getByteCode(String str) {
        int[] byteCode = new int[2];
        try {
            byte[] data = str.getBytes("GB2312");
            byteCode[0] = data[0] < 0 ? 256 + data[0] : data[0];
            byteCode[1] = data[1] < 0 ? 256 + data[1] : data[1];
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return byteCode;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.btCreate.setOnClickListener(v->{
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            // 隐藏软键盘
            imm.hideSoftInputFromWindow(getWindow().getDecorView().getWindowToken(), 0);
            // 清空内容
            mShowFont="";
            mShowHex="";
            // 显示内容
            drawString(binding, binding.etInputValue.getText().toString());
            binding.tvShowValue.setText(mShowFont);
        });

        binding.ivDelete.setOnClickListener(v-> binding.etInputValue.setText(""));

    }

}