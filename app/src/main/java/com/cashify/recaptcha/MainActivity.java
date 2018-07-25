package com.cashify.recaptcha;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.safetynet.SafetyNet;
import com.google.android.gms.safetynet.SafetyNetApi;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.POST;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String REMOTE_IP = "";
    private String TAG = "RCD";
    private String API_SITE_KEY = "6LfMN1gUAAAAAIscu7a2v1GFreA8grKqiqxwlTkF";
    private String API_SECRET_KEY = "6LfMN1gUAAAAAPpc5aMe35QkaAYds0wCQpZqmSeL";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.btn).setOnClickListener(this);
    }

    public void onClick(View view) {
        for (int i = 0; i < 10; i++)
            checkReCaptcha(view);
    }

    private void checkReCaptcha(View v) {
        OnSuccessListener<SafetyNetApi.RecaptchaTokenResponse> onSuccessListener = new OnSuccessListener<SafetyNetApi.RecaptchaTokenResponse>() {
            @Override
            public void onSuccess(SafetyNetApi.RecaptchaTokenResponse response) {
                String userResponseToken = response.getTokenResult();
                if (!userResponseToken.isEmpty()) {
                    siteVerifyRequest(userResponseToken);
                }
            }
        };
        OnFailureListener onFailureListener = new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if (e instanceof ApiException) {

                    ApiException apiException = (ApiException) e;
                    int statusCode = apiException.getStatusCode();
                    Log.d(TAG, "Error: " + CommonStatusCodes.getStatusCodeString(statusCode));
                } else {
                    Log.d(TAG, "Error: " + e.getMessage());
                }
            }
        };
        SafetyNet.getClient(this).verifyWithRecaptcha(API_SITE_KEY)
                .addOnSuccessListener(this, onSuccessListener)
                .addOnFailureListener(this, onFailureListener);
    }

    private void siteVerifyRequest(String tokenResult) {

        VerifyData data = new VerifyData();
        data.response = tokenResult;
        data.secret = API_SECRET_KEY;
        data.remoteip = REMOTE_IP;
        Gson gson = new GsonBuilder()
                .setLenient()
                .create();
        Retrofit retrofit = new Retrofit.Builder().baseUrl("https://www.google.com/")
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

        GitHubService service = retrofit.create(GitHubService.class);
        Call<VerifyResponse> verify = service.verify(data);
        Callback<VerifyResponse> callback = new Callback<VerifyResponse>() {
            @Override
            public void onResponse(@NonNull Call<VerifyResponse> call, @NonNull Response<VerifyResponse> response) {
                Log.e(TAG, "You are no a robot");
            }

            @Override
            public void onFailure(@NonNull Call<VerifyResponse> call, Throwable t) {
                Log.e(TAG, "fail: " + t.getMessage());
            }
        };
        verify.enqueue(callback);


    }

    public interface GitHubService {
        @POST("recaptcha/api/siteverify")
        Call<VerifyResponse> verify(@Body VerifyData user);
    }

    class VerifyResponse extends APIResponse {

    }
}
