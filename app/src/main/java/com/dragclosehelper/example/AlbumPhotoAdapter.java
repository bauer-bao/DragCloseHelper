package com.dragclosehelper.example;

import android.widget.ImageView;

import androidx.annotation.Nullable;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;

import java.util.List;

/**
 * @author bauer on 2019-09-12.
 */
public class AlbumPhotoAdapter extends BaseQuickAdapter<Integer, BaseViewHolder> {
    public AlbumPhotoAdapter(@Nullable List<Integer> data) {
        super(R.layout.rv_item_photos, data);
    }

    @Override
    protected void convert(BaseViewHolder helper, Integer item) {
        ImageView imageView = helper.getView(R.id.rv_item_photo_iv);
        imageView.setImageResource(item);
    }
}
