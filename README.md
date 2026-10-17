# Aile Bütçesi (Android)

Aile Bütçesi uygulamasının Android sürümü. Uygulamanın tamamı `app/src/main/assets/index.html` içindedir;
`MainActivity` onu tam ekran açar. Veriler yalnızca telefonda saklanır, uygulamanın internet izni yoktur.

- Her `main` gönderiminde GitHub Actions APK'yı derler ve `apk` dalına `Aile-Butcesi.apk` olarak koyar.
- `app/release.jks` uygulamanın imza anahtarıdır. Güncellemelerin eski sürümün üzerine kurulabilmesi için
  **değiştirilmemeli ve depo gizli (private) tutulmalıdır.**
- Güncelleme yaparken `app/build.gradle` içindeki `versionCode` bir artırılmalıdır.
