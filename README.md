![Logo](https://www.browserstack.com/images/static/header-logo.jpg)
# ProxyAware App Examples

## Intrduction
Most mobile apps make HTTP/HTTPS-based API calls to various servers (internal or over the internet) to send and receive data. In order to make these API calls the apps to need to configure an HTTP Client. An HttpClient can be used to send requests and retrieve their responses. There are multiple client libraries used for both Android and iOS like HTTPUrlConnection, Dio, Volley, LoopJ, HTTPClientHandler, NSURLConnections, CFNetwork, etc.

## [Android Project](AndroidApp)

- Build Android Project using
``` cd AndroidApp && ./gradlew assembleDebug ```
- Upload the [DebugApp](AndroidApp/app/build/outputs/apk/debug/app-debug.apk) which was build on BrowserStack [AppLive](https://app-live.browserstack.com)
- Test the App with Local Testing (Force Local Enabled) and see if requests are going through Local by visiting http://localhost:45454

## [iOS Project](iOSApp)

- Create Archive using ```cd iOSApp && xcodebuild archive -scheme <scheme-that-you-want-to-use> -sdk iphoneos -allowProvisioningUpdates -archivePath <path-and-name-for-archive>.xcarchive```
- Export the Archive ```xcodebuild -exportArchive -archivePath <path-and-name-for-archive-from-the-previous-step>.xcarchive -exportOptionsPlist exportOptions.plist -exportPath <path-where-ipa-file-will-be-saved>```
- Upload the generated `ipa` file on BrowserStack [AppLive](https://app-live.browserstack.com)
- Test the App with Local Testing (Force Local Enabled) and see if requests are going through Local by visiting http://localhost:45454
