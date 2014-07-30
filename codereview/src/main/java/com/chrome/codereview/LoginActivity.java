package com.chrome.codereview;

import android.app.Activity;
import android.app.LoaderManager;
import android.app.ProgressDialog;
import android.content.AsyncTaskLoader;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.chrome.codereview.requests.ServerCaller;

public class LoginActivity extends Activity implements LoaderManager.LoaderCallbacks<LoginActivity.Result> {

    private static final int ADD_GOOGLE_ACCOUNT = 1;
    private static final int GOOGLE_ACCOUNT_PERMISSION = 2;

    private static enum State {
        AUTH_FAILED,
        PERMISSION_REQUEST,
        OK,
    }

    static class Result {

        private State state;
        private Intent intent;

        Result(State state, Intent intent) {
            this.state = state;
            this.intent = intent;
        }

        Result(State state) {
            this(state, null);
        }
    }

    private static class AuthenticateAsyncLoader extends AsyncTaskLoader<Result> {

        public AuthenticateAsyncLoader(Context context) {
            super(context);
        }

        @Override
        public Result loadInBackground() {

            try {
                ServerCaller.from(getContext()).tryToAuthenticate();
                return new Result(State.OK);
            } catch (UserRecoverableAuthException e) {
                Intent intent = e.getIntent();
                return new Result(State.PERMISSION_REQUEST, intent);
            } catch (Exception e) {
                e.printStackTrace();
                return new Result(State.AUTH_FAILED);
            }
        }
    }

    private ProgressDialog progressDialog;
    private ServerCaller serverCaller;

    @Override
    public Loader<Result> onCreateLoader(int i, Bundle bundle) {
        return new AuthenticateAsyncLoader(this);
    }

    @Override
    public void onLoadFinished(Loader<Result> resultLoader, Result result) {
        progressDialog.dismiss();
        progressDialog = null;
        switch (result.state) {
            case PERMISSION_REQUEST:
                startActivityForResult(result.intent, GOOGLE_ACCOUNT_PERMISSION);
                break;
            case AUTH_FAILED:
                Toast.makeText(this, R.string.toast_auth_failed, Toast.LENGTH_LONG).show();
                break;
            case OK:
                setResult(RESULT_OK);
                finish();
                break;
        }
    }

    @Override
    public void onLoaderReset(Loader<Result> resultLoader) {

    }

    public void onLoginClick(View view) {
        addGoogleAccount();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        serverCaller = ServerCaller.from(this);
        setContentView(R.layout.activity_code_review_login);
        if (serverCaller.getState() == ServerCaller.State.NEEDS_AUTHORIZATION) {
            authenticate();
            return;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case ADD_GOOGLE_ACCOUNT:
                serverCaller.reset();
                if (serverCaller.getState() == ServerCaller.State.NEEDS_AUTHORIZATION) {
                    authenticate();
                }
                break;
            case GOOGLE_ACCOUNT_PERMISSION:
                //FIXME: support disagreement
                authenticate();
                break;
        }
    }

    private void addGoogleAccount() {
        Intent intent = new Intent(Settings.ACTION_ADD_ACCOUNT);
        intent.putExtra(Settings.EXTRA_ACCOUNT_TYPES, new String[]{GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE});
        startActivityForResult(intent, ADD_GOOGLE_ACCOUNT);
    }

    private void authenticate() {
        getLoaderManager().restartLoader(0, new Bundle(), this).forceLoad();
        progressDialog = new ProgressDialog(this);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage(getString(R.string.trying_to_authenticate, serverCaller.getAccountName()));
        progressDialog.show();
    }

}
