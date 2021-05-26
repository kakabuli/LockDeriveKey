package com.revolo.lock.adapter;

import android.view.View;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.revolo.lock.R;
import com.revolo.lock.bean.request.QuestionBeanReq;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class HelpQuestionListAdapter extends BaseQuickAdapter<QuestionBeanReq, BaseViewHolder> {

    public HelpQuestionListAdapter(int layoutResId, @Nullable List<QuestionBeanReq> data) {
        super(layoutResId, data);
    }

    @Override
    protected void convert(@NotNull BaseViewHolder baseViewHolder, QuestionBeanReq questionBeanReq) {
        if (questionBeanReq != null) {
            baseViewHolder.setText(R.id.tv_question_title, questionBeanReq.getQuestion());
            baseViewHolder.setText(R.id.tv_question_answer, questionBeanReq.getAnswer());
        }

        ConstraintLayout constraintLayout = baseViewHolder.getView(R.id.cl_answer);
        TextView textView = baseViewHolder.getView(R.id.tv_question_title);

        textView.setOnClickListener(v -> {
            if (constraintLayout.getVisibility() == View.GONE) {
                constraintLayout.setVisibility(View.VISIBLE);
            } else {
                constraintLayout.setVisibility(View.GONE);
            }
        });
    }
}
