package com.inspur.playwork.common.sendmail.adapter;

import android.graphics.Bitmap;
import android.support.v4.util.ArrayMap;
import android.widget.ImageView;

import com.inspur.playwork.config.AppConfig;
import com.inspur.playwork.model.common.UserInfoBean;
import com.inspur.playwork.utils.PictureUtils;
import com.inspur.playwork.utils.loadpicture.BitmapCacheManager;

/**
 * Created by Bugcode on 2016/3/10.
 */
public class AdapterUtil {

    public static void setContactsAvatar(ImageView avatarView, UserInfoBean userInfo, ArrayMap<String, Long> avatarCache, BitmapCacheManager bitmapCacheManager) {
        if (avatarCache.containsKey(userInfo.id)) {
            Long avatarId = avatarCache.get(userInfo.id);
            if (avatarId >= userInfo.avatar) {
                userInfo.avatar = avatarId;
            } else {
                avatarCache.put(userInfo.id, userInfo.avatar);
            }
        } else {
            avatarCache.put(userInfo.id, userInfo.avatar);
        }

        String avatarPath = userInfo.getAvatarPath();
//        Bitmap avatarBitmap = bitmapCacheManager.getBitmapFromMemoryCache(avatarPath);
//        if (avatarBitmap != null) {
//            avatarView.setImageBitmap(avatarBitmap);
//        } else {
        if (userInfo.isAvatarFileExit()) {
            PictureUtils.loadAvatarBitmap(avatarPath, avatarView, null);
        } else {
            PictureUtils.downLoadAvatar(avatarPath, AppConfig.AVATAR_ROOT_PATH + userInfo.avatar, avatarView, null);
        }
//        }
    }
}
