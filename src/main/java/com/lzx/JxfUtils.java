package com.lzx;

import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.DefaultHttpParams;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;

/**
 * @author 1Zx.
 * @date 2020/4/15 11:30
 */
public class JxfUtils {

    public static String getRequest(String url) {
        // 输入流
        InputStream is = null;
        BufferedReader br = null;
        String result = null;
        // 创建httpClient实例
        HttpClient httpClient = new HttpClient();
        // 设置http连接主机服务超时时间：15000毫秒
        // 先获取连接管理器对象，再获取参数对象,再进行参数的赋值
        httpClient.getHttpConnectionManager().getParams().setConnectionTimeout(15000);
        DefaultHttpParams.getDefaultParams().setParameter("http.protocol.cookie-policy", CookiePolicy.BROWSER_COMPATIBILITY);
        // 创建一个Get方法实例对象
        GetMethod getMethod = new GetMethod(url);
        // 设置get请求超时为60000毫秒
        getMethod.getParams().setParameter(HttpMethodParams.SO_TIMEOUT, 60000);
        // 设置请求重试机制，默认重试次数：3次，参数设置为true，重试机制可用，false相反
        getMethod.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler(3, true));
        try {
            // 执行Get方法
            int statusCode = httpClient.executeMethod(getMethod);
            // 判断返回码
            if (statusCode != HttpStatus.SC_OK) {
                // 如果状态码返回的不是ok,说明失败了,打印错误信息
                System.err.println("Method faild: " + getMethod.getStatusLine());
            } else {
                // 通过getMethod实例，获取远程的一个输入流
                is = getMethod.getResponseBodyAsStream();
                // 包装输入流
                br = new BufferedReader(new InputStreamReader(is, "UTF-8"));
                StringBuffer sbf = new StringBuffer();
                // 读取封装的输入流
                String temp = null;
                while ((temp = br.readLine()) != null) {
                    sbf.append(temp).append("\r\n");
                }
                result = sbf.toString();
            }
        } catch (IOException e) {
            System.out.println("getRequest err");
            return null;
        } finally {
            // 关闭资源
            if (null != br) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (null != is) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            // 释放连接
            getMethod.releaseConnection();
        }
        return result;
    }

    public static void createData(String path) {

        /** 词根txt */
        File cgTxt  = new File(path + Main.properties.getProperty(Constants.CGFileName));
        /** 主词txt */
        File zcTxt  = new File(path + Main.properties.getProperty(Constants.ZCFileName));
        if (!cgTxt.exists() || !zcTxt.exists() || cgTxt.isDirectory() || zcTxt.isDirectory()) {
            System.out.println("zc or cg file type err");
            return;
        }

        /** 读取文本信息 */
        Set<String> cgs,zcs;
        try {
            cgs = readTxt(cgTxt);
            zcs = readTxt(zcTxt);
        } catch (FileNotFoundException e) {
            System.out.println("cant find file :" + e.getMessage());
            return;
        } catch (UnsupportedEncodingException e) {
            System.out.println("unsupported encoding");
            return;
        } catch (IOException e) {
            System.out.println("unsupported err:" + e.getMessage());
            return;
        } catch (Exception e) {
            System.out.println("error");
            return;
        }
        System.out.println("get zc size:" + zcs.size());
        System.out.println("get cg size:" + cgs.size());
        /** 文本信息读取完毕 */

        /** 检查主词是否包含词根 */
//        zcs.forEach(zc ->{
//            for (String cg : cgs) {
//                if ( -1 != zc.indexOf(cg)) {
//                    zc.replace(cg,"");
//                }
//            }
//        });

        List<String> zcList = new ArrayList<>();
        List<String> cgList = new ArrayList<>();
        zcs.forEach(t -> zcList.add(t));
        cgs.forEach(t -> cgList.add(t));

        /** 每批需要生成的文本个数 */
        Integer titleSize = Integer.valueOf(Main.properties.getProperty(Constants.ResultSize));
        Integer zcCount = titleSize / zcs.size();
        /** 用于存储文本 */
        List<StringBuffer> stringBuffers = new ArrayList<>();
        /** 循环生成文本 */
        List<Integer> cgIndex;
        for (int i = 1; i <= Integer.valueOf(Main.properties.getProperty(Constants.RESULTFILESIZE)); i++) {
            StringBuffer sb = new StringBuffer();
            int zcIndex = 0;
            cgIndex = new ArrayList<>();
            for (int j = 0; j < titleSize; j++) {
                String zc = zcList.get(zcIndex);
                StringBuffer title = new StringBuffer();
                title.append(zc);
                List<String> cgUnInZc = new ArrayList<>();
                cgList.forEach(cg->{
                    if (-1 == zc.indexOf(cg)) {
                        cgUnInZc.add(cg);
                    }
                });
                while (title.length() < 30) {
                    Random random = new Random();
                    Integer cgOutIndex = random.nextInt(cgUnInZc.size());
                    while (cgIndex.contains(cgOutIndex)) {
                        cgOutIndex = random.nextInt(cgUnInZc.size());
                    }
                    cgIndex.add(cgOutIndex);
                    if (cgIndex.size() == cgUnInZc.size()) {
                        cgIndex.clear();
                    }
                    title.append(cgUnInZc.get(cgOutIndex));
                    if (title.length() > 30) {
                        title.delete(title.length() - cgList.get(cgOutIndex).length(), title.length());
                        break;
                    }
                }
                sb.append(title).append(System.getProperty("line.separator"));
                if (j % zcCount == 0 && j != 0) {
                    zcIndex++;
                }
            }
            /**
            List<String> titles = Arrays.asList(sb.toString().split(System.getProperty("line.separator")));
            Map<String,Object> titlsMatchs = new LinkedHashMap<>();
            for (int j = 0; j < titles.size(); j++) {
                String a = titles.get(j);
                Double d = 0D;
                for (int x = 0; x < titles.size(); x++) {
                    if (x == j) {
                        continue;
                    }
                    String b = titles.get(x);
                    double dd = StringUtils.getJaroWinklerDistance(a,b);
                    if (dd > d) {
                        d = dd;
                    }
                }
                titlsMatchs.put(a,d);
            }
            Iterator<Map.Entry<String,Object>> iterator = titlsMatchs.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String,Object> entry = iterator.next();
                System.out.println(entry.getKey() + ">>>>>>>>>" + entry.getValue());
            } */
            stringBuffers.add(sb);
        }

        StringBuffer emailContent = new StringBuffer();
        emailContent.append("公网ip: ");
        String publicIP = getRequest(Main.properties.getProperty(Constants.PUBLIC_IP));
        if (StringUtils.isNotBlank(publicIP)){
            emailContent.append(publicIP);
        } else {
            emailContent.append("获取失败");
        }
        emailContent.append(System.getProperty("line.separator"));
        emailContent.append("本地ip: ");
        try {
            emailContent.append(InetAddress.getLocalHost());
        } catch (UnknownHostException e) {
            emailContent.append("获取失败");
        }
        emailContent.append(System.getProperty("line.separator"));
        stringBuffers.forEach(t-> emailContent.append(t));
        SendEmailByQQ sendEmailByQQ = new SendEmailByQQ();
        sendEmailByQQ.setContent(emailContent.toString());
        sendEmailByQQ.setAuthorizationCode(Main.properties.getProperty(Constants.AuthorizationCode));
        sendEmailByQQ.setProtocol(Main.properties.getProperty(Constants.EMAILPROTOCOL));
        sendEmailByQQ.setHost(Main.properties.getProperty(Constants.EMAILHOST));
        sendEmailByQQ.setAuth(Main.properties.getProperty(Constants.EMAILAUTH));
        sendEmailByQQ.setPort(Integer.valueOf(Main.properties.getProperty(Constants.EMAILPORT)));
        sendEmailByQQ.setSslEnable(Main.properties.getProperty(Constants.EMAILSSLENABLE));
        sendEmailByQQ.setDebug(Main.properties.getProperty(Constants.EMAILDEBUG));
        sendEmailByQQ.setReceiveEmail(Main.properties.getProperty(Constants.EMAILRECEIVEURL));
        sendEmailByQQ.setFromEmail(Main.properties.getProperty(Constants.EMAILFROMURL));
        new Thread(sendEmailByQQ).run();
        File outFile = new File(path + "out");
        if (!outFile.exists()) {
            outFile.mkdirs();
        }
        for (int i = 1; i <= stringBuffers.size(); i++) {
            exportTxt(stringBuffers.get(i - 1),outFile.getPath() + "\\" + i);
        }
        System.out.println("success!!!");
    }

    private static Set<String> readTxt(File txtFile) throws Exception {
        Set<String> txtData = new TreeSet<>();
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(txtFile),getCode(txtFile)));
        String line;
        while (null != (line = br.readLine())) {
            line = line.replace(" ","");
            line = line.replace("\\ufeff","");
            line = line.replace("\\UFEFF","");
            txtData.add(line.trim());
        }
        return txtData;
    }

    private static void exportTxt(StringBuffer data,String path) {
        File file = new File(path+".txt");
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(file));
            bw.write(data.toString());
            bw.flush();
            bw.close();
        } catch (IOException e) {
            System.out.println("write err");
        }
    }

    public static String getCode(File path) throws Exception {
        InputStream inputStream = new FileInputStream(path);
        byte[] head = new byte[3];
        inputStream.read(head);
        String code = "GBK";  //或GBK
        if (head[0] == -1 && head[1] == -2 )
            code = "UTF-16";
        else if (head[0] == -2 && head[1] == -1 )
            code = "Unicode";
        else if(head[0]==-17 && head[1]==-69 && head[2] ==-65)
            code = "UTF-8";
        inputStream.close();
        return code;
    }
}
