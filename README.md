
# Fascicolo Sanitario 2.0

# _it-fse-srv-query_ 

Il microservizio in oggetto ha la responsabilità di comunicare con il Server FHIR ed esporre interfacce REST verso gli altri microservizi.

<br/>

## Repos
- [*it-fse-support*](https://github.com/ministero-salute/it-fse-support) - Pagina di supporto FSE 2.0
- [*it-fse-landing*](https://github.com/ministero-salute/it-fse-landing) - Landing page dei repository FSE 2.0

<br/>

## Test in isolamento

Con Java 21 e Maven, eseguire `mvn -PISOLATION clean test` per selezionare soltanto
i test JUnit con tag `ISOLATION`. Il comando predefinito `mvn test` li esclude e
mantiene i test esistenti.

La suite usa contesti Spring minimi, controller e servizio reali, parsing HAPI
FHIR reale e risorse FHIR reali. Solo il client HAPI remoto viene simulato: non
sono necessari server FHIR, database, container o credenziali esterne. Verifica
creazione e pseudonimizzazione del paziente, cancellazione delle sole risorse
mutabili, sostituzione, metadati, ricerca, propagazione del tenant, tracciamento,
risposte HTTP e percorsi di errore.

Il profilo genera `target/site/jacoco/index.html` e `target/site/jacoco/jacoco.xml`.

## License

[![License: AGPL v3](https://img.shields.io/badge/License-AGPL_v3-blue.svg)](https://www.gnu.org/licenses/agpl-3.0)
