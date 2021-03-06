package ng.com.tinweb.www.languagetranslator.data.translation;

import android.content.Context;
import android.util.Log;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import ng.com.tinweb.www.languagetranslator.LanguageTranslator;
import ng.com.tinweb.www.languagetranslator.data.TranslatorAPI;
import ng.com.tinweb.www.languagetranslator.data.language.Language;
import retrofit2.Call;
import retrofit2.Callback;

import static ng.com.tinweb.www.languagetranslator.data.TranslatorAPI.TranslationService.retrofit;

/**
 * Created by kamiye on 13/09/2016.
 */
public class Translation implements Callback<JsonObject> {

    private TranslationDataStore dataStore;
    private ApiCallback apiCallback;

    public Translation() {
        initialiseDataStore();
    }

    public boolean isExisting(String lang, String input) {
        return dataStore.isSaved(lang, input);
    }

    public void save(String lang, String text, String translation) {
        dataStore.saveTranslation(lang, text, translation);
    }

    public void get(String lang, String text, ApiCallback callback) {
        if (isExisting(lang, text)) {
            String translation = getFromLocalStorage(lang, text);
            callback.onSuccess(translation);
        } else {
            getFromAPI(lang, text, callback);
        }
    }

    public String getFromLocalStorage(String lang, String text) {
        return dataStore.getTranslation(lang, text);
    }

    public void getFromAPI(String lang, String text, final ApiCallback callback) {

        if (apiCallback == null) {
            apiCallback = callback;
        }
        TranslatorAPI.TranslationService translationService =
                retrofit.create(TranslatorAPI.TranslationService.class);
        Call<JsonObject> jsonObjectCall =
                translationService.getTranslation(TranslatorAPI.API_KEY, text, lang);

        jsonObjectCall.enqueue(this);
    }

    public List<String> getLanguagesByList() {
        HashMap<String, String> languagesMap = Language.getLanguagesFromLocalStorage();
        if (languagesMap != null) {
            List<String> languages = new ArrayList<>(languagesMap.values());
            Collections.sort(languages);
            return languages;
        }
        return null;
    }

    public HashMap<String, String> getLanguagesByMap() {
        return Language.getLanguagesFromLocalStorage();
    }

    @Override
    public void onResponse(Call<JsonObject> call, retrofit2.Response<JsonObject> response) {
        JsonElement array = response.body().get("text");
        String translation = array.getAsString();
        apiCallback.onSuccess(translation);
    }

    @Override
    public void onFailure(Call<JsonObject> call, Throwable t) {
        Log.e("ERROR", "An error occurred: " + t.getLocalizedMessage());
        apiCallback.onError();
    }

    private void initialiseDataStore() {
        Context context = LanguageTranslator.getContext();
        dataStore = new TranslationsDbHelper(context);
    }

    public interface ApiCallback {
        void onSuccess(String translation);

        void onError();
    }

}
