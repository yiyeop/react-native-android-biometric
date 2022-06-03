package com.rnandroidbiometric

import android.os.Build
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.fragment.app.FragmentActivity
import com.facebook.react.bridge.*

import com.facebook.react.bridge.UiThreadUtil.runOnUiThread
import java.util.concurrent.Executors

class PromptCallback(promise: Promise): BiometricPrompt.AuthenticationCallback() {
    var promise: Promise = promise

    override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
        super.onAuthenticationSucceeded(result)

        val resultMap: WritableMap = WritableNativeMap()
        resultMap.putBoolean("success", true)
        promise.resolve(resultMap)
    }

    override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
        super.onAuthenticationError(errorCode, errString)

        val resultMap: WritableMap = WritableNativeMap()
        resultMap.putBoolean("success", false)

        if(errorCode == BiometricPrompt.ERROR_NEGATIVE_BUTTON || errorCode == BiometricPrompt.ERROR_USER_CANCELED){
            resultMap.putString("errString", "사용자 취소")
        } else {
            resultMap.putString("errString", errString.toString())
        }
        resultMap.putString("errorCode", errorCode.toString())
        promise.resolve(resultMap)
    }
}


class RNBiometricModule(reactContext: ReactApplicationContext): ReactContextBaseJavaModule(reactContext) {
    override fun getName(): String {
        return "RNAndroidBiometric"
    }

    @ReactMethod
    fun authenticate (params: ReadableMap,promise: Promise) {
        runOnUiThread {
            runCatching {
                val fragmentActivity = currentActivity as FragmentActivity?
                val executor = Executors.newSingleThreadExecutor()
                val promptCallback = PromptCallback(promise)
                val biometricPrompt = BiometricPrompt(fragmentActivity!!, executor, promptCallback)

                val promptInfo = BiometricPrompt.PromptInfo.Builder()
                    .setTitle(params.getString("title") ?: "지문 인증")
                    .setSubtitle(params.getString("subtitle") ?: "기기에 등록된 지문을 이용하여 지문을 인증해주세요.")
                    .setNegativeButtonText(params.getString("negativeButtonText") ?: "취소")

                //  생체 인식 외 (패턴등) 사용 시
//                promptInfo.setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL)

                biometricPrompt.authenticate(promptInfo.build())
            }.onFailure { exception -> exception.printStackTrace() }
        }
    }

    @ReactMethod
    fun isSupported(promise: Promise) {
        val resultMap: WritableMap = WritableNativeMap()
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            resultMap.putBoolean("supported", false)
            resultMap.putString("errString", "UNSUPPORTED_VERSION")
            promise.resolve(resultMap)
            return
        }

        val biometricManager = BiometricManager.from(reactApplicationContext)

        val canAuthenticate = biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK)

        if(canAuthenticate == BiometricManager.BIOMETRIC_SUCCESS) {
            resultMap.putBoolean("supported", true)
            promise.resolve(resultMap)
        } else {
            resultMap.putBoolean("supported", false)
            when (canAuthenticate) {
                BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE ->
                    resultMap.putString("errString", "BIOMETRIC_ERROR_NO_HARDWARE")
                BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE ->
                    resultMap.putString("errString", "BIOMETRIC_ERROR_HW_UNAVAILABLE")
                BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED ->
                    resultMap.putString("errString", "BIOMETRIC_ERROR_NONE_ENROLLED")
            }
             promise.resolve(resultMap)
        }
    }
}