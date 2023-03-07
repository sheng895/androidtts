package com.gykj.paddle.lite.demo.tts;

import java.io.BufferedReader;

import java.io.File;

import java.io.FileOutputStream;

import java.io.IOException;

import java.io.InputStream;

import java.io.InputStreamReader;

import java.io.OutputStream;

import java.util.ArrayList;

import java.util.List;

import android.content.Context;

import android.content.res.AssetManager;

import android.util.Log;

public class AssetCopyer {

    private static String TAG="AssetCopyer";

    /**

     * copy all the files and folders to the destination

     * @Param context  application context

     * @param destination the destination path

     */

    public  static void copyAllAssets(Context context,String destination)

    {

        copyAssetsToDst(context,"dict",destination);

    }

    /**

     *

     * @param context :application context

     * @param srcPath :the path of source file

     * @param dstPath :the path of destination

     */

    private  static void copyAssetsToDst(Context context,String srcPath,String dstPath) {

        try {

            String fileNames[] =context.getAssets().list(srcPath);

            if (fileNames.length > 0)

            {

                File file = new File(dstPath);

                file.mkdirs();

                for (String fileName : fileNames)

                {

                    if(srcPath!="")

                    {

                        copyAssetsToDst(context,srcPath + "/" + fileName,dstPath+"/"+fileName);

                    }else{

                        copyAssetsToDst(context, fileName,dstPath+"/"+fileName);

                    }

                }

            }else

            {

                InputStream is = context.getAssets().open(srcPath);

                FileOutputStream fos = new FileOutputStream(new File(dstPath));

                byte[] buffer = new byte[1024];

                int byteCount=0;

                while((byteCount=is.read(buffer))!=-1) {

                    fos.write(buffer, 0, byteCount);

                }

                fos.flush();//刷新缓冲区

                is.close();

                fos.close();

            }

        } catch (Exception e) {

            // TODO Auto-generated catch block

            e.printStackTrace();

        }

    }

}
