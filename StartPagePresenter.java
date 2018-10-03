package oleg.lukianenko.example.startpage;

import com.facebook.FacebookCallback;
import oleg.lukianenko.example.mvp.classes.BasePresenter;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;

/*
 * Created by Oleg Lukianenko
 */

@EBean
class StartPagePresenter extends BasePresenter<StartPageFragment> {

    @Bean
    StartPageModel model;

    @Override
    protected void initModel() {
        model.applyPresenter(this);
    }

    FacebookCallback getFacebookCallback() {
        return model.getFaceBookCallback();
    }

    void checkInternet() {
        getView().checkInternet();
    }

    void openProfile() {
        getView().openUserProfile();
    }

    void tryAuthorization() {
        model.tryAuthorization();
    }

    void loginGooglePlus(String token) {
        model.loginGooglePlus(token);
    }
}
