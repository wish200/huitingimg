package com.huiting.servlet;
 
import java.io.File; 
import java.io.IOException; 
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.jaudiotagger.audio.mp3.MP3AudioHeader;
import org.jaudiotagger.audio.mp3.MP3File;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.huiting.cache.SystemCache;
import com.huiting.util.NarrowImage;

public class UploadServlet extends HttpServlet{

	public UploadServlet() {
		super();
	}

	/**
	 * Destruction of the servlet. <br>
	 */
	public void destroy() {
		super.destroy(); // Just puts "destroy" string in log
		// Put your code here
	}

	/**
	 * The doGet method of the servlet. <br>
	 *
	 * This method is called when a form has its tag value method equals to get.
	 * 
	 * @param request the request send by the client to the server
	 * @param response the response send by the server to the client
	 * @throws ServletException if an error occurred
	 * @throws IOException if an error occurred
	 */
	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		 
        doPost(request,response);
		 
	}

	/**
	 * The doPost method of the servlet. <br>
	 *
	 * This method is called when a form has its tag value method equals to post.
	 * 
	 * @param request the request send by the client to the server
	 * @param response the response send by the server to the client
	 * @throws ServletException if an error occurred
	 * @throws IOException if an error occurred
	 */
	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String resultjsonString = "";
		 boolean isMultipart = ServletFileUpload.isMultipartContent(request);  
		 Map<String, String> paramMap = new HashMap<String, String>();
		 String filePath1 ="";
		 String filePath ="";
		 File file = null;
		 SimpleDateFormat sdf  = new SimpleDateFormat("yyyyMM");
		 String yearmonth =  sdf.format(new java.util.Date());
		 String fileName ="";
		 String fileFieldName = "";
		 String isorigin="";
		 String type="";
		 
		 String result="";
		 String duration="";
			
		 String httphead = "Encoding:"+request.getCharacterEncoding()+";ContentType:"+request.getContentType()+";ReqAddr:"+request.getRemoteAddr()+";ReqPort:"+request.getRemotePort()+";ReqURI:"+request.getRequestURI();
	       if (isMultipart) {    
	           FileItemFactory factory = new DiskFileItemFactory();    
	           ServletFileUpload upload = new ServletFileUpload(factory);   
	           
	           try {    
	               List items = upload.parseRequest(request);    
	               Iterator iter = items.iterator();    
	               while (iter.hasNext()) {    
	                   FileItem item = (FileItem) iter.next();
	                   System.out.println("itemname="+item.getFieldName());
	                   if (item.isFormField()) {    
	                       //普通文本信息处理    
	                       String paramName = item.getFieldName();  
	                       String paramValue = item.getString("GBK");  
	                       paramMap.put(paramName, paramValue);
	                       System.out.println(paramName + ":" + paramValue+"--:"+item.getString("utf-8"));    
	                   } else {    
	                       //上传文件信息处理    
	                       fileName = item.getName();
	                       fileName = UUID.randomUUID().toString().replace("-", "")+(fileName.indexOf(".")>-1?fileName.substring(fileName.indexOf(".")):fileName);
	                       System.out.println("item.getFieldName()-"+item.getFieldName());
	                       //paramMap.put(item.getFieldName(), fileName);
	                       filePath1 = SystemCache.getCache("file.savedir")==null?"f":SystemCache.getCache("file.savedir");
	                       file = new File(getServletContext().getRealPath("/"+filePath1));
	                       if(!file.exists()){
	                    		  file.mkdir(); 
	                       }
	                        
	                       if(paramMap.get("UserID")!=null){
	                    	   filePath1 = "/"+filePath1+ "/"+hashfile(paramMap.get("UserID"))+ "/"+paramMap.get("UserID")+ "/"+yearmonth;
	                    	     
	                       }else{
	                    	   filePath1="/"+ filePath1+ "/"+"other"+ "/"+yearmonth;
	                    	}
	                       filePath =getServletContext().getRealPath(File.separator)+filePath1  ;
                    	   file = new File(filePath);
                    	   if(!file.exists()){
                    		  file.mkdirs(); 
                    	   }
	                       
	                       filePath = filePath+"/" + fileName;
	                       item.write(new File(filePath));
	                       
	                       if(paramMap.get("type")==null&&"autio".equals(paramMap.get("type"))){
	                    	   MP3File mfile = new MP3File(filePath);
		           				MP3AudioHeader audioHeader = (MP3AudioHeader)mfile.getAudioHeader();
		           				duration = audioHeader.getTrackLength()/60 + ":" + audioHeader.getTrackLength()%60;
	                       }
	                       System.out.println("filePath-----"+filePath);
	                       //图像压缩
	                       if(paramMap.get("isorigin")==null||!"1".equals(paramMap.get("isorigin"))){
	                    	   if(NarrowImage.writeHighQuality(NarrowImage.zoomImage(filePath,0.22f), filePath.substring(0,filePath.lastIndexOf("."))+"_1"+filePath.substring(filePath.lastIndexOf(".")))){
		                    	   fileName = fileName.substring(0,fileName.lastIndexOf("."))+"_1"+fileName.substring(fileName.lastIndexOf("."));
		                       }else{
		                    	   result = "image compress  exception";
		                       }
	    	               }
	                       
	                       result = SystemCache.getCache("web.url")+filePath1+"/" + fileName;
	                       paramMap.put(item.getFieldName(), result);
	                       paramMap.put("audiolength", duration);
	                       /*byte[] data = item.get();    
	                       System.out.println("filePath="+filePath);
	                       FileOutputStream fos = new FileOutputStream(filePath);    
	                       fos.write(data);    
	                       fos.close(); */   
	                   }    
	               }   
	               
	               
	              // resultjsonString = new HuiTingAction().parseJson(JSON.toJSONString(paramMap), httphead);
	               System.out.println("result---huitingimg163--upload-"+JSON.toJSONString(paramMap));
	           } catch (FileUploadException e) {    
	               e.printStackTrace();   
	              
	               
	           }  catch (Exception e1) {    
	               e1.printStackTrace();    
	              
	           }    
	       }  
	       response.setContentType("text/html;charset=UTF-8");
	       response.getWriter().write(JSON.toJSONString(paramMap)); 
	       response.getWriter().close();
			
			
	}
	 
	public String hashfile(String UserID){
		int hash = hash(UserID.hashCode());
		int i = hash & 1023;
		return i+"";
	}
	
	static int hash(int h) {
        // This function ensures that hashCodes that differ only by
        // constant multiples at each bit position have a bounded
        // number of collisions (approximately 8 at default load factor).
        h ^= (h >>> 20) ^ (h >>> 12);
        return h ^ (h >>> 7) ^ (h >>> 4);
    }
	
	/**
	 * Initialization of the servlet. <br>
	 *
	 * @throws ServletException if an error occurs
	 */
	public void init() throws ServletException {
		// Put your code here
		System.out.println("-------------init--------------------");
		String v = this.getInitParameter("a"); 
		System.out.println("-------------a--------------------"+v);
        Enumeration e = this.getInitParameterNames(); 
        while(e.hasMoreElements()){ 
            System.out.println(">>haha>>"+e.nextElement()); 
        } 
		try {
			
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
         
	}
}
