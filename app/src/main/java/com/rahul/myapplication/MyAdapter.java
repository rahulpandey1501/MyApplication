package com.rahul.myapplication;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.media.Image;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.Adapter;
import android.text.ClipboardManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Created by rahul on 17/1/16.
 */
public class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {

    private LayoutInflater inflater;
    private List<Information> list = Collections.emptyList();
    private Context context;
    boolean flag=true;
    public SharedPreferences sharedPreferences;
    public SharedPreferences.Editor sharedPreferencesEditor;
    static int currentPosition = 0;
    public MyAdapter(Context context, List<Information> list, boolean flag){

        inflater = LayoutInflater.from(context);
        this.list = list;
        this.context = context;
        this.flag = flag;
        sharedPreferences = context.getSharedPreferences(MainActivity.SHAREDPREFRENCES_STRING, context.MODE_PRIVATE);
        sharedPreferencesEditor = sharedPreferences.edit();
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.recycler_item, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {
        final Information information = list.get(position);
        currentPosition = position;
        if (flag) {
            holder.title.setText(information.title.replace("is here", "").replace("[latest]", "").replace("!",""));
            holder.dTitle.setText(information.desc);
            holder.date.setText(information.date);
            Picasso.with(context).load(information.image_link).into(holder.imageView);
            holder.itemView.setLongClickable(true);
            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    Toast.makeText(context, information.title, Toast.LENGTH_LONG).show();
                    return true;
                }
            });
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, ContentViewActivity.class);
                    intent.putExtra("next_link", information.link);
                    intent.putExtra("desc", information.desc);
                    intent.putExtra("image_link", information.image_link);
                    intent.putExtra("title", information.title);
                    context.startActivity(intent);
                    ((Activity) context).overridePendingTransition(R.anim.enter, R.anim.exit);
                }
            });
        }else{
            holder.dTitle.setText(information.title);
            holder.dLink.setText(information.link);
            for (String link: DirectSupportedLink.supportedLink){
                if(information.link.toLowerCase().contains(link.toLowerCase())) {
                    holder.directLinkIndicator.setVisibility(View.VISIBLE);
                    holder.dLink.setText( "[DIRECT] "+information.link.toString());
                    break;
                }
            }
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new DirectLinkGenerator().checkForLink(information.link, context);
                }
            });

            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    copyToClipBoard(information.link);
                    return true;
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder{

        TextView title,dLink,dTitle, date;
        ImageView imageView;
        View directLinkIndicator;

        public MyViewHolder(View itemView) {
            super(itemView);
            Typeface tf = Typeface.createFromAsset(context.getAssets(), "fonts/Montserrat-Regular.ttf");
            Typeface tfD = Typeface.createFromAsset(context.getAssets(), "fonts/Roboto-Light.ttf");
            if(flag) {
                title = (TextView) itemView.findViewById(R.id.title);
                dTitle = (TextView) itemView.findViewById(R.id.titleDesc);
                imageView = (ImageView) itemView.findViewById(R.id.imageView);
                date = (TextView) itemView.findViewById(R.id.upload_date);
                itemView.findViewById(R.id.descPageLayout).setVisibility(View.GONE);
                title.setTypeface(tf);
                dTitle.setTypeface(tfD);
            }else {
                dTitle = (TextView) itemView.findViewById(R.id.dTitle);
                dLink = (TextView) itemView.findViewById(R.id.dLink);
                dTitle.setTypeface(tf);
                directLinkIndicator = itemView.findViewById(R.id.direct_link_indicator);
                itemView.findViewById(R.id.descPageLayout).setVisibility(View.VISIBLE);
                itemView.findViewById(R.id.card_view_layout).setVisibility(View.GONE);
            }
        }
    }
    public void clear() {
        list.clear();
        notifyDataSetChanged();
    }

    // Add a list of items
    public void addAll(List<Information> list) {
        list.addAll(list);
        notifyDataSetChanged();
    }

    public int getCurrentPosition(){
        return currentPosition;
    }

    private void copyToClipBoard(String responseLink) {
        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(context.CLIPBOARD_SERVICE);
        clipboard.setText(responseLink);
        Toast.makeText(context, "Link copied to clipboard", Toast.LENGTH_SHORT).show();
    }
}
