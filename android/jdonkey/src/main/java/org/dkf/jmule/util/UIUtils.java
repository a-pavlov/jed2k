/*
 * Created by Angel Leon (@gubatron), Alden Torres (aldenml)
 * Copyright (c) 2011-2015, FrostWire(R). All rights reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.dkf.jmule.util;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.text.Html;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;

import androidx.core.content.FileProvider;

import org.apache.commons.io.FilenameUtils;
import org.dkf.jmule.ConfigurationManager;
import org.dkf.jmule.Constants;
import org.dkf.jmule.ED2KService;
import org.dkf.jmule.MimeDetector;
import org.dkf.jmule.BuildConfig;
import org.dkf.jmule.R;
import org.dkf.jmule.activities.MainActivity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

/**
 * @author gubatron
 * @author aldenml
 */
public final class UIUtils {

    private static final Logger LOG = LoggerFactory.getLogger(UIUtils.class);

    /**
     * Localizable Number Format constant for the current default locale.
     */
    private static NumberFormat NUMBER_FORMAT0; // localized "#,##0"

    private static final String[] BYTE_UNITS = new String[]{"b", "KB", "Mb", "Gb", "Tb"};

    public static final String GENERAL_UNIT_KBPSEC = "KB/s";

    static {
        NUMBER_FORMAT0 = NumberFormat.getNumberInstance(Locale.getDefault());
        NUMBER_FORMAT0.setMaximumFractionDigits(0);
        NUMBER_FORMAT0.setMinimumFractionDigits(0);
        NUMBER_FORMAT0.setGroupingUsed(true);
    }

    public static void showToastMessage(Context context, String message, int duration, int gravity, int xOffset, int yOffset) {
        if (context != null && message != null) {
            Toast toast = Toast.makeText(context, message, duration);
            if (gravity != (Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM)) {
                toast.setGravity(gravity, xOffset, yOffset);
            }
            toast.show();
        }
    }


    public static void sendShutdownIntent(Context ctx) {
        Intent i = new Intent(ctx, MainActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        i.putExtra("shutdown-" + ConfigurationManager.instance().getUUIDString(), true);
        ctx.startActivity(i);
    }

    public static void showToastMessage(Context context, String message, int duration) {
        showToastMessage(context, message, duration, Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM, 0, 0);
    }

    public static void showShortMessage(Context context, String message) {
        showToastMessage(context, message, Toast.LENGTH_SHORT);
    }

    public static void showLongMessage(Context context, String message) {
        showToastMessage(context, message, Toast.LENGTH_LONG);
    }

    public static void showShortMessage(Context context, int resId) {
        showShortMessage(context, context.getString(resId));
    }

    public static void showLongMessage(Context context, int resId) {
        showLongMessage(context, context.getString(resId));
    }

    public static void showShortMessage(Context context, int resId, Object... formatArgs) {
        showShortMessage(context, context.getString(resId, formatArgs));
    }

    public static Dialog showYesNoDialog(Context context, int messageId, int titleId, OnClickListener positiveListener) {
        return showYesNoDialog(context, messageId, titleId, positiveListener, new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
    }

    public static void showYesNoDialog(Context context, int iconId, String message, int titleId, OnClickListener positiveListener) {
        showYesNoDialog(context, iconId, message, titleId, positiveListener, new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
    }

    public static Dialog showYesNoDialog(Context context, int messageId, int titleId, OnClickListener positiveListener, OnClickListener negativeListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(messageId).setTitle(titleId).setCancelable(false).setPositiveButton(android.R.string.yes, positiveListener).setNegativeButton(android.R.string.no, negativeListener);
        Dialog dialog = builder.create();
        dialog.show();
        return dialog;
    }

    public static Dialog showInformationDialog(Context context, int messageId, int titleId, boolean hideTitle, final OnClickListener positiveListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(messageId);

        if (!hideTitle) {
            builder.setTitle(titleId);
        }

        builder.setPositiveButton(context.getString(android.R.string.ok), new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (positiveListener != null) {
                    try {
                        positiveListener.onClick(dialog, which);
                    } catch (Exception e) {}
                }
                dialog.dismiss();
            }
        });
        Dialog dialog = builder.create();
        if (hideTitle) {
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        }
        dialog.show();
        return dialog;
    }

    public static void showYesNoDialog(Context context, int iconId, String message, int titleId, OnClickListener positiveListener, OnClickListener negativeListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setIcon(iconId).setMessage(message).setTitle(titleId).setCancelable(false).setPositiveButton(android.R.string.yes, positiveListener).setNegativeButton(android.R.string.no, negativeListener);
        builder.create().show();
    }

    public static void showYesNoDialog(Context context,
                                       int iconId,
                                       String message,
                                       int titleId,
                                       List<String> bullets,
                                       OnClickListener positiveListener,
                                       OnClickListener negativeListener,
                                       AdapterView.OnItemClickListener bulletsClickListener) {
        if (bullets == null || bullets.isEmpty()) {
            LOG.warn("showYesNoDialog() - aborting bullet dialog, no bullets to show.");
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        // show all bullets pre-checked, prepend
        for (int i=0; i < bullets.size(); i++) {
            bullets.set(i, String.valueOf(Html.fromHtml("&#8226; " + bullets.get(i))));
        }

        ArrayAdapter bulletsAdapter;
        bulletsAdapter = new ArrayAdapter(context,
                R.layout.dialog_update_bullet,
                R.id.dialog_update_bullets_checked_text_view,
                bullets);

        ListView bulletsListView = new ListView(context);
        bulletsListView.setAdapter(bulletsAdapter);
        bulletsListView.setOnItemClickListener(bulletsClickListener);

        builder.setIcon(iconId).
                setTitle(titleId).
                setMessage(message).
                setView(bulletsListView).
                setCancelable(false).
                setPositiveButton(android.R.string.yes, positiveListener).
                setNegativeButton(android.R.string.no, negativeListener).
                create().
                show();
    }

    public static void showOkCancelDialog(Context context, View view, int titleId, OnClickListener okListener, OnClickListener cancelListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(view).setTitle(titleId).setPositiveButton(android.R.string.ok, okListener).setNegativeButton(android.R.string.cancel, cancelListener);
        builder.create().show();
    }

    public static void showOkCancelDialog(Context context, View view, int titleId, OnClickListener okListener) {
        showOkCancelDialog(context, view, titleId, okListener, new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
    }

    public static String getBytesInHuman(long size) {
        int i;
        float sizeFloat = (float) size;
        for (i = 0; sizeFloat > 1024; i++) {
            sizeFloat /= 1024f;
        }
        return String.format(Locale.US, "%.2f %s", sizeFloat, BYTE_UNITS[i]);
    }

    /**
     * Converts an rate into a human readable and localized KB/s speed.
     */
    public static String rate2speed(double rate) {
        return NUMBER_FORMAT0.format(rate) + " " + GENERAL_UNIT_KBPSEC;
    }

    public static String getFileTypeAsString(Resources resources, byte fileType) {
        switch (fileType) {
            case Constants.FILE_TYPE_APPLICATIONS:
                return resources.getString(R.string.applications);
            case Constants.FILE_TYPE_AUDIO:
                return resources.getString(R.string.audio);
            case Constants.FILE_TYPE_DOCUMENTS:
                return resources.getString(R.string.documents);
            case Constants.FILE_TYPE_PICTURES:
                return resources.getString(R.string.pictures);
            case Constants.FILE_TYPE_VIDEOS:
                return resources.getString(R.string.video);
            case Constants.FILE_TYPE_TORRENTS:
                return resources.getString(R.string.media_type_torrents);
            default:
                return resources.getString(R.string.unknown);
        }
    }

    /**
     * Opens the given file with the default Android activity for that File and
     * mime type.
     */
    public static void openFile(Context context, String filePath, String mime, boolean useFileProvider) {
        try {
            if (filePath != null) {
                Intent i = new Intent(Constants.MIME_TYPE_ANDROID_PACKAGE_ARCHIVE.equals(mime) ?
                        Intent.ACTION_INSTALL_PACKAGE : Intent.ACTION_VIEW);
                Uri fileUri = getFileUri(context, filePath, useFileProvider);
                LOG.info("openFile(...) -> fileUri=" + fileUri.toString(), true);
                i.setDataAndType(fileUri, Intent.normalizeMimeType(mime));
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_GRANT_READ_URI_PERMISSION);
                context.startActivity(i);
            }
        } catch (Throwable e) {
            UIUtils.showShortMessage(context, R.string.cant_open_file);
            LOG.error("Failed to open file: " + filePath, e);
        }
    }

    public static Uri getFileUri(Context context, String filePath, boolean useFileProvider) {
        return useFileProvider ?
                FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID + ".fileprovider", new File(filePath)) :
                Uri.fromFile(new File(filePath));
    }

    public static void openFile(Context context, File file) {
        openFile(context, file.getAbsolutePath(), getMimeType(file.getAbsolutePath()), true);
    }

    public static void openFile(Context context, File file, boolean useFileProvider) {
        openFile(context, file.getAbsolutePath(), getMimeType(file.getAbsolutePath()), useFileProvider);
    }

    public static void openURL(Context context, String url) {
        try {
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(url));
            context.startActivity(i);
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
            // ignore
            // yes, it happens
        }
    }

    public static String getMimeType(String filePath) {
        try {
            return MimeDetector.getMimeType(FilenameUtils.getExtension(filePath));
        } catch (Exception e) {
            LOG.error("Failed to read mime type for: " + filePath);
            return MimeDetector.UNKNOWN;
        }
    }

    /**
     * Checks setting to show or not the transfers window right after a download has started.
     * This should probably be moved elsewhere (similar to GUIMediator on the desktop)
     */
    public static void showTransfersOnDownloadStart(Context context) {
        if (ConfigurationManager.instance().showTransfersOnDownloadStart() && context != null) {
            Intent i = new Intent(context, MainActivity.class);
            i.setAction(ED2KService.ACTION_SHOW_TRANSFERS);
            i.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            context.startActivity(i);
        }
    }

    public static void showKeyboard(Context context, View view) {
        view.requestFocus();
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
    }

    public static void hideKeyboardFromActivity(Activity activity) {
        InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        View view = activity.getCurrentFocus();
        if (view == null) {
            view = new View(activity);
        }
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public static void goToFrostWireMainActivity(Activity activity) {
        final Intent intent = new Intent(activity, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        activity.startActivity(intent);
        activity.finish();
        activity.overridePendingTransition(0, 0);
    }

    /**
     *
     * @param context -  containing Context.
     * @param showInstallationCompleteSection - true if you want to display "Your installation is now complete. Thank You" section
     * @param dismissListener - what happens when the dialog is dismissed.
     * @param referrerContextSuffix - string appended at the end of social pages click urls's ?ref=_android_ parameter.
     */
    public static void showSocialLinksDialog(final Context context,
                                             boolean showInstallationCompleteSection,
                                             DialogInterface.OnDismissListener dismissListener,
                                             String referrerContextSuffix) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View customView = View.inflate(context, R.layout.view_social_buttons, null);
        builder.setView(customView);
        builder.setPositiveButton(context.getString(android.R.string.ok), new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        final AlertDialog socialLinksDialog = builder.create();
        socialLinksDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        socialLinksDialog.setOnDismissListener(dismissListener);

        ImageButton githubButton = (ImageButton) customView.findViewById(R.id.view_buttons_github);
        //ImageButton twitterButton = (ImageButton) customView.findViewById(R.id.view_social_buttons_twitter_button);
        //ImageButton redditButton = (ImageButton) customView.findViewById(R.id.view_social_buttons_reddit_button);


        //final String referrerParam  = "?ref=android_" + ((referrerContextSuffix!=null) ? referrerContextSuffix.trim() : "");

        githubButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UIUtils.openURL(v.getContext(), Constants.GITHUB_PAGE);
            }
        });
/*
        twitterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UIUtils.openURL(v.getContext(), Constants.SOCIAL_URL_TWITTER_PAGE + referrerParam);
            }
        });

        redditButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UIUtils.openURL(v.getContext(), Constants.SOCIAL_URL_REDDIT_PAGE + referrerParam);
            }
        });

*/
        if (showInstallationCompleteSection) {
            LinearLayout installationCompleteLayout =
                    (LinearLayout) customView.findViewById(R.id.view_social_buttons_installation_complete_layout);
            installationCompleteLayout.setVisibility(View.VISIBLE);
            ImageButton dismissCheckButton = (ImageButton) customView.findViewById(R.id.view_social_buttons_dismiss_check);
            dismissCheckButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    socialLinksDialog.dismiss();
                }
            });
        }

        socialLinksDialog.show();
    }

    // tried playing around with <T> but at the moment I only need ByteExtra's, no need to over enginner.
    public static class IntentByteExtra {
        public String name;
        public byte value;
        public IntentByteExtra(String name, byte value) {
            this.name = name;
            this.value = value;
        }
    }

    public static void broadcastAction(Context ctx, String actionCode, IntentByteExtra... extras) {
        if (ctx == null || actionCode == null) {
            return;
        }
        final Intent intent = new Intent(actionCode);
        if (extras != null && extras.length > 0) {
            for (IntentByteExtra extra : extras) {
                intent.putExtra(extra.name, extra.value);
            }
        }
        ctx.sendBroadcast(intent);
    }
}
