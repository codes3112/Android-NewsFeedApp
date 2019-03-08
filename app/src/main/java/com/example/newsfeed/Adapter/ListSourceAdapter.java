package com.example.newsfeed.Adapter;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.newsfeed.Common.Common;
import com.example.newsfeed.Interface.IconBetterIdeaService;
import com.example.newsfeed.Interface.ItemClickListener;
import com.example.newsfeed.ListNews;
import com.example.newsfeed.Model.IconBetterIdea;
import com.example.newsfeed.Model.WebSite;
import com.example.newsfeed.R;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

class ListSourceViewHolder extends RecyclerView.ViewHolder implements
        View.OnClickListener{

    ItemClickListener itemClickListener;
    TextView source_title;
    CircleImageView source_image;


    //Created a Constructor
    public ListSourceViewHolder(@NonNull View itemView) {

        super(itemView);
        source_image=(CircleImageView) itemView.findViewById(R.id.source_image);
        source_title=(TextView)itemView.findViewById(R.id.source_name);

        //get clicklistener state
        itemView.setOnClickListener(this);


    }
    //generate a setter
    public void setItemClickListener(ItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    @Override
    public void onClick(View v) {

        itemClickListener.onClick(v, getAdapterPosition(),false);
    }
}

public class ListSourceAdapter extends RecyclerView.Adapter<ListSourceViewHolder> {
    private Context context;
    private WebSite webSite;

    private IconBetterIdeaService mService;

    //Create Constructor
    public ListSourceAdapter(Context context, WebSite webSite) {
        this.context = context;
        this.webSite = webSite;

        mService= Common.getIconService();
    }

    @NonNull
    @Override
    public ListSourceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater=LayoutInflater.from(parent.getContext());
        View itemView=inflater.inflate(R.layout.source_layout,parent,false);
        return new ListSourceViewHolder(itemView);
    }
    // https://icons.better-idea.org/allicons.json?url=
    @Override
    public void onBindViewHolder(@NonNull final ListSourceViewHolder holder, int position) {
        StringBuilder iconBetterAPI =new StringBuilder("https://i.olsh.me/allicons.json?url=");
        //appending the newspaper name
        iconBetterAPI.append(webSite.getSources().get(position).getUrl());

        //setting the image
        mService.getIconUrl(iconBetterAPI.toString())
                .enqueue(new Callback<IconBetterIdea>() {
                    @Override
                    public void onResponse(Call<IconBetterIdea> call, Response<IconBetterIdea> response) {
                        if(response.body().getIcons().size()>0)
                        {
                            Picasso.get()
                                    .load(response.body().getIcons().get(0).getUrl())
                                    .into(holder.source_image);
                        }
                    }

                    @Override
                    public void onFailure(Call<IconBetterIdea> call, Throwable t) {

                    }
                });

        //setting the text
        holder.source_title.setText(webSite.getSources().get(position).getName());

        holder.setItemClickListener(new ItemClickListener() {
            @Override
            public void onClick(View view, int position, boolean isLongClick) {
                //setting up internal data networking
                Intent intent = new Intent(context, ListNews.class);
                intent.putExtra("source",webSite.getSources().get(position).getId());
                //intent.putExtra("source",webSite.getSources().get(position).getSortByAvailable().get(0));
                context.startActivity(intent);
            }
        });


    }

    @Override
    public int getItemCount() {
        return webSite.getSources().size() ;
    }
}
