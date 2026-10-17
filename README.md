# Aile Bütçesi (Android)

Aile Bütçesi uygulamasının Android sürümü. Uygulamanın tamamı `app/src/main/assets/index.html` içindedir;
`MainActivity` onu tam ekran açar. Veriler yalnızca telefonda saklanır, uygulamanın internet izni yoktur.

- Her `main` gönderiminde GitHub Actions imzasız APK'yı derler ve `apk` dalına koyar.
- İmza anahtarı bu depoda **tutulmaz**; APK depo dışında imzalanır. Güncellemelerin eski sürümün üzerine
  kurulabilmesi için hep aynı anahtar kullanılmalıdır.
- Güncelleme yaparken `app/build.gradle` içindeki `versionCode` bir artırılmalıdır.
