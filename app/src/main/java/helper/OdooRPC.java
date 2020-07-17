package helper;

import android.util.Log;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.timroes.axmlrpc.XMLRPCCallback;
import de.timroes.axmlrpc.XMLRPCClient;
import de.timroes.axmlrpc.XMLRPCException;

public class OdooRPC {
    String uid=null;
    String path = "http://192.168.0.15:8069",
            path2 = "http://192.168.43.240:8069",
            db = "ANYSERVICE",
            username = "android",
            password = "@ndroid@1326";
    XMLRPCClient client, model;
    public OdooRPC(){
        try {
            path=path2;
            URL url1 =new URL(path+ "/xmlrpc/2/common");
            URL url2 =new URL(path+ "/xmlrpc/2/object");
            client = new XMLRPCClient(url1);
            model = new XMLRPCClient(url2);
        } catch (Exception e) {
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
            Log.e("ODOO RPC :",e.getMessage());
        }
        return res;
    }

}
