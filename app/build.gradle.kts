import java.util.regex.Pattern.compile

plugins {
    id("com.android.application")
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.appchatnutritien"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.appchatnutritien"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
        multiDexEnabled = true
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    buildFeatures{
        viewBinding = true
    }
}

dependencies {

    implementation("androidx.appcompat:appcompat:1.4.1")
    implementation("com.google.android.material:material:1.5.0")
    implementation("androidx.activity:activity:1.4.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.3")

    //paypal
    implementation("com.paypal.sdk:paypal-android-sdk:2.16.0")


    implementation("com.squareup.picasso:picasso:2.71828")


    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.3")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.4.0")

    implementation ("de.hdodenhof:circleimageview:3.1.0")

    implementation ("com.github.bumptech.glide:glide:4.12.0")

    annotationProcessor ("com.github.bumptech.glide:compiler:4.12.0")

    //firebase
    implementation(platform("com.google.firebase:firebase-bom:33.0.0"))
    implementation(libs.firebase.messaging)
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.database)
    implementation("com.google.firebase:firebase-auth:21.0.1")
    implementation ("com.google.firebase:firebase-database:20.0.0")
    implementation("com.google.firebase:firebase-firestore:24.11.1")
    implementation("com.google.firebase:firebase-auth:21.0.0")
    implementation("com.google.firebase:firebase-storage:21.0.0")

    //multidex
    implementation("androidx.multidex:multidex:2.0.1")

    //Scalable Size unit (support different screen size)
    implementation("com.intuit.sdp:sdp-android:1.0.6")
    //for text
    implementation("com.intuit.ssp:ssp-android:1.0.6")

    //Rounded ImageView
    implementation("com.makeramen:roundedimageview:2.3.0")


    //WebRTC
    implementation ("com.mesibo.api:webrtc:1.0.5")

    //gson
    implementation ("com.google.code.gson:gson:2.10.1")

    //permission
    implementation ("com.guolindev.permissionx:permissionx:1.6.1")
}