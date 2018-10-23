package org.woolrim.woolrim;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class CheckBottomFragment extends BottomSheetDialogFragment {
    public static final int MY_RECORD_DELETE_REQUEST = 0;
    public static final int MY_VOLUNTEER_SCORE_SUBMIT_REQUEST = 1;
    public static final int RECORDING_BACK_REQUEST = 2;
    public static final int MY_RECORD_SUBMIT_REQUEST = 3;

    private int fragmentRequestCode = 0;
    private String filePath,poetName, poemName;
    private int deleteItemPosition;
    private TextView leftTextView, rightTextView, checkTextView1, checkTextView2, waringTextView1, waringTextView2;

    public static CheckBottomFragment newInstance(Bundle bundle) {
        CheckBottomFragment checkBottomFragment = new CheckBottomFragment();
        checkBottomFragment.setArguments(bundle);
        return checkBottomFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        fragmentRequestCode = bundle.getInt("FragmentRequestCode", 0);
        if(fragmentRequestCode == RECORDING_BACK_REQUEST){
            filePath = bundle.getString("FilePath");
            if(filePath == null){
                Log.e("NULL","NULL");
            }
        }else if(fragmentRequestCode == MY_RECORD_DELETE_REQUEST){
            deleteItemPosition = bundle.getInt("ItemPosition");
            poemName = bundle.getString("ItemPoem");
            poetName = bundle.getString("ItemPoet");
        }
    }



    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_check,container,false);

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        init(view);
        setItems();

    }

    @Override
    public void onStart() {
        super.onStart();

    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroyView() {
        if(fragmentRequestCode == MY_RECORD_SUBMIT_REQUEST){
            getActivity().getSupportFragmentManager().popBackStack("MainFragment", FragmentManager.POP_BACK_STACK_INCLUSIVE);
        }
        super.onDestroyView();
    }

    private void init(View view) {
        leftTextView = view.findViewById(R.id.check_left_textview);
        rightTextView = view.findViewById(R.id.check_right_textview);
        checkTextView1 = view.findViewById(R.id.check_text_view1);
        checkTextView2 = view.findViewById(R.id.check_text_view2);
        waringTextView1 = view.findViewById(R.id.check_warning_text_view1);
        waringTextView2 = view.findViewById(R.id.check_warning_text_view2);
    }

    private void setItems() {
        switch (fragmentRequestCode) {
            case MY_RECORD_DELETE_REQUEST:
                leftTextView.setText("취소");
                leftTextView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dismiss();
                    }
                });
                rightTextView.setText("완료");
                rightTextView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        MyMenuFragment.myRecordFragment1.adapter.deleteItem(deleteItemPosition,null);
                        dismiss();
                    }
                });
                checkTextView1.setText("'"+poetName+" - "+poemName+"' 울림을 ");
                checkTextView2.setText("삭제하시겠습니까?");
                waringTextView1.setVisibility(View.GONE);
                waringTextView2.setVisibility(View.GONE);
                break;
            case MY_VOLUNTEER_SCORE_SUBMIT_REQUEST:
                leftTextView.setText("취소");
                leftTextView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dismiss();
                    }
                });
                rightTextView.setText("신청");
                checkTextView1.setText("봉사 점수를 신청하시겠습니까?");
                checkTextView2.setVisibility(View.GONE);
                waringTextView1.setText("※봉사 점수 획득 이후에는 본인의 울림을");
                waringTextView2.setText("자의로 삭제하실 수 없습니다.");
                break;
            case RECORDING_BACK_REQUEST:
                leftTextView.setText("취소");
                rightTextView.setText("나가기");
                leftTextView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dismiss();
                    }
                });
                rightTextView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        getActivity().getSupportFragmentManager().popBackStack();
                        dismiss();
                    }
                });
                checkTextView1.setText("지금 나가시면 울림이 저장되지 않습니다.");
                checkTextView2.setVisibility(View.GONE);
                waringTextView1.setVisibility(View.GONE);
                waringTextView2.setVisibility(View.GONE);
                break;
            case MY_RECORD_SUBMIT_REQUEST:
                leftTextView.setVisibility(View.INVISIBLE);
                rightTextView.setText("확인");
                rightTextView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dismiss();
                    }
                });
                checkTextView1.setText("울림 활동에 참여해 주셔서 감사합니다.");
                checkTextView2.setText("결과는 '마이울림'에서 확인 가능합니다.");
                waringTextView1.setText("※관리자가 음질, 장난 등 녹음 상태를 확인후에");
                waringTextView2.setText("업로드 결과를 알려드립니다.");
                break;
        }
    }
}