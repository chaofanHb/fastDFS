package cn.hebin.controller;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.csource.common.MyException;
import org.csource.fastdfs.ProtoCommon;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import cn.hebin.util.FastDFSClient;

@Controller
@RequestMapping("/fdfs")
public class FastDFSController {
	
	@Autowired
	private FastDFSClient fdfsClient;
	
	@Value("${fdfs.web-server-url}")
    private String fastdfsUrl;
	
	@Value("${fdfs.http.secret_key}")
    private String fastdfsToken;
 
	/**
	 * 文件上传
	 * @param file
	 * @return
	 * @throws Exception
	 */
	@RequestMapping("/upload/file")
	@ResponseBody
	public Map<String,Object> upload(@RequestParam("file") MultipartFile file, Model model) throws Exception{
		Map<String,Object> resultMap = new HashMap<>();
		String url = null;
		
		try {
			url = fdfsClient.uploadFile(file);
			resultMap.put("code", 200);
			resultMap.put("message", "上传成功");
			resultMap.put("url", url);
			System.out.println(url);
        } catch (Exception e) {
       	    // TODO: handle exception
        	resultMap.put("status", 500);
        	resultMap.put("message", "上传异常！");
        }
		
		return resultMap;
	}
	
	/**
	 * 文件下载
	 * @param fileUrl  url 开头从组名开始
	 * @param response
	 * @throws Exception
	 */
	@RequestMapping(value="/download", method = {RequestMethod.GET})
	public void  download(HttpServletResponse response, HttpServletRequest request) throws Exception{
		String fileUrl = request.getParameter("fileUrl");
		
		byte[] data = fdfsClient.download(fileUrl);
		
		response.setCharacterEncoding("UTF-8");
		response.setHeader("Content-disposition", "attachment;filename=" + URLEncoder.encode("test.jpg", "UTF-8"));
        
		// 写出
		ServletOutputStream outputStream = response.getOutputStream();
		IOUtils.write(data, outputStream);
	}
	
	/**
	 * 生成访问链接
	 */
	@RequestMapping(value="/location", method = {RequestMethod.GET})
	@ResponseBody
	public String location(HttpServletResponse response, HttpServletRequest request, Model model) {
		//fileUrl示例： group1/M00/00/00/wKgzgFnkTPyAIAUGAAEoRmXZPp876.jpeg
		String fileUrl = request.getParameter("location");
		System.out.println(fileUrl);
		//token
    	String token = fastdfsToken;
    	String IP = fastdfsUrl;
    	
		fileUrl = getToken(fileUrl,token,IP);
		System.out.println(fileUrl);
		
		return fileUrl;
	}
	
    /**
    * 获取访问服务器的token，拼接到地址后面
    *
    * @param fid 文件路径 group1/M00/00/00/wKgzgFnkTPyAIAUGAAEoRmXZPp876.jpeg
    * @param secret_key 密钥
    * @return 返回token，如： token=078d370098b03e9020b82c829c205e1f&ts=1508141521
    */
   public static String getToken(String fid, String secret_key, String IP){
	
	   String substring = fid.substring(fid.indexOf("/")+1);
	   //unix时间戳 以秒为单位
	   int ts = (int) (System.currentTimeMillis() / 1000);
	   String token=new String();
	   try {
	   	token= ProtoCommon.getToken(substring, ts, secret_key);
	   } catch (UnsupportedEncodingException e) {
		   e.printStackTrace();
	   } catch (NoSuchAlgorithmException e) {
		   e.printStackTrace();
	   } catch (MyException e) {
		   e.printStackTrace();
		}
	   StringBuilder sb = new StringBuilder();
	   sb.append(IP);
	   sb.append(fid);
	   sb.append("?token=").append(token);
	   sb.append("&ts=").append(ts);
	   //System.out.println(sb.toString());
	   
       return sb.toString();
   }
}

