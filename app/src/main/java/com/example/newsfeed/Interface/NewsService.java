package com.example.newsfeed.Interface;

import com.example.newsfeed.Common.Common;
import com.example.newsfeed.Model.News;
import com.example.newsfeed.Model.WebSite;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Url;

public interface NewsService {
    @GET("v2/sources?apiKey="+ Common.API_KEY)
    Call<WebSite> getSources();


    @GET
    Call<News> getNewestArticles(@Url String url);
}

