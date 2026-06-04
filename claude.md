## Workflow Orchestration

### 1. Plan Mode Default
- Basit olmayan (3+ adımdan oluşan veya mimari karar gerektiren) HER görev için önce "Plan Modu"na geç.
- Eğer işler ters giderse, dur ve hemen yeniden planlama yap — körü körüne ilerlemeye devam etme.
- Plan modunu sadece bir şeyler inşa ederken değil, doğrulama (verification) adımları için de kullan.
- Belirsizliği azaltmak için en başta detaylı özellikleri (specs) yazılı olarak belirle.

### 2. Subagent Strategy
- Ana bağlam penceresini (context window) temiz tutmak için alt agent'ları (subagents) cömertçe kullan.
- Araştırma, keşif ve paralel analiz yüklerini alt agent'lara devret.
- Karmaşık problemler için alt agent'lar aracılığıyla işe daha fazla hesaplama gücü (compute) aktar.
- Odaklanmış bir yürütme için her alt agent'a sadece tek bir görev (one task) ver.

### 3. Self-Improvement Loop
- Kullanıcıdan gelen HER düzeltmeden sonra, `tasks/lessons.md` dosyasını bu hata örüntüsüyle güncelle.
- Aynı hatayı tekrar yapmanı engelleyecek kuralları kendi kendine yaz.
- Hata oranını düşürene kadar bu dersler üzerinde acımasızca tekrar et.
- İlgili projeye başlarken, oturumun en başında bu dersleri mutlaka gözden geçir.

### 4. Verification Before Done
- Bir görevin çalıştığını kanıtlamadan (ispat etmeden) asla "tamamlandı" olarak işaretleme.
- `main` dalı (branch) ile yaptığın değişiklikler arasındaki davranış farkını (diff) ilgili durumlarda karşılaştır.
- Kendine şu soruyu sor: "Kıdemli bir yazılım mimarı (Staff Engineer) bu çözümü onaylar mıydı?"
- Testleri çalıştır, logları kontrol et ve doğruluğunu somut olarak göster.

### 5. Demand Elegance (Balanced)
- Basit olmayan değişiklikler için: Duraksa ve "Bunu yapmanın daha zarif bir yolu var mı?" diye sor.
- Eğer yaptığın düzeltme geçici/yamama (hacky) hissettiriyorsa: "Şu an bildiğim her şeyle, en zarif çözümü nasıl uygulardım?" diyerek yaklaş.
- Basit ve bariz düzeltmeler için bu adımı atla — aşırı mühendislik (over-engineer) yapma.
- Ortaya koyduğun işi kullanıcıya sunmadan önce kendi içinde test et ve zorla.s

### 6. Autonomous Bug Fixing
- Bir hata raporu (bug report) aldığında, sadece çözüme odaklan. Kullanıcıdan elinden tutmasını (adım adım yönlendirmesini) isteme.
- Logları, hataları ve başarısız olan testleri doğrudan hedef al, ardından bunları çöz.
- Kullanıcının ekstra bağlam (context) sağlamasına gerek kalmadan sorunu kendi içinde anlamlandır.
- Sana söylenmesini beklemeden, CI (sürekli entegrasyon) süreçlerinde başarısız olan testleri git ve düzelt.

## Task Management

1. **Plan First:** Planı kontrol edilebilir maddeler halinde `tasks/todo.md` dosyasına yaz.
2. **Verify Plan:** Uygulamaya başlamadan önce planı kontrol et/doğrula.
3. **Track Progress:** İlerledikçe tamamlanan maddeleri işaretle.
4. **Explain Changes:** Her adımda yapılan değişikliklerin üst düzey (high-level) bir özetini çıkar.
5. **Document Results:** `tasks/todo.md` dosyasına bir gözden geçirme (review) bölümü ekle.
6. **Capture Lessons:** Düzeltmelerden sonra `tasks/lessons.md` dosyasını güncelle.

## Core Principles

- **Simplicity First:** Her değişikliği mümkün olduğunca basit tut. Minimum kod üzerinde etki yarat.
- **No Laziness:** Kök nedenleri (root causes) bul. Geçici çözümler/yamalar yapma. Kıdemli geliştirici standartlarını koru.
- **Minimal Impact:** Değişiklikler sadece yapılması zorunlu olan yerlere dokunmalıdır. Yeni hatalar (bugs) üretmekten kaçın.