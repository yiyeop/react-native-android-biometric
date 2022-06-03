import React, {useEffect} from 'react';
import {Button, SafeAreaView, ScrollView, View} from 'react-native';
import AndroidBiometric from './util/RNAndroidBiometric';

const App = () => {
  useEffect(() => {
    AndroidBiometric.isSupported().then(res => console.log('isSupported', res));
  }, []);

  return (
    <SafeAreaView>
      <ScrollView contentInsetAdjustmentBehavior="automatic">
        <View style={{margin: 20}}>
          <Button
            title="Authenticate"
            onPress={async () =>
              await AndroidBiometric.authenticate().then(res =>
                console.log(res),
              )
            }
          />
        </View>
      </ScrollView>
    </SafeAreaView>
  );
};

export default App;
