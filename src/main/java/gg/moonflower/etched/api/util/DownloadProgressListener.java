package gg.moonflower.etched.api.util;

import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

public interface DownloadProgressListener {

    void progressStartRequest(Component component);

    void progressStartDownload(float size);

    void progressStagePercentage(int percentage);

    default void progressStage(float percentage) {
        this.progressStagePercentage((int) (Mth.clamp(percentage, 0.0F, 1.0F) * 100.0F));
    }

    void progressStartLoading();

    void onSuccess();

    void onFail();
}
