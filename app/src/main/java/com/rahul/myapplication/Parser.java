package com.rahul.myapplication;


import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Parser extends Fragment {

    org.jsoup.nodes.Document document;
    Connection.Response response;
    List<Information> list = new ArrayList<>();
    View layout;
    RecyclerView recyclerView;
    MyAdapter myAdapter;
    String link, title,image_link;
    boolean fromSearch=false;
    private SwipeRefreshLayout swipeContainer;
    private int previousListCount = 1, pageCount = 1;
    LinearLayoutManager linearLayoutManager;
    LinearLayout progressBarLayout, swipeMessage;
    private boolean loading = true;
    public Parser() {
        // Required empty public constructor
    }
    public Parser(String link, String title,List<Information> list, boolean fromSearch){
        this.link = link;
        this.title = title;
        this.fromSearch = fromSearch;
//        this.list = list;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getActivity().setTitle(title);
        layout = inflater.inflate(R.layout.fragment_parser, container, false);
        swipeContainer = (SwipeRefreshLayout) layout.findViewById(R.id.swipeContainer);
        recyclerView = (RecyclerView) layout.findViewById(R.id.recyclerList);
        progressBarLayout = (LinearLayout) layout.findViewById(R.id.progressBar);
        swipeMessage = (LinearLayout) layout.findViewById(R.id.swipe_message);
        swipeMessage.setVisibility(View.GONE);
        progressBarLayout.setVisibility(View.VISIBLE);
        progressBarLayout.bringToFront();
//        progressBarLayout.getLayoutParams().height = LinearLayout.LayoutParams.MATCH_PARENT;
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                list.clear();
                pageCount = 1;
                previousListCount = 1;
                progressBarLayout.setAnimation(CustomAnimation.fadeOut(getContext()));
                swipeMessage.setVisibility(View.GONE);
                progressBarLayout.setVisibility(View.GONE);
                recyclerView.removeAllViews();
                isNetworkAvailable();
            }
        });
        recyclerView.setHasFixedSize(true);
        intializeRecyclerView();
        isNetworkAvailable();
        return layout;
    }

    class JsoupAsyncTask extends AsyncTask<String, Void, Boolean> {

        FloatingActionButton fab = ((MainActivity) getActivity()).getFloatingActionButton();
        @Override
        protected void onPreExecute() {
            if (!swipeContainer.isRefreshing()) {
                progressBarLayout.setAnimation(CustomAnimation.fadeIn(getContext()));
                progressBarLayout.setVisibility(View.VISIBLE);
                progressBarLayout.bringToFront();
            }
            previousListCount = list.size();
            if (fab != null)
                fab.setVisibility(View.GONE);
            super.onPreExecute();
        }

        @Override
        protected Boolean doInBackground(String... params) {
//            link = params[0];
            try{
                Log.d("link  ", params[0]);
                document = Jsoup.connect(params[0])
                        .timeout(0)
                        .userAgent("Mozilla/5.0 (Windows NT 6.3; WOW64; rv:35.0) Gecko/20100101 Firefox/35.0")
                        .followRedirects(true)
                        .get();
                if (!fromSearch) {
                    Elements elements = document.getElementById("lcp_instance_0").getElementsByTag("li");
                    for (org.jsoup.nodes.Element element : elements) {
                        Information information = new Information();
                        information.title = element.select("h2").text();
                        information.link = element.select("a[href]").attr("href");
                        image_link = element.select("img[src]").attr("src");
                        if (image_link.contains("resize=")) {
                            image_link = image_link.substring(0, image_link.indexOf("resize="));
                            image_link = image_link + "resize=180%2C180";
                        }
                        information.image_link = image_link;
//                        element.getElementsByTag("div")
                        information.desc = element.getElementsByTag("div").get(1).text();
                        list.add(information);
                    }
                }else{
                    if (!document.select("div.postcontent").isEmpty()) {
                        Elements elements = document.select("div.postcontent");
                        for (Element element : elements) {
                            Information information = new Information();
                            information.title = element.getAllElements().get(1).text();
                            if (information.title.isEmpty())
                                information.title = element.getAllElements().get(2).text();
                            information.desc = element.select("p:not(p:has(strong))").get(1).text();
//                            information.desc = element.select("p").get(2).text();
                            information.link = element.select("a[href]").attr("href");
                            image_link = element.select("img[src]").attr("src");
                            if (image_link.contains("resize=")) {
                                image_link = image_link.substring(0, image_link.indexOf("resize="));
                                image_link = image_link + "resize=150%2C150";
                            }
                            information.image_link = image_link;
                            list.add(information);
                        }
                    }else{
//                        Toast.makeText(getContext(), "Content not found :-(", Toast.LENGTH_SHORT).show();
                    }
                }
            }
            catch (IOException e){
                e.printStackTrace();
            }finally {
                return null;
            }
        }

        @Override
        protected void onPostExecute(Boolean th) {
            if (Parser.this.isVisible()) {
                swipeContainer.setRefreshing(false);
                myAdapter.notifyDataSetChanged();
                if (list.isEmpty()) {
                    swipeMessage.setVisibility(View.VISIBLE);
                    swipeMessage.bringToFront();
                }
                if (list.isEmpty() || list.size() == previousListCount) {
                    loading = true;
                    Toast.makeText(getContext(), "Content not found"+" may be its network problem "+"please try again", Toast.LENGTH_SHORT).show();
                    if (pageCount > 1)
                        pageCount--;
                } else {
                    progressBarLayout.setAnimation(CustomAnimation.fadeOut(getContext()));
                    progressBarLayout.setVisibility(View.GONE);
                    if (previousListCount+recyclerView.getChildCount()-2 >= 0)
                        recyclerView.smoothScrollToPosition(previousListCount+recyclerView.getChildCount()-2);
                    recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {

                        @Override
                        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                            if (loading && dy > 0 && !fromSearch) {
                                if (linearLayoutManager.findLastCompletelyVisibleItemPosition() == list.size() - 1) {
                                    loading = false;
                                    pageCount++;
                                    isNetworkAvailable();
                                }
                            }
                        }
                    });
                    loading = true;
                }
                progressBarLayout.setAnimation(CustomAnimation.fadeOut(getContext()));
                progressBarLayout.setVisibility(View.GONE);
                progressBarLayout.getLayoutParams().height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 45, getResources().getDisplayMetrics());
                ;
//                    TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 45, getResources().getDisplayMetrics());
//            if (fab != null)
//                fab.setVisibility(View.VISIBLE);
            }
        }
    }

    public void isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        if (activeNetworkInfo != null && activeNetworkInfo.isConnected()){
            Log.d("network", "async  called");
//            JsoupAsyncTask jsoupAsyncTask = new JsoupAsyncTask();
            if (fromSearch) {
                new JsoupAsyncTask().execute(link);
            }
            else if (pageCount > 1)
                new JsoupAsyncTask().execute(link + "?lcp_page0=" + pageCount);
            else
                new JsoupAsyncTask().execute(link);
        }
        else showDialogBox();
    }

    public boolean showDialogBox(){
        android.app.AlertDialog.Builder dialog = new android.app.AlertDialog.Builder(getContext());
        dialog.setTitle("Network Connectivity");
        dialog.setMessage("No internet connection detected please try again");
        dialog.setPositiveButton("Retry", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                isNetworkAvailable();
            }
        });
        dialog.setNegativeButton("Exit", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                System.exit(0);
            }
        });
//        dialog.setIcon(android.R.drawable.ic_dialog_alert);
        dialog.setCancelable(false);
        dialog.show();
        return true;
    }
    public void intializeRecyclerView(){

        myAdapter = new MyAdapter(getContext(), list, true);
        recyclerView.setAdapter(myAdapter);
        linearLayoutManager = new LinearLayoutManager(getActivity(),LinearLayoutManager.VERTICAL,false);
//                linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(linearLayoutManager);
    }

}
