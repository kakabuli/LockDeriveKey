package com.revolo.lock.ui.mine;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.revolo.lock.App;
import com.revolo.lock.R;
import com.revolo.lock.adapter.HelpQuestionListAdapter;
import com.revolo.lock.base.BaseActivity;
import com.revolo.lock.bean.request.QuestionBeanReq;
import com.revolo.lock.bean.respone.QuestionBeanRsp;
import com.revolo.lock.net.HttpRequest;
import com.revolo.lock.net.ObservableDecorator;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

public class HelpActivity extends BaseActivity {

    private RecyclerView mQuestionList;
    private HelpQuestionListAdapter mHelpQuestionListAdapter;
    private List<QuestionBeanReq> mQuestionBeanReqs = new ArrayList<>();

    @Override
    public void initData(@Nullable Bundle bundle) {

    }

    @Override
    public int bindLayout() {
        return R.layout.activity_help;
    }

    @Override
    public void initView(@Nullable Bundle savedInstanceState, @Nullable View contentView) {
        useCommonTitleBar(getString(R.string.FAQ));

        mQuestionList = findViewById(R.id.rv_question_list);
        mHelpQuestionListAdapter = new HelpQuestionListAdapter(R.layout.item_help_question_list, mQuestionBeanReqs);
        mQuestionList.getItemAnimator().setChangeDuration(300);
        mQuestionList.getItemAnimator().setMoveDuration(300);
        mQuestionList.setLayoutManager(new LinearLayoutManager(this));
        mQuestionList.setAdapter(mHelpQuestionListAdapter);
    }

    @Override
    public void doBusiness() {
        Observable<QuestionBeanRsp> observable = HttpRequest.getInstance().faqList(App.getInstance().getUserBean().getToken(), 3);
        ObservableDecorator.decorate(observable).safeSubscribe(new Observer<QuestionBeanRsp>() {
            @Override
            public void onSubscribe(@NonNull Disposable d) {

            }

            @Override
            public void onNext(@NonNull QuestionBeanRsp questionBeanRsp) {
                if (questionBeanRsp != null && questionBeanRsp.getCode().equals("200")) {
                    List<QuestionBeanReq> data = questionBeanRsp.getData();
                    faqListResponse(data);
                }
            }

            @Override
            public void onError(@NonNull Throwable e) {

            }

            @Override
            public void onComplete() {

            }
        });
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            finish();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
    private void faqListResponse(List<QuestionBeanReq> questionBeanReqs) {

        if (questionBeanReqs != null && questionBeanReqs.size() > 0) {
            mQuestionBeanReqs.clear();
            mQuestionBeanReqs.addAll(questionBeanReqs);
            mHelpQuestionListAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onDebouncingClick(@NonNull View view) {

    }
}
