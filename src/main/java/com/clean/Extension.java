package com.clean;

import burp.api.montoya.BurpExtension;
import burp.api.montoya.MontoyaApi;
import com.clean.editor.RequestEditorProvider;
import com.clean.editor.ResponseEditorProvider;
import com.clean.ui.ThemeManager;

public class Extension implements BurpExtension {
    @Override
    public void initialize(MontoyaApi montoyaApi) {
        montoyaApi.extension().setName("Clean");

        ThemeManager.get().load(montoyaApi);

        montoyaApi.userInterface().registerHttpRequestEditorProvider(new RequestEditorProvider(montoyaApi));
        montoyaApi.userInterface().registerHttpResponseEditorProvider(new ResponseEditorProvider(montoyaApi));
    }
}
