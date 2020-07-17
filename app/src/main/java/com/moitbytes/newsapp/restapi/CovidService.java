package com.moitbytes.newsapp.restapi;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Query;

public interface CovidService
{
    @Headers({
            "x-rapidapi-host: covid-19-data.p.rapidapi.com",
            "x-rapidapi-key: 98fc64666bmsh212d8c4140915d4p11b6a7jsn51dab644fa09 "
    })
    @GET("country/code")
    Call<String> getFeed(@Query("format") String format, @Query("code") String code);
}
