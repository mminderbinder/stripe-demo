package com.example.javastripeapp;

import android.app.Application;

import com.stripe.android.PaymentConfiguration;

public class JavaStripeApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        PaymentConfiguration.init(
                getApplicationContext(),
                "pk_test_51Re1vfQJ77bnOy20ZTm4jpnWNdJChifZx3Jbajrwl2eyUkBLoUJ4FvEgh0xilL24NemKZDz6l5oeT4MaxU9Yv4iR00QAu4SVZe"
        );
    }
}
