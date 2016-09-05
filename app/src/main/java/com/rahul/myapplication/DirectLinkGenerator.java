package com.rahul.myapplication;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.speech.tts.Voice;
import android.util.Log;
import android.widget.Toast;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;

/**
 * Created by Rahul on 14 May 2016.
 */

public class DirectLinkGenerator {
    boolean flag = false;
    Context mContext;
    String outputLink = null;
    public static String SERVER = Constants.SERVER_1;

    public String checkForLink(String link, Context context){
        mContext = context;
        for (String s: DirectSupportedLink.supportedLink){
            if (link.toLowerCase().contains(s.toLowerCase())) {
                flag = true;
                new DirectLinkGeneratorAsync().execute(link);
                break;
            }
        }
        Log.d("link re", flag+"");
        if (!flag)
            openBrowserIntent(link);
        return outputLink;
    }

    private class DirectLinkGeneratorAsync extends AsyncTask<String, String, String>{

//        public interface AsyncResponse {
//            void processFinish(String output);
//        }
//
//        public AsyncResponse delegate = null;
//
//        public DirectLinkGeneratorAsync(AsyncResponse delegate){
//            this.delegate = delegate;
//        }

        ProgressDialog progressDialog;
        @Override
        protected void onPreExecute() {
            progressDialog = new ProgressDialog(mContext);
            progressDialog.setTitle(mContext.getResources().getString(R.string.app_name));
            progressDialog.setMessage("please wait while we generate a direct link for you...");
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.show();
        }

        @Override
        protected String doInBackground(String... params) {
            String directLink = "";
            Document document;
            try {
                if (params[0].contains("uplod.it") || params[0].contains("uploads.to")){
                    String id = params[0].substring(params[0].lastIndexOf("/")+1);
                    document = Jsoup.connect(params[0].replace("uplod.it","uploads.to"))
                            .timeout(0)
                            .userAgent("Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/51.0.2704.103 Safari/537.36")
                            .data("op", "download2")
                            .data("id", id)
                            .data("download_block", "0")
                            .data("down_script", "1")
                            .post();
                    directLink = document.getElementsByClass("container").get(1).getElementsByTag("a").attr("href");
                }else {
                    String downloadLink;
                    if (SERVER.equals(Constants.SERVER_1))
                        downloadLink = DirectSupportedLink.generatorLinkPrefix_1 + params[0] + DirectSupportedLink.generatorLinkPostfix_1;
                    else
                        downloadLink = DirectSupportedLink.generatorLinkPrefix_2 + params[0] + DirectSupportedLink.generatorLinkPostfix_2;
                    Log.d("res", downloadLink);
                    document = Jsoup.connect(downloadLink)
                            .timeout(0)
                            .userAgent("Mozilla/5.0 (Windows NT 6.3; WOW64; rv:35.0) Gecko/20100101 Firefox/35.0")
                            .followRedirects(true)
                            .get();
                    directLink = document.getElementsByTag("a").attr("href");
                }
                Log.d("drct link", directLink);
                return directLink;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return directLink;
        }

        @Override
        protected void onPostExecute(String link) {
            progressDialog.hide();
            openBrowserIntent(link);
//            delegate.processFinish(link);
        }
    }

    public void openBrowserIntent(String link){
        try {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(link));
            browserIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            mContext.startActivity(browserIntent);
        }catch (Exception e){
            Toast.makeText(mContext, "Server seems to be down Try another link", Toast.LENGTH_SHORT).show();
        }
    }
}
