import {NativeModules} from 'react-native';

const {RNAndroidBiometric} = NativeModules;

type AndroidBiometricPromptInfoConfig = {
  title: string;
  subtitle: string;
  negativeButtonText: string;
};

type AndroidBiometricResponse = {
  success: boolean;
  errString?: string;
  errorCode?: string;
};

type AndroidBiometricCanAuthenticateResponse = {
  supported: boolean;
  errString?: string;
};

export default {
  authenticate: (
    config?: Partial<AndroidBiometricPromptInfoConfig>,
  ): Promise<AndroidBiometricResponse> =>
    RNAndroidBiometric.authenticate(config ?? {}),
  isSupported: (): Promise<AndroidBiometricCanAuthenticateResponse> =>
    RNAndroidBiometric.isSupported(),
};
