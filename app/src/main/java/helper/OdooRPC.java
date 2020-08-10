package helper;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import de.timroes.axmlrpc.XMLRPCClient;
import de.timroes.axmlrpc.XMLRPCException;

public class OdooRPC {
    String uid=null;
    String path = "http://148.66.142.98:8079",
            db = "ANYSERVICE",
            username = "android",
            password = "@ndroid@1326";
    XMLRPCClient client, model;
    public OdooRPC(){
        try {
            URL url1 =new URL(path+ "/xmlrpc/2/common");
            URL url2 =new URL(path+ "/xmlrpc/2/object");
            client = new XMLRPCClient(url1);
            model = new XMLRPCClient(url2);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public OdooRPC(String path){
        this.path = path;
        try {
            URL url1 =new URL(path+ "/xmlrpc/2/common");
            URL url2 =new URL(path+ "/xmlrpc/2/object");
            client = new XMLRPCClient(url1);
            model = new XMLRPCClient(url2);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public String login() throws XMLRPCException {
            client.setTimeout(15);
            uid = String.valueOf(client.call("login",db,username,password));
        return uid;
    }

    public Object callOdoo(String modelName,String func,Map<String,Object> map){
        Object res = null;
        Object[] list;
        Map<String,Object> emptymap = new HashMap<>();
        list = new Object[]{emptymap,map};
        try {
            res = model.call("execute_kw",db,uid,password,modelName,func,list);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return res;
    }

    private final int  MEGABYTE = 1024 * 1024;

    public void downloadInvoice(int id, File directory){
        try {

            URL url = new URL(path+"/order/print?id="+id);
            HttpURLConnection urlConnection = (HttpURLConnection)url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            InputStream inputStream = urlConnection.getInputStream();
            FileOutputStream fileOutputStream = new FileOutputStream(directory);
            int totalSize = urlConnection.getContentLength();

            byte[] buffer = new byte[MEGABYTE];
            int bufferLength = 0;
            while((bufferLength = inputStream.read(buffer))>0 ){
                fileOutputStream.write(buffer, 0, bufferLength);
            }
            fileOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public String downloadURL(int id){
        return path+"/order/print?id="+id;
    }
}
