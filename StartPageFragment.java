package oleg.lukianenko.example.startpage;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.KeyEvent;
import android.view.View;

import com.facebook.CallbackManager;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import oleg.lukianenko.example.R;
import oleg.lukianenko.example.main.authorization.AuthorizationActivity;
import oleg.lukianenko.example.main.authorization.authorization_page.AuthorizationFragment_;
import oleg.lukianenko.example.main.authorization.create_profile_page.CreateProfileFragment_;
import oleg.lukianenko.example.main.authorization.linked_in_auth.LinkedInAuthFragment_;
import oleg.lukianenko.example.mvp.classes.BaseFragment;
import oleg.lukianenko.example.util.LogUtil;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;

import java.io.IOException;

/*
 * Created by Oleg Lukianenko
 */

@EFragment(R.layout.fragment_start_page)
public class StartPageFragment extends BaseFragment {

    private static final int REQUEST_CODE_GOOGLE_AUTHORIZATION = 1001;

    private CallbackManager mCallbackManager;

    private GoogleApiClient mGoogleApiClient;

    @Bean
    protected StartPagePresenter presenter;

    @Override
    protected void initPresenter() {
        presenter.applyView(this);
    }

    @Override
    public void start() {
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        presenter.tryAuthorization();
        mCallbackManager = CallbackManager.Factory.create();
        onBackPressed();
    }

    private void onBackPressed() {
        if (getView() != null) {
            getView().setOnKeyListener(new View.OnKeyListener() {
                @Override
                public boolean onKey(View v, int keyCode, KeyEvent event) {
                    if (keyCode == KeyEvent.KEYCODE_BACK) {
                        getActivity().finish();
                    }
                    return true;
                }
            });
        }
    }

    //Button to go to the registration fragment
    @Click(R.id.btn_sign_in)
    protected void signIn() {
        ((AuthorizationActivity) getActivity()).showFragment(CreateProfileFragment_.builder().build());
    }

    //Initialize Facebook login button
    @Click(R.id.btn_sign_in_facebook)
    protected void signInFacebook() {
        LoginButton loginButton = new LoginButton(getContext());
        loginButton.setFragment(this);
        loginButton.setReadPermissions("email");
        loginButton.registerCallback(mCallbackManager, presenter.getFacebookCallback());
        loginButton.callOnClick();
    }

    //Function login with help LinkedIn
    @Click(R.id.btn_sign_in_linkedIn)
    protected void signInLinkedIn() {
        if (!hasInternetConnection()) {
            showConnectionError();
        } else {
            ((AuthorizationActivity) getActivity()).showFragment(LinkedInAuthFragment_.builder().build());
        }
    }

    //Button to go to the authorization fragment
    @Click(R.id.btn_log_in)
    protected void logIn() {
        ((AuthorizationActivity) getActivity()).showFragment(AuthorizationFragment_.builder().build());
    }

    //The function for redirecting to the StartPageModel and receiving the response to the login request
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        mCallbackManager.onActivityResult(requestCode, resultCode, data);
    }

    //Check internet
    public void checkInternet() {
        if (!hasInternetConnection()) {
            showConnectionError();
        }
    }

    public void openUserProfile() {
        ((AuthorizationActivity) getActivity()).goToMainActivity();
    }

    // Handles click on the Google authorization button.
    @Click(R.id.btn_sign_in_google)
    protected void signGoogle() {
        if (!hasInternetConnection()) {
            showConnectionError();
        } else {
            GoogleSignInOptions gso = new GoogleSignInOptions
                    .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestEmail()
                    .build();

            // Creates GoogleApiClient
            if (mGoogleApiClient == null) {
                mGoogleApiClient = new GoogleApiClient
                        .Builder(getContext())
                        .enableAutoManage(getActivity(), new GoogleApiClient.OnConnectionFailedListener() {
                            @Override
                            public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
                                LogUtil.errorLog(LogUtil.TAG_GOOGLE_AUTHORIZATION, connectionResult.getErrorMessage());
                            }
                        })
                        .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                        .build();
            }

            // Creates intent to GoogleSignIn API
            Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
            getActivity().startActivityForResult(signInIntent, REQUEST_CODE_GOOGLE_AUTHORIZATION);
        }
    }

    //The function for redirecting to the StartPageModel and receiving the response to the Google+ login request.
    public void handleGoolgPlusAuthResult(Intent data) {
        if (!hasInternetConnection()) {
            showConnectionError();
        } else {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(result);
        }
    }

    private void handleSignInResult(GoogleSignInResult result) {
        if (result.isSuccess()) {
            // Signed in successfully, show authenticated UI.
            final GoogleSignInAccount acct = result.getSignInAccount();

            // Gets token from Google account.
            new Thread() {
                @Override
                public void run() {
                    try {
                        String test = GoogleAuthUtil.getToken(getContext(), acct.getAccount(), "oauth2:");
                        presenter.loginGooglePlus(test);
                    } catch (IOException exception) {
                        LogUtil.errorLog(LogUtil.TAG_GOOGLE_AUTHORIZATION,
                                "IOException " + exception.getMessage());
                        showToast(getString(R.string.start_page_google_auth_error)
                                + " " + exception.getMessage());
                    } catch (UserRecoverableAuthException userRecoverableException) {
                        LogUtil.errorLog(LogUtil.TAG_GOOGLE_AUTHORIZATION,
                                "PermissionException " + userRecoverableException.getMessage());
                        showToast(getString(R.string.start_page_google_auth_error)
                                + " " + userRecoverableException.getMessage());
                    } catch (GoogleAuthException googleException) {
                        LogUtil.errorLog(LogUtil.TAG_GOOGLE_AUTHORIZATION,
                                "GoogleAuthException " + googleException.getMessage());
                        showToast(getString(R.string.start_page_google_auth_error)
                                + " " + googleException.getMessage());
                    }
                }
            }.start();
        } else {
            LogUtil.errorLog(LogUtil.TAG_GOOGLE_AUTHORIZATION, "failed authorization");
            showToast(getString(R.string.start_page_google_auth_error));
        }
    }
}