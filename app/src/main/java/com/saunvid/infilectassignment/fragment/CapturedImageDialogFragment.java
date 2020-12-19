package com.saunvid.infilectassignment.fragment;


import android.app.Dialog;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.view.ViewCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.saunvid.infilectassignment.listener.OnCapturedImageListener;
import com.saunvid.infilectassignment.R;
import com.saunvid.infilectassignment.helper.Util;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;


/**
 * A simple {@link Fragment} subclass.
 */
public class CapturedImageDialogFragment extends DialogFragment {
    public static final String EXTRA_CAPTURE_IMAGE_LOCATION_PATH = "CAPTURE_IMAGE_LOCATION_PATH";
    private ImageView capturedImageView;
    private FloatingActionButton retryFab, doneFab;
    private String filePath;
    private OnCapturedImageListener onCapturedImageListener;
    private CoordinatorLayout parentLayout;

    public CapturedImageDialogFragment() {
        // Required empty public constructor
    }

    public void setOnCapturedImageListener(OnCapturedImageListener onCapturedImageListener) {
        this.onCapturedImageListener = onCapturedImageListener;
    }

    public static CapturedImageDialogFragment createCapturedImageDialog(String fileLocation) {
        CapturedImageDialogFragment capturedImageDialogFragment = new CapturedImageDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putString(EXTRA_CAPTURE_IMAGE_LOCATION_PATH, fileLocation);
        capturedImageDialogFragment.setArguments(bundle);
        return capturedImageDialogFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.AppTheme_FullScreenDialog_TranslucentSystemBars);
        if (getArguments() != null) {
            filePath = getArguments().getString(EXTRA_CAPTURE_IMAGE_LOCATION_PATH);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            dialog.getWindow().setLayout(MATCH_PARENT, MATCH_PARENT);
            dialog.getWindow().setWindowAnimations(R.style.AppTheme_DialogFadeAnimation);
            dialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
                @Override
                public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                    if (keyCode == KeyEvent.KEYCODE_BACK) {
                        if (onCapturedImageListener != null) {
                            //delete the file when user backpress
                            Util.deleteFile(filePath);
                            dismiss();
                            onCapturedImageListener.onRetryClick();
                        }
                    }
                    return true;
                }
            });
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_captured_image_dialog, null, false);
        initView(view);
        return view;
    }

    private void initView(View view) {
        capturedImageView = view.findViewById(R.id.imgView_captured_image);
        doneFab = view.findViewById(R.id.fab_done_capturing);
        retryFab = view.findViewById(R.id.fab_retry_capturing);
        parentLayout = view.findViewById(R.id.coordinatorLayout);
        setUpListener();
        loadImageIntoImageView();
    }

    private void loadImageIntoImageView() {
        if (getActivity() != null) {
            Glide.with(getActivity()).load(filePath).into(capturedImageView);
        } else {
            capturedImageView.setImageURI(Uri.parse(filePath));
        }
    }

    private void setUpListener() {
        //inset from bottom to avoid overlapping with system bars
        ViewCompat.setOnApplyWindowInsetsListener(parentLayout, (v, insets) -> {
            if (getContext() != null) {
                //inset from top
                ViewGroup.MarginLayoutParams retryFabLayoutParams = (ViewGroup.MarginLayoutParams) retryFab.getLayoutParams();
                retryFabLayoutParams.bottomMargin = insets.getSystemWindowInsetBottom() + Util.getValueInDP(getContext(), 16);

                //inset from bottom
                ViewGroup.MarginLayoutParams doneFabLayoutParams = (ViewGroup.MarginLayoutParams) doneFab.getLayoutParams();
                doneFabLayoutParams.bottomMargin = insets.getSystemWindowInsetBottom() + Util.getValueInDP(getContext(), 16);
            }
            return insets.consumeSystemWindowInsets();
        });
        doneFab.setOnClickListener(view -> {
            if (onCapturedImageListener != null) {
                dismiss();
                onCapturedImageListener.onSaveClick(filePath);
            }
        });
        retryFab.setOnClickListener(view -> {
            if (onCapturedImageListener != null) {
                //delete the file when user click on retry fab
                Util.deleteFile(filePath);
                dismiss();
                onCapturedImageListener.onRetryClick();
            }
        });
    }


}
