package com.example.newsfeed;

import android.content.Intent;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.example.newsfeed.Adapter.ListNewsAdapter;
import com.example.newsfeed.Common.Common;
import com.example.newsfeed.Interface.NewsService;
import com.example.newsfeed.Model.Article;
import com.example.newsfeed.Model.News;
import com.flaviofaria.kenburnsview.KenBurnsView;
import com.github.florent37.diagonallayout.DiagonalLayout;
import com.squareup.picasso.Picasso;

import java.util.List;

import dmax.dialog.SpotsDialog;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ListNews extends AppCompatActivity {

    KenBurnsView kbv;
    DiagonalLayout diagonalLayout;
    SpotsDialog dialog;
    NewsService mService;
    TextView top_author, top_title;
    SwipeRefreshLayout swipeRefreshLayout;

    String source="", webHotURL="";

    ListNewsAdapter adapter;
    RecyclerView listNews;
    RecyclerView.LayoutManager layoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_news);

        //Service

        mService= Common.getNewsService();
        dialog= new SpotsDialog((this));

        //View

        swipeRefreshLayout=(SwipeRefreshLayout)findViewById(R.id.swipeRefresh);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadNews(source,true);
            }
        });

        diagonalLayout=(DiagonalLayout)findViewById(R.id.diagonalLayout);
        diagonalLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //click to latest hot news
                Intent detail = new Intent(getBaseContext(),DetailArticle.class);
                detail.putExtra("webURL",webHotURL);
                startActivity(detail);
            }
        });


        kbv= (KenBurnsView)findViewById(R.id.top_image);
        top_author=(TextView)findViewById(R.id.top_author);
        top_title=(TextView)findViewById(R.id.top_title);

        listNews=(RecyclerView)findViewById(R.id.list_news);
        listNews.setHasFixedSize(true);
        layoutManager=new LinearLayoutManager(this);
        listNews.setLayoutManager(layoutManager);

        //Intent

        if(getIntent() !=null)
        {
            source=getIntent().getStringExtra("source");

            if(!source.isEmpty())
            {
                loadNews(source,false);
            }
        }

    }

    private void loadNews(String source, boolean isRefreshed) {
        if(!isRefreshed)
        {
            dialog.show();

            mService.getNewestArticles(Common.getAPIUrl(source,Common.API_KEY))
                    .enqueue(new Callback<News>() {
                        @Override
                        public void onResponse(Call<News> call, Response<News> response) {
                            dialog.dismiss();
            //get first article
                            Picasso.get()
                                    .load(response.body().getArticles().get(0).getUrlToImage())
                                    .into(kbv);

             top_title.setText((response.body().getArticles().get(0).getTitle()));
             top_author.setText(response.body().getArticles().get(0).getAuthor());

             webHotURL=response.body().getArticles().get(0).getUrl();

             //load remaining news article
             List<Article>removeFirstItem =response.body().getArticles();
             //we have removed one item because its alreday shown on diagonal layout
             //we need to remove first one from the list and display rest of the the articles

             removeFirstItem.remove(0);
             adapter=new ListNewsAdapter(removeFirstItem,getBaseContext());
             adapter.notifyDataSetChanged();
             listNews.setAdapter(adapter);




                        }

                        @Override
                        public void onFailure(Call<News> call, Throwable t) {

                        }
                    });
        }
        else
        {
            dialog.show();

            mService.getNewestArticles(Common.getAPIUrl(source,Common.API_KEY))
                    .enqueue(new Callback<News>() {
                        @Override
                        public void onResponse(Call<News> call, Response<News> response) {
                            dialog.dismiss();
                            //get first article
                            Picasso.get()
                                    .load(response.body().getArticles().get(0).getUrlToImage())
                                    .into(kbv);

                            top_title.setText((response.body().getArticles().get(0).getTitle()));
                            top_author.setText(response.body().getArticles().get(0).getAuthor());

                            webHotURL=response.body().getArticles().get(0).getUrl();

                            //load remaining news article
                            List<Article>removeFirstItem =response.body().getArticles();
                            //we have removed one item because its alreday shown on diagonal layout
                            //we need to remove first one from the list and display rest of the the articles

                            removeFirstItem.remove(0);
                            adapter=new ListNewsAdapter(removeFirstItem,getBaseContext());
                            adapter.notifyDataSetChanged();
                            listNews.setAdapter(adapter);




                        }

                        @Override
                        public void onFailure(Call<News> call, Throwable t) {

                        }
                    });

            swipeRefreshLayout.setRefreshing(false);
        }
    }
}
