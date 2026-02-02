package com.example.chineseculture;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;

public class AuthUi {

    private static final String PREFS_NAME = "auth_prefs";
    private static final String KEY_CURRENT_USER = "current_user";

    public static void bind(Activity activity, View root, Runnable onLoginSuccess) {
        EditText account = root.findViewById(R.id.loginAccount);
        EditText password = root.findViewById(R.id.loginPassword);
        View loginButton = root.findViewById(R.id.loginButton);
        View registerButton = root.findViewById(R.id.registerButton);
        View forgotButton = root.findViewById(R.id.forgotButton);
        UserDbHelper dbHelper = new UserDbHelper(activity);

        if (loginButton != null) {
            loginButton.setOnClickListener(v -> {
                String username = safeText(account);
                String pwd = safeText(password);
                if (TextUtils.isEmpty(username) || TextUtils.isEmpty(pwd)) {
                    Toast.makeText(activity, R.string.login_toast_empty, Toast.LENGTH_SHORT).show();
                    return;
                }
                if (dbHelper.validateLogin(username, pwd)) {
                    saveCurrentUser(activity, username);
                    onLoginSuccess.run();
                } else {
                    Toast.makeText(activity, R.string.login_toast_login_failed, Toast.LENGTH_SHORT).show();
                }
            });
        }

        if (registerButton != null) {
            registerButton.setOnClickListener(v -> showAuthDialog(activity,
                    R.string.login_dialog_register_title,
                    R.string.login_dialog_password_hint,
                    R.string.login_dialog_confirm,
                    (username, pwd) -> {
                        if (TextUtils.isEmpty(username) || TextUtils.isEmpty(pwd)) {
                            Toast.makeText(activity, R.string.login_toast_empty, Toast.LENGTH_SHORT).show();
                            return false;
                        }
                        if (!isValidPhone(username)) {
                            Toast.makeText(activity, R.string.login_toast_phone_invalid, Toast.LENGTH_SHORT).show();
                            return false;
                        }
                        if (dbHelper.userExists(username)) {
                            Toast.makeText(activity, R.string.login_toast_register_exists, Toast.LENGTH_SHORT).show();
                            return false;
                        }
                        if (dbHelper.registerUser(username, pwd)) {
                            Toast.makeText(activity, R.string.login_toast_register_ok, Toast.LENGTH_SHORT).show();
                            return true;
                        }
                        return false;
                    }));
        }

        if (forgotButton != null) {
            forgotButton.setOnClickListener(v -> showAuthDialog(activity,
                    R.string.login_dialog_reset_title,
                    R.string.login_dialog_new_password_hint,
                    R.string.login_dialog_confirm,
                    (username, pwd) -> {
                        if (TextUtils.isEmpty(username) || TextUtils.isEmpty(pwd)) {
                            Toast.makeText(activity, R.string.login_toast_empty, Toast.LENGTH_SHORT).show();
                            return false;
                        }
                        if (!isValidPhone(username)) {
                            Toast.makeText(activity, R.string.login_toast_phone_invalid, Toast.LENGTH_SHORT).show();
                            return false;
                        }
                        if (dbHelper.updatePassword(username, pwd)) {
                            Toast.makeText(activity, R.string.login_toast_reset_ok, Toast.LENGTH_SHORT).show();
                            return true;
                        } else {
                            Toast.makeText(activity, R.string.login_toast_reset_failed, Toast.LENGTH_SHORT).show();
                            return false;
                        }
                    }));
        }
    }

    private static String safeText(EditText editText) {
        if (editText == null || editText.getText() == null) {
            return "";
        }
        return editText.getText().toString().trim();
    }

    private static boolean isValidPhone(String phone) {
        if (TextUtils.isEmpty(phone)) {
            return false;
        }
        return phone.matches("^1[3-9]\\d{9}$");
    }

    public static void saveCurrentUser(Context context, String username) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .putString(KEY_CURRENT_USER, username)
                .apply();
    }

    public static String getCurrentUser(Context context) {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .getString(KEY_CURRENT_USER, "");
    }

    private interface AuthSubmit {
        boolean onSubmit(String username, String password);
    }

    private static void showAuthDialog(Activity activity, int titleRes, int passwordHintRes,
                                       int confirmTextRes, AuthSubmit submit) {
        Dialog dialog = new Dialog(activity);
        dialog.setContentView(R.layout.dialog_auth);
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(true);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.getWindow().setLayout(
                    android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                    android.view.ViewGroup.LayoutParams.WRAP_CONTENT
            );
        }

        TextView title = dialog.findViewById(R.id.authDialogTitle);
        EditText account = dialog.findViewById(R.id.authDialogAccount);
        EditText password = dialog.findViewById(R.id.authDialogPassword);
        MaterialButton cancel = dialog.findViewById(R.id.authDialogCancel);
        MaterialButton confirm = dialog.findViewById(R.id.authDialogConfirm);

        title.setText(titleRes);
        password.setHint(passwordHintRes);
        confirm.setText(confirmTextRes);

        cancel.setOnClickListener(v -> dialog.dismiss());
        confirm.setOnClickListener(v -> {
            boolean ok = submit.onSubmit(safeText(account), safeText(password));
            if (ok) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }
}
