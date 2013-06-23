package util;

import java.io.FileInputStream;
import java.io.InputStream;
import jxl.Sheet;
import jxl.Workbook;

/**
 *读取excel表的方法
 *使用步骤：
 *1.建立对象实例
 *2.打开文件
 *3.调用getvalue(int x,int y)
 *4.关闭文件closefile()
 */
public class ExcelReader {
	public jxl.Workbook ework;
	Sheet esheet;
	public ExcelReader(){
		
	}
	public boolean openExcelFile(String filename){
		try{
			InputStream efileStream = new FileInputStream(filename);
			ework = Workbook.getWorkbook(efileStream);
			esheet = ework.getSheet(0);
			return true;
		}catch(Exception ex){
			ex.printStackTrace();
			return false;
		}
	}
	public String getvalue(int x,int y){
		return esheet.getCell(x, y).getContents();
	}
	public void closefile(){
		ework.close();
	}
	public int  getAmountRows(){
		return esheet.getRows();
	}
}
