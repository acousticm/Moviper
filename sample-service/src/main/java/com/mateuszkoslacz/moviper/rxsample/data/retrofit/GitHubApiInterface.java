package com.mateuszkoslacz.moviper.rxsample.data.retrofit;

import com.mateuszkoslacz.moviper.rxsample.viper.entity.User;

import java.util.List;

import retrofit2.http.GET;
import retrofit2.http.Path;
import io.reactivex.Observable;

public interface GitHubApiInterface {

    String GITHUB_API_URL = "https://api.github.com";

    @GET("/users")
    Observable<List<User>> getUsers();

    @GET("/users/{username}")
    Observable<User> getUserForUsername(@Path("username") String username);
}
