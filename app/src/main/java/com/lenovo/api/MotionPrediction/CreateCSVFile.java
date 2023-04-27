package com.lenovo.api.MotionPrediction;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class CreateCSVFile {
//    public String storePath = "/data/data/com.lenovo.lrpenpredictiondemo/shared_prefs";
//    public SimpleDateFormat dateFormat = new SimpleDateFormat("MMdd");
//    public String fileName = "DaveStroke_" + dateFormat.format(new Date());


    /**
     * CSV 文件生成方法 (Create a new or rewrite a existed file);
     * @param head Head line for file, 文件头;
     * @param dataList Restore data in body paragraph, 数据列表;
     * @param outPutPath Path for output, 文件输出路径, (推荐设置成 "/data/data/com.lenovo.styluspen/databases");
     * @param filename File name, 文件名;
     * @param appendFlag Append data at the end or rewrite at the beginning, 是否追加数据, (① true: 不创建表头; ② false: 覆盖写入表头 + 正文);
     */
    public static void createCSVFile(List<Object> head,
                                     List<List<Object>> dataList,
                                     String outPutPath,
                                     String filename,
                                     boolean appendFlag) {

        File csvFile;
        BufferedWriter csvWriter = null;
        try {
            csvFile = new File(outPutPath + File.separator + filename + ".csv");
            File parent = csvFile.getParentFile();
            if (parent != null && !parent.exists()) {
                parent.mkdirs();
            }
            csvFile.createNewFile();

            // Use "," as correct UTF-8 code; Dave: default "GBK" - 2312;
            FileWriter writer = new FileWriter(csvFile, appendFlag);
            csvWriter = new BufferedWriter(writer, 1024);

            if (!appendFlag) {
                writeRow(head, csvWriter);              // write head file;
                for (List<Object> row : dataList) {     // write file body content;
                    writeRow(row, csvWriter);
                }
            } else {
                for (List<Object> row : dataList) {
                    writeRow(row, csvWriter);
                }
            }
            csvWriter.flush();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                csvWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Write data in one row;
     * @param row Data in sequence;
     * @param csvWriter Write sequence by row in CSV file;
     * @throws IOException Throw exception;
     */
    public static void writeRow(List<Object> row, BufferedWriter csvWriter) throws IOException {
        for (Object data : row) {
            StringBuffer strBuf = new StringBuffer();
            String rowStr = strBuf.append("\"").append(data).append("\",").toString();
            csvWriter.write(rowStr);
        }
        csvWriter.newLine();
    }


}
