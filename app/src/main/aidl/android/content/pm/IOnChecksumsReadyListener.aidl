package android.content.pm;
import android.content.pm.ApkChecksum;

oneway interface IOnChecksumsReadyListener {
    void onChecksumsReady(in List<ApkChecksum> checksums);
}