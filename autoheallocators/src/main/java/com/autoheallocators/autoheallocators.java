package com.autoheallocators;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.OutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.time.Duration;
import java.util.List;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.pagefactory.ByChained;
import org.apache.commons.io.IOUtils;
import com.google.common.*;
import org.dhatim.fastexcel.Workbook;
import org.json.*;
import org.dhatim.fastexcel.Worksheet;
public class autoheallocators {

private static WebDriver driver;
	private static JSONObject json;
	
	// Parse JSON Config 
	// Create WebDriver Instance
	public autoheallocators() throws IOException {
		driver = new ChromeDriver();
		driver.get("https://www.leafground.com/waits.xhtml");
		driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
		driver.manage().window().maximize();
		String filePath = Paths.get("").toAbsolutePath().toString()+"\\config\\config.json";
		Path file  = Path.of(filePath);
		String str = Files.readString(file);
        json = new JSONObject(str);
	}
	
	// Method to check whether the element is present or not 
	public static WebElement checkElementPresent(String element, String parent) {
		WebElement found = null;
		By locatorElement= null;
		String locator  = json.getJSONObject(parent).getJSONObject(element).getString("locator");
		String locatorType  = json.getJSONObject(parent).getJSONObject(element).getString("locatorType");
		System.out.println(locator+' '+locatorType);
		try {
			if (locatorType.equals("id")) {
					locatorElement = By.id(locator);
				}
			if (locatorType.equals("name")) {
					locatorElement = By.name(locator);
				}
			WebElement lookup = driver.findElement(locatorElement);
			if (lookup != null)
				found = lookup;
		}
		catch(Exception e) {
			found = null;
			System.out.println("Exception happened while trying to lookup the element");
			e.printStackTrace();
		}
		return found;
	}
	
	
	public static WebElement getParentElement(String text) {
		WebElement parent = null;
		List<WebElement> list = driver.findElements(By.xpath("//*[contains(text(),'"+text+"')]//parent::div"));
		if (list.size() > 1) {
			System.out.println("More than 1 parent element found for "+ text);
		}
		if (list.size() >= 1)
			parent = list.get(0);
		return parent;
	}
	
	// Wrapper to the Find Element method with Self Heal functionality
	public static WebElement findElement(String element, String parent) {
		WebElement ret = checkElementPresent(element, parent);
		if (ret != null)
			return ret;
		else {
			//System.out.println(json.getJSONObject(parent).get(element));
			WebElement parentElement = getParentElement(json.getJSONObject(parent).getJSONObject(element).getString("parent"));
			System.out.println("parent element found "+parent);
			WebElement rem = driver.findElement(By.xpath("//*[contains(text(), '"+parent+"')]//parent::*//child::*"));
			System.out.println(rem+" ");
			List<WebElement> elements = driver.findElements(By.xpath("//*[contains(text(), '"+parent+"')]//parent::*//child::*"));
			for (WebElement ele : elements) {
				//System.out.println(ele.getText()+" "+ele.getTagName()+" "+ele.getDomAttribute("id"));
				if (ele.getText().contains(json.getJSONObject(parent).getJSONObject(element).getString("text")) && ele.getDomProperty("id") != null) {
					System.out.println("Alternative to the Locator is found "+ele);
					ret = ele;
				}
			}
		}
		return ret;
	}
	
	// Method to check read data from Excel if already added
	public static void readExcel(String excelPath) {
		
	}
	
	// Method to write data to Excel 
	public static void writeExcel(String data) {
		File filepath = new File(".");
		String path = filepath.getAbsolutePath();
		String fileLocation = path.substring(0, path.length()-1)+"excel.xlsx";
		System.out.println(filepath);
		System.out.println(path);
		System.out.println(fileLocation);
		try(FileOutputStream os = new FileOutputStream(new File(fileLocation)); Workbook wb = new Workbook(os, "MyApplication", "1.0")){
			Worksheet ws = wb.newWorksheet("Sheet 1");
			ws.range(0, 0, 0, 1).style().fontName("Arial").fontSize(16).bold().fillColor("2266FF").set();
		    ws.value(0, 0, "Name");
		    ws.value(0, 1, "Age");
		    
		    ws.range(2, 0, 2, 1).style().wrapText(true).set();
	        ws.value(2, 0, "John Smith");
	        ws.value(2, 1, 20L);
	        
	        ws.value(3, 4, "thisnals");
	        ws.value(3, 3, "asdsa");
		}
		catch(Exception e) {
			System.out.println("Exception happened");
		}
	}
	
	public static void main(String[] args) throws Exception {
		new autoheallocators();
		System.out.println("=====================================================================================");
		System.out.println("                           Auto Heal locators                                        ");
		System.out.println("=====================================================================================");
		WebElement searchElement = findElement("click", "Visibility");
		System.out.println("searched element"+ searchElement);
		driver.quit();
		System.out.println("=====================================================================================");
		System.out.println("                           Auto Heal locators                                        ");
		System.out.println("=====================================================================================");
	}
}
