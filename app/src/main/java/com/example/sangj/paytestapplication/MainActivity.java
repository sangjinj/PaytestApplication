package com.example.sangj.paytestapplication;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;

import com.example.sangj.paytestapplication.iamportsdk.InicisWebViewClient;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public class MainActivity extends Activity {
    private WebView mainWebView;
    private final String APP_SCHEME = "iamporttest://";
    String[] m_xmlString;	//app url 모음


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        mainWebView = (WebView) findViewById(R.id.mainWebView);

        WebSettings settings = mainWebView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setLoadWithOverviewMode(true);
        settings.setUseWideViewPort(true);
        //settings.setPluginsEnabled(true);
        settings.setSupportMultipleWindows(true);
        settings.setJavaScriptCanOpenWindowsAutomatically(true);
       // mainWebView.setWebViewClient(new InicisWebViewClient(this));
        mainWebView.setWebChromeClient(new WebChromeClient(){
            public void loadAppUrlXML(){
                String xml = "";
                StringBuffer sBuffer = new StringBuffer();

                try {
                    String urlAddr = "https://sp.easypay.co.kr/app/androidAppUrl.xml";
                    URL url = new URL(urlAddr);
                    HttpURLConnection conn = (HttpURLConnection)url.openConnection();
                    if(conn != null){
                        conn.setConnectTimeout(20000);
                        conn.setUseCaches(false);

                        if(conn.getResponseCode() == HttpURLConnection.HTTP_OK){
                            InputStreamReader isr = new InputStreamReader(conn.getInputStream());
                            BufferedReader br = new BufferedReader(isr);
                            while(true){
                                String line = br.readLine();
                                if(line == null){
                                    break;
                                }
                                sBuffer.append(line);
                            }
                            br.close();
                            conn.disconnect();
                        }
                    }
                    xml = sBuffer.toString();
                } catch(Exception e){}

                parseXML(xml);
            }

            // XML 파일에서 URL을 추출한다.
            public void parseXML(String xml){
                try{
                    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                    DocumentBuilder documentBuilder = factory.newDocumentBuilder();

                    InputStream is = new ByteArrayInputStream(xml.getBytes());
                    Document doc = documentBuilder.parse(is);
                    org.w3c.dom.Element element = doc.getDocumentElement();
                    NodeList items = element.getElementsByTagName("url");
                    int n = items.getLength();

                    m_xmlString = new String[n];

                    for(int i=0 ; i<n ; i++){
                        Node item = items.item(i);
                        Node text = item.getFirstChild();
                        String itemValue = text.getNodeValue();

                        m_xmlString[i] = itemValue;
                    }

                } catch(Exception e){}
            }


            public boolean shouldOverrideUrlLoading(WebView view, String url) {

                boolean bContainUrl = false;

                loadAppUrlXML();

// URL 이 포함되어 있는지 검사
                if( url != null ) {
                    for( int i=0 ; i<m_xmlString.length ; i++ ) {
                        if( url.contains(m_xmlString[i]) ) {
                            bContainUrl = true;
                            break;
                        }
                    }
                }

                if( bContainUrl ) {
                    Uri uri = Uri.parse(url);
                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                    try{
                        startActivity(intent);

                    }catch(ActivityNotFoundException e)	{

                        return false;
                        //view.loadData("<html><body>error</body></html>", "text/html", "euc-kr");
                    }
                }
                else {
                    view.loadUrl(url);
                    return false;
                }
                return true;
            }
                                       });
        Intent intent = getIntent();
        Uri intentData = intent.getData();
        System.out.println(":::::::::::::::::::::::::::::::::::::::::::::::::::: start :::::::::::::::::::");
        if ( intentData == null ) {
            mainWebView.loadUrl("http://tourmate.maketicket.co.kr/ticket/ticket.do?plan_goods=501101||66&sitetype=tourplan&plan_seq=129&scdu_date=20160906&scdu_time=0800&command=ticket_viewn");
        } else {
            //isp 인증 후 복귀했을 때 결제 후속조치
            String url = intentData.toString();
            if ( url.startsWith(APP_SCHEME) ) {
                String redirectURL = url.substring(APP_SCHEME.length()+3);
                mainWebView.loadUrl(redirectURL);
            }
        }
    }
}
