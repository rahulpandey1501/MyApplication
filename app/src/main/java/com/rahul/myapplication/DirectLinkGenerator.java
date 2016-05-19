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

    public String checkForLink(String link, Context context){
        mContext = context;
        for (String s: DirectSupportedLink.supportedLink){
            if (link.toLowerCase().contains(s.toLowerCase())) {
                flag = true;
//                new DirectLinkGeneratorAsync(new DirectLinkGeneratorAsync.AsyncResponse() {
//                    @Override
//                    public void processFinish(String output) {
//                        outputLink =  output;
//                    }
//                }).execute(link);
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
                if (params[0].contains("uplod.it")){
                    String id = params[0].substring(params[0].lastIndexOf("/")+1);
                    Log.d("id", id);
                    document = Jsoup.connect(params[0])
                            .timeout(0)
                            .userAgent("Mozilla/5.0 (Windows NT 6.3; WOW64; rv:35.0) Gecko/20100101 Firefox/35.0")
                            .followRedirects(true)
                            .data("op", "download2")
                            .data("id", id)
                            .data("download_block", "0")
                            .data("down_script", "1")
                            .post();
//                    Log.d("res", document.getElementsByClass("container").get(1).toString());
                    directLink = document.getElementsByClass("container").get(1).getElementsByTag("a").attr("href");
                }else {
                    document = Jsoup.connect(DirectSupportedLink.generatorLinkPrefix + params[0] + DirectSupportedLink.generatorLinkPostfix)
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
