package oleg.lukianenko.example.startpage;

import android.content.Context;
import android.support.annotation.NonNull;
import com.facebook.AccessToken;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginResult;
import com.google.firebase.iid.FirebaseInstanceId;
import oleg.lukianenko.example.R;
import oleg.lukianenko.example.mvp.classes.BaseModel;
import oleg.lukianenko.example.retrofit_util.ApiInterface;
import oleg.lukianenko.example.retrofit_util.request_body.authorization.UserLoginSocial;
import oleg.lukianenko.example.retrofit_util.response.authorization.ResponsedFromSocial;
import org.androidannotations.annotations.EBean;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/*
 * Created by Oleg Lukianenko
 */

@EBean
class StartPageModel extends BaseModel<StartPagePresenter> {

    StartPageModel(Context context) {
        super(context);
    }

    private FacebookCallback<LoginResult> mFacebookCallback = new FacebookCallback<LoginResult>() {
        @Override
        public void onSuccess(LoginResult loginResult) {
            loginFacebook(AccessToken.getCurrentAccessToken().getToken());
        }

        @Override
        public void onCancel() {
        }

        @Override
        public void onError(FacebookException error) {
            getPresenter().checkInternet();
        }
    };

    //Get callback from Facebook
    FacebookCallback getFaceBookCallback() {
        return mFacebookCallback;
    }

    //Request for a server to enter through facebook
    private void loginFacebook(String token) {
        UserLoginSocial userLoginFacebook = new UserLoginSocial(token, FirebaseInstanceId.getInstance().getToken());
        ApiInterface apiService = getRetrofit().create(ApiInterface.class);
        Call<ResponsedFromSocial> call = apiService.loginFacebook(userLoginFacebook);
        call.enqueue(new Callback<ResponsedFromSocial>() {
            @Override
            public void onResponse(@NonNull Call<ResponsedFromSocial> call, @NonNull Response<ResponsedFromSocial> response) {
                ResponsedFromSocial responsedFromSocial = response.body();
                if (responsedFromSocial != null) {
                    if (Integer.parseInt(responsedFromSocial.status) == 200) {
                        //if successful log in
                        saveToken(responsedFromSocial.token);
                        getPresenter().openProfile();
                    } else {
                        // if failure log in
                        getPresenter().showToast(responsedFromSocial.message);
                    }
                } else {
                    getPresenter().showToast(getResources().getString(R.string.trouble));
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponsedFromSocial> call, @NonNull Throwable t) {
                t.printStackTrace();
            }

        });
    }

    void tryAuthorization() {
        if (!getSharedPreference().getString(getResources().getString(R.string.shared_preference_user_token), "").isEmpty()) {
            getPresenter().openProfile();
        }
    }

    //Login using Google+
    void loginGooglePlus(String token) {
        UserLoginSocial userLoginGoogle = new UserLoginSocial(token, FirebaseInstanceId.getInstance().getToken());
        ApiInterface apiService = getRetrofit().create(ApiInterface.class);
        Call<ResponsedFromSocial> call = apiService.loginGooglePlus(userLoginGoogle);
        call.enqueue(new Callback<ResponsedFromSocial>() {
            @Override
            public void onResponse(@NonNull Call<ResponsedFromSocial> call, @NonNull Response<ResponsedFromSocial> response) {
                ResponsedFromSocial responsed = response.body();
                if (responsed != null) {
                    if (Integer.parseInt(responsed.status) == 200) {
                        //if successful log in
                        saveToken(responsed.token);
                        getPresenter().openProfile();
                    } else {
                        // if failure log in
                        getPresenter().showToast(responsed.message);
                    }
                } else {
                    getPresenter().showToast(getResources().getString(R.string.trouble));
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponsedFromSocial> call, @NonNull Throwable t) {
                t.printStackTrace();
            }

        });
    }

}
