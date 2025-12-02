Spring Mock Exam (console)
==========================
Piccola app da terminale che ti fa un quiz di pratica su Spring. Non servono IDE o strumenti particolari: basta Java.

Requisiti
- Java 17 o superiore installato (controlla con `java -version`).

Scaricare il progetto
- Premi il bottone “Download ZIP” su GitHub (o clona se sai usare git).
- Decomprimi lo ZIP in una cartella a tua scelta.
- Apri il terminale e spostati in quella cartella.

Eseguire il quiz con le domande interne
1. Compila: `javac SpringMockExam.java`
2. Avvia: `java SpringMockExam`
3. Rispondi digitando le lettere (per multiple choice usa `A,C` oppure `AC`).

Caricare un set di domande da file (esempio: corso1)
1. Nel terminale, sempre nella cartella del progetto, compila se non l’hai già fatto: `javac SpringMockExam.java`
2. Avvia indicando il nome del test (senza estensione): `java SpringMockExam corso1`
   - Il programma cerca `tests/corso1.txt` e carica le domande.
   - Se il file non c’è o ha errori, userà le domande interne.

Formato dei file test (cartella `tests/`)
Ogni domanda è un blocco di righe con chiave e valore. Esempio minimale:
```
id:Q1
text:Domanda di esempio?
option:Risposta A
option:Risposta B
option:Risposta C
correct:A
answer:Spiega brevemente perché la risposta è corretta (opzionale).
```
Puoi indicare più risposte corrette con `correct:A,C`. Separa i blocchi con una riga vuota. Le righe che iniziano con `#` sono commenti.
