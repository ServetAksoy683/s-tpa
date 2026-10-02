# S-Tpa (v1.1.0)

Paper (ve Folia-uyumlu) sunucular icin hizli isinlanma (teleport) plugini.

## Komutlar

| Komut | Aciklama | Yetki |
|---|---|---|
| `/stpa <oyuncu>` | Belirtilen oyuncuya isinlanma istegi gonderir | herkes |
| `/stpaccept` | Gelen istegi kabul eder | herkes |
| `/stpadisable` | Kendine gelen istekleri kapatir | herkes |
| `/stpaenable` | Kendine gelen istekleri tekrar acar | herkes |
| `/stpadmin <oyuncu>` | Onay beklemeden hedefi kendi yanina isinlar | sadece op / stpa.admin |
| `/stpafreeze` | Opler haric herkeste `/stpa` komutlarini durdurur | sadece op / stpa.admin |
| `/stpafree` | `/stpafreeze` durumunu iptal eder | sadece op / stpa.admin |
| `/stpareload` | Sunucuyu yeniden baslatmadan config.yml'i yeniler | sadece op / stpa.admin |

## v1.1.0'da yeni neler var

- **Renkli ve tiklanabilir mesajlar**: Tum mesajlar artik MiniMessage formatinda
  (`<green>`, `<gradient:#3ddc84:#00b894>`, `<bold>` vb.), `config.yml` icinden
  tamamen ozellestirilebilir. Gelen istek mesajindaki **[KABUL ET]** yazisina
  tiklamak dogrudan `/stpaccept` komutunu calistirir.
- **Ses efektleri**: Istek gonderme, istek alma, isinlanma, hata, warmup vb. her
  olay icin `config.yml` -> `sounds` bolumunden ayri ses atanabilir, ya da
  `sound: ""` yaparak kapatilabilir.
- **Cooldown**: `cooldown-seconds` ile bir oyuncunun art arda `/stpa` gondermesi
  arasinda bekleme suresi konabilir (varsayilan: 5 saniye).
- **Opsiyonel isinlanma bekleme suresi (warmup)**: `teleport-warmup-seconds` 0
  birakilirsa (varsayilan) hala hicbir bekleme olmadan aninda isinlanir. 0'dan
  buyuk bir deger verilirse, oyuncu o kadar saniye yerinde beklemek zorunda
  kalir; hareket eder ya da hasar alirsa isinlanma iptal olur.
- **Coklu es zamanli istek destegi**: `allow-multiple-requests: true` yapilirsa
  bir oyuncuya ayni anda birden fazla istek birikebilir; `/stpaccept` her
  seferinde en son gelen istegi kabul eder ve kac istek daha kaldigini bildirir.
  Varsayilan `false` (eskisi gibi, yeni istek eskisinin yerine gecer).
- **PlaceholderAPI destegi**: PlaceholderAPI kuruluysa otomatik algilanir.
  Kullanilabilir placeholder'lar: `%stpa_disabled%`, `%stpa_frozen%`, `%stpa_pending%`.
- **Folia uyumluluga temel adim**: Tum isinlanmalar `teleportAsync` ile yapilir,
  bu da Paper'in hem normal hem Folia sunucularda dogru calisan resmi yontemidir.
  (Not: tam bolge-bazli Folia optimizasyonu ileride ayrica ele alinabilir.)
- **Gercek Turkce ve Rusca karakterler**: Turkce mesajlarda `ı, ş, ğ, ü, ö, ç`
  harfleri, Rusca mesajlarda ise gercek Kiril alfabesi kullanildi.

## Dil ayari

`config.yml` icindeki `language` degerini `turkish`, `russia` veya `english`
yaparak tum mesajlarin dilini degistirebilirsin. Degistirdikten sonra
`/stpareload` yazman yeterli, sunucuyu yeniden baslatmana gerek yok.

## Derleme (JAR olusturma)

Bu klasor tam bir Maven projesidir. JAR dosyasi almak icin:

1. [Java 17+](https://adoptium.net/) ve [Maven](https://maven.apache.org/download.cgi) kur
   (veya Termux'ta `pkg install openjdk-21 maven`).
2. Terminalde bu klasore gir: `cd s-tpa`
3. Su komutu calistir: `mvn clean package`
4. Olusan JAR dosyasi `target/S-Tpa-1.1.0.jar` yolunda olacak.
5. JAR dosyasini sunucunun `plugins` klasorune atip sunucuyu baslat/reload et.

GitHub Actions ile otomatik derleme icin repo'daki `.github/workflows/build.yml`
dosyasi zaten ayarli — degisiklikleri `git push` ettiginde Actions sekmesinden
JAR'i indirebilirsin.

## Notlar

- `/stpadisable` ve `/stpafreeze` durumlari `plugins/S-Tpa/data.yml` dosyasinda
  saklanir, sunucu yeniden baslasa bile kaybolmaz.
- Isinlanma istekleri `request-expire-seconds` kadar sure sonra kendiliginden
  gecersiz olur (varsayilan: 60 saniye, 0 = suresiz).
- PlaceholderAPI yuklu degilse hicbir hata vermez, sadece o destek pasif kalir.
