package org.woolrim.woolrim;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.Toast;

import com.naver.speech.clientapi.SpeechRecognitionResult;

import org.woolrim.woolrim.Utils.AudioWriterPcm;
import org.woolrim.woolrim.Utils.DialogDismissListener;
import org.woolrim.woolrim.Utils.NaverSpeechRecognizer;

import java.lang.ref.WeakReference;
import java.util.List;

public class VoiceRecognitionFragment extends DialogFragment {

    private ImageView voiceRecognitionBgIv, voiceRecognitionIconIv;
    private DialogDismissListener mResultListener;

    private Animation itemRotate;

    private static final String CILENT_ID = "n5zrl0x602";
    private static final String CILENT_SECRET = "Zf6Ob1Su5OoU3Qi6dVYer6wcxoREZcay9R4DXbuy";

    private RecognitionHandler handler;
    private NaverSpeechRecognizer naverRecognizer;

    private AudioWriterPcm writer;

    private String mResult;


    public static VoiceRecognitionFragment newInstance(Bundle bundle) {
        VoiceRecognitionFragment testDialogFragment = new VoiceRecognitionFragment();
        testDialogFragment.setArguments(bundle);
        return testDialogFragment;
    }

//    public abstract static class DialogDismissListener implements DialogInterface.OnDismissListener {
//        public String key;
//
//        public void findSearchKey(@Nullable String key) {
//            this.key = key;
//        }
//    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        handler = new RecognitionHandler(this);
        naverRecognizer = new NaverSpeechRecognizer(context,handler,CILENT_ID);
        Log.d("InTime","OnAttach");
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Log.d("InTime","onCreateDialog");

        Dialog dialog = super.onCreateDialog(savedInstanceState);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View view = getActivity().getLayoutInflater().inflate(R.layout.fragment_voice_recognition
                , null);

        init(view);

        builder.setView(view);

        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);

        naverRecognizer.getSpeechRecognizer().initialize();

        return builder.create();
    }


    @Override
    public void onStart() {
        super.onStart();
        Log.d("InTime","onStart");

        Window window = getDialog().getWindow();

        assert window != null;
        window.setGravity(Gravity.TOP);

        WindowManager.LayoutParams params = window.getAttributes();
        params.y = dpToPx(74f); // above source view

        window.setAttributes(params);
        window.setBackgroundDrawableResource(android.R.color.transparent);

        AlertDialog alertDialog = (AlertDialog) getDialog();
        alertDialog.getButton(Dialog.BUTTON_POSITIVE).setEnabled(false);
        alertDialog.getButton(Dialog.BUTTON_NEGATIVE).setEnabled(false);
        alertDialog.getButton(Dialog.BUTTON_NEUTRAL).setEnabled(false);

        alertDialog.setOnDismissListener(mResultListener);

        if (!MainFragment.isRecognitioning) {
            voiceRecognitionBgIv.startAnimation(itemRotate);
            MainFragment.isRecognitioning = true;
        }
        if(!naverRecognizer.getSpeechRecognizer().isRunning()) {
            // Start button is pushed when SpeechRecognizer's state is inactive.
            // Run SpeechRecongizer by calling recognize().
            mResult = "";
            naverRecognizer.recognize();
        } else {
            naverRecognizer.getSpeechRecognizer().stop();
        }
        voiceRecognitionIconIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                PoemListFragment poemListFragment = PoemListFragment.newInstance(new Bundle());
//                getActivity().getSupportFragmentManager()
//                        .beginTransaction()
//                        .replace(R.id.container, poemListFragment).commit();
            }
        });


    }

    @Override
    public void onResume() {
        super.onResume();
        MainFragment.voiceRecognitionTv1.setVisibility(View.INVISIBLE);
        MainFragment.voiceRecognitionTv2.setVisibility(View.INVISIBLE);
    }


    @Override
    public void onCancel(DialogInterface dialog) {
        Log.d("Time", "onCancel");
        mResultListener.onDismissed(mResult);
        super.onCancel(dialog);
        dismiss();
    }

    @Override
    public void onPause() {
        Log.d("Time", "onPause");
        super.onPause();
        dismiss();
    }

    @Override
    public void onDestroyView() {
        Log.d("Time", "onDestroyView");
        voiceRecognitionBgIv.clearAnimation();
        MainFragment.isRecognitioning = false;
        naverRecognizer.getSpeechRecognizer().release();

        super.onDestroyView();
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        Log.d("Time","onDismiss");
        super.onDismiss(dialog);
    }

    private void init(View view) {
        voiceRecognitionBgIv = view.findViewById(R.id.voice_recognition_icon_background);
        voiceRecognitionIconIv = view.findViewById(R.id.voice_recognition_icon);
        itemRotate = AnimationUtils.loadAnimation(getContext(), R.anim.item_rotate);
    }

    public int dpToPx(float valueInDp) {
        DisplayMetrics metrics = getActivity().getResources().getDisplayMetrics();
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, valueInDp, metrics);
    }

    public void setDismissListener(DialogDismissListener listener) {
        mResultListener = listener;

    }

    private void handleMessage(Message msg, Context context) {
        switch (msg.what) {
            case R.id.clientReady:
                // Now an user can speak.
                writer = new AudioWriterPcm(
                        Environment.getExternalStorageDirectory().getAbsolutePath() + "/NaverSpeechTest");
                writer.open("Test");
                break;

            case R.id.audioRecording:
                writer.write((short[]) msg.obj);
                break;

            case R.id.partialResult:
                // Extract obj property typed with String.
                mResult = (String) (msg.obj);
                break;

            case R.id.finalResult:
                // Extract obj property typed with String array.
                // The first element is recognition result for speech.
                SpeechRecognitionResult speechRecognitionResult = (SpeechRecognitionResult) msg.obj;
                List<String> results = speechRecognitionResult.getResults();
                mResult = results.get(0).trim();
                Toast.makeText(context, mResult, Toast.LENGTH_SHORT).show();
                mResultListener.onDismissed(mResult);
                dismiss();
                break;

            case R.id.recognitionError:
                if (writer != null) {
                    writer.close();
                }
                mResult = "";
                mResultListener.onDismissed(mResult);
                dismiss();
                break;

            case R.id.clientInactive:
                if (writer != null) {
                    writer.close();
                }
                break;
        }
    }

    static class RecognitionHandler extends Handler {
        private final WeakReference<VoiceRecognitionFragment> voiceRecognitionFragment;

        RecognitionHandler(VoiceRecognitionFragment voiceRecognitionFragment) {
            this.voiceRecognitionFragment = new WeakReference<>(voiceRecognitionFragment);
        }

        @Override
        public void handleMessage(Message msg) {
            VoiceRecognitionFragment activity = voiceRecognitionFragment.get();
            if (activity != null) {
                activity.handleMessage(msg,activity.getContext());
            }
        }
    }

}
