package com.widuu;

import android.os.Environment;
import android.widget.Toast;

import com.baidu.tts.auth.AuthInfo;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
//import com.facebook.react.bridge.Callback;
//import com.facebook.react.bridge.ReadableMap;

import com.baidu.tts.client.SpeechError;
import com.baidu.tts.client.SpeechSynthesizer;
import com.baidu.tts.client.SpeechSynthesizerListener;
import com.baidu.tts.client.TtsMode;
import com.facebook.react.bridge.ReadableMap;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;


public class SpeechModule extends ReactContextBaseJavaModule implements SpeechSynthesizerListener {

    //private final ReactApplicationContext reactContext;
    private static  String TAG = "SpeechModule";
    private SpeechSynthesizer mSpeechSynthesizer;
    private String dataDirPath;
    private static final String  DIR_NAME = "widuuTts";
    private static final String SPEECH_FEMALE_MODEL_NAME = "bd_etts_speech_female.dat";
    private static final String SPEECH_MALE_MODEL_NAME = "bd_etts_speech_male.dat";
    private static final String TEXT_MODEL_NAME = "bd_etts_text.dat";
    private static final String ENGLISH_SPEECH_FEMALE_MODEL_NAME = "bd_etts_speech_female_en.dat";
    private static final String ENGLISH_SPEECH_MALE_MODEL_NAME = "bd_etts_speech_male_en.dat";
    private static final String ENGLISH_TEXT_MODEL_NAME = "bd_etts_text_en.dat";

    public SpeechModule(ReactApplicationContext reactContext)  {
        super(reactContext);
        //this .reactContext = reactContext;
    }

    @Override
    public String getName() {
    return "SpeechModule";
  }

    @ReactMethod
    public void show(String message, int duration) {
        Toast.makeText(getReactApplicationContext(), message, duration).show();
    }

    @ReactMethod
    public void  getKey(String tag,Promise promise){
        String key = Util.getApiKey(getReactApplicationContext(),tag);
        if ( key != null ){
            promise.resolve(tag + " :" + key);
        }else{
            promise.reject("1", tag+ "no set value");
        }
    }

    @ReactMethod
    public void initialTts(ReadableMap params, Promise promise) {
        //getReactApplicationContext().getResources().getAssets()
        this.mSpeechSynthesizer = SpeechSynthesizer.getInstance();
        this.mSpeechSynthesizer.setContext(getReactApplicationContext());
        this.mSpeechSynthesizer.setSpeechSynthesizerListener(this);
        // 文本模型文件路径 (离线引擎使用)
        this.mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_TTS_TEXT_MODEL_FILE, dataDirPath + "/"
                + TEXT_MODEL_NAME);
        // 声学模型文件路径 (离线引擎使用)
        this.mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_TTS_SPEECH_MODEL_FILE, dataDirPath + "/"
                + SPEECH_FEMALE_MODEL_NAME);
        if( params.hasKey("appid") ) {
            this.mSpeechSynthesizer.setAppId(params.getString("appid"));
        }
        if( params.hasKey("appkey") && params.hasKey("secretkey") ){
            this.mSpeechSynthesizer.setApiKey(params.getString("appkey"),
                    params.getString("secretkey"));
        }
        int speaker = 0;
        if( params.hasKey("speaker") ){
            speaker = params.getInt("speaker");
        }
        // 发音人（在线引擎），可用参数为0,1,2,3。。。（服务器端会动态增加，各值含义参考文档，以文档说明为准。0--普通女声，1--普通男声，2--特别男声，3--情感男声。。。）
        this.mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_SPEAKER, String.valueOf(speaker));
        // 设置Mix模式的合成策略
        this.mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_MIX_MODE, SpeechSynthesizer.MIX_MODE_DEFAULT);


        // 初始化tts
        mSpeechSynthesizer.initTts(TtsMode.MIX);
    }

    @ReactMethod
    public void loadEnglish(Promise promise){
        int result = this.mSpeechSynthesizer.loadEnglishModel(dataDirPath + "/" + ENGLISH_TEXT_MODEL_NAME, dataDirPath
                        + "/" + ENGLISH_SPEECH_FEMALE_MODEL_NAME);
        if (result < 0) {
            promise.reject("1","error,please look up error code in doc or URL:http://yuyin.baidu.com/docs/tts/122  , ErrCode :"+result);
        }else{
            promise.resolve("load english file success");
        }
    }

    @ReactMethod
    public void  Speak(String text,Promise promise){
        if( Util.isEmpty(text) ){
            promise.reject("1","did not fill in the content to say");
        }
        int result = this.mSpeechSynthesizer.speak(text);
        if (result < 0) {
            promise.reject("1","error,please look up error code in doc or URL:http://yuyin.baidu.com/docs/tts/122  , ErrCode :"+result);
        }else{
            promise.resolve("speak success");
        }
    }

    @ReactMethod
    public void  isAuth(Promise promise){
        AuthInfo authInfo = this.mSpeechSynthesizer.auth(TtsMode.MIX);

        if (authInfo.isSuccess()) {
            promise.resolve("auth success");
        } else {
            String errorMsg = authInfo.getTtsError().getDetailMessage();
            promise.reject("0","auth failed errorMsg=" + errorMsg);
        }
    }

    @ReactMethod
    public void initEnv(){

        if (dataDirPath == null) {
            String sdcardPath = Environment.getExternalStorageDirectory().toString();
            dataDirPath = sdcardPath + "/" + DIR_NAME  ;
        }

        String existsFile = dataDirPath + "/" + SPEECH_FEMALE_MODEL_NAME;
        File file = new File(existsFile);
        if( !file.exists() ){
            makeDir(dataDirPath);
            copyFromAssetsToSdcard(false, SPEECH_FEMALE_MODEL_NAME, dataDirPath + "/" + SPEECH_FEMALE_MODEL_NAME);
            copyFromAssetsToSdcard(false, SPEECH_MALE_MODEL_NAME, dataDirPath + "/" + SPEECH_MALE_MODEL_NAME);
            copyFromAssetsToSdcard(false, TEXT_MODEL_NAME, dataDirPath + "/" + TEXT_MODEL_NAME);
            copyFromAssetsToSdcard(false, "english/" + ENGLISH_SPEECH_FEMALE_MODEL_NAME, dataDirPath + "/"
                    + ENGLISH_SPEECH_FEMALE_MODEL_NAME);
            copyFromAssetsToSdcard(false, "english/" + ENGLISH_SPEECH_MALE_MODEL_NAME, dataDirPath + "/"
                    + ENGLISH_SPEECH_MALE_MODEL_NAME);
            copyFromAssetsToSdcard(false, "english/" + ENGLISH_TEXT_MODEL_NAME, dataDirPath + "/"
                    + ENGLISH_TEXT_MODEL_NAME);
        }

    }

    private void copyFromAssetsToSdcard(boolean isCover, String source, String dest) {
        File file = new File(dest);
        if (isCover || (!isCover && !file.exists())) {
            InputStream is = null;
            FileOutputStream fos = null;
            try {
                is = getReactApplicationContext().getResources().getAssets().open(source);
                String path = dest;
                fos = new FileOutputStream(path);
                byte[] buffer = new byte[1024];
                int size = 0;
                while ((size = is.read(buffer, 0, 1024)) >= 0) {
                    fos.write(buffer, 0, size);
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (fos != null) {
                    try {
                        fos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                try {
                    if (is != null) {
                        is.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void makeDir(String dirPath) {
        File file = new File(dirPath);
        if (!file.exists()) {
            file.mkdirs();
        }
    }

    @Override
    public void onSynthesizeStart(String s) {

    }

    @Override
    public void onSynthesizeDataArrived(String s, byte[] bytes, int i) {

    }

    @Override
    public void onSynthesizeFinish(String s) {

    }

    @Override
    public void onSpeechStart(String s) {

    }

    @Override
    public void onSpeechProgressChanged(String s, int i) {

    }

    @Override
    public void onSpeechFinish(String s) {

    }

    @Override
    public void onError(String s, SpeechError speechError) {

    }
}